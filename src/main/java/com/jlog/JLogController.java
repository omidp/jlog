/*
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.jlog;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jlog.elasticsearch.StorageComponent;
import com.jlog.grok.LogValueHolder;
import com.jlog.io.FileReaderFactory;
import com.jlog.io.FileReaderFactory.LogReaderType;
import com.jlog.io.LogReader;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Omid 
 *
 */
@RestController
@RequestMapping("/api/v1/jlog")
@Slf4j
public class JLogController
{

    StorageComponent storageComponent;
    JLogConfiguration conf;

    @Autowired
    public JLogController(StorageComponent storageComponent, JLogConfiguration conf)
    {
        this.storageComponent = storageComponent;
        this.conf = conf;
    }

    @GetMapping("")
    public ResponseEntity<String> get(@RequestParam("readerType") Optional<LogReaderType> readerType, HttpServletRequest request)
    {
        log.info("try to connect to elastice search");
        if(storageComponent.elasticSearchIsRunning() == false)
            return ResponseEntity.ok("elasticsearch is not running");
        boolean mappingExists = storageComponent.mappingExists();
        if (mappingExists == false)
            storageComponent.buildMapping();
        LogReader logReader = FileReaderFactory.getLogReader(readerType.orElse(LogReaderType.LOCAL));
        logReader.open();
        List<LogValueHolder> resultList = new ArrayList<LogValueHolder>();
        log.info("READING FILES FROM {} DIRECTORY", conf.getLogDirectory());
        logReader.readLogFiles(conf.getLogDirectory(), (line) -> {
            com.jlog.grok.GrokCompiler.getInstance().parseJavaException(com.jlog.grok.GrokCompiler.CAUSED_PATTERN, line, resultList::add);
            com.jlog.grok.GrokCompiler.getInstance().parseJavaException(com.jlog.grok.GrokCompiler.STACKTRACE_PATTERN, line,
                    resultList::add);
        }, (fp) -> {
            log.info("log file {} is finished processing", fp);
            List<LogValueHolder> logs = resultList.stream().map(m -> {
                m.setPath(fp);
                m.setHost(getRealIp(request));
                return m;
            }).collect(Collectors.toList());
            storageComponent.bulkInsert(logs);
            resultList.clear();
        });
        log.info("success");
        return ResponseEntity.ok("success");
    }

    @GetMapping("/search")
    public ResponseEntity<String> search()
    {
        return storageComponent.search();
    }
    
   

    public String getRealIp(HttpServletRequest req)
    {
        if (req == null)
            return "127.0.0.1";
        String ip = req.getHeader("X-Real-IP");
        if (ip == null)
            ip = req.getHeader("X-Forwarded-For");
        if (ip == null)
            ip = req.getRemoteAddr();
        if (ip == null)
            ip = "127.0.0.1";
        return ip;
    }

}

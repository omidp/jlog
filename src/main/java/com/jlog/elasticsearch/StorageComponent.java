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
package com.jlog.elasticsearch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import com.jlog.JLogConfiguration;
import com.jlog.grok.LogValueHolder;
import com.jlog.internal.ContentParser;
import com.jlog.internal.RestTemplate;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Omid 
 *
 */
@Component
@Slf4j
public class StorageComponent
{

    
    public static final String NL = System.getProperty("line.separator");
    
    RestTemplate restTemplate;
    ContentParser contentParser;
    JLogConfiguration conf;

    @Autowired
    public StorageComponent(RestTemplate restTemplate, ContentParser contentParser, JLogConfiguration conf)
    {
        this.restTemplate = restTemplate;
        this.contentParser = contentParser;
        this.conf = conf;
    }

    public boolean mappingExists()
    {
        String uri = url() + "/jlog/_mapping";
        try
        {
            ResponseEntity<String> rsp = restTemplate.get(uri, Optional.empty());
            if (rsp.getStatusCodeValue() == 200)
                return true;
        }
        catch (HttpClientErrorException ignore)
        {
            //DO NOTHING
        }
        return false;
    }

    public void buildMapping()
    {
        String uri = url() + "/jlog";
        String mp = contentParser.processTemplate("mapping.json", new HashMap<>());
        ResponseEntity<String> rsp = restTemplate.put(uri, mp);
    }
    
    public ResponseEntity<String> bulkInsert(List<LogValueHolder> logs)
    {
        String uri = url() + "/_bulk?pretty";
        StringBuilder sb = new StringBuilder();
        for (LogValueHolder item : logs)
        {
            Map<String, Object> model = new HashMap<String, Object>();
            model.put("id", UUID.randomUUID().toString());
            model.put("line", item.getLine());
            model.put("clz", item.getClz());
            model.put("method", item.getMethod());
            model.put("file", item.getFile());
            model.put("message",item.getMessage());
            model.put("path",item.getPath());
            model.put("servicename",item.getServicename());
            model.put("host",item.getHost());
            String content = contentParser.processTemplate("bulk.json", model);
            sb.append(content).append(NL);
        }        
        ResponseEntity<String> rsp = restTemplate.put(uri, sb.toString());
        return rsp;
    }
    
    
    
    public ResponseEntity<String> search()
    {
        String uri = url() + "/jlog/_search?pretty";
        ResponseEntity<String> rsp = restTemplate.get(uri, Optional.empty());
        return rsp;
    }

    
    
    public boolean elasticSearchIsRunning()
    {
        String uri = url();        
        restTemplate.getRequestFactory().timeout3seconds();
        try
        {
            ResponseEntity<String> rsp = restTemplate.get(uri, Optional.empty());
            if(rsp.getStatusCodeValue() == 200)
                return true;
        }
        catch (ResourceAccessException e)
        {
        }
        return false;
    }
    

    private String url()
    {
        StringBuilder url = new StringBuilder("http://").append(conf.getElasticIp()).append(":").append(conf.getElasticPort());
        return url.toString();
    }

}

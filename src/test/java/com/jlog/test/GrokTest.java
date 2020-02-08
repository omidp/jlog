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
package com.jlog.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.util.Assert;

import com.jlog.io.JLogFile;

import io.krakens.grok.api.Grok;
import io.krakens.grok.api.GrokCompiler;
import io.krakens.grok.api.Match;

/**
 * @author Omid 
 *
 */
public class GrokTest
{

    
    @Test
    public void test() throws IOException
    {
        InputStream is = getClass().getResourceAsStream("sampleEx.txt");
        byte[] byteArray = IOUtils.toByteArray(is); 
        JLogFile.readLines(byteArray, (line)->{
            com.jlog.grok.GrokCompiler.getInstance().parseJavaException(com.jlog.grok.GrokCompiler.CAUSED_PATTERN, line, (vo)->{
                Assert.notNull(vo.getMessage());
            });
            com.jlog.grok.GrokCompiler.getInstance().parseJavaException(com.jlog.grok.GrokCompiler.STACKTRACE_PATTERN, line, (vo)->{
                Assert.notNull(vo.getMessage());
            });
        });
        
    }
    
}

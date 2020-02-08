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

import java.util.HashMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit4.SpringRunner;

import com.jlog.JLogConfiguration;
import com.jlog.internal.ContentParser;
import com.jlog.internal.ContentParserImpl;

/**
 * @author Omid 
 *
 */
@RunWith(SpringRunner.class)
@Import(JLogConfiguration.class)
public class TemplateParserTest
{
    
    
    @Autowired
    ContentParser cp;
    
    @Test
    public void testMapping()
    {
        String processTemplate = cp.processTemplate("mapping.json", new HashMap<>());
//        
    }
    
    
    @Test
    public void testInsertMapping()
    {
        HashMap<String, Object> model = new HashMap<>();
        model.put("id", "123");
        model.put("line", "123");
        model.put("clz", "Hello");
        model.put("method", "md");
        model.put("file", "Hello.java");
        model.put("message", "error");
        model.put("servicename", "");
        model.put("path", "/opt/logs/1.log");
        model.put("host", "127.0.0.1");
        String processTemplate = cp.processTemplate("bulk.json", model);
        
    }

    @TestConfiguration
    static class Config
    {

        @Bean
        ContentParser parser()
        {
            return new ContentParserImpl();
        }

        @Bean
        public static PropertySourcesPlaceholderConfigurer propertyConfigInDev()
        {
            PropertySourcesPlaceholderConfigurer ps = new PropertySourcesPlaceholderConfigurer();
            ps.setLocations(new ClassPathResource("jlog.properties"));
            ps.setIgnoreResourceNotFound(true);
            ps.setIgnoreUnresolvablePlaceholders(true);
            return ps;
        }
    }

}

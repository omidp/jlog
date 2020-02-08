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
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit4.SpringRunner;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.jlog.JLogConfiguration;
import com.jlog.io.FileReaderFactory;
import com.jlog.io.FileReaderFactory.LogReaderType;
import com.jlog.io.LocalLogReader;
import com.jlog.io.LogReader;
import com.jlog.io.RemoteLogReader;

/**
 * @author Omid 
 *
 */
@RunWith(SpringRunner.class)
@Import(JLogConfiguration.class)
public class FileReaderTest
{

    @Test
//    @Ignore
    public void testReadeFactory() throws JSchException, IOException
    {
        LogReader logReader = FileReaderFactory.getLogReader(LogReaderType.LOCAL);
        LogReader logReader2 = FileReaderFactory.getLogReader(LogReaderType.REMOTE);
        assert logReader instanceof LocalLogReader;
        assert logReader2 instanceof RemoteLogReader;
    }
    
    
    
    
    
    @TestConfiguration
    static class Config
    {
        @Bean
        public RemoteLogReader remoteLogReader(JLogConfiguration conf)
        {
            return new RemoteLogReader(conf.getIp(), conf.getPort(), conf.getUsername(), conf.getPassword(), conf.getTimeout());
        }
        
        
        @Bean 
        public FileReaderFactory fileReaderFactory()
        {
            return new FileReaderFactory();
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

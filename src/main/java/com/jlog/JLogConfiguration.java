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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Omid 
 *
 */
@ConfigurationProperties(prefix = "jlog")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JLogConfiguration
{

    @Value("${jlog.ip}")
    private String ip;
    
    @Value("${jlog.port}")
    private int port;
    
    @Value("${jlog.username}")
    private String username;
    
    @Value("${jlog.password}")
    private String password;
    
    @Value("${jlog.timeout}")
    private int timeout;
    
    @Value("${jlog.elastic.ip:localhost}")
    private String elasticIp;
    
    
    @Value("${jlog.elastic.port:9200}")
    private int elasticPort;
    
    
    @Value("${jlog.logdir}")
    private String logDirectory;
    
    
    
}
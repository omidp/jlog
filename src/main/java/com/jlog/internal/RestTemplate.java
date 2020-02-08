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
package com.jlog.internal;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Omid 
 *
 */
@Component
@Slf4j
public class RestTemplate
{

    private org.springframework.web.client.RestTemplate restTemplate;

    @Autowired
    public RestTemplate(org.springframework.web.client.RestTemplate restTemplate)
    {
        this.restTemplate = restTemplate;        
    }

    public ResponseEntity<String> get(String url, Optional<String> body)
    {
        log.info("sending GET request to {}", url);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        HttpEntity<String> entity = new HttpEntity<String>(headers);
        if (body.isPresent())
            entity = new HttpEntity<String>(body.get(), headers);        
        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        return exchange;
    }
    
    public JLogBufferingClientHttpRequestFactory getRequestFactory()
    {
        return (JLogBufferingClientHttpRequestFactory) restTemplate.getRequestFactory();
    }

    public ResponseEntity<String> post(String url, String body)
    {
        log.info("sending POST request to {}", url);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        HttpEntity<String> entity = new HttpEntity<String>(body, headers);
        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        return exchange;
    }

    public ResponseEntity<String> put(String url, String body)
    {
        log.info("sending PUT request to {}", url);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        HttpEntity<String> entity = new HttpEntity<String>(body, headers);
        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
        return exchange;
    }

    public ResponseEntity<String> delete(String url)
    {
        log.info("sending DELETE request to {}", url);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        HttpEntity<String> entity = new HttpEntity<String>(headers);
        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
        return exchange;
    }

}

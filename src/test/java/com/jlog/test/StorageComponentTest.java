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

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.UUID;

import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import com.jlog.JLogConfiguration;
import com.jlog.elasticsearch.StorageComponent;
import com.jlog.grok.LogValueHolder;
import com.jlog.internal.ContentParser;
import com.jlog.internal.ContentParserImpl;

/**
 * @author Omid 
 *
 */
@RunWith(SpringRunner.class)
@Import(JLogConfiguration.class)
public class StorageComponentTest
{

    
    @Autowired
    StorageComponent sc;
    
    @Test
    @Ignore
    public void mappingTest()
    {
        boolean mappingExists = sc.mappingExists();        
    }
    
    @Test
    @Ignore
    public void indexCreationTest()
    {
        sc.buildMapping();
    }
    
    @Test
    @Ignore
    public void bulkInsertTest()
    {
        LogValueHolder item1 = new LogValueHolder("324", "StorageComponent", "bulkInsert", "StorageComponent.java", "Cause by : nullpointer", "" , "", "");
        LogValueHolder item2 = new LogValueHolder("112", "JLOGApplication", "main", "JLOGApplication.java", "", "", "", "");
        sc.bulkInsert(Arrays.asList(item1,item2));
    }
    
    
    @TestConfiguration
    static class Config
    {

        
        @Bean
        public StorageComponent st(com.jlog.internal.RestTemplate restTemplate, ContentParser contentParser, JLogConfiguration conf)
        {
            return new StorageComponent(restTemplate, contentParser, conf);
        }
        
        @Bean
        public com.jlog.internal.RestTemplate rt(RestTemplate restTemplate)
        {
            return new com.jlog.internal.RestTemplate(restTemplate);
        }
        
        @Bean
        public RestTemplate restTemplate() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException
        {
            RestTemplate rt = new RestTemplate();
            rt.setRequestFactory(new BufferingClientHttpRequestFactory(clientHttpRequestFactory()));
//            rt.setInterceptors(Collections.singletonList(createRestTemplateInterceptor()));
            return rt;
        }

        private ClientHttpRequestFactory clientHttpRequestFactory() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException
        {
            TrustStrategy acceptingTrustStrategy = new TrustStrategy() {
                @Override
                public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException
                {
                    return true;
                }
            };
            SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
            SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
            CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();
            return new HttpComponentsClientHttpRequestFactory(httpClient);
        }
        
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

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

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.jlog.elasticsearch.StorageComponent;
import com.jlog.internal.JLogBufferingClientHttpRequestFactory;
import com.jlog.io.FileReaderFactory;
import com.jlog.io.RemoteLogReader;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Omid 
 *
 */
@SpringBootApplication
@EnableConfigurationProperties(JLogConfiguration.class)
@Slf4j
public class JLogApplication
{

    public static void main(String[] args) throws Exception
    {
        SpringApplication.run(JLogApplication.class, args);
    }

    @Bean
    ApplicationRunner runner(JLogConfiguration conf, ApplicationContext ctx, StorageComponent sc, RemoteLogReader rl)
    {
        return args -> {
            List<String> hosts = args.getOptionValues("h");
            if (hosts != null)
            {
                String host = hosts.iterator().next();
                conf.setIp(host);
                rl.setIp(host);
            }
            List<String> ports = args.getOptionValues("p");
            if (ports != null)
            {
                int port = Integer.parseInt(ports.iterator().next());
                conf.setPort(port);
                rl.setPort(port);
            }
            List<String> users = args.getOptionValues("u");
            if (users != null)
            {
                String u = users.iterator().next();
                conf.setUsername(u);
                rl.setLogin(u);
            }
            List<String> pass = args.getOptionValues("pass");
            if (pass != null)
            {
                String p = pass.iterator().next();
                conf.setPassword(p);
                rl.setPassword(p);
            }
            List<String> logdir = args.getOptionValues("logdir");
            if (logdir != null)
            {
                conf.setLogDirectory(logdir.iterator().next());
            }
            if(sc.elasticSearchIsRunning() == false)
                log.error("ELASTICSEARCH IS NOT RUNNING");
            else
                log.info("ELASTICSEARCH IS CONNECTED");
        };
        
        
        
    }

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
    com.jlog.internal.RestTemplate restTemplate(RestTemplate restTemplate)
    {
        return new com.jlog.internal.RestTemplate(restTemplate);
    }

    @Bean
    public RestTemplate rt() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException
    {
        RestTemplate rt = new RestTemplate();
        rt.setRequestFactory(new JLogBufferingClientHttpRequestFactory(clientHttpRequestFactory()));
        // rt.setInterceptors(Collections.singletonList(createRestTemplateInterceptor()));
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

}

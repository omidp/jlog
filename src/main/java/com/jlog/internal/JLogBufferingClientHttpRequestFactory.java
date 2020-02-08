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

import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

/**
 * @author Omid 
 *
 */
public class JLogBufferingClientHttpRequestFactory extends BufferingClientHttpRequestFactory
{

    ClientHttpRequestFactory requestFactory;
    
    public JLogBufferingClientHttpRequestFactory(ClientHttpRequestFactory requestFactory)
    {
        super(requestFactory);
        this.requestFactory = requestFactory;
    }
    
    
    public void timeout3seconds()
    {
        ((HttpComponentsClientHttpRequestFactory) requestFactory).setConnectionRequestTimeout(3000);
        ((HttpComponentsClientHttpRequestFactory) requestFactory).setConnectTimeout(3000);
        ((HttpComponentsClientHttpRequestFactory) requestFactory).setReadTimeout(3000);    
    }
    

}

/*
 * Copyright 2015 ETH Zuerich, SIS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.common.http;

import java.io.InputStream;
import java.security.KeyStore;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpProxy;
import org.eclipse.jetty.client.ProxyConfiguration;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ScheduledExecutorScheduler;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;

/**
 * Singleton for {@link HttpClient}.
 * 
 * @author Franz-Josef Elmer
 */
public class JettyHttpClientFactory
{
    private static HttpClient httpClient;

    public static HttpClient getHttpClient()
    {
        if (httpClient == null)
        {
            synchronized (JettyHttpClientFactory.class)
            {
                HttpClient client = createHttpClient();

                String proxyHost = System.getProperties().getProperty("openbis.proxyHost");
                String proxyPort = System.getProperties().getProperty("openbis.proxyPort");

                if (proxyHost != null && proxyPort != null)
                {
                    ProxyConfiguration proxyConfig = client.getProxyConfiguration();
                    HttpProxy proxy = new HttpProxy(proxyHost, Integer.parseInt(proxyPort));
                    proxyConfig.getProxies().add(proxy);
                }

                try
                {
                    client.start();
                    httpClient = client;
                } catch (Exception e)
                {
                    throw CheckedExceptionTunnel.wrapIfNecessary(e);
                }
            }
        }
        return httpClient;
    }

    private static HttpClient createHttpClient()
    {
        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setEndpointIdentificationAlgorithm(null); // disable hostname verification

        String trustStorePath = System.getProperties().getProperty("javax.net.ssl.trustStore");
        if (trustStorePath != null)
        {
            // Dummy key store is needed. We can not used trust store as key store because a password would be needed
            sslContextFactory.setKeyStore(createDummyKeyStore());
            sslContextFactory.setTrustStorePath(trustStorePath);
        } else
        {
            sslContextFactory.setTrustAll(true);
        }
        HttpClient client = new HttpClient(sslContextFactory)
            {
                @Override
                protected void doStart() throws Exception
                {
                    if (getExecutor() == null)
                    {
                        QueuedThreadPool threadPool = new QueuedThreadPool();
                        threadPool.setName("openBIS-jetty");
                        threadPool.setDaemon(true);
                        setExecutor(threadPool);
                    }
                    if (getScheduler() == null)
                    {
                        setScheduler(new ScheduledExecutorScheduler("openBIS-jetty-scheduler", true));
                    }
                    super.doStart();
                }
            };
        return client;
    }

    private static KeyStore createDummyKeyStore()
    {
        try
        {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load((InputStream) null, null);
            return keyStore;
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private JettyHttpClientFactory()
    {
    }
}

/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.spring;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.remoting.httpinvoker.HttpComponentsHttpInvokerRequestExecutor;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;

import com.marathon.util.spring.StreamSupportingHttpInvokerProxyFactoryBean;
import com.marathon.util.spring.StreamSupportingHttpInvokerRequestExecutor;

/**
 * Utility methods for HTTP Invocations. <br>
 * This class is used by clients to create service stubs. If a dependency to external libraries is added to this class then build scripts used to
 * build the API jar have to be changed as well.
 * 
 * @author Franz-Josef Elmer
 */
public class HttpInvokerUtils
{
    private static SSLConnectionSocketFactory sf = initializeSSLConnectionFactory();

    private static SSLConnectionSocketFactory initializeSSLConnectionFactory()
    {
        SSLContextBuilder builder = new SSLContextBuilder();
        try
        {
            // These lines make it possible to run webstart apps from locally installed openbis with self-signed certificate
            // builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            // return new SSLConnectionSocketFactory(builder.build(), SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            return new SSLConnectionSocketFactory(SSLContexts.createDefault(), SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Creates a service stub for the specified service interface.
     * 
     * @param serviceURL URL providing the service via HTTP tunneling.
     * @param serverTimeoutInMillis Service time out in milliseconds. A values of 0 means never timeout.
     */
    public static <T> T createServiceStub(final Class<T> serviceInterface, final String serviceURL,
            final long serverTimeoutInMillis)
    {
        final HttpInvokerProxyFactoryBean httpInvokerProxy = new HttpInvokerProxyFactoryBean();

        Registry<ConnectionSocketFactory> schemeRegistry = RegistryBuilder.<ConnectionSocketFactory> create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sf)
                .build();
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(schemeRegistry);
        connectionManager.setMaxTotal(100);
        connectionManager.setDefaultMaxPerRoute(5);
        CloseableHttpClient client = HttpClientBuilder.create().setConnectionManager(connectionManager).build();

        HttpComponentsHttpInvokerRequestExecutor httpInvokerRequestExecutor = new HttpComponentsHttpInvokerRequestExecutor(client);

        httpInvokerProxy.setHttpInvokerRequestExecutor(httpInvokerRequestExecutor);
        httpInvokerProxy.setBeanClassLoader(serviceInterface.getClassLoader());
        httpInvokerProxy.setServiceUrl(serviceURL);
        httpInvokerProxy.setServiceInterface(serviceInterface);

        httpInvokerRequestExecutor.setReadTimeout((int) serverTimeoutInMillis);

        final InetSocketAddress proxyAddressOrNull = tryFindProxy(serviceURL);
        if (proxyAddressOrNull != null)
        {
            HttpHost proxy = new HttpHost(proxyAddressOrNull.getHostName(), proxyAddressOrNull.getPort(), "http");
            CloseableHttpClient client2 = HttpClientBuilder.create().setProxy(proxy).setConnectionManager(connectionManager).build();
            httpInvokerRequestExecutor.setHttpClient(client2);
        }
        httpInvokerProxy.afterPropertiesSet();
        return getCastedService(httpInvokerProxy);
    }

    public static <T> T createStreamSupportingServiceStub(final Class<T> serviceInterface,
            final String serviceURL, final long serverTimeoutInMillis)
    {
        final HttpInvokerProxyFactoryBean httpInvokerProxy = new StreamSupportingHttpInvokerProxyFactoryBean();
        httpInvokerProxy.setBeanClassLoader(serviceInterface.getClassLoader());
        httpInvokerProxy.setServiceUrl(serviceURL);
        httpInvokerProxy.setServiceInterface(serviceInterface);

        Registry<ConnectionSocketFactory> schemeRegistry = RegistryBuilder.<ConnectionSocketFactory> create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sf)
                .build();
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(schemeRegistry);
        connectionManager.setMaxTotal(100);
        connectionManager.setDefaultMaxPerRoute(5);

        StreamSupportingHttpInvokerRequestExecutor httpInvokerRequestExecutor =
                (StreamSupportingHttpInvokerRequestExecutor) httpInvokerProxy.getHttpInvokerRequestExecutor();
        httpInvokerRequestExecutor.setReadTimeout((int) serverTimeoutInMillis);

        final InetSocketAddress proxyAddressOrNull = tryFindProxy(serviceURL);
        if (proxyAddressOrNull != null)
        {
            HttpHost proxy = new HttpHost(proxyAddressOrNull.getHostName(), proxyAddressOrNull.getPort(), "http");
            CloseableHttpClient client2 = HttpClientBuilder.create().setProxy(proxy).setConnectionManager(connectionManager).build();
            httpInvokerRequestExecutor.setHttpClient(client2);
        } else
        {
            httpInvokerRequestExecutor.setHttpClient(HttpClientBuilder.create().setConnectionManager(connectionManager).build());
        }
        httpInvokerProxy.afterPropertiesSet();
        return getCastedService(httpInvokerProxy);
    }

    @SuppressWarnings("unchecked")
    private final static <T> T getCastedService(final HttpInvokerProxyFactoryBean httpInvokerProxy)
    {
        return (T) httpInvokerProxy.getObject();
    }

    /**
     * Returns the proxy's inet address for <var>serviceURL</var>, or <code>null</code>, if no proxy is defined.
     */
    public static InetSocketAddress tryFindProxy(String serviceURL)
    {
        try
        {
            final ProxySelector selector = ProxySelector.getDefault();
            final List<java.net.Proxy> proxyList = selector.select(new URI(serviceURL));
            for (java.net.Proxy proxy : proxyList)
            {
                if (java.net.Proxy.Type.HTTP == proxy.type())
                {
                    return (InetSocketAddress) proxy.address();
                }
            }
        } catch (IllegalArgumentException e)
        {
        } catch (URISyntaxException ex)
        {
        }
        return null;
    }

    private HttpInvokerUtils()
    {
    }

}

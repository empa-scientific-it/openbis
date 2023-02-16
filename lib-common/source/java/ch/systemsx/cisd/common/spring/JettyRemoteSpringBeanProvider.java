/*
 * Copyright ETH 2015 - 2023 Zürich, Scientific IT Services
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
import java.util.List;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpProxy;
import org.eclipse.jetty.client.ProxyConfiguration.Proxy;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;

import com.marathon.util.spring.StreamSupportingHttpInvokerProxyFactoryBean;

public class JettyRemoteSpringBeanProvider implements IRemoteSpringBeanProvider
{

    private HttpClient client;

    public JettyRemoteSpringBeanProvider(HttpClient client)
    {
        this.client = client;
    }

    @Override
    public <T> T create(Class<T> serviceInterface, String serviceURL, long serverTimeoutInMillis)
    {
        JettyHttpInvokerRequestExecutor jettyExecutor = new JettyHttpInvokerRequestExecutor(client, serverTimeoutInMillis);
        HttpInvokerProxyFactoryBean httpInvokerProxy = new StreamSupportingHttpInvokerProxyFactoryBean(jettyExecutor);
        httpInvokerProxy.setBeanClassLoader(serviceInterface.getClassLoader());
        httpInvokerProxy.setServiceUrl(serviceURL);
        httpInvokerProxy.setServiceInterface(serviceInterface);

        final InetSocketAddress proxyAddress = HttpInvokerUtils.tryFindProxy(serviceURL);
        if (proxyAddress != null)
        {
            synchronized (JettyRemoteSpringBeanProvider.class)
            {
                List<Proxy> proxies = client.getProxyConfiguration().getProxies();
                if (isProxyAlreadyInitialized(proxies, proxyAddress) == false)
                {
                    proxies.add(new HttpProxy(proxyAddress.getHostName(), proxyAddress.getPort()));
                }
            }
        }

        httpInvokerProxy.afterPropertiesSet();
        return HttpInvokerUtils.getCastedService(httpInvokerProxy);
    }

    private boolean isProxyAlreadyInitialized(List<Proxy> proxies, InetSocketAddress proxyAddress)
    {
        for (Proxy proxy : proxies)
        {
            if (proxy.getAddress().getHost().equals(proxyAddress.getHostName()) && proxy.getAddress().getPort() == proxyAddress.getPort())
            {
                return true;
            }
        }
        return false;
    }
}

/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.common.api.client;

import java.io.File;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.remoting.RemoteAccessException;

import ch.systemsx.cisd.common.api.IRpcService;
import ch.systemsx.cisd.common.ssl.SslCertificateHelper;

/**
 * Helper to find a remote service exported by Spring's HttpInvoker or any other way for which 
 * an implementation of {@link IServiceStubFactory} exists. Default is {@link HttpInvokerBasedServiceStubFactory}.
 * 
 * @author Chandrasekhar Ramakrishnan
 * @author Franz-Josef Elmer
 */
public class ServiceFinder
{
    public static final int SERVER_TIMEOUT_IN_MINUTES = 15;

    private static final long SERVER_TIMEOUT = SERVER_TIMEOUT_IN_MINUTES * DateUtils.MILLIS_PER_MINUTE;

    private final String applicationName;

    private final String urlServiceSuffix;

    private final IServiceStubFactory serviceStubCreator;

    /**
     * Creates an instance for specified application name and URL service suffix. 
     * Uses {@link HttpInvokerBasedServiceStubFactory} to create the service stub.
     * 
     * @param applicationName Name of the Web application.
     * @param urlServiceSuffix URL Suffix of the service.
     */
    public ServiceFinder(String applicationName, String urlServiceSuffix)
    {
        this(applicationName, urlServiceSuffix, new HttpInvokerBasedServiceStubFactory());
    }

    /**
     * Creates an instance for specified application name and URL service suffix.
     * 
     * @param applicationName Name of the Web application.
     * @param urlServiceSuffix URL Suffix of the service.
     * @param serviceStubCreator Factory which creates the service stub
     */
    public ServiceFinder(String applicationName, String urlServiceSuffix, IServiceStubFactory serviceStubCreator)
    {
        this.applicationName = applicationName;
        this.urlServiceSuffix =
                urlServiceSuffix.startsWith("/") ? urlServiceSuffix : "/" + urlServiceSuffix;
        this.serviceStubCreator = serviceStubCreator;
    }

    /**
     * Creates a remote service implementing specified interface for specified server URL. Following server URLs are accepted:
     * <ul>
     * <li>protocol://host:port
     * <li>protocol://host:port/applicationName
     * <li>protocol://host:port/applicationName/applicationName
     * </ul>
     */
    public <S extends IRpcService> S createService(Class<S> serviceInterface, String serverUrl)
    {
        return createService(serviceInterface, serverUrl, SERVER_TIMEOUT);
    }

    /**
     * Creates a remote service implementing specified interface for specified server URL. Following server URLs are accepted:
     * <ul>
     * <li>protocol://host:port
     * <li>protocol://host:port/applicationName
     * <li>protocol://host:port/applicationName/applicationName
     * </ul>
     */
    public <S extends IRpcService> S createService(Class<S> serviceInterface, String serverUrl,
            long timeoutInMillis)
    {
        return createService(serviceInterface, serverUrl, new IServicePinger<S>()
            {
                @Override
                public void ping(S service)
                {
                    service.getMajorVersion();
                }
            }, timeoutInMillis);
    }

    /**
     * Creates a remote service implementing specified interface for specified server URL by using specified pinger for checking server connection.
     * Following server URLs are accepted:
     * <ul>
     * <li>protocol://host:port
     * <li>protocol://host:port/applicationName
     * <li>protocol://host:port/applicationName/applicationName
     * </ul>
     */
    public <S> S createService(Class<S> serviceInterface, String serverUrl,
            IServicePinger<S> servicePinger)
    {
        return createService(serviceInterface, serverUrl, servicePinger, SERVER_TIMEOUT);
    }

    /**
     * Creates a remote service implementing specified interface for specified server URL by using specified pinger for checking server connection.
     * Following server URLs are accepted:
     * <ul>
     * <li>protocol://host:port
     * <li>protocol://host:port/applicationName
     * <li>protocol://host:port/applicationName/applicationName
     * </ul>
     */
    public <S> S createService(Class<S> serviceInterface, String serverUrl,
            IServicePinger<S> servicePinger, long timeoutInMillis)
    {
        ServiceWithUrl<S> serviceWithUrl =
                createServiceWithUrl(serviceInterface, serverUrl, servicePinger, timeoutInMillis);
        return serviceWithUrl.getService();
    }

    public <S> String createServiceUrl(Class<S> serviceInterface, String serverUrl,
            IServicePinger<S> servicePinger, long timeoutInMillis)
    {
        ServiceWithUrl<S> serviceWithUrl =
                createServiceWithUrl(serviceInterface, serverUrl, servicePinger, timeoutInMillis);
        return serviceWithUrl.getUrl();
    }

    private <S> ServiceWithUrl<S> createServiceWithUrl(Class<S> serviceInterface, String serverUrl,
            IServicePinger<S> servicePinger, long timeoutInMillis)
    {
        S service;
        String usedServerUrl = computeServerUrlWithDoubledApplicationName(serverUrl);
        // Try the url that ends in <applicationName>/<applicationName>
        service =
                createServiceStubStoringCertificateIfNecessary(serviceInterface, timeoutInMillis,
                        usedServerUrl);
        if (canConnectToService(service, servicePinger))
        {
            return new ServiceWithUrl<S>(usedServerUrl, service);
        }

        // Try the url that ends in just one <applicationName>
        usedServerUrl = computeServerUrl(serverUrl);
        service =
                createServiceStubStoringCertificateIfNecessary(serviceInterface, timeoutInMillis,
                        usedServerUrl);
        if (canConnectToService(service, servicePinger))
        {
            return new ServiceWithUrl<S>(usedServerUrl, service);
        }

        // Try the url as provided
        usedServerUrl = serverUrl;
        service =
                createServiceStubStoringCertificateIfNecessary(serviceInterface, timeoutInMillis,
                        usedServerUrl);
        servicePinger.ping(service);
        return new ServiceWithUrl<S>(usedServerUrl, service);
    }

    private <S> S createServiceStubStoringCertificateIfNecessary(Class<S> serviceInterface,
            long timeoutInMillis, String usedServerUrl)
    {
        storeCertificateIfNecessary(usedServerUrl);
        S service;
        service =
                serviceStubCreator.createServiceStub(serviceInterface, usedServerUrl + urlServiceSuffix, timeoutInMillis);
        return service;
    }

    private void storeCertificateIfNecessary(String usedServerUrl)
    {
        if (shouldAcceptInvalidCertificates())
        {
            try
            {
                new SslCertificateHelper(usedServerUrl, getKeystoreFile(), "bis").setUpKeyStore();
            } catch (Throwable t)
            {
                // Ignore any errors here, since the logic in this method is optional
            }
        }
    }

    // Always setup a keystore when running under webstart or when it's requested explicitly
    private boolean shouldAcceptInvalidCertificates()
    {
        String forceProperty = System.getProperty("force-accept-ssl-certificate");
        boolean forced =
                (null != forceProperty && forceProperty.equalsIgnoreCase("false") == false);
        return forced || isRunningUnderWebstart();
    }

    private boolean isRunningUnderWebstart()
    {
        return null != System.getProperty("javawebstart.version");
    }

    private static <S> boolean canConnectToService(S service, IServicePinger<S> servicePinger)
    {
        boolean result = true;
        try
        {
            servicePinger.ping(service);
        } catch (RemoteAccessException rae)
        {
            Throwable cause = rae.getCause();
            if (cause instanceof SocketTimeoutException || cause instanceof SocketException)
            {
                throw rae;
            } else
            {
                result = false;
            }
        } catch (Exception e)
        {
            result = false;
        }
        return result;

    }

    private String computeServerUrlWithDoubledApplicationName(String serverUrl)
    {
        if (serverUrl.endsWith("/" + applicationName + "/" + applicationName))
        {
            return serverUrl;
        }

        if (serverUrl.endsWith("/" + applicationName))
        {
            return serverUrl + "/" + applicationName;
        }

        String myServerUrl = serverUrl;
        if (false == serverUrl.endsWith("/"))
        {
            myServerUrl = myServerUrl + "/";
        }
        return myServerUrl + applicationName + "/" + applicationName;
    }

    private String computeServerUrl(String serverUrl)
    {
        if (serverUrl.endsWith("/" + applicationName + "/" + applicationName))
        {
            return serverUrl.substring(0, serverUrl.length() - (1 + applicationName.length()));
        }

        if (serverUrl.endsWith("/" + applicationName))
        {
            return serverUrl;
        }

        String myServerUrl = serverUrl;
        if (false == serverUrl.endsWith("/"))
        {
            myServerUrl = myServerUrl + "/";
        }
        return myServerUrl + applicationName;
    }

    private File getKeystoreFile()
    {
        return new File(getConfigDirectory(), "keystore");
    }

    private File getConfigDirectory()
    {
        String homeDir = System.getProperty("dss.root");
        File configDir;
        if (homeDir != null)
        {
            configDir = new File(homeDir, "etc");
        } else
        {
            homeDir = System.getProperty("user.home");
            configDir = new File(homeDir, ".bis");
        }
        configDir.mkdirs();
        return configDir;
    }

    private class ServiceWithUrl<S>
    {
        private String url;

        private S service;

        public ServiceWithUrl(String url, S service)
        {
            this.url = url + urlServiceSuffix;
            this.service = service;
        }

        public String getUrl()
        {
            return url;
        }

        public S getService()
        {
            return service;
        }
    }
}

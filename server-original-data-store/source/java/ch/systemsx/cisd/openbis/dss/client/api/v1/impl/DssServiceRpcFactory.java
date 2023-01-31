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

package ch.systemsx.cisd.openbis.dss.client.api.v1.impl;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;

import org.apache.commons.lang3.time.DateUtils;

import ch.systemsx.cisd.common.api.IRpcService;
import ch.systemsx.cisd.common.api.IRpcServiceFactory;
import ch.systemsx.cisd.common.api.IRpcServiceNameServer;
import ch.systemsx.cisd.common.api.IncompatibleAPIVersionsException;
import ch.systemsx.cisd.common.api.RpcServiceInterfaceDTO;
import ch.systemsx.cisd.common.api.RpcServiceInterfaceVersionDTO;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.common.ssl.SslCertificateHelper;

/**
 * Client-side factory for DssServiceRpc objects.
 * <p>
 * Create client-side proxies to server RPC interface objects.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
// TODO 2010-05-11, CR: This code should be refactored and moved into a common location,
// but it is a bit difficult due to dependencies.
public class DssServiceRpcFactory implements IRpcServiceFactory
{
    private static final long DEFAULT_SERVER_TIMEOUT = 5 * DateUtils.MILLIS_PER_MINUTE;

    private static final String NAME_SERVER_SUFFIX = "/rmi-name-server";

    private final long timeoutInMillis;

    public DssServiceRpcFactory()
    {
        this(DEFAULT_SERVER_TIMEOUT);
    }

    public DssServiceRpcFactory(long timeoutInMillis)
    {
        this.timeoutInMillis = timeoutInMillis;
    }

    @Override
    public Collection<RpcServiceInterfaceDTO> getSupportedInterfaces(String serverURL,
            boolean getServerCertificateFromServer) throws IncompatibleAPIVersionsException
    {
        // We assume the location of the name server follows the convention
        String nameServerURL = serverURL + NAME_SERVER_SUFFIX;
        Class<IRpcServiceNameServer> clazz = IRpcServiceNameServer.class;
        if (getServerCertificateFromServer)
        {
            new SslCertificateHelper(nameServerURL, getKeystoreFile(), "dss").setUpKeyStore();
        }

        IRpcServiceNameServer nameServer =
                new ServiceProxyBuilder<IRpcServiceNameServer>(nameServerURL, clazz,
                        timeoutInMillis, 1).getServiceInterface();
        return nameServer.getSupportedInterfaces();
    }

    @Override
    public <T extends IRpcService> T getService(RpcServiceInterfaceVersionDTO ifaceVersion,
            Class<T> ifaceClazz, String serverURL, boolean getServerCertificateFromServer)
            throws IncompatibleAPIVersionsException
    {
        String serviceURL = serverURL + ifaceVersion.getUrlSuffix();
        if (getServerCertificateFromServer)
        {
            new SslCertificateHelper(serviceURL, getKeystoreFile(), "dss").setUpKeyStore();
        }

        return new ServiceProxyBuilder<T>(serviceURL, ifaceClazz, timeoutInMillis, 1)
                .getServiceInterface();
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
            configDir = new File(homeDir, ".dss");
        }
        configDir.mkdirs();
        return configDir;
    }
}

/**
 * Internal helper class for constructing service proxies;
 * 
 * @author Chandrasekhar Ramakrishnan
 */
@SuppressWarnings("unchecked")
class ServiceProxyBuilder<T extends IRpcService>
{
    private final String serviceURL;

    private final Class<?> clazz;

    private final long serverTimeoutMillis;

    private final int apiClientVersion;

    ServiceProxyBuilder(String serviceURL, Class<?> clazz, long serverTimeoutMillis,
            int apiClientVersion)
    {
        this.serviceURL = serviceURL;
        this.clazz = clazz;
        this.serverTimeoutMillis = serverTimeoutMillis;
        this.apiClientVersion = apiClientVersion;
    }

    T getServiceInterface() throws IncompatibleAPIVersionsException
    {
        T service = getRawServiceProxy();
        service = wrapProxyInServiceInvocationHandler(service);
        final int apiServerVersion = service.getMajorVersion();
        if (apiClientVersion != apiServerVersion)
        {
            throw new IncompatibleAPIVersionsException(apiClientVersion, apiServerVersion);
        }

        return service;
    }

    private T getRawServiceProxy()
    {
        return (T) HttpInvokerUtils.createStreamSupportingServiceStub(clazz, serviceURL,
                serverTimeoutMillis);
    }

    private T wrapProxyInServiceInvocationHandler(T service)
    {
        final ClassLoader classLoader = DssServiceRpcFactory.class.getClassLoader();
        final ServiceInvocationHandler invocationHandler = new ServiceInvocationHandler(service);
        final T proxy = (T) Proxy.newProxyInstance(classLoader, new Class[]
        { clazz }, invocationHandler);
        return proxy;
    }

    /**
     * An invocation handler that unwraps exceptions that occur in methods called via reflection.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    private static final class ServiceInvocationHandler implements InvocationHandler
    {
        private final IRpcService service;

        private ServiceInvocationHandler(IRpcService service)
        {
            this.service = service;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
        {
            try
            {
                return method.invoke(service, args);
            } catch (InvocationTargetException ex)
            {
                throw ex.getCause();
            }
        }
    }
}

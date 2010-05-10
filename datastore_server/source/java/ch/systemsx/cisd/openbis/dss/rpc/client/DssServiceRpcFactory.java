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

package ch.systemsx.cisd.openbis.dss.rpc.client;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.common.ssl.SslCertificateHelper;
import ch.systemsx.cisd.openbis.dss.rpc.shared.IRpcService;
import ch.systemsx.cisd.openbis.dss.rpc.shared.IRpcServiceNameServer;
import ch.systemsx.cisd.openbis.dss.rpc.shared.RpcServiceInterfaceDTO;

/**
 * Client-side factory for DssServiceRpc objects.
 * <p>
 * Create client-side proxies to server RPC interface objects.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DssServiceRpcFactory implements IDssServiceRpcFactory
{
    private static final int SERVER_TIMEOUT_MIN = 5;

    private static final String NAME_SERVER_SUFFIX = "/rpc";

    public List<RpcServiceInterfaceDTO> getSupportedInterfaces(String serverURL,
            boolean getServerCertificateFromServer) throws IncompatibleAPIVersionsException
    {
        // We assume the location of the name server follows the convention
        String nameServerURL = serverURL + NAME_SERVER_SUFFIX;
        Class<IRpcServiceNameServer> clazz = IRpcServiceNameServer.class;
        if (getServerCertificateFromServer)
        {
            new SslCertificateHelper(nameServerURL, getConfigDirectory(), "dss").setUpKeyStore();
        }

        IRpcServiceNameServer nameServer =
                new ServiceProxyBuilder<IRpcServiceNameServer>(nameServerURL, clazz,
                        SERVER_TIMEOUT_MIN, 1).getServiceInterface();
        return nameServer.getSupportedInterfaces();
    }

    public <T extends IRpcService> T getService(RpcServiceInterfaceDTO iface, Class<T> ifaceClazz,
            String serverURL, boolean getServerCertificateFromServer)
            throws IncompatibleAPIVersionsException
    {
        String serviceURL = serverURL + iface.getInterfaceUrlSuffix();
        if (getServerCertificateFromServer)
        {
            new SslCertificateHelper(serviceURL, getConfigDirectory(), "dss").setUpKeyStore();
        }

        return new ServiceProxyBuilder<T>(serviceURL, ifaceClazz, SERVER_TIMEOUT_MIN, 1)
                .getServiceInterface();
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

    private final int serverTimeoutMin;

    private final int apiClientVersion;

    ServiceProxyBuilder(String serviceURL, Class<?> clazz, int serverTimeoutMin,
            int apiClientVersion)
    {
        this.serviceURL = serviceURL;
        this.clazz = clazz;
        this.serverTimeoutMin = serverTimeoutMin;
        this.apiClientVersion = apiClientVersion;
    }

    T getServiceInterface() throws IncompatibleAPIVersionsException
    {
        T service = getRawServiceProxy();
        service = wrapProxyInServiceInvocationHandler(service);
        final int apiServerVersion = service.getMajorVersion();
        final int apiMinClientVersion = service.getMinorVersion();
        if (apiClientVersion < apiMinClientVersion || apiClientVersion > apiServerVersion)
        {
            throw new IncompatibleAPIVersionsException(apiClientVersion, apiServerVersion,
                    apiMinClientVersion);
        }

        return service;
    }

    private T getRawServiceProxy()
    {
        return (T) HttpInvokerUtils.createStreamSupportingServiceStub(clazz, serviceURL,
                serverTimeoutMin);
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

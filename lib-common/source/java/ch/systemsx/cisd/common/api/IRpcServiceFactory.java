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

package ch.systemsx.cisd.common.api;

import java.util.Collection;

/**
 * A factory for creating proxies to RPC services on a data store server.
 * <p>
 * Because of the inherent potential variability in the DSS RPC, the interface has been made flexible to provide clients simultaneous access to
 * several different communication interfaces.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public interface IRpcServiceFactory
{
    /**
     * Get the RPC service interfaces supported by the server.
     * 
     * @param serverURL The URL of the data store server to query.
     * @param shouldGetServerCertificateFromServer If the URL scheme is https and shouldGetServerCertificateFromServer is true, the factory will
     *            retrieve the SSL certificate from the server.
     */
    public abstract Collection<RpcServiceInterfaceDTO> getSupportedInterfaces(String serverURL,
            boolean shouldGetServerCertificateFromServer) throws IncompatibleAPIVersionsException;

    /**
     * Get a proxy to the RPC service interface specified by <code>ifaceVersion</code>.
     * 
     * @param ifaceVersion The proxy interface to return
     * @param ifaceClazz The class of the interface
     * @param serverURL The url of the server that exports the service
     * @param getServerCertificateFromServer True if the certificate should be retrieved from the server.
     */
    public abstract <T extends IRpcService> T getService(
            RpcServiceInterfaceVersionDTO ifaceVersion, Class<T> ifaceClazz, String serverURL,
            boolean getServerCertificateFromServer) throws IncompatibleAPIVersionsException;
}
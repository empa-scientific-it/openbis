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

import ch.systemsx.cisd.openbis.dss.rpc.shared.DssServiceRpcInterface;
import ch.systemsx.cisd.openbis.dss.rpc.shared.IDssServiceRpc;

/**
 * The interface for creating proxies to the data store server.
 * <p>
 * Because of the inherent potential variability in the DSS RPC, the interface has been made
 * flexible to provide clients simultaneous access to several different communication interfaces.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public interface IDssServiceRpcFactory
{
    /**
     * Get an array of RPC service interfaces supported by the server.
     */
    public abstract DssServiceRpcInterface[] getSupportedInterfaces(String serverURL,
            boolean getServerCertificateFromServer) throws IncompatibleAPIVersionsException;

    /**
     * Get get RPC service interface specified by <code>iface</code>.
     */
    public abstract <T extends IDssServiceRpc> T getService(DssServiceRpcInterface iface,
            Class<T> ifaceClazz, String serverURL, boolean getServerCertificateFromServer)
            throws IncompatibleAPIVersionsException;
}
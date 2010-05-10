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

package ch.systemsx.cisd.common.api.server;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.api.IRpcServiceNameServer;
import ch.systemsx.cisd.common.api.RpcServiceInterfaceDTO;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * Implementation of the {@link IRpcServiceNameServer} interface which registry for accessing the
 * RPC services supported by a server.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class RpcServiceNameServer implements IRpcServiceNameServer
{
    private final ArrayList<RpcServiceInterfaceDTO> supportedInterfaces;

    static private final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, RpcServiceNameServer.class);

    RpcServiceNameServer()
    {
        supportedInterfaces = new ArrayList<RpcServiceInterfaceDTO>();
    }

    public List<RpcServiceInterfaceDTO> getSupportedInterfaces()
    {
        return supportedInterfaces;
    }

    public int getMajorVersion()
    {
        return 1;
    }

    public int getMinorVersion()
    {
        return 0;
    }

    /**
     * Add an interface to the registry.
     */
    public void addSupportedInterface(RpcServiceInterfaceDTO iface)
    {
        supportedInterfaces.add(iface);
        operationLog.info("Registered RPC Interface " + iface.toString());
    }

}

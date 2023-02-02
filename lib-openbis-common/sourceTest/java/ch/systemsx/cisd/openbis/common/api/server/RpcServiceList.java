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

package ch.systemsx.cisd.openbis.common.api.server;

import java.util.Collection;

import org.apache.commons.lang3.time.DateUtils;

import ch.systemsx.cisd.common.api.IRpcServiceNameServer;
import ch.systemsx.cisd.common.api.RpcServiceInterfaceDTO;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;

/**
 * A test that connects to a server and lists the rpc services it provides.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class RpcServiceList
{
    static
    {
        // Disable any logging output.
        System.setProperty("org.apache.commons.logging.Log",
                "org.apache.commons.logging.impl.NoOpLog");
    }

    public static void main(String[] args)
    {
        if (args.length < 1)
        {
            listServices("http://localhost:8888/openbis");
            listServices("http://localhost:8889");
        }

        for (String serverUrl : args)
        {
            listServices(serverUrl);
        }

    }

    public static void listServices(String serverUrl)
    {
        System.out.println(serverUrl);

        String nameServerUrl = serverUrl + IRpcServiceNameServer.PREFFERED_URL_SUFFIX;
        IRpcServiceNameServer nameServer =
                HttpInvokerUtils.createServiceStub(IRpcServiceNameServer.class, nameServerUrl,
                        1 * DateUtils.MILLIS_PER_MINUTE);
        Collection<RpcServiceInterfaceDTO> interfaces = nameServer.getSupportedInterfaces();
        for (RpcServiceInterfaceDTO iface : interfaces)
        {
            System.out.print("\t");
            System.out.println(iface);
        }
    }
}

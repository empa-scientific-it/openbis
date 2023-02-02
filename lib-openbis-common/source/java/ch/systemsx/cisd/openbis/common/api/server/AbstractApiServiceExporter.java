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

import javax.annotation.Resource;

import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;

import ch.systemsx.cisd.common.api.IRpcService;
import ch.systemsx.cisd.common.api.IRpcServiceNameServer;
import ch.systemsx.cisd.common.api.RpcServiceInterfaceVersionDTO;
import ch.systemsx.cisd.common.spring.ServiceExceptionTranslator;
import ch.systemsx.cisd.common.spring.WhiteAndBlackListHttpInvokerServiceExporter;

/**
 * Abstract super class of all classes make an API available via {@link HttpInvokerServiceExporter}.
 *
 * @author Franz-Josef Elmer
 */
public abstract class AbstractApiServiceExporter extends WhiteAndBlackListHttpInvokerServiceExporter
{
    @Resource(name = IRpcServiceNameServer.PREFFERED_BEAN_NAME)
    private RpcServiceNameServer nameServer;

    /**
     * Establishes the specified service which implements the specified interface. The service will register at the name server under the specified
     * name. It will accessible via HTTP by the specified service URL.
     */
    protected void establishService(Class<? extends IRpcService> serviceInterface,
            IRpcService service, String serviceName, String serviceURL)
    {
        setServiceInterface(serviceInterface);
        setService(service);
        setInterceptors(new Object[]
        { new ServiceExceptionTranslator() });
        int majorVersion = service.getMajorVersion();
        int minorVersion = service.getMinorVersion();
        RpcServiceInterfaceVersionDTO ifaceVersion =
                new RpcServiceInterfaceVersionDTO(serviceName, serviceURL, majorVersion,
                        minorVersion);
        nameServer.addSupportedInterfaceVersion(ifaceVersion);
    }
}

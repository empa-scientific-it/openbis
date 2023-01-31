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

package ch.systemsx.cisd.openbis.dss.generic.server;

import ch.systemsx.cisd.cifex.rpc.client.ICIFEXComponent;
import ch.systemsx.cisd.cifex.rpc.client.IncompatibleAPIVersionsException;
import ch.systemsx.cisd.cifex.rpc.client.RPCServiceFactory;
import ch.systemsx.cisd.cifex.shared.basic.Constants;
import ch.systemsx.cisd.common.exceptions.MasqueradingException;

final class CIFEXRPCServiceFactory implements ICIFEXRPCServiceFactory
{
    private static final long serialVersionUID = 1L;

    private final String cifexURL;

    private transient ICIFEXComponent cifexComponent;

    CIFEXRPCServiceFactory(String cifexURL)
    {
        this.cifexURL = cifexURL;
    }

    @Override
    public ICIFEXComponent createCIFEXComponent()
    {
        if (cifexComponent == null)
        {
            final String serviceURL = cifexURL + Constants.CIFEX_RPC_PATH;
            try
            {
                cifexComponent = RPCServiceFactory.createCIFEXComponent(serviceURL, true);
            } catch (IncompatibleAPIVersionsException ex)
            {
                throw new MasqueradingException(ex, "Error occured on DSS");
            }
        }
        return cifexComponent;
    }
}
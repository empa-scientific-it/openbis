/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.client.api.v1;

import ch.systemsx.cisd.openbis.dss.client.api.v1.impl.OpenbisServiceFacade;

/**
 * A factory creating {@link IOpenbisServiceFacade} instances.
 * 
 * @author Kaloyan Enimanev
 */
public class OpenbisServiceFacadeFactory implements IOpenbisServiceFacadeFactory
{
    public static OpenbisServiceFacadeFactory INSTANCE = new OpenbisServiceFacadeFactory();

    private OpenbisServiceFacadeFactory()
    {
    }

    @Override
    public IOpenbisServiceFacade tryToCreate(String sessionToken, String openbisUrl,
            long timeoutInMillis)
    {
        return OpenbisServiceFacade.tryCreate(sessionToken, openbisUrl, timeoutInMillis);
    }

    @Override
    public IOpenbisServiceFacade tryToCreate(String username, String password, String openbisUrl,
            long timeoutInMillis)
    {
        return OpenbisServiceFacade.tryCreate(username, password, openbisUrl, timeoutInMillis);
    }

    /**
     * Creates an {@link OpenbisServiceFacade} instance that can be used to interact with an openBIS backend.
     * 
     * @param username an openBIS user id.
     * @param password a password corresponding to the <code>username</code> parameter.
     * @param openbisUrl the HTTP url of the remote openBIS server e.g. https://openbis.ethz.ch/openbis/
     * @param timeoutInMillis a remote-call timeout.
     */
    public static IOpenbisServiceFacade tryCreate(String username, String password,
            String openbisUrl, long timeoutInMillis)
    {
        return INSTANCE.tryToCreate(username, password, openbisUrl, timeoutInMillis);
    }

    /**
     * Creates an {@link OpenbisServiceFacade} instance that can be used to interact with an openBIS backend.
     * 
     * @param sessionToken token for existing sesstion in openBIS.
     * @param openbisUrl the HTTP url of the remote openBIS server e.g. https://openbis.ethz.ch/openbis/
     * @param timeoutInMillis a remote-call timeout.
     */
    public static IOpenbisServiceFacade tryCreate(String sessionToken, String openbisUrl,
            long timeoutInMillis)
    {
        return INSTANCE.tryToCreate(sessionToken, openbisUrl, timeoutInMillis);
    }

}

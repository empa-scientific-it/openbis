/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.client.api.gui.model;

import org.apache.commons.lang3.time.DateUtils;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.dss.client.api.v1.OpenbisServiceFacadeFactory;
import ch.systemsx.cisd.openbis.dss.client.api.v3.IOpenbisServiceFacadeV3;
import ch.systemsx.cisd.openbis.dss.client.api.v3.OpenbisServiceFacadeV3;

public class DssCommunicationState
{
    private final IOpenbisServiceFacade openBISService;

    private final IOpenbisServiceFacadeV3 serviceFacadeV3;
    
    private final boolean logoutOnClose;

    private static final long CONNECTION_TIMEOUT_MILLIS = 60 * DateUtils.MILLIS_PER_SECOND;

    /**
     * Create a new instance of the DssCommunicationState based info in the arguments. Throws an exception if it could not be created.
     */
    public DssCommunicationState(String[] args) throws UserFailureException,
            EnvironmentFailureException
    {
        if (args.length < 2)
            throw new ConfigurationFailureException(
                    "The openBIS File Upload Client was improperly configured -- the arguments it requires were not supplied. Please talk to the openBIS administrator.");

        String openBisUrl = args[0];

        switch (args.length)
        {
            case 2:
                String sessionToken = args[1];
                serviceFacadeV3 = OpenbisServiceFacadeV3.tryCreate(sessionToken, openBisUrl, CONNECTION_TIMEOUT_MILLIS);
                if (null == serviceFacadeV3)
                {
                    throw new ConfigurationFailureException(
                            "The openBIS File Upload Client was improperly configured -- the session token is not valid. Please talk to the openBIS administrator.");
                }
                openBISService =  OpenbisServiceFacadeFactory.tryCreate(sessionToken, openBisUrl, CONNECTION_TIMEOUT_MILLIS);
                // Don't logout -- the user wants to keep his/her session token alive.
                logoutOnClose = false;
                break;
            default:
                String userName = args[1];
                String passwd = args[2];
                serviceFacadeV3 = OpenbisServiceFacadeV3.tryCreate(userName, passwd, openBisUrl, CONNECTION_TIMEOUT_MILLIS);
                if (null == serviceFacadeV3)
                {
                    throw new ConfigurationFailureException(
                            "The user name / password combination is incorrect.");
                }
                String token = serviceFacadeV3.getSessionToken();
                openBISService =  OpenbisServiceFacadeFactory.tryCreate(token, openBisUrl, CONNECTION_TIMEOUT_MILLIS);
                // Do logout on close
                logoutOnClose = true;
        }
    }

    public IOpenbisServiceFacade getOpenBISService()
    {
        return openBISService;
    }

    public IOpenbisServiceFacadeV3 getServiceFacadeV3()
    {
        return serviceFacadeV3;
    }

    public boolean isLogoutOnClose()
    {
        return logoutOnClose;
    }
}
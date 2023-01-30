/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.generic.server.task;

import java.io.File;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.ISessionWorkspaceProvider;
import ch.systemsx.cisd.openbis.generic.shared.SessionWorkspaceProvider;

/**
 * @author pkupczyk
 */
public class SessionWorkspaceCleanUpMaintenanceTask implements IMaintenanceTask
{

    public static final String DEFAULT_MAINTENANCE_TASK_NAME = "session-workspace-clean-up-task";

    public static final int DEFAULT_MAINTENANCE_TASK_INTERVAL = 3600;

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            SessionWorkspaceCleanUpMaintenanceTask.class);

    private IApplicationServerApi applicationServerApi;

    private ISessionWorkspaceProvider sessionWorkspaceProvider;

    public SessionWorkspaceCleanUpMaintenanceTask()
    {
        this(CommonServiceProvider.getApplicationServerApi(),
                (ISessionWorkspaceProvider) CommonServiceProvider.tryToGetBean(SessionWorkspaceProvider.INTERNAL_SERVICE_NAME));
    }

    SessionWorkspaceCleanUpMaintenanceTask(IApplicationServerApi applicationServerApi, ISessionWorkspaceProvider sessionWorkspaceProvider)
    {
        this.applicationServerApi = applicationServerApi;
        this.sessionWorkspaceProvider = sessionWorkspaceProvider;
    }

    @Override
    public void setUp(String pluginName, Properties properties)
    {
        operationLog.info("Setup plugin " + pluginName);
    }

    @Override
    public void execute()
    {
        Map<String, File> sessionWorkspaces = sessionWorkspaceProvider.getSessionWorkspaces();
        int count = 0;

        for (String sessionToken : sessionWorkspaces.keySet())
        {
            if (false == applicationServerApi.isSessionActive(sessionToken))
            {
                operationLog.info("Session '" + sessionToken + "' is no longer active. Its session workspace will be removed.");
                sessionWorkspaceProvider.deleteSessionWorkspace(sessionToken);
                count++;
            }
        }

        operationLog.info("Session workspace clean up finished. Removed " + count + " workspace(s) of inactive session(s).");
    }

}

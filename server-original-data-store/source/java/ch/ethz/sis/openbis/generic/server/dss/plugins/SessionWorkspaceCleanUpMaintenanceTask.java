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

package ch.ethz.sis.openbis.generic.server.dss.plugins;

import java.io.File;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.systemsx.cisd.common.filesystem.QueueingPathRemoverService;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DssPropertyParametersUtil;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.SessionWorkspaceUtil;

/**
 * @author pkupczyk
 */
public class SessionWorkspaceCleanUpMaintenanceTask implements IMaintenanceTask
{

    public static final String DEFAULT_MAINTENANCE_TASK_NAME = "session-workspace-clean-up-task";

    public static final int DEFAULT_MAINTENANCE_TASK_INTERVAL = 3600;

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            SessionWorkspaceCleanUpMaintenanceTask.class);

    private final IApplicationServerApi applicationServerApi;

    public SessionWorkspaceCleanUpMaintenanceTask()
    {
        this(ServiceProvider.getV3ApplicationService());
    }

    SessionWorkspaceCleanUpMaintenanceTask(IApplicationServerApi applicationServerApi)
    {
        this.applicationServerApi = applicationServerApi;
    }

    @Override
    public void setUp(String pluginName, Properties properties)
    {
        operationLog.info("Setup plugin " + pluginName);
    }

    @Override
    public void execute()
    {
        final Properties properties = DssPropertyParametersUtil.loadServiceProperties();
        final File sessionWorkspace = SessionWorkspaceUtil.getSessionWorkspace(properties);
        int count = 0;

        if (sessionWorkspace.exists() && sessionWorkspace.isDirectory())
        {
            final File[] sessionDirectories = sessionWorkspace.listFiles();

            if (sessionDirectories != null)
            {
                for (File sessionDirectory : sessionDirectories)
                {
                    final String sessionToken = sessionDirectory.getName();

                    if (sessionDirectory.isDirectory() && !applicationServerApi.isSessionActive(sessionToken))
                    {
                        operationLog.info("Session '" + sessionToken + "' is no longer active. Its session workspace will be removed.");
                        QueueingPathRemoverService.removeRecursively(sessionDirectory);
                        count++;
                    }
                }
            }
        }

        operationLog.info("Session workspace clean up finished. Removed " + count + " workspace(s) of inactive session(s).");
    }

}

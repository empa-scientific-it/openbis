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

import java.util.Properties;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * @author pkupczyk
 */
public class SessionCleanUpMaintenanceTask implements IMaintenanceTask
{

    public static final String DEFAULT_MAINTENANCE_TASK_NAME = "session-clean-up-task";

    public static final int DEFAULT_MAINTENANCE_TASK_INTERVAL = 120;

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            SessionCleanUpMaintenanceTask.class);

    private final ISessionManager<Session> sessionManager;

    public SessionCleanUpMaintenanceTask()
    {
        this(CommonServiceProvider.getApplicationContext().getBean(ISessionManager.class));
    }

    SessionCleanUpMaintenanceTask(ISessionManager<Session> sessionManager)
    {
        this.sessionManager = sessionManager;
    }

    @Override
    public void setUp(String pluginName, Properties properties)
    {
        operationLog.info("Setup plugin " + pluginName);
    }

    @Override
    public void execute()
    {
        int count = 0;

        for (Session session : sessionManager.getSessions())
        {
            if (!sessionManager.isSessionActive(session.getSessionToken()))
            {
                operationLog.info("Session '" + session.getSessionToken() + "' is no longer active. It will be removed.");
                sessionManager.expireSession(session.getSessionToken());
                count++;
            }
        }

        operationLog.info("Sessions clean up finished. Removed " + count + " inactive session(s).");
    }

}

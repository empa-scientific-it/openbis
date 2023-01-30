/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.task;

import java.util.Collection;
import java.util.Properties;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecution;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.OperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.store.IOperationExecutionStore;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.common.time.DateTimeUtils;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.IOpenBisSessionManager;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;

/**
 * @author pkupczyk
 */
public abstract class AbstractOperationExecutionMarkMaintenanceTask implements IMaintenanceTask
{

    private final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, getClass());

    private static final String TIME_LIMIT_PROPERTY = "time-limit";

    private static final long TIME_LIMIT_DEFAULT = DateUtils.MILLIS_PER_HOUR;

    private IOpenBisSessionManager sessionManager;

    private IOperationExecutionStore executionStore;

    private long timeLimit;

    private Session session;

    @Override
    public void setUp(String pluginName, Properties properties)
    {
        sessionManager = CommonServiceProvider.getApplicationContext().getBean(IOpenBisSessionManager.class);
        executionStore = CommonServiceProvider.getApplicationContext().getBean(IOperationExecutionStore.class);

        timeLimit = DateTimeUtils.getDurationInMillis(properties, TIME_LIMIT_PROPERTY, TIME_LIMIT_DEFAULT);

        operationLog.info("Task " + pluginName + " initialized.");
    }

    protected OperationContext getOperationContext()
    {
        if (session == null)
        {
            session = createSession();
        } else
        {
            try
            {
                // Touches the session if exists, otherwise throws an exception.
                // It does not log anything. It's good - we don't want to spam the log file.
                CommonServiceProvider.getCommonServer().getAuthSession(session.getSessionToken());
            } catch (InvalidSessionException e)
            {
                session = createSession();
            }
        }
        return new OperationContext(session);
    }

    private Session createSession()
    {
        SessionContextDTO sessionDTO = CommonServiceProvider.getCommonServer().tryToAuthenticateAsSystem();
        return getSessionManager().getSession(sessionDTO.getSessionToken());
    }

    @Override
    public final void execute()
    {
        getOperationLog().debug("execution started");

        doExecute();

        getOperationLog().debug("task executed");
    }

    protected abstract void doExecute();

    protected void markOperationExecutions(Collection<OperationExecution> executions, MarkAction action)
    {
        long startTime = System.currentTimeMillis();

        for (OperationExecution execution : executions)
        {
            if (System.currentTimeMillis() > startTime + getTimeLimit())
            {
                getOperationLog().info("Reached time limit of " + DateTimeUtils.renderDuration(timeLimit) + ".");
                return;
            } else
            {
                action.mark(execution);
            }
        }
    }

    protected Logger getOperationLog()
    {
        return operationLog;
    }

    protected IOpenBisSessionManager getSessionManager()
    {
        return sessionManager;
    }

    protected IOperationExecutionStore getExecutionStore()
    {
        return executionStore;
    }

    protected long getTimeLimit()
    {
        return timeLimit;
    }

    protected interface MarkAction
    {
        public void mark(OperationExecution execution);
    }

}
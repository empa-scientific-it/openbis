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

package ch.systemsx.cisd.openbis.generic.server;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.config.IOperationExecutionConfig;
import ch.ethz.sis.openbis.generic.server.asapi.v3.task.OperationExecutionMarkFailedAfterServerRestartMaintenanceTask;
import ch.ethz.sis.openbis.generic.server.asapi.v3.task.OperationExecutionMarkTimeOutPendingMaintenanceTask;
import ch.ethz.sis.openbis.generic.server.asapi.v3.task.OperationExecutionMarkTimedOutOrDeletedMaintenanceTask;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.maintenance.MaintenancePlugin;
import ch.systemsx.cisd.common.maintenance.MaintenanceTaskParameters;
import ch.systemsx.cisd.common.maintenance.MaintenanceTaskUtils;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;
import ch.systemsx.cisd.openbis.generic.server.task.SessionWorkspaceCleanUpMaintenanceTask;
import ch.systemsx.cisd.openbis.generic.server.task.StatisticsCollectionMaintenanceTask;
import ch.systemsx.cisd.openbis.generic.server.task.events_search.EventsSearchMaintenanceTask;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Resource;
import java.util.*;

/**
 * Configures and starts maintenance tasks.
 *
 * @author Piotr Buczek
 */
public class MaintenanceTaskStarter implements ApplicationContextAware, InitializingBean,
        DisposableBean
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, MaintenanceTaskStarter.class);

    @Resource(name = ExposablePropertyPlaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME)
    private ExposablePropertyPlaceholderConfigurer configurer;

    @Autowired
    private IOperationExecutionConfig operationExecutionConfig;

    private List<MaintenancePlugin> plugins;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        CommonServiceProvider.setApplicationContext(applicationContext);
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        MaintenanceTaskParameters[] tasks =
                MaintenanceTaskUtils.createMaintenancePlugins(configurer.getResolvedProps());

        if (false == isTaskConfigured(tasks, OperationExecutionMarkFailedAfterServerRestartMaintenanceTask.class))
        {
            tasks = addTask(tasks,
                    OperationExecutionMarkFailedAfterServerRestartMaintenanceTask.class,
                    operationExecutionConfig.getMarkFailedAfterServerRestartTaskName(),
                    true,
                    null);
        }

        if (false == isTaskConfigured(tasks, OperationExecutionMarkTimeOutPendingMaintenanceTask.class))
        {
            tasks = addTask(tasks,
                    OperationExecutionMarkTimeOutPendingMaintenanceTask.class,
                    operationExecutionConfig.getMarkTimeOutPendingTaskName(),
                    false,
                    operationExecutionConfig.getMarkTimeOutPendingTaskInterval());
        }

        if (false == isTaskConfigured(tasks, OperationExecutionMarkTimedOutOrDeletedMaintenanceTask.class))
        {
            tasks = addTask(tasks,
                    OperationExecutionMarkTimedOutOrDeletedMaintenanceTask.class,
                    operationExecutionConfig.getMarkTimedOutOrDeletedTaskName(),
                    false,
                    operationExecutionConfig.getMarkTimedOutOrDeletedTaskInterval());
        }

        if (false == isTaskConfigured(tasks, SessionWorkspaceCleanUpMaintenanceTask.class))
        {
            tasks = addTask(tasks,
                    SessionWorkspaceCleanUpMaintenanceTask.class,
                    SessionWorkspaceCleanUpMaintenanceTask.DEFAULT_MAINTENANCE_TASK_NAME,
                    false,
                    SessionWorkspaceCleanUpMaintenanceTask.DEFAULT_MAINTENANCE_TASK_INTERVAL);
        }

        if (false == isTaskConfigured(tasks, StatisticsCollectionMaintenanceTask.class))
        {
            tasks = addTask(tasks,
                    StatisticsCollectionMaintenanceTask.class,
                    StatisticsCollectionMaintenanceTask.DEFAULT_MAINTENANCE_TASK_NAME,
                    false,
                    StatisticsCollectionMaintenanceTask.DEFAULT_MAINTENANCE_TASK_INTERVAL);
        }

        if (false == isTaskConfigured(tasks, EventsSearchMaintenanceTask.class))
        {
            tasks = addTask(tasks,
                    EventsSearchMaintenanceTask.class,
                    EventsSearchMaintenanceTask.DEFAULT_MAINTENANCE_TASK_NAME,
                    false,
                    EventsSearchMaintenanceTask.DEFAULT_MAINTENANCE_TASK_INTERVAL);
        }

        plugins = MaintenanceTaskUtils.startupMaintenancePlugins(tasks);
    }

    private boolean isTaskConfigured(MaintenanceTaskParameters[] tasks, Class<?> clazz)
    {
        for (MaintenanceTaskParameters task : tasks)
        {
            if (clazz.getName().equals(task.getClassName()))
            {
                return true;
            }
        }
        return false;
    }

    private MaintenanceTaskParameters[] addTask(MaintenanceTaskParameters[] tasks, Class<?> clazz, String name, boolean executeOnce,
            Integer intervalOrNull)
    {
        Properties properties = new Properties();
        properties.setProperty(MaintenanceTaskParameters.CLASS_KEY, clazz.getName());
        properties.setProperty(MaintenanceTaskParameters.ONE_TIME_EXECUTION_KEY, String.valueOf(executeOnce));

        if (intervalOrNull != null)
        {
            properties.setProperty(MaintenanceTaskParameters.INTERVAL_KEY, String.valueOf(intervalOrNull));
        }

        if (operationLog.isInfoEnabled())
        {
            StringBuilder message = new StringBuilder();

            message.append("Automatically adding maintenance task '" + name + "' (");
            message.append("class: " + clazz.getName());
            message.append(", executeOnce: " + executeOnce);
            if (intervalOrNull != null)
            {
                message.append(", interval: " + intervalOrNull);
            }
            message.append(")");

            operationLog.info(message.toString());
        }

        List<MaintenanceTaskParameters> tasksList = new ArrayList<MaintenanceTaskParameters>(Arrays.asList(tasks));
        tasksList.add(new MaintenanceTaskParameters(properties, name));
        return tasksList.toArray(new MaintenanceTaskParameters[] {});
    }

    @Override
    public void destroy() throws Exception
    {
        MaintenanceTaskUtils.shutdownMaintenancePlugins(plugins);
    }

    public List<MaintenancePlugin> getPlugins()
    {
        return Collections.unmodifiableList(plugins);
    }

}

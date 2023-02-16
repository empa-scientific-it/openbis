/*
 * Copyright ETH 2021 - 2023 Zürich, Scientific IT Services
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

import java.util.Date;
import java.util.Properties;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.cache.ICacheManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.config.OperationExecutionConfig;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;
import org.apache.log4j.Logger;

public class CacheClearanceMaintenanceTask implements IMaintenanceTask {

    private int cacheTimeout = OperationExecutionConfig.CACHE_CLEARANCE_TIMEOUT_DEFAULT;

    private ICacheManager cacheManager;

    protected final Logger notificationLog;

    public CacheClearanceMaintenanceTask()
    {
        notificationLog = LogFactory.getLogger(LogCategory.NOTIFY, getClass());
    }

    @Override
    public void setUp(String pluginName, Properties properties) {
        cacheManager = CommonServiceProvider.getApplicationContext().getBean(ICacheManager.class);
        cacheTimeout = PropertyUtils.getInt(properties, OperationExecutionConfig.CACHE_CLEARANCE_TIMEOUT,
                OperationExecutionConfig.CACHE_CLEARANCE_TIMEOUT_DEFAULT);
    }

    @Override
    public void execute()
    {
        notificationLog.info("Cache clearance execution started.");
        cacheManager.clearOld(new Date(new Date().getTime() - cacheTimeout * 1000L));
        notificationLog.info("Cache clearance execution finished.");
    }

}

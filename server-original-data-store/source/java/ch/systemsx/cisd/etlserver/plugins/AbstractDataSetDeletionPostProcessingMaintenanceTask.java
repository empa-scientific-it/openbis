/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.etlserver.plugins;

import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSet;

/**
 * Maintenance task which executes after a data set has been deleted in openBIS. Example use cases for it include purging data set archives or/and
 * external databases.
 * 
 * @author Kaloyan Enimanev
 */
public abstract class AbstractDataSetDeletionPostProcessingMaintenanceTask implements
        IMaintenanceTask
{

    protected static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, AbstractDataSetDeletionPostProcessingMaintenanceTask.class);

    protected static final String DELAY_AFTER_DELETION = "delay-after-user-deletion";

    protected static final String CHUNK_SIZE = "chunk-size";

    protected IEncapsulatedOpenBISService openBISService;

    protected long delayAfterDeletion;

    protected int chunkSize;

    ITimeProvider timeProvider;

    protected abstract Long getLastSeenEventId();

    protected abstract void updateLastSeenEventId(Long eventId);

    protected abstract void execute(List<DeletedDataSet> datasetCodes);

    public AbstractDataSetDeletionPostProcessingMaintenanceTask()
    {
        LogInitializer.init();
    }

    protected IEncapsulatedOpenBISService getOpenBISService()
    {
        if (openBISService == null)
        {
            openBISService = ServiceProvider.getOpenBISService();
        }
        return openBISService;
    }

    protected ISimpleLogger getOperationLogAsSimpleLogger()
    {
        return new Log4jSimpleLogger(operationLog);
    }

    @Override
    public void setUp(String pluginName, Properties properties)
    {
        int delayInMinutes = PropertyUtils.getInt(properties, DELAY_AFTER_DELETION, 0);
        delayAfterDeletion = delayInMinutes * DateUtils.MILLIS_PER_MINUTE;
        chunkSize = PropertyUtils.getInt(properties, CHUNK_SIZE, Integer.MAX_VALUE);
    }

    @Override
    public void execute()
    {
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("Synchronizing data set data deletion");
        }
        try
        {
            Long lastSeenEventId = getLastSeenEventId();
            List<DeletedDataSet> deletedDataSets =
                    getOpenBISService().listDeletedDataSets(lastSeenEventId, computeMaxDeletionDate());
            if (deletedDataSets.size() > 0)
            {

                long t0 = getCurrentTime();

                for (int i = 0; i < deletedDataSets.size(); i += chunkSize)
                {
                    List<DeletedDataSet> chunk = deletedDataSets.subList(i, Math.min(deletedDataSets.size(), i + chunkSize));
                    execute(chunk);
                }
                updateLastSeenEventId(deletedDataSets, lastSeenEventId);

                if (operationLog.isInfoEnabled())
                {
                    operationLog.info("Data set deletion post-processing task took "
                            + ((getCurrentTime() - t0 + 500) / 1000) + " seconds.");
                }
            }
        } catch (Throwable t)
        {
            operationLog.error("Failed to process data-set deletion info :", t);
        }
    }

    protected Date computeMaxDeletionDate()
    {
        long now = getCurrentTime();
        long maxDeletionTimestamp = now - delayAfterDeletion;
        return new Date(maxDeletionTimestamp);
    }

    private long getCurrentTime()
    {
        return timeProvider == null ? System.currentTimeMillis() : timeProvider
                .getTimeInMilliseconds();
    }

    private void updateLastSeenEventId(List<DeletedDataSet> deleted, Long lastSeenEventIdOrNull)
    {
        Long maxEventId = lastSeenEventIdOrNull;
        for (DeletedDataSet dds : deleted)
        {
            long eventId = dds.getEventId();
            if (maxEventId == null || eventId > maxEventId)
            {
                maxEventId = eventId;
            }
        }
        if (lastSeenEventIdOrNull == null || maxEventId > lastSeenEventIdOrNull)
        {
            updateLastSeenEventId(maxEventId);
        }
    }

}

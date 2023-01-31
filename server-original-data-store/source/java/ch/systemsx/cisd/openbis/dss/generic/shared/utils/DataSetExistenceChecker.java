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

package ch.systemsx.cisd.openbis.dss.generic.shared.utils;

import java.io.File;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.time.TimingParameters;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;

/**
 * Helper class checking existence of a data set in a share. Retries existence check in case share is currently not available.
 * 
 * @author Franz-Josef Elmer
 */
public class DataSetExistenceChecker
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DataSetExistenceChecker.class);

    private static final Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY,
            DataSetExistenceChecker.class);

    private final IDataSetDirectoryProvider dataSetDirectoryProvider;

    private final long waitingTime;

    private final int maxRetries;

    public DataSetExistenceChecker(IDataSetDirectoryProvider dataSetDirectoryProvider,
            Properties properties)
    {
        this(dataSetDirectoryProvider, TimingParameters.create(properties));
    }

    public DataSetExistenceChecker(IDataSetDirectoryProvider dataSetDirectoryProvider,
            TimingParameters timingParameters)
    {
        this(dataSetDirectoryProvider, timingParameters.getIntervalToWaitAfterFailureMillis(),
                timingParameters.getMaxRetriesOnFailure());
    }

    public DataSetExistenceChecker(IDataSetDirectoryProvider dataSetDirectoryProvider,
            long waitingTime, int maxRetries)
    {
        this.dataSetDirectoryProvider = dataSetDirectoryProvider;
        this.waitingTime = waitingTime;
        this.maxRetries = maxRetries;
    }

    public boolean dataSetExists(DeletedDataSet dataset)
    {
        return dataSetExists(dataset.getCode(), dataset.getShareIdOrNull(),
                dataset.getLocationOrNull());
    }

    public boolean dataSetExists(IDatasetLocation dataSetLocation)
    {
        String dataSetCode = dataSetLocation.getDataSetCode();
        String shareId = dataSetDirectoryProvider.getShareIdManager().getShareId(dataSetCode);
        return dataSetExists(dataSetCode, shareId, dataSetLocation.getDataSetLocation());
    }

    private boolean dataSetExists(String dataSetCode, String shareId, String location)
    {
        File share = dataSetDirectoryProvider.getDataSetDirectory(shareId, "");
        File dataSetDirectory =
                dataSetDirectoryProvider.getDataSetDirectory(shareId,
                        location);
        if (share.exists())
        {
            return dataSetExists(dataSetDirectory, dataSetCode);
        }
        for (int i = 0; i < maxRetries; i++)
        {
            sleep();
            if (share.exists())
            {
                return dataSetExists(dataSetDirectory, dataSetCode);
            }
        }
        notificationLog.error("Data set '" + dataSetCode + "' couldn't retrieved because share '"
                + share + "' doesn't exists after " + maxRetries + " retries (waiting "
                + waitingTime + " msec between retries).");
        return false;
    }

    private boolean dataSetExists(File dataSetDirectory, String dataSetCode)
    {
        if (dataSetDirectory.exists())
        {
            return true;
        }
        operationLog.warn("Data set '" + dataSetCode + "' no longer exists.");
        return false;
    }

    private void sleep()
    {
        try
        {
            Thread.sleep(waitingTime);
        } catch (InterruptedException ex)
        {
            // silently ignored
        }
    }

}

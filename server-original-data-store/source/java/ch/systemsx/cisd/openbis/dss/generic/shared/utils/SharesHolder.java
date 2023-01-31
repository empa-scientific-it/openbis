/*
 * Copyright 2012 ETH Zuerich, CISD
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
import java.util.Map;

import org.apache.commons.io.FileUtils;

import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogLevel;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * A class that holds all shares and adds the data sets to them on demand.
 * 
 * @author Bernd Rinn
 */
final class SharesHolder
{
    private final String dataStoreCode;

    private final Map<String, Share> shares;

    private final IEncapsulatedOpenBISService service;

    private final ISimpleLogger log;

    private final ITimeProvider timeProvider;

    private boolean areDataSetsAdded;

    SharesHolder(String dataStoreCode, Map<String, Share> shares,
            IEncapsulatedOpenBISService service, ISimpleLogger log, ITimeProvider timeProvider)
    {
        this.dataStoreCode = dataStoreCode;
        this.shares = shares;
        this.service = service;
        this.log = log;
        this.timeProvider = timeProvider;
    }

    /**
     * Adds the datasetts to the stores, if they have not yet been added.
     */
    void addDataSetsToStores()
    {
        if (areDataSetsAdded)
        {
            return;
        }
        for (SimpleDataSetInformationDTO dataSet : service.listPhysicalDataSets())
        {
            String shareId = dataSet.getDataSetShareId();
            if (dataStoreCode.equals(dataSet.getDataStoreCode()))
            {
                Share share = shares.get(shareId);
                String dataSetCode = dataSet.getDataSetCode();
                if (share != null)
                {
                    if (dataSet.getDataSetSize() == null)
                    {
                        final File dataSetInStore =
                                new File(share.getShare(), dataSet.getDataSetLocation());
                        if (FileOperations.getMonitoredInstanceForCurrentThread()
                                .exists(dataSetInStore))
                        {
                            log.log(LogLevel.INFO, "Calculating size of " + dataSetInStore);
                            long t0 = timeProvider.getTimeInMilliseconds();
                            long size = FileUtils.sizeOfDirectory(dataSetInStore);
                            log.log(LogLevel.INFO,
                                    dataSetInStore + " contains " + size + " bytes (calculated in "
                                            + (timeProvider.getTimeInMilliseconds() - t0)
                                            + " msec)");
                            service.updateShareIdAndSize(dataSetCode, shareId, size);
                            dataSet.setDataSetSize(size);
                        } else
                        {
                            log.log(LogLevel.WARN, "Data set " + dataSetCode
                                    + " no longer exists in share " + shareId + ".");
                        }
                    }
                    if (dataSet.getDataSetSize() != null)
                    {
                        share.addDataSet(dataSet);
                    }
                }
            }
        }
        areDataSetsAdded = true;
    }

}
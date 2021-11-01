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

package ch.systemsx.cisd.etlserver.postregistration;

import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus.AVAILABLE;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus.BACKUP_PENDING;

import java.util.Collections;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogLevel;
import ch.systemsx.cisd.common.string.Template;
import ch.systemsx.cisd.openbis.dss.generic.shared.ArchiverTaskContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IArchiverPlugin;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ProcessingStatus;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.translator.DataSetTranslator;

/**
 * A post-registration task executor that archives data sets.
 * 
 * @author Kaloyan Enimanev
 */
class ArchivingExecutor implements IPostRegistrationTaskExecutor
{
    private final String dataSetCode;

    private final IEncapsulatedOpenBISService service;

    private final IDataSetDirectoryProvider dataSetDirectoryProvider;

    private final IHierarchicalContentProvider hierarchicalContentProvider;

    private final IArchiverPlugin archiver;

    private final boolean updateStatus;

    private final Template notificationTemplate;

    private final Logger operationLog;

    private final Logger notificationLog;

    ArchivingExecutor(String dataSetCode, boolean updateStatus, Template notificationTemplate,
            IEncapsulatedOpenBISService service, IArchiverPlugin archiver,
            IDataSetDirectoryProvider dataSetDirectoryProvider,
            IHierarchicalContentProvider hierarchicalContentProvider, Logger operationLog, Logger notificationLog)
    {
        this.dataSetCode = dataSetCode;
        this.updateStatus = updateStatus;
        this.notificationTemplate = notificationTemplate;
        this.service = service;
        this.archiver = archiver;
        this.dataSetDirectoryProvider = dataSetDirectoryProvider;
        this.hierarchicalContentProvider = hierarchicalContentProvider;
        this.operationLog = operationLog;
        this.notificationLog = notificationLog;

    }

    /**
     * archives the dataset for the specified dataset code.
     */
    @Override
    public void execute()
    {

        if (archiver == null)
        {
            // no archiver is configured
            operationLog
                    .error("Post-registration archiving cannot be completed, because there is "
                            + "no archiver configured. Please configure an archiver and restart. ");
            return;
        }

        AbstractExternalData dataSet = tryGetExternalData(dataSetCode, service);
        if (dataSet == null) {
            operationLog.warn("Data set '" + dataSetCode
                    + "' is no longer available in openBIS."
                    + "Archiving post-registration task will be skipped...");
            return;
        }
        if (dataSet instanceof PhysicalDataSet && ((PhysicalDataSet) dataSet).isPresentInArchive()) {
            return;
        }
        if (updateStatus == false) {
            operationLog.info("Archiving data set '" + dataSetCode
                    + "' without updating archiving status.");
        }
        boolean statusUpdated = updateStatus ?
                service.compareAndSetDataSetStatus(dataSetCode, AVAILABLE, BACKUP_PENDING,
                        false) : true;
        if (statusUpdated)
        {
            DatasetDescription dataSetDescription =
                    DataSetTranslator.translateToDescription(dataSet);
            List<DatasetDescription> dataSetAsList =
                    Collections.singletonList(dataSetDescription);
            ArchiverTaskContext context =
                    new ArchiverTaskContext(dataSetDirectoryProvider,
                            hierarchicalContentProvider);
            ProcessingStatus processingStatus = archiver.archive(dataSetAsList, context, false);
            if (false == processingStatus.getErrorStatuses().isEmpty())
            {
                notifyAdministrator(processingStatus, notificationTemplate.createFreshCopy());
            }
            if (updateStatus)
            {
                service.compareAndSetDataSetStatus(dataSetCode, BACKUP_PENDING, AVAILABLE, true);
            }
        }
    }

    private void notifyAdministrator(ProcessingStatus processingStatus, Template template)
    {
        template.bind("dataSet", dataSetCode);
        StringBuilder builder = new StringBuilder();
        for (Status status : processingStatus.getErrorStatuses())
        {
            if (status.tryGetErrorMessage() != null)
            {
                builder.append("Error encountered : " + status.tryGetErrorMessage());
                builder.append("\n");
            }
        }
        template.bind("errors", builder.toString());

        notificationLog.error(template.createText());
    }

    @Override
    public ICleanupTask createCleanupTask()
    {
        return new ArchivingCleanupTask(dataSetCode, updateStatus, service, archiver);
    }

    private static class ArchivingCleanupTask implements ICleanupTask
    {
        private static final long serialVersionUID = 1L;

        private final String dataSetCode;

        private transient IEncapsulatedOpenBISService service;

        private transient IArchiverPlugin archiver;

        private final boolean updateStatus;

        ArchivingCleanupTask(String dataSetCode, boolean updateStatus, IEncapsulatedOpenBISService service,
                IArchiverPlugin archiver)
        {
            this.dataSetCode = dataSetCode;
            this.updateStatus = updateStatus;
            this.service = service;
            this.archiver = archiver;
        }

        @Override
        public void cleanup(ISimpleLogger logger)
        {
            if (updateStatus)
            {
                boolean statusUpdated =
                        getService().compareAndSetDataSetStatus(dataSetCode, BACKUP_PENDING, AVAILABLE,
                                false);

                if (statusUpdated == false)
                {
                    // invalid data set status, do not continue
                    return;
                }
            }

            DatasetDescription dataSet = tryGetDatasetWithLocation(dataSetCode, getService());
            if (getArchiver() != null && dataSet != null && dataSet.getDataSetLocation() != null)
            {
                DatasetLocation dataset = new DatasetLocation();
                dataset.setDatasetCode(dataSetCode);
                dataset.setDataSetLocation(dataSet.getDataSetLocation());

                List<DatasetLocation> dataSetAsList = Collections.singletonList(dataset);
                getArchiver().deleteFromArchive(dataSetAsList);
                logger.log(LogLevel.INFO, "Successfully cleaned up leftovers from incomplete "
                        + "archiving of dataset '" + dataSetCode + "'.");
            }
        }

        private IEncapsulatedOpenBISService getService()
        {
            if (service == null)
            {
                service = ServiceProvider.getOpenBISService();
            }
            return service;
        }

        private IArchiverPlugin getArchiver()
        {
            if (archiver == null)
            {
                archiver = ServiceProvider.getDataStoreService().getArchiverPlugin();
            }
            return archiver;
        }
    }

    private static AbstractExternalData tryGetExternalData(String dataSetCode,
            IEncapsulatedOpenBISService service)
    {
        List<String> codeAsList = Collections.singletonList(dataSetCode);
        List<AbstractExternalData> dataList = service.listDataSetsByCode(codeAsList);
        if (dataList == null || dataList.isEmpty())
        {
            return null;
        }

        AbstractExternalData data = dataList.get(0);
        return data;
    }

    private static DatasetDescription tryGetDatasetWithLocation(String dataSetCode,
            IEncapsulatedOpenBISService service)
    {
        AbstractExternalData data = tryGetExternalData(dataSetCode, service);
        return (data != null) ? DataSetTranslator.translateToDescription(data) : null;
    }
}
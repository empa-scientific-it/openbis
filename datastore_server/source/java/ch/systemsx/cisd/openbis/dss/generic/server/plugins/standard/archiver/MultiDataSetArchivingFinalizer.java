/*
 * Copyright 2014 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.time.TimingParameters;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataStoreServiceInternal;
import ch.systemsx.cisd.openbis.dss.generic.shared.IProcessingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.shared.ProcessingStatus;
import ch.systemsx.cisd.openbis.dss.generic.shared.QueueingDataSetStatusUpdaterService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetCodesWithStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class MultiDataSetArchivingFinalizer implements IProcessingPluginTask
{
    public static final String ORIGINAL_FILE_PATH_KEY = "original-file-path";
    public static final String REPLICATED_FILE_PATH_KEY = "replicated-file-path";
    public static final String FINALIZER_POLLING_TIME_KEY = "finalizer-polling-time";
    public static final String FINALIZER_WAITING_TIME_KEY = "finalizer-waiting-time";
    public static final String STATUS_KEY = "status";
    
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            MultiDataSetArchivingFinalizer.class);

    private static final long serialVersionUID = 1L;
    
    private IDataStoreServiceInternal dataStoreService;

    public MultiDataSetArchivingFinalizer(Properties properties, File storeRoot)
    {
    }
    
    @Override
    public ProcessingStatus process(List<DatasetDescription> datasets, DataSetProcessingContext context)
    {
        List<String> dataSetCodes = extracCodes(datasets);
        Status status = Status.OK;
        try
        {
            Parameters parameters = getParameters(context);
            File originalFile = parameters.getOriginalFile();
            operationLog.info("Waiting for replication of archive '" + originalFile 
                    + "' containing the following data sets: " + CollectionUtils.abbreviate(dataSetCodes, 20));
            boolean noTimeout = waitUntilReplicated(parameters);
            if (noTimeout)
            {
                DataSetArchivingStatus archivingStatus = parameters.getStatus();
                DataSetCodesWithStatus codesWithStatus = new DataSetCodesWithStatus(dataSetCodes, archivingStatus, true);
                getDataStoreService().getDataSetDeleter().scheduleDeletionOfDataSets(datasets,
                        TimingParameters.DEFAULT_MAXIMUM_RETRY_COUNT,
                        TimingParameters.DEFAULT_INTERVAL_TO_WAIT_AFTER_FAILURE_SECONDS);
                updateStatus(codesWithStatus);
            } else
            {
                operationLog.error("Replication of '" + originalFile + "' failed.");
//                ServiceProvider.getDataStoreService().archiveDatasets(sessionToken, userSessionToken, datasets,
//                        userId, userEmailOrNull, removeFromDataStore);
            }
        } catch (Exception ex)
        {
            operationLog.error("Finalizing failed", ex);
            status = Status.createError(ex.getMessage());
        }
        ProcessingStatus processingStatus = new ProcessingStatus();
        processingStatus.addDatasetStatuses(datasets, status);
        return processingStatus;
    }

    protected void updateStatus(DataSetCodesWithStatus codesWithStatus)
    {
        QueueingDataSetStatusUpdaterService.update(codesWithStatus);
    }
    
    IDataStoreServiceInternal getDataStoreService()
    {
        if (dataStoreService == null)
        {
            dataStoreService = ServiceProvider.getDataStoreService();
        }
        return dataStoreService;
    }
    
    private boolean waitUntilReplicated(Parameters parameters)
    {
        File originalFile = parameters.getOriginalFile();
        File replicatedFile = parameters.getReplicatedFile();
        long originalSize = originalFile.length();
        long waitingTime = parameters.getWaitingTime();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        while (stopWatch.getTime() < waitingTime)
        {
            if (replicatedFile.length() >= originalSize)
            {
                stopWatch.stop();
                operationLog.info("File '" + originalFile + "' successfully replicated as '" 
                        + replicatedFile + "' [" + stopWatch + "]");
                return true;
            }
            try
            {
                Thread.sleep(parameters.getPollingTime());
            } catch (InterruptedException ex)
            {
                // silently ignored
            }
        }
        return false;
    }
    
    private List<String> extracCodes(List<DatasetDescription> datasets)
    {
        List<String> codes = new ArrayList<String>();
        for (DatasetDescription dataSet : datasets)
        {
            codes.add(dataSet.getDataSetCode());
        }
        return codes;
    }
    
    private Parameters getParameters(DataSetProcessingContext context)
    {
        Map<String, String> parameterBindings = context.getParameterBindings();
        operationLog.info("Parameters: " + parameterBindings);
        Parameters parameters = new Parameters();
        parameters.setOriginalFile(new File(getProperty(parameterBindings, ORIGINAL_FILE_PATH_KEY)));
        parameters.setReplicatedFile(new File(getProperty(parameterBindings, REPLICATED_FILE_PATH_KEY)));
        parameters.setPollingTime(getNumber(parameterBindings, FINALIZER_POLLING_TIME_KEY));
        parameters.setWaitingTime(getNumber(parameterBindings, FINALIZER_WAITING_TIME_KEY));
        parameters.setStatus(DataSetArchivingStatus.valueOf(getProperty(parameterBindings, STATUS_KEY)));
        
        return parameters;
    }
    
    private long getNumber(Map<String, String> parameterBindings, String property)
    {
        String value = getProperty(parameterBindings, property);
        try
        {
            return Long.parseLong(value);
        } catch (NumberFormatException ex)
        {
            throw new IllegalArgumentException("Property '" + property + "' isn't a number: " + value);
        }
    }
    
    private String getProperty(Map<String, String> parameterBindings, String property)
    {
        String value = parameterBindings.get(property);
        if (StringUtils.isBlank(value))
        {
            throw new IllegalArgumentException("Unknown property '" + property + "'.");
        }
        return value;
    }
    
    private static final class Parameters
    {

        private File originalFile;
        private File replicatedFile;
        private long pollingTime;
        private long waitingTime;
        private DataSetArchivingStatus status;

        public void setOriginalFile(File file)
        {
            originalFile = file;
        }

        public File getOriginalFile()
        {
            return originalFile;
        }
        
        public void setPollingTime(long pollingTime)
        {
            this.pollingTime = pollingTime;
        }
        
        public long getPollingTime()
        {
            return pollingTime;
        }
        
        public void setWaitingTime(long waitingTime)
        {
            this.waitingTime = waitingTime;
        }

        public long getWaitingTime()
        {
            return waitingTime;
        }
        
        public void setReplicatedFile(File file)
        {
            replicatedFile = file;
        }

        public File getReplicatedFile()
        {
            return replicatedFile;
        }

        public void setStatus(DataSetArchivingStatus status)
        {
            this.status = status;
        }
        
        public DataSetArchivingStatus getStatus()
        {
            return status;
        }
    }

}

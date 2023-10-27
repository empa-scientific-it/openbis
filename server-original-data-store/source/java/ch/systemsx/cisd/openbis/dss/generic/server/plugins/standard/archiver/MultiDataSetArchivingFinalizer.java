/*
 * Copyright ETH 2014 - 2023 Zürich, Scientific IT Services
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.SimpleFreeSpaceProvider;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.time.TimingParameters;
import ch.systemsx.cisd.common.utilities.ITimeAndWaitingProvider;
import ch.systemsx.cisd.common.utilities.IWaitingCondition;
import ch.systemsx.cisd.common.utilities.SystemTimeProvider;
import ch.systemsx.cisd.common.utilities.WaitingHelper;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.FileBasedPause;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.RsyncArchiveCopierFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.SshCommandExecutorFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.IMultiDataSetArchiverDBTransaction;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.IMultiDataSetArchiverReadonlyQueryDAO;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDataSetArchiverContainerDTO;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDataSetArchiverDBTransaction;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDataSetArchiverDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDataSetArchiverDataSourceUtil;
import ch.systemsx.cisd.openbis.dss.generic.shared.ArchiverTaskContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDeleter;
import ch.systemsx.cisd.openbis.dss.generic.shared.IProcessingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.shared.ProcessingStatus;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetCodesWithStatus;
import ch.systemsx.cisd.openbis.generic.shared.Constants;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Task which waits until multi data set container file in the archive is replicated and archiving status can be set.
 *
 * @author Franz-Josef Elmer
 */
class MultiDataSetArchivingFinalizer implements IProcessingPluginTask
{
    private static final long serialVersionUID = 1L;

    public static final String CONTAINER_ID_KEY = "container-id";

    public static final String CONTAINER_PATH_KEY = "container-path";

    public static final String ORIGINAL_FILE_PATH_KEY = "original-file-path";

    public static final String REPLICATED_FILE_PATH_KEY = "replicated-file-path";

    public static final String FINALIZER_POLLING_TIME_KEY = "finalizer-polling-time";

    public static final String FINALIZER_MAX_WAITING_TIME_KEY = "finalizer-max-waiting-time";

    public static final String FINALIZER_WAIT_FOR_T_FLAG_KEY = "finalizer-wait-for-t-flag";

    public static final String FINALIZER_SANITY_CHECK_KEY = "finalizer-sanity-check";

    public static final String START_TIME_KEY = "start-time";

    public static final String STATUS_KEY = "status";

    public static final String TIME_STAMP_FORMAT = "yyyyMMdd-HHmmss";

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            MultiDataSetArchivingFinalizer.class);

    private static final Logger machineLog = LogFactory.getLogger(LogCategory.MACHINE,
            MultiDataSetArchivingFinalizer.class);

    private final File pauseFile;

    private final long pauseFilePollingTime;

    private final ITimeAndWaitingProvider timeProvider;

    private final Properties cleanerProperties;

    private transient IMultiDataSetArchiveCleaner cleaner;

    private transient IMultiDataSetArchiverReadonlyQueryDAO readonlyQuery;

    MultiDataSetArchivingFinalizer(Properties cleanerProperties, File pauseFile, long pauseFilePollingTime,
            ITimeAndWaitingProvider timeProvider)
    {
        this.cleanerProperties = cleanerProperties;
        this.pauseFile = pauseFile;
        this.pauseFilePollingTime = pauseFilePollingTime;
        this.timeProvider = timeProvider;
    }

    @Override
    public ProcessingStatus process(List<DatasetDescription> datasets, DataSetProcessingContext context)
    {
        List<String> dataSetCodes = extracCodes(datasets);
        Status status = Status.OK;
        try
        {
            Parameters parameters = getParameters(context);
            DataSetArchivingStatus archivingStatus = parameters.getStatus();
            boolean removeFromDataStore = archivingStatus.isAvailable() == false;
            File originalFile = parameters.getOriginalFile();
            if (originalFile.exists() == false)
            {
                String message = "Replication of '" + originalFile + "' failed because the original file does not exist.";
                status = createStatusAndRearchive(dataSetCodes, parameters, removeFromDataStore, originalFile, message);
            } else
            {
                operationLog.info("Waiting for replication of archive '" + originalFile
                        + "' containing the following data sets: " + CollectionUtils.abbreviate(dataSetCodes, 20));
                boolean noTimeout = waitUntilReplicated(datasets, parameters);
                if (noTimeout && checkMultiDatasetArchiveDatabase(dataSetCodes, parameters))
                {
                    DataSetCodesWithStatus codesWithStatus = new DataSetCodesWithStatus(dataSetCodes, archivingStatus, true);
                    IDataSetDeleter dataSetDeleter = ServiceProvider.getDataStoreService().getDataSetDeleter();
                    if (removeFromDataStore)
                    {
                        dataSetDeleter.scheduleDeletionOfDataSets(datasets,
                                TimingParameters.DEFAULT_MAXIMUM_RETRY_COUNT,
                                TimingParameters.DEFAULT_INTERVAL_TO_WAIT_AFTER_FAILURE_SECONDS);
                    }
                    updateStatus(codesWithStatus);
                } else
                {
                    String message = "Replication of '" + originalFile + "' failed.";
                    status = createStatusAndRearchive(dataSetCodes, parameters, removeFromDataStore, originalFile, message);
                }
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

    private boolean checkMultiDatasetArchiveDatabase(List<String> dataSetCodes, Parameters parameters)
    {
        IMultiDataSetArchiverReadonlyQueryDAO query = getReadonlyQuery();
        Long containerId = parameters.getContainerId();
        MultiDataSetArchiverContainerDTO container = query.getContainerForId(containerId);
        if (container == null)
        {
            operationLog.warn("No container found in Multi Data Set Archive database with container ID "
                    + containerId + ".");
            return false;
        }
        if (parameters.getOriginalFile().getPath().endsWith(container.getPath()) == false)
        {
            operationLog.warn("Archive file '" + parameters.getOriginalFile().getPath() + "' doesn't ends with '"
                    + container.getPath() + "'.");
            return false;
        }
        List<String> dataSetCodesFromDb = query.listDataSetsForContainerId(containerId).stream()
                .map(MultiDataSetArchiverDataSetDTO::getCode).collect(Collectors.toList());
        Collections.sort(dataSetCodesFromDb);
        Collections.sort(dataSetCodes);
        if (dataSetCodes.equals(dataSetCodesFromDb) == false)
        {
            operationLog.warn("Data sets in Multi Data Set Archive database are different from provided data sets: "
                    + "Provided data sets: " + CollectionUtils.abbreviate(dataSetCodes, 30)
                    + ". Data sets in Multi Data Set Archive database: " 
                    + CollectionUtils.abbreviate(dataSetCodesFromDb, 20));
            return false;
        }
        return true;
    }

    private Status createStatusAndRearchive(List<String> dataSetCodes, Parameters parameters, boolean removeFromDataStore, File originalFile,
            String message)
    {
        operationLog.error(message);
        Status status = Status.createError(message);
        getCleaner().delete(originalFile);
        getCleaner().delete(parameters.getReplicatedFile());
        removeFromMapping(parameters.getContainerId(), originalFile);
        updateStatus(new DataSetCodesWithStatus(dataSetCodes, DataSetArchivingStatus.AVAILABLE, false));
        HashMap<String, String> options = new HashMap<>();
        if (parameters.getSubDirectory() != null)
        {
            options.put(Constants.SUB_DIR_KEY, parameters.getSubDirectory());
        }
        ServiceProvider.getOpenBISService().archiveDataSets(dataSetCodes, removeFromDataStore, options);
        return status;
    }

    private void removeFromMapping(Long containerId, File originalFile)
    {
        IMultiDataSetArchiverDBTransaction transaction = getTransaction();
        try
        {
            if (containerId != null)
            {
                transaction.deleteContainer(containerId);
            } else
            {
                transaction.deleteContainer(originalFile.getName());
            }
            transaction.commit();
        } catch (Exception ex)
        {
            transaction.rollback();
        }
        transaction.close();
    }

    protected void updateStatus(DataSetCodesWithStatus codesWithStatus)
    {
        ServiceProvider.getOpenBISService().updateDataSetStatuses(codesWithStatus.getDataSetCodes(),
                codesWithStatus.getStatus(), codesWithStatus.isPresentInArchive());
    }

    protected IMultiDataSetArchiveCleaner getCleaner()
    {
        if (cleaner == null)
        {
            cleaner = MultiDataSetArchivingUtils.createCleaner(cleanerProperties);
        }
        return cleaner;
    }

    protected IMultiDataSetArchiverReadonlyQueryDAO getReadonlyQuery()
    {
        if (readonlyQuery == null)
        {
            readonlyQuery = MultiDataSetArchiverDataSourceUtil.getReadonlyQueryDAO();
        }
        return readonlyQuery;
    }

    IMultiDataSetArchiverDBTransaction getTransaction()
    {
        return new MultiDataSetArchiverDBTransaction();
    }

    private boolean waitUntilReplicated(List<DatasetDescription> dataSets, Parameters parameters)
    {
        final File originalFile = parameters.getOriginalFile();
        final File replicatedFile = parameters.getReplicatedFile();
        final long originalSize = originalFile.length();
        long waitingTime = parameters.getWaitingTime();
        Log4jSimpleLogger logger = new Log4jSimpleLogger(operationLog);
        WaitingHelper waitingHelper = new WaitingHelper(waitingTime, parameters.getPollingTime(), timeProvider, logger, true);
        long startTime = parameters.getStartTime();
        FileBasedPause pause = new FileBasedPause(pauseFile, pauseFilePollingTime, timeProvider, logger,
                "Waiting for replicated file " + parameters.getReplicatedFile());
        boolean replicatedFileReady = waitingHelper.waitOn(startTime, new IWaitingCondition()
        {
            @Override
            public boolean conditionFulfilled()
            {
                if (replicatedFile.length() != originalSize)
                {
                    operationLog.info(
                            "Waiting for the file size of the replicated file to match the original file size. Replicated file: "
                                    + replicatedFile.getAbsolutePath());
                    return false;
                } else
                {
                    operationLog.info(
                            "Replicated file has the same file size as the original file. Replicated file: " + replicatedFile.getAbsolutePath());
                }

                if (parameters.isWaitForTFlag())
                {
                    if (!MultiDataSetArchivingUtils.isTFlagSet(replicatedFile, operationLog, machineLog))
                    {
                        operationLog.info(
                                "Waiting for T flag to be set on the replicated file. Replicated file: " + replicatedFile.getAbsolutePath());
                        return false;
                    } else
                    {
                        operationLog.info("Replicated file has T flag set. Replicated file: " + replicatedFile.getAbsolutePath());
                    }
                }

                return true;
            }

            @Override
            public String toString()
            {
                return FileUtilities.byteCountToDisplaySize(replicatedFile.length())
                        + " of " + FileUtilities.byteCountToDisplaySize(originalSize)
                        + " are replicated for " + originalFile;
            }
        }, pause);

        if (replicatedFileReady)
        {
            if (parameters.isSanityCheck())
            {
                operationLog.info("Starting sanity check of the file archived in the replicated destination. Replicated file: "
                        + replicatedFile.getAbsolutePath());
                try
                {
                    if (parameters.isWaitForSanityCheck())
                    {
                        createSanityCheckAction(dataSets, parameters).callWithRetry();
                    } else
                    {
                        createSanityCheckAction(dataSets, parameters).call();
                    }
                } catch (Exception e)
                {
                    operationLog.error("Failed sanity check of the file archived in the replicated destination. Replicated file: "
                            + replicatedFile.getAbsolutePath(), e);
                    return false;
                }
            }
            return true;
        } else
        {
            return false;
        }
    }

    private RetryCaller<Map<String, Status>, RuntimeException> createSanityCheckAction(List<DatasetDescription> dataSets, Parameters parameters)
    {
        return new RetryCaller<Map<String, Status>, RuntimeException>(parameters.getWaitForSanityCheckInitialWaitingTime(),
                parameters.getWaitForSanityCheckMaxWaitingTime(),
                new Log4jSimpleLogger(operationLog))
        {
            @Override protected Map<String, Status> call()
            {
                ArchiverTaskContext archiverContext = new ArchiverTaskContext(
                        ServiceProvider.getDataStoreService().getDataSetDirectoryProvider(),
                        ServiceProvider.getHierarchicalContentProvider());

                Properties archiverProperties = ServiceProvider.getDataStoreService().getArchiverProperties();

                MultiDataSetFileOperationsManager operationsManager = new MultiDataSetFileOperationsManager(
                        archiverProperties, new RsyncArchiveCopierFactory(), new SshCommandExecutorFactory(),
                        new SimpleFreeSpaceProvider(), SystemTimeProvider.SYSTEM_TIME_PROVIDER);

                IHierarchicalContent replicaContent = null;

                try
                {
                    replicaContent =
                            operationsManager.getReplicaAsHierarchicalContent(parameters.getContainerPath(), dataSets);

                    return MultiDataSetArchivingUtils.sanityCheck(replicaContent, dataSets, parameters.isSanityCheckVerifyChecksums(),
                            archiverContext, new Log4jSimpleLogger(operationLog));
                } finally
                {
                    if (replicaContent != null)
                    {
                        try
                        {
                            replicaContent.close();
                        } catch (Exception e)
                        {
                            operationLog.warn("Could not close replicated content node", e);
                        }
                    }
                }
            }
        };
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

        if (parameterBindings.containsKey(CONTAINER_ID_KEY))
        {
            parameters.setContainerId(getNumber(parameterBindings, CONTAINER_ID_KEY));
        }
        if (parameterBindings.containsKey(CONTAINER_PATH_KEY))
        {
            parameters.setContainerPath(getProperty(parameterBindings, CONTAINER_PATH_KEY));
        }

        parameters.setOriginalFile(new File(getProperty(parameterBindings, ORIGINAL_FILE_PATH_KEY)));
        parameters.setReplicatedFile(new File(getProperty(parameterBindings, REPLICATED_FILE_PATH_KEY)));
        parameters.setPollingTime(getNumber(parameterBindings, FINALIZER_POLLING_TIME_KEY));
        parameters.setStartTime(getTimestamp(parameterBindings, START_TIME_KEY));
        parameters.setWaitingTime(getNumber(parameterBindings, FINALIZER_MAX_WAITING_TIME_KEY));
        parameters.setStatus(DataSetArchivingStatus.valueOf(getProperty(parameterBindings, STATUS_KEY)));
        parameters.setSubDirectory(parameterBindings.get(Constants.SUB_DIR_KEY));

        if (parameterBindings.containsKey(MultiDataSetArchivingFinalizer.FINALIZER_WAIT_FOR_T_FLAG_KEY))
        {
            parameters.setWaitForTFlag(getBoolean(parameterBindings, MultiDataSetArchivingFinalizer.FINALIZER_WAIT_FOR_T_FLAG_KEY));
        } else
        {
            parameters.setWaitForTFlag(MultiDataSetArchiver.DEFAULT_FINALIZER_WAIT_FOR_T_FLAG);
        }

        if (parameterBindings.containsKey(FINALIZER_SANITY_CHECK_KEY))
        {
            parameters.setSanityCheck(getBoolean(parameterBindings, FINALIZER_SANITY_CHECK_KEY));
        } else
        {
            parameters.setSanityCheck(MultiDataSetArchiver.DEFAULT_FINALIZER_SANITY_CHECK);
        }

        if (parameterBindings.containsKey(MultiDataSetArchiver.WAIT_FOR_SANITY_CHECK_KEY))
        {
            parameters.setWaitForSanityCheck(getBoolean(parameterBindings, MultiDataSetArchiver.WAIT_FOR_SANITY_CHECK_KEY));
        } else
        {
            parameters.setWaitForSanityCheck(MultiDataSetArchiver.DEFAULT_WAIT_FOR_SANITY_CHECK);
        }

        if (parameterBindings.containsKey(MultiDataSetArchiver.WAIT_FOR_SANITY_CHECK_INITIAL_WAITING_TIME_KEY))
        {
            parameters.setWaitForSanityCheckInitialWaitingTime(
                    getNumber(parameterBindings, MultiDataSetArchiver.WAIT_FOR_SANITY_CHECK_INITIAL_WAITING_TIME_KEY));
        } else
        {
            parameters.setWaitForSanityCheckInitialWaitingTime(MultiDataSetArchiver.DEFAULT_WAIT_FOR_SANITY_CHECK_INITIAL_WAITING_TIME);
        }

        if (parameterBindings.containsKey(MultiDataSetArchiver.WAIT_FOR_SANITY_CHECK_MAX_WAITING_TIME_KEY))
        {
            parameters.setWaitForSanityCheckMaxWaitingTime(
                    getNumber(parameterBindings, MultiDataSetArchiver.WAIT_FOR_SANITY_CHECK_MAX_WAITING_TIME_KEY));
        } else
        {
            parameters.setWaitForSanityCheckMaxWaitingTime(MultiDataSetArchiver.DEFAULT_WAIT_FOR_SANITY_CHECK_MAX_WAITING_TIME);
        }

        if (parameterBindings.containsKey(MultiDataSetArchiver.SANITY_CHECK_VERIFY_CHECKSUMS_KEY))
        {
            parameters.setSanityCheckVerifyChecksums(getBoolean(parameterBindings, MultiDataSetArchiver.SANITY_CHECK_VERIFY_CHECKSUMS_KEY));
        } else
        {
            parameters.setSanityCheckVerifyChecksums(MultiDataSetArchiver.DEFAULT_SANITY_CHECK_VERIFY_CHECKSUMS);
        }

        return parameters;
    }

    private long getTimestamp(Map<String, String> parameterBindings, String property)
    {
        String value = getProperty(parameterBindings, property);
        try
        {
            return new SimpleDateFormat(TIME_STAMP_FORMAT).parse(value).getTime();
        } catch (ParseException ex)
        {
            throw new IllegalArgumentException("Property '" + property + "' isn't a time stamp of format "
                    + TIME_STAMP_FORMAT + ": " + value);
        }
    }

    private boolean getBoolean(Map<String, String> parameterBindings, String property)
    {
        String value = getProperty(parameterBindings, property);
        return Boolean.parseBoolean(value);
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

        private long startTime;

        private long waitingTime;

        private DataSetArchivingStatus status;

        private String subDirectory;

        private Long containerId;

        private String containerPath;

        private boolean sanityCheck;

        private boolean waitForSanityCheck;

        private long waitForSanityCheckInitialWaitingTime;

        private long waitForSanityCheckMaxWaitingTime;

        private boolean sanityCheckVerifyChecksums;

        private boolean waitForTFlag;

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

        public long getStartTime()
        {
            return startTime;
        }

        public void setStartTime(long startTime)
        {
            this.startTime = startTime;
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

        public String getSubDirectory()
        {
            return subDirectory;
        }

        public void setSubDirectory(String groupKey)
        {
            this.subDirectory = groupKey;
        }

        public Long getContainerId()
        {
            return containerId;
        }

        public void setContainerId(Long containerId)
        {
            this.containerId = containerId;
        }

        public String getContainerPath()
        {
            return containerPath;
        }

        public void setContainerPath(final String containerPath)
        {
            this.containerPath = containerPath;
        }

        public boolean isSanityCheck()
        {
            return sanityCheck;
        }

        public void setSanityCheck(final boolean sanityCheck)
        {
            this.sanityCheck = sanityCheck;
        }

        public boolean isWaitForSanityCheck()
        {
            return waitForSanityCheck;
        }

        public void setWaitForSanityCheck(final boolean waitForSanityCheck)
        {
            this.waitForSanityCheck = waitForSanityCheck;
        }

        public long getWaitForSanityCheckInitialWaitingTime()
        {
            return waitForSanityCheckInitialWaitingTime;
        }

        public void setWaitForSanityCheckInitialWaitingTime(final long waitForSanityCheckInitialWaitingTime)
        {
            this.waitForSanityCheckInitialWaitingTime = waitForSanityCheckInitialWaitingTime;
        }

        public long getWaitForSanityCheckMaxWaitingTime()
        {
            return waitForSanityCheckMaxWaitingTime;
        }

        public void setWaitForSanityCheckMaxWaitingTime(final long waitForSanityCheckMaxWaitingTime)
        {
            this.waitForSanityCheckMaxWaitingTime = waitForSanityCheckMaxWaitingTime;
        }

        public boolean isSanityCheckVerifyChecksums()
        {
            return sanityCheckVerifyChecksums;
        }

        public void setSanityCheckVerifyChecksums(final boolean sanityCheckVerifyChecksums)
        {
            this.sanityCheckVerifyChecksums = sanityCheckVerifyChecksums;
        }

        public boolean isWaitForTFlag()
        {
            return waitForTFlag;
        }

        public void setWaitForTFlag(final boolean waitForTFlag)
        {
            this.waitForTFlag = waitForTFlag;
        }

    }

}

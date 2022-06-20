/*
 * Copyright 2014 ETH Zuerich, CISD
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
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateUtils;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.NotImplementedException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.BooleanStatus;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IFreeSpaceProvider;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.time.DateTimeUtils;
import ch.systemsx.cisd.common.utilities.ITimeAndWaitingProvider;
import ch.systemsx.cisd.common.utilities.SystemTimeProvider;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.AbstractArchiverProcessingPlugin;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.RsyncArchiveCopierFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.SshCommandExecutorFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.IMultiDataSetArchiverDBTransaction;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.IMultiDataSetArchiverReadonlyQueryDAO;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDataSetArchiverContainerDTO;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDataSetArchiverDBTransaction;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDataSetArchiverDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDataSetArchiverDataSourceUtil;
import ch.systemsx.cisd.openbis.dss.generic.shared.ArchiverTaskContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataStoreServiceInternal;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.IUnarchivingPreparation;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DataSetAndPathInfoDBConsistencyChecker;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.SegmentedStoreUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.Share;
import ch.systemsx.cisd.openbis.generic.shared.Constants;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;
import ch.systemsx.cisd.openbis.generic.shared.translator.SimpleDataSetHelper;

/**
 * @author Jakub Straszewski
 */
public class MultiDataSetArchiver extends AbstractArchiverProcessingPlugin
{
    static final String ARCHIVING_FINALIZER = "Archiving Finalizer";

    private static final long serialVersionUID = 1L;

    private transient IMultiDataSetFileOperationsManager fileOperations;

    private final FileOperationsManagerFactory fileOperationsFactory;

    private static class FileOperationsManagerFactory implements Serializable
    {
        private static final long serialVersionUID = 1L;

        private final Properties properties;

        private final ITimeAndWaitingProvider timeProvider;

        private IFreeSpaceProvider freeSpaceProviderOrNull;

        private FileOperationsManagerFactory(Properties properties, ITimeAndWaitingProvider timeProvider,
                IFreeSpaceProvider freeSpaceProviderOrNull)
        {
            this.properties = properties;
            this.timeProvider = timeProvider;
            this.freeSpaceProviderOrNull = freeSpaceProviderOrNull;
        }

        private IMultiDataSetFileOperationsManager create()
        {
            return new MultiDataSetFileOperationsManager(properties, new RsyncArchiveCopierFactory(),
                    new SshCommandExecutorFactory(), freeSpaceProviderOrNull, timeProvider);
        }
    }

    public static final String MINIMUM_FREE_SPACE_AT_FINAL_DESTINATION_IN_BYTES = "minimum-free-space-at-final-destination-in-bytes";

    public static final String MINIMUM_FREE_SPACE_AT_FINAL_DESTINATION_IN_PERCENTAGE = "minimum-free-space-at-final-destination-in-percentage";

    public static final String MINIMUM_CONTAINER_SIZE_IN_BYTES = "minimum-container-size-in-bytes";

    public static final Long DEFAULT_MINIMUM_CONTAINER_SIZE_IN_BYTES = 10 * FileUtils.ONE_GB;

    public static final String MAXIMUM_CONTAINER_SIZE_IN_BYTES = "maximum-container-size-in-bytes";

    public static final String MAXIMUM_UNARCHIVING_CAPACITY_IN_MEGABYTES = "maximum-unarchiving-capacity-in-megabytes";

    public static final Long DEFAULT_MAXIMUM_CONTAINER_SIZE_IN_BYTES = 80 * FileUtils.ONE_GB;

    public static final long DEFAULT_FINALIZER_POLLING_TIME = DateUtils.MILLIS_PER_MINUTE;

    public static final long DEFAULT_FINALIZER_MAX_WAITING_TIME = DateUtils.MILLIS_PER_DAY;

    public static final Long DEFAULT_UNARCHIVING_CAPACITY_IN_MEGABYTES = 1000 * FileUtils.ONE_GB;

    public static final String DELAY_UNARCHIVING = "delay-unarchiving";

    public static final String CHECK_CONISTENCY = "check-consistency-between-store-and-pathinfo-db";

    public static final String CLEANER_PROPS = "cleaner";

    private transient IMultiDataSetArchiverReadonlyQueryDAO readonlyQuery;

    private transient IDataStoreServiceInternal dataStoreService;

    private transient IMultiDataSetArchiveCleaner cleaner;

    private final long absoluteMinimumFreeSpaceAtDestination;

    private final int relativeMinimumFreeSpaceAtDestination;

    private final String finalDestination;

    private final long minimumContainerSize;

    private final long maximumContainerSize;

    private final long maximumUnarchivingCapacityInMB;

    private final boolean delayUnarchiving;

    private final long finalizerPollingTime;

    private final long finalizerMaxWaitingTime;

    private final Properties cleanerProperties;

    private ITimeAndWaitingProvider timeProvider;

    private boolean checkConsistency;

    public MultiDataSetArchiver(Properties properties, File storeRoot)
    {
        this(properties, storeRoot, SystemTimeProvider.SYSTEM_TIME_PROVIDER, null);
    }

    MultiDataSetArchiver(Properties properties, File storeRoot, ITimeAndWaitingProvider timeProvider,
            IFreeSpaceProvider freeSpaceProviderOrNull)
    {
        super(properties, storeRoot, null, null);
        this.timeProvider = timeProvider;
        delayUnarchiving = PropertyUtils.getBoolean(properties, DELAY_UNARCHIVING, false);
        checkConsistency = PropertyUtils.getBoolean(properties, CHECK_CONISTENCY, true);

        absoluteMinimumFreeSpaceAtDestination = PropertyUtils.getLong(properties, MINIMUM_FREE_SPACE_AT_FINAL_DESTINATION_IN_BYTES, -1L);
        relativeMinimumFreeSpaceAtDestination = PropertyUtils.getInt(properties, MINIMUM_FREE_SPACE_AT_FINAL_DESTINATION_IN_PERCENTAGE, -1);
        finalDestination = properties.getProperty(MultiDataSetFileOperationsManager.FINAL_DESTINATION_KEY);
        this.minimumContainerSize = PropertyUtils.getLong(properties, MINIMUM_CONTAINER_SIZE_IN_BYTES, DEFAULT_MINIMUM_CONTAINER_SIZE_IN_BYTES);
        this.maximumContainerSize = PropertyUtils.getLong(properties, MAXIMUM_CONTAINER_SIZE_IN_BYTES, DEFAULT_MAXIMUM_CONTAINER_SIZE_IN_BYTES);
        this.maximumUnarchivingCapacityInMB =
                PropertyUtils.getLong(properties, MAXIMUM_UNARCHIVING_CAPACITY_IN_MEGABYTES, DEFAULT_UNARCHIVING_CAPACITY_IN_MEGABYTES);
        this.fileOperationsFactory = new FileOperationsManagerFactory(properties, timeProvider, freeSpaceProviderOrNull);
        finalizerPollingTime = DateTimeUtils.getDurationInMillis(properties,
                MultiDataSetArchivingFinalizer.FINALIZER_POLLING_TIME_KEY, DEFAULT_FINALIZER_POLLING_TIME);
        finalizerMaxWaitingTime = DateTimeUtils.getDurationInMillis(properties,
                MultiDataSetArchivingFinalizer.FINALIZER_MAX_WAITING_TIME_KEY, DEFAULT_FINALIZER_MAX_WAITING_TIME);
        cleanerProperties = PropertyParametersUtil.extractSingleSectionProperties(properties, CLEANER_PROPS, false)
                .getProperties();
        getCleaner(); // Checks proper configuration of cleaner
    }

    @Override
    public boolean isArchivingPossible()
    {
        boolean possible = super.isArchivingPossible();
        if (possible && (absoluteMinimumFreeSpaceAtDestination > 0 || relativeMinimumFreeSpaceAtDestination > 0))
        {
            try
            {
                FileStore fileStore = Files.getFileStore(new File(finalDestination).toPath());
                long usableSpace = fileStore.getUsableSpace();
                if (absoluteMinimumFreeSpaceAtDestination > 0)
                {
                    possible = usableSpace >= absoluteMinimumFreeSpaceAtDestination;
                    if (possible == false)
                    {
                        operationLog.warn("Archiving not triggered because the usable free space "
                                + FileUtils.byteCountToDisplaySize(usableSpace)
                                + " is less then the specified minimum free space "
                                + FileUtils.byteCountToDisplaySize(absoluteMinimumFreeSpaceAtDestination));
                    }
                }
                if (possible && relativeMinimumFreeSpaceAtDestination > 0)
                {
                    long totalSpace = fileStore.getTotalSpace();
                    long percentage = (usableSpace * 100) / totalSpace;
                    possible = percentage >= relativeMinimumFreeSpaceAtDestination;
                    if (possible == false)
                    {
                        operationLog.warn("Archiving not triggered because the usable free space is only "
                                + percentage + "% of the total space "
                                + FileUtils.byteCountToDisplaySize(totalSpace) + " but the specified minimum is "
                                + relativeMinimumFreeSpaceAtDestination + "%.");
                    }
                }
            } catch (IOException e)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(e);
            }
        }
        return possible;
    }

    @Override
    protected DatasetProcessingStatuses doArchive(List<DatasetDescription> paramDataSets,
            ArchiverTaskContext context, boolean removeFromDataStore)
    {
        LinkedList<DatasetDescription> dataSets = new LinkedList<DatasetDescription>(paramDataSets);
        MultiDataSetProcessingStatuses result = new MultiDataSetProcessingStatuses();

        filterBasedOnArchiveStatus(dataSets, result, FilterOption.FILTER_ARCHIVED, Status.OK, Operation.ARCHIVE);

        if (dataSets.isEmpty())
        {
            return result;
        }

        IMultiDataSetArchiverDBTransaction transaction = getTransaction();

        try
        {
            verifyDataSetsSize(dataSets);

            MultiDataSetProcessingStatuses archiveResult = archiveDataSets(dataSets, context, removeFromDataStore, transaction);

            result.addResults(archiveResult);

            transaction.commit();
            transaction.close();
        } catch (Exception e)
        {
            operationLog.warn("Archiving of " + dataSets.size() + " data sets failed", e);
            try
            {
                transaction.rollback();
                transaction.close();
            } catch (Exception ex)
            {
                operationLog.warn("Rollback of multi dataset db transaction failed", ex);
            }
            result.addResult(dataSets, Status.createError(e.getMessage()), Operation.ARCHIVE);
        }
        operationLog
                .info("archiving done. result: " + ((MultiDataSetArchiver.MultiDataSetProcessingStatuses) result).getDataSetsWaitingForReplication());
        return result;
    }

    private static enum FilterOption
    {
        FILTER_ARCHIVED,
        FILTER_UNARCHIVED
    }

    /**
     * NOTE: This method MODIFIES both arguments. It removes items from dataSets list and adds values to result. Re Iterate over the
     * <code>dataSets</code> and removes those which are present in the archive already (or not present, depending on the <code>filterOption</code>).
     * For those removed data sets it adds entry with <code>status</code> for <code>operation</code> in <code>result</code>
     */
    private void filterBasedOnArchiveStatus(LinkedList<? extends IDatasetLocation> dataSets,
            DatasetProcessingStatuses result, FilterOption filterOption, Status status, Operation operation)
    {
        for (Iterator<? extends IDatasetLocation> iterator = dataSets.iterator(); iterator.hasNext();)
        {
            IDatasetLocation dataSet = iterator.next();
            boolean isPresentInArchive = isDataSetPresentInArchive(dataSet.getDataSetCode());

            boolean isFiltered;

            switch (filterOption)
            {
                case FILTER_ARCHIVED:
                    isFiltered = isPresentInArchive;
                    break;
                case FILTER_UNARCHIVED:
                    isFiltered = (false == isPresentInArchive);
                    break;
                default:
                    throw new IllegalStateException("All cases should be covered");
            }

            if (isFiltered)
            {
                result.addResult(dataSet.getDataSetCode(), status, operation);
                iterator.remove();
            }
        }
    }

    private void verifyDataSetsSize(List<DatasetDescription> dataSets)
    {
        long datasetSize = SegmentedStoreUtils.calculateTotalSize(dataSets);
        if (dataSets.size() == 1)
        {
            if (datasetSize < minimumContainerSize)
            {
                throw new IllegalArgumentException("Dataset " + dataSets.get(0).getDataSetCode()
                        + " is too small (" + FileUtilities.byteCountToDisplaySize(datasetSize)
                        + ") to be archived with multi dataset archiver because minimum size is "
                        + FileUtilities.byteCountToDisplaySize(minimumContainerSize) + ".");
            }
            // if single dataset is bigger than specified maximum, we should still allow it being
        } else
        {
            if (datasetSize < minimumContainerSize)
            {
                throw new IllegalArgumentException("Set of data sets specified for archiving is too small ("
                        + FileUtilities.byteCountToDisplaySize(datasetSize)
                        + ") to be archived with multi dataset archiver because minimum size is "
                        + FileUtilities.byteCountToDisplaySize(minimumContainerSize) + ".");
            } else if (datasetSize > maximumContainerSize)
            {
                throw new IllegalArgumentException("Set of data sets specified for archiving is too big ("
                        + FileUtilities.byteCountToDisplaySize(datasetSize)
                        + ") to be archived with multi dataset archiver because maximum size is "
                        + FileUtilities.byteCountToDisplaySize(maximumContainerSize) + ".");
            }
        }
    }

    private MultiDataSetProcessingStatuses archiveDataSets(List<DatasetDescription> dataSets, ArchiverTaskContext context,
            boolean removeFromDataStore, IMultiDataSetArchiverDBTransaction transaction) throws Exception
    {
        assertConcistencyBetweenDataStoreAndPathinfoDB(dataSets);
        MultiDataSetProcessingStatuses statuses = new MultiDataSetProcessingStatuses();

        // for sharding we use the location of the first datast
        String containerPath = createContainerPath(dataSets, context);

        long containerId = establishContainerDataSetMapping(dataSets, containerPath, transaction);

        IHierarchicalContent archivedContent = null;
        try
        {
            Status status = getFileOperations().createContainer(containerPath, dataSets);
            if (status.isError())
            {
                throw new Exception("Couldn't create archive file " + containerPath
                        + ". Reason: " + status.tryGetErrorMessage());
            }
            archivedContent = getFileOperations().getContainerAsHierarchicalContent(containerPath, dataSets);

            checkArchivedDataSets(archivedContent, dataSets, context, statuses);
            scheduleFinalizer(containerPath, containerId, dataSets, context, removeFromDataStore, statuses);
        } catch (Exception ex)
        {
            getFileOperations().deleteContainerFromFinalDestination(getCleaner(), containerPath);
            // In case of error we actually should delete failed container here. If the transaction fail that the AbstractArchiver is unable to locate
            // container file.
            throw ex;
        } finally
        {
            // always delete staging content
            getFileOperations().deleteContainerFromStage(getCleaner(), containerPath);

            if (archivedContent != null)
            {
                archivedContent.close();
            }
        }
        return statuses;
    }

    private String createContainerPath(List<DatasetDescription> dataSets, ArchiverTaskContext context)
    {
        String containerPath = getFileOperations().generateContainerPath(dataSets);
        String subDirectory = tryGetSubDirectory(context);
        if (subDirectory != null)
        {
            containerPath = subDirectory + "/" + containerPath;
        }
        return containerPath;
    }

    private String tryGetSubDirectory(ArchiverTaskContext context)
    {
        Map<String, String> options = context.getOptions();
        return options != null ? options.get(Constants.SUB_DIR_KEY) : null;
    }

    private long establishContainerDataSetMapping(List<DatasetDescription> dataSets, String containerPath,
            IMultiDataSetArchiverDBTransaction transaction)
    {
        MultiDataSetArchiverContainerDTO container = transaction.createContainer(containerPath);
        for (DatasetDescription dataSet : dataSets)
        {
            transaction.insertDataset(dataSet, container);
        }
        return container.getId();
    }

    private void scheduleFinalizer(String containerPath, long containerId, List<DatasetDescription> dataSets,
            ArchiverTaskContext archiverContext, boolean removeFromDataStore,
            MultiDataSetProcessingStatuses statuses)
    {
        if (needsToWaitForReplication() == false)
        {
            return;
        }
        statuses.setDataSetsWaitingForReplication(dataSets);
        MultiDataSetArchivingFinalizer task = new MultiDataSetArchivingFinalizer(cleanerProperties, pauseFile,
                pauseFilePollingTime, getTimeProvider());
        String userId = archiverContext.getUserId();
        String userEmail = archiverContext.getUserEmail();
        String userSessionToken = archiverContext.getUserSessionToken();
        HashMap<String, String> parameterBindings = new LinkedHashMap<String, String>();
        IMultiDataSetFileOperationsManager operations = getFileOperations();
        String groupKey = tryGetSubDirectory(archiverContext);
        if (groupKey != null)
        {
            parameterBindings.put(Constants.SUB_DIR_KEY, groupKey);
        }
        parameterBindings.put(MultiDataSetArchivingFinalizer.CONTAINER_ID_KEY, Long.toString(containerId));
        parameterBindings.put(MultiDataSetArchivingFinalizer.ORIGINAL_FILE_PATH_KEY,
                operations.getOriginalArchiveFilePath(containerPath));
        parameterBindings.put(MultiDataSetArchivingFinalizer.REPLICATED_FILE_PATH_KEY,
                operations.getReplicatedArchiveFilePath(containerPath));
        parameterBindings.put(MultiDataSetArchivingFinalizer.FINALIZER_POLLING_TIME_KEY, Long.toString(finalizerPollingTime));
        SimpleDateFormat dateFormat = new SimpleDateFormat(MultiDataSetArchivingFinalizer.TIME_STAMP_FORMAT);
        parameterBindings.put(MultiDataSetArchivingFinalizer.START_TIME_KEY, dateFormat.format(getTimeProvider().getTimeInMilliseconds()));
        parameterBindings.put(MultiDataSetArchivingFinalizer.FINALIZER_MAX_WAITING_TIME_KEY, Long.toString(finalizerMaxWaitingTime));
        DataSetArchivingStatus status = removeFromDataStore ? DataSetArchivingStatus.ARCHIVED : DataSetArchivingStatus.AVAILABLE;
        parameterBindings.put(MultiDataSetArchivingFinalizer.STATUS_KEY, status.toString());
        getDataStoreService().scheduleTask(ARCHIVING_FINALIZER, task, parameterBindings, dataSets,
                userId, userEmail, userSessionToken);
    }

    private boolean needsToWaitForReplication()
    {
        return getFileOperations().isReplicatedArchiveDefined();
    }

    private void assertConcistencyBetweenDataStoreAndPathinfoDB(List<DatasetDescription> dataSets)
    {
        if (checkConsistency)
        {
            operationLog.info("Starts consistency check between data store and pathinfo database for "
                    + CollectionUtils.abbreviate(dataSets, 20));
            DataSetAndPathInfoDBConsistencyChecker createChecker = createChecker();
            createChecker.check(dataSets);
            operationLog.info("Consistency check finished.");
            if (createChecker.noErrorAndInconsistencyFound() == false)
            {
                throw new EnvironmentFailureException("Inconsistency between data store and pathinfo database: "
                        + createChecker.createReport());
            }
        }
    }

    protected DataSetAndPathInfoDBConsistencyChecker createChecker()
    {
        return new DataSetAndPathInfoDBConsistencyChecker(null, null);
    }

    private void checkArchivedDataSets(IHierarchicalContent archivedContent, List<DatasetDescription> dataSets,
            ArchiverTaskContext context, DatasetProcessingStatuses statuses)
    {
        Map<String, Status> statusMap = MultiDataSetArchivingUtils.sanityCheck(archivedContent, dataSets, context,
                new Log4jSimpleLogger(operationLog));

        if (needsToWaitForReplication() == false)
        {
            for (String dataSetCode : statusMap.keySet())
            {
                statuses.addResult(dataSetCode, statusMap.get(dataSetCode), Operation.ARCHIVE);
            }
        }
    }

    @Override
    protected List<DatasetDescription> getArchivedDataSets(List<DatasetDescription> datasets, DatasetProcessingStatuses statuses)
    {
        List<DatasetDescription> archivedDataSets = super.getArchivedDataSets(datasets, statuses);
        if (statuses instanceof MultiDataSetProcessingStatuses)
        {
            List<DatasetDescription> dataSetsWaitingForReplication = ((MultiDataSetProcessingStatuses) statuses).getDataSetsWaitingForReplication();
            if (dataSetsWaitingForReplication != null)
            {
                archivedDataSets.removeAll(dataSetsWaitingForReplication);
            }
        }
        return archivedDataSets;
    }

    @Override
    public List<String> getDataSetCodesForUnarchiving(List<String> dataSetCodes)
    {
        Set<Long> containerIds = assertUnarchivingCapacityNotExceeded(dataSetCodes);
        return getCodesOfAllDataSetsInContainer(containerIds);
    }

    @Override
    protected IUnarchivingPreparation getUnarchivingPreparation()
    {
        Share scratchShare = findScratchShare();

        IDataSetDirectoryProvider directoryProvider = ServiceProvider.getDataStoreService().getDataSetDirectoryProvider();

        return new MultiDataSetUnarchivingPreparations(scratchShare, getShareIdManager(), getService(), directoryProvider);
    }

    private Share findScratchShare()
    {
        IEncapsulatedOpenBISService service = getService();
        IFreeSpaceProvider freeSpaceProvider = createFreeSpaceProvider();
        ISimpleLogger logger = new Log4jSimpleLogger(operationLog);
        return MultiDataSetArchivingUtils.getScratchShare(this.storeRoot, service, freeSpaceProvider,
                ServiceProvider.getConfigProvider(), logger);
    }

    private static class MultiDataSetUnarchivingPreparations implements IUnarchivingPreparation
    {
        private final Share scratchShare;

        private final IEncapsulatedOpenBISService service;

        private final IShareIdManager shareIdManager;

        private final IDataSetDirectoryProvider directoryProvider;

        MultiDataSetUnarchivingPreparations(Share scratchShare, IShareIdManager shareIdManager, IEncapsulatedOpenBISService service,
                IDataSetDirectoryProvider directoryProvider)
        {
            this.shareIdManager = shareIdManager;
            this.service = service;
            this.scratchShare = scratchShare;
            this.directoryProvider = directoryProvider;
        }

        @Override
        public void prepareForUnarchiving(List<DatasetDescription> dataSets)
        {
            for (DatasetDescription dataSet : dataSets)
            {
                SimpleDataSetInformationDTO translatedDataSet = SimpleDataSetHelper.translate(dataSet);
                String dataSetCode = dataSet.getDataSetCode();
                translatedDataSet.setDataSetShareId(null);
                String oldShareId = shareIdManager.getShareId(dataSetCode);
                String newShareId = scratchShare.getShareId();
                if (newShareId.equals(oldShareId) == false)
                {
                    service.updateShareIdAndSize(dataSetCode, newShareId, getSize(dataSet));
                    shareIdManager.setShareId(dataSetCode, newShareId);
                }
            }

            SegmentedStoreUtils.freeSpace(scratchShare, service, dataSets, directoryProvider, shareIdManager, new Log4jSimpleLogger(operationLog));

        }

        private long getSize(DatasetDescription dataSet)
        {
            Long dataSetSize = dataSet.getDataSetSize();
            if (dataSetSize != null)
            {
                return dataSetSize;
            }
            // Get the size from pathinfo database
            IHierarchicalContentProvider contentProvider = ServiceProvider.getHierarchicalContentProvider();
            IHierarchicalContentNode rootNode = contentProvider.asContent(dataSet.getDataSetCode()).getRootNode();
            return rootNode.getFileLength();
        }
    }

    @Override
    protected boolean delayUnarchiving(List<DatasetDescription> datasets, ArchiverTaskContext context)
    {
        if (delayUnarchiving == false || context.isForceUnarchiving())
        {
            return false;
        }
        IMultiDataSetArchiverDBTransaction transaction = getTransaction();
        try
        {
            List<String> dataSetCodes = translateToDataSetCodes(datasets);
            transaction.requestUnarchiving(dataSetCodes);
            transaction.commit();
            transaction.close();
        } catch (Exception e)
        {
            operationLog.warn("Requesting unarchiving of " + datasets.size() + " data sets failed", e);
            try
            {
                transaction.rollback();
                transaction.close();
            } catch (Exception ex)
            {
                operationLog.warn("Rollback of multi dataset db transaction failed", ex);
            }
        }
        return true;
    }

    @Override
    protected DatasetProcessingStatuses doUnarchive(List<DatasetDescription> dataSets, ArchiverTaskContext context)
    {
        DatasetProcessingStatuses result = new DatasetProcessingStatuses();
        List<String> dataSetCodes = translateToDataSetCodes(dataSets);
        Set<Long> containerIds = assertUnarchivingCapacityNotExceeded(dataSetCodes);
        assertNoAvailableDatasets(dataSetCodes);

        context.getUnarchivingPreparation().prepareForUnarchiving(dataSets);

        IMultiDataSetFileOperationsManager operations = getFileOperations();
        for (Long containerId : containerIds)
        {
            MultiDataSetArchiverContainerDTO container = getReadonlyQuery().getContainerForId(containerId);
            Status status = operations.restoreDataSetsFromContainerInFinalDestination(container.getPath(), dataSets);
            if (status.isError())
            {
                result.addResult(dataSets, status, Operation.UNARCHIVE);
                return result;
            }
        }

        IHierarchicalContentProvider contentProvider = context.getHierarchicalContentProvider();
        for (String dataSetCode : dataSetCodes)
        {
            IHierarchicalContent content = contentProvider.asContentWithoutModifyingAccessTimestamp(dataSetCode);
            IHierarchicalContentNode rootNode = content.getRootNode();
            assertFilesExists(dataSetCode, rootNode);
        }

        for (String dataSetCode : dataSetCodes)
        {
            getService().notifyDatasetAccess(dataSetCode);
        }

        result.addResult(dataSets, Status.OK, Operation.UNARCHIVE);
        return result;
    }

    private void assertFilesExists(String dataSetCode, IHierarchicalContentNode node)
    {
        File file;
        try
        {
            file = node.getFile();
        } catch (UnsupportedOperationException ex)
        {
            throw createException(dataSetCode, node, ex);
        }
        if (file.exists() == false)
        {
            throw createException(dataSetCode, node, null);
        }
        if (node.isDirectory() && FileUtilities.isHDF5ContainerFile(file) == false)
        {
            for (IHierarchicalContentNode child : node.getChildNodes())
            {
                assertFilesExists(dataSetCode, child);
            }
        }
    }

    private EnvironmentFailureException createException(String dataSetCode,
            IHierarchicalContentNode node, Exception exOrNull)
    {
        String message = "Data set " + dataSetCode + ": File '" + node.getRelativePath()
                + "' does not exist.";
        if (exOrNull != null)
        {
            return new EnvironmentFailureException(message + " (reason: " + exOrNull.getMessage() + ")",
                    exOrNull);
        }
        return new EnvironmentFailureException(message);
    }

    private void assertNoAvailableDatasets(List<String> dataSetCodes)
    {
        List<PhysicalDataSet> dataSets = translateToPhysicalDataSets(dataSetCodes);
        for (PhysicalDataSet physicalDataSet : dataSets)
        {
            if (physicalDataSet.isAvailable())
            {
                throw new UserFailureException("Dataset '" + physicalDataSet.getCode() + "'specified for unarchiving is available");
            }
        }
    }

    private List<String> getCodesOfAllDataSetsInContainer(Set<Long> containerIds)
    {
        List<String> enhancedDataSetCodes = new LinkedList<String>();
        for (Long containerId : containerIds)
        {
            List<MultiDataSetArchiverDataSetDTO> datasetsInContainer = getReadonlyQuery().listDataSetsForContainerId(containerId);
            for (MultiDataSetArchiverDataSetDTO dataSet : datasetsInContainer)
            {
                enhancedDataSetCodes.add(dataSet.getCode());
            }
        }
        return enhancedDataSetCodes;
    }

    private List<PhysicalDataSet> translateToPhysicalDataSets(List<String> dataSetCodes)
    {
        List<PhysicalDataSet> result = new LinkedList<PhysicalDataSet>();
        for (AbstractExternalData dataSet : getService().listDataSetsByCode(dataSetCodes))
        {
            if (dataSet.tryGetAsDataSet() != null)
            {
                result.add(dataSet.tryGetAsDataSet());
            } else
            {
                throw new IllegalStateException("All data sets in container are expected to be physical datasets, but data set '" + dataSet.getCode()
                        + "' is not ");
            }
        }
        return result;
    }

    private List<String> translateToDataSetCodes(List<? extends IDatasetLocation> dataSets)
    {
        LinkedList<String> result = new LinkedList<String>();
        for (IDatasetLocation dataSet : dataSets)
        {
            result.add(dataSet.getDataSetCode());
        }
        return result;
    }

    private Set<Long> assertUnarchivingCapacityNotExceeded(List<String> dataSetCodes)
    {
        Set<Long> containers = getContainers(dataSetCodes);

        long totalSizeOfUnarchivingRequested = getReadonlyQuery().getTotalNoOfBytesInContainersWithUnarchivingRequested();
        long totalSumInBytes = totalSizeOfUnarchivingRequested;
        for (Long containerId : containers)
        {
            List<MultiDataSetArchiverDataSetDTO> dataSets = getReadonlyQuery().listDataSetsForContainerId(containerId);
            for (MultiDataSetArchiverDataSetDTO dataSet : dataSets)
            {
                totalSumInBytes += dataSet.getSizeInBytes();
            }
        }
        if (totalSumInBytes > maximumUnarchivingCapacityInMB * FileUtils.ONE_MB)
        {
            String message = String.format("Total size of selected data sets (%.2f MB)"
                    + " and those already scheduled for unarchiving (%.2f MB) exceeds capacity."
                    + " Please narrow down your selection or try again later.",
                    ((double) (totalSumInBytes - totalSizeOfUnarchivingRequested) / FileUtils.ONE_MB),
                    ((double) totalSizeOfUnarchivingRequested / FileUtils.ONE_MB));
            throw new UserFailureException(message);
        }
        return containers;
    }

    private Set<Long> getContainers(List<String> dataSetCodes)
    {
        Set<Long> containers = new LinkedHashSet<Long>();
        for (String code : dataSetCodes)
        {
            MultiDataSetArchiverDataSetDTO dataSet = getReadonlyQuery().getDataSetForCode(code);
            if (dataSet == null)
            {
                throw new UserFailureException("Dataset " + code
                        + " was selected for unarchiving, but is not present in the archive");
            }
            containers.add(dataSet.getContainerId());
        }
        return containers;
    }

    @Override
    protected DatasetProcessingStatuses doDeleteFromArchive(List<? extends IDatasetLocation> datasets)
    {
        LinkedList<IDatasetLocation> localDataSets = new LinkedList<IDatasetLocation>(datasets);
        DatasetProcessingStatuses results = new DatasetProcessingStatuses();

        filterBasedOnArchiveStatus(localDataSets, results, FilterOption.FILTER_UNARCHIVED, Status.OK, Operation.DELETE_FROM_ARCHIVE);

        if (localDataSets.size() > 0)
        {
            throw new NotImplementedException("Deleting from archive is not yet implemented for multi dataset archiver");
        }
        return results;
    }

    @Override
    // TODO: implement a real check? Run the checkArchivedDataSets logic
    protected BooleanStatus isDataSetSynchronizedWithArchive(DatasetDescription dataset, ArchiverTaskContext context)
    {
        return isDataSetPresentInArchive(dataset);
    }

    @Override
    protected BooleanStatus isDataSetPresentInArchive(DatasetDescription dataSet)
    {
        return BooleanStatus.createFromBoolean(isDataSetPresentInArchive(dataSet.getDataSetCode()));
    }

    protected boolean isDataSetPresentInArchive(String dataSetCode)
    {
        MultiDataSetArchiverDataSetDTO dataSetInArchiveDB = getReadonlyQuery().getDataSetForCode(dataSetCode);
        return dataSetInArchiveDB != null;
    }

    @Private
    IMultiDataSetFileOperationsManager getFileOperations()
    {
        if (fileOperations == null)
        {
            fileOperations = fileOperationsFactory.create();
        }
        return fileOperations;
    }

    @Private
    IMultiDataSetArchiverDBTransaction getTransaction()
    {
        return new MultiDataSetArchiverDBTransaction();
    }

    IMultiDataSetArchiverReadonlyQueryDAO getReadonlyQuery()
    {
        if (readonlyQuery == null)
        {
            readonlyQuery = MultiDataSetArchiverDataSourceUtil.getReadonlyQueryDAO();
        }
        return readonlyQuery;
    }

    IDataStoreServiceInternal getDataStoreService()
    {
        if (dataStoreService == null)
        {
            dataStoreService = ServiceProvider.getDataStoreService();
        }
        return dataStoreService;
    }

    IMultiDataSetArchiveCleaner getCleaner()
    {
        if (cleaner == null)
        {
            cleaner = MultiDataSetArchivingUtils.createCleaner(cleanerProperties);
        }
        return cleaner;
    }

    ITimeAndWaitingProvider getTimeProvider()
    {
        if (timeProvider == null)
        {
            timeProvider = SystemTimeProvider.SYSTEM_TIME_PROVIDER;
        }
        return timeProvider;
    }

    private static final class MultiDataSetProcessingStatuses extends DatasetProcessingStatuses
    {
        private List<DatasetDescription> dataSetsWaitingForReplication;

        public List<DatasetDescription> getDataSetsWaitingForReplication()
        {
            return dataSetsWaitingForReplication;
        }

        public void setDataSetsWaitingForReplication(List<DatasetDescription> dataSetsWaitingForReplication)
        {
            this.dataSetsWaitingForReplication = dataSetsWaitingForReplication;
        }

        public void addResults(MultiDataSetProcessingStatuses statuses)
        {
            super.addResults(statuses);
            dataSetsWaitingForReplication = statuses.getDataSetsWaitingForReplication();
        }
    }

}

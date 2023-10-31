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
package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver;

import static org.apache.commons.io.FileUtils.ONE_KB;
import static org.apache.commons.io.FileUtils.ONE_MB;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.PhysicalDataUpdate;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.IFreeSpaceProvider;
import ch.systemsx.cisd.common.filesystem.SimpleFreeSpaceProvider;
import ch.systemsx.cisd.common.logging.LogLevel;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.utilities.SystemTimeProvider;
import ch.systemsx.cisd.etlserver.plugins.AbstractDataSetDeletionPostProcessingMaintenanceTaskWhichHandlesLastSeenEvent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.RsyncArchiveCopierFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.SshCommandExecutorFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.IMultiDataSetArchiverDBTransaction;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.IMultiDataSetArchiverReadonlyQueryDAO;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDataSetArchiverContainerDTO;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDataSetArchiverDBTransaction;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDataSetArchiverDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDataSetArchiverDataSourceUtil;
import ch.systemsx.cisd.openbis.dss.generic.shared.ArchiverTaskContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IConfigProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataStoreServiceInternal;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareFinder;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.IncomingShareIdProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.MappingBasedShareFinder;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.SegmentedStoreUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.Share;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

public class MultiDataSetDeletionMaintenanceTask
        extends AbstractDataSetDeletionPostProcessingMaintenanceTaskWhichHandlesLastSeenEvent
{
    private static class FirstSuitableShareFinder implements IShareFinder
    {
        @Override
        public Share tryToFindShare(SimpleDataSetInformationDTO dataSet, List<Share> shares)
        {
            long dataSetSize = dataSet.getDataSetSize();
            // 10% but not more than 1 MB are added to the data set size to take into account that
            // creating directories consume disk space.
            dataSetSize += Math.max(ONE_KB, Math.min(ONE_MB, dataSet.getDataSetSize() / 10));

            for (Share share : shares)
            {
                // Do not unarchive into unarchiving scratch share because data sets in this share 
                // can be deleted at any time.
                if (share.isUnarchivingScratchShare())
                {
                    continue;
                }
                // take the first share which has enough free space
                long freeSpace = share.calculateFreeSpace();
                if (freeSpace > dataSetSize)
                {
                    return share;
                }
            }
            return null;
        }
    }

    private static final String ARCHIVER_PREFIX = "archiver.";

    private static final String FINAL_DESTINATION_KEY = ARCHIVER_PREFIX + MultiDataSetFileOperationsManager.FINAL_DESTINATION_KEY;

    static final String LAST_SEEN_EVENT_ID_FILE = "last-seen-event-id-file";

    private List<Share> shares;

    private IShareFinder shareFinder;

    private IShareIdManager shareIdManager;

    private IApplicationServerApi v3;

    private IFreeSpaceProvider freeSpaceProvider;

    private MultiDataSetFileOperationsManager multiDataSetFileOperationsManager;

    private transient IMultiDataSetArchiveCleaner cleaner;

    private IDataSetDirectoryProvider dataSetDirectoryProvider;

    private IHierarchicalContentProvider hierarchicalContentProvider;

    private IMultiDataSetArchiverReadonlyQueryDAO readonlyQuery;

    private IDataStoreServiceInternal dataStoreService;

    private IConfigProvider configProvider;

    private Properties properties;

    private boolean hasReplication = false;

    @Override
    public void setUp(String pluginName, Properties properties)
    {
        super.setUp(pluginName, properties);
        this.properties = properties;

        dataSetDirectoryProvider = getDataStoreService().getDataSetDirectoryProvider();
        cleaner = MultiDataSetArchivingUtils.createCleaner(
                PropertyParametersUtil.extractSingleSectionProperties(
                        properties, ARCHIVER_PREFIX + MultiDataSetArchiver.CLEANER_PROPS, false).getProperties());

        String eventIdFileName = PropertyUtils.getMandatoryProperty(properties, LAST_SEEN_EVENT_ID_FILE);
        lastSeenEventIdFile = new File(eventIdFileName);

        String finalDestination = properties.getProperty(FINAL_DESTINATION_KEY);
        if (finalDestination == null)
        {
            throw new ConfigurationFailureException("Missing property " + FINAL_DESTINATION_KEY + ". Most likely reason: No "
                    + MultiDataSetArchiver.class.getSimpleName() + " configured.");
        }
        properties.setProperty(MultiDataSetFileOperationsManager.FINAL_DESTINATION_KEY, finalDestination);
        String replicatedDestination = properties.getProperty(
                ARCHIVER_PREFIX + MultiDataSetFileOperationsManager.REPLICATED_DESTINATION_KEY);
        if (replicatedDestination != null)
        {
            hasReplication = true;
            properties.setProperty(MultiDataSetFileOperationsManager.REPLICATED_DESTINATION_KEY, replicatedDestination);
        }

        if (properties.containsKey(MappingBasedShareFinder.MAPPING_FILE_KEY))
        {
            shareFinder = new MappingBasedShareFinder(properties);
        } else
        {
            shareFinder = new FirstSuitableShareFinder();
        }
        shares = getShares();
    }

    private List<Share> getShares()
    {
        IConfigProvider configProvider = getConfigProvider();
        File storeRoot = configProvider.getStoreRoot();
        String dataStoreCode = configProvider.getDataStoreCode();
        Set<String> idsOfIncomingShares = IncomingShareIdProvider.getIdsOfIncomingShares();

        return SegmentedStoreUtils.getSharesWithDataSets(storeRoot, dataStoreCode,
                SegmentedStoreUtils.FilterOptions.AVAILABLE_FOR_SHUFFLING,
                idsOfIncomingShares, getFreeSpaceProvider(), getOpenBISService(), getOperationLogAsSimpleLogger());
    }

    protected IMultiDataSetArchiverReadonlyQueryDAO getReadonlyQuery()
    {
        if (readonlyQuery == null)
        {
            readonlyQuery = MultiDataSetArchiverDataSourceUtil.getReadonlyQueryDAO();
        }
        return readonlyQuery;
    }

    protected IMultiDataSetArchiverDBTransaction getTransaction()
    {
        return new MultiDataSetArchiverDBTransaction();
    }

    protected IDataStoreServiceInternal getDataStoreService()
    {
        if (dataStoreService == null)
        {
            dataStoreService = ServiceProvider.getDataStoreService();
        }
        return dataStoreService;
    }

    protected IHierarchicalContentProvider getHierarchicalContentProvider()
    {
        if (hierarchicalContentProvider == null)
        {
            hierarchicalContentProvider = ServiceProvider.getHierarchicalContentProvider();
        }
        return hierarchicalContentProvider;
    }

    protected IApplicationServerApi getV3ApplicationService()
    {
        if (v3 == null)
        {
            v3 = ServiceProvider.getV3ApplicationService();
        }
        return v3;
    }

    protected IShareIdManager getShareIdManager()
    {
        if (shareIdManager == null)
        {
            shareIdManager = ServiceProvider.getShareIdManager();
        }
        return shareIdManager;
    }

    protected IConfigProvider getConfigProvider()
    {
        if (configProvider == null)
        {
            configProvider = ServiceProvider.getConfigProvider();
        }
        return configProvider;
    }

    protected MultiDataSetFileOperationsManager getMultiDataSetFileOperationsManager()
    {
        if (multiDataSetFileOperationsManager == null)
        {
            multiDataSetFileOperationsManager = new MultiDataSetFileOperationsManager(
                    properties, new RsyncArchiveCopierFactory(), new SshCommandExecutorFactory(),
                    getFreeSpaceProvider(), SystemTimeProvider.SYSTEM_TIME_PROVIDER);
        }
        return multiDataSetFileOperationsManager;
    }

    protected IFreeSpaceProvider getFreeSpaceProvider()
    {
        if (freeSpaceProvider == null)
        {
            freeSpaceProvider = new SimpleFreeSpaceProvider();
        }
        return freeSpaceProvider;
    }

    @Override
    protected void execute(List<DeletedDataSet> datasetCodes)
    {
        getOperationLogAsSimpleLogger().log(LogLevel.INFO,
                String.format("MultiDataSetDeletionMaintenanceTask has started processing data sets %s.",
                        CollectionUtils.abbreviate(datasetCodes.stream().map(DeletedDataSet::getCode).collect(Collectors.toList()), 10)));

        List<MultiDataSetArchiverContainerDTO> containers = findArchivesWithDeletedDataSets(datasetCodes);
        Set<String> codes = datasetCodes.stream().map(DeletedDataSet::getCode).collect(Collectors.toSet());

        for (MultiDataSetArchiverContainerDTO container : containers)
        {
            List<MultiDataSetArchiverDataSetDTO> dataSets = getReadonlyQuery().listDataSetsForContainerId(container.getId());
            List<SimpleDataSetInformationDTO> notDeletedDataSets = new ArrayList<>();
            for (MultiDataSetArchiverDataSetDTO dataSet : dataSets)
            {
                if (codes.contains(dataSet.getCode()) == false)
                {
                    SimpleDataSetInformationDTO simpleDataSet = getSimpleDataSet(dataSet);
                    Share share = shareFinder.tryToFindShare(simpleDataSet, shares);
                    if (share != null)
                    {
                        getShareIdManager().setShareId(dataSet.getCode(), share.getShareId());
                        getOpenBISService().updateShareIdAndSize(dataSet.getCode(), share.getShareId(), dataSet.getSizeInBytes());
                        notDeletedDataSets.add(simpleDataSet);
                    } else
                    {
                        throw EnvironmentFailureException.fromTemplate(
                                "Unarchiving of data set '%s' has failed, because no appropriate "
                                        + "destination share was found. Most probably there is not enough "
                                        + "free space in the data store.", dataSet.getCode());
                    }
                }
            }
            getOperationLogAsSimpleLogger().log(LogLevel.INFO,
                    String.format("Container %s contains %d not deleted data sets.", container.getPath(), notDeletedDataSets.size()));
            if (notDeletedDataSets.isEmpty() == false)
            {

                getOperationLogAsSimpleLogger().log(LogLevel.INFO,
                        String.format("Not deleted data sets: %s.",
                                CollectionUtils.abbreviate(
                                        notDeletedDataSets.stream().map(SimpleDataSetInformationDTO::getDataSetCode).collect(Collectors.toList()),
                                        10)));
                getMultiDataSetFileOperationsManager().restoreDataSetsFromContainerInFinalDestination(
                        container.getPath(), notDeletedDataSets);
                sanityCheck(notDeletedDataSets, container.getPath());
            }
            deleteContainer(container);
            getMultiDataSetFileOperationsManager().deleteContainerFromFinalDestination(cleaner, container.getPath());
            if (hasReplication)
            {
                getMultiDataSetFileOperationsManager().deleteContainerFromFinalReplicatedDestination(cleaner, container.getPath());
            }
            if (notDeletedDataSets.isEmpty() == false)
            {
                updateDataSetsStatusAndFlags(notDeletedDataSets);
            }
        }
    }

    private List<DatasetDescription> convertToDataSetDescription(List<SimpleDataSetInformationDTO> notDeletedDataSets)
    {
        List<DatasetDescription> list = new ArrayList<>();
        for (SimpleDataSetInformationDTO simpleDataSet : notDeletedDataSets)
        {
            DatasetDescription description = new DatasetDescription();
            description.setDataSetCode(simpleDataSet.getDataSetCode());
            list.add(description);
        }
        return list;
    }

    private void sanityCheck(List<SimpleDataSetInformationDTO> notDeletedDataSets, String containerPath)
    {
        List<DatasetDescription> dataSets = convertToDataSetDescription(notDeletedDataSets);
        IHierarchicalContent archivedContent = getMultiDataSetFileOperationsManager().getContainerAsHierarchicalContent(containerPath, dataSets);
        ArchiverTaskContext context = new ArchiverTaskContext(dataSetDirectoryProvider, getHierarchicalContentProvider());
        Properties archiverProperties = getDataStoreService().getArchiverProperties();
        boolean verifyChecksums =
                PropertyUtils.getBoolean(archiverProperties, MultiDataSetArchiver.SANITY_CHECK_VERIFY_CHECKSUMS_KEY,
                        MultiDataSetArchiver.DEFAULT_SANITY_CHECK_VERIFY_CHECKSUMS);

        MultiDataSetArchivingUtils.sanityCheck(archivedContent, dataSets, verifyChecksums, context, getOperationLogAsSimpleLogger());
    }

    private void updateDataSetsStatusAndFlags(List<SimpleDataSetInformationDTO> notDeletedDataSets)
    {
        // Reset the flag is_present_in_archive back to false
        List<String> codes = notDeletedDataSets.stream()
                .map(SimpleDataSetInformationDTO::getDataSetCode)
                .collect(Collectors.toList());
        getOpenBISService().updateDataSetStatuses(codes, DataSetArchivingStatus.AVAILABLE, false);

        // Set request_archiving flag to true
        List<DataSetUpdate> dataSetUpdates = new ArrayList<>();
        for (String code : codes)
        {
            DataSetUpdate dataSetUpdate = new DataSetUpdate();
            dataSetUpdate.setDataSetId(new DataSetPermId(code));
            PhysicalDataUpdate physicalDataUpdate = new PhysicalDataUpdate();
            physicalDataUpdate.setArchivingRequested(true);
            dataSetUpdate.setPhysicalData(physicalDataUpdate);
            dataSetUpdates.add(dataSetUpdate);
        }
        getV3ApplicationService().updateDataSets(getOpenBISService().getSessionToken(), dataSetUpdates);
    }

    private SimpleDataSetInformationDTO getSimpleDataSet(MultiDataSetArchiverDataSetDTO dataSet)
    {
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withPhysicalData();
        fetchOptions.withExperiment().withProject().withSpace();
        fetchOptions.withSample().withProject().withSpace();
        fetchOptions.withSample().withSpace();
        DataSetPermId dataSetPermId = new DataSetPermId(dataSet.getCode());
        DataSet dataSet1 = getV3ApplicationService()
                .getDataSets(getOpenBISService().getSessionToken(), Arrays.asList(dataSetPermId), fetchOptions)
                .get(dataSetPermId);
        SimpleDataSetInformationDTO simpleDataSet = new SimpleDataSetInformationDTO();
        simpleDataSet.setDataSetSize(dataSet1.getPhysicalData().getSize());
        simpleDataSet.setDataSetCode(dataSet1.getCode());
        simpleDataSet.setDataSetLocation(dataSet1.getPhysicalData().getLocation());

        if (dataSet1.getExperiment() != null)
        {
            simpleDataSet.setSpaceCode(dataSet1.getExperiment().getProject().getSpace().getCode());
            simpleDataSet.setProjectCode(dataSet1.getExperiment().getProject().getCode());
            simpleDataSet.setExperimentCode(dataSet1.getExperiment().getCode());
        } else
        {
            simpleDataSet.setSpaceCode(dataSet1.getSample().getSpace().getCode());
            if (dataSet1.getSample().getProject() != null)
            {
                simpleDataSet.setProjectCode(dataSet1.getSample().getProject().getCode());
            }
        }
        return simpleDataSet;
    }

    private void deleteContainer(MultiDataSetArchiverContainerDTO container)
    {
        IMultiDataSetArchiverDBTransaction transaction = getTransaction();
        try
        {
            transaction.deleteContainer(container.getId());
            transaction.commit();
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
        transaction.close();
        getOperationLogAsSimpleLogger().log(LogLevel.INFO,
                String.format("Container %s was "
                        + "successfully deleted from the database.", container.getPath()));
    }

    private List<MultiDataSetArchiverContainerDTO> findArchivesWithDeletedDataSets(List<DeletedDataSet> datasetCodes)
    {
        String[] dataSetCodes = datasetCodes.stream().map(DeletedDataSet::getCode).toArray(String[]::new);
        return getReadonlyQuery().listContainersWithDataSets(dataSetCodes);
    }
}

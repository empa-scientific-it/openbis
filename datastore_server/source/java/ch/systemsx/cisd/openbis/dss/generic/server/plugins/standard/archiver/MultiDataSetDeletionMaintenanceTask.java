package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver;

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
import ch.systemsx.cisd.common.filesystem.SimpleFreeSpaceProvider;
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

    private static final String ARCHIVER_PREFIX = "archiver.";

    static final String LAST_SEEN_EVENT_ID_FILE = "last-seen-event-id-file";

    private List<Share> shares;

    private IShareFinder shareFinder;

    private IShareIdManager shareIdManager;

    private IApplicationServerApi v3;

    private SimpleFreeSpaceProvider simpleFreeSpaceProvider;

    private MultiDataSetFileOperationsManager multiDataSetFileOperationsManager;

    private transient IMultiDataSetArchiveCleaner cleaner;

    private IDataSetDirectoryProvider dataSetDirectoryProvider;

    private IHierarchicalContentProvider hierarchicalContentProvider;

    private IMultiDataSetArchiverReadonlyQueryDAO readonlyQuery;

    private IDataStoreServiceInternal dataStoreService;

    private IConfigProvider configProvider;

    @Override
    public void setUp(String pluginName, Properties properties)
    {
        super.setUp(pluginName, properties);

        dataSetDirectoryProvider = dataStoreService().getDataSetDirectoryProvider();
        cleaner = MultiDataSetArchivingUtils.createCleaner(
                PropertyParametersUtil.extractSingleSectionProperties(
                        properties, ARCHIVER_PREFIX + MultiDataSetArchiver.CLEANER_PROPS, false).getProperties());

        String eventIdFileName = PropertyUtils.getMandatoryProperty(properties, LAST_SEEN_EVENT_ID_FILE);
        lastSeenEventIdFile = new File(eventIdFileName);

        properties.setProperty(MultiDataSetFileOperationsManager.FINAL_DESTINATION_KEY, 
                properties.getProperty(ARCHIVER_PREFIX + MultiDataSetFileOperationsManager.FINAL_DESTINATION_KEY));
        String replicatedDestination = properties.getProperty(
                ARCHIVER_PREFIX + MultiDataSetFileOperationsManager.REPLICATED_DESTINATION_KEY);
        if (replicatedDestination != null)
        {
            properties.setProperty(MultiDataSetFileOperationsManager.REPLICATED_DESTINATION_KEY, replicatedDestination);
        }

        shareFinder = new MappingBasedShareFinder(properties);
        simpleFreeSpaceProvider = new SimpleFreeSpaceProvider();
        multiDataSetFileOperationsManager = new MultiDataSetFileOperationsManager(
                properties, new RsyncArchiveCopierFactory(), new SshCommandExecutorFactory(),
                simpleFreeSpaceProvider, SystemTimeProvider.SYSTEM_TIME_PROVIDER);
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
                idsOfIncomingShares, simpleFreeSpaceProvider, getOpenBISService(), getOperationLogAsSimpleLogger());
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

    protected IDataStoreServiceInternal dataStoreService()
    {
        if (dataStoreService == null) {
            dataStoreService = ServiceProvider.getDataStoreService();
        }
        return dataStoreService;
    }

    protected IHierarchicalContentProvider getHierarchicalContentProvider()
    {
        if (hierarchicalContentProvider == null) {
            hierarchicalContentProvider = ServiceProvider.getHierarchicalContentProvider();
        }
        return hierarchicalContentProvider;
    }

    protected IApplicationServerApi getV3ApplicationService()
    {
        if (v3 == null) {
            v3 = ServiceProvider.getV3ApplicationService();
        }
        return v3;
    }

    protected IShareIdManager getShareIdManager()
    {
        if (shareIdManager == null) {
            shareIdManager = ServiceProvider.getShareIdManager();
        }
        return shareIdManager;
    }

    protected IConfigProvider getConfigProvider()
    {
        if (configProvider == null) {
            configProvider = ServiceProvider.getConfigProvider();
        }
        return configProvider;
    }

    @Override
    protected void execute(List<DeletedDataSet> datasetCodes)
    {
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
                    if (share != null) {
                        getShareIdManager().setShareId(dataSet.getCode(), share.getShareId());
                        getOpenBISService().updateShareIdAndSize(dataSet.getCode(), share.getShareId(), dataSet.getSizeInBytes());
                        notDeletedDataSets.add(simpleDataSet);
                    }
                }
            }
            multiDataSetFileOperationsManager.restoreDataSetsFromContainerInFinalDestination(
                    container.getPath(), notDeletedDataSets);
            sanityCheck(notDeletedDataSets, container.getPath());
            multiDataSetFileOperationsManager.deleteContainerFromFinalDestination(cleaner, container.getPath());
            multiDataSetFileOperationsManager.deleteContainerFromFinalReplicatedDestination(cleaner, container.getPath());
            deleteContainer(container.getId());
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
        IHierarchicalContent archivedContent = multiDataSetFileOperationsManager.getContainerAsHierarchicalContent(containerPath, dataSets);
        ArchiverTaskContext context = new ArchiverTaskContext(dataSetDirectoryProvider, getHierarchicalContentProvider());
        MultiDataSetArchivingUtils.sanityCheck(archivedContent, dataSets, context, getOperationLogAsSimpleLogger());
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

    private void deleteContainer(long containerId)
    {
        IMultiDataSetArchiverDBTransaction transaction = getTransaction();
        try
        {
            transaction.deleteContainer(containerId);
            transaction.commit();
        } catch (Exception ex)
        {
            transaction.rollback();
        }
        transaction.close();
    }

    private List<MultiDataSetArchiverContainerDTO> findArchivesWithDeletedDataSets(List<DeletedDataSet> datasetCodes)
    {
        String[] dataSetCodes = datasetCodes.stream().map(DeletedDataSet::getCode).toArray(String[]::new);
        return getReadonlyQuery().listContainersWithDataSets(dataSetCodes);
    }
}

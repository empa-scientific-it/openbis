package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.PhysicalDataUpdate;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.SimpleFreeSpaceProvider;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.utilities.SystemTimeProvider;
import ch.systemsx.cisd.etlserver.plugins.AbstractDataSetDeletionPostProcessingMaintenanceTaskWhichHandlesLastSeenEvent;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.RsyncArchiveCopierFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.SshCommandExecutorFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.*;
import ch.systemsx.cisd.openbis.dss.generic.shared.*;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.SegmentedStoreUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.Share;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class MultiDataSetDeletionMaintenanceTask
        extends AbstractDataSetDeletionPostProcessingMaintenanceTaskWhichHandlesLastSeenEvent {

    static final String LAST_SEEN_EVENT_ID_FILE = "last-seen-event-id-file";

    private List<Share> shares;

    private IShareFinder shareFinder;

    private IShareIdManager shareIdManager;

    private IApplicationServerApi v3;

    private IEncapsulatedOpenBISService openBISService;

    private SimpleFreeSpaceProvider simpleFreeSpaceProvider;

    private MultiDataSetFileOperationsManager multiDataSetFileOperationsManager;

    private Properties cleanerProperties;

    private transient IMultiDataSetArchiveCleaner cleaner;

    @Override
    public void setUp(String pluginName, Properties properties)
    {
        super.setUp(pluginName, properties);
        cleanerProperties = PropertyParametersUtil.extractSingleSectionProperties(
                properties, "cleaner", false
        ).getProperties();
        cleaner = MultiDataSetArchivingUtils.createCleaner(cleanerProperties);

        String eventIdFileName = PropertyUtils.getMandatoryProperty(properties, LAST_SEEN_EVENT_ID_FILE);
        lastSeenEventIdFile = new File(eventIdFileName);

        properties.setProperty("final-destination", properties.getProperty("archiver.final-destination"));
        String replicatedDestination = properties.getProperty("archiver.replicated-destination");
        if (replicatedDestination != null) {
            properties.setProperty("replicated-destination", replicatedDestination);
        }

        v3 = ServiceProvider.getV3ApplicationService();
        shareFinder = new MappingBasedShareFinder(properties);
        openBISService = ServiceProvider.getOpenBISService();
        simpleFreeSpaceProvider = new SimpleFreeSpaceProvider();
        shareIdManager = ServiceProvider.getShareIdManager();
        multiDataSetFileOperationsManager = new MultiDataSetFileOperationsManager(
                properties, new RsyncArchiveCopierFactory(), new SshCommandExecutorFactory(),
                simpleFreeSpaceProvider, SystemTimeProvider.SYSTEM_TIME_PROVIDER
        );
        shares = getShares();
    }

    private List<Share> getShares()
    {
        IConfigProvider configProvider =  ServiceProvider.getConfigProvider();
        File storeRoot = configProvider.getStoreRoot();
        String dataStoreCode = configProvider.getDataStoreCode();
        Set<String> idsOfIncomingShares = IncomingShareIdProvider.getIdsOfIncomingShares();
        Log4jSimpleLogger log4jSimpleLogger = new Log4jSimpleLogger(operationLog);

        return SegmentedStoreUtils.getSharesWithDataSets(storeRoot, dataStoreCode,
                SegmentedStoreUtils.FilterOptions.AVAILABLE_FOR_SHUFFLING,
                idsOfIncomingShares, simpleFreeSpaceProvider, openBISService, log4jSimpleLogger);
    }

    @Override
    protected void execute(List<DeletedDataSet> datasetCodes)
    {
        IMultiDataSetArchiverReadonlyQueryDAO readonlyQuery = MultiDataSetArchiverDataSourceUtil.getReadonlyQueryDAO();
        IMultiDataSetArchiverDBTransaction transaction = new MultiDataSetArchiverDBTransaction();

        List<MultiDataSetArchiverContainerDTO> containers = findArchivesWithDeletedDataSets(readonlyQuery, datasetCodes);
        Set<String> codes = datasetCodes.stream().map(DeletedDataSet::getCode).collect(Collectors.toSet());

        for (MultiDataSetArchiverContainerDTO container: containers)
        {
            List<MultiDataSetArchiverDataSetDTO> dataSets = readonlyQuery.listDataSetsForContainerId(container.getId());
            List<SimpleDataSetInformationDTO> notDeletedDataSets = new ArrayList<>();
            for (MultiDataSetArchiverDataSetDTO dataSet: dataSets)
            {
                if (codes.contains(dataSet.getCode()) == false)
                {
                    SimpleDataSetInformationDTO simpleDataSet = getSimpleDataSet(dataSet);
                    Share share = shareFinder.tryToFindShare(simpleDataSet, shares);
                    shareIdManager.setShareId(dataSet.getCode(), share.getShareId());
                    openBISService.updateShareIdAndSize(dataSet.getCode(), share.getShareId(), dataSet.getSizeInBytes());
                    notDeletedDataSets.add(simpleDataSet);
                }
            }
            Status restoreStatus = multiDataSetFileOperationsManager.restoreDataSetsFromContainerInFinalDestination(
                container.getPath(), notDeletedDataSets
            );
            multiDataSetFileOperationsManager.deleteContainerFromFinalDestination(cleaner, container.getPath());
            multiDataSetFileOperationsManager.deleteContainerFromFinalReplicatedDestination(cleaner, container.getPath());
            deleteContainer(transaction, container.getId());
            updateDataSetsStatusAndFlags(notDeletedDataSets);
        }
    }

    private void updateDataSetsStatusAndFlags(List<SimpleDataSetInformationDTO> notDeletedDataSets) {
        // Reset the flag is_present_in_archive back to false
        List<String> codes = notDeletedDataSets.stream()
                .map(SimpleDataSetInformationDTO::getDataSetCode)
                .collect(Collectors.toList());
        openBISService.updateDataSetStatuses(codes, DataSetArchivingStatus.AVAILABLE, false);

        // Set request_archiving flag to true
        List<DataSetUpdate> dataSetUpdates = new ArrayList<>();
        for (String code: codes)
        {
            DataSetUpdate dataSetUpdate = new DataSetUpdate();
            dataSetUpdate.setDataSetId(new DataSetPermId(code));
            PhysicalDataUpdate physicalDataUpdate = new PhysicalDataUpdate();
            physicalDataUpdate.setArchivingRequested(true);
            dataSetUpdate.setPhysicalData(physicalDataUpdate);
            dataSetUpdates.add(dataSetUpdate);
        }
        v3.updateDataSets(openBISService.getSessionToken(), dataSetUpdates);
    }

    private SimpleDataSetInformationDTO getSimpleDataSet(MultiDataSetArchiverDataSetDTO dataSet)
    {
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withPhysicalData();
        fetchOptions.withExperiment().withProject().withSpace();
        fetchOptions.withSample().withProject().withSpace();
        fetchOptions.withSample().withSpace();
        DataSetPermId dataSetPermId = new DataSetPermId(dataSet.getCode());
        DataSet dataSet1 = v3.getDataSets(openBISService.getSessionToken(), Arrays.asList(dataSetPermId), fetchOptions)
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

    private void deleteContainer(IMultiDataSetArchiverDBTransaction transaction, Long containerId)
    {
        try
        {
            if (containerId != null)
            {
                transaction.deleteContainer(containerId);
            }
            transaction.commit();
        } catch (Exception ex)
        {
            transaction.rollback();
        }
        transaction.close();
    }

    private List<MultiDataSetArchiverContainerDTO> findArchivesWithDeletedDataSets(
            IMultiDataSetArchiverReadonlyQueryDAO readonlyQuery, List<DeletedDataSet> datasetCodes
    )
    {
        String[] dataSetCodes = datasetCodes.stream().map(DeletedDataSet::getCode).toArray(String[]::new);
        return readonlyQuery.listContainersWithDataSets(dataSetCodes);
    }
}

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.PhysicalData;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.SimpleFreeSpaceProvider;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.common.utilities.SystemTimeProvider;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.MockContent;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.RsyncArchiveCopierFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.SshCommandExecutorFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.IMultiDataSetArchiverDBTransaction;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.IMultiDataSetArchiverReadonlyQueryDAO;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDataSetArchiverContainerDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.IConfigProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataStoreServiceInternal;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProviderTestWrapper;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;
import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class MultiDataSetDeletionMaintenanceTaskTest extends AbstractFileSystemTestCase
{
    private File store;

    private File share;

    private File archive;

    private File replicate;

    private File mappingFile;

    private File lastSeenDataSetFile;

    private BufferedAppender logRecorder;

    private Mockery context;

    private MockMultiDataSetArchiverDBTransaction transaction;

    private IEncapsulatedOpenBISService openBISService;

    private IDataStoreServiceInternal dataStoreService;

    private IHierarchicalContentProvider contentProvider;

    private IShareIdManager shareIdManager;

    private IApplicationServerApi v3api;

    private IDataSetDirectoryProvider directoryProvider;

    private IConfigProvider configProvider;

    private MultiDataSetDeletionMaintenanceTask task;

    private static final String LAST_SEEN_DATA_SET_FILE = "last-seen-data-set";

    private static final String SESSION_TOKEN = "sessionToken";

    private static final String DSS_CODE = "dss1";

    private static final String ds3Code = "20220111121909356-57";

    private static final String ds4Code = "20220111121934409-58";

    private static final Long DATA_SET_STANDARD_SIZE = 8L;

    private static final class MockMultiDataSetDeletionMaintenanceTask extends MultiDataSetDeletionMaintenanceTask
    {
        private IMultiDataSetArchiverDBTransaction transaction;

        private IMultiDataSetArchiverReadonlyQueryDAO readonlyDAO;

        private IEncapsulatedOpenBISService openBISService;

        private IDataStoreServiceInternal dataStoreService;

        private IHierarchicalContentProvider contentProvider;

        private IShareIdManager shareIdManager;

        private IApplicationServerApi v3api;

        private IConfigProvider configProvider;

        private MockMultiDataSetFileOperationsManager multiDataSetManager;

        public MockMultiDataSetDeletionMaintenanceTask(IMultiDataSetArchiverDBTransaction transaction,
                IMultiDataSetArchiverReadonlyQueryDAO readonlyDAO,
                IEncapsulatedOpenBISService openBISService,
                IDataStoreServiceInternal dataStoreService,
                IHierarchicalContentProvider contentProvider,
                IShareIdManager shareIdManager,
                IApplicationServerApi v3api,
                IConfigProvider configProvider,
                MockMultiDataSetFileOperationsManager multiDataSetManager)
        {
            this.transaction = transaction;
            this.readonlyDAO = readonlyDAO;
            this.openBISService = openBISService;
            this.dataStoreService = dataStoreService;
            this.contentProvider = contentProvider;
            this.shareIdManager = shareIdManager;
            this.v3api = v3api;
            this.configProvider = configProvider;
            this.multiDataSetManager = multiDataSetManager;
        }

        @Override
        protected IMultiDataSetArchiverDBTransaction getTransaction()
        {
            return transaction;
        }

        @Override
        protected IMultiDataSetArchiverReadonlyQueryDAO getReadonlyQuery()
        {
            return readonlyDAO;
        }

        @Override
        protected IEncapsulatedOpenBISService getOpenBISService()
        {
            return openBISService;
        }

        @Override
        protected IDataStoreServiceInternal getDataStoreService()
        {
            return dataStoreService;
        }

        @Override
        protected IHierarchicalContentProvider getHierarchicalContentProvider()
        {
            return contentProvider;
        }

        @Override
        protected IApplicationServerApi getV3ApplicationService()
        {
            return v3api;
        }

        @Override
        protected IShareIdManager getShareIdManager()
        {
            return shareIdManager;
        }

        @Override
        protected IConfigProvider getConfigProvider()
        {
            return configProvider;
        }

        @Override
        protected MultiDataSetFileOperationsManager getMultiDataSetFileOperationsManager()
        {
            return multiDataSetManager;
        }
    }

    private static final class MockMultiDataSetFileOperationsManager extends MultiDataSetFileOperationsManager
    {

        public MockMultiDataSetFileOperationsManager(Properties properties,
                IDataSetDirectoryProvider directoryProvider)
        {
            super(properties, new RsyncArchiveCopierFactory(), new SshCommandExecutorFactory(),
                    new SimpleFreeSpaceProvider(), SystemTimeProvider.SYSTEM_TIME_PROVIDER);
            this.directoryProvider = directoryProvider;
        }

        @Override
        protected IDataSetDirectoryProvider getDirectoryProvider()
        {
            return directoryProvider;
        }
    }

    @BeforeMethod
    public void setUpTestEnvironment()
    {
        logRecorder = LogRecordingUtils.createRecorder("%-5p %c - %m%n", Level.INFO, "OPERATION.*");
        context = new Mockery();
        transaction = new MockMultiDataSetArchiverDBTransaction();

        createStore();

        openBISService = context.mock(IEncapsulatedOpenBISService.class);
        dataStoreService = context.mock(IDataStoreServiceInternal.class);
        contentProvider = context.mock(IHierarchicalContentProvider.class);
        shareIdManager = context.mock(IShareIdManager.class);
        v3api = context.mock(IApplicationServerApi.class);
        directoryProvider = context.mock(IDataSetDirectoryProvider.class);
        configProvider = context.mock(IConfigProvider.class);

        context.checking(new Expectations()
            {
                {
                    allowing(dataStoreService).getDataSetDirectoryProvider();
                    will(returnValue(directoryProvider));

                    allowing(configProvider).getStoreRoot();
                    will(returnValue(store));

                    allowing(configProvider).getDataStoreCode();
                    will(returnValue(DSS_CODE));

                    allowing(openBISService).getSessionToken();
                    will(returnValue(SESSION_TOKEN));
                }
            });

        Properties properties = createProperties();
        MockMultiDataSetFileOperationsManager multiDataSetManager = new MockMultiDataSetFileOperationsManager(properties, directoryProvider);
        task = new MockMultiDataSetDeletionMaintenanceTask(
                transaction, transaction, openBISService, dataStoreService,
                contentProvider, shareIdManager, v3api, configProvider, multiDataSetManager);
        task.setUp("", properties);
    }

    private DataSet generateDataSet(String code, Long sizeInBytes)
    {
        DataSet dataSet = new DataSet();
        dataSet.setCode(code);
        dataSet.setFetchOptions(fetchOptions());

        // Set the PhysicalData
        PhysicalData physicalData = new PhysicalData();
        physicalData.setSize(sizeInBytes);
        physicalData.setLocation("");
        dataSet.setPhysicalData(physicalData);

        // Set the Sample
        Sample sample = new Sample();
        sample.setCode("Sample1");
        Space space = new Space();
        space.setCode("DEFAULT"); // should match to mappingFile input
        sample.setSpace(space);
        SampleFetchOptions sfo = new SampleFetchOptions();
        sfo.withSpace();
        sfo.withProject();
        sample.setFetchOptions(sfo);
        dataSet.setSample(sample);

        return dataSet;
    }

    private DataSetFetchOptions fetchOptions()
    {
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withPhysicalData();
        fetchOptions.withExperiment().withProject().withSpace();
        fetchOptions.withSample().withProject().withSpace();
        fetchOptions.withSample().withSpace();
        return fetchOptions;
    }

    private DatasetDescription dataSetDescription(final String code)
    {
        DatasetDescription description = new DatasetDescription();
        description.setDataSetCode(code);
        description.setDataSetSize(DATA_SET_STANDARD_SIZE);
        return description;
    }

    private Properties createProperties()
    {
        Properties properties = new Properties();

        lastSeenDataSetFile = new File(workingDirectory, LAST_SEEN_DATA_SET_FILE);
        properties.setProperty(
                MultiDataSetDeletionMaintenanceTask.LAST_SEEN_EVENT_ID_FILE,
                lastSeenDataSetFile.getPath());
        properties.setProperty("archiver." + MultiDataSetFileOperationsManager.FINAL_DESTINATION_KEY, archive.getAbsolutePath());
        properties.setProperty(MultiDataSetFileOperationsManager.FINAL_DESTINATION_KEY, archive.getAbsolutePath());
        properties.setProperty("archiver." + MultiDataSetFileOperationsManager.REPLICATED_DESTINATION_KEY, replicate.getAbsolutePath());
        properties.setProperty(MultiDataSetFileOperationsManager.REPLICATED_DESTINATION_KEY, replicate.getAbsolutePath());
        properties.setProperty("mapping-file", mappingFile.getPath());
        return properties;
    }

    private void createStore()
    {
        store = new File(workingDirectory, "store");
        share = new File(store, "1");
        share.mkdirs();

        archive = new File(workingDirectory, "archive");
        archive.mkdirs();
        replicate = new File(workingDirectory, "replicate");
        replicate.mkdirs();
        mappingFile = new File(workingDirectory, "mapping-file.txt");
        try
        {
            mappingFile.createNewFile();
            FileUtilities.writeToFile(mappingFile, "Identifier\tShare IDs\tArchive Folder\n" +
                    "/DEFAULT\t1\t" + archive + "\n");
        } catch (IOException ex)
        {
            assertEquals("Invalid file path", ex.getMessage());
        }
    }

    private File copyContainerToArchive(File parent, String folderName)
    {
        File dataSetFile = new File(
                "../datastore_server/sourceTest/java/ch/systemsx/cisd/openbis/dss/generic/server/plugins/standard/archiver/resource/container.tar");

        File container = new File(parent, folderName);
        FileOperations.getInstance().copy(dataSetFile, container);
        return container;
    }

    @AfterMethod
    public void tearDown()
    {
        ServiceProviderTestWrapper.restoreApplicationContext();
        context.assertIsSatisfied();
    }

    private String getLogContent(BufferedAppender logRecorder)
    {
        String logContent = logRecorder.getLogContent();
        logContent = logContent.replaceAll("0\\.[0-9]{2,2} s", "?.?? s");
        return logContent;
    }

    @Test
    public void testExecuteWithEmptyDeletedDataSetList()
    {
        task.execute(new ArrayList<>());
        AssertionUtil.assertContainsLines(
                "INFO  OPERATION.IdentifierAttributeMappingManager - Mapping file '" +
                        mappingFile + "' successfully loaded.\n" +
                        "INFO  OPERATION.AbstractDataSetDeletionPostProcessingMaintenanceTask - " +
                        "Obtained the list of all datasets in all shares in ?.?? s.\n" +
                        "INFO  OPERATION.AbstractDataSetDeletionPostProcessingMaintenanceTask - " +
                        "MultiDataSetDeletionMaintenanceTask has started processing data sets [].",
                getLogContent(logRecorder));
    }

    @Test
    public void testContainerContainsOnlyDeletedDataSetList()
    {
        // Container1 contains only deleted dataSets
        String containerName = "container1.tar";
        MultiDataSetArchiverContainerDTO container = transaction.createContainer(containerName);
        transaction.insertDataset(dataSetDescription("ds1"), container);
        transaction.insertDataset(dataSetDescription("ds2"), container);
        transaction.commit();
        // Create a container in archive and replicate it.
        File archiveContainer = copyContainerToArchive(archive, containerName);
        File replicateContainer = copyContainerToArchive(replicate, containerName);
        // All dataSets in the Container 1 was deleted.
        DeletedDataSet deleted1 = new DeletedDataSet(1, "ds1");
        DeletedDataSet deleted2 = new DeletedDataSet(1, "ds2");

        assertEquals(1, transaction.listContainers().size());

        task.execute(Arrays.asList(deleted1, deleted2));

        assertEquals(0, transaction.listContainers().size());

        AssertionUtil.assertContainsLines(
                "INFO  OPERATION.IdentifierAttributeMappingManager - Mapping file '" +
                        mappingFile + "' successfully loaded.\n" +
                        "INFO  OPERATION.AbstractDataSetDeletionPostProcessingMaintenanceTask - " +
                        "Obtained the list of all datasets in all shares in ?.?? s.\n" +
                        "INFO  OPERATION.AbstractDataSetDeletionPostProcessingMaintenanceTask - " +
                        "MultiDataSetDeletionMaintenanceTask has started processing data sets [ds1, ds2].\n" +
                        "INFO  OPERATION.AbstractDataSetDeletionPostProcessingMaintenanceTask - " +
                        "Container 0 contains 0 not deleted data sets.\n" +
                        "INFO  OPERATION.MultiDataSetArchiveCleaner - File immediately deleted: " +
                        archiveContainer.getAbsolutePath() + "\n" +
                        "INFO  OPERATION.MultiDataSetArchiveCleaner - File immediately deleted: " +
                        replicateContainer.getAbsolutePath(),
                getLogContent(logRecorder));
    }

    @Test
    public void testTaskIsWorkingWithoutReplica()
    {
        // Container1 contains only deleted dataSets
        String containerName = "container1.tar";
        MultiDataSetArchiverContainerDTO container = transaction.createContainer(containerName);
        transaction.insertDataset(dataSetDescription("ds1"), container);
        transaction.commit();
        // Create a container in archive WITHOUT replica.
        File archiveContainer = copyContainerToArchive(archive, containerName);
        // All dataSets in the Container 1 was deleted.
        DeletedDataSet deleted1 = new DeletedDataSet(1, "ds1");

        task.execute(Arrays.asList(deleted1));

        // check that archive was deleted, but replica was not, because it is not exist
        String replicatePath = archiveContainer.getAbsolutePath();
        replicatePath = replicatePath.replace("/archive/", "/replicate/");
        AssertionUtil.assertContainsLines(
                    "INFO  OPERATION.MultiDataSetArchiveCleaner - File immediately deleted: " +
                            archiveContainer.getAbsolutePath() + "\n" +
                            "WARN  OPERATION.MultiDataSetArchiveCleaner - Failed to delete file immediately: " + replicatePath + "\n",
                getLogContent(logRecorder));
    }

    @Test
    public void testContainerContainsDeletedAndNoneDeletedDataSet()
    {
        // create dataSetMap
        Map<IDataSetId, DataSet> dataSetMap = new HashMap<>();
        dataSetMap.put(new DataSetPermId(ds4Code), generateDataSet(ds4Code, DATA_SET_STANDARD_SIZE));
        // create content for ds4
        final MockContent ds4Content = new MockContent(":0:0", "original/:0:0", "original/test.txt:8:70486887");
        RecordingMatcher<List<DataSetUpdate>> recordedUpdates = new RecordingMatcher<>();
        // prepare context
        context.checking(new Expectations()
        {
            {
                one(v3api).getDataSets(with(SESSION_TOKEN), with(Arrays.asList(new DataSetPermId(ds4Code))),
                        with(any(DataSetFetchOptions.class)));
                will(returnValue(dataSetMap));

                one(shareIdManager).setShareId(ds4Code, "1");
                one(openBISService).updateShareIdAndSize(ds4Code, "1", DATA_SET_STANDARD_SIZE);

                one(directoryProvider).getDataSetDirectory(with(any(IDatasetLocation.class)));
                will(returnValue(share));

                one(contentProvider).asContentWithoutModifyingAccessTimestamp(ds4Code);
                will(returnValue(ds4Content));

                one(openBISService).updateDataSetStatuses(Arrays.asList(ds4Code), DataSetArchivingStatus.AVAILABLE, false);
                one(v3api).updateDataSets(with(SESSION_TOKEN), with((recordedUpdates)));
            }
        });
        // Container2 contains one deleted and one not deleted dataSets
        String containerName = "container2.tar";
        MultiDataSetArchiverContainerDTO c = transaction.createContainer(containerName);
        transaction.insertDataset(dataSetDescription(ds3Code), c);
        transaction.insertDataset(dataSetDescription(ds4Code), c);
        transaction.commit();
        // Create a container in archive and replicate it.
        File archiveContainer = copyContainerToArchive(archive, containerName);
        File replicateContainer = copyContainerToArchive(replicate, containerName);
        // One of the dataSets in Container 2 was deleted.
        DeletedDataSet deleted3 = new DeletedDataSet(1, ds3Code);

        assertEquals(1, transaction.listContainers().size());

        task.execute(Arrays.asList(deleted3));

        assertEquals(0, transaction.listContainers().size());

        AssertionUtil.assertContainsLines(
                "INFO  OPERATION.IdentifierAttributeMappingManager - Mapping file '" +
                        mappingFile + "' successfully loaded.\n" +
                        "INFO  OPERATION.AbstractDataSetDeletionPostProcessingMaintenanceTask - " +
                        "Obtained the list of all datasets in all shares in ?.?? s.\n" +
                        "INFO  OPERATION.AbstractDataSetDeletionPostProcessingMaintenanceTask - " +
                        "MultiDataSetDeletionMaintenanceTask has started processing data sets " +
                        "[20220111121909356-57].\n" +
                        "INFO  OPERATION.AbstractDataSetDeletionPostProcessingMaintenanceTask - " +
                        "Container 0 contains 1 not deleted data sets.\n" +
                        "INFO  OPERATION.AbstractDataSetDeletionPostProcessingMaintenanceTask - " +
                        "Not deleted data sets [20220111121934409-58].\n" +
                        "INFO  OPERATION.AbstractDataSetDeletionPostProcessingMaintenanceTask - " +
                        "Start sanity check on [Dataset '20220111121934409-58']\n" +
                        "INFO  OPERATION.MultiDataSetFileOperationsManager - " +
                        "Reading statistics for input stream: 1.06 KB in 4 chunks took < 1sec.\n" +
                        "INFO  OPERATION.MultiDataSetFileOperationsManager - " +
                        "Writing statistics for output stream: 1.06 KB in 4 chunks took < 1sec.\n" +
                        "INFO  OPERATION.AbstractDataSetDeletionPostProcessingMaintenanceTask - Sanity check finished.\n" +
                        "INFO  OPERATION.MultiDataSetArchiveCleaner - File immediately deleted: " +
                        archiveContainer.getAbsolutePath() + "\n" +
                        "INFO  OPERATION.MultiDataSetArchiveCleaner - File immediately deleted: " +
                        replicateContainer.getAbsolutePath(),
                getLogContent(logRecorder));
    }
}

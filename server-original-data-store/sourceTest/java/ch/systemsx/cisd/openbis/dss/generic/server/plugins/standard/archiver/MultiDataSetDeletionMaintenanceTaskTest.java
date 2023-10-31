/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
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
import ch.systemsx.cisd.common.filesystem.HostAwareFile;
import ch.systemsx.cisd.common.filesystem.IFreeSpaceProvider;
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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;
import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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

    private static final Date MAX_DELETION_DATE = new Date(22);

    private static final String SESSION_TOKEN = "sessionToken";

    private static final String DSS_CODE = "dss1";

    private static final String ds3Code = "20220111121909356-57";

    private static final String ds4Code = "20220111121934409-58";

    private static final Long DATA_SET_STANDARD_SIZE = 8L;

    private final MockContent goodDs4Content = new MockContent(
            ":0:0", "original/:0:0", "original/test.txt:8:70486887"
    );

    private final MockContent badDs4Content = new MockContent(
            ":0:0", "wrong_path/:0:0", "wrong_path/test.txt:8:70486887"
    );

    private final String WRONG_PATH_ERROR =
            "Different paths: Path in the store is '20220111121934409-58/wrong_path' " +
                    "and in the archive '20220111121934409-58/original'.";

    private static final class MockMultiDataSetDeletionMaintenanceTask
            extends MultiDataSetDeletionMaintenanceTask
    {
        private final IMultiDataSetArchiverDBTransaction transaction;

        private final IMultiDataSetArchiverReadonlyQueryDAO readonlyDAO;

        private final IEncapsulatedOpenBISService openBISService;

        private final IDataStoreServiceInternal dataStoreService;

        private final IHierarchicalContentProvider contentProvider;

        private final IShareIdManager shareIdManager;

        private final IApplicationServerApi v3api;

        private final IConfigProvider configProvider;

        private final MockMultiDataSetFileOperationsManager multiDataSetManager;

        private final IFreeSpaceProvider freeSpaceProvider;

        public MockMultiDataSetDeletionMaintenanceTask(
                IMultiDataSetArchiverDBTransaction transaction,
                IMultiDataSetArchiverReadonlyQueryDAO readonlyDAO,
                IEncapsulatedOpenBISService openBISService,
                IDataStoreServiceInternal dataStoreService,
                IHierarchicalContentProvider contentProvider,
                IShareIdManager shareIdManager,
                IApplicationServerApi v3api,
                IConfigProvider configProvider,
                MockMultiDataSetFileOperationsManager multiDataSetManager,
                IFreeSpaceProvider freeSpaceProvider)
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
            this.freeSpaceProvider = freeSpaceProvider;
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

        @Override
        protected Date computeMaxDeletionDate()
        {
            return MAX_DELETION_DATE;
        }

        @Override
        protected IFreeSpaceProvider getFreeSpaceProvider()
        {
            return freeSpaceProvider;
        }
    }

    private static final class MockMultiDataSetFileOperationsManager
            extends MultiDataSetFileOperationsManager
    {
        private static final long serialVersionUID = 1L;

        public MockMultiDataSetFileOperationsManager(Properties properties,
                IDataSetDirectoryProvider directoryProvider, IFreeSpaceProvider spaceProvider)
        {
            super(properties, new RsyncArchiveCopierFactory(), new SshCommandExecutorFactory(),
                    spaceProvider, SystemTimeProvider.SYSTEM_TIME_PROVIDER);
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

        Properties properties = createProperties(true, true);

        context.checking(new Expectations()
        {
            {
                allowing(dataStoreService).getDataSetDirectoryProvider();
                will(returnValue(directoryProvider));

                allowing(dataStoreService).getArchiverProperties();
                will(returnValue(properties));

                allowing(configProvider).getStoreRoot();
                will(returnValue(store));

                allowing(configProvider).getDataStoreCode();
                will(returnValue(DSS_CODE));

                allowing(openBISService).getSessionToken();
                will(returnValue(SESSION_TOKEN));
            }
        });

        IFreeSpaceProvider freeSpaceProvider = new SimpleFreeSpaceProvider();
        task = new MockMultiDataSetDeletionMaintenanceTask(
                transaction, transaction, openBISService, dataStoreService,
                contentProvider, shareIdManager, v3api, configProvider,
                new MockMultiDataSetFileOperationsManager(properties, directoryProvider,
                        freeSpaceProvider), freeSpaceProvider);
        logRecorder = LogRecordingUtils.createRecorder("%-5p %c - %m%n", Level.INFO, "OPERATION.*");
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

    private Properties createProperties(boolean withReplica, boolean withMappingFile)
    {
        Properties properties = new Properties();

        lastSeenDataSetFile = new File(workingDirectory, LAST_SEEN_DATA_SET_FILE);
        properties.setProperty(
                MultiDataSetDeletionMaintenanceTask.LAST_SEEN_EVENT_ID_FILE,
                lastSeenDataSetFile.getPath());
        properties.setProperty(
                "archiver." + MultiDataSetFileOperationsManager.FINAL_DESTINATION_KEY,
                archive.getAbsolutePath());
        properties.setProperty(MultiDataSetFileOperationsManager.FINAL_DESTINATION_KEY,
                archive.getAbsolutePath());
        if (withReplica)
        {
            properties.setProperty(
                    "archiver." + MultiDataSetFileOperationsManager.REPLICATED_DESTINATION_KEY,
                    replicate.getAbsolutePath());
            properties.setProperty(MultiDataSetFileOperationsManager.REPLICATED_DESTINATION_KEY,
                    replicate.getAbsolutePath());
        }
        if (withMappingFile)
        {
            properties.setProperty("mapping-file", mappingFile.getPath());
        }
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
        FileUtilities.writeToFile(mappingFile, "Identifier\tShare IDs\tArchive Folder\n" +
                "/DEFAULT\t1\t" + archive + "\n");
    }

    private File copyContainerToArchive(File parent, String folderName)
    {
        File dataSetFile = new File(
                "../server-original-data-store/sourceTest/java/ch/systemsx/cisd/openbis/dss/generic/server/plugins/standard/archiver/resource/container.tar");

        File container = new File(parent, folderName);
        FileOperations.getInstance().copy(dataSetFile, container);
        return container;
    }

    @AfterMethod
    public void tearDown()
    {
        context.assertIsSatisfied();
    }

    private String getLogContent()
    {
        String logContent = logRecorder.getLogContent();
        logContent = logContent.replaceAll("0\\.[0-9]{2,2} s", "?.?? s");
        return logContent;
    }

    private Map<IDataSetId, DataSet> buildDataSetMap()
    {
        Map<IDataSetId, DataSet> dataSetMap = new HashMap<>();
        dataSetMap.put(new DataSetPermId(ds4Code),
                generateDataSet(ds4Code, DATA_SET_STANDARD_SIZE));
        return dataSetMap;
    }

    private void createContainer(String containerName, List<String> dataSetCodes)
    {
        MultiDataSetArchiverContainerDTO container = transaction.createContainer(containerName);
        for (String code : dataSetCodes)
        {
            transaction.insertDataset(dataSetDescription(code), container);
        }
        transaction.commit();
    }

    @Test
    public void testExecuteWithEmptyDeletedDataSetList()
    {
        // GIVEN
        // WHEN
        task.execute(new ArrayList<>());
        // THEN
        AssertionUtil.assertContainsLines(
                "INFO  OPERATION.IdentifierAttributeMappingManager - Mapping file '" +
                        mappingFile + "' successfully loaded.\n" +
                        "INFO  OPERATION.AbstractDataSetDeletionPostProcessingMaintenanceTask - " +
                        "Obtained the list of all datasets in all shares in ?.?? s.\n" +
                        "INFO  OPERATION.AbstractDataSetDeletionPostProcessingMaintenanceTask - " +
                        "MultiDataSetDeletionMaintenanceTask has started processing data sets [].",
                getLogContent());
    }

    @Test
    public void testContainerContainsOnlyDeletedDataSetList()
    {
        // GIVEN
        // Container1 contains only deleted dataSets
        String containerName = "container1.tar";
        createContainer(containerName, Arrays.asList("ds1", "ds2"));
        // Create a container in archive and replicate it.
        File archiveContainer = copyContainerToArchive(archive, containerName);
        File replicateContainer = copyContainerToArchive(replicate, containerName);
        // All dataSets in the Container 1 was deleted.
        DeletedDataSet deleted1 = new DeletedDataSet(1, "ds1");
        DeletedDataSet deleted2 = new DeletedDataSet(1, "ds2");

        assertEquals(1, transaction.listContainers().size());

        // WHEN
        task.execute(Arrays.asList(deleted1, deleted2));

        // THEN
        assertEquals(0, transaction.listContainers().size());

        AssertionUtil.assertContainsLines(
                "INFO  OPERATION.IdentifierAttributeMappingManager - Mapping file '" +
                        mappingFile + "' successfully loaded.\n" +
                        "INFO  OPERATION.AbstractDataSetDeletionPostProcessingMaintenanceTask - " +
                        "Obtained the list of all datasets in all shares in ?.?? s.\n" +
                        "INFO  OPERATION.AbstractDataSetDeletionPostProcessingMaintenanceTask - " +
                        "MultiDataSetDeletionMaintenanceTask has started processing data sets [ds1, ds2].\n" +
                        "INFO  OPERATION.AbstractDataSetDeletionPostProcessingMaintenanceTask - " +
                        "Container container1.tar contains 0 not deleted data sets.\n" +
                        "INFO  OPERATION.AbstractDataSetDeletionPostProcessingMaintenanceTask - " +
                        "Container container1.tar was successfully deleted from the database.\n" +
                        "INFO  OPERATION.MultiDataSetArchiveCleaner - File immediately deleted: " +
                        archiveContainer.getAbsolutePath() + "\n" +
                        "INFO  OPERATION.MultiDataSetArchiveCleaner - File immediately deleted: " +
                        replicateContainer.getAbsolutePath(),
                getLogContent());
    }

    @Test
    public void testTaskIsWorkingWithoutReplica()
    {
        // GIVEN
        Properties properties = createProperties(false, true);
        IFreeSpaceProvider freeSpaceProvider = new SimpleFreeSpaceProvider();
        MockMultiDataSetDeletionMaintenanceTask taskWithoutReplica =
                new MockMultiDataSetDeletionMaintenanceTask(
                        transaction, transaction, openBISService, dataStoreService,
                        contentProvider, shareIdManager, v3api, configProvider,
                        new MockMultiDataSetFileOperationsManager(properties, directoryProvider,
                                freeSpaceProvider), freeSpaceProvider);
        taskWithoutReplica.setUp("", properties);

        // Container1 contains only deleted dataSets
        String containerName = "container1.tar";
        createContainer(containerName, Arrays.asList("ds1", "ds2"));
        // Create a container in archive WITHOUT replica.
        File archiveContainer = copyContainerToArchive(archive, containerName);
        // All dataSets in the Container 1 was deleted.
        DeletedDataSet deleted1 = new DeletedDataSet(1, "ds1");
        DeletedDataSet deleted2 = new DeletedDataSet(1, "ds2");

        // WHEN
        taskWithoutReplica.execute(Arrays.asList(deleted1, deleted2));

        // THEN
        // check that archive was deleted, but replica was not, because it is not exist
        String replicatePath = archiveContainer.getAbsolutePath();
        replicatePath = replicatePath.replace("/archive/", "/replicate/");

        String log = getLogContent();
        AssertionUtil.assertContainsLines(
                "INFO  OPERATION.MultiDataSetArchiveCleaner - File immediately deleted: " +
                        archiveContainer.getAbsolutePath() + "\n", log);

        // There is no information of replicate file.
        AssertionUtil.assertContainsNot(
                "INFO  OPERATION.MultiDataSetArchiveCleaner - File immediately deleted: " +
                        replicatePath + "\n", log);
        AssertionUtil.assertContainsNot(
                "WARN  OPERATION.MultiDataSetArchiveCleaner - Failed to delete file immediately: " +
                        replicatePath + "\n", log);
    }

    @Test
    public void testContainerContainsDeletedAndNoneDeletedDataSet()
    {
        // GIVEN
        RecordingMatcher<List<DataSetUpdate>> recordedUpdates = new RecordingMatcher<>();
        // prepare context
        context.checking(new Expectations()
        {
            {
                one(v3api).getDataSets(with(SESSION_TOKEN),
                        with(Arrays.asList(new DataSetPermId(ds4Code))),
                        with(any(DataSetFetchOptions.class)));
                will(returnValue(buildDataSetMap()));

                one(shareIdManager).setShareId(ds4Code, "1");
                one(openBISService).updateShareIdAndSize(ds4Code, "1", DATA_SET_STANDARD_SIZE);

                one(directoryProvider).getDataSetDirectory(with(any(IDatasetLocation.class)));
                will(returnValue(share));

                one(contentProvider).asContentWithoutModifyingAccessTimestamp(ds4Code);
                will(returnValue(goodDs4Content));

                one(openBISService).updateDataSetStatuses(Arrays.asList(ds4Code),
                        DataSetArchivingStatus.AVAILABLE, false);
                one(v3api).updateDataSets(with(SESSION_TOKEN), with((recordedUpdates)));
            }
        });
        // Container2 contains one deleted and one not deleted dataSets
        String containerName = "container2.tar";
        createContainer(containerName, Arrays.asList(ds3Code, ds4Code));
        // Create a container in archive and replicate it.
        File archiveContainer = copyContainerToArchive(archive, containerName);
        File replicateContainer = copyContainerToArchive(replicate, containerName);
        // One of the dataSets in Container 2 was deleted.
        DeletedDataSet deleted3 = new DeletedDataSet(1, ds3Code);

        assertEquals(1, transaction.listContainers().size());

        // WHEN
        task.execute(Arrays.asList(deleted3));

        // THEN
        assertEquals(0, transaction.listContainers().size());
        assertEquals(1, recordedUpdates.recordedObject().size());
        DataSetUpdate dataSetUpdate = recordedUpdates.recordedObject().get(0);
        assertTrue(dataSetUpdate.getPhysicalData().getValue().isArchivingRequested().isModified());
        assertEquals(Boolean.TRUE,
                dataSetUpdate.getPhysicalData().getValue().isArchivingRequested().getValue());
        assertEquals(ds4Code, ((DataSetPermId) dataSetUpdate.getDataSetId()).getPermId());

        AssertionUtil.assertContainsLines(
                "INFO  OPERATION.IdentifierAttributeMappingManager - Mapping file '" +
                        mappingFile + "' successfully loaded.\n" +
                        "INFO  OPERATION.AbstractDataSetDeletionPostProcessingMaintenanceTask - " +
                        "Obtained the list of all datasets in all shares in ?.?? s.\n" +
                        "INFO  OPERATION.AbstractDataSetDeletionPostProcessingMaintenanceTask - " +
                        "MultiDataSetDeletionMaintenanceTask has started processing data sets " +
                        "[20220111121909356-57].\n" +
                        "INFO  OPERATION.AbstractDataSetDeletionPostProcessingMaintenanceTask - " +
                        "Container container2.tar contains 1 not deleted data sets.\n" +
                        "INFO  OPERATION.AbstractDataSetDeletionPostProcessingMaintenanceTask - " +
                        "Not deleted data sets: [20220111121934409-58].\n" +
                        "INFO  OPERATION.AbstractDataSetDeletionPostProcessingMaintenanceTask - " +
                        "Start sanity check on [Dataset '20220111121934409-58']\n" +
                        "INFO  OPERATION.MultiDataSetFileOperationsManager - " +
                        "Reading statistics for input stream: 1.06 KB in 4 chunks took < 1sec.\n" +
                        "INFO  OPERATION.MultiDataSetFileOperationsManager - " +
                        "Writing statistics for output stream: 1.06 KB in 4 chunks took < 1sec.\n" +
                        "INFO  OPERATION.AbstractDataSetDeletionPostProcessingMaintenanceTask - Sanity check finished.\n" +
                        "INFO  OPERATION.AbstractDataSetDeletionPostProcessingMaintenanceTask - " +
                        "Container container2.tar was successfully deleted from the database.\n" +
                        "INFO  OPERATION.MultiDataSetArchiveCleaner - File immediately deleted: " +
                        archiveContainer.getAbsolutePath() + "\n" +
                        "INFO  OPERATION.MultiDataSetArchiveCleaner - File immediately deleted: " +
                        replicateContainer.getAbsolutePath(),
                getLogContent());
    }

    @Test
    public void testDeleteContainerThrowAnException()
    {
        // GIVEN

        // Container2 contains one deleted and one not deleted dataSets
        String containerName = "container2.tar";
        createContainer(containerName, Arrays.asList(ds3Code, ds4Code));
        // Create a container in archive and replicate it.
        File archiveContainer = copyContainerToArchive(archive, containerName);
        File replicateContainer = copyContainerToArchive(replicate, containerName);
        // One of the dataSets in Container 2 was deleted.
        DeletedDataSet deleted3 = new DeletedDataSet(1, ds3Code);

        final Sequence sequence = context.sequence("tasks");
        RecordingMatcher<List<DataSetUpdate>> recordedUpdates = new RecordingMatcher<>();
        // prepare context
        context.checking(new Expectations()
        {
            {
                // first task.execute() call
                one(openBISService).listDeletedDataSets(null, MAX_DELETION_DATE);
                will(returnValue(Arrays.asList(deleted3)));
                inSequence(sequence);

                one(v3api).getDataSets(with(SESSION_TOKEN),
                        with(Arrays.asList(new DataSetPermId(ds4Code))),
                        with(any(DataSetFetchOptions.class)));
                will(returnValue(buildDataSetMap()));
                inSequence(sequence);

                one(shareIdManager).setShareId(ds4Code, "1");
                inSequence(sequence);
                one(openBISService).updateShareIdAndSize(ds4Code, "1", DATA_SET_STANDARD_SIZE);
                inSequence(sequence);

                one(directoryProvider).getDataSetDirectory(with(any(IDatasetLocation.class)));
                will(returnValue(share));
                inSequence(sequence);

                one(contentProvider).asContentWithoutModifyingAccessTimestamp(ds4Code);
                will(returnValue(goodDs4Content));
                inSequence(sequence);

                // second task.execute() call
                one(openBISService).listDeletedDataSets(null, MAX_DELETION_DATE);
                will(returnValue(Arrays.asList(deleted3)));
                inSequence(sequence);

                one(v3api).getDataSets(with(SESSION_TOKEN),
                        with(Arrays.asList(new DataSetPermId(ds4Code))),
                        with(any(DataSetFetchOptions.class)));
                will(returnValue(buildDataSetMap()));
                inSequence(sequence);

                one(shareIdManager).setShareId(ds4Code, "1");
                inSequence(sequence);
                one(openBISService).updateShareIdAndSize(ds4Code, "1", DATA_SET_STANDARD_SIZE);
                inSequence(sequence);

                one(directoryProvider).getDataSetDirectory(with(any(IDatasetLocation.class)));
                will(returnValue(share));
                inSequence(sequence);

                one(contentProvider).asContentWithoutModifyingAccessTimestamp(ds4Code);
                will(returnValue(goodDs4Content));
                inSequence(sequence);

                one(openBISService).updateDataSetStatuses(Arrays.asList(ds4Code),
                        DataSetArchivingStatus.AVAILABLE, false);
                inSequence(sequence);
                one(v3api).updateDataSets(with(SESSION_TOKEN), with((recordedUpdates)));
                inSequence(sequence);
            }
        });

        // let's test that transaction throw an exception
        transaction.throwAnExceptionWhenUserTryToDeleteContainer = true;

        // WHEN
        try
        {
            task.execute();
        } catch (RuntimeException e)
        {
            assertEquals(e.getMessage(),
                    "Can't delete the container because something bad happened!");
        }

        //check that archive and replica WAS NOT deleted.
        assertTrue(archiveContainer.exists());
        assertTrue(replicateContainer.exists());

        AssertionUtil.assertContainsNot(
                "INFO  OPERATION.AbstractDataSetDeletionPostProcessingMaintenanceTask - " +
                        "Container container2.tar was successfully deleted from the database.\n",
                getLogContent());

        transaction.throwAnExceptionWhenUserTryToDeleteContainer = false;
        task.execute();

        // THEN

        //check that archive and replica WAS deleted.
        assertFalse(archiveContainer.exists());
        assertFalse(replicateContainer.exists());

        AssertionUtil.assertContainsLines(
                "INFO  OPERATION.AbstractDataSetDeletionPostProcessingMaintenanceTask - " +
                        "Container container2.tar was successfully deleted from the database.\n",
                getLogContent());
    }

    @Test
    public void testTaskWillProcessDataSetAgainIfItFails()
    {
        // GIVEN
        // Container2 contains one deleted and one not deleted dataSets
        String containerName = "container2.tar";
        createContainer(containerName, Arrays.asList(ds3Code, ds4Code));
        // Create a container in archive and replicate it.
        copyContainerToArchive(archive, containerName);
        copyContainerToArchive(replicate, containerName);
        // One of the dataSets in Container 2 was deleted.
        DeletedDataSet deleted3 = new DeletedDataSet(1, ds3Code);

        final Sequence sequence = context.sequence("tasks");
        RecordingMatcher<List<DataSetUpdate>> recordedUpdates = new RecordingMatcher<>();

        // prepare context
        context.checking(new Expectations()
        {
            {
                // first task.execute() call
                one(openBISService).listDeletedDataSets(null, MAX_DELETION_DATE);
                will(returnValue(Arrays.asList(deleted3)));
                inSequence(sequence);

                one(v3api).getDataSets(with(SESSION_TOKEN),
                        with(Arrays.asList(new DataSetPermId(ds4Code))),
                        with(any(DataSetFetchOptions.class)));
                will(returnValue(buildDataSetMap()));
                inSequence(sequence);

                one(shareIdManager).setShareId(ds4Code, "1");
                inSequence(sequence);
                one(openBISService).updateShareIdAndSize(ds4Code, "1", DATA_SET_STANDARD_SIZE);
                inSequence(sequence);

                one(directoryProvider).getDataSetDirectory(with(any(IDatasetLocation.class)));
                will(returnValue(share));
                inSequence(sequence);

                one(contentProvider).asContentWithoutModifyingAccessTimestamp(ds4Code);
                will(returnValue(badDs4Content));
                inSequence(sequence);

                // second task.execute() call
                one(openBISService).listDeletedDataSets(null, MAX_DELETION_DATE);
                will(returnValue(Arrays.asList(deleted3)));
                inSequence(sequence);

                one(v3api).getDataSets(with(SESSION_TOKEN),
                        with(Arrays.asList(new DataSetPermId(ds4Code))),
                        with(any(DataSetFetchOptions.class)));
                will(returnValue(buildDataSetMap()));
                inSequence(sequence);

                one(shareIdManager).setShareId(ds4Code, "1");
                inSequence(sequence);
                one(openBISService).updateShareIdAndSize(ds4Code, "1", DATA_SET_STANDARD_SIZE);
                inSequence(sequence);

                one(directoryProvider).getDataSetDirectory(with(any(IDatasetLocation.class)));
                will(returnValue(share));
                inSequence(sequence);

                one(contentProvider).asContentWithoutModifyingAccessTimestamp(ds4Code);
                will(returnValue(goodDs4Content));
                inSequence(sequence);

                one(openBISService).updateDataSetStatuses(Arrays.asList(ds4Code),
                        DataSetArchivingStatus.AVAILABLE, false);
                inSequence(sequence);
                one(v3api).updateDataSets(with(SESSION_TOKEN), with((recordedUpdates)));
                inSequence(sequence);
            }
        });

        // WHEN
        // Call task.execute() for the first time. It should fail and NOT UPDATE lastSeenDataSetFile.
        assertFalse(lastSeenDataSetFile.exists());
        try
        {
            task.execute();
        } catch (RuntimeException e)
        {
            assertEquals(e.getMessage(), WRONG_PATH_ERROR);
        }

        assertFalse(lastSeenDataSetFile.exists());

        // Call task.execute() for the second time. It should pass and UPDATE lastSeenDataSetFile.
        task.execute();

        // THEN
        assertTrue(lastSeenDataSetFile.exists());
        assertEquals("1", FileUtilities.loadExactToString(lastSeenDataSetFile).trim());
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = WRONG_PATH_ERROR)
    public void testSanityCheckFailedBecauseOfDifferentPaths()
    {
        testSanityCheckThrowRuntimeException(badDs4Content);
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp =
            "The path '20220111121934409-58/original/test.txt' should be in store and archive either both " +
                    "directories or files but not mixed: In the store it is a directory but in the archive it is a file.")
    public void testSanityCheckFailedBecauseItExpectsFileButGotDir()
    {
        MockContent content = new MockContent(
                ":0:0", "original/:0:0", "original/test.txt/:0:0"
        );

        testSanityCheckThrowRuntimeException(content);
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp =
            "The directory '20220111121934409-58/original' has in the store 2 files but 1 in the archive.")
    public void testSanityCheckFailedBecauseItHasToManyFiles()
    {
        MockContent content = new MockContent(
                ":0:0", "original/:0:0", "original/test1.txt:0:0", "original/test2.txt:0:0"
        );
        testSanityCheckThrowRuntimeException(content);
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp =
            "The file '20220111121934409-58/original/test.txt' has in the store 10 bytes but 8 in the archive.")
    public void testSanityCheckFailedBecauseFileHasWrongSize()
    {
        MockContent content = new MockContent(
                ":0:0", "original/:0:0", "original/test.txt:10:70486887"
        );
        testSanityCheckThrowRuntimeException(content);
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp =
            "The file '20220111121934409-58/original/test.txt' has in the store the checksum 00000000 " +
                    "but 70486887 in the archive.")
    public void testSanityCheckFailedBecauseFileHasWrongChecksum()
    {
        MockContent content = new MockContent(
                ":0:0", "original/:0:0", "original/test.txt:8:0"
        );
        testSanityCheckThrowRuntimeException(content);
    }

    private void testSanityCheckThrowRuntimeException(MockContent badContent)
    {
        // prepare context
        context.checking(new Expectations()
        {
            {
                one(v3api).getDataSets(with(SESSION_TOKEN),
                        with(Arrays.asList(new DataSetPermId(ds4Code))),
                        with(any(DataSetFetchOptions.class)));
                will(returnValue(buildDataSetMap()));

                one(shareIdManager).setShareId(ds4Code, "1");
                one(openBISService).updateShareIdAndSize(ds4Code, "1", DATA_SET_STANDARD_SIZE);

                one(directoryProvider).getDataSetDirectory(with(any(IDatasetLocation.class)));
                will(returnValue(share));

                one(contentProvider).asContentWithoutModifyingAccessTimestamp(ds4Code);
                will(returnValue(badContent));
            }
        });
        // Container2 contains one deleted and one not deleted dataSets
        String containerName = "container2.tar";
        createContainer(containerName, Arrays.asList(ds3Code, ds4Code));
        // Create a container in archive.
        copyContainerToArchive(archive, containerName);
        // One of the dataSets in Container 2 was deleted.
        DeletedDataSet deleted3 = new DeletedDataSet(1, ds3Code);

        // task will throw RuntimeException because of the badContent.
        task.execute(Arrays.asList(deleted3));
    }

    @Test
    public void testFirstSuitableShareFinder() throws IOException
    {
        // GIVEN
        IFreeSpaceProvider freeSpaceProvider = context.mock(IFreeSpaceProvider.class);
        Properties properties = createProperties(true, false);

        MockMultiDataSetDeletionMaintenanceTask taskWithoutMappingFile =
                new MockMultiDataSetDeletionMaintenanceTask(
                        transaction, transaction, openBISService, dataStoreService,
                        contentProvider, shareIdManager, v3api, configProvider,
                        new MockMultiDataSetFileOperationsManager(properties, directoryProvider,
                                freeSpaceProvider), freeSpaceProvider);
        taskWithoutMappingFile.setUp("", properties);

        final HostAwareFile HostAwareShare = new HostAwareFile(share);

        RecordingMatcher<List<DataSetUpdate>> recordedUpdates = new RecordingMatcher<>();
        // prepare context
        context.checking(new Expectations()
        {
            {
                // first taskWithoutMappingFile.execute() call
                one(v3api).getDataSets(with(SESSION_TOKEN),
                        with(Arrays.asList(new DataSetPermId(ds4Code))),
                        with(any(DataSetFetchOptions.class)));
                will(returnValue(buildDataSetMap()));

                one(freeSpaceProvider).freeSpaceKb(HostAwareShare);
                will(returnValue(0L)); // not enough free space

                // second taskWithoutMappingFile.execute() call
                one(v3api).getDataSets(with(SESSION_TOKEN),
                        with(Arrays.asList(new DataSetPermId(ds4Code))),
                        with(any(DataSetFetchOptions.class)));
                will(returnValue(buildDataSetMap()));

                one(freeSpaceProvider).freeSpaceKb(HostAwareShare);
                will(returnValue(2048L)); // enough free space

                one(shareIdManager).setShareId(ds4Code, "1");
                one(openBISService).updateShareIdAndSize(ds4Code, "1", DATA_SET_STANDARD_SIZE);

                one(directoryProvider).getDataSetDirectory(with(any(IDatasetLocation.class)));
                will(returnValue(share));

                one(contentProvider).asContentWithoutModifyingAccessTimestamp(ds4Code);
                will(returnValue(goodDs4Content));

                one(openBISService).updateDataSetStatuses(Arrays.asList(ds4Code),
                        DataSetArchivingStatus.AVAILABLE, false);
                one(v3api).updateDataSets(with(SESSION_TOKEN), with((recordedUpdates)));
            }
        });
        // Container2 contains one deleted and one not deleted dataSets
        String containerName = "container2.tar";
        createContainer(containerName, Arrays.asList(ds3Code, ds4Code));
        // Create a container in archive and replicate it.
        File archiveContainer = copyContainerToArchive(archive, containerName);
        File replicateContainer = copyContainerToArchive(replicate, containerName);
        // One of the dataSets in Container 2 was deleted.
        DeletedDataSet deleted3 = new DeletedDataSet(1, ds3Code);

        // WHEN
        // call taskWithoutMappingFile.execute for the first time with no free space
        try
        {
            taskWithoutMappingFile.execute(Arrays.asList(deleted3));
        } catch (RuntimeException e)
        {
            assertEquals(e.getMessage(),
                    "Unarchiving of data set '20220111121934409-58' has failed, "
                            + "because no appropriate destination share was found. "
                            + "Most probably there is not enough free space in the data store.");
        }

        // check that container WAS NOT deleted if share WAS NOT found
        AssertionUtil.assertContainsNot(
                "INFO  OPERATION.AbstractDataSetDeletionPostProcessingMaintenanceTask - " +
                        "Container container2.tar was successfully deleted from the database.\n",
                getLogContent());
        assertTrue(archiveContainer.exists());
        assertTrue(replicateContainer.exists());

        // call taskWithoutMappingFile.execute for the second time WITH free space
        taskWithoutMappingFile.execute(Arrays.asList(deleted3));

        // THEN
        // check that container WAS deleted if share WAS found
        AssertionUtil.assertContainsLines(
                "INFO  OPERATION.AbstractDataSetDeletionPostProcessingMaintenanceTask - " +
                        "Container container2.tar was successfully deleted from the database.\n",
                getLogContent());
        assertFalse(archiveContainer.exists());
        assertFalse(replicateContainer.exists());
    }
}

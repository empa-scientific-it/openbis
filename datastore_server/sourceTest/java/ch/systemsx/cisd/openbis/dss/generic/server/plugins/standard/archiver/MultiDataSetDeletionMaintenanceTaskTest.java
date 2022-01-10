package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.PhysicalData;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.IMultiDataSetArchiverDBTransaction;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.IMultiDataSetArchiverReadonlyQueryDAO;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDataSetArchiverContainerDTO;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDataSetArchiverDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.IConfigProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataStoreServiceInternal;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProviderTestWrapper;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSet;
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

    private MultiDataSetDeletionMaintenanceTask task;

    private static final String LAST_SEEN_DATA_SET_FILE = "last-seen-data-set";

    private final static String SESSION_TOKEN = "sessionToken";

    private static final String DSS_CODE = "dss1";

    private Map<IDataSetId, DataSet> dataSetMap;

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

        public MockMultiDataSetDeletionMaintenanceTask(IMultiDataSetArchiverDBTransaction transaction,
                                                       IMultiDataSetArchiverReadonlyQueryDAO readonlyDAO,
                                                       IEncapsulatedOpenBISService openBISService,
                                                       IDataStoreServiceInternal dataStoreService,
                                                       IHierarchicalContentProvider contentProvider,
                                                       IShareIdManager shareIdManager,
                                                       IApplicationServerApi v3api,
                                                       IConfigProvider configProvider)
        {
            this.transaction = transaction;
            this.readonlyDAO = readonlyDAO;
            this.openBISService = openBISService;
            this.dataStoreService = dataStoreService;
            this.contentProvider = contentProvider;
            this.shareIdManager = shareIdManager;
            this.v3api = v3api;
            this.configProvider = configProvider;
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
        protected IDataStoreServiceInternal dataStoreService()
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
    }

    @BeforeMethod
    public void setUpTestEnvironment()
    {
        logRecorder = LogRecordingUtils.createRecorder("%-5p %c - %m%n", Level.INFO, "OPERATION.*");
        context = new Mockery();
        transaction = new MockMultiDataSetArchiverDBTransaction();

        createStore();
        prepareData();
        buildDataSetMap();

        IEncapsulatedOpenBISService openBISService = context.mock(IEncapsulatedOpenBISService.class);
        IDataStoreServiceInternal dataStoreService = context.mock(IDataStoreServiceInternal.class);
        IHierarchicalContentProvider contentProvider = context.mock(IHierarchicalContentProvider.class);
        IShareIdManager shareIdManager = context.mock(IShareIdManager.class);
        IApplicationServerApi v3api = context.mock(IApplicationServerApi.class);
        IDataSetDirectoryProvider directoryProvider = context.mock(IDataSetDirectoryProvider.class);
        IConfigProvider configProvider = context.mock(IConfigProvider.class);

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

                allowing(v3api).getDataSets(with(SESSION_TOKEN), with(Arrays.asList(new DataSetPermId("ds4"))),
                        with(any(DataSetFetchOptions.class)));
                will(returnValue(dataSetMap));
            }
        });
        task = new MockMultiDataSetDeletionMaintenanceTask(
                transaction, transaction, openBISService, dataStoreService,
                contentProvider, shareIdManager, v3api, configProvider
        );
        task.setUp("", createProperties());
    }

    private void buildDataSetMap()
    {
        dataSetMap = new HashMap<>();
        dataSetMap.put(new DataSetPermId("ds4"), generateDataSet(transaction.getDataSetForCode("ds4")));
    }

    private DataSet generateDataSet(MultiDataSetArchiverDataSetDTO ds)
    {
        DataSet dataSet = new DataSet();
        dataSet.setCode(ds.getCode());
        dataSet.setFetchOptions(fetchOptions());

        // Set the PhysicalData
        PhysicalData physicalData = new PhysicalData();
        physicalData.setSize(ds.getSizeInBytes());
        physicalData.setLocation("");
        dataSet.setPhysicalData(physicalData);

        // Set the Sample
        Sample sample = new Sample();
        sample.setCode("Sample1");
        Space space = new Space();
        space.setCode("Space1");
        sample.setSpace(space);
        SampleFetchOptions sfo = new SampleFetchOptions();
        sfo.withSpace();
        sfo.withProject();
        sample.setFetchOptions(sfo);
        dataSet.setSample(sample);

        return dataSet;
    }

    private DataSetFetchOptions fetchOptions() {
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withPhysicalData();
        fetchOptions.withExperiment().withProject().withSpace();
        fetchOptions.withSample().withProject().withSpace();
        fetchOptions.withSample().withSpace();

        return fetchOptions;
    }

    private void prepareData()
    {
        // Container1 contains only deleted dataSets
        MultiDataSetArchiverContainerDTO c1 = transaction.createContainer("container1");
        transaction.insertDataset(dataSetDescription("ds1"), c1);
        transaction.insertDataset(dataSetDescription("ds2"), c1);
        transaction.commit();

        // Container2 contains one deleted and one not deleted dataSets
        MultiDataSetArchiverContainerDTO c2 = transaction.createContainer("container2");
        transaction.insertDataset(dataSetDescription("ds3"), c2);
        transaction.insertDataset(dataSetDescription("ds4"), c2);
        transaction.commit();
    }

    private DatasetDescription dataSetDescription(final String code)
    {
        DatasetDescription description = new DatasetDescription();
        description.setDataSetCode(code);
        description.setDataSetSize(10L);
        return description;
    }

    private Properties createProperties()
    {
        Properties properties = new Properties();

        lastSeenDataSetFile = new File(workingDirectory, LAST_SEEN_DATA_SET_FILE);
        properties.setProperty(
                MultiDataSetDeletionMaintenanceTask.LAST_SEEN_EVENT_ID_FILE,
                lastSeenDataSetFile.getPath()
        );
        properties.setProperty("archiver." + MultiDataSetFileOperationsManager.FINAL_DESTINATION_KEY, archive.getAbsolutePath());
        properties.setProperty("archiver." + MultiDataSetFileOperationsManager.REPLICATED_DESTINATION_KEY, replicate.getAbsolutePath());
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
                    "DEFAULT\t1\t" + archive + "\n");
        } catch (IOException ex)
        {
            assertEquals("Invalid file path", ex.getMessage());
        }
    }

    @AfterMethod
    public void tearDown()
    {
        ServiceProviderTestWrapper.restoreApplicationContext();
        context.assertIsSatisfied();
    }

    @Test
    public void testExecuteWithEmptyDeletedDataSetList()
    {
        task.execute(new ArrayList<>());
        AssertionUtil.assertContainsLines("", logRecorder.getLogContent());
    }

    @Test
    public void testContainerContainsOnlyDeletedDataSetList()
    {
        DeletedDataSet deleted1 = new DeletedDataSet(1, "ds1");
        DeletedDataSet deleted2 = new DeletedDataSet(1, "ds2");
        task.execute(Arrays.asList(deleted1, deleted2));
        AssertionUtil.assertContainsLines("", logRecorder.getLogContent());
    }

    @Test
    public void testContainerContainsDeletedAndNoneDeletedDataSet()
    {
        DeletedDataSet deleted3 = new DeletedDataSet(1, "ds3");
        task.execute(Arrays.asList(deleted3));
        AssertionUtil.assertContainsLines("", logRecorder.getLogContent());
    }
}

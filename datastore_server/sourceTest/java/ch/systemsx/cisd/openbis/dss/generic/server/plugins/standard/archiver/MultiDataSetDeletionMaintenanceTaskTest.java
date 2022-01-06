package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.IMultiDataSetArchiverDBTransaction;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.IMultiDataSetArchiverReadonlyQueryDAO;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataStoreServiceInternal;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProviderTestWrapper;
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

    private static final class MockMultiDataSetDeletionMaintenanceTask extends MultiDataSetDeletionMaintenanceTask {

        private IMultiDataSetArchiverDBTransaction transaction;

        private IMultiDataSetArchiverReadonlyQueryDAO readonlyDAO;

        private IEncapsulatedOpenBISService openBISService;

        private IDataStoreServiceInternal dataStoreService;

        private IHierarchicalContentProvider contentProvider;

        private IShareIdManager shareIdManager;

        private IApplicationServerApi v3api;

        public MockMultiDataSetDeletionMaintenanceTask(IMultiDataSetArchiverDBTransaction transaction,
                                                       IMultiDataSetArchiverReadonlyQueryDAO readonlyDAO,
                                                       IEncapsulatedOpenBISService openBISService,
                                                       IDataStoreServiceInternal dataStoreService,
                                                       IHierarchicalContentProvider contentProvider,
                                                       IShareIdManager shareIdManager,
                                                       IApplicationServerApi v3api) {
            this.transaction = transaction;
            this.readonlyDAO = readonlyDAO;
            this.openBISService = openBISService;
            this.dataStoreService = dataStoreService;
            this.contentProvider = contentProvider;
            this.shareIdManager = shareIdManager;
            this.v3api = v3api;
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
        protected IEncapsulatedOpenBISService getOpenBISService() {
            return openBISService;
        }

        @Override
        protected IDataStoreServiceInternal dataStoreService() {
            return dataStoreService;
        }

        @Override
        protected IHierarchicalContentProvider getHierarchicalContentProvider() {
            return contentProvider;
        }

        @Override
        protected IApplicationServerApi getV3ApplicationService() {
            return v3api;
        }

        @Override
        protected IShareIdManager getShareIdManager() {
            return shareIdManager;
        }
    }

    @BeforeMethod
    public void setUpTestEnvironment()
    {
        logRecorder = LogRecordingUtils.createRecorder("%-5p %c - %m%n", Level.INFO, "OPERATION.*");
        transaction = new MockMultiDataSetArchiverDBTransaction();

        context = new Mockery();

        IEncapsulatedOpenBISService openBISService = context.mock(IEncapsulatedOpenBISService.class);
        IDataStoreServiceInternal dataStoreService = context.mock(IDataStoreServiceInternal.class);
        IHierarchicalContentProvider contentProvider = context.mock(IHierarchicalContentProvider.class);
        IShareIdManager shareIdManager = context.mock(IShareIdManager.class);
        IApplicationServerApi v3api = context.mock(IApplicationServerApi.class);
        IDataSetDirectoryProvider directoryProvider = context.mock(IDataSetDirectoryProvider.class);

        context.checking(new Expectations()
        {
            {
                allowing(dataStoreService).getDataSetDirectoryProvider();
                will(returnValue(directoryProvider));
            }
        });
        task = new MockMultiDataSetDeletionMaintenanceTask(
                transaction, transaction, openBISService,
                dataStoreService, contentProvider, shareIdManager, v3api
        );
        createStore();
        task.setUp("", createProperties());
    }

    private Properties createProperties() {
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
    public void testExecuteEmptyDeletedDataSet()
    {
        task.execute(new ArrayList<>());
        AssertionUtil.assertContainsLines("", logRecorder.getLogContent());
    }
}

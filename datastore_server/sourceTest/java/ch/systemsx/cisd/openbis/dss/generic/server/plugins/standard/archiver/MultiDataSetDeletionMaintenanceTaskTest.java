package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProviderTestWrapper;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;
import org.apache.log4j.Level;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

@Friend(toClasses = MultiDataSetDeletionMaintenanceTask.class)
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

    private MultiDataSetDeletionMaintenanceTask task;

    private static final String LAST_SEEN_DATA_SET_FILE = "last-seen-data-set";

    @BeforeMethod
    public void setUpTestEnvironment()
    {
        logRecorder = LogRecordingUtils.createRecorder("%-5p %c - %m%n", Level.INFO, "OPERATION.*");
        context = new Mockery();
//        task = new MultiDataSetDeletionMaintenanceTask();
        createStore();
//        task.setUp("", createProperties());
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
                                                        "DEFAULT\t1\ttargets\n");
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
    public void test()
    {

    }
}

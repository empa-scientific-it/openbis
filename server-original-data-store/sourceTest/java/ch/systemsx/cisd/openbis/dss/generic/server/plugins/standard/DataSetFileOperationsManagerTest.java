/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.concurrent.ExecutionResult;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.BooleanStatus;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IPathCopier;
import ch.systemsx.cisd.common.filesystem.ssh.ISshCommandExecutor;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.process.ProcessResult;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.RSyncConfig;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * @author Piotr Buczek
 */
@Friend(toClasses = DataSetFileOperationsManager.class)
public class DataSetFileOperationsManagerTest extends AbstractFileSystemTestCase
{
    private static final String DUMMY_ERROR_MESSAGE = "dummy error message";

    private static final ProcessResult OK_RESULT = new ProcessResult(Arrays.asList(""), 0, null,
            ExecutionResult.create(null), null, 0, (List<String>) null, null, null, null);

    private static final ProcessResult ERROR_RESULT = new ProcessResult(Arrays.asList(""), 0, null,
            ExecutionResult.createExceptional(new Exception(DUMMY_ERROR_MESSAGE)), null, 0,
            (List<String>) null, null, null, null);

    private static ProcessResult createOkResultWithOutput(List<String> output)
    {
        return new ProcessResult(Arrays.asList(""), 0, null, ExecutionResult.create(null), null, 0,
                output, null, null, null);
    }

    private static final long DEFAULT_TIMEOUT_MILLIS =
            DataSetFileOperationsManager.DEFAULT_TIMEOUT_SECONDS * DateUtils.MILLIS_PER_SECOND;

    private static final String LOCATION_1 = "l1";

    private static final String DS1_CODE = "ds1";

    private static final String DS2_CODE = "ds2";

    private static final String DS1_LOCATION = LOCATION_1 + File.separator + DS1_CODE;

    private static final String DS2_LOCATION = LOCATION_1 + File.separator + DS2_CODE;

    private static final String DS1_DATA_FILE_1 = "data1_1.txt";

    private static final String DS1_DATA_FILE_2 = "data1_2.txt";

    private static final String DS2_DATA_FILE = "data2.txt";

    private static final String DATA1_1 = "hello test 1 1";

    private static final String DATA1_2 = "hello test 1 2";

    private static final String DATA2 = "hello test 2";

    private static final String ORIGINAL = "original";

    private static final String SHARE_ID = "42";

    private File storeRoot;

    private DatasetDescription ds1;

    private File ds1Location;

    private File ds1Data1;

    private File ds1Data2;

    private DatasetDescription ds2;

    private File ds2Location;

    private File ds2Data;

    private File ds1ArchivedLocationFile;

    private File ds1ArchivedDataFile1;

    private File ds1ArchivedDataFile2;

    private File ds2ArchivedLocationFile;

    private File ds2ArchivedDataFile;

    private Mockery context;

    private IPathCopierFactory copierFactory;

    private IPathCopier copier;

    private ISshCommandExecutorFactory sshFactory;

    private ISshCommandExecutor sshExecutor;

    private File destination;

    private File rsyncExec;

    private File sshExec;

    private File gfindExec;

    private long timeoutInSeconds;

    @BeforeClass
    public void init()
    {
        LogInitializer.init();
    }

    @BeforeMethod
    public void beforeMethod() throws Exception
    {
        context = new Mockery();
        copierFactory = context.mock(IPathCopierFactory.class);
        copier = context.mock(IPathCopier.class);
        sshFactory = context.mock(ISshCommandExecutorFactory.class);
        sshExecutor = context.mock(ISshCommandExecutor.class);

        storeRoot = new File(workingDirectory, "store");
        storeRoot.mkdirs();
        File share = new File(storeRoot, SHARE_ID);

        ds1 = createDataSetDescription(DS1_CODE, DS1_LOCATION, true);
        ds1Location = new File(share, DS1_LOCATION);
        File ds1Folder = new File(ds1Location, ORIGINAL);
        ds1Folder.mkdirs();
        ds1Data1 = new File(ds1Folder, DS1_DATA_FILE_1);
        ds1Data2 = new File(ds1Folder, DS1_DATA_FILE_2);
        FileUtilities.writeToFile(ds1Data1, DATA1_1);
        FileUtilities.writeToFile(ds1Data2, DATA1_2);

        ds2 = createDataSetDescription(DS2_CODE, DS2_LOCATION, true);
        ds2Location = new File(share, DS2_LOCATION);
        File ds2Folder = new File(ds2Location, ORIGINAL);
        ds2Folder.mkdirs();
        ds2Data = new File(ds2Folder, DS2_DATA_FILE);
        FileUtilities.writeToFile(ds2Data, DATA2);

        destination = new File(workingDirectory, "destination");
        destination.mkdirs();
        deleted =
                new File(destination,
                        DataSetFileOperationsManager.FOLDER_OF_AS_DELETED_MARKED_DATA_SETS);

        ds1ArchivedLocationFile = new File(destination, ds1.getDataSetLocation());
        ds1ArchivedDataFile1 =
                new File(ds1ArchivedLocationFile, ORIGINAL + File.separator + ds1Data1.getName());
        ds1ArchivedDataFile2 =
                new File(ds1ArchivedLocationFile, ORIGINAL + File.separator + ds1Data2.getName());
        ds2ArchivedLocationFile = new File(destination, ds2.getDataSetLocation());
        ds2ArchivedDataFile =
                new File(ds2ArchivedLocationFile, ORIGINAL + File.separator + ds2Data.getName());

        rsyncExec = new File(workingDirectory, "my-rsync");
        rsyncExec.createNewFile();
        sshExec = new File(workingDirectory, "my-rssh");
        sshExec.createNewFile();
        gfindExec = new File(workingDirectory, "my-gfind");
        gfindExec.createNewFile();

        timeoutInSeconds = 5;
    }

    private DatasetDescription createDataSetDescription(String dataSetCode, String location,
            boolean withSample)
    {
        DatasetDescription description = new DatasetDescription();
        description.setDataSetCode(dataSetCode);
        description.setDatasetTypeCode("MY_DATA");
        description.setDataSetLocation(location);
        description.setSpaceCode("g");
        description.setProjectCode("p");
        description.setExperimentCode("e");
        description.setExperimentIdentifier("/g/p/e");
        description.setExperimentTypeCode("MY_EXPERIMENT");
        if (withSample)
        {
            description.setSampleCode("s");
            description.setSampleIdentifier("/g/s");
            description.setSampleTypeCode("MY_SAMPLE");
        }
        return description;
    }

    @AfterMethod
    public void afterMethod()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    /*
     * --< LOCAL >----------------------------------------------------------------------------------
     */

    @Test(groups = "slow")
    public void testLocalCopyToDestination()
    {
        Properties properties = createLocalDestinationProperties();
        properties.remove(DataSetFileOperationsManager.RSYNC_EXEC + "-executable");
        IDataSetFileOperationsManager dataSetCopier =
                new DataSetFileOperationsManager(properties, new RsyncArchiveCopierFactory(),
                        sshFactory);
        prepareForCheckingLastModifiedDate();
        prepareLocalCreateAndCheckCopier();

        // check that data set is not yet in archive
        assertDs1NotInArchive();

        /*
         * archive 1st time
         */
        Status status = dataSetCopier.copyToDestination(ds1Location, ds1);
        assertSuccessful(status);
        // check that data set is now in archive
        assertDs1InArchive();
        // check that data set is still in store
        assertDs1InStore();

        setDummyRsync(properties);
        dataSetCopier =
                new DataSetFileOperationsManager(properties, copierFactory, sshFactory);
        /*
         * archive 2nd time (could happen on crash of DSS, but shouldn't hurt)
         */
        context.checking(new Expectations()
            {
                {
                    /*
                     * use rsync
                     */
                    one(copier).copyToRemote(ds1Location,
                            ds1ArchivedLocationFile.getParentFile().getAbsolutePath(), null, null,
                            null, null, null);
                    will(returnValue(Status.OK));
                }
            });
        status = dataSetCopier.copyToDestination(ds1Location, ds1);
        assertSuccessful(status);
        // check that data set is now in archive
        assertDs1InArchive();
        // check that data set is still in store
        assertDs1InStore();

        context.assertIsSatisfied();
    }

    @Test(groups = "slow")
    public void testLocalCopyToNonExistentDestination()
    {
        Properties properties = createLocalDestinationProperties();
        properties.remove(DataSetFileOperationsManager.RSYNC_EXEC + "-executable");
        IDataSetFileOperationsManager dataSetCopier =
                new DataSetFileOperationsManager(properties, new RsyncArchiveCopierFactory(),
                        sshFactory);
        prepareForCheckingLastModifiedDate();

        destination.delete(); // if destination folder doesn't exist it will be created

        // check that data set is not yet in archive
        assertDs1NotInArchive();

        /*
         * archive
         */
        Status status = dataSetCopier.copyToDestination(ds1Location, ds1);
        assertSuccessful(status);
        // check that data set is now in archive
        assertDs1InArchive();
        // check that data set is still in store
        assertDs1InStore();

        context.assertIsSatisfied();
    }

    @Test(dependsOnMethods = "testLocalCopyToDestination", groups = "slow")
    public void testLocalCopyTwoDataSetsToDestination()
    {
        Properties properties = createLocalDestinationProperties();
        properties.remove(DataSetFileOperationsManager.RSYNC_EXEC + "-executable");
        IDataSetFileOperationsManager dataSetCopier =
                new DataSetFileOperationsManager(properties, new RsyncArchiveCopierFactory(),
                        sshFactory);
        prepareForCheckingLastModifiedDate();

        // check that both data sets are not yet in archive
        assertDs1NotInArchive();
        assertDs2NotInArchive();

        /*
         * copy 1st data set
         */
        Status status1 = dataSetCopier.copyToDestination(ds1Location, ds1);
        assertSuccessful(status1);
        assertDs1InArchive();
        // check that 2nd data set is not yet in archive
        assertDs2NotInArchive();

        /*
         * copy 2nd data set
         */
        Status status2 = dataSetCopier.copyToDestination(ds2Location, ds2);
        assertSuccessful(status2);
        assertDs2InArchive();
        // check that 1st data set is still in archive
        assertDs1InArchive();

        // both data sets should be in the store
        assertDs1InStore();
        assertDs2InStore();

        context.assertIsSatisfied();
    }

    @Test(dependsOnMethods = "testLocalCopyToDestination", groups = "slow")
    public void testLocalRetrieveFromDestination()
    {
        /*
         * copy to archive
         */
        Properties properties = createLocalDestinationProperties();
        properties.remove(DataSetFileOperationsManager.RSYNC_EXEC + "-executable");
        IDataSetFileOperationsManager dataSetCopier =
                new DataSetFileOperationsManager(properties, new RsyncArchiveCopierFactory(),
                        sshFactory);
        prepareForCheckingLastModifiedDate();

        // check that data set is not yet in archive
        assertDs1NotInArchive();

        Status status = dataSetCopier.copyToDestination(ds1Location, ds1);

        assertSuccessful(status);
        assertDs1InArchive();

        /*
         * delete from store
         */
        try
        {
            FileUtils.deleteDirectory(ds1Location);
        } catch (IOException e)
        {
            fail(e.getMessage());
        }
        assertFalse(ds1Data1.exists());
        assertFalse(ds1Data2.exists());

        /*
         * retrieve from archive - 1st time
         */
        Status statusRetrieve = dataSetCopier.retrieveFromDestination(ds1Location, ds1);
        assertSuccessful(statusRetrieve);
        assertDs1InStore();
        assertDs1InArchive();
        assertDs2InStore(); // ds2 shouldn't be affected at all

        /*
         * retrieve from archive - 2nd time (possible e.g. after crash)
         */
        statusRetrieve = dataSetCopier.retrieveFromDestination(ds1Location, ds1);
        assertSuccessful(statusRetrieve);
        assertDs1InStore();
        assertDs1InArchive();
        assertDs2InStore(); // ds2 shouldn't be affected at all

        context.assertIsSatisfied();
    }

    @Test(dependsOnMethods = "testLocalCopyToDestination", groups = "slow")
    public void testLocalSynchronizedWithDestination()
    {
        Properties properties = createLocalDestinationProperties();
        properties.remove(DataSetFileOperationsManager.RSYNC_EXEC + "-executable");
        IDataSetFileOperationsManager dataSetCopier =
                new DataSetFileOperationsManager(properties, new RsyncArchiveCopierFactory(),
                        sshFactory);
        prepareForCheckingLastModifiedDate();

        // check that data set is not yet in archive
        assertDs1NotInArchive();

        /*
         * before copying - doesn't exist
         */
        BooleanStatus boolStatus = dataSetCopier.isSynchronizedWithDestination(ds1Location, ds1);
        assertFalse(boolStatus);

        /*
         * copy to archive
         */
        Status status = dataSetCopier.copyToDestination(ds1Location, ds1);
        assertSuccessful(status);
        assertDs1InArchive();

        /*
         * after copying - exists
         */
        boolStatus = dataSetCopier.isSynchronizedWithDestination(ds1Location, ds1);
        assertTrue(boolStatus);

        /*
         * modify destination - presence fails
         */
        // increased size of one file - only one should be reported
        FileUtilities.writeToFile(ds1ArchivedDataFile1, DATA1_1 + DATA1_2);
        boolStatus = dataSetCopier.isSynchronizedWithDestination(ds1Location, ds1);
        assertFalse(boolStatus, "Inconsistencies:\n"
                + "'original/data1_1.txt' - different file sizes; store: 14, destination: 28\n");

        // decrease size of second file - both should be reported
        FileUtilities.writeToFile(ds1ArchivedDataFile2, DATA1_2.substring(0, DATA1_2.length() - 1));
        boolStatus = dataSetCopier.isSynchronizedWithDestination(ds1Location, ds1);
        assertFalse(boolStatus, "Inconsistencies:\n"
                + "'original/data1_1.txt' - different file sizes; store: 14, destination: 28\n"
                + "'original/data1_2.txt' - different file sizes; store: 14, destination: 13\n");

        // delete second file from destination
        FileUtilities.delete(ds1ArchivedDataFile2);
        boolStatus = dataSetCopier.isSynchronizedWithDestination(ds1Location, ds1);
        assertFalse(boolStatus, "Inconsistencies:\n"
                + "'original/data1_1.txt' - different file sizes; store: 14, destination: 28\n"
                + "'original/data1_2.txt' - exists in store but is missing in destination\n");

        // create fake file in destination
        try
        {
            File newFile =
                    new File(ds1ArchivedLocationFile, ORIGINAL + File.separator + "fake.txt");
            newFile.createNewFile();
            boolStatus = dataSetCopier.isSynchronizedWithDestination(ds1Location, ds1);
            assertFalse(boolStatus, "Inconsistencies:\n"
                    + "'original/data1_1.txt' - different file sizes; store: 14, destination: 28\n"
                    + "'original/data1_2.txt' - exists in store but is missing in destination\n"
                    + "'original/fake.txt' - exists in destination but is missing in store\n");
        } catch (IOException ex)
        {
            fail(ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test(dependsOnMethods = "testLocalCopyToDestination", groups = "slow")
    public void testLocalPresentInDestination()
    {
        Properties properties = createLocalDestinationProperties();
        properties.remove(DataSetFileOperationsManager.RSYNC_EXEC + "-executable");
        IDataSetFileOperationsManager dataSetCopier =
                new DataSetFileOperationsManager(properties, new RsyncArchiveCopierFactory(),
                        sshFactory);
        prepareForCheckingLastModifiedDate();

        // check that data set is not yet in archive
        assertDs1NotInArchive();

        /*
         * before copying - doesn't exist
         */
        BooleanStatus boolStatus = dataSetCopier.isPresentInDestination(ds1);
        assertFalse(boolStatus);

        /*
         * copy to archive
         */
        Status status = dataSetCopier.copyToDestination(ds1Location, ds1);
        assertSuccessful(status);
        assertDs1InArchive();

        /*
         * after copying - exists
         */
        boolStatus = dataSetCopier.isPresentInDestination(ds1);
        assertTrue(boolStatus);

        /*
         * modify destination - presence still works
         */
        FileUtilities.writeToFile(ds1ArchivedDataFile1, DATA1_1 + DATA1_2);
        boolStatus = dataSetCopier.isPresentInDestination(ds1);
        assertTrue(boolStatus);

        // delete second file from destination
        FileUtilities.delete(ds1ArchivedDataFile2);
        boolStatus = dataSetCopier.isPresentInDestination(ds1);
        assertTrue(boolStatus);

        // create fake file in destination
        try
        {
            File newFile =
                    new File(ds1ArchivedLocationFile, ORIGINAL + File.separator + "fake.txt");
            newFile.createNewFile();
            boolStatus = dataSetCopier.isPresentInDestination(ds1);
            assertTrue(boolStatus);
        } catch (IOException ex)
        {
            fail(ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test(dependsOnMethods = "testLocalCopyToDestination", groups = "slow")
    public void testLocalDeleteFromDestination()
    {
        /*
         * copy to archive
         */
        Properties properties = createLocalDestinationProperties();
        properties.remove(DataSetFileOperationsManager.RSYNC_EXEC + "-executable");
        IDataSetFileOperationsManager dataSetCopier =
                new DataSetFileOperationsManager(properties, new RsyncArchiveCopierFactory(),
                        sshFactory);
        prepareForCheckingLastModifiedDate();

        // check that data set is not yet in archive
        assertDs1NotInArchive();

        Status status = dataSetCopier.copyToDestination(ds1Location, ds1);

        assertSuccessful(status);
        assertDs1InArchive();

        /*
         * delete from archive
         */
        DatasetLocation deletedDs1 = datasetLocation(ds1);
        Status statusDelete = dataSetCopier.deleteFromDestination(deletedDs1);
        assertSuccessful(statusDelete);
        assertDs1NotInArchive();
        assertDs1InStore(); // we didn't delete it from store

        context.assertIsSatisfied();
    }

    private Properties createLocalDestinationProperties()
    {
        final Properties properties = new Properties();
        properties.setProperty(DataSetFileOperationsManager.DESTINATION_KEY, destination.getPath());
        setDummyRsync(properties);
        return properties;
    }

    public void setDummyRsync(final Properties properties)
    {
        properties.setProperty(DataSetFileOperationsManager.RSYNC_EXEC + "-executable",
                rsyncExec.getPath());
    }

    /*
     * --< REMOTE >---------------------------------------------------------------------------------
     */

    @Test
    public void testRemoteViaSshCopyToDestination()
    {
        Properties properties = createRemoteViaSshDestinationProperties();
        prepareRemoteCreateAndCheckCopier(HOST, null, true);
        IDataSetFileOperationsManager dataSetCopier =
                new DataSetFileOperationsManager(properties, copierFactory, sshFactory);
        context.checking(new Expectations()
            {
                {
                    /*
                     * ds1: directory doesn't exist in archive -> create and copy
                     */
                    one(sshExecutor).exists(ds1ArchivedLocationFile.getParentFile().getPath(),
                            timeoutMillis());
                    will(returnValue(BooleanStatus.createFalse()));

                    one(sshExecutor).executeCommandRemotely(
                            "mkdir -p " + ds1ArchivedLocationFile.getParentFile().getPath(),
                            timeoutMillis());
                    will(returnValue(OK_RESULT));

                    one(copier).copyToRemote(ds1Location,
                            ds1ArchivedLocationFile.getParentFile().getPath(),
                            HOST, null, null, null, null);
                    will(returnValue(Status.OK));

                    /*
                     * ds2: directory exists in archive -> just copy
                     */
                    one(sshExecutor).exists(ds2ArchivedLocationFile.getParentFile().getPath(),
                            timeoutMillis());
                    will(returnValue(BooleanStatus.createTrue()));
                    one(sshExecutor).exists(ds2ArchivedLocationFile.getPath(), timeoutMillis());
                    will(returnValue(BooleanStatus.createTrue()));

                    one(copier).copyToRemote(ds2Location,
                            ds2ArchivedLocationFile.getParentFile().getPath(),
                            HOST, null, null, null, null);
                    will(returnValue(Status.OK));
                }
            });
        Status status1 = dataSetCopier.copyToDestination(ds1Location, ds1);
        Status status2 = dataSetCopier.copyToDestination(ds2Location, ds2);
        assertSuccessful(status1);
        assertSuccessful(status2);

        context.assertIsSatisfied();
    }

    @Test
    public void testRemoteViaSshCopyToDestinationWithErrors()
    {
        Properties properties = createRemoteViaSshDestinationProperties();
        prepareRemoteCreateAndCheckCopier(HOST, null, true);
        IDataSetFileOperationsManager dataSetCopier =
                new DataSetFileOperationsManager(properties, copierFactory, sshFactory);
        context.checking(new Expectations()
            {
                {
                    one(sshExecutor).exists(ds1ArchivedLocationFile.getParentFile().getPath(),
                            timeoutMillis());
                    will(returnValue(BooleanStatus.createFalse()));

                    one(sshExecutor).executeCommandRemotely(
                            "mkdir -p " + ds1ArchivedLocationFile.getParentFile().getPath(),
                            timeoutMillis());
                    will(returnValue(OK_RESULT));

                    one(copier).copyToRemote(ds1Location,
                            ds1ArchivedLocationFile.getParentFile().getPath(),
                            HOST, null, null, null, null);
                    will(returnValue(Status.createError(DUMMY_ERROR_MESSAGE)));
                }
            });
        Status status = dataSetCopier.copyToDestination(ds1Location, ds1);
        assertError(status, DUMMY_ERROR_MESSAGE);

        context.assertIsSatisfied();
    }

    @Test
    public void testRemoteViaSshRetrieveFromDestination()
    {
        Properties properties = createRemoteViaSshDestinationProperties();
        prepareRemoteCreateAndCheckCopier(HOST, null, true);
        IDataSetFileOperationsManager dataSetCopier =
                new DataSetFileOperationsManager(properties, copierFactory, sshFactory);
        context.checking(new Expectations()
            {
                {
                    one(sshExecutor).exists(ds1ArchivedLocationFile.getPath(), timeoutMillis());
                    will(returnValue(BooleanStatus.createTrue()));
                    one(copier).copyFromRemote(ds1ArchivedLocationFile.getPath(), HOST,
                            ds1Location.getParentFile(), null, null, null, null);
                    will(returnValue(Status.OK));
                }
            });
        Status status = dataSetCopier.retrieveFromDestination(ds1Location, ds1);
        assertSuccessful(status);

        context.assertIsSatisfied();
    }

    public void testRemoteViaSshRetrieveFromDestinationWithErrors()
    {
        Properties properties = createRemoteViaSshDestinationProperties();
        prepareRemoteCreateAndCheckCopier(HOST, null, true);
        IDataSetFileOperationsManager dataSetCopier =
                new DataSetFileOperationsManager(properties, copierFactory, sshFactory);
        context.checking(new Expectations()
            {
                {
                    /*
                     * ds1: destination doesn't exist
                     */
                    one(sshExecutor).exists(ds1ArchivedLocationFile.getPath(), timeoutMillis());
                    will(returnValue(BooleanStatus.createFalse()));

                    /*
                     * ds2: copy failed
                     */
                    one(sshExecutor).exists(ds2ArchivedLocationFile.getPath(), timeoutMillis());
                    will(returnValue(BooleanStatus.createTrue()));
                    one(copier).copyFromRemote(ds2ArchivedLocationFile.getPath(), HOST,
                            ds2Location.getParentFile(), null, null, null, null);
                    will(returnValue(Status.createError(DUMMY_ERROR_MESSAGE)));
                }
            });

        Status status1 = dataSetCopier.retrieveFromDestination(ds1Location, ds1);
        assertError(status1, DataSetFileOperationsManager.DESTINATION_DOES_NOT_EXIST);

        Status status2 = dataSetCopier.retrieveFromDestination(ds2Location, ds2);
        assertError(status2, DUMMY_ERROR_MESSAGE);

        context.assertIsSatisfied();
    }

    @Test
    public void testRemoteViaSshIsSynchronizedWithDestinationSimpleCheck()
    {
        Properties properties = createRemoteViaSshDestinationProperties();
        prepareRemoteCreateAndCheckCopier(HOST, null, true);
        IDataSetFileOperationsManager dataSetCopier =
                new DataSetFileOperationsManager(properties, copierFactory, sshFactory);
        context.checking(new Expectations()
            {
                {
                    /*
                     * ds1: error
                     */
                    one(sshExecutor).exists(ds1ArchivedLocationFile.getPath(), timeoutMillis());
                    will(returnValue(BooleanStatus.createError(DUMMY_ERROR_MESSAGE)));

                    /*
                     * ds2: not present - directory doesn't exist
                     */
                    one(sshExecutor).exists(ds2ArchivedLocationFile.getPath(), timeoutMillis());
                    will(returnValue(BooleanStatus.createFalse()));
                }
            });
        BooleanStatus status1 = dataSetCopier.isSynchronizedWithDestination(ds1Location, ds1);
        BooleanStatus status2 = dataSetCopier.isSynchronizedWithDestination(ds2Location, ds2);
        assertError(status1, DUMMY_ERROR_MESSAGE);
        assertFalse(status2);

        context.assertIsSatisfied();
    }

    @Test
    public void testRemoteViaSshIsSynchronizedWithDestinationAdvancedCheck()
    {
        Properties properties = createRemoteViaSshDestinationProperties();
        prepareRemoteCreateAndCheckCopier(HOST, null, true);
        IDataSetFileOperationsManager dataSetCopier =
                new DataSetFileOperationsManager(properties, copierFactory, sshFactory);
        context.checking(new Expectations()
            {
                {
                    /*
                     * ds1: directory is present but content is WRONG
                     */
                    one(sshExecutor).exists(ds1ArchivedLocationFile.getPath(), timeoutMillis());
                    will(returnValue(BooleanStatus.createTrue()));

                    one(sshExecutor).executeCommandRemotely(
                            gfindExec.getPath() + " " + ds1ArchivedLocationFile.getPath()
                                    + " -type f -printf \"%p\\t%s\\n\"",
                            timeoutMillis());
                    String filePath1 =
                            ds1ArchivedLocationFile.getPath() + File.separator
                                    + "original/data1_2.txt";
                    String fakePath =
                            ds1ArchivedLocationFile.getPath() + File.separator
                                    + "original/fake.txt";
                    will(returnValue(createOkResultWithOutput(Arrays.asList(filePath1 + "\t4",
                            fakePath + "\t666"))));

                    /*
                     * ds2: directory is present and content is OK
                     */
                    one(sshExecutor).exists(ds2ArchivedLocationFile.getPath(), timeoutMillis());
                    will(returnValue(BooleanStatus.createTrue()));
                    one(sshExecutor).executeCommandRemotely(
                            gfindExec.getPath() + " " + ds2ArchivedLocationFile.getPath()
                                    + " -type f -printf \"%p\\t%s\\n\"",
                            timeoutMillis());
                    String filePath2 =
                            ds2ArchivedLocationFile.getPath() + File.separator
                                    + "original/data2.txt";
                    will(returnValue(createOkResultWithOutput(Arrays.asList(filePath2 + "\t12"))));
                }
            });
        BooleanStatus status1 = dataSetCopier.isSynchronizedWithDestination(ds1Location, ds1);
        assertFalse(status1, "Inconsistencies:\n"
                + "'original/data1_1.txt' - exists in store but is missing in destination\n"
                + "'original/data1_2.txt' - different file sizes; store: 14, destination: 4\n"
                + "'original/fake.txt' - exists in destination but is missing in store\n");

        BooleanStatus status2 = dataSetCopier.isSynchronizedWithDestination(ds2Location, ds2);
        assertTrue(status2);

        context.assertIsSatisfied();
    }

    @Test
    public void testRemoteViaSshIsSynchronizedWithDestinationWithErrors()
    {
        Properties properties = createRemoteViaSshDestinationProperties();
        prepareRemoteCreateAndCheckCopier(HOST, null, true);
        IDataSetFileOperationsManager dataSetCopier =
                new DataSetFileOperationsManager(properties, copierFactory, sshFactory);
        context.checking(new Expectations()
            {
                {
                    /*
                     * ds1: checking existance fails
                     */
                    one(sshExecutor).exists(ds1ArchivedLocationFile.getPath(), timeoutMillis());
                    will(returnValue(BooleanStatus.createError(DUMMY_ERROR_MESSAGE)));

                    /*
                     * ds2: listing fails
                     */
                    one(sshExecutor).exists(ds2ArchivedLocationFile.getPath(), timeoutMillis());
                    will(returnValue(BooleanStatus.createTrue()));

                    one(sshExecutor).executeCommandRemotely(
                            gfindExec.getPath() + " " + ds2ArchivedLocationFile.getPath()
                                    + " -type f -printf \"%p\\t%s\\n\"",
                            timeoutMillis());
                    will(returnValue(ERROR_RESULT));
                }
            });
        BooleanStatus status1 = dataSetCopier.isSynchronizedWithDestination(ds1Location, ds1);
        assertError(status1, DUMMY_ERROR_MESSAGE);
        BooleanStatus status2 = dataSetCopier.isSynchronizedWithDestination(ds2Location, ds2);
        assertError(status2, "listing files failed");

        context.assertIsSatisfied();
    }

    @Test
    public void testRemoteViaSshIsPresentInDestinationSimpleCheck()
    {
        Properties properties = createRemoteViaSshDestinationProperties();
        prepareRemoteCreateAndCheckCopier(HOST, null, true);
        IDataSetFileOperationsManager dataSetCopier =
                new DataSetFileOperationsManager(properties, copierFactory, sshFactory);
        context.checking(new Expectations()
            {
                {
                    /*
                     * ds1: present - directory exists
                     */
                    one(sshExecutor).exists(ds1ArchivedLocationFile.getPath(), timeoutMillis());
                    will(returnValue(BooleanStatus.createTrue()));

                    /*
                     * ds2: not present - directory doesn't exist
                     */
                    one(sshExecutor).exists(ds2ArchivedLocationFile.getPath(), timeoutMillis());
                    will(returnValue(BooleanStatus.createFalse()));
                }
            });
        BooleanStatus status1 = dataSetCopier.isPresentInDestination(ds1);
        BooleanStatus status2 = dataSetCopier.isPresentInDestination(ds2);
        assertTrue(status1);
        assertFalse(status2);

        context.assertIsSatisfied();
    }

    @Test
    public void testRemoteViaSshIsPresentInDestinationWithErrors()
    {
        Properties properties = createRemoteViaSshDestinationProperties();
        prepareRemoteCreateAndCheckCopier(HOST, null, true);
        IDataSetFileOperationsManager dataSetCopier =
                new DataSetFileOperationsManager(properties, copierFactory, sshFactory);
        context.checking(new Expectations()
            {
                {
                    /*
                     * ds1: checking existance fails
                     */
                    one(sshExecutor).exists(ds1ArchivedLocationFile.getPath(), timeoutMillis());
                    will(returnValue(BooleanStatus.createError(DUMMY_ERROR_MESSAGE)));
                }
            });
        BooleanStatus status1 = dataSetCopier.isPresentInDestination(ds1);
        assertError(status1, DUMMY_ERROR_MESSAGE);

        context.assertIsSatisfied();
    }

    @Test
    public void testRemoteViaSshMarkAsDeletedFromDestination()
    {
        Properties properties = createRemoteViaSshDestinationProperties();
        prepareRemoteCreateAndCheckCopier(HOST, null, true);
        IDataSetFileOperationsManager dataSetCopier =
                new DataSetFileOperationsManager(properties, copierFactory, sshFactory);
        context.checking(new Expectations()
            {
                {
                    /*
                     * ds1: directory exists in archive -> delete from directory
                     */
                    one(sshExecutor).exists(ds1ArchivedLocationFile.getPath(), timeoutMillis());
                    will(returnValue(BooleanStatus.createTrue()));
                    one(sshExecutor).executeCommandRemotely(
                            "mkdir -p " + deleted.getPath(), timeoutMillis());
                    will(returnValue(OK_RESULT));
                    one(sshExecutor).executeCommandRemotely(
                            "touch " + new File(deleted, ds1.getDataSetCode()).getPath(),
                            timeoutMillis());
                    will(returnValue(OK_RESULT));

                    /*
                     * ds2: directory doesn't exist in archive -> nothing to do
                     */
                    one(sshExecutor).exists(ds2ArchivedLocationFile.getPath(), timeoutMillis());
                    will(returnValue(BooleanStatus.createFalse()));
                }
            });
        Status status1 = dataSetCopier.markAsDeleted(datasetLocation(ds1));
        Status status2 = dataSetCopier.markAsDeleted(datasetLocation(ds2));
        assertSuccessful(status1);
        assertSuccessful(status2);

        context.assertIsSatisfied();
    }

    @Test
    public void testRemoteViaSshDeleteFromDestination()
    {
        Properties properties = createRemoteViaSshDestinationProperties();
        prepareRemoteCreateAndCheckCopier(HOST, null, true);
        IDataSetFileOperationsManager dataSetCopier =
                new DataSetFileOperationsManager(properties, copierFactory, sshFactory);
        context.checking(new Expectations()
            {
                {
                    /*
                     * ds1: directory exists in archive -> delete from directory
                     */
                    one(sshExecutor).exists(ds1ArchivedLocationFile.getPath(), timeoutMillis());
                    will(returnValue(BooleanStatus.createTrue()));
                    one(sshExecutor).executeCommandRemotely(
                            "rm -rf " + ds1ArchivedLocationFile.getPath(), timeoutMillis());
                    will(returnValue(OK_RESULT));

                    /*
                     * ds2: directory doesn't exist in archive -> nothing to do
                     */
                    one(sshExecutor).exists(ds2ArchivedLocationFile.getPath(), timeoutMillis());
                    will(returnValue(BooleanStatus.createFalse()));
                }
            });
        Status status1 = dataSetCopier.deleteFromDestination(datasetLocation(ds1));
        Status status2 = dataSetCopier.deleteFromDestination(datasetLocation(ds2));
        assertSuccessful(status1);
        assertSuccessful(status2);

        context.assertIsSatisfied();
    }

    @Test
    public void testRemoteViaSshDeleteFromDestinationWithErrors()
    {
        Properties properties = createRemoteViaSshDestinationProperties();
        prepareRemoteCreateAndCheckCopier(HOST, null, true);
        IDataSetFileOperationsManager dataSetCopier =
                new DataSetFileOperationsManager(properties, copierFactory, sshFactory);
        context.checking(new Expectations()
            {
                {
                    /*
                     * ds1: fail to delete
                     */
                    one(sshExecutor).exists(ds1ArchivedLocationFile.getPath(), timeoutMillis());
                    will(returnValue(BooleanStatus.createTrue()));
                    one(sshExecutor).executeCommandRemotely(
                            "rm -rf " + ds1ArchivedLocationFile.getPath(), timeoutMillis());
                    will(returnValue(ERROR_RESULT));

                    /*
                     * ds2: fail to check existence
                     */
                    one(sshExecutor).exists(ds2ArchivedLocationFile.getPath(), timeoutMillis());
                    will(returnValue(BooleanStatus.createError(DUMMY_ERROR_MESSAGE)));
                }
            });
        Status status1 = dataSetCopier.deleteFromDestination(datasetLocation(ds1));
        Status status2 = dataSetCopier.deleteFromDestination(datasetLocation(ds2));
        assertError(status1, "couldn't delete");
        assertError(status2, "couldn't check existence");

        context.assertIsSatisfied();
    }

    // TODO 2011-03-14, Piotr Buczek: test rsync operations

    // @Test
    // public void testRemoteViaRsyncCopyToDestination()
    // {
    // Properties properties = createRemoteViaRsyncDestinationProperties();
    // RsyncDataSetCopier dataSetCopier =
    // new RsyncDataSetCopier(properties, copierFactory, sshFactory);
    // prepareForCheckingLastModifiedDate();
    //
    // File copiedDataSet = ds1ArchivedLocationFile();
    // File copiedData = ds1ArchivedDataFile();
    //
    // // check that data set is not yet in archive
    // assertEquals(false, copiedDataSet.exists());
    //
    // /*
    // * archive 1st time
    // */
    // Status status = dataSetCopier.copyToDestination(ds1Location, ds1);
    // assertEquals(Status.OK, status);
    // // check that data set is now in archive
    // assertDs1InArchive(copiedDataSet, copiedData);
    // // check that data set is still in store
    // assertDs1InStore();
    //
    // /*
    // * archive 2nd time (could happen on crash of DSS, but shouldn't hurt)
    // */
    // status = dataSetCopier.copyToDestination(ds1Location, ds1);
    // assertEquals(Status.OK, status);
    // // check that data set is now in archive
    // assertDs1InArchive(copiedDataSet, copiedData);
    // // check that data set is still in store
    // assertDs1InStore();
    //
    // context.assertIsSatisfied();
    // }

    private void prepareLocalCreateAndCheckCopier()
    {
        context.checking(new Expectations()
            {
                {
                    one(copierFactory).create(rsyncExec, null, DEFAULT_TIMEOUT_MILLIS, RSyncConfig.getInstance().getAdditionalCommandLineOptions());
                    will(returnValue(copier));

                    one(copier).check();
                }
            });
    }

    private void prepareRemoteCreateAndCheckCopier(final String hostOrNull,
            final String rsyncModuleOrNull, final boolean checkingResult)
    {
        context.checking(new Expectations()
            {
                {
                    one(copierFactory).create(rsyncExec, sshExec, timeoutMillis(), RSyncConfig.getInstance().getAdditionalCommandLineOptions());
                    will(returnValue(copier));

                    one(sshFactory).create(sshExec, hostOrNull);
                    will(returnValue(sshExecutor));

                    one(copier).check();
                    if (hostOrNull != null)
                    {
                        if (rsyncModuleOrNull != null)
                        {
                            one(copier).checkRsyncConnectionViaRsyncServer(hostOrNull,
                                    rsyncModuleOrNull, rsyncModuleOrNull + "-password",
                                    timeoutMillis());
                        } else
                        {
                            one(copier).checkRsyncConnectionViaSsh(hostOrNull, null,
                                    timeoutMillis());
                        }
                        will(returnValue(checkingResult));
                    }
                }
            });
    }

    private static String HOST = "localhost";

    private static String RSYNC_MODULE = "abc";

    private File deleted;

    private Properties createRemoteViaSshDestinationProperties()
    {
        final Properties properties = new Properties();
        properties.setProperty(DataSetFileOperationsManager.DESTINATION_KEY, HOST + ":"
                + destination.getPath());
        setDummyRsync(properties);
        properties.setProperty(DataSetFileOperationsManager.SSH_EXEC + "-executable",
                sshExec.getPath());
        properties.setProperty(DataSetFileOperationsManager.GFIND_EXEC + "-executable",
                gfindExec.getPath());
        properties.setProperty(DataSetFileOperationsManager.TIMEOUT_KEY,
                String.valueOf(timeoutInSeconds));
        return properties;
    }

    @SuppressWarnings("unused")
    private Properties createRemoteViaRsyncDestinationProperties()
    {
        final Properties properties = new Properties();
        properties.setProperty(DataSetFileOperationsManager.DESTINATION_KEY, HOST + ":"
                + RSYNC_MODULE + ":" + destination.getPath());
        properties
                .setProperty(DataSetFileOperationsManager.RSYNC_PASSWORD_FILE_KEY, "abc-password");
        setDummyRsync(properties);
        properties.setProperty(DataSetFileOperationsManager.SSH_EXEC + "-executable",
                sshExec.getPath());
        properties.setProperty(DataSetFileOperationsManager.GFIND_EXEC + "-executable",
                gfindExec.getPath());
        properties.setProperty(DataSetFileOperationsManager.TIMEOUT_KEY,
                String.valueOf(timeoutInSeconds));
        return properties;
    }

    /*
     * --< COMMON >---------------------------------------------------------------------------------
     */

    private void assertSuccessful(Status status)
    {
        assertEquals(Status.OK, status);
    }

    private void assertError(Status status, String expectedErrorMessage)
    {
        assertTrue(status.isError());
        assertEquals(expectedErrorMessage, status.tryGetErrorMessage());
    }

    private void assertTrue(BooleanStatus boolStatus)
    {
        assertEquals(boolStatus.tryGetMessage(), true, boolStatus.isSuccess());
        assertEquals(false, boolStatus.isError());
    }

    private void assertFalse(BooleanStatus boolStatus)
    {
        assertEquals(false, boolStatus.isSuccess());
        assertEquals(false, boolStatus.isError());
    }

    private void assertFalse(BooleanStatus boolStatus, String expectedMessage)
    {
        assertEquals(false, boolStatus.isSuccess());
        assertEquals(false, boolStatus.isError());
        assertEquals(expectedMessage, boolStatus.tryGetMessage());
    }

    private void assertError(BooleanStatus boolStatus, String expectedErrorMessage)
    {
        assertEquals(false, boolStatus.isSuccess());
        assertEquals(true, boolStatus.isError());
        assertEquals(expectedErrorMessage, boolStatus.tryGetMessage());
    }

    private void assertDs1InStore()
    {
        assertEquals(true, ds1Data1.exists());
        assertEquals(true, ds1Data2.exists());
        assertEquals(DATA1_1, FileUtilities.loadToString(ds1Data1).trim());
        assertEquals(DATA1_2, FileUtilities.loadToString(ds1Data2).trim());
    }

    private void assertDs2InStore()
    {
        assertEquals(true, ds2Data.exists());
        assertEquals(DATA2, FileUtilities.loadToString(ds2Data).trim());
    }

    private void assertDs1InArchive()
    {
        assertEquals(true, ds1ArchivedLocationFile.isDirectory());
        assertEqualsOnSecondsLevel(ds1Data1.lastModified(), ds1ArchivedDataFile1.lastModified());
        assertEqualsOnSecondsLevel(ds1Data2.lastModified(), ds1ArchivedDataFile2.lastModified());
        assertEquals(DATA1_1, FileUtilities.loadToString(ds1ArchivedDataFile1).trim());
        assertEquals(DATA1_2, FileUtilities.loadToString(ds1ArchivedDataFile2).trim());
    }

    private void assertDs2InArchive()
    {
        assertEquals(true, ds2ArchivedLocationFile.isDirectory());
        assertEqualsOnSecondsLevel(ds2Data.lastModified(), ds2ArchivedDataFile.lastModified());
        assertEquals(DATA2, FileUtilities.loadToString(ds2ArchivedDataFile).trim());
    }

    private void assertEqualsOnSecondsLevel(long expectedTime, long actualTime)
    {
        assertEquals(expectedTime / 1000, actualTime / 1000);
    }

    private void assertDs1NotInArchive()
    {
        assertFalse(ds1ArchivedLocationFile.exists());
    }

    private void assertDs2NotInArchive()
    {
        assertFalse(ds2ArchivedLocationFile.exists());
    }

    private void prepareForCheckingLastModifiedDate()
    {
        // Sleep long enough to test last modified date of target will be same as of source.
        try
        {
            Thread.sleep(2000);
        } catch (InterruptedException ex)
        {
            // ignored
        }
    }

    private DatasetLocation datasetLocation(DatasetDescription dsd)
    {
        DatasetLocation result = new DatasetLocation();
        result.setDatasetCode(dsd.getDataSetCode());
        result.setDataSetLocation(dsd.getDataSetLocation());
        return result;
    }

    private long timeoutMillis()
    {
        return timeoutInSeconds * DateUtils.MILLIS_PER_SECOND;
    }
}

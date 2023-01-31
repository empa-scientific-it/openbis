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

import static ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.DataSetCopier.DESTINATION_KEY;
import static ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.DataSetCopier.HARD_LINK_COPY_KEY;
import static ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.DataSetCopier.RENAME_TO_DATASET_CODE_KEY;
import static ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.DataSetCopier.RSYNC_PASSWORD_FILE_KEY;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.hamcrest.core.IsNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.BooleanStatus;
import ch.systemsx.cisd.common.filesystem.IImmutableCopier;
import ch.systemsx.cisd.common.filesystem.IPathCopier;
import ch.systemsx.cisd.common.filesystem.ssh.ISshCommandExecutor;
import ch.systemsx.cisd.common.mail.EMailAddress;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.ProcessingStatus;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.RSyncConfig;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = { DataSetCopier.class, AbstractDropboxProcessingPlugin.class })
public class DataSetCopierTest extends AbstractFileSystemTestCase
{
    private static final String RSYNC_EXECUTABLE = "rsync-executable";

    private static final String SHARE_ID = "42";

    private static final String USER_EMAIL = "a@bc.de";

    private static final String USER_ID = "test-user";

    private static final String DS1_LOCATION = "ds1";

    private static final String DS2_LOCATION = "ds2";

    private static final String DS3_LOCATION = "ds3";

    private static final String DS4_LOCATION = "ds4";

    private Mockery context;

    private IPathCopierFactory pathFactory;

    private ISshCommandExecutorFactory sshFactory;

    private IPathCopier copier;

    private ISshCommandExecutor sshCommandExecutor;

    private File storeRoot;

    private File sshExecutableDummy;

    private File rsyncExecutableDummy;

    private File lnExecutableDummy;

    private Properties properties;

    private DatasetDescription ds1;

    private File ds1Data;

    private DatasetDescription ds2;

    private File ds2Data;

    private DatasetDescription ds3;

    private File ds3Data;

    private DatasetDescription ds4;

    private File ds4Data;

    private DataSetProcessingContext dummyContext;

    private ITimeProvider timeProvider;

    private IMailClient mailClient;

    private IImmutableCopier hardLinkMaker;

    private IImmutableCopierFactory hardLinkMakerFactory;

    @BeforeMethod
    public void beforeMethod() throws IOException
    {
        context = new Mockery();
        timeProvider = context.mock(ITimeProvider.class);
        mailClient = context.mock(IMailClient.class);
        pathFactory = context.mock(IPathCopierFactory.class);
        sshFactory = context.mock(ISshCommandExecutorFactory.class);
        copier = context.mock(IPathCopier.class);
        hardLinkMakerFactory = context.mock(IImmutableCopierFactory.class);
        hardLinkMaker = context.mock(IImmutableCopier.class);
        sshCommandExecutor = context.mock(ISshCommandExecutor.class);
        storeRoot = new File(workingDirectory, "store");
        storeRoot.mkdirs();
        sshExecutableDummy = new File(workingDirectory, "my-ssh");
        sshExecutableDummy.createNewFile();
        rsyncExecutableDummy = new File(workingDirectory, "my-rsync");
        rsyncExecutableDummy.createNewFile();
        lnExecutableDummy = new File(workingDirectory, "my-ln");
        lnExecutableDummy.createNewFile();
        properties = new Properties();
        properties.setProperty("ssh-executable", sshExecutableDummy.getPath());
        properties.setProperty(RSYNC_EXECUTABLE, rsyncExecutableDummy.getPath());
        properties.setProperty("ln-executable", lnExecutableDummy.getPath());
        ds1 = createDataSetDescription("ds1", DS1_LOCATION, true);
        File share = new File(storeRoot, SHARE_ID);
        File ds1Folder = new File(share, DS1_LOCATION + "/original");
        ds1Folder.mkdirs();
        ds1Data = new File(ds1Folder, "data.txt");
        ds1Data.createNewFile();
        ds2 = createDataSetDescription("ds2", DS2_LOCATION, true);
        File ds2Folder = new File(share, DS2_LOCATION + "/original");
        ds2Folder.mkdirs();
        ds2Data = new File(ds2Folder, "images");
        ds2Data.mkdirs();
        ds3 = createDataSetDescription("ds3", DS3_LOCATION, false);
        File ds3Folder = new File(share, DS3_LOCATION + "/original");
        ds3Folder.mkdirs();
        ds3Data = new File(ds3Folder, "existing");
        ds3Data.createNewFile();
        ds4 = createDataSetDescription("ds4", DS4_LOCATION, false);
        File ds4Folder = new File(share, DS4_LOCATION + "/original");
        ds4Folder.mkdirs();
        ds4Data = new File(ds4Folder, "existing");
        ds4Data.mkdirs();
        dummyContext =
                new DataSetProcessingContext(null, new MockDataSetDirectoryProvider(storeRoot,
                        SHARE_ID), null, mailClient, USER_ID, USER_EMAIL);
        context.checking(new Expectations()
            {
                {
                    allowing(timeProvider).getTimeInMilliseconds();
                    will(returnValue(42L));
                }
            });
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

    @Test
    public void testMissingDestinationProperty()
    {
        try
        {
            properties.clear();
            createCopier();
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Given key '" + DESTINATION_KEY + "' not found in properties '[]'",
                    ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testMissingRsyncExecutableFile()
    {
        rsyncExecutableDummy.delete();
        try
        {
            createCopier();
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals(
                    "Path to executable 'rsync' is not a file: "
                            + rsyncExecutableDummy.getAbsolutePath(),
                    ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testMissingSshExecutableFile()
    {
        sshExecutableDummy.delete();
        try
        {
            createCopier();
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals(
                    "Path to executable 'ssh' is not a file: "
                            + sshExecutableDummy.getAbsolutePath(),
                    ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testFailingSshConnection()
    {
        properties.setProperty(DESTINATION_KEY, "host:tmp/test");
        prepareCreateAndCheckCopier("host", null, 1, false);
        DataSetCopier dataSetCopier = createCopier();

        try
        {
            dataSetCopier.process(Arrays.asList(ds1), dummyContext);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("No good rsync executable found on host 'host'", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testFailingRsyncConnection()
    {
        properties.setProperty(DESTINATION_KEY, "host:abc:tmp/test");
        properties.setProperty(RSYNC_PASSWORD_FILE_KEY, "abc-password");
        prepareCreateAndCheckCopier("host", "abc", 1, false);
        DataSetCopier dataSetCopier = createCopier();

        try
        {
            dataSetCopier.process(Arrays.asList(ds1), dummyContext);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Connection to rsync module host::abc failed", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testCopyTwoDataSetsLocally()
    {
        properties.setProperty(DESTINATION_KEY, "tmp/test");
        prepareCreateAndCheckCopier(null, null, 2, true);
        context.checking(new Expectations()
            {
                {
                    one(copier).copyToRemote(ds1Data, getCanonicalFile("tmp/test").getPath(), null,
                            null, null, null, null);
                    will(returnValue(Status.OK));
                    one(copier).copyToRemote(ds2Data, getCanonicalFile("tmp/test").getPath(), null,
                            null, null, null, null);
                    will(returnValue(Status.OK));
                }
            });
        DataSetCopier dataSetCopier = createCopier();

        ProcessingStatus processingStatus =
                dataSetCopier.process(Arrays.asList(ds1, ds2), dummyContext);
        assertNoErrors(processingStatus);
        assertSuccessful(processingStatus, ds1, ds2);

        context.assertIsSatisfied();
    }

    @Test
    public void testCopyDataSetAndSendDetailedEMails()
    {
        properties.setProperty(DESTINATION_KEY, "tmp/test");
        properties.setProperty(AbstractDropboxProcessingPlugin.SEND_DETAILED_EMAIL_KEY, "true");
        prepareCreateAndCheckCopier(null, null, 1, true);
        final RecordingMatcher<String> subjectRecorder = new RecordingMatcher<String>();
        final RecordingMatcher<String> contentRecorder = new RecordingMatcher<String>();
        final RecordingMatcher<EMailAddress[]> recipientsRecorder =
                new RecordingMatcher<EMailAddress[]>();
        context.checking(new Expectations()
            {
                {
                    one(copier).copyToRemote(ds3Data, getCanonicalFile("tmp/test").getPath(), null,
                            null, null, null, null);
                    will(returnValue(Status.OK));

                    one(mailClient).sendEmailMessage(with(subjectRecorder), with(contentRecorder),
                            with(new IsNull<EMailAddress>()), with(new IsNull<EMailAddress>()),
                            with(recipientsRecorder));
                }
            });
        DataSetCopier dataSetCopier = createCopier();

        ProcessingStatus processingStatus = dataSetCopier.process(Arrays.asList(ds3), dummyContext);
        assertNoErrors(processingStatus);
        assertSuccessful(processingStatus, ds3);
        assertEquals(USER_EMAIL, recipientsRecorder.recordedObject()[0].tryGetEmailAddress());
        assertEquals("Data set ds3 [MY_DATA] successfully processed",
                subjectRecorder.recordedObject());
        assertEquals("Successfully processed data set ds3 [MY_DATA].\n\n" + "Processing details:\n"
                + "Description: Copy to tmp/test\n" + "Experiment: /g/p/e [MY_EXPERIMENT]\n"
                + "Started: 1970-01-01 01:00:00 +0100.\n" + "Finished: 1970-01-01 01:00:00 +0100.",
                contentRecorder.recordedObject());

        context.assertIsSatisfied();
    }

    @Test
    public void testCopyDataSetWithErrorAndSendDetailedEMails()
    {
        properties.setProperty(DESTINATION_KEY, "tmp/test");
        properties.setProperty(AbstractDropboxProcessingPlugin.SEND_DETAILED_EMAIL_KEY, "true");
        prepareCreateAndCheckCopier(null, null, 1, true);
        final RecordingMatcher<String> subjectRecorder = new RecordingMatcher<String>();
        final RecordingMatcher<String> contentRecorder = new RecordingMatcher<String>();
        final RecordingMatcher<EMailAddress[]> recipientsRecorder =
                new RecordingMatcher<EMailAddress[]>();
        context.checking(new Expectations()
            {
                {
                    one(copier).copyToRemote(ds3Data, getCanonicalFile("tmp/test").getPath(), null,
                            null, null, null, null);
                    will(returnValue(Status.createError("Oophs!")));

                    one(mailClient).sendEmailMessage(with(subjectRecorder), with(contentRecorder),
                            with(new IsNull<EMailAddress>()), with(new IsNull<EMailAddress>()),
                            with(recipientsRecorder));
                }
            });
        DataSetCopier dataSetCopier = createCopier();

        ProcessingStatus processingStatus = dataSetCopier.process(Arrays.asList(ds3), dummyContext);
        Status errorStatus = Status.createError(DataSetCopier.COPYING_FAILED_MSG);
        assertError(processingStatus, errorStatus, ds3);
        assertEquals(USER_EMAIL, recipientsRecorder.recordedObject()[0].tryGetEmailAddress());
        assertEquals("Processing of data set ds3 [MY_DATA] failed",
                subjectRecorder.recordedObject());
        assertEquals("Processing of data set ds3 [MY_DATA] failed.\nReason: copying failed\n\n"
                + "Processing details:\n" + "Description: Copy to tmp/test\n"
                + "Experiment: /g/p/e [MY_EXPERIMENT]\n" + "Started: 1970-01-01 01:00:00 +0100.\n"
                + "Finished: 1970-01-01 01:00:00 +0100.", contentRecorder.recordedObject());

        context.assertIsSatisfied();
    }

    @Test
    public void testCopyDataSetsWithSampleAndSendDetailedEMails()
    {
        properties.setProperty(DESTINATION_KEY, "tmp/test");
        properties.setProperty(AbstractDropboxProcessingPlugin.SEND_DETAILED_EMAIL_KEY, "true");
        prepareCreateAndCheckCopier(null, null, 1, true);
        final RecordingMatcher<String> subjectRecorder = new RecordingMatcher<String>();
        final RecordingMatcher<String> contentRecorder = new RecordingMatcher<String>();
        final RecordingMatcher<EMailAddress[]> recipientsRecorder =
                new RecordingMatcher<EMailAddress[]>();
        context.checking(new Expectations()
            {
                {
                    one(copier).copyToRemote(ds1Data, getCanonicalFile("tmp/test").getPath(), null,
                            null, null, null, null);
                    will(returnValue(Status.OK));

                    one(mailClient).sendEmailMessage(with(subjectRecorder), with(contentRecorder),
                            with(new IsNull<EMailAddress>()), with(new IsNull<EMailAddress>()),
                            with(recipientsRecorder));
                }
            });
        DataSetCopier dataSetCopier = createCopier();

        ProcessingStatus processingStatus = dataSetCopier.process(Arrays.asList(ds1), dummyContext);
        assertNoErrors(processingStatus);
        assertSuccessful(processingStatus, ds1);
        assertEquals(USER_EMAIL, recipientsRecorder.recordedObject()[0].tryGetEmailAddress());
        assertEquals("Data set ds1 [MY_DATA] successfully processed",
                subjectRecorder.recordedObject());
        assertEquals("Successfully processed data set ds1 [MY_DATA].\n\n" + "Processing details:\n"
                + "Description: Copy to tmp/test\n" + "Experiment: /g/p/e [MY_EXPERIMENT]\n"
                + "Sample: /g/s [MY_SAMPLE]\n" + "Started: 1970-01-01 01:00:00 +0100.\n"
                + "Finished: 1970-01-01 01:00:00 +0100.", contentRecorder.recordedObject());

        context.assertIsSatisfied();
    }

    @Test
    public void testCopyDataSetWithErrorWithSampleAndSendDetailedEMails()
    {
        properties.setProperty(DESTINATION_KEY, "tmp/test");
        properties.setProperty(AbstractDropboxProcessingPlugin.SEND_DETAILED_EMAIL_KEY, "true");
        prepareCreateAndCheckCopier(null, null, 1, true);
        final RecordingMatcher<String> subjectRecorder = new RecordingMatcher<String>();
        final RecordingMatcher<String> contentRecorder = new RecordingMatcher<String>();
        final RecordingMatcher<EMailAddress[]> recipientsRecorder =
                new RecordingMatcher<EMailAddress[]>();
        context.checking(new Expectations()
            {
                {
                    one(copier).copyToRemote(ds1Data, getCanonicalFile("tmp/test").getPath(), null,
                            null, null, null, null);
                    will(returnValue(Status.createError("Oophs!")));

                    one(mailClient).sendEmailMessage(with(subjectRecorder), with(contentRecorder),
                            with(new IsNull<EMailAddress>()), with(new IsNull<EMailAddress>()),
                            with(recipientsRecorder));
                }
            });
        DataSetCopier dataSetCopier = createCopier();

        ProcessingStatus processingStatus = dataSetCopier.process(Arrays.asList(ds1), dummyContext);
        Status errorStatus = Status.createError(DataSetCopier.COPYING_FAILED_MSG);
        assertError(processingStatus, errorStatus, ds1);
        assertEquals(USER_EMAIL, recipientsRecorder.recordedObject()[0].tryGetEmailAddress());
        assertEquals("Processing of data set ds1 [MY_DATA] failed",
                subjectRecorder.recordedObject());
        assertEquals("Processing of data set ds1 [MY_DATA] failed.\nReason: copying failed\n\n"
                + "Processing details:\n" + "Description: Copy to tmp/test\n"
                + "Experiment: /g/p/e [MY_EXPERIMENT]\n" + "Sample: /g/s [MY_SAMPLE]\n"
                + "Started: 1970-01-01 01:00:00 +0100.\n" + "Finished: 1970-01-01 01:00:00 +0100.",
                contentRecorder.recordedObject());

        context.assertIsSatisfied();
    }

    @Test
    public void testCopyLocallyFailsBecauseDestinationExists()
    {
        final File destination = new File(workingDirectory, "tmp/test");
        properties.setProperty(DESTINATION_KEY, destination.getPath());
        File existingDestinationDir = new File(destination, "existing");
        existingDestinationDir.mkdirs();
        prepareCreateAndCheckCopier(null, null, 4, true);
        context.checking(new Expectations()
            {
                {
                    one(copier).copyToRemote(ds1Data, getCanonicalFile(destination).getPath(),
                            null, null, null, null, null);
                    will(returnValue(Status.createError("error message")));

                    one(copier).copyToRemote(ds2Data, getCanonicalFile(destination).getPath(),
                            null, null, null, null, null);
                    will(returnValue(Status.OK));
                }
            });
        DataSetCopier dataSetCopier = createCopier();

        ProcessingStatus processingStatus =
                dataSetCopier.process(Arrays.asList(ds1, ds2, ds3, ds4), dummyContext);

        // processing 1st data set fails but 2nd one is processed successfully
        Status errorStatus = Status.createError(DataSetCopier.COPYING_FAILED_MSG);
        assertError(processingStatus, errorStatus, ds1);
        assertSuccessful(processingStatus, ds2);
        // 3rd and 4rd are not copied because destination directories already exist
        Status alreadyExistStatus = Status.createError(DataSetCopier.ALREADY_EXIST_MSG);
        assertError(processingStatus, alreadyExistStatus, ds3, ds4);

        context.assertIsSatisfied();
    }

    @Test
    public void testCopyLocallyWithRenamingFailsBecauseDestinationExists()
    {
        final File destination = new File(workingDirectory, "tmp/test");
        properties.setProperty(DESTINATION_KEY, destination.getPath());
        properties.setProperty(RENAME_TO_DATASET_CODE_KEY, "true");
        File existingDestinationDir = new File(destination, ds1.getDataSetCode());
        existingDestinationDir.mkdirs();
        prepareCreateAndCheckCopier(null, null, 1, true);
        DataSetCopier dataSetCopier = createCopier();

        ProcessingStatus processingStatus = dataSetCopier.process(Arrays.asList(ds1), dummyContext);

        Status errorStatus = Status.createError(DataSetCopier.ALREADY_EXIST_MSG);
        assertError(processingStatus, errorStatus, ds1);

        context.assertIsSatisfied();
    }

    @Test
    public void testCopyRemotelyViaSsh()
    {
        properties.setProperty(DESTINATION_KEY, "host:tmp/test");
        prepareCreateAndCheckCopier("host", null, 1, true);
        context.checking(new Expectations()
            {
                {
                    one(sshCommandExecutor).exists("tmp/test/data.txt",
                            DataSetCopier.SSH_TIMEOUT_MILLIS);
                    will(returnValue(BooleanStatus.createFalse()));
                    one(copier).copyToRemote(ds1Data, "tmp/test", "host", null, null, null, null);
                    will(returnValue(Status.OK));
                }
            });
        DataSetCopier dataSetCopier = createCopier();

        ProcessingStatus processingStatus = dataSetCopier.process(Arrays.asList(ds1), dummyContext);
        assertNoErrors(processingStatus);
        assertSuccessful(processingStatus, ds1);

        context.assertIsSatisfied();
    }

    @Test
    public void testCopyRemotelyViaSshWithErrors()
    {
        properties.setProperty(DESTINATION_KEY, "host:tmp/test");
        prepareCreateAndCheckCopier("host", null, 4, true);
        context.checking(new Expectations()
            {
                {
                    one(sshCommandExecutor).exists("tmp/test/data.txt",
                            DataSetCopier.SSH_TIMEOUT_MILLIS);
                    will(returnValue(BooleanStatus.createFalse()));
                    one(copier).copyToRemote(ds1Data, "tmp/test", "host", null, null, null, null);
                    will(returnValue(Status.createError("error message")));

                    one(sshCommandExecutor).exists("tmp/test/images",
                            DataSetCopier.SSH_TIMEOUT_MILLIS);
                    will(returnValue(BooleanStatus.createFalse()));
                    one(copier).copyToRemote(ds2Data, "tmp/test", "host", null, null, null, null);
                    will(returnValue(Status.OK));

                    one(sshCommandExecutor).exists("tmp/test/existing",
                            DataSetCopier.SSH_TIMEOUT_MILLIS);
                    will(returnValue(BooleanStatus.createTrue()));

                    one(sshCommandExecutor).exists("tmp/test/existing",
                            DataSetCopier.SSH_TIMEOUT_MILLIS);
                    will(returnValue(BooleanStatus.createTrue()));
                }
            });
        DataSetCopier dataSetCopier = createCopier();

        ProcessingStatus processingStatus =
                dataSetCopier.process(Arrays.asList(ds1, ds2, ds3, ds4), dummyContext);

        // processing 1st data set fails but 2nd one is processed successfully
        Status copyingFailedStatus = Status.createError(DataSetCopier.COPYING_FAILED_MSG);
        assertError(processingStatus, copyingFailedStatus, ds1);
        assertSuccessful(processingStatus, ds2);
        // 3rd and 4th are not copied because destination directories already exist
        Status alreadyExistStatus = Status.createError(DataSetCopier.ALREADY_EXIST_MSG);
        assertError(processingStatus, alreadyExistStatus, ds3, ds4);

        context.assertIsSatisfied();
    }

    @Test
    public void testCopyRemotelyViaRsyncServer()
    {
        properties.setProperty(DESTINATION_KEY, "host:abc:tmp/test");
        properties.setProperty(RSYNC_PASSWORD_FILE_KEY, "abc-password");
        prepareCreateAndCheckCopier("host", "abc", 1, true);
        context.checking(new Expectations()
            {
                {
                    one(sshCommandExecutor).exists("tmp/test/data.txt",
                            DataSetCopier.SSH_TIMEOUT_MILLIS);
                    will(returnValue(BooleanStatus.createFalse()));
                    one(copier).copyToRemote(ds1Data, "tmp/test", "host", "abc", "abc-password", null, null);
                    will(returnValue(Status.OK));
                }
            });
        DataSetCopier dataSetCopier = createCopier();

        ProcessingStatus processingStatus = dataSetCopier.process(Arrays.asList(ds1), dummyContext);
        assertNoErrors(processingStatus);
        assertSuccessful(processingStatus, ds1);

        context.assertIsSatisfied();
    }

    @Test
    public void testCopyRemotelyViaRsyncServerWithErrors()
    {
        properties.setProperty(DESTINATION_KEY, "host:abc:tmp/test");
        properties.setProperty(RSYNC_PASSWORD_FILE_KEY, "abc-password");
        prepareCreateAndCheckCopier("host", "abc", 2, true);
        context.checking(new Expectations()
            {
                {
                    one(sshCommandExecutor).exists("tmp/test/data.txt",
                            DataSetCopier.SSH_TIMEOUT_MILLIS);
                    will(returnValue(BooleanStatus.createFalse()));
                    one(copier).copyToRemote(ds1Data, "tmp/test", "host", "abc", "abc-password", null, null);
                    will(returnValue(Status.createError("error message")));

                    one(sshCommandExecutor).exists("tmp/test/images",
                            DataSetCopier.SSH_TIMEOUT_MILLIS);
                    will(returnValue(BooleanStatus.createFalse()));
                    one(copier).copyToRemote(ds2Data, "tmp/test", "host", "abc", "abc-password", null, null);
                    will(returnValue(Status.OK));
                }
            });
        DataSetCopier dataSetCopier = createCopier();

        ProcessingStatus processingStatus =
                dataSetCopier.process(Arrays.asList(ds1, ds2), dummyContext);

        // processing first data set fails but second one is processed successfully
        Status errorStatus = Status.createError(DataSetCopier.COPYING_FAILED_MSG);
        assertError(processingStatus, errorStatus, ds1);
        assertSuccessful(processingStatus, ds2);

        context.assertIsSatisfied();
    }

    @Test
    public void testHardLinkCopyingNotPossibleForRemoteDestinations()
    {
        properties.setProperty(DESTINATION_KEY, "host:tmp/test");
        properties.setProperty(HARD_LINK_COPY_KEY, "true");

        try
        {
            createCopier();
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Hard link copying not possible on an unmounted destination "
                    + "on host 'host'.", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testHardLinkCopyingSucessfully()
    {
        properties.setProperty(DESTINATION_KEY, "tmp/test");
        properties.setProperty(HARD_LINK_COPY_KEY, "true");
        properties.setProperty(AbstractDropboxProcessingPlugin.SEND_DETAILED_EMAIL_KEY, "true");
        prepareCreateAndCheckCopier(null, null, 1, true);
        final RecordingMatcher<String> subjectRecorder = new RecordingMatcher<String>();
        final RecordingMatcher<String> contentRecorder = new RecordingMatcher<String>();
        final RecordingMatcher<EMailAddress[]> recipientsRecorder =
                new RecordingMatcher<EMailAddress[]>();
        context.checking(new Expectations()
            {
                {
                    one(hardLinkMakerFactory).create(rsyncExecutableDummy, lnExecutableDummy);
                    will(returnValue(hardLinkMaker));

                    one(hardLinkMaker).copyImmutably(ds1Data, getCanonicalFile("tmp/test"), null);
                    will(returnValue(Status.OK));

                    one(mailClient).sendEmailMessage(with(subjectRecorder), with(contentRecorder),
                            with(new IsNull<EMailAddress>()), with(new IsNull<EMailAddress>()),
                            with(recipientsRecorder));
                }
            });
        DataSetCopier dataSetCopier = createCopier();

        ProcessingStatus processingStatus = dataSetCopier.process(Arrays.asList(ds1), dummyContext);

        assertNoErrors(processingStatus);
        assertSuccessful(processingStatus, ds1);
        assertEquals(USER_EMAIL, recipientsRecorder.recordedObject()[0].tryGetEmailAddress());
        assertEquals("Data set ds1 [MY_DATA] successfully processed",
                subjectRecorder.recordedObject());
        assertEquals("Successfully processed data set ds1 [MY_DATA].\n\n" + "Processing details:\n"
                + "Description: Copy to tmp/test\n" + "Experiment: /g/p/e [MY_EXPERIMENT]\n"
                + "Sample: /g/s [MY_SAMPLE]\n" + "Started: 1970-01-01 01:00:00 +0100.\n"
                + "Finished: 1970-01-01 01:00:00 +0100.", contentRecorder.recordedObject());

        context.assertIsSatisfied();
    }

    @Test
    public void testHardLinkCopyingWithRenamingSucessfully()
    {
        properties.setProperty(DESTINATION_KEY, "tmp/test");
        properties.setProperty(HARD_LINK_COPY_KEY, "true");
        properties.setProperty(RENAME_TO_DATASET_CODE_KEY, "true");
        properties.setProperty(AbstractDropboxProcessingPlugin.SEND_DETAILED_EMAIL_KEY, "true");
        prepareCreateAndCheckCopier(null, null, 1, true);
        final RecordingMatcher<String> subjectRecorder = new RecordingMatcher<String>();
        final RecordingMatcher<String> contentRecorder = new RecordingMatcher<String>();
        final RecordingMatcher<EMailAddress[]> recipientsRecorder =
                new RecordingMatcher<EMailAddress[]>();
        context.checking(new Expectations()
            {
                {
                    one(hardLinkMakerFactory).create(rsyncExecutableDummy, lnExecutableDummy);
                    will(returnValue(hardLinkMaker));

                    one(hardLinkMaker).copyImmutably(ds1Data, getCanonicalFile("tmp/test"),
                            ds1.getDataSetCode());
                    will(returnValue(Status.OK));

                    one(mailClient).sendEmailMessage(with(subjectRecorder), with(contentRecorder),
                            with(new IsNull<EMailAddress>()), with(new IsNull<EMailAddress>()),
                            with(recipientsRecorder));
                }
            });
        DataSetCopier dataSetCopier = createCopier();

        ProcessingStatus processingStatus = dataSetCopier.process(Arrays.asList(ds1), dummyContext);

        assertNoErrors(processingStatus);
        assertSuccessful(processingStatus, ds1);
        assertEquals(USER_EMAIL, recipientsRecorder.recordedObject()[0].tryGetEmailAddress());
        assertEquals("Data set ds1 [MY_DATA] successfully processed",
                subjectRecorder.recordedObject());
        assertEquals("Successfully processed data set ds1 [MY_DATA].\n\n" + "Processing details:\n"
                + "Description: Copy to tmp/test\n" + "Experiment: /g/p/e [MY_EXPERIMENT]\n"
                + "Sample: /g/s [MY_SAMPLE]\n" + "Started: 1970-01-01 01:00:00 +0100.\n"
                + "Finished: 1970-01-01 01:00:00 +0100.", contentRecorder.recordedObject());

        context.assertIsSatisfied();
    }

    private void prepareCreateAndCheckCopier(final String hostOrNull,
            final String rsyncModuleOrNull, final int numberOfExpectedCreations,
            final boolean checkingResult)
    {
        context.checking(new Expectations()
            {
                {
                    exactly(numberOfExpectedCreations).of(pathFactory).create(rsyncExecutableDummy,
                            sshExecutableDummy, DataSetCopier.SSH_TIMEOUT_MILLIS, RSyncConfig.getInstance().getAdditionalCommandLineOptions());
                    will(returnValue(copier));

                    exactly(numberOfExpectedCreations).of(sshFactory).create(sshExecutableDummy,
                            hostOrNull);
                    will(returnValue(sshCommandExecutor));

                    exactly(numberOfExpectedCreations).of(copier).check();
                    if (hostOrNull != null)
                    {
                        if (rsyncModuleOrNull != null)
                        {
                            exactly(numberOfExpectedCreations).of(copier)
                                    .checkRsyncConnectionViaRsyncServer(hostOrNull,
                                            rsyncModuleOrNull, rsyncModuleOrNull + "-password",
                                            DataSetCopier.SSH_TIMEOUT_MILLIS);
                        } else
                        {
                            exactly(numberOfExpectedCreations).of(copier)
                                    .checkRsyncConnectionViaSsh(hostOrNull, null,
                                            DataSetCopier.SSH_TIMEOUT_MILLIS);
                        }
                        will(returnValue(checkingResult));
                    }
                }
            });
    }

    // asserts for checking status

    private void assertSuccessful(ProcessingStatus processingStatus, DatasetDescription... datasets)
    {
        checkStatus(processingStatus, Status.OK, datasets);
    }

    private void assertNoErrors(ProcessingStatus processingStatus)
    {
        assertEquals(0, processingStatus.getErrorStatuses().size());
    }

    private void assertError(ProcessingStatus processingStatus, Status errorStatus,
            DatasetDescription... datasets)
    {
        List<Status> errorStatuses = processingStatus.getErrorStatuses();
        assertEquals("Error statuses " + errorStatuses + " dosn't contain " + errorStatus, true,
                errorStatuses.contains(errorStatus));
        checkStatus(processingStatus, errorStatus, datasets);
    }

    private void checkStatus(ProcessingStatus processingStatus, Status status,
            DatasetDescription... expectedDatasets)
    {
        final List<String> actualDatasets = processingStatus.getDatasetsByStatus(status);
        assertEquals(expectedDatasets.length, actualDatasets.size());
        assertTrue(actualDatasets.containsAll(asCodes(Arrays.asList(expectedDatasets))));
    }

    private static List<String> asCodes(List<DatasetDescription> datasets)
    {
        List<String> codes = new ArrayList<String>();
        for (DatasetDescription dataset : datasets)
        {
            codes.add(dataset.getDataSetCode());
        }
        return codes;
    }

    private File getCanonicalFile(String fileName)
    {
        return getCanonicalFile(new File(fileName));
    }

    private File getCanonicalFile(File file)
    {
        try
        {
            return file.getCanonicalFile();
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private DataSetCopier createCopier()
    {
        return new DataSetCopier(properties, storeRoot, pathFactory, sshFactory,
                hardLinkMakerFactory, timeProvider);
    }

}

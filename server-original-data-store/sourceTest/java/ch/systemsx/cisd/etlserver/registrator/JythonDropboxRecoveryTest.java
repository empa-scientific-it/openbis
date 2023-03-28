/*
 * Copyright ETH 2012 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.etlserver.registrator;

import java.io.File;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.jmock.Expectations;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileConstants;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.etlserver.ThreadParameters;
import ch.systemsx.cisd.etlserver.registrator.recovery.DataSetStorageRecoveryInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.EntityOperationsState;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ExperimentBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationResult;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;

/**
 * @author jakubs
 */
public class JythonDropboxRecoveryTest extends AbstractJythonDataSetHandlerTest
{

    private static final String SCRIPTS_FOLDER =
            "sourceTest/java/ch/systemsx/cisd/etlserver/registrator/";

    @Override
    protected String getRegistrationScriptsFolderPath()
    {
        return SCRIPTS_FOLDER;
    }

    private static enum RegistrationCheckResult
    {
        REGISTRATION_SUCCEEDED, REGISTRATION_FAILED, CHECK_FAILED, REGISTRATION_IN_PROGRESS
    }

    private static enum RecoveryResult
    {
        RECOVERY_SUCCEEDED, RECOVERY_ROLLED_BACK, RETRY_AT_CANT_CHECK_REGISTRATION_STATUS,
        RETRY_AT_STORAGE_FAILURE, RETRY_AT_STORAGE_CONFIRMED_FAILURE, GIVE_UP
    }

    @DataProvider(name = "recoveryTestCaseProvider")
    public Object[][] recoveryTestCasesArray()
    {
        LinkedList<RecoveryTestCase> testCases = recoveryTestCases();
        LinkedList<RecoveryTestCase> testCasesWithJython27 = recoveryTestCases();
        for (RecoveryTestCase recoveryTestCase : testCasesWithJython27)
        {
            recoveryTestCase.overrideProperties.put("plugin-jython-version", "2.5");
            recoveryTestCase.title += " @ jython 2.5";
        }
        testCases.addAll(testCasesWithJython27);

        // result value
        Object[][] resultsList = new Object[testCases.size()][];
        int index = 0;
        for (RecoveryTestCase t : testCases)
        {
            resultsList[index++] = new Object[] { t };
        }

        return resultsList;
    }

    private LinkedList<RecoveryTestCase> recoveryTestCases()
    {
        LinkedList<RecoveryTestCase> testCases = new LinkedList<RecoveryTestCase>();
        RecoveryTestCase testCase;

        testCase = new RecoveryTestCase("basic recovery succeeded");
        testCases.add(testCase);

        testCase = new RecoveryTestCase("basic recovery rollback ");
        testCase.registrationCheckResult = RegistrationCheckResult.REGISTRATION_FAILED;
        testCase.recoveryResult = RecoveryResult.RECOVERY_ROLLED_BACK;
        testCases.add(testCase);

        testCase = new RecoveryTestCase("cant verify if registration succeeded");
        testCase.registrationCheckResult = RegistrationCheckResult.CHECK_FAILED;
        testCase.recoveryResult = RecoveryResult.RETRY_AT_CANT_CHECK_REGISTRATION_STATUS;
        testCases.add(testCase);

        testCase = new RecoveryTestCase("retry if storage failed");
        testCase.shouldMakeFilesystemUnavailable = true;
        testCase.recoveryResult = RecoveryResult.RETRY_AT_STORAGE_FAILURE;
        testCases.add(testCase);

        testCase = new RecoveryTestCase("retry if storage confirmation failed");
        testCase.shouldStorageConfirmationFail = true;
        testCase.recoveryResult = RecoveryResult.RETRY_AT_STORAGE_CONFIRMED_FAILURE;
        testCases.add(testCase);

        testCase = new RecoveryTestCase("check retry count incremented");
        testCase.registrationCheckResult = RegistrationCheckResult.CHECK_FAILED;
        testCase.recoveryResult = RecoveryResult.RETRY_AT_CANT_CHECK_REGISTRATION_STATUS;
        testCase.recoveryRertyCount = 3;
        testCases.add(testCase);

        testCase =
                new RecoveryTestCase("registration in progress - don't increase the try counter");
        testCase.registrationCheckResult = RegistrationCheckResult.REGISTRATION_IN_PROGRESS;
        testCase.recoveryResult = RecoveryResult.RETRY_AT_CANT_CHECK_REGISTRATION_STATUS;
        testCase.shouldIncrementTryCount = false;
        testCase.recoveryRertyCount = 3;
        testCases.add(testCase);

        testCase = new RecoveryTestCase("check can't retry immediatelly after last failure");
        testCase.recoveryLastTry = new Date();
        testCase.nextTryInTheFuture = true;
        testCases.add(testCase);

        testCase = new RecoveryTestCase("check failure after the recovery count exceeded");
        testCase.registrationCheckResult = RegistrationCheckResult.CHECK_FAILED;
        testCase.recoveryResult = RecoveryResult.GIVE_UP;
        testCase.recoveryRertyCount = 100;
        testCases.add(testCase);

        testCase = new RecoveryTestCase("basic unrecoverable");
        testCase.canRecoverFromError = false;
        testCases.add(testCase);
        return testCases;
    }

    private static class RecoveryTestCase
    {

        /**
         * short description of the test. Will be presented in the test results view
         */
        protected String title;

        /**
         * The dropbox script file that should be used for this test case
         */
        protected String dropboxScriptPath = "v2-simple-testcase.py";

        /**
         * Specifies what properties should be overriden for this test case.
         */
        protected HashMap<String, String> overrideProperties;

        /**
         * If true, than autorecovery should take place. If not, then transaction should be rolled back, and recovery artifacts removed.
         */
        protected boolean canRecoverFromError = true;

        /**
         * Desrcibed the result of the check whether the registration was successful. If REGISTRATION_SUCCEEDED - we can continue recovery IF
         * REGISTRATION_FAILED - we can rollback IF CHECK_FAILED - we don't know and we have to try again.
         */
        protected RegistrationCheckResult registrationCheckResult =
                RegistrationCheckResult.REGISTRATION_SUCCEEDED;

        protected RecoveryResult recoveryResult = RecoveryResult.RECOVERY_SUCCEEDED;

        /**
         * if set to a value > 0, before calling the recovery the retryCount will be set to this value
         */
        protected int recoveryRertyCount = 0;

        /**
         * the timestamp of a recovery last try.
         */
        // gets initialized to some date in the past in the constructor
        protected Date recoveryLastTry;

        /**
         * if set to true, then the registration should do nothing
         */
        protected boolean nextTryInTheFuture = false;

        /**
         * If true - than during recovery store filesystem will become unavailable.
         */
        protected boolean shouldMakeFilesystemUnavailable = false;

        /**
         * If true than setting storage confirmed in application server will fail.
         */
        protected boolean shouldStorageConfirmationFail = false;

        /**
         * The behaviour on error. Default is that it should increase the try count.
         */
        protected boolean shouldIncrementTryCount = true;

        private RecoveryTestCase(String title)
        {
            this.title = title;
            this.overrideProperties = new HashMap<String, String>();
            this.overrideProperties.put("TEST_V2_API", "");

            // this the ultra-awkward way of initializing the recovery last try to some date in the
            // past
            Calendar c = Calendar.getInstance();
            c.set(2010, 1, 1);
            recoveryLastTry = c.getTime();
        }

        @Override
        public String toString()
        {
            return title;
        }
    }

    /*
     * The beginning of the basic testcase region
     */

    // INFO: basic testcase
    @Test(dataProvider = "recoveryTestCaseProvider")
    public void testBasicRecovery(final RecoveryTestCase testCase)
    {
        setUpHomeDataBaseExpectations();

        createData();

        Properties properties =
                createThreadPropertiesRelativeToScriptsFolder(testCase.dropboxScriptPath, null,
                        testCase.overrideProperties);

        createHandler(properties, true, false);

        final RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails> atomicatOperationDetails =
                new RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails>();

        // create expectations
        context.checking(new BasicRecoveryTestExpectations(testCase, atomicatOperationDetails));

        handler.handle(markerFile);

        if (testCase.canRecoverFromError)
        {
            JythonHookTestTool.assertMessagesInWorkingDirectory(workingDirectory,
                    "pre_metadata_registration");

            setTheRecoveryInfo(testCase.recoveryRertyCount, testCase.recoveryLastTry);

            assertRecoveryFile(testCase.recoveryRertyCount, RecoveryInfoDateConstraint.ORIGINAL,
                    testCase.recoveryLastTry);
            assertOriginalMarkerFileExists();
            assertDirNotEmpty(precommitDirectory, "Precommit directory should not be empty");

            handler.handle(markerFile);

            if (testCase.nextTryInTheFuture)
            {
                assertRecoveryFile(testCase.recoveryRertyCount,
                        RecoveryInfoDateConstraint.ORIGINAL, testCase.recoveryLastTry);
                assertOriginalMarkerFileExists();
                assertDirNotEmpty(precommitDirectory, "Precommit directory should not be empty");
                // nothing happened
            } else
            {
                assertPostRecoveryConstraints(testCase, atomicatOperationDetails);
            }
        } else
        {
            assertNoRecoveryTriggeredConstraints();
        }
    }

    class BasicRecoveryTestExpectations extends AbstractExpectations
    {
        final RecoveryTestCase testCase;

        public BasicRecoveryTestExpectations(final RecoveryTestCase testCase,
                final RecordingMatcher<AtomicEntityOperationDetails> atomicatOperationDetails)
        {
            super(atomicatOperationDetails);
            this.testCase = testCase;

            prepareExpecatations();
        }

        private void prepareExpecatations()
        {
            initialExpectations();

            registerDataSetsAndThrow(testCase.canRecoverFromError);

            // now registration has failed with the exception. we continue depending on where

            if (testCase.canRecoverFromError && false == testCase.nextTryInTheFuture)
            {
                checkRegistrationSucceeded();

                if (testCase.recoveryResult == RecoveryResult.RECOVERY_SUCCEEDED
                        || testCase.recoveryResult == RecoveryResult.RETRY_AT_STORAGE_CONFIRMED_FAILURE)
                {
                    setStorageConfirmed(testCase.shouldStorageConfirmationFail);
                }
            } else
            {
                // rollback
            }
        }

        protected void checkRegistrationSucceeded()
        {
            one(openBisService).didEntityOperationsSucceed(with(any(TechId.class)));
            switch (testCase.registrationCheckResult)
            {
                case REGISTRATION_SUCCEEDED:
                    returnSuccessAndChangeEnvironment();
                    break;
                case REGISTRATION_FAILED:
                    will(returnValue(EntityOperationsState.NO_OPERATION));
                    break;
                case CHECK_FAILED:
                    will(throwException(new EnvironmentFailureException(
                            "Cannot check whether the registration was successful")));
                    break;
                case REGISTRATION_IN_PROGRESS:
                    will(returnValue(EntityOperationsState.IN_PROGRESS));
                    break;
            }
        }

        private void returnSuccessAndChangeEnvironment()
        {
            if (testCase.shouldMakeFilesystemUnavailable)
            {
                will(doAll(makeFileSystemUnavailableAction(),
                        returnValue(EntityOperationsState.OPERATION_SUCCEEDED)));
            } else
            {
                will(returnValue(EntityOperationsState.OPERATION_SUCCEEDED));
            }
        }

    }

    private void assertNoRecoveryTriggeredConstraints()
    {
        assertDataSetNotStoredProcess(DATA_SET_CODE);

        assertOriginalMarkerFileExists();
        assertNoRecoveryMarkerFile();

        JythonHookTestTool.assertMessagesInWorkingDirectory(workingDirectory,
                "pre_metadata_registration", "rollback_pre_registration");

        assertDirEmptyOrContainsEmptyDirs(precommitDirectory);

        assertNoRecoveryMarkerFile();
    }

    private void assertPostRecoveryConstraints(
            final RecoveryTestCase testCase,
            final RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails> atomicatOperationDetails)
    {
        JythonHookTestTool jythonHookTestTool =
                JythonHookTestTool.createFromWorkingDirectory(workingDirectory);

        if (testCase.registrationCheckResult == RegistrationCheckResult.REGISTRATION_SUCCEEDED)
        {
            jythonHookTestTool.assertLogged("post_metadata_registration");
        }

        int expectedRetryCount =
                testCase.recoveryRertyCount + (testCase.shouldIncrementTryCount ? 1 : 0);

        switch (testCase.recoveryResult)
        {
            case RECOVERY_SUCCEEDED:
                // item in store
                assertStorageProcess(atomicatOperationDetails.recordedObject(), DATA_SET_CODE,
                        "sub_data_set_1", 0);

                assertDirEmpty(precommitDirectory);

                assertNoOriginalMarkerFileExists();
                assertNoRecoveryMarkerFile();

                // the hooks after successful registration
                jythonHookTestTool.assertLogged("post_storage");
                break;
            case RECOVERY_ROLLED_BACK:
                assertDataSetNotStoredProcess(DATA_SET_CODE);

                assertDirEmptyOrContainsEmptyDirs(precommitDirectory);

                assertNoOriginalMarkerFileExists();
                assertNoRecoveryMarkerFile();

                jythonHookTestTool.assertLogged("rollback_pre_registration");
                break;
            case RETRY_AT_CANT_CHECK_REGISTRATION_STATUS:
            case RETRY_AT_STORAGE_FAILURE:
                assertDataSetNotStoredProcess(DATA_SET_CODE);

                if (false == testCase.shouldMakeFilesystemUnavailable)
                {
                    assertDirNotEmpty(precommitDirectory, "Precommit directory should not be empty");
                }
                assertRecoveryFile(expectedRetryCount, RecoveryInfoDateConstraint.AFTER_ORIGINAL,
                        testCase.recoveryLastTry);
                assertOriginalMarkerFileExists();
                break;
            case RETRY_AT_STORAGE_CONFIRMED_FAILURE:
                assertStorageProcess(atomicatOperationDetails.recordedObject(), DATA_SET_CODE,
                        "sub_data_set_1", 0);
                assertRecoveryFile(expectedRetryCount, RecoveryInfoDateConstraint.AFTER_ORIGINAL,
                        testCase.recoveryLastTry);
                assertOriginalMarkerFileExists();
                break;
            case GIVE_UP:
                assertDataSetNotStoredProcess(DATA_SET_CODE);
                assertNoOriginalMarkerFileExists();
                assertNoRecoveryMarkerFile();

                assertDirNotEmpty(precommitDirectory, "precommit should not be empty");

                break;
        }
        jythonHookTestTool.assertNoMoreMessages();
    }

    /**
     * Use this method to update the retry count in the recovery info file.
     */
    private void setTheRecoveryInfo(int count, Date lastTryDate)
    {
        File file = getCreatedRecoveryMarkerFile();

        DataSetStorageRecoveryInfo recoveryInfo =
                handler.getGlobalState().getStorageRecoveryManager()
                        .getRecoveryFileFromMarker(file);
        // as the interface allow only increment, and not setting - we implement addition using
        // increment
        while (recoveryInfo.getTryCount() < count)
        {
            recoveryInfo.increaseTryCount();
        }

        recoveryInfo.setLastTry(lastTryDate);

        recoveryInfo.writeToFile(file);
    }

    private void assertDirNotEmpty(File file, String message)
    {
        assertFalse(message, 0 == file.list().length);
    }

    private void assertDirEmpty(File file)
    {
        String contents = file.getAbsolutePath();
        assertEquals(contents, "[]", Arrays.asList(file.list()).toString());
    }

    private void assertDirEmptyOrContainsEmptyDirs(File file)
    {
        for (String s : file.list())
        {
            File subFile = new File(file, s);
            if (subFile.isFile())
            {
                fail("Directory " + file + " is not empty! It contains a file" + s);
            } else if (subFile.isDirectory())
            {
                assertDirEmptyOrContainsEmptyDirs(subFile);
            }
        }
    }

    private void assertOriginalMarkerFileExists()
    {
        assertTrue(
                "The original registration marker file should not be deleted when entering recovery mode",
                markerFile.exists());
    }

    private void assertNoOriginalMarkerFileExists()
    {
        assertFalse("The original registration marker " + markerFile + " file should be deleted",
                markerFile.exists());
    }

    private enum RecoveryInfoDateConstraint
    {
        /**
         * the last try date is the one of the original recovery file
         */
        ORIGINAL,
        /**
         * the last try date is later then the original recovery file
         */
        AFTER_ORIGINAL
    }

    /**
     * @param tryCount - the excepted stored number of tries in a recovery file
     */
    private File assertRecoveryFile(int tryCount, RecoveryInfoDateConstraint dateConstraint,
            Date originalLastTryDate)
    {
        File file = getCreatedRecoveryMarkerFile();
        assertTrue("The recovery marker file does not exist! " + file, file.exists());
        DataSetStorageRecoveryInfo recoveryInfo =
                handler.getGlobalState().getStorageRecoveryManager()
                        .getRecoveryFileFromMarker(file);
        File recoveryFile = recoveryInfo.getRecoveryStateFile();
        assertTrue("The recovery serialized file does not exist! " + recoveryFile,
                recoveryFile.exists());

        assertEquals("The try count in a recovery file is incorrect", tryCount,
                recoveryInfo.getTryCount());

        switch (dateConstraint)
        {
            case ORIGINAL:
                assertEquals(originalLastTryDate, recoveryInfo.getLastTry());
                break;
            case AFTER_ORIGINAL:
                assertTrue(
                        "" + originalLastTryDate + " should be before " + recoveryInfo.getLastTry(),
                        originalLastTryDate.before(recoveryInfo.getLastTry()));
                break;
        }

        return file;
    }

    private void assertNoRecoveryMarkerFile()
    {
        File file = getCreatedRecoveryMarkerFile();
        assertTrue("The recovery marker file should not exist! " + file, false == file.exists());
    }

    private File getCreatedRecoveryMarkerFile()
    {
        File originalIncoming =
                FileUtilities.removePrefixFromFileName(markerFile, FileConstants.IS_FINISHED_PREFIX);
        File recoveryMarkerFile =
                handler.getGlobalState().getStorageRecoveryManager()
                        .getProcessingMarkerFile(originalIncoming);
        return recoveryMarkerFile;
    }

    // INFO: test with recovery when storage failed
    @Test
    public void testRecoveryFailureAtStorage()
    {
        RecoveryTestCase testCase = new RecoveryTestCase("No name");
        setUpHomeDataBaseExpectations();

        createData();

        Properties properties =
                createThreadPropertiesRelativeToScriptsFolder(testCase.dropboxScriptPath, null,
                        testCase.overrideProperties);

        createHandler(properties, true, false);

        final RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails> atomicatOperationDetails =
                new RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails>();

        // create expectations
        context.checking(new StorageErrorExpectations(atomicatOperationDetails));

        handleAndMakeRecoverableImmediately(testCase);

        JythonHookTestTool.assertMessagesInWorkingDirectory(workingDirectory,
                "pre_metadata_registration", "post_metadata_registration");

        assertRecoveryFile(testCase.recoveryRertyCount, RecoveryInfoDateConstraint.ORIGINAL,
                testCase.recoveryLastTry);
        assertOriginalMarkerFileExists();

        makeFileSystemAvailable(workingDirectory);

        // this recovery should succeed
        handler.handle(markerFile);

        assertStorageProcess(atomicatOperationDetails.recordedObject(), DATA_SET_CODE,
                "sub_data_set_1", 0);

        assertNoOriginalMarkerFileExists();
        assertNoRecoveryMarkerFile();

        assertDirEmpty(precommitDirectory);

        //
        // // item in store
        //
        //
        JythonHookTestTool.assertMessagesInWorkingDirectory(workingDirectory, "post_storage");
    }

    class StorageErrorExpectations extends AbstractExpectations
    {
        public StorageErrorExpectations(
                final RecordingMatcher<AtomicEntityOperationDetails> atomicatOperationDetails)
        {
            super(atomicatOperationDetails);
            prepareExpecatations();
        }

        private void prepareExpecatations()
        {
            initialExpectations();
            registerDataSetsAndMakeFileSystemUnavailable();

            // the recovery should happen here

            setStorageConfirmed(false);
        }
    }

    // INFO: test with recovery from error in storage confirmed
    @Test
    public void testRecoveryFailureAtStorageConfirmed()
    {
        RecoveryTestCase testCase = new RecoveryTestCase("No name");
        setUpHomeDataBaseExpectations();

        createData();

        Properties properties =
                createThreadPropertiesRelativeToScriptsFolder(testCase.dropboxScriptPath, null,
                        testCase.overrideProperties);

        createHandler(properties, true, false);

        final RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails> atomicatOperationDetails =
                new RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails>();

        // create expectations
        context.checking(new StorageConfirmedErrorExpectations(atomicatOperationDetails));

        handler.handle(markerFile);

        JythonHookTestTool.assertMessagesInWorkingDirectory(workingDirectory,
                "pre_metadata_registration", "post_metadata_registration");

        //
        // // item already in store
        //
        assertStorageProcess(atomicatOperationDetails.recordedObject(), DATA_SET_CODE,
                "sub_data_set_1", 0);

        setTheRecoveryInfo(testCase.recoveryRertyCount, testCase.recoveryLastTry);

        assertRecoveryFile(testCase.recoveryRertyCount, RecoveryInfoDateConstraint.ORIGINAL,
                testCase.recoveryLastTry);
        assertOriginalMarkerFileExists();

        // this recovery should succeed
        handler.handle(markerFile);

        assertNoOriginalMarkerFileExists();
        assertNoRecoveryMarkerFile();

        assertDirEmpty(precommitDirectory);

        JythonHookTestTool.assertMessagesInWorkingDirectory(workingDirectory, "post_storage");
    }

    class StorageConfirmedErrorExpectations extends AbstractExpectations
    {
        public StorageConfirmedErrorExpectations(
                final RecordingMatcher<AtomicEntityOperationDetails> atomicatOperationDetails)
        {
            super(atomicatOperationDetails);
            prepareExpecatations();
        }

        private void prepareExpecatations()
        {
            initialExpectations();
            registerDataSetsAndSucceed();
            setStorageConfirmed(true);

            // the recovery should happen here

            setStorageConfirmed(false);
        }
    }

    // INFO: the test that checks the retry mechanism

    @DataProvider(name = "retryDP")
    public Object[][] retryCounters()
    {
        return new Object[][]
        {
                { 1 },
                { 5 },
                { 15 } };
    }

    /**
     * This test tests that when the perform entity operation fails with the recoverable error, it will repeat the registration N times, and then
     * fail.
     */
    @Test(dataProvider = "retryDP")
    public void testRetryRegistrationNTimesAndFail(Integer retryCount)
    {

        RecoveryTestCase testCase = new RecoveryTestCase("No name");
        setUpHomeDataBaseExpectations();

        createData();

        Properties properties =
                createThreadPropertiesRelativeToScriptsFolder(testCase.dropboxScriptPath, null,
                        testCase.overrideProperties);

        properties.setProperty(ThreadParameters.DATASET_REGISTRATION_MAX_RETRY_COUNT,
                retryCount.toString());
        properties.setProperty(ThreadParameters.DATASET_REGISTRATION_RETRY_PAUSE_IN_SEC, "0"); // 1s

        createHandler(properties, true, false);

        final RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails> atomicatOperationDetails =
                new RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails>();

        // create expectations
        context.checking(new RetryRegistrationFail(atomicatOperationDetails, retryCount));

        handler.handle(markerFile);

        setTheRecoveryInfo(testCase.recoveryRertyCount, testCase.recoveryLastTry);

        JythonHookTestTool.assertMessagesInWorkingDirectory(workingDirectory,
                "pre_metadata_registration");

        assertOriginalMarkerFileExists();

        handler.handle(markerFile);

        // the rollback has happened
        JythonHookTestTool.assertMessagesInWorkingDirectory(workingDirectory,
                "rollback_pre_registration");
    }

    class RetryRegistrationFail extends AbstractExpectations
    {
        private final int retryCount;

        public RetryRegistrationFail(
                final RecordingMatcher<AtomicEntityOperationDetails> atomicatOperationDetails,
                int retryCount)
        {
            super(atomicatOperationDetails);
            this.retryCount = retryCount;
            prepareExpecatations();
        }

        private void prepareExpecatations()
        {
            initialExpectations();
            registerDataSetsAndThrow(true, true, EntityOperationsState.NO_OPERATION);
            for (int i = 0; i < retryCount; i++)
            {
                registerDataSetsAndThrow(true, false, EntityOperationsState.NO_OPERATION);
            }

            // the recovery - will find that nothing has happened
            checkEntityOperationsSucceeded(EntityOperationsState.NO_OPERATION);
        }
    }

    enum RetrySuccessMethod
    {
        /**
         * perform entity operations executed successfully
         */
        OPERATIONS_SUCCEDED,
        /**
         * the later check if the operations succeeded has succeeded
         */
        CHECK_SUCCEEDED
    }

    @DataProvider(name = "retrySuccessDP")
    public Object[][] retrySuccessDP()
    {
        return new Object[][]
        {
                { 1, RetrySuccessMethod.OPERATIONS_SUCCEDED },
                { 1, RetrySuccessMethod.CHECK_SUCCEEDED },
                { 5, RetrySuccessMethod.OPERATIONS_SUCCEDED },
                { 15, RetrySuccessMethod.CHECK_SUCCEEDED } };
    }

    @Test(dataProvider = "retrySuccessDP")
    public void testRetryRegistrationSucceeded(Integer retryCount, RetrySuccessMethod rsm)
    {

        RecoveryTestCase testCase = new RecoveryTestCase("No name");
        setUpHomeDataBaseExpectations();

        createData();

        Properties properties =
                createThreadPropertiesRelativeToScriptsFolder(testCase.dropboxScriptPath, null,
                        testCase.overrideProperties);

        properties.setProperty(ThreadParameters.DATASET_REGISTRATION_MAX_RETRY_COUNT,
                retryCount.toString());
        properties.setProperty(ThreadParameters.DATASET_REGISTRATION_RETRY_PAUSE_IN_SEC, "0"); // 1s

        createHandler(properties, true, false);

        final RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails> atomicatOperationDetails =
                new RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails>();

        // create expectations
        context.checking(new RetryRegistrationSucceeded(atomicatOperationDetails, retryCount, rsm));

        handler.handle(markerFile);

        // the rollback has happened
        JythonHookTestTool.assertMessagesInWorkingDirectory(workingDirectory,
                "pre_metadata_registration", "post_metadata_registration", "post_storage");

        assertStorageProcess(atomicatOperationDetails.getRecordedObjects().get(0), DATA_SET_CODE,
                "sub_data_set_1", 0);

        assertNoOriginalMarkerFileExists();
        assertNoRecoveryMarkerFile();

        assertDirEmpty(precommitDirectory);
    }

    class RetryRegistrationSucceeded extends AbstractExpectations
    {
        private final int retryCount;

        private final RetrySuccessMethod retrySuccessMethod;

        public RetryRegistrationSucceeded(
                final RecordingMatcher<AtomicEntityOperationDetails> atomicatOperationDetails,
                int retryCount, RetrySuccessMethod retrySuccessMethod)
        {
            super(atomicatOperationDetails);
            this.retryCount = retryCount;
            this.retrySuccessMethod = retrySuccessMethod;
            prepareExpecatations();
        }

        private void prepareExpecatations()
        {
            initialExpectations();
            registerDataSetsAndThrow(true, true, EntityOperationsState.NO_OPERATION);
            for (int i = 0; i < retryCount - 1; i++)
            {
                registerDataSetsAndThrow(true, false, EntityOperationsState.NO_OPERATION);
            }

            if (retrySuccessMethod == RetrySuccessMethod.OPERATIONS_SUCCEDED)
            {
                performEntityOperations();
            } else if (retrySuccessMethod == RetrySuccessMethod.CHECK_SUCCEEDED)
            {
                registerDataSetsAndThrow(true, false, EntityOperationsState.IN_PROGRESS);

                for (int i = 0; i < 40; i++)
                {
                    checkEntityOperationsSucceeded(EntityOperationsState.IN_PROGRESS);
                }
                checkEntityOperationsSucceeded(EntityOperationsState.OPERATION_SUCCEEDED);
            }

            setStorageConfirmed(false);
        }
    }

    // INFO: testcase that verifies the repeating of the jython process works.
    @Test
    public void testRetryProcessing()
    {
        RecoveryTestCase testCase = new RecoveryTestCase("No name");
        setUpHomeDataBaseExpectations();

        createData();

        Properties properties =
                createThreadPropertiesRelativeToScriptsFolder("v2-retry-process.py", null,
                        testCase.overrideProperties);

        properties.setProperty(ThreadParameters.PROCESS_MAX_RETRY_COUNT, "100"); // we dont want it
                                                                                 // to fail at all
                                                                                 // in this testcase
        properties.setProperty(ThreadParameters.PROCESS_RETRY_PAUSE_IN_SEC, "0"); // continue
                                                                                  // immediately to
                                                                                  // not stop the
                                                                                  // tests

        createHandler(properties, true, false);

        final RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails> atomicatOperationDetails =
                new RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails>();

        // create expectations
        context.checking(new RetryProcessExpectations(atomicatOperationDetails, 20));

        handler.handle(markerFile);

        JythonHookTestTool.assertMessagesInWorkingDirectory(workingDirectory,
                "pre_metadata_registration", "post_metadata_registration", "post_storage");

        assertStorageProcess(atomicatOperationDetails.getRecordedObjects().get(0), DATA_SET_CODE,
                "sub_data_set_1", 0);

        assertNoOriginalMarkerFileExists();
        assertNoRecoveryMarkerFile();

        assertDirEmpty(precommitDirectory);
    }

    class RetryProcessExpectations extends AbstractExpectations
    {
        public RetryProcessExpectations(
                final RecordingMatcher<AtomicEntityOperationDetails> atomicatOperationDetails,
                int retryCount)
        {
            super(atomicatOperationDetails);
            prepareExpectations(retryCount);
        }

        private void prepareExpectations(int retryCount)
        {
            // create dataset
            for (int i = 0; i < retryCount; i++)
            {
                createPermId(DATA_SET_CODE + i);
            }

            initialExpectations();

            registerDataSetsAndSucceed();

            setStorageConfirmed(false);
        }
    }

    // INFO: testcase that verifies the repeating of the jython process works.
    @Test
    public void testRetryProcessingFailed()
    {
        RecoveryTestCase testCase = new RecoveryTestCase("No name");
        setUpHomeDataBaseExpectations();

        createData();

        Properties properties =
                createThreadPropertiesRelativeToScriptsFolder("v2-retry-process.py", null,
                        testCase.overrideProperties);

        properties.setProperty(ThreadParameters.PROCESS_MAX_RETRY_COUNT, "10"); // we dont want it
                                                                                // to fail at all in
                                                                                // this testcase
        properties.setProperty(ThreadParameters.PROCESS_RETRY_PAUSE_IN_SEC, "0"); // continue
                                                                                  // immediately to
                                                                                  // not stop the
                                                                                  // tests

        createHandler(properties, true, false);

        final RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails> atomicatOperationDetails =
                new RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails>();

        // create expectations
        context.checking(new RetryProcessFailedExpectations(atomicatOperationDetails, 10));

        handler.handle(markerFile);

        assertDataSetNotStoredProcess(DATA_SET_CODE);

        assertOriginalMarkerFileExists();
        assertNoRecoveryMarkerFile();

        assertDirEmpty(precommitDirectory);
    }

    class RetryProcessFailedExpectations extends AbstractExpectations
    {
        public RetryProcessFailedExpectations(
                final RecordingMatcher<AtomicEntityOperationDetails> atomicatOperationDetails,
                int retryCount)
        {
            super(atomicatOperationDetails);
            prepareExpectations(retryCount);
        }

        private void prepareExpectations(int retryCount)
        {
            ignoring(openBisService).heartbeat();
            // get experiment
            atLeast(1).of(openBisService).tryGetExperiment(
                    new ExperimentIdentifierFactory(experiment.getIdentifier()).createIdentifier());
            will(returnValue(experiment));

            // create dataset
            for (int i = 0; i <= retryCount; i++)
            {
                createPermId(DATA_SET_CODE + i);
            }
        }
    }

    // INFO: the test that checks all possible recovery points one by one in single registration
    @DataProvider(name = "multipleCheckpointsDataProvider")
    public Object[][] multipleCheckpointsData()
    {
        return new Object[][]
        {
                { "v2-simple-testcase.py", false },
                { "v2-container-testcase.py", true } };
    }

    /**
     * This tests the registration with adventure, where the failure and recovery happens at every possible step.
     */
    @Test(dataProvider = "multipleCheckpointsDataProvider")
    public void testRecoveryAtMultipleCheckpoints(String script, boolean includeContainer)
    {
        RecoveryTestCase testCase = new RecoveryTestCase("No name");
        setUpHomeDataBaseExpectations();

        createData();

        Properties properties =
                createThreadPropertiesRelativeToScriptsFolder(script, null,
                        testCase.overrideProperties);

        createHandler(properties, true, false);

        final RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails> atomicatOperationDetails =
                new RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails>();

        // create expectations
        context.checking(new MultipleErrorsExpectations(atomicatOperationDetails, includeContainer));

        /*
         * First run - register datasets, and handle exception from AS
         */

        handleAndMakeRecoverableImmediately(testCase);

        // now we have registered, but error was thrown from registration
        JythonHookTestTool.assertMessagesInWorkingDirectory(workingDirectory,
                "pre_metadata_registration");

        /*
         * Second run - check that the datasets has been registered, and run the post_registration hook The file system is made unavailable, so the
         * storage will fail. Restore the filesystem after this run.
         */

        handleAndMakeRecoverableImmediately(testCase);

        // now we know we have registered, post registration hook executed, but storage failed.
        JythonHookTestTool.assertMessagesInWorkingDirectory(workingDirectory,
                "post_metadata_registration");

        // so make filesystem avaiable this time
        makeFileSystemAvailable(workingDirectory);

        /*
         * Third run - Start after the post registration hook, and run the storage - this will succeed now. Throw exception from storage confirmation.
         */

        handleAndMakeRecoverableImmediately(testCase);
        JythonHookTestTool.assertMessagesInWorkingDirectory(workingDirectory); // assert no messages
        /*
         * Last run. now only do the storage confirm part. After this is done, the registration should be complete.
         */

        handler.handle(markerFile);
        // setTheRecoveryInfo(testCase.recoveryRertyCount, testCase.recoveryLastTry);
        // now the storage confirmation has succeeded
        assertStorageProcess(atomicatOperationDetails.recordedObject(), DATA_SET_CODE,
                "sub_data_set_1", 0, includeContainer ? 2 : 1);

        assertNoOriginalMarkerFileExists();
        assertNoRecoveryMarkerFile();

        assertDirEmpty(precommitDirectory);

        JythonHookTestTool.assertMessagesInWorkingDirectory(workingDirectory, "post_storage");

    }

    private void handleAndMakeRecoverableImmediately(RecoveryTestCase testCase)
    {
        handler.handle(markerFile);
        setTheRecoveryInfo(testCase.recoveryRertyCount, testCase.recoveryLastTry);
    }

    class MultipleErrorsExpectations extends AbstractExpectations
    {
        public MultipleErrorsExpectations(
                final RecordingMatcher<AtomicEntityOperationDetails> atomicatOperationDetails,
                boolean withContainer)
        {
            super(atomicatOperationDetails);
            prepareExpectations(withContainer);
        }

        private void prepareExpectations(boolean withContainer)
        {
            initialExpectations();
            if (withContainer)
            {
                initialContainerExpectations();
            }
            // first try - fail at registration and realize that the operation has not succeeded
            registerDataSetsAndThrow(true);

            // second handle - fail at storage
            one(openBisService).didEntityOperationsSucceed(with(any(TechId.class)));
            will(doAll(makeFileSystemUnavailableAction(),
                    returnValue(EntityOperationsState.OPERATION_SUCCEEDED)));

            if (withContainer)
            {
                // third try - fail at storage confirmation
                setStorageConfirmed(true, CONTAINER_DATA_SET_CODE);

                // fourth try - success
                setStorageConfirmed(false, CONTAINER_DATA_SET_CODE);
            } else
            {
                // third try - fail at storage confirmation
                setStorageConfirmed(true);

                // fourth try - success
                setStorageConfirmed(false);

            }
        }
    }

    abstract class AbstractExpectations extends Expectations
    {
        final Experiment experiment;

        final RecordingMatcher<AtomicEntityOperationDetails> atomicatOperationDetails;

        public AbstractExpectations(
                final RecordingMatcher<AtomicEntityOperationDetails> atomicatOperationDetails)
        {
            ExperimentBuilder builder = new ExperimentBuilder().identifier(EXPERIMENT_IDENTIFIER);
            this.experiment = builder.getExperiment();
            this.atomicatOperationDetails = atomicatOperationDetails;

        }

        protected void initialExpectations()
        {
            ignoring(openBisService).heartbeat();

            // create dataset
            createPermId(DATA_SET_CODE);

            // get experiment
            atLeast(1).of(openBisService).tryGetExperiment(
                    new ExperimentIdentifierFactory(experiment.getIdentifier()).createIdentifier());
            will(returnValue(experiment));

            // validate dataset
            one(dataSetValidator).assertValidDataSet(DATA_SET_TYPE,
                    new File(new File(stagingDirectory, DATA_SET_CODE), "sub_data_set_1"));
        }

        protected void initialContainerExpectations()
        {

            // create dataset
            createPermId(CONTAINER_DATA_SET_CODE);

            // validate dataset
            one(dataSetValidator).assertValidDataSet(CONTAINER_DATA_SET_TYPE, null);
        }

        protected CustomAction makeFileSystemUnavailableAction()
        {
            return new CustomAction("makeSystemUnavailable")
                {
                    @Override
                    public Object invoke(Invocation invocation) throws Throwable
                    {
                        makeFileSystemUnavailable(workingDirectory);
                        return null;
                    }
                };
        }

        protected CustomAction makeFileSystemAvailableAction()
        {
            return new CustomAction("makeSystemAvailable")
                {
                    @Override
                    public Object invoke(Invocation invocation) throws Throwable
                    {
                        makeFileSystemAvailable(workingDirectory);
                        return null;
                    }
                };
        }

        /**
         * This method should make sure that the registration will fail, and it will go into the recovery mode. It means that it also has to assure
         * that the subsequent retries introduce in SP-107 are failing.
         */
        protected void registerDataSetsAndThrow(boolean canRecoverFromError)
        {
            registerDataSetsAndThrow(canRecoverFromError, true, EntityOperationsState.NO_OPERATION);
        }

        protected void registerDataSetsAndThrow(boolean canRecoverFromError, boolean drawId,
                EntityOperationsState entityOperationsState)
        {
            if (drawId)
            {
                drawUniqueId();
            }
            one(openBisService).performEntityOperations(with(atomicatOperationDetails));

            Throwable e;
            if (canRecoverFromError)
            {
                e =
                        new Throwable(
                                "Potentially recoverable failure in registration");
            } else
            {
                e = new UserFailureException("Unrecoverable failure in registration");
            }

            will(throwException(e));

            if (canRecoverFromError)
            {
                // the check immediately after the exception fails as well
                checkEntityOperationsSucceeded(entityOperationsState);
            }
        }

        /**
         * call openBisService.didEntityOperationsSucceeded
         */
        protected void checkEntityOperationsSucceeded(EntityOperationsState entityOperationsState)
        {
            one(openBisService).didEntityOperationsSucceed(with(any(TechId.class)));
            will(returnValue(entityOperationsState));
        }

        /**
         * Draw unique ID and perform entity operations
         */
        protected void registerDataSetsAndSucceed()
        {
            drawUniqueId();
            performEntityOperations();
        }

        public void performEntityOperations()
        {
            one(openBisService).performEntityOperations(with(atomicatOperationDetails));
            will(returnValue(new AtomicEntityOperationResult()));
        }

        protected void drawUniqueId()
        {
            one(openBisService).drawANewUniqueID();
            will(returnValue(Long.valueOf(1)));
        }

        protected void registerDataSetsAndMakeFileSystemUnavailable()
        {
            drawUniqueId();
            one(openBisService).performEntityOperations(with(atomicatOperationDetails));
            will(doAll(makeFileSystemUnavailableAction(),
                    returnValue(new AtomicEntityOperationResult())));
        }

        /**
         * @param shouldFail - if true the call to as should throw an exception
         */
        protected void setStorageConfirmed(boolean shouldFail, String... additionalCodes)
        {
            List<String> codes = new LinkedList<String>();
            codes.add(DATA_SET_CODE);

            for (String code : additionalCodes)
            {
                codes.add(code);
            }
            setStorageConfirmed(codes, shouldFail);
        }

        /**
         * @param dataSetCodes - the datasets to be confirmed
         * @param shouldFail - if true the call to as should throw an exception
         */
        protected void setStorageConfirmed(List<String> dataSetCodes, boolean shouldFail)
        {
            one(openBisService).setStorageConfirmed(dataSetCodes);
            if (shouldFail)
            {
                will(throwException(new EnvironmentFailureException(
                        "Setting storage confirmation fail.")));
            }
        }

        protected void createPermId(String dataSetCode)
        {
            one(openBisService).createPermIds(with(any(Integer.class)));
            will(returnValue(Collections.singletonList(dataSetCode)));
        }
    }

    /**
     * creates a very simple dataset to import
     */
    private void createData()
    {
        incomingDataSetFile = createDirectory(workingDirectory, "data_set");

        assertTrue(incomingDataSetFile.isDirectory());

        subDataSet1 = createDirectory(incomingDataSetFile, "sub_data_set_1");

        FileUtilities.writeToFile(new File(subDataSet1, "read1.me"), "hello world1");

        markerFile = new File(workingDirectory, FileConstants.IS_FINISHED_PREFIX + "data_set");
        FileUtilities.writeToFile(markerFile, "");
    }
}

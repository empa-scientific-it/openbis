/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.jmock.Expectations;
import org.jmock.api.Invocation;
import org.jmock.internal.ExpectationBuilder;
import org.jmock.lib.action.CustomAction;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.NotImplementedException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileConstants;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.TestBigStructureCreator;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.common.test.IPredicate;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional.UnstoreDataAction;
import ch.systemsx.cisd.etlserver.ThreadParameters;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.v1.JythonTopLevelDataSetHandler;
import ch.systemsx.cisd.etlserver.registrator.v2.ConfiguredOnErrorActionDecision;
import ch.systemsx.cisd.etlserver.registrator.v2.DataSetStorageAlgorithmRunner;
import ch.systemsx.cisd.openbis.common.eodsql.MockDataSet;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DatasetLocationUtil;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystem;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ExperimentBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.PropertyBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.SampleBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationResult;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class JythonTopLevelDataSetRegistratorTest extends AbstractJythonDataSetHandlerTest
{
    private static final Long SEARCH_RETURNED_SAMPLE_DB_ID = Long.valueOf(100);

    private static final String SCRIPTS_FOLDER =
            "sourceTest/java/ch/systemsx/cisd/etlserver/registrator/";

    @BeforeMethod
    @Override
    public void setUp() throws IOException
    {
        super.setUp();
        context.checking(new Expectations()
            {
                {
                    ignoring(openBisService).heartbeat();
                }
            });
    }

    public ArrayList<TestCaseParameters> multipleVersionsOfTestCase(TestCaseParameters params)
    {
        ArrayList<TestCaseParameters> list = new ArrayList<TestCaseParameters>(2);
        list.add(params);
        list.add(versionV2(params));
        list.add(versionJavaV2(params));
        return list;
    }

    public TestCaseParameters versionV2(final TestCaseParameters other)
    {
        TestCaseParameters params = other.clone();
        params.isV2 = true;
        params.overrideProperties = new HashMap<String, String>(params.overrideProperties);
        params.overrideProperties.put("TEST_V2_API", "");
        params.dontCallOldApiJythonHooks = true;
        params.title += " - V2";
        params.shouldUseAutoRecovery = true;
        params.dropboxScriptPath = scriptPathV2(other.dropboxScriptPath);
        return params;
    }

    public TestCaseParameters versionJavaV2(final TestCaseParameters other)
    {
        TestCaseParameters params = other.clone();
        params.isV2 = true;
        params.overrideProperties = new HashMap<String, String>(params.overrideProperties);
        params.overrideProperties.put("TEST_JAVA_V2_API", "");
        params.dontCallOldApiJythonHooks = true;
        params.title += " - JavaV2";
        params.shouldUseAutoRecovery = true;
        params.javaProgramClass = programJavaV2(other.dropboxScriptPath);
        return params;
    }

    private static String scriptPathV2(String scriptPath)
    {
        File script = new File(scriptPath);
        return new File(script.getParentFile(), "v2-" + script.getName()).getPath();
    }

    private static String programJavaV2(String scriptPath)
    {
        if (scriptPath.endsWith("simple-testcase.py"))
        {
            return JavaV2SimpleTestcase.class.getCanonicalName();
        } else if (scriptPath.endsWith("testcase-without-post-storage.py"))
        {
            return JavaV2TestcaseWithoutPostStorage.class.getCanonicalName();
        } else if (scriptPath.endsWith("file-not-found.py"))
        {
            return JavaV2FileNotFound.class.getCanonicalName();
        } else if (scriptPath.endsWith("testcase-registration-context.py"))
        {
            return JavaV2TestcaseRegistrationContext.class.getCanonicalName();
        } else if (scriptPath.endsWith("testcase-preregistration-hook-failed.py"))
        {
            return JavaV2TestcasePreregistrationHookFailed.class.getCanonicalName();
        } else if (scriptPath.endsWith("testcase-postregistration-hook-failed.py"))
        {
            return JavaV2TestcasePostregistrationHookFailed.class.getCanonicalName();
        } else if (scriptPath.endsWith("dying-script.py"))
        {
            return JavaV2DyingProgram.class.getCanonicalName();
        } else if (scriptPath.endsWith("testcase-rollback.py"))
        {
            return JavaV2TestcaseRollback.class.getCanonicalName();
        }

        return null;
    }

    private static <T> Object[][] asObjectArray(List<T> testCases)
    {
        // here is crappy code for
        // return parameters.map( (x) => new Object[]{x} )
        Object[][] resultsList = new Object[testCases.size()][];

        int index = 0;
        for (T t : testCases)
        {
            resultsList[index++] = new Object[] { t };
        }

        return resultsList;
    }

    @DataProvider(name = "simpleTransactionTestCaseProvider")
    public Object[][] simpleTransactionCases()
    {
        List<TestCaseParameters> testCases = simpleTransactionCasesList();
        return asObjectArray(testCases);
    }

    public List<TestCaseParameters> simpleTransactionCasesList()
    {
        // creates data with more than only one dataset
        IDelegatedAction createTwoDataSetsDelegate = new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createData();
                }
            };

        LinkedList<TestCaseParameters> testCases =
                new LinkedList<JythonTopLevelDataSetRegistratorTest.TestCaseParameters>();

        // // basic testCase
        // testCases.addAll(multipleVersionsOfTestCase(new TestCaseParameters(
        // "Basic successful registration")));

        // testCase without prestaging
        TestCaseParameters testCase =
                new TestCaseParameters(
                        "registration without prestaging. Should clean the incoming directory.");
        testCase.overrideProperties.put(ThreadParameters.DATASET_REGISTRATION_PRE_STAGING_BEHAVIOR,
                DataSetRegistrationPreStagingBehavior.USE_ORIGINAL.toString().toLowerCase());

        testCase.incomingDataSetAfterRegistration = "empty";
        testCases.addAll(multipleVersionsOfTestCase(testCase));

        // without pre-staging with some data left in incoming directory
        // this test case is for a particular users, who use incoming directory in the way they
        // aren't supposed to
        testCase =
                new TestCaseParameters(
                        "registration without prestaging. Should leave some data in the incoming directory.");
        testCase.overrideProperties.put(ThreadParameters.DATASET_REGISTRATION_PRE_STAGING_BEHAVIOR,
                DataSetRegistrationPreStagingBehavior.USE_ORIGINAL.toString().toLowerCase());
        testCase.incomingDataSetAfterRegistration = "content";
        testCase.createDataSetDelegate = createTwoDataSetsDelegate;
        testCases.addAll(multipleVersionsOfTestCase(testCase));

        // simple test failing registration testCase
        testCase = new TestCaseParameters("The simple transaction rollback.");
        testCase.incomingDataSetAfterRegistration = "untouched_two_datasets";
        testCase.createDataSetDelegate = createTwoDataSetsDelegate;
        testCase.failurePoint = TestCaseParameters.FailurePoint.DURING_OPENBIS_REGISTRATION;
        testCases.addAll(multipleVersionsOfTestCase(testCase));

        String[] allErrors =
        { ConfiguredOnErrorActionDecision.INVALID_DATA_SET_KEY,
                ConfiguredOnErrorActionDecision.OPENBIS_REGISTRATION_FAILURE_KEY,
                ConfiguredOnErrorActionDecision.POST_REGISTRATION_ERROR_KEY,
                ConfiguredOnErrorActionDecision.REGISTRATION_SCRIPT_ERROR_KEY,
                ConfiguredOnErrorActionDecision.STORAGE_PROCESSOR_ERROR_KEY,
                ConfiguredOnErrorActionDecision.PREPARATION_ERROR_KEY,
                ConfiguredOnErrorActionDecision.VALIDATION_SCRIPT_ERROR_KEY, };

        // simple test failing registration testCase
        testCase = new TestCaseParameters("The simple transaction rollback with DELETE on error.");
        for (String error : allErrors)
        {
            testCase.overrideProperties.put(ThreadParameters.ON_ERROR_DECISION_KEY + "." + error,
                    UnstoreDataAction.DELETE.toString());
        }
        testCase.incomingDataSetAfterRegistration = "deleted";
        testCase.createDataSetDelegate = createTwoDataSetsDelegate;
        testCase.failurePoint = TestCaseParameters.FailurePoint.DURING_OPENBIS_REGISTRATION;
        testCases.addAll(multipleVersionsOfTestCase(testCase));

        // simple test failing registration testCase
        testCase =
                new TestCaseParameters(
                        "The simple transaction rollback with DELETE on error without prestaging.");
        testCase.overrideProperties.put(ThreadParameters.DATASET_REGISTRATION_PRE_STAGING_BEHAVIOR,
                DataSetRegistrationPreStagingBehavior.USE_ORIGINAL.toString().toLowerCase());

        for (String error : allErrors)
        {
            testCase.overrideProperties.put(ThreadParameters.ON_ERROR_DECISION_KEY + "." + error,
                    UnstoreDataAction.DELETE.toString());
        }

        testCase.incomingDataSetAfterRegistration = "deleted";
        testCase.createDataSetDelegate = createTwoDataSetsDelegate;
        testCase.failurePoint = TestCaseParameters.FailurePoint.DURING_OPENBIS_REGISTRATION;
        testCases.addAll(multipleVersionsOfTestCase(testCase));

        testCase = new TestCaseParameters("The validation error with DELETE on error.");
        for (String error : allErrors)
        {
            testCase.overrideProperties.put(ThreadParameters.ON_ERROR_DECISION_KEY + "." + error,
                    UnstoreDataAction.DELETE.toString());
        }
        testCase.incomingDataSetAfterRegistration = "deleted";
        testCase.failurePoint = TestCaseParameters.FailurePoint.DURING_VALIDATION;
        testCases.addAll(multipleVersionsOfTestCase(testCase));

        testCase =

                new TestCaseParameters("The simple validation without post_storage function defined.");
        testCase.dropboxScriptPath = "testcase-without-post-storage.py";
        testCase.postStorageFunctionNotDefinedInADropbox = true;
        testCases.addAll(multipleVersionsOfTestCase(testCase));

        testCase = new TestCaseParameters("Dataset file not found.");
        testCase.dropboxScriptPath = "file-not-found.py";
        testCase.shouldThrowExceptionDuringRegistration = true;
        testCase.failurePoint = TestCaseParameters.FailurePoint.AFTER_CREATE_DATA_SET_CODE;
        testCase.exceptionAcceptor = new IPredicate<Exception>()
            {
                @Override
                public boolean execute(Exception arg)
                {
                    Throwable exc = arg;
                    if (arg instanceof org.python27.core.PyException)
                    {
                        exc = ((org.python27.core.PyException) arg).getCause();
                    } else if (arg instanceof org.python.core.PyException)
                    {
                        exc = ((org.python.core.PyException) arg).getCause();
                    }
                    if (exc instanceof IOExceptionUnchecked)
                    {
                        IOExceptionUnchecked tunnel = (IOExceptionUnchecked) exc;
                        FileNotFoundException ex = (FileNotFoundException) tunnel.getCause();
                        return ex.getMessage().startsWith("Neither '/non/existent/path' nor '");
                    }
                    return false;
                }
            };
        testCases.addAll(multipleVersionsOfTestCase(testCase));

        testCase = new TestCaseParameters("Test for registration context in hook methods.");
        testCase.dropboxScriptPath = "testcase-registration-context.py";
        testCases.addAll(multipleVersionsOfTestCase(testCase));

        testCase =
                new TestCaseParameters(
                        "Test for preregistration hook preventing registration in application server.");
        testCase.dropboxScriptPath = "testcase-preregistration-hook-failed.py";
        testCase.shouldThrowExceptionDuringRegistration = true;
        testCase.failurePoint = TestCaseParameters.FailurePoint.BEFORE_OPENBIS_REGISTRATION;
        testCases.addAll(multipleVersionsOfTestCase(testCase));

        testCase =
                new TestCaseParameters(
                        "Postregistration hook error should not prevent successful registration.");
        testCase.dropboxScriptPath = "testcase-postregistration-hook-failed.py";
        testCases.addAll(multipleVersionsOfTestCase(testCase));

        testCase = new TestCaseParameters("Postregistration hook has wrong signature.");
        testCase.dropboxScriptPath = "testcase-postregistration-hook-wrong-signature.py";
        testCase.shouldThrowExceptionDuringRegistration = true;
        testCase.exceptionAcceptor = new IPredicate<Exception>()
            {
                @Override
                public boolean execute(Exception arg)
                {
                    return arg.getMessage().contains("wrong number of arguments");
                }
            };
        testCase.failurePoint = TestCaseParameters.FailurePoint.AFTER_GET_EXPERIMENT;
        testCases.add(testCase);

        testCase = new TestCaseParameters("Postregistration hook has wrong signature.");
        testCase.dropboxScriptPath = "testcase-postregistration-hook-wrong-signature.py";
        testCase.shouldThrowExceptionDuringRegistration = true;
        testCase.exceptionAcceptor = new IPredicate<Exception>()
            {
                @Override
                public boolean execute(Exception arg)
                {
                    return arg.getMessage().contains("wrong number of arguments");
                }
            };
        testCase.failurePoint = TestCaseParameters.FailurePoint.AT_THE_BEGINNING;
        testCases.add(versionV2(testCase));

        testCase = new TestCaseParameters("Simple transaction explicit rollback");
        testCase.dropboxScriptPath = "testcase-rollback.py";
        testCase.failurePoint = TestCaseParameters.FailurePoint.AFTER_GET_EXPERIMENT;
        testCase.createDataSetDelegate = createTwoDataSetsDelegate;
        testCase.incomingDataSetAfterRegistration = "untouched_two_datasets";
        testCases.addAll(multipleVersionsOfTestCase(testCase));

        testCase = new TestCaseParameters("Dying script");
        testCase.dropboxScriptPath = "dying-script.py";
        testCase.failurePoint = TestCaseParameters.FailurePoint.AT_THE_BEGINNING;
        testCase.createDataSetDelegate = createTwoDataSetsDelegate;
        testCase.incomingDataSetAfterRegistration = "untouched_two_datasets";
        testCases.addAll(multipleVersionsOfTestCase(testCase));

        testCase = new TestCaseParameters("Rollback dying script");
        testCase.dropboxScriptPath = "rollback-dying-script.py";
        testCase.failurePoint = TestCaseParameters.FailurePoint.AT_THE_BEGINNING;
        testCase.createDataSetDelegate = createTwoDataSetsDelegate;
        testCase.incomingDataSetAfterRegistration = "untouched_two_datasets";
        testCases.addAll(multipleVersionsOfTestCase(testCase));

        testCase = new TestCaseParameters("Two transactions.");
        testCase.dropboxScriptPath = "testcase-double-transaction.py";
        testCase.shouldRegisterTwoDataSets = true;
        testCase.createDataSetDelegate = createTwoDataSetsDelegate;
        testCases.add(testCase);

        testCase =
                new TestCaseParameters(
                        "Script dying because of non-serialized argument put to the map");
        testCase = versionV2(testCase);
        testCase.dropboxScriptPath = "testcase-registration-context-invalid-data.py";
        testCase.shouldThrowExceptionDuringRegistration = true;
        testCase.failurePoint = TestCaseParameters.FailurePoint.AT_THE_BEGINNING;
        testCases.add(testCase);

        return testCases;
    }

    // INFO: testCase parameters

    /**
     * Parameters for the single run of the testSimpleTransaction
     * 
     * @author jakubs
     */
    private static class TestCaseParameters implements Cloneable
    {
        /**
         * short description of the test. Will be presented in the test results view
         */
        protected String title;

        /**
         * The dropbox script file that should be used for this test case
         */
        protected String dropboxScriptPath = "simple-testcase.py";

        /**
         * Java program class name for pure Java dropboxes
         */
        protected String javaProgramClass;

        /**
         * Specifies what properties should be overriden for this test case.
         */
        protected HashMap<String, String> overrideProperties;

        /**
         * Describe what should happen with incoming data after execution of this test case.
         */
        protected String incomingDataSetAfterRegistration = "deleted";

        /**
         * Specifies the custom creator of datasets instead of createDataWithOneSubDataSet.
         */
        protected IDelegatedAction createDataSetDelegate = null;

        /**
         * Specifies the point of failure in registration process. Used for setting the expectations (at which point we should stop expecting method
         * calls from the happy scenario), as well as for flow control (throw exception from the right mocked methods), and for verification of
         * expectations.
         */
        protected FailurePoint failurePoint = null;

        /**
         * True if the registration should throw exception to the top level. With this setting the handler is said to throw all exception to the top
         * level, so that we can catch them. To check recovery from errors (like rollback mechanism) this should be set to false.
         */
        protected boolean shouldThrowExceptionDuringRegistration = false;

        /**
         * Must return true for the exception from the registration process, when one is caught.
         */
        protected IPredicate<Exception> exceptionAcceptor = null;

        /**
         * True if commit_transaction function is defined in a jython dropbox script file, and post_storage function is not. Used to check which of
         * two should be checked for.
         */
        protected boolean postStorageFunctionNotDefinedInADropbox = false;

        /**
         * True if the jython dropbox is in version 2 and the old jython hook methods should not be called
         */
        protected boolean dontCallOldApiJythonHooks = false;

        /**
         * If true, than we expect that two datasets have been registered.
         */
        protected boolean shouldRegisterTwoDataSets = false;

        /**
         * True if the dropbox should use autorecovery
         */
        protected boolean shouldUseAutoRecovery = false;

        /**
         * True if this is a v2 dropbox
         */
        protected boolean isV2 = false;

        private TestCaseParameters(String title)
        {
            this.title = title;
            this.overrideProperties = new HashMap<String, String>();
        }

        @Override
        public TestCaseParameters clone()
        {
            try
            {
                return (TestCaseParameters) super.clone();
            } catch (CloneNotSupportedException e)
            {
                return null;
            }
        }

        @Override
        public String toString()
        {
            return title;
        }

        // add more when necessary
        public enum FailurePoint
        {

            AT_THE_BEGINNING, AFTER_CREATE_DATA_SET_CODE, AFTER_GET_EXPERIMENT, DURING_VALIDATION,
            BEFORE_OPENBIS_REGISTRATION, DURING_OPENBIS_REGISTRATION;

            boolean beforeOrEqual(FailurePoint other)
            {
                return this.ordinal() <= other.ordinal();
            }

        }
    }

    // INFO: test simple transaction
    @Test(dataProvider = "simpleTransactionTestCaseProvider")
    public void testSimpleTransaction(final TestCaseParameters testCase)
    {
        initializeStorageRecoveryManagerMock();
        setUpHomeDataBaseExpectations();

        Properties properties =
                createThreadPropertiesRelativeToScriptsFolder(testCase.dropboxScriptPath,
                        testCase.javaProgramClass, testCase.overrideProperties);

        createHandler(properties, false, testCase.shouldThrowExceptionDuringRegistration);

        if (testCase.createDataSetDelegate != null)
        {
            testCase.createDataSetDelegate.execute();
        } else
        {
            createDataWithOneSubDataSet();
        }

        final RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails> atomicatOperationDetails =
                new RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails>();

        context.checking(getSimpleTransactionExpectations(testCase, atomicatOperationDetails));

        if (testCase.shouldThrowExceptionDuringRegistration)
        {
            try
            {
                handler.handle(markerFile);
                fail("Expected an exception.");
            } catch (Exception exception)
            {
                Throwable t = AssertionUtil.tryAsErrorCausedByUnexpectedInvocation(exception);
                if (t != null)
                {
                    throw new RuntimeException("Extracted unexpected invocation error "
                            + t.getMessage(), t);
                }

                if (testCase.exceptionAcceptor != null)
                {
                    assertTrue("Exception " + exception + "was not accepted by validator",
                            testCase.exceptionAcceptor.execute(exception));
                }
            }
            context.assertIsSatisfied();
            return;
        } else
        {
            handler.handle(markerFile);
        }

        // if there is a recovery file then call the recovery service

        checkInitialDirAfterRegistration(testCase.incomingDataSetAfterRegistration);

        AssertIncomingDirectory(testCase);

        assertCommitCount(testCase);

        assertJythonHooksExecuted(testCase);

        if (testCase.failurePoint == null)
        {
            if (testCase.shouldRegisterTwoDataSets)
            {
                List<AtomicEntityOperationDetails> recordedObjects =
                        atomicatOperationDetails.getRecordedObjects();

                assertEquals("There should be two items in recordedObjects", 2,
                        recordedObjects.size());
                assertStorageProcess(recordedObjects.get(0), DATA_SET_CODE, "sub_data_set_1", 0);
                assertStorageProcess(recordedObjects.get(1), DATA_SET_CODE_1, "sub_data_set_2", 1);
            } else
            {
                assertStorageProcess(atomicatOperationDetails.recordedObject(), DATA_SET_CODE,
                        "sub_data_set_1", 0);
            }
        } else
        {
            assertEquals("[]", Arrays.asList(stagingDirectory.list()).toString());
        }
        context.assertIsSatisfied();
    }

    public Expectations getSimpleTransactionExpectations(final TestCaseParameters testCase,
            final RecordingMatcher<AtomicEntityOperationDetails> atomicatOperationDetails)
    {
        final Experiment experiment =
                new ExperimentBuilder().identifier(EXPERIMENT_IDENTIFIER).getExperiment();
        return new Expectations()
            {
                {
                    setupExpectations();
                }

                protected void setupExpectations()
                {
                    generalAllowing();

                    if (testCase.failurePoint != null
                            && testCase.failurePoint
                                    .compareTo(TestCaseParameters.FailurePoint.DURING_OPENBIS_REGISTRATION) < 0)
                    {
                        cleanRecoveryCheckpoint(false);
                    }

                    if (testCase.failurePoint == TestCaseParameters.FailurePoint.AT_THE_BEGINNING)
                    {
                        return;
                    }

                    createDataSet();

                    if (testCase.failurePoint == TestCaseParameters.FailurePoint.AFTER_CREATE_DATA_SET_CODE)
                    {
                        return;
                    }

                    tryGetExperiment();

                    if (testCase.failurePoint == TestCaseParameters.FailurePoint.AFTER_GET_EXPERIMENT)
                    {
                        return;
                    }

                    validateDataSet();

                    if (testCase.failurePoint == TestCaseParameters.FailurePoint.DURING_VALIDATION)
                    {
                        return;
                    }

                    if (testCase.failurePoint == TestCaseParameters.FailurePoint.BEFORE_OPENBIS_REGISTRATION)
                    {
                        return;
                    }

                    checkpointPrecomittedState();

                    registerDataSets();

                    if (testCase.failurePoint == TestCaseParameters.FailurePoint.DURING_OPENBIS_REGISTRATION)
                    {
                        cleanRecoveryCheckpoint(true);
                        return;
                    }

                    checkpointStored();

                    setStorageConfirmed();

                    registrationCompleted();

                    will(checkPrecommitDirIsEmpty());
                }

                private void generalAllowing()
                {
                    allowing(storageRecoveryManager).getProcessingMarkerFile(with(any(File.class)));
                    will(returnValue(new File(incomingDataSetFile.getAbsolutePath()
                            + ".NON_EXISTING")));
                }

                @SuppressWarnings("unchecked")
                private void cleanRecoveryCheckpoint(boolean required)
                {
                    if (testCase.shouldUseAutoRecovery)
                    {
                        if (required)
                        {
                            one(storageRecoveryManager).removeCheckpoint(
                                    with(any(DataSetStorageAlgorithmRunner.class)));
                        } else
                        {
                            allowing(storageRecoveryManager).removeCheckpoint(
                                    with(any(DataSetStorageAlgorithmRunner.class)));
                        }
                    }
                }

                @SuppressWarnings("unchecked")
                private void checkpointStored()
                {
                    if (testCase.shouldUseAutoRecovery)
                    {
                        one(storageRecoveryManager).checkpointStoredStateBeforeStorageConfirmation(
                                with(any(DataSetStorageAlgorithmRunner.class)));
                    }
                }

                protected void setStorageConfirmed()
                {

                    if (testCase.shouldRegisterTwoDataSets)
                    {
                        // two transactions
                        one(openBisService).setStorageConfirmed(Arrays.asList(DATA_SET_CODE));
                        one(openBisService).setStorageConfirmed(Arrays.asList(DATA_SET_CODE_1));
                    } else
                    {
                        one(openBisService).setStorageConfirmed(Collections.singletonList(DATA_SET_CODE));
                    }
                }

                @SuppressWarnings("unchecked")
                protected void registerDataSets()
                {
                    one(openBisService).drawANewUniqueID();
                    will(returnValue(Long.valueOf(1)));
                    one(openBisService).performEntityOperations(with(atomicatOperationDetails));

                    if (testCase.failurePoint == TestCaseParameters.FailurePoint.DURING_OPENBIS_REGISTRATION)
                    {
                        Error e = new AssertionError("Fail");
                        will(throwException(e));

                        if (testCase.shouldUseAutoRecovery)
                        {
                            one(storageRecoveryManager).canRecoverFromError(e);
                            will(returnValue(false));
                        }
                    } else
                    {
                        // return value from performEntityOperations
                        // perform additional check if the precommit dir is empty immediatelly after
                        // performEntityOperations returns

                        will(doAll(checkPrecommitDirIsNotEmpty(), returnValue(new AtomicEntityOperationResult())));

                        if (testCase.shouldRegisterTwoDataSets)
                        {
                            one(openBisService).drawANewUniqueID();
                            will(returnValue(Long.valueOf(2)));
                            one(openBisService).performEntityOperations(
                                    with(atomicatOperationDetails));
                            will(doAll(checkPrecommitDirIsNotEmpty(), returnValue(new AtomicEntityOperationResult())));
                        }
                        if (testCase.shouldUseAutoRecovery)
                        {
                            one(storageRecoveryManager)
                                    .checkpointPrecommittedStateAfterPostRegistrationHook(
                                            with(any(DataSetStorageAlgorithmRunner.class)));
                        }
                    }
                }

                protected void validateDataSet()
                {
                    one(dataSetValidator).assertValidDataSet(DATA_SET_TYPE,
                            new File(new File(stagingDirectory, DATA_SET_CODE), "sub_data_set_1"));

                    if (testCase.failurePoint == TestCaseParameters.FailurePoint.DURING_VALIDATION)
                    {
                        Exception innerException = new Exception();
                        will(throwException(new UserFailureException("Data set of type '"
                                + DATA_SET_CODE + "' is invalid ", innerException)));
                    } else if (testCase.shouldRegisterTwoDataSets)
                    {
                        one(dataSetValidator).assertValidDataSet(
                                DATA_SET_TYPE,
                                new File(new File(stagingDirectory, DATA_SET_CODE_1),
                                        "sub_data_set_2"));
                    }
                }

                protected void tryGetExperiment()
                {
                    atLeast(1).of(openBisService).tryGetExperiment(
                            new ExperimentIdentifierFactory(experiment.getIdentifier())
                                    .createIdentifier());
                    will(returnValue(experiment));
                }

                protected void createDataSet()
                {
                    if (testCase.isV2)
                    {
                        one(openBisService).createPermIds(with(any(Integer.class)));
                        if (testCase.shouldRegisterTwoDataSets)
                        {
                            will(returnValue(Arrays.asList(DATA_SET_CODE, DATA_SET_CODE_1)));
                        } else
                        {
                            will(returnValue(Collections.singletonList(DATA_SET_CODE)));
                        }
                    } else
                    {
                        one(openBisService).createPermId();
                        will(returnValue(DATA_SET_CODE));
                        if (testCase.shouldRegisterTwoDataSets)
                        {
                            one(openBisService).createPermId();
                            will(returnValue(DATA_SET_CODE_1));
                        }
                    }
                }

                @SuppressWarnings("unchecked")
                protected void checkpointPrecomittedState()
                {
                    if (testCase.shouldUseAutoRecovery)
                    {
                        one(storageRecoveryManager).checkpointPrecommittedState(
                                with(any(TechId.class)),
                                with(any(DataSetStorageAlgorithmRunner.class)));
                    }
                }

                @SuppressWarnings("unchecked")
                protected void registrationCompleted()
                {
                    if (testCase.shouldUseAutoRecovery)
                    {
                        one(storageRecoveryManager).registrationCompleted(
                                with(any(DataSetStorageAlgorithmRunner.class)));
                    }
                }
            };
    }

    protected void AssertIncomingDirectory(final TestCaseParameters testCase)
    {
        // the incoming dir in storage processor is created at the beginning of transaction
        // so after the successful validation

        int dataSetsStoredInIncomingDir;
        // failure point before or at the moment of validation
        if (testCase.failurePoint != null
                && testCase.failurePoint
                        .compareTo(TestCaseParameters.FailurePoint.DURING_VALIDATION) <= 0)
        {
            dataSetsStoredInIncomingDir = 0;
        } else if (testCase.shouldRegisterTwoDataSets)
        {
            dataSetsStoredInIncomingDir = 2;
        } else
        {
            dataSetsStoredInIncomingDir = 1;
        }

        assertEquals(dataSetsStoredInIncomingDir, MockStorageProcessor.instance.incomingDirs.size());
    }

    protected void assertCommitCount(final TestCaseParameters testCase)
    {
        int expectedCommitCount;

        if (testCase.failurePoint != null)
        {
            expectedCommitCount = 0;
        } else if (testCase.shouldRegisterTwoDataSets)
        {
            expectedCommitCount = 2;
        } else
        {
            expectedCommitCount = 1;
        }

        assertEquals(expectedCommitCount, MockStorageProcessor.instance.calledCommitCount);
    }

    private void assertJythonHooksExecuted(final TestCaseParameters testCase)
    {

        JythonHookTestTool jythonHookTestTool =
                JythonHookTestTool.createFromWorkingDirectory(workingDirectory);

        if (testCase.failurePoint == null)
        {
            jythonHookTestTool.assertLogged("pre_metadata_registration");
            jythonHookTestTool.assertLogged("post_metadata_registration");

            if (testCase.postStorageFunctionNotDefinedInADropbox)
            {
                if (false == testCase.dontCallOldApiJythonHooks)
                {
                    jythonHookTestTool.assertLogged("commit_transaction");
                }
            } else
            {
                jythonHookTestTool.assertLogged("post_storage");
            }
        } else if (testCase.failurePoint
                .beforeOrEqual(TestCaseParameters.FailurePoint.DURING_VALIDATION))
        {
            // no hooks in this case
        } else if (testCase.failurePoint
                .beforeOrEqual(TestCaseParameters.FailurePoint.BEFORE_OPENBIS_REGISTRATION))
        {
            throw new NotImplementedException("Not a single test comes here");
        } else
        {
            jythonHookTestTool.assertLogged("pre_metadata_registration");
            jythonHookTestTool.assertLogged("rollback_pre_registration");
        }

        // if we register two datasets all hooks on the path are executed twice
        if (testCase.shouldRegisterTwoDataSets)
        {
            jythonHookTestTool.assertLogged("pre_metadata_registration");
            jythonHookTestTool.assertLogged("post_metadata_registration");
            jythonHookTestTool.assertLogged("post_storage");
        }

        jythonHookTestTool.assertNoMoreMessages();
    }

    /**
     * Parameters for the single run of the testIncomingFileDeleted
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    private static class IncomingFileDeletedTestCaseParameters implements Cloneable
    {
        /**
         * short description of the test. Will be presented in the test results view
         */
        protected String title;

        /**
         * The dropbox script file that should be used for this test case
         */
        protected String dropboxScriptPath = scriptPathV2("simple-testcase.py");

        /**
         * Java program class name for pure Java dropboxes
         */
        protected String javaProgramClass = programJavaV2("simple-testcase.py");

        /**
         * Specifies what properties should be overriden for this test case.
         */
        protected HashMap<String, String> overrideProperties;

        /**
         * When does this incoming file get deleted?
         */
        protected DeletionPoint deletionPoint = null;

        private IncomingFileDeletedTestCaseParameters(String title)
        {
            this.title = title;
            this.overrideProperties = new HashMap<String, String>();
        }

        @Override
        public IncomingFileDeletedTestCaseParameters clone()
        {
            try
            {
                return (IncomingFileDeletedTestCaseParameters) super.clone();
            } catch (CloneNotSupportedException e)
            {
                return null;
            }
        }

        @Override
        public String toString()
        {
            return title;
        }

        public TestCaseParameters toTestCaseParameters()
        {
            TestCaseParameters testCase = new TestCaseParameters(title);
            testCase.shouldUseAutoRecovery = true;
            testCase.isV2 = true;
            if (deletionPoint == DeletionPoint.BEFORE_OPENBIS_REGISTRATION)
            {
                testCase.failurePoint = TestCaseParameters.FailurePoint.BEFORE_OPENBIS_REGISTRATION;
            } else if (deletionPoint == DeletionPoint.DURING_PRECOMMIT_COPY)
            {
                testCase.failurePoint = TestCaseParameters.FailurePoint.AT_THE_BEGINNING;
            }

            return testCase;
        }

        public enum DeletionPoint
        {
            DURING_PRECOMMIT_COPY, BEFORE_OPENBIS_REGISTRATION, AFTER_OPENBIS_REGISTRATION;
        }
    }

    @DataProvider(name = "incomingFileDeletedTestCaseProvider")
    public Object[][] incomingFileDeletedCases()
    {
        List<IncomingFileDeletedTestCaseParameters> testCases = incomingFileDeletedCasesList();
        return asObjectArray(testCases);
    }

    public List<IncomingFileDeletedTestCaseParameters> incomingFileDeletedCasesList()
    {

        ArrayList<IncomingFileDeletedTestCaseParameters> testCases =
                new ArrayList<IncomingFileDeletedTestCaseParameters>();

        // Delete the incoming file while the prestaging copy is being made
        IncomingFileDeletedTestCaseParameters testCase =
                new IncomingFileDeletedTestCaseParameters(
                        "Incoming file deleted during prestaging copy.");
        testCase.deletionPoint =
                IncomingFileDeletedTestCaseParameters.DeletionPoint.DURING_PRECOMMIT_COPY;
        testCases.add(testCase);

        // Delete the incoming file after the prestaging copy has been made, but before the metadata
        // has been registered with the AS
        testCase =
                new IncomingFileDeletedTestCaseParameters(
                        "Incoming file deleted before metadata registration.");
        testCase.dropboxScriptPath = scriptPathV2("delete-before-registration.py");
        testCase.deletionPoint =
                IncomingFileDeletedTestCaseParameters.DeletionPoint.BEFORE_OPENBIS_REGISTRATION;
        testCases.add(testCase);

        // Delete the incoming file after the metadata has been registered with the AS
        testCase =
                new IncomingFileDeletedTestCaseParameters(
                        "Incoming file deleted after metadata registration.");
        testCase.dropboxScriptPath = scriptPathV2("delete-after-registration.py");
        testCase.deletionPoint =
                IncomingFileDeletedTestCaseParameters.DeletionPoint.AFTER_OPENBIS_REGISTRATION;
        testCases.add(testCase);

        return testCases;
    }

    @Test(dataProvider = "incomingFileDeletedTestCaseProvider")
    public void testIncomingFileDeleted(final IncomingFileDeletedTestCaseParameters testCase)
    {
        // Not yet handled
        initializeStorageRecoveryManagerMock();
        setUpHomeDataBaseExpectations();

        Properties properties =
                createThreadPropertiesRelativeToScriptsFolder(testCase.dropboxScriptPath,
                        testCase.javaProgramClass, testCase.overrideProperties);

        // Create the handler
        TopLevelDataSetRegistratorGlobalState globalState = createGlobalState(properties);
        handler = new TestingDataSetHandlerV2(globalState, false, false);

        final RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails> atomicOperationDetails =
                new RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails>();

        // Used in the parallel deletion of the incoming file test
        if (testCase.deletionPoint == IncomingFileDeletedTestCaseParameters.DeletionPoint.DURING_PRECOMMIT_COPY)
        {
            TestBigStructureCreator creator = createBigStructureDataSet();
            assertTrue(creator.verifyStructure());
            context.checking(getTestIncomingFileDeletedExcpectations(testCase,
                    atomicOperationDetails));

            creator.deleteBigStructureAsync();
            handler.handle(markerFile);
            assertFalse(creator.verifyStructure());
            JythonHookTestTool jythonHookTestTool =
                    JythonHookTestTool.createFromWorkingDirectory(workingDirectory);
            jythonHookTestTool.assertNoMoreMessages();
        } else
        {
            createDataWithOneSubDataSet();
            context.checking(getTestIncomingFileDeletedExcpectations(testCase,
                    atomicOperationDetails));
            handler.handle(markerFile);
        }

        if (testCase.deletionPoint == IncomingFileDeletedTestCaseParameters.DeletionPoint.AFTER_OPENBIS_REGISTRATION)
        {
            assertEquals(1, MockStorageProcessor.instance.incomingDirs.size());
            assertEquals(1, MockStorageProcessor.instance.calledCommitCount);
            assertStorageProcess(atomicOperationDetails.recordedObject(), DATA_SET_CODE,
                    "sub_data_set_1", 0);
            assertFalse("The incoming data set file should have been deleted",
                    incomingDataSetFile.exists());
            assertEquals("[]", Arrays.asList(stagingDirectory.list()).toString());
        } else if (testCase.deletionPoint == IncomingFileDeletedTestCaseParameters.DeletionPoint.DURING_PRECOMMIT_COPY)
        {
            assertEquals(0, MockStorageProcessor.instance.incomingDirs.size());
            assertEquals(0, MockStorageProcessor.instance.calledCommitCount);
            assertEquals("[]", Arrays.asList(stagingDirectory.list()).toString());
        } else
        {
            assertEquals(1, MockStorageProcessor.instance.incomingDirs.size());
            assertEquals(0, MockStorageProcessor.instance.calledCommitCount);
            assertEquals("[]", Arrays.asList(stagingDirectory.list()).toString());
        }

        context.assertIsSatisfied();
    }

    @SuppressWarnings("unchecked")
    private ExpectationBuilder getTestIncomingFileDeletedExcpectations(
            final IncomingFileDeletedTestCaseParameters testCase,
            final RecordingMatcher<AtomicEntityOperationDetails> atomicOperationDetails)
    {
        Expectations e =
                getSimpleTransactionExpectations(testCase.toTestCaseParameters(),
                        atomicOperationDetails);
        if (testCase.deletionPoint == IncomingFileDeletedTestCaseParameters.DeletionPoint.BEFORE_OPENBIS_REGISTRATION)
        {
            e.one(openBisService).drawANewUniqueID();
            e.will(Expectations.returnValue(Long.valueOf(1)));

            e.one(storageRecoveryManager).checkpointPrecommittedState(
                    e.with(Expectations.any(TechId.class)),
                    e.with(Expectations.any(DataSetStorageAlgorithmRunner.class)));

            // e.one(storageRecoveryManager).removeCheckpoint(
            // e.with(Expectations.any(DataSetStorageAlgorithmRunner.class)));
        }
        return e;
    }

    @Test
    public void testTwoSimpleDataSets()
    {
        setUpHomeDataBaseExpectations();
        Properties properties =
                createThreadPropertiesRelativeToScriptsFolder("two-simple-datasets.py");
        createHandler(properties, false, true);
        createData();
        ExperimentBuilder builder1 = new ExperimentBuilder().identifier("/SPACE/PROJECT/EXP1");
        final Experiment experiment1 = builder1.getExperiment();
        ExperimentBuilder builder2 = new ExperimentBuilder().identifier("/SPACE/PROJECT/EXP2");
        final Experiment experiment2 = builder2.getExperiment();
        final RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails> operations =
                new RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails>();
        context.checking(new Expectations()
            {
                {
                    one(openBisService).createPermId();
                    will(returnValue(DATA_SET_CODE + 1));

                    atLeast(1).of(openBisService).tryGetExperiment(
                            new ExperimentIdentifierFactory(experiment1.getIdentifier())
                                    .createIdentifier());
                    will(returnValue(experiment1));

                    one(dataSetValidator).assertValidDataSet(
                            DATA_SET_TYPE,
                            new File(new File(stagingDirectory, DATA_SET_CODE + 1),
                                    "sub_data_set_1"));

                    one(openBisService).createPermId();
                    will(returnValue(DATA_SET_CODE + 2));

                    atLeast(1).of(openBisService).tryGetExperiment(
                            new ExperimentIdentifierFactory(experiment2.getIdentifier())
                                    .createIdentifier());
                    will(returnValue(experiment2));

                    one(dataSetValidator).assertValidDataSet(
                            DATA_SET_TYPE,
                            new File(new File(stagingDirectory, DATA_SET_CODE + 2),
                                    "sub_data_set_2"));

                    one(openBisService).drawANewUniqueID();
                    will(returnValue(Long.valueOf(1)));
                    one(openBisService).performEntityOperations(with(operations));
                    will(returnValue(new AtomicEntityOperationResult()));

                    one(openBisService).setStorageConfirmed(Arrays.asList(DATA_SET_CODE + 1, DATA_SET_CODE + 2));
                }
            });

        handler.handle(markerFile);
        checkInitialDirAfterRegistration("deleted");

        assertEquals(2, MockStorageProcessor.instance.incomingDirs.size());
        assertEquals(2, MockStorageProcessor.instance.calledCommitCount);
        assertEquals(2, operations.recordedObject().getDataSetRegistrations().size());

        NewExternalData dataSet1 = operations.recordedObject().getDataSetRegistrations().get(0);
        NewExternalData dataSet2 = operations.recordedObject().getDataSetRegistrations().get(1);

        assertEquals(experiment1.getIdentifier(), dataSet1.getExperimentIdentifierOrNull()
                .toString());
        assertEquals(DATA_SET_CODE + 1, dataSet1.getCode());
        assertEquals(DATA_SET_TYPE, dataSet1.getDataSetType());
        File datasetLocation1 =
                DatasetLocationUtil.getDatasetLocationPath(workingDirectory, DATA_SET_CODE + 1,
                        ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID,
                        DATABASE_INSTANCE_UUID);
        assertEquals(FileUtilities.getRelativeFilePath(new File(workingDirectory,
                ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID),
                datasetLocation1), dataSet1.getLocation());
        assertEquals(new File(stagingDirectory, DATA_SET_CODE + 1 + "-storage"),
                MockStorageProcessor.instance.rootDirs.get(0));
        File incomingDir1 = MockStorageProcessor.instance.incomingDirs.get(0);
        assertEquals(new File(new File(stagingDirectory, DATA_SET_CODE + 1), "sub_data_set_1"),
                incomingDir1);
        assertEquals(
                "hello world1",
                FileUtilities.loadToString(
                        new File(new File(datasetLocation1, "sub_data_set_1"), "read1.me")).trim());
        assertEquals(experiment2.getIdentifier(), dataSet2.getExperimentIdentifierOrNull()
                .toString());
        assertEquals(DATA_SET_CODE + 2, dataSet2.getCode());
        assertEquals(DATA_SET_TYPE, dataSet2.getDataSetType());
        File datasetLocation2 =
                DatasetLocationUtil.getDatasetLocationPath(workingDirectory, DATA_SET_CODE + 2,
                        ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID,
                        DATABASE_INSTANCE_UUID);
        assertEquals(FileUtilities.getRelativeFilePath(new File(workingDirectory,
                ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID),
                datasetLocation2), dataSet2.getLocation());
        assertEquals(new File(stagingDirectory, DATA_SET_CODE + 2 + "-storage"),
                MockStorageProcessor.instance.rootDirs.get(1));
        File incomingDir2 = MockStorageProcessor.instance.incomingDirs.get(1);
        assertEquals(new File(new File(stagingDirectory, DATA_SET_CODE + 2), "sub_data_set_2"),
                incomingDir2);
        assertEquals(
                "hello world2",
                FileUtilities.loadToString(
                        new File(new File(datasetLocation2, "sub_data_set_2"), "read2.me")).trim());
        context.assertIsSatisfied();
    }

    @Test
    public void testTransactionWithNewExperiment()
    {
        setUpHomeDataBaseExpectations();
        Properties properties =
                createThreadPropertiesRelativeToScriptsFolder("transaction-with-new-experiment.py");
        createHandler(properties, false, true);
        createDataWithOneSubDataSet();
        final RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails> atomicatOperationDetails =
                new RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails>();
        context.checking(new Expectations()
            {
                {
                    one(openBisService).createPermId();
                    will(returnValue(DATA_SET_CODE));

                    one(openBisService).createPermId();
                    will(returnValue(EXPERIMENT_PERM_ID));

                    one(dataSetValidator).assertValidDataSet(DATA_SET_TYPE,
                            new File(new File(stagingDirectory, DATA_SET_CODE), "sub_data_set_1"));

                    one(openBisService).drawANewUniqueID();
                    will(returnValue(Long.valueOf(1)));
                    one(openBisService).performEntityOperations(with(atomicatOperationDetails));
                    will(returnValue(new AtomicEntityOperationResult()));

                    one(openBisService).setStorageConfirmed(Arrays.asList(DATA_SET_CODE));
                }
            });

        handler.handle(markerFile);

        checkInitialDirAfterRegistration("deleted");
        assertEquals(1, MockStorageProcessor.instance.incomingDirs.size());
        assertEquals(1, atomicatOperationDetails.recordedObject().getDataSetRegistrations().size());

        NewExternalData dataSet =
                atomicatOperationDetails.recordedObject().getDataSetRegistrations().get(0);

        assertEquals(DATA_SET_CODE, dataSet.getCode());
        assertEquals(DATA_SET_TYPE, dataSet.getDataSetType());

        NewProperty newProp = new NewProperty("dataSetProp", "dataSetPropValue");
        assertTrue(dataSet.getExtractableData().getDataSetProperties().contains(newProp));

        File datasetLocation =
                DatasetLocationUtil.getDatasetLocationPath(workingDirectory, DATA_SET_CODE,
                        ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID,
                        DATABASE_INSTANCE_UUID);
        assertEquals(FileUtilities.getRelativeFilePath(new File(workingDirectory,
                ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID),
                datasetLocation), dataSet.getLocation());
        assertEquals(1, MockStorageProcessor.instance.calledCommitCount);
        File incomingDir = MockStorageProcessor.instance.incomingDirs.get(0);
        assertEquals(new File(new File(stagingDirectory, DATA_SET_CODE), "sub_data_set_1"),
                incomingDir);
        context.assertIsSatisfied();
    }

    @Test
    public void testTransactionWithNewSample()
    {
        setUpHomeDataBaseExpectations();
        Properties properties =
                createThreadPropertiesRelativeToScriptsFolder("transaction-with-new-sample.py");
        createHandler(properties, false, true);
        createData();
        final RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails> atomicatOperationDetails =
                new RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails>();
        context.checking(new Expectations()
            {
                {
                    one(openBisService).createPermId();
                    will(returnValue(DATA_SET_CODE));

                    one(openBisService).createPermId();
                    will(returnValue(EXPERIMENT_PERM_ID));

                    one(openBisService).createPermId();
                    will(returnValue(SAMPLE_PERM_ID));

                    one(dataSetValidator).assertValidDataSet(DATA_SET_TYPE,
                            new File(new File(stagingDirectory, DATA_SET_CODE), "sub_data_set_1"));

                    one(openBisService).drawANewUniqueID();
                    will(returnValue(Long.valueOf(1)));
                    one(openBisService).performEntityOperations(with(atomicatOperationDetails));
                    will(returnValue(new AtomicEntityOperationResult()));

                    one(openBisService).setStorageConfirmed(Arrays.asList(DATA_SET_CODE));
                }
            });

        handler.handle(markerFile);

        assertEquals(1, MockStorageProcessor.instance.incomingDirs.size());
        ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails operations =
                atomicatOperationDetails.recordedObject();

        assertEquals(1, operations.getDataSetRegistrations().size());
        assertEquals(0, operations.getExperimentUpdates().size());
        assertEquals(0, operations.getSampleUpdates().size());
        assertEquals(1, operations.getSampleRegistrations().size());
        assertEquals(1, operations.getExperimentRegistrations().size());

        NewSample newSample = operations.getSampleRegistrations().get(0);
        assertEquals(SAMPLE_PERM_ID, newSample.getPermID());
        assertEquals(EXPERIMENT_IDENTIFIER, newSample.getExperimentIdentifier());
        assertEquals("sample_type", newSample.getSampleType().getCode());
        String[] parents = newSample.getParentsOrNull();
        Arrays.sort(parents);
        assertEquals("[/SPACE/PARENT1, /SPACE/PARENT2]", Arrays.toString(parents));

        NewExperiment newExperiment = operations.getExperimentRegistrations().get(0);
        assertEquals(EXPERIMENT_PERM_ID, newExperiment.getPermID());
        assertEquals(EXPERIMENT_IDENTIFIER, newExperiment.getIdentifier());
        assertEquals("experiment_type", newExperiment.getExperimentTypeCode());

        NewExternalData dataSet = operations.getDataSetRegistrations().get(0);
        assertEquals(DATA_SET_CODE, dataSet.getCode());
        assertEquals(DATA_SET_TYPE, dataSet.getDataSetType());

        File datasetLocation =
                DatasetLocationUtil.getDatasetLocationPath(workingDirectory, DATA_SET_CODE,
                        ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID,
                        DATABASE_INSTANCE_UUID);
        assertEquals(FileUtilities.getRelativeFilePath(new File(workingDirectory,
                ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID),
                datasetLocation), dataSet.getLocation());
        assertEquals(1, MockStorageProcessor.instance.calledCommitCount);
        File incomingDir = MockStorageProcessor.instance.incomingDirs.get(0);
        assertEquals(new File(new File(stagingDirectory, DATA_SET_CODE), "sub_data_set_1"),
                incomingDir);
        context.assertIsSatisfied();
    }

    @Test
    public void testTransactionWithMutableSample()
    {
        setUpHomeDataBaseExpectations();
        Properties properties =
                createThreadPropertiesRelativeToScriptsFolder("transaction-with-mutable-sample.py");
        createHandler(properties, false, true);
        createData();

        final RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails> atomicatOperationDetails =
                new RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails>();
        context.checking(new Expectations()
            {
                {
                    Experiment experiment = new Experiment();
                    experiment.setIdentifier("/SPACE/PROJECT/EXP-CODE");
                    experiment.setCode("EXP-CODE");
                    Person registrator = new Person();
                    registrator.setEmail("email@email.com");
                    experiment.setRegistrator(registrator);

                    SearchCriteria searchCriteria = createTestSearchCriteria("SAMPLE_TYPE");
                    oneOf(openBisService).searchForSamples(searchCriteria);

                    SampleBuilder sampleBuilder = new SampleBuilder();
                    sampleBuilder.id(SEARCH_RETURNED_SAMPLE_DB_ID);
                    sampleBuilder.modificationDate(new Date());
                    sampleBuilder.identifier("/SPACE/SAMPLE-CODE");
                    sampleBuilder.experiment(experiment);
                    sampleBuilder.permID("SAMPLE_PERM_ID");
                    sampleBuilder.code("SAMPLE-CODE");
                    sampleBuilder.type("SAMPLE_TYPE");

                    will(returnValue(Arrays.asList(sampleBuilder.getSample())));

                    one(openBisService).drawANewUniqueID();
                    will(returnValue(Long.valueOf(1)));
                    one(openBisService).performEntityOperations(with(atomicatOperationDetails));
                    will(returnValue(new AtomicEntityOperationResult()));
                }
            });

        handler.handle(markerFile);

        ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails operations =
                atomicatOperationDetails.recordedObject();

        assertEquals(0, operations.getDataSetRegistrations().size());
        assertEquals(0, operations.getExperimentUpdates().size());
        assertEquals(1, operations.getSampleUpdates().size());
        assertEquals(0, operations.getSampleRegistrations().size());
        assertEquals(0, operations.getExperimentRegistrations().size());

        SampleUpdatesDTO updatedSample = operations.getSampleUpdates().get(0);
        assertEquals(SEARCH_RETURNED_SAMPLE_DB_ID, updatedSample.getSampleIdOrNull().getId());
        context.assertIsSatisfied();
    }

    @Test
    public void testTransactionWithNewMaterial()
    {
        setUpHomeDataBaseExpectations();
        Properties properties =
                createThreadPropertiesRelativeToScriptsFolder("transaction-with-new-material.py");
        createHandler(properties, false, true);
        createData();

        ExperimentBuilder builder = new ExperimentBuilder().identifier(EXPERIMENT_IDENTIFIER);
        final Experiment experiment = builder.getExperiment();
        final RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails> atomicatOperationDetails =
                new RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails>();
        context.checking(new Expectations()
            {
                {
                    one(openBisService).createPermId();
                    will(returnValue(DATA_SET_CODE));

                    atLeast(1).of(openBisService).tryGetExperiment(
                            new ExperimentIdentifierFactory(experiment.getIdentifier())
                                    .createIdentifier());
                    will(returnValue(experiment));

                    one(dataSetValidator).assertValidDataSet(DATA_SET_TYPE,
                            new File(new File(stagingDirectory, DATA_SET_CODE), "sub_data_set_1"));

                    one(openBisService).drawANewUniqueID();
                    will(returnValue(Long.valueOf(1)));
                    one(openBisService).performEntityOperations(with(atomicatOperationDetails));
                    will(returnValue(new AtomicEntityOperationResult()));

                    one(openBisService).setStorageConfirmed(Arrays.asList(DATA_SET_CODE));
                }
            });

        handler.handle(markerFile);

        assertEquals(1, MockStorageProcessor.instance.incomingDirs.size());
        ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails operations =
                atomicatOperationDetails.recordedObject();

        assertEquals(1, operations.getDataSetRegistrations().size());
        assertEquals(0, operations.getExperimentUpdates().size());
        assertEquals(0, operations.getSampleUpdates().size());
        assertEquals(0, operations.getSampleRegistrations().size());
        assertEquals(0, operations.getExperimentRegistrations().size());
        assertEquals(1, operations.getMaterialRegistrations().size());

        NewMaterial newMaterial =
                operations.getMaterialRegistrations().get("new-material-type").get(0);
        assertEquals("new-material", newMaterial.getCode());
        assertEquals("[material-prop: material-prop-value]",
                Arrays.asList(newMaterial.getProperties()).toString());

        NewExternalData dataSet = operations.getDataSetRegistrations().get(0);
        assertEquals(DATA_SET_CODE, dataSet.getCode());
        assertEquals(DATA_SET_TYPE, dataSet.getDataSetType());

        context.assertIsSatisfied();
    }

    @Test
    public void testTransactionWithDataSetUpdate()
    {
        setUpHomeDataBaseExpectations();
        Properties properties =
                createThreadPropertiesRelativeToScriptsFolder("transaction-with-dataset-update.py");
        createHandler(properties, false, true);
        createData();

        ExperimentBuilder builder = new ExperimentBuilder().identifier(EXPERIMENT_IDENTIFIER);
        final Experiment experiment = builder.getExperiment();

        final ContainerDataSet containerDataSet = new ContainerDataSet();
        containerDataSet.setId(1L);

        final RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails> atomicOperationDetails =
                new RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails>();
        context.checking(new Expectations()
            {
                {
                    one(openBisService).createPermId();
                    will(returnValue(DATA_SET_CODE));

                    atLeast(1).of(openBisService).tryGetExperiment(
                            new ExperimentIdentifierFactory(experiment.getIdentifier())
                                    .createIdentifier());
                    will(returnValue(experiment));

                    one(openBisService).tryGetDataSet(CONTAINER_DATA_SET_CODE);
                    will(returnValue(containerDataSet));

                    one(dataSetValidator).assertValidDataSet(DATA_SET_TYPE,
                            new File(new File(stagingDirectory, DATA_SET_CODE), "data_set"));

                    one(openBisService).drawANewUniqueID();
                    will(returnValue(Long.valueOf(1)));
                    one(openBisService).performEntityOperations(with(atomicOperationDetails));
                    will(returnValue(new AtomicEntityOperationResult()));

                    one(openBisService).setStorageConfirmed(Collections.singletonList(DATA_SET_CODE));
                }
            });

        handler.handle(markerFile);

        assertEquals(1, MockStorageProcessor.instance.incomingDirs.size());
        ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails operations =
                atomicOperationDetails.recordedObject();

        assertEquals(1, operations.getDataSetRegistrations().size());
        assertEquals(0, operations.getExperimentUpdates().size());
        assertEquals(0, operations.getSampleUpdates().size());
        assertEquals(0, operations.getSampleRegistrations().size());
        assertEquals(0, operations.getExperimentRegistrations().size());
        assertEquals(1, operations.getDataSetUpdates().size());

        NewExternalData dataSet = operations.getDataSetRegistrations().get(0);
        assertEquals(DATA_SET_CODE, dataSet.getCode());
        assertEquals(DATA_SET_TYPE, dataSet.getDataSetType());

        DataSetUpdatesDTO dataSetUpdate = operations.getDataSetUpdates().get(0);
        assertEquals(Arrays.asList(DATA_SET_CODE),
                Arrays.asList(dataSetUpdate.getModifiedContainedDatasetCodesOrNull()));

        EntityProperty propertyChanged =
                new PropertyBuilder("newProp").value("newValue").getProperty();
        assertEquals(Arrays.asList(propertyChanged).toString(), dataSetUpdate.getProperties()
                .toString());
        assertEquals(EXPERIMENT_IDENTIFIER, dataSetUpdate.getExperimentIdentifierOrNull()
                .toString());

        File datasetLocation =
                DatasetLocationUtil.getDatasetLocationPath(workingDirectory, DATA_SET_CODE,
                        ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID,
                        DATABASE_INSTANCE_UUID);
        assertEquals(FileUtilities.getRelativeFilePath(new File(workingDirectory,
                ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID),
                datasetLocation), dataSet.getLocation());
        assertEquals(1, MockStorageProcessor.instance.calledCommitCount);
        context.assertIsSatisfied();
    }

    private void createData()
    {
        incomingDataSetFile = createDirectory(workingDirectory, "data_set");

        assertTrue(incomingDataSetFile.isDirectory());

        subDataSet1 = createDirectory(incomingDataSetFile, "sub_data_set_1");
        subDataSet2 = createDirectory(incomingDataSetFile, "sub_data_set_2");

        FileUtilities.writeToFile(new File(subDataSet1, "read1.me"), "hello world1");
        FileUtilities.writeToFile(new File(subDataSet2, "read2.me"), "hello world2");

        markerFile = new File(workingDirectory, FileConstants.IS_FINISHED_PREFIX + "data_set");
        FileUtilities.writeToFile(markerFile, "");
    }

    private void createDataWithOneSubDataSet()
    {
        incomingDataSetFile = createDirectory(workingDirectory, "data_set");

        assertTrue(incomingDataSetFile.isDirectory());

        subDataSet1 = createDirectory(incomingDataSetFile, "sub_data_set_1");

        FileUtilities.writeToFile(new File(subDataSet1, "read1.me"), "hello world1");

        markerFile = new File(workingDirectory, FileConstants.IS_FINISHED_PREFIX + "data_set");
        FileUtilities.writeToFile(markerFile, "");
    }

    private TestBigStructureCreator createBigStructureDataSet()
    {
        try
        {
            File root = new File(workingDirectory, "data_set");
            int[] numberOfFolders = { 100, 10 };
            int[] numberOfFiles = { 1, 10, 10 };
            TestBigStructureCreator creator =
                    new TestBigStructureCreator(root, numberOfFolders, numberOfFiles);
            incomingDataSetFile = creator.createBigStructure();
            assertTrue(incomingDataSetFile.isDirectory());
            markerFile = new File(workingDirectory, FileConstants.IS_FINISHED_PREFIX + "data_set");
            FileUtilities.writeToFile(markerFile, "");
            return creator;
        } catch (IOException e)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(e);
        }
    }

    @Test
    public void testScriptPathDeletedLater()
    {
        setUpHomeDataBaseExpectations();
        String scriptPath = "foo.py";
        Properties threadProperties = createThreadProperties(scriptPath);

        // test the situation where script has been deleted later
        File scriptFile = new File(scriptPath);
        FileUtilities.writeToFile(scriptFile, "x");
        createHandler(threadProperties, false);
        FileUtilities.delete(scriptFile);

        createData();

        handler.handle(markerFile);

        assertTrue(handler.getExpectations().didServiceRollbackHappen);
        assertFalse(handler.getExpectations().didTransactionRollbackHappen);

        context.assertIsSatisfied();
    }

    @Test
    public void testScriptPathMissing()
    {
        setUpHomeDataBaseExpectations();
        String scriptPath = "foo.py";
        Properties threadProperties = createThreadProperties(scriptPath);

        // it should not be possible to create a handler if script does not exist
        try
        {
            createHandler(threadProperties, false);
            fail("The script should does not exist");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals(ex.getMessage(), "Script file 'foo.py' does not exist!");
        }
        context.assertIsSatisfied();
    }

    @Test
    public void testSearching()
    {
        setUpHomeDataBaseExpectations();
        Properties threadProperties = createThreadPropertiesRelativeToScriptsFolder("search.py");
        createHandler(threadProperties, false, true);

        createData();

        setUpSearchExpectations();
        context.checking(new Expectations()
            {
                {
                    one(openBisService).drawANewUniqueID();
                    will(returnValue(Long.valueOf(1)));
                    oneOf(openBisService)
                            .performEntityOperations(
                                    with(any(ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails.class)));
                    will(returnValue(new AtomicEntityOperationResult()));
                }
            });

        handler.handle(markerFile);

        assertEquals(0, MockStorageProcessor.instance.incomingDirs.size());
        assertEquals(0, MockStorageProcessor.instance.calledCommitCount);

        assertFalse(handler.getExpectations().didServiceRollbackHappen);
        assertFalse(handler.getExpectations().didTransactionRollbackHappen);

        context.assertIsSatisfied();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testV2ImportWithoutDataSet()
    {
        initializeStorageRecoveryManagerMock();

        setUpHomeDataBaseExpectations();
        Properties threadProperties =
                createThreadPropertiesRelativeToScriptsFolder("v2-testcase-no-dataset.py");
        threadProperties.put("TEST_V2_API", "");
        createHandler(threadProperties, false, true);

        createData();

        context.checking(new Expectations()
            {
                {
                    allowing(storageRecoveryManager).getProcessingMarkerFile(incomingDataSetFile);
                    will(returnValue(new File(incomingDataSetFile.getParent(), "a_marker_file")));

                    one(storageRecoveryManager)
                            .checkpointPrecommittedState(with(any(TechId.class)),
                                    with(any(DataSetStorageAlgorithmRunner.class)));

                    oneOf(openBisService).drawANewUniqueID();
                    will(returnValue(Long.valueOf(1)));

                    oneOf(openBisService)
                            .performEntityOperations(
                                    with(any(ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails.class)));
                    will(returnValue(new AtomicEntityOperationResult()));

                    one(storageRecoveryManager)
                            .checkpointPrecommittedStateAfterPostRegistrationHook(
                                    with(any(DataSetStorageAlgorithmRunner.class)));

                    one(storageRecoveryManager).checkpointStoredStateBeforeStorageConfirmation(
                            with(any(DataSetStorageAlgorithmRunner.class)));

                    oneOf(storageRecoveryManager).registrationCompleted(
                            with(any(DataSetStorageAlgorithmRunner.class)));
                }
            });

        handler.handle(markerFile);

        JythonHookTestTool hookTool =
                JythonHookTestTool.createFromWorkingDirectory(workingDirectory);

        hookTool.assertLogged("pre_metadata_registration");
        hookTool.assertLogged("post_metadata_registration");
        hookTool.assertLogged("post_storage");

        hookTool.assertNoMoreMessages();

        context.assertIsSatisfied();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testV2ImportLinkDataSet()
    {
        initializeStorageRecoveryManagerMock();

        setUpHomeDataBaseExpectations();
        Properties threadProperties =
                createThreadPropertiesRelativeToScriptsFolder("v2-link-testcase.py");
        threadProperties.put("TEST_V2_API", "");
        createHandler(threadProperties, false, true);

        ExperimentBuilder builder = new ExperimentBuilder().identifier(EXPERIMENT_IDENTIFIER);
        final Experiment experiment = builder.getExperiment();

        final ExternalDataManagementSystem edms = new ExternalDataManagementSystem();
        edms.setCode("DMS_1");

        createData();

        context.checking(new Expectations()
            {
                {
                    allowing(storageRecoveryManager).getProcessingMarkerFile(incomingDataSetFile);
                    will(returnValue(new File(incomingDataSetFile.getParent(), "a_marker_file")));

                    one(openBisService).createPermIds(1);
                    will(returnValue(Arrays.asList(DATA_SET_CODE)));

                    atLeast(1).of(openBisService).tryGetExperiment(
                            new ExperimentIdentifierFactory(experiment.getIdentifier())
                                    .createIdentifier());
                    will(returnValue(experiment));

                    one(openBisService).tryGetExternalDataManagementSystem("DMS_1");
                    will(returnValue(edms));

                    one(dataSetValidator).assertValidDataSet(LINK_DATA_SET_TYPE, null);

                    one(storageRecoveryManager)
                            .checkpointPrecommittedState(with(any(TechId.class)),
                                    with(any(DataSetStorageAlgorithmRunner.class)));

                    oneOf(openBisService).drawANewUniqueID();
                    will(returnValue(Long.valueOf(1)));

                    oneOf(openBisService)
                            .performEntityOperations(
                                    with(any(ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails.class)));
                    will(returnValue(new AtomicEntityOperationResult()));

                    one(storageRecoveryManager)
                            .checkpointPrecommittedStateAfterPostRegistrationHook(
                                    with(any(DataSetStorageAlgorithmRunner.class)));

                    one(storageRecoveryManager).checkpointStoredStateBeforeStorageConfirmation(
                            with(any(DataSetStorageAlgorithmRunner.class)));

                    oneOf(storageRecoveryManager).registrationCompleted(
                            with(any(DataSetStorageAlgorithmRunner.class)));

                    one(openBisService).setStorageConfirmed(Collections.singletonList(DATA_SET_CODE));
                }
            });

        handler.handle(markerFile);

        JythonHookTestTool hookTool =
                JythonHookTestTool.createFromWorkingDirectory(workingDirectory);

        hookTool.assertLogged("pre_metadata_registration");
        hookTool.assertLogged("post_metadata_registration");
        hookTool.assertLogged("post_storage");

        hookTool.assertNoMoreMessages();

        context.assertIsSatisfied();
    }

    @Test
    public void testQuerying()
    {
        setUpHomeDataBaseExpectations();
        Properties threadProperties =
                createThreadPropertiesRelativeToScriptsFolder("query-interface-test.py");
        createHandler(threadProperties, false, true);

        createData();

        setUpQueryExpectations();

        handler.handle(markerFile);

        assertEquals(0, MockStorageProcessor.instance.incomingDirs.size());
        assertEquals(0, MockStorageProcessor.instance.calledCommitCount);

        context.assertIsSatisfied();
    }

    @Test
    public void testDynamicQueryCommitFail()
    {

        setUpHomeDataBaseExpectations();
        Properties threadProperties =
                createThreadPropertiesRelativeToScriptsFolder("dynamic-query-failure-test.py");
        createHandler(threadProperties, false, true);

        createData();

        setUpDynamicQueryExpectations();

        handler.handle(markerFile);

        assertEquals(0, MockStorageProcessor.instance.incomingDirs.size());
        assertEquals(0, MockStorageProcessor.instance.calledCommitCount);

        JythonHookTestTool jythonHooks =
                JythonHookTestTool.createFromWorkingDirectory(workingDirectory);

        jythonHooks.assertLogged("did_encounter_secondary_transaction_errors 1");
        jythonHooks.assertNoMoreMessages();

        context.assertIsSatisfied();
    }

    private Properties createThreadProperties(String scriptPath)
    {
        Properties threadProperties = new Properties();
        threadProperties.put(ThreadParameters.INCOMING_DIR, "incoming");
        threadProperties.put(ThreadParameters.INCOMING_DATA_COMPLETENESS_CONDITION,
                ThreadParameters.INCOMING_DATA_COMPLETENESS_CONDITION_MARKER_FILE);
        threadProperties.put(ThreadParameters.DELETE_UNIDENTIFIED_KEY, "false");
        threadProperties.put(IStorageProcessorTransactional.STORAGE_PROCESSOR_KEY,
                MockStorageProcessor.class.getName());
        threadProperties.put(JythonTopLevelDataSetHandler.SCRIPT_PATH_KEY, scriptPath);
        return threadProperties;
    }

    @Override
    protected String getRegistrationScriptsFolderPath()
    {
        return SCRIPTS_FOLDER;
    }

    @Test
    public void testNoScriptPath()
    {
        setUpHomeDataBaseExpectations();

        // omit the script path
        Properties threadProperties = new Properties();
        threadProperties.put(ThreadParameters.INCOMING_DIR, "incoming");
        threadProperties.put(ThreadParameters.INCOMING_DATA_COMPLETENESS_CONDITION,
                ThreadParameters.INCOMING_DATA_COMPLETENESS_CONDITION_MARKER_FILE);
        threadProperties.put(ThreadParameters.DELETE_UNIDENTIFIED_KEY, "false");
        threadProperties.put(IStorageProcessorTransactional.STORAGE_PROCESSOR_KEY,
                MockStorageProcessor.class.getName());

        try
        {
            createHandler(threadProperties, false);
            fail("Should not be able to create the handler without specifiying a script");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals(
                    "Given key 'script-path' not found in properties '[delete-unidentified, storage-processor, incoming-data-completeness-condition, incoming-dir]'",
                    ex.getMessage());
        }
        context.assertIsSatisfied();
    }

    private void createHandler(Properties threadProperties, final boolean registrationShouldFail)
    {
        createHandler(threadProperties, registrationShouldFail, false);
    }

    private void setUpSearchExpectations()
    {
        context.checking(new Expectations()
            {
                {
                    ProjectIdentifier projectIdentifier =
                            new ProjectIdentifierFactory("/SPACE/PROJECT").createIdentifier();
                    oneOf(openBisService).listExperiments(projectIdentifier);

                    Experiment experiment = new Experiment();
                    experiment.setIdentifier("/SPACE/PROJECT/EXP-CODE");
                    experiment.setCode("EXP-CODE");
                    Person registrator = new Person();
                    registrator.setEmail("email@email.com");
                    experiment.setRegistrator(registrator);
                    will(returnValue(Arrays.asList(experiment)));

                    SearchCriteria searchCriteria = createTestSearchCriteria("DATA_SET_TYPE");
                    oneOf(openBisService).searchForDataSets(searchCriteria);
                    will(returnValue(Collections.EMPTY_LIST));

                    searchCriteria = createTestSearchCriteria("SAMPLE_TYPE");
                    oneOf(openBisService).searchForSamples(searchCriteria);

                    SampleBuilder sampleBuilder = new SampleBuilder();
                    sampleBuilder.id(SEARCH_RETURNED_SAMPLE_DB_ID);
                    sampleBuilder.modificationDate(new Date());
                    sampleBuilder.identifier("/SPACE/SAMPLE-CODE");
                    sampleBuilder.experiment(experiment);
                    sampleBuilder.permID("SAMPLE_PERM_ID");
                    sampleBuilder.code("SAMPLE-CODE");
                    sampleBuilder.type("SAMPLE_TYPE");

                    will(returnValue(Arrays.asList(sampleBuilder.getSample())));
                }
            });
    }

    private void setUpQueryExpectations()
    {
        context.checking(new Expectations()
            {
                {
                    oneOf(dataSourceQueryService).select("path-info-db",
                            "SELECT * from data_set_files WHERE parent_id is NULL");
                    Object[] args = { 155555 };
                    will(returnValue(new MockDataSet<Map<String, Object>>()));
                    oneOf(dataSourceQueryService).select("path-info-db",
                            "SELECT * from data_set_files WHERE parent_id = ?1", args);
                    will(returnValue(new MockDataSet<Map<String, Object>>()));
                }
            });
    }

    private void setUpDynamicQueryExpectations()
    {
        context.checking(new Expectations()
            {
                {
                    oneOf(dynamicTransactionQuery)
                            .select(with("SELECT * from data_set_files WHERE parent_id is NULL"),
                                    with(any((new Object[0]).getClass())));
                    will(returnValue(new MockDataSet<Map<String, Object>>()));

                    one(openBisService).drawANewUniqueID();
                    will(returnValue(Long.valueOf(1)));
                    oneOf(openBisService)
                            .performEntityOperations(
                                    with(any(ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails.class)));
                    will(returnValue(new AtomicEntityOperationResult()));
                }
            });
    }

    protected SearchCriteria createTestSearchCriteria(String typeString)
    {
        SearchCriteria sc = new SearchCriteria();
        sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE, typeString));
        sc.addMatchClause(MatchClause.createPropertyMatch("PROP", "VALUE"));
        return sc;
    }

    private void checkInitialDirAfterRegistration(String expectedBehavior)
    {
        if (expectedBehavior.equals("deleted"))
        {
            assertFalse("Incoming directory should have been deleted", incomingDataSetFile.exists());
        } else if (expectedBehavior.equals("empty"))
        {
            assertTrue("Incoming directory should not be deleted.", incomingDataSetFile.exists());
            assertEquals("Incomind directory should be empty", 0,
                    incomingDataSetFile.listFiles().length);
        } else if (expectedBehavior.equals("content"))
        {
            assertTrue("Incoming directory should not be deleted.", incomingDataSetFile.exists());
            assertNotSame("The incoming directory is not expected to be empty", 0,
                    incomingDataSetFile.listFiles().length);
        } else if (expectedBehavior.equals("untouched_two_datasets"))
        {
            assertEquals("Staging directory is supposed to be empty", "[]",
                    Arrays.asList(stagingDirectory.list()).toString());
            assertEquals(
                    "The content of the incoming dataset 1 has changed",
                    "hello world1",
                    FileUtilities.loadToString(
                            new File(workingDirectory, "data_set/sub_data_set_1/read1.me")).trim());
            assertEquals(
                    "The content of the incoming dataset 2 has changed",
                    "hello world2",
                    FileUtilities.loadToString(
                            new File(workingDirectory, "data_set/sub_data_set_2/read2.me")).trim());
        } else
        {
            fail("Unknown behavior '" + expectedBehavior + "'");
        }
    }

    private CustomAction checkPrecommitDirIsEmpty()
    {
        return new CustomAction("foo")
            {
                @Override
                public Object invoke(Invocation invocation) throws Throwable
                {
                    assertEquals("[]",
                            Arrays.asList(handler.getGlobalState().getPreCommitDir().list())
                                    .toString());
                    return null;
                }
            };
    }

    private CustomAction checkPrecommitDirIsNotEmpty()
    {
        return new CustomAction("foo")
            {
                @Override
                public Object invoke(Invocation invocation) throws Throwable
                {
                    assertNotSame(0, handler.getGlobalState().getPreCommitDir().list().length);
                    return null;
                }
            };
    }

}

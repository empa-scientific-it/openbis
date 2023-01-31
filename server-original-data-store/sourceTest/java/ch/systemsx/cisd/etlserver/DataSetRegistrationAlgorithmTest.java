/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.locks.Lock;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.action.IDelegatedActionWithResult;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.common.mail.From;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.etlserver.DataSetRegistrationAlgorithm.DataSetRegistrationAlgorithmState;
import ch.systemsx.cisd.etlserver.DataSetRegistrationAlgorithm.IDataSetInApplicationServerRegistrator;
import ch.systemsx.cisd.etlserver.DataSetRegistrationAlgorithm.IRollbackDelegate;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional.IStorageProcessorTransaction;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional.StorageProcessorTransactionParameters;
import ch.systemsx.cisd.etlserver.validation.IDataSetValidator;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LocatorType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;

/**
 * Tests the data set registration algorithm.
 * <p>
 * To debug: add a rollback expectation and print the contents of the exceptionMatcher.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetRegistrationAlgorithmTest extends AbstractFileSystemTestCase
{
    private static final int SPEED_HINT =
            (ch.systemsx.cisd.openbis.generic.shared.Constants.DEFAULT_SPEED_HINT + ch.systemsx.cisd.openbis.generic.shared.Constants.MAX_SPEED) / 2;

    private static final String DATA_STORE_CODE = "data-store";

    private static final String DATA_SET_CODE = "data-set-code";

    private static final LocatorType LOCATOR_TYPE = new LocatorType("L1");

    private static final DataSetType DATA_SET_TYPE = new DataSetType("O1");

    private static final FileFormatType FILE_FORMAT_TYPE = new FileFormatType("FF1");

    private static final String EXAMPLE_PROCESSOR_ID = "DATA_ACQUISITION";

    private DataSetRegistrationAlgorithm registrationAlgorithm;

    private Mockery context;

    private IEncapsulatedOpenBISService openBisService;

    private IRollbackDelegate rollbackDelegate;

    private IDataSetInApplicationServerRegistrator appServerRegistrator;

    private IDelegatedActionWithResult<Boolean> cleanAfterwardsAction;

    private IPreRegistrationAction preRegistrationAction;

    private IPostRegistrationAction postRegistrationAction;

    private IDataStoreStrategy dataStoreStrategy;

    private ITypeExtractor typeExtractor;

    private IStorageProcessorTransactional storageProcessor;

    private IStorageProcessorTransaction transaction;

    private IFileOperations fileOperations;

    private IDataSetValidator dataSetValidator;

    private IMailClient mailClient;

    private Lock registrationLock;

    private RecordingMatcher<Throwable> exceptionMatcher;

    private DataSetInformation dataSetInformation;

    private File incomingDataSetFile;

    private File storedDataSetFile;

    @SuppressWarnings("unchecked")
    @Override
    @BeforeMethod
    public void setUp() throws IOException
    {
        super.setUp();
        context = new Mockery();
        openBisService = context.mock(IEncapsulatedOpenBISService.class);
        rollbackDelegate = context.mock(IRollbackDelegate.class);
        appServerRegistrator = context.mock(IDataSetInApplicationServerRegistrator.class);
        cleanAfterwardsAction = context.mock(IDelegatedActionWithResult.class);
        preRegistrationAction = context.mock(IPreRegistrationAction.class);
        postRegistrationAction = context.mock(IPostRegistrationAction.class);

        dataStoreStrategy = context.mock(IDataStoreStrategy.class);
        typeExtractor = context.mock(ITypeExtractor.class);
        storageProcessor = context.mock(IStorageProcessorTransactional.class);
        transaction = context.mock(IStorageProcessorTransaction.class);
        fileOperations = FileOperations.getInstance();
        dataSetValidator = context.mock(IDataSetValidator.class);
        mailClient = context.mock(IMailClient.class);
        registrationLock = context.mock(Lock.class);
        exceptionMatcher = new RecordingMatcher<Throwable>();

        dataSetInformation = new DataSetInformation();
        dataSetInformation
                .setShareId(ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID);
        dataSetInformation.setSpeedHint(SPEED_HINT);
    }

    @Test
    public void testSuccessfulRegistrationWithoutNotification() throws Throwable
    {
        incomingDataSetFile = createDirectory("data_set");
        storedDataSetFile = createDirectory("stored_data_set");

        FileUtilities.writeToFile(new File(incomingDataSetFile, "read.me"), "hello world");

        setUpOpenBisExpectations(true);
        setUpTypeExtractorExpectations();
        setUpStorageProcessorExpectations(true);
        setUpDataSetValidatorExpectations();
        setUpDataStoreStrategyExpectations(DataStoreStrategyKey.IDENTIFIED);
        setUpPreAndPostRegistrationExpectations();
        setUpLockExpectations();
        RecordingMatcher<NewExternalData> matcher = setUpDataSetRegistratorExpectations();
        setUpCleanAfterwardsExpectations();

        createAlgorithmAndState(false, false);

        new DataSetRegistrationAlgorithmRunner(registrationAlgorithm).runAlgorithm();

        assertEquals(SPEED_HINT, matcher.recordedObject().getSpeedHint());
        context.assertIsSatisfied();
    }

    @Test
    public void testSuccessfulRegistrationWithNotification() throws Throwable
    {
        incomingDataSetFile = createDirectory("data_set");
        storedDataSetFile = createDirectory("stored_data_set");

        FileUtilities.writeToFile(new File(incomingDataSetFile, "read.me"), "hello world");

        setUpOpenBisExpectations(true);
        setUpTypeExtractorExpectations();
        setUpStorageProcessorExpectations(true);
        setUpDataSetValidatorExpectations();
        setUpDataStoreStrategyExpectations(DataStoreStrategyKey.IDENTIFIED);
        setUpPreAndPostRegistrationExpectations();
        setUpLockExpectations();
        setUpDataSetRegistratorExpectations();
        setUpMailClientExpectation();
        setUpCleanAfterwardsExpectations();

        createAlgorithmAndState(false, true);

        new DataSetRegistrationAlgorithmRunner(registrationAlgorithm).runAlgorithm();

        context.assertIsSatisfied();
    }

    @Test
    public void testUnidentifiedDataSetRegistration() throws Throwable
    {
        incomingDataSetFile = createDirectory("data_set");
        storedDataSetFile = createDirectory("stored_data_set");

        FileUtilities.writeToFile(new File(incomingDataSetFile, "read.me"), "hello world");

        setUpOpenBisExpectations(false);

        context.checking(new Expectations()
            {
                {
                    one(typeExtractor).getDataSetType(incomingDataSetFile);
                    will(returnValue(DATA_SET_TYPE));

                    one(storageProcessor).getStoreRootDirectory();
                    will(returnValue(workingDirectory));
                }
            });

        setUpDataStoreStrategyExpectations(DataStoreStrategyKey.UNIDENTIFIED);

        createAlgorithmAndState(false, true);

        new DataSetRegistrationAlgorithmRunner(registrationAlgorithm).runAlgorithm();

        context.assertIsSatisfied();
    }

    @Test
    public void testFailedDataSetRegistration() throws Throwable
    {
        incomingDataSetFile = createDirectory("data_set");
        storedDataSetFile = createDirectory("stored_data_set");

        FileUtilities.writeToFile(new File(incomingDataSetFile, "read.me"), "hello world");

        setUpOpenBisExpectations(false);
        setUpTypeExtractorExpectations();
        setUpStorageProcessorExpectations(false);
        setUpDataSetValidatorExpectations();
        setUpDataStoreStrategyExpectations(DataStoreStrategyKey.IDENTIFIED);
        setUpLockExpectations();

        context.checking(new Expectations()
            {
                {
                    one(preRegistrationAction).execute(DATA_SET_CODE,
                            incomingDataSetFile.getAbsolutePath());

                    one(appServerRegistrator).registerDataSetInApplicationServer(
                            with(any(NewExternalData.class)));
                    will(throwException(new UserFailureException("Could not register")));

                }
            });

        setUpRollbackExpectations();

        createAlgorithmAndState(false, false);

        new DataSetRegistrationAlgorithmRunner(registrationAlgorithm).runAlgorithm();

        context.assertIsSatisfied();
    }

    /**
     * Used for debugging.
     */
    @SuppressWarnings("unused")
    private void printExceptions()
    {
        for (Throwable ex : exceptionMatcher.getRecordedObjects())
        {
            ex.printStackTrace();
        }
    }

    private void setUpOpenBisExpectations(boolean registrationSuccesful)
    {
        context.checking(new Expectations()
            {
                {
                    one(openBisService).createPermId();
                    will(returnValue(DATA_SET_CODE));
                }
            });

        if (registrationSuccesful)
        {
            context.checking(new Expectations()
                {
                    {
                        one(openBisService).setStorageConfirmed(Collections.singletonList(DATA_SET_CODE));
                    }
                });
        }
    }

    private void setUpTypeExtractorExpectations()
    {
        context.checking(new Expectations()
            {
                {
                    one(typeExtractor).getLocatorType(incomingDataSetFile);
                    will(returnValue(LOCATOR_TYPE));

                    one(typeExtractor).getFileFormatType(incomingDataSetFile);
                    will(returnValue(FILE_FORMAT_TYPE));

                    one(typeExtractor).getProcessorType(incomingDataSetFile);
                    will(returnValue(EXAMPLE_PROCESSOR_ID));

                    one(typeExtractor).isMeasuredData(incomingDataSetFile);
                    will(returnValue(true));

                    one(typeExtractor).getDataSetType(incomingDataSetFile);
                    will(returnValue(DATA_SET_TYPE));

                    one(typeExtractor).getDataSetType(with(any(File.class)));
                    will(returnValue(DATA_SET_TYPE));
                }
            });
    }

    private void setUpStorageProcessorExpectations(final boolean shouldSucceed)
    {
        final Action storeDataAction = new CustomAction("StoreData")
            {

                @Override
                public Object invoke(Invocation invocation) throws Throwable
                {
                    fileOperations.move(incomingDataSetFile, storedDataSetFile);
                    return storedDataSetFile;
                }
            };

        context.checking(new Expectations()
            {
                {
                    one(storageProcessor).getStoreRootDirectory();
                    will(returnValue(workingDirectory));

                    one(storageProcessor).createTransaction(
                            with(any(StorageProcessorTransactionParameters.class)));
                    will(returnValue(transaction));

                    one(transaction).storeData(typeExtractor, mailClient, incomingDataSetFile);

                    one(transaction).getStoredDataDirectory();
                    will(storeDataAction);

                    one(storageProcessor).getStorageFormat();
                    will(returnValue(StorageFormat.PROPRIETARY));

                    if (shouldSucceed)
                    {
                        one(transaction).commit();
                    }
                }
            });
    }

    private void setUpDataStoreStrategyExpectations(final DataStoreStrategyKey key)
    {
        final boolean isIdentified = DataStoreStrategyKey.IDENTIFIED == key;
        if (isIdentified)
        {
            Experiment experiment = new Experiment();
            experiment.setIdentifier("/SPACE/PROJECT/EXP-CODE");
            experiment.setCode("EXP-CODE");
            Person registrator = new Person();
            registrator.setEmail("email@email.com");
            experiment.setRegistrator(registrator);
            dataSetInformation.setExperiment(experiment);
            dataSetInformation.setExperimentIdentifier(new ExperimentIdentifierFactory(experiment
                    .getIdentifier()).createIdentifier());
        }
        context.checking(new Expectations()
            {
                {
                    one(dataStoreStrategy).getBaseDirectory(with(workingDirectory),
                            with(any(DataSetInformation.class)), with(DATA_SET_TYPE));
                    will(returnValue(workingDirectory));

                    one(dataStoreStrategy).getKey();
                    will(returnValue(key));

                    if (false == isIdentified)
                    {
                        one(dataStoreStrategy).getTargetPath(workingDirectory, incomingDataSetFile);
                        will(returnValue(incomingDataSetFile));
                    }

                }
            });
    }

    private void setUpDataSetValidatorExpectations()
    {
        context.checking(new Expectations()
            {
                {
                    one(dataSetValidator).assertValidDataSet(DATA_SET_TYPE, incomingDataSetFile);
                }
            });
    }

    private void setUpPreAndPostRegistrationExpectations()
    {
        context.checking(new Expectations()
            {
                {
                    one(preRegistrationAction).execute(DATA_SET_CODE,
                            incomingDataSetFile.getAbsolutePath());

                    one(postRegistrationAction).execute(with(DATA_SET_CODE),
                            with(any(String.class)));
                }
            });
    }

    private void setUpLockExpectations()
    {
        context.checking(new Expectations()
            {
                {
                    one(registrationLock).lock();
                    one(registrationLock).unlock();
                }
            });
    }

    private RecordingMatcher<NewExternalData> setUpDataSetRegistratorExpectations()
            throws Throwable
    {
        final RecordingMatcher<NewExternalData> recordingMatcher =
                new RecordingMatcher<NewExternalData>();
        context.checking(new Expectations()
            {
                {
                    one(appServerRegistrator).registerDataSetInApplicationServer(
                            with(recordingMatcher));
                }
            });
        return recordingMatcher;
    }

    private void setUpMailClientExpectation()
    {
        context.checking(new Expectations()
            {
                {
                    one(mailClient).sendMessage(with(any(String.class)), with(any(String.class)),
                            with(aNull(String.class)), with(aNull(From.class)),
                            with(any(String[].class)));
                }
            });
    }

    private void setUpCleanAfterwardsExpectations()
    {
        context.checking(new Expectations()
            {
                {
                    exactly(2).of(cleanAfterwardsAction).execute(true);
                    will(returnValue(Boolean.TRUE));
                }
            });
    }

    private void setUpRollbackExpectations()
    {
        context.checking(new Expectations()
            {
                {
                    one(rollbackDelegate).rollback(with(any(DataSetRegistrationAlgorithm.class)),
                            with(exceptionMatcher));
                }
            });
    }

    private void createAlgorithmAndState(boolean shouldDeleteUnidentified,
            boolean shouldNotifySuccessfulRegistration)
    {
        String dataStoreCode = DATA_STORE_CODE;
        DataSetRegistrationAlgorithmState state =
                new DataSetRegistrationAlgorithmState(incomingDataSetFile, openBisService,
                        cleanAfterwardsAction, preRegistrationAction, postRegistrationAction,
                        dataSetInformation, dataStoreStrategy, typeExtractor, storageProcessor,
                        fileOperations, dataSetValidator, mailClient, shouldDeleteUnidentified,
                        registrationLock, dataStoreCode, shouldNotifySuccessfulRegistration);
        registrationAlgorithm =
                new DataSetRegistrationAlgorithm(state, rollbackDelegate, appServerRegistrator);
    }

    private File createDirectory(final String directoryName)
    {
        final File file = new File(workingDirectory, directoryName);
        file.mkdir();
        return file;
    }
}

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
package ch.systemsx.cisd.etlserver.registrator.api.v1.impl;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.hamcrest.core.IsNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.QueueingPathRemoverService;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.properties.ExtendedProperties;
import ch.systemsx.cisd.etlserver.DynamicTransactionQueryFactory;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional;
import ch.systemsx.cisd.etlserver.ITypeExtractor;
import ch.systemsx.cisd.etlserver.ThreadParameters;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.DataSetFile;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationDetails;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationPreStagingBehavior;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSet;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IMetaproject;
import ch.systemsx.cisd.etlserver.registrator.monitor.DssRegistrationHealthMonitor;
import ch.systemsx.cisd.etlserver.registrator.recovery.DataSetStorageRecoveryManager;
import ch.systemsx.cisd.etlserver.registrator.v1.AbstractOmniscientTopLevelDataSetRegistrator;
import ch.systemsx.cisd.etlserver.registrator.v1.DataSetRegistrationService;
import ch.systemsx.cisd.etlserver.registrator.v1.IDataSetRegistrationDetailsFactory;
import ch.systemsx.cisd.etlserver.validation.IDataSetValidator;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IExperimentImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationResult;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetRegistrationTransactionTest extends AbstractFileSystemTestCase
{
    private static final String EXPERIMENT_IDENTIFIER = "/SPACE/PROJECT/EXP-CODE";

    private static final String DATA_SET_CODE = "data-set-code".toUpperCase();

    private static final String DATABASE_INSTANCE_UUID = "db-uuid";

    private static final DataSetType DATA_SET_TYPE = new DataSetType("O1");

    private DataSetRegistrationTransaction<DataSetInformation> tr;

    private File stagingDirectory;

    private Mockery context;

    private IEncapsulatedOpenBISService openBisService;

    private IMailClient mailClient;

    private IDataSetValidator dataSetValidator;

    private BufferedAppender logAppender;

    private TestingDataSetHandler handler;

    private File srcFile;

    private DataSetRegistrationService<DataSetInformation> service;

    @BeforeTest
    public void init()
    {
        QueueingPathRemoverService.start();
    }

    @AfterTest
    public void finish()
    {
        QueueingPathRemoverService.stop();
    }

    @BeforeMethod
    @Override
    public void setUp() throws IOException
    {
        super.setUp();

        srcFile = new File(workingDirectory, "read.me");
        fillContentsOfSource();

        context = new Mockery();
        openBisService = context.mock(IEncapsulatedOpenBISService.class);
        dataSetValidator = context.mock(IDataSetValidator.class);
        mailClient = context.mock(IMailClient.class);

        logAppender = LogRecordingUtils.createRecorder();

        stagingDirectory = new File(workingDirectory, "staging");
        stagingDirectory.mkdirs();

        DssRegistrationHealthMonitor.getInstance(openBisService, workingDirectory);

        context.checking(new Expectations()
            {
                {
                    ignoring(openBisService).heartbeat();
                }
            });
    }

    @Test
    public void testCommit()
    {
        setUpOpenBisExpectations(true);
        setUpDataSetValidatorExpectations();
        createTransaction();

        IDataSet newDataSet = tr.createNewDataSet();
        String dst = tr.moveFile(srcFile.getAbsolutePath(), newDataSet);
        IExperimentImmutable experiment = tr.getExperiment(EXPERIMENT_IDENTIFIER);
        newDataSet.setExperiment(experiment);
        newDataSet.setDataSetType(DATA_SET_TYPE.getCode());

        checkContentsOfFile(new File(dst));

        File[] rollbackQueueFiles = listRollbackQueueFiles();
        assertEquals(1, rollbackQueueFiles.length);

        tr.commit();

        System.out.println(logAppender.getLogContent());

        assertTrue(logAppender.getLogContent().length() > 0);
        // Skip this for the moment
        // new LogMatcher(logAppender,
        // "Identified that database knows experiment '/SPACE/PROJECT/EXP-CODE'.*",
        // "Start storing data set for experiment '/SPACE/PROJECT/EXP-CODE'.*",
        // "Finished storing data set for experiment '/SPACE/PROJECT/EXP-CODE', took .*",
        // "Successfully registered data set: .+").assertMatches();

        rollbackQueueFiles = listRollbackQueueFiles();
        assertEquals(0, rollbackQueueFiles.length);

        context.assertIsSatisfied();
    }

    @Test
    public void testCommitSpecifyingDataSetCode()
    {
        setUpOpenBisExpectations(true, false);
        setUpDataSetValidatorExpectations("my-data-set-code");
        createTransaction();

        IDataSet newDataSet = tr.createNewDataSet("UNKNOWN", "my-data-set-code");
        String dst = tr.moveFile(srcFile.getAbsolutePath(), newDataSet);
        IExperimentImmutable experiment = tr.getExperiment(EXPERIMENT_IDENTIFIER);
        newDataSet.setExperiment(experiment);
        newDataSet.setDataSetType(DATA_SET_TYPE.getCode());

        checkContentsOfFile(new File(dst));

        File[] rollbackQueueFiles = listRollbackQueueFiles();
        assertEquals(1, rollbackQueueFiles.length);

        tr.commit();

        System.out.println(logAppender.getLogContent());

        assertTrue(logAppender.getLogContent().length() > 0);
        // Skip this for the moment
        // new LogMatcher(logAppender,
        // "Identified that database knows experiment '/SPACE/PROJECT/EXP-CODE'.*",
        // "Start storing data set for experiment '/SPACE/PROJECT/EXP-CODE'.*",
        // "Finished storing data set for experiment '/SPACE/PROJECT/EXP-CODE', took .*",
        // "Successfully registered data set: .+").assertMatches();

        rollbackQueueFiles = listRollbackQueueFiles();
        assertEquals(0, rollbackQueueFiles.length);

        context.assertIsSatisfied();
    }

    @Test
    public void testCommitContainerDataSet()
    {
        setUpOpenBisExpectations(true);
        setUpDataSetValidatorExpectationsForNullFiles();
        createTransaction();

        IDataSet newDataSet = tr.createNewDataSet();
        newDataSet.setContainedDataSetCodes(Arrays.asList("container-1"));
        IExperimentImmutable experiment = tr.getExperiment(EXPERIMENT_IDENTIFIER);
        newDataSet.setExperiment(experiment);
        newDataSet.setDataSetType(DATA_SET_TYPE.getCode());

        File[] rollbackQueueFiles = listRollbackQueueFiles();
        assertEquals(1, rollbackQueueFiles.length);

        tr.commit();

        assertTrue(logAppender.getLogContent().length() > 0);
        // Skip this for the moment
        // new LogMatcher(logAppender,
        // "Identified that database knows experiment '/SPACE/PROJECT/EXP-CODE'.*",
        // "Start storing data set for experiment '/SPACE/PROJECT/EXP-CODE'.*",
        // "Finished storing data set for experiment '/SPACE/PROJECT/EXP-CODE', took .*",
        // "Successfully registered data set: .+").assertMatches();

        rollbackQueueFiles = listRollbackQueueFiles();
        assertEquals(0, rollbackQueueFiles.length);

        context.assertIsSatisfied();
    }

    @Test(expectedExceptions = { IllegalArgumentException.class })
    public void testCommitContainerDataSetWithFiles()
    {
        setUpOpenBisExpectations(true);
        setUpDataSetValidatorExpectations();
        createTransaction();

        IDataSet newDataSet = tr.createNewDataSet();
        newDataSet.setContainedDataSetCodes(Arrays.asList("container-1"));
        String dst = tr.moveFile(srcFile.getAbsolutePath(), newDataSet);
        IExperimentImmutable experiment = tr.getExperiment(EXPERIMENT_IDENTIFIER);
        newDataSet.setExperiment(experiment);
        newDataSet.setDataSetType(DATA_SET_TYPE.getCode());

        checkContentsOfFile(new File(dst));

        File[] rollbackQueueFiles = listRollbackQueueFiles();
        assertEquals(1, rollbackQueueFiles.length);

        tr.commit();

        assertTrue(logAppender.getLogContent().length() > 0);
        // Skip this for the moment
        // new LogMatcher(logAppender,
        // "Identified that database knows experiment '/SPACE/PROJECT/EXP-CODE'.*",
        // "Start storing data set for experiment '/SPACE/PROJECT/EXP-CODE'.*",
        // "Finished storing data set for experiment '/SPACE/PROJECT/EXP-CODE', took .*",
        // "Successfully registered data set: .+").assertMatches();

        rollbackQueueFiles = listRollbackQueueFiles();
        assertEquals(0, rollbackQueueFiles.length);

        context.assertIsSatisfied();
    }

    @Test
    public void testRollback()
    {
        setUpOpenBisExpectations(false);
        createTransaction();

        IDataSet newDataSet = tr.createNewDataSet();
        String dst = tr.moveFile(srcFile.getAbsolutePath(), newDataSet);

        checkContentsOfFile(new File(dst));

        tr.rollback();

        checkContentsOfFile(srcFile);

        context.assertIsSatisfied();
    }

    @Test
    public void testRollbackWhereMoveCreatesIntermediateDirectories()
    {
        setUpOpenBisExpectations(false);
        createTransaction();

        IDataSet newDataSet = tr.createNewDataSet();
        String dst = tr.moveFile(srcFile.getAbsolutePath(), newDataSet, "original/foo");

        File dstFile = new File(dst);
        File parentDir = dstFile.getParentFile();

        checkContentsOfFile(dstFile);
        assertTrue("The parent directory should exist", parentDir.exists());

        tr.rollback();

        checkContentsOfFile(srcFile);
        assertFalse("The parent directory should have been deleted", parentDir.exists());

        context.assertIsSatisfied();
    }

    @Test
    public void testAbortAndRollback()
    {
        setUpOpenBisExpectations(false);
        createTransaction();

        IDataSet newDataSet = tr.createNewDataSet();
        String dst = tr.moveFile(srcFile.getAbsolutePath(), newDataSet);

        checkContentsOfFile(new File(dst));

        File[] rollbackQueueFiles = listRollbackQueueFiles();
        assertEquals(1, rollbackQueueFiles.length);

        DataSetRegistrationTransaction.rollbackDeadTransactions(workingDirectory);

        rollbackQueueFiles = listRollbackQueueFiles();
        assertEquals(0, rollbackQueueFiles.length);

        checkContentsOfFile(srcFile);

        context.assertIsSatisfied();
    }

    @Test
    public void testDoubleRollbackNormal()
    {
        setUpOpenBisExpectations(false);
        createTransaction();

        IDataSet newDataSet = tr.createNewDataSet();
        String dst = tr.moveFile(srcFile.getAbsolutePath(), newDataSet);

        checkContentsOfFile(new File(dst));

        tr.rollback();
        tr.rollback();

        checkContentsOfFile(srcFile);

        context.assertIsSatisfied();
    }

    @Test
    public void testDoubleRollbackFromDuplicatedQueue() throws IOException
    {
        setUpOpenBisExpectations(false);
        createTransaction();

        // Move a file into the new data set and check that it is valid
        IDataSet newDataSet = tr.createNewDataSet();
        String dst = tr.moveFile(srcFile.getAbsolutePath(), newDataSet);
        checkContentsOfFile(new File(dst));

        // Duplicate the rollback queue so we can simulate a double rollback
        File[] rollbackQueueFiles = listRollbackQueueFiles();
        assertEquals(1, rollbackQueueFiles.length);
        for (File rollbackQueueFile : rollbackQueueFiles)
        {
            File destFile = new File(workingDirectory, "dup-" + rollbackQueueFile.getName());
            FileUtils.copyFile(rollbackQueueFile, destFile);
        }
        rollbackQueueFiles = listRollbackQueueFiles();
        assertEquals(2, rollbackQueueFiles.length);

        // Do a "normal" rollback
        tr.rollback();
        rollbackQueueFiles = listRollbackQueueFiles();
        assertEquals(1, rollbackQueueFiles.length);

        // Rollback again using the queue we duplicated
        DataSetRegistrationTransaction.rollbackDeadTransactions(workingDirectory);
        rollbackQueueFiles = listRollbackQueueFiles();
        assertEquals(0, rollbackQueueFiles.length);

        // The source file should be correct and now exceptions should have been thrown
        checkContentsOfFile(srcFile);

        context.assertIsSatisfied();
    }

    @Test
    public void testGetNonExistingMetaproject()
    {
        setUpHomeDataBaseExpectations();
        createTransaction();
        context.checking(new Expectations()
            {
                {
                    one(openBisService).tryGetMetaproject("ABC", "test");
                }
            });

        IMetaproject metaproject = tr.getMetaproject("ABC", "test");

        assertEquals(null, metaproject);
        context.assertIsSatisfied();
    }

    @Test
    public void testGetExistingMetaproject()
    {
        setUpHomeDataBaseExpectations();
        createTransaction();
        context.checking(new Expectations()
            {
                {
                    one(openBisService).tryGetMetaproject("ABC", "test");
                    Metaproject metaproject = new Metaproject();
                    metaproject.setId(1L);
                    metaproject.setName("ABC");
                    metaproject.setOwnerId("test");
                    will(returnValue(metaproject));
                }
            });

        IMetaproject metaproject = tr.getMetaproject("ABC", "test");

        assertEquals("ABC", metaproject.getName());
        assertEquals("test", metaproject.getOwnerId());
        context.assertIsSatisfied();
    }

    @Test
    public void testGetSameMetaprojectTwice()
    {
        setUpHomeDataBaseExpectations();
        createTransaction();
        context.checking(new Expectations()
            {
                {
                    allowing(openBisService).tryGetMetaproject("ABC", "test");
                    Metaproject metaproject = new Metaproject();
                    metaproject.setId(1L);
                    metaproject.setName("ABC");
                    metaproject.setOwnerId("test");
                    will(returnValue(metaproject));
                }
            });

        IMetaproject metaproject1 = tr.getMetaproject("ABC", "test");
        IMetaproject metaproject2 = tr.getMetaproject("ABC", "test");

        assertEquals("ABC", metaproject1.getName());
        assertEquals("test", metaproject1.getOwnerId());
        assertSame(metaproject1, metaproject2);
        context.assertIsSatisfied();
    }

    private File[] listRollbackQueueFiles()
    {
        File[] rollbackQueueFiles = workingDirectory.listFiles(new FilenameFilter()
            {
                @Override
                public boolean accept(File dir, String name)
                {
                    final String ROLLBACK_QUEUE1_FILE_NAME_SUFFIX = "rollBackQueue";

                    return name.endsWith(ROLLBACK_QUEUE1_FILE_NAME_SUFFIX);
                }

            });
        return rollbackQueueFiles;
    }

    private void createHandler()
    {
        Properties threadProperties = createThreadProperties();

        createHandler(threadProperties, false);
    }

    private Properties createThreadProperties()
    {
        Properties threadProperties = new Properties();
        threadProperties.put(ThreadParameters.INCOMING_DIR, "incoming");
        threadProperties.put(ThreadParameters.INCOMING_DATA_COMPLETENESS_CONDITION,
                ThreadParameters.INCOMING_DATA_COMPLETENESS_CONDITION_MARKER_FILE);
        threadProperties.put(ThreadParameters.DELETE_UNIDENTIFIED_KEY, "false");
        threadProperties.put(IStorageProcessorTransactional.STORAGE_PROCESSOR_KEY,
                MockStorageProcessor.class.getName());
        return threadProperties;
    }

    private void createHandler(Properties threadProperties, final boolean registrationShouldFail)
    {
        TopLevelDataSetRegistratorGlobalState globalState = createGlobalState(threadProperties);

        handler = new TestingDataSetHandler(globalState);
        service = handler.createNewService(new DataSetFile(workingDirectory));
    }

    private TopLevelDataSetRegistratorGlobalState createGlobalState(Properties threadProperties)
    {
        ThreadParameters threadParameters =
                new ThreadParameters(threadProperties, "jython-handler-test");

        TopLevelDataSetRegistratorGlobalState globalState =
                new TopLevelDataSetRegistratorGlobalState("dss",
                        ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID,
                        workingDirectory, workingDirectory, workingDirectory, workingDirectory,
                        workingDirectory, openBisService, mailClient, dataSetValidator, null,
                        new DynamicTransactionQueryFactory(), true, threadParameters,
                        new DataSetStorageRecoveryManager());
        return globalState;
    }

    private void setUpHomeDataBaseExpectations()
    {
        context.checking(new Expectations()
            {
                {

                    DatabaseInstance databaseInstance = new DatabaseInstance();
                    databaseInstance.setUuid(DATABASE_INSTANCE_UUID);
                    one(openBisService).getHomeDatabaseInstance();
                    will(returnValue(databaseInstance));
                }
            });
    }

    private void setUpOpenBisExpectations(final boolean willRegister)
    {
        setUpOpenBisExpectations(willRegister, true);
    }

    private void setUpOpenBisExpectations(final boolean willRegister,
            final boolean createDataSetCode)
    {
        setUpHomeDataBaseExpectations();
        context.checking(new Expectations()
            {
                {
                    if (createDataSetCode)
                    {
                        oneOf(openBisService).createPermId();
                        will(returnValue(DATA_SET_CODE + 1));
                    }

                    if (willRegister)
                    {

                        Experiment experiment = new Experiment();
                        experiment.setIdentifier(EXPERIMENT_IDENTIFIER);
                        experiment.setCode("EXP-CODE");
                        Person registrator = new Person();
                        registrator.setEmail("email@email.com");
                        experiment.setRegistrator(registrator);

                        Project project = new Project();
                        project.setCode("PROJECT");

                        Space space = new Space();
                        space.setCode("SPACE");

                        project.setSpace(space);
                        experiment.setProject(project);

                        exactly(1).of(openBisService).tryGetExperiment(
                                new ExperimentIdentifierFactory(EXPERIMENT_IDENTIFIER)
                                        .createIdentifier());
                        will(returnValue(experiment));

                        exactly(1).of(openBisService).drawANewUniqueID();
                        will(returnValue(Long.valueOf(1)));

                        exactly(1).of(openBisService).performEntityOperations(
                                with(any(AtomicEntityOperationDetails.class)));
                        will(returnValue(new AtomicEntityOperationResult()));

                        one(openBisService).setStorageConfirmed(with(any(List.class)));
                    }
                }
            });
    }

    private void setUpDataSetValidatorExpectations()
    {
        setUpDataSetValidatorExpectations(DATA_SET_CODE + "1");

    }

    private void setUpDataSetValidatorExpectations(String dataSetCode)
    {
        File dataSetDir = new File(stagingDirectory, dataSetCode.toUpperCase());
        final File dataSetFile = new File(dataSetDir, srcFile.getName());
        context.checking(new Expectations()
            {
                {
                    oneOf(dataSetValidator).assertValidDataSet(with(DATA_SET_TYPE),
                            with(dataSetFile));
                }
            });
    }

    private void setUpDataSetValidatorExpectationsForNullFiles()
    {
        context.checking(new Expectations()
            {
                {
                    oneOf(dataSetValidator).assertValidDataSet(with(DATA_SET_TYPE),
                            with(new IsNull<File>()));
                }
            });
    }

    public static final class MockStorageProcessor implements IStorageProcessorTransactional,
            Serializable
    {
        private static final long serialVersionUID = 1L;

        static MockStorageProcessor instance;

        int calledGetStoreRootDirectoryCount = 0;

        int calledStoreDataCount = 0;

        int calledCommitCount = 0;

        File storeRootDirectory;

        String dataSetInfoString;

        public MockStorageProcessor(ExtendedProperties props)
        {
            instance = this;
        }

        @Override
        public File getStoreRootDirectory()
        {
            calledGetStoreRootDirectoryCount++;
            return storeRootDirectory;
        }

        @Override
        public void setStoreRootDirectory(File storeRootDirectory)
        {
            this.storeRootDirectory = storeRootDirectory;
        }

        @Override
        public StorageFormat getStorageFormat()
        {
            return StorageFormat.PROPRIETARY;
        }

        @Override
        public UnstoreDataAction getDefaultUnstoreDataAction(Throwable exception)
        {
            return UnstoreDataAction.LEAVE_UNTOUCHED;
        }

        @Override
        public IStorageProcessorTransaction createTransaction(
                StorageProcessorTransactionParameters parameters)
        {
            final File rootDir = parameters.getRootDir();
            dataSetInfoString = parameters.getDataSetInformation().toString();

            return new IStorageProcessorTransaction()
                {
                    private static final long serialVersionUID = 1L;

                    private File storedFolder;

                    @Override
                    public void storeData(ITypeExtractor typeExtractor, IMailClient mailClient,
                            File incomingDataSetDirectory)
                    {
                        calledStoreDataCount++;
                        try
                        {
                            if (incomingDataSetDirectory.isDirectory())
                            {
                                FileUtils.copyDirectory(incomingDataSetDirectory, rootDir);
                            } else
                            {
                                FileUtils.copyFileToDirectory(incomingDataSetDirectory, rootDir);
                            }
                        } catch (IOException ex)
                        {
                            ex.printStackTrace();
                            throw new IOExceptionUnchecked(ex);
                        }
                        storedFolder = rootDir;
                    }

                    @Override
                    public UnstoreDataAction rollback(Throwable exception)
                    {
                        return null;
                    }

                    @Override
                    public File getStoredDataDirectory()
                    {
                        return storedFolder;
                    }

                    @Override
                    public void setStoredDataDirectory(File folder)
                    {
                        storedFolder = folder;
                    }

                    @Override
                    public void commit()
                    {
                        calledCommitCount++;
                    }

                    @Override
                    public File tryGetProprietaryData()
                    {
                        return null;
                    }
                };
        }
    }

    private class TestingDataSetHandler extends
            AbstractOmniscientTopLevelDataSetRegistrator<DataSetInformation> implements
            IDataSetRegistrationDetailsFactory<DataSetInformation>
    {

        /**
         * @param globalState
         */
        protected TestingDataSetHandler(TopLevelDataSetRegistratorGlobalState globalState)
        {
            super(globalState);
        }

        @Override
        protected void handleDataSet(DataSetFile dataSetFile,
                DataSetRegistrationService<DataSetInformation> aService) throws Throwable
        {

        }

        public DataSetRegistrationService<DataSetInformation> createNewService(
                DataSetFile dataSetFile)
        {
            return createDataSetRegistrationService(dataSetFile, null,
                    new DoNothingDelegatedAction(), new NoOpDelegate(
                            DataSetRegistrationPreStagingBehavior.USE_ORIGINAL));
        }

        /**
         * Factory method that creates a new registration details object.
         */
        @Override
        public DataSetRegistrationDetails<DataSetInformation> createDataSetRegistrationDetails()
        {
            DataSetRegistrationDetails<DataSetInformation> registrationDetails =
                    new DataSetRegistrationDetails<DataSetInformation>();
            registrationDetails.setDataSetInformation(createDataSetInformation());
            return registrationDetails;
        }

        /**
         * Factory method that creates a new data set information object.
         */
        public DataSetInformation createDataSetInformation()
        {
            DataSetInformation dataSetInfo = new DataSetInformation();
            dataSetInfo.setInstanceUUID(getRegistratorState().getHomeDatabaseInstance().getUuid());

            return dataSetInfo;
        }

        @Override
        public DataSet<DataSetInformation> createDataSet(
                DataSetRegistrationDetails<DataSetInformation> registrationDetails, File stagingFile)
        {
            return new DataSet<DataSetInformation>(registrationDetails, stagingFile, openBisService);
        }

        /**
         * V1 test -- any file can go into faulty paths.
         */
        @Override
        public boolean shouldNotAddToFaultyPathsOrNull(File storeItem)
        {
            return false;
        }

        @Override
        public String getUserIdOrNull()
        {
            return null;
        }
    }

    private void checkContentsOfFile(File dst)
    {
        assertTrue("The file should exist", dst.exists());
        assertEquals("hello world\n", FileUtilities.loadToString(dst));
    }

    private void fillContentsOfSource()
    {
        FileUtilities.writeToFile(srcFile, "hello world");
    }

    protected void createTransaction()
    {
        createHandler();
        tr =
                new DataSetRegistrationTransaction<DataSetInformation>(workingDirectory,
                        workingDirectory, stagingDirectory, service, handler);
    }

}

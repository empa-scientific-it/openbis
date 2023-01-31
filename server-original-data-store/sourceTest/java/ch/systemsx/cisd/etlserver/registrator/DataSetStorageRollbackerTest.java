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

package ch.systemsx.cisd.etlserver.registrator;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.etlserver.DynamicTransactionQueryFactory;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional.UnstoreDataAction;
import ch.systemsx.cisd.etlserver.ThreadParameters;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.recovery.DataSetStorageRecoveryManager;
import ch.systemsx.cisd.etlserver.registrator.v1.AbstractOmniscientTopLevelDataSetRegistrator;
import ch.systemsx.cisd.etlserver.registrator.v1.AbstractOmniscientTopLevelDataSetRegistrator.OmniscientTopLevelDataSetRegistratorState;
import ch.systemsx.cisd.etlserver.registrator.v1.DataSetRegistrationService;
import ch.systemsx.cisd.etlserver.registrator.v1.DataSetStorageRollbacker;
import ch.systemsx.cisd.etlserver.registrator.v1.IDataSetOnErrorActionDecision.ErrorType;
import ch.systemsx.cisd.etlserver.validation.IDataSetValidator;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetStorageRollbackerTest extends AbstractFileSystemTestCase
{

    private static final String DATABASE_INSTANCE_UUID = "db-uuid";

    private static final Logger logger = LogFactory.getLogger(LogCategory.OPERATION,
            DataSetStorageRollbackerTest.class);

    private Mockery context;

    private IEncapsulatedOpenBISService openBisService;

    private IMailClient mailClient;

    private IDataSetValidator dataSetValidator;

    private DataSetFile incomingDataSetFile;

    private TestDataSetRegistrator testRegistrator;

    @Override
    @BeforeMethod
    public void setUp() throws IOException
    {
        super.setUp();

        context = new Mockery();
        openBisService = context.mock(IEncapsulatedOpenBISService.class);
        dataSetValidator = context.mock(IDataSetValidator.class);
        mailClient = context.mock(IMailClient.class);

        setUpHomeDataBaseExpectations();
        TopLevelDataSetRegistratorGlobalState topLevelState =
                createGlobalState(createThreadProperties());
        testRegistrator = new TestDataSetRegistrator(topLevelState);
    }

    @Test
    public void testLogging()
    {
        File realIncoming = createDirectory(workingDirectory, "data_set");
        incomingDataSetFile = new DataSetFile(realIncoming);
        OmniscientTopLevelDataSetRegistratorState globalState =
                testRegistrator.getRegistratorState();
        DataSetStorageRollbacker rollbacker =
                new DataSetStorageRollbacker(globalState, logger, UnstoreDataAction.DELETE,
                        incomingDataSetFile, null, null);
        assertEquals(
                "Performing action DELETE on targets/unit-test-wd/ch.systemsx.cisd.etlserver.registrator.DataSetStorageRollbackerTest/data_set",
                rollbacker.getErrorMessageForLog());

        rollbacker =
                new DataSetStorageRollbacker(globalState, logger, UnstoreDataAction.DELETE,
                        incomingDataSetFile, null, null, ErrorType.REGISTRATION_SCRIPT_ERROR);
        assertEquals(
                "Responding to error [REGISTRATION_SCRIPT_ERROR] by performing action DELETE on targets/unit-test-wd/ch.systemsx.cisd.etlserver.registrator.DataSetStorageRollbackerTest/data_set",
                rollbacker.getErrorMessageForLog());
    }

    private TopLevelDataSetRegistratorGlobalState createGlobalState(Properties threadProperties)
    {
        ThreadParameters threadParameters =
                new ThreadParameters(threadProperties, "rollbacker-test");

        TopLevelDataSetRegistratorGlobalState globalState =
                new TopLevelDataSetRegistratorGlobalState("dss",
                        ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID,
                        workingDirectory, workingDirectory, workingDirectory, workingDirectory,
                        workingDirectory, openBisService, mailClient, dataSetValidator, null,
                        new DynamicTransactionQueryFactory(), true, threadParameters,
                        new DataSetStorageRecoveryManager());
        return globalState;
    }

    private Properties createThreadProperties()
    {
        Properties threadProperties = new Properties();
        threadProperties.put(ThreadParameters.INCOMING_DIR, "incoming");
        threadProperties.put(ThreadParameters.INCOMING_DATA_COMPLETENESS_CONDITION,
                ThreadParameters.INCOMING_DATA_COMPLETENESS_CONDITION_MARKER_FILE);
        threadProperties.put(ThreadParameters.DELETE_UNIDENTIFIED_KEY, "false");
        threadProperties
                .put(IStorageProcessorTransactional.STORAGE_PROCESSOR_KEY,
                        ch.systemsx.cisd.etlserver.registrator.AbstractJythonDataSetHandlerTest.MockStorageProcessor.class
                                .getName());
        return threadProperties;
    }

    private class TestDataSetRegistrator extends
            AbstractOmniscientTopLevelDataSetRegistrator<DataSetInformation>
    {

        /**
         * @param globalState
         */
        protected TestDataSetRegistrator(TopLevelDataSetRegistratorGlobalState globalState)
        {
            super(globalState);
        }

        @Override
        protected void handleDataSet(DataSetFile dataSetFile,
                DataSetRegistrationService<DataSetInformation> service) throws Throwable
        {

        }

        /**
         * V1 Rollbacker test -- any file can go into faulty paths.
         */
        @Override
        public boolean shouldNotAddToFaultyPathsOrNull(File storeItem)
        {
            return false;
        }
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

    private File createDirectory(File parentDir, String directoryName)
    {
        final File file = new File(parentDir, directoryName);
        file.mkdir();
        return file;
    }
}

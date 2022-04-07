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
import java.util.Arrays;
import java.util.Properties;

import org.hamcrest.core.IsAnything;
import org.jmock.Expectations;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.filesystem.FileConstants;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.etlserver.DefaultStorageProcessor;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSetRegistrationTransaction;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ExperimentBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class JythonTopLevelDataSetRegistratorRollbackTest extends AbstractJythonDataSetHandlerTest
{
    private static final String SCRIPTS_FOLDER =
            "sourceTest/java/ch/systemsx/cisd/etlserver/registrator/";

    @BeforeMethod
    @Override
    public void setUp() throws IOException
    {
        super.setUp();
    }

    @Test
    public void testSimpleTransactionRollback()
    {
        setUpHomeDataBaseExpectations();
        Properties properties =
                createThreadPropertiesRelativeToScriptsFolder("simple-transaction.py");
        properties.setProperty(IStorageProcessorTransactional.STORAGE_PROCESSOR_KEY,
                DefaultStorageProcessor.class.getName());

        createHandler(properties, false, false);
        createData();
        ExperimentBuilder builder = new ExperimentBuilder().identifier(EXPERIMENT_IDENTIFIER);
        final Experiment experiment = builder.getExperiment();
        context.checking(new Expectations()
            {
                {
                    ignoring(openBisService).heartbeat();

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

                    one(openBisService)
                            .performEntityOperations(
                                    with(new IsAnything<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails>()));

                    CustomAction makeFileSystemUnavailable = new CustomAction("foo")
                        {
                            @Override
                            public Object invoke(Invocation invocation) throws Throwable
                            {
                                makeFileSystemUnavailable(workingDirectory);
                                return null;
                            }
                        };
                    will(doAll(makeFileSystemUnavailable,
                            throwException(new AssertionError("Fail"))));
                }
            });

        try
        {
            handler.handle(markerFile);
            fail("No IOException thrown");
        } catch (IOExceptionUnchecked e)
        {
            // Make the file system available again and rollback
            makeFileSystemAvailable(workingDirectory);
            DataSetRegistrationTransaction.rollbackDeadTransactions(workingDirectory);
        }
        assertEquals("[]", Arrays.asList(stagingDirectory.list()).toString());
        assertEquals(
                "hello world1",
                FileUtilities.loadToString(
                        new File(workingDirectory, "data_set/sub_data_set_1/read1.me")).trim());
        assertEquals(
                "hello world2",
                FileUtilities.loadToString(
                        new File(workingDirectory, "data_set/sub_data_set_2/read2.me")).trim());

        context.assertIsSatisfied();
    }

    private void createData()
    {
        incomingDataSetFile = createDirectory(workingDirectory, "data_set");

        subDataSet1 = createDirectory(incomingDataSetFile, "sub_data_set_1");
        subDataSet2 = createDirectory(incomingDataSetFile, "sub_data_set_2");

        FileUtilities.writeToFile(new File(subDataSet1, "read1.me"), "hello world1");
        FileUtilities.writeToFile(new File(subDataSet2, "read2.me"), "hello world2");

        markerFile = new File(workingDirectory, FileConstants.IS_FINISHED_PREFIX + "data_set");
        FileUtilities.writeToFile(markerFile, "");
    }

    @Override
    protected String getRegistrationScriptsFolderPath()
    {
        return SCRIPTS_FOLDER;
    }

}

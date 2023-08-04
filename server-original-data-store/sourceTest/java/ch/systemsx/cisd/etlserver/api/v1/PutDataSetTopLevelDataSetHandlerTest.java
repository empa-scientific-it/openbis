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
package ch.systemsx.cisd.etlserver.api.v1;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional;
import ch.systemsx.cisd.etlserver.ITopLevelDataSetRegistrator;
import ch.systemsx.cisd.etlserver.ITopLevelDataSetRegistratorDelegate;
import ch.systemsx.cisd.etlserver.ThreadParameters;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSetRegistrationTransactionTest.MockStorageProcessor;
import ch.systemsx.cisd.etlserver.registrator.recovery.DataSetStorageRecoveryManager;
import ch.systemsx.cisd.openbis.common.io.ByteArrayBasedContentNode;
import ch.systemsx.cisd.openbis.common.io.ConcatenatedContentInputStream;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwner;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwnerType;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

/**
 * @author Franz-Josef Elmer
 */
public class PutDataSetTopLevelDataSetHandlerTest extends AbstractFileSystemTestCase
{
    private static final String TEST_USER_NAME = "test-user";

    private static final String DATABASE_INSTANCE_CODE = "DB";

    private static final String SESSION_TOKEN = "session-1";

    private static final String DATA_SET_CODE = "ds-1";

    private static final Logger logger = LogFactory.getLogger(LogCategory.OPERATION,
            PutDataSetTopLevelDataSetHandlerTest.class);

    private BufferedAppender logRecorder;

    private Mockery context;

    private IEncapsulatedOpenBISService service;

    private PutDataSetService putDataSetService;

    private ITopLevelDataSetRegistrator registrator;

    private File incomingDir;

    private File rpcIncomingDir;

    private File storeDir;

    @BeforeMethod
    public void beforeMethod()
    {
        LogInitializer.init();
        logRecorder = LogRecordingUtils.createRecorder("%-5p %c - %m%n", Level.DEBUG);
        context = new Mockery();
        service = context.mock(IEncapsulatedOpenBISService.class);
        registrator = context.mock(ITopLevelDataSetRegistrator.class);
        final DatabaseInstance databaseInstance = new DatabaseInstance();
        databaseInstance.setCode(DATABASE_INSTANCE_CODE);
        storeDir = new File(workingDirectory, "store");
        storeDir.mkdirs();
        rpcIncomingDir = new File(storeDir, "1/rpc-incoming");
        rpcIncomingDir.mkdirs();
        incomingDir = new File(workingDirectory, "incoming/");
        incomingDir.mkdirs();

        context.checking(new Expectations()
            {
                {
                    one(service).getHomeDatabaseInstance();
                    will(returnValue(databaseInstance));

                    allowing(registrator).getGlobalState();
                    will(returnValue(createGlobalState(incomingDir)));
                }
            });

        putDataSetService =
                new PutDataSetService(service, logger, storeDir,
                        new TestDataSetTypeToTopLevelRegistratorMapper(registrator), null,
                        DATA_SET_CODE, null);
    }

    @AfterMethod
    public void afterMethod(Method method)
    {
        assertEquals(0, incomingDir.listFiles().length);
        logRecorder.reset();
        try
        {
            context.assertIsSatisfied();
        } catch (Throwable t)
        {
            // assert expectations were met, including the name of the failed method
            throw new Error(method.getName() + "() : ", t);
        }
    }

    @Test
    public void testDataSetFile() throws IOException
    {
        DataSetOwner dataSetOwner = new DataSetOwner(DataSetOwnerType.EXPERIMENT, "/S/P/E1");
        final ExperimentIdentifier experimentIdentifier =
                ExperimentIdentifierFactory.parse(dataSetOwner.getIdentifier());
        context.checking(new Expectations()
            {
                {
                    one(service).createPermId();
                    will(returnValue(DATA_SET_CODE));

                    one(service).checkExperimentAccess(SESSION_TOKEN, experimentIdentifier.toString());
                }
            });
        RecordingMatcher<DataSetInformation> dataSetInfoMatcher =
                new RecordingMatcher<DataSetInformation>();
        FileInfoDssDTO f1 = new FileInfoDssDTO("hello.txt", "hello", false, 12);
        File file1 = new File(rpcIncomingDir, DATA_SET_CODE + "/" + f1.getPathInDataSet());
        List<IHierarchicalContentNode> contents =
                prepareRegistrator(file1, Arrays.asList(file1), Arrays.asList("hello world"),
                        dataSetInfoMatcher);
        NewDataSetDTO newDataSet = new NewDataSetDTO(dataSetOwner, null, Arrays.asList(f1));
        newDataSet.setDataSetTypeOrNull("MY-TYPE");
        HashMap<String, Serializable> props = new HashMap<String, Serializable>();
        props.put("name", "Albert");
        newDataSet.setProperties(props);
        ConcatenatedContentInputStream inputStream =
                new ConcatenatedContentInputStream(true, contents);
        PutDataSetTopLevelDataSetHandler handler =
                new PutDataSetTopLevelDataSetHandler(putDataSetService, registrator, SESSION_TOKEN,
                        newDataSet, inputStream);

        handler.execute(false);

        assertEquals("MY-TYPE", dataSetInfoMatcher.recordedObject().getDataSetType().getCode());
        List<NewProperty> dataSetProperties =
                dataSetInfoMatcher.recordedObject().getDataSetProperties();
        assertEquals("name", dataSetProperties.get(0).getPropertyCode());
        assertEquals("Albert", dataSetProperties.get(0).getValue());
        assertEquals(1, dataSetProperties.size());
        assertEquals(experimentIdentifier, dataSetInfoMatcher.recordedObject()
                .getExperimentIdentifier());
        assertEquals(TEST_USER_NAME, dataSetInfoMatcher.recordedObject().getUploadingUserIdOrNull());
        assertEquals("", logRecorder.getLogContent());
    }

    @Test
    public void testDataSetFolder() throws IOException
    {
        DataSetOwner dataSetOwner = new DataSetOwner(DataSetOwnerType.SAMPLE, "/S/S1");
        final SampleIdentifier sampleIdentifier =
                SampleIdentifierFactory.parse(dataSetOwner.getIdentifier());
        context.checking(new Expectations()
            {
                {
                    one(service).createPermId();
                    will(returnValue(DATA_SET_CODE));

                    one(service).checkSampleAccess(SESSION_TOKEN, sampleIdentifier.toString());
                }
            });
        RecordingMatcher<DataSetInformation> dataSetInfoMatcher =
                new RecordingMatcher<DataSetInformation>();
        FileInfoDssDTO f1 = new FileInfoDssDTO("hello.txt", "hello", false, 12);
        File dataSet = new File(rpcIncomingDir, DATA_SET_CODE + "/ds-folder");
        File file1 = new File(dataSet, f1.getPathInDataSet());
        FileInfoDssDTO f2 = new FileInfoDssDTO("subdir/hi.txt", "hi", false, 10);
        File file2 = new File(dataSet, f2.getPathInDataSet());
        FileInfoDssDTO d1 = new FileInfoDssDTO("subdir", "subdir", true, 12);
        List<IHierarchicalContentNode> contents =
                prepareRegistrator(dataSet, Arrays.asList(file1, file2),
                        Arrays.asList("hello world", "hi universe"), dataSetInfoMatcher);
        NewDataSetDTO newDataSet =
                new NewDataSetDTO(dataSetOwner, "ds-folder", Arrays.asList(f1, d1, f2));
        ConcatenatedContentInputStream inputStream =
                new ConcatenatedContentInputStream(true, contents);
        PutDataSetTopLevelDataSetHandler handler =
                new PutDataSetTopLevelDataSetHandler(putDataSetService, registrator, SESSION_TOKEN,
                        newDataSet, inputStream);

        handler.execute(false);

        assertEquals(null, dataSetInfoMatcher.recordedObject().getDataSetType());
        assertEquals(0, dataSetInfoMatcher.recordedObject().getDataSetProperties().size());
        assertEquals(null, dataSetInfoMatcher.recordedObject().getExperimentIdentifier());
        assertEquals("S", dataSetInfoMatcher.recordedObject().getSpaceCode());
        assertEquals("S1", dataSetInfoMatcher.recordedObject().getSampleCode());
        assertEquals(TEST_USER_NAME, dataSetInfoMatcher.recordedObject().getUploadingUserIdOrNull());
        assertEquals("", logRecorder.getLogContent());
    }

    private List<IHierarchicalContentNode> prepareRegistrator(final File dataSet,
            final List<File> files, final List<String> contents,
            final RecordingMatcher<DataSetInformation> dataSetInfoMatcher)
    {
        context.checking(new Expectations()
            {
                {
                    one(service).tryGetSession(SESSION_TOKEN);
                    SessionContextDTO sessionContext = new SessionContextDTO();
                    sessionContext.setUserName(TEST_USER_NAME);
                    will(returnValue(sessionContext));

                    one(registrator).handle(with(dataSet), with(SESSION_TOKEN),
                            with(dataSetInfoMatcher), with(new BaseMatcher<ITopLevelDataSetRegistratorDelegate>()
                                {
                                    @Override
                                    public boolean matches(Object item)
                                    {
                                        // We can check file content only here because after
                                        // invocation of handle() all files are deleted.
                                        for (int i = 0; i < files.size(); i++)
                                        {
                                            File file = files.get(i);
                                            assertEquals("Content of " + file, contents.get(i),
                                                    FileUtilities.loadToString(file).trim());
                                        }
                                        return true;
                                    }

                                    @Override
                                    public void describeTo(Description description)
                                    {
                                    }
                                }));
                }
            });

        List<IHierarchicalContentNode> result = new ArrayList<IHierarchicalContentNode>();
        for (int i = 0; i < files.size(); i++)
        {
            File file = files.get(i);
            result.add(new ByteArrayBasedContentNode(contents.get(i).getBytes(), file.getName()));
        }
        return result;
    }

    private Properties createThreadProperties()
    {
        Properties threadProperties = new Properties();
        threadProperties.put(ThreadParameters.INCOMING_DIR, incomingDir.getPath());
        threadProperties.put(ThreadParameters.INCOMING_DATA_COMPLETENESS_CONDITION,
                ThreadParameters.INCOMING_DATA_COMPLETENESS_CONDITION_MARKER_FILE);
        threadProperties.put(ThreadParameters.DELETE_UNIDENTIFIED_KEY, "false");
        threadProperties.put(IStorageProcessorTransactional.STORAGE_PROCESSOR_KEY,
                MockStorageProcessor.class.getName());
        return threadProperties;
    }

    private TopLevelDataSetRegistratorGlobalState createGlobalState(File tmpIncomingDir)
    {

        ThreadParameters params =
                new ThreadParameters(createThreadProperties(), getClass().getSimpleName()
                        + "-thread");
        return new TopLevelDataSetRegistratorGlobalState(DATA_SET_CODE, "1", this.storeDir,
                tmpIncomingDir, workingDirectory, workingDirectory, workingDirectory,
                this.service, null, null, null,
                null, true, params, new DataSetStorageRecoveryManager());
    }
}

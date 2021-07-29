/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.knime.file;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.knime.core.data.uri.URIContent;
import org.knime.core.data.uri.URIPortObject;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;
import org.knime.core.node.port.image.ImagePortObject;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwnerType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetType.DataSetTypeInitializer;
import ch.systemsx.cisd.openbis.knime.common.IOpenbisServiceFacadeFactory;
import ch.systemsx.cisd.openbis.knime.common.TestUtils;
import ch.systemsx.cisd.openbis.knime.common.Util;
import ch.systemsx.cisd.openbis.plugin.query.client.api.v1.IQueryApiFacade;

/**
 * @author Franz-Josef Elmer
 */
public class DataSetRegistrationNodeModelTest extends AbstractFileSystemTestCase
{
    private static final class MockDataSetRegistrationNodeModel extends DataSetRegistrationNodeModel
    {
        private final IQueryApiFacade queryFacade;

        private final Map<String, String> flowVariables;

        public MockDataSetRegistrationNodeModel(IQueryApiFacade queryFacade,
                IOpenbisServiceFacadeFactory serviceFacadeFactory, Map<String, String> flowVariables)
        {
            super(serviceFacadeFactory);
            this.queryFacade = queryFacade;
            this.flowVariables = flowVariables;
        }

        @Override
        protected IQueryApiFacade createQueryFacade()
        {
            return queryFacade;
        }

        @Override
        protected String getStringFlowVariable(String variableName)
        {
            String value = flowVariables.get(variableName);
            if (value == null)
            {
                throw new NoSuchElementException(variableName);
            }
            return value;
        }
    }

    private BufferedAppender logRecorder;

    private Mockery context;

    private IOpenbisServiceFacadeFactory facadeFactory;

    private IOpenbisServiceFacade serviceFacade;

    private IQueryApiFacade queryFacade;

    private MockDataSetRegistrationNodeModel model;

    private NodeSettingsRO nodeSettingsRO;

    private NodeSettingsWO nodeSettingsWO;

    private File file;

    private Map<String, String> flowVariables;

    private DataSetType dataSetType;

    @BeforeMethod
    public void beforeMethod() throws Exception
    {
        LogInitializer.init();
        context = new Mockery();
        facadeFactory = context.mock(IOpenbisServiceFacadeFactory.class);
        serviceFacade = context.mock(IOpenbisServiceFacade.class);
        queryFacade = context.mock(IQueryApiFacade.class);
        nodeSettingsRO = context.mock(NodeSettingsRO.class);
        nodeSettingsWO = context.mock(NodeSettingsWO.class);
        DataSetTypeInitializer dataSetTypeInitializer = new DataSetTypeInitializer();
        dataSetTypeInitializer.setCode("MY_TYPE");
        dataSetType = new DataSetType(dataSetTypeInitializer);
        final byte[] bytes = Util.serializeDescription(dataSetType);
        context.checking(new Expectations()
            {
                {
                    allowing(nodeSettingsRO).getByteArray(DataSetRegistrationNodeModel.DATA_SET_TYPE_KEY, null);
                    will(returnValue(bytes));

                    allowing(facadeFactory).createFacade(null, null, null);
                    will(returnValue(serviceFacade));

                    allowing(queryFacade).logout();
                    allowing(serviceFacade).logout();
                }
            });
        flowVariables = new HashMap<String, String>();
        model = new MockDataSetRegistrationNodeModel(queryFacade, facadeFactory, flowVariables);
        file = new File(workingDirectory, "hello.txt");
        FileUtilities.writeToFile(file, "hello");
        logRecorder = new BufferedAppender("%-5p %c - %m%n", Level.DEBUG);
    }

    @AfterMethod
    public void afterMethod()
    {
        logRecorder.reset();
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testSaveAdditionalSettingsTo() throws Exception
    {
        prepareFileVariableOwnerAndType(DataSetOwnerType.SAMPLE, "/SPACE/SAMPLE1", "fname");
        preparePropertyTypeCodes("Name");
        preparePropertyValues("Albert");
        model.loadAdditionalValidatedSettingsFrom(nodeSettingsRO);
        context.checking(new Expectations()
            {
                {
                    one(nodeSettingsWO).addString(DataSetRegistrationNodeModel.FILE_VARIABLE_KEY, "fname");
                    one(nodeSettingsWO).addString(DataSetRegistrationNodeModel.OWNER_TYPE_KEY, DataSetOwnerType.SAMPLE.name());
                    one(nodeSettingsWO).addString(DataSetRegistrationNodeModel.OWNER_KEY, "/SPACE/SAMPLE1");
                    one(nodeSettingsWO).addByteArray(DataSetRegistrationNodeModel.DATA_SET_TYPE_KEY, Util.serializeDescription(dataSetType));
                    one(nodeSettingsWO).addStringArray(DataSetRegistrationNodeModel.PROPERTY_TYPE_CODES_KEY, "Name");
                    one(nodeSettingsWO).addStringArray(DataSetRegistrationNodeModel.PROPERTY_VALUES_KEY, "Albert");
                }
            });

        model.saveAdditionalSettingsTo(nodeSettingsWO);

        context.assertIsSatisfied();
    }

    @Test
    public void testExecuteWithInvalidNumberOfInputPorts() throws Exception
    {
        prepareOwnerAndType(DataSetOwnerType.SAMPLE, "/SPACE/SAMPLE1");
        preparePropertyTypeCodes();
        preparePropertyValues();
        model.loadAdditionalValidatedSettingsFrom(nodeSettingsRO);
        logRecorder.resetLogContent();

        try
        {
            model.execute(new PortObject[2], null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Expecting exactly one port instead of 2.", ex.getMessage());
        }

        assertEquals("", logRecorder.getLogContent());
        context.assertIsSatisfied();
    }

    @Test
    public void testExecuteWithInvalidInputPortType() throws Exception
    {
        prepareOwnerAndType(DataSetOwnerType.SAMPLE, "/SPACE/SAMPLE1");
        preparePropertyTypeCodes();
        preparePropertyValues();
        model.loadAdditionalValidatedSettingsFrom(nodeSettingsRO);
        logRecorder.resetLogContent();

        try
        {
            model.execute(new PortObject[] { new ImagePortObject() }, null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Invalid port: " + ImagePortObject.class.getName() + ".", ex.getMessage());
        }

        assertEquals("", logRecorder.getLogContent());
        context.assertIsSatisfied();
    }

    @Test
    public void testExecuteWithNoUriContent() throws Exception
    {
        prepareOwnerAndType(DataSetOwnerType.SAMPLE, "/SPACE/SAMPLE1");
        preparePropertyTypeCodes();
        preparePropertyValues();
        model.loadAdditionalValidatedSettingsFrom(nodeSettingsRO);
        logRecorder.resetLogContent();

        try
        {
            model.execute(new PortObject[] { new URIPortObject(Arrays.<URIContent> asList()) }, null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Expecting at least on URI in input port.", ex.getMessage());
        }

        assertEquals("", logRecorder.getLogContent());
        context.assertIsSatisfied();
    }

    @Test
    public void testExecuteWithUnspecifiedFileVariable() throws Exception
    {
        prepareOwnerAndType(DataSetOwnerType.SAMPLE, "/SPACE/SAMPLE1");
        preparePropertyTypeCodes();
        preparePropertyValues();
        model.loadAdditionalValidatedSettingsFrom(nodeSettingsRO);
        logRecorder.resetLogContent();

        try
        {
            model.execute(new PortObject[] { FlowVariablePortObject.INSTANCE }, null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Unspecified file variable.", ex.getMessage());
        }

        assertEquals("", logRecorder.getLogContent());
        context.assertIsSatisfied();
    }

    @Test
    public void testExecuteForUnknownExperimentOwner() throws Exception
    {
        prepareOwnerAndType(DataSetOwnerType.EXPERIMENT, "/SPACE/PROJECT/EXPERIMENT1");
        preparePropertyTypeCodes();
        preparePropertyValues();
        model.loadAdditionalValidatedSettingsFrom(nodeSettingsRO);
        context.checking(new Expectations()
            {
                {
                    one(serviceFacade).getExperiments(Arrays.asList("/SPACE/PROJECT/EXPERIMENT1"));
                    will(returnValue(Arrays.asList()));
                }
            });

        try
        {
            URIContent content = new URIContent(file.toURI(), "txt");
            model.execute(new PortObject[] { new URIPortObject(Arrays.asList(content)) }, null);
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Error for data set owner of type experiment "
                    + "'/SPACE/PROJECT/EXPERIMENT1': Unknown experiment.", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testExecuteForExperimentOwner() throws Exception
    {
        prepareOwnerAndType(DataSetOwnerType.EXPERIMENT, "/SPACE/PROJECT/EXPERIMENT1");
        preparePropertyTypeCodes("GREETINGS");
        preparePropertyValues("HI");
        model.loadAdditionalValidatedSettingsFrom(nodeSettingsRO);
        final RecordingMatcher<NewDataSetDTO> dataSetMatcher = new RecordingMatcher<NewDataSetDTO>();
        final RecordingMatcher<File> fileMatcher = new RecordingMatcher<File>();
        context.checking(new Expectations()
            {
                {
                    one(serviceFacade).getExperiments(Arrays.asList("/SPACE/PROJECT/EXPERIMENT1"));
                    will(returnValue(Arrays.asList(TestUtils.experiment("/SPACE/PROJECT", "EXPERIMENT1"))));

                    one(serviceFacade).putDataSet(with(dataSetMatcher), with(fileMatcher));
                }
            });
        logRecorder.resetLogContent();

        URIContent content = new URIContent(file.toURI(), "txt");
        URIContent content2 = new URIContent(file.getParentFile().toURI(), "txt");
        model.execute(new PortObject[] { new URIPortObject(Arrays.asList(content, content2)) }, null);

        assertEquals("NewDataSetDTO[NewDataSetMetadataDTO[data set type=MY_TYPE,property GREETINGS=HI],"
                + "NewDataSetDTO.DataSetOwner[Experiment,/SPACE/PROJECT/EXPERIMENT1],"
                + "[FileInfoDssDTO[hello.txt,hello.txt,5]]]",
                dataSetMatcher.recordedObject().toString());
        assertEquals(file.getAbsolutePath(), fileMatcher.recordedObject().getAbsolutePath());
        assertEquals("WARN  " + MockDataSetRegistrationNodeModel.class.getName() + " - 2 URIs instead of only one: ["
                + "URI: " + file.toURI() + "; EXT: txt, URI: " + file.getParentFile().toURI() + "; EXT: txt]\n"
                + "INFO  " + MockDataSetRegistrationNodeModel.class.getName() + " - data set file: "
                + file.getAbsolutePath(), logRecorder.getLogContent());
        context.assertIsSatisfied();
    }

    @Test
    public void testExecuteForUnknownSampleOwner() throws Exception
    {
        prepareOwnerAndType(DataSetOwnerType.SAMPLE, "/SPACE/SAMPLE1");
        preparePropertyTypeCodes();
        preparePropertyValues();
        model.loadAdditionalValidatedSettingsFrom(nodeSettingsRO);
        context.checking(new Expectations()
            {
                {
                    one(serviceFacade).getSamples(Arrays.asList("/SPACE/SAMPLE1"));
                    will(returnValue(Arrays.asList()));
                }
            });

        try
        {
            URIContent content = new URIContent(file.toURI(), "txt");
            model.execute(new PortObject[] { new URIPortObject(Arrays.asList(content)) }, null);
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Error for data set owner of type sample "
                    + "'/SPACE/SAMPLE1': Unknown sample.", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testExecuteForSampleOwnerNotLinkedToExperiment() throws Exception
    {
        prepareOwnerAndType(DataSetOwnerType.SAMPLE, "/SPACE/SAMPLE1");
        preparePropertyTypeCodes();
        preparePropertyValues();
        model.loadAdditionalValidatedSettingsFrom(nodeSettingsRO);
        context.checking(new Expectations()
            {
                {
                    one(serviceFacade).getSamples(Arrays.asList("/SPACE/SAMPLE1"));
                    will(returnValue(Arrays.asList(TestUtils.sample("T", "S", "S", null))));
                }
            });

        try
        {
            URIContent content = new URIContent(file.toURI(), "txt");
            model.execute(new PortObject[] { new URIPortObject(Arrays.asList(content)) }, null);
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Error for data set owner of type sample "
                    + "'/SPACE/SAMPLE1': Not directly linked to an experiment.", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testExecuteForSampleOwner() throws Exception
    {
        prepareOwnerAndType(DataSetOwnerType.SAMPLE, "/SPACE/SAMPLE1");
        preparePropertyTypeCodes();
        preparePropertyValues();
        model.loadAdditionalValidatedSettingsFrom(nodeSettingsRO);
        final RecordingMatcher<NewDataSetDTO> dataSetMatcher = new RecordingMatcher<NewDataSetDTO>();
        final RecordingMatcher<File> fileMatcher = new RecordingMatcher<File>();
        context.checking(new Expectations()
            {
                {
                    one(serviceFacade).getSamples(Arrays.asList("/SPACE/SAMPLE1"));
                    will(returnValue(Arrays.asList(TestUtils.sample("T", "S", "S", "/S/P/E"))));

                    one(serviceFacade).putDataSet(with(dataSetMatcher), with(fileMatcher));
                }
            });
        logRecorder.resetLogContent();

        URIContent content = new URIContent(file.toURI(), "txt");
        model.execute(new PortObject[] { new URIPortObject(Arrays.asList(content)) }, null);

        assertEquals("NewDataSetDTO[NewDataSetMetadataDTO[data set type=MY_TYPE],"
                + "NewDataSetDTO.DataSetOwner[Sample,/SPACE/SAMPLE1],[FileInfoDssDTO[hello.txt,hello.txt,5]]]",
                dataSetMatcher.recordedObject().toString());
        assertEquals(file.getAbsolutePath(), fileMatcher.recordedObject().getAbsolutePath());
        assertEquals("INFO  " + MockDataSetRegistrationNodeModel.class.getName() + " - data set file: "
                + file.getAbsolutePath(), logRecorder.getLogContent());
        context.assertIsSatisfied();
    }

    @Test
    public void testExecuteForSampleOwnerForFileFromFlowVariable() throws Exception
    {
        flowVariables.put("file-name", file.getAbsolutePath());
        prepareFileVariableOwnerAndType(DataSetOwnerType.SAMPLE, "/SPACE/SAMPLE1", "file-name");
        preparePropertyTypeCodes();
        preparePropertyValues();
        model.loadAdditionalValidatedSettingsFrom(nodeSettingsRO);
        final RecordingMatcher<NewDataSetDTO> dataSetMatcher = new RecordingMatcher<NewDataSetDTO>();
        final RecordingMatcher<File> fileMatcher = new RecordingMatcher<File>();
        context.checking(new Expectations()
            {
                {
                    one(serviceFacade).getSamples(Arrays.asList("/SPACE/SAMPLE1"));
                    will(returnValue(Arrays.asList(TestUtils.sample("T", "S", "S", "/S/P/E"))));

                    one(serviceFacade).putDataSet(with(dataSetMatcher), with(fileMatcher));
                }
            });
        logRecorder.resetLogContent();

        model.execute(new PortObject[] { FlowVariablePortObject.INSTANCE }, null);

        assertEquals("NewDataSetDTO[NewDataSetMetadataDTO[data set type=MY_TYPE],"
                + "NewDataSetDTO.DataSetOwner[Sample,/SPACE/SAMPLE1],[FileInfoDssDTO[hello.txt,hello.txt,5]]]",
                dataSetMatcher.recordedObject().toString());
        assertEquals(file.getAbsolutePath(), fileMatcher.recordedObject().getAbsolutePath());
        assertEquals("INFO  " + MockDataSetRegistrationNodeModel.class.getName() + " - data set file: "
                + file.getAbsolutePath(), logRecorder.getLogContent());
        context.assertIsSatisfied();
    }

    @Test
    public void testExecuteForUnknownDataSetOwner() throws Exception
    {
        prepareOwnerAndType(DataSetOwnerType.DATA_SET, "DS1");
        preparePropertyTypeCodes();
        preparePropertyValues();
        model.loadAdditionalValidatedSettingsFrom(nodeSettingsRO);
        context.checking(new Expectations()
            {
                {
                    one(serviceFacade).getDataSet("DS1");
                }
            });
        logRecorder.resetLogContent();

        try
        {
            URIContent content = new URIContent(file.toURI(), "txt");
            model.execute(new PortObject[] { new URIPortObject(Arrays.asList(content)) }, null);
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Error for data set owner of type data set "
                    + "'DS1': Unknown data set.", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testExecuteForDataSetOwner() throws Exception
    {
        prepareOwnerAndType(DataSetOwnerType.DATA_SET, " DS1 ");
        preparePropertyTypeCodes();
        preparePropertyValues();
        model.loadAdditionalValidatedSettingsFrom(nodeSettingsRO);
        final RecordingMatcher<NewDataSetDTO> dataSetMatcher = new RecordingMatcher<NewDataSetDTO>();
        final RecordingMatcher<File> fileMatcher = new RecordingMatcher<File>();
        context.checking(new Expectations()
            {
                {
                    one(serviceFacade).getDataSet("DS1");
                    will(returnValue(new DataSet(serviceFacade, null, TestUtils.dataSet("DS1"), null)));

                    one(serviceFacade).putDataSet(with(dataSetMatcher), with(fileMatcher));
                }
            });
        logRecorder.resetLogContent();

        URIContent content = new URIContent(file.toURI(), "txt");
        model.execute(new PortObject[] { new URIPortObject(Arrays.asList(content)) }, null);

        assertEquals("NewDataSetDTO[NewDataSetMetadataDTO[data set type=MY_TYPE],"
                + "NewDataSetDTO.DataSetOwner[Data Set,DS1],[FileInfoDssDTO[hello.txt,hello.txt,5]]]",
                dataSetMatcher.recordedObject().toString());
        assertEquals(file.getAbsolutePath(), fileMatcher.recordedObject().getAbsolutePath());
        assertEquals("INFO  " + MockDataSetRegistrationNodeModel.class.getName() + " - data set file: "
                + file.getAbsolutePath(), logRecorder.getLogContent());
        context.assertIsSatisfied();
    }

    @Test
    public void testExecuteForUnspecifiedDataSetOwner() throws Exception
    {
        prepareOwnerAndType(DataSetOwnerType.DATA_SET, "");
        preparePropertyTypeCodes();
        preparePropertyValues();
        model.loadAdditionalValidatedSettingsFrom(nodeSettingsRO);
        logRecorder.resetLogContent();

        try
        {
            URIContent content = new URIContent(file.toURI(), "txt");
            model.execute(new PortObject[] { new URIPortObject(Arrays.asList(content)) }, null);
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Owner data set hasn't been specified. "
                    + "Also flow variable 'openbis.DATA_SET' is undefined.", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testExecuteForDataSetOwnerSpecifiedByFlowVariable() throws Exception
    {
        prepareOwnerAndType(DataSetOwnerType.DATA_SET, "");
        preparePropertyTypeCodes();
        preparePropertyValues();
        model.loadAdditionalValidatedSettingsFrom(nodeSettingsRO);
        final RecordingMatcher<NewDataSetDTO> dataSetMatcher = new RecordingMatcher<NewDataSetDTO>();
        final RecordingMatcher<File> fileMatcher = new RecordingMatcher<File>();
        context.checking(new Expectations()
            {
                {
                    one(serviceFacade).getDataSet("DS1");
                    will(returnValue(new DataSet(serviceFacade, null, TestUtils.dataSet("DS1"), null)));

                    one(serviceFacade).putDataSet(with(dataSetMatcher), with(fileMatcher));
                }
            });
        flowVariables.put(Util.VARIABLE_PREFIX + DataSetOwnerType.DATA_SET.name(), "DS1");
        logRecorder.resetLogContent();

        URIContent content = new URIContent(file.toURI(), "txt");
        model.execute(new PortObject[] { new URIPortObject(Arrays.asList(content)) }, null);

        assertEquals("NewDataSetDTO[NewDataSetMetadataDTO[data set type=MY_TYPE],"
                + "NewDataSetDTO.DataSetOwner[Data Set,DS1],[FileInfoDssDTO[hello.txt,hello.txt,5]]]",
                dataSetMatcher.recordedObject().toString());
        assertEquals(file.getAbsolutePath(), fileMatcher.recordedObject().getAbsolutePath());
        assertEquals("INFO  " + MockDataSetRegistrationNodeModel.class.getName() + " - data set file: "
                + file.getAbsolutePath(), logRecorder.getLogContent());
        context.assertIsSatisfied();
    }

    private void prepareOwnerAndType(DataSetOwnerType ownerType, String owner) throws InvalidSettingsException
    {
        prepareFileVariableOwnerAndType(ownerType, owner, null);
    }

    private void prepareFileVariableOwnerAndType(final DataSetOwnerType ownerType, final String owner,
            final String fileVariable) throws InvalidSettingsException
    {
        context.checking(new Expectations()
            {
                {
                    one(nodeSettingsRO).getString(DataSetRegistrationNodeModel.OWNER_TYPE_KEY);
                    will(returnValue(ownerType == null ? null : ownerType.name()));

                    one(nodeSettingsRO).getString(DataSetRegistrationNodeModel.OWNER_KEY);
                    will(returnValue(owner));

                    one(nodeSettingsRO).getString(DataSetRegistrationNodeModel.FILE_VARIABLE_KEY, null);
                    will(returnValue(fileVariable));
                }
            });
    }

    private void preparePropertyTypeCodes(final String... propertyTypeCodes) throws InvalidSettingsException
    {
        context.checking(new Expectations()
            {
                {
                    one(nodeSettingsRO).getStringArray(DataSetRegistrationNodeModel.PROPERTY_TYPE_CODES_KEY,
                            new String[0]);
                    will(returnValue(propertyTypeCodes));
                }
            });
    }

    private void preparePropertyValues(final String... propertyValues) throws InvalidSettingsException
    {
        context.checking(new Expectations()
            {
                {
                    one(nodeSettingsRO).getStringArray(DataSetRegistrationNodeModel.PROPERTY_VALUES_KEY,
                            new String[0]);
                    will(returnValue(propertyValues));
                }
            });
    }
}

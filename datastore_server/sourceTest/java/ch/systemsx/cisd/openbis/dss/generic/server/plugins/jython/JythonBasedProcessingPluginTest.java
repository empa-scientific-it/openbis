/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython;

import java.io.File;
import java.util.Arrays;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.jython.evaluator.EvaluatorException;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython.api.IDataSet;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ProcessingStatus;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Tests for {@link JythonBasedProcessingPlugin} class.
 * 
 * @author Piotr Buczek
 */
public class JythonBasedProcessingPluginTest extends AbstractFileSystemTestCase
{
    private Mockery context;

    private IHierarchicalContentProvider hierarchicalContentProvider;

    private IPluginScriptRunnerFactory scriptRunnerFactory;

    private IProcessingPluginScriptRunner processingPluginScriptRunner;

    private DataSetProcessingContext processingContext;

    private File store;

    private DatasetDescription datasetDescription1;

    private DatasetDescription datasetDescription2;

    private IHierarchicalContent content1;

    private IHierarchicalContent content2;

    @BeforeMethod
    public void beforeMethod()
    {
        LogInitializer.init();
        store = new File(workingDirectory, "store");
        store.mkdirs();

        datasetDescription1 = createDatasetDescription(1);
        datasetDescription2 = createDatasetDescription(2);
        context = new Mockery();
        scriptRunnerFactory = context.mock(IPluginScriptRunnerFactory.class);
        processingPluginScriptRunner = context.mock(IProcessingPluginScriptRunner.class);
        hierarchicalContentProvider = context.mock(IHierarchicalContentProvider.class);
        content1 = context.mock(IHierarchicalContent.class, "content1");
        content2 = context.mock(IHierarchicalContent.class, "content2");
    }

    @AfterMethod
    public void afterTest()
    {
        context.assertIsSatisfied();
    }

    private JythonBasedProcessingPlugin createPlugin()
    {
        return new JythonBasedProcessingPlugin(scriptRunnerFactory, hierarchicalContentProvider);
    }

    @Test
    public void testCreateReportHappyCase()
    {
        final JythonBasedProcessingPlugin plugin = createPlugin();
        final RecordingMatcher<IDataSet> iDataSetMatcher = new RecordingMatcher<IDataSet>();
        commonPrepare();
        context.checking(new Expectations()
            {
                {
                    exactly(2).of(processingPluginScriptRunner).process(with(iDataSetMatcher));
                    will(returnValue(Status.OK));

                    one(processingPluginScriptRunner).releaseResources();
                }
            });
        ProcessingStatus status =
                plugin.process(Arrays.asList(datasetDescription1, datasetDescription2),
                        processingContext);
        assertEquals("[code1, code2]", status.getDatasetsByStatus(Status.OK).toString());
        // verify data sets
        assertEquals(2, iDataSetMatcher.getRecordedObjects().size());
        verifyDataset(content1, 1, iDataSetMatcher.getRecordedObjects().get(0));
        verifyDataset(content2, 2, iDataSetMatcher.getRecordedObjects().get(1));

        context.assertIsSatisfied();
    }

    @Test
    public void testCreateReportErrorHandling()
    {
        final JythonBasedProcessingPlugin plugin = createPlugin();
        // same as testCreateReport() but with an EvaluatorException expected in describe method
        commonPrepare();
        final RecordingMatcher<IDataSet> iDataSetMatcher = new RecordingMatcher<IDataSet>();
        context.checking(new Expectations()
            {
                {
                    one(processingPluginScriptRunner).process(with(iDataSetMatcher));
                    will(throwException(new EvaluatorException("blabla")));

                    one(processingPluginScriptRunner).releaseResources();
                }
            });
        try
        {
            plugin.process(Arrays.asList(datasetDescription1, datasetDescription2),
                    processingContext);
            fail("Expected EvaluatorException");
        } catch (EvaluatorException ex)
        {
            assertEquals("blabla", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    private void commonPrepare()
    {
        context.checking(new Expectations()
            {
                {
                    one(scriptRunnerFactory).createProcessingPluginRunner(processingContext);
                    will(returnValue(processingPluginScriptRunner));

                    one(hierarchicalContentProvider).asContent("code1");
                    will(returnValue(content1));
                    one(hierarchicalContentProvider).asContent("code2");
                    will(returnValue(content2));

                    // in the end all contents should be closed automatically
                    // (both for success and failure)
                    one(content1).close();
                    one(content2).close();
                }
            });
    }

    private static DatasetDescription createDatasetDescription(int nr)
    {
        DatasetDescription result = new DatasetDescription();
        result.setDataSetCode("code" + nr);
        result.setDataSetLocation("dataSetLocation" + nr);
        result.setDataSetSize(Long.valueOf(nr));
        result.setDatasetTypeCode("datasetTypeCode" + nr);
        result.setExperimentCode("experimentCode" + nr);
        result.setExperimentIdentifier("experimentIdentifier" + nr);
        result.setExperimentTypeCode("experimentTypeCode" + nr);
        result.setMainDataSetPath("mainDataSetPath" + nr);
        result.setMainDataSetPattern("mainDataSetPattern" + nr);
        result.setProjectCode("projectCode" + nr);
        result.setSampleCode("sampleCode" + nr);
        result.setSampleIdentifier("sampleIdentifier" + nr);
        result.setSampleTypeCode("sampleTypeCode" + nr);
        result.setSpaceCode("spaceCode" + nr);
        result.setSpeedHint(nr);
        return result;
    }

    private static void verifyDataset(IHierarchicalContent expectedContent, int expectedNr,
            IDataSet dataSet)
    {
        assertSame(expectedContent, dataSet.getContent());
        assertEquals("code" + expectedNr, dataSet.getDataSetCode());
        assertEquals("dataSetLocation" + expectedNr, dataSet.getDataSetLocation());
        assertEquals(Long.valueOf(expectedNr), dataSet.getDataSetSize());
        assertEquals("datasetTypeCode" + expectedNr, dataSet.getDataSetTypeCode());
        assertEquals("experimentCode" + expectedNr, dataSet.getExperimentCode());
        assertEquals("experimentIdentifier" + expectedNr, dataSet.getExperimentIdentifier());
        assertEquals("experimentTypeCode" + expectedNr, dataSet.getExperimentTypeCode());
        assertEquals("mainDataSetPath" + expectedNr, dataSet.getMainDataSetPath());
        assertEquals("mainDataSetPattern" + expectedNr, dataSet.getMainDataSetPattern());
        assertEquals("projectCode" + expectedNr, dataSet.getProjectCode());
        assertEquals("sampleCode" + expectedNr, dataSet.getSampleCode());
        assertEquals("sampleIdentifier" + expectedNr, dataSet.getSampleIdentifier());
        assertEquals("sampleTypeCode" + expectedNr, dataSet.getSampleTypeCode());
        assertEquals("spaceCode" + expectedNr, dataSet.getSpaceCode());
        assertEquals(expectedNr, dataSet.getSpeedHint());
    }
}

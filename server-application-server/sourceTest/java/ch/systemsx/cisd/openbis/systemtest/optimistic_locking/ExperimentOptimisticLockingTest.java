/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.systemtest.optimistic_locking;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.util.TimeIntervalChecker;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.AtomicEntityOperationDetailsBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * @author Franz-Josef Elmer
 */
public class ExperimentOptimisticLockingTest extends OptimisticLockingTestCase
{

    @Test
    public void testRegisterExperiment()
    {
        NewExperiment experiment = toolBox.experiment(1);
        logIntoCommonClientService();
        TimeIntervalChecker timeIntervalChecker = new TimeIntervalChecker(2000);

        genericClientService.registerExperiment(SESSION_KEY, SESSION_KEY, experiment);

        Experiment experimentInfo =
                commonServer.getExperimentInfo(systemSessionToken, new ExperimentIdentifierFactory(
                        experiment.getIdentifier()).createIdentifier());
        assertEquals("test", experimentInfo.getRegistrator().getUserId());
        assertEquals("test", experimentInfo.getModifier().getUserId());
        timeIntervalChecker.assertDateInInterval(experimentInfo.getRegistrationDate());
        timeIntervalChecker.assertDateInInterval(experimentInfo.getModificationDate());
        assertEquals("DESCRIPTION: hello 1", experiment.getProperties()[0].toString());
    }

    @Test
    public void testChangePropertyOfAnExistingExperiment()
    {
        Experiment experiment = toolBox.createAndLoadExperiment(1);
        String sessionToken = logIntoCommonClientService().getSessionID();
        ExperimentUpdatesDTO updates = new ExperimentUpdatesDTO();
        updates.setVersion(experiment.getVersion());
        updates.setExperimentId(new TechId(experiment));
        updates.setProjectIdentifier(toolBox.createProjectIdentifier(experiment.getProject()
                .getIdentifier()));
        updates.setAttachments(ToolBox.NO_ATTACHMENTS);
        List<IEntityProperty> properties = new ArrayList<IEntityProperty>();
        properties.addAll(experiment.getProperties());
        properties.get(0).setValue("testChangePropertyOfAnExistingExperiment");
        updates.setProperties(properties);
        TimeIntervalChecker timeIntervalChecker = toolBox.createTimeIntervalChecker();

        genericServer.updateExperiment(sessionToken, updates);

        Experiment retrievedExperiment = toolBox.loadExperiment(experiment);
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, retrievedExperiment,
                "test");
        assertEquals("DESCRIPTION: testChangePropertyOfAnExistingExperiment", retrievedExperiment
                .getProperties().get(0).toString());
    }

    @Test
    public void testChangePropertyOfAnExperimentWithOldVersion()
    {
        Experiment experiment = toolBox.createAndLoadExperiment(1);
        String sessionToken = logIntoCommonClientService().getSessionID();
        ExperimentUpdatesDTO updates = new ExperimentUpdatesDTO();
        updates.setVersion(experiment.getVersion());
        updates.setExperimentId(new TechId(experiment));
        updates.setProjectIdentifier(toolBox.createProjectIdentifier(experiment.getProject()
                .getIdentifier()));
        updates.setAttachments(ToolBox.NO_ATTACHMENTS);
        List<IEntityProperty> properties = new ArrayList<IEntityProperty>();
        properties.addAll(experiment.getProperties());
        properties.get(0).setValue("testChangePropertyOfAnExistingExperiment");
        updates.setProperties(properties);

        genericServer.updateExperiment(sessionToken, updates);

        try
        {
            genericServer.updateExperiment(sessionToken, updates);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Experiment has been modified in the meantime. Reopen tab to be able "
                    + "to continue with refreshed data.", ex.getMessage());
        }
    }

    @Test
    public void testChangeProjectOfAnExistingExperiment()
    {
        Experiment experiment = toolBox.createAndLoadExperiment(1);
        assertEquals(toolBox.project1.getIdentifier(), experiment.getProject().getIdentifier());
        String sessionToken = logIntoCommonClientService().getSessionID();
        ExperimentUpdatesDTO updates = new ExperimentUpdatesDTO();
        updates.setVersion(experiment.getVersion());
        updates.setExperimentId(new TechId(experiment));
        updates.setProjectIdentifier(toolBox.createProjectIdentifier(toolBox.project2
                .getIdentifier()));
        updates.setAttachments(ToolBox.NO_ATTACHMENTS);
        updates.setProperties(experiment.getProperties());
        TimeIntervalChecker timeIntervalChecker = toolBox.createTimeIntervalChecker();

        genericServer.updateExperiment(sessionToken, updates);

        Experiment retrievedExperiment =
                commonServer.getExperimentInfo(systemSessionToken, new TechId(experiment));
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, retrievedExperiment,
                "test");
        assertEquals(toolBox.project2.getIdentifier(), retrievedExperiment.getProject()
                .getIdentifier());
    }

    @Test
    public void testChangeProjectOfAnExperimentWithOldVersion()
    {
        Experiment experiment = toolBox.createAndLoadExperiment(1);
        String sessionToken = logIntoCommonClientService().getSessionID();
        ExperimentUpdatesDTO updates = new ExperimentUpdatesDTO();
        updates.setVersion(experiment.getVersion());
        updates.setExperimentId(new TechId(experiment));
        updates.setProjectIdentifier(toolBox.createProjectIdentifier(toolBox.project2
                .getIdentifier()));
        updates.setAttachments(ToolBox.NO_ATTACHMENTS);
        updates.setProperties(experiment.getProperties());
        genericServer.updateExperiment(sessionToken, updates);

        try
        {
            genericServer.updateExperiment(sessionToken, updates);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Experiment has been modified in the meantime. Reopen tab to be able "
                    + "to continue with refreshed data.", ex.getMessage());
        }
    }

    @Test
    public void testAddMetaProject()
    {
        Experiment experiment = toolBox.createAndLoadExperiment(1);
        ExperimentUpdatesDTO updates = new ExperimentUpdatesDTO();
        updates.setVersion(experiment.getVersion());
        updates.setExperimentId(new TechId(experiment));
        updates.setProjectIdentifier(toolBox.createProjectIdentifier(experiment.getProject()
                .getIdentifier()));
        updates.setAttachments(ToolBox.NO_ATTACHMENTS);
        updates.setProperties(experiment.getProperties());
        updates.setMetaprojectsOrNull(new String[]
        { "TEST_METAPROJECTS" });
        String sessionToken = logIntoCommonClientService().getSessionID();
        assertEquals("", toolBox.renderMetaProjects(toolBox
                .loadExperiment(sessionToken, experiment).getMetaprojects()));

        genericServer.updateExperiment(sessionToken, updates);

        Experiment loadedExperiment = toolBox.loadExperiment(sessionToken, experiment);
        assertEquals(experiment.getModifier(), loadedExperiment.getModifier());
        assertEquals(experiment.getModificationDate(), loadedExperiment.getModificationDate());
        assertEquals("/test/TEST_METAPROJECTS",
                toolBox.renderMetaProjects(loadedExperiment.getMetaprojects()));
    }

    @Test
    public void testAddAttachment()
    {
        Experiment experiment = toolBox.createAndLoadExperiment(1);
        ExperimentUpdatesDTO updates = new ExperimentUpdatesDTO();
        updates.setVersion(experiment.getVersion());
        updates.setExperimentId(new TechId(experiment));
        updates.setProjectIdentifier(toolBox.createProjectIdentifier(experiment.getProject()
                .getIdentifier()));
        NewAttachment attachment = new NewAttachment();
        attachment.setFilePath("greetings.txt");
        attachment.setTitle("greetings");
        attachment.setContent("hello world".getBytes());
        updates.setAttachments(Arrays.asList(attachment));
        updates.setProperties(experiment.getProperties());
        String sessionToken = logIntoCommonClientService().getSessionID();
        TimeIntervalChecker timeIntervalChecker = toolBox.createTimeIntervalChecker();

        genericServer.updateExperiment(sessionToken, updates);

        Experiment loadedExperiment = toolBox.loadExperiment(experiment);
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedExperiment,
                "test");
        List<Attachment> attachments = loadedExperiment.getAttachments();
        assertEquals("greetings.txt", attachments.get(0).getFileName());
        assertEquals(1, attachments.size());
    }

    @Test
    public void testMoveSampleBetweenExperiments()
    {
        Experiment experiment1 = toolBox.createAndLoadExperiment(1);
        Experiment experiment2 = toolBox.createAndLoadExperiment(2);
        Sample sample = toolBox.createAndLoadSample(1, experiment1);
        SampleUpdatesDTO sampleUpdate =
                new SampleUpdatesDTO(new TechId(sample), ToolBox.NO_PROPERTIES,
                        ExperimentIdentifierFactory.parse(experiment2.getIdentifier()), null,
                        ToolBox.NO_ATTACHMENTS, sample.getVersion(),
                        SampleIdentifierFactory.parse(sample), null, null);
        sampleUpdate.setUpdateExperimentLink(true);
        AtomicEntityOperationDetails details =
                new AtomicEntityOperationDetailsBuilder().user(ToolBox.USER_ID)
                        .sampleUpdate(sampleUpdate).getDetails();
        String sessionToken = logIntoCommonClientService().getSessionID();
        TimeIntervalChecker timeIntervalChecker = toolBox.createTimeIntervalChecker();

        etlService.performEntityOperations(sessionToken, details);

        Experiment loadedExperiment1 = toolBox.loadExperiment(experiment1);
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedExperiment1,
                ToolBox.USER_ID);
        List<Sample> samples1 =
                commonServer.listSamples(systemSessionToken,
                        ListSampleCriteria.createForExperiment(new TechId(loadedExperiment1)));
        assertEquals("[]", samples1.toString());
        Experiment loadedExperiment2 = toolBox.loadExperiment(experiment2);
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedExperiment2,
                ToolBox.USER_ID);
        List<Sample> samples2 =
                commonServer.listSamples(systemSessionToken,
                        ListSampleCriteria.createForExperiment(new TechId(loadedExperiment2)));
        assertEquals(sample.getIdentifier(), samples2.get(0).getIdentifier());
    }

    @Test
    public void testRemoveAndAddSamples()
    {
        Experiment experiment = toolBox.createAndLoadExperiment(1);
        Sample sample1 = toolBox.createAndLoadSample(1, experiment);
        Sample sample2 = toolBox.createAndLoadSample(2, experiment);
        Sample sample3 = toolBox.createAndLoadSample(3, null);
        ExperimentUpdatesDTO update = new ExperimentUpdatesDTO();
        update.setVersion(toolBox.loadExperiment(experiment).getVersion());
        update.setExperimentId(new TechId(experiment));
        update.setSampleCodes(new String[]
        { sample2.getCode(), sample3.getCode() });
        update.setProperties(ToolBox.NO_PROPERTIES);
        update.setAttachments(ToolBox.NO_ATTACHMENTS);
        update.setProjectIdentifier(toolBox.createProjectIdentifier(experiment.getProject()
                .getIdentifier()));
        String sessionToken = logIntoCommonClientService().getSessionID();
        TimeIntervalChecker timeIntervalChecker = toolBox.createTimeIntervalChecker();

        genericServer.updateExperiment(sessionToken, update);

        Experiment loadedExperiment = toolBox.loadExperiment(experiment);
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedExperiment,
                "test");
        List<Sample> samples =
                commonServer.listSamples(systemSessionToken,
                        ListSampleCriteria.createForExperiment(new TechId(loadedExperiment)));
        assertEquals("[" + sample2.getCode() + ", " + sample3.getCode() + "]", toolBox
                .extractCodes(samples).toString());
        Sample reloadedSample1 = toolBox.loadSample(sample1);
        assertEquals(null, reloadedSample1.getExperiment());
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, reloadedSample1, "test");
        Sample reloadedSample2 = toolBox.loadSample(sample2);
        assertEquals(experiment.getIdentifier(), reloadedSample2.getExperiment().getIdentifier());
        assertEquals(sample2.getModifier(), reloadedSample2.getModifier());
        assertEquals(sample2.getModificationDate(), reloadedSample2.getModificationDate());
        Sample reloadedSample3 = toolBox.loadSample(sample3);
        assertEquals(experiment.getIdentifier(), reloadedSample3.getExperiment().getIdentifier());
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, reloadedSample3, "test");

    }

    @Test
    public void testMoveDataSetBetweenExperiments()
    {
        Experiment exp1 = toolBox.createAndLoadExperiment(1);
        Experiment exp2 = toolBox.createAndLoadExperiment(2);
        AtomicEntityOperationDetailsBuilder builder = new AtomicEntityOperationDetailsBuilder();
        NewDataSet dataSet = toolBox.dataSet("DS-1", exp1);
        builder.dataSet(dataSet);
        etlService.performEntityOperations(systemSessionToken, builder.getDetails());
        List<AbstractExternalData> dataSets =
                etlService.listDataSetsByExperimentID(systemSessionToken, new TechId(exp1));
        AbstractExternalData loadedDataSet = dataSets.get(0);
        assertEquals(dataSet.getCode(), loadedDataSet.getCode());
        DataSetUpdatesDTO update = new DataSetUpdatesDTO();
        update.setDatasetId(new TechId(loadedDataSet));
        update.setVersion(loadedDataSet.getVersion());
        update.setExperimentIdentifierOrNull(ExperimentIdentifierFactory.parse(exp2.getIdentifier()));
        update.setProperties(ToolBox.NO_PROPERTIES);
        update.setFileFormatTypeCode(dataSet.getFileFormatType().getCode());
        String sessionToken = logIntoCommonClientService().getSessionID();
        TimeIntervalChecker timeIntervalChecker = toolBox.createTimeIntervalChecker();

        genericServer.updateDataSet(sessionToken, update);

        Experiment loadedExperiment1 = toolBox.loadExperiment(exp1);
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedExperiment1,
                "test");
        List<AbstractExternalData> dataSets1 =
                etlService.listDataSetsByExperimentID(systemSessionToken, new TechId(exp1));
        assertEquals("[]", dataSets1.toString());
        Experiment loadedExperiment2 = toolBox.loadExperiment(exp2);
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedExperiment2,
                "test");
        List<AbstractExternalData> dataSets2 =
                etlService.listDataSetsByExperimentID(systemSessionToken, new TechId(exp2));
        assertEquals(dataSet.getCode(), dataSets2.get(0).getCode());
    }

}

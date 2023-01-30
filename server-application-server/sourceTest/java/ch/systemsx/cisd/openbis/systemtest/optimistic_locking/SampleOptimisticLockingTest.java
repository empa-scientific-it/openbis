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

import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.util.TimeIntervalChecker;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.PropertyBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.SampleTypeBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.AtomicEntityOperationDetailsBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.SampleUpdatesDTOBuilder;

/**
 * @author Franz-Josef Elmer
 */
public class SampleOptimisticLockingTest extends OptimisticLockingTestCase
{
    @Test
    public void testChangeExperimentOfAStaleSample()
    {
        Sample sample = toolBox.createAndLoadSample(1, null);
        Experiment experiment = toolBox.createAndLoadExperiment(1);
        String sessionToken = logIntoCommonClientService().getSessionID();
        SampleUpdatesDTOBuilder builder = new SampleUpdatesDTOBuilder(sample);
        builder.experiment(experiment.getIdentifier());
        etlService.updateSample(sessionToken, builder.get());

        try
        {
            etlService.updateSample(sessionToken, builder.get());
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Sample has been modified in the meantime. Reopen tab to be able "
                    + "to continue with refreshed data.", ex.getMessage());
        }
    }

    @Test
    public void testChangePropertyOfAnExistingSample()
    {
        PropertyBuilder propBuilder = new PropertyBuilder("COMMENT").value("a");
        Sample sample = toolBox.createAndLoadSample(1, null, propBuilder.getProperty());
        String sessionToken = logIntoCommonClientService().getSessionID();
        SampleUpdatesDTOBuilder builder = new SampleUpdatesDTOBuilder(sample);
        builder.property("COMMENT", "b");
        TimeIntervalChecker timeIntervalChecker = toolBox.createTimeIntervalChecker();

        etlService.updateSample(sessionToken, builder.get());

        Sample loadedSample = toolBox.loadSample(sample);
        assertEquals("[COMMENT: b]", loadedSample.getProperties().toString());
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedSample, "test");
    }

    @Test
    public void testChangePropertyOfAStaleSample()
    {
        PropertyBuilder propBuilder = new PropertyBuilder("COMMENT").value("a");
        Sample sample = toolBox.createAndLoadSample(1, null, propBuilder.getProperty());
        String sessionToken = logIntoCommonClientService().getSessionID();
        SampleUpdatesDTOBuilder builder = new SampleUpdatesDTOBuilder(sample);
        builder.property("COMMENT", "b");
        etlService.updateSample(sessionToken, builder.get());

        try
        {
            etlService.updateSample(sessionToken, builder.get());
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Sample has been modified in the meantime. Reopen tab to be able "
                    + "to continue with refreshed data.", ex.getMessage());
        }
    }

    @Test
    public void testAddPropertyToAnExistingSample()
    {
        Sample sample = toolBox.createAndLoadSample(1, null);
        SampleUpdatesDTOBuilder builder = new SampleUpdatesDTOBuilder(sample);
        builder.property("COMMENT", "a");
        AtomicEntityOperationDetails details =
                new AtomicEntityOperationDetailsBuilder().user("test").sampleUpdate(builder.get())
                        .getDetails();
        TimeIntervalChecker timeIntervalChecker = toolBox.createTimeIntervalChecker();

        etlService.performEntityOperations(systemSessionToken, details);

        Sample loadedSample = toolBox.loadSample(sample);
        assertEquals("[COMMENT: a]", loadedSample.getProperties().toString());
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedSample, "test");
    }

    @Test
    public void testChangeSpaceSampleToASharedSample()
    {
        Sample sample = toolBox.createAndLoadSample(1, null);
        String sessionToken = logIntoCommonClientService().getSessionID();
        SampleUpdatesDTOBuilder builder = new SampleUpdatesDTOBuilder(sample);
        builder.identifier("/" + sample.getCode());
        TimeIntervalChecker timeIntervalChecker = toolBox.createTimeIntervalChecker();

        genericServer.updateSample(sessionToken, builder.get());

        Sample loadedSample =
                genericServer.getSampleInfo(systemSessionToken, new TechId(sample)).getParent();
        assertEquals(null, loadedSample.getSpace());
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedSample, "test");
    }

    @Test
    public void testChangeSharedSampleToASpaceSample()
    {
        NewSample newSample = new NewSample();
        newSample.setIdentifier("/OLT-SHARED");
        newSample.setSampleType(new SampleTypeBuilder().code(ToolBox.SAMPLE_TYPE_CODE)
                .getSampleType());
        Sample sample = toolBox.createAndLoadSample(newSample);
        String sessionToken = logIntoCommonClientService().getSessionID();
        SampleUpdatesDTOBuilder builder = new SampleUpdatesDTOBuilder(sample);
        builder.identifier("/" + ToolBox.SPACE_2 + "/" + sample.getCode());
        TimeIntervalChecker timeIntervalChecker = toolBox.createTimeIntervalChecker();

        genericServer.updateSample(sessionToken, builder.get());

        Sample loadedSample =
                genericServer.getSampleInfo(systemSessionToken, new TechId(sample)).getParent();
        assertEquals(ToolBox.SPACE_2, loadedSample.getSpace().getCode());
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedSample, "test");
    }

    @Test
    public void testLinkExperimentWithSample()
    {
        Sample sample = toolBox.createAndLoadSample(1, null);
        Experiment experiment = toolBox.createAndLoadExperiment(1);
        SampleUpdatesDTOBuilder builder = new SampleUpdatesDTOBuilder(sample);
        builder.experiment(experiment.getIdentifier());
        String sessionToken = logIntoCommonClientService().getSessionID();
        TimeIntervalChecker timeIntervalChecker = toolBox.createTimeIntervalChecker();

        etlService.updateSample(sessionToken, builder.get());

        Sample loadedSample = toolBox.loadSample(sample);
        assertEquals(experiment.getIdentifier(), loadedSample.getExperiment().getIdentifier());
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedSample, "test");
        Experiment loadedExperiment = toolBox.loadExperiment(experiment);
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedExperiment,
                "test");
    }

    @Test
    public void testRemoveLinkBetweenExperimentAndSample()
    {
        Experiment experiment = toolBox.createAndLoadExperiment(1);
        Sample sample = toolBox.createAndLoadSample(1, experiment);
        assertEquals(experiment.getIdentifier(), sample.getExperiment().getIdentifier());
        SampleUpdatesDTOBuilder builder = new SampleUpdatesDTOBuilder(sample);
        String sessionToken = logIntoCommonClientService().getSessionID();
        TimeIntervalChecker timeIntervalChecker = toolBox.createTimeIntervalChecker();

        etlService.updateSample(sessionToken, builder.get());

        Sample loadedSample = toolBox.loadSample(sample);
        assertEquals(null, loadedSample.getExperiment());
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedSample, "test");
        Experiment loadedExperiment = toolBox.loadExperiment(experiment);
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedExperiment,
                "test");
    }

    @Test
    public void testAddChildSampleToAnExistingSample()
    {
        Sample sample = toolBox.createAndLoadSample(1, null);
        Sample child = toolBox.createAndLoadSample(2, null);
        SampleUpdatesDTOBuilder builder = new SampleUpdatesDTOBuilder(child);
        builder.parent(sample.getCode());
        String sessionToken = logIntoCommonClientService().getSessionID();
        TimeIntervalChecker timeIntervalChecker = toolBox.createTimeIntervalChecker();

        etlService.updateSample(sessionToken, builder.get());

        Sample loadedSample = toolBox.loadSample(sample);
        Sample loadedChild = toolBox.loadSample(child);
        assertEquals("[OLT-S1]", toolBox.extractCodes(loadedChild.getParents()).toString());
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedSample, "test");
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedChild, "test");
    }

    @Test
    public void testAddSampleToAnExistingContainerSample()
    {
        Sample container = toolBox.createAndLoadSample(1, null);
        Sample sample = toolBox.createAndLoadSample(2, null);
        SampleUpdatesDTOBuilder builder = new SampleUpdatesDTOBuilder(sample);
        builder.container(container.getIdentifier());
        String sessionToken = logIntoCommonClientService().getSessionID();
        TimeIntervalChecker timeIntervalChecker = toolBox.createTimeIntervalChecker();

        etlService.updateSample(sessionToken, builder.get());

        Sample loadedSample = toolBox.loadSample(sample);
        Sample loadedContainer = toolBox.loadSample(container);
        assertEquals(container.getIdentifier(), loadedSample.getContainer().getIdentifier());
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedSample, "test");
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedContainer, "test");
    }

    @Test
    public void testRemoveSampleFromAnExistingContainerSample()
    {
        Sample container = toolBox.createAndLoadSample(1, null);
        NewSample newSample = toolBox.sample(2);
        newSample.setContainerIdentifier(container.getIdentifier());
        Sample sample = toolBox.createAndLoadSample(newSample);
        assertEquals(container.getIdentifier(), sample.getContainer().getIdentifier());
        SampleUpdatesDTOBuilder builder = new SampleUpdatesDTOBuilder(sample);
        builder.container(container.getIdentifier());
        etlService.updateSample(systemSessionToken, builder.get());
        String sessionToken = logIntoCommonClientService().getSessionID();
        builder.container(null);
        TimeIntervalChecker timeIntervalChecker = toolBox.createTimeIntervalChecker();

        etlService.updateSample(sessionToken, builder.get());

        Sample loadedSample =
                genericServer.getSampleInfo(systemSessionToken, new TechId(sample)).getParent();
        Sample loadedContainer = toolBox.loadSample(container);
        assertEquals(null, loadedSample.getContainer());
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedSample, "test");
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedContainer, "test");
    }

    @Test
    public void testAddMetaProject()
    {
        Sample sample = toolBox.createAndLoadSample(1, null);
        SampleUpdatesDTOBuilder builder = new SampleUpdatesDTOBuilder(sample);
        builder.metaProject("TEST_METAPROJECTS");
        String sessionToken = logIntoCommonClientService().getSessionID();
        assertEquals("", toolBox.renderMetaProjects(toolBox.loadSample(sessionToken, sample)
                .getMetaprojects()));

        genericServer.updateSample(sessionToken, builder.get());

        Sample loadedSample = toolBox.loadSample(sessionToken, sample);
        assertEquals(sample.getModifier(), loadedSample.getModifier());
        assertEquals(sample.getModificationDate(), loadedSample.getModificationDate());
        assertEquals("/test/TEST_METAPROJECTS",
                toolBox.renderMetaProjects(loadedSample.getMetaprojects()));
        assertEquals("", toolBox.renderMetaProjects(toolBox.loadSample(sample).getMetaprojects()));
    }

    @Test
    public void testAddAttachment()
    {
        Sample sample = toolBox.createAndLoadSample(1, null);
        SampleUpdatesDTOBuilder builder = new SampleUpdatesDTOBuilder(sample);
        NewAttachment attachment = new NewAttachment();
        attachment.setFilePath("greetings.txt");
        attachment.setTitle("greetings");
        attachment.setContent("hello world".getBytes());
        builder.attachment(attachment);
        String sessionToken = logIntoCommonClientService().getSessionID();
        TimeIntervalChecker timeIntervalChecker = toolBox.createTimeIntervalChecker();

        genericServer.updateSample(sessionToken, builder.get());

        Sample loadedSample = toolBox.loadSample(sample);
        toolBox.checkModifierAndModificationDateOfBean(timeIntervalChecker, loadedSample, "test");
        List<Attachment> attachments = loadedSample.getAttachments();
        assertEquals("greetings.txt", attachments.get(0).getFileName());
        assertEquals(1, attachments.size());
    }
}

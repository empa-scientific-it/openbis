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

package ch.systemsx.cisd.openbis.systemtest.perform_entity_operations;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentWithContent;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetBatchUpdateDetails;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMetaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.IObjectId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.dataset.DataSetCodeId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.experiment.ExperimentIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.material.MaterialCodeAndTypeCodeId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.metaproject.MetaprojectTechIdId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.sample.SampleIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetBatchUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.AtomicEntityOperationDetailsBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.SampleUpdatesDTOBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.systemtest.SystemTestCase;
import ch.systemsx.cisd.openbis.systemtest.optimistic_locking.ToolBox;

/**
 * @author Franz-Josef Elmer
 */
public class RegistrationTest extends SystemTestCase
{
    @Test
    public void testAddAndRemoveDataSetFromContainer()
    {
        AtomicEntityOperationDetailsBuilder builder = new AtomicEntityOperationDetailsBuilder();
        ToolBox toolBox = new ToolBox(commonServer, genericServer, etlService, systemSessionToken, applicationContext);
        Experiment experiment = commonServer.getExperimentInfo(systemSessionToken, new TechId(8L));
        builder.dataSet(toolBox.dataSet("NEW_DATA_SET", experiment));
        DataSetBatchUpdatesDTO dataSetUpdate = new DataSetBatchUpdatesDTO();
        dataSetUpdate.setDatasetCode("ROOT_CONTAINER");
        dataSetUpdate.setDatasetId(new TechId(29));
        dataSetUpdate.setProperties(Collections.<IEntityProperty> emptyList());
        dataSetUpdate.setModifiedContainedDatasetCodesOrNull(new String[] { "NEW_DATA_SET", "CONTAINER_1" });
        DataSetBatchUpdateDetails details = new DataSetBatchUpdateDetails();
        details.setContainerUpdateRequested(true);
        dataSetUpdate.setDetails(details);
        builder.dataSetUpdate(dataSetUpdate);

        etlService.performEntityOperations(systemSessionToken, builder.getDetails());

        ContainerDataSet rootContainer = commonServer.getDataSetInfo(systemSessionToken, new TechId(29)).tryGetAsContainerDataSet();
        assertNotNull(rootContainer);
        List<AbstractExternalData> containedDataSets = rootContainer.getContainedDataSets();
        Map<String, Integer> orderByCode = new TreeMap<String, Integer>();
        for (AbstractExternalData dataSet : containedDataSets)
        {
            orderByCode.put(dataSet.getCode(), dataSet.getOrderInContainer(rootContainer.getId()));
        }
        List<String> componentCodes = toolBox.extractCodes(containedDataSets);
        Collections.sort(componentCodes);
        assertEquals("[CONTAINER_1, NEW_DATA_SET]", componentCodes.toString());
        assertEquals("{CONTAINER_1=1, NEW_DATA_SET=0}", orderByCode.toString());
    }

    @Test
    public void testCreateChildrenForAnExistingSampleWithBatchSize1()
    {
        Sample parentSample =
                genericServer.getSampleInfo(systemSessionToken, new TechId(1)).getParent();
        AtomicEntityOperationDetailsBuilder builder = new AtomicEntityOperationDetailsBuilder();
        SampleType sampleType = new SampleType();
        sampleType.setCode("CELL_PLATE");
        parentSample.setSampleType(sampleType);
        NewSample childSample1 = new NewSample();
        childSample1.setIdentifier("/TEST-SPACE/PARENT_OF_TWO_CHILD_1");
        childSample1.setSampleType(sampleType);
        childSample1.setParents(parentSample.getIdentifier());
        builder.sample(childSample1);
        NewSample childSample2 = new NewSample();
        childSample2.setIdentifier("/TEST-SPACE/PARENT_OF_TWO_CHILD_2");
        childSample2.setSampleType(sampleType);
        childSample2.setParents(parentSample.getIdentifier());
        builder.sample(childSample2);
        builder.batchSize(1);

        etlService.performEntityOperations(systemSessionToken, builder.getDetails());

        List<Sample> children =
                commonServer.listSamples(systemSessionToken,
                        ListSampleCriteria.createForParent(new TechId(parentSample)));

        assertEntities("[/TEST-SPACE/PARENT_OF_TWO_CHILD_1, /TEST-SPACE/PARENT_OF_TWO_CHILD_2]",
                children);
    }

    @Test
    public void testCreateSampleWithTwoAttachments()
    {
        AtomicEntityOperationDetailsBuilder builder = new AtomicEntityOperationDetailsBuilder();
        NewSample newSample = new NewSample();
        SampleType sampleType = new SampleType();
        sampleType.setCode("CELL_PLATE");
        newSample.setSampleType(sampleType);
        newSample.setIdentifier("/TEST-SPACE/S_2_ATT");
        NewAttachment attachment1 = new NewAttachment("a/b/1", "Title 1", "Attachment 1");
        attachment1.setContent("hello attachment one".getBytes());
        NewAttachment attachment2 = new NewAttachment("a/b/2", "Title 2", "Attachment 2");
        attachment2.setContent("hello attachment two".getBytes());
        List<NewAttachment> attachments = Arrays.asList(attachment1, attachment2);
        newSample.setAttachments(attachments);
        builder.sample(newSample);

        etlService.performEntityOperations(systemSessionToken, builder.getDetails());

        Sample sample =
                etlService.tryGetSampleWithExperiment(systemSessionToken,
                        SampleIdentifierFactory.parse(newSample));
        AttachmentWithContent a1 =
                genericServer.getSampleFileAttachment(systemSessionToken, new TechId(sample), "1",
                        null);
        assertEquals("hello attachment one", new String(a1.getContent()));
    }

    @Test
    public void testCreateSampleWithOneAttachment()
    {
        AtomicEntityOperationDetailsBuilder builder = new AtomicEntityOperationDetailsBuilder();
        NewSample newSample = new NewSample();
        SampleType sampleType = new SampleType();
        sampleType.setCode("CELL_PLATE");
        newSample.setSampleType(sampleType);
        newSample.setIdentifier("/TEST-SPACE/S_2_ATT");
        NewAttachment attachment1 = new NewAttachment("a/b/1", "Title 1", "Attachment 1");
        attachment1.setContent("hello attachment one".getBytes());
        List<NewAttachment> attachments = Arrays.asList(attachment1);
        newSample.setAttachments(attachments);
        builder.sample(newSample);

        etlService.performEntityOperations(systemSessionToken, builder.getDetails());

        Sample sample =
                etlService.tryGetSampleWithExperiment(systemSessionToken,
                        SampleIdentifierFactory.parse(newSample));
        List<Attachment> loadedAttachments =
                commonServer.listSampleAttachments(systemSessionToken, new TechId(sample));
        assertEquals("Title 1", loadedAttachments.get(0).getTitle());
        assertEquals("1", loadedAttachments.get(0).getFileName());
        assertEquals(1, loadedAttachments.size());
        AttachmentWithContent a1 =
                genericServer.getSampleFileAttachment(systemSessionToken, new TechId(sample), "1",
                        null);
        assertEquals("hello attachment one", new String(a1.getContent()));
    }

    @Test
    public void testAddOneAttachmentToAnExistingSample()
    {
        AtomicEntityOperationDetailsBuilder builder = new AtomicEntityOperationDetailsBuilder();
        SampleUpdatesDTOBuilder updateBuilder = new SampleUpdatesDTOBuilder(1);
        NewAttachment attachment1 = new NewAttachment("a/b/1", "Title 1", "Attachment 1");
        attachment1.setContent("hello attachment one".getBytes());
        updateBuilder.attachment(attachment1);
        builder.sampleUpdate(updateBuilder.get());

        etlService.performEntityOperations(systemSessionToken, builder.getDetails());

        AttachmentWithContent a1 =
                genericServer.getSampleFileAttachment(systemSessionToken, new TechId(1), "1", null);
        assertEquals("hello attachment one", new String(a1.getContent()));
    }

    @Test
    public void testAddTwoAttachmentsToAnExistingSample()
    {
        AtomicEntityOperationDetailsBuilder builder = new AtomicEntityOperationDetailsBuilder();
        SampleUpdatesDTOBuilder updateBuilder = new SampleUpdatesDTOBuilder(1);
        NewAttachment attachment1 = new NewAttachment("a/b/1", "Title 1", "Attachment 1");
        attachment1.setContent("hello attachment one".getBytes());
        NewAttachment attachment2 = new NewAttachment("a/b/2", "Title 2", "Attachment 2");
        attachment2.setContent("hello attachment two".getBytes());
        updateBuilder.attachment(attachment1);
        updateBuilder.attachment(attachment2);
        builder.sampleUpdate(updateBuilder.get());

        etlService.performEntityOperations(systemSessionToken, builder.getDetails());

        AttachmentWithContent a1 =
                genericServer.getSampleFileAttachment(systemSessionToken, new TechId(1), "1", null);
        assertEquals("hello attachment one", new String(a1.getContent()));
    }

    @Test
    public void testRegistrationOfMetaprojectLinkedToExperimentSampleDataSetAndMaterial()
    {
        AtomicEntityOperationDetailsBuilder builder = new AtomicEntityOperationDetailsBuilder();
        String name = "RM-TEST";
        NewMetaproject metaProject = new NewMetaproject(name, "a test", "test");
        ExperimentIdentifierId experimentIdentifier = new ExperimentIdentifierId("/CISD/NEMO/EXP1");
        SampleIdentifierId sampleIdentifier = new SampleIdentifierId("/CISD/CL1");
        DataSetCodeId dataSetIdentifier = new DataSetCodeId("20081105092159188-3");
        MaterialCodeAndTypeCodeId materialIdentifier =
                new MaterialCodeAndTypeCodeId("AD3", "VIRUS");
        metaProject.setEntities(Arrays.<IObjectId> asList(experimentIdentifier, sampleIdentifier,
                dataSetIdentifier, materialIdentifier));
        builder.metaProject(metaProject);

        etlService.performEntityOperations(systemSessionToken, builder.getDetails());

        assertEquals(null, findMetaproject(systemSessionToken, name));
        String sessionToken = logIntoCommonClientService().getSessionID(); // login as 'test'
        Metaproject metaproject = findMetaproject(sessionToken, name);
        assertEquals("a test", metaproject.getDescription());
        List<Experiment> experiments =
                commonServer.listMetaprojectExperiments(sessionToken, new MetaprojectTechIdId(
                        metaproject.getId()));
        assertIdentifiers("[/CISD/NEMO/EXP1]", experiments);
        assertContainsMetaproject(experiments.get(0).getMetaprojects(), name);
        List<Sample> samples =
                commonServer.listMetaprojectSamples(sessionToken, new MetaprojectTechIdId(
                        metaproject.getId()));
        assertIdentifiers("[/CISD/CL1]", samples);
        assertContainsMetaproject(samples.get(0).getMetaprojects(), name);
        List<AbstractExternalData> dataSets =
                commonServer.listMetaprojectExternalData(sessionToken, new MetaprojectTechIdId(
                        metaproject.getId()));
        assertIdentifiers("[20081105092159188-3]", dataSets);
        assertContainsMetaproject(dataSets.get(0).getMetaprojects(), name);
        List<Material> materials =
                commonServer.listMetaprojectMaterials(sessionToken, new MetaprojectTechIdId(
                        metaproject.getId()));
        assertIdentifiers("[AD3 (VIRUS)]", materials);
        assertContainsMetaproject(materials.get(0).getMetaprojects(), name);
    }

    private void assertContainsMetaproject(Collection<Metaproject> metaprojects,
            String metaprojectName)
    {
        List<String> metaprojectNames = new ArrayList<String>();
        for (Metaproject metaproject : metaprojects)
        {
            String name = metaproject.getName();
            if (name.equals(metaprojectName))
            {
                return;
            }
            metaprojectNames.add(name);
        }
        fail("Unknown metaproject '" + metaprojectName + "': " + metaprojectNames);
    }

    private Metaproject findMetaproject(String sessionToken, String code)
    {
        List<Metaproject> metaprojects = commonServer.listMetaprojects(sessionToken);
        for (Metaproject metaproject : metaprojects)
        {
            if (metaproject.getCode().equals(code))
            {
                return metaproject;
            }
        }
        return null;
    }

    @Test
    public void testRegisterExperimentPlateAndWells()
    {
        AtomicEntityOperationDetailsBuilder builder = new AtomicEntityOperationDetailsBuilder();
        String identifier = "/TEST-SPACE/TEST-PROJECT/EXP-1";
        NewExperiment experiment = new NewExperiment(identifier, "COMPOUND_HCS");
        EntityProperty property = new EntityProperty();
        PropertyType propertyType = new PropertyType();
        propertyType.setCode("DESCRIPTION");
        property.setPropertyType(propertyType);
        property.setValue("hello");
        experiment.setProperties(new IEntityProperty[]
        { property });
        builder.experiment(experiment);
        String sampleIdentifier = "/TEST-SPACE/TEST-PROJECT/PLATE-1";
        builder.sample(new NewSampleBuilder(sampleIdentifier).experiment(identifier)
                .type("CELL_PLATE").get());
        builder.sample(new NewSampleBuilder(sampleIdentifier + ":A1").container(sampleIdentifier)
                .experiment(identifier).type("WELL").get());
        builder.sample(new NewSampleBuilder(sampleIdentifier + ":A2").container(sampleIdentifier)
                .experiment(identifier).type("WELL").get());

        etlService.performEntityOperations(systemSessionToken, builder.getDetails());

        Experiment loadedExperiment =
                commonServer.getExperimentInfo(systemSessionToken,
                        ExperimentIdentifierFactory.parse(identifier));
        List<Sample> plates =
                assertSamples(
                        "[/TEST-SPACE/TEST-PROJECT/PLATE-1]",
                        ListSampleCriteria.createForExperiment(new TechId(loadedExperiment.getId())));
        assertSamples("[/TEST-SPACE/TEST-PROJECT/PLATE-1:A1, /TEST-SPACE/TEST-PROJECT/PLATE-1:A2]",
                ListSampleCriteria.createForContainer(new TechId(plates.get(0))));
    }

    @DataProvider(name = "registerSharedSample")
    protected Object[][] getMappingTypes()
    {
        return new Object[][]
        {
                { "/SHARED_TEST_SAMPLE", systemSessionToken, false },
                { "/SHARED_TEST_SAMPLE", authenticateAs("test_role"), true },
                { "/CISD/TEST_SAMPLE", authenticateAs("test_role"), false } };
    }

    @Test(dataProvider = "registerSharedSample")
    public void testRegisterSharedSampleFailsForNonAdminUser(String identifier,
            String sessionToken, boolean shouldFail)
    {
        SampleType sampleType = etlService.getSampleType(systemSessionToken, "NORMAL");

        NewSample sample = new NewSample();
        sample.setIdentifier(identifier);
        sample.setSampleType(sampleType);

        NewSamplesWithTypes samplesForRegistration =
                new NewSamplesWithTypes(sampleType, Collections.singletonList(sample));

        try
        {
            genericServer.registerOrUpdateSamples(sessionToken,
                    Collections.singletonList(samplesForRegistration));
            if (shouldFail)
            {
                fail("Expected authorization error");
            }
            SampleIdentifier sampleIdentifier =
                    new SampleIdentifierFactory(sample.getIdentifier()).createIdentifier();

            Sample registeredSample =
                    etlService.tryGetSampleWithExperiment(systemSessionToken, sampleIdentifier);

            assertTrue(registeredSample != null);
            assertEquals(sample.getIdentifier(), registeredSample.getIdentifier());

        } catch (AuthorizationFailureException afe)
        {
            // is ok
        }
    }

    private List<Sample> assertSamples(String expectedSamples, ListSampleCriteria criteria)
    {
        List<Sample> samples = commonServer.listSamples(systemSessionToken, criteria);
        assertIdentifiers(expectedSamples, samples);
        return samples;
    }

    private void assertIdentifiers(String expectedIdentifiers,
            List<? extends IIdentifierHolder> identifierHolders)
    {
        List<String> identifiers = new ArrayList<String>();
        for (IIdentifierHolder sample : identifierHolders)
        {
            identifiers.add(sample.getIdentifier());
        }
        Collections.sort(identifiers);
        assertEquals(expectedIdentifiers, identifiers.toString());
    }
}

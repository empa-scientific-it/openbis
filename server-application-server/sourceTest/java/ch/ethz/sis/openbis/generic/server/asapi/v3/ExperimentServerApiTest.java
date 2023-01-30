/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.create.AttachmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.ITagId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagCode;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagPermId;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.common.test.RecordingMatcherRepository;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentHolderPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.ExperimentTypePEBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.SpacePEBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author felmer
 */
public class ExperimentServerApiTest extends AbstractApplicationServerApiTestCase
{
    private static final TagCode TAG_CODE_ID = new TagCode("red");

    private static final TagCode UNKNOWN_TAG_CODE_ID = new TagCode("unknown");

    private static final String UNKNOWN_PROJECT_CODE = "UNKNOWN";

    private static final String PROJECT_CODE = "P";

    private static final ProjectIdentifier PROJECT_IDENTIFIER = new ProjectIdentifier("/"
            + SPACE_CODE + "/" + PROJECT_CODE);

    private static final ProjectIdentifier UNKNOWN_PROJECT_IDENTIFIER = new ProjectIdentifier("/"
            + SPACE_CODE + "/" + UNKNOWN_PROJECT_CODE);

    private static final ProjectPermId UNKNOWN_PROJECT_PERM_ID = new ProjectPermId("unknown");

    private ExperimentTypePE type1;

    private ProjectPE project;

    private PropertyTypePE propertyType1;

    private MetaprojectPE tag;

    @BeforeMethod
    public void setUpExamples()
    {
        ExperimentTypePEBuilder builder1 =
                new ExperimentTypePEBuilder().code(
                        ENTITY_TYPE);
        builder1.assign(PROPERTY_TYPE_CODE);
        type1 = builder1.getExperimentTypePE();
        project = new ProjectPE();
        project.setSpace(new SpacePEBuilder().code(SPACE_CODE).getSpace());
        project.setCode(PROJECT_CODE);
        propertyType1 = new PropertyTypePE();
        propertyType1.setCode(PROPERTY_TYPE_CODE);
        DataTypePE dataType = new DataTypePE();
        dataType.setCode(DataTypeCode.VARCHAR);
        propertyType1.setType(dataType);
        tag = new MetaprojectPE();
        tag.setName(TAG_CODE_ID.getCode());
        tag.setOwner(person);
    }

    @Test(enabled = false)
    public void testCreateExperimentWithUnspecifiedType()
    {
        prepareCreationEnvironment();
        ExperimentCreation experiment = experiment("EXP1");
        experiment.setTypeId(null);

        assertFailingCreateExperiments(
                "Unspecified entity type id. (Context: [register experiment EXP1])", experiment);

        context.assertIsSatisfied();
    }

    @Test(enabled = false)
    public void testCreateExperimentWithUnknownType()
    {
        prepareCreationEnvironment();
        ExperimentCreation experiment = experiment("EXP1");
        experiment.setTypeId(new EntityTypePermId(UNKNOWN_ENTITY_TYPE));

        assertFailingCreateExperiments(
                "No entity type found with this id: UNKNOWN (Context: [register experiment EXP1])",
                experiment);

        context.assertIsSatisfied();
    }

    @Test(enabled = false)
    public void testCreateExperimentWithUnspecifiedProject()
    {
        prepareCreationEnvironment();
        ExperimentCreation experiment = experiment("EXP1");
        experiment.setProjectId(null);

        assertFailingCreateExperiments(
                "Unspecified project id. (Context: [register experiment EXP1])", experiment);

        context.assertIsSatisfied();
    }

    @Test(enabled = false)
    public void testCreateExperimentWithUnknownProjectIdentifier()
    {
        prepareCreationEnvironment();
        ExperimentCreation experiment = experiment("EXP1");
        experiment.setProjectId(UNKNOWN_PROJECT_IDENTIFIER);

        assertFailingCreateExperiments(
                "No project found with this id: /S/UNKNOWN (Context: [register experiment EXP1])",
                experiment);

        context.assertIsSatisfied();
    }

    @Test(enabled = false)
    public void testCreateExperimentWithUnknownProjectPermId()
    {
        prepareCreationEnvironment();
        ExperimentCreation experiment = experiment("EXP1");
        experiment.setProjectId(UNKNOWN_PROJECT_PERM_ID);

        assertFailingCreateExperiments(
                "No project found with this id: unknown (Context: [register experiment EXP1])",
                experiment);

        context.assertIsSatisfied();
    }

    @Test(enabled = false)
    public void testCreateExperimentWithTagIdOfWrongOwner()
    {
        prepareCreationEnvironment();
        ExperimentCreation experiment = experiment("EXP1");
        experiment.setTagIds(Arrays.<ITagId> asList(new TagPermId("/someone/green")));

        assertFailingCreateExperiments(
                "Tag id '/someone/green' doesn't belong to the registrator test. "
                        + "(Context: [register experiment EXP1])", experiment);

        context.assertIsSatisfied();
    }

    @Test(enabled = false)
    public void testCreateExperimentWithAttachmentWithUnspecifiedFileName()
    {
        prepareCreationEnvironment();
        ExperimentCreation experiment = experiment("EXP1");
        experiment.setAttachments(Arrays.asList(new AttachmentCreation()));

        assertFailingCreateExperiments("Unspecified attachment file name. "
                + "(Context: [register experiment EXP1])", experiment);

        context.assertIsSatisfied();
    }

    @Test(enabled = false)
    public void testCreateExperimentWithAttachmentWithUnspecifiedContent()
    {
        prepareCreationEnvironment();
        ExperimentCreation experiment = experiment("EXP1");
        AttachmentCreation attachment = new AttachmentCreation();
        attachment.setFileName("manual.pdf");
        experiment.setAttachments(Arrays.asList(attachment));

        assertFailingCreateExperiments("Unspecified attachment content. "
                + "(Context: [register experiment EXP1, register attachment 'manual.pdf'])",
                experiment);

        context.assertIsSatisfied();
    }

    @Test(enabled = false)
    public void testCreateExperimentWithNoTagsAndAttachmentsSuccessfully()
    {
        RecordingMatcherRepository recorderRepository = prepareCreationEnvironment();
        ExperimentCreation experiment = experiment("EXP1");

        List<ExperimentPermId> ids =
                server.createExperiments(SESSION_TOKEN, Arrays.asList(experiment));

        assertEquals("[perm-1]", ids.toString());
        ExperimentPE exp = recorderRepository.recordedObject(ExperimentPE.class);
        assertEquals(project, exp.getProject());
        assertEquals("EXP1", exp.getCode());
        assertEquals("perm-1", exp.getPermId());
        assertEquals(type1, exp.getEntityType());
        assertEquals(person, exp.getRegistrator());
        Set<ExperimentPropertyPE> properties = exp.getProperties();
        ExperimentPropertyPE property = properties.iterator().next();
        assertEquals(PROPERTY_TYPE_CODE, property.getEntityTypePropertyType().getPropertyType()
                .getCode());
        assertEquals("a test", property.getValue());
        assertEquals(1, properties.size());
        List<MetaprojectPE> tags = new ArrayList<MetaprojectPE>(exp.getMetaprojects());
        assertEquals(0, tags.size());
        assertSame(0, recorderRepository.getRecordedObjects(AttachmentHolderPE.class).size());
        assertSame(0, recorderRepository.getRecordedObjects(AttachmentPE.class).size());

        context.assertIsSatisfied();
    }

    @Test(enabled = false)
    public void testCreateExperimentWithTagAndAttachmentSuccessfully()
    {
        RecordingMatcherRepository recorderRepository = prepareCreationEnvironment();
        ExperimentCreation experiment = experiment("EXP1");
        experiment.setTagIds(Arrays.<ITagId> asList(TAG_CODE_ID));
        AttachmentCreation attachment = new AttachmentCreation();
        attachment.setFileName("manual.pdf");
        attachment.setTitle("Manual");
        attachment.setDescription("example attachment");
        attachment.setContent("hello".getBytes());
        experiment.setAttachments(Arrays.asList(attachment));

        List<ExperimentPermId> ids =
                server.createExperiments(SESSION_TOKEN, Arrays.asList(experiment));

        assertEquals("[perm-1]", ids.toString());
        ExperimentPE exp = recorderRepository.recordedObject(ExperimentPE.class);
        assertEquals(project, exp.getProject());
        assertEquals("EXP1", exp.getCode());
        assertEquals("perm-1", exp.getPermId());
        assertEquals(type1, exp.getEntityType());
        assertEquals(person, exp.getRegistrator());
        Set<ExperimentPropertyPE> properties = exp.getProperties();
        ExperimentPropertyPE property = properties.iterator().next();
        assertEquals(PROPERTY_TYPE_CODE, property.getEntityTypePropertyType().getPropertyType()
                .getCode());
        assertEquals("a test", property.getValue());
        assertEquals(1, properties.size());
        List<MetaprojectPE> tags = new ArrayList<MetaprojectPE>(exp.getMetaprojects());
        assertEquals(tag, tags.get(0));
        assertEquals(1, tags.size());
        assertSame(exp, recorderRepository.recordedObject(AttachmentHolderPE.class));
        AttachmentPE attachmentPE = recorderRepository.recordedObject(AttachmentPE.class);
        assertEquals("manual.pdf", attachmentPE.getFileName());
        assertEquals("example attachment", attachmentPE.getDescription());
        assertEquals("Manual", attachmentPE.getTitle());
        assertEquals("hello", new String(attachmentPE.getAttachmentContent().getValue()));
        assertEquals(person, attachmentPE.getRegistrator());

        context.assertIsSatisfied();
    }

    // This test is testing correct poping of context descriptions
    @Test(enabled = false)
    public void testCreateTwoExperimentSecondWithUnknownProjectPermId()
    {
        prepareCreationEnvironment();
        ExperimentCreation experiment1 = experiment("EXP1");
        AttachmentCreation attachment = new AttachmentCreation();
        attachment.setFileName("manual.pdf");
        attachment.setContent("hello".getBytes());
        experiment1.setAttachments(Arrays.asList(attachment));
        ExperimentCreation experiment2 = experiment("EXP2");
        experiment2.setProjectId(UNKNOWN_PROJECT_PERM_ID);

        assertFailingCreateExperiments(
                "No project found with this id: unknown (Context: [register experiment EXP2])",
                experiment1, experiment2);

        context.assertIsSatisfied();
    }

    private void assertFailingCreateExperiments(String expectedExceptionMessage,
            ExperimentCreation... experiments)
    {
        try
        {
            server.createExperiments(SESSION_TOKEN, Arrays.asList(experiments));
            fail("Expecting UserFailureException");
        } catch (UserFailureException e)
        {
            assertEquals(expectedExceptionMessage, e.getMessage());
        }
    }

    private RecordingMatcherRepository prepareCreationEnvironment()
    {
        prepareGetSession();
        prepareFindEntityTypeByCode(EntityKind.EXPERIMENT, UNKNOWN_ENTITY_TYPE, null);
        prepareEntityTypes(type1);
        prepareFindPropertyTypeByCode(PROPERTY_TYPE_CODE, propertyType1);
        prepareFindProject(SPACE_CODE, PROJECT_CODE, project);
        prepareFindProject(SPACE_CODE, UNKNOWN_PROJECT_CODE, null);
        prepareFindProject(UNKNOWN_PROJECT_PERM_ID.getPermId(), null);
        prepareFindTag(session.getUserName(), TAG_CODE_ID.getCode(), tag);
        prepareFindTag(session.getUserName(), UNKNOWN_TAG_CODE_ID.getCode(), null);
        RecordingMatcherRepository repository = new RecordingMatcherRepository();
        prepareCreateAttachment(repository);
        final RecordingMatcher<ExperimentPE> experimentRecoder =
                repository.getRecordingMatcher(ExperimentPE.class);
        context.checking(new Expectations()
            {
                {
                    allowing(experimentDAO).createOrUpdateExperiment(with(experimentRecoder),
                            with(person));
                }
            });
        return repository;
    }

    private ExperimentCreation experiment(String code)
    {
        ExperimentCreation experiment = new ExperimentCreation();
        experiment.setCode(code);
        experiment.setTypeId(new EntityTypePermId(ENTITY_TYPE));
        experiment.setProjectId(PROJECT_IDENTIFIER);
        experiment.setProperty(PROPERTY_TYPE_CODE, "a test");
        return experiment;
    }
}

/*
 * Copyright ETH 2014 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.Attachment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.create.AttachmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.id.AttachmentFileName;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.delete.DataSetDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.id.DataStorePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.history.ExperimentRelationType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update.ExperimentUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.HistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.PersonPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.Role;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.create.RoleAssignmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete.SampleDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.ITagId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagCode;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.FreezingFlags;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.systemtest.authorization.ProjectAuthorizationUser;

/**
 * @author pkupczyk
 */
public class UpdateExperimentTest extends AbstractExperimentTest
{
    private static final String PREFIX = "UET-";

    @Test
    public void testUpdateWithIndexCheck()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentUpdate experiment = new ExperimentUpdate();
        experiment.setExperimentId(new ExperimentPermId("200811050951882-1028"));
        experiment.setProperty("DESCRIPTION", "an updated description");

        v3api.updateExperiments(sessionToken, Arrays.asList(experiment));

        assertExperimentsExists("200811050951882-1028");
    }

    @Test
    public void testUpdateWithExperimentUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final IExperimentId experimentId = new ExperimentPermId("200811050951882-1028");
        final ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(experimentId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.updateExperiments(sessionToken, Arrays.asList(update));
            }
        }, experimentId);
    }

    @Test
    public void testUpdateWithExperimentNonexistent()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final IExperimentId experimentId = new ExperimentPermId("IDONTEXIST");
        final ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(experimentId);

        assertObjectNotFoundException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.updateExperiments(sessionToken, Arrays.asList(update));
            }
        }, experimentId);
    }

    @Test
    public void testUpdateWithProject()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("TEST_EXPERIMENT");
        creation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty("DESCRIPTION", "a description");

        List<ExperimentPermId> ids = v3api.createExperiments(sessionToken, Arrays.asList(creation));

        ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(ids.get(0));
        update.setProjectId(new ProjectIdentifier("/CISD/NOE"));

        v3api.updateExperiments(sessionToken, Arrays.asList(update));

        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        Map<IExperimentId, Experiment> map = v3api.getExperiments(sessionToken, ids, fetchOptions);
        List<Experiment> experiments = new ArrayList<Experiment>(map.values());

        AssertionUtil.assertCollectionSize(experiments, 1);

        Experiment experiment = experiments.get(0);
        assertEquals(experiment.getIdentifier().getIdentifier(), "/CISD/NOE/TEST_EXPERIMENT");
    }

    /*
     * @Test(groups = "project-samples") public void testMovingExperimentWithProjectSamplesToADifferentProject() { String sessionToken =
     * v3api.login(TEST_USER, PASSWORD); SampleCreation sampleCreation = new SampleCreation(); sampleCreation.setCode("PROJECT-SAMPLE-IN-EXPERIMENT");
     * ExperimentIdentifier experimentId = new ExperimentIdentifier("CISD", "NEMO", "EXP1"); sampleCreation.setExperimentId(experimentId);
     * sampleCreation.setSpaceId(new SpacePermId("CISD")); sampleCreation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
     * sampleCreation.setTypeId(new EntityTypePermId("CELL_PLATE")); v3api.createSamples(sessionToken, Arrays.asList(sampleCreation));
     * ExperimentUpdate experimentUpdate = new ExperimentUpdate(); experimentUpdate.setExperimentId(experimentId); ProjectIdentifier projectId = new
     * ProjectIdentifier("/CISD/NOE"); experimentUpdate.setProjectId(projectId); v3api.updateExperiments(sessionToken,
     * Arrays.asList(experimentUpdate)); ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions(); fetchOptions.withProject();
     * fetchOptions.withSamples().withProject(); Map<IExperimentId, Experiment> map = v3api.mapExperiments(sessionToken, Arrays.asList(new
     * ExperimentIdentifier("CISD", "NOE", "EXP1")), fetchOptions); List<Experiment> experiments = new ArrayList<Experiment>(map.values());
     * List<Sample> samples = experiments.get(0).getSamples(); assertEquals(experiments.get(0).getProject().getCode(), "NOE");
     * assertEquals(samples.get(0).getProject().getCode(), "NOE"); AssertionUtil.assertCollectionSize(samples, 1);
     * AssertionUtil.assertCollectionSize(experiments, 1); }
     */
    @Test
    public void testUpdateWithProjectNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("TEST_EXPERIMENT");
        creation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty("DESCRIPTION", "a description");

        List<ExperimentPermId> ids = v3api.createExperiments(sessionToken, Arrays.asList(creation));

        final ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(ids.get(0));
        update.setProjectId(null);

        assertUserFailureException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.updateExperiments(sessionToken, Arrays.asList(update));
            }
        }, "Project id cannot be null");
    }

    @Test
    public void testUpdateWithProjectInDifferentSpace()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentCreation experimentCreation = new ExperimentCreation();
        experimentCreation.setCode("TEST_EXPERIMENT_WITH_SAMPLES");
        experimentCreation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        experimentCreation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        experimentCreation.setProperty("DESCRIPTION", "a description");

        List<ExperimentPermId> experimentIds =
                v3api.createExperiments(sessionToken, Arrays.asList(experimentCreation));
        IExperimentId experimentId = experimentIds.get(0);

        SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setCode("TEST_SAMPLE_WITH_EXPERIMENT");
        sampleCreation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        sampleCreation.setSpaceId(new SpacePermId("CISD"));
        sampleCreation.setExperimentId(experimentId);

        List<SamplePermId> sampleIds =
                v3api.createSamples(sessionToken, Arrays.asList(sampleCreation));
        ISampleId sampleId = sampleIds.get(0);

        final ExperimentUpdate experimentUpdate = new ExperimentUpdate();
        experimentUpdate.setExperimentId(experimentId);
        experimentUpdate.setProjectId(new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT"));

        v3api.updateExperiments(sessionToken, Arrays.asList(experimentUpdate));

        SampleFetchOptions sampleFetchOptions = new SampleFetchOptions();
        sampleFetchOptions.withSpace();
        sampleFetchOptions.withExperiment();

        Map<ISampleId, Sample> sampleMap =
                v3api.getSamples(sessionToken, sampleIds, sampleFetchOptions);
        Sample sample = sampleMap.get(sampleId);

        assertEquals(sample.getSpace().getCode(), "TEST-SPACE");
        assertEquals(sample.getExperiment().getPermId(), experimentId);
        assertEquals(sample.getExperiment().getIdentifier().getIdentifier(),
                "/TEST-SPACE/TEST-PROJECT/TEST_EXPERIMENT_WITH_SAMPLES");
    }

    @Test
    public void testUpdateWithProjectUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final IExperimentId experimentId = new ExperimentPermId("200902091255058-1037");
        final IProjectId projectId = new ProjectIdentifier("/CISD/NEMO");
        final ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(experimentId);
        update.setProjectId(projectId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.updateExperiments(sessionToken, Arrays.asList(update));
            }
        }, projectId);
    }

    @Test
    public void testUpdateWithProjectNonexistent()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final IExperimentId experimentId = new ExperimentPermId("200902091255058-1037");
        final IProjectId projectId = new ProjectIdentifier("IDONTEXIST");
        final ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(experimentId);
        update.setProjectId(projectId);

        assertObjectNotFoundException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.updateExperiments(sessionToken, Arrays.asList(update));
            }
        }, projectId);
    }

    @Test
    public void testUpdateWithProperties()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("TEST_EXPERIMENT");
        creation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty("DESCRIPTION", "description 1");

        List<ExperimentPermId> ids = v3api.createExperiments(sessionToken, Arrays.asList(creation));

        ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(ids.get(0));
        update.setProperty("DESCRIPTION", "description 2");

        v3api.updateExperiments(sessionToken, Arrays.asList(update));

        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProperties();
        Map<IExperimentId, Experiment> map = v3api.getExperiments(sessionToken, ids, fetchOptions);
        List<Experiment> experiments = new ArrayList<Experiment>(map.values());

        AssertionUtil.assertCollectionSize(experiments, 1);

        Experiment experiment = experiments.get(0);
        assertEquals(1, experiment.getProperties().size());
        assertEquals("description 2", experiment.getProperties().get("DESCRIPTION"));
    }

    @Test
    public void testUpdateWithPropertyCodeNonexistent()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("TEST_EXPERIMENT");
        creation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty("DESCRIPTION", "a description");

        List<ExperimentPermId> ids = v3api.createExperiments(sessionToken, Arrays.asList(creation));

        final ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(ids.get(0));
        update.setProperty("NONEXISTENT_PROPERTY_CODE", "any value");

        assertUserFailureException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.updateExperiments(sessionToken, Arrays.asList(update));
            }
        }, "Property type with code 'NONEXISTENT_PROPERTY_CODE' does not exist");
    }

    @Test
    public void testUpdateWithPropertyValueIncorrect()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("TEST_EXPERIMENT");
        creation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty("DESCRIPTION", "a description");

        List<ExperimentPermId> ids = v3api.createExperiments(sessionToken, Arrays.asList(creation));

        final ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(ids.get(0));
        update.setProperty("PURCHASE_DATE", "this should be a date");

        assertUserFailureException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.updateExperiments(sessionToken, Arrays.asList(update));
            }
        }, "Date value 'this should be a date' has improper format");
    }

    @Test
    public void testUpdateWithPropertyValueMandatoryButNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final ExperimentPermId permId = createExperimentWithoutTags();

        final ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(permId);
        update.setProperty("DESCRIPTION", null);

        assertUserFailureException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.updateExperiments(sessionToken, Arrays.asList(update));
            }
        }, "Value of mandatory property 'DESCRIPTION' not specified");
    }

    @Test
    public void testUpdateWithAttachmentsAddNonexistent()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentPermId experimentId = createExperimentWithoutAttachments();

        assertAttachments(experimentId);

        AttachmentCreation attachmentCreation = new AttachmentCreation();
        attachmentCreation.setFileName("test_file");
        attachmentCreation.setTitle("test_title");
        attachmentCreation.setDescription("test_description");
        attachmentCreation.setContent(new String("test_content").getBytes());

        ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(experimentId);
        update.getAttachments().add(attachmentCreation);

        v3api.updateExperiments(sessionToken, Arrays.asList(update));

        assertAttachments(experimentId, attachmentCreation);
    }

    @Test
    public void testUpdateWithAttachmentsAddExistent()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        AttachmentCreation attachmentCreation = new AttachmentCreation();
        attachmentCreation.setFileName("test_file");
        attachmentCreation.setTitle("test_title");
        attachmentCreation.setDescription("test_description");
        attachmentCreation.setContent(new String("test_content").getBytes());

        ExperimentPermId experimentId = createExperimentWithAttachments(attachmentCreation);

        Map<String, Attachment> attachmentMap = assertAttachments(experimentId, attachmentCreation);
        Attachment attachment = attachmentMap.get(attachmentCreation.getFileName());

        assertEquals(attachment.getVersion(), Integer.valueOf(1));

        attachmentCreation.setTitle("test_title_2");
        attachmentCreation.setDescription("test_description_2");
        attachmentCreation.setContent(new String("test_content_2").getBytes());

        ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(experimentId);
        update.getAttachments().add(attachmentCreation);

        v3api.updateExperiments(sessionToken, Arrays.asList(update));

        attachmentMap = assertAttachments(experimentId, attachmentCreation);
        attachment = attachmentMap.get(attachmentCreation.getFileName());

        assertEquals(attachment.getVersion(), Integer.valueOf(2));
    }

    @Test
    public void testUpdateWithAttachmentsRemoveNonexistent()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        AttachmentCreation attachmentCreation = new AttachmentCreation();
        attachmentCreation.setFileName("test_file");
        attachmentCreation.setTitle("test_title");
        attachmentCreation.setDescription("test_description");
        attachmentCreation.setContent(new String("test_content").getBytes());

        ExperimentPermId experimentId = createExperimentWithAttachments(attachmentCreation);

        assertAttachments(experimentId, attachmentCreation);

        ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(experimentId);
        update.getAttachments().remove(new AttachmentFileName("test_file_2"));

        v3api.updateExperiments(sessionToken, Arrays.asList(update));

        assertAttachments(experimentId, attachmentCreation);
    }

    @Test
    public void testUpdateWithAttachmentsRemoveExistent()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        AttachmentCreation attachmentCreation = new AttachmentCreation();
        attachmentCreation.setFileName("test_file");
        attachmentCreation.setTitle("test_title");
        attachmentCreation.setDescription("test_description");
        attachmentCreation.setContent(new String("test_content").getBytes());

        ExperimentPermId experimentId = createExperimentWithAttachments(attachmentCreation);

        assertAttachments(experimentId, attachmentCreation);

        ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(experimentId);
        update.getAttachments().remove(new AttachmentFileName("test_file"));

        v3api.updateExperiments(sessionToken, Arrays.asList(update));

        assertAttachments(experimentId);
    }

    @Test
    public void testUpdateWithAttachmentsSet()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        AttachmentCreation attachmentCreation1 = new AttachmentCreation();
        attachmentCreation1.setFileName("test_file");
        attachmentCreation1.setTitle("test_title");
        attachmentCreation1.setDescription("test_description");
        attachmentCreation1.setContent(new String("test_content").getBytes());

        AttachmentCreation attachmentCreation2 = new AttachmentCreation();
        attachmentCreation2.setFileName("test_file_2");
        attachmentCreation2.setTitle("test_title_2");
        attachmentCreation2.setDescription("test_description_2");
        attachmentCreation2.setContent(new String("test_content_2").getBytes());

        ExperimentPermId experimentId =
                createExperimentWithAttachments(attachmentCreation1, attachmentCreation2);

        assertAttachments(experimentId, attachmentCreation1, attachmentCreation2);

        attachmentCreation2.setTitle("test_title_3");
        attachmentCreation2.setDescription("test_description_3");
        attachmentCreation2.setContent(new String("test_content_3").getBytes());

        ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(experimentId);
        update.getAttachments().set(attachmentCreation2);

        v3api.updateExperiments(sessionToken, Arrays.asList(update));

        Map<String, Attachment> attachmentMap =
                assertAttachments(experimentId, attachmentCreation2);
        Attachment attachment = attachmentMap.get(attachmentCreation2.getFileName());

        assertEquals(attachment.getVersion(), Integer.valueOf(2));
    }

    @Test
    public void testUpdateWithTagsAddExisting()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentPermId experimentId = createExperimentWithTags(new TagCode("TEST_TAG_1"));

        ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(experimentId);
        update.getTagIds().add(new TagCode("TEST_TAG_2"));

        v3api.updateExperiments(sessionToken, Arrays.asList(update));

        assertTags(experimentId, "/test/TEST_TAG_1", "/test/TEST_TAG_2");
    }

    @Test
    public void testUpdateWithTagsAddNonexistent()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final ExperimentPermId experimentId = createExperimentWithoutTags();
        final TagPermId tagId = new TagPermId("/test/THIS_TAG_SHOULD_BE_CREATED");
        final ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(experimentId);
        update.getTagIds().add(tagId);

        v3api.updateExperiments(sessionToken, Arrays.asList(update));

        assertTags(experimentId, "/test/THIS_TAG_SHOULD_BE_CREATED");
    }

    @Test
    public void testUpdateWithTagsAddUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final ExperimentPermId experimentId = createExperimentWithoutTags();

        final TagPermId tagId = new TagPermId("/test_space/TEST_METAPROJECTS");
        final ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(experimentId);
        update.getTagIds().add(tagId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.updateExperiments(sessionToken, Arrays.asList(update));
            }
        }, tagId);
    }

    @Test
    public void testUpdateWithTagsRemoveExisting()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentPermId experimentId = createExperimentWithTags(new TagCode("TEST_TAG_1"),
                new TagPermId("/test/TEST_TAG_2"));

        ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(experimentId);
        update.getTagIds().remove(new TagPermId("/test/TEST_TAG_1"));

        v3api.updateExperiments(sessionToken, Arrays.asList(update));

        assertTags(experimentId, "/test/TEST_TAG_2");
    }

    @Test
    public void testUpdateWithTagsRemoveNonexistent()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentPermId experimentId = createExperimentWithTags(new TagCode("TEST_TAG_1"),
                new TagPermId("/test/TEST_TAG_2"));

        ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(experimentId);
        update.getTagIds().remove(new TagPermId("/test/THIS_TAG_DOES_NOT_EXIST"));

        v3api.updateExperiments(sessionToken, Arrays.asList(update));

        assertTags(experimentId, "/test/TEST_TAG_1", "/test/TEST_TAG_2");
    }

    @Test
    public void testUpdateWithTagsRemoveUnassigned()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentPermId experimentId = createExperimentWithTags(new TagCode("TEST_TAG_1"),
                new TagPermId("/test/TEST_TAG_2"));

        ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(experimentId);
        update.getTagIds().remove(new TagPermId("/test/TEST_METAPROJECTS"));

        v3api.updateExperiments(sessionToken, Arrays.asList(update));

        assertTags(experimentId, "/test/TEST_TAG_1", "/test/TEST_TAG_2");
    }

    @Test
    public void testUpdateWithTagsRemoveUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_POWER_USER_CISD, PASSWORD);

        final ITagId tagId = new TagPermId("/test/TEST_TAG_1");
        final ExperimentPermId experimentId = createExperimentWithTags(tagId);

        final ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(experimentId);
        update.getTagIds().remove(tagId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.updateExperiments(sessionToken, Arrays.asList(update));
            }
        }, tagId);
    }

    @Test
    public void testUpdateWithTagsSetExisting()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentPermId experimentId = createExperimentWithTags(new TagCode("TEST_TAG_1"));

        ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(experimentId);
        update.getTagIds().set(new TagPermId("/test/TEST_METAPROJECTS"));

        v3api.updateExperiments(sessionToken, Arrays.asList(update));

        assertTags(experimentId, "/test/TEST_METAPROJECTS");
    }

    @Test
    public void testUpdateWithTagsSetNonexistent()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentPermId experimentId = createExperimentWithTags(new TagCode("TEST_TAG_1"));

        ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(experimentId);
        update.getTagIds().set(new TagCode("THIS_TAG_DOES_NOT_EXIST"));

        v3api.updateExperiments(sessionToken, Arrays.asList(update));

        assertTags(experimentId, "/test/THIS_TAG_DOES_NOT_EXIST");
    }

    @Test
    public void testUpdateWithTagsSetUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentPermId experimentId = createExperimentWithTags(new TagCode("TEST_TAG_1"));

        final ITagId tagId = new TagPermId("/test_space/TEST_METAPROJECTS");
        final ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(experimentId);
        update.getTagIds().set(tagId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.updateExperiments(sessionToken, Arrays.asList(update));
            }
        }, tagId);
    }

    @Test
    public void testUpdateWithAdminUserInAnotherSpace()
    {
        final ExperimentPermId permId = new ExperimentPermId("200902091255058-1037");

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                String sessionToken = v3api.login(TEST_ROLE_V3, PASSWORD);

                final ExperimentUpdate update = new ExperimentUpdate();
                update.setExperimentId(permId);

                v3api.updateExperiments(sessionToken, Collections.singletonList(update));
            }
        }, permId);
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testUpdateWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        AttachmentCreation attachment = new AttachmentCreation();
        attachment.setContent("test content".getBytes());
        attachment.setFileName("test.txt");

        ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));
        update.setProperty("DESCRIPTION", "an updated description");
        update.getAttachments().add(attachment);

        String sessionToken = v3api.login(user.getUserId(), PASSWORD);

        if (user.isDisabledProjectUser())
        {
            assertAuthorizationFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateExperiments(sessionToken, Collections.singletonList(update));
                }
            });
        } else if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            v3api.updateExperiments(sessionToken, Collections.singletonList(update));
        } else
        {
            assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateExperiments(sessionToken, Collections.singletonList(update));
                }
            }, update.getExperimentId());
        }
    }

    @Test
    public void testFreezeForSamples()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentIdentifier expId1 = new ExperimentIdentifier("/CISD/NEMO/EXP10");
        ExperimentIdentifier expId2 = new ExperimentIdentifier("/CISD/NEMO/EXP11");
        ExperimentUpdate update1 = new ExperimentUpdate();
        update1.setExperimentId(expId1);
        update1.freeze();
        ExperimentUpdate update2 = new ExperimentUpdate();
        update2.setExperimentId(expId2);
        update2.freezeForSamples();

        // When
        v3api.updateExperiments(sessionToken, Arrays.asList(update1, update2));

        // Then
        Map<IExperimentId, Experiment> experiments =
                v3api.getExperiments(sessionToken, Arrays.asList(expId1, expId2),
                        new ExperimentFetchOptions());
        Experiment experiment1 = experiments.get(expId1);
        assertEquals(experiment1.getIdentifier().getIdentifier(), expId1.getIdentifier());
        assertEquals(experiment1.isFrozen(), true);
        assertEquals(experiment1.isFrozenForDataSets(), false);
        assertEquals(experiment1.isFrozenForSamples(), false);
        assertFreezingEvent(TEST_USER, experiment1.getIdentifier().getIdentifier(),
                EntityType.EXPERIMENT, new FreezingFlags().freeze());
        Experiment experiment2 = experiments.get(expId2);
        assertEquals(experiment2.getIdentifier().getIdentifier(), expId2.getIdentifier());
        assertEquals(experiment2.isFrozen(), true);
        assertEquals(experiment2.isFrozenForDataSets(), false);
        assertEquals(experiment2.isFrozenForSamples(), true);
        assertFreezingEvent(TEST_USER, experiment2.getIdentifier().getIdentifier(),
                EntityType.EXPERIMENT,
                new FreezingFlags().freeze().freezeForSamples());
    }

    @Test
    public void testFreezeForDataSets()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentIdentifier expId1 = new ExperimentIdentifier("/CISD/NEMO/EXP10");
        ExperimentIdentifier expId2 = new ExperimentIdentifier("/CISD/NEMO/EXP11");
        ExperimentUpdate update1 = new ExperimentUpdate();
        update1.setExperimentId(expId1);
        update1.freeze();
        ExperimentUpdate update2 = new ExperimentUpdate();
        update2.setExperimentId(expId2);
        update2.freezeForDataSets();

        // When
        v3api.updateExperiments(sessionToken, Arrays.asList(update1, update2));

        // Then
        Map<IExperimentId, Experiment> experiments =
                v3api.getExperiments(sessionToken, Arrays.asList(expId1, expId2),
                        new ExperimentFetchOptions());
        Experiment experiment1 = experiments.get(expId1);
        assertEquals(experiment1.getIdentifier().getIdentifier(), expId1.getIdentifier());
        assertEquals(experiment1.isFrozen(), true);
        assertEquals(experiment1.isFrozenForDataSets(), false);
        assertEquals(experiment1.isFrozenForSamples(), false);
        assertFreezingEvent(TEST_USER, experiment1.getIdentifier().getIdentifier(),
                EntityType.EXPERIMENT, new FreezingFlags().freeze());
        Experiment experiment2 = experiments.get(expId2);
        assertEquals(experiment2.getIdentifier().getIdentifier(), expId2.getIdentifier());
        assertEquals(experiment2.isFrozen(), true);
        assertEquals(experiment2.isFrozenForDataSets(), true);
        assertEquals(experiment2.isFrozenForSamples(), false);
        assertFreezingEvent(TEST_USER, experiment2.getIdentifier().getIdentifier(),
                EntityType.EXPERIMENT,
                new FreezingFlags().freeze().freezeForDataSets());
    }

    @Test
    public void testFreezing()
    {
        // Given
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentIdentifier expId = new ExperimentIdentifier("/CISD/NEMO/EXP10");
        ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(expId);
        update.freeze();
        v3api.updateExperiments(sessionToken, Arrays.asList(update));
        ExperimentUpdate update2 = new ExperimentUpdate();
        update2.setExperimentId(expId);
        update2.setProperty("DESCRIPTION", "new description");

        // When
        assertUserFailureException(
                Void -> v3api.updateExperiments(sessionToken, Arrays.asList(update2)),
                // Then
                "ERROR: Operation UPDATE PROPERTY is not allowed because experiment EXP10 is frozen.");
    }

    @Test
    public void testFreezingForSampleCreations()
    {
        // Given
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentIdentifier expId = new ExperimentIdentifier("/CISD/NEMO/EXP10");
        ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(expId);
        update.freezeForSamples();
        v3api.updateExperiments(sessionToken, Arrays.asList(update));
        SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setExperimentId(expId);
        sampleCreation.setTypeId(new EntityTypePermId("NORMAL", EntityKind.SAMPLE));
        sampleCreation.setCode(PREFIX + "S1");

        // When
        assertUserFailureException(
                Void -> v3api.createSamples(sessionToken, Arrays.asList(sampleCreation)),
                // Then
                "ERROR: Operation SET EXPERIMENT is not allowed because experiment EXP10 is frozen for sample UET-S1.");
    }

    @Test
    public void testFreezingForSampleDeletions()
    {
        // Given
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentIdentifier expId = new ExperimentIdentifier("/CISD/NEMO/EXP10");
        SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setExperimentId(expId);
        sampleCreation.setTypeId(new EntityTypePermId("NORMAL", EntityKind.SAMPLE));
        sampleCreation.setCode(PREFIX + "S1");
        sampleCreation.setSpaceId(new SpacePermId("CISD"));
        SamplePermId sampleId =
                v3api.createSamples(sessionToken, Arrays.asList(sampleCreation)).get(0);
        ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(expId);
        update.freezeForSamples();
        v3api.updateExperiments(sessionToken, Arrays.asList(update));
        SampleDeletionOptions deletionOptions = new SampleDeletionOptions();
        deletionOptions.setReason("test");

        // When
        assertUserFailureException(
                Void -> v3api.deleteSamples(sessionToken, Arrays.asList(sampleId), deletionOptions),
                // Then
                "ERROR: Operation DELETE SAMPLE is not allowed because experiment EXP10 is frozen");
    }

    @Test
    public void testFreezingForDataSetCreations()
    {
        // Given
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentIdentifier expId = new ExperimentIdentifier("/CISD/NEMO/EXP10");
        ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(expId);
        update.freezeForDataSets();
        v3api.updateExperiments(sessionToken, Arrays.asList(update));
        DataSetCreation dataSetCreation = new DataSetCreation();
        dataSetCreation.setCode(PREFIX + "D1");
        dataSetCreation.setTypeId(
                new EntityTypePermId("DELETION_TEST_CONTAINER", EntityKind.DATA_SET));
        dataSetCreation.setDataStoreId(new DataStorePermId("STANDARD"));
        dataSetCreation.setDataSetKind(DataSetKind.CONTAINER);
        dataSetCreation.setExperimentId(expId);

        // When
        assertUserFailureException(
                Void -> v3api.createDataSets(sessionToken, Arrays.asList(dataSetCreation)),
                // Then
                "ERROR: Operation SET EXPERIMENT is not allowed because experiment EXP10 is frozen for data set UET-D1.");
    }

    @Test
    public void testFreezingForDataSetDeletions()
    {
        // Given
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentIdentifier expId = new ExperimentIdentifier("/CISD/NEMO/EXP10");
        DataSetCreation dataSetCreation = new DataSetCreation();
        dataSetCreation.setCode(PREFIX + "D1");
        dataSetCreation.setTypeId(
                new EntityTypePermId("DELETION_TEST_CONTAINER", EntityKind.DATA_SET));
        dataSetCreation.setDataStoreId(new DataStorePermId("STANDARD"));
        dataSetCreation.setDataSetKind(DataSetKind.CONTAINER);
        dataSetCreation.setExperimentId(expId);
        DataSetPermId dataSetId =
                v3api.createDataSets(sessionToken, Arrays.asList(dataSetCreation)).get(0);
        ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(expId);
        update.freezeForDataSets();
        v3api.updateExperiments(sessionToken, Arrays.asList(update));
        DataSetDeletionOptions deletionOptions = new DataSetDeletionOptions();
        deletionOptions.setReason("test");

        // When
        assertUserFailureException(
                Void -> v3api.deleteDataSets(sessionToken, Arrays.asList(dataSetId),
                        deletionOptions),
                // Then
                "ERROR: Operation DELETE DATA SET is not allowed because experiment EXP10 is frozen.");
    }

    @Test(dataProvider = "freezeMethods")
    public void testUnauthorizedFreezing(MethodWrapper freezeMethod) throws Exception
    {
        // Given
        RoleAssignmentCreation roleAssignmentCreation = new RoleAssignmentCreation();
        roleAssignmentCreation.setRole(Role.ADMIN);
        roleAssignmentCreation.setSpaceId(new SpacePermId("TEST-SPACE"));
        roleAssignmentCreation.setUserId(new PersonPermId(TEST_POWER_USER_CISD));
        v3api.createRoleAssignments(systemSessionToken, Arrays.asList(roleAssignmentCreation));
        final String sessionToken = v3api.login(TEST_POWER_USER_CISD, PASSWORD);
        ExperimentIdentifier expId = new ExperimentIdentifier("/CISD/NEMO/EXP10");
        ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(expId);
        freezeMethod.method.invoke(update);

        // When
        assertAuthorizationFailureException(
                Void -> v3api.updateExperiments(sessionToken, Arrays.asList(update)), null);
    }

    @DataProvider(name = "freezeMethods")
    public static Object[][] freezeMethods()
    {
        return asCartesianProduct(getFreezingMethods(ExperimentUpdate.class));
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));

        ExperimentUpdate update2 = new ExperimentUpdate();
        update2.setExperimentId(new ExperimentPermId("200811050940555-1032"));

        v3api.updateExperiments(sessionToken, Arrays.asList(update, update2));

        assertAccessLog(
                "update-experiments  EXPERIMENT_UPDATES('[ExperimentUpdate[experimentId=/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST], ExperimentUpdate[experimentId=200811050940555-1032]]')");
    }

    @Test
    public void testUpdateWithUnknownPropertyOfTypeSample()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("EXPERIMENT_WITH_SAMPLE_PROPERTY");
        creation.setTypeId(new EntityTypePermId("DELETION_TEST"));
        creation.setProjectId(new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT"));
        ExperimentPermId experimentPermId =
                v3api.createExperiments(sessionToken, Arrays.asList(creation)).get(0);

        ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(experimentPermId);
        update.setProperty("PLATE", "200811050924898-997");

        // When
        assertUserFailureException(
                Void -> v3api.updateExperiments(sessionToken, Arrays.asList(update)),
                // Then
                "Property type with code 'PLATE' does not exist");
    }

    @Test
    public void testUpdateWithPropertyOfTypeSampleWithUnknownSample()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createASamplePropertyType(sessionToken, null);
        EntityTypePermId experimentType = createAnExperimentType(sessionToken, false, propertyType);
        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("EXPERIMENT_WITH_SAMPLE_PROPERTY");
        creation.setTypeId(experimentType);
        creation.setProjectId(new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT"));
        creation.setProperty(propertyType.getPermId(), "/CISD/CL1");
        ExperimentPermId experimentPermId =
                v3api.createExperiments(sessionToken, Arrays.asList(creation)).get(0);

        ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(experimentPermId);
        update.setProperty(propertyType.getPermId(), "/CISD/UNKNOWN");

        // When
        assertUserFailureException(
                Void -> v3api.updateExperiments(sessionToken, Arrays.asList(update)),
                // Then
                "Unknown sample: /CISD/UNKNOWN");
    }

    @Test
    public void testUpdateWithPropertyOfTypeSampleWithSampleOfWrongType()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createASamplePropertyType(sessionToken,
                new EntityTypePermId("WELL", EntityKind.SAMPLE));
        EntityTypePermId experimentType = createAnExperimentType(sessionToken, false, propertyType);
        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("EXPERIMENT_WITH_SAMPLE_PROPERTY");
        creation.setTypeId(experimentType);
        creation.setProjectId(new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT"));
        creation.setProperty(propertyType.getPermId(), "200811050917877-438");
        ExperimentPermId experimentPermId =
                v3api.createExperiments(sessionToken, Arrays.asList(creation)).get(0);

        ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(experimentPermId);
        update.setProperty(propertyType.getPermId(), "200811050919915-8");

        // When
        assertUserFailureException(
                Void -> v3api.updateExperiments(sessionToken, Arrays.asList(update)),
                // Then
                "Property " + propertyType.getPermId() + " is not a sample of type WELL but of type CONTROL_LAYOUT");
    }

    @Test
    public void testUpdateWithPropertyOfTypeSampleWithSampleNotAccessable()
    {
        // Given
        String adminSessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createASamplePropertyType(adminSessionToken, null);
        EntityTypePermId experimentType =
                createAnExperimentType(adminSessionToken, false, propertyType);
        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("EXPERIMENT_WITH_SAMPLE_PROPERTY");
        creation.setTypeId(experimentType);
        creation.setProjectId(new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT"));
        creation.setProperty(propertyType.getPermId(), "/MP");
        ExperimentPermId experimentPermId =
                v3api.createExperiments(adminSessionToken, Arrays.asList(creation)).get(0);

        String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);
        ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(experimentPermId);
        update.setProperty(propertyType.getPermId(), "/CISD/CL1");

        // When
        assertUserFailureException(
                Void -> v3api.updateExperiments(sessionToken, Arrays.asList(update)),
                // Then
                "Unknown sample: /CISD/CL1");
    }

    @Test
    public void testUpdateAddSampleProperty()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createASamplePropertyType(sessionToken, null);
        EntityTypePermId experimentType = createAnExperimentType(sessionToken, false, propertyType);
        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("EXPERIMENT_WITH_SAMPLE_PROPERTY");
        creation.setTypeId(experimentType);
        creation.setProjectId(new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT"));
        ExperimentPermId experimentPermId =
                v3api.createExperiments(sessionToken, Arrays.asList(creation)).get(0);

        ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(experimentPermId);
        update.setProperty(propertyType.getPermId(), "200811050924898-997");

        // When
        v3api.updateExperiments(sessionToken, Arrays.asList(update));

        // Then
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        Experiment experiment =
                v3api.getExperiments(sessionToken, Arrays.asList(experimentPermId), fetchOptions)
                        .get(experimentPermId);
        assertEquals(experiment.getProperties().toString(),
                "{" + propertyType.getPermId() + "=200811050924898-997}");
        assertEquals(experiment.getSampleProperties().get(propertyType.getPermId()).length, 1);
        assertEquals(experiment.getSampleProperties().get(propertyType.getPermId())[0].toString(), "Sample 200811050924898-997");
    }

    @Test
    public void testUpdateChangeSampleProperty()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createASamplePropertyType(sessionToken, null);
        EntityTypePermId experimentType = createAnExperimentType(sessionToken, false, propertyType);
        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("EXPERIMENT_WITH_SAMPLE_PROPERTY");
        creation.setTypeId(experimentType);
        creation.setProjectId(new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT"));
        creation.setProperty(propertyType.getPermId(), "200811050919915-8");
        ExperimentPermId experimentPermId =
                v3api.createExperiments(sessionToken, Arrays.asList(creation)).get(0);

        ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(experimentPermId);
        update.setProperty(propertyType.getPermId(), "200811050924898-997");

        // When
        v3api.updateExperiments(sessionToken, Arrays.asList(update));

        // Then
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        fetchOptions.withHistory().withAuthor();
        Experiment experiment =
                v3api.getExperiments(sessionToken, Arrays.asList(experimentPermId), fetchOptions)
                        .get(experimentPermId);
        assertEquals(experiment.getProperties().toString(),
                "{" + propertyType.getPermId() + "=200811050924898-997}");
        assertEquals(experiment.getSampleProperties().get(propertyType.getPermId()).length, 1);
        assertEquals(experiment.getSampleProperties().get(propertyType.getPermId())[0].toString(), "Sample 200811050924898-997");

        List<HistoryEntry> history = experiment.getHistory();
        assertEquals(history.size(), 3);

        assertPropertyHistory(history.get(0), propertyType.getPermId(), "200811050919915-8",
                experiment.getRegistrationDate(),
                experiment.getModificationDate());
        assertPropertyHistory(history.get(1), propertyType.getPermId(), "200811050924898-997",
                experiment.getRegistrationDate(), null);
        assertRelationshipHistory(history.get(2), new ProjectPermId("20120814110011738-105"),
                ExperimentRelationType.PROJECT,
                experiment.getRegistrationDate(), null);
    }

    @Test
    public void testUpdateRemoveSampleProperty()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createASamplePropertyType(sessionToken, null);
        EntityTypePermId experimentType = createAnExperimentType(sessionToken, false, propertyType);
        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("EXPERIMENT_WITH_SAMPLE_PROPERTY");
        creation.setTypeId(experimentType);
        creation.setProjectId(new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT"));
        creation.setProperty(propertyType.getPermId(), "200811050919915-8");
        ExperimentPermId experimentPermId =
                v3api.createExperiments(sessionToken, Arrays.asList(creation)).get(0);

        ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(experimentPermId);
        update.setProperty(propertyType.getPermId(), null);

        // When
        v3api.updateExperiments(sessionToken, Arrays.asList(update));

        // Then
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        fetchOptions.withHistory().withAuthor();
        Experiment experiment =
                v3api.getExperiments(sessionToken, Arrays.asList(experimentPermId), fetchOptions)
                        .get(experimentPermId);
        assertEquals(experiment.getProperties().toString(), "{}");
        assertEquals(experiment.getSampleProperties().toString(), "{}");

        List<HistoryEntry> history = experiment.getHistory();
        assertEquals(history.size(), 2);

        assertPropertyHistory(history.get(0), propertyType.getPermId(), "200811050919915-8",
                experiment.getRegistrationDate(),
                experiment.getModificationDate());
        assertRelationshipHistory(history.get(1), new ProjectPermId("20120814110011738-105"),
                ExperimentRelationType.PROJECT,
                experiment.getRegistrationDate(), null);
    }

    @Test
    public void testUpdateRemoveMandatorySampleProperty()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createASamplePropertyType(sessionToken, null);
        EntityTypePermId experimentType = createAnExperimentType(sessionToken, true, propertyType);
        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("EXPERIMENT_WITH_SAMPLE_PROPERTY");
        creation.setTypeId(experimentType);
        creation.setProjectId(new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT"));
        creation.setProperty(propertyType.getPermId(), "200811050919915-8");
        ExperimentPermId experimentPermId =
                v3api.createExperiments(sessionToken, Arrays.asList(creation)).get(0);

        ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(experimentPermId);
        update.setProperty(propertyType.getPermId(), null);

        // When
        assertUserFailureException(
                Void -> v3api.updateExperiments(sessionToken, Arrays.asList(update)),
                "Property " + propertyType + " of entity type " + experimentType.getPermId()
                        + " can not be deleted because it is mandatory.");
    }

    @Test
    public void testUpdateMetaData()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        // Prepare
        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("TEST_EXPERIMENT_META_DATA");
        creation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty("DESCRIPTION", "a description");
        creation.setMetaData(Map.of("key_modify", "value_modify", "key_delete", "value_delete"));

        List<ExperimentPermId> ids = v3api.createExperiments(sessionToken, Arrays.asList(creation));

        final ExperimentPermId permId = ids.get(0);

        // Act
        final ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(permId);
        update.getMetaData().put("key_modify", "new_value");
        update.getMetaData().add(Map.of("key_add", "value_add"));
        update.getMetaData().remove("key_delete");

        v3api.updateExperiments(sessionToken, Arrays.asList(update));

        // Verify
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        fetchOptions.withHistory().withAuthor();
        Experiment experiment =
                v3api.getExperiments(sessionToken, Arrays.asList(permId), fetchOptions).get(permId);

        assertEquals(experiment.getMetaData(),
                Map.of("key_modify", "new_value", "key_add", "value_add"));
    }

    @Test
    public void testUpdateMetaDataSetEmpty()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        // Prepare
        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("TEST_EXPERIMENT_META_DATA");
        creation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty("DESCRIPTION", "a description");
        creation.setMetaData(Map.of("key_modify", "value_modify", "key_delete", "value_delete"));

        List<ExperimentPermId> ids = v3api.createExperiments(sessionToken, Arrays.asList(creation));

        final ExperimentPermId permId = ids.get(0);

        // Act
        final ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(permId);
        update.getMetaData().set(Map.of());

        v3api.updateExperiments(sessionToken, Arrays.asList(update));

        // Verify
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        fetchOptions.withHistory().withAuthor();
        Experiment experiment =
                v3api.getExperiments(sessionToken, Arrays.asList(permId), fetchOptions).get(permId);

        assertEquals(experiment.getMetaData(), Map.of());
    }

    @Test(dataProvider = USER_ROLES_PROVIDER)
    public void testUpdateWithDifferentRoles(RoleWithHierarchy role)
    {
        testWithUserRole(role, params ->
        {
            final ExperimentCreation experimentCreation = new ExperimentCreation();
            experimentCreation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
            experimentCreation.setCode("TEST_EXPERIMENT_" + UUID.randomUUID());
            experimentCreation.setProjectId(params.space1Project1Id);
            experimentCreation.setProperty("DESCRIPTION", "test description");

            final ExperimentPermId experimentId =
                    v3api.createExperiments(params.adminSessionToken, Collections.singletonList(experimentCreation)).get(0);

            final ExperimentUpdate experimentUpdate = new ExperimentUpdate();
            experimentUpdate.setExperimentId(experimentId);
            experimentUpdate.setProjectId(params.space1Project2Id);

            if (List.of(RoleWithHierarchy.RoleCode.ADMIN, RoleWithHierarchy.RoleCode.POWER_USER)
                    .contains(role.getRoleCode()))
            {
                v3api.updateExperiments(params.userSessionToken, Collections.singletonList(experimentUpdate));
            } else
            {
                assertAnyAuthorizationException(
                        () -> v3api.updateExperiments(params.userSessionToken, Collections.singletonList(experimentUpdate)));
            }
        });
    }

    @Test
    private ExperimentPermId createExperimentWithoutAttachments()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("TEST_EXPERIMENT");
        creation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty("DESCRIPTION", "a description");

        List<ExperimentPermId> ids = v3api.createExperiments(sessionToken, Arrays.asList(creation));
        return ids.get(0);
    }

    private ExperimentPermId createExperimentWithAttachments(AttachmentCreation... attachments)
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("TEST_EXPERIMENT");
        creation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty("DESCRIPTION", "a description");
        creation.setAttachments(Arrays.asList(attachments));

        List<ExperimentPermId> ids = v3api.createExperiments(sessionToken, Arrays.asList(creation));
        return ids.get(0);
    }

    private ExperimentPermId createExperimentWithoutTags()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("TEST_EXPERIMENT");
        creation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty("DESCRIPTION", "a description");

        List<ExperimentPermId> ids = v3api.createExperiments(sessionToken, Arrays.asList(creation));
        return ids.get(0);
    }

    private ExperimentPermId createExperimentWithTags(ITagId... tags)
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("TEST_EXPERIMENT");
        creation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty("DESCRIPTION", "a description");
        creation.setTagIds(Arrays.asList(tags));

        List<ExperimentPermId> ids = v3api.createExperiments(sessionToken, Arrays.asList(creation));
        return ids.get(0);
    }

    private void assertTags(IExperimentId experimentId, String... expectedTagPermIds)
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withTags();

        Map<IExperimentId, Experiment> experiments =
                v3api.getExperiments(sessionToken, Arrays.asList(experimentId), fetchOptions);
        assertEquals(experiments.size(), 1);

        assertTags(experiments.get(experimentId).getTags(), expectedTagPermIds);
    }

    private Map<String, Attachment> assertAttachments(IExperimentId experimentId,
            AttachmentCreation... expectedAttachments)
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withAttachments().withContent();

        Map<IExperimentId, Experiment> experiments =
                v3api.getExperiments(sessionToken, Arrays.asList(experimentId), fetchOptions);

        assertEquals(experiments.size(), 1);

        return assertAttachments(experiments.get(experimentId).getAttachments(),
                expectedAttachments);
    }
}

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
import static org.testng.Assert.assertEqualsNoOrder;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.*;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.Attachment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.create.AttachmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.Tag;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.ITagId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagPermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.systemtest.authorization.ProjectAuthorizationUser;
import junit.framework.Assert;

/**
 * @author pkupczyk
 */
public class CreateExperimentTest extends AbstractExperimentTest
{
    private static final PropertyTypePermId PLATE_GEOMETRY = new PropertyTypePermId("$PLATE_GEOMETRY");

    @Test
    public void testCreateWithIndexCheck()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentCreation experiment = new ExperimentCreation();
        experiment.setCode("TO_BE_REINDEXED");
        experiment.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        experiment.setProjectId(new ProjectPermId("20120814110011738-103"));
        experiment.setProperty("DESCRIPTION", "a description");

        List<ExperimentPermId> permIds = v3api.createExperiments(sessionToken, Arrays.asList(experiment));

        assertExperimentsExists(permIds.get(0).getPermId());
    }

    @Test
    public void testCreateWithCodeNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final ExperimentCreation experiment = new ExperimentCreation();
        experiment.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        experiment.setProjectId(new ProjectIdentifier("/TESTGROUP/TESTPROJ"));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createExperiments(sessionToken, Arrays.asList(experiment));
                }
            }, "Code cannot be empty");
    }

    @Test
    public void testCreateWithAdminUserInAnotherSpace()
    {
        final String code = "WILL-FAIL";
        final ExperimentIdentifier identifier = new ExperimentIdentifier("/TEST-SPACE/NOE/" + code);
        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken = v3api.login(TEST_ROLE_V3, PASSWORD);

                    final ExperimentCreation experiment = new ExperimentCreation();
                    experiment.setTypeId(new EntityTypePermId("SIRNA_HCS"));
                    experiment.setProjectId(new ProjectIdentifier("/TEST-SPACE/NOE"));
                    experiment.setCode(code);

                    v3api.createExperiments(sessionToken, Collections.singletonList(experiment));
                }
            }, identifier);
    }

    @Test
    public void testCreateWithCodeExisting()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final ExperimentCreation experiment = new ExperimentCreation();
        experiment.setCode("EXPERIMENT_WITH_EXISTING_CODE");
        experiment.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        experiment.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        experiment.setProperty("DESCRIPTION", "a description");

        v3api.createExperiments(sessionToken, Arrays.asList(experiment));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createExperiments(sessionToken, Arrays.asList(experiment));
                }
            }, "Experiment already exists in the database and needs to be unique");
    }

    @Test
    public void testCreateWithCodeIncorrect()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final ExperimentCreation experiment = new ExperimentCreation();
        experiment.setCode("?!*");
        experiment.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        experiment.setProjectId(new ProjectIdentifier("/TESTGROUP/TESTPROJ"));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createExperiments(sessionToken, Arrays.asList(experiment));
                }
            }, "The code '?!*' contains illegal characters");
    }

    @Test
    public void testCreateWithProjectNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final ExperimentCreation experiment = new ExperimentCreation();
        experiment.setCode("TEST_EXPERIMENT1");
        experiment.setTypeId(new EntityTypePermId("SIRNA_HCS"));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createExperiments(sessionToken, Arrays.asList(experiment));
                }
            }, "Project id cannot be null");
    }

    @Test
    public void testCreateWithProjectUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_POWER_USER_CISD, PASSWORD);

        final IProjectId projectId = new ProjectIdentifier("/TESTGROUP/TESTPROJ");
        final ExperimentCreation experiment = new ExperimentCreation();
        experiment.setCode("TEST_EXPERIMENT1");
        experiment.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        experiment.setProjectId(projectId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createExperiments(sessionToken, Arrays.asList(experiment));
                }
            }, projectId);
    }

    @Test
    public void testCreateWithProjectNonexistent()
    {
        final String sessionToken = v3api.login(TEST_POWER_USER_CISD, PASSWORD);

        final IProjectId projectId = new ProjectIdentifier("/TESTGROUP/IDONTEXIST");
        final ExperimentCreation experiment = new ExperimentCreation();
        experiment.setCode("TEST_EXPERIMENT1");
        experiment.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        experiment.setProjectId(projectId);

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createExperiments(sessionToken, Arrays.asList(experiment));
                }
            }, projectId);
    }

    @Test
    public void testCreateWithTypeNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final ExperimentCreation experiment = new ExperimentCreation();
        experiment.setCode("TEST_EXPERIMENT1");
        experiment.setProjectId(new ProjectIdentifier("/TESTGROUP/TESTPROJ"));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createExperiments(sessionToken, Arrays.asList(experiment));
                }
            }, "Type id cannot be null");
    }

    @Test
    public void testCreateWithTypeNonexistent()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final IEntityTypeId typeId = new EntityTypePermId("IDONTEXIST");
        final IProjectId projectId = new ProjectIdentifier("/TESTGROUP/TESTPROJ");
        final ExperimentCreation experiment = new ExperimentCreation();
        experiment.setCode("TEST_EXPERIMENT1");
        experiment.setTypeId(typeId);
        experiment.setProjectId(projectId);

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createExperiments(sessionToken, Arrays.asList(experiment));
                }
            }, typeId);
    }

    @Test
    public void testCreateWithTagExisting()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final Date now = new Date();

        final ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("TEST_EXPERIMENT1");
        creation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        creation.setProjectId(new ProjectIdentifier("/TESTGROUP/TESTPROJ"));
        creation.setProperty("DESCRIPTION", "a description");
        creation.setTagIds(Arrays.asList(new TagPermId("/test/TEST_METAPROJECTS")));

        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withTags();

        List<ExperimentPermId> permIds = v3api.createExperiments(sessionToken, Arrays.asList(creation));
        Map<IExperimentId, Experiment> map = v3api.getExperiments(sessionToken, permIds, fetchOptions);
        List<Experiment> experiments = new ArrayList<Experiment>(map.values());

        assertEquals(experiments.size(), 1);

        Experiment experiment = experiments.get(0);

        assertEquals(experiment.getIdentifier().getIdentifier(), "/TESTGROUP/TESTPROJ/TEST_EXPERIMENT1");
        assertEquals(experiment.getTags().size(), 1);

        Tag tag = experiment.getTags().iterator().next();

        assertEquals(tag.getCode(), "TEST_METAPROJECTS");
        assertEquals(tag.getPermId().getPermId(), "/test/TEST_METAPROJECTS");
        assertTrue(tag.getRegistrationDate().getTime() < now.getTime());
    }

    @Test
    public void testCreateWithSystemProperty()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        NewETPTAssignment assignment = new NewETPTAssignment();
        assignment.setPropertyTypeCode("$PLATE_GEOMETRY");
        assignment.setEntityTypeCode("SIRNA_HCS");
        assignment.setEntityKind(EntityKind.EXPERIMENT);
        assignment.setOrdinal(1000L);
        commonServer.assignPropertyType(sessionToken, assignment);

        final ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("TEST_EXPERIMENT1");
        creation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        creation.setProjectId(new ProjectIdentifier("/TESTGROUP/TESTPROJ"));
        creation.setProperty("DESCRIPTION", "a description");
        creation.setProperty("$PLATE_GEOMETRY", "384_WELLS_16X24");

        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProperties();

        List<ExperimentPermId> permIds = v3api.createExperiments(sessionToken, Arrays.asList(creation));
        Map<IExperimentId, Experiment> map = v3api.getExperiments(sessionToken, permIds, fetchOptions);
        List<Experiment> experiments = new ArrayList<Experiment>(map.values());

        assertEquals(experiments.size(), 1);

        Experiment experiment = experiments.get(0);

        assertEquals(experiment.getIdentifier().getIdentifier(), "/TESTGROUP/TESTPROJ/TEST_EXPERIMENT1");
        assertEquals(experiment.getProperties().size(), 2);

        assertEquals(experiment.getProperty("$PLATE_GEOMETRY"), "384_WELLS_16X24");
    }

    @Test
    public void testCreateWithTagNonexistent()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final Date now = new Date();

        final ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("TEST_EXPERIMENT1");
        creation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        creation.setProjectId(new ProjectIdentifier("/TESTGROUP/TESTPROJ"));
        creation.setProperty("DESCRIPTION", "a description");
        creation.setTagIds(Arrays.asList(new TagPermId("/test/NEW_TAG_THAT_SHOULD_BE_CREATED")));

        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withTags();

        List<ExperimentPermId> permIds = v3api.createExperiments(sessionToken, Arrays.asList(creation));
        Map<IExperimentId, Experiment> map = v3api.getExperiments(sessionToken, permIds, fetchOptions);
        List<Experiment> experiments = new ArrayList<Experiment>(map.values());

        assertEquals(experiments.size(), 1);

        Experiment experiment = experiments.get(0);

        assertEquals(experiment.getIdentifier().getIdentifier(), "/TESTGROUP/TESTPROJ/TEST_EXPERIMENT1");
        assertEquals(experiment.getTags().size(), 1);

        Tag tag = experiment.getTags().iterator().next();

        assertEquals(tag.getCode(), "NEW_TAG_THAT_SHOULD_BE_CREATED");
        assertEquals(tag.getPermId().getPermId(), "/test/NEW_TAG_THAT_SHOULD_BE_CREATED");
        // there can be a 1 second rounding when converting database date to java date
        assertTrue(tag.getRegistrationDate().getTime() + 1000 >= now.getTime());
    }

    @Test
    public void testCreateWithTagUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ITagId tagId = new TagPermId("/test/TEST_METAPROJECTS");
        final ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("TEST_EXPERIMENT1");
        creation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        creation.setProjectId(new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT"));
        creation.setTagIds(Arrays.asList(tagId));

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createExperiments(sessionToken, Arrays.asList(creation));
                }
            }, tagId);
    }

    @Test
    public void testCreateWithPropertyCodeNonexistent()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("TEST_EXPERIMENT1");
        creation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        creation.setProjectId(new ProjectIdentifier("/TESTGROUP/TESTPROJ"));
        creation.setProperty("NONEXISTENT_PROPERTY_CODE", "any value");

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createExperiments(sessionToken, Arrays.asList(creation));
                }
            }, "Property type with code 'NONEXISTENT_PROPERTY_CODE' does not exist");
    }

    @Test
    public void testCreateWithPropertyValueIncorrect()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("TEST_EXPERIMENT1");
        creation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        creation.setProjectId(new ProjectIdentifier("/TESTGROUP/TESTPROJ"));
        creation.setProperty("PURCHASE_DATE", "this should be a date");

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createExperiments(sessionToken, Arrays.asList(creation));
                }
            }, "Date value 'this should be a date' has improper format");
    }

    @Test
    public void testCreateWithPropertyValueMandatoryButNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("TEST_EXPERIMENT1");
        creation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        creation.setProjectId(new ProjectIdentifier("/TESTGROUP/TESTPROJ"));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createExperiments(sessionToken, Arrays.asList(creation));
                }
            }, "Value of mandatory property 'DESCRIPTION' not specified");
    }

    @Test
    public void testCreateWithMandatoryFieldsOnly()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("TEST_EXPERIMENT1");
        creation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        creation.setProjectId(new ProjectIdentifier("/TESTGROUP/TESTPROJ"));
        creation.setProperty("DESCRIPTION", "a description");

        List<ExperimentPermId> permIds = v3api.createExperiments(sessionToken, Arrays.asList(creation));

        assertEquals(permIds.size(), 1);

        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withType();
        fetchOptions.withProject();
        fetchOptions.withModifier();
        fetchOptions.withRegistrator();
        fetchOptions.withProperties();
        fetchOptions.withAttachments().withContent();
        fetchOptions.withTags();

        Map<IExperimentId, Experiment> map = v3api.getExperiments(sessionToken, permIds, fetchOptions);
        List<Experiment> experiments = new ArrayList<Experiment>(map.values());

        assertEquals(experiments.size(), 1);

        Experiment experiment = experiments.get(0);
        assertEquals(experiment.getCode(), "TEST_EXPERIMENT1");
        assertEquals(experiment.getPermId(), permIds.get(0));
        assertEquals(experiment.getIdentifier().getIdentifier(), "/TESTGROUP/TESTPROJ/TEST_EXPERIMENT1");
        assertNotNull(experiment.getType().getCode(), "SIRNA_HCS");
        assertNotNull(experiment.getProject().getIdentifier(), "/TESTGROUP/TESTPROJ");
        assertEquals(experiment.getProperties().size(), 1);
        assertTrue(experiment.getAttachments().isEmpty());
        assertTrue(experiment.getTags().isEmpty());
        assertEquals(experiment.getRegistrator().getUserId(), TEST_USER);
        assertEquals(experiment.getModifier().getUserId(), TEST_USER);
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testCreateWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        AttachmentCreation attachment = new AttachmentCreation();
        attachment.setContent("test content".getBytes());
        attachment.setFileName("test.txt");

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("ANOTHER_TEST_EXPERIMENT");
        creation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        creation.setProjectId(new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT"));
        creation.setProperty("DESCRIPTION", "a description");
        creation.setAttachments(Arrays.asList(attachment));

        String sessionToken = v3api.login(user.getUserId(), PASSWORD);

        if (user.isDisabledProjectUser())
        {
            assertAuthorizationFailureException(new IDelegatedAction()
                {
                    @Override
                    public void execute()
                    {
                        v3api.createExperiments(sessionToken, Collections.singletonList(creation));
                    }
                });
        } else if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            List<ExperimentPermId> permIds = v3api.createExperiments(sessionToken, Collections.singletonList(creation));
            assertEquals(permIds.size(), 1);
        } else
        {
            assertUnauthorizedObjectAccessException(new IDelegatedAction()
                {
                    @Override
                    public void execute()
                    {
                        v3api.createExperiments(sessionToken, Collections.singletonList(creation));
                    }
                }, creation.getProjectId());
        }
    }

    @Test
    public void testCreateWithMultipleExperiments()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentCreation experiment1 = new ExperimentCreation();
        experiment1.setCode("TEST_EXPERIMENT1");
        experiment1.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        experiment1.setProjectId(new ProjectPermId("20120814110011738-103"));
        experiment1.setProperty("DESCRIPTION", "a description");
        experiment1.setProperty("PURCHASE_DATE", "2008-11-05 09:18:00");

        AttachmentCreation a = new AttachmentCreation();

        byte[] attachmentContent = "attachment".getBytes();
        a.setContent(attachmentContent);
        a.setDescription("attachment description");
        a.setFileName("attachment.txt");
        a.setTitle("attachment title");
        experiment1.setAttachments(Arrays.asList(a));

        ExperimentCreation experiment2 = new ExperimentCreation();
        experiment2.setCode("TEST_EXPERIMENT2");
        experiment2.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        experiment2.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        experiment2.setProperty("DESCRIPTION", "a description");
        experiment2.setProperty("GENDER", "MALE");
        experiment2.setTagIds(Arrays.<ITagId> asList(
                new TagPermId("/test/TEST_METAPROJECTS"), new TagPermId("/test/ANOTHER_TEST_METAPROJECTS")));

        List<ExperimentPermId> result = v3api.createExperiments(sessionToken, Arrays.asList(experiment1, experiment2));

        assertEquals(result.size(), 2);

        ExperimentFetchOptions experimentFetchOptions = new ExperimentFetchOptions();
        experimentFetchOptions.withType();
        experimentFetchOptions.withProject();
        experimentFetchOptions.withModifier();
        experimentFetchOptions.withRegistrator();
        experimentFetchOptions.withProperties();
        experimentFetchOptions.withAttachments().withContent();
        experimentFetchOptions.withTags();

        Map<IExperimentId, Experiment> map = v3api.getExperiments(sessionToken, result, experimentFetchOptions);
        List<Experiment> experiments = new ArrayList<Experiment>(map.values());

        Assert.assertEquals(2, experiments.size());

        Assert.assertFalse(experiments.get(0).getPermId().getPermId().equals(experiments.get(1).getPermId().getPermId()));

        Experiment exp = experiments.get(0);
        assertEquals(exp.getCode(), "TEST_EXPERIMENT1");
        assertEquals(exp.getType().getCode(), "SIRNA_HCS");
        assertEquals(exp.getProject().getIdentifier().getIdentifier(), "/CISD/NEMO");
        assertEquals(exp.getProperties().size(), 2, exp.getProperties().toString());
        assertEquals(exp.getProperties().get("DESCRIPTION"), "a description");
        assertEquals(exp.getProperties().get("PURCHASE_DATE"), "2008-11-05 09:18:00 +0100");
        List<Attachment> attachments = exp.getAttachments();
        assertEquals(attachments.size(), 1);
        assertEquals(attachments.get(0).getContent(), attachmentContent);

        exp = experiments.get(1);
        assertEquals(exp.getCode(), "TEST_EXPERIMENT2");
        assertEquals(exp.getType().getCode(), "SIRNA_HCS");
        assertEquals(exp.getProject().getIdentifier().getIdentifier(), "/CISD/NEMO");
        assertEquals(exp.getProperties().size(), 2, exp.getProperties().toString());
        assertEquals(exp.getProperties().get("GENDER"), "MALE");
        assertEquals(exp.getProperties().get("DESCRIPTION"), "a description");

        HashSet<String> tagIds = new HashSet<String>();
        for (Tag tag : exp.getTags())
        {
            tagIds.add(tag.getPermId().getPermId());
        }
        assertEquals(tagIds, new HashSet<String>(Arrays.asList("/test/TEST_METAPROJECTS", "/test/ANOTHER_TEST_METAPROJECTS")));
        assertEquals(exp.getModifier().getUserId(), TEST_USER);
        assertEquals(exp.getRegistrator().getUserId(), TEST_USER);

    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("LOG_TEST_1");
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        creation.setProperty("DESCRIPTION", "a description");

        ExperimentCreation creation2 = new ExperimentCreation();
        creation2.setCode("LOG_TEST_2");
        creation2.setProjectId(new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT"));
        creation2.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        creation2.setProperty("DESCRIPTION", "a description 2");

        v3api.createExperiments(sessionToken, Arrays.asList(creation, creation2));

        assertAccessLog(
                "create-experiments  NEW_EXPERIMENTS('[ExperimentCreation[projectId=/CISD/NEMO,code=LOG_TEST_1], ExperimentCreation[projectId=/TEST-SPACE/TEST-PROJECT,code=LOG_TEST_2]]')");
    }

    @Test
    public void testCreateWithUnknownPropertyOfTypeSample()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentCreation experiment = new ExperimentCreation();
        experiment.setCode("EXPERIMENT_WITH_SAMPLE_PROPERTY");
        experiment.setTypeId(new EntityTypePermId("COMPOUND_HCS"));
        experiment.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        experiment.setProperty("PLATE", "/CISD/CL1");

        // When
        assertUserFailureException(Void -> v3api.createExperiments(sessionToken, Arrays.asList(experiment)),
                // Then
                "Property type with code 'PLATE' does not exist");
    }

    @Test
    public void testCreateWithPropertyOfTypeSampleWithUnknownSample()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createASamplePropertyType(sessionToken, null);
        EntityTypePermId experimentType = createAnExperimentType(sessionToken, false, propertyType);

        ExperimentCreation experiment = new ExperimentCreation();
        experiment.setCode("EXPERIMENT_WITH_SAMPLE_PROPERTY");
        experiment.setTypeId(experimentType);
        experiment.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        experiment.setProperty(propertyType.getPermId(), "/CISD/UNKNOWN");

        // When
        assertUserFailureException(Void -> v3api.createExperiments(sessionToken, Arrays.asList(experiment)),
                // Then
                "Unknown sample: /CISD/UNKNOWN");
    }

    @Test
    public void testCreateWithMissingMandatoryPropertyOfTypeSample()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createASamplePropertyType(sessionToken, null);
        EntityTypePermId experimentType = createAnExperimentType(sessionToken, true, propertyType);

        ExperimentCreation experiment = new ExperimentCreation();
        experiment.setCode("EXPERIMENT_WITH_SAMPLE_PROPERTY");
        experiment.setTypeId(experimentType);
        experiment.setProjectId(new ProjectIdentifier("/CISD/NEMO"));

        // When
        assertUserFailureException(Void -> v3api.createExperiments(sessionToken, Arrays.asList(experiment)),
                // Then
                "Value of mandatory property '" + propertyType.getPermId() + "' not specified.");
    }

    @Test
    public void testCreateWithPropertyOfTypeSampleWithSampleOfWrongType()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createASamplePropertyType(sessionToken,
                new EntityTypePermId("WELL", ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind.SAMPLE));
        EntityTypePermId experimentType = createAnExperimentType(sessionToken, false, propertyType);

        ExperimentCreation experiment = new ExperimentCreation();
        experiment.setCode("EXPERIMENT_WITH_SAMPLE_PROPERTY");
        experiment.setTypeId(experimentType);
        experiment.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        experiment.setProperty(propertyType.getPermId(), "200811050919915-8");

        // When
        assertUserFailureException(Void -> v3api.createExperiments(sessionToken, Arrays.asList(experiment)),
                // Then
                "Property " + propertyType.getPermId() + " is not a sample of type WELL but of type CONTROL_LAYOUT");
    }

    @Test
    public void testCreateWithPropertyOfTypeSampleWithSampleNotAccessable()
    {
        // Given
        String adminSessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createASamplePropertyType(adminSessionToken, null);
        EntityTypePermId experimentType = createAnExperimentType(adminSessionToken, true, propertyType);
        v3api.logout(adminSessionToken);

        String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);
        ExperimentCreation experiment = new ExperimentCreation();
        experiment.setCode("EXPERIMENT_WITH_SAMPLE_PROPERTY");
        experiment.setTypeId(experimentType);
        experiment.setProjectId(new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT"));
        experiment.setProperty(propertyType.getPermId(), "/CISD/CL1");

        // When
        assertUserFailureException(Void -> v3api.createExperiments(sessionToken, Arrays.asList(experiment)),
                // Then
                "Unknown sample: /CISD/CL1");
    }

    @Test
    public void testCreateWithPropertyOfTypeSampleWithInvisibleSample()
    {
        // Given
        String adminSessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createASamplePropertyType(adminSessionToken, null);
        EntityTypePermId experimentType = createAnExperimentType(adminSessionToken, true, propertyType, PLATE_GEOMETRY);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("EXPERIMENT_WITH_SAMPLE_PROPERTY");
        creation.setTypeId(experimentType);
        creation.setProjectId(new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT"));
        creation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        creation.setProperty(propertyType.getPermId(), "/CISD/CL1");
        ExperimentPermId experimentPermId = v3api.createExperiments(adminSessionToken, Arrays.asList(creation)).get(0);
        v3api.logout(adminSessionToken);

        String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();

        // When
        Experiment experiment = v3api.getExperiments(sessionToken, Arrays.asList(experimentPermId), fetchOptions).get(experimentPermId);

        // Then
        assertEquals(experiment.getSampleProperties().toString(), "{}");
        assertEquals(experiment.getProperties().toString(), "{$PLATE_GEOMETRY=384_WELLS_16X24}");
    }

    @Test
    public void testCreateWithPropertyOfTypeSample()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createASamplePropertyType(sessionToken, null);
        EntityTypePermId experimentType = createAnExperimentType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("EXPERIMENT_WITH_SAMPLE_PROPERTY");
        creation.setTypeId(experimentType);
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        creation.setProperty(propertyType.getPermId(), "/CISD/CL1");
        // creation.setSampleProperty(propertyType.getPermId(), new SampleIdentifier("/CISD/CL1"));

        // When
        List<ExperimentPermId> experimentIds = v3api.createExperiments(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(experimentIds.size(), 1);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        Experiment experiment2 = v3api.getExperiments(sessionToken, experimentIds, fetchOptions).get(experimentIds.get(0));
        Sample sampleProperty = experiment2.getSampleProperties().get(propertyType.getPermId())[0];
        assertEquals(sampleProperty.getIdentifier().getIdentifier(), "/CISD/CL1");
        assertEquals(experiment2.getSampleProperties().size(), 1);
        assertEquals(experiment2.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        assertEquals(experiment2.getProperties().get(propertyType.getPermId()), sampleProperty.getPermId().getPermId());
        assertEquals(experiment2.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithPropertyOfTypeDate()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.DATE);
        EntityTypePermId experimentType = createAnExperimentType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("EXPERIMENT_WITH_SAMPLE_PROPERTY");
        creation.setTypeId(experimentType);
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        creation.setProperty(propertyType.getPermId(), "2/17/20");

        // When
        List<ExperimentPermId> experimentIds = v3api.createExperiments(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(experimentIds.size(), 1);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        Experiment experiment2 = v3api.getExperiments(sessionToken, experimentIds, fetchOptions).get(experimentIds.get(0));
        assertEquals(experiment2.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        assertEquals(experiment2.getProperties().get(propertyType.getPermId()), "2020-02-17");
        assertEquals(experiment2.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithPropertyOfTypeTimestamp()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.TIMESTAMP);
        EntityTypePermId experimentType = createAnExperimentType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("EXPERIMENT_WITH_SAMPLE_PROPERTY");
        creation.setTypeId(experimentType);
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        creation.setProperty(propertyType.getPermId(), "2020-02-17 18:13:47");

        // When
        List<ExperimentPermId> experimentIds = v3api.createExperiments(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(experimentIds.size(), 1);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        Experiment experiment2 = v3api.getExperiments(sessionToken, experimentIds, fetchOptions).get(experimentIds.get(0));
        assertEquals(experiment2.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        assertEquals(experiment2.getProperties().get(propertyType.getPermId()), "2020-02-17 18:13:47 +0100");
        assertEquals(experiment2.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithPropertyOfTypeTimestampDedicatedMethod()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.TIMESTAMP);
        EntityTypePermId experimentType = createAnExperimentType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("EXPERIMENT_WITH_SAMPLE_PROPERTY");
        creation.setTypeId(experimentType);
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        ZonedDateTime time1 = ZonedDateTime.parse("2023-05-16T11:22:33+02");
        creation.setTimestampProperty(propertyType.getPermId(), time1);

        // When
        List<ExperimentPermId> experimentIds = v3api.createExperiments(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(experimentIds.size(), 1);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        Experiment experiment2 = v3api.getExperiments(sessionToken, experimentIds, fetchOptions).get(experimentIds.get(0));
        assertEquals(experiment2.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        assertEquals(experiment2.getTimestampProperty(propertyType.getPermId()), time1);
        assertEquals(experiment2.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithMultiValuePropertyOfTypeTimestamp()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.TIMESTAMP, true);
        EntityTypePermId experimentType = createAnExperimentType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("EXPERIMENT_WITH_SAMPLE_PROPERTY");
        creation.setTypeId(experimentType);
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        ZonedDateTime time1 = ZonedDateTime.parse("2023-05-16T11:22:33+02");
        ZonedDateTime time2 = ZonedDateTime.parse("2023-05-18T11:17:03+02");
        creation.setMultiValueTimestampProperty(propertyType.getPermId(), List.of(time1, time2));

        // When
        List<ExperimentPermId> experimentIds = v3api.createExperiments(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(experimentIds.size(), 1);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        Experiment experiment2 = v3api.getExperiments(sessionToken, experimentIds, fetchOptions).get(experimentIds.get(0));
        assertEquals(experiment2.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        assertEqualsNoOrder(experiment2.getMultiValueTimestampProperty(propertyType.getPermId()).toArray(ZonedDateTime[]::new), new ZonedDateTime[] {time1, time2});
        assertEquals(experiment2.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithPropertyOfTypeJson()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.JSON);
        EntityTypePermId experimentType = createAnExperimentType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("EXPERIMENT_WITH_SAMPLE_PROPERTY");
        creation.setTypeId(experimentType);
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        creation.setJsonProperty(propertyType.getPermId(), "{\"key\": \"value\", \"array\":[1,2,3]}");

        // When
        List<ExperimentPermId> experimentIds = v3api.createExperiments(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(experimentIds.size(), 1);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        Experiment experiment2 = v3api.getExperiments(sessionToken, experimentIds, fetchOptions).get(experimentIds.get(0));
        assertEquals(experiment2.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        assertEquals(experiment2.getJsonProperty(propertyType.getPermId()), "{\"key\": \"value\", \"array\": [1, 2, 3]}");
        assertEquals(experiment2.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithMultiValuePropertyOfTypeJson()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.JSON, true);
        EntityTypePermId experimentType = createAnExperimentType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("EXPERIMENT_WITH_SAMPLE_PROPERTY");
        creation.setTypeId(experimentType);
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        creation.setMultiValueJsonProperty(propertyType.getPermId(), List.of("{\"key\": \"value\", \"array\":[1,2,3]}", "{\"key\": \"value2\", \"array\":[]}"));

        // When
        List<ExperimentPermId> experimentIds = v3api.createExperiments(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(experimentIds.size(), 1);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        Experiment experiment2 = v3api.getExperiments(sessionToken, experimentIds, fetchOptions).get(experimentIds.get(0));
        assertEquals(experiment2.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        List<String> properties = experiment2.getMultiValueJsonProperty(propertyType.getPermId());
        assertEquals(properties.size(), 2);
        assertEqualsNoOrder(properties.toArray(String[]::new), new String[] {"{\"key\": \"value\", \"array\": [1, 2, 3]}", "{\"key\": \"value2\", \"array\": []}"});
        assertEquals(experiment2.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithMultiValuePropertyOfTypeInteger()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.INTEGER, true);
        EntityTypePermId experimentType = createAnExperimentType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("EXPERIMENT_WITH_MULTI_INTEGER_PROPERTY");
        creation.setTypeId(experimentType);
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        creation.setProperty(propertyType.getPermId(), new Long[] {1L, 1L, 3L});

        // When
        List<ExperimentPermId> experimentIds = v3api.createExperiments(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(experimentIds.size(), 1);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();

        Experiment experiment2 = v3api.getExperiments(sessionToken, experimentIds, fetchOptions).get(experimentIds.get(0));
        assertEquals(experiment2.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        assertEqualsNoOrder((Serializable[]) experiment2.getProperties().get(propertyType.getPermId()), new Serializable[] { "1", "1", "3" });
        assertEqualsNoOrder(experiment2.getMultiValueIntegerProperty(propertyType.getPermId()).toArray(Long[]::new), new Long[] { 1L, 1L, 3L });
        assertEquals(experiment2.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithMultiValuePropertyOfTypeIntegerDedicatedMethod()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.INTEGER, true);
        EntityTypePermId experimentType = createAnExperimentType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("EXPERIMENT_WITH_MULTI_INTEGER_PROPERTY");
        creation.setTypeId(experimentType);
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        creation.setMultiValueIntegerProperty(propertyType.getPermId(), List.of(1L, 2L, 3L));

        // When
        List<ExperimentPermId> experimentIds = v3api.createExperiments(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(experimentIds.size(), 1);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();

        Experiment experiment2 = v3api.getExperiments(sessionToken, experimentIds, fetchOptions).get(experimentIds.get(0));
        assertEquals(experiment2.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        assertEqualsNoOrder((Serializable[]) experiment2.getProperties().get(propertyType.getPermId()), new Serializable[] { "1", "2", "3" });
        assertEqualsNoOrder(experiment2.getMultiValueIntegerProperty(propertyType.getPermId()).toArray(Long[]::new), new Long[] { 1L, 2L, 3L });
        assertEquals(experiment2.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithPropertyOfTypeArrayInteger()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.ARRAY_INTEGER);
        EntityTypePermId experimentType = createAnExperimentType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("EXPERIMENT_WITH_ARRAY_INTEGER_PROPERTY");
        creation.setTypeId(experimentType);
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        creation.setIntegerArrayProperty(propertyType.getPermId(), new Long[]{1L, 2L, 3L});

        // When
        List<ExperimentPermId> experimentIds = v3api.createExperiments(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(experimentIds.size(), 1);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        Experiment experiment2 = v3api.getExperiments(sessionToken, experimentIds, fetchOptions).get(experimentIds.get(0));
        assertEquals(experiment2.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        assertEquals(experiment2.getIntegerArrayProperty(propertyType.getPermId()), new Long[]{1L, 2L, 3L});
        assertEquals(experiment2.getProperties().size(), 2);
    }


    @Test
    public void testCreateWithMultiValuePropertyOfTypeArrayInteger()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.ARRAY_INTEGER, true);
        EntityTypePermId experimentType = createAnExperimentType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("EXPERIMENT_WITH_MULTI_VALUE_INTEGER_ARRAY_PROPERTY");
        creation.setTypeId(experimentType);
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        creation.setProperty(propertyType.getPermId(), new Long[][]{ new Long[]{1L, 2L, 3L}, new Long[]{4L, 5L, 6L} });

        // When
        List<ExperimentPermId> experimentIds = v3api.createExperiments(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(experimentIds.size(), 1);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        Experiment experiment2 = v3api.getExperiments(sessionToken, experimentIds, fetchOptions).get(experimentIds.get(0));
        assertEquals(experiment2.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");

        List<Long[]> props = experiment2.getMultiValueIntegerArrayProperty(propertyType.getPermId());
        assertEquals(props.size(), 2);
        for(Long[] prop : props) {
            if(prop[0] > 3L) {
                assertEqualsNoOrder(prop, new Long[] {4L, 5L, 6L});
            } else {
                assertEqualsNoOrder(prop, new Long[] {1L, 2L, 3L});
            }
        }

        assertEquals(experiment2.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithMultiValuePropertyOfTypeArrayIntegerDedicatedMethod()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.ARRAY_INTEGER, true);
        EntityTypePermId experimentType = createAnExperimentType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("EXPERIMENT_WITH_MULTI_VALUE_INTEGER_ARRAY_PROPERTY");
        creation.setTypeId(experimentType);
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        creation.setMultiValueIntegerArrayProperty(propertyType.getPermId(), List.of( new Long[]{1L, 2L, 3L}, new Long[]{4L, 5L, 6L} ));

        // When
        List<ExperimentPermId> experimentIds = v3api.createExperiments(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(experimentIds.size(), 1);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        Experiment experiment2 = v3api.getExperiments(sessionToken, experimentIds, fetchOptions).get(experimentIds.get(0));
        assertEquals(experiment2.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");

        List<Long[]> props = experiment2.getMultiValueIntegerArrayProperty(propertyType.getPermId());
        assertEquals(props.size(), 2);
        for(Long[] prop : props) {
            if(prop[0] > 3L) {
                assertEqualsNoOrder(prop, new Long[] {4L, 5L, 6L});
            } else {
                assertEqualsNoOrder(prop, new Long[] {1L, 2L, 3L});
            }
        }

        assertEquals(experiment2.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithPropertyOfTypeArrayReal()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.ARRAY_REAL);
        EntityTypePermId experimentType = createAnExperimentType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("EXPERIMENT_WITH_REAL_PROPERTY");
        creation.setTypeId(experimentType);
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        creation.setRealArrayProperty(propertyType.getPermId(), new Double[]{1.0, 2.0, 3.0});

        // When
        List<ExperimentPermId> experimentIds = v3api.createExperiments(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(experimentIds.size(), 1);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        Experiment experiment2 = v3api.getExperiments(sessionToken, experimentIds, fetchOptions).get(experimentIds.get(0));
        assertEquals(experiment2.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        assertEquals(experiment2.getRealArrayProperty(propertyType.getPermId()), new Double[]{1.0, 2.0, 3.0});
        assertEquals(experiment2.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithMultiValuePropertyOfTypeArrayReal()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.ARRAY_REAL, true);
        EntityTypePermId experimentType = createAnExperimentType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("EXPERIMENT_WITH_MULTI_VALUE_REAL_ARRAY_PROPERTY");
        creation.setTypeId(experimentType);
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        creation.setProperty(propertyType.getPermId(), new Double[][]{ new Double[]{1.0, 2.0, 3.0}, new Double[]{4.0, 5.0, 6.0} });

        // When
        List<ExperimentPermId> experimentIds = v3api.createExperiments(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(experimentIds.size(), 1);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        Experiment experiment2 = v3api.getExperiments(sessionToken, experimentIds, fetchOptions).get(experimentIds.get(0));
        assertEquals(experiment2.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");

        List<Long[]> props = experiment2.getMultiValueIntegerArrayProperty(propertyType.getPermId());
        assertEquals(props.size(), 2);
        for(Long[] prop : props) {
            if(prop[0] > 3L) {
                assertEqualsNoOrder(prop, new Long[] {4L, 5L, 6L});
            } else {
                assertEqualsNoOrder(prop, new Long[] {1L, 2L, 3L});
            }
        }

        assertEquals(experiment2.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithMultiValuePropertyOfTypeArrayRealDedicatedMethod()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.ARRAY_INTEGER, true);
        EntityTypePermId experimentType = createAnExperimentType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("EXPERIMENT_WITH_MULTI_VALUE_INTEGER_ARRAY_PROPERTY");
        creation.setTypeId(experimentType);
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        creation.setMultiValueIntegerArrayProperty(propertyType.getPermId(), List.of( new Long[]{1L, 2L, 3L}, new Long[]{4L, 5L, 6L} ));

        // When
        List<ExperimentPermId> experimentIds = v3api.createExperiments(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(experimentIds.size(), 1);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        Experiment experiment2 = v3api.getExperiments(sessionToken, experimentIds, fetchOptions).get(experimentIds.get(0));
        assertEquals(experiment2.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");

        List<Long[]> props = experiment2.getMultiValueIntegerArrayProperty(propertyType.getPermId());
        assertEquals(props.size(), 2);
        for(Long[] prop : props) {
            if(prop[0] > 3L) {
                assertEqualsNoOrder(prop, new Long[] {4L, 5L, 6L});
            } else {
                assertEqualsNoOrder(prop, new Long[] {1L, 2L, 3L});
            }
        }

        assertEquals(experiment2.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithPropertyOfTypeArrayString()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.ARRAY_STRING);
        EntityTypePermId experimentType = createAnExperimentType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("EXPERIMENT_WITH_ARRAY_STRING_PROPERTY");
        creation.setTypeId(experimentType);
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        creation.setStringArrayProperty(propertyType.getPermId(), new String[]{"a,a", "b", "c"});

        // When
        List<ExperimentPermId> experimentIds = v3api.createExperiments(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(experimentIds.size(), 1);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        Experiment experiment2 = v3api.getExperiments(sessionToken, experimentIds, fetchOptions).get(experimentIds.get(0));
        assertEquals(experiment2.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        assertEquals(experiment2.getStringArrayProperty(propertyType.getPermId()), new String[]{"a,a", "b", "c"});
        assertEquals(experiment2.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithMultiValuePropertyOfTypeArrayString()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.ARRAY_STRING, true);
        EntityTypePermId experimentType = createAnExperimentType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("EXPERIMENT_WITH_ARRAY_STRING_PROPERTY");
        creation.setTypeId(experimentType);
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        creation.setMultiValueStringArrayProperty(propertyType.getPermId(), List.of(new String[]{"a,a", "b", "c"}, new String[]{"a", "b", "c", "d"}));

        // When
        List<ExperimentPermId> experimentIds = v3api.createExperiments(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(experimentIds.size(), 1);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        Experiment experiment2 = v3api.getExperiments(sessionToken, experimentIds, fetchOptions).get(experimentIds.get(0));
        assertEquals(experiment2.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        List<String[]> result = experiment2.getMultiValueStringArrayProperty(propertyType.getPermId());
        assertEquals(result.size(), 2);
        for(String[] prop : result) {
            if(prop.length > 3) {
                assertEqualsNoOrder(prop, new String[]{"a", "b", "c", "d"});
            } else {
                assertEqualsNoOrder(prop, new String[]{"a,a", "b", "c"});
            }
        }
        assertEquals(experiment2.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithPropertyOfTypeArrayTimestamp()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.ARRAY_TIMESTAMP);
        EntityTypePermId experimentType = createAnExperimentType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("EXPERIMENT_WITH_ARRAY_TIMESTAMP_PROPERTY");
        creation.setTypeId(experimentType);
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        ZonedDateTime time1 = ZonedDateTime.parse("2023-05-16T11:22:33+02");
        ZonedDateTime time2 = ZonedDateTime.parse("2023-05-18T11:17:03+02");
        creation.setTimestampArrayProperty(propertyType.getPermId(), new ZonedDateTime[]{time1, time2});

        // When
        List<ExperimentPermId> experimentIds = v3api.createExperiments(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(experimentIds.size(), 1);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        Experiment experiment2 = v3api.getExperiments(sessionToken, experimentIds, fetchOptions).get(experimentIds.get(0));
        assertEquals(experiment2.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        assertEquals(experiment2.getTimestampArrayProperty(propertyType.getPermId()), new ZonedDateTime[] { time1, time2 });
        assertEquals(experiment2.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithMultiValuePropertyOfTypeArrayTimestamp()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.ARRAY_TIMESTAMP, true);
        EntityTypePermId experimentType = createAnExperimentType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("EXPERIMENT_WITH_ARRAY_TIMESTAMP_PROPERTY");
        creation.setTypeId(experimentType);
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        ZonedDateTime time1 = ZonedDateTime.parse("2023-05-16T11:22:33+02");
        ZonedDateTime time2 = ZonedDateTime.parse("2023-05-18T11:17:03+02");
        ZonedDateTime time3 = ZonedDateTime.parse("2023-05-20T11:10:03+02");
        creation.setMultiValueTimestampArrayProperty(propertyType.getPermId(), List.of(new ZonedDateTime[]{time1, time2}, new ZonedDateTime[]{time3, time2, time1}));

        // When
        List<ExperimentPermId> experimentIds = v3api.createExperiments(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(experimentIds.size(), 1);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        Experiment experiment2 = v3api.getExperiments(sessionToken, experimentIds, fetchOptions).get(experimentIds.get(0));
        assertEquals(experiment2.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");

        List<ZonedDateTime[]> properties = experiment2.getMultiValueTimestampArrayProperty(propertyType.getPermId());
        assertEquals(properties.size(), 2);
        for(ZonedDateTime[] prop : properties) {
            if(prop.length == 2) {
                assertEqualsNoOrder(prop, new ZonedDateTime[]{time1, time2});
            } else {
                assertEqualsNoOrder(prop, new ZonedDateTime[]{time1, time2, time3});
            }
        }
        assertEquals(experiment2.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithMultiValuePropertyVocabulary()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final PropertyTypeCreation propertyTypeCreation = new PropertyTypeCreation();
        propertyTypeCreation.setCode("TYPE-" + System.currentTimeMillis());
        propertyTypeCreation.setDataType(DataType.CONTROLLEDVOCABULARY);
        propertyTypeCreation.setLabel("label");
        propertyTypeCreation.setDescription("description");
        propertyTypeCreation.setMultiValue(true);
        propertyTypeCreation.setVocabularyId(new VocabularyPermId("ORGANISM"));
        PropertyTypePermId propertyType = v3api.createPropertyTypes(sessionToken, Collections.singletonList(propertyTypeCreation)).get(0);

        EntityTypePermId experimentType = createAnExperimentType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("EXPERIMENT_WITH_MULTI_VOCAB_PROPERTY-" + System.currentTimeMillis());
        creation.setTypeId(experimentType);
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        creation.setProperty(propertyType.getPermId(), new String[] {"DOG", "HUMAN"});

        // When
        List<ExperimentPermId> experimentIds = v3api.createExperiments(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(experimentIds.size(), 1);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        Experiment experiment2 = v3api.getExperiments(sessionToken, experimentIds, fetchOptions).get(experimentIds.get(0));
        assertEquals(experiment2.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        String[] vocabProperties = Arrays.stream((Serializable[])experiment2.getProperty(propertyType.getPermId()))
                                            .map(Serializable::toString)
                                            .toArray(String[]::new);
        Arrays.sort(vocabProperties);
        assertEquals(vocabProperties, new String[] {"DOG", "HUMAN"});
        assertEquals(experiment2.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithMultiValuePropertyVocabularyDedicatedMethod()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final PropertyTypeCreation propertyTypeCreation = new PropertyTypeCreation();
        propertyTypeCreation.setCode("TYPE-" + System.currentTimeMillis());
        propertyTypeCreation.setDataType(DataType.CONTROLLEDVOCABULARY);
        propertyTypeCreation.setLabel("label");
        propertyTypeCreation.setDescription("description");
        propertyTypeCreation.setMultiValue(true);
        propertyTypeCreation.setVocabularyId(new VocabularyPermId("ORGANISM"));
        PropertyTypePermId propertyType = v3api.createPropertyTypes(sessionToken, Collections.singletonList(propertyTypeCreation)).get(0);

        EntityTypePermId experimentType = createAnExperimentType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("EXPERIMENT_WITH_MULTI_VOCAB_PROPERTY-" + System.currentTimeMillis());
        creation.setTypeId(experimentType);
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        creation.setMultiValueControlledVocabularyProperty(propertyType.getPermId(), List.of("DOG", "HUMAN"));

        // When
        List<ExperimentPermId> experimentIds = v3api.createExperiments(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(experimentIds.size(), 1);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        Experiment experiment2 = v3api.getExperiments(sessionToken, experimentIds, fetchOptions).get(experimentIds.get(0));
        assertEquals(experiment2.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        List<String> vocabProperties = experiment2.getMultiValueControlledVocabularyProperty(propertyType.getPermId());
        Collections.sort(vocabProperties);
        assertEquals(vocabProperties, List.of("DOG", "HUMAN"));
        assertEquals(experiment2.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithMultiValuePropertySample()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        //Create sample
        PropertyTypePermId propertyType1 = createASamplePropertyType(sessionToken, null);
        EntityTypePermId sampleType = createASampleType(sessionToken, true, propertyType1, PLATE_GEOMETRY);

        SampleCreation sample = new SampleCreation();
        sample.setCode("SAMPLE_WITH_SAMPLE_PROPERTY-" + System.currentTimeMillis());
        sample.setTypeId(sampleType);
        sample.setSpaceId(new SpacePermId("CISD"));
        sample.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        sample.setProperty(propertyType1.getPermId(), "200811050919915-8");

        // When
        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken, Arrays.asList(sample));

        // Then
        assertEquals(sampleIds.size(), 1);

        SampleFetchOptions sampleFetchOptions = new SampleFetchOptions();
        sampleFetchOptions.withProperties();
        sampleFetchOptions.withSampleProperties();
        Sample sample2 = v3api.getSamples(sessionToken, sampleIds, sampleFetchOptions).get(sampleIds.get(0));

        final PropertyTypeCreation propertyTypeCreation = new PropertyTypeCreation();
        propertyTypeCreation.setCode("TYPE-" + System.currentTimeMillis());
        propertyTypeCreation.setDataType(DataType.SAMPLE);
        propertyTypeCreation.setLabel("label");
        propertyTypeCreation.setDescription("description");
        propertyTypeCreation.setMultiValue(true);
        PropertyTypePermId propertyType = v3api.createPropertyTypes(sessionToken, Collections.singletonList(propertyTypeCreation)).get(0);

        EntityTypePermId experimentType = createAnExperimentType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("EXPERIMENT_WITH_SAMPLE_PROPERTY-" + System.currentTimeMillis());
        creation.setTypeId(experimentType);
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        creation.setProperty(propertyType.getPermId(), new String[] {"/CISD/CL1", sampleIds.get(0).getPermId()});

        // When
        List<ExperimentPermId> experimentIds = v3api.createExperiments(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(experimentIds.size(), 1);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        Experiment experiment2 = v3api.getExperiments(sessionToken, experimentIds, fetchOptions).get(experimentIds.get(0));
        assertEquals(experiment2.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        Map<String, Sample[]> sampleProperties = experiment2.getSampleProperties();

        Sample[] samples = sampleProperties.get(propertyType.getPermId());
        Serializable[] sampleProps = Arrays.stream(samples).map(x -> x.getPermId().getPermId()).sorted().toArray(String[]::new);
        assertEquals(sampleProps, new Serializable[]{"200811050919915-8", sample2.getPermId().getPermId()});

        sampleProps = (Serializable[]) experiment2.getProperties().get(propertyType.getPermId());
        Arrays.sort(sampleProps);
        assertEquals(sampleProps, new Serializable[]{"200811050919915-8", sample2.getPermId().getPermId()});
        assertEquals(experiment2.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithMultiValuePropertySampleDedicatedMethod()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        //Create sample
        PropertyTypePermId propertyType1 = createASamplePropertyType(sessionToken, null);
        EntityTypePermId sampleType = createASampleType(sessionToken, true, propertyType1, PLATE_GEOMETRY);

        SampleCreation sample = new SampleCreation();
        sample.setCode("SAMPLE_WITH_SAMPLE_PROPERTY-" + System.currentTimeMillis());
        sample.setTypeId(sampleType);
        sample.setSpaceId(new SpacePermId("CISD"));
        sample.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        sample.setProperty(propertyType1.getPermId(), "200811050919915-8");

        // When
        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken, Arrays.asList(sample));

        // Then
        assertEquals(sampleIds.size(), 1);

        SampleFetchOptions sampleFetchOptions = new SampleFetchOptions();
        sampleFetchOptions.withProperties();
        sampleFetchOptions.withSampleProperties();
        Sample sample2 = v3api.getSamples(sessionToken, sampleIds, sampleFetchOptions).get(sampleIds.get(0));

        final PropertyTypeCreation propertyTypeCreation = new PropertyTypeCreation();
        propertyTypeCreation.setCode("TYPE-" + System.currentTimeMillis());
        propertyTypeCreation.setDataType(DataType.SAMPLE);
        propertyTypeCreation.setLabel("label");
        propertyTypeCreation.setDescription("description");
        propertyTypeCreation.setMultiValue(true);
        PropertyTypePermId propertyType = v3api.createPropertyTypes(sessionToken, Collections.singletonList(propertyTypeCreation)).get(0);

        EntityTypePermId experimentType = createAnExperimentType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("EXPERIMENT_WITH_SAMPLE_PROPERTY-" + System.currentTimeMillis());
        creation.setTypeId(experimentType);
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        creation.setMultiValueSampleProperty(propertyType.getPermId(), List.of(new SamplePermId("200811050919915-8"), sampleIds.get(0)));

        // When
        List<ExperimentPermId> experimentIds = v3api.createExperiments(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(experimentIds.size(), 1);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        Experiment experiment2 = v3api.getExperiments(sessionToken, experimentIds, fetchOptions).get(experimentIds.get(0));
        assertEquals(experiment2.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");

        List<SamplePermId> properties = experiment2.getMultiValueSampleProperty(propertyType.getPermId());
        assertEquals(properties.size(), 2);
        assertEqualsNoOrder(properties.stream().map(SamplePermId::getPermId).toArray(String[]::new), new String[] {"200811050919915-8", sample2.getPermId().getPermId()});

        Map<String, Sample[]> sampleProperties = experiment2.getSampleProperties();

        Sample[] samples = sampleProperties.get(propertyType.getPermId());
        Serializable[] sampleProps = Arrays.stream(samples).map(x -> x.getPermId().getPermId()).sorted().toArray(String[]::new);
        assertEquals(sampleProps, new Serializable[]{"200811050919915-8", sample2.getPermId().getPermId()});

        sampleProps = (Serializable[]) experiment2.getProperties().get(propertyType.getPermId());
        Arrays.sort(sampleProps);
        assertEquals(sampleProps, new Serializable[]{"200811050919915-8", sample2.getPermId().getPermId()});
        assertEquals(experiment2.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithMultiValuePropertySample2()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        //Create sample
        PropertyTypePermId propertyType1 = createASamplePropertyType(sessionToken, null);
        EntityTypePermId sampleType = createASampleType(sessionToken, true, propertyType1, PLATE_GEOMETRY);

        SampleCreation sample = new SampleCreation();
        sample.setCode("SAMPLE_WITH_SAMPLE_PROPERTY-" + System.currentTimeMillis());
        sample.setTypeId(sampleType);
        sample.setSpaceId(new SpacePermId("CISD"));
        sample.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        sample.setProperty(propertyType1.getPermId(), "200811050919915-8");

        // When
        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken, Arrays.asList(sample));

        // Then
        assertEquals(sampleIds.size(), 1);

        SampleFetchOptions sampleFetchOptions = new SampleFetchOptions();
        sampleFetchOptions.withProperties();
        sampleFetchOptions.withSampleProperties();
        Sample sample2 = v3api.getSamples(sessionToken, sampleIds, sampleFetchOptions).get(sampleIds.get(0));

        final PropertyTypeCreation propertyTypeCreation = new PropertyTypeCreation();
        propertyTypeCreation.setCode("TYPE-" + System.currentTimeMillis());
        propertyTypeCreation.setDataType(DataType.SAMPLE);
        propertyTypeCreation.setLabel("label");
        propertyTypeCreation.setDescription("description");
        propertyTypeCreation.setMultiValue(true);
        PropertyTypePermId propertyType = v3api.createPropertyTypes(sessionToken, Collections.singletonList(propertyTypeCreation)).get(0);

        EntityTypePermId experimentType = createAnExperimentType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("EXPERIMENT_WITH_SAMPLE_PROPERTY-" + System.currentTimeMillis());
        creation.setTypeId(experimentType);
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        creation.setMultiValueSampleProperty(propertyType.getPermId(), List.of(new SamplePermId("/CISD/CL1"), sampleIds.get(0)));

        // When
        List<ExperimentPermId> experimentIds = v3api.createExperiments(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(experimentIds.size(), 1);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        Experiment experiment2 = v3api.getExperiments(sessionToken, experimentIds, fetchOptions).get(experimentIds.get(0));
        assertEquals(experiment2.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        Map<String, Sample[]> sampleProperties = experiment2.getSampleProperties();

        Sample[] samples = sampleProperties.get(propertyType.getPermId());
        Serializable[] sampleProps = Arrays.stream(samples).map(x -> x.getPermId().getPermId()).sorted().toArray(String[]::new);
        assertEquals(sampleProps, new Serializable[]{"200811050919915-8", sample2.getPermId().getPermId()});

        sampleProps = (Serializable[]) experiment2.getProperties().get(propertyType.getPermId());
        Arrays.sort(sampleProps);
        assertEquals(sampleProps, new Serializable[]{"200811050919915-8", sample2.getPermId().getPermId()});
        assertEquals(experiment2.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithMetaData()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.BOOLEAN);
        EntityTypePermId experimentType = createAnExperimentType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("EXPERIMENT_WITH_SAMPLE_PROPERTY");
        creation.setTypeId(experimentType);
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        creation.setBooleanProperty(propertyType.getPermId(), true);
        creation.setMetaData(Map.of("key", "value"));

        // When
        List<ExperimentPermId> experimentIds = v3api.createExperiments(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(experimentIds.size(), 1);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        Experiment experiment2 = v3api.getExperiments(sessionToken, experimentIds, fetchOptions).get(experimentIds.get(0));
        assertEquals(experiment2.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        assertEquals(experiment2.getBooleanProperty(propertyType.getPermId()).booleanValue(), true);
        assertEquals(experiment2.getMetaData(), Map.of("key", "value"));
    }

    @Test(dataProvider = USER_ROLES_PROVIDER)
    public void testCreateWithDifferentRoles(RoleWithHierarchy role)
    {
        testWithUserRole(role, params ->
        {
            final ExperimentCreation experimentCreation = new ExperimentCreation();
            experimentCreation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
            experimentCreation.setCode("TEST_EXPERIMENT_" + UUID.randomUUID());
            experimentCreation.setProjectId(params.space1Project1Id);
            experimentCreation.setProperty("DESCRIPTION", "test description");

            if (List.of(RoleWithHierarchy.RoleCode.ADMIN, RoleWithHierarchy.RoleCode.POWER_USER, RoleWithHierarchy.RoleCode.USER)
                    .contains(role.getRoleCode()))
            {
                v3api.createExperiments(params.userSessionToken, Collections.singletonList(experimentCreation));
            } else
            {
                assertAnyAuthorizationException(
                        () -> v3api.createExperiments(params.userSessionToken, Collections.singletonList(experimentCreation)));
            }
        });
    }

}

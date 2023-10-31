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
import static org.testng.Assert.assertNull;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.Attachment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.create.AttachmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.CreationId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.Tag;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.ITagId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.Batch;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.systemtest.authorization.ProjectAuthorizationUser;
import junit.framework.Assert;

/**
 * @author pkupczyk
 */
public class CreateSampleTest extends AbstractSampleTest
{
    private static final PropertyTypePermId PLATE_GEOMETRY = new PropertyTypePermId("$PLATE_GEOMETRY");

    @Test
    public void testCreateSampleUsingCreationIdAsSpaceId()
    {
        assertUserFailureException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                String sessionToken = v3api.login(TEST_USER, PASSWORD);

                SampleCreation creation = new SampleCreation();
                creation.setCode("TEST_SAMPLE_42");
                creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
                CreationId creationId = new CreationId("not-a-space-id");
                creation.setCreationId(creationId);
                creation.setSpaceId(creationId);

                v3api.createSamples(sessionToken, Collections.singletonList(creation));
            }
        }, "Object with CreationId = [not-a-space-id] has not been found");

    }

    @Test
    public void testCreateSharedSampleWithNoHomeSpaceAndNoAdminRights()
    {
        final String code = "TEST_TO_FAIL";
        SampleIdentifier identifier = new SampleIdentifier("/" + code);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                String sessionToken = v3api.login(TEST_NO_HOME_SPACE, PASSWORD);

                SampleCreation creation = new SampleCreation();
                creation.setCode(code);
                creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
                creation.setCreationId(new CreationId("creation " + code));

                v3api.createSamples(sessionToken, Collections.singletonList(creation));
            }
        }, identifier);
    }

    @Test
    public void testCreateSharedSampleWithNoAdminRights()
    {
        final String code = "TEST_TO_FAIL";
        SampleIdentifier identifier = new SampleIdentifier("/" + code);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                String sessionToken = v3api.login(TEST_ROLE_V3, PASSWORD);

                SampleCreation creation = new SampleCreation();
                creation.setCode(code);
                creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
                creation.setCreationId(new CreationId("creation " + code));

                v3api.createSamples(sessionToken, Collections.singletonList(creation));
            }
        }, identifier);
    }

    @Test
    public void testCreateWithIndexCheck()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation sample = sampleCreation("TO_BE_REINDEXED");

        List<SamplePermId> permIds = v3api.createSamples(sessionToken, Arrays.asList(sample));

        assertSamplesExists(permIds.get(0).getPermId());
    }

    @Test
    public void testCreateWithNonAutogeneratedCodeNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final SampleCreation sample = sampleCreation(null);

        assertUserFailureException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.createSamples(sessionToken, Arrays.asList(sample));
            }
        }, "Code cannot be empty for a non auto generated code", patternContains("checking data (1/1)"));
    }

    @Test
    public void testCreateWithAutogeneratedCodeNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final SampleCreation sample1 = sampleCreation(null);
        sample1.setAutoGeneratedCode(true);
        final SampleCreation sample2 = sampleCreation(null);
        sample2.setAutoGeneratedCode(true);

        List<SamplePermId> sampleWithAutogeneratedCode = v3api.createSamples(sessionToken, Arrays.asList(sample1, sample2));
        AssertionUtil.assertCollectionSize(sampleWithAutogeneratedCode, 2);
    }

    @Test
    public void testCreateWithAdminUserInAnotherSpace()
    {
        final String code = "TEST_TO_FAIL";
        final SampleIdentifier identifier = new SampleIdentifier("/TEST-SPACE/" + code);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                String sessionToken = v3api.login(TEST_ROLE_V3, PASSWORD);

                SampleCreation creation = new SampleCreation();
                creation.setCode(code);
                creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
                creation.setSpaceId(new SpacePermId("TEST-SPACE"));
                creation.setCreationId(new CreationId("creation " + code));

                v3api.createSamples(sessionToken, Collections.singletonList(creation));
            }
        }, identifier);
    }

    @Test
    public void testCreateWithCodeExisting()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleCreation sample = sampleCreation("SAMPLE_WITH_EXISTING_CODE");
        v3api.createSamples(sessionToken, Arrays.asList(sample));

        assertUserFailureException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.createSamples(sessionToken, Arrays.asList(sample));
            }
        }, "Insert/Update of sample (code: SAMPLE_WITH_EXISTING_CODE) failed because sample with the same code already exists");
    }

    @Test
    public void testCreateWithCodeIncorrect()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final SampleCreation sample = sampleCreation("?!*");

        assertUserFailureException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.createSamples(sessionToken, Arrays.asList(sample));
            }
        }, "The code '?!*' contains illegal characters", patternContains("checking data (1/1)", toDblQuotes("'code' : '?!*'")));
    }

    @Test
    public void testCreateWithTypeNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleCreation sample = new SampleCreation();
        sample.setCode("SAMPLE_WITHOUT_TYPE");
        sample.setSpaceId(new SpacePermId("CISD"));

        assertUserFailureException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.createSamples(sessionToken, Arrays.asList(sample));
            }
        }, "Type id cannot be null", patternContains("checking data (1/1)", toDblQuotes("'code' : 'SAMPLE_WITHOUT_TYPE'")));
    }

    @Test
    public void testCreateWithTypeNonexistent()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final IEntityTypeId typeId = new EntityTypePermId("IDONTEXIST");
        final SampleCreation sample = new SampleCreation();
        sample.setTypeId(typeId);
        sample.setCode("SAMPLE_WITH_NONEXISTENT_TYPE");
        sample.setSpaceId(new SpacePermId("CISD"));

        assertObjectNotFoundException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.createSamples(sessionToken, Arrays.asList(sample));
            }
        }, typeId, patternContains("checking data (1/1)", toDblQuotes("'code' : 'SAMPLE_WITH_NONEXISTENT_TYPE'")));
    }

    @Test
    public void testCreateWithPropertyCodeNonexistent()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_WITH_NONEXISTENT_PROPERTY_CODE");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setProperty("NONEXISTENT_PROPERTY_CODE", "any value");

        assertUserFailureException(new IDelegatedAction()
                                   {
                                       @Override
                                       public void execute()
                                       {
                                           v3api.createSamples(sessionToken, Arrays.asList(creation));
                                       }
                                   }, "Property type with code 'NONEXISTENT_PROPERTY_CODE' does not exist",
                patternContains("updating properties (1/1)", toDblQuotes("'identifier' : '/CISD/SAMPLE_WITH_NONEXISTENT_PROPERTY_CODE'")));
    }

    @Test
    public void testCreateWithPropertyValueIncorrect()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_WITH_INCORRECT_PROPERTY_VALUE");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setProperty("ORGANISM", "NON_EXISTENT_ORGANISM");

        assertUserFailureException(new IDelegatedAction()
                                   {
                                       @Override
                                       public void execute()
                                       {
                                           v3api.createSamples(sessionToken, Arrays.asList(creation));
                                       }
                                   }, "Vocabulary value 'NON_EXISTENT_ORGANISM' of property 'ORGANISM' is not valid. It must exist in 'ORGANISM' controlled vocabulary [FLY, DOG, GORILLA, RAT, HUMAN]",
                patternContains("updating properties (1/1)", toDblQuotes("'identifier' : '/CISD/SAMPLE_WITH_INCORRECT_PROPERTY_VALUE'")));
    }

    @Test
    public void testCreateWithPropertyValueMandatoryButNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_WITH_EMPTY_MANDATORY_PROPERTY");
        creation.setTypeId(new EntityTypePermId("CONTROL_LAYOUT"));
        creation.setSpaceId(new SpacePermId("CISD"));

        assertUserFailureException(new IDelegatedAction()
                                   {
                                       @Override
                                       public void execute()
                                       {
                                           v3api.createSamples(sessionToken, Arrays.asList(creation));
                                       }
                                   }, "Value of mandatory property '$PLATE_GEOMETRY' not specified",
                patternContains("verifying (1/1)", toDblQuotes("'identifier' : '/CISD/SAMPLE_WITH_EMPTY_MANDATORY_PROPERTY'")));
    }

    @Test
    public void testCreateWithSpaceNullAndExperimentNotNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleCreation creation = new SampleCreation();
        creation.setCode("SHARED_SAMPLE_TEST");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP1"));

        assertUserFailureException(new IDelegatedAction()
                                   {
                                       @Override
                                       public void execute()
                                       {
                                           v3api.createSamples(sessionToken, Collections.singletonList(creation));
                                       }
                                   }, "Shared samples cannot be attached to experiments",
                patternContains("verifying (1/1)", toDblQuotes("'identifier' : '/SHARED_SAMPLE_TEST'")));
    }

    @Test
    public void testCreateWithExperiment()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_WITH_EXPERIMENT");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP1"));

        List<SamplePermId> permIds = v3api.createSamples(sessionToken, Collections.singletonList(creation));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withSpace();
        fetchOptions.withExperiment();
        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, permIds, fetchOptions);
        Sample sample = map.get(permIds.get(0));
        assertEquals(sample.getCode(), "SAMPLE_WITH_EXPERIMENT");
        assertEquals(sample.getSpace().getCode(), "CISD");
        assertEquals(sample.getExperiment().getCode(), "EXP1");
        assertSampleIdentifier(sample, "/CISD/NEMO/SAMPLE_WITH_EXPERIMENT");
    }

    @Test
    public void testCreateWithSpaceNullAsAdminUser()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleCreation creation = new SampleCreation();
        creation.setCode("SHARED_SAMPLE_TEST");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));

        List<SamplePermId> permIds = v3api.createSamples(sessionToken, Collections.singletonList(creation));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withSpace();

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, permIds, fetchOptions);
        Sample sample = map.values().iterator().next();

        assertEquals(sample.getCode(), "SHARED_SAMPLE_TEST");
        assertNull(sample.getSpace());
        assertSampleIdentifier(sample, "/SHARED_SAMPLE_TEST");
    }

    @Test
    public void testCreateWithSpaceNullAsSpaceUser()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final SampleCreation creation = new SampleCreation();
        creation.setCode("SHARED_SAMPLE_TEST");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
                                                {
                                                    @Override
                                                    public void execute()
                                                    {
                                                        v3api.createSamples(sessionToken, Collections.singletonList(creation));
                                                    }
                                                }, new SampleIdentifier("/SHARED_SAMPLE_TEST"),
                patternContains("checking access (1/1)", toDblQuotes("'identifier' : '/SHARED_SAMPLE_TEST'")));
    }

    @Test
    public void testCreateWithSpaceUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISpaceId spaceId = new SpacePermId("CISD");
        final SampleCreation creation = new SampleCreation();
        creation.setCode("UNAUTHORIZED_SPACE");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(spaceId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
                                                {
                                                    @Override
                                                    public void execute()
                                                    {
                                                        v3api.createSamples(sessionToken, Collections.singletonList(creation));
                                                    }
                                                }, new SampleIdentifier("/CISD/UNAUTHORIZED_SPACE"),
                patternContains("checking access (1/1)", toDblQuotes("'identifier' : '/CISD/UNAUTHORIZED_SPACE'")));
    }

    @Test
    public void testCreateWithSpaceNonexistent()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISpaceId spaceId = new SpacePermId("IDONTEXIST");
        final SampleCreation creation = new SampleCreation();
        creation.setCode("NONEXISTENT_SPACE");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(spaceId);

        assertObjectNotFoundException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.createSamples(sessionToken, Collections.singletonList(creation));
            }
        }, spaceId, patternContains("setting relation sample-space (1/1)", toDblQuotes("'identifier' : '/NONEXISTENT_SPACE'")));
    }

    @Test
    public void testCreateWithSpaceInconsistent()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_WITH_INCONSISTENT_SPACE");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("TEST-SPACE"));
        creation.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP1"));

        assertUserFailureException(new IDelegatedAction()
                                   {
                                       @Override
                                       public void execute()
                                       {
                                           v3api.createSamples(sessionToken, Collections.singletonList(creation));
                                       }
                                   }, "Sample space must be the same as experiment space",
                patternContains("verifying (1/1)", toDblQuotes("'identifier' : '/TEST-SPACE/SAMPLE_WITH_INCONSISTENT_SPACE'")));
    }

    @Test
    public void testCreateWithExperimentUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final IExperimentId experimentId = new ExperimentPermId("200811050951882-1028");
        final SampleCreation creation = new SampleCreation();
        creation.setCode("UNAUTHORIZED_EXPERIMENT");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("TEST-SPACE"));
        creation.setExperimentId(experimentId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
                                                {
                                                    @Override
                                                    public void execute()
                                                    {
                                                        v3api.createSamples(sessionToken, Collections.singletonList(creation));
                                                    }
                                                }, experimentId,
                patternContains("setting relation sample-experiment (1/1)", toDblQuotes("'identifier' : '/TEST-SPACE/UNAUTHORIZED_EXPERIMENT'")));
    }

    @Test
    public void testCreateWithExperimentNonexistent()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final IExperimentId experimentId = new ExperimentPermId("IDONTEXIST");
        final SampleCreation creation = new SampleCreation();
        creation.setCode("NONEXISTENT_EXPERIMENT");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("TEST-SPACE"));
        creation.setExperimentId(experimentId);

        assertObjectNotFoundException(new IDelegatedAction()
                                      {
                                          @Override
                                          public void execute()
                                          {
                                              v3api.createSamples(sessionToken, Collections.singletonList(creation));
                                          }
                                      }, experimentId,
                patternContains("setting relation sample-experiment (1/1)", toDblQuotes("'identifier' : '/TEST-SPACE/NONEXISTENT_EXPERIMENT'")));
    }

    @Test(dataProvider = "tf-ft-tt")
    public void testCreateWithParentChild(boolean setRelationOnChild, boolean setRelationOnParent)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation sampleParent = new SampleCreation();
        sampleParent.setCode("SAMPLE_PARENT");
        sampleParent.setTypeId(new EntityTypePermId("CELL_PLATE"));
        sampleParent.setSpaceId(new SpacePermId("CISD"));
        sampleParent.setCreationId(new CreationId("parentid"));

        SampleCreation sampleChild = new SampleCreation();
        sampleChild.setCode("SAMPLE_CHILDREN");
        sampleChild.setTypeId(new EntityTypePermId("CELL_PLATE"));
        sampleChild.setSpaceId(new SpacePermId("CISD"));
        sampleChild.setCreationId(new CreationId("childid"));

        if (setRelationOnChild)
        {
            sampleChild.setParentIds(Arrays.asList(sampleParent.getCreationId()));
        }

        if (setRelationOnParent)
        {
            sampleParent.setChildIds(Arrays.asList(sampleChild.getCreationId()));
        }

        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken, Arrays.asList(sampleParent, sampleChild));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withChildrenUsing(fetchOptions);
        fetchOptions.withParentsUsing(fetchOptions);

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, sampleIds, fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        Sample parent = samples.get(0);
        Sample child = samples.get(1);

        AssertionUtil.assertCollectionContains(parent.getChildren(), child);
        AssertionUtil.assertCollectionContains(child.getParents(), parent);

        AssertionUtil.assertCollectionSize(child.getParents(), 1);
        AssertionUtil.assertCollectionSize(child.getChildren(), 0);

        AssertionUtil.assertCollectionSize(parent.getParents(), 0);
        AssertionUtil.assertCollectionSize(parent.getChildren(), 1);
    }

    @Test
    public void testCreateParentChildWithAnnotations()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleCreation parent1Creation = masterPlateCreation("CISD", "TEST_PARENT_1");
        SampleCreation parent2Creation = masterPlateCreation("CISD", "TEST_PARENT_2");
        SampleCreation parent3Creation = masterPlateCreation("CISD", "TEST_PARENT_3");
        CreationId creationId1 = new CreationId("PARENT_1");
        parent1Creation.setCreationId(creationId1);
        CreationId creationId2 = new CreationId("PARENT_2");
        parent2Creation.setCreationId(creationId2);
        CreationId creationId3 = new CreationId("PARENT_3");
        parent3Creation.setCreationId(creationId3);
        SamplePermId parent4Id = new SamplePermId("200811050924898-997");

        SampleCreation childCreation = masterPlateCreation("CISD", "TEST_CHILD");
        childCreation.setParentIds(Arrays.asList(creationId1, creationId2, creationId3, parent4Id));
        childCreation.relationship(creationId1)
                .addParentAnnotation("type", "father").addChildAnnotation("type", "daughter");
        childCreation.relationship(creationId2).addChildAnnotation("color", "red");
        childCreation.relationship(parent4Id).addChildAnnotation("color", "green").addParentAnnotation("color", "blue")
                .addChildAnnotation("type", "daughter");

        // When
        List<SamplePermId> ids = v3api.createSamples(sessionToken, Arrays.asList(childCreation, parent1Creation,
                parent2Creation, parent3Creation));

        // Then
        SamplePermId childId = ids.get(0);
        SamplePermId parent1Id = ids.get(1);
        SamplePermId parent2Id = ids.get(2);
        SamplePermId parent3Id = ids.get(3);
        assertAnnotations(sessionToken, "[type=father]", "[type=daughter]", parent1Id, childId);
        assertAnnotations(sessionToken, "[]", "[color=red]", parent2Id, childId);
        assertAnnotations(sessionToken, "[]", "[]", parent3Id, childId);
        assertAnnotations(sessionToken, "[color=blue]", "[color=green, type=daughter]", parent4Id, childId);
    }

    @Test
    public void testCreateWithTwoParentsWithAnnotations()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleCreation creation1 = creation("TEST_1001");
        SampleCreation creation2 = creation("TEST_1002");
        SamplePermId sc1Id = new SamplePermId("200811050924898-997"); // /CISD/C1

        creation2.setParentIds(Arrays.asList(creation1.getCreationId(), sc1Id));
        creation2.relationship(creation1.getCreationId()).addParentAnnotation("type", "mother").addChildAnnotation("type", "son");
        creation2.relationship(sc1Id).addChildAnnotation("color", "blue").addParentAnnotation("valid", "true");

        // When
        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken, Arrays.asList(creation1, creation2));

        // Then
        List<ISampleId> ids = new ArrayList<>();
        ids.addAll(sampleIds);
        ids.add(sc1Id);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withChildren();
        fetchOptions.withParents();
        Map<ISampleId, Sample> samples = v3api.getSamples(sessionToken, ids, fetchOptions);
        Sample s1 = samples.get(sampleIds.get(0));
        Sample s2 = samples.get(sampleIds.get(1));
        Sample sc1 = samples.get(sc1Id);
        assertAnnotations("[type=mother]", "[type=son]", s1, s2);
        assertAnnotations("[valid=true]", "[color=blue]", sc1, s2);
    }

    @Test
    public void testCreateWithTwoChildrenWithAnnotations()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleCreation creation1 = creation("TEST_1001");
        SampleCreation creation2 = creation("TEST_1002");
        SamplePermId sc1Id = new SamplePermId("200811050924898-997"); // /CISD/C1

        creation2.setChildIds(Arrays.asList(creation1.getCreationId(), sc1Id));
        creation2.relationship(creation1.getCreationId()).addChildAnnotation("type", "daughter").addParentAnnotation("type", "father");
        creation2.relationship(sc1Id).addParentAnnotation("valid", "false").addChildAnnotation("color", "green");

        // When
        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken, Arrays.asList(creation1, creation2));

        // Then
        List<ISampleId> ids = new ArrayList<>();
        ids.addAll(sampleIds);
        ids.add(sc1Id);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withChildren();
        fetchOptions.withParents();
        Map<ISampleId, Sample> samples = v3api.getSamples(sessionToken, ids, fetchOptions);
        Sample s1 = samples.get(sampleIds.get(0));
        Sample s2 = samples.get(sampleIds.get(1));
        Sample sc1 = samples.get(sc1Id);
        assertAnnotations("[type=father]", "[type=daughter]", s2, s1);
        assertAnnotations("[valid=false]", "[color=green]", s2, sc1);
    }

    @Test
    public void testCreateNetOfSamplesWithAnnotations()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleCreation creation1 = creation("TEST_1001");
        SampleCreation creation2 = creation("TEST_1002");
        SampleCreation creation3 = creation("TEST_1003");
        SampleCreation creation4 = creation("TEST_1004");
        SampleCreation creation5 = creation("TEST_1005");
        SampleCreation creation6 = creation("TEST_1006");
        SamplePermId sc1Id = new SamplePermId("200811050924898-997"); // /CISD/C1
        SamplePermId sc2Id = new SamplePermId("200811050925507-999"); // /CISD/C2
        SamplePermId sc3Id = new SamplePermId("200811050926038-1001"); // /CISD/C3

        // sc1 -> s3 -> s4 -> s2
        // s1 ---^ \--> s6
        creation3.setParentIds(Arrays.asList(creation1.getCreationId(), sc1Id));
        creation3.relationship(creation1.getCreationId()).addChildAnnotation("type", "son");
        creation3.relationship(sc1Id).addChildAnnotation("color", "blue");
        creation3.setChildIds(Arrays.asList(creation4.getCreationId()));
        creation4.setChildIds(Arrays.asList(creation2.getCreationId(), creation6.getCreationId()));
        creation4.relationship(creation2.getCreationId())
                .addParentAnnotation("type", "mother").addChildAnnotation("type", "daughter")
                .addParentAnnotation("color", "red");
        // sc2 -> s5 -> sc3
        creation5.setParentIds(Arrays.asList(sc2Id));
        creation5.setChildIds(Arrays.asList(sc3Id));
        creation5.relationship(creation2.getCreationId()).addChildAnnotation("ignored", "true"); // ignored because s2 isn't related

        // When
        List<SamplePermId> ids = v3api.createSamples(sessionToken, Arrays.asList(creation1, creation2,
                creation3, creation4, creation5, creation6));

        // Then
        ids = new ArrayList<>(ids);
        ids.addAll(Arrays.asList(sc1Id, sc2Id, sc3Id));
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withChildren();
        fetchOptions.withParents();
        Map<ISampleId, Sample> samples = v3api.getSamples(sessionToken, ids, fetchOptions);
        Sample s1 = samples.get(ids.get(0));
        Sample s2 = samples.get(ids.get(1));
        Sample s3 = samples.get(ids.get(2));
        Sample s4 = samples.get(ids.get(3));
        Sample s5 = samples.get(ids.get(4));
        Sample s6 = samples.get(ids.get(5));
        Sample sc1 = samples.get(sc1Id);
        Sample sc2 = samples.get(sc2Id);
        Sample sc3 = samples.get(sc3Id);
        // s1 as parent
        assertAnnotations("[]", "[type=son]", s1, s3);
        assertNoRelationships(s1, s2, s4, s5, s6, sc1, sc2, sc3);

        // s2 as parent
        assertNoRelationships(s2, s1, s3, s4, s5, s6, sc1, sc2, sc3);

        // s3 as parent
        assertAnnotations("[]", "[]", s3, s4);
        assertNoRelationships(s3, s1, s2, s5, s6, sc1, sc2, sc3);

        // s4 as parent
        assertAnnotations("[color=red, type=mother]", "[type=daughter]", s4, s2);
        assertAnnotations("[]", "[]", s4, s6);
        assertNoRelationships(s4, s1, s3, s5, sc1, sc2, sc3);

        // s5 as parent
        assertAnnotations("[]", "[]", s5, sc3);
        assertNoRelationships(s5, s1, s2, s3, s4, s6, sc1, sc2);

        // s6 as parent
        assertNoRelationships(s6, s1, s2, s3, s4, s5, sc1, sc2, sc3);

        // sc1 as parent
        assertAnnotations("[]", "[color=blue]", sc1, s3);
        assertNoRelationships(sc1, s1, s2, s4, s5, s6, sc2, sc3);

        // sc2 as parent
        assertAnnotations("[]", "[]", sc2, s5);
        assertNoRelationships(sc2, s1, s2, s3, s4, s6, sc1, sc3);

        // sc3 as parent
        assertNoRelationships(sc3, s1, s2, s3, s4, s5, s6, sc1, sc2);
    }

    private SampleCreation creation(String code)
    {
        SampleCreation creation = masterPlateCreation("CISD", code);
        creation.setCreationId(new CreationId(code));
        return creation;
    }

    @Test
    public void testCreateWithParentChildCircularDependency()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation sampleParent = new SampleCreation();
        sampleParent.setCode("SAMPLE_PARENT");
        sampleParent.setTypeId(new EntityTypePermId("CELL_PLATE"));
        sampleParent.setSpaceId(new SpacePermId("CISD"));
        sampleParent.setCreationId(new CreationId("parentid"));

        List<SamplePermId> parentPermId = v3api.createSamples(sessionToken, Arrays.asList(sampleParent));

        final SampleCreation sampleChild = new SampleCreation();
        sampleChild.setCode("SAMPLE_CHILD");
        sampleChild.setTypeId(new EntityTypePermId("CELL_PLATE"));
        sampleChild.setSpaceId(new SpacePermId("CISD"));
        sampleChild.setCreationId(new CreationId("childid"));
        sampleChild.setParentIds(parentPermId);

        final SampleCreation sampleGrandChild = new SampleCreation();
        sampleGrandChild.setCode("SAMPLE_GRAND_CHILD");
        sampleGrandChild.setTypeId(new EntityTypePermId("CELL_PLATE"));
        sampleGrandChild.setSpaceId(new SpacePermId("CISD"));
        sampleGrandChild.setParentIds(Arrays.asList(sampleChild.getCreationId()));
        sampleGrandChild.setChildIds(parentPermId);

        assertUserFailureException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.createSamples(sessionToken, Arrays.asList(sampleChild, sampleGrandChild));
            }
        }, "Circular dependency found");
    }

    @Test
    public void testCreateWithParentAndChildViolatingBusinessRules()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleCreation sampleParent = new SampleCreation();
        sampleParent.setCode("SAMPLE_PARENT");
        sampleParent.setTypeId(new EntityTypePermId("CELL_PLATE"));
        sampleParent.setSpaceId(new SpacePermId("CISD"));
        sampleParent.setCreationId(new CreationId("parentid"));

        final SampleCreation sampleChild = new SampleCreation();
        sampleChild.setCode("SAMPLE_CHILDREN");
        sampleChild.setTypeId(new EntityTypePermId("CELL_PLATE"));

        sampleChild.setParentIds(Arrays.asList(sampleParent.getCreationId()));

        assertUserFailureException(new IDelegatedAction()
                                   {
                                       @Override
                                       public void execute()
                                       {
                                           v3api.createSamples(sessionToken, Arrays.asList(sampleParent, sampleChild));
                                       }
                                   }, "can not be a space sample because of a child database instance sample",
                patternContains("verifying (1/2)", toDblQuotes("'identifier' : '/CISD/SAMPLE_PARENT'")));
    }

    @Test
    public void testCreateWithParentUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISampleId parentId = createCisdSample("PARENT_SAMPLE");
        final SampleCreation creation = sampleCreation("TEST-SPACE", "HAS_UNAUTHORIZED_PARENT");
        creation.setParentIds(Collections.singletonList(parentId));

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
                                                {
                                                    @Override
                                                    public void execute()
                                                    {
                                                        v3api.createSamples(sessionToken, Collections.singletonList(creation));
                                                    }
                                                }, parentId,
                patternContains("setting relation sample-parents (1/1)", toDblQuotes("'identifier' : '/TEST-SPACE/HAS_UNAUTHORIZED_PARENT'")));
    }

    @Test
    public void testCreateWithParentNonexistent()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISampleId parentId = new SamplePermId("IDONTEXIST");
        final SampleCreation creation = sampleCreation("TEST-SPACE", "HAS_NONEXISTENT_PARENT");
        creation.setParentIds(Collections.singletonList(parentId));

        assertObjectNotFoundException(new IDelegatedAction()
                                      {
                                          @Override
                                          public void execute()
                                          {
                                              v3api.createSamples(sessionToken, Collections.singletonList(creation));
                                          }
                                      }, parentId,
                patternContains("setting relation sample-parents (1/1)", toDblQuotes("'identifier' : '/TEST-SPACE/HAS_NONEXISTENT_PARENT'")));
    }

    @Test
    public void testCreateWithChildUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISampleId childId = createCisdSample("CHILD_SAMPLE");
        final SampleCreation creation = sampleCreation("TEST-SPACE", "HAS_UNAUTHORIZED_CHILD");
        creation.setChildIds(Collections.singletonList(childId));

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
                                                {
                                                    @Override
                                                    public void execute()
                                                    {
                                                        v3api.createSamples(sessionToken, Collections.singletonList(creation));
                                                    }
                                                }, childId,
                patternContains("setting relation sample-children (1/1)", toDblQuotes("'identifier' : '/TEST-SPACE/HAS_UNAUTHORIZED_CHILD'")));
    }

    @Test
    public void testCreateWithChildNonexistent()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISampleId childId = new SamplePermId("IDONTEXIST");
        final SampleCreation creation = sampleCreation("TEST-SPACE", "HAS_NONEXISTENT_CHILD");
        creation.setChildIds(Collections.singletonList(childId));

        assertObjectNotFoundException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.createSamples(sessionToken, Collections.singletonList(creation));
            }
        }, childId, patternContains("setting relation sample-children (1/1)", toDblQuotes("'identifier' : '/TEST-SPACE/HAS_NONEXISTENT_CHILD'")));
    }

    @Test
    public void testCreateWithChildViolatingBusinessRules()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleCreation sampleChild = new SampleCreation();
        sampleChild.setCode("SAMPLE_CHILDREN");
        sampleChild.setTypeId(new EntityTypePermId("CELL_PLATE"));

        sampleChild.setParentIds(Arrays.asList(new SampleIdentifier("/CISD/MP002-1")));

        assertUserFailureException(new IDelegatedAction()
                                   {
                                       @Override
                                       public void execute()
                                       {
                                           v3api.createSamples(sessionToken, Arrays.asList(sampleChild));
                                       }
                                   }, "The database instance sample '/SAMPLE_CHILDREN' can not be child of the space sample '/CISD/MP002-1'",
                patternContains("verifying (1/1)", toDblQuotes("'identifier' : '/SAMPLE_CHILDREN'")));
    }

    @Test
    public void testCreateWithContainerUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISampleId containerId = createCisdSample("CONTAINER_SAMPLE");
        final SampleCreation creation = sampleCreation("TEST-SPACE", "HAS_UNAUTHORIZED_CONTAINER");
        creation.setContainerId(containerId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
                                                {
                                                    @Override
                                                    public void execute()
                                                    {
                                                        v3api.createSamples(sessionToken, Collections.singletonList(creation));
                                                    }
                                                }, containerId,
                patternContains("setting relation sample-container (1/1)", toDblQuotes("'identifier' : '/TEST-SPACE/HAS_UNAUTHORIZED_CONTAINER'")));
    }

    @Test
    public void testCreateWithContainerNonexistent()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISampleId containerId = new SamplePermId("IDONTEXIST");
        final SampleCreation creation = sampleCreation("TEST-SPACE", "HAS_NONEXISTENT_CONTAINER");
        creation.setContainerId(containerId);

        assertObjectNotFoundException(new IDelegatedAction()
                                      {
                                          @Override
                                          public void execute()
                                          {
                                              v3api.createSamples(sessionToken, Collections.singletonList(creation));
                                          }
                                      }, containerId,
                patternContains("setting relation sample-container (1/1)", toDblQuotes("'identifier' : '/TEST-SPACE/HAS_NONEXISTENT_CONTAINER'")));
    }

    @Test(dataProvider = "tf-ft-tt")
    public void testCreateWithContainerComponents(boolean setRelationOnChild, boolean setRelationOnParent)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation sampleParent = new SampleCreation();
        sampleParent.setCode("SAMPLE_CONTAINER");
        sampleParent.setTypeId(new EntityTypePermId("CELL_PLATE"));
        sampleParent.setSpaceId(new SpacePermId("CISD"));
        sampleParent.setCreationId(new CreationId("parentid"));

        SampleCreation sampleChild = new SampleCreation();
        sampleChild.setCode("SAMPLE_SUB_SAMPLE");
        sampleChild.setTypeId(new EntityTypePermId("CELL_PLATE"));
        sampleChild.setSpaceId(new SpacePermId("CISD"));
        sampleChild.setCreationId(new CreationId("childid"));

        if (setRelationOnChild)
        {
            sampleChild.setContainerId(sampleParent.getCreationId());
        }

        if (setRelationOnParent)
        {
            sampleParent.setComponentIds(Arrays.asList(sampleChild.getCreationId()));
        }

        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken, Arrays.asList(sampleParent, sampleChild));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withComponentsUsing(fetchOptions);
        fetchOptions.withContainerUsing(fetchOptions);

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, sampleIds, fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        Sample container = samples.get(0);
        Sample subSample = samples.get(1);

        AssertionUtil.assertCollectionContains(container.getComponents(), subSample);
        assertEquals(subSample.getContainer(), container);

        AssertionUtil.assertCollectionSize(subSample.getComponents(), 0);

        assertEquals(container.getContainer(), null);
        AssertionUtil.assertCollectionSize(container.getComponents(), 1);
    }

    @Test
    public void testCreateWithContainerCircularDependency()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleCreation sample1 = sampleCreation("SAMPLE_1");
        final SampleCreation sample2 = sampleCreation("SAMPLE_2");
        final SampleCreation sample3 = sampleCreation("SAMPLE_3");

        sample2.setContainerId(sample1.getCreationId());
        sample3.setContainerId(sample2.getCreationId());
        sample1.setContainerId(sample3.getCreationId());

        assertUserFailureException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.createSamples(sessionToken, Arrays.asList(sample1, sample2, sample3));
            }
        }, "cannot be it's own container", patternContains("verifying (1/3)", toDblQuotes("'identifier' : '/CISD/SAMPLE_3:SAMPLE_1'")));
    }

    @Test(dataProvider = "tf-ft-tt", enabled = false)
    public void testCreateWithContainerInconsistent(boolean setSubSample, boolean setOtherContainer)
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleCreation container1 = new SampleCreation();
        container1.setCode("SAMPLE_CONTAINER_1");
        container1.setTypeId(new EntityTypePermId("CELL_PLATE"));
        container1.setSpaceId(new SpacePermId("CISD"));
        container1.setCreationId(new CreationId("cont1"));

        final SampleCreation container2 = new SampleCreation();
        container2.setCode("SAMPLE_CONTAINER_2");
        container2.setTypeId(new EntityTypePermId("CELL_PLATE"));
        container2.setSpaceId(new SpacePermId("CISD"));
        container2.setCreationId(new CreationId("cont2"));

        final SampleCreation subSample = new SampleCreation();
        subSample.setCode("SAMPLE_SUB_SAMPLE");
        subSample.setTypeId(new EntityTypePermId("CELL_PLATE"));
        subSample.setSpaceId(new SpacePermId("CISD"));
        subSample.setCreationId(new CreationId("subSample"));

        container1.setComponentIds(Arrays.asList(subSample.getCreationId()));

        if (setSubSample)
        {
            subSample.setContainerId(container2.getCreationId());
        }

        if (setOtherContainer)
        {
            container2.setComponentIds(Arrays.asList(subSample.getCreationId()));
        }

        assertUserFailureException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.createSamples(sessionToken, Arrays.asList(container1, container2, subSample));
            }
        }, "Inconsistent container");
    }

    @Test
    public void testCreateWithContainerCreatedWithSampleReferencedByIdentifier()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation sc1 = sampleCreation("SAMPLE_1");
        SampleCreation sc2 = sampleCreation("SAMPLE_2");

        sc2.setContainerId(new SampleIdentifier("/CISD/SAMPLE_1"));

        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken, Arrays.asList(sc1, sc2));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withContainerUsing(fetchOptions);

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, sampleIds, fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        AssertionUtil.assertCollectionSize(samples, 2);

        Sample sample1 = samples.get(0);
        Sample sample2 = samples.get(1);

        assertEquals(sample2.getContainer(), sample1);
    }

    @Test
    public void testCreateWithContainerCreatedWithSampleReferencedByCreationId()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation sc1 = sampleCreation("SAMPLE_1");
        SampleCreation sc2 = sampleCreation("SAMPLE_2");

        sc2.setContainerId(sc1.getCreationId());

        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken, Arrays.asList(sc1, sc2));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withContainerUsing(fetchOptions);

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, sampleIds, fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        AssertionUtil.assertCollectionSize(samples, 2);

        Sample sample1 = samples.get(0);
        Sample sample2 = samples.get(1);

        assertEquals(sample2.getContainer(), sample1);
    }

    @Test
    public void testCreateWithComponentAndIndexCheck()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SamplePermId componentId = createCisdSample("COMPONENT_SAMPLE");
        final SampleCreation creation = sampleCreation("CISD", "HAS_COMPONENT");
        creation.setComponentIds(Collections.singletonList(componentId));

        List<SamplePermId> containerIds = v3api.createSamples(sessionToken, Collections.singletonList(creation));
        assertEquals(containerIds.size(), 1);
        SamplePermId containerId = containerIds.get(0);

        assertSamplesExists(containerId.getPermId(), componentId.getPermId());
    }

    @Test
    public void testCreateWithComponentsUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISampleId componentId = createCisdSample("COMPONENT_SAMPLE");
        final SampleCreation creation = sampleCreation("TEST-SPACE", "HAS_UNAUTHORIZED_COMPONENT");
        creation.setComponentIds(Collections.singletonList(componentId));

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
                                                {
                                                    @Override
                                                    public void execute()
                                                    {
                                                        v3api.createSamples(sessionToken, Collections.singletonList(creation));
                                                    }
                                                }, componentId,
                patternContains("setting relation sample-components (1/1)", toDblQuotes("'identifier' : '/TEST-SPACE/HAS_UNAUTHORIZED_COMPONENT'")));
    }

    @Test
    public void testCreateWithComponentNonexistent()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISampleId componentId = new SamplePermId("IDONTEXIST");
        final SampleCreation creation = sampleCreation("TEST-SPACE", "HAS_NONEXISTENT_COMPONENT");
        creation.setComponentIds(Collections.singletonList(componentId));

        assertObjectNotFoundException(new IDelegatedAction()
                                      {
                                          @Override
                                          public void execute()
                                          {
                                              v3api.createSamples(sessionToken, Collections.singletonList(creation));
                                          }
                                      }, componentId,
                patternContains("setting relation sample-components (1/1)", toDblQuotes("'identifier' : '/TEST-SPACE/HAS_NONEXISTENT_COMPONENT'")));
    }

    @Test
    public void testCreateWithSystemProperty()
    {
        String systemPropertyName = "$PLATE_GEOMETRY";
        String systemPropertyValue = "384_WELLS_16X24";

        String simplePropertyCode = "PLATE_GEOMETRY";
        String simplePropertValue = "I'm just random";

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        createNewPropertyType(sessionToken, "MASTER_PLATE", simplePropertyCode);

        SampleCreation samp1 = new SampleCreation();
        samp1.setCode("SAMPLE_WITH_SYS_PROPERTY");
        samp1.setTypeId(new EntityTypePermId("MASTER_PLATE"));
        samp1.setSpaceId(new SpacePermId("CISD"));
        samp1.setProperty(systemPropertyName, systemPropertyValue);
        samp1.setProperty(simplePropertyCode, simplePropertValue);

        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken,
                Arrays.asList(samp1));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, sampleIds, fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        Sample foundSample = samples.get(0);
        assertEquals(foundSample.getProperty(systemPropertyName), systemPropertyValue);
        assertEquals(foundSample.getProperty(simplePropertyCode), simplePropertValue);
    }

    @Test
    public void testCreateWithUnknownPropertyOfTypeSample()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleCreation sample = new SampleCreation();
        sample.setCode("SAMPLE_WITH_SAMPLE_PROPERTY");
        sample.setTypeId(new EntityTypePermId("MASTER_PLATE"));
        sample.setSpaceId(new SpacePermId("CISD"));
        sample.setProperty("$PLATE_GEOMETRY", "384_WELLS_16X24");
        sample.setProperty("PLATE", "/CISD/CL1");

        // When
        assertUserFailureException(Void -> v3api.createSamples(sessionToken, Arrays.asList(sample)),
                // Then
                "Property type with code 'PLATE' does not exist");
    }

    @Test
    public void testCreateWithPropertyOfTypeSampleWithUnknownSample()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createASamplePropertyType(sessionToken, null);
        EntityTypePermId sampleType = createASampleType(sessionToken, false, propertyType);

        SampleCreation sample = new SampleCreation();
        sample.setCode("SAMPLE_WITH_SAMPLE_PROPERTY");
        sample.setTypeId(sampleType);
        sample.setSpaceId(new SpacePermId("CISD"));
        sample.setProperty(propertyType.getPermId(), "/CISD/UNKNOWN");

        // When
        assertUserFailureException(Void -> v3api.createSamples(sessionToken, Arrays.asList(sample)),
                // Then
                "Unknown sample: /CISD/UNKNOWN");
    }

    @Test
    public void testCreateWithMissingMandatoryPropertyOfTypeSample()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createASamplePropertyType(sessionToken, null);
        EntityTypePermId sampleType = createASampleType(sessionToken, true, propertyType);

        SampleCreation sample = new SampleCreation();
        sample.setCode("SAMPLE_WITH_SAMPLE_PROPERTY");
        sample.setTypeId(sampleType);
        sample.setSpaceId(new SpacePermId("CISD"));

        // When
        assertUserFailureException(Void -> v3api.createSamples(sessionToken, Arrays.asList(sample)),
                // Then
                "Value of mandatory property '" + propertyType.getPermId() + "' not specified.");
    }

    @Test
    public void testCreateWithPropertyOfTypeSampleWithSampleOfWrongType()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createASamplePropertyType(sessionToken,
                new EntityTypePermId("WELL", EntityKind.SAMPLE));
        EntityTypePermId sampleType = createASampleType(sessionToken, false, propertyType);

        SampleCreation sample = new SampleCreation();
        sample.setCode("SAMPLE_WITH_SAMPLE_PROPERTY");
        sample.setTypeId(sampleType);
        sample.setSpaceId(new SpacePermId("CISD"));
        sample.setProperty(propertyType.getPermId(), "200811050919915-8");

        // When
        assertUserFailureException(Void -> v3api.createSamples(sessionToken, Arrays.asList(sample)),
                // Then
                "Property " + propertyType.getPermId() + " is not a sample of type WELL but of type CONTROL_LAYOUT");
    }

    @Test
    public void testCreateWithPropertyOfTypeSampleWithSampleNotAccessable()
    {
        // Given
        String adminSessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createASamplePropertyType(adminSessionToken, null);
        EntityTypePermId sampleType = createASampleType(adminSessionToken, true, propertyType);
        v3api.logout(adminSessionToken);

        String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);
        SampleCreation sample = new SampleCreation();
        sample.setCode("SAMPLE_WITH_SAMPLE_PROPERTY");
        sample.setTypeId(sampleType);
        sample.setSpaceId(new SpacePermId("TEST-SPACE"));
        sample.setProperty(propertyType.getPermId(), "/CISD/CL1");

        // When
        assertUserFailureException(Void -> v3api.createSamples(sessionToken, Arrays.asList(sample)),
                // Then
                "Unknown sample: /CISD/CL1");
    }

    @Test
    public void testCreateWithPropertyOfTypeSampleWithInvisibleSample()
    {
        // Given
        String adminSessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createASamplePropertyType(adminSessionToken, null);
        EntityTypePermId sampleType = createASampleType(adminSessionToken, true, propertyType, PLATE_GEOMETRY);
        SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setCode("SAMPLE_WITH_SAMPLE_PROPERTY");
        sampleCreation.setTypeId(sampleType);
        sampleCreation.setSpaceId(new SpacePermId("TEST-SPACE"));
        sampleCreation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        sampleCreation.setProperty(propertyType.getPermId(), "/CISD/CL1");
        SamplePermId samplePermId = v3api.createSamples(adminSessionToken, Arrays.asList(sampleCreation)).get(0);
        v3api.logout(adminSessionToken);

        String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();

        // When
        Sample sample = v3api.getSamples(sessionToken, Arrays.asList(samplePermId), fetchOptions).get(samplePermId);

        // Then
        assertEquals(sample.getSampleProperties().toString(), "{}");
        assertEquals(sample.getProperties().toString(), "{$PLATE_GEOMETRY=384_WELLS_16X24}");
    }

    @Test
    public void testCreateWithPropertyOfTypeSampleByIdentifier()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createASamplePropertyType(sessionToken, null);
        EntityTypePermId sampleType = createASampleType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        SampleCreation sample = new SampleCreation();
        sample.setCode("SAMPLE_WITH_SAMPLE_PROPERTY");
        sample.setTypeId(sampleType);
        sample.setSpaceId(new SpacePermId("CISD"));
        sample.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        sample.setProperty(propertyType.getPermId(), "/MP");

        // When
        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken, Arrays.asList(sample));

        // Then
        assertEquals(sampleIds.size(), 1);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        Sample sample2 = v3api.getSamples(sessionToken, sampleIds, fetchOptions).get(sampleIds.get(0));
        Sample sampleProperty = sample2.getSampleProperties().get(propertyType.getPermId())[0];
        assertEquals(sampleProperty.getPermId().getPermId(), "200811050947161-652");
        assertEquals(sampleProperty.getIdentifier().getIdentifier(), "/MP");
        assertEquals(sample2.getSampleProperties().size(), 1);
        assertEquals(sample2.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        assertEquals(sample2.getProperties().get(propertyType.getPermId()), sampleProperty.getPermId().getPermId());
        assertEquals(sample2.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithPropertyOfTypeSampleByPermId()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createASamplePropertyType(sessionToken, null);
        EntityTypePermId sampleType = createASampleType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        SampleCreation sample = new SampleCreation();
        sample.setCode("SAMPLE_WITH_SAMPLE_PROPERTY");
        sample.setTypeId(sampleType);
        sample.setSpaceId(new SpacePermId("CISD"));
        sample.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        sample.setProperty(propertyType.getPermId(), "200811050919915-8");

        // When
        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken, Arrays.asList(sample));

        // Then
        assertEquals(sampleIds.size(), 1);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        Sample sample2 = v3api.getSamples(sessionToken, sampleIds, fetchOptions).get(sampleIds.get(0));
        Sample sampleProperty = sample2.getSampleProperties().get(propertyType.getPermId())[0];
        assertEquals(sampleProperty.getPermId().getPermId(), "200811050919915-8");
        assertEquals(sampleProperty.getIdentifier().getIdentifier(), "/CISD/CL1");
        assertEquals(sample2.getSampleProperties().size(), 1);
        assertEquals(sample2.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        assertEquals(sample2.getProperties().get(propertyType.getPermId()), sampleProperty.getPermId().getPermId());
        assertEquals(sample2.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithPropertyOfTypeDate()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.DATE);
        EntityTypePermId sampleType = createASampleType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        SampleCreation sample = new SampleCreation();
        sample.setCode("SAMPLE_WITH_SAMPLE_PROPERTY");
        sample.setTypeId(sampleType);
        sample.setSpaceId(new SpacePermId("CISD"));
        sample.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        sample.setProperty(propertyType.getPermId(), "2008-11-05");

        // When
        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken, Arrays.asList(sample));

        // Then
        assertEquals(sampleIds.size(), 1);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        Sample sample2 = v3api.getSamples(sessionToken, sampleIds, fetchOptions).get(sampleIds.get(0));
        assertEquals(sample2.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        assertEquals(sample2.getProperties().get(propertyType.getPermId()), "2008-11-05");
        assertEquals(sample2.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithPropertyOfTypeDateWIthTimestampInput()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.DATE);
        EntityTypePermId sampleType = createASampleType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        SampleCreation sample = new SampleCreation();
        sample.setCode("SAMPLE_WITH_SAMPLE_PROPERTY");
        sample.setTypeId(sampleType);
        sample.setSpaceId(new SpacePermId("CISD"));
        sample.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        sample.setProperty(propertyType.getPermId(), "2008-11-05 12:35:37");

        // When
        assertUserFailureException(Void -> v3api.createSamples(sessionToken, Arrays.asList(sample)),
                // Then
                "Date value '2008-11-05 12:35:37' has improper format.");
    }

    @Test
    public void testCreateWithPropertyOfTypeTimestamp()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.TIMESTAMP);
        EntityTypePermId sampleType = createASampleType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        SampleCreation sample = new SampleCreation();
        sample.setCode("SAMPLE_WITH_SAMPLE_PROPERTY");
        sample.setTypeId(sampleType);
        sample.setSpaceId(new SpacePermId("CISD"));
        sample.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        sample.setProperty(propertyType.getPermId(), "11/5/20 7:23 pm");

        // When
        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken, Arrays.asList(sample));

        // Then
        assertEquals(sampleIds.size(), 1);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        Sample sample2 = v3api.getSamples(sessionToken, sampleIds, fetchOptions).get(sampleIds.get(0));
        assertEquals(sample2.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        assertEquals(sample2.getProperties().get(propertyType.getPermId()), "2020-11-05 19:23:00 +0100");
        assertEquals(sample2.getProperties().size(), 2);
    }
    
    @Test
    public void testCreateWithPropertyOfTypeTimestampDedicatedMethod()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.TIMESTAMP);
        EntityTypePermId sampleType = createASampleType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        SampleCreation sample = new SampleCreation();
        sample.setCode("SAMPLE_WITH_SAMPLE_PROPERTY");
        sample.setTypeId(sampleType);
        sample.setSpaceId(new SpacePermId("CISD"));
        sample.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        ZonedDateTime time1 = ZonedDateTime.parse("2023-05-16T11:22:33+02");
        sample.setTimestampProperty(propertyType.getPermId(), time1);

        // When
        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken, Arrays.asList(sample));

        // Then
        assertEquals(sampleIds.size(), 1);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        Sample sample2 = v3api.getSamples(sessionToken, sampleIds, fetchOptions).get(sampleIds.get(0));
        assertEquals(sample2.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        assertEquals(sample2.getTimestampProperty(propertyType.getPermId()), ZonedDateTime.parse("2023-05-16T11:22:33+02:00"));
        assertEquals(sample2.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithMultiValuePropertyOfTypeTimestamp()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.TIMESTAMP, true);
        EntityTypePermId sampleType = createASampleType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        SampleCreation sample = new SampleCreation();
        sample.setCode("SAMPLE_WITH_SAMPLE_PROPERTY");
        sample.setTypeId(sampleType);
        sample.setSpaceId(new SpacePermId("CISD"));
        sample.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        ZonedDateTime time1 = ZonedDateTime.parse("2023-05-16T11:22:33+02");
        ZonedDateTime time2 = ZonedDateTime.parse("2023-05-18T11:17:03+02");
        sample.setMultiValueTimestampProperty(propertyType.getPermId(), List.of(time1, time2));

        // When
        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken, Arrays.asList(sample));

        // Then
        assertEquals(sampleIds.size(), 1);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        Sample sample2 = v3api.getSamples(sessionToken, sampleIds, fetchOptions).get(sampleIds.get(0));
        assertEquals(sample2.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        assertEqualsNoOrder(sample2.getMultiValueTimestampProperty(propertyType.getPermId()).toArray(ZonedDateTime[]::new), new ZonedDateTime[] {time1, time2});
        assertEquals(sample2.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithPropertyOfTypeJson()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.JSON);
        EntityTypePermId sampleType = createASampleType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        SampleCreation sample = new SampleCreation();
        sample.setCode("SAMPLE_WITH_SAMPLE_PROPERTY");
        sample.setTypeId(sampleType);
        sample.setSpaceId(new SpacePermId("CISD"));
        sample.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        sample.setJsonProperty(propertyType.getPermId(), "{\"key\": \"value\", \"array\":[1,2,3]}");

        // When
        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken, Arrays.asList(sample));

        // Then
        assertEquals(sampleIds.size(), 1);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        Sample sample2 = v3api.getSamples(sessionToken, sampleIds, fetchOptions).get(sampleIds.get(0));
        assertEquals(sample2.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        assertEquals(sample2.getJsonProperty(propertyType.getPermId()), "{\"key\": \"value\", \"array\": [1, 2, 3]}");
        assertEquals(sample2.getProperties().size(), 2);
    }
    
    @Test
    public void testCreateWithMultiValuePropertyOfTypeJson()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.JSON, true);
        EntityTypePermId sampleType = createASampleType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        SampleCreation sample = new SampleCreation();
        sample.setCode("SAMPLE_WITH_SAMPLE_PROPERTY");
        sample.setTypeId(sampleType);
        sample.setSpaceId(new SpacePermId("CISD"));
        sample.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        sample.setMultiValueJsonProperty(propertyType.getPermId(), List.of("{\"key\": \"value\", \"array\":[1,2,3]}", "{\"key\": \"value2\", \"array\":[]}"));

        // When
        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken, Arrays.asList(sample));

        // Then
        assertEquals(sampleIds.size(), 1);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        Sample sample2 = v3api.getSamples(sessionToken, sampleIds, fetchOptions).get(sampleIds.get(0));

        assertEquals(sample2.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        List<String> properties = sample2.getMultiValueJsonProperty(propertyType.getPermId());
        assertEquals(properties.size(), 2);
        assertEqualsNoOrder(properties.toArray(String[]::new), new String[] {"{\"key\": \"value\", \"array\": [1, 2, 3]}", "{\"key\": \"value2\", \"array\": []}"});
        assertEquals(sample2.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithMultiValuePropertyOfTypeInteger()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.INTEGER, true);
        EntityTypePermId sampleType = createASampleType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        SampleCreation sample = new SampleCreation();
        sample.setCode("SAMPLE_WITH_SAMPLE_PROPERTY");
        sample.setTypeId(sampleType);
        sample.setSpaceId(new SpacePermId("CISD"));
        sample.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        sample.setProperty(propertyType.getPermId(), new Long[] {1L, 1L, 3L});

        // When
        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken, Arrays.asList(sample));

        // Then
        assertEquals(sampleIds.size(), 1);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        Sample sample2 = v3api.getSamples(sessionToken, sampleIds, fetchOptions).get(sampleIds.get(0));

        assertEquals(sample2.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        assertEqualsNoOrder((Serializable[]) sample2.getProperties().get(propertyType.getPermId()), new Serializable[] { "1", "1", "3" });
        assertEqualsNoOrder(sample2.getMultiValueIntegerProperty(propertyType.getPermId()).toArray(Long[]::new), new Long[] { 1L, 1L, 3L });
        assertEquals(sample2.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithMultiValuePropertyOfTypeIntegerDedicatedMethod()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.INTEGER, true);
        EntityTypePermId sampleType = createASampleType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        SampleCreation sample = new SampleCreation();
        sample.setCode("SAMPLE_WITH_SAMPLE_PROPERTY");
        sample.setTypeId(sampleType);
        sample.setSpaceId(new SpacePermId("CISD"));
        sample.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        sample.setMultiValueIntegerProperty(propertyType.getPermId(), List.of(1L, 2L, 3L));

        // When
        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken, Arrays.asList(sample));

        // Then
        assertEquals(sampleIds.size(), 1);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        Sample sample2 = v3api.getSamples(sessionToken, sampleIds, fetchOptions).get(sampleIds.get(0));

        assertEquals(sample2.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        assertEqualsNoOrder((Serializable[]) sample2.getProperties().get(propertyType.getPermId()), new Serializable[] { "1", "2", "3" });
        assertEqualsNoOrder(sample2.getMultiValueIntegerProperty(propertyType.getPermId()).toArray(Long[]::new), new Long[] { 1L, 2L, 3L });
        assertEquals(sample2.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithPropertyOfTypeArrayInteger()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.ARRAY_INTEGER);
        EntityTypePermId sampleType = createASampleType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        SampleCreation sample = new SampleCreation();
        sample.setCode("SAMPLE_WITH_SAMPLE_PROPERTY");
        sample.setTypeId(sampleType);
        sample.setSpaceId(new SpacePermId("CISD"));
        sample.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        sample.setIntegerArrayProperty(propertyType.getPermId(), new Long[]{1L, 2L, 3L});

        // When
        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken, Arrays.asList(sample));

        // Then
        assertEquals(sampleIds.size(), 1);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        Sample sample2 = v3api.getSamples(sessionToken, sampleIds, fetchOptions).get(sampleIds.get(0));
        assertEquals(sample2.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        assertEquals(sample2.getIntegerArrayProperty(propertyType.getPermId()), new Long[]{1L, 2L, 3L});
        assertEquals(sample2.getProperties().size(), 2);
    }
    
     @Test
    public void testCreateWithMultiValuePropertyOfTypeArrayInteger()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.ARRAY_INTEGER, true);
        EntityTypePermId sampleType = createASampleType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        SampleCreation sample = new SampleCreation();
        sample.setCode("SAMPLE_WITH_SAMPLE_PROPERTY");
        sample.setTypeId(sampleType);
        sample.setSpaceId(new SpacePermId("CISD"));
        sample.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        sample.setProperty(propertyType.getPermId(), new Long[][]{ new Long[]{1L, 2L, 3L}, new Long[]{4L, 5L, 6L} });

        // When
        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken, Arrays.asList(sample));
        
        // Then
        assertEquals(sampleIds.size(), 1);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        Sample sample2 = v3api.getSamples(sessionToken, sampleIds, fetchOptions).get(sampleIds.get(0));
        assertEquals(sample2.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");

        List<Long[]> props = sample2.getMultiValueIntegerArrayProperty(propertyType.getPermId());
        assertEquals(props.size(), 2);
        for(Long[] prop : props) {
            if(prop[0] > 3L) {
                assertEqualsNoOrder(prop, new Long[] {4L, 5L, 6L});
            } else {
                assertEqualsNoOrder(prop, new Long[] {1L, 2L, 3L});
            }
        }

        assertEquals(sample2.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithMultiValuePropertyOfTypeArrayIntegerDedicatedMethod()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.ARRAY_INTEGER, true);
        EntityTypePermId sampleType = createASampleType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        SampleCreation sample = new SampleCreation();
        sample.setCode("SAMPLE_WITH_SAMPLE_PROPERTY");
        sample.setTypeId(sampleType);
        sample.setSpaceId(new SpacePermId("CISD"));
        sample.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        sample.setMultiValueIntegerArrayProperty(propertyType.getPermId(), List.of( new Long[]{1L, 2L, 3L}, new Long[]{4L, 5L, 6L} ));

        // When
        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken, Arrays.asList(sample));

        // Then
        assertEquals(sampleIds.size(), 1);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        Sample sample2 = v3api.getSamples(sessionToken, sampleIds, fetchOptions).get(sampleIds.get(0));
        assertEquals(sample2.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");

        List<Long[]> props = sample2.getMultiValueIntegerArrayProperty(propertyType.getPermId());
        assertEquals(props.size(), 2);
        for(Long[] prop : props) {
            if(prop[0] > 3L) {
                assertEqualsNoOrder(prop, new Long[] {4L, 5L, 6L});
            } else {
                assertEqualsNoOrder(prop, new Long[] {1L, 2L, 3L});
            }
        }

        assertEquals(sample2.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithPropertyOfTypeArrayReal()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.ARRAY_REAL);
        EntityTypePermId sampleType = createASampleType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        SampleCreation sample = new SampleCreation();
        sample.setCode("SAMPLE_WITH_SAMPLE_PROPERTY");
        sample.setTypeId(sampleType);
        sample.setSpaceId(new SpacePermId("CISD"));
        sample.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        sample.setRealArrayProperty(propertyType.getPermId(), new Double[]{1.0, 2.0, 3.0});

        // When
        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken, Arrays.asList(sample));

        // Then
        assertEquals(sampleIds.size(), 1);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        Sample sample2 = v3api.getSamples(sessionToken, sampleIds, fetchOptions).get(sampleIds.get(0));
        assertEquals(sample2.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        assertEquals(sample2.getRealArrayProperty(propertyType.getPermId()), new Double[]{1.0, 2.0, 3.0});
        assertEquals(sample2.getProperties().size(), 2);
    }
    
    @Test
    public void testCreateWithMultiValuePropertyOfTypeArrayReal()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.ARRAY_REAL, true);
        EntityTypePermId sampleType = createASampleType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        SampleCreation sample = new SampleCreation();
        sample.setCode("SAMPLE_WITH_SAMPLE_PROPERTY");
        sample.setTypeId(sampleType);
        sample.setSpaceId(new SpacePermId("CISD"));
        sample.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        sample.setProperty(propertyType.getPermId(), new Double[][]{ new Double[]{1.0, 2.0, 3.0}, new Double[]{4.0, 5.0, 6.0} });

        // When
        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken, Arrays.asList(sample));
        
        // Then
        assertEquals(sampleIds.size(), 1);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        Sample sample2 = v3api.getSamples(sessionToken, sampleIds, fetchOptions).get(sampleIds.get(0));
        assertEquals(sample2.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");

        List<Long[]> props = sample2.getMultiValueIntegerArrayProperty(propertyType.getPermId());
        assertEquals(props.size(), 2);
        for(Long[] prop : props) {
            if(prop[0] > 3L) {
                assertEqualsNoOrder(prop, new Long[] {4L, 5L, 6L});
            } else {
                assertEqualsNoOrder(prop, new Long[] {1L, 2L, 3L});
            }
        }

        assertEquals(sample2.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithMultiValuePropertyOfTypeArrayRealDedicatedMethod()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.ARRAY_INTEGER, true);
        EntityTypePermId sampleType = createASampleType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        SampleCreation sample = new SampleCreation();
        sample.setCode("SAMPLE_WITH_SAMPLE_PROPERTY");
        sample.setTypeId(sampleType);
        sample.setSpaceId(new SpacePermId("CISD"));
        sample.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        sample.setMultiValueIntegerArrayProperty(propertyType.getPermId(), List.of( new Long[]{1L, 2L, 3L}, new Long[]{4L, 5L, 6L} ));

        // When
        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken, Arrays.asList(sample));

        // Then
        assertEquals(sampleIds.size(), 1);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        Sample sample2 = v3api.getSamples(sessionToken, sampleIds, fetchOptions).get(sampleIds.get(0));
        assertEquals(sample2.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");

        List<Long[]> props = sample2.getMultiValueIntegerArrayProperty(propertyType.getPermId());
        assertEquals(props.size(), 2);
        for(Long[] prop : props) {
            if(prop[0] > 3L) {
                assertEqualsNoOrder(prop, new Long[] {4L, 5L, 6L});
            } else {
                assertEqualsNoOrder(prop, new Long[] {1L, 2L, 3L});
            }
        }

        assertEquals(sample2.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithPropertyOfTypeArrayString()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.ARRAY_STRING);
        EntityTypePermId sampleType = createASampleType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        SampleCreation sample = new SampleCreation();
        sample.setCode("SAMPLE_WITH_SAMPLE_PROPERTY");
        sample.setTypeId(sampleType);
        sample.setSpaceId(new SpacePermId("CISD"));
        sample.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        sample.setStringArrayProperty(propertyType.getPermId(), new String[]{"a", "b", "c"});

        // When
        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken, Arrays.asList(sample));

        // Then
        assertEquals(sampleIds.size(), 1);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        Sample sample2 = v3api.getSamples(sessionToken, sampleIds, fetchOptions).get(sampleIds.get(0));
        assertEquals(sample2.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        assertEquals(sample2.getStringArrayProperty(propertyType.getPermId()), new String[]{"a", "b", "c"});
        assertEquals(sample2.getProperties().size(), 2);
    }
    
    @Test
    public void testCreateWithMultiValuePropertyOfTypeArrayString()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.ARRAY_STRING, true);
        EntityTypePermId sampleType = createASampleType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        SampleCreation sample = new SampleCreation();
        sample.setCode("SAMPLE_WITH_SAMPLE_PROPERTY");
        sample.setTypeId(sampleType);
        sample.setSpaceId(new SpacePermId("CISD"));
        sample.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        sample.setMultiValueStringArrayProperty(propertyType.getPermId(), List.of(new String[]{"a,a", "b", "c"}, new String[]{"a", "b", "c", "d"}));

        // When
        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken, Arrays.asList(sample));
        
        // Then
        assertEquals(sampleIds.size(), 1);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        Sample sample2 = v3api.getSamples(sessionToken, sampleIds, fetchOptions).get(sampleIds.get(0));
        assertEquals(sample2.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        List<String[]> result = sample2.getMultiValueStringArrayProperty(propertyType.getPermId());
        assertEquals(result.size(), 2);
        for(String[] prop : result) {
            if(prop.length > 3) {
                assertEqualsNoOrder(prop, new String[]{"a", "b", "c", "d"});
            } else {
                assertEqualsNoOrder(prop, new String[]{"a,a", "b", "c"});
            }
        }
        assertEquals(sample2.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithPropertyOfTypeArrayTimestamp()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.ARRAY_TIMESTAMP);
        EntityTypePermId sampleType = createASampleType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        SampleCreation sample = new SampleCreation();
        sample.setCode("SAMPLE_WITH_SAMPLE_PROPERTY");
        sample.setTypeId(sampleType);
        sample.setSpaceId(new SpacePermId("CISD"));
        sample.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        ZonedDateTime time1 = ZonedDateTime.parse("2023-05-16T11:22:33+02");
        ZonedDateTime time2 = ZonedDateTime.parse("2023-05-18T11:17:03+02");
        sample.setTimestampArrayProperty(propertyType.getPermId(), new ZonedDateTime[]{time1, time2});

        // When
        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken, Arrays.asList(sample));

        // Then
        assertEquals(sampleIds.size(), 1);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        Sample sample2 = v3api.getSamples(sessionToken, sampleIds, fetchOptions).get(sampleIds.get(0));
        assertEquals(sample2.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        assertEquals(sample2.getTimestampArrayProperty(propertyType.getPermId()), new ZonedDateTime[]{time1, time2});
        assertEquals(sample2.getProperties().size(), 2);
    }
    
    @Test
    public void testCreateWithMultiValuePropertyOfTypeArrayTimestamp()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.ARRAY_TIMESTAMP, true);
        EntityTypePermId sampleType = createASampleType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        SampleCreation sample = new SampleCreation();
        sample.setCode("SAMPLE_WITH_SAMPLE_PROPERTY");
        sample.setTypeId(sampleType);
        sample.setSpaceId(new SpacePermId("CISD"));
        sample.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        ZonedDateTime time1 = ZonedDateTime.parse("2023-05-16T11:22:33+02");
        ZonedDateTime time2 = ZonedDateTime.parse("2023-05-18T11:17:03+02");
        ZonedDateTime time3 = ZonedDateTime.parse("2023-05-20T11:10:03+02");
        sample.setMultiValueTimestampArrayProperty(propertyType.getPermId(), List.of(new ZonedDateTime[]{time1, time2}, new ZonedDateTime[]{time3, time2, time1}));

        // When
        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken, Arrays.asList(sample));

        // Then
        assertEquals(sampleIds.size(), 1);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        Sample sample2 = v3api.getSamples(sessionToken, sampleIds, fetchOptions).get(sampleIds.get(0));
        assertEquals(sample2.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");

        List<ZonedDateTime[]> properties = sample2.getMultiValueTimestampArrayProperty(propertyType.getPermId());
        assertEquals(properties.size(), 2);
        for(ZonedDateTime[] prop : properties) {
            if(prop.length == 2) {
                assertEqualsNoOrder(prop, new ZonedDateTime[]{time1, time2});
            } else {
                assertEqualsNoOrder(prop, new ZonedDateTime[]{time1, time2, time3});
            }
        }
        assertEquals(sample2.getProperties().size(), 2);
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

        EntityTypePermId sampleType = createASampleType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        SampleCreation sample = new SampleCreation();
        sample.setCode("SAMPLE_WITH_MULTI_VOCAB_PROPERTY-" + System.currentTimeMillis());
        sample.setTypeId(sampleType);
        sample.setSpaceId(new SpacePermId("CISD"));
        sample.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        sample.setMultiValueControlledVocabularyProperty(propertyType.getPermId(), List.of("DOG", "HUMAN"));

        // When
        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken, Arrays.asList(sample));

        // Then
        assertEquals(sampleIds.size(), 1);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        Sample sample2 = v3api.getSamples(sessionToken, sampleIds, fetchOptions).get(sampleIds.get(0));
        assertEquals(sample2.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        List<String> vocabProperties = sample2.getMultiValueControlledVocabularyProperty(propertyType.getPermId());
        Collections.sort(vocabProperties);
        assertEquals(vocabProperties, List.of("DOG", "HUMAN"));
        assertEquals(sample2.getProperties().size(), 2);
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

        EntityTypePermId sampleType = createASampleType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        SampleCreation sample = new SampleCreation();
        sample.setCode("SAMPLE_WITH_MULTI_VOCAB_PROPERTY-" + System.currentTimeMillis());
        sample.setTypeId(sampleType);
        sample.setSpaceId(new SpacePermId("CISD"));
        sample.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        sample.setMultiValueControlledVocabularyProperty(propertyType.getPermId(), List.of("DOG", "HUMAN"));

        // When
        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken, Arrays.asList(sample));

        // Then
        assertEquals(sampleIds.size(), 1);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        Sample sample2 = v3api.getSamples(sessionToken, sampleIds, fetchOptions).get(sampleIds.get(0));
        assertEquals(sample2.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        List<String> vocabProperties = sample2.getMultiValueControlledVocabularyProperty(propertyType.getPermId());
        Collections.sort(vocabProperties);
        assertEquals(vocabProperties, List.of("DOG", "HUMAN"));
        assertEquals(sample2.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithMultiValuePropertySample() {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        //Create new sample that will be used as property
        PropertyTypePermId propertyType1 = createASamplePropertyType(sessionToken, null);
        EntityTypePermId sampleType1 = createASampleType(sessionToken, true, propertyType1, PLATE_GEOMETRY);

        SampleCreation sample = new SampleCreation();
        sample.setCode("SAMPLE_WITH_SOME_SAMPLE_PROPERTY-" + System.currentTimeMillis());
        sample.setTypeId(sampleType1);
        sample.setSpaceId(new SpacePermId("CISD"));
        sample.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        sample.setProperty(propertyType1.getPermId(), "200811050919915-8");

        List<SamplePermId> testSampleIds = v3api.createSamples(sessionToken, Arrays.asList(sample));

        assertEquals(testSampleIds.size(), 1);

        // Create multi-value sample property
        final PropertyTypeCreation propertyTypeCreation = new PropertyTypeCreation();
        propertyTypeCreation.setCode("TYPE-" + System.currentTimeMillis());
        propertyTypeCreation.setDataType(DataType.SAMPLE);
        propertyTypeCreation.setLabel("label");
        propertyTypeCreation.setDescription("description");
        propertyTypeCreation.setMultiValue(true);
        PropertyTypePermId propertyType = v3api.createPropertyTypes(sessionToken, Collections.singletonList(propertyTypeCreation)).get(0);

        EntityTypePermId sampleType = createASampleType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        // Create a sample with multi-value sample property
        SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setCode("SAMPLE_WITH_MULTI_SAMPLE_PROPERTY-" + System.currentTimeMillis());
        sampleCreation.setTypeId(sampleType);
        sampleCreation.setSpaceId(new SpacePermId("CISD"));
        sampleCreation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        sampleCreation.setProperty(propertyType.getPermId(), new String[] {"/CISD/CL1", testSampleIds.get(0).getPermId()});

        // When
        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken, Arrays.asList(sampleCreation));

        // Then
        assertEquals(sampleIds.size(), 1);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        Sample sample2 = v3api.getSamples(sessionToken, sampleIds, fetchOptions).get(sampleIds.get(0));
        assertEquals(sample2.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");

        Map<String, Sample[]> sampleProperties = sample2.getSampleProperties();

        Sample[] samples = sampleProperties.get(propertyType.getPermId());
        Serializable[] sampleProps = Arrays.stream(samples).map(x -> x.getPermId().getPermId()).sorted().toArray(String[]::new);
        assertEquals(sampleProps, new Serializable[]{"200811050919915-8", testSampleIds.get(0).getPermId()});

        sampleProps = (Serializable[]) sample2.getProperties().get(propertyType.getPermId());
        Arrays.sort(sampleProps);
        assertEquals(sampleProps, new Serializable[]{"200811050919915-8", testSampleIds.get(0).getPermId()});
        assertEquals(sample2.getProperties().size(), 2);
    }
    
    @Test
    public void testCreateWithMultiValuePropertySampleDedicatedMethod()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        //Create new sample that will be used as property
        PropertyTypePermId propertyType1 = createASamplePropertyType(sessionToken, null);
        EntityTypePermId sampleType1 = createASampleType(sessionToken, true, propertyType1, PLATE_GEOMETRY);

        SampleCreation sample = new SampleCreation();
        sample.setCode("SAMPLE_WITH_SOME_SAMPLE_PROPERTY-" + System.currentTimeMillis());
        sample.setTypeId(sampleType1);
        sample.setSpaceId(new SpacePermId("CISD"));
        sample.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        sample.setProperty(propertyType1.getPermId(), "200811050919915-8");

        List<SamplePermId> testSampleIds = v3api.createSamples(sessionToken, Arrays.asList(sample));

        assertEquals(testSampleIds.size(), 1);

        // Create multi-value sample property
        final PropertyTypeCreation propertyTypeCreation = new PropertyTypeCreation();
        propertyTypeCreation.setCode("TYPE-" + System.currentTimeMillis());
        propertyTypeCreation.setDataType(DataType.SAMPLE);
        propertyTypeCreation.setLabel("label");
        propertyTypeCreation.setDescription("description");
        propertyTypeCreation.setMultiValue(true);
        PropertyTypePermId propertyType = v3api.createPropertyTypes(sessionToken, Collections.singletonList(propertyTypeCreation)).get(0);

        EntityTypePermId sampleType = createASampleType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        // Create a sample with multi-value sample property
        SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setCode("SAMPLE_WITH_MULTI_SAMPLE_PROPERTY-" + System.currentTimeMillis());
        sampleCreation.setTypeId(sampleType);
        sampleCreation.setSpaceId(new SpacePermId("CISD"));
        sampleCreation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        sampleCreation.setMultiValueSampleProperty(propertyType.getPermId(), List.of(new SamplePermId("200811050919915-8"), testSampleIds.get(0)));

        // When
        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken, Arrays.asList(sampleCreation));

        // Then
        assertEquals(sampleIds.size(), 1);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        Sample sample2 = v3api.getSamples(sessionToken, sampleIds, fetchOptions).get(sampleIds.get(0));
        assertEquals(sample2.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");

        Map<String, Sample[]> sampleProperties = sample2.getSampleProperties();

        Sample[] samples = sampleProperties.get(propertyType.getPermId());
        Serializable[] sampleProps = Arrays.stream(samples).map(x -> x.getPermId().getPermId()).sorted().toArray(String[]::new);
        assertEquals(sampleProps, new Serializable[]{"200811050919915-8", testSampleIds.get(0).getPermId()});

        sampleProps = (Serializable[]) sample2.getProperties().get(propertyType.getPermId());
        Arrays.sort(sampleProps);
        assertEquals(sampleProps, new Serializable[]{"200811050919915-8", testSampleIds.get(0).getPermId()});
        assertEquals(sample2.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithMultiValuePropertySample2() {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        //Create sample
        PropertyTypePermId propertyType1 = createASamplePropertyType(sessionToken, null);
        EntityTypePermId sampleType1 = createASampleType(sessionToken, true, propertyType1, PLATE_GEOMETRY);

        SampleCreation sample = new SampleCreation();
        sample.setCode("SAMPLE_WITH_SOME_SAMPLE_PROPERTY-" + System.currentTimeMillis());
        sample.setTypeId(sampleType1);
        sample.setSpaceId(new SpacePermId("CISD"));
        sample.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        sample.setProperty(propertyType1.getPermId(), "200811050919915-8");

        List<SamplePermId> testSampleIds = v3api.createSamples(sessionToken, Arrays.asList(sample));

        assertEquals(testSampleIds.size(), 1);


        final PropertyTypeCreation propertyTypeCreation = new PropertyTypeCreation();
        propertyTypeCreation.setCode("TYPE-" + System.currentTimeMillis());
        propertyTypeCreation.setDataType(DataType.SAMPLE);
        propertyTypeCreation.setLabel("label");
        propertyTypeCreation.setDescription("description");
        propertyTypeCreation.setMultiValue(true);
        PropertyTypePermId propertyType = v3api.createPropertyTypes(sessionToken, Collections.singletonList(propertyTypeCreation)).get(0);

        EntityTypePermId sampleType = createASampleType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setCode("SAMPLE_WITH_MULTI_SAMPLE_PROPERTY-" + System.currentTimeMillis());
        sampleCreation.setTypeId(sampleType);
        sampleCreation.setSpaceId(new SpacePermId("CISD"));
        sampleCreation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        sampleCreation.setMultiValueSampleProperty(propertyType.getPermId(), List.of(new SamplePermId("/CISD/CL1"), testSampleIds.get(0)));

        // When
        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken, Arrays.asList(sampleCreation));

        // Then
        assertEquals(sampleIds.size(), 1);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        Sample sample2 = v3api.getSamples(sessionToken, sampleIds, fetchOptions).get(sampleIds.get(0));
        assertEquals(sample2.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");

        Map<String, Sample[]> sampleProperties = sample2.getSampleProperties();

        Sample[] samples = sampleProperties.get(propertyType.getPermId());
        Serializable[] sampleProps = Arrays.stream(samples).map(x -> x.getPermId().getPermId()).sorted().toArray(String[]::new);
        assertEquals(sampleProps, new Serializable[]{"200811050919915-8", testSampleIds.get(0).getPermId()});

        sampleProps = (Serializable[]) sample2.getProperties().get(propertyType.getPermId());
        Arrays.sort(sampleProps);
        assertEquals(sampleProps, new Serializable[]{"200811050919915-8", testSampleIds.get(0).getPermId()});
        assertEquals(sample2.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithMetaData()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.INTEGER);
        EntityTypePermId sampleType = createASampleType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        SampleCreation sample = new SampleCreation();
        sample.setCode("SAMPLE_WITH_PROPERTY");
        sample.setTypeId(sampleType);
        sample.setSpaceId(new SpacePermId("CISD"));
        sample.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        sample.setIntegerProperty(propertyType.getPermId(), 123L);
        sample.setMetaData(Map.of("key", "value"));

        // When
        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken, Arrays.asList(sample));

        // Then
        assertEquals(sampleIds.size(), 1);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        Sample sample2 = v3api.getSamples(sessionToken, sampleIds, fetchOptions).get(sampleIds.get(0));
        assertEquals(sample2.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        assertEquals(sample2.getIntegerProperty(propertyType.getPermId()).longValue(), 123L);
        assertEquals(sample2.getProperties().size(), 2);

        assertEquals(sample2.getMetaData().size(), 1);
        assertEquals(sample2.getMetaData(), Map.of("key", "value"));
    }

    @Test
    public void testCreateWithMultipleSamples()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleCreation samp1 = new SampleCreation();
        samp1.setCode("SAMPLE_WITH_SPACE1");
        samp1.setTypeId(new EntityTypePermId("CELL_PLATE"));
        samp1.setSpaceId(new SpacePermId("CISD"));
        samp1.setProperty("COMMENT", "hello");
        samp1.setContainerId(new SampleIdentifier("/CISD/MP002-1"));
        samp1.setTagIds(Arrays.<ITagId>asList(
                new TagPermId("/test/TEST_METAPROJECTS"), new TagPermId("/test/ANOTHER_TEST_METAPROJECTS")));
        AttachmentCreation a = new AttachmentCreation();

        byte[] attachmentContent = "attachment".getBytes();
        a.setContent(attachmentContent);
        a.setDescription("attachment description");
        a.setFileName("attachment.txt");
        a.setTitle("attachment title");
        samp1.setAttachments(Arrays.asList(a));

        SampleCreation samp2 = new SampleCreation();
        samp2.setCode("SAMPLE_WITH_SPACE2");
        samp2.setTypeId(new EntityTypePermId("CELL_PLATE"));
        samp2.setSpaceId(new SpacePermId("CISD"));
        samp2.setContainerId(new SamplePermId("200811050917877-331"));
        List<SamplePermId> sample2Parents = Arrays.asList(new SamplePermId("200811050917877-331"), new SamplePermId("200902091219327-1025"));
        samp2.setParentIds(sample2Parents);

        SampleCreation sampleWithOutSpace = new SampleCreation();
        sampleWithOutSpace.setCode("SAMPLE_WITHOUT_SPACE");
        sampleWithOutSpace.setTypeId(new EntityTypePermId("CELL_PLATE"));
        sampleWithOutSpace.setChildIds(sample2Parents);

        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken,
                Arrays.asList(samp1, samp2, sampleWithOutSpace));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withType();
        fetchOptions.withSpace();
        fetchOptions.withRegistrator();
        fetchOptions.withAttachments().withContent();
        fetchOptions.withContainer();
        fetchOptions.withTags();
        fetchOptions.withParents();

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, sampleIds, fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        Sample sampleWithSpace1 = samples.get(0);
        assertEquals(sampleWithSpace1.getCode(), "SAMPLE_WITH_SPACE1");
        assertEquals(sampleWithSpace1.getType().getCode(), "CELL_PLATE");
        assertEquals(sampleWithSpace1.getSpace().getPermId().getPermId(), "CISD");
        assertEquals(sampleWithSpace1.getIdentifier().getIdentifier(), "/CISD/MP002-1:SAMPLE_WITH_SPACE1");
        assertSampleIdentifier(sampleWithSpace1, "/CISD/MP002-1:SAMPLE_WITH_SPACE1");
        assertEquals(sampleWithSpace1.getRegistrator().getUserId(), TEST_USER);
        assertEquals(sampleWithSpace1.getContainer().getCode(), "MP002-1");
        List<Attachment> attachments = sampleWithSpace1.getAttachments();
        AssertionUtil.assertCollectionSize(attachments, 1);
        assertEquals(attachments.get(0).getContent(), attachmentContent);
        HashSet<String> tagIds = new HashSet<String>();
        for (Tag tag : sampleWithSpace1.getTags())
        {
            tagIds.add(tag.getPermId().getPermId());
        }
        assertEquals(tagIds, new HashSet<String>(Arrays.asList("/test/TEST_METAPROJECTS", "/test/ANOTHER_TEST_METAPROJECTS")));

        Sample sampleWithSpace2 = samples.get(1);
        assertEquals(sampleWithSpace2.getCode(), "SAMPLE_WITH_SPACE2");
        assertEquals(sampleWithSpace2.getType().getCode(), "CELL_PLATE");
        assertEquals(sampleWithSpace2.getSpace().getPermId().getPermId(), "CISD");
        assertEquals(sampleWithSpace2.getIdentifier().getIdentifier(), "/CISD/MP002-1:SAMPLE_WITH_SPACE2");
        assertEquals(sampleWithSpace2.getRegistrator().getUserId(), TEST_USER);
        assertEquals(sampleWithSpace2.getContainer().getCode(), "MP002-1");
        List<String> parentsIds = new LinkedList<String>();
        for (Sample s : sampleWithSpace2.getParents())
        {
            parentsIds.add(s.getPermId().getPermId());
        }
        Collections.sort(parentsIds);
        assertEquals(parentsIds.toString(), "[200811050917877-331, 200902091219327-1025]");

        Sample sampleWithoutSpace = samples.get(2);
        assertEquals(sampleWithoutSpace.getCode(), "SAMPLE_WITHOUT_SPACE");
        assertEquals(sampleWithoutSpace.getType().getCode(), "CELL_PLATE");
        assertEquals(sampleWithoutSpace.getSpace(), null);
        assertEquals(sampleWithoutSpace.getIdentifier().getIdentifier(), "/SAMPLE_WITHOUT_SPACE");
        assertEquals(sampleWithoutSpace.getRegistrator().getUserId(), TEST_USER);

        SampleFetchOptions onlyParentsAndChildren = new SampleFetchOptions();
        onlyParentsAndChildren.withParents();
        onlyParentsAndChildren.withChildren();

        map = v3api.getSamples(sessionToken, sample2Parents, onlyParentsAndChildren);
        samples = new ArrayList<Sample>(map.values());

        for (Sample sample : samples)
        {
            AssertionUtil.assertCollectionContainsString(sample.getParents(), sampleWithoutSpace.getPermId().getPermId());
            AssertionUtil.assertCollectionContainsString(sample.getChildren(), sampleWithSpace2.getPermId().getPermId());
        }
    }

    @Test
    public void testCreateWithExceptionContextAndFewerSamplesThanBatchSize()
    {
        int sampleCount = 5;
        int incorrectSampleIndex = 1;

        Assert.assertTrue(sampleCount < Batch.DEFAULT_BATCH_SIZE);
        Assert.assertTrue(incorrectSampleIndex < sampleCount);
        Assert.assertTrue(incorrectSampleIndex > 0);

        testWithExceptionContext(sampleCount, incorrectSampleIndex);
    }

    @Test
    public void testCreateWithExceptionContextAndMoreSamplesThanBatchSize()
    {
        int sampleCount = Batch.DEFAULT_BATCH_SIZE + 5;
        int incorrectSampleIndex = Batch.DEFAULT_BATCH_SIZE + 1;

        Assert.assertTrue(sampleCount > Batch.DEFAULT_BATCH_SIZE);
        Assert.assertTrue(incorrectSampleIndex < sampleCount);
        Assert.assertTrue(incorrectSampleIndex > Batch.DEFAULT_BATCH_SIZE);

        testWithExceptionContext(sampleCount, incorrectSampleIndex);
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testCreateWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String sessionToken = v3api.login(user.getUserId(), PASSWORD);

        SampleCreation creation = new SampleCreation();
        creation.setCode("PA_TEST");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("TEST-SPACE"));
        creation.setExperimentId(new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));

        if (user.isDisabledProjectUser())
        {
            assertAuthorizationFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Collections.singletonList(creation));
                }
            });
        } else if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            List<SamplePermId> permIds = v3api.createSamples(sessionToken, Collections.singletonList(creation));
            assertEquals(permIds.size(), 1);
        } else
        {
            assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Collections.singletonList(creation));
                }
            }, creation.getExperimentId());
        }
    }

    @Test
    public void testCreateWithProjectAndSpace()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation creation = new SampleCreation();
        creation.setCode("TEST_SAMPLE");
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setProjectId(new ProjectIdentifier("/CISD/DEFAULT"));
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));

        // When
        if (SamplePE.projectSamplesEnabled)
        {
            SamplePermId id = v3api.createSamples(sessionToken, Collections.singletonList(creation)).get(0);

            // Then
            SampleFetchOptions fo = new SampleFetchOptions();
            fo.withSpace();
            fo.withProject();
            fo.withExperiment();

            Sample sample = v3api.getSamples(sessionToken, Collections.singletonList(id), fo).get(id);

            assertEquals(sample.getSpace().getCode(), "CISD");
            assertEquals(sample.getProject().getIdentifier().getIdentifier(), "/CISD/DEFAULT");
            assertNull(sample.getExperiment());
        } else
        {
            assertUserFailureException(() -> v3api.createSamples(sessionToken, Collections.singletonList(creation)),
                    "Can not assign sample /CISD/TEST_SAMPLE to project /CISD/DEFAULT because project samples are not enabled.");
        }
    }

    @Test
    public void testCreateWithProjectAndSpaceInconsistent()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation creation = new SampleCreation();
        creation.setCode("TEST_SAMPLE");
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setProjectId(new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT"));
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));

        // When
        if (SamplePE.projectSamplesEnabled)
        {
            assertUserFailureException(() -> v3api.createSamples(sessionToken, Collections.singletonList(creation)),
                    "Sample space must be the same as project space");
        } else
        {
            assertUserFailureException(() -> v3api.createSamples(sessionToken, Collections.singletonList(creation)),
                    "Can not assign sample /CISD/TEST_SAMPLE to project /TEST-SPACE/TEST-PROJECT because project samples are not enabled.");
        }
    }

    @Test
    public void testCreateWithProjectAndSpaceAndExperiment()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation creation = new SampleCreation();
        creation.setCode("TEST_SAMPLE");
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setProjectId(new ProjectIdentifier("/CISD/DEFAULT"));
        creation.setExperimentId(new ExperimentIdentifier("/CISD/DEFAULT/EXP-REUSE"));
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));

        // When
        SamplePermId id = v3api.createSamples(sessionToken, Collections.singletonList(creation)).get(0);

        // Then
        SampleFetchOptions fo = new SampleFetchOptions();
        fo.withSpace();
        fo.withProject();
        fo.withExperiment();

        Sample sample = v3api.getSamples(sessionToken, Collections.singletonList(id), fo).get(id);

        assertEquals(sample.getSpace().getCode(), "CISD");
        if (SamplePE.projectSamplesEnabled)
        {
            assertEquals(sample.getProject().getIdentifier().getIdentifier(), "/CISD/DEFAULT");
        } else
        {
            assertNull(sample.getProject());
        }
        assertEquals(sample.getExperiment().getIdentifier().getIdentifier(), "/CISD/DEFAULT/EXP-REUSE");
    }

    @Test
    public void testCreateWithProjectNullAndSpaceAndExperiment()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation creation = new SampleCreation();
        creation.setCode("TEST_SAMPLE");
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setProjectId(null);
        creation.setExperimentId(new ExperimentIdentifier("/CISD/DEFAULT/EXP-REUSE"));
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));

        // When
        SamplePermId id = v3api.createSamples(sessionToken, Collections.singletonList(creation)).get(0);

        // Then
        SampleFetchOptions fo = new SampleFetchOptions();
        fo.withSpace();
        fo.withProject();
        fo.withExperiment();

        Sample sample = v3api.getSamples(sessionToken, Collections.singletonList(id), fo).get(id);

        assertEquals(sample.getSpace().getCode(), "CISD");
        if (SamplePE.projectSamplesEnabled)
        {
            assertEquals(sample.getProject().getIdentifier().getIdentifier(), "/CISD/DEFAULT");
        } else
        {
            assertNull(sample.getProject());
        }
        assertEquals(sample.getExperiment().getIdentifier().getIdentifier(), "/CISD/DEFAULT/EXP-REUSE");
    }

    @Test
    public void testCreateWithProjectAndSpaceAndExperimentInconsistent()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation creation = new SampleCreation();
        creation.setCode("TEST_SAMPLE");
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setExperimentId(new ExperimentIdentifier("/CISD/DEFAULT/EXP-REUSE"));
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));

        // When
        if (SamplePE.projectSamplesEnabled)
        {
            assertUserFailureException(() -> v3api.createSamples(sessionToken, Collections.singletonList(creation)),
                    "Sample project must be the same as experiment project");
        } else
        {
            assertUserFailureException(() -> v3api.createSamples(sessionToken, Collections.singletonList(creation)),
                    "Can not assign sample /CISD/TEST_SAMPLE to project /CISD/NEMO because project samples are not enabled.");
        }
    }

    @Test(dataProvider = USER_ROLES_PROVIDER)
    public void testCreateWithDifferentRolesInstanceSample(RoleWithHierarchy role)
    {
        testWithUserRole(role, params ->
        {
            final SampleCreation sampleCreation = new SampleCreation();
            sampleCreation.setCode("TEST_INSTANCE_SAMPLE_" + UUID.randomUUID());
            sampleCreation.setTypeId(new EntityTypePermId("CELL_PLATE"));

            if (RoleWithHierarchy.INSTANCE_ADMIN.equals(role))
            {
                v3api.createSamples(params.userSessionToken, Collections.singletonList(sampleCreation));
            } else
            {
                assertAnyAuthorizationException(() -> v3api.createSamples(params.userSessionToken, Collections.singletonList(sampleCreation)));
            }
        });
    }

    @Test(dataProvider = USER_ROLES_PROVIDER)
    public void testCreateWithDifferentRolesSpaceSample(RoleWithHierarchy role)
    {
        testWithUserRole(role, params ->
        {
            final SampleCreation sampleCreation = new SampleCreation();
            sampleCreation.setCode("TEST_SPACE_SAMPLE_" + UUID.randomUUID());
            sampleCreation.setTypeId(new EntityTypePermId("CELL_PLATE"));
            sampleCreation.setSpaceId(params.space1Id);

            if (List.of(RoleWithHierarchy.RoleLevel.INSTANCE, RoleWithHierarchy.RoleLevel.SPACE).contains(role.getRoleLevel()) && List.of(
                            RoleWithHierarchy.RoleCode.ADMIN, RoleWithHierarchy.RoleCode.POWER_USER, RoleWithHierarchy.RoleCode.USER)
                    .contains(role.getRoleCode()))
            {
                v3api.createSamples(params.userSessionToken, Collections.singletonList(sampleCreation));
            } else
            {
                assertAnyAuthorizationException(() -> v3api.createSamples(params.userSessionToken, Collections.singletonList(sampleCreation)));
            }
        });
    }

    @Test(dataProvider = USER_ROLES_PROVIDER)
    public void testCreateWithDifferentRolesProjectSample(RoleWithHierarchy role)
    {
        testWithUserRole(role, params ->
        {
            final SampleCreation sampleCreation = new SampleCreation();
            sampleCreation.setCode("TEST_PROJECT_SAMPLE_" + UUID.randomUUID());
            sampleCreation.setTypeId(new EntityTypePermId("CELL_PLATE"));
            sampleCreation.setSpaceId(params.space1Id);
            sampleCreation.setProjectId(params.space1Project1Id);

            if (List.of(RoleWithHierarchy.RoleCode.ADMIN, RoleWithHierarchy.RoleCode.POWER_USER, RoleWithHierarchy.RoleCode.USER)
                    .contains(role.getRoleCode()))
            {
                v3api.createSamples(params.userSessionToken, Collections.singletonList(sampleCreation));
            } else
            {
                assertAnyAuthorizationException(() -> v3api.createSamples(params.userSessionToken, Collections.singletonList(sampleCreation)));
            }
        });
    }

    @Test(dataProvider = USER_ROLES_PROVIDER)
    public void testCreateWithDifferentRolesExperimentSample(RoleWithHierarchy role)
    {
        testWithUserRole(role, params ->
        {
            final ExperimentCreation experimentCreation = new ExperimentCreation();
            experimentCreation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
            experimentCreation.setCode("TEST_EXPERIMENT_" + UUID.randomUUID());
            experimentCreation.setProjectId(params.space1Project1Id);
            experimentCreation.setProperty("DESCRIPTION", "test description");
            final ExperimentPermId experimentId = v3api.createExperiments(params.adminSessionToken, List.of(experimentCreation)).get(0);

            final SampleCreation sampleCreation = new SampleCreation();
            sampleCreation.setCode("TEST_EXPERIMENT_SAMPLE_" + UUID.randomUUID());
            sampleCreation.setTypeId(new EntityTypePermId("CELL_PLATE"));
            sampleCreation.setSpaceId(params.space1Id);
            sampleCreation.setExperimentId(experimentId);

            if (List.of(RoleWithHierarchy.RoleCode.ADMIN, RoleWithHierarchy.RoleCode.POWER_USER, RoleWithHierarchy.RoleCode.USER)
                    .contains(role.getRoleCode()))
            {
                v3api.createSamples(params.userSessionToken, Collections.singletonList(sampleCreation));
            } else
            {
                assertAnyAuthorizationException(() -> v3api.createSamples(params.userSessionToken, Collections.singletonList(sampleCreation)));
            }
        });
    }

    @Test(dataProvider = USER_ROLES_PROVIDER)
    public void testCreateWithDifferentRolesParentChildSample(RoleWithHierarchy role)
    {
        testWithUserRole(role, params ->
        {
            final SampleCreation parentCreation = new SampleCreation();
            parentCreation.setCreationId(new CreationId(UUID.randomUUID().toString()));
            parentCreation.setCode("TEST_PARENT_SAMPLE_" + UUID.randomUUID());
            parentCreation.setTypeId(new EntityTypePermId("CELL_PLATE"));
            parentCreation.setSpaceId(params.space1Id);

            final SampleCreation childCreation = new SampleCreation();
            childCreation.setCreationId(new CreationId(UUID.randomUUID().toString()));
            childCreation.setCode("TEST_CHILD_" + UUID.randomUUID());
            childCreation.setTypeId(new EntityTypePermId("CELL_PLATE"));
            childCreation.setSpaceId(params.space1Id);

            parentCreation.setChildIds(List.of(childCreation.getCreationId()));
            childCreation.setParentIds(List.of(parentCreation.getCreationId()));

            if (List.of(RoleWithHierarchy.RoleLevel.INSTANCE, RoleWithHierarchy.RoleLevel.SPACE).contains(role.getRoleLevel()) && List.of(
                            RoleWithHierarchy.RoleCode.ADMIN, RoleWithHierarchy.RoleCode.POWER_USER, RoleWithHierarchy.RoleCode.USER)
                    .contains(role.getRoleCode()))
            {
                v3api.createSamples(params.userSessionToken, List.of(parentCreation, childCreation));
            } else
            {
                assertAnyAuthorizationException(() -> v3api.createSamples(params.userSessionToken, Collections.singletonList(parentCreation)));
            }
        });
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation creation = new SampleCreation();
        creation.setCode("LOG_TEST_1");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));

        SampleCreation creation2 = new SampleCreation();
        creation2.setCode("LOG_TEST_2");
        creation2.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation2.setSpaceId(new SpacePermId("CISD"));

        SampleCreation creation3 = new SampleCreation();
        creation3.setCode("LOG_TEST_3");
        creation3.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation3.setSpaceId(new SpacePermId("CISD"));
        creation3.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP1"));

        v3api.createSamples(sessionToken, Arrays.asList(creation, creation2, creation3));

        assertAccessLog(
                "create-samples  NEW_SAMPLES('[SampleCreation[spaceId=<null>,projectId=<null>,experimentId=<null>,code=LOG_TEST_1], SampleCreation[spaceId=CISD,projectId=<null>,experimentId=<null>,code=LOG_TEST_2], SampleCreation[spaceId=CISD,projectId=<null>,experimentId=/CISD/NEMO/EXP1,code=LOG_TEST_3]]')");
    }

    private void testWithExceptionContext(int sampleCount, int incorrectSampleIndex)
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final List<SampleCreation> samples = new ArrayList<SampleCreation>();
        for (int i = 0; i < sampleCount; i++)
        {
            samples.add(sampleCreation("TEST_SAMPLE_" + (i + 1)));
        }

        samples.get(incorrectSampleIndex).setTypeId(null);

        assertUserFailureException(new IDelegatedAction()
                                   {
                                       @Override
                                       public void execute()
                                       {
                                           v3api.createSamples(sessionToken, samples);
                                       }
                                   }, "Type id cannot be null",
                patternContains("checking data (" + (incorrectSampleIndex + 1) + "/" + sampleCount + ")",
                        toDblQuotes("'code' : 'TEST_SAMPLE_" + (incorrectSampleIndex + 1) + "'")));
    }

    private SampleCreation sampleCreation(String code)
    {
        SampleCreation creation = new SampleCreation();
        creation.setCode(code);
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setCreationId(new CreationId("creation " + code));
        return creation;
    }

    private SampleCreation sampleCreation(String spaceCode, String code)
    {
        SampleCreation creation = new SampleCreation();
        creation.setCode(code);
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId(spaceCode));
        creation.setCreationId(new CreationId("creation " + code));
        return creation;
    }

    private SamplePermId createCisdSample(String code)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleCreation creation = sampleCreation("CISD", code);
        List<SamplePermId> ids = v3api.createSamples(sessionToken, Collections.singletonList(creation));
        v3api.logout(sessionToken);
        return ids.get(0);
    }

    @DataProvider(name = "tf-ft-tt")
    Object[][] getDataProviderForRelationTest()
    {
        return new Object[][] {
                new Object[] { true, false },
                new Object[] { false, true },
                new Object[] { true, true },
        };
    }

}

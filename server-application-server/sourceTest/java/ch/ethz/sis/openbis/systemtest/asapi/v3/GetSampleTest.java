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
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.Attachment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.CreationId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.HistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.PropertyHistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.RelationHistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.MaterialPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.create.PersonCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.IPersonId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create.ProjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.Role;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.create.RoleAssignmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.history.SampleRelationType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.Tag;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.fetchoptions.TagFetchOptions;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.systemtest.authorization.ProjectAuthorizationUser;
import junit.framework.Assert;

/**
 * @author pkupczyk
 */
public class GetSampleTest extends AbstractSampleTest
{
    @Test
    public void testGetMoreThanThousandSamples()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        List<SampleCreation> newSamples = new ArrayList<>();
        for (int i = 0; i < 1024; i++) // size should be larger than BatchOperationExecutor.DEFAULT_BATCH_SIZE
        {
            SampleCreation creation = new SampleCreation();
            creation.setCode("TEST_SAMPLE_" + (i + 1));
            creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
            creation.setSpaceId(new SpacePermId("TEST-SPACE"));
            newSamples.add(creation);
        }
        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken, newSamples);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withType();
        fetchOptions.withSpace();

        // When
        Map<ISampleId, Sample> samples = v3api.getSamples(sessionToken, sampleIds, fetchOptions);

        // Then
        assertEquals(samples.size(), newSamples.size());
    }

    @Test
    public void testGetByPermId()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SamplePermId permId1 = new SamplePermId("200902091219327-1025");
        SamplePermId permId2 = new SamplePermId("201206191219327-1055");

        Map<ISampleId, Sample> map =
                v3api.getSamples(sessionToken, Arrays.asList(permId1, permId2),
                        new SampleFetchOptions());

        assertEquals(2, map.size());

        Iterator<Sample> iter = map.values().iterator();
        assertEquals(iter.next().getPermId(), permId1);
        assertEquals(iter.next().getPermId(), permId2);

        assertEquals(map.get(permId1).getPermId(), permId1);
        assertEquals(map.get(permId2).getPermId(), permId2);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetByIdentifier()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleIdentifier identifier1 = new SampleIdentifier("/CISD/NEMO/CP-TEST-1");
        SampleIdentifier identifier2 = new SampleIdentifier("/TEST-SPACE/NOE/CP-TEST-4");
        SampleIdentifier identifier3 = new SampleIdentifier("/CISD/3VCP8");
        SampleIdentifier identifier4 = new SampleIdentifier("/MP");
        SampleIdentifier identifier5 = new SampleIdentifier("/MP:a03");
        SampleIdentifier identifier6 = new SampleIdentifier("/cisd/cl1:A03");
        SampleIdentifier identifier7 = new SampleIdentifier("//CL1:A01");

        List<SampleIdentifier> identifiers = Arrays.asList(identifier1, identifier2, identifier3, identifier4,
                identifier5, identifier6, identifier7);
        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, identifiers, new SampleFetchOptions());

        assertEquals(map.size(), identifiers.size());

        Iterator<Sample> iter = map.values().iterator();
        for (SampleIdentifier identifier : identifiers)
        {
            assertEquals(iter.next().getIdentifier(), normalize(identifier));
        }
        for (SampleIdentifier identifier : identifiers)
        {
            assertEquals(map.get(identifier).getIdentifier(), normalize(identifier));
        }

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetBySharedComponentIdentifier()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleIdentifier identifier1 = new SampleIdentifier("/MP:A03");

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, Arrays.asList(identifier1), new SampleFetchOptions());

        assertEquals(map.size(), 1);

        Sample sample1 = map.get(identifier1);

        assertEquals(sample1.getIdentifier().getIdentifier(), "/MP:A03");

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetBySharedComponentIdentifierWithOmittedContainerCodeAndUniqueComponentCode()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleIdentifier identifier1 = new SampleIdentifier("/A03");

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, Arrays.asList(identifier1), new SampleFetchOptions());

        assertEquals(map.size(), 0);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetBySharedComponentIdentifierWithOmittedContainerCodeAndNotUniqueComponentCode()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation sharedContainer = new SampleCreation();
        sharedContainer.setCode("MP2");
        sharedContainer.setTypeId(new EntityTypePermId("MASTER_PLATE"));
        sharedContainer.setProperty("$PLATE_GEOMETRY", "384_WELLS_16X24");

        // creating shared sample /MP2:A03 (/MP:A03 already exists)
        SampleCreation sharedComponent = new SampleCreation();
        sharedComponent.setCode("A03");
        sharedComponent.setTypeId(new EntityTypePermId("WELL"));
        sharedComponent.setContainerId(new SampleIdentifier("/MP2"));

        v3api.createSamples(sessionToken, Arrays.asList(sharedContainer, sharedComponent));

        // there is more than one shared sample with A03 code (/MP:A03 and /MP2:A03)
        // therefore nothing should be found if we don't specify the container code
        SampleIdentifier identifier1 = new SampleIdentifier("/A03");

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, Arrays.asList(identifier1), new SampleFetchOptions());
        assertEquals(map.size(), 0);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetBySpaceComponentIdentifier()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleIdentifier identifier1 = new SampleIdentifier("/CISD/DEFAULT/PLATE_WELLSEARCH:WELL-A01");
        SampleIdentifier identifier2 = new SampleIdentifier("/CISD/DEFAULT/PLATE_WELLSEARCH:WELL-A02");

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, Arrays.asList(identifier1, identifier2), new SampleFetchOptions());

        assertEquals(map.size(), 2);

        Sample sample1 = map.get(identifier1);
        Sample sample2 = map.get(identifier2);

        assertEquals(sample1.getIdentifier().getIdentifier(), "/CISD/DEFAULT/PLATE_WELLSEARCH:WELL-A01");
        assertEquals(sample2.getIdentifier().getIdentifier(), "/CISD/DEFAULT/PLATE_WELLSEARCH:WELL-A02");

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetBySpaceComponentIdentifierWithOmittedContainerCodeAndUniqueComponentCode()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleIdentifier identifier1 = new SampleIdentifier("/CISD/WELL-A01");
        SampleIdentifier identifier2 = new SampleIdentifier("/CISD/WELL-A02");

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, Arrays.asList(identifier1, identifier2), new SampleFetchOptions());

        assertEquals(map.size(), 0);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetBySpaceComponentIdentifierWithOmittedContainerCodeAndNotUniqueComponentCode()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleIdentifier identifier1 = new SampleIdentifier("/CISD/A01");
        SampleIdentifier identifier2 = new SampleIdentifier("/CISD/A02");

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, Arrays.asList(identifier1, identifier2), new SampleFetchOptions());

        assertEquals(map.size(), 0);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetBySpaceComponentIdentifierWithOmittedContainerCodeAndUniqueAndNotUniqueComponentCodes()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleIdentifier identifier1 = new SampleIdentifier("/CISD/WELL-A01");
        SampleIdentifier identifier2 = new SampleIdentifier("/CISD/A02");

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, Arrays.asList(identifier1, identifier2), new SampleFetchOptions());

        assertEquals(map.size(), 0);

        v3api.logout(sessionToken);
    }

    private SampleIdentifier normalize(SampleIdentifier identifier)
    {
        ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier identifier2 =
                SampleIdentifierFactory.parse(identifier.getIdentifier());
        String spaceCode = null;
        if (identifier2.isSpaceLevel())
        {
            if (identifier2.isInsideHomeSpace())
            {
                spaceCode = "CISD";
            } else
            {
                spaceCode = CodeConverter.tryToDatabase(identifier2.getSpaceLevel().getSpaceCode());
            }
        }
        String projectCode = null;
        if (identifier2.isProjectLevel())
        {
            projectCode = identifier2.getProjectLevel().getProjectCode();
            spaceCode = identifier2.getProjectLevel().getSpaceCode();
        }
        String sampleSubCode = CodeConverter.tryToDatabase(identifier2.getSampleSubCode());
        String containerCode = CodeConverter.tryToDatabase(identifier2.tryGetContainerCode());
        return new SampleIdentifier(spaceCode, projectCode, containerCode, sampleSubCode);
    }

    @Test
    public void testGetByIdentifierCaseInsensitive()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleIdentifier identifier1 = new SampleIdentifier("/CISD/NEMO/CP-TEST-1");
        SampleIdentifier identifier2 = new SampleIdentifier("/Mp");
        SampleIdentifier identifier3 = new SampleIdentifier("/mP:a03");
        SampleIdentifier identifier4 = new SampleIdentifier("/cisd/cl1:A03");
        SampleIdentifier identifier5 = new SampleIdentifier("//cL1:a01");

        List<SampleIdentifier> identifiers = Arrays.asList(identifier1, identifier2, identifier3, identifier4, identifier5);
        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, identifiers, new SampleFetchOptions());

        assertEquals(map.size(), identifiers.size());

        Iterator<Sample> iter = map.values().iterator();
        assertEquals(iter.next().getIdentifier(), identifier1);
        assertEquals(iter.next().getIdentifier(), identifier2);
        assertEquals(iter.next().getIdentifier(), identifier3);
        assertEquals(iter.next().getIdentifier(), identifier4);
        assertEquals(iter.next().getIdentifier(), new SampleIdentifier("/CISD/CL1:A01"));

        assertEquals(map.get(identifier1).getIdentifier().getIdentifier(), "/CISD/NEMO/CP-TEST-1");
        assertEquals(map.get(new SampleIdentifier("/CISD/NEMO/CP-TEST-1")).getIdentifier().getIdentifier(), "/CISD/NEMO/CP-TEST-1");

        assertEquals(map.get(identifier2).getIdentifier().getIdentifier(), "/MP");
        assertEquals(map.get(new SampleIdentifier("/MP")).getIdentifier().getIdentifier(), "/MP");

        assertEquals(map.get(identifier3).getIdentifier().getIdentifier(), "/MP:A03");
        assertEquals(map.get(new SampleIdentifier("/MP:A03")).getIdentifier().getIdentifier(), "/MP:A03");

        assertEquals(map.get(identifier4).getIdentifier().getIdentifier(), "/CISD/CL1:A03");
        assertEquals(map.get(new SampleIdentifier("/CISD/CL1:A03")).getIdentifier().getIdentifier(), "/CISD/CL1:A03");

        assertEquals(map.get(identifier5).getIdentifier().getIdentifier(), "/CISD/CL1:A01");
        assertEquals(map.get(new SampleIdentifier("//CL1:A01")).getIdentifier().getIdentifier(), "/CISD/CL1:A01");

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetByIdsNonexistent()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleIdentifier identifier1 = new SampleIdentifier("/CISD/NEMO/CP-TEST-1");
        SampleIdentifier identifier2 = new SampleIdentifier("/TEST-SPACE/NOE/CP-TEST-4");
        SampleIdentifier identifier3 = new SampleIdentifier("/NONEXISTENT_SPACE/CP-TEST-1");
        SamplePermId permId1 = new SamplePermId("200902091250077-1026");
        SamplePermId permId2 = new SamplePermId("NONEXISTENT_SAMPLE");
        SampleIdentifier identifier4 = new SampleIdentifier("/CISD/NONEXISTENT_SAMPLE");
        SampleIdentifier identifier5 = new SampleIdentifier("/CISD/3VCP8");
        SamplePermId permId3 = new SamplePermId("200902091225616-1027");

        Map<ISampleId, Sample> map =
                v3api.getSamples(sessionToken,
                        Arrays.asList(identifier1, identifier2, identifier3, permId1, permId2, identifier4, identifier5, permId3),
                        new SampleFetchOptions());

        assertEquals(5, map.size());

        Iterator<Sample> iter = map.values().iterator();
        assertEquals(iter.next().getIdentifier(), identifier1);
        assertEquals(iter.next().getIdentifier(), identifier2);
        assertEquals(iter.next().getPermId(), permId1);
        assertEquals(iter.next().getIdentifier(), identifier5);
        assertEquals(iter.next().getPermId(), permId3);

        assertEquals(map.get(identifier1).getIdentifier(), identifier1);
        assertEquals(map.get(identifier2).getIdentifier(), identifier2);
        assertEquals(map.get(permId1).getPermId(), permId1);
        assertEquals(map.get(identifier5).getIdentifier(), identifier5);
        assertEquals(map.get(permId3).getPermId(), permId3);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetByIdsDifferent()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleIdentifier identifier1 = new SampleIdentifier("/CISD/NEMO/CP-TEST-1");
        SamplePermId permId = new SamplePermId("200902091250077-1026");
        SampleIdentifier identifier2 = new SampleIdentifier("/TEST-SPACE/NOE/CP-TEST-4");

        Map<ISampleId, Sample> map =
                v3api.getSamples(sessionToken, Arrays.asList(identifier1, permId, identifier2), new SampleFetchOptions());

        assertEquals(3, map.size());

        Iterator<Sample> iter = map.values().iterator();
        assertEquals(iter.next().getIdentifier(), identifier1);
        assertEquals(iter.next().getPermId(), permId);
        assertEquals(iter.next().getIdentifier(), identifier2);

        assertEquals(map.get(identifier1).getIdentifier(), identifier1);
        assertEquals(map.get(permId).getPermId(), permId);
        assertEquals(map.get(identifier2).getIdentifier(), identifier2);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetByIdsDuplicated()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        // "/CISD/NEMO/CP-TEST-1" and "200902091219327-1025" is the same sample
        SampleIdentifier identifier1 = new SampleIdentifier("/CISD/NEMO/CP-TEST-1");
        SamplePermId permId1 = new SamplePermId("200902091219327-1025");
        SampleIdentifier identifier2 = new SampleIdentifier("/TEST-SPACE/NOE/CP-TEST-4");
        SamplePermId permId2 = new SamplePermId("200902091219327-1025");

        Map<ISampleId, Sample> map =
                v3api.getSamples(sessionToken, Arrays.asList(identifier1, permId1, identifier2, permId2), new SampleFetchOptions());

        assertEquals(3, map.size());

        Iterator<Sample> iter = map.values().iterator();
        assertEquals(iter.next().getIdentifier(), identifier1);
        assertEquals(iter.next().getPermId(), permId1);
        assertEquals(iter.next().getIdentifier(), identifier2);

        assertEquals(map.get(identifier1).getIdentifier(), identifier1);
        assertEquals(map.get(permId1).getPermId(), permId1);
        assertEquals(map.get(identifier2).getIdentifier(), identifier2);

        assertTrue(map.get(identifier1) == map.get(permId1));

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetByIdsUnauthorized()
    {
        SampleIdentifier identifier1 = new SampleIdentifier("/CISD/NEMO/CP-TEST-1");
        SampleIdentifier identifier2 = new SampleIdentifier("/TEST-SPACE/NOE/CP-TEST-4");
        SampleIdentifier identifier3 = new SampleIdentifier("/CISD/NOE/CP-TEST-2");
        SampleIdentifier identifier4 = new SampleIdentifier("/TEST-SPACE/TEST-PROJECT/EV-TEST");

        List<? extends ISampleId> ids = Arrays.asList(identifier1, identifier2, identifier3, identifier4);

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, ids, new SampleFetchOptions());

        assertEquals(map.size(), 4);
        v3api.logout(sessionToken);

        sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);
        map = v3api.getSamples(sessionToken, ids, new SampleFetchOptions());

        assertEquals(map.size(), 2);

        Iterator<Sample> iter = map.values().iterator();
        assertEquals(iter.next().getIdentifier(), identifier2);
        assertEquals(iter.next().getIdentifier(), identifier4);

        assertEquals(map.get(identifier2).getIdentifier(), identifier2);
        assertEquals(map.get(identifier4).getIdentifier(), identifier4);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithFetchOptionsEmpty()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        Map<ISampleId, Sample> map =
                v3api.getSamples(sessionToken, Collections.singletonList(new SamplePermId("200902091219327-1025")),
                        new SampleFetchOptions());
        List<Sample> samples = new ArrayList<Sample>(map.values());

        assertEquals(1, samples.size());

        Sample sample = samples.get(0);
        assertEquals(sample.getPermId().toString(), "200902091219327-1025");
        assertEquals(sample.getCode(), "CP-TEST-1");
        assertEquals(sample.getIdentifier().toString(), "/CISD/NEMO/CP-TEST-1");
        assertEqualsDate(sample.getRegistrationDate(), "2009-02-09 12:09:19");
        assertNotNull(sample.getModificationDate());

        assertExperimentNotFetched(sample);
        assertPropertiesNotFetched(sample);
        assertParentsNotFetched(sample);
        assertChildrenNotFetched(sample);
        assertComponentsNotFetched(sample);
        assertContainerNotFetched(sample);
        assertModifierNotFetched(sample);
        assertRegistratorNotFetched(sample);
        assertTagsNotFetched(sample);
        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithFetchOptionsNested()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withType().withValidationPlugin();

        SampleFetchOptions nestedFetchOptions = fetchOptions.withComponents().withContainer();
        nestedFetchOptions.withExperiment();
        nestedFetchOptions.withType().withValidationPlugin();

        Map<ISampleId, Sample> map =
                v3api.getSamples(sessionToken, Collections.singletonList(new SamplePermId("200902091250077-1050")), fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        assertEquals(1, samples.size());

        Sample sample = samples.get(0);
        Sample nestedSample = sample.getComponents().get(0).getContainer();

        // sample and nested sample represent the same object in the database but are two different
        // objects in memory as they have been fetched with different fetch options

        assertEquals(sample.getCode(), "PLATE_WELLSEARCH");
        assertEquals(sample.getCode(), nestedSample.getCode());
        assertTrue(sample != nestedSample);

        assertEquals(sample.getProperties().size(), 0);
        assertPropertiesNotFetched(nestedSample);

        assertExperimentNotFetched(sample);
        Experiment nestedExperiment = nestedSample.getExperiment();
        assertEquals(nestedExperiment.getIdentifier().toString(), "/CISD/DEFAULT/EXP-WELLS");

        // sample type and nested sample type represent the same object in the database and are the same
        // object in memory as they have both been fetched with equal fetch options

        SampleType type = sample.getType();
        SampleType nestedType = nestedSample.getType();
        assertEquals(type.getCode(), "CELL_PLATE");
        assertTrue(type == nestedType);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithRegistratorAndModifier()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation newSample = new SampleCreation();
        newSample.setCode("SAMPLE_WITH_MODIFIER");
        newSample.setTypeId(new EntityTypePermId("CELL_PLATE"));
        newSample.setSpaceId(new SpacePermId("CISD"));

        List<SamplePermId> newSamplePermIds = v3api.createSamples(sessionToken, Collections.singletonList(newSample));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        PersonFetchOptions personFetchOptions = new PersonFetchOptions();
        fetchOptions.withModifierUsing(personFetchOptions);
        fetchOptions.withRegistratorUsing(personFetchOptions);

        Map<ISampleId, Sample> map =
                v3api.getSamples(sessionToken, newSamplePermIds,
                        fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        assertEquals(1, samples.size());

        Sample sample = samples.get(0);

        assertEquals(sample.getRegistrator().getUserId(), "test");
        assertEquals(sample.getModifier().getUserId(), "test");

        assertTrue(sample.getRegistrator() == sample.getModifier());

        assertExperimentNotFetched(sample);
        assertPropertiesNotFetched(sample);
        assertParentsNotFetched(sample);
        assertChildrenNotFetched(sample);
        assertComponentsNotFetched(sample);
        assertContainerNotFetched(sample);

        assertTagsNotFetched(sample);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithModifierReused()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withModifier();

        SamplePermId permId1 = new SamplePermId("200811050919915-8");
        SamplePermId permId2 = new SamplePermId("200902091219327-1025");

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, Arrays.asList(permId1, permId2), fetchOptions);

        assertEquals(2, map.size());
        Sample sample1 = map.get(permId1);
        Sample sample2 = map.get(permId2);

        assertFalse(sample1 == sample2);
        assertEquals(sample1.getModifier().getUserId(), "test_role");
        assertEquals(sample2.getModifier().getUserId(), "test_role");
        assertTrue(sample1.getModifier() == sample2.getModifier());

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithTags()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        TagFetchOptions tagfe = fetchOptions.withTags();
        tagfe.withOwner();

        Map<ISampleId, Sample> map =
                v3api.getSamples(sessionToken, Collections.singletonList(new SamplePermId("201206191219327-1055")),
                        fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        assertEquals(1, samples.size());

        Sample sample = samples.get(0);

        Set<Tag> tags = sample.getTags();

        assertEquals(tags.size(), 1);

        for (Tag tag : tags)
        {
            assertEquals(TEST_USER, tag.getOwner().getUserId());
        }

        assertExperimentNotFetched(sample);
        assertPropertiesNotFetched(sample);
        assertParentsNotFetched(sample);
        assertChildrenNotFetched(sample);
        assertComponentsNotFetched(sample);
        assertContainerNotFetched(sample);
        assertRegistratorNotFetched(sample);
        assertModifierNotFetched(sample);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithTagsReused()
    {
        String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withTags();

        SamplePermId permId1 = new SamplePermId("201206191219327-1054");
        SamplePermId permId2 = new SamplePermId("201206191219327-1055");

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, Arrays.asList(permId1, permId2), fetchOptions);

        assertEquals(map.size(), 2);
        Sample sample1 = map.get(permId1);
        Sample sample2 = map.get(permId2);

        assertFalse(sample1 == sample2);
        assertEquals(sample1.getTags().size(), 1);
        assertEquals(sample2.getTags().size(), 1);
        assertContainSameObjects(sample1.getTags(), sample2.getTags(), 1);

        v3api.logout(sessionToken);
    }

    public void testGetWithSpace()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withSpace();

        Map<ISampleId, Sample> map =
                v3api.getSamples(sessionToken, Collections.singletonList(new SamplePermId("200902091219327-1025")), fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        Sample sample = samples.get(0);
        assertEquals(sample.getSpace().getCode(), "CISD");
        assertExperimentNotFetched(sample);
        assertPropertiesNotFetched(sample);
        assertParentsNotFetched(sample);
        assertChildrenNotFetched(sample);
        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithSpaceReused()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withSpace();

        SamplePermId permId1 = new SamplePermId("201206191219327-1054");
        SamplePermId permId2 = new SamplePermId("201206191219327-1055");

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, Arrays.asList(permId1, permId2), fetchOptions);

        assertEquals(2, map.size());
        Sample sample1 = map.get(permId1);
        Sample sample2 = map.get(permId2);

        assertFalse(sample1 == sample2);
        assertEquals(sample1.getSpace().getCode(), "TEST-SPACE");
        assertEquals(sample2.getSpace().getCode(), "TEST-SPACE");
        assertTrue(sample1.getSpace() == sample2.getSpace());

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithParentsAndProperties()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();

        // fetch parents and their properties
        fetchOptions.withParents().withProperties();

        Map<ISampleId, Sample> map =
                v3api.getSamples(sessionToken, Collections.singletonList(new SamplePermId("200811050946559-982")), fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        assertEquals(1, samples.size());

        Sample sample = samples.get(0);
        assertEquals(sample.getPermId().toString(), "200811050946559-982");
        assertEquals(sample.getCode(), "3VCP8");
        assertEquals(sample.getIdentifier().toString(), "/CISD/3VCP8");

        assertExperimentNotFetched(sample);
        assertChildrenNotFetched(sample);
        assertPropertiesNotFetched(sample);

        List<Sample> parents = sample.getParents();
        assertEquals(parents.size(), 1);

        Sample parent = parents.get(0);
        assertEquals(parent.getPermId().toString(), "200811050945092-976");
        assertEquals(parent.getCode(), "3V-125");
        assertEquals(parent.getProperties().size(), 1);
        assertEquals(parent.getProperties().get("OFFSET"), "49");
        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithParents()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setCode("LIST_SAMPLES__SAMPLE");
        sampleCreation.setSpaceId(new SpacePermId("CISD"));
        sampleCreation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        sampleCreation.setParentIds(Arrays.asList(new CreationId("parent_1"), new CreationId("parent_2")));

        SampleCreation parent1Creation = new SampleCreation();
        parent1Creation.setCreationId(new CreationId("parent_1"));
        parent1Creation.setCode("LIST_SAMPLES__PARENT_1");
        parent1Creation.setSpaceId(new SpacePermId("CISD"));
        parent1Creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        parent1Creation.setParentIds(Arrays.asList(new CreationId("grandparent_1"), new CreationId("grandparent_2")));

        SampleCreation parent2Creation = new SampleCreation();
        parent2Creation.setCreationId(new CreationId("parent_2"));
        parent2Creation.setCode("LIST_SAMPLES__PARENT_2");
        parent2Creation.setSpaceId(new SpacePermId("CISD"));
        parent2Creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        parent2Creation.setParentIds(Arrays.asList(new CreationId("grandparent_1"), new CreationId("grandparent_2")));

        SampleCreation grandparent1Creation = new SampleCreation();
        grandparent1Creation.setCreationId(new CreationId("grandparent_1"));
        grandparent1Creation.setCode("LIST_SAMPLES__GRANDPARENT_1");
        grandparent1Creation.setSpaceId(new SpacePermId("CISD"));
        grandparent1Creation.setTypeId(new EntityTypePermId("CELL_PLATE"));

        SampleCreation grandparent2Creation = new SampleCreation();
        grandparent2Creation.setCreationId(new CreationId("grandparent_2"));
        grandparent2Creation.setCode("LIST_SAMPLES__GRANDPARENT_2");
        grandparent2Creation.setSpaceId(new SpacePermId("CISD"));
        grandparent2Creation.setTypeId(new EntityTypePermId("CELL_PLATE"));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withParentsUsing(fetchOptions);

        List<SamplePermId> sampleIds =
                v3api.createSamples(sessionToken,
                        Arrays.asList(sampleCreation, parent1Creation, parent2Creation, grandparent1Creation, grandparent2Creation));
        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, sampleIds, fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        Sample sample = samples.get(0);
        Sample parent1 = samples.get(1);
        Sample parent2 = samples.get(2);
        Sample grandparent1 = samples.get(3);
        Sample grandparent2 = samples.get(4);

        assertEquals(sample.getCode(), sampleCreation.getCode());
        assertEquals(parent1.getCode(), parent1Creation.getCode());
        assertEquals(parent2.getCode(), parent2Creation.getCode());
        assertEquals(grandparent1.getCode(), grandparent1Creation.getCode());
        assertEquals(grandparent2.getCode(), grandparent2Creation.getCode());

        assertTrue(sample.getParents().get(0) == parent1);
        assertTrue(sample.getParents().get(1) == parent2);
        assertTrue(parent1.getParents().get(0) == grandparent1);
        assertTrue(parent1.getParents().get(1) == grandparent2);
        assertTrue(parent2.getParents().get(0) == grandparent1);
        assertTrue(parent2.getParents().get(1) == grandparent2);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithParentsReused()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withParents();

        SamplePermId permId1 = new SamplePermId("200811050946559-980");
        SamplePermId permId2 = new SamplePermId("200811050946559-982");

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, Arrays.asList(permId1, permId2), fetchOptions);

        assertEquals(2, map.size());
        Sample sample1 = map.get(permId1);
        Sample sample2 = map.get(permId2);

        assertFalse(sample1 == sample2);
        assertEquals(sample1.getParents().size(), 2);
        assertEquals(sample2.getParents().size(), 1);
        assertContainSameObjects(sample1.getParents(), sample2.getParents(), 1);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithChildren()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();

        fetchOptions.withChildren();

        Map<ISampleId, Sample> map =
                v3api.getSamples(sessionToken, Collections.singletonList(new SamplePermId("200811050929940-1019")), fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        assertEquals(1, samples.size());

        Sample sample = samples.get(0);
        assertEquals(sample.getPermId().toString(), "200811050929940-1019");
        assertEquals(sample.getCode(), "CP1-B1");

        assertExperimentNotFetched(sample);
        assertPropertiesNotFetched(sample);
        assertParentsNotFetched(sample);

        List<Sample> children = sample.getChildren();
        assertEquals(children.size(), 1);

        Sample child = children.get(0);
        assertEquals(child.getPermId().toString(), "200811050931564-1022");
        assertEquals(child.getCode(), "RP1-B1X");
        assertPropertiesNotFetched(child);
        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithChildrenReused()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withChildren();

        SamplePermId permId1 = new SamplePermId("200811050944030-975");
        SamplePermId permId2 = new SamplePermId("200811050945092-976");

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, Arrays.asList(permId1, permId2), fetchOptions);

        assertEquals(2, map.size());
        Sample sample1 = map.get(permId1);
        Sample sample2 = map.get(permId2);

        assertFalse(sample1 == sample2);
        assertEquals(sample1.getChildren().size(), 1);
        assertEquals(sample2.getChildren().size(), 4);
        assertContainSameObjects(sample1.getChildren(), sample2.getChildren(), 1);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithGrandParentAndGrandChildWithParentInbetween()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withChildren();
        fetchOptions.withChildrenUsing(fetchOptions);

        SamplePermId permId1 = new SamplePermId("200811050929035-1015"); // grand parent
        SamplePermId permId2 = new SamplePermId("200811050931564-1022"); // grand child of permId1

        // When
        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, Arrays.asList(permId1, permId2), fetchOptions);

        // Then
        assertEquals(2, map.size());
        Sample sample1 = map.get(permId1);
        Sample sample2 = map.get(permId2);
        assertEquals(sample1.getChildren().size(), 1);
        assertEquals(sample1.getChildren().get(0).getPermId().getPermId(), "200811050929940-1019"); // parent
        assertEquals(sample1.getChildrenRelationships().toString(),
                "{200811050929940-1019=Relationship[parent annotations={},child annotations={}]}");
        assertEquals(sample1.getChildren().get(0).getChildren().get(0).getPermId().getPermId(), "200811050931564-1022");
        assertEquals(sample1.getChildren().get(0).getChildrenRelationships().toString(),
                "{200811050931564-1022=Relationship[parent annotations={},child annotations={}]}");
        assertEquals(sample2.getChildren().size(), 0);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithComponentsAndContainer()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();

        fetchOptions.withComponents().withContainer();
        fetchOptions.withProperties();

        Map<ISampleId, Sample> map =
                v3api.getSamples(sessionToken, Collections.singletonList(new SamplePermId("200811050919915-8")), fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        assertEquals(1, samples.size());

        Sample sample = samples.get(0);
        assertEquals(sample.getPermId().toString(), "200811050919915-8");
        assertEquals(sample.getCode(), "CL1");

        assertEquals(sample.getProperties().size(), 2);

        assertExperimentNotFetched(sample);
        assertParentsNotFetched(sample);
        assertChildrenNotFetched(sample);

        List<Sample> components = sample.getComponents();
        assertEquals(components.size(), 2);

        for (Sample s : components)
        {
            assertExperimentNotFetched(s);
            assertPropertiesNotFetched(s);
            assertParentsNotFetched(s);
            assertChildrenNotFetched(s);
            assertEquals(s.getContainer().getPermId(), sample.getPermId());
        }
        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithContainerReused()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withContainer();

        SamplePermId permId1 = new SamplePermId("200811050919915-9");
        SamplePermId permId2 = new SamplePermId("200811050919915-10");

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, Arrays.asList(permId1, permId2), fetchOptions);

        assertEquals(2, map.size());
        Sample sample1 = map.get(permId1);
        Sample sample2 = map.get(permId2);

        assertFalse(sample1 == sample2);
        assertEquals(sample1.getContainer().getCode(), "CL1");
        assertEquals(sample2.getContainer().getCode(), "CL1");
        assertTrue(sample1.getContainer() == sample2.getContainer());

        v3api.logout(sessionToken);
    }

    /**
     * Test that translation can handle reference loops
     */
    @Test
    public void testGetWithContainerLoop()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();

        // fetch components, with the container and loop.
        fetchOptions.withComponents().withContainerUsing(fetchOptions);
        fetchOptions.withProperties();

        Map<ISampleId, Sample> map =
                v3api.getSamples(sessionToken, Collections.singletonList(new SamplePermId("200811050919915-8")), fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        assertEquals(1, samples.size());

        Sample sample = samples.get(0);
        assertEquals(sample.getPermId().toString(), "200811050919915-8");
        assertEquals(sample.getCode(), "CL1");

        assertEquals(sample.getProperties().size(), 2);

        assertExperimentNotFetched(sample);
        assertParentsNotFetched(sample);
        assertChildrenNotFetched(sample);

        List<Sample> components = sample.getComponents();
        assertEquals(components.size(), 2);

        Assert.assertTrue(sample.getComponents().get(0).getContainer() == sample);

        for (Sample s : components)
        {
            assertExperimentNotFetched(s);
            assertPropertiesNotFetched(s);
            assertParentsNotFetched(s);
            assertChildrenNotFetched(s);
            assertSpaceNotFetched(sample);
        }
        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithExperiment()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();

        fetchOptions.withExperiment();

        Map<ISampleId, Sample> map =
                v3api.getSamples(sessionToken, Arrays.asList(new SamplePermId("200811050946559-979"),
                        new SampleIdentifier("/CISD/DEFAULT/RP1-B1X"),
                        new SampleIdentifier("/CISD/DEFAULT/RP2-A1X")), fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        assertEquals(samples.size(), 3);

        Sample sample1 = samples.get(0);
        assertEquals(sample1.getIdentifier().toString(), "/CISD/NEMO/3VCP5");
        assertEquals(sample1.getExperiment().getIdentifier().toString(), "/CISD/NEMO/EXP10");

        Sample sample2 = samples.get(1);
        assertEquals(sample2.getIdentifier().toString(), "/CISD/DEFAULT/RP1-B1X");
        assertEquals(sample2.getExperiment().getIdentifier().toString(), "/CISD/DEFAULT/EXP-REUSE");

        Sample sample3 = samples.get(2);
        assertEquals(sample3.getIdentifier().toString(), "/CISD/DEFAULT/RP2-A1X");
        assertEquals(sample3.getExperiment().getIdentifier().toString(), "/CISD/DEFAULT/EXP-REUSE");

        assertTrue(sample2.getExperiment() == sample3.getExperiment());

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithExperimentReused()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withExperiment();

        SamplePermId permId1 = new SamplePermId("201206191219327-1054");
        SamplePermId permId2 = new SamplePermId("201206191219327-1055");

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, Arrays.asList(permId1, permId2), fetchOptions);

        assertEquals(2, map.size());

        Sample sample1 = map.get(permId1);
        Sample sample2 = map.get(permId2);

        assertFalse(sample1 == sample2);
        assertEquals(sample1.getExperiment().getCode(), "EXP-SPACE-TEST");
        assertEquals(sample2.getExperiment().getCode(), "EXP-SPACE-TEST");
        assertTrue(sample1.getExperiment() == sample2.getExperiment());

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithType()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();

        fetchOptions.withType();

        List<SamplePermId> sampleIds = Collections.singletonList(new SamplePermId("200811050946559-979"));
        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, sampleIds, fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        assertEquals(samples.size(), 1);

        Sample sample = samples.get(0);
        assertEquals(sample.getIdentifier().toString(), "/CISD/NEMO/3VCP5");

        SampleType type = sample.getType();
        assertEquals(type.getCode(), "CELL_PLATE");
        assertEquals(type.getPermId().getPermId(), "CELL_PLATE");
        assertEquals(type.getDescription(), "Cell Plate");
        assertTrue(type.isListable());
        assertFalse(type.isAutoGeneratedCode());
        assertFalse(type.isShowParentMetadata());
        assertFalse(type.isSubcodeUnique());
        assertEquals(type.getGeneratedCodePrefix(), "S");
        assertEqualsDate(type.getModificationDate(), "2009-03-23 15:34:44");
        assertEquals(type.getFetchOptions().hasPropertyAssignments(), false);

        assertExperimentNotFetched(sample);
        assertPropertiesNotFetched(sample);
        assertParentsNotFetched(sample);
        assertChildrenNotFetched(sample);
        assertSpaceNotFetched(sample);
        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithTypeAndPropertyAssignments()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withType().withPropertyAssignments().sortBy().label().desc();

        List<SamplePermId> sampleIds = Collections.singletonList(new SamplePermId("200811050917877-331"));
        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, sampleIds, fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        assertEquals(samples.size(), 1);

        Sample sample = samples.get(0);
        assertEquals(sample.getIdentifier().toString(), "/CISD/MP002-1");

        SampleType type = sample.getType();
        assertEquals(type.getCode(), "MASTER_PLATE");
        assertEquals(type.getFetchOptions().hasPropertyAssignments(), true);
        List<PropertyAssignment> propertyAssignments = type.getPropertyAssignments();
        assertEquals(propertyAssignments.get(0).getPropertyType().getCode(), "$PLATE_GEOMETRY");
        assertEquals(propertyAssignments.get(0).getPropertyType().getLabel(), "Plate Geometry");
        assertEquals(propertyAssignments.get(0).getPropertyType().getDescription(), "Plate Geometry");
        assertEquals(propertyAssignments.get(0).getPropertyType().isManagedInternally(), Boolean.TRUE);
        assertEquals(propertyAssignments.get(0).getPropertyType().getDataType(), DataType.CONTROLLEDVOCABULARY);
        assertEquals(propertyAssignments.get(0).isMandatory(), Boolean.TRUE);
        assertEquals(propertyAssignments.get(1).getPropertyType().getCode(), "DESCRIPTION");
        assertEquals(propertyAssignments.get(1).getPropertyType().getLabel(), "Description");
        assertEquals(propertyAssignments.get(1).getPropertyType().getDescription(), "A Description");
        assertEquals(propertyAssignments.get(1).getPropertyType().isManagedInternally(), Boolean.FALSE);
        assertEquals(propertyAssignments.get(1).getPropertyType().getDataType(), DataType.VARCHAR);
        assertEquals(propertyAssignments.get(1).isMandatory(), Boolean.FALSE);
        assertEquals(propertyAssignments.size(), 2);
        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithPropertyWhichIsManagedInternally()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();

        List<SamplePermId> sampleIds = Collections.singletonList(new SamplePermId("200811050919915-8"));
        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, sampleIds, fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        assertEquals(samples.size(), 1);

        Sample sample = samples.get(0);
        assertEquals(sample.getPermId().getPermId(), "200811050919915-8");

        Map<String, Serializable> properties = sample.getProperties();
        assertEquals(properties.get("$PLATE_GEOMETRY"), "384_WELLS_16X24");
        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithTypeReused()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withType();

        SamplePermId permId1 = new SamplePermId("200902091219327-1025");
        SamplePermId permId2 = new SamplePermId("200902091250077-1026");

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, Arrays.asList(permId1, permId2), fetchOptions);

        assertEquals(2, map.size());
        Sample sample1 = map.get(permId1);
        Sample sample2 = map.get(permId2);

        assertFalse(sample1 == sample2);
        assertEquals(sample1.getType().getCode(), "CELL_PLATE");
        assertEquals(sample2.getType().getCode(), "CELL_PLATE");
        assertTrue(sample1.getType() == sample2.getType());

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithDataSetAndItsTypeReused()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withDataSets().withType();

        SamplePermId permId1 = new SamplePermId("200902091225616-1027");

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, Arrays.asList(permId1), fetchOptions);

        assertEquals(1, map.size());
        Sample sample1 = map.get(permId1);

        List<DataSet> dataSets = sample1.getDataSets();
        AssertionUtil.assertCollectionSize(dataSets, 2);

        DataSet ds1 = dataSets.get(0);
        DataSet ds2 = dataSets.get(1);

        assertFalse(ds1 == ds2);
        assertEquals(ds1.getType().getCode(), "HCS_IMAGE");
        assertEquals(ds2.getType().getCode(), "HCS_IMAGE");
        assertTrue(ds1.getType() == ds2.getType());

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithDataSetsInCircularFetchOptions()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withDataSets().withSample().withDataSets().withType();

        SamplePermId permId1 = new SamplePermId("200902091225616-1027");

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, Arrays.asList(permId1), fetchOptions);

        assertEquals(1, map.size());
        Sample sample1 = map.get(permId1);

        List<DataSet> dataSets = sample1.getDataSets();
        AssertionUtil.assertCollectionSize(dataSets, 2);

        DataSet ds1 = dataSets.get(0);
        DataSet nestedDs1 = ds1.getSample().getDataSets().get(0);

        assertTrue(nestedDs1 != ds1);
        assertEquals(nestedDs1.getCode(), ds1.getCode());
        assertTypeNotFetched(ds1);
        assertEquals(nestedDs1.getType().getCode(), "HCS_IMAGE");

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithSpaceWithProjectAndExperiments()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withSpace().withProjects().withExperiments();

        SamplePermId permId = new SamplePermId("200902091250077-1060");

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, Arrays.asList(permId), fetchOptions);

        List<Project> totalProjects = new ArrayList<Project>();
        List<Experiment> totalExperiments = new ArrayList<Experiment>();

        assertEquals(1, map.size());
        Sample sample = map.get(permId);

        for (Project p : sample.getSpace().getProjects())
        {
            totalProjects.add(p);
            totalExperiments.addAll(p.getExperiments());
        }

        Collection<String> projectCodes = CollectionUtils.collect(totalProjects, new Transformer<Project, String>()
        {
            @Override
            public String transform(Project input)
            {
                return input.getCode();
            }
        });
        Collection<String> experimentCodes = CollectionUtils.collect(totalExperiments, new Transformer<Experiment, String>()
        {
            @Override
            public String transform(Experiment input)
            {
                return input.getCode();
            }
        });

        AssertionUtil.assertCollectionContainsOnly(projectCodes, "TEST-PROJECT", "NOE", "PROJECT-TO-DELETE");
        AssertionUtil.assertCollectionContainsOnly(experimentCodes, "EXP-SPACE-TEST", "EXP-TEST-2", "EXPERIMENT-TO-DELETE");

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithMaterialProperties()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withMaterialProperties().withRegistrator();
        fetchOptions.withProperties();

        SamplePermId permId = new SamplePermId("200902091219327-1025");

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, Arrays.asList(permId), fetchOptions);

        Sample sample = map.get(permId);

        assertEquals(sample.getProperties().get("BACTERIUM"), "BACTERIUM-X (BACTERIUM)");
        assertEquals(sample.getProperties().get("ANY_MATERIAL"), "1 (GENE)");

        Map<String, Material> materialProperties = sample.getMaterialProperties();

        Material bacterium = materialProperties.get("BACTERIUM");
        assertEquals(bacterium.getPermId(), new MaterialPermId("BACTERIUM-X", "BACTERIUM"));
        assertEquals(bacterium.getRegistrator().getUserId(), "test");
        assertTagsNotFetched(bacterium);

        Material gene = materialProperties.get("ANY_MATERIAL");
        assertEquals(gene.getPermId(), new MaterialPermId("1", "GENE"));
        assertEquals(gene.getRegistrator().getUserId(), "test");
        assertTagsNotFetched(gene);
    }

    @Test
    public void testGetWithHistoryNoChanges()
    {
        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_WITH_EMPTY_HISTORY");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("CISD"));

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.withHistory();

        Sample sample = testGetWithHistory(creation, fo, new SampleUpdate[] {});
        List<HistoryEntry> history = sample.getHistory();

        assertEquals(history.size(), 1);

        assertRelationshipHistory(history.get(0), new SpacePermId("CISD"), SampleRelationType.SPACE, sample.getRegistrationDate(), null);

        assertNotFetched(sample::getPropertiesHistory);
        assertNotFetched(sample::getSpaceHistory);
        assertNotFetched(sample::getProjectHistory);
        assertNotFetched(sample::getExperimentHistory);
        assertNotFetched(sample::getParentsHistory);
        assertNotFetched(sample::getChildrenHistory);
        assertNotFetched(sample::getContainerHistory);
        assertNotFetched(sample::getComponentsHistory);
        assertNotFetched(sample::getDataSetsHistory);
        assertNotFetched(sample::getUnknownHistory);
    }

    @Test(enabled = false)
    public void testGetWithHistoryDates()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        Date start = new Date();

        SampleUpdate update = new SampleUpdate();
        update.setSampleId(new SamplePermId("200902091219327-1025"));
        update.setProperty("SIZE", "12");

        v3api.updateSamples(sessionToken, Arrays.asList(update));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withHistory();

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, Arrays.asList(update.getSampleId()), fetchOptions);

        Date end = new Date();

        Sample sample = map.get(update.getSampleId());

        List<HistoryEntry> history = sample.getHistory();
        assertEquals(history.size(), 1);

        PropertyHistoryEntry entry = (PropertyHistoryEntry) history.get(0);
        assertEquals(entry.getPropertyName(), "SIZE");
        assertEquals(entry.getPropertyValue(), "123");

        assertTrue(entry.getValidFrom().after(start));
        assertTrue(entry.getValidFrom().before(end));
        assertTrue(entry.getValidTo().after(entry.getValidFrom()));
        assertTrue(entry.getValidTo().before(end));

        assertHistoryNotFetched(entry);
    }

    @Test
    public void testGetWithHistoryProperty()
    {
        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_WITH_PROPERTY_HISTORY");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setProperty("COMMENT", "comment1");

        SampleUpdate update = new SampleUpdate();
        update.setProperty("COMMENT", "comment2");

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.withHistory();
        fo.withPropertiesHistory();

        Sample sample = testGetWithHistory(creation, fo, update);

        List<HistoryEntry> history = sample.getHistory();
        assertEquals(history.size(), 3);

        assertPropertyHistory(history.get(0), "COMMENT", "comment1", sample.getRegistrationDate(), sample.getModificationDate());
        assertPropertyHistory(history.get(1), "COMMENT", "comment2", sample.getModificationDate(), null);
        assertRelationshipHistory(history.get(2), new SpacePermId("CISD"), SampleRelationType.SPACE, sample.getRegistrationDate(), null);

        List<HistoryEntry> propertiesHistory = sample.getPropertiesHistory();
        assertEquals(propertiesHistory.size(), 2);

        assertPropertyHistory(propertiesHistory.get(0), "COMMENT", "comment1", sample.getRegistrationDate(), sample.getModificationDate());
        assertPropertyHistory(propertiesHistory.get(1), "COMMENT", "comment2", sample.getModificationDate(), null);
    }

    @Test
    public void testGetWithHistorySystemProperty()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        String systemPropertyCode = "$PLATE_GEOMETRY";
        String simplePropertyCode = "PLATE_GEOMETRY";
        String originalSystemPropertyValue = "384_WELLS_16X24";
        String originalSimplePropertyValue = "I'm just random";
        String sampleTypeCode = "MASTER_PLATE";

        createNewPropertyType(sessionToken, sampleTypeCode, simplePropertyCode);

        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_WITH_SYS_PROPERTY");
        creation.setTypeId(new EntityTypePermId(sampleTypeCode));
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setProperty(systemPropertyCode, originalSystemPropertyValue);
        creation.setProperty(simplePropertyCode, originalSimplePropertyValue);

        SampleUpdate update1 = new SampleUpdate();
        update1.setProperty(systemPropertyCode, "96_WELLS_8X12");

        SampleUpdate update2 = new SampleUpdate();
        update2.setProperty(simplePropertyCode, "I have been updated");

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.withHistory();

        Sample sample = testGetWithHistory(creation, fo, update1, update2);
        List<HistoryEntry> history = sample.getHistory();

        assertEquals(history.size(), 5);

        assertPropertyHistory(history.get(0), systemPropertyCode, originalSystemPropertyValue + " [PLATE_GEOMETRY]");
        assertPropertyHistory(history.get(1), simplePropertyCode, originalSimplePropertyValue);
        assertPropertyHistory(history.get(2), systemPropertyCode, update1.getProperty(systemPropertyCode) + " [PLATE_GEOMETRY]",
                sample.getModificationDate(), null);
        assertPropertyHistory(history.get(3), simplePropertyCode, update2.getProperty(simplePropertyCode), sample.getModificationDate(), null);
        assertRelationshipHistory(history.get(4), creation.getSpaceId(), SampleRelationType.SPACE, sample.getRegistrationDate(), null);
    }

    @Test
    public void testGetWithHistorySpace()
    {
        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_WITH_SPACE_HISTORY");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("CISD"));

        SampleUpdate update = new SampleUpdate();
        update.setSpaceId(new SpacePermId("TEST-SPACE"));

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.withHistory();
        fo.withSpaceHistory();

        Sample sample = testGetWithHistory(creation, fo, update);

        List<HistoryEntry> history = sample.getHistory();
        assertEquals(history.size(), 2);

        assertRelationshipHistory(history.get(0), new SpacePermId("CISD"), SampleRelationType.SPACE,
                sample.getRegistrationDate(), sample.getModificationDate());
        assertRelationshipHistory(history.get(1), new SpacePermId("TEST-SPACE"), SampleRelationType.SPACE,
                sample.getRegistrationDate(), null);

        List<HistoryEntry> spaceHistory = sample.getSpaceHistory();
        assertEquals(spaceHistory.size(), 2);

        assertRelationshipHistory(spaceHistory.get(0), new SpacePermId("CISD"), SampleRelationType.SPACE,
                sample.getRegistrationDate(), sample.getModificationDate());
        assertRelationshipHistory(spaceHistory.get(1), new SpacePermId("TEST-SPACE"), SampleRelationType.SPACE,
                sample.getRegistrationDate(), null);
    }

    @Test
    public void testGetWithHistoryProject()
    {
        String instanceAdminSessionToken = v3api.login(TEST_USER, PASSWORD);

        ISpaceId spaceId = new SpacePermId("CISD");

        // create projects /CISD/A, /CISD/B, /CISD/C
        ProjectCreation projectACreation = new ProjectCreation();
        projectACreation.setCode("PROJECT_A");
        projectACreation.setSpaceId(spaceId);

        ProjectCreation projectBCreation = new ProjectCreation();
        projectBCreation.setCode("PROJECT_B");
        projectBCreation.setSpaceId(spaceId);

        ProjectCreation projectCCreation = new ProjectCreation();
        projectCCreation.setCode("PROJECT_C");
        projectCCreation.setSpaceId(spaceId);

        List<ProjectPermId> projectIds =
                v3api.createProjects(instanceAdminSessionToken, Arrays.asList(projectACreation, projectBCreation, projectCCreation));

        // create a user with access to project A and C
        PersonCreation projectUserACCreation = new PersonCreation();
        projectUserACCreation.setUserId("project_user_AC_pa_on");
        IPersonId projectACUserId = v3api.createPersons(instanceAdminSessionToken, Arrays.asList(projectUserACCreation)).get(0);

        RoleAssignmentCreation projectUserARoleCreation = new RoleAssignmentCreation();
        projectUserARoleCreation.setUserId(projectACUserId);
        projectUserARoleCreation.setRole(Role.USER);
        projectUserARoleCreation.setProjectId(projectIds.get(0));

        RoleAssignmentCreation projectUserCRoleCreation = new RoleAssignmentCreation();
        projectUserCRoleCreation.setUserId(projectACUserId);
        projectUserCRoleCreation.setRole(Role.OBSERVER);
        projectUserCRoleCreation.setProjectId(projectIds.get(2));

        v3api.createRoleAssignments(instanceAdminSessionToken, Arrays.asList(projectUserARoleCreation, projectUserCRoleCreation));

        // create sample in project /CISD/A
        SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setCode("SAMPLE_WITH_PROJECT_HISTORY");
        sampleCreation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        sampleCreation.setSpaceId(spaceId);
        sampleCreation.setProjectId(projectIds.get(0));
        SamplePermId sampleId = v3api.createSamples(instanceAdminSessionToken, Arrays.asList(sampleCreation)).get(0);

        // move sample to project /CISD/B
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sampleId);
        sampleUpdate.setProjectId(projectIds.get(1));
        v3api.updateSamples(instanceAdminSessionToken, Arrays.asList(sampleUpdate));

        // move sample to project /CISD/C
        SampleUpdate sampleUpdate2 = new SampleUpdate();
        sampleUpdate2.setSampleId(sampleId);
        sampleUpdate2.setProjectId(projectIds.get(2));
        v3api.updateSamples(instanceAdminSessionToken, Arrays.asList(sampleUpdate2));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withHistory();
        fetchOptions.withProjectHistory();

        // get history with CISD space user
        String cisdSpaceUserSessionToken = v3api.login(TEST_POWER_USER_CISD, PASSWORD);
        Sample sample = v3api.getSamples(cisdSpaceUserSessionToken, Arrays.asList(sampleId), fetchOptions).get(sampleId);

        assertEquals(sample.getHistory().size(), 3);
        assertRelationshipHistory(sample.getHistory().get(0), projectIds.get(0), SampleRelationType.PROJECT);
        assertRelationshipHistory(sample.getHistory().get(1), projectIds.get(1), SampleRelationType.PROJECT);
        assertRelationshipHistory(sample.getHistory().get(2), projectIds.get(2), SampleRelationType.PROJECT);

        assertEquals(sample.getProjectHistory().size(), 3);
        assertRelationshipHistory(sample.getProjectHistory().get(0), projectIds.get(0), SampleRelationType.PROJECT);
        assertRelationshipHistory(sample.getProjectHistory().get(1), projectIds.get(1), SampleRelationType.PROJECT);
        assertRelationshipHistory(sample.getProjectHistory().get(2), projectIds.get(2), SampleRelationType.PROJECT);

        // get history with user that has access to project A and C
        String projectUserACSessionToken = v3api.login(projectUserACCreation.getUserId(), PASSWORD);
        sample = v3api.getSamples(projectUserACSessionToken, Arrays.asList(sampleId), fetchOptions).get(sampleId);

        assertEquals(sample.getHistory().size(), 2);
        assertRelationshipHistory(sample.getHistory().get(0), projectIds.get(0), SampleRelationType.PROJECT);
        assertRelationshipHistory(sample.getHistory().get(1), projectIds.get(2), SampleRelationType.PROJECT);

        assertEquals(sample.getProjectHistory().size(), 2);
        assertRelationshipHistory(sample.getProjectHistory().get(0), projectIds.get(0), SampleRelationType.PROJECT);
        assertRelationshipHistory(sample.getProjectHistory().get(1), projectIds.get(2), SampleRelationType.PROJECT);

        // get history with instance admin
        sample = v3api.getSamples(instanceAdminSessionToken, Arrays.asList(sampleId), fetchOptions).get(sampleId);

        assertEquals(sample.getHistory().size(), 3);
        assertRelationshipHistory(sample.getHistory().get(0), projectIds.get(0), SampleRelationType.PROJECT);
        assertRelationshipHistory(sample.getHistory().get(1), projectIds.get(1), SampleRelationType.PROJECT);
        assertRelationshipHistory(sample.getHistory().get(2), projectIds.get(2), SampleRelationType.PROJECT);

        assertEquals(sample.getProjectHistory().size(), 3);
        assertRelationshipHistory(sample.getProjectHistory().get(0), projectIds.get(0), SampleRelationType.PROJECT);
        assertRelationshipHistory(sample.getProjectHistory().get(1), projectIds.get(1), SampleRelationType.PROJECT);
        assertRelationshipHistory(sample.getProjectHistory().get(2), projectIds.get(2), SampleRelationType.PROJECT);
    }

    @Test
    public void testGetWithHistoryExperiment()
    {
        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_WITH_EXPERIMENT_HISTORY");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP1"));

        SampleUpdate update = new SampleUpdate();
        update.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP11"));

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.withHistory();
        fo.withExperimentHistory();

        Sample sample = testGetWithHistory(creation, fo, update);

        assertEquals(sample.getHistory().size(), 2);
        assertRelationshipHistory(sample.getHistory().get(0), new ExperimentPermId("200811050951882-1028"), SampleRelationType.EXPERIMENT,
                sample.getRegistrationDate(), sample.getModificationDate());
        assertRelationshipHistory(sample.getHistory().get(1), new ExperimentPermId("200811050952663-1030"), SampleRelationType.EXPERIMENT,
                sample.getRegistrationDate(), null);

        assertEquals(sample.getExperimentHistory().size(), 2);
        assertRelationshipHistory(sample.getExperimentHistory().get(0), new ExperimentPermId("200811050951882-1028"), SampleRelationType.EXPERIMENT,
                sample.getRegistrationDate(), sample.getModificationDate());
        assertRelationshipHistory(sample.getExperimentHistory().get(1), new ExperimentPermId("200811050952663-1030"), SampleRelationType.EXPERIMENT,
                sample.getRegistrationDate(), null);
    }

    @Test
    public void testGetWithHistoryDataSet()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_WITH_DATA_SET_HISTORY");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setExperimentId(new ExperimentIdentifier("/CISD/DEFAULT/EXP-REUSE"));

        List<SamplePermId> permIds = v3api.createSamples(sessionToken, Arrays.asList(creation));

        DataSetUpdate update1 = new DataSetUpdate();
        update1.setDataSetId(new DataSetPermId("COMPONENT_1A"));
        update1.setSampleId(permIds.get(0));

        v3api.updateDataSets(sessionToken, Arrays.asList(update1));

        DataSet dataSetAfterUpdate1 =
                v3api.getDataSets(sessionToken, Arrays.asList(update1.getDataSetId()), new DataSetFetchOptions()).get(update1.getDataSetId());

        DataSetUpdate update2 = new DataSetUpdate();
        update2.setDataSetId(new DataSetPermId("COMPONENT_1A"));
        update2.setSampleId(null);

        v3api.updateDataSets(sessionToken, Arrays.asList(update2));

        DataSet dataSetAfterUpdate2 =
                v3api.getDataSets(sessionToken, Arrays.asList(update2.getDataSetId()), new DataSetFetchOptions()).get(update2.getDataSetId());

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withHistory();
        fetchOptions.withDataSetsHistory();

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, permIds, fetchOptions);
        assertEquals(map.size(), 1);

        Sample sample = map.get(permIds.get(0));

        assertEquals(sample.getHistory().size(), 2);
        assertRelationshipHistory(sample.getHistory().get(0), new ExperimentPermId("200811050940555-1032"), SampleRelationType.EXPERIMENT,
                sample.getRegistrationDate(), null);
        assertRelationshipHistory(sample.getHistory().get(1), new DataSetPermId("COMPONENT_1A"), SampleRelationType.DATA_SET,
                dataSetAfterUpdate1.getModificationDate(), dataSetAfterUpdate2.getModificationDate());

        assertEquals(sample.getDataSetsHistory().size(), 1);
        assertRelationshipHistory(sample.getDataSetsHistory().get(0), new DataSetPermId("COMPONENT_1A"), SampleRelationType.DATA_SET,
                dataSetAfterUpdate1.getModificationDate(), dataSetAfterUpdate2.getModificationDate());

        assertNotFetched(sample::getExperimentHistory);
    }

    @Test
    public void testGetWithHistoryContainer()
    {
        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_WITH_CONTAINER_HISTORY");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setContainerId(new SampleIdentifier("/CISD/CL1"));

        SampleUpdate update = new SampleUpdate();
        update.setContainerId(null);

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.withHistory();
        fo.withContainerHistory();

        Sample sample = testGetWithHistory(creation, fo, update);

        assertEquals(sample.getHistory().size(), 2);
        assertRelationshipHistory(sample.getHistory().get(0), new SpacePermId("CISD"), SampleRelationType.SPACE, sample.getRegistrationDate(), null);
        assertRelationshipHistory(sample.getHistory().get(1), new SamplePermId("200811050919915-8"), SampleRelationType.CONTAINER,
                sample.getRegistrationDate(), sample.getModificationDate());

        assertEquals(sample.getContainerHistory().size(), 1);
        assertRelationshipHistory(sample.getContainerHistory().get(0), new SamplePermId("200811050919915-8"), SampleRelationType.CONTAINER,
                sample.getRegistrationDate(), sample.getModificationDate());

        assertNotFetched(sample::getSpaceHistory);
    }

    @Test
    public void testGetWithHistoryComponents()
    {
        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_WITH_COMPONENTS_HISTORY");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setComponentIds(Arrays.asList(new SampleIdentifier("/CISD/CL1")));

        SampleUpdate update = new SampleUpdate();
        update.getComponentIds().set();

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.withHistory();
        fo.withComponentsHistory();

        Sample sample = testGetWithHistory(creation, fo, update);

        assertEquals(sample.getHistory().size(), 2);
        assertRelationshipHistory(sample.getHistory().get(0), new SpacePermId("CISD"), SampleRelationType.SPACE, sample.getRegistrationDate(), null);
        assertRelationshipHistory(sample.getHistory().get(1), new SamplePermId("200811050919915-8"), SampleRelationType.COMPONENT,
                sample.getRegistrationDate(), sample.getModificationDate());

        assertEquals(sample.getComponentsHistory().size(), 1);
        assertRelationshipHistory(sample.getComponentsHistory().get(0), new SamplePermId("200811050919915-8"), SampleRelationType.COMPONENT,
                sample.getRegistrationDate(), sample.getModificationDate());

        assertNotFetched(sample::getSpaceHistory);
    }

    @Test
    public void testGetWithHistoryParent()
    {
        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_WITH_PARENT_HISTORY");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setParentIds(Arrays.asList(new SampleIdentifier("/CISD/CL1")));

        SampleUpdate update = new SampleUpdate();
        update.getParentIds().set();

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.withHistory();
        fo.withParentsHistory();

        Sample sample = testGetWithHistory(creation, fo, update);

        assertEquals(sample.getHistory().size(), 2);
        assertRelationshipHistory(sample.getHistory().get(0), new SpacePermId("CISD"), SampleRelationType.SPACE, sample.getRegistrationDate(), null);
        assertRelationshipHistory(sample.getHistory().get(1), new SamplePermId("200811050919915-8"), SampleRelationType.PARENT,
                sample.getRegistrationDate(), sample.getModificationDate());

        assertEquals(sample.getParentsHistory().size(), 1);
        assertRelationshipHistory(sample.getParentsHistory().get(0), new SamplePermId("200811050919915-8"), SampleRelationType.PARENT,
                sample.getRegistrationDate(), sample.getModificationDate());

        assertNotFetched(sample::getSpaceHistory);
    }

    @Test
    public void testGetWithHistoryChild()
    {
        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_WITH_CHILD_HISTORY");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setChildIds(Arrays.asList(new SampleIdentifier("/CISD/CL1")));

        SampleUpdate update = new SampleUpdate();
        update.getChildIds().set();

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.withHistory();
        fo.withChildrenHistory();

        Sample sample = testGetWithHistory(creation, fo, update);

        assertEquals(sample.getHistory().size(), 2);
        assertRelationshipHistory(sample.getHistory().get(0), new SpacePermId("CISD"), SampleRelationType.SPACE, sample.getRegistrationDate(),
                null);
        assertRelationshipHistory(sample.getHistory().get(1), new SamplePermId("200811050919915-8"), SampleRelationType.CHILD,
                sample.getRegistrationDate(), sample.getModificationDate());

        assertEquals(sample.getChildrenHistory().size(), 1);
        assertRelationshipHistory(sample.getChildrenHistory().get(0), new SamplePermId("200811050919915-8"), SampleRelationType.CHILD,
                sample.getRegistrationDate(), sample.getModificationDate());

        assertNotFetched(sample::getSpaceHistory);
    }

    private Sample testGetWithHistory(SampleCreation creation, SampleFetchOptions fo, SampleUpdate... updates)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        List<SamplePermId> permIds = v3api.createSamples(sessionToken, Arrays.asList(creation));

        if (updates != null)
        {
            for (SampleUpdate update : updates)
            {
                update.setSampleId(permIds.get(0));
                v3api.updateSamples(sessionToken, Arrays.asList(update));
            }
        }

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, permIds, fo);

        assertEquals(map.size(), 1);
        Sample sample = map.get(permIds.get(0));

        v3api.logout(sessionToken);

        return sample;
    }

    @Test
    public void testGetWithAttachment()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ISampleId id = new SamplePermId("200811050946559-980");

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withAttachments();

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, Arrays.asList(id), fetchOptions);

        assertEquals(map.size(), 1);
        Sample sample = map.get(id);

        List<Attachment> attachments = sample.getAttachments();
        assertEquals(attachments.size(), 1);

        Attachment attachment = attachments.get(0);
        assertEquals(attachment.getFileName(), "sampleHistory.txt");

        v3api.logout(sessionToken);
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testGetWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String sessionToken = v3api.login(user.getUserId(), PASSWORD);

        ISampleId id = new SampleIdentifier("/TEST-SPACE/TEST-PROJECT/EV-TEST");
        SampleFetchOptions fetchOptions = new SampleFetchOptions();

        if (user.isDisabledProjectUser())
        {
            assertAuthorizationFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.getSamples(sessionToken, Arrays.asList(id), fetchOptions);
                }
            });
        } else
        {
            Map<ISampleId, Sample> samples = v3api.getSamples(sessionToken, Arrays.asList(id), fetchOptions);

            if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
            {
                assertEquals(samples.size(), 1);
            } else
            {
                assertEquals(samples.size(), 0);
            }
        }
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.withSpace();
        fo.withProject();

        v3api.getSamples(sessionToken, Arrays.asList(new SamplePermId("200902091219327-1025"), new SampleIdentifier("/TEST-SPACE/NOE/CP-TEST-4")),
                fo);

        assertAccessLog(
                "get-samples  SAMPLE_IDS('[200902091219327-1025, /TEST-SPACE/NOE/CP-TEST-4]') FETCH_OPTIONS('Sample\n    with Project\n    with Space\n')");
    }

}

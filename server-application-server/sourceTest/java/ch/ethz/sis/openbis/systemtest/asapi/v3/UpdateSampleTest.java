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

import static ch.systemsx.cisd.common.test.AssertionUtil.assertCollectionContainsOnly;
import static ch.systemsx.cisd.common.test.AssertionUtil.assertCollectionSize;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.Attachment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.create.AttachmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.id.AttachmentFileName;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.Relationship;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.CreationId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.delete.DataSetDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.id.DataStorePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.HistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.MaterialPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.PersonPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create.ProjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.Role;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.create.RoleAssignmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete.SampleDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.SpaceCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.ITagId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagCode;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.FreezingFlags;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.systemtest.authorization.ProjectAuthorizationUser;
import junit.framework.Assert;

/**
 * @author pkupczyk
 */
public class UpdateSampleTest extends AbstractSampleTest
{
    private static final String PREFIX = "UST-";

    @Test
    public void testUpdateSharedSampleWithHomelessPowerUser()
    {
        final SamplePermId permId = new SamplePermId("200811050947161-653");

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                String sessionToken = v3api.login(TEST_NO_HOME_SPACE, PASSWORD);

                final SampleUpdate update = new SampleUpdate();
                update.setSampleId(permId);
                v3api.updateSamples(sessionToken, Collections.singletonList(update));
            }
        }, permId);
    }

    @Test
    public void testUpdateBiggerThanPostgresDriverArgumentsLimitWithIndexCheck()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        List<SampleUpdate> updates = new ArrayList<SampleUpdate>();
        for (int i = 0; i < 40000; i++)
        {
            SampleUpdate update = new SampleUpdate();
            update.setSampleId(new SamplePermId("200811050945092-976"));
            update.setProperty("OFFSET", "50");
            updates.add(update);
        }

        v3api.updateSamples(sessionToken, updates);

        assertSamplesExists("200811050945092-976");
    }

    @Test
    public void testUpdateWithIndexCheck()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleUpdate update = new SampleUpdate();
        update.setSampleId(new SamplePermId("200811050945092-976"));
        update.setProperty("OFFSET", "50");

        v3api.updateSamples(sessionToken, Arrays.asList(update));

        assertSamplesExists("200811050945092-976");
    }

    @Test
    public void testUpdateWithSampleExisting()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("CISD"));

        List<SamplePermId> ids = v3api.createSamples(sessionToken, Arrays.asList(creation));

        SampleUpdate update = new SampleUpdate();
        update.setSampleId(ids.get(0));
        update.setSpaceId(new SpacePermId("TEST-SPACE"));
        update.setExperimentId(new ExperimentPermId("201206190940555-1032"));
        update.setProperty("COMMENT", "test update");

        v3api.updateSamples(sessionToken, Arrays.asList(update));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withSpace();
        fetchOptions.withExperiment();
        fetchOptions.withProperties();

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, ids, fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        AssertionUtil.assertCollectionSize(samples, 1);

        Sample sample = samples.get(0);
        assertEquals(sample.getSpace().getCode(), "TEST-SPACE");
        assertEquals(sample.getExperiment().getIdentifier().getIdentifier(), "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
        assertEquals(sample.getProperties().size(), 1);
        assertEquals(sample.getProperties().get("COMMENT"), "test update");
    }

    @Test
    public void testUpdateSampleMetaData()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setMetaData(Map.of("key_modify", "value_modify", "key_delete", "value_delete"));

        List<SamplePermId> ids = v3api.createSamples(sessionToken, Arrays.asList(creation));

        SampleUpdate update = new SampleUpdate();
        update.setSampleId(ids.get(0));
        update.setSpaceId(new SpacePermId("TEST-SPACE"));
        update.setExperimentId(new ExperimentPermId("201206190940555-1032"));
        update.getMetaData().put("key_modify", "new_value");
        update.getMetaData().remove("key_delete");
        update.getMetaData().add(Map.of("key_add", "value_add"));

        v3api.updateSamples(sessionToken, Arrays.asList(update));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withSpace();
        fetchOptions.withExperiment();

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, ids, fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        AssertionUtil.assertCollectionSize(samples, 1);

        Sample sample = samples.get(0);
        assertEquals(sample.getSpace().getCode(), "TEST-SPACE");
        assertEquals(sample.getExperiment().getIdentifier().getIdentifier(), "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
        assertEquals(sample.getMetaData(), Map.of("key_modify", "new_value", "key_add", "value_add"));
    }

    @Test
    public void testUpdateSampleMetaDataSetEmpty()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setMetaData(Map.of("key_modify", "value_modify", "key_delete", "value_delete"));

        List<SamplePermId> ids = v3api.createSamples(sessionToken, Arrays.asList(creation));

        SampleUpdate update = new SampleUpdate();
        update.setSampleId(ids.get(0));
        update.setSpaceId(new SpacePermId("TEST-SPACE"));
        update.setExperimentId(new ExperimentPermId("201206190940555-1032"));
        update.getMetaData().set(Map.of());

        v3api.updateSamples(sessionToken, Arrays.asList(update));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withSpace();
        fetchOptions.withExperiment();

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, ids, fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        AssertionUtil.assertCollectionSize(samples, 1);

        Sample sample = samples.get(0);
        assertEquals(sample.getSpace().getCode(), "TEST-SPACE");
        assertEquals(sample.getExperiment().getIdentifier().getIdentifier(), "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
        assertEquals(sample.getMetaData(), Map.of());
    }

    @Test
    public void testUpdateSampleMetaDataLastSetMatters()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setMetaData(Map.of("key_modify", "value_modify", "key_delete", "value_delete"));

        List<SamplePermId> ids = v3api.createSamples(sessionToken, Arrays.asList(creation));

        SampleUpdate update = new SampleUpdate();
        update.setSampleId(ids.get(0));
        update.setSpaceId(new SpacePermId("TEST-SPACE"));
        update.setExperimentId(new ExperimentPermId("201206190940555-1032"));
        update.getMetaData().set(Map.of("a", "b"), Map.of());

        v3api.updateSamples(sessionToken, Arrays.asList(update));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withSpace();
        fetchOptions.withExperiment();

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, ids, fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        AssertionUtil.assertCollectionSize(samples, 1);

        Sample sample = samples.get(0);
        assertEquals(sample.getSpace().getCode(), "TEST-SPACE");
        assertEquals(sample.getExperiment().getIdentifier().getIdentifier(), "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
        assertEquals(sample.getMetaData(), Map.of());
    }

    @Test
    public void testUpdateWithSampleNonexistent()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISampleId sampleId = new SamplePermId("IDONTEXIST");
        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(sampleId);

        assertObjectNotFoundException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.updateSamples(sessionToken, Arrays.asList(update));
            }
        }, sampleId);
    }

    @Test
    public void testUpdateWithSampleUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISampleId sampleId = new SamplePermId("200902091219327-1025");
        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(sampleId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.updateSamples(sessionToken, Arrays.asList(update));
            }
        }, sampleId, patternContains("checking access (1/1)", toDblQuotes("'identifier' : '/CISD/NEMO/CP-TEST-1'")));
    }

    @Test
    public void testUpdateWithSpace()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("CISD"));

        List<SamplePermId> ids = v3api.createSamples(sessionToken, Arrays.asList(creation));

        SampleUpdate update = new SampleUpdate();
        update.setSampleId(ids.get(0));
        update.setSpaceId(new SpacePermId("TEST-SPACE"));

        v3api.updateSamples(sessionToken, Arrays.asList(update));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, ids, fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        AssertionUtil.assertCollectionSize(samples, 1);

        Sample sample = samples.get(0);
        assertEquals(sample.getIdentifier().getIdentifier(), "/TEST-SPACE/SAMPLE");
        assertSampleIdentifier(sample, "/TEST-SPACE/SAMPLE");
    }

    @Test
    public void testUpdateWithSpaceUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISpaceId spaceId = new SpacePermId("CISD");
        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(new SamplePermId("200902091250077-1060"));
        update.setSpaceId(spaceId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.updateSamples(sessionToken, Arrays.asList(update));
            }
        }, spaceId, patternContains("updating relation sample-space (1/1)", toDblQuotes("'identifier' : '/TEST-SPACE/NOE/CP-TEST-4'")));
    }

    @Test
    public void testUpdateWithSpaceNonexistent()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISpaceId spaceId = new SpacePermId("IDONTEXIST");
        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(new SamplePermId("200902091250077-1060"));
        update.setSpaceId(spaceId);

        assertObjectNotFoundException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.updateSamples(sessionToken, Arrays.asList(update));
            }
        }, spaceId, patternContains("updating relation sample-space (1/1)", toDblQuotes("'identifier' : '/TEST-SPACE/NOE/CP-TEST-4'")));
    }

    @Test
    public void testUpdateWithSpaceNullForSpaceSampleAsAdminUser()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("TEST-SPACE"));
        creation.setExperimentId(new ExperimentPermId("201206190940555-1032"));

        List<SamplePermId> ids = v3api.createSamples(sessionToken, Arrays.asList(creation));

        SampleUpdate update = new SampleUpdate();
        update.setSampleId(ids.get(0));
        update.setSpaceId(null);
        update.setProjectId(null);
        update.setExperimentId(null);

        v3api.updateSamples(sessionToken, Arrays.asList(update));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withSpace();
        fetchOptions.withExperiment();
        fetchOptions.withProperties();

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, ids, fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        AssertionUtil.assertCollectionSize(samples, 1);

        Sample sample = samples.get(0);
        assertEquals(sample.getSpace(), null);
        assertEquals(sample.getExperiment(), null);
        assertSampleIdentifier(sample, "/SAMPLE");
    }

    @Test
    public void testUpdateWithSpaceNullForSpaceSampleAsSpaceUser()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("TEST-SPACE"));
        creation.setExperimentId(new ExperimentPermId("201206190940555-1032"));

        List<SamplePermId> ids = v3api.createSamples(sessionToken, Arrays.asList(creation));

        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(ids.get(0));
        update.setSpaceId(null);
        update.setProjectId(null);
        update.setExperimentId(null);

        assertAuthorizationFailureException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.updateSamples(sessionToken, Arrays.asList(update));
            }
        }, patternContains("updating relation sample-space (1/1)", toDblQuotes("'identifier' : '/TEST-SPACE/TEST-PROJECT/SAMPLE'")));
    }

    @Test
    public void testUpdateWithSpaceNullForSpaceSampleWithDataSets()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(new SampleIdentifier("/CISD/NEMO/CP-TEST-1"));
        update.setSpaceId(null);
        update.setProjectId(null);
        update.setExperimentId(null);

        assertUserFailureException(new IDelegatedAction()
                                   {
                                       @Override
                                       public void execute()
                                       {
                                           v3api.updateSamples(sessionToken, Arrays.asList(update));
                                       }
                                   },
                "Cannot detach the sample /CP-TEST-1 (perm id: 200902091219327-1025) from the space because there are already datasets attached to the sample",
                patternContains("verifying (1/1)", toDblQuotes("'identifier' : '/CP-TEST-1'")));
    }

    @Test
    public void testUpdateWithSpaceNotNullForSharedSampleAsAdminUser()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));

        List<SamplePermId> ids = v3api.createSamples(sessionToken, Arrays.asList(creation));

        SampleUpdate update = new SampleUpdate();
        update.setSampleId(ids.get(0));
        update.setSpaceId(new SpacePermId("TEST-SPACE"));
        update.setExperimentId(new ExperimentPermId("201206190940555-1032"));

        v3api.updateSamples(sessionToken, Arrays.asList(update));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withSpace();
        fetchOptions.withExperiment();
        fetchOptions.withProperties();

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, ids, fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        AssertionUtil.assertCollectionSize(samples, 1);

        Sample sample = samples.get(0);
        assertEquals(sample.getSpace().getCode(), "TEST-SPACE");
        assertEquals(sample.getExperiment().getIdentifier().getIdentifier(), "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
        assertSampleIdentifier(sample, "/TEST-SPACE/TEST-PROJECT/SAMPLE");
    }

    @Test
    public void testUpdateWithSpaceNotNullForSharedSampleAsSpaceUser()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));

        List<SamplePermId> ids = v3api.createSamples(sessionToken, Arrays.asList(creation));

        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(ids.get(0));
        update.setSpaceId(new SpacePermId("TEST-SPACE"));

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                String sessionToken2 = v3api.login(TEST_SPACE_USER, PASSWORD);
                v3api.updateSamples(sessionToken2, Arrays.asList(update));
            }
        }, ids.get(0), patternContains("checking access (1/1)", toDblQuotes("'identifier' : '/SAMPLE'")));
    }

    @Test
    public void testUpdateWithExperimentInTheSameSpace()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("CISD"));

        List<SamplePermId> ids = v3api.createSamples(sessionToken, Arrays.asList(creation));

        SampleUpdate update = new SampleUpdate();
        update.setSampleId(ids.get(0));
        update.setExperimentId(new ExperimentPermId("200811050952663-1029"));

        v3api.updateSamples(sessionToken, Arrays.asList(update));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withExperiment();

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, ids, fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        AssertionUtil.assertCollectionSize(samples, 1);

        Sample sample = samples.get(0);
        assertEquals(sample.getExperiment().getIdentifier().getIdentifier(), "/CISD/NEMO/EXP10");
    }

    @Test
    public void testUpdateWithExperimentInDifferentSpace()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("CISD"));

        List<SamplePermId> ids = v3api.createSamples(sessionToken, Arrays.asList(creation));

        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(ids.get(0));
        update.setExperimentId(new ExperimentPermId("201206190940555-1032"));

        assertUserFailureException(new IDelegatedAction()
                                   {
                                       @Override
                                       public void execute()
                                       {
                                           v3api.updateSamples(sessionToken, Arrays.asList(update));
                                       }
                                   }, "Experiment: /TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST (perm id: 201206190940555-1032)",
                patternContains("verifying (1/1)", toDblQuotes("'identifier' : '/CISD/SAMPLE'")));
    }

    @Test
    public void testUpdateWithExperimentForSharedSample()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));

        List<SamplePermId> ids = v3api.createSamples(sessionToken, Arrays.asList(creation));

        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(ids.get(0));
        update.setExperimentId(new ExperimentPermId("201206190940555-1032"));

        assertUserFailureException(new IDelegatedAction()
                                   {
                                       @Override
                                       public void execute()
                                       {
                                           v3api.updateSamples(sessionToken, Arrays.asList(update));
                                       }
                                   }, "Experiment: /TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST (perm id: 201206190940555-1032)",
                patternContains("verifying (1/1)", toDblQuotes("'identifier' : '/SAMPLE'")));
    }

    @Test
    public void testUpdateWithExperimentNullForSampleWithoutDataSets()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setExperimentId(new ExperimentPermId("200811050952663-1029"));

        List<SamplePermId> ids = v3api.createSamples(sessionToken, Arrays.asList(creation));

        SampleUpdate update = new SampleUpdate();
        update.setSampleId(ids.get(0));
        update.setExperimentId(null);

        v3api.updateSamples(sessionToken, Arrays.asList(update));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withExperiment();

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, ids, fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        AssertionUtil.assertCollectionSize(samples, 1);

        Sample sample = samples.get(0);
        Assert.assertNull(sample.getExperiment());
    }

    @Test
    public void testUpdateWithExperimentSampleTurnedIntoSpaceSampleAsProjectUser()
    {
        String sessionToken = v3api.login(ProjectAuthorizationUser.TEST_PROJECT_PA_ON, PASSWORD);

        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("TEST-SPACE"));
        creation.setExperimentId(new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));

        List<SamplePermId> ids = v3api.createSamples(sessionToken, Arrays.asList(creation));

        SampleUpdate update = new SampleUpdate();
        update.setSampleId(ids.get(0));
        update.setProjectId(null);
        update.setExperimentId(null);

        assertAuthorizationFailureException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.updateSamples(sessionToken, Arrays.asList(update));
            }
        });
    }

    @Test
    public void testUpdateWithExperimentNullForSampleWithDataSets()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ISampleId sampleId = new SampleIdentifier("/CISD/NEMO/CP-TEST-1");
        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(sampleId);
        update.setExperimentId(null);

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withExperiment();
        fetchOptions.withDataSets().withExperiment();

        v3api.updateSamples(sessionToken, Arrays.asList(update));

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, Arrays.asList(sampleId), fetchOptions);
        Sample sample = map.get(sampleId);

        Assert.assertNull(sample.getExperiment());
        Assert.assertTrue(sample.getDataSets().size() > 0);

        for (DataSet dataSet : sample.getDataSets())
        {
            Assert.assertNull(dataSet.getExperiment());
        }
    }

    @Test
    public void testUpdateWithExperimentUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final IExperimentId experimentId = new ExperimentPermId("200811050951882-1028");
        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(new SamplePermId("200902091250077-1060"));
        update.setExperimentId(experimentId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.updateSamples(sessionToken, Arrays.asList(update));
            }
        }, experimentId, patternContains("updating relation sample-experiment (1/1)", toDblQuotes("'identifier' : '/TEST-SPACE/NOE/CP-TEST-4'")));
    }

    @Test
    public void testUpdateWithExperimentNonexistent()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final IExperimentId experimentId = new ExperimentPermId("IDONTEXIST");
        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(new SamplePermId("200902091250077-1060"));
        update.setExperimentId(experimentId);

        assertObjectNotFoundException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.updateSamples(sessionToken, Arrays.asList(update));
            }
        }, experimentId, patternContains("updating relation sample-experiment (1/1)", toDblQuotes("'identifier' : '/TEST-SPACE/NOE/CP-TEST-4'")));
    }

    @Test
    public void testUpdateWithProperties()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setProperty("COMMENT", "comment 1");
        creation.setProperty("SIZE", "1");
        creation.setProperty("ORGANISM", "GORILLA");

        List<SamplePermId> ids = v3api.createSamples(sessionToken, Arrays.asList(creation));

        SampleUpdate update = new SampleUpdate();
        update.setSampleId(ids.get(0));
        // change existing property
        update.setProperty("COMMENT", "comment 2");
        // remove existing property
        update.setProperty("ORGANISM", null);
        // remove non existing property
        update.setProperty("ANY_MATERIAL", null);
        // set new property
        update.setProperty("BACTERIUM", "BACTERIUM1 (BACTERIUM)");

        v3api.updateSamples(sessionToken, Arrays.asList(update));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, ids, fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        AssertionUtil.assertCollectionSize(samples, 1);

        Sample sample = samples.get(0);

        Map<String, String> expectedProperties = new HashMap<String, String>();
        expectedProperties.put("COMMENT", "comment 2");
        expectedProperties.put("SIZE", "1");
        expectedProperties.put("BACTERIUM", "BACTERIUM1 (BACTERIUM)");
        assertEquals(sample.getProperties(), expectedProperties);
    }

    @Test
    public void testUpdateWithUnknownPropertyOfTypeSample()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleCreation creation = new SampleCreation();
        creation.setAutoGeneratedCode(true);
        creation.setTypeId(new EntityTypePermId("MASTER_PLATE"));
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setProperty("$PLATE_GEOMETRY", "384_WELLS_16X24");
        SamplePermId samplePermId = v3api.createSamples(sessionToken, Arrays.asList(creation)).get(0);

        SampleUpdate update = new SampleUpdate();
        update.setSampleId(samplePermId);
        update.setProperty("PLATE", "200811050924898-997");

        // When
        assertUserFailureException(Void -> v3api.updateSamples(sessionToken, Arrays.asList(update)),
                // Then
                "Property type with code 'PLATE' does not exist");
    }

    @Test
    public void testUpdateWithPropertyOfTypeSampleWithUnknownSample()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createASamplePropertyType(sessionToken, null);
        EntityTypePermId sampleType = createASampleType(sessionToken, false, propertyType);
        SampleCreation creation = new SampleCreation();
        creation.setAutoGeneratedCode(true);
        creation.setTypeId(sampleType);
        creation.setProperty(propertyType.getPermId(), "/CISD/CL1");
        SamplePermId samplePermId = v3api.createSamples(sessionToken, Arrays.asList(creation)).get(0);

        SampleUpdate update = new SampleUpdate();
        update.setSampleId(samplePermId);
        update.setProperty(propertyType.getPermId(), "/CISD/UNKNOWN");

        // When
        assertUserFailureException(Void -> v3api.updateSamples(sessionToken, Arrays.asList(update)),
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
        EntityTypePermId sampleType = createASampleType(sessionToken, false, propertyType);
        SampleCreation creation = new SampleCreation();
        creation.setAutoGeneratedCode(true);
        creation.setTypeId(sampleType);
        creation.setProperty(propertyType.getPermId(), "200811050917877-438");
        SamplePermId samplePermId = v3api.createSamples(sessionToken, Arrays.asList(creation)).get(0);

        SampleUpdate update = new SampleUpdate();
        update.setSampleId(samplePermId);
        update.setProperty(propertyType.getPermId(), "200811050919915-8");

        // When
        assertUserFailureException(Void -> v3api.updateSamples(sessionToken, Arrays.asList(update)),
                // Then
                "Property " + propertyType.getPermId() + " is not a sample of type WELL but of type CONTROL_LAYOUT");
    }

    @Test
    public void testUpdateWithPropertyOfTypeSampleWithSampleNotAccessable()
    {
        // Given
        String adminSessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createASamplePropertyType(adminSessionToken, null);
        EntityTypePermId sampleType = createASampleType(adminSessionToken, false, propertyType);
        SampleCreation creation = new SampleCreation();
        creation.setAutoGeneratedCode(true);
        creation.setTypeId(sampleType);
        creation.setSpaceId(new SpacePermId("TEST-SPACE"));
        creation.setProperty(propertyType.getPermId(), "/MP");
        SamplePermId samplePermId = v3api.createSamples(adminSessionToken, Arrays.asList(creation)).get(0);

        String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(samplePermId);
        update.setProperty(propertyType.getPermId(), "/CISD/CL1");

        // When
        assertUserFailureException(Void -> v3api.updateSamples(sessionToken, Arrays.asList(update)),
                // Then
                "Unknown sample: /CISD/CL1");
    }

    @Test
    public void testUpdateAddSampleProperty()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createASamplePropertyType(sessionToken, null);
        EntityTypePermId sampleType = createASampleType(sessionToken, false, propertyType);
        SampleCreation creation = new SampleCreation();
        creation.setAutoGeneratedCode(true);
        creation.setTypeId(sampleType);
        SamplePermId samplePermId = v3api.createSamples(sessionToken, Arrays.asList(creation)).get(0);

        SampleUpdate update = new SampleUpdate();
        update.setSampleId(samplePermId);
        update.setProperty(propertyType.getPermId(), "200811050924898-997");

        // When
        v3api.updateSamples(sessionToken, Arrays.asList(update));

        // Then
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        fetchOptions.withHistory().withAuthor();
        Sample sample = v3api.getSamples(sessionToken, Arrays.asList(samplePermId), fetchOptions).get(samplePermId);
        assertEquals(sample.getProperties().toString(), "{" + propertyType.getPermId() + "=200811050924898-997}");
        assertEquals(sample.getSampleProperties().get(propertyType.getPermId()).length, 1);
        assertEquals(sample.getSampleProperties().get(propertyType.getPermId())[0].toString(), "Sample 200811050924898-997");

        List<HistoryEntry> history = sample.getHistory();
        assertEquals(history.size(), 1);
        assertPropertyHistory(history.get(0), propertyType.getPermId(), "200811050924898-997", sample.getModificationDate(), null);
    }

    @Test
    public void testUpdateChangeSampleProperty()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createASamplePropertyType(sessionToken, null);
        EntityTypePermId sampleType = createASampleType(sessionToken, false, propertyType);
        SampleCreation creation = new SampleCreation();
        creation.setAutoGeneratedCode(true);
        creation.setTypeId(sampleType);
        creation.setProperty(propertyType.getPermId(), "200811050919915-8");
        SamplePermId samplePermId = v3api.createSamples(sessionToken, Arrays.asList(creation)).get(0);

        SampleUpdate update = new SampleUpdate();
        update.setSampleId(samplePermId);
        update.setProperty(propertyType.getPermId(), "200811050924898-997");

        // When
        v3api.updateSamples(sessionToken, Arrays.asList(update));

        // Then
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        fetchOptions.withHistory().withAuthor();
        Sample sample = v3api.getSamples(sessionToken, Arrays.asList(samplePermId), fetchOptions).get(samplePermId);
        assertEquals(sample.getProperties().toString(), "{" + propertyType.getPermId() + "=200811050924898-997}");
        assertEquals(sample.getSampleProperties().get(propertyType.getPermId()).length, 1);
        assertEquals(sample.getSampleProperties().get(propertyType.getPermId())[0].toString(), "Sample 200811050924898-997");

        List<HistoryEntry> history = sample.getHistory();
        assertEquals(history.size(), 2);

        assertPropertyHistory(history.get(0), propertyType.getPermId(), "200811050919915-8", sample.getRegistrationDate(),
                sample.getModificationDate());
        assertPropertyHistory(history.get(1), propertyType.getPermId(), "200811050924898-997", sample.getModificationDate(), null);
    }

    @Test
    public void testUpdateRemoveSampleProperty()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createASamplePropertyType(sessionToken, null);
        EntityTypePermId sampleType = createASampleType(sessionToken, false, propertyType);
        SampleCreation creation = new SampleCreation();
        creation.setAutoGeneratedCode(true);
        creation.setTypeId(sampleType);
        creation.setProperty(propertyType.getPermId(), "200811050919915-8");
        SamplePermId samplePermId = v3api.createSamples(sessionToken, Arrays.asList(creation)).get(0);

        SampleUpdate update = new SampleUpdate();
        update.setSampleId(samplePermId);
        update.setProperty(propertyType.getPermId(), null);

        // When
        v3api.updateSamples(sessionToken, Arrays.asList(update));

        // Then
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        fetchOptions.withHistory().withAuthor();
        Sample sample = v3api.getSamples(sessionToken, Arrays.asList(samplePermId), fetchOptions).get(samplePermId);
        assertEquals(sample.getProperties().toString(), "{}");
        assertEquals(sample.getSampleProperties().toString(), "{}");

        List<HistoryEntry> history = sample.getHistory();
        assertEquals(history.size(), 1);

        assertPropertyHistory(history.get(0), propertyType.getPermId(), "200811050919915-8", sample.getRegistrationDate(),
                sample.getModificationDate());
    }

    @Test
    public void testUpdateRemoveMandatorySampleProperty()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createASamplePropertyType(sessionToken, null);
        EntityTypePermId sampleType = createASampleType(sessionToken, true, propertyType);
        SampleCreation creation = new SampleCreation();
        creation.setAutoGeneratedCode(true);
        creation.setTypeId(sampleType);
        creation.setProperty(propertyType.getPermId(), "200811050919915-8");
        SamplePermId samplePermId = v3api.createSamples(sessionToken, Arrays.asList(creation)).get(0);

        SampleUpdate update = new SampleUpdate();
        update.setSampleId(samplePermId);
        update.setProperty(propertyType.getPermId(), null);

        // When
        assertUserFailureException(Void -> v3api.updateSamples(sessionToken, Arrays.asList(update)),
                "Property " + propertyType + " of entity type " + sampleType.getPermId()
                        + " can not be deleted because it is mandatory.");
    }

    @Test
    public void testUpdateDatePropertyWithTimestamp()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.DATE);
        EntityTypePermId sampleType = createASampleType(sessionToken, true, propertyType);
        SampleCreation creation = new SampleCreation();
        creation.setAutoGeneratedCode(true);
        creation.setTypeId(sampleType);
        creation.setProperty(propertyType.getPermId(), "2008-02-23");
        SamplePermId samplePermId = v3api.createSamples(sessionToken, Arrays.asList(creation)).get(0);

        SampleUpdate update = new SampleUpdate();
        update.setSampleId(samplePermId);
        update.setProperty(propertyType.getPermId(), "2020-08-31 19:23:34");

        // When
        assertUserFailureException(Void -> v3api.updateSamples(sessionToken, Arrays.asList(update)),
                "Date value '2020-08-31 19:23:34' has improper format.");
    }

    @Test
    public void testUpdateWithSystemProperty()
    {
        String systemPropertyName = "$PLATE_GEOMETRY";
        String systemPropertyValue = "384_WELLS_16X24";

        String simplePropertyCode = "PLATE_GEOMETRY";
        String simplePropertValue = "I'm just random";

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        createNewPropertyType(sessionToken, "MASTER_PLATE", simplePropertyCode);

        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_WITH_SYS_PROPERTY");
        creation.setTypeId(new EntityTypePermId("MASTER_PLATE"));
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setProperty(systemPropertyName, "96_WELLS_8X12");
        creation.setProperty(simplePropertyCode, "initial value");

        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken,
                Arrays.asList(creation));
        ISampleId sampleId = sampleIds.get(0);

        SampleUpdate update = new SampleUpdate();
        update.setSampleId(sampleId);
        update.setProperty(systemPropertyName, systemPropertyValue);
        update.setProperty(simplePropertyCode, simplePropertValue);

        v3api.updateSamples(sessionToken, Arrays.asList(update));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, sampleIds, fetchOptions);

        Sample foundSample = map.get(sampleId);
        assertEquals(foundSample.getProperty(systemPropertyName), systemPropertyValue);
        assertEquals(foundSample.getProperty(simplePropertyCode), simplePropertValue);
    }

    @Test
    public void testUpdateWithContainer()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation containerCreation = masterPlateCreation("CISD", "TEST_CONTAINER");
        SampleCreation componentCreation = wellCreation("CISD", "TEST_COMPONENT");

        List<SamplePermId> ids = v3api.createSamples(sessionToken, Arrays.asList(containerCreation, componentCreation));

        SamplePermId containerId = ids.get(0);
        SamplePermId componentId = ids.get(1);

        SampleUpdate updateComponent = new SampleUpdate();
        updateComponent.setSampleId(componentId);
        updateComponent.setContainerId(containerId);

        v3api.updateSamples(sessionToken, Arrays.asList(updateComponent));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withContainerUsing(fetchOptions);
        fetchOptions.withComponentsUsing(fetchOptions);

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, ids, fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        AssertionUtil.assertCollectionSize(samples, 2);

        Sample container = samples.get(0);
        Sample component = samples.get(1);

        assertSampleIdentifier(container, "/CISD/TEST_CONTAINER");
        assertCollectionContainsOnly(container.getComponents(), component);

        assertSampleIdentifier(component, "/CISD/TEST_CONTAINER:TEST_COMPONENT");
        assertEquals(component.getContainer(), container);
    }

    @Test
    public void testUpdateWithContainerUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISampleId containerId = new SamplePermId("200902091219327-1025");
        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(new SamplePermId("200902091250077-1060"));
        update.setContainerId(containerId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.updateSamples(sessionToken, Arrays.asList(update));
            }
        }, containerId, patternContains("updating relation sample-containers (1/1)", toDblQuotes("'identifier' : '/TEST-SPACE/NOE/CP-TEST-4'")));
    }

    @Test
    public void testUpdateWithContainerNonexistent()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISampleId containerId = new SamplePermId("IDONTEXIST");
        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(new SamplePermId("200902091250077-1060"));
        update.setContainerId(containerId);

        assertObjectNotFoundException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.updateSamples(sessionToken, Arrays.asList(update));
            }
        }, containerId, patternContains("updating relation sample-containers (1/1)", toDblQuotes("'identifier' : '/TEST-SPACE/NOE/CP-TEST-4'")));
    }

    @Test
    public void testUpdateWithContainerCircularDependency()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(new SamplePermId("200811050919915-8"));
        update.setContainerId(new SamplePermId("200811050919915-9"));

        assertUserFailureException(new IDelegatedAction()
                                   {
                                       @Override
                                       public void execute()
                                       {
                                           v3api.updateSamples(sessionToken, Arrays.asList(update));
                                       }
                                   }, "/CISD/A01:CL1 (perm id: 200811050919915-8) cannot be it's own container",
                patternContains("verifying (1/1)", toDblQuotes("'identifier' : '/CISD/A01:CL1'")));
    }

    @Test
    public void testUpdateWithContainerViolatingBusinessRules()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(new SampleIdentifier("/MP"));
        update.setContainerId(new SampleIdentifier("/CISD/3V-125"));

        assertUserFailureException(new IDelegatedAction()
                                   {
                                       @Override
                                       public void execute()
                                       {
                                           v3api.updateSamples(sessionToken, Arrays.asList(update));
                                       }
                                   }, "The database instance sample '/3V-125:MP' can not be component in the space sample '/CISD/3V-125",
                patternContains("verifying (1/1)", toDblQuotes("'identifier' : '/3V-125:MP'")));
    }

    @Test
    public void testUpdateWithComponentsSetAddRemove()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation component1Creation = masterPlateCreation("CISD", "TEST_COMPONENT_1");
        SampleCreation component2Creation = masterPlateCreation("CISD", "TEST_COMPONENT_2");
        SampleCreation component3Creation = masterPlateCreation("CISD", "TEST_COMPONENT_3");
        component1Creation.setCreationId(new CreationId("COMPONENT_1"));
        component2Creation.setCreationId(new CreationId("COMPONENT_2"));
        component3Creation.setCreationId(new CreationId("COMPONENT_3"));

        SampleCreation container1Creation = masterPlateCreation("CISD", "TEST_CONTAINER_1");
        SampleCreation container2Creation = masterPlateCreation("CISD", "TEST_CONTAINER_2");
        container1Creation.setComponentIds(Arrays.asList(new CreationId("COMPONENT_3")));
        container2Creation.setComponentIds(Arrays.asList(new CreationId("COMPONENT_1"), new CreationId("COMPONENT_2")));

        List<SamplePermId> ids =
                v3api.createSamples(sessionToken,
                        Arrays.asList(container1Creation, container2Creation, component1Creation, component2Creation, component3Creation));

        SamplePermId container1Id = ids.get(0);
        SamplePermId container2Id = ids.get(1);
        SamplePermId component1Id = ids.get(2);
        SamplePermId component2Id = ids.get(3);
        SamplePermId component3Id = ids.get(4);

        SampleUpdate updateContainer1 = new SampleUpdate();
        updateContainer1.setSampleId(container1Id);
        // change from [component3] to [component2]
        updateContainer1.getComponentIds().set(component2Id);

        SampleUpdate updateContainer2 = new SampleUpdate();
        updateContainer2.setSampleId(container2Id);
        // change from [component1, component2] to [component1, component3]
        updateContainer2.getComponentIds().remove(component2Id);
        updateContainer2.getComponentIds().add(component3Id);
        // check that adding a component twice does not break anything
        updateContainer2.getComponentIds().add(component1Id);

        v3api.updateSamples(sessionToken, Arrays.asList(updateContainer1, updateContainer2));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withContainerUsing(fetchOptions);
        fetchOptions.withComponentsUsing(fetchOptions);

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, ids, fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        AssertionUtil.assertCollectionSize(samples, 5);

        Sample container1 = samples.get(0);
        Sample container2 = samples.get(1);
        Sample component1 = samples.get(2);
        Sample component2 = samples.get(3);
        Sample component3 = samples.get(4);

        assertSampleIdentifier(container1, "/CISD/TEST_CONTAINER_1");
        assertCollectionContainsOnly(container1.getComponents(), component2);

        assertSampleIdentifier(container2, "/CISD/TEST_CONTAINER_2");
        assertCollectionContainsOnly(container2.getComponents(), component1, component3);

        assertSampleIdentifier(component1, "/CISD/TEST_CONTAINER_2:TEST_COMPONENT_1");
        assertEquals(component1.getContainer(), container2);

        assertSampleIdentifier(component2, "/CISD/TEST_CONTAINER_1:TEST_COMPONENT_2");
        assertEquals(component2.getContainer(), container1);

        assertSampleIdentifier(component3, "/CISD/TEST_CONTAINER_2:TEST_COMPONENT_3");
        assertEquals(component3.getContainer(), container2);
    }

    @Test
    public void testUpdateWithComponentsAndIndexCheck()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation containerCreation = masterPlateCreation("CISD", "TEST_CONTAINER");
        SampleCreation componentCreation = wellCreation("CISD", "TEST_COMPONENT");

        List<SamplePermId> ids = v3api.createSamples(sessionToken, Arrays.asList(containerCreation, componentCreation));

        SamplePermId containerId = ids.get(0);
        SamplePermId componentId = ids.get(1);

        SampleUpdate containerUpdate = new SampleUpdate();
        containerUpdate.setSampleId(containerId);
        containerUpdate.getComponentIds().add(componentId);

        v3api.updateSamples(sessionToken, Arrays.asList(containerUpdate));

        assertSamplesExists(containerId.getPermId(), componentId.getPermId());
    }

    @Test
    public void testUpdateWithComponentsUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISampleId componentId = new SamplePermId("200902091219327-1025");
        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(new SamplePermId("200902091250077-1060"));
        update.getComponentIds().add(componentId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.updateSamples(sessionToken, Arrays.asList(update));
            }
        }, componentId, patternContains("updating relation sample-components (1/1)", toDblQuotes("'identifier' : '/TEST-SPACE/NOE/CP-TEST-4'")));
    }

    @Test
    public void testUpdateWithComponentsNonexistent()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISampleId componentId = new SamplePermId("IDONTEXIST");
        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(new SamplePermId("200902091250077-1060"));
        update.getComponentIds().add(componentId);

        assertObjectNotFoundException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.updateSamples(sessionToken, Arrays.asList(update));
            }
        }, componentId, patternContains("updating relation sample-components (1/1)", toDblQuotes("'identifier' : '/TEST-SPACE/NOE/CP-TEST-4'")));
    }

    @Test
    public void testUpdateWithComponentsCircularDependency()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(new SamplePermId("200811050919915-9"));
        update.getComponentIds().add(new SamplePermId("200811050919915-8"));

        assertUserFailureException(new IDelegatedAction()
                                   {
                                       @Override
                                       public void execute()
                                       {
                                           v3api.updateSamples(sessionToken, Arrays.asList(update));
                                       }
                                   }, "/CISD/CL1:A01 (perm id: 200811050919915-9) cannot be it's own container",
                patternContains("verifying (1/1)", toDblQuotes("'identifier' : '/CISD/CL1:A01'")));
    }

    @Test
    public void testUpdateWithComponentsViolatingBusinessRules()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(new SampleIdentifier("/CISD/3V-125"));
        update.getComponentIds().add(new SampleIdentifier("/MP"));

        assertUserFailureException(new IDelegatedAction()
                                   {
                                       @Override
                                       public void execute()
                                       {
                                           v3api.updateSamples(sessionToken, Arrays.asList(update));
                                       }
                                   }, "Sample '/CISD/3V-125' can not be a space sample because of a component database instance sample '/3V-125:MP",
                patternContains("verifying (1/1)", toDblQuotes("'identifier' : '/CISD/3V-125'")));
    }

    @Test
    public void testUpdateWithParentSetAddRemove()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation parent1Creation = masterPlateCreation("CISD", "TEST_PARENT_1");
        SampleCreation parent2Creation = masterPlateCreation("CISD", "TEST_PARENT_2");
        SampleCreation parent3Creation = masterPlateCreation("CISD", "TEST_PARENT_3");
        parent1Creation.setCreationId(new CreationId("PARENT_1"));
        parent2Creation.setCreationId(new CreationId("PARENT_2"));
        parent3Creation.setCreationId(new CreationId("PARENT_3"));

        SampleCreation child1Creation = masterPlateCreation("CISD", "TEST_CHILD_1");
        SampleCreation child2Creation = masterPlateCreation("CISD", "TEST_CHILD_2");
        child1Creation.setParentIds(Arrays.asList(new CreationId("PARENT_3")));
        child2Creation.setParentIds(Arrays.asList(new CreationId("PARENT_1"), new CreationId("PARENT_2")));

        List<SamplePermId> ids =
                v3api.createSamples(sessionToken, Arrays.asList(child1Creation, child2Creation, parent1Creation, parent2Creation, parent3Creation));

        SamplePermId child1Id = ids.get(0);
        SamplePermId child2Id = ids.get(1);
        SamplePermId parent1Id = ids.get(2);
        SamplePermId parent2Id = ids.get(3);
        SamplePermId parent3Id = ids.get(4);

        SampleUpdate updateChild1 = new SampleUpdate();
        updateChild1.setSampleId(child1Id);
        // change from [parent3] to [parent1, parent2]
        updateChild1.getParentIds().set(parent1Id, parent2Id);

        SampleUpdate updateChild2 = new SampleUpdate();
        updateChild2.setSampleId(child2Id);
        // change from [parent1, parent2] to [parent2, parent3]
        updateChild2.getParentIds().remove(parent1Id);
        updateChild2.getParentIds().add(parent3Id);
        // check that adding a parent twice does not break anything
        updateChild2.getParentIds().add(parent2Id);

        v3api.updateSamples(sessionToken, Arrays.asList(updateChild1, updateChild2));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withParentsUsing(fetchOptions);
        fetchOptions.withChildrenUsing(fetchOptions);

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, ids, fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        AssertionUtil.assertCollectionSize(samples, 5);

        Sample child1 = samples.get(0);
        Sample child2 = samples.get(1);
        Sample parent1 = samples.get(2);
        Sample parent2 = samples.get(3);
        Sample parent3 = samples.get(4);

        assertSampleIdentifier(child1, "/CISD/TEST_CHILD_1");
        assertCollectionContainsOnly(child1.getParents(), parent1, parent2);

        assertSampleIdentifier(child2, "/CISD/TEST_CHILD_2");
        assertCollectionContainsOnly(child2.getParents(), parent2, parent3);

        assertSampleIdentifier(parent1, "/CISD/TEST_PARENT_1");
        assertCollectionContainsOnly(parent1.getChildren(), child1);

        assertSampleIdentifier(parent2, "/CISD/TEST_PARENT_2");
        assertCollectionContainsOnly(parent2.getChildren(), child1, child2);

        assertSampleIdentifier(parent3, "/CISD/TEST_PARENT_3");
        assertCollectionContainsOnly(parent3.getChildren(), child2);
    }

    @Test
    public void testUpdateParentAnnotations()
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

        SampleCreation childCreation = masterPlateCreation("CISD", "TEST_CHILD");
        childCreation.setParentIds(Arrays.asList(creationId1, creationId2, creationId3));
        childCreation.relationship(creationId1)
                .addParentAnnotation("type", "father").addChildAnnotation("type", "daughter");
        childCreation.relationship(creationId2).addParentAnnotation("color", "red");
        childCreation.relationship(creationId3)
                .addParentAnnotation("type", "mother").addChildAnnotation("type", "daughter");

        List<SamplePermId> ids = v3api.createSamples(sessionToken, Arrays.asList(childCreation, parent1Creation,
                parent2Creation, parent3Creation));

        SamplePermId childId = ids.get(0);
        SamplePermId parent1Id = ids.get(1);
        SamplePermId parent2Id = ids.get(2);
        SamplePermId parent3Id = ids.get(3);
        assertAnnotations(sessionToken, "[type=father]", "[type=daughter]", parent1Id, childId);
        assertAnnotations(sessionToken, "[color=red]", "[]", parent2Id, childId);
        assertAnnotations(sessionToken, "[type=mother]", "[type=daughter]", parent3Id, childId);

        SampleUpdate updateChild = new SampleUpdate();
        updateChild.setSampleId(childId);
        SamplePermId parent4Id = new SamplePermId("200811050924898-997");
        updateChild.getParentIds().add(parent4Id);
        updateChild.relationship(parent1Id).addParentAnnotation("name", "beta").removeChildAnnotations("type");
        updateChild.relationship(parent2Id).removeParentAnnotations("color").addChildAnnotation("type", "daughter");
        updateChild.relationship(parent3Id).setRelationship(new Relationship().addParentAnnotation("name", "alpha"));
        updateChild.relationship(parent4Id).addChildAnnotation("type", "son");

        // When
        v3api.updateSamples(sessionToken, Arrays.asList(updateChild));

        // Then
        assertAnnotations(sessionToken, "[name=beta, type=father]", "[]", parent1Id, childId);
        assertAnnotations(sessionToken, "[]", "[type=daughter]", parent2Id, childId);
        assertAnnotations(sessionToken, "[name=alpha]", "[]", parent3Id, childId);
        assertAnnotations(sessionToken, "[]", "[type=son]", parent4Id, childId);
    }

    @Test
    public void testUpdateChildAnnotations()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleCreation child1Creation = masterPlateCreation("CISD", "TEST_CHILD_1");
        SampleCreation child2Creation = masterPlateCreation("CISD", "TEST_CHILD_2");
        CreationId creationId1 = new CreationId("CHILD_1");
        child1Creation.setCreationId(creationId1);
        CreationId creationId2 = new CreationId("CHILD_2");
        child2Creation.setCreationId(creationId2);

        SampleCreation parentCreation = masterPlateCreation("CISD", "TEST_PARENT");
        parentCreation.setChildIds(Arrays.asList(creationId1, creationId2));
        parentCreation.relationship(creationId1)
                .addParentAnnotation("type", "father").addChildAnnotation("type", "daughter");
        parentCreation.relationship(creationId2).addParentAnnotation("color", "red");

        List<SamplePermId> ids = v3api.createSamples(sessionToken, Arrays.asList(parentCreation, child1Creation,
                child2Creation));

        SamplePermId parentId = ids.get(0);
        SamplePermId child1Id = ids.get(1);
        SamplePermId child2Id = ids.get(2);
        assertAnnotations(sessionToken, "[type=father]", "[type=daughter]", parentId, child1Id);
        assertAnnotations(sessionToken, "[color=red]", "[]", parentId, child2Id);

        SampleUpdate updateParent = new SampleUpdate();
        updateParent.setSampleId(parentId);
        SamplePermId child3Id = new SamplePermId("200811050924898-997");
        updateParent.getChildIds().add(child3Id);
        updateParent.relationship(child1Id).addParentAnnotation("name", "beta").removeChildAnnotations("type");
        updateParent.relationship(child2Id).removeParentAnnotations("color").addChildAnnotation("type", "daughter");
        updateParent.relationship(child3Id).addChildAnnotation("type", "son");

        // When
        v3api.updateSamples(sessionToken, Arrays.asList(updateParent));

        // Then
        assertAnnotations(sessionToken, "[name=beta, type=father]", "[]", parentId, child1Id);
        assertAnnotations(sessionToken, "[]", "[type=daughter]", parentId, child2Id);
        assertAnnotations(sessionToken, "[]", "[type=son]", parentId, child3Id);
    }

    @Test
    public void testUpdateWithParentUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISampleId parentId = new SamplePermId("200902091219327-1025");
        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(new SamplePermId("200902091250077-1060"));
        update.getParentIds().add(parentId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.updateSamples(sessionToken, Arrays.asList(update));
            }
        }, parentId, patternContains("updating relation sample-parents (1/1)", toDblQuotes("'identifier' : '/TEST-SPACE/NOE/CP-TEST-4'")));
    }

    @Test
    public void testUpdateWithParentNonexistent()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISampleId parentId = new SamplePermId("IDONTEXIST");
        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(new SamplePermId("200902091250077-1060"));
        update.getParentIds().add(parentId);

        assertObjectNotFoundException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.updateSamples(sessionToken, Arrays.asList(update));
            }
        }, parentId, patternContains("updating relation sample-parents (1/1)", toDblQuotes("'identifier' : '/TEST-SPACE/NOE/CP-TEST-4'")));
    }

    @Test
    public void testUpdateWithParentCircularDependency()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(new SamplePermId("200811050945092-976"));
        update.getParentIds().add(new SamplePermId("200811050946559-982"));

        assertUserFailureException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.updateSamples(sessionToken, Arrays.asList(update));
            }
        }, "Circular dependency found: /CISD/3V-125");
    }

    @Test
    public void testUpdateWithParentViolatingBusinessRules()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(new SampleIdentifier("/MP"));
        update.getParentIds().add(new SampleIdentifier("/CISD/3V-125"));

        assertUserFailureException(new IDelegatedAction()
                                   {
                                       @Override
                                       public void execute()
                                       {
                                           v3api.updateSamples(sessionToken, Arrays.asList(update));
                                       }
                                   }, "The database instance sample '/MP' can not be child of the space sample '/CISD/3V-125'",
                patternContains("verifying (1/1)", toDblQuotes("'identifier' : '/MP'")));
    }

    @Test
    public void testUpdateWithChildSetAddRemove()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation child1Creation = masterPlateCreation("CISD", "TEST_CHILD_1");
        SampleCreation child2Creation = masterPlateCreation("CISD", "TEST_CHILD_2");
        SampleCreation child3Creation = masterPlateCreation("CISD", "TEST_CHILD_3");
        child1Creation.setCreationId(new CreationId("CHILD_1"));
        child2Creation.setCreationId(new CreationId("CHILD_2"));
        child3Creation.setCreationId(new CreationId("CHILD_3"));

        SampleCreation parent1Creation = masterPlateCreation("CISD", "TEST_PARENT_1");
        SampleCreation parent2Creation = masterPlateCreation("CISD", "TEST_PARENT_2");
        parent1Creation.setChildIds(Arrays.asList(new CreationId("CHILD_3")));
        parent2Creation.setChildIds(Arrays.asList(new CreationId("CHILD_1"), new CreationId("CHILD_2")));

        List<SamplePermId> ids =
                v3api.createSamples(sessionToken, Arrays.asList(parent1Creation, parent2Creation, child1Creation, child2Creation, child3Creation));

        SamplePermId parent1Id = ids.get(0);
        SamplePermId parent2Id = ids.get(1);
        SamplePermId child1Id = ids.get(2);
        SamplePermId child2Id = ids.get(3);
        SamplePermId child3Id = ids.get(4);

        SampleUpdate updateParent1 = new SampleUpdate();
        updateParent1.setSampleId(parent1Id);
        // change from [child3] to [child1, child2]
        updateParent1.getChildIds().set(child1Id, child2Id);

        SampleUpdate updateParent2 = new SampleUpdate();
        updateParent2.setSampleId(parent2Id);
        // change from [child1, child2] to [child2, child3]
        updateParent2.getChildIds().remove(child1Id);
        updateParent2.getChildIds().add(child3Id);
        // check that adding a child twice does not break anything
        updateParent2.getChildIds().add(child2Id);

        v3api.updateSamples(sessionToken, Arrays.asList(updateParent1, updateParent2));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withParentsUsing(fetchOptions);
        fetchOptions.withChildrenUsing(fetchOptions);

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, ids, fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        AssertionUtil.assertCollectionSize(samples, 5);

        Sample parent1 = samples.get(0);
        Sample parent2 = samples.get(1);
        Sample child1 = samples.get(2);
        Sample child2 = samples.get(3);
        Sample child3 = samples.get(4);

        assertSampleIdentifier(parent1, "/CISD/TEST_PARENT_1");
        assertCollectionContainsOnly(parent1.getChildren(), child1, child2);

        assertSampleIdentifier(parent2, "/CISD/TEST_PARENT_2");
        assertCollectionContainsOnly(parent2.getChildren(), child2, child3);

        assertSampleIdentifier(child1, "/CISD/TEST_CHILD_1");
        assertCollectionContainsOnly(child1.getParents(), parent1);

        assertSampleIdentifier(child2, "/CISD/TEST_CHILD_2");
        assertCollectionContainsOnly(child2.getParents(), parent1, parent2);

        assertSampleIdentifier(child3, "/CISD/TEST_CHILD_3");
        assertCollectionContainsOnly(child3.getParents(), parent2);
    }

    @Test
    public void testUpdateWithChildUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISampleId childId = new SamplePermId("200902091219327-1025");
        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(new SamplePermId("200902091250077-1060"));
        update.getChildIds().add(childId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.updateSamples(sessionToken, Arrays.asList(update));
            }
        }, childId, patternContains("updating relation sample-children (1/1)", toDblQuotes("'identifier' : '/TEST-SPACE/NOE/CP-TEST-4'")));
    }

    @Test
    public void testUpdateWithChildNonexistent()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final ISampleId childId = new SamplePermId("IDONTEXIST");
        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(new SamplePermId("200902091250077-1060"));
        update.getChildIds().add(childId);

        assertObjectNotFoundException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.updateSamples(sessionToken, Arrays.asList(update));
            }
        }, childId, patternContains("updating relation sample-children (1/1)", toDblQuotes("'identifier' : '/TEST-SPACE/NOE/CP-TEST-4'")));
    }

    @Test
    public void testUpdateWithChildCircularDependency()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(new SamplePermId("200811050946559-982"));
        update.getChildIds().add(new SamplePermId("200811050945092-976"));

        assertUserFailureException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.updateSamples(sessionToken, Arrays.asList(update));
            }
        }, "Circular dependency found: /CISD/3VCP8");
    }

    @Test
    public void testUpdateWithChildViolatingBusinessRules()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleUpdate update = new SampleUpdate();
        update.setSampleId(new SampleIdentifier("/CISD/3V-125"));
        update.getChildIds().add(new SampleIdentifier("/MP"));

        assertUserFailureException(new IDelegatedAction()
                                   {
                                       @Override
                                       public void execute()
                                       {
                                           v3api.updateSamples(sessionToken, Arrays.asList(update));
                                       }
                                   }, "Sample '/CISD/3V-125' can not be a space sample because of a child database instance sample '/MP'",
                patternContains("verifying (1/1)", toDblQuotes("'identifier' : '/CISD/3V-125'")));
    }

    @Test
    public void testMoveSampleToSpaceWithSampleHavingSameCode()
    {
        // Given
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SpaceCreation space1 = new SpaceCreation();
        space1.setCode("TEST-SPACE-A");
        SpaceCreation space2 = new SpaceCreation();
        space2.setCode("TEST-SPACE-B");
        List<SpacePermId> spaceIds = v3api.createSpaces(sessionToken, Arrays.asList(space1, space2));

        SampleCreation s1 = new SampleCreation();
        s1.setCode("SAMPLE");
        s1.setTypeId(new EntityTypePermId("NORMAL", EntityKind.SAMPLE));
        s1.setSpaceId(spaceIds.get(0));
        SampleCreation s2 = new SampleCreation();
        s2.setCode("SAMPLE");
        s2.setTypeId(new EntityTypePermId("NORMAL", EntityKind.SAMPLE));
        s2.setSpaceId(spaceIds.get(1));
        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken, Arrays.asList(s1, s2));

        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sampleIds.get(0));
        sampleUpdate.setSpaceId(spaceIds.get(1));

        // When
        assertUserFailureException(Void -> v3api.updateSamples(sessionToken, Arrays.asList(sampleUpdate)),
                // Then
                "Insert/Update of sample (code: SAMPLE) failed because sample with the same code already exists.");
    }

    @Test
    public void testMoveSampleToProjectWithSampleHavingSameCode()
    {
        // Given
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SpacePermId spaceId = new SpacePermId("TEST-SPACE");
        ProjectCreation p1 = new ProjectCreation();
        p1.setCode("TEST-A");
        p1.setSpaceId(spaceId);
        ProjectCreation p2 = new ProjectCreation();
        p2.setCode("TEST-B");
        p2.setSpaceId(spaceId);
        List<ProjectPermId> projectIds = v3api.createProjects(sessionToken, Arrays.asList(p1, p2));

        SampleCreation s1 = new SampleCreation();
        s1.setCode("SAMPLE");
        s1.setTypeId(new EntityTypePermId("NORMAL", EntityKind.SAMPLE));
        s1.setSpaceId(spaceId);
        s1.setProjectId(projectIds.get(0));
        SampleCreation s2 = new SampleCreation();
        s2.setCode("SAMPLE");
        s2.setTypeId(new EntityTypePermId("NORMAL", EntityKind.SAMPLE));
        s2.setSpaceId(spaceId);
        s2.setProjectId(projectIds.get(1));

        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken, Arrays.asList(s1, s2));
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sampleIds.get(0));
        sampleUpdate.setProjectId(projectIds.get(1));

        // When
        assertUserFailureException(Void -> v3api.updateSamples(sessionToken, Arrays.asList(sampleUpdate)),
                // Then
                "Insert/Update of sample (code: SAMPLE) failed because sample with the same code already exists.");
    }

    @Test
    public void testUpdateSampleWithAdminUserInAnotherSpace()
    {
        final SamplePermId permId = new SamplePermId("200902091250077-1060");

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                String sessionToken = v3api.login(TEST_ROLE_V3, PASSWORD);

                final SampleUpdate update = new SampleUpdate();
                update.setSampleId(permId);
                v3api.updateSamples(sessionToken, Collections.singletonList(update));
            }
        }, permId);
    }

    @Test
    public void testUpdateWithAttachmentsSetAddRemove()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        AttachmentCreation attachmentCreation1 = new AttachmentCreation();
        attachmentCreation1.setFileName("test_file_1");
        attachmentCreation1.setContent(new String("test_content_1").getBytes());

        AttachmentCreation attachmentCreation2 = new AttachmentCreation();
        attachmentCreation2.setFileName("test_file_2");
        attachmentCreation2.setContent(new String("test_content_2").getBytes());

        AttachmentCreation attachmentCreation3 = new AttachmentCreation();
        attachmentCreation3.setFileName("test_file_3");
        attachmentCreation3.setContent(new String("test_content_3").getBytes());

        SampleCreation creation1 = masterPlateCreation("CISD", "SAMPLE_1_WITH_ATTACHMENTS");
        SampleCreation creation2 = masterPlateCreation("CISD", "SAMPLE_2_WITH_ATTACHMENTS");

        creation1.setAttachments(Arrays.asList(attachmentCreation3));
        creation2.setAttachments(Arrays.asList(attachmentCreation1, attachmentCreation2));

        List<SamplePermId> sampleIds =
                v3api.createSamples(sessionToken, Arrays.asList(creation1, creation2));

        SamplePermId sampleId1 = sampleIds.get(0);
        SamplePermId sampleId2 = sampleIds.get(1);

        SampleUpdate update1 = new SampleUpdate();
        update1.setSampleId(sampleId1);
        // change from [test_file_3] to [test_file_1, test_file_3]
        update1.getAttachments().set(attachmentCreation1, attachmentCreation3);

        SampleUpdate update2 = new SampleUpdate();
        update2.setSampleId(sampleId2);
        // change from [test_file_1, test_file_2] to [test_file_2, test_file_3]
        update2.getAttachments().remove(new AttachmentFileName(attachmentCreation1.getFileName()));
        update2.getAttachments().add(attachmentCreation3);

        v3api.updateSamples(sessionToken, Arrays.asList(update1, update2));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withAttachments().withContent();

        Map<ISampleId, Sample> samples = v3api.getSamples(sessionToken, sampleIds, fetchOptions);

        Map<String, Attachment> attachmentMap1 =
                assertAttachments(samples.get(sampleId1).getAttachments(), attachmentCreation1, attachmentCreation3);

        assertEquals(attachmentMap1.get(attachmentCreation1.getFileName()).getVersion(), Integer.valueOf(1));
        assertEquals(attachmentMap1.get(attachmentCreation3.getFileName()).getVersion(), Integer.valueOf(2));

        Map<String, Attachment> attachmentMap2 =
                assertAttachments(samples.get(sampleId2).getAttachments(), attachmentCreation2, attachmentCreation3);

        assertEquals(attachmentMap2.get(attachmentCreation2.getFileName()).getVersion(), Integer.valueOf(1));
        assertEquals(attachmentMap2.get(attachmentCreation3.getFileName()).getVersion(), Integer.valueOf(1));
    }

    @Test
    public void testUpdateWithTagsWithSetAddRemove()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation creation1 = masterPlateCreation("CISD", "SAMPLE_1_WITH_TAGS");
        SampleCreation creation2 = masterPlateCreation("CISD", "SAMPLE_2_WITH_TAGS");

        ITagId tag1Id = new TagCode("TEST_TAG_1");
        ITagId tag2Id = new TagCode("TEST_TAG_2");
        ITagId tag3Id = new TagCode("TEST_TAG_3");

        creation1.setTagIds(Arrays.asList(tag3Id));
        creation2.setTagIds(Arrays.asList(tag1Id, tag2Id));

        List<SamplePermId> ids =
                v3api.createSamples(sessionToken, Arrays.asList(creation1, creation2));

        SampleUpdate update1 = new SampleUpdate();
        update1.setSampleId(ids.get(0));
        // change from [tag3] to [tag1, tag3]
        update1.getTagIds().set(tag1Id, tag3Id);

        SampleUpdate update2 = new SampleUpdate();
        update2.setSampleId(ids.get(1));
        // change from [tag1, tag2] to [tag2, tag3]
        update2.getTagIds().remove(tag1Id);
        update2.getTagIds().add(tag3Id);
        // check that adding a tag twice does not break anything
        update2.getTagIds().add(tag2Id);

        v3api.updateSamples(sessionToken, Arrays.asList(update1, update2));

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withTags();

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, ids, fetchOptions);
        List<Sample> samples = new ArrayList<Sample>(map.values());

        assertCollectionSize(samples, 2);

        Sample sample1 = samples.get(0);
        Sample sample2 = samples.get(1);

        assertSampleIdentifier(sample1, "/CISD/SAMPLE_1_WITH_TAGS");
        assertTags(sample1.getTags(), "/test/TEST_TAG_1", "/test/TEST_TAG_3");

        assertSampleIdentifier(sample2, "/CISD/SAMPLE_2_WITH_TAGS");
        assertTags(sample2.getTags(), "/test/TEST_TAG_2", "/test/TEST_TAG_3");
    }

    @Test
    public void testUpdateWithMaterialProperties()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withMaterialProperties().withRegistrator();
        fetchOptions.withProperties();

        SamplePermId permId = new SamplePermId("200902091219327-1025");

        SampleUpdate update1 = new SampleUpdate();
        update1.setSampleId(permId);
        update1.setProperty("ANY_MATERIAL", "BACTERIUM-X (BACTERIUM)");

        v3api.updateSamples(sessionToken, Arrays.asList(update1));

        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, Arrays.asList(permId), fetchOptions);

        Sample sample = map.get(permId);

        assertEquals(sample.getProperties().get("BACTERIUM"), "BACTERIUM-X (BACTERIUM)");
        assertEquals(sample.getProperties().get("ANY_MATERIAL"), "BACTERIUM-X (BACTERIUM)");

        Map<String, Material> materialProperties = sample.getMaterialProperties();

        Material updatedBacterium = materialProperties.get("ANY_MATERIAL");
        assertEquals(updatedBacterium.getPermId(), new MaterialPermId("BACTERIUM-X", "BACTERIUM"));
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testUpdateWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String sessionToken = v3api.login(user.getUserId(), PASSWORD);

        SampleUpdate update = new SampleUpdate();
        update.setSampleId(new SampleIdentifier("/TEST-SPACE/TEST-PROJECT/EV-TEST"));
        update.setExperimentId(new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));
        update.setProperty("COMMENT", "test comment");

        AttachmentCreation attachment = new AttachmentCreation();
        attachment.setFileName("test_file_name");
        attachment.setContent("test_content".getBytes());
        update.getAttachments().add(attachment);

        if (user.isDisabledProjectUser())
        {
            assertAuthorizationFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateSamples(sessionToken, Arrays.asList(update));
                }
            });
        } else if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            v3api.updateSamples(sessionToken, Arrays.asList(update));
        } else
        {
            assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateSamples(sessionToken, Arrays.asList(update));
                }
            }, update.getSampleId());
        }
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleUpdate update = new SampleUpdate();
        update.setSampleId(new SampleIdentifier("/CISD/NEMO/CP-TEST-1"));

        SampleUpdate update2 = new SampleUpdate();
        update2.setSampleId(new SamplePermId("201206191219327-1055"));

        v3api.updateSamples(sessionToken, Arrays.asList(update, update2));

        assertAccessLog(
                "update-samples  SAMPLE_UPDATES('[SampleUpdate[sampleId=/CISD/NEMO/CP-TEST-1], SampleUpdate[sampleId=201206191219327-1055]]')");
    }

    @Test
    public void testFreeze()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleIdentifier sampleId1 = new SampleIdentifier("/CISD/MP002-1:B11");
        SampleUpdate update1 = new SampleUpdate();
        update1.setSampleId(sampleId1);
        update1.freeze();
        SampleIdentifier sampleId2 = new SampleIdentifier("/CISD/MP002-1:B12");
        SampleUpdate update2 = new SampleUpdate();
        update2.setSampleId(sampleId2);
        update2.freezeForComponents();
        SampleIdentifier sampleId3 = new SampleIdentifier("/CISD/MP002-1:B13");
        SampleUpdate update3 = new SampleUpdate();
        update3.setSampleId(sampleId3);
        update3.freezeForChildren();
        SampleIdentifier sampleId4 = new SampleIdentifier("/CISD/MP002-1:B14");
        SampleUpdate update4 = new SampleUpdate();
        update4.setSampleId(sampleId4);
        update4.freezeForParents();
        SampleIdentifier sampleId5 = new SampleIdentifier("/CISD/MP002-1:B15");
        SampleUpdate update5 = new SampleUpdate();
        update5.setSampleId(sampleId5);
        update5.freezeForDataSets();

        // When
        v3api.updateSamples(sessionToken, Arrays.asList(update1, update2, update3, update4, update5));

        // Then
        Map<ISampleId, Sample> samples = v3api.getSamples(sessionToken,
                Arrays.asList(sampleId1, sampleId2, sampleId3, sampleId4, sampleId5), new SampleFetchOptions());
        Sample sample1 = samples.get(sampleId1);
        assertEquals(sample1.getIdentifier().getIdentifier(), sampleId1.getIdentifier());
        assertEquals(sample1.isFrozen(), true);
        assertEquals(sample1.isFrozenForComponents(), false);
        assertEquals(sample1.isFrozenForChildren(), false);
        assertEquals(sample1.isFrozenForParents(), false);
        assertEquals(sample1.isFrozenForDataSets(), false);
        assertFreezingEvent(TEST_USER, sample1.getIdentifier().getIdentifier(), EntityType.SAMPLE,
                new FreezingFlags().freeze().freeze());
        Sample sample2 = samples.get(sampleId2);
        assertEquals(sample2.getIdentifier().getIdentifier(), sampleId2.getIdentifier());
        assertEquals(sample2.isFrozen(), true);
        assertEquals(sample2.isFrozenForComponents(), true);
        assertEquals(sample2.isFrozenForChildren(), false);
        assertEquals(sample2.isFrozenForParents(), false);
        assertEquals(sample2.isFrozenForDataSets(), false);
        assertFreezingEvent(TEST_USER, sample2.getIdentifier().getIdentifier(), EntityType.SAMPLE,
                new FreezingFlags().freeze().freezeForComponents());
        Sample sample3 = samples.get(sampleId3);
        assertEquals(sample3.getIdentifier().getIdentifier(), sampleId3.getIdentifier());
        assertEquals(sample3.isFrozen(), true);
        assertEquals(sample3.isFrozenForComponents(), false);
        assertEquals(sample3.isFrozenForChildren(), true);
        assertEquals(sample3.isFrozenForParents(), false);
        assertEquals(sample3.isFrozenForDataSets(), false);
        assertFreezingEvent(TEST_USER, sample3.getIdentifier().getIdentifier(), EntityType.SAMPLE,
                new FreezingFlags().freeze().freezeForChildren());
        Sample sample4 = samples.get(sampleId4);
        assertEquals(sample4.getIdentifier().getIdentifier(), sampleId4.getIdentifier());
        assertEquals(sample4.isFrozen(), true);
        assertEquals(sample4.isFrozenForComponents(), false);
        assertEquals(sample4.isFrozenForChildren(), false);
        assertEquals(sample4.isFrozenForParents(), true);
        assertEquals(sample4.isFrozenForDataSets(), false);
        assertFreezingEvent(TEST_USER, sample4.getIdentifier().getIdentifier(), EntityType.SAMPLE,
                new FreezingFlags().freeze().freezeForParents());
        Sample sample5 = samples.get(sampleId5);
        assertEquals(sample5.getIdentifier().getIdentifier(), sampleId5.getIdentifier());
        assertEquals(sample5.isFrozen(), true);
        assertEquals(sample5.isFrozenForComponents(), false);
        assertEquals(sample5.isFrozenForChildren(), false);
        assertEquals(sample5.isFrozenForParents(), false);
        assertEquals(sample5.isFrozenForDataSets(), true);
        assertFreezingEvent(TEST_USER, sample5.getIdentifier().getIdentifier(), EntityType.SAMPLE,
                new FreezingFlags().freeze().freezeForDataSets());
    }

    @Test
    public void testFreezing()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleIdentifier sampleId = new SampleIdentifier("/CISD/C1");
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(sampleId);
        update.freeze();
        v3api.updateSamples(sessionToken, Arrays.asList(update));
        SampleUpdate update2 = new SampleUpdate();
        update2.setSampleId(sampleId);
        update2.setProperty("DESCRIPTION", "a test");

        // When
        assertUserFailureException(Void -> v3api.updateSamples(sessionToken, Arrays.asList(update2)),
                // Then
                "ERROR: Operation INSERT PROPERTY is not allowed because sample C1 is frozen.");
    }

    @Test
    public void testFreezingForComponents()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleIdentifier sampleId = new SampleIdentifier("/CISD/C1");
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(sampleId);
        update.freezeForComponents();
        v3api.updateSamples(sessionToken, Arrays.asList(update));
        SampleUpdate update2 = new SampleUpdate();
        update2.setSampleId(sampleId);
        update2.getComponentIds().remove(new SampleIdentifier("/CISD/C1:C01"));

        // When
        assertUserFailureException(Void -> v3api.updateSamples(sessionToken, Arrays.asList(update2)),
                // Then
                "ERROR: Operation REMOVE CONTAINER is not allowed because sample C1 is frozen for sample C01.");
    }

    @Test
    public void testFreezingForComponentDeletions()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleIdentifier sampleId = new SampleIdentifier("/CISD/C1");
        SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        sampleCreation.setSpaceId(new SpacePermId("CISD"));
        sampleCreation.setCode("SAMPLE");
        sampleCreation.setContainerId(sampleId);
        SamplePermId compId = v3api.createSamples(sessionToken, Arrays.asList(sampleCreation)).get(0);
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(sampleId);
        update.freezeForComponents();
        v3api.updateSamples(sessionToken, Arrays.asList(update));
        SampleDeletionOptions deletionOptions = new SampleDeletionOptions();
        deletionOptions.setReason("test");

        // When
        assertUserFailureException(Void -> v3api.deleteSamples(sessionToken, Arrays.asList(compId), deletionOptions),
                // Then
                "ERROR: Operation DELETE SAMPLE COMPONENT is not allowed because sample C1 is frozen.");
    }

    @Test
    public void testFreezingForChildren()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleIdentifier sampleId = new SampleIdentifier("/CISD/C1");
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(sampleId);
        update.freezeForChildren();
        v3api.updateSamples(sessionToken, Arrays.asList(update));
        SampleUpdate update2 = new SampleUpdate();
        update2.setSampleId(sampleId);
        update2.getChildIds().add(new SampleIdentifier("/CISD/C2"));

        // When
        assertUserFailureException(Void -> v3api.updateSamples(sessionToken, Arrays.asList(update2)),
                // Then
                "ERROR: Operation INSERT is not allowed because sample C1 or C2 is frozen.");
    }

    @Test
    public void testFreezingForChildrenDeletions()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleIdentifier sampleId = new SampleIdentifier("/CISD/C1");
        SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        sampleCreation.setSpaceId(new SpacePermId("CISD"));
        sampleCreation.setCode("SAMPLE");
        sampleCreation.setParentIds(Arrays.asList(sampleId));
        SamplePermId childId = v3api.createSamples(sessionToken, Arrays.asList(sampleCreation)).get(0);
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(sampleId);
        update.freezeForChildren();
        v3api.updateSamples(sessionToken, Arrays.asList(update));
        SampleDeletionOptions deletionOptions = new SampleDeletionOptions();
        deletionOptions.setReason("test");

        // When
        assertUserFailureException(Void -> v3api.deleteSamples(sessionToken, Arrays.asList(childId), deletionOptions),
                // Then
                "ERROR: Operation DELETE SAMPLE CHILD is not allowed because sample C1 is frozen.");
    }

    @Test
    public void testFreezingForParents()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleIdentifier sampleId = new SampleIdentifier("/CISD/C1");
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(sampleId);
        update.freezeForParents();
        v3api.updateSamples(sessionToken, Arrays.asList(update));
        SampleUpdate update2 = new SampleUpdate();
        update2.setSampleId(sampleId);
        update2.getParentIds().add(new SampleIdentifier("/CISD/C2"));

        // When
        assertUserFailureException(Void -> v3api.updateSamples(sessionToken, Arrays.asList(update2)),
                // Then
                "ERROR: Operation INSERT is not allowed because sample C2 or C1 is frozen.");
    }

    @Test
    public void testFreezingForParentsDeletions()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleIdentifier sampleId = new SampleIdentifier("/CISD/C1");
        SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        sampleCreation.setSpaceId(new SpacePermId("CISD"));
        sampleCreation.setCode("SAMPLE");
        sampleCreation.setChildIds(Arrays.asList(sampleId));
        SamplePermId parentId = v3api.createSamples(sessionToken, Arrays.asList(sampleCreation)).get(0);
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(sampleId);
        update.freezeForParents();
        v3api.updateSamples(sessionToken, Arrays.asList(update));
        SampleDeletionOptions deletionOptions = new SampleDeletionOptions();
        deletionOptions.setReason("test");

        // When
        assertUserFailureException(Void -> v3api.deleteSamples(sessionToken, Arrays.asList(parentId), deletionOptions),
                // Then
                "ERROR: Operation DELETE SAMPLE PARENT is not allowed because sample C1 is frozen.");
    }

    @Test
    public void testFreezingForDataSets()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleIdentifier sampleId = new SampleIdentifier("/CISD/C1");
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(sampleId);
        update.freezeForDataSets();
        v3api.updateSamples(sessionToken, Arrays.asList(update));
        DataSetCreation dataSetCreation = new DataSetCreation();
        dataSetCreation.setCode(PREFIX + "D1");
        dataSetCreation.setTypeId(new EntityTypePermId("DELETION_TEST_CONTAINER", EntityKind.DATA_SET));
        dataSetCreation.setDataStoreId(new DataStorePermId("STANDARD"));
        dataSetCreation.setDataSetKind(DataSetKind.CONTAINER);
        dataSetCreation.setSampleId(sampleId);

        // When
        assertUserFailureException(Void -> v3api.createDataSets(sessionToken, Arrays.asList(dataSetCreation)),
                // Then
                "ERROR: Operation SET SAMPLE is not allowed because sample C1 is frozen for data set UST-D1.");
    }

    @Test
    public void testFreezingForDataSetDeletions()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleIdentifier sampleId = new SampleIdentifier("/CISD/C1");
        DataSetCreation dataSetCreation = new DataSetCreation();
        dataSetCreation.setCode(PREFIX + "D1");
        dataSetCreation.setTypeId(new EntityTypePermId("DELETION_TEST_CONTAINER", EntityKind.DATA_SET));
        dataSetCreation.setDataStoreId(new DataStorePermId("STANDARD"));
        dataSetCreation.setDataSetKind(DataSetKind.CONTAINER);
        dataSetCreation.setSampleId(sampleId);
        DataSetPermId dataSetId = v3api.createDataSets(sessionToken, Arrays.asList(dataSetCreation)).get(0);
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(sampleId);
        update.freezeForDataSets();
        v3api.updateSamples(sessionToken, Arrays.asList(update));
        DataSetDeletionOptions deletionOptions = new DataSetDeletionOptions();
        deletionOptions.setReason("test");

        // When
        assertUserFailureException(Void -> v3api.deleteDataSets(sessionToken, Arrays.asList(dataSetId), deletionOptions),
                // Then
                "ERROR: Operation DELETE DATA SET is not allowed because sample C1 is frozen.");
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
        SampleIdentifier sampleId = new SampleIdentifier("/CISD/C1");
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(sampleId);
        freezeMethod.method.invoke(update);

        // When
        assertAuthorizationFailureException(Void -> v3api.updateSamples(sessionToken, Arrays.asList(update)), null);
    }

    @DataProvider(name = "freezeMethods")
    public static Object[][] freezeMethods()
    {
        return asCartesianProduct(getFreezingMethods(SampleUpdate.class));
    }

    @Test
    public void testUpdateProjectOfSharedSample() throws Exception
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation creation = new SampleCreation();
        creation.setCode("TEST_SHARED_SAMPLE");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));

        SamplePermId id = v3api.createSamples(sessionToken, Collections.singletonList(creation)).get(0);

        // When
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(id);
        update.setSpaceId(new SpacePermId("CISD"));
        update.setProjectId(new ProjectIdentifier("/CISD/DEFAULT"));

        if (SamplePE.projectSamplesEnabled)
        {
            v3api.updateSamples(sessionToken, Collections.singletonList(update));

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
            assertUserFailureException(() -> v3api.updateSamples(sessionToken, Collections.singletonList(update)),
                    "Can not assign sample /CISD/TEST_SHARED_SAMPLE to project /CISD/DEFAULT because project samples are not enabled.");
        }
    }

    @Test
    public void testUpdateProjectOfSpaceSample() throws Exception
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation creation = new SampleCreation();
        creation.setCode("TEST_SPACE_SAMPLE");
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));

        SamplePermId id = v3api.createSamples(sessionToken, Collections.singletonList(creation)).get(0);

        // When
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(id);
        update.setProjectId(new ProjectIdentifier("/CISD/DEFAULT"));

        if (SamplePE.projectSamplesEnabled)
        {
            v3api.updateSamples(sessionToken, Collections.singletonList(update));

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
            assertUserFailureException(() -> v3api.updateSamples(sessionToken, Collections.singletonList(update)),
                    "Can not assign sample /CISD/TEST_SPACE_SAMPLE to project /CISD/DEFAULT because project samples are not enabled.");
        }
    }

    @Test
    public void testUpdateProjectToTheSameProjectAsCurrent() throws Exception
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation creation = new SampleCreation();
        creation.setCode("TEST_SAMPLE");
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setExperimentId(new ExperimentIdentifier("/CISD/DEFAULT/EXP-REUSE"));
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));

        SamplePermId id = v3api.createSamples(sessionToken, Collections.singletonList(creation)).get(0);

        // When
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(id);
        update.setProjectId(new ProjectIdentifier("/CISD/DEFAULT"));

        v3api.updateSamples(sessionToken, Collections.singletonList(update));

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
    public void testUpdateProjectToNullTheSameAsCurrent() throws Exception
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation creation = new SampleCreation();
        creation.setCode("TEST_SAMPLE");
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setExperimentId(new ExperimentIdentifier("/CISD/DEFAULT/EXP-REUSE"));
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));

        SamplePermId id = v3api.createSamples(sessionToken, Collections.singletonList(creation)).get(0);

        // When
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(id);
        update.setProjectId(null);

        if (SamplePE.projectSamplesEnabled)
        {
            assertUserFailureException(() -> v3api.updateSamples(sessionToken, Collections.singletonList(update)),
                    "Project has to be defined for experiment level samples");
        } else
        {
            v3api.updateSamples(sessionToken, Collections.singletonList(update));

            // Then
            SampleFetchOptions fo = new SampleFetchOptions();
            fo.withSpace();
            fo.withProject();
            fo.withExperiment();

            Sample sample = v3api.getSamples(sessionToken, Collections.singletonList(id), fo).get(id);

            assertEquals(sample.getSpace().getCode(), "CISD");
            assertNull(sample.getProject());
            assertEquals(sample.getExperiment().getIdentifier().getIdentifier(), "/CISD/DEFAULT/EXP-REUSE");
        }
    }

    @Test
    public void testUpdateProjectToDifferentProjectThanCurrent() throws Exception
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation creation = new SampleCreation();
        creation.setCode("TEST_SAMPLE");
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setExperimentId(new ExperimentIdentifier("/CISD/DEFAULT/EXP-REUSE"));
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));

        SamplePermId id = v3api.createSamples(sessionToken, Collections.singletonList(creation)).get(0);

        // When
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(id);
        update.setProjectId(new ProjectIdentifier("/CISD/NEMO"));

        if (SamplePE.projectSamplesEnabled)
        {
            assertUserFailureException(() -> v3api.updateSamples(sessionToken, Collections.singletonList(update)),
                    "Sample project must be the same as experiment project. Sample: /CISD/NEMO/TEST_SAMPLE (perm id: " + id.getPermId()
                            + "), Project: /CISD/NEMO (perm id: 20120814110011738-103), Experiment: /CISD/DEFAULT/EXP-REUSE (perm id: 200811050940555-1032)");
        } else
        {
            assertUserFailureException(() -> v3api.updateSamples(sessionToken, Collections.singletonList(update)),
                    "Can not assign sample /CISD/TEST_SAMPLE to project /CISD/NEMO because project samples are not enabled.");
        }
    }

    @Test
    public void testUpdateProjectAndExperimentToTheSameProjectAsCurrent() throws Exception
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation creation = new SampleCreation();
        creation.setCode("TEST_SAMPLE");
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setExperimentId(new ExperimentIdentifier("/CISD/DEFAULT/EXP-REUSE"));
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));

        SamplePermId id = v3api.createSamples(sessionToken, Collections.singletonList(creation)).get(0);

        // When
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(id);
        update.setProjectId(new ProjectIdentifier("/CISD/DEFAULT"));
        update.setExperimentId(new ExperimentIdentifier("/CISD/DEFAULT/EXP-WELLS"));

        v3api.updateSamples(sessionToken, Collections.singletonList(update));

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
        assertEquals(sample.getExperiment().getIdentifier().getIdentifier(), "/CISD/DEFAULT/EXP-WELLS");
    }

    @Test
    public void testUpdateProjectAndExperimentToDifferentProjectThanCurrent() throws Exception
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation creation = new SampleCreation();
        creation.setCode("TEST_SAMPLE");
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setExperimentId(new ExperimentIdentifier("/CISD/DEFAULT/EXP-REUSE"));
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));

        SamplePermId id = v3api.createSamples(sessionToken, Collections.singletonList(creation)).get(0);

        // When
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(id);
        update.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        update.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP1"));

        v3api.updateSamples(sessionToken, Collections.singletonList(update));

        // Then
        SampleFetchOptions fo = new SampleFetchOptions();
        fo.withSpace();
        fo.withProject();
        fo.withExperiment();

        Sample sample = v3api.getSamples(sessionToken, Collections.singletonList(id), fo).get(id);

        assertEquals(sample.getSpace().getCode(), "CISD");
        if (SamplePE.projectSamplesEnabled)
        {
            assertEquals(sample.getProject().getIdentifier().getIdentifier(), "/CISD/NEMO");
        } else
        {
            assertNull(sample.getProject());
        }
        assertEquals(sample.getExperiment().getIdentifier().getIdentifier(), "/CISD/NEMO/EXP1");
    }

    @Test
    public void testUpdateProjectAndExperimentToDifferentProjectThanCurrentInconsistently() throws Exception
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation creation = new SampleCreation();
        creation.setCode("TEST_SAMPLE");
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setExperimentId(new ExperimentIdentifier("/CISD/DEFAULT/EXP-REUSE"));
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));

        SamplePermId id = v3api.createSamples(sessionToken, Collections.singletonList(creation)).get(0);

        // When
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(id);
        update.setProjectId(new ProjectIdentifier("/CISD/NOE"));
        update.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP1"));

        if (SamplePE.projectSamplesEnabled)
        {
            assertUserFailureException(() -> v3api.updateSamples(sessionToken, Collections.singletonList(update)),
                    "Sample project must be the same as experiment project. Sample: /CISD/NOE/TEST_SAMPLE (perm id: " + id.getPermId()
                            + "), Project: /CISD/NOE (perm id: 20120814110011738-104), Experiment: /CISD/NEMO/EXP1 (perm id: 200811050951882-1028)");
        } else
        {
            assertUserFailureException(() -> v3api.updateSamples(sessionToken, Collections.singletonList(update)),
                    "Can not assign sample /CISD/TEST_SAMPLE to project /CISD/NOE because project samples are not enabled.");
        }
    }

    @Test(dataProvider = USER_ROLES_PROVIDER)
    public void testUpdateWithDifferentRolesInstanceSample(RoleWithHierarchy role)
    {
        testWithUserRole(role, params ->
        {
            final SampleCreation sampleCreation = new SampleCreation();
            sampleCreation.setCode("TEST_INSTANCE_SAMPLE_" + UUID.randomUUID());
            sampleCreation.setTypeId(new EntityTypePermId("CELL_PLATE"));
            final SamplePermId sampleId = v3api.createSamples(params.adminSessionToken, List.of(sampleCreation)).get(0);

            final SampleUpdate sampleUpdate = new SampleUpdate();
            sampleUpdate.setSampleId(sampleId);
            sampleUpdate.setProperty("COMMENT", "test comment");

            if (RoleWithHierarchy.INSTANCE_ADMIN.equals(role))
            {
                v3api.updateSamples(params.userSessionToken, Collections.singletonList(sampleUpdate));
            } else
            {
                assertUnauthorizedObjectAccessException(() -> v3api.updateSamples(params.userSessionToken, Collections.singletonList(sampleUpdate)),
                        null);
            }
        });
    }

    @Test(dataProvider = USER_ROLES_PROVIDER)
    public void testUpdateWithDifferentRolesSpaceSample(RoleWithHierarchy role)
    {
        testWithUserRole(role, params ->
        {
            final SampleCreation sampleCreation = new SampleCreation();
            sampleCreation.setCode("TEST_SPACE_SAMPLE_" + UUID.randomUUID());
            sampleCreation.setTypeId(new EntityTypePermId("CELL_PLATE"));
            sampleCreation.setSpaceId(params.space1Id);
            final SamplePermId sampleId = v3api.createSamples(params.adminSessionToken, List.of(sampleCreation)).get(0);

            final SampleUpdate sampleUpdate = new SampleUpdate();
            sampleUpdate.setSampleId(sampleId);
            sampleUpdate.setSpaceId(params.space2Id);

            if (List.of(RoleWithHierarchy.RoleLevel.INSTANCE, RoleWithHierarchy.RoleLevel.SPACE).contains(role.getRoleLevel()) && List.of(
                    RoleWithHierarchy.RoleCode.ADMIN, RoleWithHierarchy.RoleCode.POWER_USER).contains(role.getRoleCode()))
            {
                v3api.updateSamples(params.userSessionToken, Collections.singletonList(sampleUpdate));
            } else
            {
                assertAnyAuthorizationException(() -> v3api.updateSamples(params.userSessionToken, Collections.singletonList(sampleUpdate)));
            }
        });
    }

    @Test(dataProvider = USER_ROLES_PROVIDER)
    public void testUpdateWithDifferentRolesProjectSample(RoleWithHierarchy role)
    {
        testWithUserRole(role, params ->
        {
            final SampleCreation sampleCreation = new SampleCreation();
            sampleCreation.setCode("TEST_PROJECT_SAMPLE_" + UUID.randomUUID());
            sampleCreation.setTypeId(new EntityTypePermId("CELL_PLATE"));
            sampleCreation.setSpaceId(params.space1Id);
            sampleCreation.setProjectId(params.space1Project1Id);

            final SamplePermId sampleId = v3api.createSamples(params.adminSessionToken, List.of(sampleCreation)).get(0);

            final SampleUpdate sampleUpdate = new SampleUpdate();
            sampleUpdate.setSampleId(sampleId);
            sampleUpdate.setProjectId(params.space1Project2Id);

            if (List.of(RoleWithHierarchy.RoleCode.ADMIN, RoleWithHierarchy.RoleCode.POWER_USER).contains(role.getRoleCode()))
            {
                v3api.updateSamples(params.userSessionToken, Collections.singletonList(sampleUpdate));
            } else
            {
                assertAnyAuthorizationException(() -> v3api.updateSamples(params.userSessionToken, Collections.singletonList(sampleUpdate)));
            }
        });
    }

    @Test(dataProvider = USER_ROLES_PROVIDER)
    public void testUpdateWithDifferentRolesExperimentSample(RoleWithHierarchy role)
    {
        testWithUserRole(role, params ->
        {
            final ExperimentCreation experimentCreation = new ExperimentCreation();
            experimentCreation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
            experimentCreation.setCode("TEST_EXPERIMENT_" + UUID.randomUUID());
            experimentCreation.setProjectId(params.space1Project1Id);
            experimentCreation.setProperty("DESCRIPTION", "test description");

            final ExperimentCreation experimentCreation2 = new ExperimentCreation();
            experimentCreation2.setTypeId(new EntityTypePermId("SIRNA_HCS"));
            experimentCreation2.setCode("TEST_EXPERIMENT_" + UUID.randomUUID());
            experimentCreation2.setProjectId(params.space1Project2Id);
            experimentCreation2.setProperty("DESCRIPTION", "test description");

            final List<ExperimentPermId> experimentIds =
                    v3api.createExperiments(params.adminSessionToken, List.of(experimentCreation, experimentCreation2));
            ExperimentPermId experiment1Id = experimentIds.get(0);
            ExperimentPermId experiment2Id = experimentIds.get(1);

            final SampleCreation sampleCreation = new SampleCreation();
            sampleCreation.setCode("TEST_EXPERIMENT_SAMPLE_" + UUID.randomUUID());
            sampleCreation.setTypeId(new EntityTypePermId("CELL_PLATE"));
            sampleCreation.setSpaceId(params.space1Id);
            sampleCreation.setExperimentId(experiment1Id);

            final SamplePermId sampleId = v3api.createSamples(params.adminSessionToken, List.of(sampleCreation)).get(0);

            final SampleUpdate sampleUpdate = new SampleUpdate();
            sampleUpdate.setSampleId(sampleId);
            sampleUpdate.setExperimentId(experiment2Id);

            if (List.of(RoleWithHierarchy.RoleCode.ADMIN, RoleWithHierarchy.RoleCode.POWER_USER).contains(role.getRoleCode()))
            {
                v3api.updateSamples(params.userSessionToken, Collections.singletonList(sampleUpdate));
            } else
            {
                assertAnyAuthorizationException(() -> v3api.updateSamples(params.userSessionToken, Collections.singletonList(sampleUpdate)));
            }
        });
    }

    @Test(dataProvider = USER_ROLES_PROVIDER)
    public void testUpdateWithDifferentRolesParentChildSample(RoleWithHierarchy role)
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

            List<SamplePermId> sampleIds = v3api.createSamples(params.adminSessionToken, List.of(parentCreation, childCreation));
            SamplePermId parentId = sampleIds.get(0);
            SamplePermId childId = sampleIds.get(1);

            SampleUpdate parentUpdate = new SampleUpdate();
            parentUpdate.setSampleId(parentId);
            parentUpdate.getChildIds().remove(childId);

            SampleUpdate childUpdate = new SampleUpdate();
            childUpdate.setSampleId(childId);
            childUpdate.getParentIds().remove(parentId);

            if (List.of(RoleWithHierarchy.RoleLevel.INSTANCE, RoleWithHierarchy.RoleLevel.SPACE).contains(role.getRoleLevel()) && List.of(
                            RoleWithHierarchy.RoleCode.ADMIN, RoleWithHierarchy.RoleCode.POWER_USER)
                    .contains(role.getRoleCode()))
            {
                v3api.updateSamples(params.userSessionToken, List.of(parentUpdate, childUpdate));
            } else
            {
                assertAnyAuthorizationException(() -> v3api.updateSamples(params.userSessionToken, List.of(parentUpdate, childUpdate)));
            }
        });
    }

}

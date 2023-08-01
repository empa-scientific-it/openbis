/*
 * Copyright ETH 2015 - 2023 Zürich, Scientific IT Services
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
import static org.testng.Assert.assertNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.PhysicalData;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.delete.DataSetDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.history.DataSetRelationType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.FileFormatTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.PhysicalDataUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.id.DataStorePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.HistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.PersonPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.Role;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.create.RoleAssignmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.Tag;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagCode;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.FreezingFlags;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.systemtest.authorization.ProjectAuthorizationUser;

/**
 * @author pkupczyk
 */
public class UpdateDataSetTest extends AbstractDataSetTest
{

    @Test
    public void testUpdateWithIndexCheck()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(new DataSetPermId("20081105092159111-1"));
        update.setProperty("COMMENT", "an updated comment");

        v3api.updateDataSets(sessionToken, Arrays.asList(update));

        assertDataSetsExists("20081105092159111-1");
    }

    @Test
    public void testUpdateWithDataSetIdNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        assertUserFailureException(new IDelegatedAction()
        {

            @Override
            public void execute()
            {
                DataSetUpdate update = new DataSetUpdate();
                SamplePermId sampleId = new SamplePermId("201206191219327-1058");
                update.setSampleId(sampleId);

                v3api.updateDataSets(sessionToken, Collections.singletonList(update));
            }
        }, "Data set id cannot be null.");
    }

    @Test
    public void testUpdateWithDataSetExisting()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId dataSetId = new DataSetPermId("20081105092259000-18");

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetId);
        update.setProperty("COMMENT", "Updated description");

        v3api.updateDataSets(sessionToken, Collections.singletonList(update));

        DataSetFetchOptions fe = new DataSetFetchOptions();
        fe.withProperties();
        DataSet result = v3api.getDataSets(sessionToken, Collections.singletonList(dataSetId), fe).get(dataSetId);

        assertEquals(result.getProperties().get("COMMENT"), "Updated description");
    }

    @Test
    public void testUpdateDSWithAdminUserInAnotherSpace()
    {
        final DataSetPermId permId = new DataSetPermId("20120619092259000-22");

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                String sessionToken = v3api.login(TEST_ROLE_V3, PASSWORD);

                DataSetUpdate update = new DataSetUpdate();
                update.setDataSetId(permId);

                v3api.updateDataSets(sessionToken, Collections.singletonList(update));
            }
        }, permId);
    }

    @Test
    public void testUpdateWithDataSetNonexistent()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final IDataSetId dataSetId = new DataSetPermId("IDONTEXIST");
        final DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetId);

        assertObjectNotFoundException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.updateDataSets(sessionToken, Arrays.asList(update));
            }
        }, dataSetId);
    }

    @Test
    public void testUpdateWithSample()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId dataSetId = new DataSetPermId("COMPONENT_1A");

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetId);
        SamplePermId sampleId = new SamplePermId("201206191219327-1058");
        update.setSampleId(sampleId);
        v3api.updateDataSets(sessionToken, Collections.singletonList(update));

        DataSetFetchOptions fe = new DataSetFetchOptions();
        fe.withSample();
        fe.withExperiment();
        Map<IDataSetId, DataSet> map = v3api.getDataSets(sessionToken, Arrays.asList(dataSetId), fe);

        DataSet result = map.get(dataSetId);
        assertEquals(result.getSample().getPermId(), sampleId);
        assertEquals(result.getExperiment().getCode(), "EXP-SPACE-TEST");
    }

    @Test(expectedExceptions = { UserFailureException.class }, expectedExceptionsMessageRegExp = "(?s).*Access denied.*")
    public void testUpdateWithSampleNotAllowed()
    {
        String sessionToken = v3api.login(TEST_POWER_USER_CISD, PASSWORD);

        DataSetPermId dataSetId = new DataSetPermId("COMPONENT_1A");

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetId);
        SamplePermId sampleId = new SamplePermId("200902091250077-1060");
        update.setSampleId(sampleId);
        v3api.updateDataSets(sessionToken, Collections.singletonList(update));

        DataSetFetchOptions fe = new DataSetFetchOptions();
        fe.withSample();
        Map<IDataSetId, DataSet> map = v3api.getDataSets(sessionToken, Arrays.asList(dataSetId), fe);

        DataSet result = map.get(dataSetId);
        assertEquals(result.getSample().getPermId(), sampleId);
    }

    @Test
    public void testUpdateWithSampleWithoutAnExperiment()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId dataSetId = new DataSetPermId("COMPONENT_1A");

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetId);
        SamplePermId sampleId = new SamplePermId("200811050943584-1024");
        update.setSampleId(sampleId);
        v3api.updateDataSets(sessionToken, Collections.singletonList(update));

        DataSetFetchOptions fe = new DataSetFetchOptions();
        fe.withSample();
        fe.withExperiment();
        Map<IDataSetId, DataSet> map = v3api.getDataSets(sessionToken, Arrays.asList(dataSetId), fe);

        DataSet result = map.get(dataSetId);
        assertEquals(result.getSample().getPermId(), sampleId);
        assertEquals(result.getExperiment(), null);
    }

    @Test
    public void testUpdateWithSampleNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId dataSetId = new DataSetPermId("20081105092159111-1");
        DataSetFetchOptions fe = new DataSetFetchOptions();
        fe.withExperiment();
        fe.withSample();

        Map<IDataSetId, DataSet> map = v3api.getDataSets(sessionToken, Arrays.asList(dataSetId), fe);

        DataSet result = map.get(dataSetId);
        assertEquals(result.getExperiment().getPermId().getPermId(), "200902091239077-1033");
        assertEquals(result.getSample().getPermId().getPermId(), "200902091219327-1025");

        final DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetId);
        update.setSampleId(null);

        v3api.updateDataSets(sessionToken, Arrays.asList(update));

        map = v3api.getDataSets(sessionToken, Arrays.asList(dataSetId), fe);

        result = map.get(dataSetId);
        assertEquals(result.getExperiment().getPermId().getPermId(), "200902091239077-1033");
        assertNull(result.getSample());
    }

    @Test
    public void testUpdateWithExperiment()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId dataSetId = new DataSetPermId("20081105092259000-18");

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetId);
        update.setExperimentId(new ExperimentPermId("200811050951882-1028"));

        v3api.updateDataSets(sessionToken, Collections.singletonList(update));

        DataSetFetchOptions fe = new DataSetFetchOptions();
        fe.withProperties();
        fe.withExperiment();
        DataSet result = v3api.getDataSets(sessionToken, Collections.singletonList(dataSetId), fe).get(dataSetId);

        assertEquals(result.getExperiment().getPermId().getPermId(), "200811050951882-1028");
    }

    @Test
    public void testUpdateWithExperimentUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        DataSetPermId dataSetId = new DataSetPermId("20120619092259000-22");
        IExperimentId experimentId = new ExperimentIdentifier("/CISD/NEMO/EXP1");

        final DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetId);
        update.setExperimentId(experimentId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.updateDataSets(sessionToken, Collections.singletonList(update));
            }
        }, experimentId);
    }

    @Test
    public void testUpdateWithExperimentNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId dataSetId = new DataSetPermId("20081105092259000-18");

        final DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetId);
        update.setExperimentId(null);

        assertUserFailureException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.updateDataSets(sessionToken, Collections.singletonList(update));
            }
        }, "Neither experiment nor sample is specified for data set 20081105092259000-18");
    }

    @Test
    public void testUpdateWithExperimentAndSample()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId dataSetId = new DataSetPermId("20081105092159111-1");
        DataSetFetchOptions fe = new DataSetFetchOptions();
        fe.withExperiment();
        fe.withSample();

        Map<IDataSetId, DataSet> map = v3api.getDataSets(sessionToken, Arrays.asList(dataSetId), fe);

        DataSet result = map.get(dataSetId);
        assertEquals(result.getExperiment().getPermId().getPermId(), "200902091239077-1033");
        assertEquals(result.getSample().getPermId().getPermId(), "200902091219327-1025");

        final DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetId);
        update.setExperimentId(new ExperimentPermId("200902091258949-1034"));
        update.setSampleId(new SamplePermId("200902091250077-1026"));

        v3api.updateDataSets(sessionToken, Arrays.asList(update));

        map = v3api.getDataSets(sessionToken, Arrays.asList(dataSetId), fe);

        result = map.get(dataSetId);
        assertEquals(result.getExperiment().getPermId().getPermId(), "200902091258949-1034");
        assertEquals(result.getSample().getPermId().getPermId(), "200902091250077-1026");
    }

    @Test
    public void testUpdateWithExperimentWhenComponentsExist()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId containerId = new DataSetPermId("CONTAINER_1");
        DataSetPermId component1Id = new DataSetPermId("COMPONENT_1A");
        DataSetPermId component2Id = new DataSetPermId("COMPONENT_1B");

        ExperimentPermId experimentBeforeId = new ExperimentPermId("200811050940555-1032");
        ExperimentPermId experimentAfterId = new ExperimentPermId("200811050951882-1028");

        DataSetFetchOptions fe = new DataSetFetchOptions();
        fe.withExperiment();
        fe.withComponents().withExperiment();

        DataSet result = v3api.getDataSets(sessionToken, Collections.singletonList(containerId), fe).get(containerId);

        assertEquals(result.getCode(), containerId.getPermId());
        assertEquals(result.getExperiment().getPermId(), experimentBeforeId);
        AssertionUtil.assertCollectionContainsOnly(dataSetCodes(result.getComponents()), component1Id.getPermId(), component2Id.getPermId());
        for (DataSet component : result.getComponents())
        {
            assertEquals(component.getExperiment().getPermId(), experimentBeforeId);
        }

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(containerId);
        update.setExperimentId(experimentAfterId);

        v3api.updateDataSets(sessionToken, Collections.singletonList(update));

        result = v3api.getDataSets(sessionToken, Collections.singletonList(containerId), fe).get(containerId);

        assertEquals(result.getCode(), containerId.getPermId());
        assertEquals(result.getExperiment().getPermId(), experimentAfterId);
        AssertionUtil.assertCollectionContainsOnly(dataSetCodes(result.getComponents()), component1Id.getPermId(), component2Id.getPermId());
        for (DataSet component : result.getComponents())
        {
            assertEquals(component.getExperiment().getPermId(), experimentAfterId);
        }

        assertDataSetsExists(containerId.getPermId(), component1Id.getPermId(), component2Id.getPermId());
    }

    @Test
    public void testUpdateWithExternalDataSet()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId dataSetId = new DataSetPermId("20081105092259000-18");

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetId);
        PhysicalDataUpdate pdupt = new PhysicalDataUpdate();
        pdupt.setFileFormatTypeId(new FileFormatTypePermId("PLKPROPRIETARY"));
        pdupt.setArchivingRequested(true);
        update.setPhysicalData(pdupt);

        v3api.updateDataSets(sessionToken, Collections.singletonList(update));

        DataSetFetchOptions fe = new DataSetFetchOptions();
        fe.withProperties();
        fe.withPhysicalData().withFileFormatType();
        DataSet result = v3api.getDataSets(sessionToken, Collections.singletonList(dataSetId), fe).get(dataSetId);

        PhysicalData physicalData = result.getPhysicalData();
        assertEquals(physicalData.getFileFormatType().getCode(), "PLKPROPRIETARY");
        assertEquals(physicalData.isArchivingRequested(), Boolean.TRUE);
    }

    @Test
    public void testUpdateWithParentUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        DataSetPermId dataSetId = new DataSetPermId("20120619092259000-22");
        final DataSetPermId parentId = new DataSetPermId("20081105092159111-1");

        final DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetId);
        update.getParentIds().add(parentId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.updateDataSets(sessionToken, Collections.singletonList(update));
            }
        }, parentId);
    }

    @Test
    public void testUpdateWithParentCircularDependency()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId dataSetId = new DataSetPermId("20081105092259000-8");
        final DataSetPermId parentId = new DataSetPermId("20081105092259000-9");

        final DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetId);
        update.getParentIds().add(parentId);
        update.getChildIds().add(parentId);

        assertUserFailureException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.updateDataSets(sessionToken, Collections.singletonList(update));
            }
        }, "Circular dependency found: 20081105092259000-8");
    }

    @Test
    public void testUpdateWithParentAdd()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId dataSetId = new DataSetPermId("20081105092259000-18");

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetId);
        update.getParentIds().add(new DataSetPermId("20081105092259000-20"));

        v3api.updateDataSets(sessionToken, Collections.singletonList(update));

        DataSetFetchOptions fe = new DataSetFetchOptions();
        fe.withProperties();
        fe.withExperiment();
        fe.withParents();
        DataSet result = v3api.getDataSets(sessionToken, Collections.singletonList(dataSetId), fe).get(dataSetId);

        AssertionUtil.assertSize(result.getParents(), 2);
    }

    @Test
    public void testUpdateWithParentRemove()
    {
        DataSetPermId originalChild = new DataSetPermId("20081105092259000-18");
        DataSetPermId originalParent = new DataSetPermId("20110805092359990-17");

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(originalChild);
        update.getParentIds().remove(originalParent);

        assertRemovingParent(originalChild, originalParent, update);
    }

    @Test
    public void testUpdateWithChildrenUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        DataSetPermId dataSetId = new DataSetPermId("20120619092259000-22");
        final DataSetPermId childId = new DataSetPermId("20081105092159111-1");

        final DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetId);
        update.getChildIds().add(childId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.updateDataSets(sessionToken, Collections.singletonList(update));
            }
        }, childId);
    }

    @Test
    public void testUpdateWithChildrenRemove()
    {
        DataSetPermId originalChild = new DataSetPermId("20081105092259000-18");
        DataSetPermId originalParent = new DataSetPermId("20110805092359990-17");

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(originalParent);
        update.getChildIds().remove(originalChild);

        assertRemovingParent(originalChild, originalParent, update);
    }

    @Test
    public void testUpdateWithComponentUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        DataSetPermId dataSetId = new DataSetPermId("20120619092259000-22");
        final DataSetPermId componentId = new DataSetPermId("20081105092159111-1");

        final DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetId);
        update.getComponentIds().add(componentId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.updateDataSets(sessionToken, Collections.singletonList(update));
            }
        }, componentId);
    }

    @Test
    public void testUpdateWithComponentCircularDependency()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId dataSetId = new DataSetPermId("CONTAINER_1");
        final DataSetPermId componentId = new DataSetPermId("CONTAINER_2");

        final DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetId);
        update.getComponentIds().add(componentId);
        update.getContainerIds().add(componentId);

        assertUserFailureException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.updateDataSets(sessionToken, Collections.singletonList(update));
            }
        }, "Circular dependency found: CONTAINER_1");
    }

    @Test
    public void testUpdateWithComponentAddRemove()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId dataSetId = new DataSetPermId("CONTAINER_1");

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetId);
        update.getComponentIds().add(new DataSetPermId("COMPONENT_2A"));
        update.getComponentIds().remove(new DataSetPermId("COMPONENT_1A"));

        v3api.updateDataSets(sessionToken, Collections.singletonList(update));

        DataSetFetchOptions fe = new DataSetFetchOptions();
        fe.withComponents();
        DataSet result = v3api.getDataSets(sessionToken, Collections.singletonList(dataSetId), fe).get(dataSetId);

        AssertionUtil.assertCollectionContainsOnly(dataSetCodes(result.getComponents()), "COMPONENT_1B", "COMPONENT_2A");
    }

    @Test
    public void testUpdateWithComponentWithNonContainerDataSet()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(new DataSetPermId("COMPONENT_1A"));
        update.getComponentIds().add(new DataSetPermId("COMPONENT_2A"));

        assertUserFailureException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.updateDataSets(sessionToken, Collections.singletonList(update));
            }
        }, "Data set COMPONENT_1A is not of a container type therefore cannot have component data sets.");
    }

    @Test
    public void testUpdateWithContainerAddRemove()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId dataSetId = new DataSetPermId("COMPONENT_1A");

        DataSetPermId cont2 = new DataSetPermId("CONTAINER_2");
        DataSetPermId cont3a = new DataSetPermId("CONTAINER_3A");
        DataSetPermId cont1 = new DataSetPermId("CONTAINER_1");

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetId);
        update.getContainerIds().add(cont2, cont3a);
        update.getContainerIds().remove(cont1);

        v3api.updateDataSets(sessionToken, Collections.singletonList(update));

        DataSetFetchOptions fe = new DataSetFetchOptions();
        fe.withContainers();
        fe.withComponents();
        Map<IDataSetId, DataSet> map = v3api.getDataSets(sessionToken, Arrays.asList(dataSetId, cont2, cont3a, cont1), fe);

        DataSet result = map.get(dataSetId);

        AssertionUtil.assertCollectionContainsOnly(dataSetCodes(result.getContainers()), cont2.getPermId(), cont3a.getPermId());
        AssertionUtil.assertCollectionDoesntContain(dataSetCodes(map.get(cont1).getComponents()), dataSetId.getPermId());
        AssertionUtil.assertCollectionContains(dataSetCodes(map.get(cont2).getComponents()), dataSetId.getPermId());
        AssertionUtil.assertCollectionContains(dataSetCodes(map.get(cont3a).getComponents()), dataSetId.getPermId());
    }

    @Test
    public void testUpdateWithContainerAddNotContainerDataSet()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId dataSetId = new DataSetPermId("CONTAINER_1");
        DataSetPermId containerId = new DataSetPermId("COMPONENT_2A");

        final DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetId);
        update.getContainerIds().add(containerId);

        assertUserFailureException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.updateDataSets(sessionToken, Collections.singletonList(update));
            }
        }, "Data set COMPONENT_2A is not of a container type therefore cannot be set as a container of data set CONTAINER_1.");
    }

    @Test
    public void testUpdateWithContainerUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        DataSetPermId dataSetId = new DataSetPermId("20120619092259000-22");
        final DataSetPermId containerId = new DataSetPermId("20081105092159111-1");

        final DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetId);
        update.getContainerIds().add(containerId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.updateDataSets(sessionToken, Collections.singletonList(update));
            }
        }, containerId);
    }

    @Test
    public void testUpdateWithTagsWithSetAddRemove()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        DataSetFetchOptions fe = new DataSetFetchOptions();
        fe.withTags();

        DataSetPermId dataSet = new DataSetPermId("20120619092259000-22");

        TagCode existingTag = new TagCode("TEST_METAPROJECTS");
        TagCode newTag1 = new TagCode("NEW_TAG_1");
        TagCode newTag2 = new TagCode("NEW_TAG_2");

        // add tag1
        DataSetUpdate firstUpdate = new DataSetUpdate();
        firstUpdate.setDataSetId(dataSet);
        firstUpdate.getTagIds().add(newTag1);

        v3api.updateDataSets(sessionToken, Arrays.asList(firstUpdate));

        Map<IDataSetId, DataSet> result = v3api.getDataSets(sessionToken, Arrays.asList(dataSet), fe);
        AssertionUtil.assertCollectionContainsOnly(tagCodes(result.get(dataSet).getTags()), existingTag.getCode(), newTag1.getCode());

        // remove test_metaprojects and add a new tag2
        DataSetUpdate secondUpdate = new DataSetUpdate();
        secondUpdate.setDataSetId(dataSet);
        secondUpdate.getTagIds().remove(existingTag);
        secondUpdate.getTagIds().add(newTag2);

        v3api.updateDataSets(sessionToken, Arrays.asList(secondUpdate));

        result = v3api.getDataSets(sessionToken, Arrays.asList(dataSet), fe);
        AssertionUtil.assertCollectionContainsOnly(tagCodes(result.get(dataSet).getTags()), newTag1.getCode(), newTag2.getCode());
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testUpdateWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String sessionToken = v3api.login(user.getUserId(), PASSWORD);

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(new DataSetPermId("20120619092259000-22"));
        update.setProperty("COMMENT", "updated comment");

        if (user.isDisabledProjectUser())
        {
            assertAuthorizationFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateDataSets(sessionToken, Arrays.asList(update));
                }
            });
        } else if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            v3api.updateDataSets(sessionToken, Arrays.asList(update));
        } else
        {
            assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateDataSets(sessionToken, Arrays.asList(update));
                }
            }, update.getDataSetId());
        }
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(new DataSetPermId("20081105092159111-1"));

        DataSetUpdate update2 = new DataSetUpdate();
        update2.setDataSetId(new DataSetPermId("20110509092359990-10"));

        v3api.updateDataSets(sessionToken, Arrays.asList(update, update2));

        assertAccessLog(
                "update-data-sets  DATA_SET_UPDATES('[DataSetUpdate[dataSetId=20081105092159111-1], DataSetUpdate[dataSetId=20110509092359990-10]]')");
    }

    @Test
    public void testFreeze()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        DataSetPermId dsId1 = new DataSetPermId("20081105092159111-1");
        DataSetUpdate update1 = new DataSetUpdate();
        update1.setDataSetId(dsId1);
        update1.freeze();
        DataSetPermId dsId2 = new DataSetPermId("20081105092159222-2");
        DataSetUpdate update2 = new DataSetUpdate();
        update2.setDataSetId(dsId2);
        update2.freezeForChildren();
        DataSetPermId dsId3 = new DataSetPermId("20081105092159333-3");
        DataSetUpdate update3 = new DataSetUpdate();
        update3.setDataSetId(dsId3);
        update3.freezeForParents();
        DataSetPermId dsId4 = new DataSetPermId("20081105092259000-8");
        DataSetUpdate update4 = new DataSetUpdate();
        update4.setDataSetId(dsId4);
        update4.freezeForComponents();
        DataSetPermId dsId5 = new DataSetPermId("20081105092259000-20");
        DataSetUpdate update5 = new DataSetUpdate();
        update5.setDataSetId(dsId5);
        update5.freezeForContainers();

        // When
        v3api.updateDataSets(sessionToken, Arrays.asList(update1, update2, update3, update4, update5));

        // Then
        Map<IDataSetId, DataSet> dataSets =
                v3api.getDataSets(sessionToken, Arrays.asList(dsId1, dsId2, dsId3, dsId4, dsId5), new DataSetFetchOptions());
        DataSet ds1 = dataSets.get(dsId1);
        assertEquals(ds1.isFrozen(), true);
        assertEquals(ds1.isFrozenForChildren(), false);
        assertEquals(ds1.isFrozenForParents(), false);
        assertEquals(ds1.isFrozenForComponents(), false);
        assertEquals(ds1.isFrozenForContainers(), false);
        assertFreezingEvent(TEST_USER, ds1.getCode(), EntityType.DATASET,
                new FreezingFlags().freeze());
        DataSet ds2 = dataSets.get(dsId2);
        assertEquals(ds2.isFrozen(), true);
        assertEquals(ds2.isFrozenForChildren(), true);
        assertEquals(ds2.isFrozenForParents(), false);
        assertEquals(ds2.isFrozenForComponents(), false);
        assertEquals(ds2.isFrozenForContainers(), false);
        assertFreezingEvent(TEST_USER, ds2.getCode(), EntityType.DATASET,
                new FreezingFlags().freeze().freezeForChildren());
        DataSet ds3 = dataSets.get(dsId3);
        assertEquals(ds3.isFrozen(), true);
        assertEquals(ds3.isFrozenForChildren(), false);
        assertEquals(ds3.isFrozenForParents(), true);
        assertEquals(ds3.isFrozenForComponents(), false);
        assertEquals(ds3.isFrozenForContainers(), false);
        assertFreezingEvent(TEST_USER, ds3.getCode(), EntityType.DATASET,
                new FreezingFlags().freeze().freezeForParents());
        DataSet ds4 = dataSets.get(dsId4);
        assertEquals(ds4.isFrozen(), true);
        assertEquals(ds4.isFrozenForChildren(), false);
        assertEquals(ds4.isFrozenForParents(), false);
        assertEquals(ds4.isFrozenForComponents(), true);
        assertEquals(ds4.isFrozenForContainers(), false);
        assertFreezingEvent(TEST_USER, ds4.getCode(), EntityType.DATASET,
                new FreezingFlags().freeze().freezeForComponents());
        DataSet ds5 = dataSets.get(dsId5);
        assertEquals(ds5.isFrozen(), true);
        assertEquals(ds5.isFrozenForChildren(), false);
        assertEquals(ds5.isFrozenForParents(), false);
        assertEquals(ds5.isFrozenForComponents(), false);
        assertEquals(ds5.isFrozenForContainers(), true);
        assertFreezingEvent(TEST_USER, ds5.getCode(), EntityType.DATASET,
                new FreezingFlags().freeze().freezeForContainers());
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
        DataSetPermId dsId = new DataSetPermId("20081105092159111-1");
        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dsId);
        freezeMethod.method.invoke(update);

        // When
        assertAuthorizationFailureException(Void -> v3api.updateDataSets(sessionToken, Arrays.asList(update)), null);
    }

    @DataProvider(name = "freezeMethods")
    public static Object[][] freezeMethods()
    {
        return asCartesianProduct(getFreezingMethods(DataSetUpdate.class));
    }

    @Test
    public void testFreezing()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        DataSetPermId dsId = new DataSetPermId("20081105092159111-1");
        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dsId);
        update.freeze();
        v3api.updateDataSets(sessionToken, Arrays.asList(update));
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dsId);
        dataSetUpdate.setProperty("COMMENT", "test comment");

        // When
        assertUserFailureException(Void -> v3api.updateDataSets(sessionToken, Arrays.asList(dataSetUpdate)),
                // Then
                "ERROR: Operation UPDATE PROPERTY is not allowed because data set 20081105092159111-1 is frozen.");
    }

    @Test
    public void testFreezingForChildren()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        DataSetPermId dsId = new DataSetPermId("20081105092159111-1");
        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dsId);
        update.freezeForChildren();
        v3api.updateDataSets(sessionToken, Arrays.asList(update));
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dsId);
        dataSetUpdate.getChildIds().add(new DataSetPermId("20081105092259000-20"));

        // When
        assertUserFailureException(Void -> v3api.updateDataSets(sessionToken, Arrays.asList(dataSetUpdate)),
                // Then
                "ERROR: Operation INSERT PARENT_CHILD is not allowed because data set 20081105092159111-1 "
                        + "or 20081105092259000-20 is frozen.");
    }

    @Test
    public void testFreezingForChildrenDeletion()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        DataSetPermId dsId = new DataSetPermId("20081105092159111-1");
        DataSetCreation dataSetCreation = createDataSet();
        dataSetCreation.setParentIds(Arrays.asList(dsId));
        DataSetPermId childId = v3api.createDataSets(sessionToken, Arrays.asList(dataSetCreation)).get(0);

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dsId);
        update.freezeForChildren();
        v3api.updateDataSets(sessionToken, Arrays.asList(update));
        DataSetDeletionOptions deletionOptions = new DataSetDeletionOptions();
        deletionOptions.setReason("test");

        // When
        assertUserFailureException(Void -> v3api.deleteDataSets(sessionToken, Arrays.asList(childId), deletionOptions),
                // Then
                "ERROR: Operation DELETE DATA SET CHILD is not allowed because data set 20081105092159111-1 is frozen.");
    }

    protected DataSetCreation createDataSet()
    {
        return createDataSet("D1");
    }

    protected DataSetCreation createDataSet(String code)
    {
        DataSetCreation dataSetCreation = new DataSetCreation();
        dataSetCreation.setCode(code);
        dataSetCreation.setTypeId(new EntityTypePermId("DELETION_TEST_CONTAINER", EntityKind.DATA_SET));
        dataSetCreation.setDataStoreId(new DataStorePermId("STANDARD"));
        dataSetCreation.setDataSetKind(DataSetKind.CONTAINER);
        dataSetCreation.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP-TEST-1"));
        return dataSetCreation;
    }

    @Test
    public void testFreezingForParents()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        DataSetPermId dsId = new DataSetPermId("20081105092159111-1");
        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dsId);
        update.freezeForParents();
        v3api.updateDataSets(sessionToken, Arrays.asList(update));
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dsId);
        dataSetUpdate.getParentIds().add(new DataSetPermId("20081105092259000-20"));

        // When
        assertUserFailureException(Void -> v3api.updateDataSets(sessionToken, Arrays.asList(dataSetUpdate)),
                // Then
                "ERROR: Operation INSERT PARENT_CHILD is not allowed because data set 20081105092259000-20 "
                        + "or 20081105092159111-1 is frozen.");
    }

    @Test
    public void testFreezingForParentDeletion()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        DataSetPermId dsId = new DataSetPermId("20081105092159111-1");
        DataSetCreation dataSetCreation = createDataSet();
        dataSetCreation.setChildIds(Arrays.asList(dsId));
        DataSetPermId parentId = v3api.createDataSets(sessionToken, Arrays.asList(dataSetCreation)).get(0);
        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dsId);
        update.freezeForParents();
        v3api.updateDataSets(sessionToken, Arrays.asList(update));
        DataSetDeletionOptions deletionOptions = new DataSetDeletionOptions();
        deletionOptions.setReason("test");

        // When
        assertUserFailureException(Void -> v3api.deleteDataSets(sessionToken, Arrays.asList(parentId), deletionOptions),
                // Then
                "ERROR: Operation DELETE DATA SET PARENT is not allowed because data set 20081105092159111-1 is frozen.");
    }

    @Test
    public void testFreezingForComponents()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        DataSetPermId dsId = new DataSetPermId("CONTAINER_1");
        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dsId);
        update.freezeForComponents();
        v3api.updateDataSets(sessionToken, Arrays.asList(update));
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dsId);
        dataSetUpdate.getComponentIds().add(new DataSetPermId("20081105092259000-20"));

        // When
        assertUserFailureException(Void -> v3api.updateDataSets(sessionToken, Arrays.asList(dataSetUpdate)),
                // Then
                "ERROR: Operation INSERT CONTAINER_COMPONENT is not allowed because data set CONTAINER_1 "
                        + "or 20081105092259000-20 is frozen.");
    }

    @Test
    public void testFreezingForComponentDeletion()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        DataSetCreation dsContainer = createDataSet("D1");
        DataSetPermId contId = v3api.createDataSets(sessionToken, Arrays.asList(dsContainer)).get(0);
        DataSetCreation dsComp = createDataSet("D2");
        dsComp.setContainerIds(Arrays.asList(contId));
        DataSetPermId compId = v3api.createDataSets(sessionToken, Arrays.asList(dsComp)).get(0);

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(contId);
        update.freezeForComponents();
        v3api.updateDataSets(sessionToken, Arrays.asList(update));
        DataSetDeletionOptions deletionOptions = new DataSetDeletionOptions();
        deletionOptions.setReason("test");

        // When
        assertUserFailureException(Void -> v3api.deleteDataSets(sessionToken, Arrays.asList(compId), deletionOptions),
                // Then
                "ERROR: Operation DELETE DATA SET COMPONENT is not allowed because data set D1 is frozen.");
    }

    @Test
    public void testFreezingForContainers()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        DataSetPermId dsId = new DataSetPermId("20081105092259000-20");
        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dsId);
        update.freezeForContainers();
        v3api.updateDataSets(sessionToken, Arrays.asList(update));
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dsId);
        dataSetUpdate.getContainerIds().add(new DataSetPermId("CONTAINER_1"));

        // When
        assertUserFailureException(Void -> v3api.updateDataSets(sessionToken, Arrays.asList(dataSetUpdate)),
                // Then
                "ERROR: Operation INSERT CONTAINER_COMPONENT is not allowed because data set CONTAINER_1 "
                        + "or 20081105092259000-20 is frozen.");
    }

    @Test
    public void testFreezingForContainerDeletion()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        DataSetPermId dsId = new DataSetPermId("20081105092159111-1");
        DataSetCreation dataSetCreation = createDataSet();
        dataSetCreation.setComponentIds(Arrays.asList(dsId));
        DataSetPermId containerId = v3api.createDataSets(sessionToken, Arrays.asList(dataSetCreation)).get(0);
        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dsId);
        update.freezeForContainers();
        v3api.updateDataSets(sessionToken, Arrays.asList(update));
        DataSetDeletionOptions deletionOptions = new DataSetDeletionOptions();
        deletionOptions.setReason("test");

        // When
        assertUserFailureException(Void -> v3api.deleteDataSets(sessionToken, Arrays.asList(containerId), deletionOptions),
                // Then
                "ERROR: Operation TRASH is not allowed because data set 20081105092159111-1 is frozen.");
    }

    @Test
    public void testUpdateWithUnknownPropertyOfTypeSample()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetCreation creation = physicalDataSetCreation();
        DataSetPermId dataSetPermId = v3api.createDataSets(sessionToken, Arrays.asList(creation)).get(0);

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetPermId);
        update.setProperty("PLATE", "200811050924898-997");

        // When
        assertUserFailureException(Void -> v3api.updateDataSets(sessionToken, Arrays.asList(update)),
                // Then
                "Property type with code 'PLATE' does not exist");
    }

    @Test
    public void testUpdateWithPropertyOfTypeSampleWithUnknownSample()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createASamplePropertyType(sessionToken, null);
        EntityTypePermId dataSetType = createADataSetType(sessionToken, false, propertyType);
        DataSetCreation creation = physicalDataSetCreation();
        creation.setTypeId(dataSetType);
        creation.setProperty(propertyType.getPermId(), "/CISD/CL1");
        DataSetPermId dataSetPermId = v3api.createDataSets(sessionToken, Arrays.asList(creation)).get(0);

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetPermId);
        update.setProperty(propertyType.getPermId(), "/CISD/UNKNOWN");

        // When
        assertUserFailureException(Void -> v3api.updateDataSets(sessionToken, Arrays.asList(update)),
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
        EntityTypePermId dataSetType = createADataSetType(sessionToken, false, propertyType);
        DataSetCreation creation = physicalDataSetCreation();
        creation.setTypeId(dataSetType);
        creation.setProperty(propertyType.getPermId(), "200811050917877-438");
        DataSetPermId dataSetPermId = v3api.createDataSets(sessionToken, Arrays.asList(creation)).get(0);

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetPermId);
        update.setProperty(propertyType.getPermId(), "200811050919915-8");

        // When
        assertUserFailureException(Void -> v3api.updateDataSets(sessionToken, Arrays.asList(update)),
                // Then
                "Property " + propertyType.getPermId() + " is not a sample of type WELL but of type CONTROL_LAYOUT");
    }

    @Test
    public void testUpdateWithPropertyOfTypeSampleWithSampleNotAccessable()
    {
        // Given
        String adminSessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createASamplePropertyType(adminSessionToken, null);
        EntityTypePermId dataSetType = createADataSetType(adminSessionToken, false, propertyType);
        DataSetCreation creation = physicalDataSetCreation();
        creation.setTypeId(dataSetType);
        creation.setExperimentId(new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));
        creation.setProperty(propertyType.getPermId(), "/MP");
        DataSetPermId dataSetPermId = v3api.createDataSets(adminSessionToken, Arrays.asList(creation)).get(0);

        String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);
        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetPermId);
        update.setProperty(propertyType.getPermId(), "/CISD/CL1");

        // When
        assertUserFailureException(Void -> v3api.updateDataSets(sessionToken, Arrays.asList(update)),
                // Then
                "Unknown sample: /CISD/CL1");
    }

    @Test
    public void testUpdateAddSampleProperty()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createASamplePropertyType(sessionToken, null);
        EntityTypePermId dataSetType = createADataSetType(sessionToken, false, propertyType);
        DataSetCreation creation = physicalDataSetCreation();
        creation.setTypeId(dataSetType);
        creation.setExperimentId(new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));
        DataSetPermId dataSetPermId = v3api.createDataSets(sessionToken, Arrays.asList(creation)).get(0);

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetPermId);
        update.setProperty(propertyType.getPermId(), "200811050924898-997");

        // When
        v3api.updateDataSets(sessionToken, Arrays.asList(update));

        // Then
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        DataSet dataSet = v3api.getDataSets(sessionToken, Arrays.asList(dataSetPermId), fetchOptions).get(dataSetPermId);
        assertEquals(dataSet.getProperties().toString(), "{" + propertyType.getPermId() + "=200811050924898-997}");
        assertEquals(dataSet.getSampleProperties().get(propertyType.getPermId()).length, 1);
        assertEquals(dataSet.getSampleProperties().get(propertyType.getPermId())[0].toString(), "Sample 200811050924898-997");
    }

    @Test
    public void testUpdateChangeSampleProperty()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createASamplePropertyType(sessionToken, null);
        EntityTypePermId dataSetType = createADataSetType(sessionToken, false, propertyType);
        DataSetCreation creation = physicalDataSetCreation();
        creation.setTypeId(dataSetType);
        creation.setExperimentId(new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));
        creation.setProperty(propertyType.getPermId(), "200811050919915-8");
        DataSetPermId dataSetPermId = v3api.createDataSets(sessionToken, Arrays.asList(creation)).get(0);

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetPermId);
        update.setProperty(propertyType.getPermId(), "200811050924898-997");

        // When
        v3api.updateDataSets(sessionToken, Arrays.asList(update));

        // Then
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        fetchOptions.withHistory().withAuthor();
        DataSet dataSet = v3api.getDataSets(sessionToken, Arrays.asList(dataSetPermId), fetchOptions).get(dataSetPermId);
        assertEquals(dataSet.getProperties().toString(), "{" + propertyType.getPermId() + "=200811050924898-997}");
        assertEquals(dataSet.getSampleProperties().get(propertyType.getPermId()).length, 1);
        assertEquals(dataSet.getSampleProperties().get(propertyType.getPermId())[0].toString(), "Sample 200811050924898-997");

        List<HistoryEntry> history = dataSet.getHistory();
        assertEquals(history.size(), 3);

        assertPropertyHistory(history.get(0), propertyType.getPermId(), "200811050919915-8", dataSet.getRegistrationDate(),
                dataSet.getModificationDate());
        assertPropertyHistory(history.get(1), propertyType.getPermId(), "200811050924898-997", dataSet.getModificationDate(), null);
        assertRelationshipHistory(history.get(2), new ExperimentPermId("201206190940555-1032"), DataSetRelationType.EXPERIMENT,
                dataSet.getRegistrationDate(), null);
    }

    @Test
    public void testUpdateRemoveSampleProperty()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createASamplePropertyType(sessionToken, null);
        EntityTypePermId dataSetType = createADataSetType(sessionToken, false, propertyType);
        DataSetCreation creation = physicalDataSetCreation();
        creation.setTypeId(dataSetType);
        creation.setExperimentId(new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));
        creation.setProperty(propertyType.getPermId(), "200811050919915-8");
        DataSetPermId dataSetPermId = v3api.createDataSets(sessionToken, Arrays.asList(creation)).get(0);

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetPermId);
        update.setProperty(propertyType.getPermId(), null);

        // When
        v3api.updateDataSets(sessionToken, Arrays.asList(update));

        // Then
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        fetchOptions.withHistory().withAuthor();
        DataSet dataSet = v3api.getDataSets(sessionToken, Arrays.asList(dataSetPermId), fetchOptions).get(dataSetPermId);
        assertEquals(dataSet.getProperties().toString(), "{}");
        assertEquals(dataSet.getSampleProperties().toString(), "{}");

        List<HistoryEntry> history = dataSet.getHistory();
        assertEquals(history.size(), 2);

        assertPropertyHistory(history.get(0), propertyType.getPermId(), "200811050919915-8", dataSet.getRegistrationDate(),
                dataSet.getModificationDate());
        assertRelationshipHistory(history.get(1), new ExperimentPermId("201206190940555-1032"), DataSetRelationType.EXPERIMENT,
                dataSet.getRegistrationDate(), null);
    }

    @Test
    public void testUpdateRemoveMandatorySampleProperty()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createASamplePropertyType(sessionToken, null);
        EntityTypePermId dataSetType = createADataSetType(sessionToken, true, propertyType);
        DataSetCreation creation = physicalDataSetCreation();
        creation.setTypeId(dataSetType);
        creation.setExperimentId(new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));
        creation.setProperty(propertyType.getPermId(), "200811050919915-8");
        DataSetPermId dataSetPermId = v3api.createDataSets(sessionToken, Arrays.asList(creation)).get(0);

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetPermId);
        update.setProperty(propertyType.getPermId(), null);

        // When
        assertUserFailureException(Void -> v3api.updateDataSets(sessionToken, Arrays.asList(update)),
                "Property " + propertyType + " of entity type " + dataSetType.getPermId()
                        + " can not be deleted because it is mandatory.");
    }


    @Test
    public void testUpdateMetaData()
    {
        // Prepare
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetCreation dataSetCreation = new DataSetCreation();
        dataSetCreation.setCode("DATA_SET_META_1");
        dataSetCreation.setTypeId(new EntityTypePermId("DELETION_TEST_CONTAINER", EntityKind.DATA_SET));
        dataSetCreation.setDataStoreId(new DataStorePermId("STANDARD"));
        dataSetCreation.setDataSetKind(DataSetKind.CONTAINER);
        dataSetCreation.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP-TEST-1"));
        dataSetCreation.setMetaData(Map.of("key_modify", "value_modify", "key_delete", "value_delete"));
        DataSetPermId id = v3api.createDataSets(sessionToken, Arrays.asList(dataSetCreation)).get(0);

        // Act
        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(id);
        update.getMetaData().put("key_modify", "new_value");
        update.getMetaData().add(Map.of("key_add", "value_add"));
        update.getMetaData().remove("key_delete");
        v3api.updateDataSets(sessionToken, Arrays.asList(update));

        // Verify
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        fetchOptions.withHistory().withAuthor();
        DataSet dataSet = v3api.getDataSets(sessionToken, Arrays.asList(id), fetchOptions).get(id);

        assertEquals(dataSet.getMetaData(), Map.of("key_modify", "new_value", "key_add", "value_add"));
    }

    @Test
    public void testUpdateMetaDataSetEmpty()
    {
        // Prepare
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetCreation dataSetCreation = new DataSetCreation();
        dataSetCreation.setCode("DATA_SET_META_1");
        dataSetCreation.setTypeId(new EntityTypePermId("DELETION_TEST_CONTAINER", EntityKind.DATA_SET));
        dataSetCreation.setDataStoreId(new DataStorePermId("STANDARD"));
        dataSetCreation.setDataSetKind(DataSetKind.CONTAINER);
        dataSetCreation.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP-TEST-1"));
        dataSetCreation.setMetaData(Map.of("key_modify", "value_modify", "key_delete", "value_delete"));
        DataSetPermId id = v3api.createDataSets(sessionToken, Arrays.asList(dataSetCreation)).get(0);

        // Act
        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(id);
        update.getMetaData().set(Map.of());
        v3api.updateDataSets(sessionToken, Arrays.asList(update));

        // Verify
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        fetchOptions.withHistory().withAuthor();
        DataSet dataSet = v3api.getDataSets(sessionToken, Arrays.asList(id), fetchOptions).get(id);

        assertEquals(dataSet.getMetaData(), Map.of());
    }

    @Test(dataProvider = USER_ROLES_PROVIDER)
    public void testUpdateWithDifferentRolesExperimentDataSet(RoleWithHierarchy role)
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

            final DataSetCreation dataSetCreation = physicalDataSetCreation();
            dataSetCreation.setExperimentId(experiment1Id);
            dataSetCreation.setSampleId(null);

            final DataSetPermId dataSetId = v3api.createDataSets(params.adminSessionToken, Collections.singletonList(dataSetCreation)).get(0);

            final DataSetUpdate dataSetUpdate = new DataSetUpdate();
            dataSetUpdate.setDataSetId(dataSetId);
            dataSetUpdate.setExperimentId(experiment2Id);

            // use instance admin to login on behalf of the user
            final String onBehalfOfSessionToken = v3api.loginAs(TEST_USER, PASSWORD, params.userId);

            if (List.of(RoleWithHierarchy.RoleCode.ADMIN, RoleWithHierarchy.RoleCode.POWER_USER)
                    .contains(role.getRoleCode()))
            {
                v3api.updateDataSets(onBehalfOfSessionToken, Collections.singletonList(dataSetUpdate));
            } else
            {
                assertAnyAuthorizationException(() -> v3api.updateDataSets(onBehalfOfSessionToken, Collections.singletonList(dataSetUpdate)));
            }
        });
    }

    @Test(dataProvider = USER_ROLES_PROVIDER)
    public void testUpdateWithDifferentRolesSpaceSampleDataSet(RoleWithHierarchy role)
    {
        testWithUserRole(role, params ->
        {
            final SampleCreation sampleCreation = new SampleCreation();
            sampleCreation.setCode("TEST_SPACE_SAMPLE_" + UUID.randomUUID());
            sampleCreation.setTypeId(new EntityTypePermId("CELL_PLATE"));
            sampleCreation.setSpaceId(params.space1Id);

            final SampleCreation sampleCreation2 = new SampleCreation();
            sampleCreation2.setCode("TEST_SPACE_SAMPLE_" + UUID.randomUUID());
            sampleCreation2.setTypeId(new EntityTypePermId("CELL_PLATE"));
            sampleCreation2.setSpaceId(params.space2Id);

            final List<SamplePermId> sampleIds = v3api.createSamples(params.adminSessionToken, List.of(sampleCreation, sampleCreation2));
            SamplePermId sample1Id = sampleIds.get(0);
            SamplePermId sample2Id = sampleIds.get(1);

            final DataSetCreation dataSetCreation = physicalDataSetCreation();
            dataSetCreation.setExperimentId(null);
            dataSetCreation.setSampleId(sample1Id);

            final DataSetPermId dataSetId = v3api.createDataSets(params.adminSessionToken, Collections.singletonList(dataSetCreation)).get(0);

            final DataSetUpdate dataSetUpdate = new DataSetUpdate();
            dataSetUpdate.setDataSetId(dataSetId);
            dataSetUpdate.setSampleId(sample2Id);

            // use instance admin to login on behalf of the user
            final String onBehalfOfSessionToken = v3api.loginAs(TEST_USER, PASSWORD, params.userId);

            if (List.of(RoleWithHierarchy.RoleLevel.INSTANCE, RoleWithHierarchy.RoleLevel.SPACE).contains(role.getRoleLevel()) && List.of(
                            RoleWithHierarchy.RoleCode.ADMIN, RoleWithHierarchy.RoleCode.POWER_USER)
                    .contains(role.getRoleCode()))
            {
                v3api.updateDataSets(onBehalfOfSessionToken, Collections.singletonList(dataSetUpdate));
            } else
            {
                assertAnyAuthorizationException(() -> v3api.updateDataSets(onBehalfOfSessionToken, Collections.singletonList(dataSetUpdate)));
            }
        });
    }

    @Test(dataProvider = USER_ROLES_PROVIDER)
    public void testUpdateWithDifferentRolesProjectSampleDataSet(RoleWithHierarchy role)
    {
        testWithUserRole(role, params ->
        {
            final SampleCreation sampleCreation = new SampleCreation();
            sampleCreation.setCode("TEST_PROJECT_SAMPLE_" + UUID.randomUUID());
            sampleCreation.setTypeId(new EntityTypePermId("CELL_PLATE"));
            sampleCreation.setSpaceId(params.space1Id);
            sampleCreation.setProjectId(params.space1Project1Id);

            final SampleCreation sampleCreation2 = new SampleCreation();
            sampleCreation2.setCode("TEST_PROJECT_SAMPLE_" + UUID.randomUUID());
            sampleCreation2.setTypeId(new EntityTypePermId("CELL_PLATE"));
            sampleCreation2.setSpaceId(params.space1Id);
            sampleCreation2.setProjectId(params.space1Project2Id);

            final List<SamplePermId> sampleIds = v3api.createSamples(params.adminSessionToken, List.of(sampleCreation, sampleCreation2));
            SamplePermId sample1Id = sampleIds.get(0);
            SamplePermId sample2Id = sampleIds.get(1);

            final DataSetCreation dataSetCreation = physicalDataSetCreation();
            dataSetCreation.setExperimentId(null);
            dataSetCreation.setSampleId(sample1Id);

            final DataSetPermId dataSetId = v3api.createDataSets(params.adminSessionToken, Collections.singletonList(dataSetCreation)).get(0);

            final DataSetUpdate dataSetUpdate = new DataSetUpdate();
            dataSetUpdate.setDataSetId(dataSetId);
            dataSetUpdate.setSampleId(sample2Id);

            // use instance admin to login on behalf of the user
            final String onBehalfOfSessionToken = v3api.loginAs(TEST_USER, PASSWORD, params.userId);

            if (List.of(RoleWithHierarchy.RoleCode.ADMIN, RoleWithHierarchy.RoleCode.POWER_USER).contains(role.getRoleCode()))
            {
                v3api.updateDataSets(onBehalfOfSessionToken, Collections.singletonList(dataSetUpdate));
            } else
            {
                assertAnyAuthorizationException(() -> v3api.updateDataSets(onBehalfOfSessionToken, Collections.singletonList(dataSetUpdate)));
            }
        });
    }

    @Test(dataProvider = USER_ROLES_PROVIDER)
    public void testUpdateWithDifferentRolesExperimentSampleDataSet(RoleWithHierarchy role)
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

            final SampleCreation sampleCreation2 = new SampleCreation();
            sampleCreation2.setCode("TEST_EXPERIMENT_SAMPLE_" + UUID.randomUUID());
            sampleCreation2.setTypeId(new EntityTypePermId("CELL_PLATE"));
            sampleCreation2.setSpaceId(params.space1Id);
            sampleCreation2.setExperimentId(experiment2Id);

            final List<SamplePermId> sampleIds = v3api.createSamples(params.adminSessionToken, List.of(sampleCreation, sampleCreation2));
            SamplePermId sample1Id = sampleIds.get(0);
            SamplePermId sample2Id = sampleIds.get(1);

            final DataSetCreation dataSetCreation = physicalDataSetCreation();
            dataSetCreation.setExperimentId(null);
            dataSetCreation.setSampleId(sample1Id);

            final DataSetPermId dataSetId = v3api.createDataSets(params.adminSessionToken, Collections.singletonList(dataSetCreation)).get(0);

            final DataSetUpdate dataSetUpdate = new DataSetUpdate();
            dataSetUpdate.setDataSetId(dataSetId);
            dataSetUpdate.setSampleId(sample2Id);

            // use instance admin to login on behalf of the user
            final String onBehalfOfSessionToken = v3api.loginAs(TEST_USER, PASSWORD, params.userId);

            if (List.of(RoleWithHierarchy.RoleCode.ADMIN, RoleWithHierarchy.RoleCode.POWER_USER).contains(role.getRoleCode()))
            {
                v3api.updateDataSets(onBehalfOfSessionToken, Collections.singletonList(dataSetUpdate));
            } else
            {
                assertAnyAuthorizationException(() -> v3api.updateDataSets(onBehalfOfSessionToken, Collections.singletonList(dataSetUpdate)));
            }
        });
    }

    private Collection<String> dataSetCodes(Collection<? extends DataSet> list)
    {
        LinkedList<String> result = new LinkedList<String>();
        for (DataSet dataSet : list)
        {
            result.add(dataSet.getCode());
        }
        return result;
    }

    private Collection<String> tagCodes(Collection<? extends Tag> list)
    {
        LinkedList<String> result = new LinkedList<String>();
        for (Tag tag : list)
        {
            result.add(tag.getCode());
        }
        return result;
    }

    private void assertRemovingParent(DataSetPermId originalChild, DataSetPermId originalParent, DataSetUpdate update)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        // assert parent to begin with
        DataSetFetchOptions fe = new DataSetFetchOptions();
        fe.withProperties();
        fe.withParents();
        fe.withChildren();
        Map<IDataSetId, DataSet> result = v3api.getDataSets(sessionToken, Arrays.asList(originalChild, originalParent), fe);

        AssertionUtil.assertCollectionContains(dataSetCodes(result.get(originalParent).getChildren()), originalChild.getPermId());
        AssertionUtil.assertCollectionContains(dataSetCodes(result.get(originalChild).getParents()), originalParent.getPermId());

        // update
        v3api.updateDataSets(sessionToken, Collections.singletonList(update));

        // assert parent removed
        result = v3api.getDataSets(sessionToken, Arrays.asList(originalChild, originalParent), fe);

        AssertionUtil.assertCollectionDoesntContain(dataSetCodes(result.get(originalParent).getChildren()), originalChild.getPermId());
        AssertionUtil.assertCollectionDoesntContain(dataSetCodes(result.get(originalChild).getParents()), originalParent.getPermId());
    }

}

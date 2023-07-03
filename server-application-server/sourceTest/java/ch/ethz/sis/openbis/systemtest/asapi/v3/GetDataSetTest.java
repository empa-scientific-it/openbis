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
import static org.testng.Assert.assertFalse;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.ArchivingStatus;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.Complete;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.FileFormatType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.LinkedData;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.LocatorType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.PhysicalData;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.StorageFormat;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.ContentCopyCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.LinkedDataCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.LinkedDataFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.PhysicalDataFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.history.DataSetRelationType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.LinkedDataUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.id.DataStorePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.ExternalDmsPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.ContentCopyHistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.HistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.MaterialPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.Tag;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.fetchoptions.TagFetchOptions;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.openbis.systemtest.authorization.ProjectAuthorizationUser;

/**
 * @author pkupczyk
 */
public class GetDataSetTest extends AbstractDataSetTest
{

    @Test
    public void testGetByPermId()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId permId1 = new DataSetPermId("20081105092159111-1");
        DataSetPermId permId2 = new DataSetPermId("20110509092359990-10");

        Map<IDataSetId, DataSet> map =
                v3api.getDataSets(sessionToken, Arrays.asList(permId1, permId2),
                        new DataSetFetchOptions());

        assertEquals(map.size(), 2);

        Iterator<DataSet> iter = map.values().iterator();
        assertEquals(iter.next().getPermId(), permId1);
        assertEquals(iter.next().getPermId(), permId2);

        assertEquals(map.get(permId1).getPermId(), permId1);
        assertEquals(map.get(permId2).getPermId(), permId2);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetByPermIdCaseInsensitive()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId permId1 = new DataSetPermId("root_CONTAINER");

        Map<IDataSetId, DataSet> map =
                v3api.getDataSets(sessionToken, Arrays.asList(permId1),
                        new DataSetFetchOptions());

        assertEquals(map.size(), 1);

        Iterator<DataSet> iter = map.values().iterator();
        assertEquals(iter.next().getPermId(), permId1);

        assertEquals(map.get(permId1).getPermId().getPermId(), "ROOT_CONTAINER");
        assertEquals(map.get(new DataSetPermId("ROOT_CONTAINER")).getPermId().getPermId(), "ROOT_CONTAINER");

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetByIdsNonexistent()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId permId1 = new DataSetPermId("20081105092159111-1");
        DataSetPermId permId2 = new DataSetPermId("NONEXISTENT");
        DataSetPermId permId3 = new DataSetPermId("20110509092359990-10");

        Map<IDataSetId, DataSet> map = v3api.getDataSets(sessionToken, Arrays.asList(permId1, permId2, permId3), new DataSetFetchOptions());

        assertEquals(map.size(), 2);

        Iterator<DataSet> iter = map.values().iterator();
        assertEquals(iter.next().getPermId(), permId1);
        assertEquals(iter.next().getPermId(), permId3);

        assertEquals(map.get(permId1).getPermId(), permId1);
        assertEquals(map.get(permId3).getPermId(), permId3);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetByIdsDuplicated()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId permId1 = new DataSetPermId("20081105092159111-1");
        DataSetPermId permId2 = new DataSetPermId("20081105092159111-1");
        DataSetPermId permId3 = new DataSetPermId("20110509092359990-10");

        Map<IDataSetId, DataSet> map = v3api.getDataSets(sessionToken, Arrays.asList(permId1, permId2, permId3), new DataSetFetchOptions());

        assertEquals(map.size(), 2);

        Iterator<DataSet> iter = map.values().iterator();
        assertEquals(iter.next().getPermId(), permId1);
        assertEquals(iter.next().getPermId(), permId3);

        assertEquals(map.get(permId1).getPermId(), permId1);
        assertEquals(map.get(permId3).getPermId(), permId3);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetByIdsUnauthorized()
    {
        DataSetPermId permId1 = new DataSetPermId("20081105092159111-1");
        DataSetPermId permId2 = new DataSetPermId("20120619092259000-22");

        List<? extends IDataSetId> ids = Arrays.asList(permId1, permId2);

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        Map<IDataSetId, DataSet> map = v3api.getDataSets(sessionToken, ids, new DataSetFetchOptions());

        assertEquals(map.size(), 2);
        v3api.logout(sessionToken);

        sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);
        map = v3api.getDataSets(sessionToken, ids, new DataSetFetchOptions());

        assertEquals(map.size(), 1);

        assertEquals(map.get(permId2).getPermId(), permId2);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithFetchOptionsEmpty()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId permId = new DataSetPermId("20081105092159111-1");

        Map<IDataSetId, DataSet> map = v3api.getDataSets(sessionToken, Collections.singletonList(permId), new DataSetFetchOptions());

        assertEquals(map.size(), 1);

        DataSet dataSet = map.get(permId);
        assertEquals(dataSet.getPermId().toString(), "20081105092159111-1");
        assertEquals(dataSet.getCode(), "20081105092159111-1");
        assertEqualsDate(dataSet.getAccessDate(), "2011-04-01 09:56:25");
        assertEqualsDate(dataSet.getModificationDate(), "2009-03-23 15:34:44");
        assertEqualsDate(dataSet.getRegistrationDate(), "2009-02-09 12:20:21");
        assertEquals(dataSet.isMeasured(), Boolean.TRUE);
        assertEquals(dataSet.isPostRegistered(), Boolean.TRUE);

        assertTypeNotFetched(dataSet);
        assertPhysicalDataNotFetched(dataSet);
        assertExperimentNotFetched(dataSet);
        assertSampleNotFetched(dataSet);
        assertPropertiesNotFetched(dataSet);
        assertParentsNotFetched(dataSet);
        assertChildrenNotFetched(dataSet);
        assertComponentsNotFetched(dataSet);
        assertContainersNotFetched(dataSet);
        assertModifierNotFetched(dataSet);
        assertRegistratorNotFetched(dataSet);
        assertTagsNotFetched(dataSet);
        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithType()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId permId = new DataSetPermId("20081105092159111-1");
        DataSetPermId permId2 = new DataSetPermId("ROOT_CONTAINER");

        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withType();

        Map<IDataSetId, DataSet> map =
                v3api.getDataSets(sessionToken, Arrays.asList(permId, permId2),
                        fetchOptions);

        assertEquals(map.size(), 2);

        DataSet dataSet = map.get(permId);

        assertEquals(dataSet.getType().getCode(), "HCS_IMAGE");
        assertEquals(dataSet.getType().getPermId().getPermId(), "HCS_IMAGE");
        assertEquals(dataSet.getType().getDescription(), "High Content Screening Image");
        assertEquals(dataSet.getKind(), DataSetKind.PHYSICAL);
        assertEqualsDate(dataSet.getType().getModificationDate(), "2009-03-23 15:34:44");
        assertEquals(dataSet.getType().getFetchOptions().hasPropertyAssignments(), false);

        assertPhysicalDataNotFetched(dataSet);
        assertExperimentNotFetched(dataSet);
        assertSampleNotFetched(dataSet);
        assertPropertiesNotFetched(dataSet);
        assertParentsNotFetched(dataSet);
        assertChildrenNotFetched(dataSet);
        assertComponentsNotFetched(dataSet);
        assertContainersNotFetched(dataSet);
        assertModifierNotFetched(dataSet);
        assertRegistratorNotFetched(dataSet);
        assertTagsNotFetched(dataSet);

        DataSet dataSet2 = map.get(permId2);

        assertEquals(dataSet2.getType().getCode(), "CONTAINER_TYPE");
        assertEquals(dataSet2.getType().getPermId().getPermId(), "CONTAINER_TYPE");
        assertEquals(dataSet2.getType().getDescription(), "A container (virtual) data set type");
        assertEquals(dataSet2.getKind(), DataSetKind.CONTAINER);
        assertEqualsDate(dataSet2.getType().getModificationDate(), "2011-05-09 12:24:44");

        assertPhysicalDataNotFetched(dataSet2);
        assertExperimentNotFetched(dataSet2);
        assertSampleNotFetched(dataSet2);
        assertPropertiesNotFetched(dataSet2);
        assertParentsNotFetched(dataSet2);
        assertChildrenNotFetched(dataSet2);
        assertComponentsNotFetched(dataSet2);
        assertContainersNotFetched(dataSet2);
        assertModifierNotFetched(dataSet2);
        assertRegistratorNotFetched(dataSet2);
        assertTagsNotFetched(dataSet2);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithTypeWithPropertyAssignments()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        DataSetPermId permId = new DataSetPermId("20081105092159111-1");
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withType().withPropertyAssignments().sortBy().code().desc();

        Map<IDataSetId, DataSet> map = v3api.getDataSets(sessionToken, Arrays.asList(permId), fetchOptions);

        assertEquals(map.size(), 1);
        DataSet dataSet = map.get(permId);
        DataSetType type = dataSet.getType();
        assertEquals(type.getFetchOptions().hasPropertyAssignments(), true);
        List<PropertyAssignment> propertyAssignments = type.getPropertyAssignments();
        assertEquals(propertyAssignments.get(0).getPropertyType().getCode(), "GENDER");
        assertEquals(propertyAssignments.get(0).getPropertyType().getLabel(), "Gender");
        assertEquals(propertyAssignments.get(0).getPropertyType().getDescription(), "The gender of the living organism");
        assertEquals(propertyAssignments.get(0).getPropertyType().isManagedInternally(), Boolean.FALSE);
        assertEquals(propertyAssignments.get(0).getPropertyType().getDataType(), DataType.CONTROLLEDVOCABULARY);
        assertEquals(propertyAssignments.get(0).isMandatory(), Boolean.FALSE);
        assertEquals(propertyAssignments.get(1).getPropertyType().getCode(), "COMMENT");
        assertEquals(propertyAssignments.get(2).getPropertyType().getCode(), "BACTERIUM");
        assertEquals(propertyAssignments.get(3).getPropertyType().getCode(), "ANY_MATERIAL");
        assertEquals(propertyAssignments.size(), 4);
        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithExperiment()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId permId = new DataSetPermId("20081105092159111-1");

        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withExperiment();

        Map<IDataSetId, DataSet> map =
                v3api.getDataSets(sessionToken, Arrays.asList(permId),
                        fetchOptions);

        assertEquals(map.size(), 1);

        DataSet dataSet = map.get(permId);

        assertEquals(dataSet.getExperiment().getIdentifier().getIdentifier(), "/CISD/NEMO/EXP-TEST-1");

        assertTypeNotFetched(dataSet);
        assertPhysicalDataNotFetched(dataSet);
        assertSampleNotFetched(dataSet);
        assertPropertiesNotFetched(dataSet);
        assertParentsNotFetched(dataSet);
        assertChildrenNotFetched(dataSet);
        assertComponentsNotFetched(dataSet);
        assertContainersNotFetched(dataSet);
        assertModifierNotFetched(dataSet);
        assertRegistratorNotFetched(dataSet);
        assertTagsNotFetched(dataSet);
        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithSample()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId permId = new DataSetPermId("20081105092159111-1");

        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withSample();

        Map<IDataSetId, DataSet> map =
                v3api.getDataSets(sessionToken, Arrays.asList(permId),
                        fetchOptions);

        assertEquals(map.size(), 1);

        DataSet dataSet = map.get(permId);

        assertEquals(dataSet.getSample().getIdentifier().getIdentifier(), "/CISD/NEMO/CP-TEST-1");

        assertTypeNotFetched(dataSet);
        assertPhysicalDataNotFetched(dataSet);
        assertExperimentNotFetched(dataSet);
        assertPropertiesNotFetched(dataSet);
        assertParentsNotFetched(dataSet);
        assertChildrenNotFetched(dataSet);
        assertComponentsNotFetched(dataSet);
        assertContainersNotFetched(dataSet);
        assertModifierNotFetched(dataSet);
        assertRegistratorNotFetched(dataSet);
        assertTagsNotFetched(dataSet);
        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithProperties()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId permId = new DataSetPermId("20081105092159111-1");

        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withProperties();

        Map<IDataSetId, DataSet> map =
                v3api.getDataSets(sessionToken, Arrays.asList(permId),
                        fetchOptions);

        assertEquals(map.size(), 1);

        DataSet dataSet = map.get(permId);

        Map<String, Serializable> properties = dataSet.getProperties();
        assertEquals(properties.get("COMMENT"), "no comment");
        assertEquals(properties.get("GENDER"), "FEMALE");
        assertEquals(properties.get("BACTERIUM"), "BACTERIUM1 (BACTERIUM)");
        assertEquals(properties.get("ANY_MATERIAL"), "1000_C (SIRNA)");

        assertTypeNotFetched(dataSet);
        assertPhysicalDataNotFetched(dataSet);
        assertExperimentNotFetched(dataSet);
        assertSampleNotFetched(dataSet);
        assertParentsNotFetched(dataSet);
        assertChildrenNotFetched(dataSet);
        assertComponentsNotFetched(dataSet);
        assertContainersNotFetched(dataSet);
        assertModifierNotFetched(dataSet);
        assertRegistratorNotFetched(dataSet);
        assertTagsNotFetched(dataSet);
        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithChildren()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId permId = new DataSetPermId("20081105092159111-1");

        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withChildren();

        Map<IDataSetId, DataSet> map =
                v3api.getDataSets(sessionToken, Arrays.asList(permId),
                        fetchOptions);

        assertEquals(map.size(), 1);

        DataSet dataSet = map.get(permId);

        List<DataSet> children = dataSet.getChildren();

        assertEquals(children.size(), 1);

        DataSet child = children.get(0);
        assertEquals(child.getCode(), "20081105092259000-9");

        assertTypeNotFetched(dataSet);
        assertPhysicalDataNotFetched(dataSet);
        assertExperimentNotFetched(dataSet);
        assertSampleNotFetched(dataSet);
        assertPropertiesNotFetched(dataSet);
        assertParentsNotFetched(dataSet);
        assertComponentsNotFetched(dataSet);
        assertContainersNotFetched(dataSet);
        assertModifierNotFetched(dataSet);
        assertRegistratorNotFetched(dataSet);
        assertTagsNotFetched(dataSet);
        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithParents()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId permId = new DataSetPermId("20081105092259000-9");

        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withParents();

        Map<IDataSetId, DataSet> map =
                v3api.getDataSets(sessionToken, Arrays.asList(permId),
                        fetchOptions);

        assertEquals(map.size(), 1);

        DataSet dataSet = map.get(permId);

        List<DataSet> parents = dataSet.getParents();
        assertEquals(parents.get(0).isPostRegistered(), Boolean.TRUE);

        assertEquals(parents.size(), 3);

        assertIdentifiers(parents, "20081105092159111-1", "20081105092159222-2", "20081105092159333-3");

        assertTypeNotFetched(dataSet);
        assertPhysicalDataNotFetched(dataSet);
        assertExperimentNotFetched(dataSet);
        assertSampleNotFetched(dataSet);
        assertPropertiesNotFetched(dataSet);
        assertChildrenNotFetched(dataSet);
        assertComponentsNotFetched(dataSet);
        assertContainersNotFetched(dataSet);
        assertModifierNotFetched(dataSet);
        assertRegistratorNotFetched(dataSet);
        assertTagsNotFetched(dataSet);
        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithComponents()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId permId = new DataSetPermId("20110509092359990-10");

        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withComponents();

        Map<IDataSetId, DataSet> map =
                v3api.getDataSets(sessionToken, Arrays.asList(permId),
                        fetchOptions);

        assertEquals(map.size(), 1);

        DataSet dataSet = map.get(permId);

        List<DataSet> components = dataSet.getComponents();

        assertEquals(components.size(), 2);

        assertIdentifiers(components, "20110509092359990-11", "20110509092359990-12");

        assertTypeNotFetched(dataSet);
        assertPhysicalDataNotFetched(dataSet);
        assertExperimentNotFetched(dataSet);
        assertSampleNotFetched(dataSet);
        assertPropertiesNotFetched(dataSet);
        assertParentsNotFetched(dataSet);
        assertChildrenNotFetched(dataSet);
        assertContainersNotFetched(dataSet);
        assertModifierNotFetched(dataSet);
        assertRegistratorNotFetched(dataSet);
        assertTagsNotFetched(dataSet);
        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithContainers()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId permId = new DataSetPermId("20110509092359990-11");

        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withContainers();

        Map<IDataSetId, DataSet> map =
                v3api.getDataSets(sessionToken, Arrays.asList(permId),
                        fetchOptions);

        assertEquals(map.size(), 1);

        DataSet dataSet = map.get(permId);

        List<DataSet> containers = dataSet.getContainers();

        assertEquals(containers.size(), 1);

        assertIdentifiers(containers, "20110509092359990-10");

        assertTypeNotFetched(dataSet);
        assertPhysicalDataNotFetched(dataSet);
        assertExperimentNotFetched(dataSet);
        assertSampleNotFetched(dataSet);
        assertPropertiesNotFetched(dataSet);
        assertParentsNotFetched(dataSet);
        assertChildrenNotFetched(dataSet);
        assertComponentsNotFetched(dataSet);
        assertModifierNotFetched(dataSet);
        assertRegistratorNotFetched(dataSet);
        assertTagsNotFetched(dataSet);
        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithPhysicalDataForPhysicalDataSet()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId permId = new DataSetPermId("20081105092159111-1");
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();

        PhysicalDataFetchOptions physicalDataFetchOptions = fetchOptions.withPhysicalData();
        physicalDataFetchOptions.withFileFormatType();
        physicalDataFetchOptions.withLocatorType();
        physicalDataFetchOptions.withStorageFormat();

        Map<IDataSetId, DataSet> dataSets = v3api.getDataSets(sessionToken, Arrays.asList(permId), fetchOptions);

        assertEquals(dataSets.size(), 1);
        DataSet dataSet = dataSets.get(permId);

        assertEquals(dataSet.getCode(), "20081105092159111-1");

        PhysicalData physicalData = dataSet.getPhysicalData();
        assertEquals(physicalData.getShareId(), "42");
        assertEquals(physicalData.getLocation(), "a/1");
        assertEquals(physicalData.getSize(), Long.valueOf(4711));
        assertEquals(physicalData.getComplete(), Complete.UNKNOWN);
        assertEquals(physicalData.getStatus(), ArchivingStatus.AVAILABLE);
        assertFalse(physicalData.isPresentInArchive());
        assertFalse(physicalData.isStorageConfirmation());

        FileFormatType fileFormatType = physicalData.getFileFormatType();
        assertEquals(fileFormatType.getCode(), "TIFF");
        assertEquals(fileFormatType.getDescription(), "TIFF File");

        LocatorType locatorType = physicalData.getLocatorType();
        assertEquals(locatorType.getCode(), "RELATIVE_LOCATION");
        assertEquals(locatorType.getDescription(), "Relative Location");

        StorageFormat storageFormatTerm = physicalData.getStorageFormat();
        assertEquals(storageFormatTerm.getCode(), "PROPRIETARY");
        assertEquals(storageFormatTerm.getDescription(), "proprietary description");

        assertTypeNotFetched(dataSet);
        assertExperimentNotFetched(dataSet);
        assertSampleNotFetched(dataSet);
        assertPropertiesNotFetched(dataSet);
        assertParentsNotFetched(dataSet);
        assertChildrenNotFetched(dataSet);
        assertComponentsNotFetched(dataSet);
        assertContainersNotFetched(dataSet);
        assertModifierNotFetched(dataSet);
        assertRegistratorNotFetched(dataSet);
        assertTagsNotFetched(dataSet);
    }

    @Test
    public void testGetWithPhysicalDataForNonPhysicalDataSet()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId permId = new DataSetPermId("ROOT_CONTAINER");
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withPhysicalData();

        Map<IDataSetId, DataSet> dataSets = v3api.getDataSets(sessionToken, Arrays.asList(permId), fetchOptions);

        assertEquals(dataSets.size(), 1);
        DataSet dataSet = dataSets.get(permId);

        assertEquals(dataSet.getCode(), "ROOT_CONTAINER");
        assertEquals(dataSet.getPhysicalData(), null);
    }

    @Test
    public void testGetWithLinkedDataForLinkDataSet()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId permId = new DataSetPermId("20120628092259000-23");
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();

        LinkedDataFetchOptions linkedDataFetchOptions = fetchOptions.withLinkedData();
        linkedDataFetchOptions.withExternalDms();

        Map<IDataSetId, DataSet> dataSets = v3api.getDataSets(sessionToken, Arrays.asList(permId), fetchOptions);

        assertEquals(dataSets.size(), 1);
        DataSet dataSet = dataSets.get(permId);

        assertEquals(dataSet.getCode(), "20120628092259000-23");

        LinkedData linkedData = dataSet.getLinkedData();
        assertEquals(linkedData.getExternalCode(), "CODE1");
        assertEquals(linkedData.getExternalDms().getCode(), "DMS_1");
        assertEquals(linkedData.getExternalDms().getLabel(), "Test EDMS");
        assertEquals(linkedData.getExternalDms().getUrlTemplate(), "http://example.edms.pl/code=${code}");
        assertEquals(linkedData.getExternalDms().isOpenbis(), Boolean.FALSE);

        assertTypeNotFetched(dataSet);
        assertExperimentNotFetched(dataSet);
        assertSampleNotFetched(dataSet);
        assertPropertiesNotFetched(dataSet);
        assertParentsNotFetched(dataSet);
        assertChildrenNotFetched(dataSet);
        assertComponentsNotFetched(dataSet);
        assertContainersNotFetched(dataSet);
        assertModifierNotFetched(dataSet);
        assertRegistratorNotFetched(dataSet);
        assertTagsNotFetched(dataSet);
    }

    @Test
    public void testGetWithLinkedDataForNonLinkDataSet()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId permId = new DataSetPermId("ROOT_CONTAINER");
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withLinkedData();

        Map<IDataSetId, DataSet> dataSets = v3api.getDataSets(sessionToken, Arrays.asList(permId), fetchOptions);

        assertEquals(dataSets.size(), 1);
        DataSet dataSet = dataSets.get(permId);

        assertEquals(dataSet.getCode(), "ROOT_CONTAINER");
        assertEquals(dataSet.getLinkedData(), null);
    }

    @Test
    public void testGetWithModifier()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId permId = new DataSetPermId("20081105092159111-1");

        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withModifier();

        Map<IDataSetId, DataSet> map =
                v3api.getDataSets(sessionToken, Arrays.asList(permId),
                        fetchOptions);

        assertEquals(1, map.size());

        DataSet dataSet = map.get(permId);

        assertEquals(dataSet.getModifier().getUserId(), "test");
        assertEquals(dataSet.isPostRegistered(), Boolean.TRUE);

        assertTypeNotFetched(dataSet);
        assertPhysicalDataNotFetched(dataSet);
        assertExperimentNotFetched(dataSet);
        assertSampleNotFetched(dataSet);
        assertPropertiesNotFetched(dataSet);
        assertParentsNotFetched(dataSet);
        assertChildrenNotFetched(dataSet);
        assertComponentsNotFetched(dataSet);
        assertContainersNotFetched(dataSet);
        assertRegistratorNotFetched(dataSet);
        assertTagsNotFetched(dataSet);
        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithRegistrator()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId permId = new DataSetPermId("20081105092259000-19");

        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withRegistrator();

        Map<IDataSetId, DataSet> map =
                v3api.getDataSets(sessionToken, Arrays.asList(permId),
                        fetchOptions);

        assertEquals(1, map.size());

        DataSet dataSet = map.get(permId);

        assertEquals(dataSet.getRegistrator().getUserId(), "test");

        assertTypeNotFetched(dataSet);
        assertPhysicalDataNotFetched(dataSet);
        assertExperimentNotFetched(dataSet);
        assertSampleNotFetched(dataSet);
        assertPropertiesNotFetched(dataSet);
        assertParentsNotFetched(dataSet);
        assertChildrenNotFetched(dataSet);
        assertComponentsNotFetched(dataSet);
        assertContainersNotFetched(dataSet);
        assertModifierNotFetched(dataSet);
        assertTagsNotFetched(dataSet);
        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithTags()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        TagFetchOptions tagfe = fetchOptions.withTags();
        tagfe.withOwner();

        DataSetPermId permId = new DataSetPermId("20120619092259000-22");

        Map<IDataSetId, DataSet> map = v3api.getDataSets(sessionToken, Collections.singletonList(permId), fetchOptions);

        assertEquals(map.size(), 1);

        DataSet dataSet = map.get(permId);

        Set<Tag> tags = dataSet.getTags();

        assertEquals(tags.size(), 1);

        Tag tag = tags.iterator().next();
        assertEquals(tag.getOwner().getUserId(), TEST_USER);
        assertEquals(tag.getCode(), "TEST_METAPROJECTS");
        assertEquals(tag.getPermId().getPermId(), "/" + TEST_USER + "/TEST_METAPROJECTS");

        assertTypeNotFetched(dataSet);
        assertPhysicalDataNotFetched(dataSet);
        assertExperimentNotFetched(dataSet);
        assertSampleNotFetched(dataSet);
        assertPropertiesNotFetched(dataSet);
        assertParentsNotFetched(dataSet);
        assertChildrenNotFetched(dataSet);
        assertComponentsNotFetched(dataSet);
        assertContainersNotFetched(dataSet);
        assertModifierNotFetched(dataSet);
        assertRegistratorNotFetched(dataSet);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithMaterialProperties()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withMaterialProperties().withRegistrator();
        fetchOptions.withProperties();

        DataSetPermId permId = new DataSetPermId("20081105092159111-1");

        Map<IDataSetId, DataSet> map = v3api.getDataSets(sessionToken, Arrays.asList(permId), fetchOptions);

        DataSet data = map.get(permId);

        assertEquals(data.getProperties().get("ANY_MATERIAL"), "1000_C (SIRNA)");
        assertEquals(data.getProperties().get("BACTERIUM"), "BACTERIUM1 (BACTERIUM)");

        Map<String, Material> materialProperties = data.getMaterialProperties();

        Material gene = materialProperties.get("ANY_MATERIAL");
        assertEquals(gene.getPermId(), new MaterialPermId("1000_C", "SIRNA"));
        assertEquals(gene.getRegistrator().getUserId(), "test");
        assertTagsNotFetched(gene);

        Material bacterium = materialProperties.get("BACTERIUM");
        assertEquals(bacterium.getPermId(), new MaterialPermId("BACTERIUM1", "BACTERIUM"));
        assertEquals(bacterium.getRegistrator().getUserId(), "etlserver");
        assertTagsNotFetched(bacterium);
    }

    @Test
    public void testGetWithHistoryNoChanges()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        IDataSetId id = new DataSetPermId("COMPONENT_1A");

        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withHistory();

        Map<IDataSetId, DataSet> map = v3api.getDataSets(sessionToken, Arrays.asList(id), fetchOptions);

        assertEquals(map.size(), 1);
        DataSet dataSet = map.get(id);

        assertEquals(dataSet.isPostRegistered(), Boolean.FALSE);

        List<HistoryEntry> history = dataSet.getHistory();
        assertEquals(history.size(), 2);

        assertPropertyHistory(history.get(0), "COMMENT", "co comment");
        assertRelationshipHistory(history.get(1), new DataSetPermId("CONTAINER_1"), DataSetRelationType.PARENT);

        assertNotFetched(dataSet::getPropertiesHistory);
        assertNotFetched(dataSet::getExperimentHistory);
        assertNotFetched(dataSet::getSampleHistory);
        assertNotFetched(dataSet::getParentsHistory);
        assertNotFetched(dataSet::getChildrenHistory);
        assertNotFetched(dataSet::getContainersHistory);
        assertNotFetched(dataSet::getComponentsHistory);
        assertNotFetched(dataSet::getContentCopiesHistory);
        assertNotFetched(dataSet::getUnknownHistory);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithHistoryProperty()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        IDataSetId id = new DataSetPermId("COMPONENT_1A");

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(id);
        update.setProperty("COMMENT", "new comment");

        v3api.updateDataSets(sessionToken, Arrays.asList(update));

        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withHistory();
        fetchOptions.withPropertiesHistory();

        Map<IDataSetId, DataSet> map = v3api.getDataSets(sessionToken, Arrays.asList(id), fetchOptions);

        assertEquals(map.size(), 1);
        DataSet dataSet = map.get(id);

        assertEquals(dataSet.getHistory().size(), 3);
        assertPropertyHistory(dataSet.getHistory().get(0), "COMMENT", "co comment");
        assertPropertyHistory(dataSet.getHistory().get(1), "COMMENT", "new comment", dataSet.getModificationDate(), null);
        assertRelationshipHistory(dataSet.getHistory().get(2), new DataSetPermId("CONTAINER_1"), DataSetRelationType.PARENT);

        assertEquals(dataSet.getPropertiesHistory().size(), 2);
        assertPropertyHistory(dataSet.getPropertiesHistory().get(0), "COMMENT", "co comment");
        assertPropertyHistory(dataSet.getPropertiesHistory().get(1), "COMMENT", "new comment", dataSet.getModificationDate(), null);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetWithHistoryContentCopy()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        // given - data set with content copy
        ContentCopyCreation contentCopyCreation = getContentCopyCreation();
        DataSetCreation dataSetCreation = getDataSetCreation(contentCopyCreation, "testGetWithHistoryContentCopy-01");

        List<DataSetPermId> dataSetIds = v3api.createDataSets(sessionToken, Arrays.asList(dataSetCreation));
        assertEquals(dataSetIds.size(), 1);

        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withHistory();
        fetchOptions.withContentCopiesHistory();
        fetchOptions.withLinkedData();

        IDataSetId id = dataSetIds.get(0);

        Map<IDataSetId, DataSet> map = v3api.getDataSets(sessionToken, Arrays.asList(id), fetchOptions);
        DataSet dataSet = map.get(id);

        assertEquals(dataSet.getLinkedData().getContentCopies().size(), 1);

        assertEquals(dataSet.getHistory().size(), 2);
        assertRelationshipHistory(dataSet.getHistory().get(0), new SamplePermId("200811050919915-9"), DataSetRelationType.SAMPLE,
                dataSet.getRegistrationDate(), null);
        ContentCopyHistoryEntry entry = (ContentCopyHistoryEntry) dataSet.getHistory().get(1);
        assertEquals(entry.getPath(), contentCopyCreation.getPath());
        assertEquals(entry.getGitCommitHash(), contentCopyCreation.getGitCommitHash());
        assertEquals(entry.getExternalDmsAddress(), "sprint:/path/to/location");
        assertEquals(entry.getValidFrom(), dataSet.getRegistrationDate());
        assertEquals(entry.getValidTo(), null);

        assertEquals(dataSet.getContentCopiesHistory().size(), 1);
        entry = (ContentCopyHistoryEntry) dataSet.getContentCopiesHistory().get(0);
        assertEquals(entry.getPath(), contentCopyCreation.getPath());
        assertEquals(entry.getGitCommitHash(), contentCopyCreation.getGitCommitHash());
        assertEquals(entry.getExternalDmsAddress(), "sprint:/path/to/location");
        assertEquals(entry.getValidFrom(), dataSet.getRegistrationDate());
        assertEquals(entry.getValidTo(), null);

        // when - remove content copy
        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(id);

        LinkedDataUpdate linkedDataUpdate = new LinkedDataUpdate();
        linkedDataUpdate.getContentCopies().remove(dataSet.getLinkedData().getContentCopies().get(0).getId());

        update.setLinkedData(linkedDataUpdate);

        v3api.updateDataSets(sessionToken, Arrays.asList(update));

        // then - content copy is in history
        map = v3api.getDataSets(sessionToken, Arrays.asList(id), fetchOptions);
        dataSet = map.get(id);

        assertEquals(dataSet.getHistory().size(), 2);
        assertRelationshipHistory(dataSet.getHistory().get(0), new SamplePermId("200811050919915-9"), DataSetRelationType.SAMPLE,
                dataSet.getRegistrationDate(), null);

        assertEquals(dataSet.getLinkedData().getContentCopies().size(), 0);
        entry = (ContentCopyHistoryEntry) dataSet.getHistory().get(1);
        assertEquals(entry.getPath(), contentCopyCreation.getPath());
        assertEquals(entry.getGitCommitHash(), contentCopyCreation.getGitCommitHash());
        assertEquals(entry.getExternalDmsAddress(), "sprint:/path/to/location");
        assertEquals(entry.getValidFrom(), dataSet.getRegistrationDate());
        assertEquals(entry.getValidTo(), dataSet.getModificationDate());

        assertEquals(dataSet.getContentCopiesHistory().size(), 1);
        entry = (ContentCopyHistoryEntry) dataSet.getContentCopiesHistory().get(0);
        assertEquals(entry.getPath(), contentCopyCreation.getPath());
        assertEquals(entry.getGitCommitHash(), contentCopyCreation.getGitCommitHash());
        assertEquals(entry.getExternalDmsAddress(), "sprint:/path/to/location");
        assertEquals(entry.getValidFrom(), dataSet.getRegistrationDate());
        assertEquals(entry.getValidTo(), dataSet.getModificationDate());

        v3api.logout(sessionToken);
    }

    private DataSetCreation getDataSetCreation(ContentCopyCreation contentCopyCreation, String code)
    {
        LinkedDataCreation linkedData = new LinkedDataCreation();
        linkedData.setContentCopies(Arrays.asList(contentCopyCreation));
        DataSetCreation dataSetCreation = new DataSetCreation();
        dataSetCreation.setCode(code);
        dataSetCreation.setLinkedData(linkedData);
        dataSetCreation.setDataSetKind(DataSetKind.LINK);
        dataSetCreation.setTypeId(new EntityTypePermId("UNKNOWN"));
        dataSetCreation.setSampleId(new SampleIdentifier("//CL1:A01"));
        dataSetCreation.setDataStoreId(new DataStorePermId("STANDARD"));
        return dataSetCreation;
    }

    private ContentCopyCreation getContentCopyCreation()
    {
        ContentCopyCreation contentCopyCreation = new ContentCopyCreation();
        contentCopyCreation.setExternalDmsId(new ExternalDmsPermId("DMS_3"));
        contentCopyCreation.setPath("/path");
        contentCopyCreation.setGitCommitHash("0");
        contentCopyCreation.setGitRepositoryId("0");
        return contentCopyCreation;
    }

    @Test
    public void testGetWithHistoryExperiment()
    {
        IDataSetId id = new DataSetPermId("COMPONENT_1A");

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(id);
        update.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP1"));

        DataSetUpdate update2 = new DataSetUpdate();
        update2.setDataSetId(id);
        update2.setExperimentId(new ExperimentIdentifier("/CISD/DEFAULT/EXP-REUSE"));

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withHistory();
        fo.withExperimentHistory();

        DataSet dataSet = testGetWithHistory(fo, update, update2);

        assertEquals(dataSet.getHistory().size(), 4);
        assertPropertyHistory(dataSet.getHistory().get(0), "COMMENT", "co comment");
        assertRelationshipHistory(dataSet.getHistory().get(1), new ExperimentPermId("200811050951882-1028"), DataSetRelationType.EXPERIMENT);
        assertRelationshipHistory(dataSet.getHistory().get(2), new ExperimentPermId("200811050940555-1032"), DataSetRelationType.EXPERIMENT);
        assertRelationshipHistory(dataSet.getHistory().get(3), new DataSetPermId("CONTAINER_1"), DataSetRelationType.PARENT);

        assertEquals(dataSet.getExperimentHistory().size(), 2);
        assertRelationshipHistory(dataSet.getExperimentHistory().get(0), new ExperimentPermId("200811050951882-1028"),
                DataSetRelationType.EXPERIMENT);
        assertRelationshipHistory(dataSet.getExperimentHistory().get(1), new ExperimentPermId("200811050940555-1032"),
                DataSetRelationType.EXPERIMENT);

        assertNotFetched(dataSet::getPropertiesHistory);
        assertNotFetched(dataSet::getParentsHistory);
    }

    @Test
    public void testGetWithHistorySample()
    {
        IDataSetId id = new DataSetPermId("COMPONENT_1A");

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(id);
        update.setSampleId(new SampleIdentifier("/CISD/NEMO/3VCP5"));

        DataSetUpdate update2 = new DataSetUpdate();
        update2.setDataSetId(id);
        update2.setSampleId(new SampleIdentifier("/CISD/NEMO/3VCP6"));

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withHistory();
        fo.withSampleHistory();

        DataSet dataSet = testGetWithHistory(fo, update, update2);

        assertEquals(dataSet.getHistory().size(), 4);
        assertPropertyHistory(dataSet.getHistory().get(0), "COMMENT", "co comment");
        assertRelationshipHistory(dataSet.getHistory().get(1), new SamplePermId("200811050946559-979"), DataSetRelationType.SAMPLE);
        assertRelationshipHistory(dataSet.getHistory().get(2), new SamplePermId("200811050946559-980"), DataSetRelationType.SAMPLE);
        assertRelationshipHistory(dataSet.getHistory().get(3), new DataSetPermId("CONTAINER_1"), DataSetRelationType.PARENT);

        assertEquals(dataSet.getSampleHistory().size(), 2);
        assertRelationshipHistory(dataSet.getSampleHistory().get(0), new SamplePermId("200811050946559-979"), DataSetRelationType.SAMPLE);
        assertRelationshipHistory(dataSet.getSampleHistory().get(1), new SamplePermId("200811050946559-980"), DataSetRelationType.SAMPLE);

        assertNotFetched(dataSet::getPropertiesHistory);
        assertNotFetched(dataSet::getParentsHistory);
    }

    @Test
    public void testGetWithHistoryContainer()
    {
        IDataSetId id = new DataSetPermId("COMPONENT_1A");

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(id);
        update.getContainerIds().set(new DataSetPermId("CONTAINER_2"));

        DataSetUpdate update2 = new DataSetUpdate();
        update2.setDataSetId(id);
        update2.getContainerIds().set(new DataSetPermId("CONTAINER_1"));

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withHistory();
        fo.withContainersHistory();

        DataSet dataSet = testGetWithHistory(fo, update, update2);

        assertEquals(dataSet.getHistory().size(), 4);
        assertPropertyHistory(dataSet.getHistory().get(0), "COMMENT", "co comment");
        assertRelationshipHistory(dataSet.getHistory().get(1), new DataSetPermId("CONTAINER_1"), DataSetRelationType.PARENT);
        assertRelationshipHistory(dataSet.getHistory().get(2), new DataSetPermId("CONTAINER_2"), DataSetRelationType.CONTAINER);
        assertRelationshipHistory(dataSet.getHistory().get(3), new DataSetPermId("CONTAINER_1"), DataSetRelationType.CONTAINER);

        assertEquals(dataSet.getContainersHistory().size(), 2);
        assertRelationshipHistory(dataSet.getContainersHistory().get(0), new DataSetPermId("CONTAINER_2"), DataSetRelationType.CONTAINER);
        assertRelationshipHistory(dataSet.getContainersHistory().get(1), new DataSetPermId("CONTAINER_1"), DataSetRelationType.CONTAINER);

        assertNotFetched(dataSet::getPropertiesHistory);
        assertNotFetched(dataSet::getParentsHistory);
    }

    @Test
    public void testGetWithHistoryComponents()
    {
        IDataSetId id = new DataSetPermId("CONTAINER_1");

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(id);
        update.getComponentIds().set(new DataSetPermId("COMPONENT_2A"));

        DataSetUpdate update2 = new DataSetUpdate();
        update2.setDataSetId(id);
        update2.getComponentIds().set(new DataSetPermId("COMPONENT_1A"));

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withHistory();
        fo.withComponentsHistory();

        DataSet dataSet = testGetWithHistory(fo, update, update2);

        assertEquals(dataSet.getHistory().size(), 5);
        assertRelationshipHistory(dataSet.getHistory().get(0), new DataSetPermId("ROOT_CONTAINER"), DataSetRelationType.PARENT);
        assertRelationshipHistory(dataSet.getHistory().get(1), new DataSetPermId("COMPONENT_1A"), DataSetRelationType.CHILD);
        assertRelationshipHistory(dataSet.getHistory().get(2), new DataSetPermId("COMPONENT_1B"), DataSetRelationType.CHILD);
        assertRelationshipHistory(dataSet.getHistory().get(3), new DataSetPermId("COMPONENT_2A"), DataSetRelationType.COMPONENT);
        assertRelationshipHistory(dataSet.getHistory().get(4), new DataSetPermId("COMPONENT_1A"), DataSetRelationType.COMPONENT);

        assertEquals(dataSet.getComponentsHistory().size(), 2);
        assertRelationshipHistory(dataSet.getComponentsHistory().get(0), new DataSetPermId("COMPONENT_2A"), DataSetRelationType.COMPONENT);
        assertRelationshipHistory(dataSet.getComponentsHistory().get(1), new DataSetPermId("COMPONENT_1A"), DataSetRelationType.COMPONENT);

        assertNotFetched(dataSet::getParentsHistory);
        assertNotFetched(dataSet::getChildrenHistory);
    }

    @Test
    public void testGetWithHistoryParent()
    {
        IDataSetId id = new DataSetPermId("COMPONENT_1A");

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(id);
        update.getParentIds().set(new DataSetPermId("CONTAINER_2"));

        DataSetUpdate update2 = new DataSetUpdate();
        update2.setDataSetId(id);
        update2.getParentIds().set(new DataSetPermId("CONTAINER_1"));

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withHistory();
        fo.withParentsHistory();

        DataSet dataSet = testGetWithHistory(fo, update, update2);

        assertEquals(dataSet.getHistory().size(), 4);
        assertPropertyHistory(dataSet.getHistory().get(0), "COMMENT", "co comment");
        assertRelationshipHistory(dataSet.getHistory().get(1), new DataSetPermId("CONTAINER_1"), DataSetRelationType.PARENT);
        assertRelationshipHistory(dataSet.getHistory().get(2), new DataSetPermId("CONTAINER_2"), DataSetRelationType.PARENT);
        assertRelationshipHistory(dataSet.getHistory().get(3), new DataSetPermId("CONTAINER_1"), DataSetRelationType.PARENT);

        assertEquals(dataSet.getParentsHistory().size(), 3);
        assertRelationshipHistory(dataSet.getParentsHistory().get(0), new DataSetPermId("CONTAINER_1"), DataSetRelationType.PARENT);
        assertRelationshipHistory(dataSet.getParentsHistory().get(1), new DataSetPermId("CONTAINER_2"), DataSetRelationType.PARENT);
        assertRelationshipHistory(dataSet.getParentsHistory().get(2), new DataSetPermId("CONTAINER_1"), DataSetRelationType.PARENT);

        assertNotFetched(dataSet::getPropertiesHistory);
    }

    @Test
    public void testGetWithHistoryChild()
    {
        IDataSetId id = new DataSetPermId("CONTAINER_1");

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(id);
        update.getChildIds().set(new DataSetPermId("COMPONENT_2A"));

        DataSetUpdate update2 = new DataSetUpdate();
        update2.setDataSetId(id);
        update2.getChildIds().set(new DataSetPermId("COMPONENT_1A"));

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withHistory();
        fo.withChildrenHistory();

        DataSet dataSet = testGetWithHistory(fo, update, update2);

        assertEquals(dataSet.getHistory().size(), 5);
        assertRelationshipHistory(dataSet.getHistory().get(0), new DataSetPermId("ROOT_CONTAINER"), DataSetRelationType.PARENT);
        assertRelationshipHistory(dataSet.getHistory().get(1), new DataSetPermId("COMPONENT_1A"), DataSetRelationType.CHILD);
        assertRelationshipHistory(dataSet.getHistory().get(2), new DataSetPermId("COMPONENT_1B"), DataSetRelationType.CHILD);
        assertRelationshipHistory(dataSet.getHistory().get(3), new DataSetPermId("COMPONENT_2A"), DataSetRelationType.CHILD);
        assertRelationshipHistory(dataSet.getHistory().get(4), new DataSetPermId("COMPONENT_1A"), DataSetRelationType.CHILD);

        assertEquals(dataSet.getChildrenHistory().size(), 4);
        assertRelationshipHistory(dataSet.getChildrenHistory().get(0), new DataSetPermId("COMPONENT_1A"), DataSetRelationType.CHILD);
        assertRelationshipHistory(dataSet.getChildrenHistory().get(1), new DataSetPermId("COMPONENT_1B"), DataSetRelationType.CHILD);
        assertRelationshipHistory(dataSet.getChildrenHistory().get(2), new DataSetPermId("COMPONENT_2A"), DataSetRelationType.CHILD);
        assertRelationshipHistory(dataSet.getChildrenHistory().get(3), new DataSetPermId("COMPONENT_1A"), DataSetRelationType.CHILD);

        assertNotFetched(dataSet::getParentsHistory);
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testGetWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        List<IDataSetId> ids = Arrays.asList(new DataSetPermId("20120619092259000-22"),
                new DataSetPermId("20081105092159188-3"));

        String sessionToken = v3api.login(user.getUserId(), PASSWORD);

        if (user.isDisabledProjectUser())
        {
            assertAuthorizationFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.getDataSets(sessionToken, ids, new DataSetFetchOptions());
                }
            });
        } else
        {
            Map<IDataSetId, DataSet> result = v3api.getDataSets(sessionToken, ids, new DataSetFetchOptions());

            if (user.isInstanceUser())
            {
                assertEquals(result.size(), 2);
            } else if (user.isTestSpaceUser() || user.isTestProjectUser())
            {
                assertEquals(result.size(), 1);
                assertEquals(result.values().iterator().next().getCode(), "20120619092259000-22");
            } else
            {
                assertEquals(result.size(), 0);
            }
        }

        v3api.logout(sessionToken);
    }

    private DataSet testGetWithHistory(DataSetFetchOptions fo, DataSetUpdate... updates)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        IDataSetId id = updates[0].getDataSetId();

        for (DataSetUpdate update : updates)
        {
            v3api.updateDataSets(sessionToken, Arrays.asList(update));
        }

        Map<IDataSetId, DataSet> map = v3api.getDataSets(sessionToken, Arrays.asList(id), fo);

        assertEquals(map.size(), 1);
        DataSet dataSet = map.get(id);

        v3api.logout(sessionToken);

        return dataSet;
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withSample();
        fo.withExperiment();

        v3api.getDataSets(sessionToken, Arrays.asList(new DataSetPermId("20081105092159111-1"), new DataSetPermId("20110509092359990-10")), fo);

        assertAccessLog(
                "get-data-sets  DATA_SET_IDS('[20081105092159111-1, 20110509092359990-10]') FETCH_OPTIONS('DataSet\n    with Experiment\n    with Sample\n')");
    }

}

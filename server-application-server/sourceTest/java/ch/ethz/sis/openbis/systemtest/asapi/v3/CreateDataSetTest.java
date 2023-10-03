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
import static org.testng.Assert.assertEqualsNoOrder;
import static org.testng.Assert.assertNull;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.CreationId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.Complete;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.archive.DataSetArchiveOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.LinkedDataCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.PhysicalDataCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.BdsDirectoryStorageFormatPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.FileFormatTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IFileFormatTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.ILocatorTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IStorageFormatId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.LocatorTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.ProprietaryStorageFormatPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.RelativeLocationLocatorTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.StorageFormatPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.unarchive.DataSetUnarchiveOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.id.DataStorePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.id.IDataStoreId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.delete.ExperimentDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.ExternalDmsPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.IExternalDmsId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete.SampleDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagCode;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.systemtest.authorization.ProjectAuthorizationUser;

/**
 * @author pkupczyk
 */
public class CreateDataSetTest extends AbstractDataSetTest
{
    private static final PropertyTypePermId PLATE_GEOMETRY = new PropertyTypePermId("$PLATE_GEOMETRY");

    @Test
    public void testCreateLinkDataSetWithSpaceUser()
    {
        // given
        String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);
        String code = UUID.randomUUID().toString();
        LinkedDataCreation linkedData = new LinkedDataCreation();
        linkedData.setExternalDmsId(new ExternalDmsPermId("DMS_1"));
        linkedData.setExternalCode("test-" + System.currentTimeMillis());
        DataSetCreation creation = new DataSetCreation();
        creation.setCode(code);
        creation.setDataSetKind(DataSetKind.LINK);
        creation.setTypeId(new EntityTypePermId("LINK_TYPE"));
        creation.setExperimentId(new ExperimentIdentifier("/TEST-SPACE/NOE/EXP-TEST-2"));
        creation.setDataStoreId(new DataStorePermId("STANDARD"));
        creation.setLinkedData(linkedData);
        creation.setCreationId(new CreationId(code));

        // when
        List<DataSetPermId> dataSetIds = v3api.createDataSets(sessionToken, Collections.singletonList(creation));

        // then
        assertEquals(dataSetIds.size(), 1);
        assertEquals(dataSetIds.get(0).getPermId(), code.toUpperCase());
        assertDataSetKind(sessionToken, dataSetIds, DataSetKind.LINK);
    }

    private void assertDataSetKind(String sessionToken, List<DataSetPermId> dataSetIds, DataSetKind kind)
    {
        Map<IDataSetId, DataSet> dataSets = v3api.getDataSets(sessionToken, dataSetIds, new DataSetFetchOptions());
        assertEquals(dataSets.size(), 1);
        assertEquals(dataSets.get(dataSetIds.get(0)).getKind(), kind);
    }

    @Test
    public void testCreateContainerDataSetWithSpaceUser()
    {
        // given
        String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);
        String code = UUID.randomUUID().toString();
        DataSetCreation creation = new DataSetCreation();
        creation.setCode(code);
        creation.setDataSetKind(DataSetKind.CONTAINER);
        creation.setTypeId(new EntityTypePermId("CONTAINER_TYPE"));
        creation.setExperimentId(new ExperimentIdentifier("/TEST-SPACE/NOE/EXP-TEST-2"));
        creation.setDataStoreId(new DataStorePermId("STANDARD"));
        creation.setCreationId(new CreationId(code));

        // when
        List<DataSetPermId> dataSetIds = v3api.createDataSets(sessionToken, Collections.singletonList(creation));

        // then
        assertEquals(dataSetIds.size(), 1);
        assertEquals(dataSetIds.get(0).getPermId(), code.toUpperCase());
        assertDataSetKind(sessionToken, dataSetIds, DataSetKind.CONTAINER);
    }

    @Test
    public void testDataSetWhichDefaultsToPhysical()
    {
        // given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        DataSetCreation creation = physicalDataSetCreation();
        creation.setDataSetKind(null);

        // when
        List<DataSetPermId> dataSetIds = v3api.createDataSets(sessionToken, Collections.singletonList(creation));

        // then
        assertEquals(dataSetIds.size(), 1);
        assertEquals(dataSetIds.get(0).getPermId(), creation.getCode().toUpperCase());
        assertDataSetKind(sessionToken, dataSetIds, DataSetKind.PHYSICAL);
    }

    @Test
    public void testCreateDSWithAdminUserInBehalfOfASpaceObserver()
    {
        final DataSetPermId permId = new DataSetPermId("NO_SHALL_CREATE");

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken = v3api.loginAs(TEST_USER, PASSWORD, TEST_OBSERVER_CISD);

                    PhysicalDataCreation physicalCreation = new PhysicalDataCreation();
                    physicalCreation.setLocation("test/location/" + permId.getPermId());
                    physicalCreation.setFileFormatTypeId(new FileFormatTypePermId("TIFF"));
                    physicalCreation.setLocatorTypeId(new RelativeLocationLocatorTypePermId());
                    physicalCreation.setStorageFormatId(new ProprietaryStorageFormatPermId());

                    DataSetCreation creation = new DataSetCreation();
                    creation.setCode(permId.getPermId());
                    creation.setTypeId(new EntityTypePermId("UNKNOWN"));
                    creation.setDataStoreId(new DataStorePermId("STANDARD"));
                    creation.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP1"));
                    creation.setPhysicalData(physicalCreation);
                    creation.setCreationId(new CreationId(permId.getPermId()));

                    v3api.createDataSets(sessionToken, Collections.singletonList(creation));
                }
            }, "Access denied to object with DataSetPermId = [NO_SHALL_CREATE]");
    }

    @Test
    public void testCreateDSForSampleWithAdminUserInBehalfOfASpaceObserver()
    {
        final DataSetPermId permId = new DataSetPermId("NO_SHALL_CREATE");

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken = v3api.loginAs(TEST_USER, PASSWORD, TEST_OBSERVER_CISD);

                    PhysicalDataCreation physicalCreation = new PhysicalDataCreation();
                    physicalCreation.setLocation("test/location/" + permId.getPermId());
                    physicalCreation.setFileFormatTypeId(new FileFormatTypePermId("TIFF"));
                    physicalCreation.setLocatorTypeId(new RelativeLocationLocatorTypePermId());
                    physicalCreation.setStorageFormatId(new ProprietaryStorageFormatPermId());

                    DataSetCreation creation = new DataSetCreation();
                    creation.setCode(permId.getPermId());
                    creation.setTypeId(new EntityTypePermId("UNKNOWN"));
                    creation.setSampleId(new SampleIdentifier("/CISD/C1"));
                    creation.setDataStoreId(new DataStorePermId("STANDARD"));
                    creation.setPhysicalData(physicalCreation);
                    creation.setCreationId(new CreationId(permId.getPermId()));

                    v3api.createDataSets(sessionToken, Collections.singletonList(creation));
                }
            }, "Access denied to object with DataSetPermId = [NO_SHALL_CREATE]");
    }

    @Test
    public void testArchiveWithAdminUserInAnotherSpace()
    {
        assertAuthorizationFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    List<DataSetPermId> permIds = testCreateWithIndexCheck();
                    String sessionToken = v3api.login(TEST_OBSERVER_CISD, PASSWORD);
                    v3api.archiveDataSets(sessionToken, permIds, new DataSetArchiveOptions());
                }
            });
    }

    @Test
    public void testUnArchiveWithAdminUserInAnotherSpace()
    {
        assertAuthorizationFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    List<DataSetPermId> permIds = testCreateWithIndexCheck();
                    String sessionToken = v3api.login(TEST_OBSERVER_CISD, PASSWORD);
                    v3api.unarchiveDataSets(sessionToken, permIds, new DataSetUnarchiveOptions());
                }
            });
    }

    @Test
    public List<DataSetPermId> testCreateWithIndexCheck()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetCreation dataSet = physicalDataSetCreation();

        List<DataSetPermId> permIds = v3api.createDataSets(sessionToken, Arrays.asList(dataSet));

        assertDataSetsExists(permIds.get(0).getPermId());
        return permIds;
    }

    @Test
    public void testCreateWithNonAutogeneratedCodeNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final DataSetCreation dataSet = physicalDataSetCreation();
        dataSet.setCode(null);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createDataSets(sessionToken, Arrays.asList(dataSet));
                }
            }, "Code cannot be empty for a non auto generated code");
    }

    @Test
    public void testCreateWithAutogeneratedCodeNotNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final DataSetCreation dataSet = physicalDataSetCreation();
        dataSet.setCode("DATASET_WITH_USER_GIVEN_CODE");
        dataSet.setAutoGeneratedCode(true);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createDataSets(sessionToken, Arrays.asList(dataSet));
                }
            }, "Code should be empty when auto generated code is selected");
    }

    @Test
    public void testCreateWithAutogeneratedCodeNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final DataSetCreation dataSet1 = physicalDataSetCreation();
        dataSet1.setCode(null);
        dataSet1.setAutoGeneratedCode(true);
        final DataSetCreation dataSet2 = physicalDataSetCreation();
        dataSet2.setCode(null);
        dataSet2.setAutoGeneratedCode(true);

        List<DataSetPermId> datasetWithAutogeneratedCode = v3api.createDataSets(sessionToken, Arrays.asList(dataSet1, dataSet2));
        AssertionUtil.assertCollectionSize(datasetWithAutogeneratedCode, 2);
    }

    @Test
    public void testCreateWithCodeExisting()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation dataSet = physicalDataSetCreation();
        dataSet.setCode("DATA_SET_WITH_EXISTING_CODE");
        v3api.createDataSets(sessionToken, Arrays.asList(dataSet));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createDataSets(sessionToken, Arrays.asList(dataSet));
                }
            }, "DataSet already exists in the database and needs to be unique");
    }

    @Test
    public void testCreateWithCodeIncorrect()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final DataSetCreation dataSet = physicalDataSetCreation();
        dataSet.setCode("?!*");

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createDataSets(sessionToken, Arrays.asList(dataSet));
                }
            }, "Given code '?!*' contains illegal characters (allowed: A-Z, a-z, 0-9 and _, -, .)");
    }

    @Test
    public void testCreateWithTypeNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation dataSet = physicalDataSetCreation();
        dataSet.setTypeId(null);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createDataSets(sessionToken, Arrays.asList(dataSet));
                }
            }, "Type id cannot be null");
    }

    @Test
    public void testCreateWithTypeNonexistent()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final IEntityTypeId typeId = new EntityTypePermId("IDONTEXIST");
        final DataSetCreation dataSet = physicalDataSetCreation();
        dataSet.setTypeId(typeId);

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createDataSets(sessionToken, Arrays.asList(dataSet));
                }
            }, typeId);
    }

    @Test
    public void testCreateWithPropertyCodeNonexistent()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.setProperty("NONEXISTENT_PROPERTY_CODE", "any value");

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createDataSets(sessionToken, Arrays.asList(creation));
                }
            }, "Property type with code 'NONEXISTENT_PROPERTY_CODE' does not exist");
    }

    @Test
    public void testCreateWithPropertyValueIncorrect()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.setTypeId(new EntityTypePermId("HCS_IMAGE"));
        creation.setProperty("GENDER", "NON_EXISTENT_GENDER");

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createDataSets(sessionToken, Arrays.asList(creation));
                }
            }, "Vocabulary value 'NON_EXISTENT_GENDER' of property 'GENDER' is not valid. It must exist in 'GENDER' controlled vocabulary [MALE, FEMALE]");
    }

    @Test
    public void testCreateWithPropertyValueMandatoryButNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.setTypeId(new EntityTypePermId("HCS_IMAGE"));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createDataSets(sessionToken, Arrays.asList(creation));
                }
            }, "Value of mandatory property 'COMMENT' not specified");
    }

    @Test
    public void testCreateWithDataStoreNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.setDataStoreId(null);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createDataSets(sessionToken, Arrays.asList(creation));
                }
            }, "Data store id cannot be null.");
    }

    @Test
    public void testCreateWithDataStoreNonexistent()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final IDataStoreId dataStoreId = new DataStorePermId("IDONTEXIST");
        final DataSetCreation creation = physicalDataSetCreation();
        creation.setDataStoreId(dataStoreId);

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createDataSets(sessionToken, Arrays.asList(creation));
                }
            }, dataStoreId);
    }

    @Test
    public void testCreateWithMeasuredTrue()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.setMeasured(true);

        DataSet dataSet = createDataSet(sessionToken, creation, new DataSetFetchOptions());
        assertEquals(dataSet.getCode(), creation.getCode().toUpperCase());
        assertEquals(dataSet.isMeasured(), Boolean.TRUE);
    }

    @Test
    public void testCreateWithMeasuredFalse()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.setMeasured(false);

        DataSet dataSet = createDataSet(sessionToken, creation, new DataSetFetchOptions());
        assertEquals(dataSet.getCode(), creation.getCode().toUpperCase());
        assertEquals(dataSet.isMeasured(), Boolean.FALSE);
    }

    @Test
    public void testCreateWithTagsExisting()
    {
        final String sessionToken = v3api.loginAs(TEST_USER, PASSWORD, TEST_SPACE_USER);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.setExperimentId(new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));
        creation.setTagIds(Arrays.asList(new TagCode("TEST_METAPROJECTS")));

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withTags();

        DataSet dataSet = createDataSet(sessionToken, creation, fo);
        assertEquals(dataSet.getCode(), creation.getCode().toUpperCase());
        assertTags(dataSet.getTags(), "/test_space/TEST_METAPROJECTS");
    }

    @Test
    public void testCreateWithTagsNonexistent()
    {
        final String sessionToken = v3api.loginAs(TEST_USER, PASSWORD, TEST_SPACE_USER);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.setExperimentId(new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));
        creation.setTagIds(Arrays.asList(new TagCode("IDONTEXIST")));

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withTags();

        DataSet dataSet = createDataSet(sessionToken, creation, fo);
        assertEquals(dataSet.getCode(), creation.getCode().toUpperCase());
        assertTags(dataSet.getTags(), "/test_space/IDONTEXIST");
    }

    @Test
    public void testCreateWithSystemProperty()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        NewETPTAssignment assignment = new NewETPTAssignment();
        assignment.setPropertyTypeCode("$PLATE_GEOMETRY");
        assignment.setEntityTypeCode("UNKNOWN");
        assignment.setEntityKind(EntityKind.DATA_SET);
        assignment.setOrdinal(1000L);
        commonServer.assignPropertyType(sessionToken, assignment);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.setExperimentId(new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));
        creation.setProperty("$PLATE_GEOMETRY", "384_WELLS_16X24");

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withProperties();

        DataSet dataSet = createDataSet(sessionToken, creation, fo);
        assertEquals(dataSet.getCode(), creation.getCode().toUpperCase());
        assertEquals(dataSet.getProperty("$PLATE_GEOMETRY"), "384_WELLS_16X24");
    }

    @Test
    public void testCreateWithDataProducer()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.setDataProducer("TEST_DATA_PRODUCER");
        creation.setDataProductionDate(new Date());

        DataSet dataSet = createDataSet(sessionToken, creation, new DataSetFetchOptions());

        assertEquals(dataSet.getDataProducer(), creation.getDataProducer());
        assertEqualsDate(dataSet.getDataProductionDate(), creation.getDataProductionDate());
    }

    @Test
    public void testCreateWithExperimentNullAndSampleNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.setExperimentId(null);
        creation.setSampleId(null);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createDataSets(sessionToken, Arrays.asList(creation));
                }
            }, "Experiment id and sample id cannot be both null.");
    }

    @Test
    public void testCreateWithExperimentNotNullAndSampleNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP1"));
        creation.setSampleId(null);

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withExperiment();
        fo.withSample();

        DataSet dataSet = createDataSet(sessionToken, creation, fo);
        assertEquals(dataSet.getCode(), creation.getCode().toUpperCase());
        assertEquals(dataSet.getExperiment().getIdentifier().getIdentifier(), "/CISD/NEMO/EXP1");
        assertEquals(dataSet.getSample(), null);
    }

    @Test
    public void testCreateWithExperimentNullAndSampleInExperiment()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.setExperimentId(null);
        creation.setSampleId(new SampleIdentifier("/CISD/NEMO/CP-TEST-1"));

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withExperiment();
        fo.withSample();

        DataSet dataSet = createDataSet(sessionToken, creation, fo);
        assertEquals(dataSet.getCode(), creation.getCode().toUpperCase());
        assertEquals(dataSet.getExperiment().getIdentifier().getIdentifier(), "/CISD/NEMO/EXP-TEST-1");
        assertEquals(dataSet.getSample().getIdentifier().getIdentifier(), "/CISD/NEMO/CP-TEST-1");
    }

    @Test
    public void testCreateWithExperimentNullAndSampleNotInExperimentWhenTypeAllows()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.setTypeId(new EntityTypePermId("UNKNOWN"));
        creation.setExperimentId(null);
        creation.setSampleId(new SampleIdentifier("/CISD/3V-125"));

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withExperiment();
        fo.withSample();

        DataSet dataSet = createDataSet(sessionToken, creation, fo);
        assertEquals(dataSet.getCode(), creation.getCode().toUpperCase());
        assertEquals(dataSet.getExperiment(), null);
        assertEquals(dataSet.getSample().getIdentifier().getIdentifier(), "/CISD/3V-125");
    }

    @Test
    public void testCreateWithExperimentNullAndSampleNotInExperimentWhenTypeDoesNotAllow()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.setTypeId(new EntityTypePermId("REQUIRES_EXPERIMENT"));
        creation.setExperimentId(null);
        creation.setSampleId(new SampleIdentifier("/CISD/3V-125"));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createDataSets(sessionToken, Arrays.asList(creation));
                }
            }, "Data set can not be registered because it is not connected to an experiment.");
    }

    @Test
    public void testCreateWithExperimentNotNullAndSampleNotNullNotInExperiment()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.setTypeId(new EntityTypePermId("UNKNOWN"));
        creation.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP-TEST-1"));
        creation.setSampleId(new SampleIdentifier("/CISD/3V-125"));

        assertUserFailureException(new IDelegatedAction()
            {

                @Override
                public void execute()
                {
                    v3api.createDataSets(sessionToken, Arrays.asList(creation));
                }
            }, "Data set can not be registered because it connected to a different experiment than its sample.");
    }

    @Test
    public void testCreateWithExperimentNotNullAndSampleNotNullInSameExperiment()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.setTypeId(new EntityTypePermId("UNKNOWN"));
        creation.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP-TEST-1"));
        creation.setSampleId(new SampleIdentifier("/CISD/NEMO/CP-TEST-1"));

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withExperiment();
        fo.withSample();

        DataSet dataSet = createDataSet(sessionToken, creation, fo);
        assertEquals(dataSet.getCode(), creation.getCode().toUpperCase());
        assertEquals(dataSet.getExperiment().getIdentifier().getIdentifier(), "/CISD/NEMO/EXP-TEST-1");
        assertEquals(dataSet.getSample().getIdentifier().getIdentifier(), "/CISD/NEMO/CP-TEST-1");
    }

    @Test
    public void testCreateWithExperimentNotNullAndSampleNotNullInDifferentExperiment()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.setTypeId(new EntityTypePermId("UNKNOWN"));
        creation.setExperimentId(new ExperimentIdentifier("/CISD/DEFAULT/EXP-REUSE"));
        creation.setSampleId(new SampleIdentifier("/CISD/NEMO/CP-TEST-1"));

        assertUserFailureException(new IDelegatedAction()
            {

                @Override
                public void execute()
                {
                    v3api.createDataSets(sessionToken, Arrays.asList(creation));
                }
            }, "Data set can not be registered because it connected to a different experiment than its sample.");
    }

    @Test
    public void testCreateWithExperimentInTrash()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        List<ExperimentPermId> experimentIds = v3api.createExperiments(sessionToken, Arrays.asList(experimentCreation()));
        ExperimentDeletionOptions deletion = new ExperimentDeletionOptions();
        deletion.setReason("testing");
        v3api.deleteExperiments(sessionToken, experimentIds, deletion);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.setExperimentId(experimentIds.get(0));
        creation.setSampleId(null);

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createDataSets(sessionToken, Arrays.asList(creation));
                }
            }, experimentIds.get(0));
    }

    @Test
    public void testCreateWithExperimentUnauthorized()
    {
        final String sessionToken = v3api.loginAs(TEST_USER, PASSWORD, TEST_SPACE_USER);

        final IExperimentId experimentId = new ExperimentIdentifier("/CISD/NEMO/EXP1");
        final DataSetCreation creation = physicalDataSetCreation();
        creation.setExperimentId(experimentId);
        creation.setSampleId(null);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createDataSets(sessionToken, Arrays.asList(creation));
                }
            }, experimentId);
    }

    @Test
    public void testCreateWithSampleInTrash()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        List<SamplePermId> sampleIds = v3api.createSamples(sessionToken, Arrays.asList(sampleCreation()));
        SampleDeletionOptions deletion = new SampleDeletionOptions();
        deletion.setReason("testing");
        v3api.deleteSamples(sessionToken, sampleIds, deletion);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.setExperimentId(null);
        creation.setSampleId(sampleIds.get(0));

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createDataSets(sessionToken, Arrays.asList(creation));
                }
            }, sampleIds.get(0));
    }

    @Test
    public void testCreateWithSampleUnauthorized()
    {
        final String sessionToken = v3api.loginAs(TEST_USER, PASSWORD, TEST_SPACE_USER);

        final ISampleId sampleId = new SampleIdentifier("/CISD/NEMO/CP-TEST-1");
        final DataSetCreation creation = physicalDataSetCreation();
        creation.setExperimentId(null);
        creation.setSampleId(sampleId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createDataSets(sessionToken, Arrays.asList(creation));
                }
            }, sampleId);
    }

    @Test
    public void testCreateWithSampleShared()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.setExperimentId(null);
        creation.setSampleId(new SamplePermId("200811050947161-652"));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createDataSets(sessionToken, Arrays.asList(creation));
                }
            }, "Data set can not be registered because sample '/MP' is a shared sample.");
    }

    @Test
    public void testCreateWithContainersThatAreNonContainerDataSets()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.setContainerIds(Collections.singletonList(new DataSetPermId("20081105092159111-1")));

        assertUserFailureException(new IDelegatedAction()
            {

                @Override
                public void execute()
                {
                    v3api.createDataSets(sessionToken, Arrays.asList(creation));
                }
            }, "Data set 20081105092159111-1 is not of a container type therefore cannot be set as a container of data set "
                    + creation.getCode().toUpperCase() + ".");
    }

    @Test
    public void testCreateWithContainersThatAreContainerDataSets()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetCreation creation = physicalDataSetCreation();
        creation.setContainerIds(Collections.singletonList(new DataSetPermId("20110509092359990-10")));

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withContainers();

        DataSet dataSet = createDataSet(sessionToken, creation, fo);

        assertEquals(dataSet.getCode(), creation.getCode().toUpperCase());
        assertDataSetCodes(dataSet.getContainers(), "20110509092359990-10");
    }

    @Test
    public void testCreateWithContainersCircularDependency()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation1 = containerDataSetCreation();
        final DataSetCreation creation2 = containerDataSetCreation();
        final DataSetCreation creation3 = containerDataSetCreation();

        creation2.setContainerIds(Collections.singletonList(creation1.getCreationId()));
        creation3.setContainerIds(Collections.singletonList(creation2.getCreationId()));
        creation1.setContainerIds(Collections.singletonList(creation3.getCreationId()));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createDataSets(sessionToken, Arrays.asList(creation1, creation2, creation3));
                }
            }, "Circular dependency found");
    }

    @Test
    public void testCreateWithContainersUnauthorized()
    {
        final String sessionToken = v3api.loginAs(TEST_USER, PASSWORD, TEST_SPACE_USER);

        final IDataSetId containerId = new DataSetPermId("20110509092359990-10");
        final DataSetCreation creation = physicalDataSetCreation();
        creation.setExperimentId(new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));
        creation.setContainerIds(Collections.singletonList(containerId));

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createDataSet(sessionToken, creation, new DataSetFetchOptions());
                }
            }, containerId);
    }

    @Test
    public void testCreateWithComponentsForContainerDataSet()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetCreation creation = containerDataSetCreation();
        creation.setComponentIds(Collections.singletonList(new DataSetPermId("20081105092159111-1")));

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withComponents();

        DataSet dataSet = createDataSet(sessionToken, creation, fo);

        assertEquals(dataSet.getCode(), creation.getCode().toUpperCase());
        assertDataSetCodes(dataSet.getComponents(), "20081105092159111-1");
    }

    @Test
    public void testCreateWithComponentsForNonContainerDataSet()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.setComponentIds(Collections.singletonList(new DataSetPermId("20081105092159111-1")));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createDataSet(sessionToken, creation, new DataSetFetchOptions());
                }
            }, "Data set " + creation.getCode().toUpperCase() + " is not of a container type therefore cannot have component data sets.");
    }

    @Test
    public void testCreateWithComponentsCircularDependency()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation1 = containerDataSetCreation();
        final DataSetCreation creation2 = containerDataSetCreation();
        final DataSetCreation creation3 = containerDataSetCreation();

        creation2.setComponentIds(Collections.singletonList(creation1.getCreationId()));
        creation3.setComponentIds(Collections.singletonList(creation2.getCreationId()));
        creation1.setComponentIds(Collections.singletonList(creation3.getCreationId()));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createDataSets(sessionToken, Arrays.asList(creation1, creation2, creation3));
                }
            }, "Circular dependency found");
    }

    @Test
    public void testCreateWithComponentsUnauthorized()
    {
        final String sessionToken = v3api.loginAs(TEST_USER, PASSWORD, TEST_SPACE_USER);

        final IDataSetId componentId = new DataSetPermId("20081105092159111-1");
        final DataSetCreation creation = containerDataSetCreation();
        creation.setExperimentId(new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));
        creation.setComponentIds(Collections.singletonList(componentId));

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createDataSet(sessionToken, creation, new DataSetFetchOptions());
                }
            }, componentId);
    }

    @Test
    public void testCreateWithParents()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetCreation creation = physicalDataSetCreation();
        creation.setParentIds(Collections.singletonList(new DataSetPermId("20081105092159111-1")));

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withParents();

        DataSet dataSet = createDataSet(sessionToken, creation, fo);

        assertEquals(dataSet.getCode(), creation.getCode().toUpperCase());
        assertDataSetCodes(dataSet.getParents(), "20081105092159111-1");
    }

    @Test
    public void testCreateWithParentsCircularDependency()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation1 = physicalDataSetCreation();
        final DataSetCreation creation2 = physicalDataSetCreation();
        final DataSetCreation creation3 = physicalDataSetCreation();

        creation2.setParentIds(Collections.singletonList(creation1.getCreationId()));
        creation3.setParentIds(Collections.singletonList(creation2.getCreationId()));
        creation1.setParentIds(Collections.singletonList(creation3.getCreationId()));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createDataSets(sessionToken, Arrays.asList(creation1, creation2, creation3));
                }
            }, "Circular dependency found");
    }

    @Test
    public void testCreateWithParentsUnauthorized()
    {
        final String sessionToken = v3api.loginAs(TEST_USER, PASSWORD, TEST_SPACE_USER);

        final IDataSetId parentId = new DataSetPermId("20081105092159111-1");
        final DataSetCreation creation = physicalDataSetCreation();
        creation.setExperimentId(new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));
        creation.setParentIds(Collections.singletonList(parentId));

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createDataSet(sessionToken, creation, new DataSetFetchOptions());
                }
            }, parentId);
    }

    @Test
    public void testCreateWithChildren()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetCreation creation = physicalDataSetCreation();
        creation.setChildIds(Collections.singletonList(new DataSetPermId("20081105092159111-1")));

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withChildren();

        DataSet dataSet = createDataSet(sessionToken, creation, fo);

        assertEquals(dataSet.getCode(), creation.getCode().toUpperCase());
        assertDataSetCodes(dataSet.getChildren(), "20081105092159111-1");
    }

    @Test
    public void testCreateWithChildrenCircularDependency()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation1 = physicalDataSetCreation();
        final DataSetCreation creation2 = physicalDataSetCreation();
        final DataSetCreation creation3 = physicalDataSetCreation();

        creation2.setChildIds(Collections.singletonList(creation1.getCreationId()));
        creation3.setChildIds(Collections.singletonList(creation2.getCreationId()));
        creation1.setChildIds(Collections.singletonList(creation3.getCreationId()));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createDataSets(sessionToken, Arrays.asList(creation1, creation2, creation3));
                }
            }, "Circular dependency found");
    }

    @Test
    public void testCreateWithChildrenUnauthorized()
    {
        final String sessionToken = v3api.loginAs(TEST_USER, PASSWORD, TEST_SPACE_USER);

        final IDataSetId childId = new DataSetPermId("20081105092159111-1");
        final DataSetCreation creation = physicalDataSetCreation();
        creation.setExperimentId(new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));
        creation.setChildIds(Collections.singletonList(childId));

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createDataSet(sessionToken, creation, new DataSetFetchOptions());
                }
            }, childId);
    }

    @Test
    public void testCreateWithUserNonEtlServer()
    {
        final String sessionToken = v3api.login(TEST_POWER_USER_CISD, PASSWORD);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createDataSet(sessionToken, physicalDataSetCreation(), new DataSetFetchOptions());
                }
            }, "Data set creation can be only executed by a system user or a user with at least SPACE_ETL_SERVER role");
    }

    @Test
    public void testCreateWithUserEtlServer()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.setExperimentId(new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withExperiment();
        fo.withSample();
        fo.withRegistrator();

        DataSet dataSet = createDataSet(sessionToken, creation, fo);
        assertEquals(dataSet.getCode(), creation.getCode().toUpperCase());
        assertEquals(dataSet.getExperiment().getIdentifier().getIdentifier(), "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
        assertEquals(dataSet.getSample(), null);
        assertEquals(dataSet.getRegistrator().getUserId(), TEST_USER);
    }

    @Test
    public void testCreateWithUserEtlServerOnBehalfOtherUser()
    {
        final String sessionToken = v3api.loginAs(TEST_USER, PASSWORD, TEST_SPACE_USER);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.setExperimentId(new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withExperiment();
        fo.withSample();
        fo.withRegistrator();

        DataSet dataSet = createDataSet(sessionToken, creation, fo);
        assertEquals(dataSet.getCode(), creation.getCode().toUpperCase());
        assertEquals(dataSet.getExperiment().getIdentifier().getIdentifier(), "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
        assertEquals(dataSet.getSample(), null);
        assertEquals(dataSet.getRegistrator().getUserId(), TEST_SPACE_USER);
    }

    @Test
    public void testCreatePhysicalDataSetWithPhysicalDataNotNull()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PhysicalDataCreation physicalCreation = new PhysicalDataCreation();
        physicalCreation.setLocation("a/b/c");
        physicalCreation.setFileFormatTypeId(new FileFormatTypePermId("TIFF"));
        physicalCreation.setLocatorTypeId(new RelativeLocationLocatorTypePermId());
        physicalCreation.setStorageFormatId(new ProprietaryStorageFormatPermId());

        DataSetCreation creation = new DataSetCreation();
        creation.setCode("TEST_PHYSICAL_DATASET");
        creation.setDataSetKind(DataSetKind.PHYSICAL);
        creation.setTypeId(new EntityTypePermId("UNKNOWN"));
        creation.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP1"));
        creation.setDataStoreId(new DataStorePermId("STANDARD"));
        creation.setPhysicalData(physicalCreation);

        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withType();
        fetchOptions.withExperiment();
        fetchOptions.withDataStore();
        fetchOptions.withPhysicalData().withFileFormatType();
        fetchOptions.withPhysicalData().withLocatorType();
        fetchOptions.withPhysicalData().withStorageFormat();
        fetchOptions.withLinkedData();

        DataSet dataSet = createDataSet(sessionToken, creation, fetchOptions);
        assertEquals(dataSet.getCode(), "TEST_PHYSICAL_DATASET");
        assertEquals(dataSet.getType().getCode(), "UNKNOWN");
        assertEquals(dataSet.getExperiment().getPermId().getPermId(), "200811050951882-1028");
        assertEquals(dataSet.getDataStore().getCode(), "STANDARD");
        assertEquals(dataSet.getPhysicalData().getLocation(), "a/b/c");
        assertEquals(dataSet.getPhysicalData().getFileFormatType().getCode(), "TIFF");
        assertEquals(dataSet.getPhysicalData().getLocatorType().getCode(), "RELATIVE_LOCATION");
        assertEquals(dataSet.getPhysicalData().getStorageFormat().getCode(), "PROPRIETARY");
        assertNull(dataSet.getLinkedData());
    }

    @Test
    public void testCreatePhysicalDataSetWithPhysicalDataNotNullAndLinkedDataNotNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.setLinkedData(new LinkedDataCreation());

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createDataSet(sessionToken, creation, new DataSetFetchOptions());
                }
            }, "Linked data cannot be set for a non-link data set.", patternContains("setting relation dataset-linkeddata (1/1)"));
    }

    @Test
    public void testCreatePhysicalDataSetWithPhysicalDataNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.setPhysicalData(null);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createDataSet(sessionToken, creation, new DataSetFetchOptions());
                }
            }, "Physical data cannot be null for a physical data set.", patternContains("setting relation dataset-physicaldata (1/1)"));
    }

    @Test
    public void testCreatePhysicalDataSetWithShareIdNull()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetCreation creation = physicalDataSetCreation();
        creation.getPhysicalData().setShareId(null);

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withPhysicalData();

        DataSet dataSet = createDataSet(sessionToken, creation, fo);

        assertEquals(dataSet.getPhysicalData().getShareId(), null);
    }

    @Test
    public void testCreatePhysicalDataSetWithShareIdNotNull()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetCreation creation = physicalDataSetCreation();
        creation.getPhysicalData().setShareId("SOME_SHARE");

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withPhysicalData();

        DataSet dataSet = createDataSet(sessionToken, creation, fo);

        assertEquals(dataSet.getPhysicalData().getShareId(), "SOME_SHARE");
    }

    @Test
    public void testCreatePhysicalDataSetWithLocationNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.getPhysicalData().setLocation(null);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createDataSet(sessionToken, creation, new DataSetFetchOptions());
                }
            }, "Location can not be null.");
    }

    @Test
    public void testCreatePhysicalDataSetWithLocationAbsolute()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.getPhysicalData().setLocation("/cannot_be_absolute_path/sorry");

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createDataSet(sessionToken, creation, new DataSetFetchOptions());
                }
            }, "Location is not relative");
    }

    @Test
    public void testCreatePhysicalDataSetWithLocationRelative()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetCreation creation = physicalDataSetCreation();
        creation.getPhysicalData().setLocation("relative_path_should_be/fine");

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withPhysicalData();

        DataSet dataSet = createDataSet(sessionToken, creation, fo);

        assertEquals(dataSet.getPhysicalData().getLocation(), creation.getPhysicalData().getLocation());
    }

    @Test
    public void testCreatePhysicalDataSetWithLocationExisting()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final String location = "duplicated_location/should_fail";

        final DataSetCreation creation1 = physicalDataSetCreation();
        creation1.getPhysicalData().setLocation(location);

        final DataSetCreation creation2 = physicalDataSetCreation();
        creation2.getPhysicalData().setLocation(location);

        final DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withPhysicalData();

        DataSet dataSet1 = createDataSet(sessionToken, creation1, fo);
        assertEquals(dataSet1.getPhysicalData().getLocation(), creation1.getPhysicalData().getLocation());

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createDataSet(sessionToken, creation2, fo);
                }
            }, "DataSet already exists in the database and needs to be unique.");
    }

    @Test
    public void testCreatePhysicalDataSetWithSizeNull()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetCreation creation = physicalDataSetCreation();
        creation.getPhysicalData().setSize(null);

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withPhysicalData();

        DataSet dataSet = createDataSet(sessionToken, creation, fo);

        assertEquals(dataSet.getPhysicalData().getSize(), null);
    }

    @Test
    public void testCreatePhysicalDataSetWithSizeNotNull()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetCreation creation = physicalDataSetCreation();
        creation.getPhysicalData().setSize(12345L);

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withPhysicalData();

        DataSet dataSet = createDataSet(sessionToken, creation, fo);

        assertEquals(dataSet.getPhysicalData().getSize(), Long.valueOf(12345));
    }

    @Test
    public void testCreatePhysicalDataSetWithSizeNegative()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.getPhysicalData().setSize(-12345L);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createDataSet(sessionToken, creation, new DataSetFetchOptions());
                }
            }, "Physical data set size cannot be < 0.");
    }

    @Test
    public void testCreatePhysicalDataSetWithStorageFormatNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.getPhysicalData().setStorageFormatId(null);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createDataSet(sessionToken, creation, new DataSetFetchOptions());
                }
            }, "Storage format id cannot be null for a physical data set.");
    }

    @Test
    public void testCreatePhysicalDataSetWithStorageFormatNotNull()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetCreation creation = physicalDataSetCreation();
        creation.getPhysicalData().setStorageFormatId(new BdsDirectoryStorageFormatPermId());

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withPhysicalData().withStorageFormat();

        DataSet dataSet = createDataSet(sessionToken, creation, fo);

        assertEquals(dataSet.getPhysicalData().getStorageFormat().getCode(), "BDS_DIRECTORY");
    }

    @Test
    public void testCreatePhysicalDataSetWithStorageFormatNonexistent()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final IStorageFormatId storageFormatId = new StorageFormatPermId("IDONTEXIST");
        final DataSetCreation creation = physicalDataSetCreation();
        creation.getPhysicalData().setStorageFormatId(storageFormatId);

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createDataSet(sessionToken, creation, new DataSetFetchOptions());
                }
            }, storageFormatId);
    }

    @Test
    public void testCreatePhysicalDataSetWithFileFormatTypeNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.getPhysicalData().setFileFormatTypeId(null);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createDataSet(sessionToken, creation, new DataSetFetchOptions());
                }
            }, "File format type id cannot be null for a physical data set.");
    }

    @Test
    public void testCreatePhysicalDataSetWithFileFormatTypeNotNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.getPhysicalData().setFileFormatTypeId(new FileFormatTypePermId("XML"));

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withPhysicalData().withFileFormatType();

        DataSet dataSet = createDataSet(sessionToken, creation, fo);

        assertEquals(dataSet.getPhysicalData().getFileFormatType().getCode(), "XML");
    }

    @Test
    public void testCreatePhysicalDataSetWithFileFormatTypeNonexistent()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final IFileFormatTypeId formatId = new FileFormatTypePermId("IDONTEXIST");
        final DataSetCreation creation = physicalDataSetCreation();
        creation.getPhysicalData().setFileFormatTypeId(formatId);

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createDataSet(sessionToken, creation, new DataSetFetchOptions());
                }
            }, formatId);
    }

    @Test
    public void testCreatePhysicalDataSetWithLocatorTypeNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.getPhysicalData().setLocatorTypeId(null);

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withPhysicalData().withLocatorType();

        DataSet dataSet = createDataSet(sessionToken, creation, fo);

        // got default value
        assertEquals(dataSet.getPhysicalData().getLocatorType().getCode(), "RELATIVE_LOCATION");
    }

    @Test
    public void testCreatePhysicalDataSetWithLocatorTypeNotNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.getPhysicalData().setLocatorTypeId(new RelativeLocationLocatorTypePermId());

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withPhysicalData().withLocatorType();

        DataSet dataSet = createDataSet(sessionToken, creation, fo);

        assertEquals(dataSet.getPhysicalData().getLocatorType().getCode(), "RELATIVE_LOCATION");
    }

    @Test
    public void testCreatePhysicalDataSetWithLocatorTypeNonexistent()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final ILocatorTypeId locatorTypeId = new LocatorTypePermId("IDONTEXIST");
        final DataSetCreation creation = physicalDataSetCreation();
        creation.getPhysicalData().setLocatorTypeId(locatorTypeId);

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createDataSet(sessionToken, creation, new DataSetFetchOptions());
                }
            }, locatorTypeId);
    }

    @Test
    public void testCreatePhysicalDataSetWithCompleteNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.getPhysicalData().setComplete(null);

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withPhysicalData();

        DataSet dataSet = createDataSet(sessionToken, creation, fo);

        // got default value
        assertEquals(dataSet.getPhysicalData().getComplete(), Complete.UNKNOWN);
    }

    @Test
    public void testCreatePhysicalDataSetWithCompleteNotNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.getPhysicalData().setComplete(Complete.YES);

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withPhysicalData();

        DataSet dataSet = createDataSet(sessionToken, creation, fo);

        assertEquals(dataSet.getPhysicalData().getComplete(), Complete.YES);
    }

    @Test
    public void testCreatePhysicalDataSetWithSpeedHintNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.getPhysicalData().setSpeedHint(null);

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withPhysicalData();

        DataSet dataSet = createDataSet(sessionToken, creation, fo);

        // got default value
        assertEquals(dataSet.getPhysicalData().getSpeedHint(), Integer.valueOf(-50));
    }

    @Test
    public void testCreatePhysicalDataSetWithSpeedHintNotNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = physicalDataSetCreation();
        creation.getPhysicalData().setSpeedHint(123);

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withPhysicalData();

        DataSet dataSet = createDataSet(sessionToken, creation, fo);

        assertEquals(dataSet.getPhysicalData().getSpeedHint(), Integer.valueOf(123));
    }

    @Test
    public void testCreateContainerDataSet()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetCreation creation = new DataSetCreation();
        creation.setCode("TEST_CONTAINER_DATASET");
        creation.setDataSetKind(DataSetKind.CONTAINER);
        creation.setTypeId(new EntityTypePermId("CONTAINER_TYPE"));
        creation.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP1"));
        creation.setDataStoreId(new DataStorePermId("STANDARD"));
        creation.setComponentIds(Arrays.asList(new DataSetPermId("20081105092159188-3")));

        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withType();
        fetchOptions.withExperiment();
        fetchOptions.withDataStore();
        fetchOptions.withPhysicalData();
        fetchOptions.withLinkedData();
        fetchOptions.withComponents();

        DataSet dataSet = createDataSet(sessionToken, creation, fetchOptions);
        assertEquals(dataSet.getCode(), "TEST_CONTAINER_DATASET");
        assertEquals(dataSet.getType().getCode(), "CONTAINER_TYPE");
        assertEquals(dataSet.getExperiment().getPermId().getPermId(), "200811050951882-1028");
        assertEquals(dataSet.getDataStore().getCode(), "STANDARD");
        assertNull(dataSet.getPhysicalData());
        assertNull(dataSet.getLinkedData());
        assertEquals(dataSet.getComponents().size(), 1);
        assertEquals(dataSet.getComponents().iterator().next().getCode(), "20081105092159188-3");
    }

    @Test
    public void testCreateContainerDataSetWithPhysicalDataNotNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = containerDataSetCreation();
        creation.setPhysicalData(new PhysicalDataCreation());

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createDataSet(sessionToken, creation, new DataSetFetchOptions());
                }
            }, "Physical data cannot be set for a non-physical data set.");
    }

    @Test
    public void testCreateContainerDataSetWithLinkedDataNotNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = containerDataSetCreation();
        creation.setLinkedData(new LinkedDataCreation());

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createDataSet(sessionToken, creation, new DataSetFetchOptions());
                }
            }, "Linked data cannot be set for a non-link data set.");
    }

    @Test
    public void testCreateLinkDataSetWithLinkedDataNotNull()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        LinkedDataCreation linkedCreation = new LinkedDataCreation();
        linkedCreation.setExternalCode("TEST_EXTERNAL_CODE");
        linkedCreation.setExternalDmsId(new ExternalDmsPermId("DMS_1"));

        DataSetCreation creation = new DataSetCreation();
        creation.setCode("TEST_LINK_DATASET");
        creation.setDataSetKind(DataSetKind.LINK);
        creation.setTypeId(new EntityTypePermId("LINK_TYPE"));
        creation.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP1"));
        creation.setDataStoreId(new DataStorePermId("STANDARD"));
        creation.setLinkedData(linkedCreation);

        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withType();
        fetchOptions.withExperiment();
        fetchOptions.withDataStore();
        fetchOptions.withPhysicalData();
        fetchOptions.withLinkedData().withExternalDms();

        DataSet dataSet = createDataSet(sessionToken, creation, fetchOptions);
        assertEquals(dataSet.getCode(), "TEST_LINK_DATASET");
        assertEquals(dataSet.getType().getCode(), "LINK_TYPE");
        assertEquals(dataSet.getExperiment().getPermId().getPermId(), "200811050951882-1028");
        assertEquals(dataSet.getDataStore().getCode(), "STANDARD");
        assertEquals(dataSet.getLinkedData().getExternalCode(), "TEST_EXTERNAL_CODE");
        assertEquals(dataSet.getLinkedData().getExternalDms().getCode(), "DMS_1");
        assertNull(dataSet.getPhysicalData());
    }

    @Test
    public void testCreateLinkDataSetWithLinkedDataNotNullAndPhyscialDataNotNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = linkDataSetCreation();
        creation.setPhysicalData(new PhysicalDataCreation());

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createDataSet(sessionToken, creation, new DataSetFetchOptions());
                }
            }, "Physical data cannot be set for a non-physical data set.");
    }

    @Test
    public void testCreateLinkDataSetWithLinkedDataNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = linkDataSetCreation();
        creation.setLinkedData(null);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createDataSet(sessionToken, creation, new DataSetFetchOptions());
                }
            }, "Linked data cannot be null for a link data set.");
    }

    @Test
    public void testCreateLinkDataSetWithExternalCodeNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = linkDataSetCreation();
        creation.getLinkedData().setExternalCode(null);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createDataSet(sessionToken, creation, new DataSetFetchOptions());
                }
            }, "External code can not be null.");
    }

    @Test
    public void testCreateLinkDataSetWithExternalDmsNonexistent()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final IExternalDmsId externalDmsId = new ExternalDmsPermId("IDONTEXIST");
        final DataSetCreation creation = linkDataSetCreation();
        creation.getLinkedData().setExternalDmsId(externalDmsId);

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createDataSet(sessionToken, creation, new DataSetFetchOptions());
                }
            }, externalDmsId);
    }

    @Test
    public void testCreateLinkDataSetWithExternalDmsNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final DataSetCreation creation = linkDataSetCreation();
        creation.getLinkedData().setExternalDmsId(null);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    createDataSet(sessionToken, creation, new DataSetFetchOptions());
                }
            }, "External data management system id cannot be null for a link data set.");
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testCreateWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String sessionToken = v3api.loginAs(TEST_USER, PASSWORD, user.getUserId());

        DataSetCreation creation = physicalDataSetCreation();
        creation.setExperimentId(new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));

        if (user.isDisabledProjectUser())
        {
            assertAuthorizationFailureException(new IDelegatedAction()
                {
                    @Override
                    public void execute()
                    {
                        v3api.createDataSets(sessionToken, Collections.singletonList(creation));
                    }
                });
        } else if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            List<DataSetPermId> permIds = v3api.createDataSets(sessionToken, Collections.singletonList(creation));
            assertEquals(permIds.size(), 1);
        } else
        {
            assertUnauthorizedObjectAccessException(new IDelegatedAction()
                {
                    @Override
                    public void execute()
                    {
                        v3api.createDataSets(sessionToken, Collections.singletonList(creation));
                    }
                }, creation.getExperimentId());
        }
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetCreation creation = physicalDataSetCreation();
        creation.setCode("LOG_TEST_1");
        creation.setTypeId(new EntityTypePermId("UNKNOWN"));
        creation.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP-TEST-1"));
        creation.setSampleId(new SampleIdentifier("/CISD/NEMO/CP-TEST-1"));

        DataSetCreation creation2 = physicalDataSetCreation();
        creation2.setCode("LOG_TEST_2");
        creation2.setTypeId(new EntityTypePermId("UNKNOWN"));
        creation2.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP-TEST-1"));
        creation2.setSampleId(null);

        DataSetCreation creation3 = physicalDataSetCreation();
        creation3.setCode("LOG_TEST_3");
        creation3.setTypeId(new EntityTypePermId("UNKNOWN"));
        creation3.setExperimentId(null);
        creation3.setSampleId(new SampleIdentifier("/CISD/NEMO/CP-TEST-1"));

        v3api.createDataSets(sessionToken, Arrays.asList(creation, creation2, creation3));

        assertAccessLog(
                "create-data-sets  NEW_DATA_SETS('[DataSetCreation[experimentId=/CISD/NEMO/EXP-TEST-1,"
                + "sampleId=/CISD/NEMO/CP-TEST-1,code=LOG_TEST_1], "
                + "DataSetCreation[experimentId=/CISD/NEMO/EXP-TEST-1,sampleId=<null>,code=LOG_TEST_2], "
                + "DataSetCreation[experimentId=<null>,sampleId=/CISD/NEMO/CP-TEST-1,code=LOG_TEST_3]]')");
    }

    @Test
    public void testCreateWithUnknownPropertyOfTypeSample()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        DataSetCreation creation = physicalDataSetCreation();
        creation.setExperimentId(new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));
        creation.setProperty("PLATE", "/CISD/CL1");

        // When
        assertUserFailureException(Void -> v3api.createDataSets(sessionToken, Arrays.asList(creation)),
                // Then
                "Property type with code 'PLATE' does not exist");
    }

    @Test
    public void testCreateWithPropertyOfTypeSampleWithUnknownSample()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createASamplePropertyType(sessionToken, null);
        EntityTypePermId dataSetType = createADataSetType(sessionToken, false, propertyType);

        DataSetCreation creation = physicalDataSetCreation();
        creation.setTypeId(dataSetType);
        creation.setExperimentId(new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));
        creation.setProperty(propertyType.getPermId(), "/CISD/UNKNOWN");

        // When
        assertUserFailureException(Void -> v3api.createDataSets(sessionToken, Arrays.asList(creation)),
                // Then
                "Unknown sample: /CISD/UNKNOWN");
    }

    @Test
    public void testCreateWithMissingMandatoryPropertyOfTypeSample()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createASamplePropertyType(sessionToken, null);
        EntityTypePermId dataSetType = createADataSetType(sessionToken, true, propertyType);

        DataSetCreation creation = physicalDataSetCreation();
        creation.setTypeId(dataSetType);

        // When
        assertUserFailureException(Void -> v3api.createDataSets(sessionToken, Arrays.asList(creation)),
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
        EntityTypePermId dataSetType = createADataSetType(sessionToken, false, propertyType);

        DataSetCreation creation = physicalDataSetCreation();
        creation.setTypeId(dataSetType);
        creation.setProperty(propertyType.getPermId(), "200811050919915-8");

        // When
        assertUserFailureException(Void -> v3api.createDataSets(sessionToken, Arrays.asList(creation)),
                // Then
                "Property " + propertyType.getPermId() + " is not a sample of type WELL but of type CONTROL_LAYOUT");
    }

    @Test
    public void testCreateWithPropertyOfTypeSampleWithSampleNotAccessable()
    {
        // Given
        String adminSessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createASamplePropertyType(adminSessionToken, null);
        EntityTypePermId dataSetType = createADataSetType(adminSessionToken, true, propertyType);
        v3api.logout(adminSessionToken);

        String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);
        DataSetCreation creation = physicalDataSetCreation();
        creation.setTypeId(dataSetType);
        creation.setExperimentId(new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));
        creation.setProperty(propertyType.getPermId(), "/CISD/CL1");

        // When
        assertUserFailureException(Void -> v3api.createDataSets(sessionToken, Arrays.asList(creation)),
                // Then
                "Unknown sample: /CISD/CL1");
    }

    @Test
    public void testCreateWithPropertyOfTypeSampleWithInvisibleSample()
    {
        // Given
        String adminSessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createASamplePropertyType(adminSessionToken, null);
        EntityTypePermId dataSetType = createADataSetType(adminSessionToken, true, propertyType, PLATE_GEOMETRY);

        DataSetCreation creation = physicalDataSetCreation();
        creation.setTypeId(dataSetType);
        creation.setExperimentId(new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST"));
        creation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        creation.setProperty(propertyType.getPermId(), "/CISD/CL1");
        DataSetPermId dataSetPermId = v3api.createDataSets(adminSessionToken, Arrays.asList(creation)).get(0);
        v3api.logout(adminSessionToken);

        String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();

        // When
        DataSet dataSet = v3api.getDataSets(sessionToken, Arrays.asList(dataSetPermId), fetchOptions).get(dataSetPermId);

        // Then
        assertEquals(dataSet.getSampleProperties().toString(), "{}");
        assertEquals(dataSet.getProperties().toString(), "{$PLATE_GEOMETRY=384_WELLS_16X24}");
    }

    @Test
    public void testCreateWithPropertyOfTypeSample()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createASamplePropertyType(sessionToken, null);
        EntityTypePermId dataSetType = createADataSetType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        DataSetCreation creation = physicalDataSetCreation();
        creation.setTypeId(dataSetType);
        creation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        creation.setProperty(propertyType.getPermId(), "/CISD/CL1");

        // When
        List<DataSetPermId> dataSetIds = v3api.createDataSets(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(dataSetIds.size(), 1);
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        DataSet dataSet = v3api.getDataSets(sessionToken, dataSetIds, fetchOptions).get(dataSetIds.get(0));
        Sample sampleProperty = dataSet.getSampleProperties().get(propertyType.getPermId())[0];
        assertEquals(sampleProperty.getIdentifier().getIdentifier(), "/CISD/CL1");
        assertEquals(dataSet.getSampleProperties().size(), 1);
        assertEquals(dataSet.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        assertEquals(dataSet.getProperties().get(propertyType.getPermId()), sampleProperty.getPermId().getPermId());
        assertEquals(dataSet.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithPropertyOfTypeDate()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.DATE);
        EntityTypePermId dataSetType = createADataSetType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        DataSetCreation creation = physicalDataSetCreation();
        creation.setTypeId(dataSetType);
        creation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        creation.setProperty(propertyType.getPermId(), "1999-12-19");

        // When
        List<DataSetPermId> dataSetIds = v3api.createDataSets(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(dataSetIds.size(), 1);
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        DataSet dataSet = v3api.getDataSets(sessionToken, dataSetIds, fetchOptions).get(dataSetIds.get(0));
        assertEquals(dataSet.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        assertEquals(dataSet.getProperties().get(propertyType.getPermId()), "1999-12-19");
        assertEquals(dataSet.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithPropertyOfTypeTimestamp()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.TIMESTAMP);
        EntityTypePermId dataSetType = createADataSetType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        DataSetCreation creation = physicalDataSetCreation();
        creation.setTypeId(dataSetType);
        creation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        creation.setProperty(propertyType.getPermId(), "12/24/08 3:4");

        // When
        List<DataSetPermId> dataSetIds = v3api.createDataSets(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(dataSetIds.size(), 1);
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        DataSet dataSet = v3api.getDataSets(sessionToken, dataSetIds, fetchOptions).get(dataSetIds.get(0));
        assertEquals(dataSet.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        assertEquals(dataSet.getProperties().get(propertyType.getPermId()), "2008-12-24 03:04:00 +0100");
        assertEquals(dataSet.getProperties().size(), 2);
    }
    
    @Test
    public void testCreateWithPropertyOfTypeTimestampDedicatedMethod()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.TIMESTAMP);
        EntityTypePermId dataSetType = createADataSetType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        DataSetCreation creation = physicalDataSetCreation();
        creation.setTypeId(dataSetType);
        creation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        ZonedDateTime time1 = ZonedDateTime.parse("2023-05-16T11:22:33+02");
        creation.setTimestampProperty(propertyType.getPermId(), time1);

        // When
        List<DataSetPermId> dataSetIds = v3api.createDataSets(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(dataSetIds.size(), 1);
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        DataSet dataSet = v3api.getDataSets(sessionToken, dataSetIds, fetchOptions).get(dataSetIds.get(0));
        assertEquals(dataSet.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        assertEquals(dataSet.getTimestampProperty(propertyType.getPermId()), ZonedDateTime.parse("2023-05-16T11:22:33+02:00"));
        assertEquals(dataSet.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithMultiValuePropertyOfTypeTimestamp()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.TIMESTAMP, true);
        EntityTypePermId dataSetType = createADataSetType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        DataSetCreation creation = physicalDataSetCreation();
        creation.setTypeId(dataSetType);
        creation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        ZonedDateTime time1 = ZonedDateTime.parse("2023-05-16T11:22:33+02");
        ZonedDateTime time2 = ZonedDateTime.parse("2023-05-18T11:17:03+02");
        creation.setMultiValueTimestampProperty(propertyType.getPermId(), List.of(time1, time2));

        // When
        List<DataSetPermId> dataSetIds = v3api.createDataSets(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(dataSetIds.size(), 1);
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        DataSet dataSet = v3api.getDataSets(sessionToken, dataSetIds, fetchOptions).get(dataSetIds.get(0));
        assertEquals(dataSet.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        assertEqualsNoOrder(dataSet.getMultiValueTimestampProperty(propertyType.getPermId()).toArray(ZonedDateTime[]::new), new ZonedDateTime[] {time1, time2});
        assertEquals(dataSet.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithPropertyOfTypeJson()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.JSON);
        EntityTypePermId dataSetType = createADataSetType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        DataSetCreation creation = physicalDataSetCreation();
        creation.setTypeId(dataSetType);
        creation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        creation.setJsonProperty(propertyType.getPermId(), "{\"key\": \"value\", \"array\":[1,2,3]}");

        // When
        List<DataSetPermId> dataSetIds = v3api.createDataSets(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(dataSetIds.size(), 1);
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        DataSet dataSet = v3api.getDataSets(sessionToken, dataSetIds, fetchOptions).get(dataSetIds.get(0));
        assertEquals(dataSet.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        assertEquals(dataSet.getJsonProperty(propertyType.getPermId()), "{\"key\": \"value\", \"array\": [1, 2, 3]}");
        assertEquals(dataSet.getProperties().size(), 2);
    }
    
    @Test
    public void testCreateWithMultiValuePropertyOfTypeJson()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.JSON, true);
        EntityTypePermId dataSetType = createADataSetType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        DataSetCreation creation = physicalDataSetCreation();
        creation.setTypeId(dataSetType);
        creation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        creation.setMultiValueJsonProperty(propertyType.getPermId(), List.of("{\"key\": \"value\", \"array\":[1,2,3]}", "{\"key\": \"value2\", \"array\":[]}"));

        // When
        List<DataSetPermId> dataSetIds = v3api.createDataSets(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(dataSetIds.size(), 1);
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        DataSet dataSet = v3api.getDataSets(sessionToken, dataSetIds, fetchOptions).get(dataSetIds.get(0));
        assertEquals(dataSet.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        
        List<String> properties = dataSet.getMultiValueJsonProperty(propertyType.getPermId());
        assertEquals(properties.size(), 2);
        assertEqualsNoOrder(properties.toArray(String[]::new), new String[] {"{\"key\": \"value\", \"array\": [1, 2, 3]}", "{\"key\": \"value2\", \"array\": []}"});
        assertEquals(dataSet.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithMultiValuePropertyOfTypeInteger()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.INTEGER, true);
        EntityTypePermId dataSetType = createADataSetType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        DataSetCreation creation = physicalDataSetCreation();
        creation.setTypeId(dataSetType);
        creation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        creation.setProperty(propertyType.getPermId(), new Long[] {1L, 1L, 3L});

        // When
        List<DataSetPermId> dataSetIds = v3api.createDataSets(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(dataSetIds.size(), 1);
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        DataSet dataSet = v3api.getDataSets(sessionToken, dataSetIds, fetchOptions).get(dataSetIds.get(0));
        assertEquals(dataSet.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        
        assertEqualsNoOrder((Serializable[]) dataSet.getProperties().get(propertyType.getPermId()), new Serializable[] { "1", "1", "3" });
        assertEqualsNoOrder(dataSet.getMultiValueIntegerProperty(propertyType.getPermId()).toArray(Long[]::new), new Long[] { 1L, 1L, 3L });
        assertEquals(dataSet.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithMultiValuePropertyOfTypeIntegerDedicatedMethod()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.INTEGER, true);
        EntityTypePermId dataSetType = createADataSetType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        DataSetCreation creation = physicalDataSetCreation();
        creation.setTypeId(dataSetType);
        creation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        creation.setMultiValueIntegerProperty(propertyType.getPermId(), List.of(1L, 2L, 3L));

        // When
        List<DataSetPermId> dataSetIds = v3api.createDataSets(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(dataSetIds.size(), 1);
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        DataSet dataSet = v3api.getDataSets(sessionToken, dataSetIds, fetchOptions).get(dataSetIds.get(0));
        assertEquals(dataSet.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        
        assertEqualsNoOrder((Serializable[]) dataSet.getProperties().get(propertyType.getPermId()), new Serializable[] { "1", "2", "3" });
        assertEqualsNoOrder(dataSet.getMultiValueIntegerProperty(propertyType.getPermId()).toArray(Long[]::new), new Long[] { 1L, 2L, 3L });
        assertEquals(dataSet.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithPropertyOfTypeArrayInteger()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.ARRAY_INTEGER);
        EntityTypePermId dataSetType = createADataSetType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        DataSetCreation creation = physicalDataSetCreation();
        creation.setTypeId(dataSetType);
        creation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        creation.setIntegerArrayProperty(propertyType.getPermId(), new Long[]{1L, 2L, 3L});

        // When
        List<DataSetPermId> dataSetIds = v3api.createDataSets(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(dataSetIds.size(), 1);
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        DataSet dataSet = v3api.getDataSets(sessionToken, dataSetIds, fetchOptions).get(dataSetIds.get(0));
        assertEquals(dataSet.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        assertEquals(dataSet.getIntegerArrayProperty(propertyType.getPermId()), new Long[]{1L, 2L, 3L});
        assertEquals(dataSet.getProperties().size(), 2);
    }
    
     @Test
    public void testCreateWithMultiValuePropertyOfTypeArrayInteger()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.ARRAY_INTEGER, true);
        EntityTypePermId dataSetType = createADataSetType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        DataSetCreation creation = physicalDataSetCreation();
        creation.setTypeId(dataSetType);
        creation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        creation.setProperty(propertyType.getPermId(), new Long[][]{ new Long[]{1L, 2L, 3L}, new Long[]{4L, 5L, 6L} });

        // When
        List<DataSetPermId> dataSetIds = v3api.createDataSets(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(dataSetIds.size(), 1);
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        DataSet dataSet = v3api.getDataSets(sessionToken, dataSetIds, fetchOptions).get(dataSetIds.get(0));
        assertEquals(dataSet.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");

        List<Long[]> props = dataSet.getMultiValueIntegerArrayProperty(propertyType.getPermId());
        assertEquals(props.size(), 2);
        for(Long[] prop : props) {
            if(prop[0] > 3L) {
                assertEqualsNoOrder(prop, new Long[] {4L, 5L, 6L});
            } else {
                assertEqualsNoOrder(prop, new Long[] {1L, 2L, 3L});
            }
        }

        assertEquals(dataSet.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithMultiValuePropertyOfTypeArrayIntegerDedicatedMethod()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.ARRAY_INTEGER, true);
        EntityTypePermId dataSetType = createADataSetType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        DataSetCreation creation = physicalDataSetCreation();
        creation.setTypeId(dataSetType);
        creation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        creation.setMultiValueIntegerArrayProperty(propertyType.getPermId(), List.of( new Long[]{1L, 2L, 3L}, new Long[]{4L, 5L, 6L} ));

        // When
        List<DataSetPermId> dataSetIds = v3api.createDataSets(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(dataSetIds.size(), 1);
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        DataSet dataSet = v3api.getDataSets(sessionToken, dataSetIds, fetchOptions).get(dataSetIds.get(0));
        assertEquals(dataSet.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");

        List<Long[]> props = dataSet.getMultiValueIntegerArrayProperty(propertyType.getPermId());
        assertEquals(props.size(), 2);
        for(Long[] prop : props) {
            if(prop[0] > 3L) {
                assertEqualsNoOrder(prop, new Long[] {4L, 5L, 6L});
            } else {
                assertEqualsNoOrder(prop, new Long[] {1L, 2L, 3L});
            }
        }

        assertEquals(dataSet.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithPropertyOfTypeArrayReal()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.ARRAY_REAL);
        EntityTypePermId dataSetType = createADataSetType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        DataSetCreation creation = physicalDataSetCreation();
        creation.setTypeId(dataSetType);
        creation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        creation.setRealArrayProperty(propertyType.getPermId(), new Double[]{1.0, 2.0, 3.0});

        // When
        List<DataSetPermId> dataSetIds = v3api.createDataSets(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(dataSetIds.size(), 1);
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        DataSet dataSet = v3api.getDataSets(sessionToken, dataSetIds, fetchOptions).get(dataSetIds.get(0));
        assertEquals(dataSet.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        assertEquals(dataSet.getRealArrayProperty(propertyType.getPermId()), new Double[]{1.0, 2.0, 3.0});
        assertEquals(dataSet.getProperties().size(), 2);
    }
    
    @Test
    public void testCreateWithMultiValuePropertyOfTypeArrayReal()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.ARRAY_REAL, true);
        EntityTypePermId dataSetType = createADataSetType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        DataSetCreation creation = physicalDataSetCreation();
        creation.setTypeId(dataSetType);
        creation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        creation.setProperty(propertyType.getPermId(), new Double[][]{ new Double[]{1.0, 2.0, 3.0}, new Double[]{4.0, 5.0, 6.0} });

        // When
        List<DataSetPermId> dataSetIds = v3api.createDataSets(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(dataSetIds.size(), 1);
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        DataSet dataSet = v3api.getDataSets(sessionToken, dataSetIds, fetchOptions).get(dataSetIds.get(0));
        assertEquals(dataSet.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");

        List<Long[]> props = dataSet.getMultiValueIntegerArrayProperty(propertyType.getPermId());
        assertEquals(props.size(), 2);
        for(Long[] prop : props) {
            if(prop[0] > 3L) {
                assertEqualsNoOrder(prop, new Long[] {4L, 5L, 6L});
            } else {
                assertEqualsNoOrder(prop, new Long[] {1L, 2L, 3L});
            }
        }

        assertEquals(dataSet.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithMultiValuePropertyOfTypeArrayRealDedicatedMethod()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.ARRAY_INTEGER, true);
        EntityTypePermId dataSetType = createADataSetType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        DataSetCreation creation = physicalDataSetCreation();
        creation.setTypeId(dataSetType);
        creation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        creation.setMultiValueIntegerArrayProperty(propertyType.getPermId(), List.of( new Long[]{1L, 2L, 3L}, new Long[]{4L, 5L, 6L} ));

        // When
        List<DataSetPermId> dataSetIds = v3api.createDataSets(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(dataSetIds.size(), 1);
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        DataSet dataSet = v3api.getDataSets(sessionToken, dataSetIds, fetchOptions).get(dataSetIds.get(0));
        assertEquals(dataSet.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");

        List<Long[]> props = dataSet.getMultiValueIntegerArrayProperty(propertyType.getPermId());
        assertEquals(props.size(), 2);
        for(Long[] prop : props) {
            if(prop[0] > 3L) {
                assertEqualsNoOrder(prop, new Long[] {4L, 5L, 6L});
            } else {
                assertEqualsNoOrder(prop, new Long[] {1L, 2L, 3L});
            }
        }

        assertEquals(dataSet.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithPropertyOfTypeArrayString()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.ARRAY_STRING);
        EntityTypePermId dataSetType = createADataSetType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        DataSetCreation creation = physicalDataSetCreation();
        creation.setTypeId(dataSetType);
        creation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        creation.setStringArrayProperty(propertyType.getPermId(), new String[]{"a", "b", "c"});

        // When
        List<DataSetPermId> dataSetIds = v3api.createDataSets(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(dataSetIds.size(), 1);
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        DataSet dataSet = v3api.getDataSets(sessionToken, dataSetIds, fetchOptions).get(dataSetIds.get(0));
        assertEquals(dataSet.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        assertEquals(dataSet.getStringArrayProperty(propertyType.getPermId()), new String[]{"a", "b", "c"});
        assertEquals(dataSet.getProperties().size(), 2);
    }
    
    @Test
    public void testCreateWithMultiValuePropertyOfTypeArrayString()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.ARRAY_STRING, true);
        EntityTypePermId dataSetType = createADataSetType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        DataSetCreation creation = physicalDataSetCreation();
        creation.setTypeId(dataSetType);
        creation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        creation.setMultiValueStringArrayProperty(propertyType.getPermId(), List.of(new String[]{"a,a", "b", "c"}, new String[]{"a", "b", "c", "d"}));

        // When
        List<DataSetPermId> dataSetIds = v3api.createDataSets(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(dataSetIds.size(), 1);
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        DataSet dataSet = v3api.getDataSets(sessionToken, dataSetIds, fetchOptions).get(dataSetIds.get(0));
        assertEquals(dataSet.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        List<String[]> result = dataSet.getMultiValueStringArrayProperty(propertyType.getPermId());
        assertEquals(result.size(), 2);
        for(String[] prop : result) {
            if(prop.length > 3) {
                assertEqualsNoOrder(prop, new String[]{"a", "b", "c", "d"});
            } else {
                assertEqualsNoOrder(prop, new String[]{"a,a", "b", "c"});
            }
        }
        assertEquals(dataSet.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithPropertyOfTypeArrayTimestamp()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.ARRAY_TIMESTAMP);
        EntityTypePermId dataSetType = createADataSetType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        DataSetCreation creation = physicalDataSetCreation();
        creation.setTypeId(dataSetType);
        creation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        ZonedDateTime time1 = ZonedDateTime.parse("2023-05-16T11:22:33+02");
        ZonedDateTime time2 = ZonedDateTime.parse("2023-05-18T11:17:03+02");
        creation.setTimestampArrayProperty(propertyType.getPermId(), new ZonedDateTime[]{time1, time2});

        // When
        List<DataSetPermId> dataSetIds = v3api.createDataSets(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(dataSetIds.size(), 1);
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        DataSet dataSet = v3api.getDataSets(sessionToken, dataSetIds, fetchOptions).get(dataSetIds.get(0));
        assertEquals(dataSet.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        assertEquals(dataSet.getTimestampArrayProperty(propertyType.getPermId()), new ZonedDateTime[]{time1, time2});
        assertEquals(dataSet.getProperties().size(), 2);
    }
    
    @Test
    public void testCreateWithMultiValuePropertyOfTypeArrayTimestamp()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.ARRAY_TIMESTAMP, true);
        EntityTypePermId dataSetType = createADataSetType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        DataSetCreation creation = physicalDataSetCreation();
        creation.setTypeId(dataSetType);
        creation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        ZonedDateTime time1 = ZonedDateTime.parse("2023-05-16T11:22:33+02");
        ZonedDateTime time2 = ZonedDateTime.parse("2023-05-18T11:17:03+02");
        ZonedDateTime time3 = ZonedDateTime.parse("2023-05-20T11:10:03+02");
        creation.setMultiValueTimestampArrayProperty(propertyType.getPermId(), List.of(new ZonedDateTime[]{time1, time2}, new ZonedDateTime[]{time3, time2, time1}));

        // When
        List<DataSetPermId> dataSetIds = v3api.createDataSets(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(dataSetIds.size(), 1);
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        DataSet dataSet = v3api.getDataSets(sessionToken, dataSetIds, fetchOptions).get(dataSetIds.get(0));
        assertEquals(dataSet.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");

        List<ZonedDateTime[]> properties = dataSet.getMultiValueTimestampArrayProperty(propertyType.getPermId());
        assertEquals(properties.size(), 2);
        for(ZonedDateTime[] prop : properties) {
            if(prop.length == 2) {
                assertEqualsNoOrder(prop, new ZonedDateTime[]{time1, time2});
            } else {
                assertEqualsNoOrder(prop, new ZonedDateTime[]{time1, time2, time3});
            }
        }
        assertEquals(dataSet.getProperties().size(), 2);
    }


    @Test
    public void testCreateWithMultiValuePropertyVocabulary() {
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

        EntityTypePermId dataSetType = createADataSetType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        DataSetCreation creation = physicalDataSetCreation();
        creation.setTypeId(dataSetType);
        creation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        creation.setMultiValueControlledVocabularyProperty(propertyType.getPermId(), List.of("DOG", "HUMAN"));

        // When
        List<DataSetPermId> dataSetIds = v3api.createDataSets(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(dataSetIds.size(), 1);
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        DataSet dataSet = v3api.getDataSets(sessionToken, dataSetIds, fetchOptions).get(dataSetIds.get(0));
        assertEquals(dataSet.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        List<String> vocabProperties = dataSet.getMultiValueControlledVocabularyProperty(propertyType.getPermId());
        Collections.sort(vocabProperties);
        assertEquals(vocabProperties, List.of("DOG", "HUMAN"));
        assertEquals(dataSet.getProperties().size(), 2);
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

        EntityTypePermId dataSetType = createADataSetType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        DataSetCreation creation = physicalDataSetCreation();
        creation.setTypeId(dataSetType);
        creation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        creation.setMultiValueControlledVocabularyProperty(propertyType.getPermId(), List.of("DOG", "HUMAN"));

        // When
        List<DataSetPermId> dataSetIds = v3api.createDataSets(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(dataSetIds.size(), 1);
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        DataSet dataSet = v3api.getDataSets(sessionToken, dataSetIds, fetchOptions).get(dataSetIds.get(0));
        assertEquals(dataSet.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        List<String> vocabProperties = dataSet.getMultiValueControlledVocabularyProperty(propertyType.getPermId());
        Collections.sort(vocabProperties);
        assertEquals(vocabProperties, List.of("DOG", "HUMAN"));
        assertEquals(dataSet.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithMultiValuePropertySample() {
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

        EntityTypePermId dataSetType = createADataSetType(sessionToken, true, propertyType, PLATE_GEOMETRY);


        DataSetCreation creation = physicalDataSetCreation();
        creation.setTypeId(dataSetType);
        creation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        creation.setProperty(propertyType.getPermId(), new String[] {"/CISD/CL1", sampleIds.get(0).getPermId()});

        // When
        List<DataSetPermId> dataSetIds = v3api.createDataSets(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(dataSetIds.size(), 1);
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        DataSet dataSet = v3api.getDataSets(sessionToken, dataSetIds, fetchOptions).get(dataSetIds.get(0));
        assertEquals(dataSet.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");

        Map<String, Sample[]> sampleProperties = dataSet.getSampleProperties();

        Sample[] samples = sampleProperties.get(propertyType.getPermId());
        Serializable[] sampleProps = Arrays.stream(samples).map(x -> x.getPermId().getPermId()).sorted().toArray(String[]::new);
        assertEquals(sampleProps, new Serializable[]{"200811050919915-8", sample2.getPermId().getPermId()});

        sampleProps = (Serializable[]) dataSet.getProperties().get(propertyType.getPermId());
        Arrays.sort(sampleProps);
        assertEquals(sampleProps, new Serializable[]{"200811050919915-8", sample2.getPermId().getPermId()});
        assertEquals(dataSet.getProperties().size(), 2);
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

        EntityTypePermId dataSetType = createADataSetType(sessionToken, true, propertyType, PLATE_GEOMETRY);


        DataSetCreation creation = physicalDataSetCreation();
        creation.setTypeId(dataSetType);
        creation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        creation.setMultiValueSampleProperty(propertyType.getPermId(), List.of(new SamplePermId("200811050919915-8"), sampleIds.get(0)));

        // When
        List<DataSetPermId> dataSetIds = v3api.createDataSets(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(dataSetIds.size(), 1);
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        DataSet dataSet = v3api.getDataSets(sessionToken, dataSetIds, fetchOptions).get(dataSetIds.get(0));
        assertEquals(dataSet.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");

        Map<String, Sample[]> sampleProperties = dataSet.getSampleProperties();

        Sample[] samples = sampleProperties.get(propertyType.getPermId());
        Serializable[] sampleProps = Arrays.stream(samples).map(x -> x.getPermId().getPermId()).sorted().toArray(String[]::new);
        assertEquals(sampleProps, new Serializable[]{"200811050919915-8", sample2.getPermId().getPermId()});

        sampleProps = (Serializable[]) dataSet.getProperties().get(propertyType.getPermId());
        Arrays.sort(sampleProps);
        assertEquals(sampleProps, new Serializable[]{"200811050919915-8", sample2.getPermId().getPermId()});
        assertEquals(dataSet.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithMultiValuePropertySample2() {
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

        EntityTypePermId dataSetType = createADataSetType(sessionToken, true, propertyType, PLATE_GEOMETRY);


        DataSetCreation creation = physicalDataSetCreation();
        creation.setTypeId(dataSetType);
        creation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        creation.setMultiValueSampleProperty(propertyType.getPermId(), List.of(new SamplePermId("/CISD/CL1"), sampleIds.get(0)));

        // When
        List<DataSetPermId> dataSetIds = v3api.createDataSets(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(dataSetIds.size(), 1);
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        DataSet dataSet = v3api.getDataSets(sessionToken, dataSetIds, fetchOptions).get(dataSetIds.get(0));
        assertEquals(dataSet.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");

        Map<String, Sample[]> sampleProperties = dataSet.getSampleProperties();

        Sample[] samples = sampleProperties.get(propertyType.getPermId());
        Serializable[] sampleProps = Arrays.stream(samples).map(x -> x.getPermId().getPermId()).sorted().toArray(String[]::new);
        assertEquals(sampleProps, new Serializable[]{"200811050919915-8", sample2.getPermId().getPermId()});

        sampleProps = (Serializable[]) dataSet.getProperties().get(propertyType.getPermId());
        Arrays.sort(sampleProps);
        assertEquals(sampleProps, new Serializable[]{"200811050919915-8", sample2.getPermId().getPermId()});
        assertEquals(dataSet.getProperties().size(), 2);
    }

    @Test
    public void testCreateWithMetaData()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyType = createAPropertyType(sessionToken, DataType.BOOLEAN);
        EntityTypePermId dataSetType = createADataSetType(sessionToken, true, propertyType, PLATE_GEOMETRY);

        DataSetCreation creation = physicalDataSetCreation();
        creation.setTypeId(dataSetType);
        creation.setProperty(PLATE_GEOMETRY.getPermId(), "384_WELLS_16X24");
        creation.setBooleanProperty(propertyType.getPermId(), true);
        creation.setMetaData(Map.of("key", "value"));

        // When
        List<DataSetPermId> dataSetIds = v3api.createDataSets(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(dataSetIds.size(), 1);
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withSampleProperties();
        DataSet dataSet = v3api.getDataSets(sessionToken, dataSetIds, fetchOptions).get(dataSetIds.get(0));
        assertEquals(dataSet.getProperties().get(PLATE_GEOMETRY.getPermId()), "384_WELLS_16X24");
        assertEquals(dataSet.getBooleanProperty(propertyType.getPermId()).booleanValue(), true);
        assertEquals(dataSet.getProperties().size(), 2);
        assertEquals(dataSet.getMetaData(), Map.of("key", "value"));
    }

    @Test(dataProvider = USER_ROLES_PROVIDER)
    public void testCreateWithDifferentRolesExperimentDataSet(RoleWithHierarchy role)
    {
        testWithUserRole(role, params ->
        {
            final ExperimentCreation experimentCreation = new ExperimentCreation();
            experimentCreation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
            experimentCreation.setCode("TEST_EXPERIMENT_" + UUID.randomUUID());
            experimentCreation.setProjectId(params.space1Project1Id);
            experimentCreation.setProperty("DESCRIPTION", "test description");
            final ExperimentPermId experimentId = v3api.createExperiments(params.adminSessionToken, List.of(experimentCreation)).get(0);

            final DataSetCreation dataSetCreation = physicalDataSetCreation();
            dataSetCreation.setExperimentId(experimentId);
            dataSetCreation.setSampleId(null);

            // use instance admin to login on behalf of the user
            final String onBehalfOfSessionToken = v3api.loginAs(TEST_USER, PASSWORD, params.userId);

            if (List.of(RoleWithHierarchy.RoleCode.ADMIN, RoleWithHierarchy.RoleCode.POWER_USER, RoleWithHierarchy.RoleCode.USER)
                    .contains(role.getRoleCode()))
            {
                v3api.createDataSets(onBehalfOfSessionToken, Collections.singletonList(dataSetCreation));
            } else
            {
                assertAnyAuthorizationException(() -> v3api.createDataSets(onBehalfOfSessionToken, Collections.singletonList(dataSetCreation)));
            }
        });
    }

    @Test(dataProvider = USER_ROLES_PROVIDER)
    public void testCreateWithDifferentRolesInstanceSampleDataSet(RoleWithHierarchy role)
    {
        testWithUserRole(role, params ->
        {
            final SampleCreation sampleCreation = new SampleCreation();
            sampleCreation.setCode("TEST_INSTANCE_SAMPLE_" + UUID.randomUUID());
            sampleCreation.setTypeId(new EntityTypePermId("CELL_PLATE"));
            final SamplePermId sampleId = v3api.createSamples(params.adminSessionToken, List.of(sampleCreation)).get(0);

            final DataSetCreation dataSetCreation = physicalDataSetCreation();
            dataSetCreation.setExperimentId(null);
            dataSetCreation.setSampleId(sampleId);

            // use instance admin to login on behalf of the user
            final String onBehalfOfSessionToken = v3api.loginAs(TEST_USER, PASSWORD, params.userId);

            if (RoleWithHierarchy.INSTANCE_ADMIN.equals(role))
            {
                // shared samples cannot have data sets
                assertUserFailureException(() -> v3api.createDataSets(onBehalfOfSessionToken, Collections.singletonList(dataSetCreation)),
                        "shared sample");
            } else
            {
                assertAnyAuthorizationException(() -> v3api.createDataSets(onBehalfOfSessionToken, Collections.singletonList(dataSetCreation)));
            }
        });
    }

    @Test(dataProvider = USER_ROLES_PROVIDER)
    public void testCreateWithDifferentRolesSpaceSampleDataSet(RoleWithHierarchy role)
    {
        testWithUserRole(role, params ->
        {
            final SampleCreation sampleCreation = new SampleCreation();
            sampleCreation.setCode("TEST_SPACE_SAMPLE_" + UUID.randomUUID());
            sampleCreation.setTypeId(new EntityTypePermId("CELL_PLATE"));
            sampleCreation.setSpaceId(params.space1Id);
            final SamplePermId sampleId = v3api.createSamples(params.adminSessionToken, List.of(sampleCreation)).get(0);

            final DataSetCreation dataSetCreation = physicalDataSetCreation();
            dataSetCreation.setExperimentId(null);
            dataSetCreation.setSampleId(sampleId);

            // use instance admin to login on behalf of the user
            final String onBehalfOfSessionToken = v3api.loginAs(TEST_USER, PASSWORD, params.userId);

            if (List.of(RoleWithHierarchy.RoleLevel.INSTANCE, RoleWithHierarchy.RoleLevel.SPACE).contains(role.getRoleLevel()) && List.of(
                            RoleWithHierarchy.RoleCode.ADMIN, RoleWithHierarchy.RoleCode.POWER_USER, RoleWithHierarchy.RoleCode.USER)
                    .contains(role.getRoleCode()))
            {
                v3api.createDataSets(onBehalfOfSessionToken, Collections.singletonList(dataSetCreation));
            } else
            {
                assertAnyAuthorizationException(() -> v3api.createDataSets(onBehalfOfSessionToken, Collections.singletonList(dataSetCreation)));
            }
        });
    }

    @Test(dataProvider = USER_ROLES_PROVIDER)
    public void testCreateWithDifferentRolesProjectSampleDataSet(RoleWithHierarchy role)
    {
        testWithUserRole(role, params ->
        {
            final SampleCreation sampleCreation = new SampleCreation();
            sampleCreation.setCode("TEST_PROJECT_SAMPLE_" + UUID.randomUUID());
            sampleCreation.setTypeId(new EntityTypePermId("CELL_PLATE"));
            sampleCreation.setSpaceId(params.space1Id);
            sampleCreation.setProjectId(params.space1Project1Id);
            final SamplePermId sampleId = v3api.createSamples(params.adminSessionToken, List.of(sampleCreation)).get(0);

            final DataSetCreation dataSetCreation = physicalDataSetCreation();
            dataSetCreation.setExperimentId(null);
            dataSetCreation.setSampleId(sampleId);

            // use instance admin to login on behalf of the user
            final String onBehalfOfSessionToken = v3api.loginAs(TEST_USER, PASSWORD, params.userId);

            if (List.of(RoleWithHierarchy.RoleCode.ADMIN, RoleWithHierarchy.RoleCode.POWER_USER, RoleWithHierarchy.RoleCode.USER)
                    .contains(role.getRoleCode()))
            {
                v3api.createDataSets(onBehalfOfSessionToken, Collections.singletonList(dataSetCreation));
            } else
            {
                assertAnyAuthorizationException(() -> v3api.createDataSets(onBehalfOfSessionToken, Collections.singletonList(dataSetCreation)));
            }
        });
    }

    @Test(dataProvider = USER_ROLES_PROVIDER)
    public void testCreateWithDifferentRolesExperimentSampleDataSet(RoleWithHierarchy role)
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
            final SamplePermId sampleId = v3api.createSamples(params.adminSessionToken, List.of(sampleCreation)).get(0);

            final DataSetCreation dataSetCreation = physicalDataSetCreation();
            dataSetCreation.setExperimentId(null);
            dataSetCreation.setSampleId(sampleId);

            // use instance admin to login on behalf of the user
            final String onBehalfOfSessionToken = v3api.loginAs(TEST_USER, PASSWORD, params.userId);

            if (List.of(RoleWithHierarchy.RoleCode.ADMIN, RoleWithHierarchy.RoleCode.POWER_USER, RoleWithHierarchy.RoleCode.USER)
                    .contains(role.getRoleCode()))
            {
                v3api.createDataSets(onBehalfOfSessionToken, Collections.singletonList(dataSetCreation));
            } else
            {
                assertAnyAuthorizationException(() -> v3api.createDataSets(onBehalfOfSessionToken, Collections.singletonList(dataSetCreation)));
            }
        });
    }

    private DataSetCreation containerDataSetCreation()
    {
        String code = UUID.randomUUID().toString();

        DataSetCreation creation = new DataSetCreation();
        creation.setCode(code);
        creation.setDataSetKind(DataSetKind.CONTAINER);
        creation.setTypeId(new EntityTypePermId("CONTAINER_TYPE"));
        creation.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP1"));
        creation.setDataStoreId(new DataStorePermId("STANDARD"));
        creation.setCreationId(new CreationId(code));
        return creation;
    }

    private DataSetCreation linkDataSetCreation()
    {
        String code = UUID.randomUUID().toString();

        LinkedDataCreation linkedCreation = new LinkedDataCreation();
        linkedCreation.setExternalCode("TEST_EXTERNAL_CODE");
        linkedCreation.setExternalDmsId(new ExternalDmsPermId("DMS_1"));

        DataSetCreation creation = new DataSetCreation();
        creation.setCode(code);
        creation.setDataSetKind(DataSetKind.LINK);
        creation.setTypeId(new EntityTypePermId("LINK_TYPE"));
        creation.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP1"));
        creation.setDataStoreId(new DataStorePermId("STANDARD"));
        creation.setLinkedData(linkedCreation);
        creation.setCreationId(new CreationId(code));

        return creation;
    }

    private ExperimentCreation experimentCreation()
    {
        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode(UUID.randomUUID().toString());
        creation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty("DESCRIPTION", "a description");
        return creation;
    }

    private SampleCreation sampleCreation()
    {
        SampleCreation creation = new SampleCreation();
        creation.setCode(UUID.randomUUID().toString());
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("CISD"));
        return creation;
    }

    private DataSet createDataSet(String sessionToken, DataSetCreation creation, DataSetFetchOptions fo)
    {
        List<DataSetPermId> permIds = v3api.createDataSets(sessionToken, Arrays.asList(creation));
        Map<IDataSetId, DataSet> dataSets = v3api.getDataSets(sessionToken, permIds, fo);
        return dataSets.values().iterator().next();
    }

}

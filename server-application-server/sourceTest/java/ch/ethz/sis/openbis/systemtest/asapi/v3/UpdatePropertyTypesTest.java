/*
 * Copyright ETH 2018 - 2023 Zürich, Scientific IT Services
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

import java.util.*;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.IPropertyTypeId;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.id.DataStorePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyAssignmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.update.PropertyTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;

/**
 * @author Franz-Josef Elmer
 */
public class UpdatePropertyTypesTest extends AbstractTest
{
    private static final String ALL_DATA_TYPES = "ALL_DATA_TYPES";

    @DataProvider
    private Object[][] providerTestUpdateAuthorization()
    {
        return new Object[][] {
                { "NEW_NON_INTERNAL", SYSTEM_USER, SYSTEM_USER, null },
                { "NEW_NON_INTERNAL", SYSTEM_USER, TEST_USER, null },
                { "NEW_NON_INTERNAL", SYSTEM_USER, TEST_POWER_USER_CISD, "Access denied to object with PropertyTypePermId = [NEW_NON_INTERNAL]" },

                { "NEW_NON_INTERNAL", TEST_USER, SYSTEM_USER, null },
                { "NEW_NON_INTERNAL", TEST_USER, TEST_USER, null },
                { "NEW_NON_INTERNAL", TEST_USER, TEST_POWER_USER_CISD, "Access denied to object with PropertyTypePermId = [NEW_NON_INTERNAL]" },

                { "$NEW_INTERNAL", SYSTEM_USER, SYSTEM_USER, null },
                { "$NEW_INTERNAL", SYSTEM_USER, TEST_USER, "Access denied to object with PropertyTypePermId = [$NEW_INTERNAL]" },
                { "$NEW_INTERNAL", SYSTEM_USER, TEST_POWER_USER_CISD, "Access denied to object with PropertyTypePermId = [$NEW_INTERNAL]" },
        };
    }

    @Test(dataProvider = "providerTestUpdateAuthorization")
    public void testUpdateAuthorization(String propertyTypeCode, String propertyTypeRegistrator, String propertyTypeUpdater, String expectedError)
    {
        String registratorSessionToken =
                propertyTypeRegistrator.equals(SYSTEM_USER) ? v3api.loginAsSystem() : v3api.login(propertyTypeRegistrator, PASSWORD);
        String updaterSessionToken = propertyTypeUpdater.equals(SYSTEM_USER) ? v3api.loginAsSystem() : v3api.login(propertyTypeUpdater, PASSWORD);

        PropertyTypeCreation creation = new PropertyTypeCreation();
        creation.setCode(propertyTypeCode);
        creation.setDataType(DataType.VARCHAR);
        creation.setLabel("Test label");
        creation.setDescription("Test description");
        creation.setManagedInternally(propertyTypeCode.startsWith("$"));
        creation.setMultiValue(false);
        List<PropertyTypePermId> ids = v3api.createPropertyTypes(registratorSessionToken, Arrays.asList(creation));
        assertEquals(ids.size(), 1);

        PropertyTypeUpdate update = new PropertyTypeUpdate();
        update.setTypeId(ids.get(0));
        update.setDescription("New description");

        assertExceptionMessage(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updatePropertyTypes(updaterSessionToken, Arrays.asList(update));
                }
            }, expectedError);
    }

    @Test
    public void testUpdatePropertyTypeWhichIsManagedInternally()
    {
        // Given
        String sessionToken = v3api.loginAsSystem();
        PropertyTypePermId id = new PropertyTypePermId("$PLATE_GEOMETRY");
        PropertyTypeUpdate update = new PropertyTypeUpdate();
        update.setTypeId(id);
        update.setDescription("Test description");

        // When
        v3api.updatePropertyTypes(sessionToken, Arrays.asList(update));

        // Then
        PropertyTypeFetchOptions fetchOptions = new PropertyTypeFetchOptions();
        PropertyType propertyType = v3api.getPropertyTypes(sessionToken, Arrays.asList(id), fetchOptions).get(id);
        assertEquals(propertyType.getDescription(), update.getDescription().getValue());
        assertEquals(propertyType.getLabel(), "Plate Geometry");
        assertEquals(propertyType.isManagedInternally().booleanValue(), true);

        v3api.logout(sessionToken);
    }

    @Test
    public void testUpdatePropertyTypeFromExternalNamespace()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId id = new PropertyTypePermId("COMMENT");
        PropertyTypeUpdate update = new PropertyTypeUpdate();
        update.setTypeId(id);
        update.setLabel("Test label");

        // When
        v3api.updatePropertyTypes(sessionToken, Arrays.asList(update));

        // Then
        PropertyTypeFetchOptions fetchOptions = new PropertyTypeFetchOptions();
        PropertyType propertyType = v3api.getPropertyTypes(sessionToken, Arrays.asList(id), fetchOptions).get(id);
        assertEquals(propertyType.getDescription(), "Any other comments");
        assertEquals(propertyType.getLabel(), update.getLabel().getValue());
        assertEquals(propertyType.isManagedInternally().booleanValue(), false);

        v3api.logout(sessionToken);
    }

    @Test
    public void testAddMetaData()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId id = new PropertyTypePermId("COMMENT");
        PropertyTypeUpdate update = new PropertyTypeUpdate();
        update.setTypeId(id);
        update.getMetaData().put("greetings", "hello world");

        // When
        v3api.updatePropertyTypes(sessionToken, Arrays.asList(update));

        // Then
        PropertyTypeFetchOptions fetchOptions = new PropertyTypeFetchOptions();
        PropertyType propertyType = v3api.getPropertyTypes(sessionToken, Arrays.asList(id), fetchOptions).get(id);
        assertEquals(propertyType.getDescription(), "Any other comments");
        assertEquals(propertyType.getLabel(), "Comment");
        assertEquals(propertyType.isManagedInternally().booleanValue(), false);
        assertEquals(propertyType.getMetaData().toString(), "{greetings=hello world}");

        v3api.logout(sessionToken);
    }

    @Test
    public void testRemoveMetaData()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId id = new PropertyTypePermId("DESCRIPTION");
        PropertyTypeUpdate update = new PropertyTypeUpdate();
        update.setTypeId(id);
        update.getMetaData().remove("answer");

        // When
        v3api.updatePropertyTypes(sessionToken, Arrays.asList(update));

        // Then
        PropertyTypeFetchOptions fetchOptions = new PropertyTypeFetchOptions();
        PropertyType propertyType = v3api.getPropertyTypes(sessionToken, Arrays.asList(id), fetchOptions).get(id);
        assertEquals(propertyType.getMetaData().toString(), "{}");

        v3api.logout(sessionToken);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSetMetaData()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId id = new PropertyTypePermId("DESCRIPTION");
        PropertyTypeUpdate update = new PropertyTypeUpdate();
        update.setTypeId(id);
        update.getMetaData().put("greetings", "hello world");
        update.getMetaData().set(Collections.singletonMap("new key", "new value"));

        // When
        v3api.updatePropertyTypes(sessionToken, Arrays.asList(update));

        // Then
        PropertyTypeFetchOptions fetchOptions = new PropertyTypeFetchOptions();
        PropertyType propertyType = v3api.getPropertyTypes(sessionToken, Arrays.asList(id), fetchOptions).get(id);
        assertEquals(propertyType.getMetaData().toString(), "{new key=new value}");

        v3api.logout(sessionToken);
    }

    @DataProvider
    Object[][] dataTypeConversion()
    {
        return new Object[][] {
                new Object[] { DataType.INTEGER, "1234", DataType.VARCHAR, "1234" },
                new Object[] { DataType.INTEGER, "1234", DataType.MULTILINE_VARCHAR, "1234" },
                new Object[] { DataType.REAL, "12.34", DataType.VARCHAR, "12.34" },
                new Object[] { DataType.REAL, "12.34", DataType.MULTILINE_VARCHAR, "12.34" },
                new Object[] { DataType.DATE, "2020-09-03", DataType.VARCHAR, "2020-09-03" },
                new Object[] { DataType.DATE, "2020-09-03", DataType.MULTILINE_VARCHAR, "2020-09-03" },
                new Object[] { DataType.TIMESTAMP, "2020-01-03 14:23:56", DataType.VARCHAR, "2020-01-03 14:23:56 +0100" },
                new Object[] { DataType.TIMESTAMP, "2020-01-03 14:23:56", DataType.MULTILINE_VARCHAR, "2020-01-03 14:23:56 +0100" },
                new Object[] { DataType.CONTROLLEDVOCABULARY, "FLY", DataType.VARCHAR, "FLY" },
                new Object[] { DataType.CONTROLLEDVOCABULARY, "FLY", DataType.MULTILINE_VARCHAR, "FLY" },
                new Object[] { DataType.MATERIAL, "FLU (VIRUS)", DataType.VARCHAR, "FLU (VIRUS)" },
                new Object[] { DataType.MATERIAL, "FLU (VIRUS)", DataType.MULTILINE_VARCHAR, "FLU (VIRUS)" },
                new Object[] { DataType.SAMPLE, "200811050919915-8", DataType.VARCHAR, "200811050919915-8" },
                new Object[] { DataType.SAMPLE, "200811050919915-8", DataType.MULTILINE_VARCHAR, "200811050919915-8" },

                new Object[] { DataType.INTEGER, "1234", DataType.REAL, "1234" },
                new Object[] { DataType.TIMESTAMP, "2020-09-03 14:23:56", DataType.DATE, "2020-09-03" },
        };
    }

    @Test(dataProvider = "dataTypeConversion")
    public void testDataTypeConversion(DataType dataType, String value, DataType newDataType, String convertedValue)
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        createEntityTypesWithAllDataTypeProperties(sessionToken);
        EntityTypePermId entityTypePermId = new EntityTypePermId(ALL_DATA_TYPES);
        SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setTypeId(entityTypePermId);
        sampleCreation.setAutoGeneratedCode(true);
        sampleCreation.setSpaceId(new SpacePermId("CISD"));
        String propertyTypeCode = createPropertyTypeCode(dataType);
        sampleCreation.setProperty(propertyTypeCode, value);
        SamplePermId sampleId = v3api.createSamples(sessionToken, Arrays.asList(sampleCreation)).get(0);
        ExperimentCreation experimentCreation = new ExperimentCreation();
        experimentCreation.setTypeId(entityTypePermId);
        experimentCreation.setCode(dataType + "_EXP");
        experimentCreation.setProjectId(new ProjectIdentifier("/CISD/NOE"));
        experimentCreation.setProperty(propertyTypeCode, value);
        ExperimentPermId experimentId = v3api.createExperiments(sessionToken, Arrays.asList(experimentCreation)).get(0);
        DataSetCreation dataSetCreation = new DataSetCreation();
        dataSetCreation.setTypeId(entityTypePermId);
        dataSetCreation.setDataSetKind(DataSetKind.CONTAINER);
        dataSetCreation.setDataStoreId(new DataStorePermId("STANDARD"));
        dataSetCreation.setProperty(propertyTypeCode, value);
        dataSetCreation.setAutoGeneratedCode(true);
        dataSetCreation.setExperimentId(new ExperimentIdentifier("/CISD/NOE/EXP-TEST-2"));
        DataSetPermId dataSetId = v3api.createDataSets(sessionToken, Arrays.asList(dataSetCreation)).get(0);

        PropertyTypeUpdate update = new PropertyTypeUpdate();
        PropertyTypePermId propertyTypePermId = new PropertyTypePermId(propertyTypeCode);
        update.setTypeId(propertyTypePermId);
        update.convertToDataType(newDataType);
        assertDataType(sessionToken, propertyTypePermId, dataType);

        // When
        v3api.updatePropertyTypes(sessionToken, Arrays.asList(update));

        // Then
        if (dataType == DataType.CONTROLLEDVOCABULARY && newDataType != DataType.CONTROLLEDVOCABULARY) {
            PropertyTypeFetchOptions propertyTypeFetchOptions = new PropertyTypeFetchOptions();
            propertyTypeFetchOptions.withVocabulary();
            PropertyType updated = v3api.getPropertyTypes(sessionToken, Arrays.asList(propertyTypePermId), propertyTypeFetchOptions).get(propertyTypePermId);
            assertNull(updated.getVocabulary());
        }
        assertDataType(sessionToken, propertyTypePermId, newDataType);
        SampleFetchOptions sampleFetchOptions = new SampleFetchOptions();
        sampleFetchOptions.withProperties();
        Sample sample = v3api.getSamples(sessionToken, Arrays.asList(sampleId), sampleFetchOptions).get(sampleId);
        assertEquals(sample.getProperties().get(propertyTypeCode), convertedValue);
        ExperimentFetchOptions experimentFetchOptions = new ExperimentFetchOptions();
        experimentFetchOptions.withProperties();
        Experiment experiment = v3api.getExperiments(sessionToken, Arrays.asList(experimentId), experimentFetchOptions).get(experimentId);
        assertEquals(experiment.getProperties().get(propertyTypeCode), convertedValue);
        DataSetFetchOptions dataSetFetchOptions = new DataSetFetchOptions();
        dataSetFetchOptions.withProperties();
        DataSet dataSet = v3api.getDataSets(sessionToken, Arrays.asList(dataSetId), dataSetFetchOptions).get(dataSetId);
        assertEquals(dataSet.getProperties().get(propertyTypeCode), convertedValue);
        v3api.logout(sessionToken);
    }

    private void assertDataType(String sessionToken, PropertyTypePermId id, DataType expectedDataType)
    {
        PropertyTypeFetchOptions fetchOptions = new PropertyTypeFetchOptions();
        PropertyType propertyType = v3api.getPropertyTypes(sessionToken, Arrays.asList(id), fetchOptions).get(id);
        assertEquals(propertyType.getDataType(), expectedDataType);
    }

    private void createEntityTypesWithAllDataTypeProperties(String sessionToken)
    {
        createPropertyTypesForAllDataTypes(sessionToken);
        List<PropertyAssignmentCreation> propertyAssignments = createAllDataTypePropertyAssignments();

        SampleTypeCreation sampleTypeCreation = new SampleTypeCreation();
        sampleTypeCreation.setCode(ALL_DATA_TYPES);
        sampleTypeCreation.setAutoGeneratedCode(true);
        sampleTypeCreation.setGeneratedCodePrefix("ADT-");
        sampleTypeCreation.setPropertyAssignments(propertyAssignments);
        v3api.createSampleTypes(sessionToken, Arrays.asList(sampleTypeCreation));

        ExperimentTypeCreation experimentTypeCreation = new ExperimentTypeCreation();
        experimentTypeCreation.setCode(ALL_DATA_TYPES);
        experimentTypeCreation.setPropertyAssignments(propertyAssignments);
        v3api.createExperimentTypes(sessionToken, Arrays.asList(experimentTypeCreation));

        DataSetTypeCreation dataSetTypeCreation = new DataSetTypeCreation();
        dataSetTypeCreation.setCode(ALL_DATA_TYPES);
        dataSetTypeCreation.setPropertyAssignments(propertyAssignments);
        v3api.createDataSetTypes(sessionToken, Arrays.asList(dataSetTypeCreation));
    }

    private List<PropertyAssignmentCreation> createAllDataTypePropertyAssignments()
    {
        List<PropertyAssignmentCreation> propertyAssignments = new ArrayList<>();
        for (DataType dataType : DataType.values())
        {
            PropertyAssignmentCreation assignmentCreation = new PropertyAssignmentCreation();
            assignmentCreation.setPropertyTypeId(new PropertyTypePermId(createPropertyTypeCode(dataType)));
            propertyAssignments.add(assignmentCreation);
        }
        return propertyAssignments;
    }

    private void createPropertyTypesForAllDataTypes(String sessionToken)
    {
        List<PropertyTypeCreation> propertyTypes = new ArrayList<>();
        for (DataType dataType : DataType.values())
        {
            PropertyTypeCreation typeCreation = new PropertyTypeCreation();
            typeCreation.setCode(createPropertyTypeCode(dataType));
            typeCreation.setDataType(dataType);
            typeCreation.setLabel("label");
            typeCreation.setDescription("description");
            if (dataType == DataType.CONTROLLEDVOCABULARY)
            {
                typeCreation.setVocabularyId(new VocabularyPermId("ORGANISM"));
            }
            typeCreation.setMultiValue(false);
            propertyTypes.add(typeCreation);
        }
        v3api.createPropertyTypes(sessionToken, propertyTypes);
    }

    private String createPropertyTypeCode(DataType dataType)
    {
        return dataType + "_PROPERTY";
    }

    @Test
    public void testMissingId()
    {
        PropertyTypeUpdate update = new PropertyTypeUpdate();

        assertUserFailureException(update, "Property type id cannot be null.");
    }

    @Test
    public void testNullDescription()
    {
        PropertyTypeUpdate update = new PropertyTypeUpdate();
        update.setTypeId(new PropertyTypePermId("COMMENT"));
        update.setDescription(null);

        assertUserFailureException(update, "Description cannot be empty.");
    }

    @Test
    public void testEmptyDescription()
    {
        PropertyTypeUpdate update = new PropertyTypeUpdate();
        update.setTypeId(new PropertyTypePermId("COMMENT"));
        update.setDescription("");

        assertUserFailureException(update, "Description cannot be empty.");
    }

    @Test
    public void testNullLabel()
    {
        PropertyTypeUpdate update = new PropertyTypeUpdate();
        update.setTypeId(new PropertyTypePermId("COMMENT"));
        update.setLabel(null);

        assertUserFailureException(update, "Label cannot be empty.");
    }

    @Test
    public void testEmptyLabel()
    {
        PropertyTypeUpdate update = new PropertyTypeUpdate();
        update.setTypeId(new PropertyTypePermId("COMMENT"));
        update.setLabel("");

        assertUserFailureException(update, "Label cannot be empty.");
    }

    @Test
    public void testInvalidSchema()
    {
        PropertyTypeUpdate update = new PropertyTypeUpdate();
        update.setTypeId(createXmlPropertyType());
        update.setSchema("blabla");

        assertUserFailureException(update, "isn't a well formed XML document. Content is not allowed in prolog.");
    }

    @Test(groups = "broken")
    public void testInvalidTransformation()
    {
        PropertyTypeUpdate update = new PropertyTypeUpdate();
        update.setTypeId(createXmlPropertyType());
        update.setTransformation(CreatePropertyTypeTest.EXAMPLE_INCORRECT_XSLT);

        assertUserFailureException(update, "Provided XSLT isn't valid.");
    }

    @Test
    public void testSchemaSpecifiedButDataTypeNotXML()
    {
        PropertyTypeUpdate update = new PropertyTypeUpdate();
        update.setTypeId(new PropertyTypePermId("COMMENT"));
        update.setSchema(CreatePropertyTypeTest.EXAMPLE_SCHEMA);

        assertUserFailureException(update, "XML schema is specified but data type is VARCHAR.");
    }

    @Test
    public void testTransformationSpecifiedButDataTypeNotXML()
    {
        PropertyTypeUpdate update = new PropertyTypeUpdate();
        update.setTypeId(new PropertyTypePermId("COMMENT"));
        update.setTransformation(CreatePropertyTypeTest.EXAMPLE_XSLT);

        assertUserFailureException(update, "XSLT transformation is specified but data type is VARCHAR.");
    }

    @Test(dataProvider = "usersNotAllowedToUpdatePropertyTypes")
    public void testUpdateWithUserCausingAuthorizationFailure(final String user)
    {
        PropertyTypePermId typeId = new PropertyTypePermId("COMMENT");
        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken = v3api.login(user, PASSWORD);
                    PropertyTypeUpdate update = new PropertyTypeUpdate();
                    update.setTypeId(typeId);
                    update.setDescription("test");
                    v3api.updatePropertyTypes(sessionToken, Arrays.asList(update));
                }
            }, typeId);
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PropertyTypeUpdate update = new PropertyTypeUpdate();
        update.setTypeId(new PropertyTypePermId("BACTERIUM"));

        PropertyTypeUpdate update2 = new PropertyTypeUpdate();
        update2.setTypeId(new PropertyTypePermId("ORGANISM"));

        v3api.updatePropertyTypes(sessionToken, Arrays.asList(update, update2));

        assertAccessLog(
                "update-property-types  PROPERTY_TYPE_UPDATES('[PropertyTypeUpdate[typeId=BACTERIUM], PropertyTypeUpdate[typeId=ORGANISM]]')");
    }

    @DataProvider
    Object[][] usersNotAllowedToUpdatePropertyTypes()
    {
        return createTestUsersProvider(TEST_GROUP_ADMIN, TEST_GROUP_OBSERVER, TEST_GROUP_POWERUSER,
                TEST_INSTANCE_OBSERVER, TEST_OBSERVER_CISD, TEST_POWER_USER_CISD, TEST_SPACE_USER);
    }

    private PropertyTypePermId createXmlPropertyType()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypeCreation creation = new PropertyTypeCreation();
        creation.setCode("TEST-" + System.currentTimeMillis());
        creation.setLabel("Test");
        creation.setDescription("Testing");
        creation.setDataType(DataType.XML);
        creation.setMultiValue(false);
        PropertyTypePermId permId = v3api.createPropertyTypes(sessionToken, Arrays.asList(creation)).get(0);
        v3api.logout(sessionToken);
        return permId;
    }

    private void assertUserFailureException(PropertyTypeUpdate update, String expectedMessage)
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    // When
                    v3api.updatePropertyTypes(sessionToken, Arrays.asList(update));
                }
            },
                // Then
                expectedMessage);
        v3api.logout(sessionToken);
    }

}

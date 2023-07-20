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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.IPropertyTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search.PropertyTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;

/**
 * @author Franz-Josef Elmer
 */
public class CreatePropertyTypeTest extends AbstractTest
{
    static String EXAMPLE_SCHEMA =
            "<?xml version='1.0'?>\n"
                    + "<xs:schema targetNamespace='http://my.host.org' xmlns:xs='http://www.w3.org/2001/XMLSchema'>\n"
                    + "<xs:element name='note'>\n"
                    + "  <xs:complexType>\n"
                    + "    <xs:sequence>\n"
                    + "      <xs:element name='to' type='xs:string'/>\n"
                    + "      <xs:element name='from' type='xs:string'/>\n"
                    + "      <xs:element name='heading' type='xs:string'/>\n"
                    + "      <xs:element name='body' type='xs:string'/>\n"
                    + "    </xs:sequence>\n"
                    + "  </xs:complexType>\n"
                    + "</xs:element>\n"
                    + "</xs:schema>";

    static String EXAMPLE_XSLT = "<?xml version='1.0'?>\n"
            + "<xsl:stylesheet version='1.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>"
            + "<xsl:output method='html'/>\n                            "
            + "<xsl:template match='child::person'>\n                   "
            + " <html>\n                                                "
            + "  <head>\n                                               "
            + "   <title>\n                                             "
            + "    <xsl:value-of select='descendant::firstname' />\n    "
            + "    <xsl:text> </xsl:text>\n                             "
            + "    <xsl:value-of select='descendant::lastname' />\n     "
            + "   </title>\n                                            "
            + "  </head>\n                                              "
            + "  <body>\n                                               "
            + "   <xsl:value-of select='descendant::firstname' />\n     "
            + "   <xsl:text> </xsl:text>\n                              "
            + "   <xsl:value-of select='descendant::lastname' />\n      "
            + "  </body>\n                                              "
            + " </html>\n                                               "
            + "</xsl:template>\n                                        "
            + "</xsl:stylesheet>";

    static String EXAMPLE_INCORRECT_XSLT = EXAMPLE_XSLT.replaceAll("xsl:stylesheet",
            "xsl:styleshet");

    @DataProvider
    private Object[][] providerTestCreateAuthorization()
    {
        return new Object[][] {
                { "NEW_NON_INTERNAL", SYSTEM_USER, null },
                { "NEW_NON_INTERNAL", TEST_USER, null },
                { "NEW_NON_INTERNAL", TEST_POWER_USER_CISD, "Access denied to object with PropertyTypePermId = [NEW_NON_INTERNAL]" },

                { "$NEW_INTERNAL", SYSTEM_USER, null },
                { "$NEW_INTERNAL", TEST_USER, "Access denied to object with PropertyTypePermId = [$NEW_INTERNAL]" },
                { "$NEW_INTERNAL", TEST_POWER_USER_CISD, "Access denied to object with PropertyTypePermId = [$NEW_INTERNAL]" }
        };
    }

    @Test(dataProvider = "providerTestCreateAuthorization")
    public void testCreateAuthorization(String propertyTypeCode, String propertyTypeRegistrator, String expectedError)
    {
        String sessionToken = propertyTypeRegistrator.equals(SYSTEM_USER) ? v3api.loginAsSystem() : v3api.login(propertyTypeRegistrator, PASSWORD);

        PropertyTypeCreation creation = new PropertyTypeCreation();
        creation.setCode(propertyTypeCode);
        creation.setDataType(DataType.VARCHAR);
        creation.setLabel("Test label");
        creation.setDescription("Test description");
        creation.setManagedInternally(propertyTypeCode.startsWith("$"));
        creation.setMultiValue(false);
        assertExceptionMessage(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    List<PropertyTypePermId> ids = v3api.createPropertyTypes(sessionToken, Arrays.asList(creation));
                    assertEquals(ids.size(), 1);
                }
            }, expectedError);
    }

    @Test
    public void testCreateManagedInternallyPropertyTypeWithCodeWithDolarSign()
    {
        // Given
        String sessionToken = v3api.loginAsSystem();
        PropertyTypeCreation creation = new PropertyTypeCreation();
        creation.setCode("$test-property");
        creation.setDataType(DataType.REAL);
        creation.setDescription("only for testing");
        creation.setLabel("Test Property");
        creation.setManagedInternally(true);
        creation.setMultiValue(false);
        // When
        List<PropertyTypePermId> ids = v3api.createPropertyTypes(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(ids.toString(), "[$TEST-PROPERTY]");
        PropertyTypeSearchCriteria searchCriteria = new PropertyTypeSearchCriteria();
        searchCriteria.withCode().thatEquals(creation.getCode().toUpperCase());
        PropertyTypeFetchOptions fetchOptions = new PropertyTypeFetchOptions();
        Map<IPropertyTypeId, PropertyType> types = v3api.getPropertyTypes(sessionToken, ids, fetchOptions);
        PropertyType propertyType = types.get(ids.get(0));
        assertEquals(propertyType.getCode(), creation.getCode().toUpperCase());
        assertEquals(propertyType.getPermId(), ids.get(0));
        assertEquals(propertyType.getDataType(), creation.getDataType());
        assertEquals(propertyType.getDescription(), creation.getDescription());
        assertEquals(propertyType.getLabel(), creation.getLabel());
        assertEquals(propertyType.isManagedInternally().booleanValue(), creation.isManagedInternally());
        assertEquals(propertyType.getMetaData().toString(), "{}");
        assertEquals(types.size(), 1);

        v3api.logout(sessionToken);
    }

    @Test
    public void testCreateManagedInternallyPropertyTypeWithCodeWithoutDollarSign()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypeCreation creation = new PropertyTypeCreation();
        creation.setCode("test-property");
        creation.setLabel("Test Property");
        creation.setDescription("only for testing");
        creation.setDataType(DataType.REAL);
        creation.setManagedInternally(true);
        creation.setMultiValue(false);
        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createPropertyTypes(sessionToken, Arrays.asList(creation));
                }
            }, "Code of an internally managed property type has to start with '$' prefix");
    }

    @Test
    public void testCreateNonManagedInternallyPropertyTypeWithCodeWithDolarSign()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypeCreation creation = new PropertyTypeCreation();
        creation.setCode("$test-property");
        creation.setLabel("Test Property");
        creation.setDescription("only for testing");
        creation.setDataType(DataType.REAL);
        creation.setManagedInternally(false);
        creation.setMultiValue(false);
        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createPropertyTypes(sessionToken, Arrays.asList(creation));
                }
            }, "'$' code prefix can be only used for the internally managed property types");
    }

    @Test
    public void testCreateNonManagedInternallyPropertyTypeWithCodeWithoutDolarSign()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypeCreation creation = new PropertyTypeCreation();
        creation.setCode("test-property");
        creation.setLabel("Test Property");
        creation.setDescription("only for testing");
        creation.setDataType(DataType.REAL);
        creation.setManagedInternally(false);
        HashMap<String, String> metaData = new HashMap<>();
        metaData.put("greeting", "hello { meta data }");
        creation.setMetaData(metaData);
        creation.setMultiValue(false);
        List<PropertyTypePermId> ids = v3api.createPropertyTypes(sessionToken, Arrays.asList(creation));
        assertEquals(ids.toString(), "[TEST-PROPERTY]");

        PropertyTypeSearchCriteria searchCriteria = new PropertyTypeSearchCriteria();
        searchCriteria.withCode().thatEquals(creation.getCode().toUpperCase());

        Map<IPropertyTypeId, PropertyType> types = v3api.getPropertyTypes(sessionToken, ids, new PropertyTypeFetchOptions());

        PropertyType propertyType = types.get(ids.get(0));
        assertEquals(propertyType.getCode(), creation.getCode().toUpperCase());
        assertEquals(propertyType.getDataType(), creation.getDataType());
        assertEquals(propertyType.isManagedInternally(), (Boolean) creation.isManagedInternally());
        assertEquals(propertyType.getMetaData().toString(), "{greeting=hello { meta data }}");
    }

    @Test(groups = "broken")
    public void testCreateXmlPropertyType()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypeCreation creation = new PropertyTypeCreation();
        creation.setCode("test-property");
        creation.setDataType(DataType.XML);
        creation.setDescription("only for testing");
        creation.setLabel("Test Property");
        creation.setSchema(EXAMPLE_SCHEMA);
        creation.setTransformation(EXAMPLE_XSLT);
        creation.setMultiValue(false);
        // When
        List<PropertyTypePermId> ids = v3api.createPropertyTypes(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(ids.toString(), "[TEST-PROPERTY]");
        PropertyTypeSearchCriteria searchCriteria = new PropertyTypeSearchCriteria();
        searchCriteria.withCode().thatEquals(creation.getCode().toUpperCase());
        PropertyTypeFetchOptions fetchOptions = new PropertyTypeFetchOptions();
        Map<IPropertyTypeId, PropertyType> types = v3api.getPropertyTypes(sessionToken, ids, fetchOptions);
        PropertyType propertyType = types.get(ids.get(0));
        assertEquals(propertyType.getCode(), creation.getCode().toUpperCase());
        assertEquals(propertyType.getPermId(), ids.get(0));
        assertEquals(propertyType.getDataType(), creation.getDataType());
        assertEquals(propertyType.getDescription(), creation.getDescription());
        assertEquals(propertyType.getLabel(), creation.getLabel());
        assertEquals(propertyType.isManagedInternally().booleanValue(), false);
        assertEquals(propertyType.getSchema(), creation.getSchema());
        assertEquals(propertyType.getTransformation(), creation.getTransformation());
        assertEquals(types.size(), 1);

        v3api.logout(sessionToken);
    }

    @Test
    public void testCreateVocabularyPropertyType()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypeCreation creation = new PropertyTypeCreation();
        creation.setCode("test-voca-property");
        creation.setDataType(DataType.CONTROLLEDVOCABULARY);
        creation.setDescription("only for testing");
        creation.setLabel("Test Vocabulary Property");
        creation.setVocabularyId(new VocabularyPermId("test_vocabulary"));
        creation.setMultiValue(false);
        // When
        List<PropertyTypePermId> ids = v3api.createPropertyTypes(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(ids.toString(), "[TEST-VOCA-PROPERTY]");
        PropertyTypeSearchCriteria searchCriteria = new PropertyTypeSearchCriteria();
        searchCriteria.withCode().thatEquals(creation.getCode().toUpperCase());
        PropertyTypeFetchOptions fetchOptions = new PropertyTypeFetchOptions();
        fetchOptions.withVocabulary();
        Map<IPropertyTypeId, PropertyType> types = v3api.getPropertyTypes(sessionToken, ids, fetchOptions);
        PropertyType propertyType = types.get(ids.get(0));
        assertEquals(propertyType.getCode(), creation.getCode().toUpperCase());
        assertEquals(propertyType.getPermId(), ids.get(0));
        assertEquals(propertyType.getDataType(), creation.getDataType());
        assertEquals(propertyType.getDescription(), creation.getDescription());
        assertEquals(propertyType.getLabel(), creation.getLabel());
        assertEquals(propertyType.isManagedInternally().booleanValue(), false);
        assertEquals(propertyType.getVocabulary().getCode(), "TEST_VOCABULARY");
        assertEquals(propertyType.getVocabulary().getDescription(), "Test vocabulary");
        assertEquals(types.size(), 1);

        v3api.logout(sessionToken);
    }

    @Test
    public void testCreateMaterialPropertyType()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypeCreation creation = new PropertyTypeCreation();
        creation.setCode("test-material-property");
        creation.setDataType(DataType.MATERIAL);
        creation.setDescription("only for testing");
        creation.setLabel("Test Material Property");
        creation.setMaterialTypeId(new EntityTypePermId("SIRNA", EntityKind.MATERIAL));
        creation.setMultiValue(false);
        // When
        List<PropertyTypePermId> ids = v3api.createPropertyTypes(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(ids.toString(), "[TEST-MATERIAL-PROPERTY]");
        PropertyTypeSearchCriteria searchCriteria = new PropertyTypeSearchCriteria();
        searchCriteria.withCode().thatEquals(creation.getCode().toUpperCase());
        PropertyTypeFetchOptions fetchOptions = new PropertyTypeFetchOptions();
        fetchOptions.withMaterialType();
        Map<IPropertyTypeId, PropertyType> types = v3api.getPropertyTypes(sessionToken, ids, fetchOptions);
        PropertyType propertyType = types.get(ids.get(0));
        assertEquals(propertyType.getCode(), creation.getCode().toUpperCase());
        assertEquals(propertyType.getPermId(), ids.get(0));
        assertEquals(propertyType.getDataType(), creation.getDataType());
        assertEquals(propertyType.getDescription(), creation.getDescription());
        assertEquals(propertyType.getLabel(), creation.getLabel());
        assertEquals(propertyType.isManagedInternally().booleanValue(), false);
        assertEquals(propertyType.getMaterialType().getCode(), "SIRNA");
        assertEquals(propertyType.getMaterialType().getDescription(), "Oligo nucleotide");
        assertEquals(types.size(), 1);

        v3api.logout(sessionToken);
    }

    @Test
    public void testCreateSamplePropertyType()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypeCreation creation = new PropertyTypeCreation();
        creation.setCode("test-sample-property");
        creation.setDataType(DataType.SAMPLE);
        creation.setDescription("only for testing");
        creation.setLabel("Test Sample Property");
        creation.setSampleTypeId(new EntityTypePermId("WELL", EntityKind.SAMPLE));
        creation.setMultiValue(false);
        // When
        List<PropertyTypePermId> ids = v3api.createPropertyTypes(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(ids.toString(), "[TEST-SAMPLE-PROPERTY]");
        PropertyTypeSearchCriteria searchCriteria = new PropertyTypeSearchCriteria();
        searchCriteria.withCode().thatEquals(creation.getCode().toUpperCase());
        PropertyTypeFetchOptions fetchOptions = new PropertyTypeFetchOptions();
        fetchOptions.withSampleType();
        Map<IPropertyTypeId, PropertyType> types = v3api.getPropertyTypes(sessionToken, ids, fetchOptions);
        PropertyType propertyType = types.get(ids.get(0));
        assertEquals(propertyType.getCode(), creation.getCode().toUpperCase());
        assertEquals(propertyType.getPermId(), ids.get(0));
        assertEquals(propertyType.getDataType(), creation.getDataType());
        assertEquals(propertyType.getDescription(), creation.getDescription());
        assertEquals(propertyType.getLabel(), creation.getLabel());
        assertEquals(propertyType.isManagedInternally().booleanValue(), false);
        assertEquals(propertyType.getSampleType().getCode(), "WELL");
        assertEquals(propertyType.getSampleType().getDescription(), "Plate Well");
        assertEquals(types.size(), 1);

        v3api.logout(sessionToken);
    }

    @Test
    public void testCreateDatePropertyType()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypeCreation creation = new PropertyTypeCreation();
        creation.setCode("test-date-property");
        creation.setDataType(DataType.DATE);
        creation.setDescription("only for testing");
        creation.setLabel("Test Date Property");
        creation.setMultiValue(false);
        // When
        List<PropertyTypePermId> ids = v3api.createPropertyTypes(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(ids.toString(), "[TEST-DATE-PROPERTY]");
        PropertyTypeSearchCriteria searchCriteria = new PropertyTypeSearchCriteria();
        searchCriteria.withCode().thatEquals(creation.getCode().toUpperCase());
        PropertyTypeFetchOptions fetchOptions = new PropertyTypeFetchOptions();
        fetchOptions.withSampleType();
        Map<IPropertyTypeId, PropertyType> types = v3api.getPropertyTypes(sessionToken, ids, fetchOptions);
        PropertyType propertyType = types.get(ids.get(0));
        assertEquals(propertyType.getCode(), creation.getCode().toUpperCase());
        assertEquals(propertyType.getPermId(), ids.get(0));
        assertEquals(propertyType.getDataType(), creation.getDataType());
        assertEquals(propertyType.getDescription(), creation.getDescription());
        assertEquals(propertyType.getLabel(), creation.getLabel());
        assertEquals(propertyType.isManagedInternally().booleanValue(), false);
        assertEquals(types.size(), 1);

        v3api.logout(sessionToken);
    }

    @Test
    public void testMissingCode()
    {
        PropertyTypeCreation creation = createBasic();
        creation.setCode(null);

        assertUserFailureException(creation, "Code cannot be empty.");
    }

    @Test
    public void testEmptyCode()
    {
        PropertyTypeCreation creation = createBasic();
        creation.setCode("");

        assertUserFailureException(creation, "Code cannot be empty.");
    }

    @Test
    public void testMissingLabel()
    {
        PropertyTypeCreation creation = createBasic();
        creation.setLabel(null);

        assertUserFailureException(creation, "Label cannot be empty.");
    }

    @Test
    public void testEmptyLabel()
    {
        PropertyTypeCreation creation = createBasic();
        creation.setLabel("");

        assertUserFailureException(creation, "Label cannot be empty.");
    }

    @Test
    public void testMissingDescription()
    {
        PropertyTypeCreation creation = createBasic();
        creation.setDescription(null);

        assertUserFailureException(creation, "Description cannot be empty.");
    }

    @Test
    public void testEmptyDescription()
    {
        PropertyTypeCreation creation = createBasic();
        creation.setDescription("");

        assertUserFailureException(creation, "Description cannot be empty.");
    }

    @Test
    public void testMissingDataType()
    {
        PropertyTypeCreation creation = createBasic();
        creation.setDataType(null);
        ;

        assertUserFailureException(creation, "Data type not specified.");
    }

    @Test
    public void testVocabularyTypeMissingVocabulary()
    {
        PropertyTypeCreation creation = createBasic();
        creation.setDataType(DataType.CONTROLLEDVOCABULARY);

        assertUserFailureException(creation, "Data type has been specified as CONTROLLEDVOCABULARY but vocabulary id is missing.");
    }

    @Test
    public void tesVocabularyTypetWithUnknownVocabulary()
    {
        PropertyTypeCreation creation = createBasic();
        creation.setDataType(DataType.CONTROLLEDVOCABULARY);
        creation.setVocabularyId(new VocabularyPermId("UNKNOWN"));

        assertUserFailureException(creation, "VocabularyPermId = [UNKNOWN] has not been found");
    }

    @Test
    public void testVocabularyIdButWrongDataType()
    {
        PropertyTypeCreation creation = createBasic();
        creation.setDataType(DataType.REAL);
        creation.setVocabularyId(new VocabularyPermId("GENDER"));

        assertUserFailureException(creation, "Vocabulary id has been specified but data type is REAL.");
    }

    @Test
    public void testMaterialTypeIdButWrongDataType()
    {
        PropertyTypeCreation creation = createBasic();
        creation.setDataType(DataType.REAL);
        creation.setMaterialTypeId(new EntityTypePermId("SIRNA", EntityKind.MATERIAL));

        assertUserFailureException(creation, "Material type id has been specified but data type is REAL.");
    }

    @Test
    public void testEntityTypeIdButWrongEntityKind()
    {
        PropertyTypeCreation creation = createBasic();
        creation.setDataType(DataType.MATERIAL);
        creation.setMaterialTypeId(new EntityTypePermId("UNKNOWN", EntityKind.DATA_SET));

        assertUserFailureException(creation, "Specified entity type id (UNKNOWN (DATA_SET)) is not a MATERIAL type.");
    }

    @Test
    public void testUnknownMaterialType()
    {
        PropertyTypeCreation creation = createBasic();
        creation.setDataType(DataType.MATERIAL);
        creation.setMaterialTypeId(new EntityTypePermId("UNKNOWN", EntityKind.MATERIAL));

        assertUserFailureException(creation, "EntityTypePermId = [UNKNOWN (MATERIAL)] has not been found.");
    }

    @Test
    public void testVocabularyTypeWithVocabularyIdAndMaterialTypeId()
    {
        PropertyTypeCreation creation = createBasic();
        creation.setDataType(DataType.CONTROLLEDVOCABULARY);
        creation.setVocabularyId(new VocabularyPermId("GENDER"));
        creation.setMaterialTypeId(new EntityTypePermId("SIRNA", EntityKind.MATERIAL));

        assertUserFailureException(creation, "Material type id has been specified but data type is CONTROLLEDVOCABULARY.");
    }

    @Test
    public void testMaterialTypeWithVocabularyIdAndMaterialTypeId()
    {
        PropertyTypeCreation creation = createBasic();
        creation.setDataType(DataType.MATERIAL);
        creation.setVocabularyId(new VocabularyPermId("GENDER"));
        creation.setMaterialTypeId(new EntityTypePermId("SIRNA", EntityKind.MATERIAL));

        assertUserFailureException(creation, "Vocabulary id has been specified but data type is MATERIAL.");
    }

    @Test
    public void testInvalidSchema()
    {
        PropertyTypeCreation creation = createBasic();
        creation.setDataType(DataType.XML);
        creation.setSchema("blabla");

        assertUserFailureException(creation, "isn't a well formed XML document. Content is not allowed in prolog.");
    }

    @Test(groups = "broken")
    public void testInvalidTransformation()
    {
        PropertyTypeCreation creation = createBasic();
        creation.setDataType(DataType.XML);
        creation.setTransformation(EXAMPLE_INCORRECT_XSLT);

        assertUserFailureException(creation, "Provided XSLT isn't valid.");
    }

    @Test
    public void testSchemaSpecifiedButDataTypeNotXML()
    {
        PropertyTypeCreation creation = createBasic();
        creation.setDataType(DataType.VARCHAR);
        creation.setSchema(EXAMPLE_SCHEMA);

        assertUserFailureException(creation, "XML schema is specified but data type is VARCHAR.");
    }

    @Test
    public void testTransformationSpecifiedButDataTypeNotXML()
    {
        PropertyTypeCreation creation = createBasic();
        creation.setDataType(DataType.VARCHAR);
        creation.setTransformation(EXAMPLE_XSLT);

        assertUserFailureException(creation, "XSLT transformation is specified but data type is VARCHAR.");
    }

    @Test(dataProvider = "usersNotAllowedToCreatePropertyTypes")
    public void testCreateWithUserCausingAuthorizationFailure(final String user)
    {
        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken = v3api.login(user, PASSWORD);
                    PropertyTypeCreation creation = new PropertyTypeCreation();
                    creation.setCode("TEST");
                    creation.setLabel("test label");
                    creation.setDescription("test description");
                    creation.setDataType(DataType.REAL);
                    creation.setMultiValue(false);
                    v3api.createPropertyTypes(sessionToken, Arrays.asList(creation));
                }
            }, new PropertyTypePermId("TEST"));
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PropertyTypeCreation creation = new PropertyTypeCreation();
        creation.setCode("LOG_TEST_1");
        creation.setLabel("label1");
        creation.setDescription("description1");
        creation.setDataType(DataType.BOOLEAN);
        creation.setMultiValue(false);
        PropertyTypeCreation creation2 = new PropertyTypeCreation();
        creation2.setCode("LOG_TEST_2");
        creation2.setLabel("label2");
        creation2.setDescription("description2");
        creation2.setDataType(DataType.BOOLEAN);
        creation.setMultiValue(false);
        v3api.createPropertyTypes(sessionToken, Arrays.asList(creation, creation2));

        assertAccessLog(
                "create-property-types  NEW_PROPERTY_TYPES('[PropertyTypeCreation[code=LOG_TEST_1], PropertyTypeCreation[code=LOG_TEST_2]]')");
    }

    @DataProvider
    Object[][] usersNotAllowedToCreatePropertyTypes()
    {
        return createTestUsersProvider(TEST_GROUP_ADMIN, TEST_GROUP_OBSERVER, TEST_GROUP_POWERUSER,
                TEST_INSTANCE_OBSERVER, TEST_OBSERVER_CISD, TEST_POWER_USER_CISD, TEST_SPACE_USER);
    }

    private PropertyTypeCreation createBasic()
    {
        PropertyTypeCreation creation = new PropertyTypeCreation();
        creation.setCode("TEST");
        creation.setLabel("Test");
        creation.setDescription("Testing");
        creation.setDataType(DataType.REAL);
        creation.setMultiValue(false);
        return creation;
    }

    private void assertUserFailureException(PropertyTypeCreation creation, String expectedMessage)
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        assertUserFailureException(new IDelegatedAction()
            {

                @Override
                public void execute()
                {
                    // When
                    v3api.createPropertyTypes(sessionToken, Arrays.asList(creation));
                }
            },

                // Then
                expectedMessage);
        v3api.logout(sessionToken);
    }

}

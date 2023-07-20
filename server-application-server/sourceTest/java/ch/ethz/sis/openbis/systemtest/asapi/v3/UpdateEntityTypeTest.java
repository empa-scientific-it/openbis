/*
 * Copyright ETH 2017 - 2023 Zürich, Scientific IT Services
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IEntityType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertiesHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractEntitySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.FieldUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.create.IEntityTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.update.IEntityTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.PluginPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyAssignmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.IPropertyTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyAssignmentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author Franz-Josef Elmer
 */
public abstract class UpdateEntityTypeTest<CREATION extends IEntityTypeCreation, UPDATE extends IEntityTypeUpdate, TYPE extends IEntityType>
        extends AbstractTest
{
    protected abstract EntityKind getEntityKind();

    protected abstract CREATION newTypeCreation();

    protected abstract UPDATE newTypeUpdate();

    protected abstract EntityTypePermId getTypeId();

    protected abstract void createEntity(String sessionToken, IEntityTypeId entityType, String propertyType, String propertyValue);

    protected abstract List<EntityTypePermId> createTypes(String sessionToken, List<CREATION> updates);

    protected abstract void updateTypes(String sessionToken, List<UPDATE> updates);

    protected abstract TYPE getType(String sessionToken, EntityTypePermId typeId);

    protected abstract void updateTypeSpecificFields(UPDATE update, int variant);

    protected abstract void assertTypeSpecificFields(TYPE type, UPDATE update, int variant);

    protected abstract String getValidationPluginOrNull(String sessionToken, EntityTypePermId typeId);

    protected abstract AbstractEntitySearchCriteria<?> createSearchCriteria(EntityTypePermId typeId);

    protected abstract List<? extends IPropertiesHolder> searchEntities(String sessionToken, AbstractEntitySearchCriteria<?> searchCriteria);

    @DataProvider
    public Object[][] providerTestUpdateAuthorizationWithCreateAssignment()
    {
        return new Object[][] {
                { "NEW_NON_INTERNAL", SYSTEM_USER, null },
                { "NEW_NON_INTERNAL", TEST_USER, null },
                { "NEW_NON_INTERNAL", TEST_POWER_USER_CISD,
                        "Access denied to object with EntityTypePermId = [NEW_ENTITY_TYPE (" + getEntityKind() + ")]" },

                { "$NEW_INTERNAL", SYSTEM_USER, null },
                { "$NEW_INTERNAL", TEST_USER, null },
                { "$NEW_INTERNAL", TEST_POWER_USER_CISD,
                        "Access denied to object with EntityTypePermId = [NEW_ENTITY_TYPE (" + getEntityKind() + ")]" }
        };
    }

    @Test(dataProvider = "providerTestUpdateAuthorizationWithCreateAssignment")
    public void testUpdateAuthorizationWithCreateAssignment(String propertyTypeCode, String propertyAssignmentRegistrator, String expectedError)
    {
        String systemSessionToken = v3api.loginAsSystem();
        String registratorSessionToken =
                propertyAssignmentRegistrator.equals(SYSTEM_USER) ? v3api.loginAsSystem() : v3api.login(propertyAssignmentRegistrator, PASSWORD);

        CREATION entityTypeCreation = newTypeCreation();
        entityTypeCreation.setCode("NEW_ENTITY_TYPE");
        List<EntityTypePermId> entityTypeIds = createTypes(systemSessionToken, Arrays.asList(entityTypeCreation));

        PropertyTypeCreation propertyTypeCreation = new PropertyTypeCreation();
        propertyTypeCreation.setCode(propertyTypeCode);
        propertyTypeCreation.setDataType(DataType.VARCHAR);
        propertyTypeCreation.setLabel("Test label");
        propertyTypeCreation.setDescription("Test description");
        propertyTypeCreation.setManagedInternally(propertyTypeCode.startsWith("$"));
        propertyTypeCreation.setMultiValue(false);
        List<PropertyTypePermId> propertyTypeIds = v3api.createPropertyTypes(systemSessionToken, Arrays.asList(propertyTypeCreation));

        PropertyAssignmentCreation propertyAssignmentCreation = new PropertyAssignmentCreation();
        propertyAssignmentCreation.setPropertyTypeId(propertyTypeIds.get(0));

        UPDATE entityTypeUpdate = newTypeUpdate();
        entityTypeUpdate.setTypeId(entityTypeIds.get(0));
        entityTypeUpdate.getPropertyAssignments().add(propertyAssignmentCreation);

        assertExceptionMessage(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    updateTypes(registratorSessionToken, Arrays.asList(entityTypeUpdate));

                    TYPE entityType = getType(systemSessionToken, entityTypeIds.get(0));
                    assertEquals(entityType.getPropertyAssignments().size(), 1);
                }
            }, expectedError);
    }

    @DataProvider
    public Object[][] providerTestUpdateAuthorizationWithUpdateAssignment()
    {
        return new Object[][] {
                { "NEW_NON_INTERNAL", SYSTEM_USER, SYSTEM_USER, false, null },
                { "NEW_NON_INTERNAL", SYSTEM_USER, TEST_USER, false, null },
                { "NEW_NON_INTERNAL", SYSTEM_USER, TEST_POWER_USER_CISD, false,
                        "Access denied to object with EntityTypePermId = [NEW_ENTITY_TYPE (" + getEntityKind() + ")]" },

                { "NEW_NON_INTERNAL", SYSTEM_USER, SYSTEM_USER, true, null },
                { "NEW_NON_INTERNAL", SYSTEM_USER, TEST_USER, true, null },
                { "NEW_NON_INTERNAL", SYSTEM_USER, TEST_POWER_USER_CISD, true,
                        "Access denied to object with EntityTypePermId = [NEW_ENTITY_TYPE (" + getEntityKind() + ")]" },

                { "NEW_NON_INTERNAL", TEST_USER, SYSTEM_USER, false, null },
                { "NEW_NON_INTERNAL", TEST_USER, TEST_USER, false, null },
                { "NEW_NON_INTERNAL", TEST_USER, TEST_POWER_USER_CISD, false,
                        "Access denied to object with EntityTypePermId = [NEW_ENTITY_TYPE (" + getEntityKind() + ")]" },

                { "NEW_NON_INTERNAL", TEST_USER, SYSTEM_USER, true, null },
                { "NEW_NON_INTERNAL", TEST_USER, TEST_USER, true, null },
                { "NEW_NON_INTERNAL", TEST_USER, TEST_POWER_USER_CISD, true,
                        "Access denied to object with EntityTypePermId = [NEW_ENTITY_TYPE (" + getEntityKind() + ")]" },

                { "$NEW_INTERNAL", SYSTEM_USER, SYSTEM_USER, false, null },
                { "$NEW_INTERNAL", SYSTEM_USER, TEST_USER, false,
                        "Property assignments created by the system user for internal property types can be managed only by the system user" },
                { "$NEW_INTERNAL", SYSTEM_USER, TEST_POWER_USER_CISD, false,
                        "Access denied to object with EntityTypePermId = [NEW_ENTITY_TYPE (" + getEntityKind() + ")]" },

                { "$NEW_INTERNAL", SYSTEM_USER, SYSTEM_USER, true, null },
                { "$NEW_INTERNAL", SYSTEM_USER, TEST_USER, true, null },
                { "$NEW_INTERNAL", SYSTEM_USER, TEST_POWER_USER_CISD, true,
                        "Access denied to object with EntityTypePermId = [NEW_ENTITY_TYPE (" + getEntityKind() + ")]" },

                { "$NEW_INTERNAL", TEST_USER, SYSTEM_USER, false, null },
                { "$NEW_INTERNAL", TEST_USER, TEST_USER, false, null },
                { "$NEW_INTERNAL", TEST_USER, TEST_POWER_USER_CISD, false,
                        "Access denied to object with EntityTypePermId = [NEW_ENTITY_TYPE (" + getEntityKind() + ")]" },

                { "$NEW_INTERNAL", TEST_USER, SYSTEM_USER, true, null },
                { "$NEW_INTERNAL", TEST_USER, TEST_USER, true, null },
                { "$NEW_INTERNAL", TEST_USER, TEST_POWER_USER_CISD, true,
                        "Access denied to object with EntityTypePermId = [NEW_ENTITY_TYPE (" + getEntityKind() + ")]" },
        };
    }

    @Test(dataProvider = "providerTestUpdateAuthorizationWithUpdateAssignment")
    public void testUpdateAuthorizationWithUpdateAssignment(String propertyTypeCode, String propertyAssignmentRegistrator,
            String propertyAssignmentUpdater, boolean updateLayoutFieldsOnly, String expectedError)
    {
        String systemSessionToken = v3api.loginAsSystem();
        String registratorSessionToken =
                propertyAssignmentRegistrator.equals(SYSTEM_USER) ? v3api.loginAsSystem() : v3api.login(propertyAssignmentRegistrator, PASSWORD);
        String updaterSessionToken =
                propertyAssignmentUpdater.equals(SYSTEM_USER) ? v3api.loginAsSystem() : v3api.login(propertyAssignmentUpdater, PASSWORD);

        CREATION entityTypeCreation = newTypeCreation();
        entityTypeCreation.setCode("NEW_ENTITY_TYPE");
        List<EntityTypePermId> entityTypeIds = createTypes(systemSessionToken, Arrays.asList(entityTypeCreation));

        PropertyTypeCreation propertyTypeCreation = new PropertyTypeCreation();
        propertyTypeCreation.setCode(propertyTypeCode);
        propertyTypeCreation.setDataType(DataType.VARCHAR);
        propertyTypeCreation.setLabel("Test label");
        propertyTypeCreation.setDescription("Test description");
        propertyTypeCreation.setManagedInternally(propertyTypeCode.startsWith("$"));
        propertyTypeCreation.setMultiValue(false);
        List<PropertyTypePermId> propertyTypeIds = v3api.createPropertyTypes(systemSessionToken, Arrays.asList(propertyTypeCreation));

        PropertyAssignmentCreation propertyAssignmentCreation = new PropertyAssignmentCreation();
        propertyAssignmentCreation.setPropertyTypeId(propertyTypeIds.get(0));
        propertyAssignmentCreation.setSection("Test section");
        propertyAssignmentCreation.setOrdinal(1);
        propertyAssignmentCreation.setMandatory(false);

        UPDATE entityTypeUpdateWithAssignmentCreation = newTypeUpdate();
        entityTypeUpdateWithAssignmentCreation.setTypeId(entityTypeIds.get(0));
        entityTypeUpdateWithAssignmentCreation.getPropertyAssignments().add(propertyAssignmentCreation);
        updateTypes(registratorSessionToken, Arrays.asList(entityTypeUpdateWithAssignmentCreation));

        PropertyAssignmentCreation propertyAssignmentUpdate = new PropertyAssignmentCreation();
        propertyAssignmentUpdate.setPropertyTypeId(propertyTypeIds.get(0));

        if (updateLayoutFieldsOnly)
        {
            propertyAssignmentUpdate.setSection("Updated section");
            propertyAssignmentUpdate.setOrdinal(2);
            propertyAssignmentUpdate.setMandatory(false);
        } else
        {
            propertyAssignmentUpdate.setSection("Test section");
            propertyAssignmentUpdate.setOrdinal(1);
            propertyAssignmentUpdate.setMandatory(true);
        }

        UPDATE entityTypeUpdateWithAssignmentUpdate = newTypeUpdate();
        entityTypeUpdateWithAssignmentUpdate.setTypeId(entityTypeIds.get(0));
        entityTypeUpdateWithAssignmentUpdate.getPropertyAssignments().set(propertyAssignmentUpdate);

        assertExceptionMessage(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    updateTypes(updaterSessionToken, Arrays.asList(entityTypeUpdateWithAssignmentUpdate));

                    TYPE entityType = getType(systemSessionToken, entityTypeIds.get(0));
                    assertEquals(entityType.getPropertyAssignments().size(), 1);

                    PropertyAssignment updatedAssignment = entityType.getPropertyAssignments().get(0);

                    if (updateLayoutFieldsOnly)
                    {
                        assertEquals(updatedAssignment.getSection(), "Updated section");
                        assertEquals(updatedAssignment.getOrdinal(), Integer.valueOf(2));
                        assertEquals(updatedAssignment.isMandatory(), Boolean.valueOf(false));
                    } else
                    {
                        assertEquals(updatedAssignment.getSection(), "Test section");
                        assertEquals(updatedAssignment.getOrdinal(), Integer.valueOf(1));
                        assertEquals(updatedAssignment.isMandatory(), Boolean.valueOf(true));
                    }
                }
            }, expectedError);
    }

    @DataProvider
    public Object[][] providerTestUpdateAuthorizationWithDeleteAssignment()
    {
        return new Object[][] {
                { "NEW_NON_INTERNAL", SYSTEM_USER, SYSTEM_USER, null },
                { "NEW_NON_INTERNAL", SYSTEM_USER, TEST_USER, null },
                { "NEW_NON_INTERNAL", SYSTEM_USER, TEST_POWER_USER_CISD,
                        "Access denied to object with EntityTypePermId = [NEW_ENTITY_TYPE (" + getEntityKind() + ")]" },

                { "NEW_NON_INTERNAL", TEST_USER, SYSTEM_USER, null },
                { "NEW_NON_INTERNAL", TEST_USER, TEST_USER, null },
                { "NEW_NON_INTERNAL", TEST_USER, TEST_POWER_USER_CISD,
                        "Access denied to object with EntityTypePermId = [NEW_ENTITY_TYPE (" + getEntityKind() + ")]" },

                { "$NEW_INTERNAL", SYSTEM_USER, SYSTEM_USER, null },
                { "$NEW_INTERNAL", SYSTEM_USER, TEST_USER,
                        "Property assignments created by the system user for internal property types can be managed only by the system user" },
                { "$NEW_INTERNAL", SYSTEM_USER, TEST_POWER_USER_CISD,
                        "Access denied to object with EntityTypePermId = [NEW_ENTITY_TYPE (" + getEntityKind() + ")]" },

                { "$NEW_INTERNAL", TEST_USER, SYSTEM_USER, null },
                { "$NEW_INTERNAL", TEST_USER, TEST_USER, null },
                { "$NEW_INTERNAL", TEST_USER, TEST_POWER_USER_CISD,
                        "Access denied to object with EntityTypePermId = [NEW_ENTITY_TYPE (" + getEntityKind() + ")]" },
        };
    }

    @Test(dataProvider = "providerTestUpdateAuthorizationWithDeleteAssignment")
    public void testUpdateAuthorizationWithDeleteAssignment(String propertyTypeCode, String propertyAssignmentRegistrator,
            String propertyAssignmentDeleter, String expectedError)
    {
        String systemSessionToken = v3api.loginAsSystem();
        String registratorSessionToken =
                propertyAssignmentRegistrator.equals(SYSTEM_USER) ? v3api.loginAsSystem() : v3api.login(propertyAssignmentRegistrator, PASSWORD);
        String deleterSessionToken =
                propertyAssignmentDeleter.equals(SYSTEM_USER) ? v3api.loginAsSystem() : v3api.login(propertyAssignmentDeleter, PASSWORD);

        CREATION entityTypeCreation = newTypeCreation();
        entityTypeCreation.setCode("NEW_ENTITY_TYPE");
        List<EntityTypePermId> entityTypeIds = createTypes(systemSessionToken, Arrays.asList(entityTypeCreation));

        PropertyTypeCreation propertyTypeCreation = new PropertyTypeCreation();
        propertyTypeCreation.setCode(propertyTypeCode);
        propertyTypeCreation.setDataType(DataType.VARCHAR);
        propertyTypeCreation.setLabel("Test label");
        propertyTypeCreation.setDescription("Test description");
        propertyTypeCreation.setManagedInternally(propertyTypeCode.startsWith("$"));
        propertyTypeCreation.setMultiValue(false);
        List<PropertyTypePermId> propertyTypeIds = v3api.createPropertyTypes(systemSessionToken, Arrays.asList(propertyTypeCreation));

        PropertyAssignmentCreation propertyAssignmentCreation = new PropertyAssignmentCreation();
        propertyAssignmentCreation.setPropertyTypeId(propertyTypeIds.get(0));

        UPDATE entityTypeUpdateWithAssignmentCreation = newTypeUpdate();
        entityTypeUpdateWithAssignmentCreation.setTypeId(entityTypeIds.get(0));
        entityTypeUpdateWithAssignmentCreation.getPropertyAssignments().add(propertyAssignmentCreation);
        updateTypes(registratorSessionToken, Arrays.asList(entityTypeUpdateWithAssignmentCreation));

        UPDATE entityTypeUpdateWithAssignmentDeletion = newTypeUpdate();
        entityTypeUpdateWithAssignmentDeletion.setTypeId(entityTypeIds.get(0));
        entityTypeUpdateWithAssignmentDeletion.getPropertyAssignments().set();

        assertExceptionMessage(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    updateTypes(deleterSessionToken, Arrays.asList(entityTypeUpdateWithAssignmentDeletion));

                    TYPE entityType = getType(systemSessionToken, entityTypeIds.get(0));
                    assertEquals(entityType.getPropertyAssignments().size(), 0);
                }
            }, expectedError);
    }

    @Test
    public void testUpdateWithUnspecifiedId()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        UPDATE update = newTypeUpdate();

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {// When
                    updateTypes(sessionToken, Arrays.asList(update));
                }
            },
                // Then
                "Missing type id.");
    }

    @Test
    public void testUpdateWithUnknownId()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        UPDATE update = newTypeUpdate();

        update.setTypeId(new EntityTypePermId("UNDEFINED", getTypeId().getEntityKind()));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {// When
                    updateTypes(sessionToken, Arrays.asList(update));
                }
            },
                "Object with EntityTypePermId = [" + update.getTypeId() + "] has not been found.");
    }

    @Test
    public void testUpdateWithIdWrongEntityKind()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        UPDATE update = newTypeUpdate();
        EntityTypePermId typeId = getTypeId();
        update.setTypeId(new EntityTypePermId(typeId.getPermId(), nextEntityKind(typeId.getEntityKind())));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    // When
                    updateTypes(sessionToken, Arrays.asList(update));
                }
            },
                // Then
                "Entity kind " + typeId.getEntityKind() + " expected: ");
    }

    @Test
    public void testUpdateDescription()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        UPDATE update = newTypeUpdate();
        EntityTypePermId typeId = getTypeId();
        update.setTypeId(typeId);
        update.setDescription("new description " + System.currentTimeMillis());
        updateTypeSpecificFields(update, 0);

        // When
        updateTypes(sessionToken, Arrays.asList(update));

        // Then
        TYPE type = getType(sessionToken, typeId);
        assertEquals(type.getDescription(), update.getDescription().getValue());
        assertTypeSpecificFields(type, update, 0);
    }

    @Test
    public void testUpdateDescriptionUsingEntityTypePermIdWithoutEntityKind()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        UPDATE update = newTypeUpdate();
        EntityTypePermId typeId = new EntityTypePermId(getTypeId().getPermId());
        update.setTypeId(typeId);
        update.setDescription("new description " + System.currentTimeMillis());
        updateTypeSpecificFields(update, 0);

        // When
        updateTypes(sessionToken, Arrays.asList(update));

        // Then
        TYPE type = getType(sessionToken, typeId);
        assertEquals(type.getDescription(), update.getDescription().getValue());
        assertTypeSpecificFields(type, update, 0);
    }

    @Test
    public void testUpdateWithValidationPlugin()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        UPDATE update = newTypeUpdate();
        EntityTypePermId typeId = getTypeId();
        update.setTypeId(typeId);
        update.setValidationPluginId(new PluginPermId("validateOK"));
        updateTypeSpecificFields(update, 1);

        // When
        updateTypes(sessionToken, Arrays.asList(update));

        // Then
        assertEquals(getValidationPluginOrNull(sessionToken, typeId), "validateOK");
        TYPE type = getType(sessionToken, typeId);
        assertTypeSpecificFields(type, update, 1);
    }

    @Test
    public void testUpdateRemovingValidationPlugin()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        UPDATE update = newTypeUpdate();
        EntityTypePermId typeId = getTypeId();
        update.setTypeId(typeId);
        update.setValidationPluginId(new PluginPermId("validateOK"));
        updateTypeSpecificFields(update, 1);
        updateTypes(sessionToken, Arrays.asList(update));
        assertEquals(getValidationPluginOrNull(sessionToken, typeId), "validateOK");

        update = newTypeUpdate();
        update.setTypeId(typeId);
        update.getValidationPluginId().setValue(null);

        // When
        updateTypes(sessionToken, Arrays.asList(update));

        // Then
        assertEquals(getValidationPluginOrNull(sessionToken, typeId), null);
        TYPE type = getType(sessionToken, typeId);
        assertTypeSpecificFields(type, update, 1);
    }

    @Test
    public void testUpdateWithValidationPluginOfIncorrectType()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        UPDATE update = newTypeUpdate();
        EntityTypePermId typeId = getTypeId();
        update.setTypeId(typeId);
        update.setValidationPluginId(new PluginPermId("properties"));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    // When
                    updateTypes(sessionToken, Arrays.asList(update));
                }
            },
                // Then
                "Entity type validation plugin has to be of type 'Entity Validator'. "
                        + "The specified plugin with id 'properties' is of type 'Dynamic Property Evaluator'");
    }

    @Test
    public void testUpdateWithValidationPluginOfIncorrectEntityType()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        UPDATE update = newTypeUpdate();
        EntityTypePermId typeId = getTypeId();
        update.setTypeId(typeId);
        EntityKind incorrectEntityKind = getIncorrectEntityKind();
        update.setValidationPluginId(new PluginPermId("test" + incorrectEntityKind));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    // When
                    updateTypes(sessionToken, Arrays.asList(update));
                }
            },
                // Then
                "Entity type validation plugin has entity kind set to '" + incorrectEntityKind.name()
                        + "'. Expected a plugin where entity kind is either '" + getEntityKind().name() + "' or null");
    }

    @Test
    public void testUpdateWithValidationPluginOfCorrectEntityType()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        UPDATE update = newTypeUpdate();
        EntityTypePermId typeId = getTypeId();
        update.setTypeId(typeId);
        EntityKind correctEntityKind = getCorrectEntityKind();
        String pluginPermId = null;
        if (correctEntityKind != null)
        {
            pluginPermId = "test" + correctEntityKind;
            update.setValidationPluginId(new PluginPermId(pluginPermId));
        }

        // When
        updateTypes(sessionToken, Arrays.asList(update));

        // Then
        assertEquals(getValidationPluginOrNull(sessionToken, typeId), pluginPermId);
        TYPE type = getType(sessionToken, typeId);
        assertTypeSpecificFields(type, update, 1);
    }

    @Test
    public void testAddAndRemovePropertyTypeAssignment()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        UPDATE update = newTypeUpdate();
        EntityTypePermId typeId = getTypeId();
        update.setTypeId(typeId);
        PropertyAssignmentCreation assignmentCreation = new PropertyAssignmentCreation();
        assignmentCreation.setPropertyTypeId(new PropertyTypePermId("SIZE"));
        assignmentCreation.setMandatory(false);
        assignmentCreation.setSection("test");
        assignmentCreation.setOrdinal(3);
        assignmentCreation.setShowRawValueInForms(false);
        assignmentCreation.setShowInEditView(false);
        update.getPropertyAssignments().setForceRemovingAssignments(true);
        update.getPropertyAssignments().add(assignmentCreation);
        update.getPropertyAssignments().remove(new PropertyAssignmentPermId(typeId, new PropertyTypePermId("description")));
        Map<String, String> renderedAssignments = getCurrentRenderedPropertyAssignmentsByPropertyTypeCode(sessionToken);
        renderedAssignments.remove("DESCRIPTION");
        renderedAssignments.put("SIZE", "PropertyAssignment entity type: " + typeId.getPermId()
                + ", property type: SIZE, mandatory: false, showInEditView: false, showRawValueInForms: false");

        // When
        updateTypes(sessionToken, Arrays.asList(update));

        // Then
        List<String> expected = getSortedRenderedAssignments(renderedAssignments);
        List<String> actual = getSortedRenderedAssignments(sessionToken);
        assertEquals(actual.toString(), expected.toString());
    }

    @Test
    public void testRemovePropertyTypeAssignmentFailsBecauseOfEntitiesWithSuchProperty()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        UPDATE update = newTypeUpdate();
        EntityTypePermId typeId = getTypeId();
        String propertyType = "DESCRIPTION";
        createEntity(sessionToken, typeId, propertyType, "new property");
        update.setTypeId(typeId);
        update.getPropertyAssignments().remove(new PropertyAssignmentPermId(typeId, new PropertyTypePermId(propertyType)));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    // When
                    updateTypes(sessionToken, Arrays.asList(update));
                }
            },
                // Then
                "Can not remove property type " + propertyType + " from type " + typeId.getPermId());
    }

    @DataProvider
    public Object[][] testRemovePropertyTypeAssignmentWithEntitiesWithSuchPropertyAndForceFlagProvider()
    {
        return new Object[][] { { DataType.VARCHAR, "abc" }, { DataType.INTEGER, "123" }, { DataType.DATE, "2023-06-21" },
                { DataType.TIMESTAMP, "2023-06-21 12:07:01" } };
    }

    @Test(dataProvider = "testRemovePropertyTypeAssignmentWithEntitiesWithSuchPropertyAndForceFlagProvider")
    public void testRemovePropertyTypeAssignmentWithEntitiesWithSuchPropertyAndForceFlag(DataType propertyDataType, String propertyValue)
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        EntityTypePermId typeId = getTypeId();
        PropertyTypePermId propertyTypeId = createAPropertyType(sessionToken, propertyDataType);

        PropertyAssignmentCreation assignmentCreation = new PropertyAssignmentCreation();
        assignmentCreation.setPropertyTypeId(propertyTypeId);

        UPDATE updateAddAssignment = newTypeUpdate();
        updateAddAssignment.setTypeId(typeId);
        updateAddAssignment.getPropertyAssignments().add(assignmentCreation);
        updateTypes(sessionToken, List.of(updateAddAssignment));

        createEntity(sessionToken, typeId, propertyTypeId.getPermId(), propertyValue);

        UPDATE updateRemoveAssignment = newTypeUpdate();
        updateRemoveAssignment.setTypeId(typeId);
        updateRemoveAssignment.getPropertyAssignments().remove(new PropertyAssignmentPermId(typeId, propertyTypeId));
        updateRemoveAssignment.getPropertyAssignments().setForceRemovingAssignments(true);
        updateTypes(sessionToken, List.of(updateRemoveAssignment));
    }

    @Test
    public void testAddAlreadyExistingPropertyTypeAssignment()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        UPDATE update = newTypeUpdate();
        EntityTypePermId typeId = getTypeId();
        PropertyType propertyType = getType(sessionToken, typeId).getPropertyAssignments().get(0).getPropertyType();
        String propertyTypePermId = propertyType.getCode();
        update.setTypeId(typeId);
        PropertyAssignmentCreation assignmentCreation = new PropertyAssignmentCreation();
        assignmentCreation.setPropertyTypeId(new PropertyTypePermId(propertyTypePermId));
        update.getPropertyAssignments().add(assignmentCreation);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    // When
                    updateTypes(sessionToken, Arrays.asList(update));
                }
            },
                "Property type '" + propertyTypePermId + "' is already assigned to "
                        + getEntityKind().getLabel() + " type '" + typeId.getPermId() + "'.");
    }

    @Test
    public void testSetPropertyTypeAssignment()
    {
        // Given
        String sessionToken = v3api.loginAsSystem();
        UPDATE update = newTypeUpdate();
        EntityTypePermId typeId = getTypeId();
        update.setTypeId(typeId);
        PropertyAssignmentCreation newCreation = new PropertyAssignmentCreation();
        newCreation.setPropertyTypeId(new PropertyTypePermId("SIZE"));
        newCreation.setInitialValueForExistingEntities("42");
        newCreation.setMandatory(true);
        newCreation.setShowRawValueInForms(true);
        newCreation.setShowInEditView(false);
        newCreation.setSection("test");
        PropertyAssignmentCreation replaceCreation = new PropertyAssignmentCreation();
        replaceCreation.setPropertyTypeId(new PropertyTypePermId("$PLATE_GEOMETRY"));
        replaceCreation.setMandatory(false);
        replaceCreation.setShowInEditView(true);
        update.getPropertyAssignments().set(newCreation, replaceCreation);
        update.getPropertyAssignments().setForceRemovingAssignments(true);
        Map<String, String> renderedAssignments = getCurrentRenderedPropertyAssignmentsByPropertyTypeCode(sessionToken);
        renderedAssignments.remove("DESCRIPTION");
        renderedAssignments.remove("BACTERIUM");
        renderedAssignments.remove("ANY_MATERIAL");
        renderedAssignments.remove("ORGANISM");
        renderedAssignments.remove("DELETION_TEST");
        renderedAssignments.remove("COMMENT");
        renderedAssignments.remove("COMPOUND_HCS");
        renderedAssignments.remove("GENE_SYMBOL");
        renderedAssignments.put("SIZE", "PropertyAssignment entity type: " + typeId.getPermId()
                + ", property type: SIZE, mandatory: true, showInEditView: false, showRawValueInForms: true");
        renderedAssignments.put("$PLATE_GEOMETRY", "PropertyAssignment entity type: " + typeId.getPermId()
                + ", property type: $PLATE_GEOMETRY, mandatory: false, showInEditView: true, showRawValueInForms: false");

        // When
        updateTypes(sessionToken, Arrays.asList(update));

        // Then
        List<String> expected = getSortedRenderedAssignments(renderedAssignments);
        List<String> actual = getSortedRenderedAssignments(sessionToken);
        assertEquals(actual.toString(), expected.toString());
    }

    @Test
    public void testReplacingPropertyTypeWithPropertyTypeIdNull()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        UPDATE update = newTypeUpdate();
        EntityTypePermId typeId = getTypeId();
        update.setTypeId(typeId);
        PropertyAssignmentCreation replaceCreation = new PropertyAssignmentCreation();
        replaceCreation.setMandatory(false);
        update.getPropertyAssignments().set(replaceCreation);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    // When
                    updateTypes(sessionToken, Arrays.asList(update));
                }
            },
                // Then
                "PropertyTypeId cannot be null.");
    }

    @Test
    public void testReplacingPropertyTypeWithUnknownPropertyTypeIdClass()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        UPDATE update = newTypeUpdate();
        EntityTypePermId typeId = getTypeId();
        update.setTypeId(typeId);
        PropertyAssignmentCreation replaceCreation = new PropertyAssignmentCreation();
        replaceCreation.setPropertyTypeId(new IPropertyTypeId()
            {
                private static final long serialVersionUID = 1L;
            });
        update.getPropertyAssignments().set(replaceCreation);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    // When
                    updateTypes(sessionToken, Arrays.asList(update));
                }
            },
                // Then
                "Unknown type of property type id: ch.ethz.sis.openbis.systemtest.asapi.v3.UpdateEntityTypeTest$");
    }

    @Test(dataProvider = "usersNotAllowedToUpdate")
    public void testUpdateWithUserCausingAuthorizationFailure(final String user)
    {
        EntityTypePermId typeId = getTypeId();
        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    // Given
                    String sessionToken = v3api.login(user, PASSWORD);
                    UPDATE update = newTypeUpdate();
                    update.setTypeId(typeId);
                    update.setDescription("new description " + System.currentTimeMillis());

                    // When
                    updateTypes(sessionToken, Arrays.asList(update));
                }
            }, typeId, patternContains("checking access"));
    }

    @DataProvider
    Object[][] usersNotAllowedToUpdate()
    {
        return createTestUsersProvider(TEST_GROUP_ADMIN, TEST_GROUP_OBSERVER, TEST_GROUP_POWERUSER,
                TEST_INSTANCE_OBSERVER, TEST_OBSERVER_CISD, TEST_POWER_USER_CISD, TEST_SPACE_USER);
    }

    protected <T> T getNewValue(FieldUpdateValue<T> fieldUpdateValue, T currentValue)
    {
        return fieldUpdateValue != null && fieldUpdateValue.isModified() ? fieldUpdateValue.getValue() : currentValue;
    }

    private EntityKind getIncorrectEntityKind()
    {
        if (EntityKind.EXPERIMENT.equals(getEntityKind()))
        {
            return EntityKind.SAMPLE;
        } else
        {
            return EntityKind.EXPERIMENT;
        }
    }

    private EntityKind getCorrectEntityKind()
    {
        if (EntityKind.EXPERIMENT.equals(getEntityKind()))
        {
            return EntityKind.EXPERIMENT;
        } else if (EntityKind.SAMPLE.equals(getEntityKind()))
        {
            return EntityKind.SAMPLE;
        } else
        {
            return null;
        }
    }

    private List<String> getSortedRenderedAssignments(String sessionToken)
    {
        return getSortedRenderedAssignments(getCurrentRenderedPropertyAssignmentsByPropertyTypeCode(sessionToken));
    }

    private List<String> getSortedRenderedAssignments(Map<String, String> currentRenderedPropertyAssignments)
    {
        List<String> renderedAssignments = new ArrayList<String>(currentRenderedPropertyAssignments.values());
        Collections.sort(renderedAssignments);
        return renderedAssignments;
    }

    private Map<String, String> getCurrentRenderedPropertyAssignmentsByPropertyTypeCode(String sessionToken)
    {
        Map<String, String> result = new HashMap<String, String>();
        TYPE type = getType(sessionToken, getTypeId());
        List<PropertyAssignment> assignments = type.getPropertyAssignments();
        for (PropertyAssignment propertyAssignment : assignments)
        {
            PropertyType propertyType = propertyAssignment.getPropertyType();
            String code = propertyType.getCode();
            StringBuilder builder = new StringBuilder(propertyAssignment.toString());
            builder.append(", showInEditView: ").append(propertyAssignment.isShowInEditView());
            builder.append(", showRawValueInForms: ").append(propertyAssignment.isShowRawValueInForms());
            result.put(code, builder.toString());
        }
        return result;
    }

    private ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind nextEntityKind(
            ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind entityKind)
    {
        ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind[] values =
                ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind.values();
        return values[(entityKind.ordinal() + 1) % values.length];
    }

}

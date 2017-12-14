/*
 * Copyright 2017 ETH Zuerich, SIS
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IEntityType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertiesHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractEntitySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.update.IEntityTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.PluginPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyAssignmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyAssignmentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.collection.SimpleComparator;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author Franz-Josef Elmer
 */
public abstract class UpdateEntityTypeTest<UPDATE extends IEntityTypeUpdate, TYPE extends IEntityType> extends AbstractTest
{
    public static final Comparator<PropertyAssignment> ASSIGNMENT_COMPARATOR = new SimpleComparator<PropertyAssignment, String>()
        {
            @Override
            public String evaluate(PropertyAssignment item)
            {
                return item.getPermId().toString();
            }
        };

    protected abstract EntityKind getEntityKind();

    protected abstract UPDATE newTypeUpdate();

    protected abstract EntityTypePermId getTypeId();

    protected abstract void updateTypes(String sessionToken, List<UPDATE> updates);

    protected abstract TYPE getType(String sessionToken, EntityTypePermId typeId);

    protected abstract String getValidationPluginOrNull(String sessionToken, EntityTypePermId typeId);

    protected abstract AbstractEntitySearchCriteria<?> createSearchCriteria(EntityTypePermId typeId);
    
    protected abstract List<? extends IPropertiesHolder> searchEntities(String sessionToken, AbstractEntitySearchCriteria<?> searchCriteria);
    
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
    public void testUpdateWithIdButUnspecifiedEntityKind()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        UPDATE update = newTypeUpdate();
        update.setTypeId(new EntityTypePermId(getTypeId().getPermId()));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {// When
                    updateTypes(sessionToken, Arrays.asList(update));
                }
            },
                // Then
                "Unspecified entity kind in type id: " + update.getTypeId());
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
        
        // When
        updateTypes(sessionToken, Arrays.asList(update));
        
        // Then
        assertEquals(getType(sessionToken, typeId).getDescription(), update.getDescription().getValue());
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
        
        // When
        updateTypes(sessionToken, Arrays.asList(update));
        
        // Then
        assertEquals(getValidationPluginOrNull(sessionToken, typeId), "validateOK");
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
    public void testAddAndRemovePropertyTypeAssignment()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        UPDATE update = newTypeUpdate();
        EntityTypePermId typeId = getTypeId();
        update.setTypeId(typeId);
        PropertyAssignmentCreation assignmentCreation = new PropertyAssignmentCreation();
        assignmentCreation.setPropertyTypeId(new PropertyTypePermId("SIZE"));
        assignmentCreation.setMandatory(true);
        assignmentCreation.setInitialValueForExistingEntities("42");
        assignmentCreation.setSection("test");
        assignmentCreation.setOrdinal(3);
        update.getPropertyAssignments().add(assignmentCreation);
        update.getPropertyAssignments().remove(new PropertyAssignmentPermId(typeId, new PropertyTypePermId("description")));
        Map<String, String> renderedAssignments = getCurrentRenderedPropertyAssignmentsByPropertyTypeCode(sessionToken);
        renderedAssignments.remove("DESCRIPTION");
        renderedAssignments.put("SIZE", "PropertyAssignment entity type: " + typeId.getPermId() + ", property type: SIZE, mandatory: true");

        // When
        updateTypes(sessionToken, Arrays.asList(update));

        // Then
        List<String> expected = getSortedRenderedAssignments(renderedAssignments);
        List<String> actual = getSortedRenderedAssignments(sessionToken);
        assertEquals(actual.toString(), expected.toString());
    }
    
    @Test
    public void testAddAlreadyExistingPropertyTypeAssignment()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        UPDATE update = newTypeUpdate();
        EntityTypePermId typeId = getTypeId();
        PropertyType propertyType = getType(sessionToken, typeId).getPropertyAssignments().get(0).getPropertyType();
        String prefix = propertyType.isInternalNameSpace() ? "$" : "";
        String propertyTypePermId = prefix + propertyType.getCode();
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
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        UPDATE update = newTypeUpdate();
        EntityTypePermId typeId = getTypeId();
        update.setTypeId(typeId);
        PropertyAssignmentCreation newCreation = new PropertyAssignmentCreation();
        newCreation.setPropertyTypeId(new PropertyTypePermId("SIZE"));
        newCreation.setInitialValueForExistingEntities("42");
        newCreation.setMandatory(true);
        newCreation.setSection("test");
        PropertyAssignmentCreation replaceCreation = new PropertyAssignmentCreation();
        replaceCreation.setPropertyTypeId(new PropertyTypePermId("$PLATE_GEOMETRY"));
        replaceCreation.setMandatory(false);
        update.getPropertyAssignments().set(newCreation, replaceCreation);
        
        // When
        updateTypes(sessionToken, Arrays.asList(update));
        
        // Then
        TYPE type = getType(sessionToken, typeId);
        List<PropertyAssignment> assignments = type.getPropertyAssignments();
        Collections.sort(assignments, ASSIGNMENT_COMPARATOR);
        assertEquals(assignments.toString(),
                "[PropertyAssignment entity type: " + typeId.getPermId() + ", property type: PLATE_GEOMETRY, mandatory: false, "
                + "PropertyAssignment entity type: " + typeId.getPermId() + ", property type: SIZE, mandatory: true]");
        PropertyAssignment assignment = assignments.get(1);
        assertEquals(assignment.getSection(), "test");
        AbstractEntitySearchCriteria<?> searchCriteria = createSearchCriteria(typeId);
        searchCriteria.withProperty("SIZE");
        for (IPropertiesHolder propertiesHolder : searchEntities(sessionToken, searchCriteria))
        {
            assertEquals(propertiesHolder.getProperty("SIZE"), "42");
        }
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
            result.put(code, propertyAssignment.toString());
        }
        return result;
    }
    
}

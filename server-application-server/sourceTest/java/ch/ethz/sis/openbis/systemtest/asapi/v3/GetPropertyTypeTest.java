/*
 * Copyright 2018 ETH Zuerich, SIS
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

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;

/**
 * @author Franz-Josef Elmer
 */
public class GetPropertyTypeTest extends AbstractTest
{
    @Test
    public void testGetSimplePropertyTypeWithBasics()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId permId = new PropertyTypePermId("Description");
        PropertyTypeFetchOptions fetchOptions = new PropertyTypeFetchOptions();

        // When
        PropertyType propertyType = v3api.getPropertyTypes(sessionToken, Arrays.asList(permId), fetchOptions).get(permId);

        // Then
        assertEquals(propertyType.getCode(), "DESCRIPTION");
        assertEquals(propertyType.getPermId(), permId);
        assertEquals(propertyType.getDescription(), "A Description");
        assertEquals(propertyType.getDataType().toString(), DataType.VARCHAR.toString());
        assertEquals(propertyType.getLabel(), "Description");
        assertEquals(propertyType.isManagedInternally().booleanValue(), false);
        assertEquals(propertyType.getSchema(), null);
        assertEquals(propertyType.getTransformation(), null);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetManagedInternallyPropertyTypeWithAll()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId permId = new PropertyTypePermId("$PLATE_GEOMETRY");
        PropertyTypeFetchOptions fetchOptions = new PropertyTypeFetchOptions();
        fetchOptions.withMaterialType();
        fetchOptions.withVocabulary().withTerms().sortBy().code().desc();
        ;

        // When
        PropertyType propertyType = v3api.getPropertyTypes(sessionToken, Arrays.asList(permId), fetchOptions).get(permId);

        // Then
        assertEquals(propertyType.getCode(), "$PLATE_GEOMETRY");
        assertEquals(propertyType.getPermId(), permId);
        assertEquals(propertyType.getDescription(), "Plate Geometry");
        assertEquals(propertyType.getDataType().toString(), DataType.CONTROLLEDVOCABULARY.toString());
        assertEquals(propertyType.getLabel(), "Plate Geometry");
        assertEquals(propertyType.isManagedInternally().booleanValue(), true);
        assertEquals(propertyType.getSchema(), null);
        assertEquals(propertyType.getTransformation(), null);
        assertEquals(propertyType.getMaterialType(), null);
        assertEquals(propertyType.getVocabulary().getTerms().toString(),
                "[VocabularyTerm 1536_WELLS_32X48, VocabularyTerm 384_WELLS_16X24, VocabularyTerm 96_WELLS_8X12]");

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetPropertyTypeWithMaterialType()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId permId = new PropertyTypePermId("BACTERIUM");
        PropertyTypeFetchOptions fetchOptions = new PropertyTypeFetchOptions();
        fetchOptions.withMaterialType();
        fetchOptions.withVocabulary().withTerms().sortBy().code().desc();
        ;

        // When
        PropertyType propertyType = v3api.getPropertyTypes(sessionToken, Arrays.asList(permId), fetchOptions).get(permId);

        // Then
        assertEquals(propertyType.getCode(), "BACTERIUM");
        assertEquals(propertyType.getPermId(), permId);
        assertEquals(propertyType.getDataType().toString(), DataType.MATERIAL.toString());
        assertEquals(propertyType.getVocabulary(), null);
        assertEquals(propertyType.getMaterialType().getCode(), "BACTERIUM");
        assertEquals(propertyType.getMaterialType().getDescription(), "Bacterium");
        assertEquals(propertyType.getMaterialType().getPermId().toString(), "BACTERIUM (MATERIAL)");

        v3api.logout(sessionToken);
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PropertyTypeFetchOptions fo = new PropertyTypeFetchOptions();
        fo.withMaterialType();
        fo.withVocabulary();

        v3api.getPropertyTypes(sessionToken, Arrays.asList(new PropertyTypePermId("DESCRIPTION"), new PropertyTypePermId("$PLATE_GEOMETRY")), fo);

        assertAccessLog(
                "get-property-types  PROPERTY_TYPE_IDS('[DESCRIPTION, $PLATE_GEOMETRY]') FETCH_OPTIONS('PropertyType\n    with Vocabulary\n    with MaterialType\n')");
    }

}

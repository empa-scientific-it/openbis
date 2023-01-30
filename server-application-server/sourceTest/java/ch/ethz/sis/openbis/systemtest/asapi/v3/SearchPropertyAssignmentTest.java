/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.search.EntityTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyAssignmentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search.PropertyAssignmentSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;

/**
 * @author pkupczyk
 */
public class SearchPropertyAssignmentTest extends AbstractTest
{

    @Test
    public void testSearchWithEmptyCriteria()
    {
        String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        SearchResult<PropertyAssignment> searchResult =
                v3api.searchPropertyAssignments(sessionToken, new PropertyAssignmentSearchCriteria(), new PropertyAssignmentFetchOptions());
        List<PropertyAssignment> propertyAssignments = searchResult.getObjects();

        List<EntityTypePropertyType<?>> etpts = commonServer.listEntityTypePropertyTypes(sessionToken);

        Assert.assertEquals(propertyAssignments.size(), etpts.size());
    }

    @Test
    public void testSearchWithId()
    {
        PropertyAssignmentSearchCriteria criteria = new PropertyAssignmentSearchCriteria();
        criteria.withId()
                .thatEquals(new PropertyAssignmentPermId(new EntityTypePermId("CELL_PLATE", EntityKind.SAMPLE), new PropertyTypePermId("SIZE")));
        testSearch(TEST_USER, criteria, "CELL_PLATE.SIZE");
    }

    @Test
    public void testSearchWithIds()
    {
        PropertyAssignmentSearchCriteria criteria = new PropertyAssignmentSearchCriteria();
        criteria.withIds().thatIn(Arrays.asList(
                new PropertyAssignmentPermId(new EntityTypePermId("CELL_PLATE", EntityKind.SAMPLE), new PropertyTypePermId("SIZE")),
                new PropertyAssignmentPermId(new EntityTypePermId("COMPOUND_HCS", EntityKind.EXPERIMENT), new PropertyTypePermId("ANY_MATERIAL")),
                new PropertyAssignmentPermId(new EntityTypePermId("HCS_IMAGE", EntityKind.DATA_SET), new PropertyTypePermId("COMMENT")),
                new PropertyAssignmentPermId(new EntityTypePermId("SIRNA", EntityKind.MATERIAL), new PropertyTypePermId("OFFSET"))));
        testSearch(TEST_USER, criteria, "CELL_PLATE.SIZE", "COMPOUND_HCS.ANY_MATERIAL", "HCS_IMAGE.COMMENT", "SIRNA.OFFSET");
    }

    @Test
    public void testSearchWithIdNonexistent()
    {
        PropertyAssignmentSearchCriteria criteria = new PropertyAssignmentSearchCriteria();
        criteria.withId().thatEquals(
                new PropertyAssignmentPermId(new EntityTypePermId("IDONTEXIST", EntityKind.SAMPLE), new PropertyTypePermId("IDONTEXIST")));
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithIdsNonexistent()
    {
        PropertyAssignmentSearchCriteria criteria = new PropertyAssignmentSearchCriteria();
        criteria.withIds().thatIn(Arrays.asList(
                new PropertyAssignmentPermId(new EntityTypePermId("CELL_PLATE", EntityKind.SAMPLE), new PropertyTypePermId("SIZE")),
                new PropertyAssignmentPermId(new EntityTypePermId("IDONTEXIST", EntityKind.SAMPLE), new PropertyTypePermId("IDONTEXIST"))));
        testSearch(TEST_USER, criteria, "CELL_PLATE.SIZE");
    }

    @Test
    public void testSearchWithEntityTypeWithId()
    {
        PropertyAssignmentSearchCriteria criteria = new PropertyAssignmentSearchCriteria();
        criteria.withEntityType().withId().thatEquals(new EntityTypePermId("CELL_PLATE", EntityKind.SAMPLE));
        testSearch(TEST_USER, criteria, "CELL_PLATE.SIZE", "CELL_PLATE.COMMENT", "CELL_PLATE.ORGANISM", "CELL_PLATE.BACTERIUM",
                "CELL_PLATE.ANY_MATERIAL");
    }

    @Test
    public void testSearchWithPropertyTypeWithId()
    {
        PropertyAssignmentSearchCriteria criteria = new PropertyAssignmentSearchCriteria();
        criteria.withPropertyType().withId().thatEquals(new PropertyTypePermId("COMMENT"));
        testSearch(TEST_USER, criteria, "CELL_PLATE.COMMENT", "DYNAMIC_PLATE.COMMENT", "VALIDATE_CHILDREN.COMMENT", "COMPOUND_HCS.COMMENT",
                "HCS_IMAGE.COMMENT");
    }

    @Test
    public void testSearchWithPropertyTypeWithInternalNamespace()
    {
        PropertyAssignmentSearchCriteria criteria = new PropertyAssignmentSearchCriteria();
        criteria.withPropertyType().withId().thatEquals(new PropertyTypePermId("$PLATE_GEOMETRY"));
        testSearch(TEST_USER, criteria, "MASTER_PLATE.$PLATE_GEOMETRY", "CONTROL_LAYOUT.$PLATE_GEOMETRY");
    }

    @Test
    public void testSearchWithSemanticAnnotations()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PropertyAssignmentSearchCriteria criteria = new PropertyAssignmentSearchCriteria();
        criteria.withSemanticAnnotations();

        PropertyAssignmentFetchOptions fo = new PropertyAssignmentFetchOptions();
        fo.withEntityType();
        fo.withPropertyType();
        fo.withSemanticAnnotations();

        SearchResult<PropertyAssignment> searchResult =
                v3api.searchPropertyAssignments(sessionToken, criteria, fo);

        List<PropertyAssignment> withOwnSemanticAnnotations = new ArrayList<PropertyAssignment>();
        List<PropertyAssignment> withInheritedSemanticAnnotations = new ArrayList<PropertyAssignment>();

        for (PropertyAssignment propertyAssignment : searchResult.getObjects())
        {
            if (propertyAssignment.isSemanticAnnotationsInherited())
            {
                withInheritedSemanticAnnotations.add(propertyAssignment);
            } else
            {
                withOwnSemanticAnnotations.add(propertyAssignment);
            }
        }

        assertPropertyAssignments(withOwnSemanticAnnotations, null, "MASTER_PLATE.$PLATE_GEOMETRY", "CONTROL_LAYOUT.$PLATE_GEOMETRY",
                "CELL_PLATE.ORGANISM");
        assertPropertyAssignments(withInheritedSemanticAnnotations, null, "MASTER_PLATE.DESCRIPTION", "CONTROL_LAYOUT.DESCRIPTION", "NORMAL.ORGANISM",
                "DELETION_TEST.ORGANISM", "DELETION_TEST.DESCRIPTION");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithAndOperator()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PropertyAssignmentFetchOptions fo = new PropertyAssignmentFetchOptions();
        fo.withEntityType();
        fo.withPropertyType();

        PropertyAssignmentSearchCriteria criteria = new PropertyAssignmentSearchCriteria();
        criteria.withAndOperator();
        EntityTypeSearchCriteria entityTypeSearchCriteria = criteria.withEntityType();
        entityTypeSearchCriteria.withOrOperator();
        entityTypeSearchCriteria.withCode().thatStartsWith("VALI");
        entityTypeSearchCriteria.withCode().thatEndsWith("PLATE");
        criteria.withPropertyType().withId().thatEquals(new PropertyTypePermId("COMMENT"));

        SearchResult<PropertyAssignment> searchResult =
                v3api.searchPropertyAssignments(sessionToken, criteria, fo);

        List<PropertyAssignment> propertyAssignments = searchResult.getObjects();

        for (PropertyAssignment propertyAssignment : propertyAssignments)
        {
            Assert.assertTrue((propertyAssignment.getEntityType().getCode().startsWith("VALI")
                    || propertyAssignment.getEntityType().getCode().endsWith("PLATE"))
                    && propertyAssignment.getPropertyType().getCode().equals("COMMENT"));
        }
        assertEquals(propertyAssignments.size(), 3);
    }

    @Test
    public void testSearchWithOrOperator()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PropertyAssignmentFetchOptions fo = new PropertyAssignmentFetchOptions();
        fo.withEntityType();
        fo.withPropertyType();

        PropertyAssignmentSearchCriteria criteria = new PropertyAssignmentSearchCriteria();
        criteria.withOrOperator();
        EntityTypeSearchCriteria entityTypeSearchCriteria = criteria.withEntityType();
        entityTypeSearchCriteria.withAndOperator();
        entityTypeSearchCriteria.withCode().thatStartsWith("CELL");
        entityTypeSearchCriteria.withCode().thatEndsWith("PLATE");
        criteria.withPropertyType().withId().thatEquals(new PropertyTypePermId("BACTERIUM"));


        SearchResult<PropertyAssignment> searchResult =
                v3api.searchPropertyAssignments(sessionToken, criteria, fo);

        List<PropertyAssignment> propertyAssignments = searchResult.getObjects();

        for (PropertyAssignment propertyAssignment : propertyAssignments)
        {
            Assert.assertTrue(propertyAssignment.getEntityType().getCode().equals("CELL_PLATE")
                    || propertyAssignment.getPropertyType().getCode().equals("BACTERIUM"));
        }
        assertEquals(propertyAssignments.size() >= 11, true);
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PropertyAssignmentSearchCriteria c = new PropertyAssignmentSearchCriteria();
        c.withId().thatEquals(new PropertyAssignmentPermId(new EntityTypePermId("CELL_PLATE", EntityKind.SAMPLE), new PropertyTypePermId("SIZE")));

        PropertyAssignmentFetchOptions fo = new PropertyAssignmentFetchOptions();
        fo.withEntityType();
        fo.withPropertyType();

        v3api.searchPropertyAssignments(sessionToken, c, fo);

        assertAccessLog(
                "search-property-assignments  SEARCH_CRITERIA:\n'PROPERTY_ASSIGNMENT\n    with id 'CELL_PLATE (SAMPLE), SIZE'\n'\nFETCH_OPTIONS:\n'PropertyAssignment\n    with EntityType\n    with PropertyType\n'");
    }

    private void testSearch(String user, PropertyAssignmentSearchCriteria criteria, String... expectedEntityTypeAndPropertyTypeCodes)
    {
        String sessionToken = v3api.login(user, PASSWORD);

        PropertyAssignmentFetchOptions fo = new PropertyAssignmentFetchOptions();
        fo.withEntityType();
        fo.withPropertyType();
        fo.withPlugin();

        SearchResult<PropertyAssignment> searchResult =
                v3api.searchPropertyAssignments(sessionToken, criteria, fo);
        List<PropertyAssignment> propertyAssignments = searchResult.getObjects();

        assertPropertyAssignments(propertyAssignments, null, expectedEntityTypeAndPropertyTypeCodes);
        v3api.logout(sessionToken);
    }

}

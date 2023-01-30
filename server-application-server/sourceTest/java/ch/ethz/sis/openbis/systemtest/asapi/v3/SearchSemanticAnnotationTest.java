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

import static ch.systemsx.cisd.common.test.AssertionUtil.assertCollectionContainsOnly;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyAssignmentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.SemanticAnnotation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.fetchoptions.SemanticAnnotationFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.id.SemanticAnnotationPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.search.SemanticAnnotationSearchCriteria;

/**
 * @author pkupczyk
 */
public class SearchSemanticAnnotationTest extends AbstractTest
{

    @Test
    public void testSearchWithEmptyCriteria()
    {
        testSearch(TEST_USER, new SemanticAnnotationSearchCriteria(), "ST_MASTER_PLATE", "ST_DILUTION_PLATE", "ST_MASTER_PLATE_PT_PLATE_GEOMETRY",
                "ST_CONTROL_LAYOUT_PT_PLATE_GEOMETRY", "PT_DESCRIPTION", "PT_GENE_SYMBOL", "ST_CELL_PLATE_PT_ORGANISM", "PT_ORGANISM");
    }

    @Test
    public void testSearchWithIdThatEquals()
    {
        SemanticAnnotationSearchCriteria criteria = new SemanticAnnotationSearchCriteria();
        criteria.withId().thatEquals(new SemanticAnnotationPermId("ST_DILUTION_PLATE"));
        testSearch(TEST_USER, criteria, "ST_DILUTION_PLATE");
    }

    @Test
    public void testSearchWithIdNonexistent()
    {
        SemanticAnnotationSearchCriteria criteria = new SemanticAnnotationSearchCriteria();
        criteria.withId().thatEquals(new SemanticAnnotationPermId("IDONTEXIST"));
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithPermIdThatEquals()
    {
        SemanticAnnotationSearchCriteria criteria = new SemanticAnnotationSearchCriteria();
        criteria.withPermId().thatEquals("ST_DILUTION_PLATE");
        testSearch(TEST_USER, criteria, "ST_DILUTION_PLATE");
    }

    @Test
    public void testSearchWithPredicateOntologyIdThatEquals()
    {
        SemanticAnnotationSearchCriteria criteria = new SemanticAnnotationSearchCriteria();
        criteria.withPredicateOntologyId().thatEquals("testPredicateOntologyId3");
        testSearch(TEST_USER, criteria, "ST_MASTER_PLATE_PT_PLATE_GEOMETRY");
    }

    @Test
    public void testSearchWithPredicateOntologyVersionThatEquals()
    {
        SemanticAnnotationSearchCriteria criteria = new SemanticAnnotationSearchCriteria();
        criteria.withPredicateOntologyVersion().thatEquals("testPredicateOntologyVersion4");
        testSearch(TEST_USER, criteria, "ST_CONTROL_LAYOUT_PT_PLATE_GEOMETRY");
    }

    @Test
    public void testSearchWithPredicateAccessionIdThatEquals()
    {
        SemanticAnnotationSearchCriteria criteria = new SemanticAnnotationSearchCriteria();
        criteria.withPredicateAccessionId().thatEquals("testPredicateAccessionId5");
        testSearch(TEST_USER, criteria, "PT_DESCRIPTION");
    }

    @Test
    public void testSearchWithDescriptorOntologyIdThatEquals()
    {
        SemanticAnnotationSearchCriteria criteria = new SemanticAnnotationSearchCriteria();
        criteria.withDescriptorOntologyId().thatEquals("testDescriptorOntologyId3");
        testSearch(TEST_USER, criteria, "ST_MASTER_PLATE_PT_PLATE_GEOMETRY");
    }

    @Test
    public void testSearchWithDescriptorOntologyVersionThatEquals()
    {
        SemanticAnnotationSearchCriteria criteria = new SemanticAnnotationSearchCriteria();
        criteria.withDescriptorOntologyVersion().thatEquals("testDescriptorOntologyVersion4");
        testSearch(TEST_USER, criteria, "ST_CONTROL_LAYOUT_PT_PLATE_GEOMETRY");
    }

    @Test
    public void testSearchWithDescriptorAccessionIdThatEquals()
    {
        SemanticAnnotationSearchCriteria criteria = new SemanticAnnotationSearchCriteria();
        criteria.withDescriptorAccessionId().thatEquals("testDescriptorAccessionId5");
        testSearch(TEST_USER, criteria, "PT_DESCRIPTION");
    }

    @Test
    public void testSearchWithEntityType()
    {
        SemanticAnnotationSearchCriteria criteria = new SemanticAnnotationSearchCriteria();
        criteria.withEntityType();
        testSearch(TEST_USER, criteria, "ST_MASTER_PLATE", "ST_DILUTION_PLATE");
    }

    @Test
    public void testSearchWithEntityTypeWithIdThatEquals()
    {
        SemanticAnnotationSearchCriteria criteria = new SemanticAnnotationSearchCriteria();
        criteria.withEntityType().withId().thatEquals(new EntityTypePermId("DILUTION_PLATE", EntityKind.SAMPLE));
        testSearch(TEST_USER, criteria, "ST_DILUTION_PLATE");
    }

    @Test
    public void testSearchWithEntityTypeWithCodeThatEquals()
    {
        SemanticAnnotationSearchCriteria criteria = new SemanticAnnotationSearchCriteria();
        criteria.withEntityType().withCode().thatEquals("MASTER_PLATE");
        testSearch(TEST_USER, criteria, "ST_MASTER_PLATE");
    }

    @Test
    public void testSearchWithEntityTypeWithKindThatEquals()
    {
        SemanticAnnotationSearchCriteria criteria = new SemanticAnnotationSearchCriteria();
        criteria.withEntityType().withKind().thatEquals(EntityKind.SAMPLE);
        testSearch(TEST_USER, criteria, "ST_MASTER_PLATE", "ST_DILUTION_PLATE");
        criteria.withEntityType().withKind().thatEquals(EntityKind.EXPERIMENT);
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithEntityTypeWithCodeThatEqualsAndWithKindThatEquals()
    {
        SemanticAnnotationSearchCriteria criteria = new SemanticAnnotationSearchCriteria();
        criteria.withEntityType().withCode().thatEquals("MASTER_PLATE");
        criteria.withEntityType().withKind().thatEquals(EntityKind.SAMPLE);
        testSearch(TEST_USER, criteria, "ST_MASTER_PLATE");
    }

    @Test
    public void testSearchWithPropertyType()
    {
        SemanticAnnotationSearchCriteria criteria = new SemanticAnnotationSearchCriteria();
        criteria.withPropertyType();
        testSearch(TEST_USER, criteria, "PT_DESCRIPTION", "PT_GENE_SYMBOL", "PT_ORGANISM");
    }

    @Test
    public void testSearchWithPropertyTypeWithIdThatEquals()
    {
        SemanticAnnotationSearchCriteria criteria = new SemanticAnnotationSearchCriteria();
        criteria.withPropertyType().withId().thatEquals(new PropertyTypePermId("GENE_SYMBOL"));
        testSearch(TEST_USER, criteria, "PT_GENE_SYMBOL");
    }

    @Test
    public void testSearchWithPropertyTypeWithCodeThatEquals()
    {
        SemanticAnnotationSearchCriteria criteria = new SemanticAnnotationSearchCriteria();
        criteria.withPropertyType().withCode().thatEquals("DESCRIPTION");
        testSearch(TEST_USER, criteria, "PT_DESCRIPTION");
    }

    @Test
    public void testSearchWithPropertyAssignment()
    {
        SemanticAnnotationSearchCriteria criteria = new SemanticAnnotationSearchCriteria();
        criteria.withPropertyAssignment();
        testSearch(TEST_USER, criteria, "ST_MASTER_PLATE_PT_PLATE_GEOMETRY", "ST_CONTROL_LAYOUT_PT_PLATE_GEOMETRY", "ST_CELL_PLATE_PT_ORGANISM");
    }

    @Test
    public void testSearchWithPropertyAssignmentWithIdThatEquals()
    {
        SemanticAnnotationSearchCriteria criteria = new SemanticAnnotationSearchCriteria();
        criteria.withPropertyAssignment().withId()
                .thatEquals(new PropertyAssignmentPermId(new EntityTypePermId("CONTROL_LAYOUT", EntityKind.SAMPLE),
                        new PropertyTypePermId("$PLATE_GEOMETRY")));
        testSearch(TEST_USER, criteria, "ST_CONTROL_LAYOUT_PT_PLATE_GEOMETRY");
    }

    @Test
    public void testSearchWithPropertyAssignmentWithEntityTypeWithIdThatEquals()
    {
        SemanticAnnotationSearchCriteria criteria = new SemanticAnnotationSearchCriteria();
        criteria.withPropertyAssignment().withEntityType().withId().thatEquals(new EntityTypePermId("CONTROL_LAYOUT", EntityKind.SAMPLE));
        testSearch(TEST_USER, criteria, "ST_CONTROL_LAYOUT_PT_PLATE_GEOMETRY");
    }

    @Test
    public void testSearchWithPropertyAssignmentWithPropertyTypeWithIdThatEquals()
    {
        SemanticAnnotationSearchCriteria criteria = new SemanticAnnotationSearchCriteria();
        criteria.withPropertyAssignment().withPropertyType().withId().thatEquals(new PropertyTypePermId("$PLATE_GEOMETRY"));
        testSearch(TEST_USER, criteria, "ST_MASTER_PLATE_PT_PLATE_GEOMETRY", "ST_CONTROL_LAYOUT_PT_PLATE_GEOMETRY");
    }

    @Test
    public void testSearchWithPropertyAssignmentWithEntityTypeWithIdThatEqualsAndWithPropertyTypeWithIdThatEquals()
    {
        SemanticAnnotationSearchCriteria criteria = new SemanticAnnotationSearchCriteria();
        criteria.withPropertyAssignment().withEntityType().withId().thatEquals(new EntityTypePermId("CONTROL_LAYOUT", EntityKind.SAMPLE));
        criteria.withPropertyAssignment().withPropertyType().withId().thatEquals(new PropertyTypePermId("$PLATE_GEOMETRY"));
        testSearch(TEST_USER, criteria, "ST_CONTROL_LAYOUT_PT_PLATE_GEOMETRY");
    }

    @Test
    public void testSearchWithAndOperator()
    {
        SemanticAnnotationSearchCriteria criteria = new SemanticAnnotationSearchCriteria();
        criteria.withAndOperator();
        criteria.withPermId().thatContains("PLATE");
        criteria.withPermId().thatContains("GEOMETRY");
        testSearch(TEST_USER, criteria, "ST_MASTER_PLATE_PT_PLATE_GEOMETRY", "ST_CONTROL_LAYOUT_PT_PLATE_GEOMETRY");
    }

    @Test
    public void testSearchWithOrOperator()
    {
        SemanticAnnotationSearchCriteria criteria = new SemanticAnnotationSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatContains("PLATE");
        criteria.withPermId().thatContains("GEOMETRY");
        testSearch(TEST_USER, criteria, "ST_MASTER_PLATE", "ST_DILUTION_PLATE", "ST_MASTER_PLATE_PT_PLATE_GEOMETRY",
                "ST_CONTROL_LAYOUT_PT_PLATE_GEOMETRY", "ST_CELL_PLATE_PT_ORGANISM");
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SemanticAnnotationSearchCriteria c = new SemanticAnnotationSearchCriteria();
        c.withPermId().thatEquals("ST_MASTER_PLATE");

        SemanticAnnotationFetchOptions fo = new SemanticAnnotationFetchOptions();
        fo.withEntityType();
        fo.withPropertyType();

        v3api.searchSemanticAnnotations(sessionToken, c, fo);

        assertAccessLog(
                "search-semantic-annotations  SEARCH_CRITERIA:\n'SEMANTIC_ANNOTATION\n    with attribute 'perm id' equal to 'ST_MASTER_PLATE'\n'\nFETCH_OPTIONS:\n'SemanticAnnotation\n    with EntityType\n    with PropertyType\n'");
    }

    private void testSearch(String user, SemanticAnnotationSearchCriteria criteria, String... expectedPermIds)
    {
        String sessionToken = v3api.login(user, PASSWORD);

        SearchResult<SemanticAnnotation> searchResult = v3api.searchSemanticAnnotations(sessionToken, criteria, new SemanticAnnotationFetchOptions());
        List<SemanticAnnotation> annotations = searchResult.getObjects();

        Set<String> actualSet = new HashSet<String>();
        for (SemanticAnnotation annotation : annotations)
        {
            actualSet.add(annotation.getPermId().getPermId());
        }

        assertCollectionContainsOnly(actualSet, expectedPermIds);

        v3api.logout(sessionToken);
    }

}

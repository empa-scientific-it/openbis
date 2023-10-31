/*
 * Copyright ETH 2016 - 2023 Zürich, Scientific IT Services
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
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.SemanticAnnotation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.id.SemanticAnnotationPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort.CodeComparator;
import ch.systemsx.cisd.common.action.IDelegatedAction;

/**
 * @author Franz-Josef Elmer
 */
public class SearchSampleTypeTest extends AbstractTest
{
    @Test
    public void testSearchAllWithVocabularies()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleTypeSearchCriteria searchCriteria = new SampleTypeSearchCriteria();
        SampleTypeFetchOptions fetchOptions = new SampleTypeFetchOptions();
        fetchOptions.withPropertyAssignments().withPropertyType().withVocabulary();

        SearchResult<SampleType> searchResult = v3api.searchSampleTypes(sessionToken, searchCriteria, fetchOptions);

        List<SampleType> types = searchResult.getObjects();
        List<String> codes = extractCodes(types);

        List<String> vocabularyCodes = new ArrayList<String>();
        for (SampleType type : types)
        {
            vocabularyCodes.addAll(extractVocabularyCodes(type.getPropertyAssignments()));
        }

        Collections.sort(vocabularyCodes);
        assertEquals(
                vocabularyCodes.toString(),
                "[$PLATE_GEOMETRY, $PLATE_GEOMETRY, ORGANISM, ORGANISM, ORGANISM, TEST_VOCABULARY]");
        Collections.sort(codes);
        assertEquals(
                codes.toString(),
                "[CELL_PLATE, CONTROL_LAYOUT, DELETION_TEST, DILUTION_PLATE, DYNAMIC_PLATE, IMPOSSIBLE, IMPOSSIBLE_TO_UPDATE, MASTER_PLATE, NORMAL, REINFECT_PLATE, VALIDATE_CHILDREN, WELL]");
        assertEquals(types.get(0).getFetchOptions().hasPropertyAssignments(), true);
        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithAndOperator()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleTypeSearchCriteria searchCriteria = new SampleTypeSearchCriteria();
        searchCriteria.withAndOperator();
        searchCriteria.withCode().thatContains("ION");
        searchCriteria.withCode().thatContains("PLATE");
        SampleTypeFetchOptions fetchOptions = new SampleTypeFetchOptions();

        SearchResult<SampleType> searchResult = v3api.searchSampleTypes(sessionToken, searchCriteria, fetchOptions);

        List<SampleType> types = searchResult.getObjects();
        List<String> codes = extractCodes(types);
        Collections.sort(codes);
        assertEquals(codes.toString(), "[DILUTION_PLATE]");
        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithOrOperator()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleTypeSearchCriteria searchCriteria = new SampleTypeSearchCriteria();
        searchCriteria.withOrOperator();
        searchCriteria.withCode().thatContains("ION");
        searchCriteria.withCode().thatContains("PLATE");
        SampleTypeFetchOptions fetchOptions = new SampleTypeFetchOptions();

        SearchResult<SampleType> searchResult = v3api.searchSampleTypes(sessionToken, searchCriteria, fetchOptions);

        List<SampleType> types = searchResult.getObjects();
        List<String> codes = extractCodes(types);
        Collections.sort(codes);
        assertEquals(codes.toString(), "[CELL_PLATE, DELETION_TEST, DILUTION_PLATE, DYNAMIC_PLATE, MASTER_PLATE, REINFECT_PLATE]");
        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithIdThatEquals()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        // permId without entityKind

        SampleTypeSearchCriteria searchCriteria = new SampleTypeSearchCriteria();
        searchCriteria.withId().thatEquals(new EntityTypePermId("CELL_PLATE"));

        SearchResult<SampleType> searchResult = v3api.searchSampleTypes(sessionToken, searchCriteria, new SampleTypeFetchOptions());
        assertEquals(searchResult.getObjects().size(), 1);

        SampleType type = searchResult.getObjects().get(0);
        assertEquals(type.getCode(), "CELL_PLATE");

        // permId with SAMPLE entityKind

        searchCriteria = new SampleTypeSearchCriteria();
        searchCriteria.withId().thatEquals(new EntityTypePermId("CELL_PLATE", EntityKind.SAMPLE));

        searchResult = v3api.searchSampleTypes(sessionToken, searchCriteria, new SampleTypeFetchOptions());
        assertEquals(searchResult.getObjects().size(), 1);

        type = searchResult.getObjects().get(0);
        assertEquals(type.getCode(), "CELL_PLATE");

        // permId with non-SAMPLE entityKind

        searchCriteria = new SampleTypeSearchCriteria();
        searchCriteria.withId().thatEquals(new EntityTypePermId("CELL_PLATE", EntityKind.EXPERIMENT));

        searchResult = v3api.searchSampleTypes(sessionToken, searchCriteria, new SampleTypeFetchOptions());
        assertEquals(searchResult.getObjects().size(), 0);

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithPermIdThatEquals()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleTypeSearchCriteria searchCriteria = new SampleTypeSearchCriteria();
        searchCriteria.withPermId().thatEquals("CELL_PLATE");

        SearchResult<SampleType> searchResult = v3api.searchSampleTypes(sessionToken, searchCriteria, new SampleTypeFetchOptions());
        assertEquals(searchResult.getObjects().size(), 1);

        SampleType type = searchResult.getObjects().get(0);
        assertEquals(type.getCode(), "CELL_PLATE");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithUnknownIdClass()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleTypeSearchCriteria searchCriteria = new SampleTypeSearchCriteria();
        searchCriteria.withId().thatEquals(new IEntityTypeId()
            {
                private static final long serialVersionUID = 1L;
            });

        assertRuntimeException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.searchSampleTypes(sessionToken, searchCriteria, new SampleTypeFetchOptions());
                }
            }, "Unknown id: class ");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithCodeThatEquals()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleTypeSearchCriteria searchCriteria = new SampleTypeSearchCriteria();
        searchCriteria.withCode().thatEquals("MASTER_PLATE");
        SampleTypeFetchOptions fetchOptions = new SampleTypeFetchOptions();
        fetchOptions.withPropertyAssignments();
        SearchResult<SampleType> searchResult = v3api.searchSampleTypes(sessionToken, searchCriteria, fetchOptions);

        List<SampleType> types = searchResult.getObjects();
        List<String> codes = extractCodes(types);
        Collections.sort(codes);
        assertEquals(codes.toString(), "[MASTER_PLATE]");
        assertEquals(types.get(0).getFetchOptions().hasPropertyAssignments(), true);
        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithCodeThatEqualsWithStarWildcard()
    {
        final SampleTypeSearchCriteria criteria1 = new SampleTypeSearchCriteria();
        criteria1.withCode().withWildcards().thatEquals("*_PLATE");
        testSearch(criteria1, "CELL_PLATE", "DILUTION_PLATE", "DYNAMIC_PLATE", "MASTER_PLATE", "REINFECT_PLATE");

        final SampleTypeSearchCriteria criteria2 = new SampleTypeSearchCriteria();
        criteria2.withCode().withoutWildcards().thatEquals("*_PLATE");
        testSearch(criteria2);
    }

    @Test
    public void testSearchWithCodeThatEqualsWithQuestionMarkWildcard()
    {
        final SampleTypeSearchCriteria criteria1 = new SampleTypeSearchCriteria();
        criteria1.withCode().withWildcards().thatEquals("????????_PLATE");
        testSearch(criteria1, "DILUTION_PLATE", "REINFECT_PLATE");

        final SampleTypeSearchCriteria criteria2 = new SampleTypeSearchCriteria();
        criteria2.withCode().withoutWildcards().thatEquals("????????_PLATE");
        testSearch(criteria2);
    }

    @Test
    public void testSearchWithCodeThatStartsWithD()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleTypeSearchCriteria searchCriteria = new SampleTypeSearchCriteria();
        searchCriteria.withCode().thatStartsWith("D");
        SampleTypeFetchOptions fetchOptions = new SampleTypeFetchOptions();

        SearchResult<SampleType> searchResult = v3api.searchSampleTypes(sessionToken, searchCriteria, fetchOptions);

        List<SampleType> types = searchResult.getObjects();
        List<String> codes = extractCodes(types);
        Collections.sort(codes);
        assertEquals(codes.toString(), "[DELETION_TEST, DILUTION_PLATE, DYNAMIC_PLATE]");
        assertEquals(types.get(0).getFetchOptions().hasPropertyAssignments(), false);
        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithCodeThatStartsWithStarWildcard()
    {
        final SampleTypeSearchCriteria criteria1 = new SampleTypeSearchCriteria();
        criteria1.withCode().withWildcards().thatStartsWith("D*N_");
        testSearch(criteria1, "DELETION_TEST", "DILUTION_PLATE");

        final SampleTypeSearchCriteria criteria2 = new SampleTypeSearchCriteria();
        criteria2.withCode().withoutWildcards().thatStartsWith("D*N_");
        testSearch(criteria2);
    }

    @Test
    public void testSearchWithCodeThatStartsWithQuestionMarkWildcard()
    {
        final SampleTypeSearchCriteria criteria1 = new SampleTypeSearchCriteria();
        criteria1.withCode().withWildcards().thatStartsWith("D??????_");
        testSearch(criteria1, "DYNAMIC_PLATE");

        final SampleTypeSearchCriteria criteria2 = new SampleTypeSearchCriteria();
        criteria2.withCode().withoutWildcards().thatStartsWith("D??????_");
        testSearch(criteria2);
    }

    @Test
    public void testSearchWithCodeThatEndsWithStarWildcard()
    {
        final SampleTypeSearchCriteria criteria1 = new SampleTypeSearchCriteria();
        criteria1.withCode().withWildcards().thatEndsWith("_P*E");
        testSearch(criteria1, "CELL_PLATE", "DILUTION_PLATE", "DYNAMIC_PLATE", "MASTER_PLATE", "REINFECT_PLATE");

        final SampleTypeSearchCriteria criteria2 = new SampleTypeSearchCriteria();
        criteria2.withCode().withoutWildcards().thatEndsWith("_P*E");
        testSearch(criteria2);
    }

    @Test
    public void testSearchWithCodeThatEndsWithQuestionMarkWildcard()
    {
        final SampleTypeSearchCriteria criteria1 = new SampleTypeSearchCriteria();
        criteria1.withCode().withWildcards().thatEndsWith("_???T");
        testSearch(criteria1, "DELETION_TEST");

        final SampleTypeSearchCriteria criteria2 = new SampleTypeSearchCriteria();
        criteria2.withCode().withoutWildcards().thatEndsWith("_???T");
        testSearch(criteria2);
    }

    @Test
    public void testSearchWithCodeThatContainsWithStarWildcard()
    {
        final SampleTypeSearchCriteria criteria1 = new SampleTypeSearchCriteria();
        criteria1.withCode().withWildcards().thatContains("POS*BLE");
        testSearch(criteria1, "IMPOSSIBLE", "IMPOSSIBLE_TO_UPDATE");

        final SampleTypeSearchCriteria criteria2 = new SampleTypeSearchCriteria();
        criteria2.withCode().withoutWildcards().thatContains("POS*BLE");
        testSearch(criteria2);
    }

    @Test
    public void testSearchWithCodeThatContainsWithQuestionMarkWildcard()
    {
        final SampleTypeSearchCriteria criteria1 = new SampleTypeSearchCriteria();
        criteria1.withCode().withWildcards().thatContains("R??_");
        testSearch(criteria1, "CONTROL_LAYOUT");

        final SampleTypeSearchCriteria criteria2 = new SampleTypeSearchCriteria();
        criteria2.withCode().withoutWildcards().thatContains("R??_");
        testSearch(criteria2);
    }

    @Test
    public void testSearchWithCodesThatIn()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleTypeSearchCriteria searchCriteria = new SampleTypeSearchCriteria();
        searchCriteria.withCodes().thatIn(Arrays.asList("MASTER_PLATE", "CELL_PLATE"));

        SearchResult<SampleType> searchResult = v3api.searchSampleTypes(sessionToken, searchCriteria, new SampleTypeFetchOptions());

        List<SampleType> types = searchResult.getObjects();
        List<String> codes = extractCodes(types);
        Collections.sort(codes);

        assertEquals(codes.toString(), "[CELL_PLATE, MASTER_PLATE]");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithCodeThatStartsWithStarWildcardWithIn()
    {
        final SampleTypeSearchCriteria criteria1 = new SampleTypeSearchCriteria();
        criteria1.withOrOperator();
        criteria1.withCode().withWildcards().thatStartsWith("D*N_");
        criteria1.withCodes().thatIn(Arrays.asList("MASTER_PLATE", "CELL_PLATE"));
        testSearch(criteria1, "CELL_PLATE", "DELETION_TEST", "DILUTION_PLATE", "MASTER_PLATE");

        final SampleTypeSearchCriteria criteria2 = new SampleTypeSearchCriteria();
        criteria2.withOrOperator();
        criteria2.withCode().withoutWildcards().thatStartsWith("D*N_");
        criteria2.withCodes().thatIn(Arrays.asList("MASTER_PLATE", "CELL_PLATE"));
        testSearch(criteria2, "CELL_PLATE", "MASTER_PLATE");
    }

    @Test
    public void testSearchWithIdsThatIn()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleTypeSearchCriteria searchCriteria = new SampleTypeSearchCriteria();
        searchCriteria.withIds().thatIn(Arrays.asList(new EntityTypePermId("MASTER_PLATE"), new EntityTypePermId("CELL_PLATE")));

        SearchResult<SampleType> searchResult = v3api.searchSampleTypes(sessionToken, searchCriteria, new SampleTypeFetchOptions());

        List<SampleType> types = searchResult.getObjects();
        List<String> codes = extractCodes(types);
        Collections.sort(codes);

        assertEquals(codes.toString(), "[CELL_PLATE, MASTER_PLATE]");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithListableOnly()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleTypeSearchCriteria searchCriteria = new SampleTypeSearchCriteria();
        searchCriteria.withListable().thatEquals(true);
        SampleTypeFetchOptions fetchOptions = new SampleTypeFetchOptions();

        SearchResult<SampleType> searchResult = v3api.searchSampleTypes(sessionToken, searchCriteria, fetchOptions);

        List<SampleType> types = searchResult.getObjects();
        List<String> codes = extractCodes(types);
        Collections.sort(codes);
        assertEquals(codes.toString(), "[CELL_PLATE, CONTROL_LAYOUT, DELETION_TEST, DILUTION_PLATE, "
                + "DYNAMIC_PLATE, IMPOSSIBLE, IMPOSSIBLE_TO_UPDATE, MASTER_PLATE, NORMAL, REINFECT_PLATE, "
                + "VALIDATE_CHILDREN]");
        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithNonListableOnly()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleTypeSearchCriteria searchCriteria = new SampleTypeSearchCriteria();
        searchCriteria.withListable().thatEquals(false);
        SampleTypeFetchOptions fetchOptions = new SampleTypeFetchOptions();

        SearchResult<SampleType> searchResult = v3api.searchSampleTypes(sessionToken, searchCriteria, fetchOptions);

        List<SampleType> types = searchResult.getObjects();
        List<String> codes = extractCodes(types);
        Collections.sort(codes);
        assertEquals(codes.toString(), "[WELL]");
        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithListableAndNonListable()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleTypeSearchCriteria searchCriteria = new SampleTypeSearchCriteria();
        SampleTypeFetchOptions fetchOptions = new SampleTypeFetchOptions();

        SearchResult<SampleType> searchResult = v3api.searchSampleTypes(sessionToken, searchCriteria, fetchOptions);

        List<SampleType> types = searchResult.getObjects();
        List<String> codes = extractCodes(types);
        Collections.sort(codes);
        assertEquals(codes.toString(), "[CELL_PLATE, CONTROL_LAYOUT, DELETION_TEST, DILUTION_PLATE, "
                + "DYNAMIC_PLATE, IMPOSSIBLE, IMPOSSIBLE_TO_UPDATE, MASTER_PLATE, NORMAL, REINFECT_PLATE, "
                + "VALIDATE_CHILDREN, WELL]");
        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithPropertyAssignmentsSortByLabelDesc()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleTypeSearchCriteria searchCriteria = new SampleTypeSearchCriteria();
        searchCriteria.withCode().thatStartsWith("D");
        SampleTypeFetchOptions fetchOptions = new SampleTypeFetchOptions();
        fetchOptions.withPropertyAssignments().sortBy().label().desc();

        SearchResult<SampleType> searchResult = v3api.searchSampleTypes(sessionToken, searchCriteria, fetchOptions);

        List<SampleType> types = searchResult.getObjects();
        Collections.sort(types, new CodeComparator<SampleType>());
        List<String> codes = extractCodes(types);
        assertEquals(codes.toString(), "[DELETION_TEST, DILUTION_PLATE, DYNAMIC_PLATE]");
        assertEquals(types.get(0).getFetchOptions().hasPropertyAssignments(), true);
        List<PropertyAssignment> propertyAssignments = types.get(0).getPropertyAssignments();
        assertOrder(propertyAssignments, "BACTERIUM", "ORGANISM", "DESCRIPTION");
        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithPropertyAssignments()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final SampleTypeSearchCriteria searchCriteria = new SampleTypeSearchCriteria();
        final Set<String> requiredPropertyTypeCodes = Set.of("BACTERIUM", "ORGANISM", "DESCRIPTION");
        searchCriteria.withPropertyAssignments().withPropertyType().withCodes().thatIn(requiredPropertyTypeCodes);

        final SampleTypeFetchOptions fetchOptions = new SampleTypeFetchOptions();
        fetchOptions.withPropertyAssignments().sortBy().code().desc();

        final SearchResult<SampleType> searchResult = v3api.searchSampleTypes(sessionToken, searchCriteria, fetchOptions);
        final List<SampleType> sampleTypes = searchResult.getObjects();

        assertTrue(sampleTypes.get(0).getFetchOptions().hasPropertyAssignments());

        for (final SampleType sampleType : sampleTypes)
        {
            final Set<String> propertyTypeCodes = sampleType.getPropertyAssignments().stream()
                    .map(propertyAssignment -> propertyAssignment.getPropertyType().getCode())
                    .collect(Collectors.toSet());
            final Set<String> originalPropertyTypeCodes = new HashSet<>(propertyTypeCodes);
            propertyTypeCodes.retainAll(requiredPropertyTypeCodes);
            assertFalse(propertyTypeCodes.isEmpty(),
                    String.format("Sample type %s contains assignments to property types %s which do not have any of the required ones %s.",
                            sampleType, originalPropertyTypeCodes, requiredPropertyTypeCodes));
        }

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithPropertyAssignmentsWithEntityTypeWithCodeThatEquals()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleTypeSearchCriteria searchCriteria = new SampleTypeSearchCriteria();
        searchCriteria.withPropertyAssignments().withEntityType().withCode().thatEquals("CELL_PLATE");

        SearchResult<SampleType> searchResult = v3api.searchSampleTypes(sessionToken, searchCriteria, new SampleTypeFetchOptions());

        List<String> codes = extractCodes(searchResult.getObjects());
        assertEquals(codes.toString(), "[CELL_PLATE]");
    }

    @Test
    public void testSearchWithPropertyAssignmentsWithEntityTypeWithCodes()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleTypeSearchCriteria searchCriteria = new SampleTypeSearchCriteria();
        searchCriteria.withPropertyAssignments().withEntityType().withCodes().thatIn(Arrays.asList("CELL_PLATE"));

        SearchResult<SampleType> searchResult = v3api.searchSampleTypes(sessionToken, searchCriteria, new SampleTypeFetchOptions());

        List<String> codes = extractCodes(searchResult.getObjects());
        assertEquals(codes.toString(), "[CELL_PLATE]");
    }

    @Test
    public void testSearchWithPropertyAssignmentsWithPropertyTypeWithIdThatEquals()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleTypeSearchCriteria criteria = new SampleTypeSearchCriteria();
        criteria.withPropertyAssignments().withPropertyType().withId().thatEquals(new PropertyTypePermId("DESCRIPTION"));

        SearchResult<SampleType> searchResult = v3api.searchSampleTypes(sessionToken, criteria, new SampleTypeFetchOptions());

        List<String> codes = extractCodes(searchResult.getObjects());
        Collections.sort(codes);
        assertEquals(codes.toString(), "[CONTROL_LAYOUT, DELETION_TEST, MASTER_PLATE]");
    }

    @Test
    public void testSearchWithPropertyAssignmentsWithPropertyTypeWithCodeThatEquals()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleTypeSearchCriteria criteria = new SampleTypeSearchCriteria();
        criteria.withPropertyAssignments().withPropertyType().withCode().thatEquals("DESCRIPTION");

        SearchResult<SampleType> searchResult = v3api.searchSampleTypes(sessionToken, criteria, new SampleTypeFetchOptions());

        List<String> codes = extractCodes(searchResult.getObjects());
        Collections.sort(codes);
        assertEquals(codes.toString(), "[CONTROL_LAYOUT, DELETION_TEST, MASTER_PLATE]");
    }

    @Test
    public void testSearchWithPropertyAssignmentsWithPropertyTypeWithSemanticAnnotationsWithIdThatEquals()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleTypeSearchCriteria criteria = new SampleTypeSearchCriteria();
        criteria.withPropertyAssignments().withPropertyType().withSemanticAnnotations().withId()
                .thatEquals(new SemanticAnnotationPermId("PT_DESCRIPTION"));

        SearchResult<SampleType> searchResult = v3api.searchSampleTypes(sessionToken, criteria, new SampleTypeFetchOptions());

        List<String> codes = extractCodes(searchResult.getObjects());
        Collections.sort(codes);
        assertEquals(codes.toString(), "[CONTROL_LAYOUT, DELETION_TEST, MASTER_PLATE]");
    }

    @Test
    public void testSearchWithPropertyAssignmentsWithSemanticAnnotationsWithIdThatEqualsWhereSemanticAnnotationDefinedAtPropertyAssignmentLevel()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleTypeSearchCriteria criteria = new SampleTypeSearchCriteria();
        criteria.withPropertyAssignments().withSemanticAnnotations().withId()
                .thatEquals(new SemanticAnnotationPermId("ST_CELL_PLATE_PT_ORGANISM"));

        SearchResult<SampleType> searchResult = v3api.searchSampleTypes(sessionToken, criteria, new SampleTypeFetchOptions());

        assertEquals(searchResult.getObjects().size(), 1);
        SampleType type = searchResult.getObjects().get(0);
        assertEquals(type.getCode(), "CELL_PLATE");
    }

    @Test
    public void testSearchWithPropertyAssignmentsWithSemanticAnnotationsWithIdThatEqualsWhereSemanticAnnotationDefinedAtPropertyTypeLevel()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleTypeSearchCriteria criteria = new SampleTypeSearchCriteria();
        criteria.withPropertyAssignments().withSemanticAnnotations().withId()
                .thatEquals(new SemanticAnnotationPermId("PT_ORGANISM"));

        SearchResult<SampleType> searchResult = v3api.searchSampleTypes(sessionToken, criteria, new SampleTypeFetchOptions());

        List<String> codes = extractCodes(searchResult.getObjects());
        Collections.sort(codes);
        assertEquals(codes.toString(), "[DELETION_TEST, NORMAL]");
    }

    @Test
    public void testSearchWithPropertyAssignmentsFetched()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleTypeSearchCriteria searchCriteria = new SampleTypeSearchCriteria();
        searchCriteria.withId().thatEquals(new EntityTypePermId("MASTER_PLATE"));

        SampleTypeFetchOptions fetchOptions = new SampleTypeFetchOptions();
        fetchOptions.withPropertyAssignments().sortBy().code().asc();

        SearchResult<SampleType> searchResult = v3api.searchSampleTypes(sessionToken, searchCriteria, fetchOptions);
        assertEquals(searchResult.getObjects().size(), 1);

        SampleType type = searchResult.getObjects().get(0);
        assertEquals(type.getCode(), "MASTER_PLATE");
        assertEquals(type.getPropertyAssignments().size(), 2);
        assertEquals(type.getPropertyAssignments().get(0).getPropertyType().getCode(), "$PLATE_GEOMETRY");
        assertEquals(type.getPropertyAssignments().get(1).getPropertyType().getCode(), "DESCRIPTION");
    }

    @Test
    public void testSearchWithIdsThatInWithPropertyAssignmentsWithPropertyTypeWithIdThatEquals()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleTypeSearchCriteria criteria = new SampleTypeSearchCriteria();
        criteria.withAndOperator();
        criteria.withPropertyAssignments().withPropertyType().withId().thatEquals(new PropertyTypePermId("DESCRIPTION"));
        criteria.withIds().thatIn(Arrays.asList(new EntityTypePermId("MASTER_PLATE"), new EntityTypePermId("CONTROL_LAYOUT")));

        SearchResult<SampleType> searchResult = v3api.searchSampleTypes(sessionToken, criteria, new SampleTypeFetchOptions());

        List<String> codes = extractCodes(searchResult.getObjects());
        Collections.sort(codes);
        assertEquals(codes.toString(), "[CONTROL_LAYOUT, MASTER_PLATE]");
    }

    @Test
    public void testSearchWithValidationPlugin()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleTypeSearchCriteria searchCriteria = new SampleTypeSearchCriteria();
        searchCriteria.withCode().thatEquals("IMPOSSIBLE_TO_UPDATE");
        SampleTypeFetchOptions fetchOptions = new SampleTypeFetchOptions();
        fetchOptions.withValidationPlugin().withScript();

        // When
        SampleType type = v3api.searchSampleTypes(sessionToken, searchCriteria, fetchOptions).getObjects().get(0);

        // Then
        assertEquals(type.getFetchOptions().hasValidationPlugin(), true);
        assertEquals(type.getValidationPlugin().getFetchOptions().hasScript(), true);
        assertEquals(type.getValidationPlugin().getName(), "validateUpdateFAIL");
        assertEquals(type.getValidationPlugin().getScript(), "def validate(entity, isNew):\n  if (not isNew):\n"
                + "    return \"Cannot update this entity\"\n ");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithSemanticAnnotationsWithIdThatEquals()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleTypeSearchCriteria searchCriteria = new SampleTypeSearchCriteria();
        searchCriteria.withSemanticAnnotations().withId().thatEquals(new SemanticAnnotationPermId("ST_DILUTION_PLATE"));

        SearchResult<SampleType> searchResult = v3api.searchSampleTypes(sessionToken, searchCriteria, new SampleTypeFetchOptions());

        List<String> codes = extractCodes(searchResult.getObjects());
        Collections.sort(codes);
        assertEquals(codes.toString(), "[DILUTION_PLATE]");
    }

    @Test
    public void testSearchWithSemanticAnnotationsWithPermIdThatEquals()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleTypeSearchCriteria searchCriteria = new SampleTypeSearchCriteria();
        searchCriteria.withSemanticAnnotations().withPermId().thatEquals("ST_DILUTION_PLATE");

        SearchResult<SampleType> searchResult = v3api.searchSampleTypes(sessionToken, searchCriteria, new SampleTypeFetchOptions());

        List<String> codes = extractCodes(searchResult.getObjects());
        Collections.sort(codes);
        assertEquals(codes.toString(), "[DILUTION_PLATE]");
    }

    @Test
    public void testSearchWithSemanticAnnotationsFetchedForSampleTypeAndDefined()
    {
        testSearchWithSemanticAnnotationsFetchedForSampleType("MASTER_PLATE", 1);
    }

    @Test
    public void testSearchWithSemanticAnnotationsFetchedForSampleTypeAndNotDefined()
    {
        testSearchWithSemanticAnnotationsFetchedForSampleType("VALIDATE_CHILDREN", null);
    }

    private void testSearchWithSemanticAnnotationsFetchedForSampleType(String typeCode, Integer annotationIndex)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleTypeSearchCriteria searchCriteria = new SampleTypeSearchCriteria();
        searchCriteria.withId().thatEquals(new EntityTypePermId(typeCode));

        SampleTypeFetchOptions fetchOptions = new SampleTypeFetchOptions();
        fetchOptions.withSemanticAnnotations();

        SearchResult<SampleType> searchResult = v3api.searchSampleTypes(sessionToken, searchCriteria, fetchOptions);
        assertEquals(searchResult.getObjects().size(), 1);

        SampleType type = searchResult.getObjects().get(0);
        assertEquals(type.getCode(), typeCode);
        assertEquals(type.getFetchOptions().hasSemanticAnnotations(), true);

        List<SemanticAnnotation> annotations = type.getSemanticAnnotations();

        if (annotationIndex == null)
        {
            assertEquals(annotations.size(), 0);
        } else
        {
            assertEquals(annotations.get(0).getPredicateOntologyId(), "testPredicateOntologyId" + annotationIndex);
            assertEquals(annotations.get(0).getPredicateOntologyVersion(), "testPredicateOntologyVersion" + annotationIndex);
            assertEquals(annotations.get(0).getPredicateAccessionId(), "testPredicateAccessionId" + annotationIndex);
            assertEquals(annotations.get(0).getDescriptorOntologyId(), "testDescriptorOntologyId" + annotationIndex);
            assertEquals(annotations.get(0).getDescriptorOntologyVersion(), "testDescriptorOntologyVersion" + annotationIndex);
            assertEquals(annotations.get(0).getDescriptorAccessionId(), "testDescriptorAccessionId" + annotationIndex);
            assertEquals(annotations.size(), 1);
        }
    }

    @Test
    public void testSearchWithSemanticAnnotationsFetchedForPropertyTypeAndDefined()
    {
        testSearchWithSemanticAnnotationsFetchedForPropertyType("MASTER_PLATE", "DESCRIPTION", 5);
    }

    @Test
    public void testSearchWithSemanticAnnotationsFetchedForPropertyTypeAndNotDefined()
    {
        testSearchWithSemanticAnnotationsFetchedForPropertyType("VALIDATE_CHILDREN", "COMMENT", null);
    }

    @SuppressWarnings("null")
    private void testSearchWithSemanticAnnotationsFetchedForPropertyType(String typeCode, String propertyCode, Integer annotationIndex)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleTypeSearchCriteria searchCriteria = new SampleTypeSearchCriteria();
        searchCriteria.withId().thatEquals(new EntityTypePermId(typeCode));

        SampleTypeFetchOptions fetchOptions = new SampleTypeFetchOptions();
        fetchOptions.withPropertyAssignments().withPropertyType().withSemanticAnnotations();

        SearchResult<SampleType> searchResult = v3api.searchSampleTypes(sessionToken, searchCriteria, fetchOptions);
        assertEquals(searchResult.getObjects().size(), 1);
        SampleType type = searchResult.getObjects().get(0);

        PropertyAssignment matchingAssignment = null;

        for (PropertyAssignment assignment : type.getPropertyAssignments())
        {
            if (assignment.getPropertyType().getCode().equals(propertyCode))
            {
                matchingAssignment = assignment;
                break;
            }
        }

        assertNotNull(matchingAssignment);

        if (annotationIndex == null)
        {
            assertEquals(matchingAssignment.getPropertyType().getSemanticAnnotations().size(), 0);
        } else
        {
            assertEquals(matchingAssignment.getPropertyType().getSemanticAnnotations().size(), 1);

            SemanticAnnotation annotation = matchingAssignment.getPropertyType().getSemanticAnnotations().get(0);
            assertEquals(annotation.getPredicateOntologyId(), "testPredicateOntologyId" + annotationIndex);
            assertEquals(annotation.getPredicateOntologyVersion(), "testPredicateOntologyVersion" + annotationIndex);
            assertEquals(annotation.getPredicateAccessionId(), "testPredicateAccessionId" + annotationIndex);
            assertEquals(annotation.getDescriptorOntologyId(), "testDescriptorOntologyId" + annotationIndex);
            assertEquals(annotation.getDescriptorOntologyVersion(), "testDescriptorOntologyVersion" + annotationIndex);
            assertEquals(annotation.getDescriptorAccessionId(), "testDescriptorAccessionId" + annotationIndex);
        }
    }

    @Test
    public void testSearchWithSemanticAnnotationsFetchedForSamplePropertyAssignmentsAndSemanticAnnotationsNotDefined()
    {
        testSearchWithSemanticAnnotationsFetchedForSamplePropertyAssignments("VALIDATE_CHILDREN", "COMMENT", null);
    }

    @Test
    public void testSearchWithSemanticAnnotationsFetchedForSamplePropertyAssignmentsAndSemanticAnnotationsDefinedAtPropertyTypeLevelOnly()
    {
        testSearchWithSemanticAnnotationsFetchedForSamplePropertyAssignments("MASTER_PLATE", "DESCRIPTION", 5);
    }

    @Test
    public void testSearchWithSemanticAnnotationsFetchedForSamplePropertyAssignmentsAndSemanticAnnotationsDefinedAtSamplePropertyAssignmentLevelOnly()
    {
        testSearchWithSemanticAnnotationsFetchedForSamplePropertyAssignments("CELL_PLATE", "ORGANISM", 7);
    }

    @Test
    public void testSearchWithSemanticAnnotationsFetchedForSamplePropertyAssignmentsAndSemanticAnnotationsDefinedAtBothSamplePropertyAssignmentAndPropertyTypeLevels()
    {
        testSearchWithSemanticAnnotationsFetchedForSamplePropertyAssignments("MASTER_PLATE", "$PLATE_GEOMETRY", 3);
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleTypeSearchCriteria c = new SampleTypeSearchCriteria();
        c.withCode().thatEquals("MASTER_PLATE");

        SampleTypeFetchOptions fo = new SampleTypeFetchOptions();
        fo.withPropertyAssignments();

        v3api.searchSampleTypes(sessionToken, c, fo);

        assertAccessLog(
                "search-sample-types  SEARCH_CRITERIA:\n'SAMPLE_TYPE\n    with attribute 'code' equal to 'MASTER_PLATE'\n'\nFETCH_OPTIONS:\n'SampleType\n    with PropertyAssignments\n'");
    }

    @SuppressWarnings("null")
    private void testSearchWithSemanticAnnotationsFetchedForSamplePropertyAssignments(String typeCode, String propertyCode, Integer annotationIndex)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleTypeSearchCriteria searchCriteria = new SampleTypeSearchCriteria();
        searchCriteria.withId().thatEquals(new EntityTypePermId(typeCode));

        SampleTypeFetchOptions fetchOptions = new SampleTypeFetchOptions();
        fetchOptions.withPropertyAssignments().withPropertyType();
        fetchOptions.withPropertyAssignments().withSemanticAnnotations();

        SearchResult<SampleType> searchResult = v3api.searchSampleTypes(sessionToken, searchCriteria, fetchOptions);
        assertEquals(searchResult.getObjects().size(), 1);
        SampleType type = searchResult.getObjects().get(0);

        PropertyAssignment matchingAssignment = null;

        for (PropertyAssignment assignment : type.getPropertyAssignments())
        {
            if (assignment.getPropertyType().getCode().equals(propertyCode))
            {
                matchingAssignment = assignment;
                break;
            }
        }

        assertNotNull(matchingAssignment);

        if (annotationIndex == null)
        {
            assertEquals(matchingAssignment.getSemanticAnnotations().size(), 0);
        } else
        {
            SemanticAnnotation annotation = matchingAssignment.getSemanticAnnotations().get(0);
            assertEquals(annotation.getPredicateOntologyId(), "testPredicateOntologyId" + annotationIndex);
            assertEquals(annotation.getPredicateOntologyVersion(), "testPredicateOntologyVersion" + annotationIndex);
            assertEquals(annotation.getPredicateAccessionId(), "testPredicateAccessionId" + annotationIndex);
            assertEquals(annotation.getDescriptorOntologyId(), "testDescriptorOntologyId" + annotationIndex);
            assertEquals(annotation.getDescriptorOntologyVersion(), "testDescriptorOntologyVersion" + annotationIndex);
            assertEquals(annotation.getDescriptorAccessionId(), "testDescriptorAccessionId" + annotationIndex);
        }
    }

    private void testSearch(SampleTypeSearchCriteria criteria, String... expectedCodes)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SearchResult<SampleType> searchResult = v3api.searchSampleTypes(sessionToken, criteria, new SampleTypeFetchOptions());

        List<SampleType> types = searchResult.getObjects();
        List<String> codes = extractCodes(types);
        Collections.sort(codes);
        assertEquals(codes.toString(), Arrays.toString(expectedCodes));
        v3api.logout(sessionToken);
    }

}

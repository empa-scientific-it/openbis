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
import static org.testng.Assert.assertTrue;

import java.util.*;
import java.util.stream.Collectors;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.PluginPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort.CodeComparator;

/**
 * @author Franz-Josef Elmer
 */
public class SearchExperimentTypeTest extends AbstractTest
{
    @Test
    public void testSearchAllWithVocabularies()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentTypeSearchCriteria searchCriteria = new ExperimentTypeSearchCriteria();
        ExperimentTypeFetchOptions fetchOptions = new ExperimentTypeFetchOptions();
        fetchOptions.withPropertyAssignments().withPropertyType().withVocabulary();
        SearchResult<ExperimentType> searchResult = v3api.searchExperimentTypes(sessionToken, searchCriteria, fetchOptions);

        List<ExperimentType> types = searchResult.getObjects();
        List<String> codes = extractCodes(types);
        Collections.sort(codes);
        assertEquals(codes.toString(), "[COMPOUND_HCS, DELETION_TEST, SIRNA_HCS]");
        assertEquals(types.get(0).getFetchOptions().hasPropertyAssignments(), true);

        List<String> vocabularyCodes = new ArrayList<String>();
        for (ExperimentType type : types)
        {
            vocabularyCodes.addAll(extractVocabularyCodes(type.getPropertyAssignments()));
        }
        Collections.sort(vocabularyCodes);
        assertEquals(vocabularyCodes.toString(), "[GENDER, ORGANISM]");
        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithAndOperator()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentTypeSearchCriteria searchCriteria = new ExperimentTypeSearchCriteria();
        searchCriteria.withAndOperator();
        searchCriteria.withCode().thatContains("I");
        searchCriteria.withCode().thatContains("HCS");
        ExperimentTypeFetchOptions fetchOptions = new ExperimentTypeFetchOptions();

        SearchResult<ExperimentType> searchResult = v3api.searchExperimentTypes(sessionToken, searchCriteria, fetchOptions);

        List<ExperimentType> types = searchResult.getObjects();
        List<String> codes = extractCodes(types);
        Collections.sort(codes);
        assertEquals(codes.toString(), "[SIRNA_HCS]");
        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithOrOperator()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentTypeSearchCriteria searchCriteria = new ExperimentTypeSearchCriteria();
        searchCriteria.withOrOperator();
        searchCriteria.withCode().thatContains("I");
        searchCriteria.withCode().thatContains("HCS");
        ExperimentTypeFetchOptions fetchOptions = new ExperimentTypeFetchOptions();

        SearchResult<ExperimentType> searchResult = v3api.searchExperimentTypes(sessionToken, searchCriteria, fetchOptions);

        List<ExperimentType> types = searchResult.getObjects();
        List<String> codes = extractCodes(types);
        Collections.sort(codes);
        assertEquals(codes.toString(), "[COMPOUND_HCS, DELETION_TEST, SIRNA_HCS]");
        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchExactCode()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentTypeSearchCriteria searchCriteria = new ExperimentTypeSearchCriteria();
        searchCriteria.withCode().thatEquals("SIRNA_HCS");
        ExperimentTypeFetchOptions fetchOptions = new ExperimentTypeFetchOptions();
        fetchOptions.withPropertyAssignments();
        SearchResult<ExperimentType> searchResult = v3api.searchExperimentTypes(sessionToken, searchCriteria, fetchOptions);

        List<ExperimentType> types = searchResult.getObjects();
        List<String> codes = extractCodes(types);
        Collections.sort(codes);
        assertEquals(codes.toString(), "[SIRNA_HCS]");
        assertEquals(types.get(0).getFetchOptions().hasPropertyAssignments(), true);
        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithIds()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentTypeSearchCriteria searchCriteria = new ExperimentTypeSearchCriteria();
        searchCriteria.withIds().thatIn(Arrays.asList(new EntityTypePermId("SIRNA_HCS")));
        SearchResult<ExperimentType> searchResult = v3api.searchExperimentTypes(sessionToken, searchCriteria, new ExperimentTypeFetchOptions());

        List<ExperimentType> types = searchResult.getObjects();
        List<String> codes = extractCodes(types);
        Collections.sort(codes);
        assertEquals(codes.toString(), "[SIRNA_HCS]");
        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithCodeThatStartsWithD()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentTypeSearchCriteria searchCriteria = new ExperimentTypeSearchCriteria();
        searchCriteria.withCode().thatStartsWith("D");
        ExperimentTypeFetchOptions fetchOptions = new ExperimentTypeFetchOptions();

        SearchResult<ExperimentType> searchResult = v3api.searchExperimentTypes(sessionToken, searchCriteria, fetchOptions);

        List<ExperimentType> types = searchResult.getObjects();
        List<String> codes = extractCodes(types);
        Collections.sort(codes);
        assertEquals(codes.toString(), "[DELETION_TEST]");
        assertEquals(types.get(0).getFetchOptions().hasPropertyAssignments(), false);
        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithPropertyAssignmentSortByCodeDesc()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentTypeSearchCriteria searchCriteria = new ExperimentTypeSearchCriteria();
        searchCriteria.withCode().thatStartsWith("D");
        ExperimentTypeFetchOptions fetchOptions = new ExperimentTypeFetchOptions();
        fetchOptions.withPropertyAssignments().sortBy().code().desc();

        SearchResult<ExperimentType> searchResult = v3api.searchExperimentTypes(sessionToken, searchCriteria, fetchOptions);

        List<ExperimentType> types = searchResult.getObjects();
        Collections.sort(types, new CodeComparator<ExperimentType>());
        List<String> codes = extractCodes(types);
        assertEquals(codes.toString(), "[DELETION_TEST]");
        assertEquals(types.get(0).getFetchOptions().hasPropertyAssignments(), true);
        List<PropertyAssignment> propertyAssignments = types.get(0).getPropertyAssignments();
        assertOrder(propertyAssignments, "ORGANISM", "DESCRIPTION", "BACTERIUM");
        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithPropertyAssignments()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final ExperimentTypeSearchCriteria searchCriteria = new ExperimentTypeSearchCriteria();
        final Set<String> requiredPropertyTypeCodes = Set.of("DESCRIPTION", "GENDER");
        searchCriteria.withPropertyAssignments().withPropertyType().withCodes().thatIn(requiredPropertyTypeCodes);

        final ExperimentTypeFetchOptions fetchOptions = new ExperimentTypeFetchOptions();
        fetchOptions.withPropertyAssignments().sortBy().code().desc();

        final SearchResult<ExperimentType> searchResult = v3api.searchExperimentTypes(sessionToken, searchCriteria, fetchOptions);
        final List<ExperimentType> experimentTypes = searchResult.getObjects();

        assertTrue(experimentTypes.get(0).getFetchOptions().hasPropertyAssignments());

        for (final ExperimentType experimentType : experimentTypes)
        {
            final Set<String> propertyTypeCodes = experimentType.getPropertyAssignments().stream()
                    .map(propertyAssignment -> propertyAssignment.getPropertyType().getCode())
                    .collect(Collectors.toSet());
            final Set<String> originalPropertyTypeCodes = new HashSet<>(propertyTypeCodes);
            propertyTypeCodes.retainAll(requiredPropertyTypeCodes);
            assertFalse(propertyTypeCodes.isEmpty(),
                    String.format("Experiment type %s contains assignments to property types %s which do not have any of the required ones %s.",
                            experimentType, originalPropertyTypeCodes, requiredPropertyTypeCodes));
        }

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithValidationPlugin()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentTypeCreation creation = new ExperimentTypeCreation();
        creation.setCode("MY-TYPE-" + System.currentTimeMillis());
        creation.setValidationPluginId(new PluginPermId("testEXPERIMENT"));
        EntityTypePermId typePermId = v3api.createExperimentTypes(sessionToken, Arrays.asList(creation)).get(0);
        ExperimentTypeSearchCriteria searchCriteria = new ExperimentTypeSearchCriteria();
        searchCriteria.withId().thatEquals(typePermId);
        ExperimentTypeFetchOptions fetchOptions = new ExperimentTypeFetchOptions();
        fetchOptions.withValidationPlugin().withScript();

        // When
        ExperimentType type = v3api.searchExperimentTypes(sessionToken, searchCriteria, fetchOptions).getObjects().get(0);

        // Then
        assertEquals(type.getFetchOptions().hasValidationPlugin(), true);
        assertEquals(type.getValidationPlugin().getFetchOptions().hasScript(), true);
        assertEquals(type.getValidationPlugin().getName(), "testEXPERIMENT");
        assertEquals(type.getValidationPlugin().getScript(), "import time;\ndef validate(entity, isNew):\n  pass\n ");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithValidationPluginAndIdIn()
    {
        // Given
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final ExperimentTypeCreation creation = new ExperimentTypeCreation();
        creation.setCode("MY-TYPE-" + System.currentTimeMillis());
        creation.setValidationPluginId(new PluginPermId("testEXPERIMENT"));
        final EntityTypePermId typePermId = v3api.createExperimentTypes(sessionToken, Arrays.asList(creation)).get(0);
        final ExperimentTypeSearchCriteria searchCriteria = new ExperimentTypeSearchCriteria();
        searchCriteria.withOrOperator();
        searchCriteria.withId().thatEquals(typePermId);
        searchCriteria.withIds().thatIn(Arrays.asList(new EntityTypePermId("SIRNA_HCS")));
        final ExperimentTypeFetchOptions fetchOptions = new ExperimentTypeFetchOptions();
        fetchOptions.withValidationPlugin().withScript();

        // When
        final SearchResult<ExperimentType> searchResult = v3api.searchExperimentTypes(sessionToken, searchCriteria, fetchOptions);

        // Then
        List<ExperimentType> types = searchResult.getObjects();
        List<String> codes = extractCodes(types);
        Collections.sort(codes);
        assertTrue(codes.toString().matches("\\[MY-TYPE-\\d{13}, SIRNA_HCS\\]"));

        final Optional<ExperimentType> typeOptional = types.stream().filter(experimentType -> experimentType.getCode().startsWith("MY-TYPE-"))
                .findFirst();
        assertTrue(typeOptional.isPresent());

        final ExperimentType type = typeOptional.get();
        assertTrue(type.getFetchOptions().hasValidationPlugin());
        assertTrue(type.getValidationPlugin().getFetchOptions().hasScript());
        assertEquals(type.getValidationPlugin().getName(), "testEXPERIMENT");
        assertEquals(type.getValidationPlugin().getScript(), "import time;\ndef validate(entity, isNew):\n  pass\n ");

        v3api.logout(sessionToken);
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentTypeSearchCriteria c = new ExperimentTypeSearchCriteria();
        c.withCode().thatEquals("SIRNA_HCS");

        ExperimentTypeFetchOptions fo = new ExperimentTypeFetchOptions();
        fo.withPropertyAssignments();

        v3api.searchExperimentTypes(sessionToken, c, fo);

        assertAccessLog(
                "search-experiment-types  SEARCH_CRITERIA:\n'EXPERIMENT_TYPE\n    with attribute 'code' equal to 'SIRNA_HCS'\n'\nFETCH_OPTIONS:\n'ExperimentType\n    with PropertyAssignments\n'");
    }

}

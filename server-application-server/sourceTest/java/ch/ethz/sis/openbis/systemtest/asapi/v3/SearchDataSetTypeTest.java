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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.Vocabulary;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort.CodeComparator;

/**
 * @author Franz-Josef Elmer
 */
public class SearchDataSetTypeTest extends AbstractTest
{
    @Test
    public void testSearchAll()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        DataSetTypeSearchCriteria searchCriteria = new DataSetTypeSearchCriteria();
        DataSetTypeFetchOptions fetchOptions = new DataSetTypeFetchOptions();
        fetchOptions.withPropertyAssignments();
        SearchResult<DataSetType> searchResult = v3api.searchDataSetTypes(sessionToken, searchCriteria, fetchOptions);

        List<DataSetType> types = searchResult.getObjects();
        List<String> codes = extractCodes(types);
        Collections.sort(codes);
        assertEquals(codes.toString(), "[CONTAINER_TYPE, DELETION_TEST, DELETION_TEST_CONTAINER, HCS_IMAGE, "
                + "HCS_IMAGE_ANALYSIS_DATA, IMAGING_DATA, LINK_TYPE, REQUIRES_EXPERIMENT, UNKNOWN, VALIDATED_CONTAINER_TYPE, "
                + "VALIDATED_IMPOSSIBLE_TO_UPDATE_TYPE, VALIDATED_NORMAL_TYPE]");
        assertEquals(types.get(0).getFetchOptions().hasPropertyAssignments(), true);
        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchAllWithoutVocabularies()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        DataSetTypeSearchCriteria searchCriteria = new DataSetTypeSearchCriteria();
        DataSetTypeFetchOptions fetchOptions = new DataSetTypeFetchOptions();
        fetchOptions.withPropertyAssignments().sortBy().code();

        SearchResult<DataSetType> searchResult = v3api.searchDataSetTypes(sessionToken, searchCriteria, fetchOptions);

        List<DataSetType> types = searchResult.getObjects();
        List<String> codes = extractCodes(types);
        Collections.sort(codes);
        assertEquals(codes.toString(), "[CONTAINER_TYPE, DELETION_TEST, DELETION_TEST_CONTAINER, HCS_IMAGE, "
                + "HCS_IMAGE_ANALYSIS_DATA, IMAGING_DATA, LINK_TYPE, REQUIRES_EXPERIMENT, UNKNOWN, VALIDATED_CONTAINER_TYPE, "
                + "VALIDATED_IMPOSSIBLE_TO_UPDATE_TYPE, VALIDATED_NORMAL_TYPE]");
        assertEquals(types.get(0).getFetchOptions().hasPropertyAssignments(), true);
        assertEquals(getDataSetTypePropertyTypeInfo(types).toString(), "[CONTAINER_TYPE:, "
                + "DELETION_TEST: BACTERIUM[MATERIAL] DESCRIPTION[VARCHAR] ORGANISM[CONTROLLEDVOCABULARY:?], "
                + "DELETION_TEST_CONTAINER: BACTERIUM[MATERIAL] DESCRIPTION[VARCHAR] ORGANISM[CONTROLLEDVOCABULARY:?], "
                + "HCS_IMAGE: ANY_MATERIAL[MATERIAL] BACTERIUM[MATERIAL] COMMENT[VARCHAR] GENDER[CONTROLLEDVOCABULARY:?], "
                + "HCS_IMAGE_ANALYSIS_DATA:, IMAGING_DATA: $IMAGING_DATA_CONFIG[JSON], LINK_TYPE:, REQUIRES_EXPERIMENT:, UNKNOWN:, "
                + "VALIDATED_CONTAINER_TYPE:, VALIDATED_IMPOSSIBLE_TO_UPDATE_TYPE:, VALIDATED_NORMAL_TYPE:]");
        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithAndOperator()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        DataSetTypeSearchCriteria searchCriteria = new DataSetTypeSearchCriteria();
        searchCriteria.withAndOperator();
        searchCriteria.withCode().thatContains("ELE");
        searchCriteria.withCode().thatContains("CONT");
        DataSetTypeFetchOptions fetchOptions = new DataSetTypeFetchOptions();
        fetchOptions.withPropertyAssignments().sortBy().code();

        SearchResult<DataSetType> searchResult = v3api.searchDataSetTypes(sessionToken, searchCriteria, fetchOptions);

        List<DataSetType> types = searchResult.getObjects();
        List<String> codes = extractCodes(types);
        Collections.sort(codes);
        assertEquals(codes.toString(), "[DELETION_TEST_CONTAINER]");
        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithIds()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        DataSetTypeSearchCriteria searchCriteria = new DataSetTypeSearchCriteria();
        searchCriteria.withIds().thatIn(Arrays.asList(new EntityTypePermId("DELETION_TEST_CONTAINER")));

        SearchResult<DataSetType> searchResult = v3api.searchDataSetTypes(sessionToken, searchCriteria, new DataSetTypeFetchOptions());

        List<DataSetType> types = searchResult.getObjects();
        List<String> codes = extractCodes(types);
        assertEquals(codes.toString(), "[DELETION_TEST_CONTAINER]");
        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithOrOperator()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        DataSetTypeSearchCriteria searchCriteria = new DataSetTypeSearchCriteria();
        searchCriteria.withOrOperator();
        searchCriteria.withCode().thatContains("ELE");
        searchCriteria.withCode().thatContains("CONT");
        DataSetTypeFetchOptions fetchOptions = new DataSetTypeFetchOptions();
        fetchOptions.withPropertyAssignments().sortBy().code();

        SearchResult<DataSetType> searchResult = v3api.searchDataSetTypes(sessionToken, searchCriteria, fetchOptions);

        List<DataSetType> types = searchResult.getObjects();
        List<String> codes = extractCodes(types);
        Collections.sort(codes);
        assertEquals(codes.toString(), "[CONTAINER_TYPE, DELETION_TEST, DELETION_TEST_CONTAINER, VALIDATED_CONTAINER_TYPE]");
        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchAllWithVocabularies()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        DataSetTypeSearchCriteria searchCriteria = new DataSetTypeSearchCriteria();
        DataSetTypeFetchOptions fetchOptions = new DataSetTypeFetchOptions();
        PropertyAssignmentFetchOptions assignmentFetchOptions = fetchOptions.withPropertyAssignments();
        assignmentFetchOptions.sortBy().label().desc();
        assignmentFetchOptions.withPropertyType().withVocabulary();

        SearchResult<DataSetType> searchResult = v3api.searchDataSetTypes(sessionToken, searchCriteria, fetchOptions);

        List<DataSetType> types = searchResult.getObjects();
        List<String> codes = extractCodes(types);
        Collections.sort(codes);
        assertEquals(codes.toString(), "[CONTAINER_TYPE, DELETION_TEST, DELETION_TEST_CONTAINER, HCS_IMAGE, "
                + "HCS_IMAGE_ANALYSIS_DATA, IMAGING_DATA, LINK_TYPE, REQUIRES_EXPERIMENT, UNKNOWN, VALIDATED_CONTAINER_TYPE, "
                + "VALIDATED_IMPOSSIBLE_TO_UPDATE_TYPE, VALIDATED_NORMAL_TYPE]");
        assertEquals(types.get(0).getFetchOptions().hasPropertyAssignments(), true);
        assertEquals(getDataSetTypePropertyTypeInfo(types).toString(), "[CONTAINER_TYPE:, "
                + "DELETION_TEST: BACTERIUM[MATERIAL] ORGANISM[CONTROLLEDVOCABULARY:ORGANISM] DESCRIPTION[VARCHAR], "
                + "DELETION_TEST_CONTAINER: BACTERIUM[MATERIAL] ORGANISM[CONTROLLEDVOCABULARY:ORGANISM] DESCRIPTION[VARCHAR], "
                + "HCS_IMAGE: BACTERIUM[MATERIAL] ANY_MATERIAL[MATERIAL] GENDER[CONTROLLEDVOCABULARY:GENDER] COMMENT[VARCHAR], "
                + "HCS_IMAGE_ANALYSIS_DATA:, IMAGING_DATA: $IMAGING_DATA_CONFIG[JSON], LINK_TYPE:, REQUIRES_EXPERIMENT:, UNKNOWN:, "
                + "VALIDATED_CONTAINER_TYPE:, VALIDATED_IMPOSSIBLE_TO_UPDATE_TYPE:, VALIDATED_NORMAL_TYPE:]");
        List<String> vocabularyCodes = new ArrayList<String>();
        for (DataSetType type : types)
        {
            vocabularyCodes.addAll(extractVocabularyCodes(type.getPropertyAssignments()));
        }
        Collections.sort(vocabularyCodes);
        assertEquals(vocabularyCodes.toString(), "[GENDER, ORGANISM, ORGANISM]");
        v3api.logout(sessionToken);
    }

    protected List<String> getDataSetTypePropertyTypeInfo(List<DataSetType> types)
    {
        List<String> infos = new ArrayList<>();
        for (DataSetType type : types)
        {
            List<PropertyAssignment> assignments = type.getPropertyAssignments();
            String info = type.getCode() + ":";
            for (PropertyAssignment assignment : assignments)
            {
                PropertyType propertyType = assignment.getPropertyType();
                info += " " + propertyType.getCode() + "[" + propertyType.getDataType();
                if (propertyType.getFetchOptions().hasVocabulary())
                {
                    Vocabulary vocabulary = propertyType.getVocabulary();
                    if (vocabulary != null)
                    {
                        info += ":" + vocabulary.getCode();
                    }
                } else if (propertyType.getDataType() == DataType.CONTROLLEDVOCABULARY)
                {
                    info += ":?";
                }
                info += "]";
            }
            infos.add(info);
        }
        Collections.sort(infos);
        return infos;
    }

    @Test
    public void testSearchExactCode()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        DataSetTypeSearchCriteria searchCriteria = new DataSetTypeSearchCriteria();
        searchCriteria.withCode().thatEquals("HCS_IMAGE");
        DataSetTypeFetchOptions fetchOptions = new DataSetTypeFetchOptions();
        fetchOptions.withPropertyAssignments();
        SearchResult<DataSetType> searchResult = v3api.searchDataSetTypes(sessionToken, searchCriteria, fetchOptions);

        List<DataSetType> types = searchResult.getObjects();
        List<String> codes = extractCodes(types);
        Collections.sort(codes);
        assertEquals(codes.toString(), "[HCS_IMAGE]");
        assertEquals(types.get(0).getFetchOptions().hasPropertyAssignments(), true);
        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithCodeThatStartsWithD()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        DataSetTypeSearchCriteria searchCriteria = new DataSetTypeSearchCriteria();
        searchCriteria.withCode().thatStartsWith("D");
        DataSetTypeFetchOptions fetchOptions = new DataSetTypeFetchOptions();

        SearchResult<DataSetType> searchResult = v3api.searchDataSetTypes(sessionToken, searchCriteria, fetchOptions);

        List<DataSetType> types = searchResult.getObjects();
        List<String> codes = extractCodes(types);
        Collections.sort(codes);
        assertEquals(codes.toString(), "[DELETION_TEST, DELETION_TEST_CONTAINER]");
        assertEquals(types.get(0).getFetchOptions().hasPropertyAssignments(), false);
        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithPropertyAssignmentSortByCodeDesc()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        DataSetTypeSearchCriteria searchCriteria = new DataSetTypeSearchCriteria();
        searchCriteria.withCode().thatStartsWith("D");
        DataSetTypeFetchOptions fetchOptions = new DataSetTypeFetchOptions();
        fetchOptions.withPropertyAssignments().sortBy().code().desc();

        SearchResult<DataSetType> searchResult = v3api.searchDataSetTypes(sessionToken, searchCriteria, fetchOptions);

        List<DataSetType> types = searchResult.getObjects();
        Collections.sort(types, new CodeComparator<DataSetType>());
        List<String> codes = extractCodes(types);
        assertEquals(codes.toString(), "[DELETION_TEST, DELETION_TEST_CONTAINER]");
        assertEquals(types.get(0).getFetchOptions().hasPropertyAssignments(), true);
        List<PropertyAssignment> propertyAssignments = types.get(0).getPropertyAssignments();
        assertOrder(propertyAssignments, "ORGANISM", "DESCRIPTION", "BACTERIUM");
        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithPropertyAssignments()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final DataSetTypeSearchCriteria searchCriteria = new DataSetTypeSearchCriteria();
        final Set<String> requiredPropertyTypeCodes = Set.of("GENDER", "COMMENT");
        searchCriteria.withPropertyAssignments().withPropertyType().withCodes().thatIn(requiredPropertyTypeCodes);

        final DataSetTypeFetchOptions fetchOptions = new DataSetTypeFetchOptions();
        fetchOptions.withPropertyAssignments().sortBy().code().desc();

        final SearchResult<DataSetType> searchResult = v3api.searchDataSetTypes(sessionToken, searchCriteria, fetchOptions);
        final List<DataSetType> dataSetTypes = searchResult.getObjects();

        assertTrue(dataSetTypes.get(0).getFetchOptions().hasPropertyAssignments());

        for (final DataSetType dataSetType : dataSetTypes)
        {
            final Set<String> propertyTypeCodes = dataSetType.getPropertyAssignments().stream()
                    .map(propertyAssignment -> propertyAssignment.getPropertyType().getCode())
                    .collect(Collectors.toSet());
            final Set<String> originalPropertyTypeCodes = new HashSet<>(propertyTypeCodes);
            propertyTypeCodes.retainAll(requiredPropertyTypeCodes);
            assertFalse(propertyTypeCodes.isEmpty(),
                    String.format("DataSet type %s contains assignments to property types %s which do not have any of the required ones %s.",
                            dataSetType, originalPropertyTypeCodes, requiredPropertyTypeCodes));
        }

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithValidationPlugin()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        DataSetTypeSearchCriteria searchCriteria = new DataSetTypeSearchCriteria();
        searchCriteria.withCode().thatEquals("VALIDATED_NORMAL_TYPE");
        DataSetTypeFetchOptions fetchOptions = new DataSetTypeFetchOptions();
        fetchOptions.withValidationPlugin();

        // When
        DataSetType type = v3api.searchDataSetTypes(sessionToken, searchCriteria, fetchOptions).getObjects().get(0);

        // Then
        assertEquals(type.getFetchOptions().hasValidationPlugin(), true);
        assertEquals(type.getValidationPlugin().getFetchOptions().hasScript(), false);
        assertEquals(type.getValidationPlugin().getName(), "test");

        v3api.logout(sessionToken);
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetTypeSearchCriteria c = new DataSetTypeSearchCriteria();
        c.withCode().thatEquals("HCS_IMAGE");

        DataSetTypeFetchOptions fo = new DataSetTypeFetchOptions();
        fo.withPropertyAssignments();

        v3api.searchDataSetTypes(sessionToken, c, fo);

        assertAccessLog(
                "search-data-set-types  SEARCH_CRITERIA:\n'DATA_SET_TYPE\n    with attribute 'code' equal to 'HCS_IMAGE'\n'\nFETCH_OPTIONS:\n'DataSetType\n    with PropertyAssignments\n'");
    }

}

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

package ch.ethz.sis.openbis.generic.dss.systemtest.api.v3;

import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.id.DataStorePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.SearchDomainService;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.SearchDomainServiceExecutionResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.SearchDomainServiceSearchOption;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.SearchDomainServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.fetchoptions.SearchDomainServiceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.id.DssServicePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.search.SearchDomainServiceSearchCriteria;

/**
 * @author Franz-Josef Elmer
 */
public class SearchDomainServiceTest extends AbstractFileTest
{
    @Test
    public void testSearchSearchDomainServiceAll()
    {
        // Given
        String sessionToken = as.login(TEST_USER, PASSWORD);
        SearchDomainServiceSearchCriteria searchCriteria = new SearchDomainServiceSearchCriteria();
        SearchDomainServiceFetchOptions fetchOptions = new SearchDomainServiceFetchOptions();

        // When
        List<SearchDomainService> result = as.searchSearchDomainServices(sessionToken, searchCriteria, fetchOptions).getObjects();
        
        // Then
        assertEquals(2, result.size());
        
        as.logout(sessionToken);
    }
    
    @Test
    public void testSearchSearchDomainServiceById()
    {
        // Given
        String sessionToken = as.login(TEST_USER, PASSWORD);
        SearchDomainServiceSearchCriteria searchCriteria = new SearchDomainServiceSearchCriteria();
        DssServicePermId id = new DssServicePermId("b", new DataStorePermId("STANDARD"));
        searchCriteria.withId().thatEquals(id);
        SearchDomainServiceFetchOptions fetchOptions = new SearchDomainServiceFetchOptions();
        
        // When
        List<SearchDomainService> result = as.searchSearchDomainServices(sessionToken, searchCriteria, fetchOptions).getObjects();
        
        // Then
        SearchDomainService searchDomainService = result.get(0);
        assertEquals("optionsKey", searchDomainService.getPossibleSearchOptionsKey());
        List<SearchDomainServiceSearchOption> possibleSearchOptions = searchDomainService.getPossibleSearchOptions();
        assertEquals("[Alpha [alpha], beta [beta]]", possibleSearchOptions.toString());
        assertEquals("[alpha:Alpha:, beta:beta:I'm Beta]", possibleSearchOptions.stream()
                .map(o -> o.getCode() + ":" + o.getLabel() + ":" + o.getDescription()).collect(Collectors.toList()).toString());
        assertEquals(id, searchDomainService.getPermId());
        assertEquals("b", searchDomainService.getName());
        assertEquals("Search Domain B", searchDomainService.getLabel());
        assertEquals(fetchOptions.toString(), searchDomainService.getFetchOptions().toString());
        assertEquals(1, result.size());
        
        as.logout(sessionToken);
    }
    
    @Test
    public void testSearchSearchDomainServiceByName()
    {
        // Given
        String sessionToken = as.login(TEST_USER, PASSWORD);
        SearchDomainServiceSearchCriteria searchCriteria = new SearchDomainServiceSearchCriteria();
        searchCriteria.withName().thatContains("c");
        SearchDomainServiceFetchOptions fetchOptions = new SearchDomainServiceFetchOptions();
        
        // When
        List<SearchDomainService> result = as.searchSearchDomainServices(sessionToken, searchCriteria, fetchOptions).getObjects();
        
        // Then
        SearchDomainService searchDomainService = result.get(0);
        assertEquals(null, searchDomainService.getPossibleSearchOptionsKey());
        List<SearchDomainServiceSearchOption> possibleSearchOptions = searchDomainService.getPossibleSearchOptions();
        assertEquals("[]", possibleSearchOptions.toString());
        assertEquals("c", searchDomainService.getPermId().getPermId());
        assertEquals("STANDARD", searchDomainService.getPermId().getDataStoreId().toString());
        assertEquals("c", searchDomainService.getName());
        assertEquals("Search Domain C", searchDomainService.getLabel());
        assertEquals(fetchOptions.toString(), searchDomainService.getFetchOptions().toString());
        assertEquals(1, result.size());
        
        as.logout(sessionToken);
    }
    
    @Test
    public void testExecuteSearchDomainServiceFirstAvailable()
    {
        // Given
        String sessionToken = as.login(TEST_USER, PASSWORD);
        SearchDomainServiceExecutionOptions options = new SearchDomainServiceExecutionOptions().withSearchString("key")
                .withParameter("key", "{\"searchDomain\" : \"Echo database\","
                        + "\"pathInDataSet\": \"a/path/to/a/file\","
                        + "\"alignmentMatchSequenceStart\": 3,"
                        + "\"alignmentMatchSequenceEnd\": 20,"
                        + "\"alignmentMatchQueryStart\": 4,"
                        + "\"alignmentMatchQueryEnd\": 6,"
                        + "\"alignmentMatchMismatches\": 2,"
                        + "\"alignmentMatchGaps\": 1,"
                        + "\"positionInSequence\": 42,"
                        + "\"sequenceIdentifier\": \"seq123\","
                        + "\"dataSetCode\": \"COMPONENT_3A\"}");

        // When
        List<SearchDomainServiceExecutionResult> results = as.executeSearchDomainService(sessionToken, options).getObjects();

        // Then
        SearchDomainServiceExecutionResult result = results.get(0);
        assertEquals("STANDARD:b", result.getServicePermId().toString());
        assertEquals("b", result.getSearchDomainName());
        assertEquals("Search Domain B", result.getSearchDomainLabel());
        assertEquals(EntityKind.DATA_SET, result.getEntityKind());
        assertEquals("HCS_IMAGE", result.getEntityType());
        assertEquals("COMPONENT_3A", result.getEntityIdentifier());
        assertEquals("COMPONENT_3A", result.getEntityPermId());
        assertEquals("{identifier=seq123, number_of_mismatches=2, path_in_data_set=a/path/to/a/file, position=42, "
                + "query_end=6, query_start=4, sequence_end=20, sequence_start=3, total_number_of_gaps=1}",
                new TreeMap<>(result.getResultDetails()).toString());
        assertEquals(1, results.size());

        as.logout(sessionToken);
    }

    @Test
    public void testExecuteSearchDomainServiceWithPreferredSearchDomain()
    {
        // Given
        String sessionToken = as.login(TEST_USER, PASSWORD);
        SearchDomainServiceExecutionOptions options = new SearchDomainServiceExecutionOptions();
        options.withPreferredSearchDomain("c");
        options.withSearchString("key");
        options.withParameter("key", "{\"searchDomain\" : \"Echo database\","
                + "\"permId\": \"200902091250077-1060\","
                + "\"entityKind\": \"SAMPLE\","
                + "\"propertyType\": \"PLATE_GEO\"}");

        // When
        List<SearchDomainServiceExecutionResult> results = as.executeSearchDomainService(sessionToken, options).getObjects();

        // Then
        SearchDomainServiceExecutionResult result = results.get(0);
        assertEquals("STANDARD:c", result.getServicePermId().toString());
        assertEquals("Search Domain C", result.getSearchDomainLabel());
        assertEquals(EntityKind.SAMPLE, result.getEntityKind());
        assertEquals("CELL_PLATE", result.getEntityType());
        assertEquals("/TEST-SPACE/NOE/CP-TEST-4", result.getEntityIdentifier());
        assertEquals("200902091250077-1060", result.getEntityPermId());
        assertEquals("{position=0, property_type=PLATE_GEO}", new TreeMap<>(result.getResultDetails()).toString());
        assertEquals(1, results.size());

        as.logout(sessionToken);
    }
}

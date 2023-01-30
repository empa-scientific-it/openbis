/*
 * Copyright 2015 ETH Zuerich, SIS
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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.CustomASService;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.fetchoptions.CustomASServiceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.search.CustomASServiceSearchCriteria;

/**
 * @author Franz-Josef Elmer
 */
public class SearchServiceTest extends AbstractTest
{

    @Test
    public void testSearchServices()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        CustomASServiceSearchCriteria searchCriteria = new CustomASServiceSearchCriteria();
        searchCriteria.withCode().thatEquals("simple-service");

        SearchResult<CustomASService> result = v3api.searchCustomASServices(sessionToken, searchCriteria,
                new CustomASServiceFetchOptions());

        assertEquals(result.getTotalCount(), 1);
    }

    @Test
    public void testSearchServicesWithCodes()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        CustomASServiceSearchCriteria searchCriteria = new CustomASServiceSearchCriteria();
        searchCriteria.withCodes().thatIn(Arrays.asList("simple-service"));
        
        SearchResult<CustomASService> result = v3api.searchCustomASServices(sessionToken, searchCriteria,
                new CustomASServiceFetchOptions());
        
        assertEquals(result.getTotalCount(), 1);
    }
    
    @Test
    public void testSearchAllServicesSortedPage2()
    {
        CustomASServiceFetchOptions fetchOptions = new CustomASServiceFetchOptions();
        fetchOptions.from(2).count(1).sortBy();
        SearchResult<CustomASService> result = v3api.searchCustomASServices(systemSessionToken,
                new CustomASServiceSearchCriteria(),
                fetchOptions);

        assertEquals(result.getObjects().toString(), "[CustomASService: service3]");
        assertEquals(result.getTotalCount(), 4);
    }

    @Test
    public void testSearchServiceByCode()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        CustomASServiceSearchCriteria searchCriteria = new CustomASServiceSearchCriteria();
        searchCriteria.withCode().thatStartsWith("simple");

        SearchResult<CustomASService> result = v3api.searchCustomASServices(sessionToken, searchCriteria, new CustomASServiceFetchOptions());

        assertEquals(result.getObjects().toString(), "[CustomASService: simple-service]");
        assertEquals(result.getTotalCount(), 1);
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        CustomASServiceSearchCriteria c = new CustomASServiceSearchCriteria();
        c.withCode().thatEquals("simple-service");

        CustomASServiceFetchOptions fo = new CustomASServiceFetchOptions();

        v3api.searchCustomASServices(sessionToken, c, fo);

        assertAccessLog(
                "search-custom-as-services  SEARCH_CRITERIA:\n'CUSTOM_AS_SERVICE\n    with attribute 'code' equal to 'simple-service'\n'\nFETCH_OPTIONS:\n'CustomASService\n'");
    }

}
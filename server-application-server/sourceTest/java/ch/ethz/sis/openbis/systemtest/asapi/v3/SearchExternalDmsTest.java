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

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.ExternalDms;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.ExternalDmsAddressType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.fetchoptions.ExternalDmsFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.search.ExternalDmsSearchCriteria;

public class SearchExternalDmsTest extends AbstractExternalDmsTest
{

    @Test
    public void searchReturnsAllExternalDataManagementSystems()
    {
        ExternalDms edms1 = get(create(externalDms()));
        ExternalDms edms2 = get(create(externalDms()));

        ExternalDmsSearchCriteria criteria = new ExternalDmsSearchCriteria();
        ExternalDmsFetchOptions fetchOptions = new ExternalDmsFetchOptions();
        SearchResult<ExternalDms> result = v3api.searchExternalDataManagementSystems(session, criteria, fetchOptions);
        assertThat(result.getObjects(), hasItem(isSimilarTo(edms1)));
        assertThat(result.getObjects(), hasItem(isSimilarTo(edms2)));
    }

    @Test
    public void searchReturnsSpecifiedExternalDataManagementSystem()
    {
        get(create(externalDms()));
        ExternalDms edms2 = get(create(externalDms()));

        ExternalDmsSearchCriteria criteria = new ExternalDmsSearchCriteria();
        criteria.withCode().thatEquals(edms2.getCode());
        ExternalDmsFetchOptions fetchOptions = new ExternalDmsFetchOptions();
        List<ExternalDms> result = v3api.searchExternalDataManagementSystems(session, criteria, fetchOptions).getObjects();

        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getCode(), edms2.getCode());
        assertEquals(result.get(0).getAddress(), edms2.getAddress());
        assertEquals(result.get(0).getAddressType(), edms2.getAddressType());
    }

    @Test
    public void searchReturnsExternalDataManagementSystemWithCodes()
    {
        get(create(externalDms()));
        ExternalDms edms2 = get(create(externalDms()));
        
        ExternalDmsSearchCriteria criteria = new ExternalDmsSearchCriteria();
        criteria.withCodes().thatIn(Arrays.asList(edms2.getCode()));
        ExternalDmsFetchOptions fetchOptions = new ExternalDmsFetchOptions();
        List<ExternalDms> result = v3api.searchExternalDataManagementSystems(session, criteria, fetchOptions).getObjects();
        
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getCode(), edms2.getCode());
        assertEquals(result.get(0).getAddress(), edms2.getAddress());
        assertEquals(result.get(0).getAddressType(), edms2.getAddressType());
    }
    
    @Test
    public void canSearchWithLabel()
    {
        ExternalDms edms = get(create(externalDms().withLabel("this is a label")));
        create(externalDms().withLabel("something else"));

        ExternalDmsSearchCriteria criteria = new ExternalDmsSearchCriteria();
        criteria.withLabel().thatEndsWith("is a label");
        ExternalDmsFetchOptions fetchOptions = new ExternalDmsFetchOptions();
        List<ExternalDms> result = v3api.searchExternalDataManagementSystems(session, criteria, fetchOptions).getObjects();

        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getCode(), edms.getCode());
        assertEquals(result.get(0).getAddress(), edms.getAddress());
        assertEquals(result.get(0).getAddressType(), edms.getAddressType());
    }

    @Test
    public void canSearchWithAddress()
    {
        ExternalDms edms = get(create(externalDms().withLabel("this is an address")));
        create(externalDms().withAddress("something else"));

        ExternalDmsSearchCriteria criteria = new ExternalDmsSearchCriteria();
        criteria.withLabel().thatStartsWith("this is a");
        ExternalDmsFetchOptions fetchOptions = new ExternalDmsFetchOptions();
        List<ExternalDms> result = v3api.searchExternalDataManagementSystems(session, criteria, fetchOptions).getObjects();

        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getCode(), edms.getCode());
        assertEquals(result.get(0).getAddress(), edms.getAddress());
        assertEquals(result.get(0).getAddressType(), edms.getAddressType());
    }

    @Test
    public void canSearchWithDmsType()
    {
        ExternalDms edms = get(create(externalDms().withType(ExternalDmsAddressType.FILE_SYSTEM)));
        create(externalDms().withType(ExternalDmsAddressType.OPENBIS));

        ExternalDmsSearchCriteria criteria = new ExternalDmsSearchCriteria();
        criteria.withType().thatEquals(ExternalDmsAddressType.FILE_SYSTEM);
        ExternalDmsFetchOptions fetchOptions = new ExternalDmsFetchOptions();
        SearchResult<ExternalDms> result = v3api.searchExternalDataManagementSystems(session, criteria, fetchOptions);

        assertThat(result.getObjects(), hasItem(isSimilarTo(edms)));
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExternalDmsSearchCriteria c = new ExternalDmsSearchCriteria();
        c.withCode().thatEquals("DMS_1");

        ExternalDmsFetchOptions fo = new ExternalDmsFetchOptions();

        v3api.searchExternalDataManagementSystems(sessionToken, c, fo);

        assertAccessLog(
                "search-external-dms  SEARCH_CRITERIA:\n'EXTERNAL_DMS\n    with attribute 'code' equal to 'DMS_1'\n'\nFETCH_OPTIONS:\n'ExternalDms\n'");
    }

}

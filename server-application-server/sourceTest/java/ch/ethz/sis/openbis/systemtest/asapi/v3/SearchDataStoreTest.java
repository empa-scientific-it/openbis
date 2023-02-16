/*
 * Copyright ETH 2017 - 2023 Zürich, Scientific IT Services
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

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.DataStore;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.fetchoptions.DataStoreFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.id.DataStorePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.search.DataStoreSearchCriteria;

/**
 * @author pkupczyk
 */
public class SearchDataStoreTest extends AbstractTest
{

    @Test
    public void testSearchWithEmptyCriteria()
    {
        testSearch(TEST_USER, new DataStoreSearchCriteria(), "STANDARD");
    }

    @Test
    public void testSearchWithIdSetToPermId()
    {
        DataStoreSearchCriteria criteria = new DataStoreSearchCriteria();
        criteria.withId().thatEquals(new DataStorePermId("STANDARD"));
        testSearch(TEST_USER, criteria, "STANDARD");

        DataStoreSearchCriteria criteriaDifferentCase = new DataStoreSearchCriteria();
        criteriaDifferentCase.withId().thatEquals(new DataStorePermId("standard"));
        testSearch(TEST_USER, criteriaDifferentCase, "STANDARD");
    }

    @Test
    public void testSearchWithIdSetToNonexistentPermId()
    {
        DataStoreSearchCriteria criteria = new DataStoreSearchCriteria();
        criteria.withId().thatEquals(new DataStorePermId("IDONTEXIST"));
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithPermIdThatEquals()
    {
        DataStoreSearchCriteria criteria = new DataStoreSearchCriteria();
        criteria.withPermId().thatEquals("STANDARD");
        testSearch(TEST_USER, criteria, "STANDARD");

        DataStoreSearchCriteria criteriaDifferentCase = new DataStoreSearchCriteria();
        criteriaDifferentCase.withPermId().thatEquals("standARD");
        testSearch(TEST_USER, criteriaDifferentCase, "STANDARD");

        DataStoreSearchCriteria criteriaNonMatching = new DataStoreSearchCriteria();
        criteriaNonMatching.withPermId().thatEquals("ANDAR");
        testSearch(TEST_USER, criteriaNonMatching);
    }

    @Test
    public void testSearchWithPermIdThatContains()
    {
        DataStoreSearchCriteria criteria = new DataStoreSearchCriteria();
        criteria.withPermId().thatContains("ANDAR");
        testSearch(TEST_USER, criteria, "STANDARD");

        DataStoreSearchCriteria criteriaDifferentCase = new DataStoreSearchCriteria();
        criteriaDifferentCase.withPermId().thatContains("andar");
        testSearch(TEST_USER, criteriaDifferentCase, "STANDARD");

        DataStoreSearchCriteria criteriaNonMatching = new DataStoreSearchCriteria();
        criteriaNonMatching.withPermId().thatContains("ABCDE");
        testSearch(TEST_USER, criteriaNonMatching);
    }

    @Test
    public void testSearchWithPermIdThatStartsWith()
    {
        DataStoreSearchCriteria criteria = new DataStoreSearchCriteria();
        criteria.withPermId().thatStartsWith("STA");
        testSearch(TEST_USER, criteria, "STANDARD");

        DataStoreSearchCriteria criteriaDifferentCase = new DataStoreSearchCriteria();
        criteriaDifferentCase.withPermId().thatStartsWith("sta");
        testSearch(TEST_USER, criteriaDifferentCase, "STANDARD");

        DataStoreSearchCriteria criteriaNonMatching = new DataStoreSearchCriteria();
        criteriaNonMatching.withPermId().thatStartsWith("ANDAR");
        testSearch(TEST_USER, criteriaNonMatching);
    }

    @Test
    public void testSearchWithPermIdThatEndsWith()
    {
        DataStoreSearchCriteria criteria = new DataStoreSearchCriteria();
        criteria.withPermId().thatEndsWith("ARD");
        testSearch(TEST_USER, criteria, "STANDARD");

        DataStoreSearchCriteria criteriaDifferentCase = new DataStoreSearchCriteria();
        criteriaDifferentCase.withPermId().thatEndsWith("ard");
        testSearch(TEST_USER, criteriaDifferentCase, "STANDARD");

        DataStoreSearchCriteria criteriaNonMatching = new DataStoreSearchCriteria();
        criteriaNonMatching.withPermId().thatEndsWith("ANDAR");
        testSearch(TEST_USER, criteriaNonMatching);
    }

    @Test
    public void testSearchWithCodeThatEquals()
    {
        DataStoreSearchCriteria criteria = new DataStoreSearchCriteria();
        criteria.withCode().thatEquals("STANDARD");
        testSearch(TEST_USER, criteria, "STANDARD");

        DataStoreSearchCriteria criteriaDifferentCase = new DataStoreSearchCriteria();
        criteriaDifferentCase.withCode().thatEquals("standARD");
        testSearch(TEST_USER, criteriaDifferentCase, "STANDARD");

        DataStoreSearchCriteria criteriaNonMatching = new DataStoreSearchCriteria();
        criteriaNonMatching.withCode().thatEquals("ANDAR");
        testSearch(TEST_USER, criteriaNonMatching);
    }
    
    @Test
    public void testSearchWithCodes()
    {
        DataStoreSearchCriteria criteria = new DataStoreSearchCriteria();
        criteria.withCodes().thatIn(Arrays.asList("STANDARD", "BLABLA"));
        testSearch(TEST_USER, criteria, "STANDARD");
    }

    @Test
    public void testSearchWithCodeThatContains()
    {
        DataStoreSearchCriteria criteria = new DataStoreSearchCriteria();
        criteria.withCode().thatContains("ANDAR");
        testSearch(TEST_USER, criteria, "STANDARD");

        DataStoreSearchCriteria criteriaDifferentCase = new DataStoreSearchCriteria();
        criteriaDifferentCase.withCode().thatContains("andar");
        testSearch(TEST_USER, criteriaDifferentCase, "STANDARD");

        DataStoreSearchCriteria criteriaNonMatching = new DataStoreSearchCriteria();
        criteriaNonMatching.withCode().thatContains("ABCDE");
        testSearch(TEST_USER, criteriaNonMatching);
    }

    @Test
    public void testSearchWithCodeThatStartsWith()
    {
        DataStoreSearchCriteria criteria = new DataStoreSearchCriteria();
        criteria.withCode().thatStartsWith("STA");
        testSearch(TEST_USER, criteria, "STANDARD");

        DataStoreSearchCriteria criteriaDifferentCase = new DataStoreSearchCriteria();
        criteriaDifferentCase.withCode().thatStartsWith("sta");
        testSearch(TEST_USER, criteriaDifferentCase, "STANDARD");

        DataStoreSearchCriteria criteriaNonMatching = new DataStoreSearchCriteria();
        criteriaNonMatching.withCode().thatStartsWith("ANDAR");
        testSearch(TEST_USER, criteriaNonMatching);
    }

    @Test
    public void testSearchWithCodeThatEndsWith()
    {
        DataStoreSearchCriteria criteria = new DataStoreSearchCriteria();
        criteria.withCode().thatEndsWith("ARD");
        testSearch(TEST_USER, criteria, "STANDARD");

        DataStoreSearchCriteria criteriaDifferentCase = new DataStoreSearchCriteria();
        criteriaDifferentCase.withCode().thatEndsWith("ard");
        testSearch(TEST_USER, criteriaDifferentCase, "STANDARD");

        DataStoreSearchCriteria criteriaNonMatching = new DataStoreSearchCriteria();
        criteriaNonMatching.withCode().thatEndsWith("ANDAR");
        testSearch(TEST_USER, criteriaNonMatching);
    }

    @Test
    public void testSearchWithAndOperator()
    {
        DataStoreSearchCriteria criteria = new DataStoreSearchCriteria();
        criteria.withAndOperator();
        criteria.withCode().thatContains("STAND");
        criteria.withCode().thatContains("ARD");
        testSearch(TEST_USER, criteria, "STANDARD");

        DataStoreSearchCriteria criteriaNonMatching = new DataStoreSearchCriteria();
        criteriaNonMatching.withAndOperator();
        criteriaNonMatching.withCode().thatContains("STAND");
        criteriaNonMatching.withCode().thatContains("ABC");
        testSearch(TEST_USER, criteriaNonMatching);
    }

    @Test
    public void testSearchWithOrOperator()
    {
        DataStoreSearchCriteria criteria = new DataStoreSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("STANDARD");
        criteria.withPermId().thatEquals("ABC");
        testSearch(TEST_USER, criteria, "STANDARD");

        DataStoreSearchCriteria criteriaNonMatching = new DataStoreSearchCriteria();
        criteriaNonMatching.withOrOperator();
        criteriaNonMatching.withPermId().thatEquals("ABC");
        criteriaNonMatching.withPermId().thatEquals("DEF");
        testSearch(TEST_USER, criteriaNonMatching);
    }
    
    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataStoreSearchCriteria c = new DataStoreSearchCriteria();
        c.withCode().thatEquals("STANDARD");
        
        DataStoreFetchOptions fo = new DataStoreFetchOptions();

        v3api.searchDataStores(sessionToken, c, fo);

        assertAccessLog(
                "search-data-stores  SEARCH_CRITERIA:\n'DATA_STORE\n    with attribute 'code' equal to 'STANDARD'\n'\nFETCH_OPTIONS:\n'DataStore\n'");
    }

    private void testSearch(String user, DataStoreSearchCriteria criteria, String... expectedCodes)
    {
        String sessionToken = v3api.login(user, PASSWORD);

        SearchResult<DataStore> searchResult =
                v3api.searchDataStores(sessionToken, criteria, new DataStoreFetchOptions());
        List<DataStore> dataStores = searchResult.getObjects();

        assertDataStoreCodes(dataStores, expectedCodes);
        v3api.logout(sessionToken);
    }

}

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

import org.testng.Assert;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search.PropertyTypeSearchCriteria;

/**
 * @author pkupczyk
 */
public class SearchPropertyTypeTest extends AbstractTest
{

    @Test
    public void testSearchWithEmptyCriteria()
    {
        testSearch(TEST_SPACE_USER, new PropertyTypeSearchCriteria(), 19);
    }

    @Test
    public void testSearchWithId()
    {
        PropertyTypeSearchCriteria criteria = new PropertyTypeSearchCriteria();
        criteria.withId().thatEquals(new PropertyTypePermId("OFFSET"));
        testSearch(TEST_USER, criteria, "OFFSET");
    }

    @Test
    public void testSearchWithIds()
    {
        PropertyTypeSearchCriteria criteria = new PropertyTypeSearchCriteria();
        criteria.withIds().thatIn(Arrays.asList(new PropertyTypePermId("OFFSET"), new PropertyTypePermId("VOLUME")));
        testSearch(TEST_USER, criteria, "OFFSET", "VOLUME");
    }

    @Test
    public void testSearchWithIdNonexistent()
    {
        PropertyTypeSearchCriteria criteria = new PropertyTypeSearchCriteria();
        criteria.withId().thatEquals(new PropertyTypePermId("IDONTEXIST"));
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithIdsNonexistent()
    {
        PropertyTypeSearchCriteria criteria = new PropertyTypeSearchCriteria();
        criteria.withIds().thatIn(Arrays.asList(new PropertyTypePermId("OFFSET"), new PropertyTypePermId("IDONTEXIST")));
        testSearch(TEST_USER, criteria, "OFFSET");
    }

    @Test
    public void testSearchWithCode()
    {
        PropertyTypeSearchCriteria criteria = new PropertyTypeSearchCriteria();
        criteria.withCode().thatEquals("OFFSET");
        testSearch(TEST_USER, criteria, "OFFSET");
    }

    @Test
    public void testSearchWithCodes()
    {
        PropertyTypeSearchCriteria criteria = new PropertyTypeSearchCriteria();
        criteria.withCodes().thatIn(Arrays.asList("OFFSET", "VOLUME"));
        testSearch(TEST_USER, criteria, "OFFSET", "VOLUME");
    }

    @Test
    public void testSearchWithSemanticAnnotations()
    {
        PropertyTypeSearchCriteria criteria = new PropertyTypeSearchCriteria();
        criteria.withSemanticAnnotations();
        testSearch(TEST_USER, criteria, "DESCRIPTION", "GENE_SYMBOL", "ORGANISM");

        criteria = new PropertyTypeSearchCriteria();
        criteria.withSemanticAnnotations().withPermId().thatEquals("PT_GENE_SYMBOL");
        testSearch(TEST_USER, criteria, "GENE_SYMBOL");
    }

    @Test
    public void testSearchWithAndOperator()
    {
        PropertyTypeSearchCriteria criteria = new PropertyTypeSearchCriteria();
        criteria.withAndOperator();
        criteria.withCode().thatContains("DYNAMIC");
        criteria.withCode().thatContains("DESCRIPTION");
        testSearch(TEST_USER, criteria, "DYNAMIC_DESCRIPTION");
    }

    @Test
    public void testSearchWithOrOperator()
    {
        PropertyTypeSearchCriteria criteria = new PropertyTypeSearchCriteria();
        criteria.withOrOperator();
        criteria.withCode().thatContains("DYNAMIC");
        criteria.withCode().thatContains("DESCRIPTION");
        testSearch(TEST_USER, criteria, "DESCRIPTION", "DYNAMIC_DESCRIPTION");
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PropertyTypeSearchCriteria c = new PropertyTypeSearchCriteria();
        c.withCode().thatEquals("OFFSET");

        PropertyTypeFetchOptions fo = new PropertyTypeFetchOptions();
        fo.withSemanticAnnotations();
        fo.withVocabulary();

        v3api.searchPropertyTypes(sessionToken, c, fo);

        assertAccessLog(
                "search-property-types  SEARCH_CRITERIA:\n'PROPERTY_TYPE\n    with attribute 'code' equal to 'OFFSET'\n'\nFETCH_OPTIONS:\n'PropertyType\n    with Vocabulary\n    with SemanticAnnotations\n'");
    }

    private void testSearch(String user, PropertyTypeSearchCriteria criteria, String... expectedCodes)
    {
        String sessionToken = v3api.login(user, PASSWORD);

        SearchResult<PropertyType> searchResult =
                v3api.searchPropertyTypes(sessionToken, criteria, new PropertyTypeFetchOptions());
        List<PropertyType> propertyTypes = searchResult.getObjects();

        assertPropertyTypeCodes(propertyTypes, expectedCodes);
        v3api.logout(sessionToken);
    }

    private void testSearch(String user, PropertyTypeSearchCriteria criteria, int expectedCount)
    {
        String sessionToken = v3api.login(user, PASSWORD);

        SearchResult<PropertyType> searchResult =
                v3api.searchPropertyTypes(sessionToken, criteria, new PropertyTypeFetchOptions());
        List<PropertyType> propertyTypes = searchResult.getObjects();

        Assert.assertEquals(propertyTypes.size(), expectedCount);
        v3api.logout(sessionToken);
    }

}

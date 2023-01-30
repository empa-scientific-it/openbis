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

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.Tag;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.create.TagCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.fetchoptions.TagFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.search.TagSearchCriteria;
import ch.systemsx.cisd.openbis.systemtest.authorization.ProjectAuthorizationUser;

/**
 * @author pkupczyk
 */
public class SearchTagTest extends AbstractTest
{

    @Test
    public void testSearchWithEmptyCriteria()
    {
        testSearch(TEST_USER, new TagSearchCriteria(), "/test/TEST_METAPROJECTS", "/test/ANOTHER_TEST_METAPROJECTS");
    }

    @Test
    public void testSearchWithIdSetToPermId()
    {
        TagSearchCriteria criteria = new TagSearchCriteria();
        criteria.withId().thatEquals(new TagPermId(TEST_USER, "TEST_METAPROJECTS"));
        testSearch(TEST_USER, criteria, "/test/TEST_METAPROJECTS");
    }

    @Test
    public void testSearchWithIdSetToNonexistentPermId()
    {
        TagSearchCriteria criteria = new TagSearchCriteria();
        criteria.withId().thatEquals(new TagPermId("/test/IDONTEXIST"));
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithPermIdThatEquals()
    {
        TagSearchCriteria criteria = new TagSearchCriteria();
        criteria.withPermId().thatEquals("/test/TEST_METAPROJECTS");
        testSearch(TEST_USER, criteria, "/test/TEST_METAPROJECTS");
    }

    @Test
    public void testSearchWithPermIdThatContains()
    {
        TagSearchCriteria criteria = new TagSearchCriteria();
        criteria.withPermId().thatContains("est/ANOT");
        testSearch(TEST_USER, criteria, "/test/ANOTHER_TEST_METAPROJECTS");
    }

    @Test
    public void testSearchWithPermIdThatStartsWith()
    {
        TagSearchCriteria criteria = new TagSearchCriteria();
        criteria.withPermId().thatStartsWith("/test/TEST");
        testSearch(TEST_USER, criteria, "/test/TEST_METAPROJECTS");
    }

    @Test
    public void testSearchWithPermIdThatEndsWith()
    {
        TagSearchCriteria criteria = new TagSearchCriteria();
        criteria.withPermId().thatEndsWith("role/TEST_METAPROJECTS_2");
        testSearch(TEST_POWER_USER_CISD, criteria, "/test_role/TEST_METAPROJECTS_2");
    }

    @Test
    public void testSearchWithCodeThatEquals()
    {
        TagSearchCriteria criteria = new TagSearchCriteria();
        criteria.withCode().thatEquals("TEST_METAPROJECTS");
        testSearch(TEST_USER, criteria, "/test/TEST_METAPROJECTS");
    }

    @Test
    public void testSearchWithCodes()
    {
        TagSearchCriteria criteria = new TagSearchCriteria();
        criteria.withCodes().thatIn(Arrays.asList("TEST_METAPROJECTS", "ANOTHER_TEST_METAPROJECTS"));
        testSearch(TEST_USER, criteria, "/test/TEST_METAPROJECTS", "/test/ANOTHER_TEST_METAPROJECTS");
    }

    @Test
    public void testSearchWithCodeThatContains()
    {
        TagSearchCriteria criteria = new TagSearchCriteria();
        criteria.withCode().thatContains("NOTHER");
        testSearch(TEST_USER, criteria, "/test/ANOTHER_TEST_METAPROJECTS");
    }

    @Test
    public void testSearchWithCodeThatStartsWith()
    {
        TagSearchCriteria criteria = new TagSearchCriteria();
        criteria.withCode().thatStartsWith("TEST");
        testSearch(TEST_USER, criteria, "/test/TEST_METAPROJECTS");
    }

    @Test
    public void testSearchWithCodeThatEndsWith()
    {
        TagSearchCriteria criteria = new TagSearchCriteria();
        criteria.withCode().thatEndsWith("_2");
        testSearch(TEST_POWER_USER_CISD, criteria, "/test_role/TEST_METAPROJECTS_2");
    }

    @Test
    public void testSearchWithAndOperator()
    {
        TagSearchCriteria criteria = new TagSearchCriteria();
        criteria.withAndOperator();
        criteria.withCode().thatContains("TEST");
        criteria.withCode().thatContains("OTHER");
        testSearch(TEST_USER, criteria, "/test/ANOTHER_TEST_METAPROJECTS");
    }

    @Test
    public void testSearchWithOrOperator()
    {
        TagSearchCriteria criteria = new TagSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatContains("TEST");
        criteria.withPermId().thatContains("OTHER");
        testSearch(TEST_USER, criteria, "/test/TEST_METAPROJECTS", "/test/ANOTHER_TEST_METAPROJECTS");
    }

    @Test
    public void testSearchWithUnauthorized()
    {
        final TagSearchCriteria criteria1 = new TagSearchCriteria();
        criteria1.withPermId().thatEquals("/test/TEST_METAPROJECTS");
        testSearch(TEST_USER, criteria1, "/test/TEST_METAPROJECTS");
        testSearch(TEST_SPACE_USER, criteria1);

        final TagSearchCriteria criteria2 = new TagSearchCriteria();
        criteria2.withCode().thatEquals("TEST_METAPROJECTS");
        testSearch(TEST_USER, criteria2, "/test/TEST_METAPROJECTS");
        testSearch(TEST_SPACE_USER, criteria2, "/test_space/TEST_METAPROJECTS");
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testSearchWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        TagCreation creation = new TagCreation();
        creation.setCode("TAG_TO_SEARCH");

        TagPermId permId = createTag(user.getUserId(), creation);

        TagSearchCriteria criteria = new TagSearchCriteria();
        criteria.withId().thatEquals(permId);

        // Search should succeed for any user.
        testSearch(user.getUserId(), criteria, permId.getPermId());
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        TagSearchCriteria c = new TagSearchCriteria();
        c.withCode().thatEquals("TEST_METAPROJECTS");

        TagFetchOptions fo = new TagFetchOptions();
        fo.withDataSets();
        fo.withExperiments();

        v3api.searchTags(sessionToken, c, fo);

        assertAccessLog(
                "search-tags  SEARCH_CRITERIA:\n'TAG\n    with attribute 'code' equal to 'TEST_METAPROJECTS'\n'\nFETCH_OPTIONS:\n'Tag\n    with Experiments\n    with DataSets\n'");
    }

    private void testSearch(String user, TagSearchCriteria criteria, String... expectedPermIds)
    {
        String sessionToken = v3api.login(user, PASSWORD);

        SearchResult<Tag> searchResult =
                v3api.searchTags(sessionToken, criteria, new TagFetchOptions());
        List<Tag> tags = searchResult.getObjects();

        assertTags(tags, expectedPermIds);
        v3api.logout(sessionToken);
    }

}

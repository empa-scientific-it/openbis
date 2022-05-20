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

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.openbis.systemtest.authorization.ProjectAuthorizationUser;

/**
 * @author pkupczyk
 */
public class SearchSpaceTest extends AbstractTest
{

    @Test
    public void testSearchWithEmptyCriteria()
    {
        testSearch(TEST_SPACE_USER, new SpaceSearchCriteria(), "TEST-SPACE");
    }

    @Test
    public void testSearchWithIdSetToPermId()
    {
        SpaceSearchCriteria criteria = new SpaceSearchCriteria();
        criteria.withId().thatEquals(new SpacePermId("CISD"));
        testSearch(TEST_USER, criteria, "CISD");
    }

    @Test
    public void testSearchWithIdSetToNonexistentPermId()
    {
        SpaceSearchCriteria criteria = new SpaceSearchCriteria();
        criteria.withId().thatEquals(new SpacePermId("IDONTEXIST"));
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithPermIdThatEquals()
    {
        SpaceSearchCriteria criteria = new SpaceSearchCriteria();
        criteria.withPermId().thatEquals("TEST-SPACE");
        testSearch(TEST_USER, criteria, "TEST-SPACE");
    }

    @Test
    public void testSearchWithPermIdThatContains()
    {
        SpaceSearchCriteria criteria = new SpaceSearchCriteria();
        criteria.withPermId().thatContains("ST-SPA");
        testSearch(TEST_USER, criteria, "TEST-SPACE");
    }

    @Test
    public void testSearchWithPermIdThatStartsWith()
    {
        SpaceSearchCriteria criteria = new SpaceSearchCriteria();
        criteria.withPermId().thatStartsWith("CI");
        testSearch(TEST_USER, criteria, "CISD");
    }

    @Test
    public void testSearchWithPermIdThatEndsWith()
    {
        SpaceSearchCriteria criteria = new SpaceSearchCriteria();
        criteria.withPermId().thatEndsWith("ACE");
        testSearch(TEST_USER, criteria, "TEST-SPACE");
    }

    @Test
    public void testSearchWithCodeThatEquals()
    {
        SpaceSearchCriteria criteria = new SpaceSearchCriteria();
        criteria.withCode().thatEquals("TEST-SPACE");
        testSearch(TEST_USER, criteria, "TEST-SPACE");
    }

    @Test
    public void testSearchWithCodes()
    {
        SpaceSearchCriteria criteria = new SpaceSearchCriteria();
        criteria.withCodes().thatIn(Arrays.asList("TEST-SPACE"));
        testSearch(TEST_USER, criteria, "TEST-SPACE");
    }
    
    @Test
    public void testSearchWithCodeThatContains()
    {
        SpaceSearchCriteria criteria = new SpaceSearchCriteria();
        criteria.withCode().thatContains("ST-sPa");
        testSearch(TEST_USER, criteria, "TEST-SPACE");
    }

    @Test
    public void testSearchWithCodeThatStartsWith()
    {
        SpaceSearchCriteria criteria = new SpaceSearchCriteria();
        criteria.withCode().thatStartsWith("ci");
        testSearch(TEST_USER, criteria, "CISD");
    }

    @Test
    public void testSearchWithCodeThatEndsWith()
    {
        SpaceSearchCriteria criteria = new SpaceSearchCriteria();
        criteria.withCode().thatEndsWith("ace");
        testSearch(TEST_USER, criteria, "TEST-SPACE");
    }

    @Test
    public void testSearchWithAndOperator()
    {
        SpaceSearchCriteria criteria = new SpaceSearchCriteria();
        criteria.withAndOperator();
        criteria.withCode().thatContains("TEST");
        criteria.withCode().thatContains("SPACE");
        testSearch(TEST_USER, criteria, "TEST-SPACE");
    }

    @Test
    public void testSearchWithOrOperator()
    {
        SpaceSearchCriteria criteria = new SpaceSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("CISD");
        criteria.withPermId().thatEquals("TEST-SPACE");
        testSearch(TEST_USER, criteria, "CISD", "TEST-SPACE");
    }

    @Test
    public void testSearchWithSpaceUnauthorized()
    {
        SpaceSearchCriteria criteria = new SpaceSearchCriteria();
        criteria.withPermId().thatEquals("CISD");
        testSearch(TEST_USER, criteria, "CISD");
        testSearch(TEST_SPACE_USER, criteria);
    }

    @Test
    public void testSearchWithSortingByCode()
    {
        SpaceSearchCriteria criteria = new SpaceSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(new SpacePermId("CISD"));
        criteria.withId().thatEquals(new SpacePermId("TESTGROUP"));
        criteria.withId().thatEquals(new SpacePermId("TEST-SPACE"));

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SpaceFetchOptions fo = new SpaceFetchOptions();

        fo.sortBy().code().asc();
        List<Space> spaces1 = v3api.searchSpaces(sessionToken, criteria, fo).getObjects();
        assertSpaceCodes(spaces1, "CISD", "TEST-SPACE", "TESTGROUP");

        fo.sortBy().code().desc();
        List<Space> spaces2 = v3api.searchSpaces(sessionToken, criteria, fo).getObjects();
        assertSpaceCodes(spaces2, "TESTGROUP", "TEST-SPACE", "CISD");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithSortingByRegistrationDate()
    {
        SpaceSearchCriteria criteria = new SpaceSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(new SpacePermId("CISD"));
        criteria.withId().thatEquals(new SpacePermId("TESTGROUP"));
        criteria.withId().thatEquals(new SpacePermId("TEST-SPACE"));

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SpaceFetchOptions fo = new SpaceFetchOptions();

        fo.sortBy().registrationDate().asc();
        List<Space> spaces1 = v3api.searchSpaces(sessionToken, criteria, fo).getObjects();
        assertSpaceCodes(spaces1, "CISD", "TEST-SPACE", "TESTGROUP");

        fo.sortBy().registrationDate().desc();
        List<Space> spaces2 = v3api.searchSpaces(sessionToken, criteria, fo).getObjects();
        assertSpaceCodes(spaces2, "TESTGROUP", "TEST-SPACE", "CISD");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithSortingByModificationDate()
    {
        SpaceSearchCriteria criteria = new SpaceSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(new SpacePermId("CISD"));
        criteria.withId().thatEquals(new SpacePermId("TESTGROUP"));
        criteria.withId().thatEquals(new SpacePermId("TEST-SPACE"));

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SpaceFetchOptions fo = new SpaceFetchOptions();

        fo.sortBy().modificationDate().asc();
        List<Space> spaces1 = v3api.searchSpaces(sessionToken, criteria, fo).getObjects();
        assertSpaceCodes(spaces1, "CISD", "TEST-SPACE", "TESTGROUP");

        fo.sortBy().modificationDate().desc();
        List<Space> spaces2 = v3api.searchSpaces(sessionToken, criteria, fo).getObjects();
        assertSpaceCodes(spaces2, "TESTGROUP", "TEST-SPACE", "CISD");

        v3api.logout(sessionToken);
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testSearchWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SpacePermId permId1 = new SpacePermId("/CISD");
        SpacePermId permId2 = new SpacePermId("/TEST-SPACE");

        SpaceSearchCriteria criteria = new SpaceSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(permId1);
        criteria.withId().thatEquals(permId2);

        String sessionToken = v3api.login(user.getUserId(), PASSWORD);

        SearchResult<Space> result = v3api.searchSpaces(sessionToken, criteria, new SpaceFetchOptions());

        if (user.isInstanceUser())
        {
            assertEquals(result.getTotalCount(), 2);
            assertEquals(result.getObjects().size(), 2);
        } else if ((user.isTestSpaceUser() || user.isTestProjectUser()) && !user.isDisabledProjectUser())
        {
            assertEquals(result.getTotalCount(), 1);
            assertEquals(result.getObjects().size(), 1);
            assertEquals(result.getObjects().get(0).getPermId(), permId2);
        } else
        {
            assertEquals(result.getTotalCount(), 0);
            assertEquals(result.getObjects().size(), 0);
        }

        v3api.logout(sessionToken);
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SpaceSearchCriteria c = new SpaceSearchCriteria();
        c.withCode().thatEquals("CISD");

        SpaceFetchOptions fo = new SpaceFetchOptions();
        fo.withProjects();
        fo.withSamples();

        v3api.searchSpaces(sessionToken, c, fo);

        assertAccessLog(
                "search-spaces  SEARCH_CRITERIA:\n'SPACE\n    with attribute 'code' equal to 'CISD'\n'\nFETCH_OPTIONS:\n'Space\n    with Samples\n    with Projects\n'");
    }

    private void testSearch(String user, SpaceSearchCriteria criteria, String... expectedCodes)
    {
        String sessionToken = v3api.login(user, PASSWORD);

        SearchResult<Space> searchResult =
                v3api.searchSpaces(sessionToken, criteria, new SpaceFetchOptions());
        List<Space> spaces = searchResult.getObjects();

        assertSpaceCodes(spaces, expectedCodes);
        v3api.logout(sessionToken);
    }

}

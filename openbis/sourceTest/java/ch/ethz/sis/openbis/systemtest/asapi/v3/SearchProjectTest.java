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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.ProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.openbis.systemtest.authorization.ProjectAuthorizationUser;

/**
 * @author pkupczyk
 */
public class SearchProjectTest extends AbstractTest
{

    @Test
    public void testSearchWithEmptyCriteria()
    {
        testSearch(TEST_SPACE_USER, new ProjectSearchCriteria(), "/TEST-SPACE/TEST-PROJECT", "/TEST-SPACE/NOE", "/TEST-SPACE/PROJECT-TO-DELETE");
    }

    @Test
    public void testSearchWithIdSetToPermId()
    {
        ProjectSearchCriteria criteria = new ProjectSearchCriteria();
        criteria.withId().thatEquals(new ProjectPermId("20120814110011738-105"));
        testSearch(TEST_USER, criteria, "/TEST-SPACE/TEST-PROJECT");
    }

    @Test
    public void testSearchWithIdSetToIdentifier()
    {
        ProjectSearchCriteria criteria = new ProjectSearchCriteria();
        criteria.withId().thatEquals(new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT"));
        testSearch(TEST_USER, criteria, "/TEST-SPACE/TEST-PROJECT");
    }

    @Test
    public void testSearchWithIdSetToNonexistentPermId()
    {
        ProjectSearchCriteria criteria = new ProjectSearchCriteria();
        criteria.withId().thatEquals(new ProjectPermId("IDONTEXIST"));
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithIdSetToNonexistentIdentifier()
    {
        ProjectSearchCriteria criteria = new ProjectSearchCriteria();
        criteria.withId().thatEquals(new ProjectIdentifier("/IDONT/EXIST"));
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithPermIdThatEquals()
    {
        ProjectSearchCriteria criteria = new ProjectSearchCriteria();
        criteria.withPermId().thatEquals("20120814110011738-105");
        testSearch(TEST_USER, criteria, "/TEST-SPACE/TEST-PROJECT");
    }

    @Test
    public void testSearchWithCodeThatEquals()
    {
        ProjectSearchCriteria criteria = new ProjectSearchCriteria();
        criteria.withCode().thatEquals("TEST-PROJECT");
        testSearch(TEST_USER, criteria, "/TEST-SPACE/TEST-PROJECT");
    }

    @Test
    public void testSearchWithCodes()
    {
        ProjectSearchCriteria criteria = new ProjectSearchCriteria();
        criteria.withCodes().thatIn(Arrays.asList("TEST-PROJECT", "TESTPROJ"));
        testSearch(TEST_USER, criteria, "/TESTGROUP/TESTPROJ", "/TEST-SPACE/TEST-PROJECT");
    }

    @Test
    public void testSearchWithCodeThatContains()
    {
        ProjectSearchCriteria criteria = new ProjectSearchCriteria();
        criteria.withCode().thatContains("pRoJ");
        testSearch(TEST_USER, criteria, "/TESTGROUP/TESTPROJ", "/TEST-SPACE/TEST-PROJECT", "/TEST-SPACE/PROJECT-TO-DELETE");
    }

    @Test
    public void testSearchWithCodeThatStartsWith()
    {
        ProjectSearchCriteria criteria = new ProjectSearchCriteria();
        criteria.withCode().thatStartsWith("n");
        testSearch(TEST_USER, criteria, "/CISD/NEMO", "/CISD/NOE", "/TEST-SPACE/NOE");
    }

    @Test
    public void testSearchWithCodeThatEndsWith()
    {
        ProjectSearchCriteria criteria = new ProjectSearchCriteria();
        criteria.withCode().thatEndsWith("t");
        testSearch(TEST_USER, criteria, "/CISD/DEFAULT", "/TEST-SPACE/TEST-PROJECT");
    }

    @Test
    public void testSearchWithSpaceWithIdThatEquals()
    {
        ProjectSearchCriteria criteria = new ProjectSearchCriteria();
        criteria.withSpace().withId().thatEquals(new SpacePermId("CISD"));
        testSearch(TEST_USER, criteria, "/CISD/DEFAULT", "/CISD/NEMO", "/CISD/NOE");
    }

    @Test
    public void testSearchWithSpaceWithCodeThatStartsWith()
    {
        ProjectSearchCriteria criteria = new ProjectSearchCriteria();
        criteria.withSpace().withCode().thatStartsWith("TEST");
        testSearch(TEST_USER, criteria, "/TESTGROUP/TESTPROJ", "/TEST-SPACE/TEST-PROJECT", "/TEST-SPACE/NOE", "/TEST-SPACE/PROJECT-TO-DELETE");
    }

    @Test
    public void testSearchWithAndOperator()
    {
        ProjectSearchCriteria criteria = new ProjectSearchCriteria();
        criteria.withAndOperator();
        criteria.withCode().thatContains("TEST");
        criteria.withCode().thatContains("PROJECT");
        testSearch(TEST_USER, criteria, "/TEST-SPACE/TEST-PROJECT");
    }

    @Test
    public void testSearchWithOrOperator()
    {
        ProjectSearchCriteria criteria = new ProjectSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("20120814110011738-101");
        criteria.withPermId().thatEquals("20120814110011738-105");
        testSearch(TEST_USER, criteria, "/CISD/DEFAULT", "/TEST-SPACE/TEST-PROJECT");
    }

    @Test
    public void testSearchWithProjectUnauthorized()
    {
        ProjectSearchCriteria criteria = new ProjectSearchCriteria();
        criteria.withId().thatEquals(new ProjectIdentifier("/CISD/DEFAULT"));
        testSearch(TEST_USER, criteria, "/CISD/DEFAULT");
        testSearch(TEST_SPACE_USER, criteria);
    }

    @Test
    public void testSearchWithSortingByCode()
    {
        ProjectSearchCriteria criteria = new ProjectSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(new ProjectIdentifier("/CISD/NEMO"));
        criteria.withId().thatEquals(new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT"));
        criteria.withId().thatEquals(new ProjectIdentifier("/TEST-SPACE/PROJECT-TO-DELETE"));

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ProjectFetchOptions fo = new ProjectFetchOptions();

        fo.sortBy().code().asc();
        List<Project> projects1 = v3api.searchProjects(sessionToken, criteria, fo).getObjects();
        assertProjectIdentifiers(projects1, "/CISD/NEMO", "/TEST-SPACE/PROJECT-TO-DELETE", "/TEST-SPACE/TEST-PROJECT");

        fo.sortBy().code().desc();
        List<Project> projects2 = v3api.searchProjects(sessionToken, criteria, fo).getObjects();
        assertProjectIdentifiers(projects2, "/TEST-SPACE/TEST-PROJECT", "/TEST-SPACE/PROJECT-TO-DELETE", "/CISD/NEMO");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithSortingByIdentifier()
    {
        ProjectSearchCriteria criteria = new ProjectSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(new ProjectIdentifier("/CISD/NOE"));
        criteria.withId().thatEquals(new ProjectIdentifier("/TEST-SPACE/NOE"));
        criteria.withId().thatEquals(new ProjectIdentifier("/TEST-SPACE/PROJECT-TO-DELETE"));

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ProjectFetchOptions fo = new ProjectFetchOptions();

        fo.sortBy().code().asc();
        List<Project> projects1 = v3api.searchProjects(sessionToken, criteria, fo).getObjects();
        assertProjectIdentifiers(projects1, "/CISD/NOE", "/TEST-SPACE/NOE", "/TEST-SPACE/PROJECT-TO-DELETE");

        fo.sortBy().code().desc();
        List<Project> projects2 = v3api.searchProjects(sessionToken, criteria, fo).getObjects();
        assertProjectIdentifiers(projects2, "/TEST-SPACE/PROJECT-TO-DELETE", "/TEST-SPACE/NOE", "/CISD/NOE");

        v3api.logout(sessionToken);
    }

    @Test()
    public void testSearchWithSortingByModificationDate()
    {
        final ProjectSearchCriteria criteria = new ProjectSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT"));
        criteria.withId().thatEquals(new ProjectIdentifier("/TESTGROUP/TESTPROJ"));

        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final ProjectFetchOptions fo1 = new ProjectFetchOptions();
        fo1.from(1).count(1);
        fo1.sortBy().modificationDate().asc();

        final SearchResult<Project> result1 = v3api.searchProjects(sessionToken, criteria, fo1);
        final Project project1 = result1.getObjects().get(0);
        assertEquals(project1.getCode(), "TESTPROJ");

        final ProjectFetchOptions fo2 = new ProjectFetchOptions();
        fo2.from(1).count(1);
        fo2.sortBy().modificationDate().desc();

        final SearchResult<Project> result2 = v3api.searchProjects(sessionToken, criteria, fo2);
        final Project project2 = result2.getObjects().get(0);
        assertEquals(project2.getCode(), "TEST-PROJECT");

        v3api.logout(sessionToken);
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testSearchWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        ProjectIdentifier identifier1 = new ProjectIdentifier("/CISD/NEMO");
        ProjectIdentifier identifier2 = new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT");

        String sessionToken = v3api.login(user.getUserId(), PASSWORD);
        ProjectSearchCriteria criteria = new ProjectSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(identifier1);
        criteria.withId().thatEquals(identifier2);

        SearchResult<Project> result = v3api.searchProjects(sessionToken, criteria, projectFetchOptionsFull());

        if (user.isInstanceUser())
        {
            assertEquals(result.getObjects().size(), 2);
        } else if ((user.isTestSpaceUser() || user.isTestProjectUser()) && !user.isDisabledProjectUser())
        {
            assertEquals(result.getObjects().size(), 1);
            assertEquals(result.getObjects().get(0).getIdentifier(), identifier2);
        } else
        {
            assertEquals(result.getObjects().size(), 0);
        }

        v3api.logout(sessionToken);
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ProjectSearchCriteria c = new ProjectSearchCriteria();
        c.withCode().thatEquals("test-PROJECT");

        ProjectFetchOptions fo = new ProjectFetchOptions();
        fo.withSpace();
        fo.withExperiments();

        v3api.searchProjects(sessionToken, c, fo);

        assertAccessLog(
                "search-projects  SEARCH_CRITERIA:\n'PROJECT\n    with attribute 'code' equal to 'test-PROJECT'\n'\nFETCH_OPTIONS:\n'Project\n    with Experiments\n    with Space\n'");
    }

    private void testSearch(String user, ProjectSearchCriteria criteria, String... expectedIdentifiers)
    {
        String sessionToken = v3api.login(user, PASSWORD);

        SearchResult<Project> searchResult = v3api.searchProjects(sessionToken, criteria, new ProjectFetchOptions());
        List<Project> projects = searchResult.getObjects();

        assertProjectIdentifiers(projects, expectedIdentifiers);
        v3api.logout(sessionToken);
    }

}

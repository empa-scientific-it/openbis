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

import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.Query;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.QueryType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.create.QueryCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.fetchoptions.QueryFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.QueryDatabaseName;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.QueryName;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.QueryTechId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.search.QuerySearchCriteria;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.shared.dto.QueryPE;
import ch.systemsx.cisd.openbis.systemtest.authorization.ProjectAuthorizationUser;

/**
 * @author pkupczyk
 */
public class SearchQueryTest extends AbstractQueryTest
{

    private Query query1;

    private Query query2;

    private Query query3;

    private Query query4;

    @BeforeMethod
    private void createQueries()
    {
        QueryCreation creation1 = new QueryCreation();
        creation1.setName("test name 1");
        creation1.setDescription("test description 1");
        creation1.setDatabaseId(DB_OPENBIS_METADATA_ID);
        creation1.setQueryType(QueryType.GENERIC);
        creation1.setEntityTypeCodePattern(null);
        creation1.setSql("select code from spaces");
        creation1.setPublic(true);

        QueryCreation creation2 = new QueryCreation();
        creation2.setName("test name 2");
        creation2.setDatabaseId(DB_OPENBIS_METADATA_ID);
        creation2.setQueryType(QueryType.EXPERIMENT);
        creation2.setEntityTypeCodePattern("EXPERIMENT_TYPE_1");
        creation2.setSql("select code from experiments");
        creation2.setPublic(true);

        QueryCreation creation3 = new QueryCreation();
        creation3.setName("other name 1 2");
        creation3.setDescription("test description 3");
        creation3.setDatabaseId(DB_OPENBIS_METADATA_ID);
        creation3.setQueryType(QueryType.EXPERIMENT);
        creation3.setEntityTypeCodePattern("EXPERIMENT_TYPE_12");
        creation3.setSql("select perm_id from experiments");
        creation3.setPublic(false);

        QueryCreation creation4 = new QueryCreation();
        creation4.setName("other database");
        creation4.setDescription("other description 14");
        creation4.setDatabaseId(DB_TEST_CISD_ID);
        creation4.setQueryType(QueryType.GENERIC);
        creation4.setSql("select perm_id from spaces");
        creation4.setPublic(true);

        query1 = createQuery(TEST_USER, PASSWORD, creation1);
        query2 = createQuery(TEST_USER, PASSWORD, creation2);
        query3 = createQuery(TEST_USER, PASSWORD, creation3);
        query4 = createQuery(TEST_USER, PASSWORD, creation4);
    }

    @Test
    public void testSearchWithEmptyCriteria()
    {
        testSearch(TEST_USER, new QuerySearchCriteria(), query1.getName(), query2.getName(), query3.getName(), query4.getName());
    }

    @Test
    public void testSearchWithIdSetToTechId()
    {
        QuerySearchCriteria criteria = new QuerySearchCriteria();
        criteria.withId().thatEquals(query2.getPermId());
        testSearch(TEST_USER, criteria, query2.getName());
    }

    @Test
    public void testSearchWithIdSetToNonexistentTechId()
    {
        QuerySearchCriteria criteria = new QuerySearchCriteria();
        criteria.withId().thatEquals(new QueryTechId(-1L));
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithIdSetToName()
    {
        QuerySearchCriteria criteria = new QuerySearchCriteria();
        criteria.withId().thatEquals(new QueryName(query2.getName()));
        testSearch(TEST_USER, criteria, query2.getName());
    }

    @Test
    public void testSearchWithIdSetToNonexistentName()
    {
        QuerySearchCriteria criteria = new QuerySearchCriteria();
        criteria.withId().thatEquals(new QueryName("idontexist"));
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithDatabaseId()
    {
        QuerySearchCriteria criteria = new QuerySearchCriteria();
        criteria.withDatabaseId().thatEquals(DB_TEST_CISD_ID);
        testSearch(TEST_USER, criteria, query4.getName());
    }

    @Test
    public void testSearchWithDatabaseIdNonexistent()
    {
        QuerySearchCriteria criteria = new QuerySearchCriteria();
        criteria.withDatabaseId().thatEquals(new QueryDatabaseName("idontexist"));
        testSearch(TEST_USER, criteria);
    }

    @Test
    public void testSearchWithQueryThatBelongsToNonexistentDatabase()
    {
        QueryPE queryWithExistingDB = createQueryWithExistingDB(TEST_USER);
        QueryPE queryWithNonExistentDB = createQueryWithNonExistentDB(TEST_USER);

        QuerySearchCriteria criteria = new QuerySearchCriteria();
        criteria.withOrOperator();
        criteria.withName().thatEquals(queryWithExistingDB.getName());
        criteria.withName().thatEquals(queryWithNonExistentDB.getName());
        testSearch(TEST_USER, criteria, queryWithExistingDB.getName());
    }

    @Test
    public void testSearchWithNameThatEquals()
    {
        QuerySearchCriteria criteria = new QuerySearchCriteria();
        criteria.withName().thatEquals(query2.getName());
        testSearch(TEST_USER, criteria, query2.getName());
    }

    @Test
    public void testSearchWithNameThatContains()
    {
        QuerySearchCriteria criteria = new QuerySearchCriteria();
        criteria.withName().thatContains("name 1");
        testSearch(TEST_USER, criteria, query1.getName(), query3.getName());
    }

    @Test
    public void testSearchWithNameThatStartsWith()
    {
        QuerySearchCriteria criteria = new QuerySearchCriteria();
        criteria.withName().thatStartsWith("test");
        testSearch(TEST_USER, criteria, query1.getName(), query2.getName());
    }

    @Test
    public void testSearchWithNameThatEndsWith()
    {
        QuerySearchCriteria criteria = new QuerySearchCriteria();
        criteria.withName().thatEndsWith("2");
        testSearch(TEST_USER, criteria, query2.getName(), query3.getName());
    }

    @Test
    public void testSearchWithDescriptionThatEquals()
    {
        QuerySearchCriteria criteria = new QuerySearchCriteria();
        criteria.withDescription().thatEquals("test description 3");
        testSearch(TEST_USER, criteria, query3.getName());
    }

    @Test
    public void testSearchWithDescriptionThatContains()
    {
        QuerySearchCriteria criteria = new QuerySearchCriteria();
        criteria.withDescription().thatContains("description");
        testSearch(TEST_USER, criteria, query1.getName(), query3.getName(), query4.getName());
    }

    @Test
    public void testSearchWithDescriptionThatStartsWith()
    {
        QuerySearchCriteria criteria = new QuerySearchCriteria();
        criteria.withDescription().thatStartsWith("test");
        testSearch(TEST_USER, criteria, query1.getName(), query3.getName());
    }

    @Test
    public void testSearchWithDescriptionThatEndsWith()
    {
        QuerySearchCriteria criteria = new QuerySearchCriteria();
        criteria.withDescription().thatEndsWith("1");
        testSearch(TEST_USER, criteria, query1.getName());
    }

    @Test
    public void testSearchWithSqlThatEquals()
    {
        QuerySearchCriteria criteria = new QuerySearchCriteria();
        criteria.withSql().thatEquals("select code from experiments");
        testSearch(TEST_USER, criteria, query2.getName());
    }

    @Test
    public void testSearchWithSqlThatContains()
    {
        QuerySearchCriteria criteria = new QuerySearchCriteria();
        criteria.withSql().thatContains("space");
        testSearch(TEST_USER, criteria, query1.getName(), query4.getName());
    }

    @Test
    public void testSearchWithSqlThatStartsWith()
    {
        QuerySearchCriteria criteria = new QuerySearchCriteria();
        criteria.withSql().thatStartsWith("select perm_id");
        testSearch(TEST_USER, criteria, query3.getName(), query4.getName());
    }

    @Test
    public void testSearchWithSqlThatEndsWith()
    {
        QuerySearchCriteria criteria = new QuerySearchCriteria();
        criteria.withSql().thatEndsWith("spaces");
        testSearch(TEST_USER, criteria, query1.getName(), query4.getName());
    }

    @Test
    public void testSearchWithQueryTypeThatEquals()
    {
        QuerySearchCriteria criteria = new QuerySearchCriteria();
        criteria.withQueryType().thatEquals(QueryType.EXPERIMENT);
        testSearch(TEST_USER, criteria, query2.getName(), query3.getName());
    }

    @Test
    public void testSearchWithEntityTypeCodePatternThatEquals()
    {
        QuerySearchCriteria criteria = new QuerySearchCriteria();
        criteria.withEntityTypeCodePattern().thatEquals("EXPERIMENT_TYPE_12");
        testSearch(TEST_USER, criteria, query3.getName());
    }

    @Test
    public void testSearchWithEntityTypeCodePatternThatStartsWith()
    {
        QuerySearchCriteria criteria = new QuerySearchCriteria();
        criteria.withEntityTypeCodePattern().thatStartsWith("EXPERIMENT_TYPE");
        testSearch(TEST_USER, criteria, query2.getName(), query3.getName());
    }

    @Test
    public void testSearchWithEntityTypeCodePatternThatEndsWith()
    {
        QuerySearchCriteria criteria = new QuerySearchCriteria();
        criteria.withEntityTypeCodePattern().thatEndsWith("TYPE_1");
        testSearch(TEST_USER, criteria, query2.getName());
    }

    @Test
    public void testSearchWithEntityTypeCodePatternThatContains()
    {
        QuerySearchCriteria criteria = new QuerySearchCriteria();
        criteria.withEntityTypeCodePattern().thatContains("TYPE_1");
        testSearch(TEST_USER, criteria, query2.getName(), query3.getName());
    }

    @Test
    public void testSearchWithAndOperator()
    {
        QuerySearchCriteria criteria = new QuerySearchCriteria();
        criteria.withAndOperator();
        criteria.withName().thatContains("test");
        criteria.withName().thatContains("name");
        testSearch(TEST_USER, criteria, query1.getName(), query2.getName());
    }

    @Test
    public void testSearchWithOrOperator()
    {
        QuerySearchCriteria criteria = new QuerySearchCriteria();
        criteria.withOrOperator();
        criteria.withName().thatContains("test");
        criteria.withName().thatContains("name");
        testSearch(TEST_USER, criteria, query1.getName(), query2.getName(), query3.getName());
    }

    @Test
    public void testSearchWithUnauthorized()
    {
        QuerySearchCriteria criteria = new QuerySearchCriteria();
        testSearch(TEST_USER, criteria, query1.getName(), query2.getName(), query3.getName(), query4.getName());
        testSearch(TEST_SPACE_USER, criteria, query1.getName(), query2.getName(), query4.getName());
    }

    @Test(dataProvider = PROVIDER_TRUE_FALSE)
    public void testSearchWithOwner(boolean isPublic)
    {
        QueryCreation creation = testCreation();
        creation.setDatabaseId(new QueryDatabaseName(DB_TEST_CISD));
        creation.setPublic(isPublic);
        Query query = createQuery(TEST_POWER_USER_CISD, PASSWORD, creation);

        QuerySearchCriteria criteria = new QuerySearchCriteria();
        criteria.withId().thatEquals(query.getPermId());

        testSearch(TEST_POWER_USER_CISD, criteria, query.getName());
    }

    @Test(dataProvider = PROVIDER_TRUE_FALSE)
    public void testSearchWithNonOwner(boolean isPublic)
    {
        QueryCreation creation = testCreation();
        creation.setDatabaseId(new QueryDatabaseName(DB_TEST_CISD));
        creation.setPublic(isPublic);
        Query query = createQuery(TEST_NO_HOME_SPACE, PASSWORD, creation);

        QuerySearchCriteria criteria = new QuerySearchCriteria();
        criteria.withId().thatEquals(query.getPermId());

        if (isPublic)
        {
            testSearch(TEST_POWER_USER_CISD, criteria, query.getName());
        } else
        {
            testSearch(TEST_POWER_USER_CISD, criteria);
        }
    }

    @Test(dataProvider = PROVIDER_TRUE_FALSE)
    public void testSearchWithNonOwnerInstanceAdmin(boolean isPublic)
    {
        QueryCreation creation = testCreation();
        creation.setDatabaseId(new QueryDatabaseName(DB_TEST_CISD));
        creation.setPublic(isPublic);
        Query query = createQuery(TEST_NO_HOME_SPACE, PASSWORD, creation);

        QuerySearchCriteria criteria = new QuerySearchCriteria();
        criteria.withId().thatEquals(query.getPermId());

        testSearch(TEST_USER, criteria, query.getName());
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testSearchWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        QuerySearchCriteria criteria = new QuerySearchCriteria();
        criteria.withId().thatEquals(query1.getPermId());

        if (user.isDisabledProjectUser())
        {
            assertAuthorizationFailureException(new IDelegatedAction()
                {
                    @Override
                    public void execute()
                    {
                        testSearch(user.getUserId(), criteria);
                    }
                });
        } else
        {
            testSearch(user.getUserId(), criteria, query1.getName());
        }
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        QuerySearchCriteria c = new QuerySearchCriteria();
        c.withName().thatEquals("query-name");

        QueryFetchOptions fo = new QueryFetchOptions();
        fo.withRegistrator();

        v3api.searchQueries(sessionToken, c, fo);

        assertAccessLog(
                "search-queries  SEARCH_CRITERIA:\n'QUERY\n    with attribute 'name' equal to 'query-name'\n'\nFETCH_OPTIONS:\n'Query\n    with Registrator\n'");
    }

    private void testSearch(String user, QuerySearchCriteria criteria, String... expectedNames)
    {
        String sessionToken = v3api.login(user, PASSWORD);

        SearchResult<Query> searchResult =
                v3api.searchQueries(sessionToken, criteria, new QueryFetchOptions());
        List<Query> queries = searchResult.getObjects();

        assertQueryNames(queries, expectedNames);
        v3api.logout(sessionToken);
    }

}

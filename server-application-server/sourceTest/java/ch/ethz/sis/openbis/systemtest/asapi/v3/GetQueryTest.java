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
import static org.testng.Assert.assertNull;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.Query;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.QueryType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.create.QueryCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.fetchoptions.QueryFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.IQueryId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.QueryDatabaseName;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.QueryName;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.QueryTechId;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.shared.dto.QueryPE;
import ch.systemsx.cisd.openbis.systemtest.authorization.ProjectAuthorizationUser;

/**
 * @author pkupczyk
 */
public class GetQueryTest extends AbstractQueryTest
{

    private Query query1;

    private Query query2;

    private Query query3;

    @BeforeMethod
    private void createQueries()
    {
        QueryCreation creation1 = testCreation();
        creation1.setName("test name 1");

        QueryCreation creation2 = testCreation();
        creation2.setName("test name 2");

        QueryCreation creation3 = testCreation();
        creation3.setName("test name 3");
        creation3.setPublic(false);

        query1 = createQuery(TEST_USER, PASSWORD, creation1);
        query2 = createQuery(TEST_USER, PASSWORD, creation2);
        query3 = createQuery(TEST_USER, PASSWORD, creation3);
    }

    @Test
    public void testGetByTechId()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        QueryTechId techId1 = (QueryTechId) query1.getPermId();
        QueryTechId techId2 = (QueryTechId) query2.getPermId();

        Map<IQueryId, Query> map =
                v3api.getQueries(sessionToken, Arrays.asList(techId1, techId2),
                        new QueryFetchOptions());

        assertEquals(2, map.size());

        Iterator<Query> iter = map.values().iterator();
        assertEquals(iter.next().getPermId(), techId1);
        assertEquals(iter.next().getPermId(), techId2);

        assertEquals(map.get(techId1).getPermId(), query1.getPermId());
        assertEquals(map.get(techId2).getPermId(), query2.getPermId());

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetByName()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        QueryName name1 = new QueryName(query1.getName());
        QueryName name2 = new QueryName(query2.getName());

        Map<IQueryId, Query> map = v3api.getQueries(sessionToken, Arrays.asList(name1, name2), new QueryFetchOptions());

        assertEquals(2, map.size());

        Iterator<Query> iter = map.values().iterator();
        assertEquals(iter.next().getPermId(), query1.getPermId());
        assertEquals(iter.next().getPermId(), query2.getPermId());

        assertEquals(map.get(name1).getPermId(), query1.getPermId());
        assertEquals(map.get(name2).getPermId(), query2.getPermId());

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetByIdsNonexistent()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        QueryName id1 = new QueryName(query1.getName());
        QueryName id2 = new QueryName("IDONTEXIST");
        QueryName id3 = new QueryName(query2.getName());

        Map<IQueryId, Query> map = v3api.getQueries(sessionToken, Arrays.asList(id1, id2, id3), new QueryFetchOptions());

        assertEquals(2, map.size());

        Iterator<Query> iter = map.values().iterator();
        assertEquals(iter.next().getPermId(), query1.getPermId());
        assertEquals(iter.next().getPermId(), query2.getPermId());

        assertEquals(map.get(id1).getPermId(), query1.getPermId());
        assertEquals(map.get(id3).getPermId(), query2.getPermId());

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetByIdsDuplicated()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        QueryName id1 = new QueryName(query1.getName());
        QueryName id2 = new QueryName(query1.getName());

        Map<IQueryId, Query> map = v3api.getQueries(sessionToken, Arrays.asList(id1, id2), new QueryFetchOptions());

        assertEquals(1, map.size());

        assertEquals(map.get(id1).getPermId(), query1.getPermId());
        assertEquals(map.get(id2).getPermId(), query1.getPermId());

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetByIdsUnauthorized()
    {
        List<? extends IQueryId> ids = Arrays.asList(query1.getPermId(), query2.getPermId(), query3.getPermId());

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        Map<IQueryId, Query> map = v3api.getQueries(sessionToken, ids, new QueryFetchOptions());

        assertEquals(map.size(), 3);
        v3api.logout(sessionToken);

        sessionToken = v3api.login(TEST_INSTANCE_OBSERVER, PASSWORD);
        map = v3api.getQueries(sessionToken, ids, new QueryFetchOptions());

        assertEquals(map.size(), 2);

        Iterator<Query> iter = map.values().iterator();
        assertEquals(iter.next().getPermId(), query1.getPermId());
        assertEquals(iter.next().getPermId(), query2.getPermId());

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetByIdsWithFetchOptionsEmpty()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        QueryCreation creation = new QueryCreation();
        creation.setName("test name");
        creation.setDescription("test description");
        creation.setDatabaseId(DB_OPENBIS_METADATA_ID);
        creation.setEntityTypeCodePattern("SOME_EXPERIMENT_TYPE");
        creation.setQueryType(QueryType.EXPERIMENT);
        creation.setPublic(true);
        creation.setSql("select * from experiments where perm_id = ${key}");

        createQuery(TEST_USER, PASSWORD, creation);

        Map<IQueryId, Query> map = v3api.getQueries(sessionToken, Arrays.asList(new QueryName(creation.getName())), new QueryFetchOptions());
        Query query = map.values().iterator().next();

        assertEquals(query.getName(), creation.getName());
        assertEquals(query.getDescription(), creation.getDescription());
        assertEquals(query.getDatabaseId(), creation.getDatabaseId());
        assertEquals(query.getDatabaseLabel(), "openBIS meta data");
        assertEquals(query.getEntityTypeCodePattern(), creation.getEntityTypeCodePattern());
        assertEquals(query.getQueryType(), creation.getQueryType());
        assertEquals(query.isPublic(), creation.isPublic());
        assertEquals(query.getSql(), creation.getSql());

        assertRegistratorNotFetched(query);
    }

    @Test
    public void testGetByIdsWithRegistrator()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        QueryCreation creation = new QueryCreation();
        creation.setName("test name");
        creation.setDatabaseId(DB_OPENBIS_METADATA_ID);
        creation.setQueryType(QueryType.GENERIC);
        creation.setSql("select * from spaces");

        createQuery(TEST_USER, PASSWORD, creation);

        QueryFetchOptions fo = new QueryFetchOptions();
        fo.withRegistrator();

        Map<IQueryId, Query> map = v3api.getQueries(sessionToken, Arrays.asList(new QueryName(creation.getName())), fo);
        Query query = map.values().iterator().next();

        assertEquals(query.getName(), creation.getName());
        assertEquals(query.getRegistrator().getUserId(), TEST_USER);
    }

    @Test
    public void testGetWithQueryThatBelongsToNonexistentDatabase()
    {
        QueryPE queryWithExistingDB = createQueryWithExistingDB(TEST_USER);
        QueryPE queryWithNonExistentDB = createQueryWithNonExistentDB(TEST_USER);

        Query query = getQuery(TEST_USER, PASSWORD, new QueryName(queryWithExistingDB.getName()));
        assertEquals(query.getName(), queryWithExistingDB.getName());

        query = getQuery(TEST_USER, PASSWORD, new QueryName(queryWithNonExistentDB.getName()));
        assertNull(query);
    }

    @Test(dataProvider = PROVIDER_TRUE_FALSE)
    public void testGetWithOwner(boolean isPublic)
    {
        QueryCreation creation = testCreation();
        creation.setDatabaseId(new QueryDatabaseName(DB_TEST_CISD));
        creation.setPublic(isPublic);
        createQuery(TEST_POWER_USER_CISD, PASSWORD, creation);

        Query query = getQuery(TEST_POWER_USER_CISD, PASSWORD, new QueryName(creation.getName()));
        assertEquals(query.getName(), creation.getName());
    }

    @Test(dataProvider = PROVIDER_TRUE_FALSE)
    public void testGetWithNonOwner(boolean isPublic)
    {
        QueryCreation creation = testCreation();
        creation.setDatabaseId(new QueryDatabaseName(DB_TEST_CISD));
        creation.setPublic(isPublic);
        createQuery(TEST_NO_HOME_SPACE, PASSWORD, creation);

        Query query = getQuery(TEST_POWER_USER_CISD, PASSWORD, new QueryName(creation.getName()));

        if (isPublic)
        {
            assertEquals(query.getName(), creation.getName());
        } else
        {
            assertNull(query);
        }
    }

    @Test(dataProvider = PROVIDER_TRUE_FALSE)
    public void testGetWithNonOwnerInstanceAdmin(boolean isPublic)
    {
        QueryCreation creation = testCreation();
        creation.setDatabaseId(new QueryDatabaseName(DB_TEST_CISD));
        creation.setPublic(isPublic);
        createQuery(TEST_NO_HOME_SPACE, PASSWORD, creation);

        Query query = getQuery(TEST_USER, PASSWORD, new QueryName(creation.getName()));

        assertEquals(query.getName(), creation.getName());
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testGetWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String sessionToken = v3api.login(user.getUserId(), PASSWORD);

        if (user.isDisabledProjectUser())
        {
            assertAuthorizationFailureException(new IDelegatedAction()
                {
                    @Override
                    public void execute()
                    {
                        v3api.getQueries(sessionToken, Arrays.asList(query1.getPermId()), new QueryFetchOptions());
                    }
                });
        } else
        {
            Map<IQueryId, Query> map = v3api.getQueries(sessionToken, Arrays.asList(query1.getPermId()), new QueryFetchOptions());
            assertEquals(map.size(), 1);
        }

        v3api.logout(sessionToken);
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        QueryFetchOptions fo = new QueryFetchOptions();
        fo.withRegistrator();

        v3api.getQueries(sessionToken, Arrays.asList(new QueryName(query1.getName()), new QueryName(query2.getName())), fo);

        assertAccessLog("get-queries  QUERY_IDS('[" + query1.getName() + ", " + query2.getName()
                + "]') FETCH_OPTIONS('Query\n    with Registrator\n')");
    }

}

/*
 * Copyright 2015 ETH Zuerich, CISD
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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.Query;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.QueryType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.create.QueryCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.QueryDatabaseName;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.QueryName;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.update.QueryUpdate;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.shared.dto.QueryPE;

/**
 * @author pkupczyk
 */
public class UpdateQueryTest extends AbstractQueryTest
{

    @Test
    public void testUpdate()
    {
        Query originalQuery = createQuery(TEST_USER, PASSWORD, testCreation());

        QueryUpdate update = new QueryUpdate();
        update.setQueryId(originalQuery.getPermId());
        update.setName("updated name");
        update.setDescription("updated description");
        update.setDatabaseId(new QueryDatabaseName(DB_TEST_CISD));
        update.setEntityTypeCodePattern("SOME_SAMPLE_TYPE");
        update.setQueryType(QueryType.SAMPLE);
        update.setPublic(false);
        update.setSql("select * from samples where perm_id = ${key}");

        Query updatedQuery = updateQuery(TEST_USER, PASSWORD, update);

        assertEquals(updatedQuery.getName(), update.getName().getValue());
        assertEquals(updatedQuery.getDescription(), update.getDescription().getValue());
        assertEquals(updatedQuery.getDatabaseId(), update.getDatabaseId().getValue());
        assertEquals(updatedQuery.getEntityTypeCodePattern(), update.getEntityTypeCodePattern().getValue());
        assertEquals(updatedQuery.getQueryType(), update.getQueryType().getValue());
        assertEquals((Boolean) updatedQuery.isPublic(), update.isPublic().getValue());
        assertEquals(updatedQuery.getSql(), update.getSql().getValue());
        assertEquals(updatedQuery.getRegistrator().getUserId(), TEST_USER);
        assertToday(updatedQuery.getRegistrationDate());
        assertToday(updatedQuery.getModificationDate());
    }

    @Test
    public void testUpdateWithNameNull()
    {
        Query query = createQuery(TEST_USER, PASSWORD, testCreation());

        QueryUpdate update = new QueryUpdate();
        update.setQueryId(query.getPermId());
        update.setName(null);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    updateQuery(TEST_USER, PASSWORD, update);
                }
            }, "Name cannot be empty");
    }

    @Test
    public void testUpdateWithNameExisting()
    {
        QueryCreation creation1 = testCreation();
        QueryCreation creation2 = testCreation();
        creation2.setName("another name");

        Query query1 = createQuery(TEST_USER, PASSWORD, creation1);
        Query query2 = createQuery(TEST_USER, PASSWORD, creation2);

        QueryUpdate update = new QueryUpdate();
        update.setQueryId(query2.getPermId());
        update.setName(query1.getName());

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    updateQuery(TEST_USER, PASSWORD, update);
                }
            }, "Query already exists in the database and needs to be unique");
    }

    @Test
    public void testUpdateWithQueryIdNull()
    {
        QueryUpdate update = new QueryUpdate();

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    updateQuery(TEST_USER, PASSWORD, update);
                }
            }, "Query id cannot be null");
    }

    @Test
    public void testUpdateWithQueryIdNonexistent()
    {
        QueryUpdate update = new QueryUpdate();
        update.setQueryId(new QueryName("idontexist"));

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    updateQuery(TEST_USER, PASSWORD, update);
                }
            }, update.getQueryId());
    }

    @Test
    public void testUpdateWithDatabaseIdNull()
    {
        QueryUpdate update = new QueryUpdate();
        update.setDatabaseId(null);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    updateQuery(TEST_USER, PASSWORD, update);
                }
            }, "Query id cannot be null");
    }

    @Test
    public void testUpdateWithDatabaseIdNonexistent()
    {
        Query query = createQuery(TEST_USER, PASSWORD, testCreation());

        QueryUpdate update = new QueryUpdate();
        update.setQueryId(query.getPermId());
        update.setDatabaseId(new QueryDatabaseName("idontexist"));

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    updateQuery(TEST_USER, PASSWORD, update);
                }
            }, update.getDatabaseId().getValue());
    }

    @Test
    public void testUpdateWithQueryThatBelongsToNonexistentDatabase()
    {
        QueryPE queryWithNonExistentDB = createQueryWithNonExistentDB(TEST_USER);

        QueryUpdate update = new QueryUpdate();
        update.setQueryId(new QueryName(queryWithNonExistentDB.getName()));

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    updateQuery(TEST_USER, PASSWORD, update);
                }
            }, new QueryDatabaseName("idontexist"));
    }

    @Test
    public void testUpdateWithQueryTypeNull()
    {
        QueryCreation creation = testCreation();
        Query query = createQuery(TEST_USER, PASSWORD, creation);

        QueryUpdate update = new QueryUpdate();
        update.setQueryId(query.getPermId());
        update.setQueryType(null);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    updateQuery(TEST_USER, PASSWORD, update);
                }
            }, "Query type cannot be null");
    }

    @Test
    public void testUpdateWithQueryTypeFromGenericToNonGenericAndWithEntityTypeCodePatternSet()
    {
        QueryCreation creation = testCreation();
        creation.setQueryType(QueryType.GENERIC);
        creation.setEntityTypeCodePattern(null);

        Query originalQuery = createQuery(TEST_USER, PASSWORD, creation);

        QueryUpdate update = new QueryUpdate();
        update.setQueryId(originalQuery.getPermId());
        update.setQueryType(QueryType.EXPERIMENT);
        update.setEntityTypeCodePattern("SOME_EXPERIMENT_TYPE");

        Query updatedQuery = updateQuery(TEST_USER, PASSWORD, update);

        assertEquals(updatedQuery.getPermId(), originalQuery.getPermId());
        assertEquals(updatedQuery.getQueryType(), update.getQueryType().getValue());
        assertEquals(updatedQuery.getEntityTypeCodePattern(), update.getEntityTypeCodePattern().getValue());
    }

    @Test
    public void testUpdateWithQueryTypeFromNonGenericToGenericAndWithEntityTypeCodePatternNotCleared()
    {
        QueryCreation creation = testCreation();
        creation.setQueryType(QueryType.EXPERIMENT);
        creation.setEntityTypeCodePattern("SOME_EXPERIMENT_TYPE");

        Query originalQuery = createQuery(TEST_USER, PASSWORD, creation);

        QueryUpdate update = new QueryUpdate();
        update.setQueryId(originalQuery.getPermId());
        update.setQueryType(QueryType.GENERIC);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    updateQuery(TEST_USER, PASSWORD, update);
                }
            }, "Entity type code pattern cannot be specified for a query with type GENERIC");
    }

    @Test
    public void testUpdateWithQueryTypeFromNonGenericToGenericAndWithEntityTypeCodePatternCleared()
    {
        QueryCreation creation = testCreation();
        creation.setQueryType(QueryType.EXPERIMENT);
        creation.setEntityTypeCodePattern("SOME_EXPERIMENT_TYPE");

        Query originalQuery = createQuery(TEST_USER, PASSWORD, creation);

        QueryUpdate update = new QueryUpdate();
        update.setQueryId(originalQuery.getPermId());
        update.setQueryType(QueryType.GENERIC);
        update.setEntityTypeCodePattern(null);

        Query updatedQuery = updateQuery(TEST_USER, PASSWORD, update);

        assertEquals(updatedQuery.getPermId(), originalQuery.getPermId());
        assertEquals(updatedQuery.getQueryType(), update.getQueryType().getValue());
        assertEquals(updatedQuery.getEntityTypeCodePattern(), update.getEntityTypeCodePattern().getValue());
    }

    @Test
    public void testUpdateWithEntityTypeCodePatternSetForGenericQueryType()
    {
        QueryCreation creation = testCreation();
        creation.setQueryType(QueryType.GENERIC);
        creation.setEntityTypeCodePattern(null);

        Query originalQuery = createQuery(TEST_USER, PASSWORD, creation);

        QueryUpdate update = new QueryUpdate();
        update.setQueryId(originalQuery.getPermId());
        update.setEntityTypeCodePattern("SOME_TYPE");

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    updateQuery(TEST_USER, PASSWORD, update);
                }
            }, "Entity type code pattern cannot be specified for a query with type GENERIC");
    }

    @Test
    public void testUpdateWithSqlNull()
    {
        QueryCreation creation = testCreation();
        Query query = createQuery(TEST_USER, PASSWORD, creation);

        QueryUpdate update = new QueryUpdate();
        update.setQueryId(query.getPermId());
        update.setSql(null);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    updateQuery(TEST_USER, PASSWORD, update);
                }
            }, "Sql cannot be empty");
    }

    @Test
    public void testUpdateWithNonSelectSql()
    {
        QueryCreation creation = testCreation();
        Query query = createQuery(TEST_USER, PASSWORD, creation);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    QueryUpdate update = new QueryUpdate();
                    update.setQueryId(query.getPermId());
                    update.setSql("update spaces set code = 'YOU_HAVE_BEEN_HACKED' where code = 'CISD'");
                    updateQuery(TEST_USER, PASSWORD, update);
                }
            }, "Sorry, only select statements are allowed");

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    QueryUpdate update = new QueryUpdate();
                    update.setQueryId(query.getPermId());
                    update.setSql("select * from spaces; update spaces set code = 'YOU_HAVE_BEEN_HACKED' where code = 'CISD'");
                    updateQuery(TEST_USER, PASSWORD, update);
                }
            }, "Sorry, only one query statement is allowed: A ';' somewhere in the middle has been found.");
    }

    @Test
    public void testUpdateWithDatabaseWithSpaceNullAndDefaultCreatorRoleUsingSpaceObserver()
    {
        QueryCreation creation = testCreation();
        creation.setDatabaseId(DB_OPENBIS_METADATA_ID);
        creation.setPublic(true);

        Query query = createQuery(TEST_USER, PASSWORD, creation);

        QueryUpdate update = new QueryUpdate();
        update.setQueryId(query.getPermId());

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    updateQuery(TEST_GROUP_OBSERVER, PASSWORD, update);
                }
            }, creation.getDatabaseId());
    }

    @Test
    public void testUpdateWithDatabaseWithSpaceNullAndDefaultCreatorRoleUsingInstanceObserver()
    {
        QueryCreation creation = testCreation();
        creation.setDatabaseId(DB_OPENBIS_METADATA_ID);
        creation.setPublic(true);

        Query query = createQuery(TEST_USER, PASSWORD, creation);

        QueryUpdate update = new QueryUpdate();
        update.setQueryId(query.getPermId());
        update.setDescription("updated description");

        Query updatedQuery = updateQuery(TEST_INSTANCE_OBSERVER, PASSWORD, update);

        assertEquals(updatedQuery.getDescription(), update.getDescription().getValue());
    }

    @Test
    public void testUpdateWithDatabaseWithSpaceNotNullAndDefaultCreatorRoleUsingSpaceObserver()
    {
        QueryCreation creation = testCreation();
        creation.setDatabaseId(new QueryDatabaseName(DB_TEST_CISD));
        creation.setPublic(true);

        Query query = createQuery(TEST_USER, PASSWORD, creation);

        QueryUpdate update = new QueryUpdate();
        update.setQueryId(query.getPermId());

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    updateQuery(TEST_OBSERVER_CISD, PASSWORD, update);
                }
            }, creation.getDatabaseId());
    }

    @Test
    public void testUpdateWithDatabaseWithSpaceNotNullAndDefaultCreatorRoleUsingSpacePowerUser()
    {
        QueryCreation creation = testCreation();
        creation.setDatabaseId(new QueryDatabaseName(DB_TEST_CISD));
        creation.setPublic(true);

        Query query = createQuery(TEST_USER, PASSWORD, creation);

        QueryUpdate update = new QueryUpdate();
        update.setQueryId(query.getPermId());
        update.setDescription("updated description");

        Query updatedQuery = updateQuery(TEST_POWER_USER_CISD, PASSWORD, update);

        assertEquals(updatedQuery.getDescription(), update.getDescription().getValue());
    }

    @Test(dataProvider = PROVIDER_TRUE_FALSE)
    public void testUpdateWithOwner(boolean isPublic)
    {
        QueryCreation creation = testCreation();
        creation.setDatabaseId(new QueryDatabaseName(DB_TEST_CISD));
        creation.setPublic(isPublic);

        Query query = createQuery(TEST_POWER_USER_CISD, PASSWORD, creation);

        QueryUpdate update = new QueryUpdate();
        update.setQueryId(query.getPermId());
        update.setDescription("updated description");

        Query updatedQuery = updateQuery(TEST_POWER_USER_CISD, PASSWORD, update);

        assertEquals(updatedQuery.getDescription(), update.getDescription().getValue());
    }

    @Test(dataProvider = PROVIDER_TRUE_FALSE)
    public void testUpdateWithNonOwner(boolean isPublic)
    {
        QueryCreation creation = testCreation();
        creation.setDatabaseId(new QueryDatabaseName(DB_TEST_CISD));
        creation.setPublic(isPublic);

        Query query = createQuery(TEST_NO_HOME_SPACE, PASSWORD, creation);

        QueryUpdate update = new QueryUpdate();
        update.setQueryId(query.getPermId());
        update.setDescription("updated description");

        if (isPublic)
        {
            Query updatedQuery = updateQuery(TEST_POWER_USER_CISD, PASSWORD, update);
            assertEquals(updatedQuery.getDescription(), update.getDescription().getValue());
        } else
        {
            assertUnauthorizedObjectAccessException(new IDelegatedAction()
                {
                    @Override
                    public void execute()
                    {
                        updateQuery(TEST_POWER_USER_CISD, PASSWORD, update);
                    }
                }, query.getPermId());
        }
    }

    @Test(dataProvider = PROVIDER_TRUE_FALSE)
    public void testUpdateWithNonOwnerInstanceAdmin(boolean isPublic)
    {
        QueryCreation creation = testCreation();
        creation.setDatabaseId(new QueryDatabaseName(DB_TEST_CISD));
        creation.setPublic(isPublic);

        Query query = createQuery(TEST_NO_HOME_SPACE, PASSWORD, creation);

        QueryUpdate update = new QueryUpdate();
        update.setQueryId(query.getPermId());
        update.setDescription("updated description");

        Query updatedQuery = updateQuery(TEST_USER, PASSWORD, update);

        assertEquals(updatedQuery.getDescription(), update.getDescription().getValue());
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        QueryCreation creation = new QueryCreation();
        creation.setName("test name");
        creation.setDatabaseId(DB_OPENBIS_METADATA_ID);
        creation.setQueryType(QueryType.GENERIC);
        creation.setSql("select * from experiments where perm_id = ${key}");

        v3api.createQueries(sessionToken, Arrays.asList(creation));

        QueryUpdate update = new QueryUpdate();
        update.setQueryId(new QueryName("test name"));

        v3api.updateQueries(sessionToken, Arrays.asList(update));

        assertAccessLog("update-queries  QUERY_UPDATES('[QueryUpdate[queryId=test name]]')");
    }

}

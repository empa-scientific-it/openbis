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

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.Query;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.create.QueryCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.delete.QueryDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.IQueryId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.QueryDatabaseName;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.QueryName;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.QueryTechId;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.shared.dto.QueryPE;

/**
 * @author pkupczyk
 */
public class DeleteQueryTest extends AbstractQueryTest
{

    @Test
    public void testDeleteWithEmptyList()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        QueryDeletionOptions options = new QueryDeletionOptions();
        options.setReason("It is just a test");

        v3api.deleteQueries(sessionToken, new ArrayList<QueryTechId>(), options);
    }

    @Test
    public void testDeleteWithNonexistent()
    {
        IQueryId queryId = new QueryName("idontexist");

        Query beforeQuery = getQuery(TEST_POWER_USER_CISD, PASSWORD, queryId);
        assertNull(beforeQuery);

        Query afterQuery = deleteQuery(TEST_POWER_USER_CISD, PASSWORD, queryId);
        assertNull(afterQuery);
    }

    @Test
    public void testDeleteWithOptionsNull()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        QueryCreation creation = testCreation();
        Query query = createQuery(TEST_USER, PASSWORD, creation);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.deleteQueries(sessionToken, Arrays.asList(query.getPermId()), null);
                }

            }, "Deletion options cannot be null");
    }

    @Test
    public void testDeleteWithQueryThatBelongsToNonexistentDatabase()
    {
        QueryPE queryWithNonExistentDB = createQueryWithNonExistentDB(TEST_USER);

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    deleteQuery(TEST_USER, PASSWORD, new QueryName(queryWithNonExistentDB.getName()));
                }
            }, new QueryDatabaseName("idontexist"));
    }

    @Test(dataProvider = PROVIDER_TRUE_FALSE)
    public void testDeleteWithOwner(boolean isPublic)
    {
        QueryCreation creation = testCreation();
        creation.setDatabaseId(new QueryDatabaseName(DB_TEST_CISD));
        creation.setPublic(isPublic);

        Query beforeQuery = createQuery(TEST_POWER_USER_CISD, PASSWORD, creation);
        assertNotNull(beforeQuery);

        Query afterQuery = deleteQuery(TEST_POWER_USER_CISD, PASSWORD, beforeQuery.getPermId());
        assertNull(afterQuery);
    }

    @Test(dataProvider = PROVIDER_TRUE_FALSE)
    public void testDeleteWithNonOwner(boolean isPublic)
    {
        QueryCreation creation = testCreation();
        creation.setDatabaseId(new QueryDatabaseName(DB_TEST_CISD));
        creation.setPublic(isPublic);

        Query beforeQuery = createQuery(TEST_NO_HOME_SPACE, PASSWORD, creation);
        assertNotNull(beforeQuery);

        if (isPublic)
        {
            Query afterQuery = deleteQuery(TEST_POWER_USER_CISD, PASSWORD, beforeQuery.getPermId());
            assertNull(afterQuery);
        } else
        {
            assertUnauthorizedObjectAccessException(new IDelegatedAction()
                {
                    @Override
                    public void execute()
                    {
                        deleteQuery(TEST_POWER_USER_CISD, PASSWORD, beforeQuery.getPermId());
                    }
                }, beforeQuery.getPermId());
        }
    }

    @Test(dataProvider = PROVIDER_TRUE_FALSE)
    public void testDeleteWithNonOwnerInstanceAdmin(boolean isPublic)
    {
        QueryCreation creation = testCreation();
        creation.setDatabaseId(new QueryDatabaseName(DB_TEST_CISD));
        creation.setPublic(isPublic);

        Query beforeQuery = createQuery(TEST_NO_HOME_SPACE, PASSWORD, creation);
        assertNotNull(beforeQuery);

        Query afterQuery = deleteQuery(TEST_USER, PASSWORD, beforeQuery.getPermId());
        assertNull(afterQuery);
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        QueryDeletionOptions o = new QueryDeletionOptions();
        o.setReason("test-reason");

        v3api.deleteQueries(sessionToken,
                Arrays.asList(new QueryName("TEST-LOGGING-1"), new QueryName("TEST-LOGGING-2")), o);

        assertAccessLog(
                "delete-queries  QUERY_IDS('[TEST-LOGGING-1, TEST-LOGGING-2]') DELETION_OPTIONS('QueryDeletionOptions[reason=test-reason]')");
    }

}

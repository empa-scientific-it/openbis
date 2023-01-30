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

import java.util.Collections;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.TableModel;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.Query;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.create.QueryCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.execute.QueryExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.IQueryDatabaseId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.QueryDatabaseName;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.QueryName;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.Role;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.shared.dto.QueryPE;

/**
 * @author pkupczyk
 */
public class ExecuteQueryTest extends AbstractQueryTest
{

    @Test
    public void testExecute()
    {
        Query query = createQuery(TEST_USER, PASSWORD, selectSpaceCodesQueryCreation(DB_OPENBIS_METADATA_ID));

        TableModel result = executeQuery(TEST_USER, PASSWORD, query.getPermId(), null);

        assertEquals(result.getRows().size(), 3);
        assertEquals(result.getRows().get(0).get(0).toString(), "CISD");
        assertEquals(result.getRows().get(1).get(0).toString(), "TESTGROUP");
        assertEquals(result.getRows().get(2).get(0).toString(), "TEST-SPACE");
    }

    @Test
    public void testExecuteWithParametersNeededAndProvided()
    {
        Query query = createQuery(TEST_USER, PASSWORD, selectPropertyTypeCodeAndDescriptionQueryCreation(DB_OPENBIS_METADATA_ID));

        TableModel result = executeQuery(TEST_USER, PASSWORD, query.getPermId(), Collections.singletonMap("code", "EYE_COLOR"));

        assertEquals(result.getRows().size(), 1);
        assertEquals(result.getRows().get(0).get(0).toString(), "EYE_COLOR");
        assertEquals(result.getRows().get(0).get(1).toString(), "The color of the eyes");
    }

    @Test
    public void testExecuteWithParametersNeededAndNotProvided()
    {
        Query query = createQuery(TEST_USER, PASSWORD, selectPropertyTypeCodeAndDescriptionQueryCreation(DB_OPENBIS_METADATA_ID));

        assertRuntimeException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    executeQuery(TEST_USER, PASSWORD, query.getPermId(), null);
                }
            }, "The following variables are not bound: code");
    }

    @Test
    public void testExecuteWithParametersNotNeededAndProvided()
    {
        Query query = createQuery(TEST_USER, PASSWORD, selectSpaceCodesQueryCreation(DB_OPENBIS_METADATA_ID));

        assertRuntimeException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    executeQuery(TEST_USER, PASSWORD, query.getPermId(), Collections.singletonMap("code", "EYE_COLOR"));
                }
            }, "Unknown variable 'code'");
    }

    @Test
    public void testExecuteWithDatabaseWithSpaceNullAndResultsFiltering()
    {
        Query query =
                createQuery(TEST_USER, PASSWORD, selectExperimentPermIdsAndIdentifiersQueryCreation(DB_OPENBIS_METADATA_ID));

        TableModel result = executeQuery(TEST_USER, PASSWORD, query.getPermId(), null);
        assertEquals(result.getRows().size(), 12);
        assertEquals(result.getRows().get(0).get(1).toString(), "/CISD/DEFAULT/EXP-REUSE");
        assertEquals(result.getRows().get(11).get(1).toString(), "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");

        result = executeQuery(TEST_POWER_USER_CISD, PASSWORD, query.getPermId(), null);
        assertEquals(result.getRows().size(), 9);
        assertEquals(result.getRows().get(0).get(1).toString(), "/CISD/DEFAULT/EXP-REUSE");
        assertEquals(result.getRows().get(8).get(1).toString(), "/CISD/NOE/EXP-TEST-2");
    }

    @Test
    public void testExecuteWithDatabaseWithSpaceNotNullAndNoResultsFiltering()
    {
        Query query = createQuery(TEST_USER, PASSWORD, selectExperimentPermIdsAndIdentifiersQueryCreation(new QueryDatabaseName(DB_TEST_CISD)));

        TableModel result = executeQuery(TEST_USER, PASSWORD, query.getPermId(), null);
        assertEquals(result.getRows().size(), 12);
        assertEquals(result.getRows().get(0).get(1).toString(), "/CISD/DEFAULT/EXP-REUSE");
        assertEquals(result.getRows().get(11).get(1).toString(), "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");

        result = executeQuery(TEST_POWER_USER_CISD, PASSWORD, query.getPermId(), null);
        assertEquals(result.getRows().size(), 12);
        assertEquals(result.getRows().get(0).get(1).toString(), "/CISD/DEFAULT/EXP-REUSE");
        assertEquals(result.getRows().get(11).get(1).toString(), "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
    }

    @Test
    public void testExecuteWithQueryThatBelongsToNonexistentDatabase()
    {
        QueryPE queryWithNonExistentDB = createQueryWithNonExistentDB(TEST_USER);

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    executeQuery(TEST_USER, PASSWORD, new QueryName(queryWithNonExistentDB.getName()), null);
                }
            }, new QueryDatabaseName("idontexist"));
    }

    @Test(dataProvider = PROVIDER_TRUE_FALSE)
    public void testExecuteWithOwner(boolean isPublic)
    {
        QueryCreation creation = selectSpaceCodesQueryCreation(new QueryDatabaseName(DB_TEST_CISD));
        creation.setPublic(isPublic);

        Query query = createQuery(TEST_POWER_USER_CISD, PASSWORD, creation);

        TableModel result = executeQuery(TEST_POWER_USER_CISD, PASSWORD, query.getPermId(), null);
        assertEquals(result.getRows().size(), 3);
    }

    @Test(dataProvider = PROVIDER_TRUE_FALSE)
    public void testExecuteWithNonOwner(boolean isPublic)
    {
        QueryCreation creation = selectSpaceCodesQueryCreation(new QueryDatabaseName(DB_TEST_CISD));
        creation.setPublic(isPublic);

        Query query = createQuery(TEST_NO_HOME_SPACE, PASSWORD, creation);

        if (isPublic)
        {
            TableModel result = executeQuery(TEST_POWER_USER_CISD, PASSWORD, query.getPermId(), null);
            assertEquals(result.getRows().size(), 3);
        } else
        {
            assertUnauthorizedObjectAccessException(new IDelegatedAction()
                {
                    @Override
                    public void execute()
                    {
                        executeQuery(TEST_POWER_USER_CISD, PASSWORD, query.getPermId(), null);
                    }
                }, query.getPermId());
        }
    }

    @Test(dataProvider = PROVIDER_TRUE_FALSE)
    public void testExecuteWithNonOwnerInstanceAdmin(boolean isPublic)
    {
        QueryCreation creation = selectSpaceCodesQueryCreation(new QueryDatabaseName(DB_TEST_CISD));
        creation.setPublic(isPublic);

        Query query = createQuery(TEST_NO_HOME_SPACE, PASSWORD, creation);

        TableModel result = executeQuery(TEST_USER, PASSWORD, query.getPermId(), null);
        assertEquals(result.getRows().size(), 3);
    }

    @Test
    public void testExecuteWithDatabaseWithSpaceNullUsingProjectObserver()
    {
        Person user = createUser(Role.OBSERVER, null, new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT"));
        Query query = createQuery(TEST_USER, PASSWORD, selectSpaceCodesQueryCreation(DB_OPENBIS_METADATA_ID));

        TableModel result = executeQuery(user.getUserId(), PASSWORD, query.getPermId(), null);
        assertEquals(result.getRows().size(), 3);
    }

    @Test
    public void testExecuteWithDatabaseWithSpaceNotNullUsingProjectObserver()
    {
        Person user = createUser(Role.OBSERVER, null, new ProjectIdentifier("/CISD/DEFAULT"));

        IQueryDatabaseId databaseId = new QueryDatabaseName(DB_TEST_CISD);
        Query query = createQuery(TEST_USER, PASSWORD, selectSpaceCodesQueryCreation(databaseId));

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    executeQuery(user.getUserId(), PASSWORD, query.getPermId(), null);
                }
            }, databaseId);
    }

    @Test
    public void testExecuteWithDatabaseWithSpaceNotNullUsingSpaceObserverWithMatchingSpace()
    {
        Person user = createUser(Role.OBSERVER, new SpacePermId("CISD"), null);
        Query query = createQuery(TEST_USER, PASSWORD, selectSpaceCodesQueryCreation(new QueryDatabaseName(DB_TEST_CISD)));

        TableModel result = executeQuery(user.getUserId(), PASSWORD, query.getPermId(), null);
        assertEquals(result.getRows().size(), 3);
    }

    @Test
    public void testExecuteWithDatabaseWithSpaceNotNullUsingSpaceObserverWithNonMatchingSpace()
    {
        Person user = createUser(Role.OBSERVER, new SpacePermId("TEST-SPACE"), null);

        IQueryDatabaseId databaseId = new QueryDatabaseName(DB_TEST_CISD);
        Query query = createQuery(TEST_USER, PASSWORD, selectSpaceCodesQueryCreation(databaseId));

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    executeQuery(user.getUserId(), PASSWORD, query.getPermId(), null);
                }
            }, databaseId);
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        Query query = createQuery(TEST_USER, PASSWORD, selectPropertyTypeCodeAndDescriptionQueryCreation(DB_OPENBIS_METADATA_ID));

        QueryExecutionOptions o = new QueryExecutionOptions();
        o.withParameter("code", "abc");

        v3api.executeQuery(sessionToken, new QueryName(query.getName()), o);

        assertAccessLog(
                "execute-query  QUERY_ID('" + query.getName() + "') EXECUTION_OPTIONS('QueryExecutionOptions: parameterKeys=[code]')");
    }

}

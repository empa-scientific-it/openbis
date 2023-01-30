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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.execute.SqlExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.QueryDatabaseName;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.Role;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;

/**
 * @author pkupczyk
 */
public class ExecuteSqlTest extends AbstractQueryTest
{

    @Test
    public void testExecute()
    {
        TableModel result = executeSql(TEST_USER, PASSWORD, SELECT_SPACE_CODES_SQL, DB_OPENBIS_METADATA_ID, null);

        assertEquals(result.getRows().size(), 3);
        assertEquals(result.getRows().get(0).get(0).toString(), "CISD");
        assertEquals(result.getRows().get(1).get(0).toString(), "TESTGROUP");
        assertEquals(result.getRows().get(2).get(0).toString(), "TEST-SPACE");
    }

    @Test
    public void testExecuteWithParametersNeededAndProvided()
    {
        TableModel result = executeSql(TEST_USER, PASSWORD, SELECT_PROPERTY_TYPE_CODE_AND_DESCRIPTION_SQL, DB_OPENBIS_METADATA_ID,
                Collections.singletonMap("code", "EYE_COLOR"));

        assertEquals(result.getRows().size(), 1);
        assertEquals(result.getRows().get(0).get(0).toString(), "EYE_COLOR");
        assertEquals(result.getRows().get(0).get(1).toString(), "The color of the eyes");
    }

    @Test
    public void testExecuteWithParametersNeededAndNotProvided()
    {
        assertRuntimeException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    executeSql(TEST_USER, PASSWORD, SELECT_PROPERTY_TYPE_CODE_AND_DESCRIPTION_SQL, DB_OPENBIS_METADATA_ID, null);
                }
            }, "The following variables are not bound: code");
    }

    @Test
    public void testExecuteWithParametersNotNeededAndProvided()
    {
        assertRuntimeException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    executeSql(TEST_USER, PASSWORD, SELECT_SPACE_CODES_SQL, DB_OPENBIS_METADATA_ID, Collections.singletonMap("code", "EYE_COLOR"));
                }
            }, "Unknown variable 'code'");
    }

    @Test
    public void testExecuteWithDatabaseNonexistent()
    {
        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    executeSql(TEST_USER, PASSWORD, SELECT_SPACE_CODES_SQL, new QueryDatabaseName("idontexist"), null);
                }
            }, new QueryDatabaseName("idontexist"));
    }

    @Test
    public void testExecuteWithDatabaseWithSpaceNullUsingSpaceObserver()
    {
        Person user = createUser(Role.OBSERVER, new SpacePermId("TEST-SPACE"), null);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    executeSql(user.getUserId(), PASSWORD, SELECT_SPACE_CODES_SQL, DB_OPENBIS_METADATA_ID, null);
                }
            }, DB_OPENBIS_METADATA_ID);
    }

    @Test
    public void testExecuteWithDatabaseWithSpaceNullUsingInstanceObserver()
    {
        Person user = createUser(Role.OBSERVER, null, null);
        TableModel result = executeSql(user.getUserId(), PASSWORD, SELECT_SPACE_CODES_SQL, DB_OPENBIS_METADATA_ID, null);
        assertEquals(result.getRows().size(), 3);
    }

    @Test
    public void testExecuteWithDatabaseWithSpaceNotNullUsingProjectAdmin()
    {
        Person user = createUser(Role.ADMIN, null, new ProjectIdentifier("/CISD/DEFAULT"));

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    executeSql(user.getUserId(), PASSWORD, SELECT_SPACE_CODES_SQL, DB_TEST_CISD_ID, null);
                }
            }, DB_TEST_CISD_ID);
    }

    @Test
    public void testExecuteWithDatabaseWithSpaceNotNullUsingSpacePowerUserWithMatchingSpace()
    {
        Person user = createUser(Role.POWER_USER, new SpacePermId("CISD"), null);

        TableModel result = executeSql(user.getUserId(), PASSWORD, SELECT_SPACE_CODES_SQL, DB_TEST_CISD_ID, null);
        assertEquals(result.getRows().size(), 3);
    }

    @Test
    public void testExecuteWithDatabaseWithSpaceNotNullUsingSpacePowerUserWithNonMatchingSpace()
    {
        Person user = createUser(Role.POWER_USER, new SpacePermId("TEST-SPACE"), null);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    executeSql(user.getUserId(), PASSWORD, SELECT_SPACE_CODES_SQL, DB_TEST_CISD_ID, null);
                }
            }, DB_TEST_CISD_ID);
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SqlExecutionOptions o = new SqlExecutionOptions();
        o.withDatabaseId(DB_OPENBIS_METADATA_ID);
        o.withParameter("code", "abc");

        v3api.executeSql(sessionToken, SELECT_PROPERTY_TYPE_CODE_AND_DESCRIPTION_SQL, o);

        assertAccessLog(
                "execute-sql  SQL('" + SELECT_PROPERTY_TYPE_CODE_AND_DESCRIPTION_SQL
                        + "') EXECUTION_OPTIONS('SqlExecutionOptions: databaseId=1, parameterKeys=[code]')");
    }

}

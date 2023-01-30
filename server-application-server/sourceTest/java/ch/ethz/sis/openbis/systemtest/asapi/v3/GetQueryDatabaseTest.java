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
import static org.testng.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.QueryDatabase;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.fetchoptions.QueryDatabaseFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.IQueryDatabaseId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.QueryDatabaseName;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.Role;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.RoleLevel;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.openbis.systemtest.authorization.ProjectAuthorizationUser;

/**
 * @author pkupczyk
 */
public class GetQueryDatabaseTest extends AbstractQueryTest
{

    @Test
    public void testGetByName()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        QueryDatabaseFetchOptions fo = new QueryDatabaseFetchOptions();
        fo.withSpace();

        Map<IQueryDatabaseId, QueryDatabase> map =
                v3api.getQueryDatabases(sessionToken, Arrays.asList(DB_TEST_CISD_ID), fo);

        assertEquals(1, map.size());

        QueryDatabase database = map.get(DB_TEST_CISD_ID);
        assertEquals(database.getPermId(), new QueryDatabaseName("test-database"));
        assertEquals(database.getName(), "test-database");
        assertEquals(database.getLabel(), "Test Database");
        assertEquals(database.getCreatorMinimalRole(), Role.POWER_USER);
        assertEquals(database.getCreatorMinimalRoleLevel(), RoleLevel.SPACE);
        assertEquals(database.getSpace().getCode(), "CISD");

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetByIdsNonexistent()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        QueryDatabaseName id2 = new QueryDatabaseName("IDONTEXIST");

        QueryDatabaseFetchOptions fo = new QueryDatabaseFetchOptions();
        fo.withSpace();

        Map<IQueryDatabaseId, QueryDatabase> map =
                v3api.getQueryDatabases(sessionToken, Arrays.asList(DB_OPENBIS_METADATA_ID, id2), fo);

        assertEquals(1, map.size());

        QueryDatabase database = map.get(DB_OPENBIS_METADATA_ID);
        assertEquals(database.getPermId(), DB_OPENBIS_METADATA_ID);
        assertEquals(database.getName(), DB_OPENBIS_METADATA);
        assertEquals(database.getLabel(), "openBIS meta data");
        assertEquals(database.getCreatorMinimalRole(), Role.OBSERVER);
        assertEquals(database.getCreatorMinimalRoleLevel(), RoleLevel.INSTANCE);
        assertEquals(database.getSpace(), null);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetByIdsDuplicated()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        QueryDatabaseFetchOptions fo = new QueryDatabaseFetchOptions();
        fo.withSpace();

        Map<IQueryDatabaseId, QueryDatabase> map =
                v3api.getQueryDatabases(sessionToken, Arrays.asList(DB_OPENBIS_METADATA_ID, DB_OPENBIS_METADATA_ID), fo);

        assertEquals(1, map.size());

        QueryDatabase database = map.get(DB_OPENBIS_METADATA_ID);
        assertEquals(database.getPermId(), DB_OPENBIS_METADATA_ID);
        assertEquals(database.getName(), DB_OPENBIS_METADATA);
        assertEquals(database.getLabel(), "openBIS meta data");
        assertEquals(database.getCreatorMinimalRole(), Role.OBSERVER);
        assertEquals(database.getCreatorMinimalRoleLevel(), RoleLevel.INSTANCE);
        assertEquals(database.getSpace(), null);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetByIdsUnauthorized()
    {
        List<IQueryDatabaseId> databaseIds = Arrays.asList(DB_OPENBIS_METADATA_ID, DB_TEST_CISD_ID);

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        Map<IQueryDatabaseId, QueryDatabase> map =
                v3api.getQueryDatabases(sessionToken, databaseIds, new QueryDatabaseFetchOptions());
        assertEquals(map.size(), 2);

        v3api.logout(sessionToken);

        sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);
        map = v3api.getQueryDatabases(sessionToken, databaseIds, new QueryDatabaseFetchOptions());
        assertEquals(map.size(), 1);
        assertNotNull(map.get(DB_OPENBIS_METADATA_ID));

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetByIdsWithFetchOptionsEmpty()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        QueryDatabaseFetchOptions fo = new QueryDatabaseFetchOptions();

        Map<IQueryDatabaseId, QueryDatabase> map =
                v3api.getQueryDatabases(sessionToken, Arrays.asList(DB_TEST_CISD_ID), fo);

        assertEquals(1, map.size());

        QueryDatabase database = map.get(DB_TEST_CISD_ID);
        assertEquals(database.getPermId(), DB_TEST_CISD_ID);
        assertEquals(database.getName(), DB_TEST_CISD);
        assertEquals(database.getLabel(), "Test Database");
        assertEquals(database.getCreatorMinimalRole(), Role.POWER_USER);
        assertEquals(database.getCreatorMinimalRoleLevel(), RoleLevel.SPACE);

        assertSpaceNotFetched(database);

        v3api.logout(sessionToken);
    }

    @Test
    public void testGetByIdsWithSpace()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        QueryDatabaseFetchOptions fo = new QueryDatabaseFetchOptions();
        fo.withSpace();

        Map<IQueryDatabaseId, QueryDatabase> map =
                v3api.getQueryDatabases(sessionToken, Arrays.asList(DB_TEST_CISD_ID), fo);

        assertEquals(1, map.size());

        QueryDatabase database = map.get(DB_TEST_CISD_ID);
        assertEquals(database.getPermId(), DB_TEST_CISD_ID);
        assertEquals(database.getName(), DB_TEST_CISD);
        assertEquals(database.getLabel(), "Test Database");
        assertEquals(database.getCreatorMinimalRole(), Role.POWER_USER);
        assertEquals(database.getCreatorMinimalRoleLevel(), RoleLevel.SPACE);
        assertEquals(database.getSpace().getCode(), "CISD");

        v3api.logout(sessionToken);

    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testGetWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String sessionToken = v3api.login(user.getUserId(), PASSWORD);

        QueryDatabaseFetchOptions fetchOptions = new QueryDatabaseFetchOptions();
        if (user.isDisabledProjectUser())
        {
            assertAuthorizationFailureException(new IDelegatedAction()
                {
                    @Override
                    public void execute()
                    {
                        v3api.getQueryDatabases(sessionToken, Arrays.asList(DB_OPENBIS_METADATA_ID), fetchOptions);
                    }
                });
        } else
        {
            Map<IQueryDatabaseId, QueryDatabase> map =
                    v3api.getQueryDatabases(sessionToken, Arrays.asList(DB_OPENBIS_METADATA_ID), fetchOptions);
            assertEquals(map.size(), 1);
        }

        v3api.logout(sessionToken);
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        QueryDatabaseFetchOptions fo = new QueryDatabaseFetchOptions();
        fo.withSpace();

        v3api.getQueryDatabases(sessionToken, Arrays.asList(DB_OPENBIS_METADATA_ID, DB_TEST_CISD_ID), fo);

        assertAccessLog("get-query-databases  QUERY_DATABASE_IDS('[1, test-database]') FETCH_OPTIONS('QueryDatabase\n    with Space\n')");
    }

}

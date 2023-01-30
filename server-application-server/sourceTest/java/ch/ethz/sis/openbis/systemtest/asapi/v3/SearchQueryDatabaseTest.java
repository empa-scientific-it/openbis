/*
 * Copyright 2018 ETH Zuerich, SIS
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

import java.util.List;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.QueryDatabase;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.fetchoptions.QueryDatabaseFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.search.QueryDatabaseSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.Role;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.RoleLevel;

/**
 * @author pkupczyk
 */
public class SearchQueryDatabaseTest extends AbstractQueryTest
{

    @Test
    public void testSearchAll()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        QueryDatabaseFetchOptions fo = new QueryDatabaseFetchOptions();
        fo.withSpace();

        List<QueryDatabase> databases =
                v3api.searchQueryDatabases(sessionToken, new QueryDatabaseSearchCriteria(), fo).getObjects();

        assertEquals(databases.size(), 2);

        QueryDatabase database1 = databases.get(0);
        assertEquals(database1.getName(), DB_OPENBIS_METADATA);
        assertEquals(database1.getLabel(), "openBIS meta data");
        assertEquals(database1.getCreatorMinimalRole(), Role.OBSERVER);
        assertEquals(database1.getCreatorMinimalRoleLevel(), RoleLevel.INSTANCE);
        assertEquals(database1.getSpace(), null);

        QueryDatabase database2 = databases.get(1);
        assertEquals(database2.getName(), DB_TEST_CISD);
        assertEquals(database2.getLabel(), "Test Database");
        assertEquals(database2.getCreatorMinimalRole(), Role.POWER_USER);
        assertEquals(database2.getCreatorMinimalRoleLevel(), RoleLevel.SPACE);
        assertEquals(database2.getSpace().getCode(), "CISD");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithId()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        QueryDatabaseSearchCriteria criteria = new QueryDatabaseSearchCriteria();
        criteria.withId().thatEquals(DB_OPENBIS_METADATA_ID);

        QueryDatabaseFetchOptions fo = new QueryDatabaseFetchOptions();
        fo.withSpace();

        List<QueryDatabase> databases =
                v3api.searchQueryDatabases(sessionToken, criteria, fo).getObjects();

        assertEquals(databases.size(), 1);

        QueryDatabase database1 = databases.get(0);
        assertEquals(database1.getName(), DB_OPENBIS_METADATA);
        assertEquals(database1.getLabel(), "openBIS meta data");
        assertEquals(database1.getCreatorMinimalRole(), Role.OBSERVER);
        assertEquals(database1.getCreatorMinimalRoleLevel(), RoleLevel.INSTANCE);
        assertEquals(database1.getSpace(), null);

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithIds()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        QueryDatabaseSearchCriteria criteria = new QueryDatabaseSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(DB_TEST_CISD_ID);
        criteria.withId().thatEquals(DB_OPENBIS_METADATA_ID);

        QueryDatabaseFetchOptions fo = new QueryDatabaseFetchOptions();
        fo.withSpace();

        List<QueryDatabase> databases =
                v3api.searchQueryDatabases(sessionToken, criteria, fo).getObjects();

        assertEquals(databases.size(), 2);

        QueryDatabase database1 = databases.get(0);
        assertEquals(database1.getName(), DB_OPENBIS_METADATA);
        assertEquals(database1.getLabel(), "openBIS meta data");
        assertEquals(database1.getCreatorMinimalRole(), Role.OBSERVER);
        assertEquals(database1.getCreatorMinimalRoleLevel(), RoleLevel.INSTANCE);
        assertEquals(database1.getSpace(), null);

        QueryDatabase database2 = databases.get(1);
        assertEquals(database2.getName(), DB_TEST_CISD);
        assertEquals(database2.getLabel(), "Test Database");
        assertEquals(database2.getCreatorMinimalRole(), Role.POWER_USER);
        assertEquals(database2.getCreatorMinimalRoleLevel(), RoleLevel.SPACE);
        assertEquals(database2.getSpace().getCode(), "CISD");

        v3api.logout(sessionToken);
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        QueryDatabaseSearchCriteria criteria = new QueryDatabaseSearchCriteria();
        criteria.withId().thatEquals(DB_OPENBIS_METADATA_ID);

        QueryDatabaseFetchOptions fo = new QueryDatabaseFetchOptions();
        fo.withSpace();

        v3api.searchQueryDatabases(sessionToken, criteria, fo).getObjects();

        assertAccessLog(
                "search-query-databases  SEARCH_CRITERIA:\n'QUERY_DATABASE\n    with id '1'\n'\nFETCH_OPTIONS:\n'QueryDatabase\n    with Space\n'");
    }

}

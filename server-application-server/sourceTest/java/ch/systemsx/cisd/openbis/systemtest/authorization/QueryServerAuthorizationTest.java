/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.systemtest.authorization;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.QueryType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.NewQuery;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryDatabase;
import ch.systemsx.cisd.openbis.systemtest.base.BaseTest;

/**
 * @author Franz-Josef Elmer
 */
public class QueryServerAuthorizationTest extends BaseTest
{
    @Test(expectedExceptions = AuthorizationFailureException.class)
    public void testRegisterQueryByUnauthorizedUser()
    {
        Space space = create(aSpace());
        String sessionToken =
                create(aSession().withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, space));
        int databases = queryServer.initDatabases(sessionToken);
        assertEquals(1, databases);
        QueryDatabase database = queryServer.listQueryDatabases(sessionToken).get(0);
        NewQuery query = new NewQuery();
        query.setExpression("select * from sample_types order by code");
        query.setName("List sample types");
        query.setQueryType(QueryType.GENERIC);
        query.setQueryDatabase(database);

        queryServer.registerQuery(sessionToken, query);
    }
}

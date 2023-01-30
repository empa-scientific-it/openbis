/*
 * Copyright 2017 ETH Zuerich, SIS
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
import java.util.Map;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.AuthorizationGroup;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.create.AuthorizationGroupCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.delete.AuthorizationGroupDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.fetchoptions.AuthorizationGroupFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.id.AuthorizationGroupPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.id.IAuthorizationGroupId;
import ch.systemsx.cisd.common.action.IDelegatedAction;

/**
 * @author Franz-Josef Elmer
 */
public class DeleteAuthorizationGroupTest extends AbstractTest
{
    @Test
    public void testDeleteAthorizationGroup()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        AuthorizationGroupCreation groupCreation = new AuthorizationGroupCreation();
        groupCreation.setCode("MY-GROUp");
        AuthorizationGroupPermId permId = v3api.createAuthorizationGroups(sessionToken, Arrays.asList(groupCreation)).get(0);
        assertEquals(permId.getPermId(), groupCreation.getCode().toUpperCase());
        AuthorizationGroupFetchOptions fetchOptions = new AuthorizationGroupFetchOptions();
        AuthorizationGroup group = v3api.getAuthorizationGroups(sessionToken, Arrays.asList(permId), fetchOptions).get(permId);
        assertEquals(group.getCode(), groupCreation.getCode().toUpperCase());
        AuthorizationGroupDeletionOptions deletionOptions = new AuthorizationGroupDeletionOptions();
        deletionOptions.setReason("testing");

        // When
        v3api.deleteAuthorizationGroups(sessionToken, Arrays.asList(permId), deletionOptions);

        // Then
        Map<IAuthorizationGroupId, AuthorizationGroup> map = v3api.getAuthorizationGroups(sessionToken, Arrays.asList(permId), fetchOptions);
        assertEquals(map.toString(), "{}");

        v3api.logout(sessionToken);
    }

    @Test(dataProvider = "usersNotAllowedToDeleteAuthorizationGroups")
    public void testDeleteWithUserCausingAuthorizationFailure(final String user)
    {
        assertAnyAuthorizationException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken = v3api.login(user, PASSWORD);
                    AuthorizationGroupDeletionOptions deletionOptions = new AuthorizationGroupDeletionOptions();
                    deletionOptions.setReason("testing");
                    v3api.deleteAuthorizationGroups(sessionToken, Arrays.asList(new AuthorizationGroupPermId("AGROUP")), deletionOptions);
                }
            });
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        AuthorizationGroupDeletionOptions o = new AuthorizationGroupDeletionOptions();
        o.setReason("test-reason");

        v3api.deleteAuthorizationGroups(sessionToken,
                Arrays.asList(new AuthorizationGroupPermId("TEST-LOGGING-1"), new AuthorizationGroupPermId("TEST-LOGGING-2")), o);

        assertAccessLog(
                "delete-authorization-groups  AUTHORIZATION_GROUP_IDS('[TEST-LOGGING-1, TEST-LOGGING-2]') DELETION_OPTIONS('AuthorizationGroupDeletionOptions[reason=test-reason]')");
    }

    @DataProvider
    Object[][] usersNotAllowedToDeleteAuthorizationGroups()
    {
        String[] users = { TEST_GROUP_ADMIN, TEST_GROUP_OBSERVER, TEST_GROUP_POWERUSER, TEST_INSTANCE_OBSERVER,
                TEST_OBSERVER_CISD, TEST_POWER_USER_CISD, TEST_SPACE_USER };
        Object[][] objects = new Object[users.length][];
        for (int i = 0; i < users.length; i++)
        {
            objects[i] = new Object[] { users[i] };
        }
        return objects;
    }

}

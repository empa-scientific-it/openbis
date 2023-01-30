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
import java.util.List;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.id.AuthorizationGroupPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.Me;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.PersonPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.Role;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.RoleAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.RoleLevel;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.create.RoleAssignmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.fetchoptions.RoleAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.id.RoleAssignmentTechId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;

/**
 * @author Franz-Josef Elmer
 */
public class CreateRoleAssignmentTest extends AbstractTest
{
    @Test
    public void testCreateInstanceETLServerUser()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        RoleAssignmentCreation creation = new RoleAssignmentCreation();
        creation.setRole(Role.ETL_SERVER);
        creation.setUserId(new PersonPermId(TEST_OBSERVER_CISD));

        // When
        List<RoleAssignmentTechId> ids = v3api.createRoleAssignments(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(ids.size(), 1);
        RoleAssignmentFetchOptions fetchOptions = new RoleAssignmentFetchOptions();
        fetchOptions.withUser();
        fetchOptions.withSpace();
        fetchOptions.withProject();
        RoleAssignment roleAssignment = v3api.getRoleAssignments(sessionToken, ids, fetchOptions).get(ids.get(0));
        assertEquals(roleAssignment.getUser().getUserId(), TEST_OBSERVER_CISD);
        assertEquals(roleAssignment.getRole(), Role.ETL_SERVER);
        assertEquals(roleAssignment.getRoleLevel(), RoleLevel.INSTANCE);
        assertEquals(roleAssignment.getSpace(), null);
        assertEquals(roleAssignment.getProject(), null);
    }

    @Test
    public void testCreateRoleForMe()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        RoleAssignmentCreation creation = new RoleAssignmentCreation();
        creation.setRole(Role.POWER_USER);
        creation.setSpaceId(new SpacePermId("TEST-SPACE"));
        creation.setUserId(new Me());

        // When
        List<RoleAssignmentTechId> ids = v3api.createRoleAssignments(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(ids.size(), 1);
        RoleAssignmentFetchOptions fetchOptions = new RoleAssignmentFetchOptions();
        fetchOptions.withUser();
        fetchOptions.withSpace();
        fetchOptions.withProject();
        RoleAssignment roleAssignment = v3api.getRoleAssignments(sessionToken, ids, fetchOptions).get(ids.get(0));
        assertEquals(roleAssignment.getUser().getUserId(), TEST_USER);
        assertEquals(roleAssignment.getRole(), Role.POWER_USER);
        assertEquals(roleAssignment.getRoleLevel(), RoleLevel.SPACE);
        assertEquals(roleAssignment.getSpace().getCode(), "TEST-SPACE");
        assertEquals(roleAssignment.getProject(), null);
    }

    @Test
    public void testCreateSpaceETLServerUser()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        RoleAssignmentCreation creation = new RoleAssignmentCreation();
        creation.setRole(Role.ETL_SERVER);
        creation.setUserId(new PersonPermId(TEST_OBSERVER_CISD));
        creation.setSpaceId(new SpacePermId("CISD"));

        // When
        List<RoleAssignmentTechId> ids = v3api.createRoleAssignments(sessionToken, Arrays.asList(creation));

        // Then
        assertEquals(ids.size(), 1);
        RoleAssignmentFetchOptions fetchOptions = new RoleAssignmentFetchOptions();
        fetchOptions.withSpace();
        fetchOptions.withProject();
        RoleAssignment roleAssignment = v3api.getRoleAssignments(sessionToken, ids, fetchOptions).get(ids.get(0));
        assertEquals(roleAssignment.getRole(), Role.ETL_SERVER);
        assertEquals(roleAssignment.getRoleLevel(), RoleLevel.SPACE);
        assertEquals(roleAssignment.getSpace().getCode(), "CISD");
        assertEquals(roleAssignment.getProject(), null);
    }

    @Test(dataProvider = "usersNotAllowedToCreateInstanceRoleAssignment")
    public void testCreateInstanceRoleAssignmentWithUserCausingAuthorizationFailure(final String user)
    {
        assertAnyAuthorizationException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken = v3api.login(user, PASSWORD);
                    RoleAssignmentCreation creation = new RoleAssignmentCreation();
                    creation.setRole(Role.ADMIN);
                    creation.setUserId(new PersonPermId(TEST_OBSERVER_CISD));
                    v3api.createRoleAssignments(sessionToken, Arrays.asList(creation));
                }
            });
    }

    @DataProvider
    Object[][] usersNotAllowedToCreateInstanceRoleAssignment()
    {
        return createTestUsersProvider(TEST_GROUP_ADMIN, TEST_INSTANCE_ETLSERVER, TEST_GROUP_OBSERVER,
                TEST_GROUP_POWERUSER, TEST_INSTANCE_OBSERVER, TEST_OBSERVER_CISD, TEST_POWER_USER_CISD,
                TEST_SPACE_USER);
    }

    @Test(dataProvider = "usersNotAllowedToCreateRoleAssignmentForSpaceCISD")
    public void testCreateSpaceRoleAssignmentWithUserCausingAuthorizationFailure(final String user)
    {
        assertAnyAuthorizationException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken = v3api.login(user, PASSWORD);
                    RoleAssignmentCreation creation = new RoleAssignmentCreation();
                    creation.setRole(Role.USER);
                    creation.setSpaceId(new SpacePermId("CISD"));
                    creation.setUserId(new PersonPermId(TEST_INSTANCE_ETLSERVER));
                    v3api.createRoleAssignments(sessionToken, Arrays.asList(creation));
                }
            });
    }

    @DataProvider
    Object[][] usersNotAllowedToCreateRoleAssignmentForSpaceCISD()
    {
        return createTestUsersProvider(TEST_GROUP_ADMIN, TEST_INSTANCE_ETLSERVER, TEST_GROUP_OBSERVER,
                TEST_GROUP_POWERUSER, TEST_INSTANCE_OBSERVER, TEST_OBSERVER_CISD, TEST_POWER_USER_CISD,
                TEST_SPACE_USER);
    }

    @Test(dataProvider = "usersAllowedToCreateRoleAssignmentForSpaceCISD")
    public void testCreateSpaceRoleAssignmentWithUser(final String user)
    {
        String sessionToken = v3api.login(user, PASSWORD);
        RoleAssignmentCreation creation = new RoleAssignmentCreation();
        creation.setRole(Role.USER);
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setUserId(new PersonPermId(TEST_INSTANCE_ETLSERVER));
        v3api.createRoleAssignments(sessionToken, Arrays.asList(creation));
    }

    @DataProvider
    Object[][] usersAllowedToCreateRoleAssignmentForSpaceCISD()
    {
        return createTestUsersProvider(TEST_USER, TEST_ROLE_V3);
    }

    @Test(dataProvider = "usersNotAllowedToCreateRoleAssignmentForProjectTEST_PROJECT")
    public void testCreateProjectRoleAssignmentWithUserCausingAuthorizationFailure(final String user)
    {
        assertAnyAuthorizationException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken = v3api.login(user, PASSWORD);
                    RoleAssignmentCreation creation = new RoleAssignmentCreation();
                    creation.setRole(Role.USER);
                    creation.setProjectId(new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT"));
                    creation.setUserId(new PersonPermId(TEST_INSTANCE_ETLSERVER));
                    v3api.createRoleAssignments(sessionToken, Arrays.asList(creation));
                }
            });
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        RoleAssignmentCreation creation = new RoleAssignmentCreation();
        creation.setRole(Role.POWER_USER);
        creation.setSpaceId(new SpacePermId("TEST-SPACE"));
        creation.setUserId(new Me());

        RoleAssignmentCreation creation2 = new RoleAssignmentCreation();
        creation2.setRole(Role.ADMIN);
        creation2.setProjectId(new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT"));
        creation2.setUserId(new PersonPermId("test"));

        RoleAssignmentCreation creation3 = new RoleAssignmentCreation();
        creation3.setRole(Role.OBSERVER);
        creation3.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation3.setAuthorizationGroupId(new AuthorizationGroupPermId("AGROUP"));

        v3api.createRoleAssignments(sessionToken, Arrays.asList(creation, creation2, creation3));

        assertAccessLog(
                "create-role-assignments  NEW_ROLE_ASSIGNMENTS('[RoleAssignmentCreation[groupId=<null>,userId=me,spaceId=TEST-SPACE,projectId=<null>,role=POWER_USER], RoleAssignmentCreation[groupId=<null>,userId=test,spaceId=<null>,projectId=/TEST-SPACE/TEST-PROJECT,role=ADMIN], RoleAssignmentCreation[groupId=AGROUP,userId=<null>,spaceId=<null>,projectId=/CISD/NEMO,role=OBSERVER]]')");
    }

    @DataProvider
    Object[][] usersNotAllowedToCreateRoleAssignmentForProjectTEST_PROJECT()
    {
        return createTestUsersProvider(TEST_GROUP_ADMIN, TEST_INSTANCE_ETLSERVER, TEST_GROUP_OBSERVER,
                TEST_GROUP_POWERUSER, TEST_INSTANCE_OBSERVER, TEST_OBSERVER_CISD, TEST_POWER_USER_CISD);
    }

    @Test(dataProvider = "usersAllowedToCreateRoleAssignmentForProjectTEST_PROJECT")
    public void testCreateProjectRoleAssignmentWithUser(final String user)
    {
        String sessionToken = v3api.login(user, PASSWORD);
        RoleAssignmentCreation creation = new RoleAssignmentCreation();
        creation.setRole(Role.USER);
        creation.setProjectId(new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT"));
        creation.setUserId(new PersonPermId(TEST_INSTANCE_ETLSERVER));
        v3api.createRoleAssignments(sessionToken, Arrays.asList(creation));
    }

    @DataProvider
    Object[][] usersAllowedToCreateRoleAssignmentForProjectTEST_PROJECT()
    {
        return createTestUsersProvider(TEST_USER, TEST_SPACE_USER);
    }

}

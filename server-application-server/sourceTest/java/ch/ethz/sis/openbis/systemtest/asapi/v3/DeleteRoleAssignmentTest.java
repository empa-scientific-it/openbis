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
import static org.testng.Assert.fail;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.PersonPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.Role;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.RoleAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.create.RoleAssignmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.delete.RoleAssignmentDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.fetchoptions.RoleAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.id.IRoleAssignmentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.id.RoleAssignmentTechId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.SpaceCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * @author Franz-Josef Elmer
 */
public class DeleteRoleAssignmentTest extends AbstractTest
{
    @Test
    public void testDelete()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SpaceCreation spaceCreation = new SpaceCreation();
        spaceCreation.setCode("TEST-" + System.currentTimeMillis());
        SpacePermId spacePermId = v3api.createSpaces(sessionToken, Arrays.asList(spaceCreation)).get(0);
        RoleAssignmentCreation creation = new RoleAssignmentCreation();
        creation.setRole(Role.POWER_USER);
        creation.setUserId(new PersonPermId(TEST_NO_HOME_SPACE));
        creation.setSpaceId(spacePermId);
        List<RoleAssignmentTechId> assignments = v3api.createRoleAssignments(sessionToken, Arrays.asList(creation));
        RoleAssignmentFetchOptions fetchOptions = new RoleAssignmentFetchOptions();
        fetchOptions.withSpace();
        RoleAssignment assignment = v3api.getRoleAssignments(sessionToken, assignments, fetchOptions).get(assignments.get(0));
        assertEquals(assignment.getSpace().getCode(), spaceCreation.getCode());
        RoleAssignmentDeletionOptions deletionOptions = new RoleAssignmentDeletionOptions();
        deletionOptions.setReason("test");

        // When
        v3api.deleteRoleAssignments(sessionToken, assignments, deletionOptions);

        // Then
        Map<IRoleAssignmentId, RoleAssignment> map = v3api.getRoleAssignments(sessionToken, assignments, fetchOptions);
        assertEquals(map.toString(), "{}");
        v3api.logout(sessionToken);
    }

    @Test
    public void testDeleteSpaceAdminRoleAssignmentBySameUser()
    {
        // Given
        String sessionToken = v3api.login(TEST_ROLE_V3, PASSWORD);
        RoleAssignmentDeletionOptions deletionOptions = new RoleAssignmentDeletionOptions();
        deletionOptions.setReason("testing");

        try
        {
            // Then
            // delete role assignment: user: test_v3, space: CISD, role: ADMIN
            v3api.deleteRoleAssignments(sessionToken, Arrays.asList(new RoleAssignmentTechId(11L)), deletionOptions);
            fail("UserFailureException expected");
        } catch (UserFailureException e)
        {
            // When
            assertEquals(e.getMessage(), "For safety reason you cannot give away your own space admin power. "
                    + "Ask instance admin to do that for you. (Context: [])");
        }
    }

    @Test(dataProvider = "usersNotAllowedToDeleteInstanceRoleAssignment")
    public void testDeleteInstanceRoleAssignmentWithUserCausingAuthorizationFailure(final String user)
    {
        assertAnyAuthorizationException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken = v3api.login(user, PASSWORD);
                    RoleAssignmentDeletionOptions deletionOptions = new RoleAssignmentDeletionOptions();
                    deletionOptions.setReason("testing");
                    // delete role assignment: user: test, role: ADMIN
                    v3api.deleteRoleAssignments(sessionToken, Arrays.asList(new RoleAssignmentTechId(1L)), deletionOptions);
                }
            });
    }

    @DataProvider
    Object[][] usersNotAllowedToDeleteInstanceRoleAssignment()
    {
        return createTestUsersProvider(TEST_GROUP_ADMIN, TEST_INSTANCE_ETLSERVER, TEST_GROUP_OBSERVER,
                TEST_GROUP_POWERUSER, TEST_INSTANCE_OBSERVER, TEST_OBSERVER_CISD, TEST_POWER_USER_CISD,
                TEST_SPACE_USER);
    }

    @Test(dataProvider = "usersNotAllowedToDeleteRoleAssignmentForSpaceCISD")
    public void testDeleteSpaceRoleAssignmentWithUserCausingAuthorizationFailure(final String user)
    {
        assertAnyAuthorizationException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken = v3api.login(user, PASSWORD);
                    RoleAssignmentDeletionOptions deletionOptions = new RoleAssignmentDeletionOptions();
                    deletionOptions.setReason("testing");
                    // delete role assignment: user: test_role, space: CISD, role: POWER_USER
                    v3api.deleteRoleAssignments(sessionToken, Arrays.asList(new RoleAssignmentTechId(8L)), deletionOptions);
                }
            });
    }

    @DataProvider
    Object[][] usersNotAllowedToDeleteRoleAssignmentForSpaceCISD()
    {
        return createTestUsersProvider(TEST_GROUP_ADMIN, TEST_INSTANCE_ETLSERVER, TEST_GROUP_OBSERVER,
                TEST_GROUP_POWERUSER, TEST_INSTANCE_OBSERVER, TEST_OBSERVER_CISD, TEST_POWER_USER_CISD,
                TEST_SPACE_USER);
    }

    @Test(dataProvider = "usersAllowedToDeleteRoleAssignmentForSpaceCISD")
    public void testDeleteSpaceRoleAssignmentWithUser(final String user)
    {
        String sessionToken = v3api.login(user, PASSWORD);
        RoleAssignmentDeletionOptions deletionOptions = new RoleAssignmentDeletionOptions();
        deletionOptions.setReason("testing");
        // delete role assignment: user: test_role, space: CISD, role: POWER_USER
        v3api.deleteRoleAssignments(sessionToken, Arrays.asList(new RoleAssignmentTechId(8L)), deletionOptions);
    }

    @DataProvider
    Object[][] usersAllowedToDeleteRoleAssignmentForSpaceCISD()
    {
        return createTestUsersProvider(TEST_USER, TEST_ROLE_V3);
    }

    @Test(dataProvider = "usersNotAllowedToDeleteRoleAssignmentForProjectTEST_PROJECT")
    public void testDeleteProjectRoleAssignmentWithUserCausingAuthorizationFailure(final String user)
    {
        assertAnyAuthorizationException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken = v3api.login(user, PASSWORD);
                    RoleAssignmentDeletionOptions deletionOptions = new RoleAssignmentDeletionOptions();
                    deletionOptions.setReason("testing");
                    // delete role assignment: user: test_project_pa_on, project: /TEST-SPACE/TEST-PROJECT, role: ADMIN
                    v3api.deleteRoleAssignments(sessionToken, Arrays.asList(new RoleAssignmentTechId(20L)), deletionOptions);
                }
            });
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        RoleAssignmentDeletionOptions o = new RoleAssignmentDeletionOptions();
        o.setReason("test-reason");

        v3api.deleteRoleAssignments(sessionToken, Arrays.asList(new RoleAssignmentTechId(10L), new RoleAssignmentTechId(20L)), o);

        assertAccessLog(
                "delete-role-assignments  ROLE_ASSIGNMENT_IDS('[10, 20]') DELETION_OPTIONS('RoleAssignmentDeletionOptions[reason=test-reason]')");
    }

    @DataProvider
    Object[][] usersNotAllowedToDeleteRoleAssignmentForProjectTEST_PROJECT()
    {
        return createTestUsersProvider(TEST_GROUP_ADMIN, TEST_INSTANCE_ETLSERVER, TEST_GROUP_OBSERVER,
                TEST_GROUP_POWERUSER, TEST_INSTANCE_OBSERVER, TEST_OBSERVER_CISD, TEST_POWER_USER_CISD);
    }

    @Test(dataProvider = "usersAllowedToDeleteRoleAssignmentForProjectTEST_PROJECT")
    public void testDeleteProjectRoleAssignmentWithUser(final String user)
    {
        String sessionToken = v3api.login(user, PASSWORD);
        RoleAssignmentDeletionOptions deletionOptions = new RoleAssignmentDeletionOptions();
        deletionOptions.setReason("testing");
        // delete role assignment: user: test_project_pa_on, project: /TEST-SPACE/TEST-PROJECT, role: ADMIN
        v3api.deleteRoleAssignments(sessionToken, Arrays.asList(new RoleAssignmentTechId(20L)), deletionOptions);
    }

    @DataProvider
    Object[][] usersAllowedToDeleteRoleAssignmentForProjectTEST_PROJECT()
    {
        return createTestUsersProvider(TEST_USER, TEST_SPACE_USER);
    }

}

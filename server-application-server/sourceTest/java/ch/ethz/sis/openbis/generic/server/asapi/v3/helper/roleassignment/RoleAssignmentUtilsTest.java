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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.roleassignment;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class RoleAssignmentUtilsTest
{
    private static final String USER = "a";
    private static final String ANOTHER_USER = "b";

    @Test
    public void testCheckForSelfreducingWithRoleAssignmentForUserGroup()
    {
        RoleAssignmentPE role = role("OBSERVER:S1");
        RoleAssignmentUtils.checkForSelfreducingAdminAuthorization(role, user(USER, "ADMIN"));
    }
    
    @Test
    public void testCheckForSelfreducingWithRoleAssignmentForDifferentUser()
    {
        RoleAssignmentPE role = role("OBSERVER:S1");
        role.setPersonInternal(user(ANOTHER_USER));
        RoleAssignmentUtils.checkForSelfreducingAdminAuthorization(role, user(USER, "ADMIN"));
    }
    
    @Test
    public void testCheckForSelfreducingWithNonAdminRoleAssignment()
    {
        RoleAssignmentPE role = role("OBSERVER:S1");
        role.setPersonInternal(user(USER));
        RoleAssignmentUtils.checkForSelfreducingAdminAuthorization(role, user(USER, "ADMIN"));
    }
    
    @Test
    public void testCheckForSelfreducingWithInstanceAdminRoleAssignmentOfSameInstanceAdmin()
    {
        RoleAssignmentPE role = role("ADMIN");
        role.setPersonInternal(user(USER));
        assertFailureBecauseOfSelfReducingInstanceAdminPower(role, user(USER, "OBSERVER", "ADMIN:S1", "ADMIN:S1:P1", "ADMIN"));
    }

    @Test
    public void testCheckForSelfreducingWithSpaceAdminRoleAssignmentOfSameSpaceAdmin()
    {
        RoleAssignmentPE role = role("ADMIN:S1");
        role.setPersonInternal(user(USER));
        assertFailurwBecauseOfSelfReducingSpaceAdminPower(role, user(USER, "ADMIN:S1"));
    }

    @Test
    public void testCheckForSelfreducingWithSpaceAdminRoleAssignmentOfSameInstanceAdmin()
    {
        RoleAssignmentPE role = role("ADMIN:S1");
        role.setPersonInternal(user(USER));
        RoleAssignmentUtils.checkForSelfreducingAdminAuthorization(role, user(USER, "ADMIN:S1", "ADMIN"));
    }
    
    @Test
    public void testCheckForSelfreducingWithProjectAdminRoleAssignmentOfSameProjectAdmin()
    {
        RoleAssignmentPE role = role("ADMIN:S1:P1");
        role.setPersonInternal(user(USER));
        assertFailurwBecauseOfSelfReducingProjectAdminPower(role, user(USER, "ADMIN:S1:P1"));
    }
    
    @Test
    public void testCheckForSelfreducingWithProjectAdminRoleAssignmentOfSameSpaceAdmin()
    {
        RoleAssignmentPE role = role("ADMIN:S1:P1");
        role.setPersonInternal(user(USER));
        RoleAssignmentUtils.checkForSelfreducingAdminAuthorization(role, user(USER, "ADMIN:S1:P1", "ADMIN:S1"));
    }
    
    @Test
    public void testCheckForSelfreducingWithProjectAdminRoleAssignmentOfSameInstanceAdmin()
    {
        RoleAssignmentPE role = role("ADMIN:S1:P1");
        role.setPersonInternal(user(USER));
        RoleAssignmentUtils.checkForSelfreducingAdminAuthorization(role, user(USER, "ADMIN:S1:P1", "ADMIN"));
    }
    
    private void assertFailurwBecauseOfSelfReducingProjectAdminPower(RoleAssignmentPE role, PersonPE user)
    {
        assertUserFailure(role, user, "For safety reason you cannot give away your own project admin power. "
                + "Ask space or instance admin to do that for you.");
    }
    
    private void assertFailurwBecauseOfSelfReducingSpaceAdminPower(RoleAssignmentPE role, PersonPE user)
    {
        assertUserFailure(role, user, "For safety reason you cannot give away your own space admin power. "
                + "Ask instance admin to do that for you.");
    }
    
    private void assertFailureBecauseOfSelfReducingInstanceAdminPower(RoleAssignmentPE role, PersonPE user)
    {
        assertUserFailure(role, user, "For safety reason you cannot give away your own omnipotence. "
                + "Ask another instance admin to do that for you.");
    }

    private void assertUserFailure(RoleAssignmentPE role, PersonPE user, String errorMessage)
    {
        try
        {
            RoleAssignmentUtils.checkForSelfreducingAdminAuthorization(role, user);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals(ex.getMessage(), errorMessage);
        }
    }
    
    private PersonPE user(String userId, String... roles)
    {
        PersonPE user = new PersonPE();
        user.setUserId(userId);
        for (String roleDefinition : roles)
        {
            RoleAssignmentPE roleAssignment = role(roleDefinition);
            roleAssignment.setPersonInternal(user);
            user.addRoleAssignment(roleAssignment);
        }
        return user;
    }

    private RoleAssignmentPE role(String roleDefinition)
    {
        RoleAssignmentPE roleAssignment = new RoleAssignmentPE();
        String[] parts = roleDefinition.split(":");
        roleAssignment.setRole(RoleCode.valueOf(parts[0]));
        SpacePE space = null;
        if (parts.length > 1)
        {
            space = new SpacePE();
            space.setCode(parts[1]);
        }
        ProjectPE project = null;
        if (parts.length == 3)
        {
            project = new ProjectPE();
            project.setSpace(space);
            project.setCode(parts[2]);
            roleAssignment.setProject(project);
        } else
        {
            roleAssignment.setSpace(space);
        }
        return roleAssignment;
    }

}

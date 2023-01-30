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

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Map;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.Role;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.RoleAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.RoleLevel;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.fetchoptions.RoleAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.id.IRoleAssignmentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.id.RoleAssignmentTechId;

/**
 * @author Franz-Josef Elmer
 */
public class GetRoleAssignmentsTest extends AbstractTest
{

    @Test
    public void testOnlyBasic()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        RoleAssignmentTechId id1 = new RoleAssignmentTechId(1L);
        RoleAssignmentTechId id2 = new RoleAssignmentTechId(2L);
        RoleAssignmentFetchOptions fetchOptions = new RoleAssignmentFetchOptions();

        // When
        Map<IRoleAssignmentId, RoleAssignment> map = v3api.getRoleAssignments(sessionToken, Arrays.asList(id1, id2), fetchOptions);

        // Then
        assertEquals(map.get(id1).getRole(), Role.ADMIN);
        assertEquals(map.get(id1).getRoleLevel(), RoleLevel.INSTANCE);
        assertEquals(map.get(id2).getRole(), Role.ADMIN);
        assertEquals(map.get(id2).getRoleLevel(), RoleLevel.SPACE);
        assertEquals(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(map.get(id2).getRegistrationDate()),
                "2008-11-05 09:18:11");
    }

    @Test
    public void testWithRegistratorAndUserAndGroup()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        RoleAssignmentTechId id1 = new RoleAssignmentTechId(29L);
        RoleAssignmentTechId id2 = new RoleAssignmentTechId(6L);
        RoleAssignmentFetchOptions fetchOptions = new RoleAssignmentFetchOptions();
        fetchOptions.withRegistrator();
        fetchOptions.withUser();
        fetchOptions.withAuthorizationGroup().withUsers();

        // When
        Map<IRoleAssignmentId, RoleAssignment> map = v3api.getRoleAssignments(sessionToken, Arrays.asList(id1, id2), fetchOptions);

        // Then
        assertEquals(map.get(id1).getRole(), Role.USER);
        assertEquals(map.get(id1).getRoleLevel(), RoleLevel.PROJECT);
        assertEquals(map.get(id1).getUser(), null);
        assertEquals(map.get(id1).getAuthorizationGroup().getCode(), "AGROUP");
        assertEquals(map.get(id1).getFetchOptions().hasRegistrator(), true);
        assertEquals(map.get(id1).getRegistrator().getUserId(), "test");
        assertEquals(map.get(id1).getFetchOptions().hasUser(), true);
        assertEquals(map.get(id1).getFetchOptions().hasAuthorizationGroup(), true);
        assertEquals(map.get(id1).getFetchOptions().hasProject(), false);
        assertEquals(map.get(id1).getFetchOptions().hasSpace(), false);
        assertEquals(map.get(id2).getRole(), Role.OBSERVER);
        assertEquals(map.get(id2).getRoleLevel(), RoleLevel.SPACE);
        assertEquals(map.get(id2).getUser().getUserId(), "observer");
        assertEquals(map.get(id2).getAuthorizationGroup(), null);
        assertEquals(map.get(id2).getFetchOptions().hasRegistrator(), true);
        assertEquals(map.get(id2).getRegistrator().getUserId(), "test");
        assertEquals(map.get(id2).getFetchOptions().hasUser(), true);
        assertEquals(map.get(id2).getFetchOptions().hasAuthorizationGroup(), true);
        assertEquals(map.get(id2).getFetchOptions().hasProject(), false);
        assertEquals(map.get(id2).getFetchOptions().hasSpace(), false);
    }

    @Test
    public void testWithSpaceAndProject()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        RoleAssignmentTechId id1 = new RoleAssignmentTechId(19L);
        RoleAssignmentTechId id2 = new RoleAssignmentTechId(6L);
        RoleAssignmentFetchOptions fetchOptions = new RoleAssignmentFetchOptions();
        fetchOptions.withProject().withSpace();
        fetchOptions.withSpace();

        // When
        Map<IRoleAssignmentId, RoleAssignment> map = v3api.getRoleAssignments(sessionToken, Arrays.asList(id1, id2), fetchOptions);

        // Then
        assertEquals(map.get(id1).getRole(), Role.ADMIN);
        assertEquals(map.get(id1).getRoleLevel(), RoleLevel.PROJECT);
        assertEquals(map.get(id1).getSpace(), null);
        assertEquals(map.get(id1).getProject().getIdentifier().getIdentifier(), "/TEST-SPACE/TEST-PROJECT");
        assertEquals(map.get(id1).getProject().getCode(), "TEST-PROJECT");
        assertEquals(map.get(id1).getProject().getSpace().getCode(), "TEST-SPACE");
        assertEquals(map.get(id1).getFetchOptions().hasUser(), false);
        assertEquals(map.get(id1).getFetchOptions().hasAuthorizationGroup(), false);
        assertEquals(map.get(id1).getFetchOptions().hasProject(), true);
        assertEquals(map.get(id1).getFetchOptions().hasSpace(), true);
        assertEquals(map.get(id2).getRole(), Role.OBSERVER);
        assertEquals(map.get(id2).getRoleLevel(), RoleLevel.SPACE);
        assertEquals(map.get(id2).getSpace().getCode(), "TESTGROUP");
        assertEquals(map.get(id2).getProject(), null);
        assertEquals(map.get(id2).getFetchOptions().hasUser(), false);
        assertEquals(map.get(id2).getFetchOptions().hasAuthorizationGroup(), false);
        assertEquals(map.get(id2).getFetchOptions().hasProject(), true);
        assertEquals(map.get(id2).getFetchOptions().hasSpace(), true);
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        RoleAssignmentFetchOptions fo = new RoleAssignmentFetchOptions();
        fo.withRegistrator();
        fo.withUser();

        v3api.getRoleAssignments(sessionToken, Arrays.asList(new RoleAssignmentTechId(1L), new RoleAssignmentTechId(2L)), fo);

        assertAccessLog(
                "get-role-assignments  IDS('[1, 2]') FETCH_OPTIONS('RoleAssignment\n    with User\n    with Registrator\n')");
    }

}

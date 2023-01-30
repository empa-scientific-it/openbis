/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.query.server.authorization;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.authorization.TestAuthorizationConfig;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * Test cases for {@link QueryAccessController}
 * 
 * @author Piotr Buczek
 */
public class QueryAccessControllerTest
{

    private IDAOFactory daoFactory;

    private final static RoleAssignmentPE createGroupRole(String groupCode, RoleCode role)
    {
        final RoleAssignmentPE groupRole = new RoleAssignmentPE();

        final SpacePE groupPE = new SpacePE();
        groupPE.setCode(groupCode);
        groupRole.setSpace(groupPE);
        groupRole.setRole(role);

        return groupRole;
    }

    private final static SpacePE createGroup(String groupCode)
    {
        final SpacePE groupPE = new SpacePE();
        groupPE.setCode(groupCode);
        return groupPE;
    }

    private final static RoleAssignmentPE createInstanceRole(RoleCode role)
    {
        final RoleAssignmentPE instanceRole = new RoleAssignmentPE();

        instanceRole.setRole(role);

        return instanceRole;
    }

    // no person

    @BeforeClass
    public void setUp()
    {
        Mockery context = new Mockery();
        daoFactory = context.mock(IDAOFactory.class);
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getAuthorizationConfig();
                    will(returnValue(new TestAuthorizationConfig(false, false)));
                }
            });
        QueryAccessController.initialize(daoFactory, new HashMap<>());
    }

    @Test
    public final void testIsAuthorizedWithNoPersonFailure()
    {
        assertFalse(QueryAccessController.isAuthorized(null, null, RoleWithHierarchy.SPACE_USER));
    }

    // no space

    @Test
    public final void testIsAuthorizedWithNoSpaceRequiredSuccessfulWithInstanceRole()
    {
        final PersonPE person = new PersonPE();
        final Set<RoleAssignmentPE> roleAssignments = new HashSet<RoleAssignmentPE>();
        roleAssignments.add(createInstanceRole(RoleCode.ADMIN));
        person.setRoleAssignments(roleAssignments);

        assertTrue(QueryAccessController.isAuthorized(person, null,
                RoleWithHierarchy.INSTANCE_ADMIN));
    }

    @Test
    public final void testIsAuthorizedWithNoSpaceRequiredSuccessfulWithGroupRole()
    {
        final PersonPE person = new PersonPE();
        final Set<RoleAssignmentPE> roleAssignments = new HashSet<RoleAssignmentPE>();
        roleAssignments.add(createGroupRole("G1", RoleCode.USER));
        roleAssignments.add(createGroupRole("G2", RoleCode.POWER_USER));
        person.setRoleAssignments(roleAssignments);

        assertTrue(QueryAccessController.isAuthorized(person, null,
                RoleWithHierarchy.SPACE_POWER_USER));
    }

    @Test
    public final void testIsAuthorizedWithNoSpaceRequiredFailureWithInstanceRole()
    {
        final PersonPE person = new PersonPE();
        final Set<RoleAssignmentPE> roleAssignments = new HashSet<RoleAssignmentPE>();
        roleAssignments.add(createGroupRole("G1", RoleCode.USER));
        roleAssignments.add(createGroupRole("G2", RoleCode.POWER_USER));
        person.setRoleAssignments(roleAssignments);

        assertFalse(QueryAccessController.isAuthorized(person, null,
                RoleWithHierarchy.INSTANCE_ADMIN));
    }

    @Test
    public final void testIsAuthorizedWithNoSpaceRequiredFailureWithGroupRole()
    {
        final PersonPE person = new PersonPE();
        final Set<RoleAssignmentPE> roleAssignments = new HashSet<RoleAssignmentPE>();
        roleAssignments.add(createGroupRole("G1", RoleCode.OBSERVER));
        roleAssignments.add(createGroupRole("G2", RoleCode.USER));
        person.setRoleAssignments(roleAssignments);

        assertFalse(QueryAccessController.isAuthorized(person, null,
                RoleWithHierarchy.SPACE_POWER_USER));
    }

    // with space

    @Test
    public final void testIsAuthorizedWithSpaceRequiredSuccessful()
    {
        final PersonPE person = new PersonPE();
        final Set<RoleAssignmentPE> roleAssignments = new HashSet<RoleAssignmentPE>();
        roleAssignments.add(createGroupRole("G1", RoleCode.USER));
        roleAssignments.add(createGroupRole("G2", RoleCode.POWER_USER));
        person.setRoleAssignments(roleAssignments);

        assertTrue(QueryAccessController.isAuthorized(person, createGroup("G2"),
                RoleWithHierarchy.SPACE_POWER_USER));
    }

    @Test
    public final void testIsAuthorizedWithSpaceRequiredSuccessfulForAdmin()
    {
        final PersonPE person = new PersonPE();
        final Set<RoleAssignmentPE> roleAssignments = new HashSet<RoleAssignmentPE>();
        roleAssignments.add(createInstanceRole(RoleCode.ADMIN));
        person.setRoleAssignments(roleAssignments);

        assertTrue(QueryAccessController.isAuthorized(person, createGroup("G1"),
                RoleWithHierarchy.SPACE_POWER_USER));
    }

    @Test
    public final void testIsAuthorizedWithSpaceRequiredFailure()
    {
        final PersonPE person = new PersonPE();
        final Set<RoleAssignmentPE> roleAssignments = new HashSet<RoleAssignmentPE>();
        roleAssignments.add(createGroupRole("G1", RoleCode.USER));
        roleAssignments.add(createGroupRole("G2", RoleCode.POWER_USER));
        person.setRoleAssignments(roleAssignments);

        assertFalse(QueryAccessController.isAuthorized(person, createGroup("G1"),
                RoleWithHierarchy.SPACE_POWER_USER));
    }

}

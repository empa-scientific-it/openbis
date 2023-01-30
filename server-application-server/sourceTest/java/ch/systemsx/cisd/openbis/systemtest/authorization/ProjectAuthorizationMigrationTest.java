/*
 * Copyright 2017 ETH Zuerich, CISD
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

import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.INSTANCE_ADMIN;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.INSTANCE_OBSERVER;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.PROJECT_ADMIN;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.PROJECT_OBSERVER;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.PROJECT_POWER_USER;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.PROJECT_USER;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.SPACE_ADMIN;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.SPACE_OBSERVER;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.SPACE_POWER_USER;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.SPACE_USER;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.TestAuthSessionProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.TestPredicate;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleLevel;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSessionProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.systemtest.SystemTestCase;

/**
 * @author pkupczyk
 */
public class ProjectAuthorizationMigrationTest extends SystemTestCase
{

    private static final String TEST_VALUE = "test";

    private static final SpacePE TEST_SPACE = new SpacePE();

    private static final ProjectPE TEST_PROJECT = new ProjectPE();

    private static final IAuthSessionProvider SESSION_WITH_ALL_OLD_ROLES =
            createSessionWithRoles(INSTANCE_ADMIN, INSTANCE_OBSERVER, SPACE_ADMIN, SPACE_POWER_USER, SPACE_USER, SPACE_OBSERVER);

    private static final IAuthSessionProvider SESSION_WITH_ALL_NEW_ROLES =
            createSessionWithRoles(INSTANCE_ADMIN, INSTANCE_OBSERVER, SPACE_ADMIN, SPACE_POWER_USER, SPACE_USER, SPACE_OBSERVER, PROJECT_ADMIN,
                    PROJECT_POWER_USER, PROJECT_USER, PROJECT_OBSERVER);

    static
    {
        TEST_SPACE.setCode("TEST_SPACE");
        TEST_PROJECT.setCode("TEST_PROJECT");
        TEST_PROJECT.setSpace(TEST_SPACE);
    }

    @Autowired
    private ProjectAuthorizationMigrationTestService service;

    @Test
    public void testMethodWithSpaceObserverToProjectObserverMigration()
    {
        // e.g. migration of methods that fetch a project or entities in a project

        final RoleWithHierarchy[] EXPECTED_OLD_ROLES =
                new RoleWithHierarchy[] { INSTANCE_ADMIN, INSTANCE_OBSERVER, SPACE_ADMIN, SPACE_POWER_USER, SPACE_USER, SPACE_OBSERVER };

        final RoleWithHierarchy[] EXPECTED_NEW_ROLES =
                new RoleWithHierarchy[] { INSTANCE_ADMIN, INSTANCE_OBSERVER, SPACE_ADMIN, SPACE_POWER_USER, SPACE_USER, SPACE_OBSERVER, PROJECT_ADMIN,
                        PROJECT_POWER_USER, PROJECT_USER, PROJECT_OBSERVER };

        service.methodWithSpaceObserver(SESSION_WITH_ALL_OLD_ROLES, TEST_VALUE);
        assertRoles(TestPredicate.getAllowedRoles(), EXPECTED_OLD_ROLES);

        service.methodWithProjectObserver(SESSION_WITH_ALL_OLD_ROLES, TEST_VALUE);
        assertRoles(TestPredicate.getAllowedRoles(), EXPECTED_OLD_ROLES);

        service.methodWithSpaceObserver(SESSION_WITH_ALL_NEW_ROLES, TEST_VALUE);
        assertRoles(TestPredicate.getAllowedRoles(), EXPECTED_OLD_ROLES);

        service.methodWithProjectObserver(SESSION_WITH_ALL_NEW_ROLES, TEST_VALUE);
        assertRoles(TestPredicate.getAllowedRoles(), EXPECTED_NEW_ROLES);
    }

    @Test
    public void testMethodWithSpaceUserToProjectUserMigration()
    {
        // e.g. migration of methods that create/update entities in a project

        final RoleWithHierarchy[] EXPECTED_OLD_ROLES =
                new RoleWithHierarchy[] { INSTANCE_ADMIN, SPACE_ADMIN, SPACE_POWER_USER, SPACE_USER };

        final RoleWithHierarchy[] EXPECTED_NEW_ROLES =
                new RoleWithHierarchy[] { INSTANCE_ADMIN, SPACE_ADMIN, SPACE_POWER_USER, SPACE_USER, PROJECT_ADMIN, PROJECT_POWER_USER,
                        PROJECT_USER };

        service.methodWithSpaceUser(SESSION_WITH_ALL_OLD_ROLES, TEST_VALUE);
        assertRoles(TestPredicate.getAllowedRoles(), EXPECTED_OLD_ROLES);

        service.methodWithProjectUser(SESSION_WITH_ALL_OLD_ROLES, TEST_VALUE);
        assertRoles(TestPredicate.getAllowedRoles(), EXPECTED_OLD_ROLES);

        service.methodWithSpaceUser(SESSION_WITH_ALL_NEW_ROLES, TEST_VALUE);
        assertRoles(TestPredicate.getAllowedRoles(), EXPECTED_OLD_ROLES);

        service.methodWithProjectUser(SESSION_WITH_ALL_NEW_ROLES, TEST_VALUE);
        assertRoles(TestPredicate.getAllowedRoles(), EXPECTED_NEW_ROLES);
    }

    @Test
    public void testMethodWithSpacePowerUserToSpacePowerUserAndProjectAdminMigration()
    {
        // e.g. migration of methods that update/delete a project

        final RoleWithHierarchy[] EXPECTED_OLD_ROLES =
                new RoleWithHierarchy[] { INSTANCE_ADMIN, SPACE_ADMIN, SPACE_POWER_USER };

        final RoleWithHierarchy[] EXPECTED_NEW_ROLES =
                new RoleWithHierarchy[] { INSTANCE_ADMIN, SPACE_ADMIN, SPACE_POWER_USER, PROJECT_ADMIN };

        service.methodWithSpacePowerUser(SESSION_WITH_ALL_OLD_ROLES, TEST_VALUE);
        assertRoles(TestPredicate.getAllowedRoles(), EXPECTED_OLD_ROLES);

        service.methodWithSpacePowerUserAndProjectAdmin(SESSION_WITH_ALL_OLD_ROLES, TEST_VALUE);
        assertRoles(TestPredicate.getAllowedRoles(), EXPECTED_OLD_ROLES);

        service.methodWithSpacePowerUser(SESSION_WITH_ALL_NEW_ROLES, TEST_VALUE);
        assertRoles(TestPredicate.getAllowedRoles(), EXPECTED_OLD_ROLES);

        service.methodWithSpacePowerUserAndProjectAdmin(SESSION_WITH_ALL_NEW_ROLES, TEST_VALUE);
        assertRoles(TestPredicate.getAllowedRoles(), EXPECTED_NEW_ROLES);
    }

    @Test
    public void testMethodWithSpacePowerUserToProjectPowerUserMigration()
    {
        // e.g. migration of methods that delete entities in a project

        final RoleWithHierarchy[] EXPECTED_OLD_ROLES =
                new RoleWithHierarchy[] { INSTANCE_ADMIN, SPACE_ADMIN, SPACE_POWER_USER };

        final RoleWithHierarchy[] EXPECTED_NEW_ROLES =
                new RoleWithHierarchy[] { INSTANCE_ADMIN, SPACE_ADMIN, SPACE_POWER_USER, PROJECT_ADMIN, PROJECT_POWER_USER };

        service.methodWithSpacePowerUser(SESSION_WITH_ALL_OLD_ROLES, TEST_VALUE);
        assertRoles(TestPredicate.getAllowedRoles(), EXPECTED_OLD_ROLES);

        service.methodWithProjectPowerUser(SESSION_WITH_ALL_OLD_ROLES, TEST_VALUE);
        assertRoles(TestPredicate.getAllowedRoles(), EXPECTED_OLD_ROLES);

        service.methodWithSpacePowerUser(SESSION_WITH_ALL_NEW_ROLES, TEST_VALUE);
        assertRoles(TestPredicate.getAllowedRoles(), EXPECTED_OLD_ROLES);

        service.methodWithProjectPowerUser(SESSION_WITH_ALL_NEW_ROLES, TEST_VALUE);
        assertRoles(TestPredicate.getAllowedRoles(), EXPECTED_NEW_ROLES);
    }

    private static IAuthSessionProvider createSessionWithRoles(RoleWithHierarchy... roles)
    {
        PersonPE person = new PersonPE();
        person.setUserId("some_user_with_pa_on");

        for (RoleWithHierarchy role : roles)
        {
            RoleAssignmentPE rolePE = new RoleAssignmentPE();
            rolePE.setRole(role.getRoleCode());

            if (RoleLevel.INSTANCE.equals(role.getRoleLevel()))
            {
                // do nothing
            } else if (RoleLevel.SPACE.equals(role.getRoleLevel()))
            {
                rolePE.setSpace(TEST_SPACE);
            } else if (RoleLevel.PROJECT.equals(role.getRoleLevel()))
            {
                rolePE.setProject(TEST_PROJECT);
            } else
            {
                throw new RuntimeException();
            }

            person.addRoleAssignment(rolePE);
        }

        SimpleSession session = new SimpleSession();
        session.setPerson(person);

        return new TestAuthSessionProvider(session);
    }

    private void assertRoles(List<RoleWithIdentifier> actualRolesWithIdentifier, RoleWithHierarchy... expectedRoles)
    {
        Set<RoleWithHierarchy> actualRoles = new HashSet<RoleWithHierarchy>();

        for (RoleWithIdentifier actualRoleWithIdentifier : actualRolesWithIdentifier)
        {
            if (actualRoleWithIdentifier.getRole().isInstanceLevel())
            {
                // do nothing
            } else if (actualRoleWithIdentifier.getRole().isSpaceLevel())
            {
                Assert.assertEquals(actualRoleWithIdentifier.getAssignedSpace(), TEST_SPACE);
            } else if (actualRoleWithIdentifier.getRole().isProjectLevel())
            {
                Assert.assertEquals(actualRoleWithIdentifier.getAssignedProject(), TEST_PROJECT);
            } else
            {
                Assert.fail();
            }
            actualRoles.add(actualRoleWithIdentifier.getRole());
        }

        AssertionUtil.assertCollectionContainsOnly(actualRoles, expectedRoles);
    }

}

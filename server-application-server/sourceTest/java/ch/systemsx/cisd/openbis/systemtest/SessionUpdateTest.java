/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.systemtest;

import static org.testng.AssertJUnit.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ColumnSetting;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Grantee;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.displaysettings.ColumnDisplaySettingsUpdate;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * @author Jakub Straszewski
 */
public class SessionUpdateTest extends SystemTestCase
{

    private static final String ADMIN = "test";

    @Test
    public void testAuthorizationGroupModificationsAreVisible()
    {
        String groupCode = "AUTHORIZATION_TEST_GROUP";

        String sessionToken = authenticateAs(ADMIN);
        String sessionTokenUser = authenticateAs(TEST_SPACE_USER);

        // create a group

        NewAuthorizationGroup newGroup = new NewAuthorizationGroup();
        newGroup.setCode(groupCode);
        commonServer.registerAuthorizationGroup(sessionToken, newGroup);
        List<AuthorizationGroup> groups = commonServer.listAuthorizationGroups(sessionToken);
        TechId authorizationGroupTechId = new TechId(findAuthorizationGroup(groups, groupCode).getId());

        // add authorization to the group
        commonServer.registerSpaceRole(sessionToken, RoleCode.ADMIN, new SpaceIdentifier("TESTGROUP"),
                Grantee.createAuthorizationGroup(groupCode));

        // add user to the group
        commonServer.addPersonsToAuthorizationGroup(sessionToken, authorizationGroupTechId, Arrays.asList(TEST_SPACE_USER));

        // check the user sees space

        assertUserCanAccessSpace(sessionTokenUser, "TESTGROUP");

        // remove the group
        commonServer.removePersonsFromAuthorizationGroup(sessionToken, authorizationGroupTechId, Arrays.asList(TEST_SPACE_USER));

        // check user doesnt see space
        assertUserCantAccessSpace(sessionTokenUser, "TESTGROUP");

        // cleanup

        commonServer.deleteAuthorizationGroups(sessionToken, Arrays.asList(authorizationGroupTechId), "no reason");
    }

    @Test
    public void testCreateSpaceForUserAndAssignIdenticalRoleFailsWithNoSideeffect()
    {
        String sessionToken = authenticateAs(ADMIN);
        String sessionTokenUser = authenticateAs(TEST_SPACE_USER);

        commonServer.tryGetSession(sessionTokenUser);

        String spaceIdentifier = new SpaceIdentifier("TEST_SPACE_1").toString();

        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder().user(ADMIN).space("TEST_SPACE_1", TEST_SPACE_USER).
                        assignRoleToSpace(RoleCode.ADMIN, spaceIdentifier, Arrays.asList(TEST_SPACE_USER), null).create();

        try
        {
            etlService.performEntityOperations(sessionToken, eo);
            fail("Exception expected");
        } catch (UserFailureException ufe)
        {
            // this is expected
        }

        // commonServer.updateDisplaySettings(sessionTokenUser,
        // new ColumnDisplaySettingsUpdate("id_a_b_C", Collections.<ColumnSetting> emptyList()));
    }

    @Test
    public void testSpaceWithRoleAssignmentDeleted()
    {
        String sessionTokenForInstanceAdmin = commonServer.tryAuthenticate("test", "a").getSessionToken();

        // reproduce

        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder().user(ADMIN).space("TEST_SPACE_1", TEST_SPACE_USER).create();

        etlService.performEntityOperations(sessionTokenForInstanceAdmin, eo);

        String sessionTokenForSpaceAdmin = commonServer.tryAuthenticate(TEST_SPACE_USER, "a").getSessionToken();

        List<Space> spaces = commonServer.listSpaces(sessionTokenForSpaceAdmin);
        Space space = findSpace(spaces, "TEST_SPACE_1");

        commonServer.deleteSpaces(sessionTokenForInstanceAdmin, Arrays.asList(new TechId(space.getId())), "no reason");

        commonServer.updateDisplaySettings(sessionTokenForSpaceAdmin,
                new ColumnDisplaySettingsUpdate("id_a_b_C", Collections.<ColumnSetting> emptyList()));
    }

    @Test
    public void testRoleAssingmentDeleted()
    {
        String sessionTokenForInstanceAdmin = commonServer.tryAuthenticate("test", "a").getSessionToken();
        String sessionTokenForSpaceAdmin = commonServer.tryAuthenticate(TEST_SPACE_USER, "a").getSessionToken();

        // reproduce

        commonServer.deleteSpaceRole(sessionTokenForInstanceAdmin, RoleCode.ADMIN,
                new SpaceIdentifier("TEST-SPACE"), Grantee.createPerson(TEST_SPACE_USER));

        commonServer.updateDisplaySettings(sessionTokenForSpaceAdmin,
                new ColumnDisplaySettingsUpdate("id_a_b_C", Collections.<ColumnSetting> emptyList()));
        // clean up
        commonServer.registerSpaceRole(sessionTokenForInstanceAdmin, RoleCode.ADMIN,
                new SpaceIdentifier("TEST-SPACE"), Grantee.createPerson(TEST_SPACE_USER));
    }

    @Test
    public void testRoleAssignmentAdded()
    {
        String spaceCode = "TESTGROUP";

        String sessionTokenForInstanceAdmin = commonServer.tryAuthenticate("test", "a").getSessionToken();
        String sessionTokenForSpaceAdmin = commonServer.tryAuthenticate(TEST_SPACE_USER, "a").getSessionToken();

        List<Space> spaces = commonServer.listSpaces(sessionTokenForSpaceAdmin);
        boolean matchingSpaces = containsSpace(spaces, spaceCode);
        AssertJUnit.assertFalse(spaceCode + " should not be in test_space user groups before the role assignment" + spaces, matchingSpaces);

        commonServer.registerSpaceRole(sessionTokenForInstanceAdmin, RoleCode.ADMIN,
                new SpaceIdentifier(spaceCode), Grantee.createPerson(TEST_SPACE_USER));

        spaces = commonServer.listSpaces(sessionTokenForSpaceAdmin);
        matchingSpaces = containsSpace(spaces, spaceCode);
        AssertJUnit.assertTrue("Couldn't find " + spaceCode + " space in spaces of test_space user. Found only " + spaces, matchingSpaces);

        // cleanup

        commonServer.deleteSpaceRole(sessionTokenForInstanceAdmin, RoleCode.ADMIN,
                new SpaceIdentifier(spaceCode), Grantee.createPerson(TEST_SPACE_USER));

    }

    void assertUserCanAccessSpace(String sessionToken, String spaceCode)
    {
        List<Space> spaces = commonServer.listSpaces(sessionToken);
        boolean foundSpace = containsSpace(spaces, spaceCode);
        AssertJUnit.assertTrue(spaceCode + " should be in test_space user." + spaces, foundSpace);
    }

    void assertUserCantAccessSpace(String sessionToken, String spaceCode)
    {
        List<Space> spaces = commonServer.listSpaces(sessionToken);
        boolean foundSpace = containsSpace(spaces, spaceCode);
        AssertJUnit.assertFalse(spaceCode + " should not be in test_space user." + spaces, foundSpace);
    }

    private boolean containsSpace(List<Space> spaces, final String spaceCode)
    {
        int matchingSpaces = CollectionUtils.countMatches(spaces, new Predicate<Space>()
            {
                @Override
                public boolean evaluate(Space object)
                {
                    return object.getCode().equals(spaceCode);
                }

            });
        return matchingSpaces > 0;
    }

    private Space findSpace(List<Space> spaces, final String spaceCode)
    {
        return CollectionUtils.find(spaces, new Predicate<Space>()
            {
                @Override
                public boolean evaluate(Space object)
                {
                    return object.getCode().equals(spaceCode);
                }

            });
    }

    private AuthorizationGroup findAuthorizationGroup(List<AuthorizationGroup> spaces, final String spaceCode)
    {
        return CollectionUtils.find(spaces, new Predicate<AuthorizationGroup>()
            {
                @Override
                public boolean evaluate(AuthorizationGroup object)
                {
                    return object.getCode().equals(spaceCode);
                }

            });
    }

}

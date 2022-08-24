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
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.test.context.transaction.TestTransaction;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.AuthorizationGroup;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.create.AuthorizationGroupCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.delete.AuthorizationGroupDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.fetchoptions.AuthorizationGroupFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.id.IAuthorizationGroupId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.create.PersonalAccessTokenCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.id.PersonalAccessTokenPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.create.PersonCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.delete.PersonDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.IPersonId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.PersonPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.Role;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.RoleAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.create.RoleAssignmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.fetchoptions.RoleAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.id.IRoleAssignmentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.id.RoleAssignmentTechId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.SpaceCreation;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * @author pkupczyk
 */
public class DeletePersonTest extends AbstractTest
{

    @Test
    public void testDeleteNonexistent()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        IPersonId personId = new PersonPermId("idontexist");

        Person beforePerson = getPerson(sessionToken, personId);
        assertNull(beforePerson);

        PersonDeletionOptions options = new PersonDeletionOptions();
        options.setReason("testing");
        v3api.deletePersons(sessionToken, Collections.singletonList(personId), options);

        Person afterPerson = getPerson(sessionToken, personId);
        assertNull(afterPerson);
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = ".*You cannot remove your own user.*")
    public void testDeleteYourself()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        IPersonId personId = new PersonPermId(TEST_USER);

        PersonDeletionOptions options = new PersonDeletionOptions();
        options.setReason("testing");

        v3api.deletePersons(sessionToken, Collections.singletonList(personId), options);
    }

    @Test
    public void testDeleteYourselfOnBehalfOf()
    {
        String sessionToken = v3api.loginAs(TEST_USER, PASSWORD, TEST_INSTANCE_ETLSERVER);

        PersonDeletionOptions options = new PersonDeletionOptions();
        options.setReason("testing");

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.deletePersons(sessionToken, Collections.singletonList(new PersonPermId(TEST_USER)), options);
                }
            }, "You cannot remove your own user");

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    // shall behave as if TEST_INSTANCE_ETLSERVER user was trying to delete itself
                    v3api.deletePersons(sessionToken, Collections.singletonList(new PersonPermId(TEST_INSTANCE_ETLSERVER)), options);
                }
            }, "You cannot remove your own user");
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = ".*Deletion options cannot be null.*")
    public void testDeleteWithoutOptions()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        IPersonId personId = new PersonPermId("doesnotmatter");

        v3api.deletePersons(sessionToken, Collections.singletonList(personId), null);
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = ".*Access denied to object with PersonPermId = \\[deletion_test_no_access\\].*")
    public void testDeleteWithoutAccessRights()
    {
        String adminSessionToken = v3api.login(TEST_USER, PASSWORD);
        String userSessionToken = v3api.login(TEST_INSTANCE_OBSERVER, PASSWORD);

        PersonCreation personCreation = new PersonCreation();
        personCreation.setUserId("deletion_test_no_access");
        PersonPermId personId = v3api.createPersons(adminSessionToken, Collections.singletonList(personCreation)).get(0);

        PersonDeletionOptions options = new PersonDeletionOptions();
        options.setReason("testing");

        v3api.deletePersons(userSessionToken, Collections.singletonList(personId), options);
    }

    @Test
    public void testDeletePersonWhoHasNotCreatedAnyObjects()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PersonCreation personCreation = new PersonCreation();
        personCreation.setUserId("deletion_test_person_with_no_objects");
        PersonPermId personId = v3api.createPersons(sessionToken, Collections.singletonList(personCreation)).get(0);

        RoleAssignmentCreation roleCreation = new RoleAssignmentCreation();
        roleCreation.setUserId(personId);
        roleCreation.setRole(Role.ADMIN);
        RoleAssignmentTechId roleId = v3api.createRoleAssignments(sessionToken, Collections.singletonList(roleCreation)).get(0);

        AuthorizationGroupCreation groupCreation = new AuthorizationGroupCreation();
        groupCreation.setCode("deletion_test_group");
        groupCreation.setUserIds(Collections.singletonList(personId));
        IAuthorizationGroupId groupId = v3api.createAuthorizationGroups(sessionToken, Collections.singletonList(groupCreation)).get(0);

        // before
        Person personBefore = getPerson(sessionToken, personId);
        assertNotNull(personBefore);
        assertEquals(personBefore.getRoleAssignments().get(0).getId(), roleId);

        RoleAssignment roleBefore = getRole(sessionToken, roleId);
        assertNotNull(roleBefore);
        assertEquals(roleBefore.getUser().getPermId(), personId);

        AuthorizationGroup groupBefore = getGroup(sessionToken, groupId);
        assertNotNull(groupBefore);
        assertEquals(groupBefore.getUsers().get(0).getPermId(), personId);

        // delete person
        PersonDeletionOptions personOptions = new PersonDeletionOptions();
        personOptions.setReason("testing");
        v3api.deletePersons(sessionToken, Collections.singletonList(personId), personOptions);

        Person personAfter = getPerson(sessionToken, personId);
        assertNull(personAfter);

        RoleAssignment roleAfter = getRole(sessionToken, roleId);
        assertNull(roleAfter);

        AuthorizationGroup groupAfter = getGroup(sessionToken, groupId);
        assertNotNull(groupAfter);
        assertEquals(groupAfter.getUsers(), Collections.emptyList());

        // delete group
        AuthorizationGroupDeletionOptions groupOptions = new AuthorizationGroupDeletionOptions();
        groupOptions.setReason("testing");
        v3api.deleteAuthorizationGroups(sessionToken, Collections.singletonList(groupId), groupOptions);

        groupAfter = getGroup(sessionToken, groupId);
        assertNull(groupAfter);

        v3api.logout(sessionToken);

        commitTransaction();
    }

    @Test
    public void testDeletePersonWhoHasCreatedSomeObjects()
    {
        try
        {
            String adminSessionToken = v3api.login(TEST_USER, PASSWORD);

            PersonCreation personCreation = new PersonCreation();
            personCreation.setUserId("deletion_test_person_with_objects");
            PersonPermId personId = v3api.createPersons(adminSessionToken, Collections.singletonList(personCreation)).get(0);

            RoleAssignmentCreation roleCreation = new RoleAssignmentCreation();
            roleCreation.setUserId(personId);
            roleCreation.setRole(Role.ADMIN);
            v3api.createRoleAssignments(adminSessionToken, Collections.singletonList(roleCreation));

            String userSessionToken = v3api.login(personCreation.getUserId(), PASSWORD);

            SpaceCreation spaceCreation = new SpaceCreation();
            spaceCreation.setCode("deletion_test_space");
            v3api.createSpaces(userSessionToken, Collections.singletonList(spaceCreation));

            // delete
            PersonDeletionOptions options = new PersonDeletionOptions();
            options.setReason("testing");
            v3api.deletePersons(adminSessionToken, Collections.singletonList(personId), options);

            commitTransaction();
            fail("Expected the transaction to fail with a foreign key constraint violation");
        } catch (Exception e)
        {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            assertTrue(stackTrace.contains("insert or update on table \"spaces\" violates foreign key constraint \"space_pers_fk_registerer\""),
                    stackTrace);
        }
    }

    @Test
    public void testDeletePersonWithPersonalAccessTokenSession()
    {
        String adminSessionToken = v3api.login(TEST_USER, PASSWORD);

        PersonCreation personCreation = new PersonCreation();
        personCreation.setUserId("deletion_test_person_with_personal_access_tokens");
        PersonPermId personId = v3api.createPersons(adminSessionToken, Collections.singletonList(personCreation)).get(0);

        RoleAssignmentCreation roleCreation = new RoleAssignmentCreation();
        roleCreation.setUserId(personId);
        roleCreation.setRole(Role.ADMIN);
        v3api.createRoleAssignments(adminSessionToken, Collections.singletonList(roleCreation));

        String userSessionToken = v3api.login(personCreation.getUserId(), PASSWORD);
        PersonalAccessTokenPermId tokenId = v3api.createPersonalAccessTokens(userSessionToken, Arrays.asList(tokenCreation())).get(0);

        assertNotNull(v3api.getSessionInformation(tokenId.getPermId()));

        PersonDeletionOptions options = new PersonDeletionOptions();
        options.setReason("testing");
        v3api.deletePersons(adminSessionToken, Collections.singletonList(personId), options);

        try
        {
            v3api.getSessionInformation(tokenId.getPermId());
            fail();
        } catch (InvalidSessionException e)
        {
            // expected
        }
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        PersonCreation personCreation = new PersonCreation();
        personCreation.setUserId("deletion_test_logging");
        PersonPermId personId = v3api.createPersons(sessionToken, Collections.singletonList(personCreation)).get(0);

        PersonDeletionOptions options = new PersonDeletionOptions();
        options.setReason("testing");
        v3api.deletePersons(sessionToken, Collections.singletonList(personId), options);

        assertAccessLog(
                "delete-persons  PERSON_IDS('[deletion_test_logging]') DELETION_OPTIONS('PersonDeletionOptions[reason=testing]')");
    }

    private Person getPerson(String sessionToken, IPersonId personId)
    {
        PersonFetchOptions fo = new PersonFetchOptions();
        fo.withRoleAssignments();
        return v3api.getPersons(sessionToken, Collections.singletonList(personId), fo).get(personId);
    }

    private AuthorizationGroup getGroup(String sessionToken, IAuthorizationGroupId groupId)
    {
        AuthorizationGroupFetchOptions fo = new AuthorizationGroupFetchOptions();
        fo.withUsers();
        return v3api.getAuthorizationGroups(sessionToken, Collections.singletonList(groupId), fo).get(groupId);
    }

    private RoleAssignment getRole(String sessionToken, IRoleAssignmentId roleId)
    {
        RoleAssignmentFetchOptions fo = new RoleAssignmentFetchOptions();
        fo.withUser();
        return v3api.getRoleAssignments(sessionToken, Collections.singletonList(roleId), fo).get(roleId);
    }

    private void commitTransaction()
    {
        // force transaction commit for DB deferred constraints to be checked
        TestTransaction.flagForCommit();
        TestTransaction.end();
    }

}

/*
 * Copyright ETH 2018 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.systemtest.task;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.create.PersonCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.PersonPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.Role;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.create.RoleAssignmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.ethz.sis.openbis.systemtest.asapi.v3.AbstractTest;
import ch.systemsx.cisd.authentication.NullAuthenticationService;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.MockLogger;
import ch.systemsx.cisd.common.utilities.MockTimeProvider;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.task.UserGroup;
import ch.systemsx.cisd.openbis.generic.server.task.UserManager;
import ch.systemsx.cisd.openbis.generic.server.task.UserManagerReport;
import ch.systemsx.cisd.openbis.generic.shared.IOpenBisSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.annotation.Resource;
import java.io.File;
import java.util.*;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

/**
 * @author Franz-Josef Elmer
 */
public class UserManagerTest extends AbstractTest
{
    private static final File UNIT_TEST_ROOT_DIRECTORY = new File("targets", "unit-test-wd");

    private static final String EXPERIMENT_TYPE = "DELETION_TEST";

    private static final String SAMPLE_TYPE = "NORMAL";

    private static final Principal U1 = new Principal("u1", "Albert", "Einstein", "a.e@abc.de");

    private static final Principal U2 = new Principal("u2", "Isaac", "Newton", "i.n@abc.de");

    private static final Principal U3 = new Principal("u3", "Alan", "Turing", "a.t@abc.de");

    private static final Principal U4 = new Principal("u4", "Leonard", "Euler", "l.e@abc.de");

    @Autowired
    private UserManagerTestService testService;

    @Resource(name = ComponentNames.SESSION_MANAGER)
    private IOpenBisSessionManager sessionManager;

    private File workingDir;

    private File mappingFile;

    private UserManagerReport report;

    private static Map<Role, List<String>> commonSpaces()
    {
        Map<Role, List<String>> commonSpacesByRole = new EnumMap<>(Role.class);
        commonSpacesByRole.put(Role.USER, Arrays.asList("ALPHA", "BETA"));
        commonSpacesByRole.put(Role.OBSERVER, Arrays.asList("GAMMA"));
        return commonSpacesByRole;
    }

    @BeforeMethod
    public void createFreshWorkingDirectory()
    {
        workingDir = new File(UNIT_TEST_ROOT_DIRECTORY, getClass().getName());
        workingDir.delete();
        FileUtilities.deleteRecursively(workingDir);
        workingDir.mkdirs();
        mappingFile = new File(workingDir, "mapping.txt");
    }

    @Test
    public void testCreateInstanceAdmin()
    {
        // Given
        MockLogger logger = new MockLogger();
        UserManager userManager = new UserManagerBuilder(v3api, logger, report()).instanceAdmins(U1.getUserId()).get();

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [ADD-USER] u1\n"
                + "1970-01-01 01:00:01 [ASSIGN-ROLE-TO-USER] user: u1, role: INSTANCE_ADMIN\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.space("").instanceAdmin(U1);
        builder.assertExpectations();
    }

    @Test
    public void testCreateInstanceAdmin2()
    {
        // Given
        MockLogger logger = new MockLogger();
        UserManager userManager = new UserManagerBuilder(v3api, logger, report()).instanceAdmins(U1.getUserId()).get();
        manage(userManager);
        
        // When
        UserManagerReport report = manage(userManager);
        
        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [ADD-USER] u1\n"
                + "1970-01-01 01:00:01 [ASSIGN-ROLE-TO-USER] user: u1, role: INSTANCE_ADMIN\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.space("").instanceAdmin(U1);
        builder.assertExpectations();
    }
    
    @Test
    public void testCreateOneGroupWithAUserWhichAlreadyHasAHomeSpace()
    {
        // Given
        // 1. create user U2 with home space TEST-SPACE and SPACE_ADMIN on this space
        createUserForTestSpace(U2);
        // 2. create group G1 with users U1 (admin) and U2
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G1").admins(U1.getUserId(), "blabla"), users(U1, U2));
        
        // When
        UserManagerReport report = manage(userManager);
        
        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [ADD-AUTHORIZATION-GROUP] G1\n"
                + "1970-01-01 01:00:01 [ADD-AUTHORIZATION-GROUP] G1_ADMIN\n"
                + "1970-01-01 01:00:02 [ADD-SPACE] G1_ALPHA\n"
                + "1970-01-01 01:00:03 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1, role: SPACE_USER for G1_ALPHA\n"
                + "1970-01-01 01:00:04 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_ALPHA\n"
                + "1970-01-01 01:00:05 [ADD-SPACE] G1_BETA\n"
                + "1970-01-01 01:00:06 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1, role: SPACE_USER for G1_BETA\n"
                + "1970-01-01 01:00:07 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_BETA\n"
                + "1970-01-01 01:00:08 [ADD-SPACE] G1_GAMMA\n"
                + "1970-01-01 01:00:09 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1, role: SPACE_OBSERVER for G1_GAMMA\n"
                + "1970-01-01 01:00:10 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_GAMMA\n"
                + "1970-01-01 01:00:11 [ADD-SPACE] G1_U1\n"
                + "1970-01-01 01:00:12 [ADD-USER] u1\n"
                + "1970-01-01 01:00:13 [ASSIGN-ROLE-TO-USER] user: u1, role: SPACE_ADMIN for G1_U1\n"
                + "1970-01-01 01:00:14 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_U1\n"
                + "1970-01-01 01:00:15 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G1, user: u1\n"
                + "1970-01-01 01:00:16 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, user: u1\n"
                + "1970-01-01 01:00:17 [ADD-SPACE] G1_U2\n"
                + "1970-01-01 01:00:18 [ASSIGN-ROLE-TO-USER] user: u2, role: SPACE_ADMIN for G1_U2\n"
                + "1970-01-01 01:00:19 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_U2\n"
                + "1970-01-01 01:00:20 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G1, user: u2\n"
                + "1970-01-01 01:00:21 [ASSIGN-HOME-SPACE-FOR-USER] user: u1, home space: G1_U1\n"
                + "1970-01-01 01:00:22 [ASSIGN-HOME-SPACE-FOR-USER] user: u2, home space: G1_U2\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.groups("G1").commonSpaces(commonSpaces).users(U1, U2);
        builder.space("G1_ALPHA").admin(U1).user(U2);
        builder.space("G1_BETA").admin(U1).user(U2);
        builder.space("G1_GAMMA").admin(U1).observer(U2);
        builder.space("G1_U1").admin(U1).non(U2);
        builder.space("G1_U2").admin(U1, U2);
        builder.space("TEST-SPACE").non(U1).admin(U2);
        builder.homeSpace(U1, "G1_U1");
        builder.homeSpace(U2, "G1_U2");
        builder.assertExpectations();
    }
    
    @Test
    public void testAddAndRemoveAUserWhichAlreadyHasAHomeSpace()
    {
        // Given
        // 1. create user U2 with home space TEST-SPACE and SPACE_ADMIN on this space
        createUserForTestSpace(U2);
        // 2. create group G1 with users U1 (admin) and U2
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.setGlobalSpaces(Arrays.asList("A"));
        userManager.addGroup(new UserGroupAsBuilder("G1").admins(U1.getUserId(), "blabla"), users(U1, U2));
        assertEquals(manage(userManager).getErrorReport(), "");
        createBuilder().groups("G1").space("A").observer(U1, U2).assertExpectations();
        // 3. remove U2 from G1
        userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.setGlobalSpaces(Arrays.asList("A"));
        userManager.addGroup(new UserGroupAsBuilder("G1").admins(U1), users(U1));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [REMOVE-USER-FROM-AUTHORIZATION-GROUP] group: G1, user: u2\n"
                + "1970-01-01 01:00:01 [UNASSIGN-ROLE-FORM-USER] user: u2, role: SPACE_ADMIN for G1_U2\n"
                + "1970-01-01 01:00:02 [REMOVE-USER-FROM-AUTHORIZATION-GROUP] group: ALL_GROUPS, user: u2\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.groups("G1").commonSpaces(commonSpaces).users(U1, U2);
        builder.space("A").observer(U1).non(U2);
        builder.space("G1_ALPHA").admin(U1).non(U2);
        builder.space("G1_BETA").admin(U1).non(U2);
        builder.space("G1_GAMMA").admin(U1).non(U2);
        builder.space("G1_U1").admin(U1).non(U2);
        builder.space("G1_U2").admin(U1).non(U2);
        builder.homeSpace(U1, "G1_U1");
        builder.homeSpace(U2, "G1_U2");
        builder.space("TEST-SPACE").non(U1).admin(U2);
        builder.assertExpectations();
    }

    @Test
    public void testGroupWithoutUserSpaces()
    {
        // Given
        // 1. create user U2 with home space TEST-SPACE and SPACE_ADMIN on this space
        createUserForTestSpace(U2);

        // 2. create groups:
        // - G1 with users U1 (admin) and U2
        // - G2 with users U3 (admin) and U4
        // - SHARED with users U1 (admin), U2, U3 (admin) and U4
        // - G1 and G2 have default value (i.e. true) of 'createUserSpace' flag while SHARED has 'createUserSpace' flag set to false
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.setGlobalSpaces(Arrays.asList("A"));
        userManager.addGroup(new UserGroupAsBuilder("G1").admins(U1), users(U1, U2));
        userManager.addGroup(new UserGroupAsBuilder("G2").admins(U3), users(U3, U4));
        userManager.addGroup(new UserGroupAsBuilder("SHARED").admins(U1, U3).createUserSpace(false), users(U1, U2, U3, U4));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.commonSpaces(commonSpaces);
        builder.users(U1, U2, U3, U4);
        builder.groups("G1", "G2", "SHARED");
        builder.space("A").observer(U1, U2, U3, U4);
        builder.space("G1_ALPHA").admin(U1).user(U2).non(U3, U4);
        builder.space("G1_BETA").admin(U1).user(U2).non(U3, U4);
        builder.space("G1_GAMMA").admin(U1).observer(U2).non(U3, U4);
        builder.space("G1_U1").admin(U1).non(U2, U3, U4);
        builder.space("G1_U2").admin(U1, U2).non(U3, U4);
        builder.space("G2_ALPHA").admin(U3).user(U4).non(U1, U2);
        builder.space("G2_BETA").admin(U3).user(U4).non(U1, U2);
        builder.space("G2_GAMMA").admin(U3).observer(U4).non(U1, U2);
        builder.space("G2_U3").admin(U3).non(U1, U2, U4);
        builder.space("G2_U4").admin(U3, U4).non(U1, U2);
        builder.space("SHARED_ALPHA").admin(U1, U3).user(U2, U4);
        builder.space("SHARED_BETA").admin(U1, U3).user(U2, U4);
        builder.space("SHARED_GAMMA").admin(U1, U3).observer(U2, U4);
        builder.space("TEST-SPACE").admin(U2).non(U1, U3, U4);
        builder.homeSpace(U1, "G1_U1");
        builder.homeSpace(U2, "G1_U2");
        builder.homeSpace(U3, "G2_U3");
        builder.homeSpace(U4, "G2_U4");
        builder.assertExpectations();

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        Map<ISpaceId, Space> sharedUserSpaces =
                v3api.getSpaces(sessionToken, Arrays.asList(new SpacePermId("SHARED_U1"), new SpacePermId("SHARED_U2"), new SpacePermId("SHARED_U3"),
                        new SpacePermId("SHARED_U4")), new SpaceFetchOptions());
        assertEquals(sharedUserSpaces, Collections.emptyMap());
    }

    @Test
    public void testAddGlobalSpacesAndCommonSpacesSamplesAndExperiments()
    {
        // Given
        // 1. create group G1 with users U1 (admin) and U2
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = new EnumMap<>(Role.class);
        commonSpaces.put(Role.USER, Arrays.asList("ALPHA", "BETA"));
        commonSpaces.put(Role.OBSERVER, Arrays.asList("GAMMA"));
        UserManager userManager = new UserManagerBuilder(v3api, logger, report())
                .commonSpaces(commonSpaces).commonSample("GAMMA/G", SAMPLE_TYPE)
                .commonExperiment("ALPHA/P1/E1", EXPERIMENT_TYPE).get();
        userManager.setGlobalSpaces(Arrays.asList("A", "B"));
        userManager.addGroup(new UserGroupAsBuilder("G1").admins(U1), users(U1));
        assertEquals(manage(userManager).getErrorReport(), "");
        createBuilder().groups("G1").samples(SAMPLE_TYPE, "/G1_GAMMA/G1_G").space("G1_ALPHA").assertExpectations();
        // 2. add common spaces, samples and experiments
        commonSpaces.put(Role.OBSERVER, Arrays.asList("GAMMA", "DELTA"));
        userManager = new UserManagerBuilder(v3api, logger, report())
                .commonSpaces(commonSpaces).commonSample("GAMMA/G", SAMPLE_TYPE).commonSample("ALPHA/G", SAMPLE_TYPE)
                .commonExperiment("ALPHA/P1/E1", EXPERIMENT_TYPE).commonExperiment("ALPHA/P1/E2", EXPERIMENT_TYPE)
                .commonExperiment("BETA/P1/E1", EXPERIMENT_TYPE).get();
        userManager.setGlobalSpaces(Arrays.asList("A", "C"));
        userManager.addGroup(new UserGroupAsBuilder("G1").admins(U1), users(U1));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [ADD-SPACES] [C]\n"
                + "1970-01-01 01:00:01 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: ALL_GROUPS, role: SPACE_OBSERVER for C\n"
                + "1970-01-01 01:00:02 [ADD-SPACE] G1_DELTA\n"
                + "1970-01-01 01:00:03 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1, role: SPACE_OBSERVER for G1_DELTA\n"
                + "1970-01-01 01:00:04 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_DELTA\n"
                + "1970-01-01 01:00:05 [ADD-SAMPLE] /G1_ALPHA/G1_G\n"
                + "1970-01-01 01:00:06 [ADD-PROJECT] /G1_BETA/G1_P1\n"
                + "1970-01-01 01:00:07 [ADD-EXPERIMENT] /G1_ALPHA/G1_P1/G1_E2\n"
                + "1970-01-01 01:00:08 [ADD-EXPERIMENT] /G1_BETA/G1_P1/G1_E1\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.globalSpaces(Arrays.asList("A", "B", "C")).groups("G1").commonSpaces(commonSpaces).users(U1);
        builder.samples(SAMPLE_TYPE, "/G1_GAMMA/G1_G", "/G1_ALPHA/G1_G");
        builder.experiments(EXPERIMENT_TYPE, "/G1_ALPHA/G1_P1/G1_E1", "/G1_ALPHA/G1_P1/G1_E2", "/G1_BETA/G1_P1/G1_E1");
        builder.space("A").observer(U1);
        builder.space("B").observer(U1);
        builder.space("C").observer(U1);
        builder.space("G1_ALPHA").admin(U1);
        builder.space("G1_BETA").admin(U1);
        builder.space("G1_GAMMA").admin(U1);
        builder.space("G1_DELTA").admin(U1);
        builder.space("G1_U1").admin(U1);
        builder.homeSpace(U1, "G1_U1");
        builder.assertExpectations();
    }

    private void createUserForTestSpace(Principal user)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PersonCreation personCreation = new PersonCreation();
        personCreation.setUserId(user.getUserId());
        SpacePermId spaceId = new SpacePermId("TEST-SPACE");
        personCreation.setSpaceId(spaceId);
        v3api.createPersons(sessionToken, Arrays.asList(personCreation));
        RoleAssignmentCreation assignmentCreation = new RoleAssignmentCreation();
        assignmentCreation.setUserId(new PersonPermId(user.getUserId()));
        assignmentCreation.setRole(Role.ADMIN);
        assignmentCreation.setSpaceId(spaceId);
        v3api.createRoleAssignments(sessionToken, Arrays.asList(assignmentCreation));
    }

    @Test
    public void testCreateOneGroupWithAUserWhichAlreadyTriedToLogin()
    {
        // Given
        try
        {
            v3api.login(U2.getUserId(), PASSWORD);
            fail("UserFailureException expected");
        } catch (UserFailureException e)
        {
            // expected exception ignored
        }
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G1").admins(U1.getUserId(), "blabla"), users(U1, U2));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [ADD-AUTHORIZATION-GROUP] G1\n"
                + "1970-01-01 01:00:01 [ADD-AUTHORIZATION-GROUP] G1_ADMIN\n"
                + "1970-01-01 01:00:02 [ADD-SPACE] G1_ALPHA\n"
                + "1970-01-01 01:00:03 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1, role: SPACE_USER for G1_ALPHA\n"
                + "1970-01-01 01:00:04 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_ALPHA\n"
                + "1970-01-01 01:00:05 [ADD-SPACE] G1_BETA\n"
                + "1970-01-01 01:00:06 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1, role: SPACE_USER for G1_BETA\n"
                + "1970-01-01 01:00:07 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_BETA\n"
                + "1970-01-01 01:00:08 [ADD-SPACE] G1_GAMMA\n"
                + "1970-01-01 01:00:09 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1, role: SPACE_OBSERVER for G1_GAMMA\n"
                + "1970-01-01 01:00:10 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_GAMMA\n"
                + "1970-01-01 01:00:11 [ADD-SPACE] G1_U1\n"
                + "1970-01-01 01:00:12 [ADD-USER] u1\n"
                + "1970-01-01 01:00:13 [ASSIGN-ROLE-TO-USER] user: u1, role: SPACE_ADMIN for G1_U1\n"
                + "1970-01-01 01:00:14 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_U1\n"
                + "1970-01-01 01:00:15 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G1, user: u1\n"
                + "1970-01-01 01:00:16 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, user: u1\n"
                + "1970-01-01 01:00:17 [ADD-SPACE] G1_U2\n"
                + "1970-01-01 01:00:18 [ASSIGN-ROLE-TO-USER] user: u2, role: SPACE_ADMIN for G1_U2\n"
                + "1970-01-01 01:00:19 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_U2\n"
                + "1970-01-01 01:00:20 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G1, user: u2\n"
                + "1970-01-01 01:00:21 [ASSIGN-HOME-SPACE-FOR-USER] user: u1, home space: G1_U1\n"
                + "1970-01-01 01:00:22 [ASSIGN-HOME-SPACE-FOR-USER] user: u2, home space: G1_U2\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.groups("G1").commonSpaces(commonSpaces).users(U1, U2);
        builder.space("G1_ALPHA").admin(U1).user(U2);
        builder.space("G1_BETA").admin(U1).user(U2);
        builder.space("G1_GAMMA").admin(U1).observer(U2);
        builder.space("G1_U1").admin(U1).non(U2);
        builder.space("G1_U2").admin(U1, U2);
        builder.homeSpace(U1, "G1_U1");
        builder.homeSpace(U2, "G1_U2");
        builder.assertExpectations();
    }

    @Test
    public void testCreateTwoGroupsWithSamplesExperimentsShareIdsAndDistinctUsers()
    {
        // Given
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger, report())
                .commonSpaces(commonSpaces).commonSample("GAMMA/G", SAMPLE_TYPE)
                .commonExperiment("ALPHA/ST/STC", EXPERIMENT_TYPE).shareIdsMappingFile(mappingFile).get();
        List<String> globalSpaces = Arrays.asList("A", "B");
        userManager.setGlobalSpaces(globalSpaces);
        UserGroup group1 = new UserGroupAsBuilder("G1").admins(U1.getUserId(), "blabla");
        group1.setShareIds(Arrays.asList("1", "2"));
        userManager.addGroup(group1, users(U3, U1, U2));
        UserGroup group2 = new UserGroupAsBuilder("G2").admins(U4);
        group2.setShareIds(Arrays.asList("3"));
        userManager.addGroup(group2, users(U4));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [ADD-SPACES] [A, B]\n"
                + "1970-01-01 01:00:01 [ADD-AUTHORIZATION-GROUP] ALL_GROUPS\n"
                + "1970-01-01 01:00:02 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: ALL_GROUPS, role: SPACE_OBSERVER for A\n"
                + "1970-01-01 01:00:03 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: ALL_GROUPS, role: SPACE_OBSERVER for B\n"
                + "1970-01-01 01:00:04 [ADD-AUTHORIZATION-GROUP] G1\n"
                + "1970-01-01 01:00:05 [ADD-AUTHORIZATION-GROUP] G1_ADMIN\n"
                + "1970-01-01 01:00:06 [ADD-SPACE] G1_ALPHA\n"
                + "1970-01-01 01:00:07 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1, role: SPACE_USER for G1_ALPHA\n"
                + "1970-01-01 01:00:08 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_ALPHA\n"
                + "1970-01-01 01:00:09 [ADD-SPACE] G1_BETA\n"
                + "1970-01-01 01:00:10 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1, role: SPACE_USER for G1_BETA\n"
                + "1970-01-01 01:00:11 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_BETA\n"
                + "1970-01-01 01:00:12 [ADD-SPACE] G1_GAMMA\n"
                + "1970-01-01 01:00:13 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1, role: SPACE_OBSERVER for G1_GAMMA\n"
                + "1970-01-01 01:00:14 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_GAMMA\n"
                + "1970-01-01 01:00:15 [ADD-SPACE] G1_U1\n"
                + "1970-01-01 01:00:16 [ADD-USER] u1\n"
                + "1970-01-01 01:00:17 [ASSIGN-ROLE-TO-USER] user: u1, role: SPACE_ADMIN for G1_U1\n"
                + "1970-01-01 01:00:18 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_U1\n"
                + "1970-01-01 01:00:19 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G1, user: u1\n"
                + "1970-01-01 01:00:20 [ADD-USER-TO-AUTHORIZATION-GROUP] group: ALL_GROUPS, user: u1\n"
                + "1970-01-01 01:00:21 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, user: u1\n"
                + "1970-01-01 01:00:22 [ADD-SPACE] G1_U2\n"
                + "1970-01-01 01:00:23 [ADD-USER] u2\n"
                + "1970-01-01 01:00:24 [ASSIGN-ROLE-TO-USER] user: u2, role: SPACE_ADMIN for G1_U2\n"
                + "1970-01-01 01:00:25 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_U2\n"
                + "1970-01-01 01:00:26 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G1, user: u2\n"
                + "1970-01-01 01:00:27 [ADD-USER-TO-AUTHORIZATION-GROUP] group: ALL_GROUPS, user: u2\n"
                + "1970-01-01 01:00:28 [ADD-SPACE] G1_U3\n"
                + "1970-01-01 01:00:29 [ADD-USER] u3\n"
                + "1970-01-01 01:00:30 [ASSIGN-ROLE-TO-USER] user: u3, role: SPACE_ADMIN for G1_U3\n"
                + "1970-01-01 01:00:31 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_U3\n"
                + "1970-01-01 01:00:32 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G1, user: u3\n"
                + "1970-01-01 01:00:33 [ADD-USER-TO-AUTHORIZATION-GROUP] group: ALL_GROUPS, user: u3\n"
                + "1970-01-01 01:00:34 [ADD-SAMPLE] /G1_GAMMA/G1_G\n"
                + "1970-01-01 01:00:35 [ADD-PROJECT] /G1_ALPHA/G1_ST\n"
                + "1970-01-01 01:00:36 [ADD-EXPERIMENT] /G1_ALPHA/G1_ST/G1_STC\n"
                + "1970-01-01 01:00:37 [ADD-AUTHORIZATION-GROUP] G2\n"
                + "1970-01-01 01:00:38 [ADD-AUTHORIZATION-GROUP] G2_ADMIN\n"
                + "1970-01-01 01:00:39 [ADD-SPACE] G2_ALPHA\n"
                + "1970-01-01 01:00:40 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2, role: SPACE_USER for G2_ALPHA\n"
                + "1970-01-01 01:00:41 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_ALPHA\n"
                + "1970-01-01 01:00:42 [ADD-SPACE] G2_BETA\n"
                + "1970-01-01 01:00:43 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2, role: SPACE_USER for G2_BETA\n"
                + "1970-01-01 01:00:44 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_BETA\n"
                + "1970-01-01 01:00:45 [ADD-SPACE] G2_GAMMA\n"
                + "1970-01-01 01:00:46 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2, role: SPACE_OBSERVER for G2_GAMMA\n"
                + "1970-01-01 01:00:47 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_GAMMA\n"
                + "1970-01-01 01:00:48 [ADD-SPACE] G2_U4\n"
                + "1970-01-01 01:00:49 [ADD-USER] u4\n"
                + "1970-01-01 01:00:50 [ASSIGN-ROLE-TO-USER] user: u4, role: SPACE_ADMIN for G2_U4\n"
                + "1970-01-01 01:00:51 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_U4\n"
                + "1970-01-01 01:00:52 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u4\n"
                + "1970-01-01 01:00:53 [ADD-USER-TO-AUTHORIZATION-GROUP] group: ALL_GROUPS, user: u4\n"
                + "1970-01-01 01:00:54 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, user: u4\n"
                + "1970-01-01 01:00:55 [ADD-SAMPLE] /G2_GAMMA/G2_G\n"
                + "1970-01-01 01:00:56 [ADD-PROJECT] /G2_ALPHA/G2_ST\n"
                + "1970-01-01 01:00:57 [ADD-EXPERIMENT] /G2_ALPHA/G2_ST/G2_STC\n"
                + "1970-01-01 01:00:58 [ASSIGN-HOME-SPACE-FOR-USER] user: u1, home space: G1_U1\n"
                + "1970-01-01 01:00:59 [ASSIGN-HOME-SPACE-FOR-USER] user: u2, home space: G1_U2\n"
                + "1970-01-01 01:01:00 [ASSIGN-HOME-SPACE-FOR-USER] user: u3, home space: G1_U3\n"
                + "1970-01-01 01:01:01 [ASSIGN-HOME-SPACE-FOR-USER] user: u4, home space: G2_U4\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.globalSpaces(globalSpaces).groups("G1", "G2").commonSpaces(commonSpaces).users(U1, U2, U3, U4);
        builder.samples(SAMPLE_TYPE, "/G1_GAMMA/G1_G", "/G2_GAMMA/G2_G");
        builder.experiments(EXPERIMENT_TYPE, "/G1_ALPHA/G1_ST/G1_STC", "/G2_ALPHA/G2_ST/G2_STC");
        builder.space("A").observer(U1, U2, U3, U4);
        builder.space("B").observer(U1, U2, U3, U4);
        builder.space("G1_ALPHA").admin(U1).user(U2, U3).non(U4);
        builder.space("G1_BETA").admin(U1).user(U2, U3).non(U4);
        builder.space("G1_GAMMA").admin(U1).observer(U2, U3).non(U4);
        builder.space("G1_U1").admin(U1).non(U2, U3, U4);
        builder.space("G1_U2").admin(U1, U2).non(U3, U4);
        builder.space("G1_U3").admin(U1).non(U2).admin(U3).non(U4);
        builder.space("G2_ALPHA").non(U1, U2, U3).admin(U4);
        builder.space("G2_BETA").non(U1, U2, U3).admin(U4);
        builder.space("G2_GAMMA").non(U1, U2, U3).admin(U4);
        builder.space("G2_U4").non(U1, U2, U3).admin(U4);
        builder.homeSpace(U1, "G1_U1");
        builder.homeSpace(U2, "G1_U2");
        builder.homeSpace(U3, "G1_U3");
        builder.homeSpace(U4, "G2_U4");
        builder.assertExpectations();
        assertEquals(FileUtilities.loadToString(mappingFile), "Identifier\tShare IDs\tArchive Folder\n"
                + "/G1_.*\t1, 2\t\n"
                + "/G2_.*\t3\t\n");
    }

    @Test
    public void testCreateTwoGroupsWithSharedUsers()
    {
        // Given
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G1").admins(U1), users(U1, U2, U3));
        userManager.addGroup(new UserGroupAsBuilder("G2").admins(U3, U4), users(U2, U3, U4));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [ADD-AUTHORIZATION-GROUP] G1\n"
                + "1970-01-01 01:00:01 [ADD-AUTHORIZATION-GROUP] G1_ADMIN\n"
                + "1970-01-01 01:00:02 [ADD-SPACE] G1_ALPHA\n"
                + "1970-01-01 01:00:03 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1, role: SPACE_USER for G1_ALPHA\n"
                + "1970-01-01 01:00:04 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_ALPHA\n"
                + "1970-01-01 01:00:05 [ADD-SPACE] G1_BETA\n"
                + "1970-01-01 01:00:06 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1, role: SPACE_USER for G1_BETA\n"
                + "1970-01-01 01:00:07 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_BETA\n"
                + "1970-01-01 01:00:08 [ADD-SPACE] G1_GAMMA\n"
                + "1970-01-01 01:00:09 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1, role: SPACE_OBSERVER for G1_GAMMA\n"
                + "1970-01-01 01:00:10 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_GAMMA\n"
                + "1970-01-01 01:00:11 [ADD-SPACE] G1_U1\n"
                + "1970-01-01 01:00:12 [ADD-USER] u1\n"
                + "1970-01-01 01:00:13 [ASSIGN-ROLE-TO-USER] user: u1, role: SPACE_ADMIN for G1_U1\n"
                + "1970-01-01 01:00:14 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_U1\n"
                + "1970-01-01 01:00:15 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G1, user: u1\n"
                + "1970-01-01 01:00:16 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, user: u1\n"
                + "1970-01-01 01:00:17 [ADD-SPACE] G1_U2\n"
                + "1970-01-01 01:00:18 [ADD-USER] u2\n"
                + "1970-01-01 01:00:19 [ASSIGN-ROLE-TO-USER] user: u2, role: SPACE_ADMIN for G1_U2\n"
                + "1970-01-01 01:00:20 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_U2\n"
                + "1970-01-01 01:00:21 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G1, user: u2\n"
                + "1970-01-01 01:00:22 [ADD-SPACE] G1_U3\n"
                + "1970-01-01 01:00:23 [ADD-USER] u3\n"
                + "1970-01-01 01:00:24 [ASSIGN-ROLE-TO-USER] user: u3, role: SPACE_ADMIN for G1_U3\n"
                + "1970-01-01 01:00:25 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_U3\n"
                + "1970-01-01 01:00:26 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G1, user: u3\n"
                + "1970-01-01 01:00:27 [ADD-AUTHORIZATION-GROUP] G2\n"
                + "1970-01-01 01:00:28 [ADD-AUTHORIZATION-GROUP] G2_ADMIN\n"
                + "1970-01-01 01:00:29 [ADD-SPACE] G2_ALPHA\n"
                + "1970-01-01 01:00:30 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2, role: SPACE_USER for G2_ALPHA\n"
                + "1970-01-01 01:00:31 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_ALPHA\n"
                + "1970-01-01 01:00:32 [ADD-SPACE] G2_BETA\n"
                + "1970-01-01 01:00:33 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2, role: SPACE_USER for G2_BETA\n"
                + "1970-01-01 01:00:34 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_BETA\n"
                + "1970-01-01 01:00:35 [ADD-SPACE] G2_GAMMA\n"
                + "1970-01-01 01:00:36 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2, role: SPACE_OBSERVER for G2_GAMMA\n"
                + "1970-01-01 01:00:37 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_GAMMA\n"
                + "1970-01-01 01:00:38 [ADD-SPACE] G2_U2\n"
                + "1970-01-01 01:00:39 [ASSIGN-ROLE-TO-USER] user: u2, role: SPACE_ADMIN for G2_U2\n"
                + "1970-01-01 01:00:40 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_U2\n"
                + "1970-01-01 01:00:41 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u2\n"
                + "1970-01-01 01:00:42 [ADD-SPACE] G2_U3\n"
                + "1970-01-01 01:00:43 [ASSIGN-ROLE-TO-USER] user: u3, role: SPACE_ADMIN for G2_U3\n"
                + "1970-01-01 01:00:44 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_U3\n"
                + "1970-01-01 01:00:45 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u3\n"
                + "1970-01-01 01:00:46 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, user: u3\n"
                + "1970-01-01 01:00:47 [ADD-SPACE] G2_U4\n"
                + "1970-01-01 01:00:48 [ADD-USER] u4\n"
                + "1970-01-01 01:00:49 [ASSIGN-ROLE-TO-USER] user: u4, role: SPACE_ADMIN for G2_U4\n"
                + "1970-01-01 01:00:50 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_U4\n"
                + "1970-01-01 01:00:51 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u4\n"
                + "1970-01-01 01:00:52 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, user: u4\n"
                + "1970-01-01 01:00:53 [ASSIGN-HOME-SPACE-FOR-USER] user: u1, home space: G1_U1\n"
                + "1970-01-01 01:00:54 [ASSIGN-HOME-SPACE-FOR-USER] user: u2, home space: G1_U2\n"
                + "1970-01-01 01:00:55 [ASSIGN-HOME-SPACE-FOR-USER] user: u3, home space: G1_U3\n"
                + "1970-01-01 01:00:56 [ASSIGN-HOME-SPACE-FOR-USER] user: u4, home space: G2_U4\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.groups("G1", "G2").commonSpaces(commonSpaces).users(U1, U2, U3, U4);
        builder.space("G1_ALPHA").admin(U1).user(U2, U3).non(U4);
        builder.space("G1_BETA").admin(U1).user(U2, U3).non(U4);
        builder.space("G1_GAMMA").admin(U1).observer(U2, U3).non(U4);
        builder.space("G1_U1").admin(U1).non(U2, U3, U4);
        builder.space("G1_U2").admin(U1, U2).non(U3, U4);
        builder.space("G1_U3").admin(U1).non(U2).admin(U3).non(U4);
        builder.space("G2_ALPHA").non(U1).user(U2).admin(U3, U4);
        builder.space("G2_BETA").non(U1).user(U2).admin(U3, U4);
        builder.space("G2_GAMMA").non(U1).observer(U2).admin(U3, U4);
        builder.space("G2_U2").non(U1).admin(U2, U3, U4);
        builder.space("G2_U3").non(U1, U2).admin(U3, U4);
        builder.space("G2_U4").non(U1, U2).admin(U3, U4);
        builder.homeSpace(U1, "G1_U1");
        builder.homeSpace(U2, "G1_U2");
        builder.homeSpace(U3, "G1_U3");
        builder.homeSpace(U4, "G2_U4");
        builder.assertExpectations();
    }

    @Test
    public void testCreateGroupUsingEmailAsUserId()
    {
        // Given
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G1").useEmailAsUserId(true).admins(U1), users(U1, U2, U3));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [ADD-AUTHORIZATION-GROUP] G1\n"
                + "1970-01-01 01:00:01 [ADD-AUTHORIZATION-GROUP] G1_ADMIN\n"
                + "1970-01-01 01:00:02 [ADD-SPACE] G1_ALPHA\n"
                + "1970-01-01 01:00:03 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1, role: SPACE_USER for G1_ALPHA\n"
                + "1970-01-01 01:00:04 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_ALPHA\n"
                + "1970-01-01 01:00:05 [ADD-SPACE] G1_BETA\n"
                + "1970-01-01 01:00:06 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1, role: SPACE_USER for G1_BETA\n"
                + "1970-01-01 01:00:07 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_BETA\n"
                + "1970-01-01 01:00:08 [ADD-SPACE] G1_GAMMA\n"
                + "1970-01-01 01:00:09 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1, role: SPACE_OBSERVER for G1_GAMMA\n"
                + "1970-01-01 01:00:10 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_GAMMA\n"
                + "1970-01-01 01:00:11 [ADD-SPACE] G1_A.E_AT_ABC.DE\n"
                + "1970-01-01 01:00:12 [ADD-USER] a.e_AT_abc.de\n"
                + "1970-01-01 01:00:13 [ASSIGN-ROLE-TO-USER] user: a.e_AT_abc.de, role: SPACE_ADMIN for G1_A.E_AT_ABC.DE\n"
                + "1970-01-01 01:00:14 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_A.E_AT_ABC.DE\n"
                + "1970-01-01 01:00:15 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G1, user: a.e_AT_abc.de\n"
                + "1970-01-01 01:00:16 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, user: a.e_AT_abc.de\n"
                + "1970-01-01 01:00:17 [ADD-SPACE] G1_I.N_AT_ABC.DE\n"
                + "1970-01-01 01:00:18 [ADD-USER] i.n_AT_abc.de\n"
                + "1970-01-01 01:00:19 [ASSIGN-ROLE-TO-USER] user: i.n_AT_abc.de, role: SPACE_ADMIN for G1_I.N_AT_ABC.DE\n"
                + "1970-01-01 01:00:20 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_I.N_AT_ABC.DE\n"
                + "1970-01-01 01:00:21 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G1, user: i.n_AT_abc.de\n"
                + "1970-01-01 01:00:22 [ADD-SPACE] G1_A.T_AT_ABC.DE\n"
                + "1970-01-01 01:00:23 [ADD-USER] a.t_AT_abc.de\n"
                + "1970-01-01 01:00:24 [ASSIGN-ROLE-TO-USER] user: a.t_AT_abc.de, role: SPACE_ADMIN for G1_A.T_AT_ABC.DE\n"
                + "1970-01-01 01:00:25 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_A.T_AT_ABC.DE\n"
                + "1970-01-01 01:00:26 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G1, user: a.t_AT_abc.de\n"
                + "1970-01-01 01:00:27 [ASSIGN-HOME-SPACE-FOR-USER] user: a.e_AT_abc.de, home space: G1_A.E_AT_ABC.DE\n"
                + "1970-01-01 01:00:28 [ASSIGN-HOME-SPACE-FOR-USER] user: a.t_AT_abc.de, home space: G1_A.T_AT_ABC.DE\n"
                + "1970-01-01 01:00:29 [ASSIGN-HOME-SPACE-FOR-USER] user: i.n_AT_abc.de, home space: G1_I.N_AT_ABC.DE\n");
        UserManagerExpectationsBuilder builder = createBuilder().useEmailAsUserId();
        builder.groups("G1").commonSpaces(commonSpaces).users(U1, U2, U3);
        builder.space("G1_ALPHA").admin(U1).user(U2, U3);
        builder.space("G1_BETA").admin(U1).user(U2, U3);
        builder.space("G1_GAMMA").admin(U1).observer(U2, U3);
        builder.space("G1_A.E_AT_ABC.DE").admin(U1).non(U2, U3);
        builder.space("G1_I.N_AT_ABC.DE").admin(U1).admin(U2).non(U3);
        builder.space("G1_A.T_AT_ABC.DE").admin(U1).non(U2).admin(U3);
        builder.homeSpace(U1, "G1_A.E_AT_ABC.DE");
        builder.homeSpace(U2, "G1_I.N_AT_ABC.DE");
        builder.homeSpace(U3, "G1_A.T_AT_ABC.DE");
        builder.assertExpectations();
    }

    @Test
    public void testAddUsersToAnExistingGroup()
    {
        // Given
        // 1. create group G2 with user U1 (admin)
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).shareIdsMappingFile(mappingFile).get();
        UserGroup group = new UserGroupAsBuilder("G2").admins(U1.getUserId(), "blabla");
        group.setShareIds(Arrays.asList("2", "3"));
        userManager.addGroup(group, users(U1));
        assertEquals(manage(userManager).getErrorReport(), "");
        assertEquals(FileUtilities.loadToString(mappingFile), "Identifier\tShare IDs\tArchive Folder\n/G2_.*\t2, 3\t\n");
        // 2. add users U2 and U3 to group G2
        userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).shareIdsMappingFile(mappingFile).get();
        group = new UserGroupAsBuilder("G2").admins(U1);
        group.setShareIds(Arrays.asList("4"));
        userManager.addGroup(group, users(U1, U2, U3));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [ADD-SPACE] G2_U2\n" +
                "1970-01-01 01:00:01 [ADD-USER] u2\n" +
                "1970-01-01 01:00:02 [ASSIGN-ROLE-TO-USER] user: u2, role: SPACE_ADMIN for G2_U2\n" +
                "1970-01-01 01:00:03 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_U2\n" +
                "1970-01-01 01:00:04 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u2\n" +
                "1970-01-01 01:00:05 [ADD-SPACE] G2_U3\n" +
                "1970-01-01 01:00:06 [ADD-USER] u3\n" +
                "1970-01-01 01:00:07 [ASSIGN-ROLE-TO-USER] user: u3, role: SPACE_ADMIN for G2_U3\n" +
                "1970-01-01 01:00:08 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_U3\n" +
                "1970-01-01 01:00:09 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u3\n" +
                "1970-01-01 01:00:10 [ASSIGN-HOME-SPACE-FOR-USER] user: u2, home space: G2_U2\n" +
                "1970-01-01 01:00:11 [ASSIGN-HOME-SPACE-FOR-USER] user: u3, home space: G2_U3\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.groups("G2").commonSpaces(commonSpaces).users(U1, U2, U3);
        builder.space("G2_ALPHA").admin(U1).user(U2, U3);
        builder.space("G2_BETA").admin(U1).user(U2, U3);
        builder.space("G2_GAMMA").admin(U1).observer(U2, U3);
        builder.space("G2_U1").admin(U1).non(U2, U3);
        builder.space("G2_U2").admin(U1, U2).non(U3);
        builder.space("G2_U3").admin(U1).non(U2).admin(U3);
        builder.homeSpace(U1, "G2_U1");
        builder.homeSpace(U2, "G2_U2");
        builder.homeSpace(U3, "G2_U3");
        builder.assertExpectations();
        assertEquals(FileUtilities.loadToString(mappingFile), "Identifier\tShare IDs\tArchive Folder\n"
                + "/G2_.*\t4\t\n");
    }

    @Test
    public void testRemoveNormalUserFromAGroup()
    {
        // Given
        // 1. create group G2 with users U1 (admin), U2 and U3
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        List<String> globalSpaces = Arrays.asList("A", "B");
        userManager.setGlobalSpaces(globalSpaces);
        userManager.addGroup(new UserGroupAsBuilder("G2").admins(U1), users(U1, U2, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        // 2. remove U2 from group G2
        userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.setGlobalSpaces(globalSpaces);
        userManager.addGroup(new UserGroupAsBuilder("G2").admins(U1), users(U1, U3));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [REMOVE-USER-FROM-AUTHORIZATION-GROUP] group: G2, user: u2\n"
                + "1970-01-01 01:00:01 [UNASSIGN-ROLE-FORM-USER] user: u2, role: SPACE_ADMIN for G2_U2\n"
                + "1970-01-01 01:00:02 [REMOVE-USER-FROM-AUTHORIZATION-GROUP] group: ALL_GROUPS, user: u2\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.groups("G2").commonSpaces(commonSpaces).users(U1, U3);
        builder.usersWithoutAuthentication(U2);
        builder.space("A").observer(U1).observer(U3);
        builder.space("B").observer(U1).observer(U3);
        builder.space("G2_ALPHA").admin(U1).user(U3);
        builder.space("G2_BETA").admin(U1).user(U3);
        builder.space("G2_GAMMA").admin(U1).observer(U3);
        builder.space("G2_U1").admin(U1).non(U3);
        builder.space("G2_U2").admin(U1).non(U3);
        builder.space("G2_U3").admin(U1).admin(U3);
        builder.homeSpace(U1, "G2_U1");
        builder.homeSpace(U2, "G2_U2");
        builder.homeSpace(U3, "G2_U3");
        builder.assertExpectations();
    }

    @Test
    public void testRemoveUserToBeIgnoredFromAGroup()
    {
        // Given
        // 1. create group G2 with users U1 (admin), U2 and U3
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        List<String> globalSpaces = Arrays.asList("A", "B");
        userManager.setGlobalSpaces(globalSpaces);
        userManager.addGroup(new UserGroupAsBuilder("G2").admins(U1), users(U1, U2, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        // 2. remove U2 from group G2
        userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.setGlobalSpaces(globalSpaces);
        userManager.addGroup(new UserGroupAsBuilder("G2").admins(U1), users(U1, U3));

        // When
        UserManagerReport report = manage(userManager, Collections.singleton(U2.getUserId()));

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.groups("G2").commonSpaces(commonSpaces).users(U1, U2, U3);
        builder.space("A").observer(U1).observer(U2).observer(U3);
        builder.space("B").observer(U1).observer(U2).observer(U3);
        builder.space("G2_ALPHA").admin(U1).user(U2, U3);
        builder.space("G2_BETA").admin(U1).user(U2, U3);
        builder.space("G2_GAMMA").admin(U1).observer(U2, U3);
        builder.space("G2_U1").admin(U1).non(U2, U3);
        builder.space("G2_U2").admin(U1).admin(U2).non(U3);
        builder.space("G2_U3").admin(U1).non(U2).admin(U3);
        builder.homeSpace(U1, "G2_U1");
        builder.homeSpace(U2, "G2_U2");
        builder.homeSpace(U3, "G2_U3");
        builder.assertExpectations();
    }

    @Test
    public void testMoveUserToAnotherGroup()
    {
        // Given
        // 1. create group G1 with users U1 (admin), U2 and group G2 with user U3 (admin)
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        List<String> globalSpaces = Arrays.asList("A", "B");
        userManager.setGlobalSpaces(globalSpaces);
        userManager.addGroup(new UserGroupAsBuilder("G1").admins(U1), users(U1, U2));
        userManager.addGroup(new UserGroupAsBuilder("G2").admins(U3), users(U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        // 2. move user U2 from group G1 to group G2
        userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.setGlobalSpaces(globalSpaces);
        userManager.addGroup(new UserGroupAsBuilder("G1").admins(U1), users(U1));
        userManager.addGroup(new UserGroupAsBuilder("G2").admins(U3), users(U2, U3));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [REMOVE-USER-FROM-AUTHORIZATION-GROUP] group: G1, user: u2\n"
                + "1970-01-01 01:00:01 [UNASSIGN-ROLE-FORM-USER] user: u2, role: SPACE_ADMIN for G1_U2\n"
                + "1970-01-01 01:00:02 [ADD-SPACE] G2_U2\n"
                + "1970-01-01 01:00:03 [ASSIGN-ROLE-TO-USER] user: u2, role: SPACE_ADMIN for G2_U2\n"
                + "1970-01-01 01:00:04 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_U2\n"
                + "1970-01-01 01:00:05 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u2\n"
                + "1970-01-01 01:00:06 [ASSIGN-HOME-SPACE-FOR-USER] user: u2, home space: G2_U2\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.groups("G1").commonSpaces(commonSpaces).users(U1);
        builder.groups("G2").commonSpaces(commonSpaces).users(U2, U3);
        builder.usersWithoutAuthentication();
        builder.space("A").observer(U1, U2, U3);
        builder.space("B").observer(U1, U2, U3);
        builder.space("G1_ALPHA").admin(U1).non(U2, U3);
        builder.space("G1_BETA").admin(U1).non(U2, U3);
        builder.space("G1_GAMMA").admin(U1).non(U2, U3);
        builder.space("G2_ALPHA").admin(U3).user(U2).non(U1);
        builder.space("G2_BETA").admin(U3).user(U2).non(U1);
        builder.space("G2_GAMMA").admin(U3).observer(U2).non(U1);
        builder.homeSpace(U1, "G1_U1");
        builder.homeSpace(U2, "G2_U2");
        builder.homeSpace(U3, "G2_U3");
        builder.assertExpectations();
    }

    
    @Test
    public void testMoveUserToAnotherGroupAndRemovePreviousGroup()
    {
        // Given
        // 1. create group G1 with users U1 (admin), U2 and group G2 with user U3 (admin)
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        List<String> globalSpaces = Arrays.asList("A", "B");
        userManager.setGlobalSpaces(globalSpaces);
        userManager.addGroup(new UserGroupAsBuilder("G1").admins(U1), users(U1, U2));
        userManager.addGroup(new UserGroupAsBuilder("G2").admins(U3), users(U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        // 2. move user U2 from group G1 to group G2 and remove group G1
        userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.setGlobalSpaces(globalSpaces);
        userManager.addGroup(new UserGroupAsBuilder("G2").admins(U3), users(U2, U3));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [REMOVE-USER-FROM-AUTHORIZATION-GROUP] group: G1, user: u1\n"
                + "1970-01-01 01:00:01 [REMOVE-USER-FROM-AUTHORIZATION-GROUP] group: G1_ADMIN, user: u1\n"
                + "1970-01-01 01:00:02 [UNASSIGN-ROLE-FORM-USER] user: u1, role: SPACE_ADMIN for G1_U1\n"
                + "1970-01-01 01:00:03 [REMOVE-USER-FROM-AUTHORIZATION-GROUP] group: G1, user: u2\n"
                + "1970-01-01 01:00:04 [UNASSIGN-ROLE-FORM-USER] user: u2, role: SPACE_ADMIN for G1_U2\n"
                + "1970-01-01 01:00:05 [REMOVE-AUTHORIZATION-GROUP] G1\n"
                + "1970-01-01 01:00:06 [REMOVE-AUTHORIZATION-GROUP] G1_ADMIN\n"
                + "1970-01-01 01:00:07 [ADD-SPACE] G2_U2\n"
                + "1970-01-01 01:00:08 [ASSIGN-ROLE-TO-USER] user: u2, role: SPACE_ADMIN for G2_U2\n"
                + "1970-01-01 01:00:09 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_U2\n"
                + "1970-01-01 01:00:10 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u2\n"
                + "1970-01-01 01:00:11 [ASSIGN-HOME-SPACE-FOR-USER] user: u2, home space: G2_U2\n"
                + "1970-01-01 01:00:12 [REMOVE-USER-FROM-AUTHORIZATION-GROUP] group: ALL_GROUPS, user: u1\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.groups("G1").commonSpaces(commonSpaces).users();
        builder.groups("G2").commonSpaces(commonSpaces).users(U2, U3);
        builder.usersWithoutAuthentication(U1);
        builder.space("A").observer(U2, U3);
        builder.space("B").observer(U2, U3);
        builder.space("G1_ALPHA").non(U2, U3);
        builder.space("G1_BETA").non(U2, U3);
        builder.space("G1_GAMMA").non(U2, U3);
        builder.space("G2_ALPHA").admin(U3).user(U2);
        builder.space("G2_BETA").admin(U3).user(U2);
        builder.space("G2_GAMMA").admin(U3).observer(U2);
        builder.homeSpace(U1, "G1_U1");
        builder.homeSpace(U2, "G2_U2");
        builder.homeSpace(U3, "G2_U3");
        builder.assertExpectations();
    }

    @Test
    public void testDisableAGroup()
    {
        // Given
        // 1. create group G2 with users U1 (admin), U2 and U3
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        List<String> globalSpaces = Arrays.asList("A", "B");
        userManager.setGlobalSpaces(globalSpaces);
        userManager.addGroup(new UserGroupAsBuilder("G2").admins(U1), users(U1, U2, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        // 2. disable group G2
        userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.setGlobalSpaces(globalSpaces);
        userManager.addGroup(new UserGroupAsBuilder("G2").admins(U1).disable(), users(U1, U2, U3));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [REMOVE-USER-FROM-AUTHORIZATION-GROUP] group: G2, user: u1\n"
                + "1970-01-01 01:00:01 [REMOVE-USER-FROM-AUTHORIZATION-GROUP] group: G2_ADMIN, user: u1\n"
                + "1970-01-01 01:00:02 [UNASSIGN-ROLE-FORM-USER] user: u1, role: SPACE_ADMIN for G2_U1\n"
                + "1970-01-01 01:00:03 [REMOVE-USER-FROM-AUTHORIZATION-GROUP] group: G2, user: u2\n"
                + "1970-01-01 01:00:04 [UNASSIGN-ROLE-FORM-USER] user: u2, role: SPACE_ADMIN for G2_U2\n"
                + "1970-01-01 01:00:05 [REMOVE-USER-FROM-AUTHORIZATION-GROUP] group: G2, user: u3\n"
                + "1970-01-01 01:00:06 [UNASSIGN-ROLE-FORM-USER] user: u3, role: SPACE_ADMIN for G2_U3\n"
                + "1970-01-01 01:00:07 [REMOVE-USER-FROM-AUTHORIZATION-GROUP] group: ALL_GROUPS, user: u1\n"
                + "1970-01-01 01:00:08 [REMOVE-USER-FROM-AUTHORIZATION-GROUP] group: ALL_GROUPS, user: u2\n"
                + "1970-01-01 01:00:09 [REMOVE-USER-FROM-AUTHORIZATION-GROUP] group: ALL_GROUPS, user: u3\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.groups("G2").users();
        builder.usersWithoutAuthentication(U1, U2, U3);
        builder.homeSpace(U1, "G2_U1");
        builder.homeSpace(U2, "G2_U2");
        builder.homeSpace(U3, "G2_U3");
        builder.assertExpectations();
    }

    @Test
    public void testDisableEnableAGroup()
    {
        // Given
        // 1. create group G2 with users U1 (admin), U2 and U3
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        List<String> globalSpaces = Arrays.asList("A", "B");
        userManager.setGlobalSpaces(globalSpaces);
        userManager.addGroup(new UserGroupAsBuilder("G2").admins(U1), users(U1, U2, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        // 2. disable group G2
        userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.setGlobalSpaces(globalSpaces);
        userManager.addGroup(new UserGroupAsBuilder("G2").admins(U1).disable(), users(U1, U2, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        createBuilder().groups("G2").users().usersWithoutAuthentication(U1, U2, U3).assertExpectations();
        // 3. enable group G2 again
        userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.setGlobalSpaces(globalSpaces);
        userManager.addGroup(new UserGroupAsBuilder("G2").admins(U1), users(U1, U2, U3));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [ADD-SPACE] G2_U1_2\n"
                + "1970-01-01 01:00:01 [ASSIGN-ROLE-TO-USER] user: u1, role: SPACE_ADMIN for G2_U1_2\n"
                + "1970-01-01 01:00:02 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_U1_2\n"
                + "1970-01-01 01:00:03 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u1\n"
                + "1970-01-01 01:00:04 [ADD-USER-TO-AUTHORIZATION-GROUP] group: ALL_GROUPS, user: u1\n"
                + "1970-01-01 01:00:05 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, user: u1\n"
                + "1970-01-01 01:00:06 [ADD-SPACE] G2_U2_2\n"
                + "1970-01-01 01:00:07 [ASSIGN-ROLE-TO-USER] user: u2, role: SPACE_ADMIN for G2_U2_2\n"
                + "1970-01-01 01:00:08 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_U2_2\n"
                + "1970-01-01 01:00:09 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u2\n"
                + "1970-01-01 01:00:10 [ADD-USER-TO-AUTHORIZATION-GROUP] group: ALL_GROUPS, user: u2\n"
                + "1970-01-01 01:00:11 [ADD-SPACE] G2_U3_2\n"
                + "1970-01-01 01:00:12 [ASSIGN-ROLE-TO-USER] user: u3, role: SPACE_ADMIN for G2_U3_2\n"
                + "1970-01-01 01:00:13 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_U3_2\n"
                + "1970-01-01 01:00:14 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u3\n"
                + "1970-01-01 01:00:15 [ADD-USER-TO-AUTHORIZATION-GROUP] group: ALL_GROUPS, user: u3\n"
                + "1970-01-01 01:00:16 [ASSIGN-HOME-SPACE-FOR-USER] user: u1, home space: G2_U1_2\n"
                + "1970-01-01 01:00:17 [ASSIGN-HOME-SPACE-FOR-USER] user: u2, home space: G2_U2_2\n"
                + "1970-01-01 01:00:18 [ASSIGN-HOME-SPACE-FOR-USER] user: u3, home space: G2_U3_2\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.groups("G2").commonSpaces(commonSpaces).users(U1, U2, U3);
        builder.space("A").observer(U1).observer(U2).observer(U3);
        builder.space("B").observer(U1).observer(U2).observer(U3);
        builder.space("G2_ALPHA").admin(U1).user(U2).user(U3);
        builder.space("G2_BETA").admin(U1).user(U2).user(U3);
        builder.space("G2_GAMMA").admin(U1).observer(U2).observer(U3);
        builder.space("G2_U1").admin(U1).non(U2).non(U3);
        builder.space("G2_U2").admin(U1).non(U2).non(U3);
        builder.space("G2_U3").admin(U1).non(U2).non(U3);
        builder.space("G2_U1_2").admin(U1).non(U2).non(U3);
        builder.space("G2_U2_2").admin(U1).admin(U2).non(U3);
        builder.space("G2_U3_2").admin(U1).non(U2).admin(U3);
        builder.homeSpace(U1, "G2_U1_2");
        builder.homeSpace(U2, "G2_U2_2");
        builder.homeSpace(U3, "G2_U3_2");
        builder.assertExpectations();
    }

    @Test
    public void testRemoveAGroup()
    {
        // Given
        // 1. create group G1 with users U1 (admin) and group G2 with U2 (admin) and U3
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        List<String> globalSpaces = Arrays.asList("A", "B");
        userManager.setGlobalSpaces(globalSpaces);
        userManager.addGroup(new UserGroupAsBuilder("G1").admins(U1), users(U1));
        userManager.addGroup(new UserGroupAsBuilder("G2").admins(U2), users(U2, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        // 2. remove group G2
        userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.setGlobalSpaces(globalSpaces);
        userManager.addGroup(new UserGroupAsBuilder("G1").admins(U1), users(U1));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [REMOVE-USER-FROM-AUTHORIZATION-GROUP] group: G2, user: u2\n"
                + "1970-01-01 01:00:01 [REMOVE-USER-FROM-AUTHORIZATION-GROUP] group: G2_ADMIN, user: u2\n"
                + "1970-01-01 01:00:02 [UNASSIGN-ROLE-FORM-USER] user: u2, role: SPACE_ADMIN for G2_U2\n"
                + "1970-01-01 01:00:03 [REMOVE-USER-FROM-AUTHORIZATION-GROUP] group: G2, user: u3\n"
                + "1970-01-01 01:00:04 [UNASSIGN-ROLE-FORM-USER] user: u3, role: SPACE_ADMIN for G2_U3\n"
                + "1970-01-01 01:00:05 [REMOVE-AUTHORIZATION-GROUP] G2\n"
                + "1970-01-01 01:00:06 [REMOVE-AUTHORIZATION-GROUP] G2_ADMIN\n"
                + "1970-01-01 01:00:07 [REMOVE-USER-FROM-AUTHORIZATION-GROUP] group: ALL_GROUPS, user: u2\n"
                + "1970-01-01 01:00:08 [REMOVE-USER-FROM-AUTHORIZATION-GROUP] group: ALL_GROUPS, user: u3\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.groups("G1").commonSpaces(commonSpaces).users(U1);
        builder.usersWithoutAuthentication(U2, U3);
        builder.space("G1_ALPHA").admin(U1);
        builder.space("G1_BETA").admin(U1);
        builder.space("G1_GAMMA").admin(U1);
        builder.homeSpace(U1, "G1_U1");
        builder.homeSpace(U2, "G2_U2");
        builder.homeSpace(U3, "G2_U3");
        builder.assertExpectations();
    }

    @Test
    public void testRemoveAGroupWithAUserToBeIgnored()
    {
        // Given
        // 1. create group G1 with users U1 (admin) and group G2 with U2 (admin) and U3
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        List<String> globalSpaces = Arrays.asList("A", "B");
        userManager.setGlobalSpaces(globalSpaces);
        userManager.addGroup(new UserGroupAsBuilder("G1").admins(U1), users(U1));
        userManager.addGroup(new UserGroupAsBuilder("G2").admins(U2), users(U2, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        // 2. remove group G2
        userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.setGlobalSpaces(globalSpaces);
        userManager.addGroup(new UserGroupAsBuilder("G1").admins(U1), users(U1));

        // When
        UserManagerReport report = manage(userManager, Collections.singleton(U3.getUserId()));

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [REMOVE-USER-FROM-AUTHORIZATION-GROUP] group: G2, user: u2\n"
                + "1970-01-01 01:00:01 [REMOVE-USER-FROM-AUTHORIZATION-GROUP] group: G2_ADMIN, user: u2\n"
                + "1970-01-01 01:00:02 [UNASSIGN-ROLE-FORM-USER] user: u2, role: SPACE_ADMIN for G2_U2\n"
                + "1970-01-01 01:00:03 [REMOVE-AUTHORIZATION-GROUP] G2\n"
                + "1970-01-01 01:00:04 [REMOVE-AUTHORIZATION-GROUP] G2_ADMIN\n"
                + "1970-01-01 01:00:05 [REMOVE-USER-FROM-AUTHORIZATION-GROUP] group: ALL_GROUPS, user: u2\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.groups("G1").commonSpaces(commonSpaces).users(U1);
        builder.usersWithoutAuthentication(U2);
        builder.space("G1_ALPHA").admin(U1);
        builder.space("G1_BETA").admin(U1);
        builder.space("G1_GAMMA").admin(U1);
        builder.space("G2_U3").non(U1).admin(U3);
        builder.homeSpace(U1, "G1_U1");
        builder.homeSpace(U2, "G2_U2");
        builder.homeSpace(U3, "G2_U3");
        builder.assertExpectations();
    }

    @Test
    public void testRemoveAGroupAndAddItAgain()
    {
        // Given
        // 1. create group G1 with users U1 (admin) and group G2 with U2 (admin) and U3
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        List<String> globalSpaces = Arrays.asList("A", "B");
        userManager.setGlobalSpaces(globalSpaces);
        userManager.addGroup(new UserGroupAsBuilder("G1").admins(U1), users(U1));
        userManager.addGroup(new UserGroupAsBuilder("G2").admins(U2), users(U2, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.groups("G1").commonSpaces(commonSpaces).users(U1);
        builder.groups("G2").commonSpaces(commonSpaces).users(U2, U3);
        builder.space("G1_ALPHA").admin(U1).non(U2, U3);
        builder.space("G1_BETA").admin(U1).non(U2, U3);
        builder.space("G1_GAMMA").admin(U1).non(U2, U3);
        builder.space("G2_ALPHA").non(U1).admin(U2).user(U3);
        builder.space("G2_BETA").non(U1).admin(U2).user(U3);
        builder.space("G2_GAMMA").non(U1).admin(U2).observer(U3);
        builder.space("G2_U2").non(U1).admin(U2).non(U3);
        builder.space("G2_U3").non(U1).admin(U2).admin(U3);
        builder.homeSpace(U1, "G1_U1");
        builder.homeSpace(U2, "G2_U2");
        builder.homeSpace(U3, "G2_U3");
        builder.assertExpectations();
        // 2. remove group G2
        userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.setGlobalSpaces(globalSpaces);
        userManager.addGroup(new UserGroupAsBuilder("G1").admins(U1), users(U1));
        assertEquals(manage(userManager).getErrorReport(), "");
        // 3. add group G2 again
        userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.setGlobalSpaces(globalSpaces);
        userManager.addGroup(new UserGroupAsBuilder("G1").admins(U1), users(U1));
        userManager.addGroup(new UserGroupAsBuilder("G2").admins(U2), users(U2, U3));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [ADD-AUTHORIZATION-GROUP] G2\n"
                + "1970-01-01 01:00:01 [ADD-AUTHORIZATION-GROUP] G2_ADMIN\n"
                + "1970-01-01 01:00:02 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2, role: SPACE_USER for G2_ALPHA\n"
                + "1970-01-01 01:00:03 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_ALPHA\n"
                + "1970-01-01 01:00:04 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2, role: SPACE_USER for G2_BETA\n"
                + "1970-01-01 01:00:05 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_BETA\n"
                + "1970-01-01 01:00:06 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2, role: SPACE_OBSERVER for G2_GAMMA\n"
                + "1970-01-01 01:00:07 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_GAMMA\n"
                + "1970-01-01 01:00:08 [ADD-SPACE] G2_U2_2\n"
                + "1970-01-01 01:00:09 [ASSIGN-ROLE-TO-USER] user: u2, role: SPACE_ADMIN for G2_U2_2\n"
                + "1970-01-01 01:00:10 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_U2_2\n"
                + "1970-01-01 01:00:11 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u2\n"
                + "1970-01-01 01:00:12 [ADD-USER-TO-AUTHORIZATION-GROUP] group: ALL_GROUPS, user: u2\n"
                + "1970-01-01 01:00:13 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, user: u2\n"
                + "1970-01-01 01:00:14 [ADD-SPACE] G2_U3_2\n"
                + "1970-01-01 01:00:15 [ASSIGN-ROLE-TO-USER] user: u3, role: SPACE_ADMIN for G2_U3_2\n"
                + "1970-01-01 01:00:16 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_U3_2\n"
                + "1970-01-01 01:00:17 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u3\n"
                + "1970-01-01 01:00:18 [ADD-USER-TO-AUTHORIZATION-GROUP] group: ALL_GROUPS, user: u3\n"
                + "1970-01-01 01:00:19 [ASSIGN-HOME-SPACE-FOR-USER] user: u2, home space: G2_U2_2\n"
                + "1970-01-01 01:00:20 [ASSIGN-HOME-SPACE-FOR-USER] user: u3, home space: G2_U3_2\n");
        builder = createBuilder();
        builder.groups("G1").commonSpaces(commonSpaces).users(U1);
        builder.groups("G2").commonSpaces(commonSpaces).users(U2, U3);
        builder.space("G1_ALPHA").admin(U1).non(U2, U3);
        builder.space("G1_BETA").admin(U1).non(U2, U3);
        builder.space("G1_GAMMA").admin(U1).non(U2, U3);
        builder.space("G2_ALPHA").non(U1).admin(U2).user(U3);
        builder.space("G2_BETA").non(U1).admin(U2).user(U3);
        builder.space("G2_GAMMA").non(U1).admin(U2).observer(U3);
        builder.space("G2_U2_2").non(U1).admin(U2).non(U3);
        builder.space("G2_U3_2").non(U1).admin(U2).admin(U3);
        builder.homeSpace(U1, "G1_U1");
        builder.homeSpace(U2, "G2_U2_2");
        builder.homeSpace(U3, "G2_U3_2");
        builder.assertExpectations();
    }

    @Test
    public void testChangeNormalUserToAdmin()
    {
        // Given
        // 1. create group G2 with users U1, U2 and U3
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G2"), users(U1, U2, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        createBuilder().groups("G2").space("G2_ALPHA").user(U1, U2, U3).assertExpectations();
        // 2. make U1 admin
        userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G2").admins(U1), users(U1, U2, U3));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, user: u1\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.groups("G2").commonSpaces(commonSpaces).users(U1, U2, U3);
        builder.space("G2_ALPHA").admin(U1).user(U2, U3);
        builder.space("G2_BETA").admin(U1).user(U2, U3);
        builder.space("G2_GAMMA").admin(U1).observer(U2, U3);
        builder.space("G2_U1").admin(U1).non(U2, U3);
        builder.space("G2_U2").admin(U1, U2).non(U3);
        builder.space("G2_U3").admin(U1).non(U2).admin(U3);
        builder.homeSpace(U1, "G2_U1");
        builder.homeSpace(U2, "G2_U2");
        builder.homeSpace(U3, "G2_U3");
        builder.assertExpectations();
    }

    @Test
    public void testChangeAdminUserToNormal()
    {
        // Given
        // 1. create group G2 with users U1 (admin), U2 and U3
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G2").admins(U1), users(U1, U2, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        createBuilder().groups("G2").space("G2_ALPHA").admin(U1).user(U2, U3).assertExpectations();
        // 2. make U1 normal user
        userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G2"), users(U1, U2, U3));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [REMOVE-USER-FROM-AUTHORIZATION-GROUP] group: G2_ADMIN, user: u1\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.groups("G2").commonSpaces(commonSpaces).users(U1, U2, U3);
        builder.space("G2_ALPHA").user(U1, U2, U3);
        builder.space("G2_BETA").user(U1, U2, U3);
        builder.space("G2_GAMMA").observer(U1, U2, U3);
        builder.space("G2_U1").admin(U1).non(U2, U3);
        builder.space("G2_U2").non(U1).admin(U2).non(U3);
        builder.space("G2_U3").non(U1, U2).admin(U3);
        builder.homeSpace(U1, "G2_U1");
        builder.homeSpace(U2, "G2_U2");
        builder.homeSpace(U3, "G2_U3");
        builder.assertExpectations();
    }

    @Test
    public void testRemoveNormalUserFromAGroupAndAddItAgain()
    {
        // Given
        // 1. create group G2 with users U1 (admin), U2 and U3
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G2").admins(U1), users(U1, U2, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        createBuilder().groups("G2").space("G2_GAMMA").admin(U1).observer(U2, U3).assertExpectations();
        // 2. remove U2 from group G2
        userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G2").admins(U1), users(U1, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        createBuilder().groups("G2").usersWithoutAuthentication(U2).space("G2_U2").admin(U1).non(U3).assertExpectations();
        // 3. add U2 again to group G2
        userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G2").admins(U1), users(U1, U2, U3));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [ADD-SPACE] G2_U2_2\n"
                + "1970-01-01 01:00:01 [ASSIGN-ROLE-TO-USER] user: u2, role: SPACE_ADMIN for G2_U2_2\n"
                + "1970-01-01 01:00:02 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_U2_2\n"
                + "1970-01-01 01:00:03 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u2\n"
                + "1970-01-01 01:00:04 [ASSIGN-HOME-SPACE-FOR-USER] user: u2, home space: G2_U2_2\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.groups("G2").commonSpaces(commonSpaces).users(U1, U2, U3);
        builder.space("G2_ALPHA").admin(U1).user(U2, U3);
        builder.space("G2_BETA").admin(U1).user(U2, U3);
        builder.space("G2_GAMMA").admin(U1).observer(U2, U3);
        builder.space("G2_U1").admin(U1).non(U2, U3);
        builder.space("G2_U2").admin(U1).non(U2, U3);
        builder.space("G2_U2_2").admin(U1, U2).non(U3);
        builder.space("G2_U3").admin(U1).non(U2).admin(U3);
        builder.homeSpace(U1, "G2_U1");
        builder.homeSpace(U2, "G2_U2_2");
        builder.homeSpace(U3, "G2_U3");
        builder.assertExpectations();
    }

    @Test
    public void testRemoveAdminUserFromAGroup()
    {
        // Given
        // 1. create group G2 with users U1 (admin), U2 and U3
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G2").admins(U1), users(U1, U2, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        createBuilder().groups("G2").space("G2_U1").admin(U1).non(U2, U3).assertExpectations();
        // 2. remove U1 from group G2
        userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G2").admins(U1), users(U2, U3));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [REMOVE-USER-FROM-AUTHORIZATION-GROUP] group: G2, user: u1\n"
                + "1970-01-01 01:00:01 [REMOVE-USER-FROM-AUTHORIZATION-GROUP] group: G2_ADMIN, user: u1\n"
                + "1970-01-01 01:00:02 [UNASSIGN-ROLE-FORM-USER] user: u1, role: SPACE_ADMIN for G2_U1\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.groups("G2").commonSpaces(commonSpaces).users(U2, U3);
        builder.usersWithoutAuthentication(U1);
        builder.space("G2_ALPHA").user(U2, U3);
        builder.space("G2_BETA").user(U2, U3);
        builder.space("G2_GAMMA").observer(U2, U3);
        builder.space("G2_U1").non(U2, U3);
        builder.space("G2_U2").admin(U2).non(U3);
        builder.space("G2_U3").non(U2).admin(U3);
        builder.homeSpace(U1, "G2_U1");
        builder.homeSpace(U2, "G2_U2");
        builder.homeSpace(U3, "G2_U3");
        builder.assertExpectations();
    }

    @Test
    public void testRemoveAdminUserFromAGroupAndAddItAgain()
    {
        // Given
        // 1. create group G2 with users U1 (admin), U2 and U3
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G2").admins(U1), users(U1, U2, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        // 2. remove U1 from group G2
        userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G2").admins(U1), users(U2, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        createBuilder().groups("G2").usersWithoutAuthentication(U1).space("G2_U1").non(U2, U3).assertExpectations();
        // 3. add U1 again to group G2 as admin
        userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G2").admins(U1), users(U1, U2, U3));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [ADD-SPACE] G2_U1_2\n"
                + "1970-01-01 01:00:01 [ASSIGN-ROLE-TO-USER] user: u1, role: SPACE_ADMIN for G2_U1_2\n"
                + "1970-01-01 01:00:02 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_U1_2\n"
                + "1970-01-01 01:00:03 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u1\n"
                + "1970-01-01 01:00:04 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, user: u1\n"
                + "1970-01-01 01:00:05 [ASSIGN-HOME-SPACE-FOR-USER] user: u1, home space: G2_U1_2\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.groups("G2").commonSpaces(commonSpaces).users(U1, U2, U3);
        builder.space("G2_ALPHA").admin(U1).user(U2, U3);
        builder.space("G2_BETA").admin(U1).user(U2, U3);
        builder.space("G2_GAMMA").admin(U1).observer(U2, U3);
        builder.space("G2_U1").admin(U1).non(U2, U3);
        builder.space("G2_U1_2").admin(U1).non(U2, U3);
        builder.space("G2_U2").admin(U1, U2).non(U3);
        builder.space("G2_U3").admin(U1).non(U2).admin(U3);
        builder.homeSpace(U1, "G2_U1_2");
        builder.homeSpace(U2, "G2_U2");
        builder.homeSpace(U3, "G2_U3");
        builder.assertExpectations();
    }

    @Test
    public void testCreateSecondGroupWithNormalUserInBothGroups()
    {
        // Given
        // 1. create group G1 with users U1 (admin) and U2
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G1").admins(U1), users(U1, U2));
        assertEquals(manage(userManager).getErrorReport(), "");
        createBuilder().groups("G1").space("G1_U2").admin(U1, U2).assertExpectations();
        // 2. create group G2 with users U2 and U3 (admin)
        userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G1").admins(U1), users(U1, U2));
        userManager.addGroup(new UserGroupAsBuilder("G2").admins(U3), users(U2, U3));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [ADD-AUTHORIZATION-GROUP] G2\n"
                + "1970-01-01 01:00:01 [ADD-AUTHORIZATION-GROUP] G2_ADMIN\n"
                + "1970-01-01 01:00:02 [ADD-SPACE] G2_ALPHA\n"
                + "1970-01-01 01:00:03 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2, role: SPACE_USER for G2_ALPHA\n"
                + "1970-01-01 01:00:04 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_ALPHA\n"
                + "1970-01-01 01:00:05 [ADD-SPACE] G2_BETA\n"
                + "1970-01-01 01:00:06 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2, role: SPACE_USER for G2_BETA\n"
                + "1970-01-01 01:00:07 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_BETA\n"
                + "1970-01-01 01:00:08 [ADD-SPACE] G2_GAMMA\n"
                + "1970-01-01 01:00:09 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2, role: SPACE_OBSERVER for G2_GAMMA\n"
                + "1970-01-01 01:00:10 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_GAMMA\n"
                + "1970-01-01 01:00:11 [ADD-SPACE] G2_U2\n"
                + "1970-01-01 01:00:12 [ASSIGN-ROLE-TO-USER] user: u2, role: SPACE_ADMIN for G2_U2\n"
                + "1970-01-01 01:00:13 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_U2\n"
                + "1970-01-01 01:00:14 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u2\n"
                + "1970-01-01 01:00:15 [ADD-SPACE] G2_U3\n"
                + "1970-01-01 01:00:16 [ADD-USER] u3\n"
                + "1970-01-01 01:00:17 [ASSIGN-ROLE-TO-USER] user: u3, role: SPACE_ADMIN for G2_U3\n"
                + "1970-01-01 01:00:18 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_U3\n"
                + "1970-01-01 01:00:19 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u3\n"
                + "1970-01-01 01:00:20 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, user: u3\n"
                + "1970-01-01 01:00:21 [ASSIGN-HOME-SPACE-FOR-USER] user: u2, home space: G2_U2\n"
                + "1970-01-01 01:00:22 [ASSIGN-HOME-SPACE-FOR-USER] user: u3, home space: G2_U3\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.groups("G1", "G2").commonSpaces(commonSpaces).users(U1, U2, U3);
        builder.space("G1_ALPHA").admin(U1).user(U2).non(U3);
        builder.space("G1_BETA").admin(U1).user(U2).non(U3);
        builder.space("G1_GAMMA").admin(U1).observer(U2).non(U3);
        builder.space("G1_U1").admin(U1).non(U2, U3);
        builder.space("G1_U2").admin(U1, U2).non(U3);
        builder.space("G2_ALPHA").non(U1).user(U2).admin(U3);
        builder.space("G2_BETA").non(U1).user(U2).admin(U3);
        builder.space("G2_GAMMA").non(U1).observer(U2).admin(U3);
        builder.space("G2_U2").non(U1).admin(U2, U3);
        builder.space("G2_U3").non(U1).non(U2).admin(U3);
        builder.homeSpace(U1, "G1_U1");
        builder.homeSpace(U2, "G2_U2");
        builder.homeSpace(U3, "G2_U3");
        builder.assertExpectations();
    }

    @Test
    public void testCreateSecondGroupWithAdminUserInBothGroups()
    {
        // Given
        // 1. create group G1 with users U1 (admin) and U2
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G1").admins(U1), users(U1, U2));
        assertEquals(manage(userManager).getErrorReport(), "");
        createBuilder().groups("G1").space("G1_U1").admin(U1).non(U2).assertExpectations();
        // 2. create group G2 with users U1 (admin) and U3
        userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G1").admins(U1), users(U1, U2));
        userManager.addGroup(new UserGroupAsBuilder("G2").admins(U1), users(U1, U3));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [ADD-AUTHORIZATION-GROUP] G2\n"
                + "1970-01-01 01:00:01 [ADD-AUTHORIZATION-GROUP] G2_ADMIN\n"
                + "1970-01-01 01:00:02 [ADD-SPACE] G2_ALPHA\n"
                + "1970-01-01 01:00:03 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2, role: SPACE_USER for G2_ALPHA\n"
                + "1970-01-01 01:00:04 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_ALPHA\n"
                + "1970-01-01 01:00:05 [ADD-SPACE] G2_BETA\n"
                + "1970-01-01 01:00:06 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2, role: SPACE_USER for G2_BETA\n"
                + "1970-01-01 01:00:07 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_BETA\n"
                + "1970-01-01 01:00:08 [ADD-SPACE] G2_GAMMA\n"
                + "1970-01-01 01:00:09 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2, role: SPACE_OBSERVER for G2_GAMMA\n"
                + "1970-01-01 01:00:10 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_GAMMA\n"
                + "1970-01-01 01:00:11 [ADD-SPACE] G2_U1\n"
                + "1970-01-01 01:00:12 [ASSIGN-ROLE-TO-USER] user: u1, role: SPACE_ADMIN for G2_U1\n"
                + "1970-01-01 01:00:13 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_U1\n"
                + "1970-01-01 01:00:14 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u1\n"
                + "1970-01-01 01:00:15 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, user: u1\n"
                + "1970-01-01 01:00:16 [ADD-SPACE] G2_U3\n"
                + "1970-01-01 01:00:17 [ADD-USER] u3\n"
                + "1970-01-01 01:00:18 [ASSIGN-ROLE-TO-USER] user: u3, role: SPACE_ADMIN for G2_U3\n"
                + "1970-01-01 01:00:19 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_U3\n"
                + "1970-01-01 01:00:20 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u3\n"
                + "1970-01-01 01:00:21 [ASSIGN-HOME-SPACE-FOR-USER] user: u1, home space: G2_U1\n"
                + "1970-01-01 01:00:22 [ASSIGN-HOME-SPACE-FOR-USER] user: u3, home space: G2_U3\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.groups("G1", "G2").commonSpaces(commonSpaces).users(U1, U2, U3);
        builder.space("G1_ALPHA").admin(U1).user(U2).non(U3);
        builder.space("G1_BETA").admin(U1).user(U2).non(U3);
        builder.space("G1_GAMMA").admin(U1).observer(U2).non(U3);
        builder.space("G1_U1").admin(U1).non(U2, U3);
        builder.space("G1_U2").admin(U1, U2).non(U3);
        builder.space("G2_ALPHA").admin(U1).non(U2).user(U3);
        builder.space("G2_BETA").admin(U1).non(U2).user(U3);
        builder.space("G2_GAMMA").admin(U1).non(U2).observer(U3);
        builder.space("G2_U1").admin(U1).non(U2, U3);
        builder.space("G2_U3").admin(U1).non(U2).admin(U3);
        builder.homeSpace(U1, "G2_U1");
        builder.homeSpace(U2, "G1_U2");
        builder.homeSpace(U3, "G2_U3");
        builder.assertExpectations();
    }

    @Test
    public void testCreateSecondGroupWithUserNormalInFirstGroupAndAdminInSecondGroup()
    {
        // Given
        // 1. create group G1 with users U1 (admin) and U2
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G1").admins(U1), users(U1, U2));
        assertEquals(manage(userManager).getErrorReport(), "");
        createBuilder().groups("G1").space("G1_U1").admin(U1).non(U2).assertExpectations();
        // 2. create group G2 with users U2 (admin) and U3
        userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G1").admins(U1), users(U1, U2));
        userManager.addGroup(new UserGroupAsBuilder("G2").admins(U2), users(U2, U3));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [ADD-AUTHORIZATION-GROUP] G2\n"
                + "1970-01-01 01:00:01 [ADD-AUTHORIZATION-GROUP] G2_ADMIN\n"
                + "1970-01-01 01:00:02 [ADD-SPACE] G2_ALPHA\n"
                + "1970-01-01 01:00:03 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2, role: SPACE_USER for G2_ALPHA\n"
                + "1970-01-01 01:00:04 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_ALPHA\n"
                + "1970-01-01 01:00:05 [ADD-SPACE] G2_BETA\n"
                + "1970-01-01 01:00:06 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2, role: SPACE_USER for G2_BETA\n"
                + "1970-01-01 01:00:07 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_BETA\n"
                + "1970-01-01 01:00:08 [ADD-SPACE] G2_GAMMA\n"
                + "1970-01-01 01:00:09 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2, role: SPACE_OBSERVER for G2_GAMMA\n"
                + "1970-01-01 01:00:10 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_GAMMA\n"
                + "1970-01-01 01:00:11 [ADD-SPACE] G2_U2\n"
                + "1970-01-01 01:00:12 [ASSIGN-ROLE-TO-USER] user: u2, role: SPACE_ADMIN for G2_U2\n"
                + "1970-01-01 01:00:13 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_U2\n"
                + "1970-01-01 01:00:14 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u2\n"
                + "1970-01-01 01:00:15 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, user: u2\n"
                + "1970-01-01 01:00:16 [ADD-SPACE] G2_U3\n"
                + "1970-01-01 01:00:17 [ADD-USER] u3\n"
                + "1970-01-01 01:00:18 [ASSIGN-ROLE-TO-USER] user: u3, role: SPACE_ADMIN for G2_U3\n"
                + "1970-01-01 01:00:19 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_U3\n"
                + "1970-01-01 01:00:20 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u3\n"
                + "1970-01-01 01:00:21 [ASSIGN-HOME-SPACE-FOR-USER] user: u2, home space: G2_U2\n"
                + "1970-01-01 01:00:22 [ASSIGN-HOME-SPACE-FOR-USER] user: u3, home space: G2_U3\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.groups("G1", "G2").commonSpaces(commonSpaces).users(U1, U2, U3);
        builder.space("G1_ALPHA").admin(U1).user(U2).non(U3);
        builder.space("G1_BETA").admin(U1).user(U2).non(U3);
        builder.space("G1_GAMMA").admin(U1).observer(U2).non(U3);
        builder.space("G1_U1").admin(U1).non(U2, U3);
        builder.space("G1_U2").admin(U1, U2).non(U3);
        builder.space("G2_ALPHA").non(U1).admin(U2).user(U3);
        builder.space("G2_BETA").non(U1).admin(U2).user(U3);
        builder.space("G2_GAMMA").non(U1).admin(U2).observer(U3);
        builder.space("G2_U2").non(U1).admin(U2).non(U3);
        builder.space("G2_U3").non(U1).admin(U2).admin(U3);
        builder.homeSpace(U1, "G1_U1");
        builder.homeSpace(U2, "G2_U2");
        builder.homeSpace(U3, "G2_U3");
        builder.assertExpectations();
    }

    @Test
    public void testCreateTwoGroupsAndMoveUserFromFirstGroupToSecondGroup()
    {
        // Given
        // 1. create group G1 with users U1 (admin) and U2
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G1").admins(U1), users(U1, U2));
        assertEquals(manage(userManager).getErrorReport(), "");
        // 2. create group G2 with users U3 (admin) and U4
        userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G1").admins(U1), users(U1, U2));
        userManager.addGroup(new UserGroupAsBuilder("G2").admins(U3), users(U3, U4));
        assertEquals(manage(userManager).getErrorReport(), "");
        // 3. Move U2 from G1 -> G2
        userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G1").admins(U1), users(U1));
        userManager.addGroup(new UserGroupAsBuilder("G2").admins(U3), users(U2, U3, U4));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [REMOVE-USER-FROM-AUTHORIZATION-GROUP] group: G1, user: u2\n"
                + "1970-01-01 01:00:01 [UNASSIGN-ROLE-FORM-USER] user: u2, role: SPACE_ADMIN for G1_U2\n"
                + "1970-01-01 01:00:02 [ADD-SPACE] G2_U2\n"
                + "1970-01-01 01:00:03 [ASSIGN-ROLE-TO-USER] user: u2, role: SPACE_ADMIN for G2_U2\n"
                + "1970-01-01 01:00:04 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_U2\n"
                + "1970-01-01 01:00:05 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u2\n"
                + "1970-01-01 01:00:06 [ASSIGN-HOME-SPACE-FOR-USER] user: u2, home space: G2_U2\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.groups("G1", "G2").commonSpaces(commonSpaces).users(U1, U2, U3, U4);
        builder.space("G1_ALPHA").admin(U1).non(U2, U3, U4);
        builder.space("G1_BETA").admin(U1).non(U2, U3, U4);
        builder.space("G1_GAMMA").admin(U1).non(U2, U3, U4);
        builder.space("G1_U1").admin(U1).non(U2, U3, U4);
        builder.space("G1_U2").admin(U1).non(U2, U3, U4);
        builder.space("G2_ALPHA").non(U1).user(U2).admin(U3).user(U4);
        builder.space("G2_BETA").non(U1).user(U2).admin(U3).user(U4);
        builder.space("G2_GAMMA").non(U1).observer(U2).admin(U3).observer(U4);
        builder.space("G2_U2").non(U1).admin(U2).admin(U3).non(U4);
        builder.space("G2_U3").non(U1).non(U2).admin(U3).non(U4);
        builder.space("G2_U4").non(U1).non(U2).admin(U3).admin(U4);
        builder.homeSpace(U1, "G1_U1");
        builder.homeSpace(U2, "G2_U2");
        builder.homeSpace(U3, "G2_U3");
        builder.homeSpace(U4, "G2_U4");
        builder.assertExpectations();
    }

    @Test
    public void testCreateTwoGroupsAndMoveUserFromSecondGroupToFirstGroup()
    {
        // Given
        // 1. create group G1 with users U1 (admin) and U2
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G1").admins(U1), users(U1, U2));
        assertEquals(manage(userManager).getErrorReport(), "");
        // 2. create group G2 with users U3 (admin) and U4
        userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G1").admins(U1), users(U1, U2));
        userManager.addGroup(new UserGroupAsBuilder("G2").admins(U3), users(U3, U4));
        assertEquals(manage(userManager).getErrorReport(), "");
        // 3. Move U4 from G2 -> G1
        userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G1").admins(U1), users(U1, U2, U4));
        userManager.addGroup(new UserGroupAsBuilder("G2").admins(U3), users(U3));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [ADD-SPACE] G1_U4\n"
                + "1970-01-01 01:00:01 [ASSIGN-ROLE-TO-USER] user: u4, role: SPACE_ADMIN for G1_U4\n"
                + "1970-01-01 01:00:02 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_U4\n"
                + "1970-01-01 01:00:03 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G1, user: u4\n"
                + "1970-01-01 01:00:04 [REMOVE-USER-FROM-AUTHORIZATION-GROUP] group: G2, user: u4\n"
                + "1970-01-01 01:00:05 [UNASSIGN-ROLE-FORM-USER] user: u4, role: SPACE_ADMIN for G2_U4\n"
                + "1970-01-01 01:00:06 [ASSIGN-HOME-SPACE-FOR-USER] user: u4, home space: G1_U4\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.groups("G1", "G2").commonSpaces(commonSpaces).users(U1, U2, U3, U4);
        builder.space("G1_ALPHA").admin(U1).user(U2).non(U3).user(U4);
        builder.space("G1_BETA").admin(U1).user(U2).non(U3).user(U4);
        builder.space("G1_GAMMA").admin(U1).observer(U2).non(U3).observer(U4);
        builder.space("G1_U1").admin(U1).non(U2).non(U3).non(U4);
        builder.space("G1_U2").admin(U1).admin(U2).non(U3).non(U4);
        builder.space("G1_U4").admin(U1).non(U2).non(U3).admin(U4);
        builder.space("G2_ALPHA").non(U1).non(U2).admin(U3).non(U4);
        builder.space("G2_BETA").non(U1).non(U2).admin(U3).non(U4);
        builder.space("G2_GAMMA").non(U1).non(U2).admin(U3).non(U4);
        builder.space("G2_U3").non(U1).non(U2).admin(U3).non(U4);
        builder.space("G2_U4").non(U1).non(U2).admin(U3).non(U4);
        builder.homeSpace(U1, "G1_U1");
        builder.homeSpace(U2, "G1_U2");
        builder.homeSpace(U3, "G2_U3");
        builder.homeSpace(U4, "G1_U4");
        builder.assertExpectations();
    }

    @Test
    public void testCreateTwoGroupsAndMoveUserWithTestSpaceAsHomeSpaceFromSecondGroupToFirstGroup()
    {
        // Given
        // 1. create user U4 with home space TEST-SPACE and SPACE_ADMIN on this space
        createUserForTestSpace(U4);
        // 2. create group G1 with users U1 (admin) and U2
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G1").admins(U1), users(U1, U2));
        assertEquals(manage(userManager).getErrorReport(), "");
        // 3. create group G2 with users U3 (admin) and U4
        userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G1").admins(U1), users(U1, U2));
        userManager.addGroup(new UserGroupAsBuilder("G2").admins(U3), users(U3, U4));
        assertEquals(manage(userManager).getErrorReport(), "");
        // 4. Move U4 from G2 -> G1
        userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G1").admins(U1), users(U1, U2, U4));
        userManager.addGroup(new UserGroupAsBuilder("G2").admins(U3), users(U3));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [ADD-SPACE] G1_U4\n"
                + "1970-01-01 01:00:01 [ASSIGN-ROLE-TO-USER] user: u4, role: SPACE_ADMIN for G1_U4\n"
                + "1970-01-01 01:00:02 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_U4\n"
                + "1970-01-01 01:00:03 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G1, user: u4\n"
                + "1970-01-01 01:00:04 [REMOVE-USER-FROM-AUTHORIZATION-GROUP] group: G2, user: u4\n"
                + "1970-01-01 01:00:05 [UNASSIGN-ROLE-FORM-USER] user: u4, role: SPACE_ADMIN for G2_U4\n"
                + "1970-01-01 01:00:06 [ASSIGN-HOME-SPACE-FOR-USER] user: u4, home space: G1_U4\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.groups("G1", "G2").commonSpaces(commonSpaces).users(U1, U2, U3, U4);
        builder.space("G1_ALPHA").admin(U1).user(U2).non(U3).user(U4);
        builder.space("G1_BETA").admin(U1).user(U2).non(U3).user(U4);
        builder.space("G1_GAMMA").admin(U1).observer(U2).non(U3).observer(U4);
        builder.space("G1_U1").admin(U1).non(U2).non(U3).non(U4);
        builder.space("G1_U2").admin(U1).admin(U2).non(U3).non(U4);
        builder.space("G1_U4").admin(U1).non(U2).non(U3).admin(U4);
        builder.space("G2_ALPHA").non(U1).non(U2).admin(U3).non(U4);
        builder.space("G2_BETA").non(U1).non(U2).admin(U3).non(U4);
        builder.space("G2_GAMMA").non(U1).non(U2).admin(U3).non(U4);
        builder.space("G2_U3").non(U1).non(U2).admin(U3).non(U4);
        builder.space("G2_U4").non(U1).non(U2).admin(U3).non(U4);
        builder.homeSpace(U1, "G1_U1");
        builder.homeSpace(U2, "G1_U2");
        builder.homeSpace(U3, "G2_U3");
        builder.homeSpace(U4, "G1_U4");
        builder.assertExpectations();
    }

    @Test
    public void testUserFromAGroupHasLefted()
    {
        // Given
        // 1. create group G2 with users U1 (admin), U2 and U3
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G2").admins(U1), users(U1, U2, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        // 2. U2 is no longer known by the authentication service
        userManager = new UserManagerBuilder(v3api, logger, report()).unknownUser(U2).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G2").admins(U1), users(U1, U3));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [DEACTIVATE-USER] u2\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.groups("G2").commonSpaces(commonSpaces).users(U1, U3);
        builder.space("G2_ALPHA").admin(U1).user(U3);
        builder.space("G2_BETA").admin(U1).user(U3);
        builder.space("G2_GAMMA").admin(U1).observer(U3);
        builder.space("G2_U1").admin(U1).non(U3);
        builder.space("G2_U2").admin(U1).non(U3);
        builder.space("G2_U3").admin(U1).admin(U3);
        builder.homeSpace(U1, "G2_U1");
        builder.homeSpace(U2, "G2_U2");
        builder.unknownUser(U2);
        builder.homeSpace(U3, "G2_U3");
        builder.assertExpectations();
    }

    @Test
    public void testUserFromAGroupHasLeftedNoDeactivation()
    {
        // Given
        // 1. create group G2 with users U1 (admin), U2 and U3
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G2").admins(U1), users(U1, U2, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        // 2. U2 is no longer known
        userManager = new UserManagerBuilder(v3api, logger, report()).unknownUser(U2).commonSpaces(commonSpaces).noDeactivation().get();
        userManager.addGroup(new UserGroupAsBuilder("G2").admins(U1), users(U1, U3));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [REMOVE-USER-FROM-AUTHORIZATION-GROUP] group: G2, user: u2\n"
                + "1970-01-01 01:00:01 [UNASSIGN-ROLE-FORM-USER] user: u2, role: SPACE_ADMIN for G2_U2\n");
        UserManagerExpectationsBuilder builder = createBuilder().noDeactivation();
        builder.groups("G2").commonSpaces(commonSpaces).users(U1, U3);
        builder.space("G2_ALPHA").admin(U1).user(U3);
        builder.space("G2_BETA").admin(U1).user(U3);
        builder.space("G2_GAMMA").admin(U1).observer(U3);
        builder.space("G2_U1").admin(U1).non(U3);
        builder.space("G2_U2").admin(U1).non(U3);
        builder.space("G2_U3").admin(U1).admin(U3);
        builder.homeSpace(U1, "G2_U1");
        builder.homeSpace(U2, "G2_U2");
        builder.homeSpace(U3, "G2_U3");
        builder.assertExpectations();
    }

    @Test
    public void testReuseSameUserId()
    {
        // Given
        // 1. create group G2 with users U1 (admin), U2 and U3
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G2").admins(U1), users(U1, U2, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        // 2. U2 is no longer known by the authentication service
        userManager = new UserManagerBuilder(v3api, logger, report()).unknownUser(U2).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G2").admins(U1), users(U1, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        // 3. U2 is reused and added to group G2
        userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G2").admins(U1), users(U1, U2, U3));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [ADD-SPACE] G2_U2_2\n"
                + "1970-01-01 01:00:01 [REUSE-USER] u2\n"
                + "1970-01-01 01:00:02 [ASSIGN-ROLE-TO-USER] user: u2, role: SPACE_ADMIN for G2_U2_2\n"
                + "1970-01-01 01:00:03 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_U2_2\n"
                + "1970-01-01 01:00:04 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u2\n"
                + "1970-01-01 01:00:05 [ASSIGN-HOME-SPACE-FOR-USER] user: u2, home space: G2_U2_2\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.groups("G2").commonSpaces(commonSpaces).users(U1, U2, U3);
        builder.space("G2_ALPHA").admin(U1).user(U2).user(U3);
        builder.space("G2_BETA").admin(U1).user(U2).user(U3);
        builder.space("G2_GAMMA").admin(U1).observer(U2).observer(U3);
        builder.space("G2_U1").admin(U1).non(U2).non(U3);
        builder.space("G2_U2").admin(U1).non(U2).non(U3);
        builder.space("G2_U2_2").admin(U1).admin(U2).non(U3);
        builder.space("G2_U3").admin(U1).non(U2).admin(U3);
        builder.homeSpace(U1, "G2_U1");
        builder.homeSpace(U2, "G2_U2_2");
        builder.homeSpace(U3, "G2_U3");
        builder.assertExpectations();
    }

    @Test
    public void testReuseSameUserIdTwice()
    {
        // Given
        // 1. create group G2 with users U1 (admin), U2 and U3
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G2").admins(U1), users(U1, U2, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        // 2. U2 is no longer known by the authentication service
        userManager = new UserManagerBuilder(v3api, logger, report()).unknownUser(U2).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G2").admins(U1), users(U1, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        // 3. U2 is reused and added to group G2
        userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G2").admins(U1), users(U1, U2, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        // 4. U2 is no longer known by the authentication service
        userManager = new UserManagerBuilder(v3api, logger, report()).unknownUser(U2).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G2").admins(U1), users(U1, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        // 5. U2 is reused and added to group G2
        userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G2").admins(U1), users(U1, U2, U3));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [ADD-SPACE] G2_U2_3\n"
                + "1970-01-01 01:00:01 [REUSE-USER] u2\n"
                + "1970-01-01 01:00:02 [ASSIGN-ROLE-TO-USER] user: u2, role: SPACE_ADMIN for G2_U2_3\n"
                + "1970-01-01 01:00:03 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_U2_3\n"
                + "1970-01-01 01:00:04 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u2\n"
                + "1970-01-01 01:00:05 [ASSIGN-HOME-SPACE-FOR-USER] user: u2, home space: G2_U2_3\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.groups("G2").commonSpaces(commonSpaces).users(U1, U2, U3);
        builder.space("G2_ALPHA").admin(U1).user(U2).user(U3);
        builder.space("G2_BETA").admin(U1).user(U2).user(U3);
        builder.space("G2_GAMMA").admin(U1).observer(U2).observer(U3);
        builder.space("G2_U1").admin(U1).non(U2).non(U3);
        builder.space("G2_U2").admin(U1).non(U2).non(U3);
        builder.space("G2_U2_2").admin(U1).non(U2).non(U3);
        builder.space("G2_U2_3").admin(U1).admin(U2).non(U3);
        builder.space("G2_U3").admin(U1).non(U2).admin(U3);
        builder.homeSpace(U1, "G2_U1");
        builder.homeSpace(U2, "G2_U2_3");
        builder.homeSpace(U3, "G2_U3");
        builder.assertExpectations();
    }

    @Test
    public void testUserSpaceRole()
    {
        // Given
        // create group G1 with users U1 (admin), U2 and U3 and user space role OBSERVER
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G1").userSpaceRole(Role.OBSERVER).admins(U1), users(U1, U2, U3));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [ADD-AUTHORIZATION-GROUP] G1\n"
                + "1970-01-01 01:00:01 [ADD-AUTHORIZATION-GROUP] G1_ADMIN\n"
                + "1970-01-01 01:00:02 [ADD-SPACE] G1_ALPHA\n"
                + "1970-01-01 01:00:03 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1, role: SPACE_USER for G1_ALPHA\n"
                + "1970-01-01 01:00:04 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_ALPHA\n"
                + "1970-01-01 01:00:05 [ADD-SPACE] G1_BETA\n"
                + "1970-01-01 01:00:06 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1, role: SPACE_USER for G1_BETA\n"
                + "1970-01-01 01:00:07 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_BETA\n"
                + "1970-01-01 01:00:08 [ADD-SPACE] G1_GAMMA\n"
                + "1970-01-01 01:00:09 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1, role: SPACE_OBSERVER for G1_GAMMA\n"
                + "1970-01-01 01:00:10 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_GAMMA\n"
                + "1970-01-01 01:00:11 [ADD-SPACE] G1_U1\n"
                + "1970-01-01 01:00:12 [ADD-USER] u1\n"
                + "1970-01-01 01:00:13 [ASSIGN-ROLE-TO-USER] user: u1, role: SPACE_ADMIN for G1_U1\n"
                + "1970-01-01 01:00:14 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_U1\n"
                + "1970-01-01 01:00:15 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1, role: SPACE_OBSERVER for G1_U1\n"
                + "1970-01-01 01:00:16 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G1, user: u1\n"
                + "1970-01-01 01:00:17 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, user: u1\n"
                + "1970-01-01 01:00:18 [ADD-SPACE] G1_U2\n"
                + "1970-01-01 01:00:19 [ADD-USER] u2\n"
                + "1970-01-01 01:00:20 [ASSIGN-ROLE-TO-USER] user: u2, role: SPACE_ADMIN for G1_U2\n"
                + "1970-01-01 01:00:21 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_U2\n"
                + "1970-01-01 01:00:22 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1, role: SPACE_OBSERVER for G1_U2\n"
                + "1970-01-01 01:00:23 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G1, user: u2\n"
                + "1970-01-01 01:00:24 [ADD-SPACE] G1_U3\n"
                + "1970-01-01 01:00:25 [ADD-USER] u3\n"
                + "1970-01-01 01:00:26 [ASSIGN-ROLE-TO-USER] user: u3, role: SPACE_ADMIN for G1_U3\n"
                + "1970-01-01 01:00:27 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_U3\n"
                + "1970-01-01 01:00:28 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1, role: SPACE_OBSERVER for G1_U3\n"
                + "1970-01-01 01:00:29 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G1, user: u3\n"
                + "1970-01-01 01:00:30 [ASSIGN-HOME-SPACE-FOR-USER] user: u1, home space: G1_U1\n"
                + "1970-01-01 01:00:31 [ASSIGN-HOME-SPACE-FOR-USER] user: u2, home space: G1_U2\n"
                + "1970-01-01 01:00:32 [ASSIGN-HOME-SPACE-FOR-USER] user: u3, home space: G1_U3\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.groups("G1").commonSpaces(commonSpaces).users(U1, U2, U3);
        builder.space("G1_ALPHA").admin(U1).user(U2).user(U3);
        builder.space("G1_BETA").admin(U1).user(U2).user(U3);
        builder.space("G1_GAMMA").admin(U1).observer(U2).observer(U3);
        builder.space("G1_U1").admin(U1).observer(U2).observer(U3);
        builder.space("G1_U2").admin(U1).admin(U2).observer(U3);
        builder.space("G1_U3").admin(U1).observer(U2).admin(U3);
        builder.homeSpace(U1, "G1_U1");
        builder.homeSpace(U2, "G1_U2");
        builder.homeSpace(U3, "G1_U3");
        builder.assertExpectations();
    }

    @Test
    public void testUserSpaceRoleAddAndRemoveUser()
    {
        // Given
        // 1. create group G1 with users U1 (admin) and U2 and user space role OBSERVER
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G1").userSpaceRole(Role.OBSERVER).admins(U1), users(U1, U2));
        assertEquals(manage(userManager).getErrorReport(), "");
        // 2. remove U2 and add U3 as normal user to group G1
        userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G1").userSpaceRole(Role.OBSERVER).admins(U1), users(U1, U3));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [ADD-SPACE] G1_U3\n"
                + "1970-01-01 01:00:01 [ADD-USER] u3\n"
                + "1970-01-01 01:00:02 [ASSIGN-ROLE-TO-USER] user: u3, role: SPACE_ADMIN for G1_U3\n"
                + "1970-01-01 01:00:03 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_U3\n"
                + "1970-01-01 01:00:04 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1, role: SPACE_OBSERVER for G1_U3\n"
                + "1970-01-01 01:00:05 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G1, user: u3\n"
                + "1970-01-01 01:00:06 [REMOVE-USER-FROM-AUTHORIZATION-GROUP] group: G1, user: u2\n"
                + "1970-01-01 01:00:07 [UNASSIGN-ROLE-FORM-USER] user: u2, role: SPACE_ADMIN for G1_U2\n"
                + "1970-01-01 01:00:08 [ASSIGN-HOME-SPACE-FOR-USER] user: u3, home space: G1_U3\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.usersWithoutAuthentication(U2);
        builder.groups("G1").commonSpaces(commonSpaces).users(U1, U3);
        builder.space("G1_ALPHA").admin(U1).user(U3);
        builder.space("G1_BETA").admin(U1).user(U3);
        builder.space("G1_GAMMA").admin(U1).observer(U3);
        builder.space("G1_U1").admin(U1).observer(U3);
        builder.space("G1_U2").admin(U1).observer(U3);
        builder.space("G1_U3").admin(U1).admin(U3);
        builder.homeSpace(U1, "G1_U1");
        builder.homeSpace(U2, "G1_U2");
        builder.homeSpace(U3, "G1_U3");
        builder.assertExpectations();
    }

    @Test
    public void testChangeUserSpaceRole()
    {
        // Given
        // 1. create group G1 with users U1 (admin) and U2 and user space role OBSERVER
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G1").userSpaceRole(Role.OBSERVER).admins(U1), users(U1, U2));
        assertEquals(manage(userManager).getErrorReport(), "");
        // 2. change user space role to USER
        userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G1").userSpaceRole(Role.USER).admins(U1), users(U1, U2));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [UNASSIGN-ROLE-FORM-AUTHORIZATION-GROUP] group: G1, role: SPACE_OBSERVER for G1_U1\n"
                + "1970-01-01 01:00:01 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1, role: SPACE_USER for G1_U1\n"
                + "1970-01-01 01:00:02 [UNASSIGN-ROLE-FORM-AUTHORIZATION-GROUP] group: G1, role: SPACE_OBSERVER for G1_U2\n"
                + "1970-01-01 01:00:03 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1, role: SPACE_USER for G1_U2\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.groups("G1").commonSpaces(commonSpaces).users(U1, U2);
        builder.space("G1_ALPHA").admin(U1).user(U2);
        builder.space("G1_BETA").admin(U1).user(U2);
        builder.space("G1_GAMMA").admin(U1).observer(U2);
        builder.space("G1_U1").admin(U1).user(U2);
        builder.space("G1_U2").admin(U1).admin(U2);
        builder.homeSpace(U1, "G1_U1");
        builder.homeSpace(U2, "G1_U2");
        builder.assertExpectations();
    }

    @Test
    public void testRemoveUserSpaceRole()
    {
        // Given
        // 1. create group G1 with users U1 (admin) and U2 and user space role OBSERVER
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G1").userSpaceRole(Role.OBSERVER).admins(U1), users(U1, U2));
        assertEquals(manage(userManager).getErrorReport(), "");
        // 2. remove user space role
        userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G1").admins(U1), users(U1, U2));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [UNASSIGN-ROLE-FORM-AUTHORIZATION-GROUP] group: G1, role: SPACE_OBSERVER for G1_U1\n"
                + "1970-01-01 01:00:01 [UNASSIGN-ROLE-FORM-AUTHORIZATION-GROUP] group: G1, role: SPACE_OBSERVER for G1_U2\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.groups("G1").commonSpaces(commonSpaces).users(U1, U2);
        builder.space("G1_ALPHA").admin(U1).user(U2);
        builder.space("G1_BETA").admin(U1).user(U2);
        builder.space("G1_GAMMA").admin(U1).observer(U2);
        builder.space("G1_U1").admin(U1).non(U2);
        builder.space("G1_U2").admin(U1).admin(U2);
        builder.homeSpace(U1, "G1_U1");
        builder.homeSpace(U2, "G1_U2");
        builder.assertExpectations();
    }

    @Test
    public void testSetUserSpaceRole()
    {
        // Given
        // 1. create group G1 with users U1 (admin) and U2 and no user space role
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G1").admins(U1), users(U1, U2));
        assertEquals(manage(userManager).getErrorReport(), "");
        // 2. set user space role OBSERVER
        userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G1").userSpaceRole(Role.OBSERVER).admins(U1), users(U1, U2));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1, role: SPACE_OBSERVER for G1_U1\n"
                + "1970-01-01 01:00:01 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1, role: SPACE_OBSERVER for G1_U2\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.groups("G1").commonSpaces(commonSpaces).users(U1, U2);
        builder.space("G1_ALPHA").admin(U1).user(U2);
        builder.space("G1_BETA").admin(U1).user(U2);
        builder.space("G1_GAMMA").admin(U1).observer(U2);
        builder.space("G1_U1").admin(U1).observer(U2);
        builder.space("G1_U2").admin(U1).admin(U2);
        builder.homeSpace(U1, "G1_U1");
        builder.homeSpace(U2, "G1_U2");
        builder.assertExpectations();
    }

    @Test
    public void testNoCreatedSpacesUserSpaceRole()
    {
        // Given
        // Create group G1 with users U1 (admin) and U2, no 'create user space' and user space role USER
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G1").userSpaceRole(Role.OBSERVER)
                .createUserSpace(false).admins(U1), users(U1, U2));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [ADD-AUTHORIZATION-GROUP] G1\n"
                + "1970-01-01 01:00:01 [ADD-AUTHORIZATION-GROUP] G1_ADMIN\n"
                + "1970-01-01 01:00:02 [ADD-SPACE] G1_ALPHA\n"
                + "1970-01-01 01:00:03 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1, role: SPACE_USER for G1_ALPHA\n"
                + "1970-01-01 01:00:04 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_ALPHA\n"
                + "1970-01-01 01:00:05 [ADD-SPACE] G1_BETA\n"
                + "1970-01-01 01:00:06 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1, role: SPACE_USER for G1_BETA\n"
                + "1970-01-01 01:00:07 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_BETA\n"
                + "1970-01-01 01:00:08 [ADD-SPACE] G1_GAMMA\n"
                + "1970-01-01 01:00:09 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1, role: SPACE_OBSERVER for G1_GAMMA\n"
                + "1970-01-01 01:00:10 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_GAMMA\n"
                + "1970-01-01 01:00:11 [ADD-USER] u1\n"
                + "1970-01-01 01:00:12 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G1, user: u1\n"
                + "1970-01-01 01:00:13 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, user: u1\n"
                + "1970-01-01 01:00:14 [ADD-USER] u2\n"
                + "1970-01-01 01:00:15 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G1, user: u2\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.groups("G1").commonSpaces(commonSpaces).users(U1, U2);
        builder.space("G1_ALPHA").admin(U1).user(U2);
        builder.space("G1_BETA").admin(U1).user(U2);
        builder.space("G1_GAMMA").admin(U1).observer(U2);
        builder.homeSpace(U1, null);
        builder.homeSpace(U2, null);
        builder.assertExpectations();
    }

    @Test
    public void testChangeUserSpaceRoleAndChangeUsers()
    {
        // Given
        // 1. create user U2 with home space TEST-SPACE and SPACE_ADMIN on this space
        createUserForTestSpace(U2);
        // 2. create group G1 with users U1 (admin), U2 and U3 and user space role USER
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G1").userSpaceRole(Role.USER).admins(U1), users(U1, U2, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        // 3. change user space role to OBSERVER, remove user U2 and add user U4
        userManager = new UserManagerBuilder(v3api, logger, report()).commonSpaces(commonSpaces).get();
        userManager.addGroup(new UserGroupAsBuilder("G1").userSpaceRole(Role.OBSERVER).admins(U1), users(U1, U3, U4));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [ADD-SPACE] G1_U4\n"
                + "1970-01-01 01:00:01 [ADD-USER] u4\n"
                + "1970-01-01 01:00:02 [ASSIGN-ROLE-TO-USER] user: u4, role: SPACE_ADMIN for G1_U4\n"
                + "1970-01-01 01:00:03 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_U4\n"
                + "1970-01-01 01:00:04 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1, role: SPACE_OBSERVER for G1_U4\n"
                + "1970-01-01 01:00:05 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G1, user: u4\n"
                + "1970-01-01 01:00:06 [REMOVE-USER-FROM-AUTHORIZATION-GROUP] group: G1, user: u2\n"
                + "1970-01-01 01:00:07 [UNASSIGN-ROLE-FORM-USER] user: u2, role: SPACE_ADMIN for G1_U2\n"
                + "1970-01-01 01:00:08 [UNASSIGN-ROLE-FORM-AUTHORIZATION-GROUP] group: G1, role: SPACE_USER for G1_U1\n"
                + "1970-01-01 01:00:09 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1, role: SPACE_OBSERVER for G1_U1\n"
                + "1970-01-01 01:00:10 [UNASSIGN-ROLE-FORM-AUTHORIZATION-GROUP] group: G1, role: SPACE_USER for G1_U2\n"
                + "1970-01-01 01:00:11 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1, role: SPACE_OBSERVER for G1_U2\n"
                + "1970-01-01 01:00:12 [UNASSIGN-ROLE-FORM-AUTHORIZATION-GROUP] group: G1, role: SPACE_USER for G1_U3\n"
                + "1970-01-01 01:00:13 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1, role: SPACE_OBSERVER for G1_U3\n"
                + "1970-01-01 01:00:14 [ASSIGN-HOME-SPACE-FOR-USER] user: u4, home space: G1_U4\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.groups("G1").commonSpaces(commonSpaces).users(U1, U3, U4);
        builder.space("G1_ALPHA").admin(U1).non(U2).user(U3, U4);
        builder.space("G1_BETA").admin(U1).non(U2).user(U3, U4);
        builder.space("G1_GAMMA").admin(U1).non(U2).observer(U3, U4);
        builder.space("G1_U1").admin(U1).non(U2).observer(U3).observer(U4);
        builder.space("G1_U2").admin(U1).non(U2).observer(U3).observer(U4);
        builder.space("G1_U3").admin(U1).non(U2).admin(U3).observer(U4);
        builder.space("G1_U4").admin(U1).non(U2).observer(U3).admin(U4);
        builder.space("TEST-SPACE").non(U1, U3, U4).admin(U2);
        builder.homeSpace(U1, "G1_U1");
        builder.homeSpace(U2, "G1_U2");
        builder.homeSpace(U3, "G1_U3");
        builder.homeSpace(U4, "G1_U4");
        builder.assertExpectations();
    }

    private UserManagerExpectationsBuilder createBuilder()
    {
        return new UserManagerExpectationsBuilder(v3api, testService, sessionManager);
    }

    private UserManagerReport manage(UserManager userManager, String... knownUsers)
    {
        return manage(userManager, Collections.emptySet(), knownUsers);
    }

    private UserManagerReport manage(UserManager userManager, Set<String> usersToBeIgnored, String... knownUsers)
    {
        userManager.manage(new TreeSet<>(Arrays.asList(knownUsers)), usersToBeIgnored);
        daoFactory.getSessionFactory().getCurrentSession().flush();
        return report;
    }
    
    private Map<String, Principal> users(Principal... principals)
    {
        Map<String, Principal> map = new TreeMap<>();
        for (Principal principal : principals)
        {
            map.put(principal.getUserId(), principal);
        }
        return map;
    }

    private UserManagerReport report()
    {
        report = new UserManagerReport(new MockTimeProvider(0, 1000));
        return report;
    }

    private static class UserGroupAsBuilder extends UserGroup
    {
        UserGroupAsBuilder(String groupKey)
        {
            setKey(groupKey);
        }

        UserGroupAsBuilder admins(String... admins)
        {
            setAdmins(Arrays.asList(admins));
            return this;
        }

        UserGroupAsBuilder admins(Principal... admins)
        {
            setAdmins(UserManagerExpectationsBuilder.getUserIds(isUseEmailAsUserId(), admins));
            return this;
        }

        UserGroupAsBuilder disable()
        {
            setEnabled(false);
            return this;
        }

        UserGroupAsBuilder createUserSpace(boolean createUserSpace)
        {
            setCreateUserSpace(createUserSpace);
            return this;
        }

        UserGroupAsBuilder useEmailAsUserId(boolean useEmailAsUserId)
        {
            setUseEmailAsUserId(useEmailAsUserId);
            return this;
        }

        UserGroupAsBuilder userSpaceRole(Role role)
        {
            setUserSpaceRole(role.name());
            return this;
        }
    }

    private static class UserManagerBuilder
    {
        private IApplicationServerInternalApi service;

        private ISimpleLogger logger;

        private UserManagerReport report;

        private Set<String> usersUnknownByAuthenticationService = new TreeSet<>();

        private List<String> globalSpaces = new ArrayList<>();

        private List<String> instanceAdmins;

        private Map<Role, List<String>> commonSpacesByRole = new TreeMap<>();

        private Map<String, String> commonSamples = new TreeMap<>();

        private List<Map<String, String>> commonExperiments = new ArrayList<>();

        private File shareIdsMappingFile;

        private boolean deactivateUnknownUsers = true;

        UserManagerBuilder(IApplicationServerInternalApi service, ISimpleLogger logger, UserManagerReport report)
        {
            this.service = service;
            this.logger = logger;
            this.report = report;
        }

        UserManager get()
        {
            NullAuthenticationService authenticationService = new NullAuthenticationService()
                {
                    @Override
                    public Principal getPrincipal(String user) throws IllegalArgumentException
                    {
                        if (usersUnknownByAuthenticationService.contains(user))
                        {
                            throw new IllegalArgumentException("Unknown user " + user);
                        }
                        return new Principal(user, "John", "Doe", "jd@abc.de");
                    }
                };
            UserManager userManager = new UserManager(authenticationService, service, shareIdsMappingFile, logger, report);
            userManager.setGlobalSpaces(globalSpaces);
            userManager.setInstanceAdmins(instanceAdmins);
            userManager.setCommon(commonSpacesByRole, commonSamples, commonExperiments);
            userManager.setDeactivateUnknownUsers(deactivateUnknownUsers);
            return userManager;
        }

        private UserManagerBuilder noDeactivation()
        {
            deactivateUnknownUsers = false;
            return this;
        }

        private UserManagerBuilder unknownUser(Principal user)
        {
            usersUnknownByAuthenticationService.add(user.getUserId());
            return this;
        }

        private UserManagerBuilder instanceAdmins(String... instanceAdmins)
        {
            this.instanceAdmins = Arrays.asList(instanceAdmins);
            return this;
        }

        private UserManagerBuilder commonSpaces(Map<Role, List<String>> commonSpaces)
        {
            commonSpacesByRole = commonSpaces;
            return this;
        }

        private UserManagerBuilder commonSample(String sampleIdentifierTemplate, String sampleType)
        {
            commonSamples.put(sampleIdentifierTemplate, sampleType);
            return this;
        }

        private UserManagerBuilder commonExperiment(String experimentIdentifierTemplate, String experimentType)
        {
            Map<String, String> commonExperimentData = new HashMap<>();
            commonExperimentData.put("identifierTemplate", experimentIdentifierTemplate);
            commonExperimentData.put("experimentType", experimentType);
            commonExperiments.add(commonExperimentData);
            return this;
        }

        private UserManagerBuilder shareIdsMappingFile(File file)
        {
            shareIdsMappingFile = file;
            return this;
        }
    }

}

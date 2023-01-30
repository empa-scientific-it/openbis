/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.authorization;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.StatusFlag;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.reflection.AnnotationUtils;
import ch.systemsx.cisd.common.reflection.AnnotationUtils.Parameter;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.Capability;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.IPredicate;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IProjectDAO;
import ch.systemsx.cisd.openbis.generic.shared.authorization.IAuthorizationConfig;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.PermId;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.SpacePEBuilder;

/**
 * Test cases for corresponding {@link DefaultAccessController} class.
 * 
 * @author Christian Ribeaud
 */
@Friend(toClasses = DefaultAccessController.class)
public final class DefaultAccessControllerTest
{
    private DefaultAccessController accessController;

    private Mockery context;

    private IDAOFactory daoFactory;

    private IProjectDAO projectDAO;

    private IAuthorizationConfig authorizationConfig;

    private ProjectPE project;

    @BeforeTest
    void init()
    {
        LogInitializer.init();
        final File capFile = new File("etc/capabilities");
        capFile.delete();
        capFile.deleteOnExit();
        try
        {
            FileUtils.writeLines(capFile,
                    Arrays.asList("# Test overriding annotation",
                            "MY_CAP: SPACE_OBSERVER; ARG1 = SPACE_USER",
                            "my_cap2: arg1 = space_user"));
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        daoFactory = context.mock(IDAOFactory.class);
        projectDAO = context.mock(IProjectDAO.class);
        authorizationConfig = context.mock(IAuthorizationConfig.class);
        project = new ProjectPE();
        project.setCode("TEST");
        project.setSpace(new SpacePEBuilder().code("TEST").getSpace());

        context.checking(new Expectations()
            {
                {
                    allowing(authorizationConfig).isProjectLevelEnabled();
                    will(returnValue(false));
                    
                    allowing(daoFactory).getAuthorizationConfig();
                    will(returnValue(authorizationConfig));
                    
                    allowing(daoFactory).getProjectDAO();
                    will(returnValue(projectDAO));

                    allowing(projectDAO).tryGetByPermID("42");
                    will(returnValue(project));
                }
            });

        accessController = new DefaultAccessController(daoFactory);
    }

    @AfterMethod
    public void tearDown()
    {
        context.assertIsSatisfied();
    }

    private final static Set<RoleAssignmentPE> createRoleAssignments()
    {
        final Set<RoleAssignmentPE> roleAssignments = new LinkedHashSet<RoleAssignmentPE>();

        final RoleAssignmentPE groupRole = new RoleAssignmentPE();

        final SpacePE space = new SpacePE();
        space.setCode("CISD");
        groupRole.setSpace(space);
        groupRole.setRole(RoleCode.USER);
        roleAssignments.add(groupRole);

        final RoleAssignmentPE instanceRole = new RoleAssignmentPE();

        instanceRole.setRole(RoleCode.OBSERVER);
        roleAssignments.add(instanceRole);

        return roleAssignments;
    }

    @Test
    public final void testIsAuthorizedWithEmptyMethodRoles() throws Exception
    {
        final IAuthSession session = AuthorizationTestUtil.createSession();
        Method method = MyInterface.class.getMethod("myMethod");
        assertNotNull(method);
        final Argument<?>[] arguments = Argument.EMPTY_ARRAY;
        Status authorized = accessController.isAuthorized(session, method, arguments);
        assertEquals(StatusFlag.ERROR, authorized.getFlag());
        assertTrue(authorized.tryGetErrorMessage().indexOf(
                String.format(DefaultAccessController.METHOD_ROLES_NOT_FOUND_TEMPLATE,
                        "MyInterface.myMethod")) > -1);
        method = MyInterface.class.getMethod("myMethodWithEmptyRoles");
        authorized = accessController.isAuthorized(session, method, arguments);
        assertEquals(StatusFlag.ERROR, authorized.getFlag());
        assertTrue(authorized.tryGetErrorMessage().indexOf(
                String.format(DefaultAccessController.METHOD_ROLES_NOT_FOUND_TEMPLATE,
                        "MyInterface.myMethodWithEmptyRoles")) > -1);
    }

    @Test
    public final void testIsAuthorizedWithEmptyUserRoles() throws Exception
    {
        final IAuthSession session = AuthorizationTestUtil.createSession();
        final Method method = MyInterface.class.getMethod("myMethodWithSomeRoles");
        assertNotNull(method);
        final Argument<?>[] arguments = Argument.EMPTY_ARRAY;
        final Status authorized = accessController.isAuthorized(session, method, arguments);
        assertEquals(StatusFlag.ERROR, authorized.getFlag());
        assertEquals(String.format(
                DefaultAccessController.USER_ROLE_ASSIGNMENTS_NOT_FOUND_TEMPLATE,
                session.getUserName()), authorized.tryGetErrorMessage());
    }

    @Test
    public final void testIsAuthorizedWithNoMatchingRole() throws Exception
    {
        final IAuthSession session = AuthorizationTestUtil.createSession();
        session.tryGetPerson().setRoleAssignments(createRoleAssignments());
        final Method method = MyInterface.class.getMethod("myMethodWithSomeRoles");
        assertNotNull(method);
        final Argument<?>[] arguments = Argument.EMPTY_ARRAY;
        final Status authorized = accessController.isAuthorized(session, method, arguments);
        assertEquals(StatusFlag.ERROR, authorized.getFlag());
        Set<RoleWithHierarchy> roles =
                new HashSet<RoleWithHierarchy>(Arrays.asList(RoleWithHierarchy.INSTANCE_ADMIN));
        String expectedMessage =
                String.format(DefaultAccessController.MATCHING_ROLE_NOT_FOUND_TEMPLATE, roles,
                        session.getUserName());
        assertEquals(expectedMessage, authorized.tryGetErrorMessage());
    }

    @Test
    public final void testIsAuthorizedWithMatchingRole() throws Exception
    {
        final IAuthSession session = AuthorizationTestUtil.createSession();
        session.tryGetPerson().setRoleAssignments(createRoleAssignments());
        final Method method = MyInterface.class.getMethod("myMethodWithOtherRoles");
        assertNotNull(method);
        final Argument<?>[] arguments = Argument.EMPTY_ARRAY;
        final Status authorized = accessController.isAuthorized(session, method, arguments);
        assertEquals(StatusFlag.OK, authorized.getFlag());
        assertNull(authorized.tryGetErrorMessage());
    }

    @Test
    public final void testIsAuthorizedWithMatchingRoleOverrideCapabilities() throws Exception
    {
        final IAuthSession session = AuthorizationTestUtil.createSession();
        session.tryGetPerson().setRoleAssignments(createRoleAssignments());
        final Method method = MyInterface.class.getMethod("myMethodWithOtherRolesOverridden");
        assertNotNull(method);
        final Argument<?>[] arguments = Argument.EMPTY_ARRAY;
        final Status authorized = accessController.isAuthorized(session, method, arguments);
        assertEquals(StatusFlag.OK, authorized.getFlag());
        assertNull(authorized.tryGetErrorMessage());
    }

    @Test
    public final void testIsAuthorizedWithMatchingFirstRole() throws Exception
    {
        final IAuthSession session = AuthorizationTestUtil.createSession();
        session.tryGetPerson().setRoleAssignments(createRoleAssignments());
        final Method method = MyInterface.class.getMethod("myMethodWithTwoRoles");
        assertNotNull(method);
        final Argument<?>[] arguments = Argument.EMPTY_ARRAY;
        final Status authorized = accessController.isAuthorized(session, method, arguments);
        assertEquals(StatusFlag.OK, authorized.getFlag());
        assertNull(authorized.tryGetErrorMessage());
    }

    @Test
    public final void testIsAuthorizedWithMatchingSecondRole() throws Exception
    {
        final IAuthSession session = AuthorizationTestUtil.createSession();
        session.tryGetPerson().setRoleAssignments(createRoleAssignments());
        final Method method = MyInterface.class.getMethod("myMethodWithTwoRoles");
        assertNotNull(method);
        final Argument<?>[] arguments = Argument.EMPTY_ARRAY;
        final Status authorized = accessController.isAuthorized(session, method, arguments);
        assertEquals(StatusFlag.OK, authorized.getFlag());
        assertNull(authorized.tryGetErrorMessage());
    }

    @Test
    public void testIsAuthorizedWithUngardedArgument() throws Exception
    {
        final IAuthSession session = AuthorizationTestUtil.createSession();
        session.tryGetPerson().setRoleAssignments(createRoleAssignments());
        final Method method = MyInterface.class.getMethod("myMethodWithUngardedArgument", String.class, String.class);
        assertNotNull(method);
        Argument<?>[] arguments = createArguments(method);

        final Status authorized = accessController.isAuthorized(session, method, arguments);

        assertEquals("OK", authorized.toString());
        assertEquals(null, project.getDescription());
        context.assertIsSatisfied();
    }

    @Test
    public void testIsAuthorizedWithGardedArgument() throws Exception
    {
        final IAuthSession session = AuthorizationTestUtil.createSession();
        session.tryGetPerson().setRoleAssignments(createRoleAssignments());
        final Method method = MyInterface.class.getMethod("myMethodWithGardedArgument",
                String.class, String.class, String.class);
        assertNotNull(method);
        Argument<?>[] arguments = createArguments(method);

        final Status authorized = accessController.isAuthorized(session, method, arguments);

        assertEquals("OK", authorized.toString());
        assertEquals("person: john_doe, roles: [INSTANCE_OBSERVER, SPACE_USER], value: arg0\n"
                + "person: john_doe, roles: [INSTANCE_OBSERVER, SPACE_USER], value: arg1", project.getDescription());
        context.assertIsSatisfied();
    }

    @Test
    public void testIsNotAuthorizedWithGardedArgument() throws Exception
    {
        final IAuthSession session = AuthorizationTestUtil.createSession();
        session.tryGetPerson().setRoleAssignments(createRoleAssignments());
        final Method method = MyInterface.class.getMethod("myMethodWithGardedArgument",
                String.class, String.class, String.class);
        assertNotNull(method);
        Argument<?>[] arguments = createArguments(method);
        project.setId(1L);

        final Status authorized = accessController.isAuthorized(session, method, arguments);

        assertEquals("ERROR", authorized.toString());
        assertEquals("person: john_doe, roles: [INSTANCE_OBSERVER, SPACE_USER], value: arg0", project.getDescription());
        context.assertIsSatisfied();
    }

    @Test
    public void testIsNotAuthorizedWithGardedArgumentWithDifferentRoles() throws Exception
    {
        final IAuthSession session = AuthorizationTestUtil.createSession();
        session.tryGetPerson().setRoleAssignments(createRoleAssignments());
        final Method method = MyInterface.class.getMethod("myMethodWithGardedArgumentWithDifferentRoles",
                String.class, String.class);
        assertNotNull(method);
        Argument<?>[] arguments = createArguments(method);

        final Status authorized = accessController.isAuthorized(session, method, arguments);

        assertEquals("ERROR: \"None of method roles '[SPACE_ETL_SERVER, INSTANCE_ETL_SERVER, INSTANCE_ADMIN]' "
                + "could be found in roles of user 'John Doe'.\"", authorized.toString());
        assertEquals(null, project.getDescription());
        context.assertIsSatisfied();
    }

    @Test
    public void testIsAuthorizedWithGardedArgumentWithRolesOverridden() throws Exception
    {
        final IAuthSession session = AuthorizationTestUtil.createSession();
        session.tryGetPerson().setRoleAssignments(createRoleAssignments());
        final Method method = MyInterface.class.getMethod("myMethodWithGardedArgumentWithRolesOverridden",
                String.class, String.class);
        assertNotNull(method);
        Argument<?>[] arguments = createArguments(method);

        final Status authorized = accessController.isAuthorized(session, method, arguments);

        assertEquals("OK", authorized.toString());
        assertEquals("person: john_doe, roles: [SPACE_USER], value: arg0", project.getDescription());
        context.assertIsSatisfied();
    }

    @Test
    public void testIsAuthorizedWithGardedArgumentWithRolesOverridden2() throws Exception
    {
        final IAuthSession session = AuthorizationTestUtil.createSession();
        session.tryGetPerson().setRoleAssignments(createRoleAssignments());
        final Method method = MyInterface.class.getMethod("myMethodWithGardedArgumentWithRolesOverridden2",
                String.class, String.class);
        assertNotNull(method);
        Argument<?>[] arguments = createArguments(method);

        final Status authorized = accessController.isAuthorized(session, method, arguments);

        assertEquals("OK", authorized.toString());
        assertEquals("person: john_doe, roles: [SPACE_USER], value: arg0", project.getDescription());
        context.assertIsSatisfied();
    }

    @Test
    public void testIsAuthorizedWithGardedArgumentWithRolesOverridden3() throws Exception
    {
        final IAuthSession session = AuthorizationTestUtil.createSession();
        session.tryGetPerson().setRoleAssignments(createRoleAssignments());
        final Method method = MyInterface.class.getMethod("myMethodWithGardedArgumentWithRolesOverridden3",
                String.class, String.class, String.class);
        assertNotNull(method);
        Argument<?>[] arguments = createArguments(method);

        final Status authorized = accessController.isAuthorized(session, method, arguments);

        assertEquals("ERROR: \"None of method roles '[SPACE_POWER_USER, SPACE_ADMIN, INSTANCE_ADMIN]' "
                + "could be found in roles of user 'John Doe'.\"", authorized.toString());
        assertEquals(null, project.getDescription());
        context.assertIsSatisfied();
    }

    private Argument<?>[] createArguments(final Method method)
    {
        List<Argument<?>> arguments = new ArrayList<>();
        List<Parameter<AuthorizationGuard>> parameters = AnnotationUtils.getAnnotatedParameters(method, AuthorizationGuard.class);
        for (int i = 0; i < parameters.size(); i++)
        {
            Parameter<AuthorizationGuard> parameter = parameters.get(i);
            arguments.add(new Argument<>(String.class, "arg" + i, parameter.getAnnotation()));
        }
        return arguments.toArray(new Argument[0]);
    }

    //
    // Helper classes
    //

    private static interface MyInterface
    {
        public void myMethod();

        @RolesAllowed
        public void myMethodWithEmptyRoles();

        @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
        public void myMethodWithSomeRoles();

        @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
        public void myMethodWithOtherRoles();

        @RolesAllowed(RoleWithHierarchy.SPACE_ADMIN)
        @Capability("MY_CAP")
        public void myMethodWithOtherRolesOverridden();

        @RolesAllowed({ RoleWithHierarchy.SPACE_ETL_SERVER, RoleWithHierarchy.SPACE_OBSERVER })
        public void myMethodWithTwoRoles();

        @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
        public void myMethodWithUngardedArgument(String sessionToken, String argument1);

        @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
        public void myMethodWithGardedArgument(String sessionToken,
                @AuthorizationGuard(guardClass = MockPredicate.class) String argument1,
                @AuthorizationGuard(guardClass = MockPredicate.class) String argument2);

        @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
        public void myMethodWithGardedArgumentWithDifferentRoles(String sessionToken,
                @AuthorizationGuard(guardClass = MockPredicate.class, rolesAllowed = { RoleWithHierarchy.SPACE_ETL_SERVER }) String argument1);

        @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
        @Capability("MY_CAP")
        public void myMethodWithGardedArgumentWithRolesOverridden(String sessionToken,
                @AuthorizationGuard(name = "ARG1", guardClass = MockPredicate.class, rolesAllowed = {
                        RoleWithHierarchy.SPACE_ETL_SERVER }) String argument1);

        @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
        @Capability("MY_CAP2")
        public void myMethodWithGardedArgumentWithRolesOverridden2(String sessionToken,
                @AuthorizationGuard(name = "ARG1", guardClass = MockPredicate.class, rolesAllowed = {
                        RoleWithHierarchy.SPACE_ETL_SERVER }) String argument1);

        @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
        public void myMethodWithGardedArgumentWithRolesOverridden3(String sessionToken,
                @AuthorizationGuard(name = "ARG1", guardClass = MockPredicate.class, rolesAllowed = {
                        RoleWithHierarchy.SPACE_POWER_USER }) String argument1,
                @AuthorizationGuard(name = "ARG2", guardClass = MockPredicate.class, rolesAllowed = {
                        RoleWithHierarchy.SPACE_USER }) String argument2);
    }

    /**
     * A mock predicate which uses a ProjectPE instance for communication.
     */
    public static class MockPredicate implements IPredicate<String>
    {
        private ProjectPE project;

        @Override
        public Status evaluate(PersonPE person, List<RoleWithIdentifier> allowedRoles, String valueOrNull)
        {
            List<RoleWithHierarchy> roles = new ArrayList<>();
            for (RoleWithIdentifier roleWithIdentifier : allowedRoles)
            {
                roles.add(roleWithIdentifier.getRole());
            }
            Collections.sort(roles);

            String description = "person: " + person.getUserId() + ", roles: " + roles + ", value: " + valueOrNull;
            if (project.getDescription() != null)
            {
                description = project.getDescription() + "\n" + description;
            }
            project.setDescription(description);
            return project.getId() == null ? Status.OK : Status.createError();
        }

        @Override
        public void init(IAuthorizationDataProvider provider)
        {
            project = provider.tryGetProjectByPermId(new PermId("42"));
        }
    }
}

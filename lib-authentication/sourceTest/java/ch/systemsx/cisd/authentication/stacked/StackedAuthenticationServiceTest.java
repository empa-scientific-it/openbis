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

package ch.systemsx.cisd.authentication.stacked;

import static org.testng.AssertJUnit.*;

import java.util.Arrays;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.Principal;

/**
 * Test cases for the {@link StackedAuthenticationService}.
 * 
 * @author Bernd Rinn
 */
public class StackedAuthenticationServiceTest
{
    private Mockery context;

    private IAuthenticationService authService1;

    private IAuthenticationService authService2;

    private IAuthenticationService authService3;

    private IAuthenticationService stackedAuthService;

    @BeforeMethod
    public final void setUp()
    {
        context = new Mockery();
        authService1 = context.mock(IAuthenticationService.class, "auth service 1");
        authService2 = context.mock(IAuthenticationService.class, "auth service 2");
        authService3 = context.mock(IAuthenticationService.class, "auth service 3");
        addStandardExpectations();
        addAlways();
        stackedAuthService =
                new StackedAuthenticationService(Arrays.asList(authService1, authService2,
                        authService3));
    }

    private void addAlways()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(authService1).isConfigured();
                    will(returnValue(true));
                    allowing(authService2).isConfigured();
                    will(returnValue(true));
                    allowing(authService3).isConfigured();
                    will(returnValue(false));
                }
            });
    }

    private void addStandardExpectations()
    {
        context.checking(new Expectations()
            {
                {
                    one(authService1).isRemote();
                    one(authService1).supportsListingByUserId();
                    one(authService1).supportsListingByEmail();
                    one(authService1).supportsListingByLastName();
                    one(authService1).supportsAuthenticatingByEmail();

                    one(authService2).isRemote();
                    one(authService2).supportsListingByUserId();
                    one(authService2).supportsListingByEmail();
                    one(authService2).supportsListingByLastName();
                    one(authService2).supportsAuthenticatingByEmail();
                }
            });
    }

    @AfterMethod
    public void tearDown()
    {
        context.assertIsSatisfied();
    }

    @Test
    public void testCheck()
    {
        context.checking(new Expectations()
            {
                {
                    one(authService1).check();
                    one(authService2).check();
                }
            });
        assertTrue(stackedAuthService.isConfigured());
        stackedAuthService.check();
        context.assertIsSatisfied();
    }

    @Test
    public void testSupportsListingByUserIdFalse()
    {
        assertFalse(stackedAuthService.supportsListingByUserId());
        context.assertIsSatisfied();
    }

    @Test
    public void testSupportsListingByEmailFalse()
    {
        assertFalse(stackedAuthService.supportsListingByEmail());
        context.assertIsSatisfied();
    }

    @Test
    public void testSupportsListingByLastNameFalse()
    {
        assertFalse(stackedAuthService.supportsListingByLastName());
        context.assertIsSatisfied();
    }

    @Test
    public void testSupportsListingByUserIdTrue()
    {
        context = new Mockery();
        authService1 = context.mock(IAuthenticationService.class, "auth service 1");
        authService2 = context.mock(IAuthenticationService.class, "auth service 2");
        addAlways();
        context.checking(new Expectations()
            {
                {
                    one(authService1).isRemote();
                    one(authService1).supportsListingByUserId();
                    one(authService1).supportsListingByEmail();
                    one(authService1).supportsListingByLastName();
                    one(authService1).supportsAuthenticatingByEmail();

                    one(authService2).isRemote();
                    one(authService2).supportsListingByUserId();
                    will(returnValue(true));
                    one(authService2).supportsListingByEmail();
                    one(authService2).supportsListingByLastName();
                    one(authService2).supportsAuthenticatingByEmail();
                }
            });
        stackedAuthService =
                new StackedAuthenticationService(Arrays.asList(authService1, authService2));
        assertTrue(stackedAuthService.supportsListingByUserId());
        context.assertIsSatisfied();
    }

    @Test
    public void testSupportsListingByEmailTrue()
    {
        context = new Mockery();
        authService1 = context.mock(IAuthenticationService.class, "auth service 1");
        authService2 = context.mock(IAuthenticationService.class, "auth service 2");
        addAlways();
        context.checking(new Expectations()
            {
                {
                    one(authService1).isRemote();
                    one(authService1).supportsListingByUserId();
                    one(authService1).supportsListingByEmail();
                    will(returnValue(true));
                    one(authService1).supportsListingByLastName();
                    one(authService1).supportsAuthenticatingByEmail();

                    one(authService2).isRemote();
                    one(authService2).supportsListingByUserId();
                    one(authService2).supportsListingByEmail();
                    one(authService2).supportsListingByLastName();
                    one(authService2).supportsAuthenticatingByEmail();
                }
            });
        stackedAuthService =
                new StackedAuthenticationService(Arrays.asList(authService1, authService2));
        assertTrue(stackedAuthService.supportsListingByEmail());
        context.assertIsSatisfied();
    }

    @Test
    public void testSupportsListingByLastNameTrue()
    {
        context = new Mockery();
        authService1 = context.mock(IAuthenticationService.class, "auth service 1");
        authService2 = context.mock(IAuthenticationService.class, "auth service 2");
        addAlways();
        context.checking(new Expectations()
            {
                {
                    one(authService1).isRemote();
                    one(authService1).supportsListingByUserId();
                    one(authService1).supportsListingByEmail();
                    one(authService1).supportsListingByLastName();
                    one(authService1).supportsAuthenticatingByEmail();

                    one(authService2).isRemote();
                    one(authService2).supportsListingByUserId();
                    one(authService2).supportsListingByEmail();
                    one(authService2).supportsListingByLastName();
                    will(returnValue(true));
                    one(authService2).supportsAuthenticatingByEmail();
                }
            });
        stackedAuthService =
                new StackedAuthenticationService(Arrays.asList(authService1, authService2));
        assertTrue(stackedAuthService.supportsListingByLastName());
        context.assertIsSatisfied();
    }

    @Test
    public void testAuthenticateUserFalse()
    {
        final String user = "user";
        final String password = "password";

        context.checking(new Expectations()
            {
                {
                    one(authService1).tryGetAndAuthenticateUser(user, password);
                    one(authService2).tryGetAndAuthenticateUser(user, password);
                }
            });
        addAlways();
        assertFalse(stackedAuthService.authenticateUser(user, password));
        context.assertIsSatisfied();
    }

    @Test
    public void testAuthenticateUserFirstServiceTrue()
    {
        final String user = "user";
        final String password = "password";
        final Principal principal = new Principal(user, "", "", "", true);

        context.checking(new Expectations()
            {
                {
                    one(authService1).tryGetAndAuthenticateUser(user, password);
                    will(returnValue(principal));
                }
            });
        addAlways();
        assertTrue(stackedAuthService.authenticateUser(user, password));
        context.assertIsSatisfied();
    }

    @Test
    public void testAuthenticateUserSecondServiceTrue()
    {
        final String user = "user";
        final String password = "password";
        final Principal principal = new Principal(user, "", "", "", true);

        context.checking(new Expectations()
            {
                {
                    one(authService1).tryGetAndAuthenticateUser(user, password);
                    one(authService2).tryGetAndAuthenticateUser(user, password);
                    will(returnValue(principal));
                }
            });
        addAlways();
        assertTrue(stackedAuthService.authenticateUser(user, password));
        context.assertIsSatisfied();
    }

    @Test
    public void testGetPrincipalFirstService()
    {
        final String user = "user";
        final String firstName = "first name";
        final String lastName = "last name";
        final String email = "email address";
        final Principal principal = new Principal(user, firstName, lastName, email, false);

        context.checking(new Expectations()
            {
                {
                    one(authService1).tryGetAndAuthenticateUser(user, null);
                    will(returnValue(principal));
                }
            });
        addAlways();
        assertEquals(principal, stackedAuthService.getPrincipal(user));
        context.assertIsSatisfied();
    }

    @Test
    public void testGetPrincipalSecondService()
    {
        final String user = "user";
        final String firstName = "first name";
        final String lastName = "last name";
        final String email = "email address";
        final Principal principal = new Principal(user, firstName, lastName, email, false);

        context.checking(new Expectations()
            {
                {
                    one(authService1).tryGetAndAuthenticateUser(user, null);
                    one(authService2).tryGetAndAuthenticateUser(user, null);
                    will(returnValue(principal));
                }
            });
        addAlways();
        assertEquals(principal, stackedAuthService.getPrincipal(user));
        context.assertIsSatisfied();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetPrincipalNoService()
    {
        final String user = "user";

        context.checking(new Expectations()
            {
                {
                    one(authService1).tryGetAndAuthenticateUser(user, null);
                    one(authService2).tryGetAndAuthenticateUser(user, null);
                }
            });
        addAlways();
        stackedAuthService.getPrincipal(user);
    }

    @Test
    public void testListByEmailFirstService()
    {
        context = new Mockery();
        authService1 = context.mock(IAuthenticationService.class, "auth service 1");
        authService2 = context.mock(IAuthenticationService.class, "auth service 2");
        final String emailQuery = "some email with *";
        final Principal principal1 =
                new Principal("user1", "first name 1", "last name 1", "email 1", false);
        final Principal principal2 =
                new Principal("user2", "first name 2", "last name 2", "email 2", false);

        context.checking(new Expectations()
            {
                {
                    one(authService1).isRemote();
                    one(authService1).isConfigured();
                    will(returnValue(true));
                    one(authService1).supportsListingByUserId();
                    exactly(2).of(authService1).supportsListingByEmail();
                    will(returnValue(true));
                    one(authService1).supportsAuthenticatingByEmail();
                    one(authService1).supportsListingByLastName();

                    one(authService2).isRemote();
                    one(authService2).isConfigured();
                    will(returnValue(true));
                    one(authService2).supportsListingByUserId();
                    exactly(2).of(authService2).supportsListingByEmail();
                    one(authService2).supportsAuthenticatingByEmail();
                    one(authService2).supportsListingByLastName();

                    one(authService1).listPrincipalsByEmail(emailQuery);
                    will(returnValue(Arrays.asList(principal1, principal2)));
                }
            });
        addAlways();
        stackedAuthService =
                new StackedAuthenticationService(Arrays.asList(authService1, authService2));
        final List<Principal> result = stackedAuthService.listPrincipalsByEmail(emailQuery);
        assertEquals(2, result.size());
        assertEquals(principal1, result.get(0));
        assertEquals(principal2, result.get(1));
        context.assertIsSatisfied();
    }

    @Test
    public void testListByEmailSecondService()
    {
        context = new Mockery();
        authService1 = context.mock(IAuthenticationService.class, "auth service 1");
        authService2 = context.mock(IAuthenticationService.class, "auth service 2");
        final String emailQuery = "some email with *";
        final Principal principal =
                new Principal("user3", "first name 3", "last name 3", "email 3", false);

        context.checking(new Expectations()
            {
                {
                    one(authService1).isRemote();
                    one(authService1).isConfigured();
                    will(returnValue(true));
                    one(authService1).supportsListingByUserId();
                    exactly(2).of(authService1).supportsListingByEmail();
                    one(authService1).supportsAuthenticatingByEmail();
                    one(authService1).supportsListingByLastName();

                    one(authService2).isRemote();
                    one(authService2).isConfigured();
                    will(returnValue(true));
                    one(authService2).supportsListingByUserId();
                    exactly(2).of(authService2).supportsListingByEmail();
                    will(returnValue(true));
                    one(authService2).supportsAuthenticatingByEmail();
                    one(authService2).supportsListingByLastName();

                    one(authService2).listPrincipalsByEmail(emailQuery);
                    will(returnValue(Arrays.asList(principal)));
                }
            });
        addAlways();
        stackedAuthService =
                new StackedAuthenticationService(Arrays.asList(authService1, authService2));
        final List<Principal> result = stackedAuthService.listPrincipalsByEmail(emailQuery);
        assertEquals(1, result.size());
        assertEquals(principal, result.get(0));
        context.assertIsSatisfied();
    }

    @Test
    public void testListByEmailBothServicees()
    {
        context = new Mockery();
        authService1 = context.mock(IAuthenticationService.class, "auth service 1");
        authService2 = context.mock(IAuthenticationService.class, "auth service 2");
        final String emailQuery = "some email with *";
        final Principal principal1 =
                new Principal("user1", "first name 1", "last name 1", "email 1", false);
        final Principal principal2 =
                new Principal("user2", "first name 2", "last name 2", "email 2", false);
        final Principal principal3 =
                new Principal("user3", "first name 3", "last name 3", "email 3", false);

        context.checking(new Expectations()
            {
                {
                    one(authService1).isRemote();
                    one(authService1).isConfigured();
                    will(returnValue(true));
                    one(authService1).supportsListingByUserId();
                    exactly(2).of(authService1).supportsListingByEmail();
                    will(returnValue(true));
                    one(authService1).supportsAuthenticatingByEmail();
                    one(authService1).supportsListingByLastName();

                    one(authService2).isRemote();
                    one(authService2).isConfigured();
                    will(returnValue(true));
                    one(authService2).supportsListingByUserId();
                    exactly(2).of(authService2).supportsListingByEmail();
                    will(returnValue(true));
                    one(authService2).supportsAuthenticatingByEmail();
                    one(authService2).supportsListingByLastName();

                    one(authService1).listPrincipalsByEmail(emailQuery);
                    will(returnValue(Arrays.asList(principal1, principal2)));
                    one(authService2).listPrincipalsByEmail(emailQuery);
                    will(returnValue(Arrays.asList(principal3)));
                }
            });
        addAlways();
        stackedAuthService =
                new StackedAuthenticationService(Arrays.asList(authService1, authService2));
        final List<Principal> result = stackedAuthService.listPrincipalsByEmail(emailQuery);
        assertEquals(3, result.size());
        assertEquals(principal1, result.get(0));
        assertEquals(principal2, result.get(1));
        assertEquals(principal3, result.get(2));
        context.assertIsSatisfied();
    }

    @Test
    public void testListByUserIdFirstService()
    {
        context = new Mockery();
        authService1 = context.mock(IAuthenticationService.class, "auth service 1");
        authService2 = context.mock(IAuthenticationService.class, "auth service 2");
        final String userIdQuery = "some user id with *";
        final Principal principal1 =
                new Principal("user1", "first name 1", "last name 1", "email 1", false);
        final Principal principal2 =
                new Principal("user2", "first name 2", "last name 2", "email 2", false);

        context.checking(new Expectations()
            {
                {
                    one(authService1).isRemote();
                    exactly(2).of(authService1).supportsListingByUserId();
                    will(returnValue(true));
                    one(authService1).supportsListingByEmail();
                    one(authService1).supportsListingByLastName();
                    one(authService1).supportsAuthenticatingByEmail();

                    one(authService2).isRemote();
                    exactly(2).of(authService2).supportsListingByUserId();
                    one(authService2).supportsListingByEmail();
                    one(authService2).supportsListingByLastName();
                    one(authService2).supportsAuthenticatingByEmail();

                    one(authService1).listPrincipalsByUserId(userIdQuery);
                    will(returnValue(Arrays.asList(principal1, principal2)));
                }
            });
        addAlways();
        stackedAuthService =
                new StackedAuthenticationService(Arrays.asList(authService1, authService2));
        final List<Principal> result = stackedAuthService.listPrincipalsByUserId(userIdQuery);
        assertEquals(2, result.size());
        assertEquals(principal1, result.get(0));
        assertEquals(principal2, result.get(1));
        context.assertIsSatisfied();
    }

    @Test
    public void testListByuserIdSecondService()
    {
        context = new Mockery();
        authService1 = context.mock(IAuthenticationService.class, "auth service 1");
        authService2 = context.mock(IAuthenticationService.class, "auth service 2");
        final String userIdQuery = "some user id with *";
        final Principal principal =
                new Principal("user3", "first name 3", "last name 3", "email 3", false);

        context.checking(new Expectations()
            {
                {
                    one(authService1).isRemote();
                    exactly(2).of(authService1).supportsListingByUserId();
                    one(authService1).supportsListingByEmail();
                    one(authService1).supportsListingByLastName();
                    one(authService1).supportsAuthenticatingByEmail();

                    one(authService2).isRemote();
                    exactly(2).of(authService2).supportsListingByUserId();
                    will(returnValue(true));
                    one(authService2).supportsListingByEmail();
                    one(authService2).supportsListingByLastName();
                    one(authService2).supportsAuthenticatingByEmail();

                    one(authService2).listPrincipalsByUserId(userIdQuery);
                    will(returnValue(Arrays.asList(principal)));
                }
            });
        addAlways();
        stackedAuthService =
                new StackedAuthenticationService(Arrays.asList(authService1, authService2));
        final List<Principal> result = stackedAuthService.listPrincipalsByUserId(userIdQuery);
        assertEquals(1, result.size());
        assertEquals(principal, result.get(0));
        context.assertIsSatisfied();
    }

    @Test
    public void testListByUserIdBothServicees()
    {
        context = new Mockery();
        authService1 = context.mock(IAuthenticationService.class, "auth service 1");
        authService2 = context.mock(IAuthenticationService.class, "auth service 2");
        final String userIdQuery = "some user id with *";
        final Principal principal1 =
                new Principal("user1", "first name 1", "last name 1", "email 1", false);
        final Principal principal2 =
                new Principal("user2", "first name 2", "last name 2", "email 2", false);
        final Principal principal3 =
                new Principal("user3", "first name 3", "last name 3", "email 3", false);

        context.checking(new Expectations()
            {
                {
                    one(authService1).isRemote();
                    exactly(2).of(authService1).supportsListingByUserId();
                    will(returnValue(true));
                    one(authService1).supportsListingByEmail();
                    one(authService1).supportsListingByLastName();
                    one(authService1).supportsAuthenticatingByEmail();

                    one(authService2).isRemote();
                    exactly(2).of(authService2).supportsListingByUserId();
                    will(returnValue(true));
                    one(authService2).supportsListingByEmail();
                    one(authService2).supportsListingByLastName();
                    one(authService2).supportsAuthenticatingByEmail();

                    one(authService1).listPrincipalsByUserId(userIdQuery);
                    will(returnValue(Arrays.asList(principal1, principal2)));
                    one(authService2).listPrincipalsByUserId(userIdQuery);
                    will(returnValue(Arrays.asList(principal3)));
                }
            });
        addAlways();
        stackedAuthService =
                new StackedAuthenticationService(Arrays.asList(authService1, authService2));
        final List<Principal> result = stackedAuthService.listPrincipalsByUserId(userIdQuery);
        assertEquals(3, result.size());
        assertEquals(principal1, result.get(0));
        assertEquals(principal2, result.get(1));
        assertEquals(principal3, result.get(2));
        context.assertIsSatisfied();
    }

    @Test
    public void testListByLastNameFirstService()
    {
        context = new Mockery();
        authService1 = context.mock(IAuthenticationService.class, "auth service 1");
        authService2 = context.mock(IAuthenticationService.class, "auth service 2");
        final String lastNameQuery = "some user id with *";
        final Principal principal1 =
                new Principal("user1", "first name 1", "last name 1", "email 1", false);
        final Principal principal2 =
                new Principal("user2", "first name 2", "last name 2", "email 2", false);

        context.checking(new Expectations()
            {
                {
                    one(authService1).isRemote();
                    one(authService1).supportsListingByUserId();
                    one(authService1).supportsListingByEmail();
                    exactly(2).of(authService1).supportsListingByLastName();
                    will(returnValue(true));
                    one(authService1).supportsAuthenticatingByEmail();

                    one(authService2).isRemote();
                    one(authService2).supportsListingByUserId();
                    one(authService2).supportsListingByEmail();
                    exactly(2).of(authService2).supportsListingByLastName();
                    one(authService2).supportsAuthenticatingByEmail();

                    one(authService1).listPrincipalsByLastName(lastNameQuery);
                    will(returnValue(Arrays.asList(principal1, principal2)));
                }
            });
        addAlways();
        stackedAuthService =
                new StackedAuthenticationService(Arrays.asList(authService1, authService2));
        final List<Principal> result = stackedAuthService.listPrincipalsByLastName(lastNameQuery);
        assertEquals(2, result.size());
        assertEquals(principal1, result.get(0));
        assertEquals(principal2, result.get(1));
        context.assertIsSatisfied();
    }

    @Test
    public void testListByLastNameSecondService()
    {
        context = new Mockery();
        authService1 = context.mock(IAuthenticationService.class, "auth service 1");
        authService2 = context.mock(IAuthenticationService.class, "auth service 2");
        final String lastNameQuery = "some user id with *";
        final Principal principal =
                new Principal("user3", "first name 3", "last name 3", "email 3", false);

        context.checking(new Expectations()
            {
                {
                    one(authService1).isRemote();
                    one(authService1).supportsListingByUserId();
                    one(authService1).supportsListingByEmail();
                    exactly(2).of(authService1).supportsListingByLastName();
                    one(authService1).supportsAuthenticatingByEmail();

                    one(authService2).isRemote();
                    one(authService2).supportsListingByUserId();
                    one(authService2).supportsListingByEmail();
                    exactly(2).of(authService2).supportsListingByLastName();
                    will(returnValue(true));
                    one(authService2).supportsAuthenticatingByEmail();

                    one(authService2).listPrincipalsByLastName(lastNameQuery);
                    will(returnValue(Arrays.asList(principal)));
                }
            });
        addAlways();
        stackedAuthService =
                new StackedAuthenticationService(Arrays.asList(authService1, authService2));
        final List<Principal> result = stackedAuthService.listPrincipalsByLastName(lastNameQuery);
        assertEquals(1, result.size());
        assertEquals(principal, result.get(0));
        context.assertIsSatisfied();
    }

    @Test
    public void testListByLastNameBothServicees()
    {
        context = new Mockery();
        authService1 = context.mock(IAuthenticationService.class, "auth service 1");
        authService2 = context.mock(IAuthenticationService.class, "auth service 2");
        final String lastNameQuery = "some user id with *";
        final Principal principal1 =
                new Principal("user1", "first name 1", "last name 1", "email 1", false);
        final Principal principal2 =
                new Principal("user2", "first name 2", "last name 2", "email 2", false);
        final Principal principal3 =
                new Principal("user3", "first name 3", "last name 3", "email 3", false);

        context.checking(new Expectations()
            {
                {
                    one(authService1).isRemote();
                    one(authService1).isConfigured();
                    will(returnValue(true));
                    one(authService1).supportsListingByUserId();
                    one(authService1).supportsListingByEmail();
                    one(authService1).supportsAuthenticatingByEmail();
                    exactly(2).of(authService1).supportsListingByLastName();
                    will(returnValue(true));

                    one(authService2).isRemote();
                    one(authService2).isConfigured();
                    will(returnValue(true));
                    one(authService2).supportsListingByUserId();
                    one(authService2).supportsListingByEmail();
                    one(authService2).supportsAuthenticatingByEmail();
                    exactly(2).of(authService2).supportsListingByLastName();
                    will(returnValue(true));

                    one(authService1).listPrincipalsByLastName(lastNameQuery);
                    will(returnValue(Arrays.asList(principal1, principal2)));
                    one(authService2).listPrincipalsByLastName(lastNameQuery);
                    will(returnValue(Arrays.asList(principal3)));
                }
            });
        addAlways();
        stackedAuthService =
                new StackedAuthenticationService(Arrays.asList(authService1, authService2));
        final List<Principal> result = stackedAuthService.listPrincipalsByLastName(lastNameQuery);
        assertEquals(3, result.size());
        assertEquals(principal1, result.get(0));
        assertEquals(principal2, result.get(1));
        assertEquals(principal3, result.get(2));
        context.assertIsSatisfied();
    }

}

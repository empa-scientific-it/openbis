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

package ch.systemsx.cisd.authentication.file;

import static org.testng.AssertJUnit.*;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.authentication.Principal;

/**
 * Test cases for the {@link FileAuthenticationService}.
 *
 * @author Bernd Rinn
 */
public class FileAuthenticationServiceTest
{

    private Mockery context;

    private IUserStore<UserEntry> userStore;

    private FileAuthenticationService authService;

    @SuppressWarnings("unchecked")
    @BeforeMethod
    public final void setUp()
    {
        context = new Mockery();
        userStore = context.mock(IUserStore.class);
        authService = new FileAuthenticationService(userStore, null);
    }

    @AfterMethod
    public void tearDown()
    {
        context.assertIsSatisfied();
    }

    @Test
    public void testAuthenticateUserSuccess()
    {
        final String user = "User";
        final String password = "passw0rd";
        context.checking(new Expectations()
            {
                {
                    one(userStore).isPasswordCorrect(user, password);
                    will(returnValue(true));
                }
            });
        assertTrue(authService.authenticateUser(user, password));
        context.assertIsSatisfied();
    }

    @Test
    public void testAuthenticateUserFailure()
    {
        final String user = "User";
        final String password = "passw0rd";
        context.checking(new Expectations()
            {
                {
                    one(userStore).isPasswordCorrect(user, password);
                    will(returnValue(false));
                }
            });
        assertFalse(authService.authenticateUser(user, password));
        context.assertIsSatisfied();
    }

    @Test
    public void testGetPrincipalSuccess()
    {
        final String uid = "uid";
        final UserEntry user = new UserEntry(uid, "email", "first", "last", "pwd");
        context.checking(new Expectations()
            {
                {
                    one(userStore).tryGetUserById(uid);
                    will(returnValue(user));
                }
            });
        assertEquals(user.asPrincipal(), authService.getPrincipal(uid));
        context.assertIsSatisfied();
    }

    @Test
    public void testGetPrincipalFailure()
    {
        final String uid = "uid";
        context.checking(new Expectations()
            {
                {
                    one(userStore).tryGetUserById(uid);
                    will(returnValue(null));
                }
            });
        try
        {
            authService.getPrincipal(uid);
            fail("Unknown user went undetected.");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Cannot find user 'uid'.", ex.getMessage());
        }
        context.assertIsSatisfied();
    }

    @Test
    public void testGetAndAuthenticateUserSuccess()
    {
        final String uid = "uid";
        final String password = "passw0rd";
        final UserEntry user = new UserEntry(uid, "email", "first", "last", password);
        context.checking(new Expectations()
            {
                {
                    one(userStore).tryGetAndAuthenticateUserById(uid, password);
                    will(returnValue(new UserEntryAuthenticationState<UserEntry>(user, true)));
                }
            });
        final Principal principal = user.asPrincipal();
        principal.setAuthenticated(true);
        assertEquals(principal, authService.tryGetAndAuthenticateUser(uid, password));
        context.assertIsSatisfied();
    }

    @Test
    public void testGetAndAuthenticateUserAuthenticationFailed()
    {
        final String uid = "uid";
        final String password = "passw0rd";
        final UserEntry user = new UserEntry(uid, "email", "first", "last", password);
        context.checking(new Expectations()
            {
                {
                    one(userStore).tryGetAndAuthenticateUserById(uid, password);
                    will(returnValue(new UserEntryAuthenticationState<UserEntry>(user, false)));
                }
            });
        final Principal principal = user.asPrincipal();
        assertEquals(principal, authService.tryGetAndAuthenticateUser(uid, password));
        context.assertIsSatisfied();
    }

    @Test
    public void testGetAndAuthenticateUserByEmailSuccess()
    {
        final String uid = "uid";
        final String email = "email@null.org";
        final String password = "passw0rd";
        final UserEntry user = new UserEntry(uid, email, "first", "last", password);
        context.checking(new Expectations()
            {
                {
                    one(userStore).tryGetAndAuthenticateUserByEmail(email, password);
                    will(returnValue(new UserEntryAuthenticationState<UserEntry>(user, true)));
                }
            });
        final Principal principal = user.asPrincipal();
        principal.setAuthenticated(true);
        assertEquals(principal, authService.tryGetAndAuthenticateUserByEmail(email, password));
        context.assertIsSatisfied();
    }

    @Test
    public void testGetAndAuthenticateUserByEmailAuthenticationFailed()
    {
        final String uid = "uid";
        final String email = "email@null.org";
        final String password = "passw0rd";
        final UserEntry user = new UserEntry(uid, email, "first", "last", password);
        context.checking(new Expectations()
            {
                {
                    one(userStore).tryGetAndAuthenticateUserByEmail(email, password);
                    will(returnValue(new UserEntryAuthenticationState<UserEntry>(user, false)));
                }
            });
        final Principal principal = user.asPrincipal();
        assertEquals(principal, authService.tryGetAndAuthenticateUserByEmail(email, password));
        context.assertIsSatisfied();
    }

}

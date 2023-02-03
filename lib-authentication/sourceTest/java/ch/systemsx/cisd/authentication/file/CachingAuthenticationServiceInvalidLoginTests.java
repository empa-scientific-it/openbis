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

package ch.systemsx.cisd.authentication.file;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.utilities.ITimeProvider;

/**
 * Test cases for the {@link CachingAuthenticationService} with invalid login attempts.
 * 
 * @author Bernd Rinn
 */
public class CachingAuthenticationServiceInvalidLoginTests
{
    private static final File workingDirectory =
            new File("targets/unit-test-wd/CachingAuthenticationServiceInvalidLoginTests");

    private static final File PASSWD_FILE = new File(workingDirectory, "passwd");

    private static final String PASSWD_FILENAME = PASSWD_FILE.getPath();

    private static final long CACHE_TIME_NO_REVAL_MILLIS = 1000L;

    private static final long CACHE_TIME_MILLIS = 2000L;

    private static final String user = "User";

    private static final String firstName = "First Name";

    private static final String lastName = "Last Name";

    private static final String email = "e@mail";

    private static final String invalidPassword = "passw0rd";

    private static final String validPassword = "s@crit";

    private static final long timeStart = 450L;

    private static final long timeCache = 500L;

    private static final long time2 = 550L;

    private static final long time2Cache = 600L;

    private static final long time3 = 650L;

    private static final long time4 = 700L;

    private static final long time5 = 800L;

    private Mockery context;

    private IAuthenticationService delegateService;

    private ITimeProvider timeProvider;

    private CachingAuthenticationService service;

    private CachingAuthenticationService.RevalidationRunnable revalidator;

    @BeforeClass
    public void setUp()
    {
        LogInitializer.init();
        FileUtilities.deleteRecursively(workingDirectory);
        workingDirectory.mkdirs();
        context = new Mockery();
        delegateService = context.mock(IAuthenticationService.class);
        timeProvider = context.mock(ITimeProvider.class);
        service =
                new CachingAuthenticationService(delegateService,
                        CachingAuthenticationService.createUserStore(PASSWD_FILENAME),
                        CACHE_TIME_NO_REVAL_MILLIS, CACHE_TIME_MILLIS, false, timeProvider);
        revalidator = service.new RevalidationRunnable();
    }

    @AfterMethod
    public void tearDown()
    {
        context.assertIsSatisfied();
    }

    @AfterClass
    public void cleanUp()
    {
        FileUtilities.deleteRecursively(workingDirectory);
    }

    @Test
    public void testAuthenticateUserFirstTimeUserDoesntExist()
    {
        PASSWD_FILE.delete();
        context.checking(new Expectations()
            {
                {
                    one(timeProvider).getTimeInMilliseconds();
                    will(returnValue(timeStart));
                    one(delegateService).tryGetAndAuthenticateUser(user, invalidPassword);
                    will(returnValue(null));
                }
            });
        final Principal p = service.tryGetAndAuthenticateUser(user, invalidPassword);
        assertNull(p);
        checkCacheEmpty();
        assertTrue(service.getValidationQueue().isEmpty());
        context.assertIsSatisfied();
    }

    // Note: subsequent test methods depend on testAuthenticateUserFirstTimeUserDoesntExist() not
    // being called after this method, so we force a dependency.
    @Test(dependsOnMethods = "testAuthenticateUserFirstTimeUserDoesntExist")
    public void testAuthenticateUserFirstTimeInvalidPassword()
    {
        PASSWD_FILE.delete();
        context.checking(new Expectations()
            {
                {
                    one(timeProvider).getTimeInMilliseconds();
                    will(returnValue(timeStart));
                    one(delegateService).tryGetAndAuthenticateUser(user, invalidPassword);
                    will(returnValue(new Principal(user, firstName, lastName, email, false)));
                    one(timeProvider).getTimeInMilliseconds();
                    will(returnValue(timeCache));
                }
            });
        final Principal p = service.tryGetAndAuthenticateUser(user, invalidPassword);
        assertNotNull(p);
        checkPrincipalUser(user, firstName, lastName, email, false, p);
        checkCache(user, firstName, lastName, email, timeCache, false);
        assertTrue(service.getValidationQueue().isEmpty());
        context.assertIsSatisfied();
    }

    @Test(dependsOnMethods = "testAuthenticateUserFirstTimeInvalidPassword")
    public void testGetPrincipalAfterCacheWithoutPassword()
    {
        context.checking(new Expectations()
            {
                {
                    one(timeProvider).getTimeInMilliseconds();
                    will(returnValue(time2));
                }
            });
        final Principal p = service.tryGetAndAuthenticateUser(user, null);
        assertNotNull(p);
        checkPrincipalUser(user, firstName, lastName, email, false, p);
        checkCache(user, firstName, lastName, email, timeCache, false);
        assertTrue(service.getValidationQueue().isEmpty());
        context.assertIsSatisfied();
    }

    @Test(dependsOnMethods = "testGetPrincipalAfterCacheWithoutPassword")
    public void testAuthenticateUserSecondTimeValidPassword()
    {
        context.checking(new Expectations()
            {
                {
                    one(timeProvider).getTimeInMilliseconds();
                    will(returnValue(time3));
                    // Cannot use the cached entry because it doesn't have a valid password hash.
                    one(delegateService).tryGetAndAuthenticateUser(user, validPassword);
                    will(returnValue(new Principal(user, firstName, lastName, email, true)));
                    one(timeProvider).getTimeInMilliseconds();
                    will(returnValue(time2Cache));
                }
            });
        final Principal p = service.tryGetAndAuthenticateUser(user, validPassword);
        assertNotNull(p);
        checkPrincipalUser(user, firstName, lastName, email, true, p);
        checkCache(user, firstName, lastName, email, time2Cache, true);
        assertTrue(service.getValidationQueue().isEmpty());
        context.assertIsSatisfied();
    }

    @Test(dependsOnMethods = "testGetPrincipalAfterCacheWithoutPassword")
    public void testAuthenticateUserThirdTimeInvalidPassword() throws InterruptedException
    {
        context.checking(new Expectations()
            {
                {
                    one(timeProvider).getTimeInMilliseconds();
                    will(returnValue(time4));
                }
            });
        final Principal p = service.tryGetAndAuthenticateUser(user, invalidPassword);
        assertNotNull(p);
        checkPrincipalUser(user, firstName, lastName, email, false, p);
        checkCache(user, firstName, lastName, email, time2Cache, true);
        assertFalse(service.getValidationQueue().isEmpty());

        // Run validator with request, but the password is invalid so the result will not be cached.
        context.checking(new Expectations()
            {
                {
                    one(delegateService).tryGetAndAuthenticateUser(user, invalidPassword);
                    will(returnValue(new Principal(user, firstName, lastName, email, false)));
                }
            });
        revalidator.runOnce();
        checkCache(user, firstName, lastName, email, time2Cache, true);

        // A valid call is still served from the cache.
        context.checking(new Expectations()
            {
                {
                    one(timeProvider).getTimeInMilliseconds();
                    will(returnValue(time5));
                }
            });
        final Principal p2 = service.tryGetAndAuthenticateUser(user, validPassword);
        assertNotNull(p2);
        checkPrincipalUser(user, firstName, lastName, email, true, p2);
        checkCache(user, firstName, lastName, email, time2Cache, true);

        context.assertIsSatisfied();
    }

    @SuppressWarnings("hiding")
    private void checkPrincipalUser(final String user, final String firstName,
            final String lastName, final String email, boolean authenticated, Principal p)
    {
        assertNotNull(p);
        assertEquals(user, p.getUserId());
        assertEquals(firstName, p.getFirstName());
        assertEquals(lastName, p.getLastName());
        assertEquals(email, p.getEmail());
        assertEquals(authenticated, p.isAuthenticated());
    }

    private void checkCacheEmpty()
    {
        assertTrue(PASSWD_FILE.length() == 0);
    }

    @SuppressWarnings("hiding")
    private void checkCache(final String user, final String firstName,
            final String lastName, final String email, final long timeCache, final boolean withHash)
    {
        assertTrue(PASSWD_FILE.exists());
        assertTrue(PASSWD_FILE.length() > 0);
        final List<String> passwdLines = FileUtilities.loadToStringList(PASSWD_FILE, null);
        assertEquals(1, passwdLines.size());
        final String[] split = StringUtils.splitPreserveAllTokens(passwdLines.get(0), ':');
        assertEquals(UserCacheEntry.NUMBER_OF_COLUMNS_IN_PASSWORD_CACHE_FILE, split.length);
        assertEquals(user, split[0]);
        assertEquals(email, split[1]);
        assertEquals(firstName, split[2]);
        assertEquals(lastName, split[3]);
        if (withHash)
        {
            assertTrue(split[4], Pattern.matches("[\\w\\/,\\+]+", split[4]));
        } else
        {
            assertEquals("", split[4]);
        }
        assertEquals(Long.toString(timeCache), split[5]);
    }
}

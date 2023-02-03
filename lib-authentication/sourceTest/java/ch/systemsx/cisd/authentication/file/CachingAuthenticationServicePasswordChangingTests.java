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

import static org.testng.AssertJUnit.*;

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
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.test.LogMonitoringAppender;
import ch.systemsx.cisd.common.utilities.ITimeProvider;

/**
 * Test cases for the {@link CachingAuthenticationService} with changing data
 * 
 * @author Bernd Rinn
 */
public class CachingAuthenticationServicePasswordChangingTests
{
    private static final File workingDirectory =
            new File("targets/unit-test-wd/CachingAuthenticationServicePasswordChangingTests");

    private static final File PASSWD_FILE = new File(workingDirectory, "passwd");

    private static final String PASSWD_FILENAME = PASSWD_FILE.getPath();

    private static final long CACHE_TIME_NO_REVAL_MILLIS = 1000L;

    private static final long CACHE_TIME_MILLIS = 2000L;

    private static final String user = "User";

    private static final String firstName = "First Name";

    private static final String lastName = "Last Name";

    private static final String email = "e@mail";

    private static final String password = "passw0rd";

    private static final String password2 = "newPass";

    private static final String password3 = "s@cret4u";

    private static final long timeStart = 450L;

    private static final long timeCache = 500L;

    private static final long time2 = 550L;

    private static final long time2Cache = 700L;

    private static final long time3 = 900L;

    private static final long time4 = 1800L;

    private static final long time5 = 2000L;

    private static final long time3Cache = 2100L;

    private static final long time6 = 3500L;

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
    public void testAuthenticateUserFirstTimeSuccess()
    {
        PASSWD_FILE.delete();
        context.checking(new Expectations()
            {
                {
                    one(timeProvider).getTimeInMilliseconds();
                    will(returnValue(timeStart));
                    one(delegateService).tryGetAndAuthenticateUser(user, password);
                    will(returnValue(new Principal(user, firstName, lastName, email, true)));
                    one(timeProvider).getTimeInMilliseconds();
                    will(returnValue(timeCache));
                }
            });
        final Principal p = service.tryGetAndAuthenticateUser(user, password);
        checkPrincipalUser(user, firstName, lastName, email, true, p);
        checkCache(user, firstName, lastName, email, timeCache, true);
        assertTrue(service.getValidationQueue().isEmpty());
        context.assertIsSatisfied();
    }

    @Test(dependsOnMethods = "testAuthenticateUserFirstTimeSuccess")
    public void testAuthenticateUserFromCacheStatusOKNewPasswordFailure() throws InterruptedException
    {
        // Now it has to be answered from the cache.
        context.checking(new Expectations()
            {
                {
                    one(timeProvider).getTimeInMilliseconds();
                    will(returnValue(time2));
                }
            });
        final Principal p = service.tryGetAndAuthenticateUser(user, password2);
        assertNotNull(p);
        checkPrincipalUser(user, firstName, lastName, email, false, p);
        checkCache(user, firstName, lastName, email, timeCache, true);
        assertFalse(service.getValidationQueue().isEmpty());

        // Perform revalidation
        context.checking(new Expectations()
            {
                {
                    one(delegateService).tryGetAndAuthenticateUser(user, password2);
                    will(returnValue(new Principal(user, firstName, lastName, email, true)));
                    one(timeProvider).getTimeInMilliseconds();
                    will(returnValue(time2Cache));
                }
            });
        revalidator.runOnce();
        assertTrue(service.getValidationQueue().isEmpty());
        checkCache(user, firstName, lastName, email, time2Cache, true);

        context.assertIsSatisfied();
    }

    @Test(dependsOnMethods = "testAuthenticateUserFromCacheStatusOKNewPasswordFailure")
    public void testAuthenticateUserFromCacheStatusOKNewPasswordSuccess() throws InterruptedException
    {
        // Now it has to be answered from the cache.
        context.checking(new Expectations()
            {
                {
                    one(timeProvider).getTimeInMilliseconds();
                    will(returnValue(time3));
                }
            });
        final Principal p = service.tryGetAndAuthenticateUser(user, password2);
        assertNotNull(p);
        checkPrincipalUser(user, firstName, lastName, email, true, p);
        checkCache(user, firstName, lastName, email, time2Cache, true);
        assertTrue(service.getValidationQueue().isEmpty());

        context.assertIsSatisfied();
    }

    @Test(dependsOnMethods = "testAuthenticateUserFromCacheStatusOKNewPasswordSuccess")
    public void testAuthenticateUserFromCacheStatusOKRevalOldPasswordSuccessRevalFails() throws InterruptedException
    {
        // Now it has to be answered from the cache, triggering re-validation.
        context.checking(new Expectations()
            {
                {
                    one(timeProvider).getTimeInMilliseconds();
                    will(returnValue(time4));
                }
            });
        final Principal p = service.tryGetAndAuthenticateUser(user, password2);
        assertNotNull(p);
        checkPrincipalUser(user, firstName, lastName, email, true, p);
        checkCache(user, firstName, lastName, email, time2Cache, true);
        assertFalse(service.getValidationQueue().isEmpty());

        // Perform revalidation
        context.checking(new Expectations()
            {
                {
                    one(delegateService).tryGetAndAuthenticateUser(user, password2);
                    will(returnValue(new Principal(user, firstName, lastName, email, false)));
                }
            });
        final LogMonitoringAppender appender =
                LogMonitoringAppender.addAppender(LogCategory.OPERATION,
                        "User 'User' has been logged in with an outdated password.");
        try
        {
            revalidator.runOnce();
        } finally
        {
            LogMonitoringAppender.removeAppender(appender);
        }
        assertTrue(service.getValidationQueue().isEmpty());
        checkCacheEmpty();
        appender.verifyLogHasHappened();

        context.assertIsSatisfied();
    }

    @Test(dependsOnMethods = "testAuthenticateUserFromCacheStatusOKRevalOldPasswordSuccessRevalFails")
    public void testAuthenticateUserFreshAgainSuccess()
    {
        context.checking(new Expectations()
            {
                {
                    one(timeProvider).getTimeInMilliseconds();
                    will(returnValue(time5));
                    one(delegateService).tryGetAndAuthenticateUser(user, password3);
                    will(returnValue(new Principal(user, firstName, lastName, email, true)));
                    one(timeProvider).getTimeInMilliseconds();
                    will(returnValue(time3Cache));
                }
            });
        final Principal p = service.tryGetAndAuthenticateUser(user, password3);
        checkPrincipalUser(user, firstName, lastName, email, true, p);
        checkCache(user, firstName, lastName, email, time3Cache, true);
        assertTrue(service.getValidationQueue().isEmpty());
        context.assertIsSatisfied();
    }

    @Test(dependsOnMethods = "testAuthenticateUserFreshAgainSuccess")
    public void testAuthenticateUserFromCacheStatusOKRevalUserRemoved() throws InterruptedException
    {
        // Now it has to be answered from the cache, triggering re-validation.
        context.checking(new Expectations()
            {
                {
                    one(timeProvider).getTimeInMilliseconds();
                    will(returnValue(time6));
                }
            });
        final Principal p = service.tryGetAndAuthenticateUser(user, password3);
        assertNotNull(p);
        checkPrincipalUser(user, firstName, lastName, email, true, p);
        checkCache(user, firstName, lastName, email, time3Cache, true);
        assertFalse(service.getValidationQueue().isEmpty());

        // Perform revalidation
        context.checking(new Expectations()
            {
                {
                    one(delegateService).tryGetAndAuthenticateUser(user, password3);
                    will(returnValue(null));
                }
            });
        final LogMonitoringAppender appender =
                LogMonitoringAppender.addAppender(LogCategory.OPERATION,
                        "User 'User' has been logged in which is no longer a valid user.");
        try
        {
            revalidator.runOnce();
        } finally
        {
            LogMonitoringAppender.removeAppender(appender);
        }
        assertTrue(service.getValidationQueue().isEmpty());
        checkCacheEmpty();
        appender.verifyLogHasHappened();

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

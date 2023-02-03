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
 * Test cases for the {@link CachingAuthenticationService} with successful logins.
 * 
 * @author Bernd Rinn
 */
public class CachingAuthenticationServiceSuccessTests
{
    private static final File workingDirectory =
            new File("targets/unit-test-wd/CachingAuthenticationServiceSuccessTests");

    private static final File PASSWD_FILE = new File(workingDirectory, "passwd");

    private static final String PASSWD_FILENAME = PASSWD_FILE.getPath();

    private static final long CACHE_TIME_NO_REVAL_MILLIS = 1000L;

    private static final long CACHE_TIME_MILLIS = 2000L;

    private static final String user = "User";

    private static final String firstName = "First Name";

    private static final String lastName = "Last Name";

    private static final String email = "e@mail";

    private static final String email2 = "e@mail2";

    private static final String email3 = "e@mail3";

    private static final String password = "passw0rd";

    private static final long timeStart = 450L;

    private static final long timeCache = 500L;

    private static final long timeSecondRequest = 800L;

    private static final long timeThirdRequest = 1200L;

    private static final long timeForthRequest = 1750L; // Triggers re-validation

    private static final long time2Cache = 1800L; // In re-validation

    private static final long timeFifthRequest = 1900L; // That's after re-validation from the
                                                        // cache.

    private static final long timeSixthRequest = 9950L; // Triggers expiration

    private static final long time3Cache = 10000L; // New cache after expiration

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
        checkPrincipalHappyCase(user, firstName, lastName, email, p);
        checkCacheHappyCase(user, firstName, lastName, email, timeCache);
        assertTrue(service.getValidationQueue().isEmpty());
        context.assertIsSatisfied();
    }

    @Test(dependsOnMethods = "testAuthenticateUserFirstTimeSuccess")
    public void testAuthenticateUserFromCacheStatusOKSuccess()
    {
        // Now it has to be answered from the cache.
        context.checking(new Expectations()
            {
                {
                    one(timeProvider).getTimeInMilliseconds();
                    will(returnValue(timeSecondRequest));
                }
            });
        final Principal p = service.tryGetAndAuthenticateUser(user, password);
        checkPrincipalHappyCase(user, firstName, lastName, email, p);
        checkCacheHappyCase(user, firstName, lastName, email, timeCache);
        assertTrue(service.getValidationQueue().isEmpty());
        context.assertIsSatisfied();
    }

    @Test(dependsOnMethods = "testAuthenticateUserFromCacheStatusOKSuccess")
    public void testAuthenticateUserFromCacheByEmailStatusOKSuccess()
    {
        // Now it has to be answered from the cache.
        context.checking(new Expectations()
            {
                {
                    one(timeProvider).getTimeInMilliseconds();
                    will(returnValue(timeThirdRequest));
                }
            });
        final Principal p = service.tryGetAndAuthenticateUserByEmail(email.toUpperCase(), password);
        checkPrincipalHappyCase(user, firstName, lastName, email, p);
        checkCacheHappyCase(user, firstName, lastName, email, timeCache);
        assertTrue(service.getValidationQueue().isEmpty());
        context.assertIsSatisfied();
    }

    @Test(dependsOnMethods = "testAuthenticateUserFromCacheByEmailStatusOKSuccess")
    public void testAuthenticateUserFromCacheStatusOKRevalSuccess() throws InterruptedException
    {
        // Now it has to be answered from the cache, but re-validation is required.
        context.checking(new Expectations()
            {
                {
                    one(timeProvider).getTimeInMilliseconds();
                    will(returnValue(timeForthRequest));
                }
            });
        final Principal p = service.tryGetAndAuthenticateUser(user, password);
        checkPrincipalHappyCase(user, firstName, lastName, email, p);
        checkCacheHappyCase(user, firstName, lastName, email, timeCache);
        assertFalse(service.getValidationQueue().isEmpty());

        // Perform revalidation
        context.checking(new Expectations()
            {
                {
                    one(delegateService).tryGetAndAuthenticateUser(user, password);
                    will(returnValue(new Principal(user, firstName, lastName, email2, true)));
                    one(timeProvider).getTimeInMilliseconds();
                    will(returnValue(time2Cache));
                }
            });
        revalidator.runOnce();
        assertTrue(service.getValidationQueue().isEmpty());
        checkCacheHappyCase(user, firstName, lastName, email2, time2Cache);

        // Now it has to be answered from the cache again.
        context.checking(new Expectations()
            {
                {
                    one(timeProvider).getTimeInMilliseconds();
                    will(returnValue(timeFifthRequest));
                }
            });
        final Principal p2 =
                service.tryGetAndAuthenticateUserByEmail(email2.toUpperCase(), password);
        checkPrincipalHappyCase(user, firstName, lastName, email2, p2);
        checkCacheHappyCase(user, firstName, lastName, email2, time2Cache);
        assertTrue(service.getValidationQueue().isEmpty());

        // Check that email is gone from the cache.
        context.checking(new Expectations()
            {
                {
                    one(timeProvider).getTimeInMilliseconds();
                    will(returnValue(timeFifthRequest));
                    one(delegateService).tryGetAndAuthenticateUserByEmail(email.toUpperCase(),
                            password);
                    will(returnValue(null));
                }
            });
        final Principal p3 =
                service.tryGetAndAuthenticateUserByEmail(email.toUpperCase(), password);
        assertNull(p3);
        context.assertIsSatisfied();
    }

    @Test(dependsOnMethods = "testAuthenticateUserFromCacheStatusOKRevalSuccess")
    public void testAuthenticateUserAfterCacheExpiratonSuccess()
    {
        context.checking(new Expectations()
            {
                {
                    one(timeProvider).getTimeInMilliseconds();
                    will(returnValue(timeSixthRequest));
                    one(delegateService).tryGetAndAuthenticateUser(user, password);
                    will(returnValue(new Principal(user, firstName, lastName, email3, true)));
                    one(timeProvider).getTimeInMilliseconds();
                    will(returnValue(time3Cache));
                }
            });
        final Principal p = service.tryGetAndAuthenticateUser(user, password);
        checkPrincipalHappyCase(user, firstName, lastName, email3, p);
        checkCacheHappyCase(user, firstName, lastName, email3, time3Cache);
        assertTrue(service.getValidationQueue().isEmpty());
        context.assertIsSatisfied();
    }

    @SuppressWarnings("hiding")
    private void checkPrincipalHappyCase(final String user, final String firstName,
            final String lastName, final String email, Principal p)
    {
        assertNotNull(p);
        assertEquals(user, p.getUserId());
        assertEquals(firstName, p.getFirstName());
        assertEquals(lastName, p.getLastName());
        assertEquals(email, p.getEmail());
        assertTrue(p.isAuthenticated());
    }

    @SuppressWarnings("hiding")
    private void checkCacheHappyCase(final String user, final String firstName,
            final String lastName,
            final String email, final long timeCache)
    {
        assertTrue(PASSWD_FILE.exists());
        assertTrue(PASSWD_FILE.length() > 0);
        final List<String> passwdLines = FileUtilities.loadToStringList(PASSWD_FILE, null);
        assertEquals(1, passwdLines.size());
        final String[] split = StringUtils.split(passwdLines.get(0), ':');
        assertEquals(UserCacheEntry.NUMBER_OF_COLUMNS_IN_PASSWORD_CACHE_FILE, split.length);
        assertEquals(user, split[0]);
        assertEquals(email, split[1]);
        assertEquals(firstName, split[2]);
        assertEquals(lastName, split[3]);
        assertTrue(split[4], Pattern.matches("[\\w\\/,\\+]+", split[4]));
        assertEquals(Long.toString(timeCache), split[5]);
    }
}

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

package ch.systemsx.cisd.common.filesystem;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.regex.Pattern;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.StatusWithResult;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.test.LogMonitoringAppender;
import ch.systemsx.cisd.common.utilities.ITimeProvider;

/**
 * Test cases for the quiet period file filter.
 * 
 * @author Bernd Rinn
 */
@Friend(toClasses = QuietPeriodFileFilter.class)
public class QuietPeriodFileFilterTest
{
    private final static String PATH_NAME = "testPath";

    private final static StoreItem ITEM = new StoreItem(PATH_NAME);

    private final static long QUIET_PERIOD_MILLIS = 100L;

    private Mockery context;

    private ITimeProvider timeProvider;

    private ILastModificationChecker fileStore;

    private QuietPeriodFileFilter filterUnderTest;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        timeProvider = context.mock(ITimeProvider.class);
        fileStore = context.mock(ILastModificationChecker.class);
        filterUnderTest =
                new QuietPeriodFileFilter(fileStore, QUIET_PERIOD_MILLIS, timeProvider, 3);
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testGetTimeFormat()
    {
        assertEquals("%0$tY-%0$tm-%0$td %0$tH:%0$tM:%0$tS", QuietPeriodFileFilter.getTimeFormat(0));
        assertEquals("%17$tY-%17$tm-%17$td %17$tH:%17$tM:%17$tS", QuietPeriodFileFilter
                .getTimeFormat(17));
    }

    @Test
    public void testGetClockProblemLogMessage()
    {
        final String expected =
                "Last modification time of path 'bla' jumped back: check at 1970-01-01 01:00:01 -> "
                        + "last modification time 1970-01-01 01:00:50, check at later time 1970-01-01 01:00:10 -> "
                        + "last modification time 1970-01-01 01:00:30 (which is 20000 ms younger)";
        assertEquals(expected, QuietPeriodFileFilter.getClockProblemLogMessage("bla", 1000L,
                50000L, 10000L, 30000L));
    }

    @Test
    public void testInitialAcceptCall()
    {
        // 1000 - 500 > 100, but the initial call will never accept
        final long nowMillis = 1000L;
        final long pathLastChangedMillis = 500L;
        context.checking(new Expectations()
            {
                {
                    prepareLastChanged(nowMillis, 0L, pathLastChangedMillis);
                }
            });
        assertNoAccept();
        context.assertIsSatisfied();
    }

    @Test
    public void testTwoAcceptCallsUntilAccept()
    {
        final long nowMillis1 = 1000L;
        final long nowMillis2 = 1100L;
        final long pathLastChangedMillis = 500L;
        context.checking(new Expectations()
            {
                {
                    // first call
                    prepareLastChanged(nowMillis1, 0L, pathLastChangedMillis);
                    // second call
                    prepareLastChanged(nowMillis2, pathLastChangedMillis, pathLastChangedMillis);
                }
            });
        assertNoAccept();
        assertFilterAccepts();
        context.assertIsSatisfied();
    }

    @Test
    public void testFrequentButUselessCallsUntilAccept()
    {
        final long nowMillis = 1000L;
        final long pathLastChangedMillis = 500L;
        context.checking(new Expectations()
            {
                {
                    // first call
                    one(timeProvider).getTimeInMilliseconds();
                    will(returnValue(nowMillis));
                    one(fileStore).lastChanged(ITEM, 0L);
                    will(returnValue(StatusWithResult.<Long> create(pathLastChangedMillis)));
                    for (int i = 1; i <= 100; ++i)
                    {
                        one(timeProvider).getTimeInMilliseconds();
                        will(returnValue(nowMillis + i));
                    }
                    // last call - will check only when last check is longer ago than the quiet
                    // period
                    one(fileStore).lastChanged(ITEM, pathLastChangedMillis);
                    will(returnValue(StatusWithResult.<Long> create(pathLastChangedMillis)));
                }
            });
        for (int i = 0; i < 100; ++i)
        {
            assertNoAccept();
        }
        assertFilterAccepts();
        context.assertIsSatisfied();
    }

    @Test
    public void testThreeAcceptCallsUntilAccept()
    {
        final long nowMillis1 = 1000L;
        final long nowMillis2 = 1100L;
        final long nowMillis3 = 1200L;
        final long pathLastChangedMillis1 = 1000L;
        final long pathLastChangedMillis2 = 1001L;
        context.checking(new Expectations()
            {
                {
                    // first call
                    prepareLastChanged(nowMillis1, 0L, pathLastChangedMillis1);
                    // second call
                    prepareLastChanged(nowMillis2, pathLastChangedMillis1, pathLastChangedMillis2);
                    // third call
                    prepareLastChanged(nowMillis3, pathLastChangedMillis2, pathLastChangedMillis2);
                }
            });
        assertNoAccept();
        assertNoAccept();
        assertFilterAccepts();
        context.assertIsSatisfied();
    }

    @Test
    public void testThreeAcceptCallsUntilAcceptClockProblem()
    {
        final long nowMillis1 = 1000L;
        final long nowMillis2 = 1100L;
        final long nowMillis3 = 1200L;
        final long pathLastChangedMillis1 = 1000L;
        final long pathLastChangedMillis2 = 999L;
        context.checking(new Expectations()
            {
                {
                    // first call
                    prepareLastChanged(nowMillis1, 0L, pathLastChangedMillis1);
                    // second call
                    prepareLastChanged(nowMillis2, pathLastChangedMillis1, pathLastChangedMillis2);
                    // third call
                    prepareLastChanged(nowMillis3, pathLastChangedMillis2, pathLastChangedMillis2);
                }
            });
        final LogMonitoringAppender appender =
                LogMonitoringAppender.addAppender(LogCategory.MACHINE, Pattern
                        .compile("Last modification time of path '.+' jumped back"));
        assertNoAccept();
        assertNoAccept();
        assertFilterAccepts();
        appender.verifyLogHasHappened();
        LogMonitoringAppender.removeAppender(appender);
        context.assertIsSatisfied();
    }

    @Test
    public void testErrorInLastChangeDoesNotAccept()
    {
        // simulate several times an error during acquiring last modification time
        int errorRepetitions = 3;
        long now = 0;
        for (int i = 0; i < errorRepetitions; i++)
        {
            prepareLastChanged(now, 0L, StatusWithResult.<Long> createErrorWithResult());
            now += QUIET_PERIOD_MILLIS;
        }
        for (int i = 0; i < errorRepetitions; i++)
        {
            assertNoAccept();
        }
        // first time we acquire modification time
        final StatusWithResult<Long> lastChange = StatusWithResult.<Long> create(0L);
        prepareLastChanged(now, 0L, lastChange);
        now += QUIET_PERIOD_MILLIS;
        assertNoAccept();
        // error again
        prepareLastChanged(now, 0L, StatusWithResult.<Long> createErrorWithResult());
        now += QUIET_PERIOD_MILLIS;
        assertNoAccept();
        // second time we acquire modification time - and nothing change during the quite period, so
        // accept should succeed
        prepareLastChanged(now, 0L, lastChange);
        now += QUIET_PERIOD_MILLIS;
        assertFilterAccepts();
        context.assertIsSatisfied();
    }

    private void assertNoAccept()
    {
        assertFalse(filterUnderTest.accept(ITEM));
    }

    private void assertFilterAccepts()
    {
        assertTrue(filterUnderTest.accept(ITEM));
    }

    private void prepareLastChanged(final long currentTime, final long stopWhenFindYounger,
            final long lastChanged)
    {
        prepareLastChanged(currentTime, stopWhenFindYounger, StatusWithResult
                .<Long> create(lastChanged));
    }

    private void prepareLastChanged(final long currentTime, final long stopWhenFindYounger,
            final StatusWithResult<Long> lastChanged)
    {
        context.checking(new Expectations()
            {
                {
                    one(timeProvider).getTimeInMilliseconds();
                    will(returnValue(currentTime));
                    one(fileStore).lastChanged(ITEM, stopWhenFindYounger);
                    will(returnValue(lastChanged));
                }
            });
    }

    @Test
    public void testCleanUpVanishedDirectory()
    {
        final long nowMillis1 = 1000L;
        final long nowMillis2 = 10000L;
        final long pathLastChangedMillis1 = 1000L;
        final long pathLastChangedMillis2 = 1000L;
        final String vanishingPathName = "vanished";
        final StoreItem vanishingItem = new StoreItem(vanishingPathName);
        context.checking(new Expectations()
            {
                {
                    // first call for the vanishing file
                    one(timeProvider).getTimeInMilliseconds();
                    will(returnValue(nowMillis1));
                    allowing(fileStore).lastChanged(vanishingItem, 0L);
                    will(returnValue(StatusWithResult.<Long> create(pathLastChangedMillis1)));
                    // calls to get the required number of calls for clean up
                    allowing(timeProvider).getTimeInMilliseconds();
                    will(returnValue(nowMillis2));
                    allowing(fileStore).lastChanged(with(same(ITEM)),
                            with(greaterThanOrEqualTo(0L)));
                    will(returnValue(StatusWithResult.<Long> create(pathLastChangedMillis2)));
                }
            });
        final LogMonitoringAppender appender =
                LogMonitoringAppender.addAppender(LogCategory.OPERATION, "removing from path map");
        assertFalse(filterUnderTest.accept(vanishingItem));
        for (int i = 0; i < QuietPeriodFileFilter.MAX_CALLS_BEFORE_CLEANUP; ++i)
        {
            assertNoAccept();
        }
        appender.verifyLogHasHappened();
        LogMonitoringAppender.removeAppender(appender);
        context.assertIsSatisfied();
    }

}

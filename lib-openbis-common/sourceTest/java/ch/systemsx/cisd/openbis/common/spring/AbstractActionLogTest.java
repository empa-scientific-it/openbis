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

package ch.systemsx.cisd.openbis.common.spring;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.servlet.AbstractActionLog;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.common.test.LogMonitoringAppender;

/**
 * @author Franz-Josef Elmer
 */
public class AbstractActionLogTest
{
    private static final String HOST = "test-host";

    private static final String USER = "Albert Einstein";

    private static final class MockActionLog extends AbstractActionLog
    {
        private final HttpSession session;

        public MockActionLog(IRequestContextProvider requestContextProvider, HttpSession session)
        {
            super(requestContextProvider);
            this.session = session;
        }

        @Override
        protected String getUserCode(HttpSession httpSession)
        {
            AssertJUnit.assertSame(session, httpSession);
            return USER;
        }
    }

    private Mockery context;

    private IRequestContextProvider requestContextProvider;

    private HttpServletRequest request;

    private HttpSession session;

    private MockActionLog actionLog;

    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();
        requestContextProvider = context.mock(IRequestContextProvider.class);
        request = context.mock(HttpServletRequest.class);
        session = context.mock(HttpSession.class);

        context.checking(new Expectations()
            {
                {
                    allowing(requestContextProvider).getHttpServletRequest();
                    will(returnValue(request));

                    allowing(request).getRemoteHost();
                    will(returnValue(HOST));
                }
            });
        actionLog = new MockActionLog(requestContextProvider, session);
    }

    @AfterMethod
    public final void afterMethod()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testLogFailedLoginAttempt()
    {
        String unknownUser = "albert";
        LogMonitoringAppender appender =
                LogMonitoringAppender.addAppender(LogCategory.AUTH, "{USER: " + unknownUser
                        + ", HOST: " + HOST + "} login: FAILED");

        actionLog.logFailedLoginAttempt(unknownUser);

        appender.verifyLogHasHappened();
        context.assertIsSatisfied();
    }

    @Test
    public void testLoginSuccessfully()
    {
        final String sessionID = "session-id";
        context.checking(new Expectations()
            {
                {
                    one(request).getSession();
                    will(returnValue(session));

                    one(session).getId();
                    will(returnValue(sessionID));
                }
            });
        LogMonitoringAppender appender =
                LogMonitoringAppender.addAppender(LogCategory.AUTH, "{USER: " + USER
                        + ", HOST: " + HOST + ", WEBSESSION: " + sessionID + "} login: OK");

        actionLog.logSuccessfulLogin();

        appender.verifyLogHasHappened();
        context.assertIsSatisfied();
    }

    @Test
    public void testLoginSuccessfullyForUnknownSession()
    {
        context.checking(new Expectations()
            {
                {
                    one(request).getSession();
                    will(returnValue(null));
                }
            });
        LogMonitoringAppender appender =
                LogMonitoringAppender.addAppender(LogCategory.AUTH, "{USER: UNKNOWN"
                        + ", HOST: " + HOST + ", WEBSESSION: UNKNOWN} login: OK");

        actionLog.logSuccessfulLogin();

        appender.verifyLogHasHappened();
        context.assertIsSatisfied();
    }

    @Test
    public void testLogLogout()
    {
        final String session2ID = "id2";
        final HttpSession session2 = context.mock(HttpSession.class, "session2");
        context.checking(new Expectations()
            {
                {
                    one(session2).getId();
                    will(returnValue(session2ID));

                    one(session2).getLastAccessedTime();
                    will(returnValue(42L));

                    one(session2).getMaxInactiveInterval();
                    will(returnValue(0));
                }
            });
        LogMonitoringAppender appender =
                LogMonitoringAppender.addAppender(LogCategory.AUTH, "{USER: " + USER
                        + ", WEBSESSION: " + session2ID + "} logout (session timed out)");

        MockActionLog log = new MockActionLog(requestContextProvider, session2);
        log.logLogout(session2);

        appender.verifyLogHasHappened();
        context.assertIsSatisfied();
    }
}

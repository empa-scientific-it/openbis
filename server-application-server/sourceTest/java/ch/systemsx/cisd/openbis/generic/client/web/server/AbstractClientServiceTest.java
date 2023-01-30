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

package ch.systemsx.cisd.openbis.generic.client.web.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.IClientService;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IResultSetManager;
import ch.systemsx.cisd.openbis.generic.shared.basic.SessionConstants;

/**
 * An <i>abstract</i> test class for all {@link IClientService} implementations.
 * 
 * @author Christian Ribeaud
 */
public abstract class AbstractClientServiceTest extends AssertJUnit
{
    protected static final String SESSION_TOKEN = "session-token";

    protected Mockery context;

    protected IRequestContextProvider requestContextProvider;

    protected HttpServletRequest servletRequest;

    protected HttpSession httpSession;

    protected IResultSetManager<String> resultSetManager;

    protected final void prepareGetHttpSession(final Expectations expectations)
    {
        expectations.allowing(requestContextProvider).getHttpServletRequest();
        expectations.will(Expectations.returnValue(servletRequest));

        expectations.allowing(servletRequest).getSession(false);
        expectations.will(Expectations.returnValue(httpSession));
    }

    protected final void prepareGetResultSetManager(final Expectations expectations)
    {
        expectations.allowing(httpSession)
                .getAttribute(SessionConstants.OPENBIS_RESULT_SET_MANAGER);
        expectations.will(Expectations.returnValue(resultSetManager));
    }

    protected final void prepareGetSessionToken(final Expectations expectations)
    {
        prepareGetHttpSession(expectations);

        expectations.allowing(httpSession).getAttribute(
                SessionConstants.OPENBIS_SESSION_TOKEN_ATTRIBUTE_KEY);
        expectations.will(Expectations.returnValue(SESSION_TOKEN));
    }

    @BeforeMethod
    @SuppressWarnings("unchecked")
    public void setUp()
    {
        context = new Mockery();
        requestContextProvider = context.mock(IRequestContextProvider.class);
        servletRequest = context.mock(HttpServletRequest.class);
        httpSession = context.mock(HttpSession.class);
        resultSetManager = context.mock(IResultSetManager.class);
    }

    @AfterMethod
    public void tearDown()
    {
        // The following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }
}

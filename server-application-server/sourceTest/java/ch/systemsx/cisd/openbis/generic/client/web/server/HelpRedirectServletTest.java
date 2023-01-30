/*
 * Copyright 2009 ETH Zuerich, CISD
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
import javax.servlet.http.HttpServletResponse;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.client.web.client.IOnlineHelpResourceLocatorService;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;

/**
 * @author Chandrasekhar Ramakrishnan
 */
@Friend(toClasses = HelpRedirectServlet.class)
public class HelpRedirectServletTest
{

    static final String SESSION_TOKEN = "session-token";

    static final String GENERIC_ROOT_URL = "https://wiki-bsse.ethz.ch/display/CISDDoc/OnlineHelp";

    static final String SPECIFIC_ROOT_URL = "https://irgendwo.li/display/OurDoc/OnlineHelp";

    static final String GENERIC_CREATE_PAGE_ACTION_TEMPLATE =
            "createpage.action?spaceKey=CISDDoc&title=%s&linkCreation=true&fromPageId=40633829";

    static final String SPECIFIC_CREATE_PAGE_ACTION_TEMPLATE =
            "createpage.action?spaceKey=OurDoc&title=%s&linkCreation=true&fromPageId=40633829";

    static final String GENERIC_PAGE_TEMPLATE =
            "https://wiki-bsse.ethz.ch/pages/" + GENERIC_CREATE_PAGE_ACTION_TEMPLATE;

    static final String SPECIFIC_PAGE_TEMPLATE =
            "https://irgendwo.li/pages/" + GENERIC_CREATE_PAGE_ACTION_TEMPLATE;

    static final String PAGE_TITLE = "Page Title";

    private Mockery context;

    private HttpServletRequest servletRequest;

    private HttpServletResponse servletResponse;

    private IOnlineHelpResourceLocatorService service;

    /**
     * Internal helper method to mock the the IOnlineHelpResourceLocatorService
     */
    private void prepareServiceExpectations(Expectations exps)
    {
        exps.allowing(service).getOnlineHelpGenericRootURL();
        exps.will(Expectations.returnValue(GENERIC_ROOT_URL));
        exps.allowing(service).getOnlineHelpGenericPageTemplate();
        exps.will(Expectations.returnValue(String.format(GENERIC_PAGE_TEMPLATE, "${title}")));
        exps.allowing(service).getOnlineHelpSpecificRootURL();
        exps.will(Expectations.returnValue(SPECIFIC_ROOT_URL));
        exps.allowing(service).getOnlineHelpSpecificPageTemplate();
        exps.will(Expectations.returnValue(String.format(SPECIFIC_PAGE_TEMPLATE, "${title}")));
    }

    @BeforeMethod
    public final void setUp()
    {
        context = new Mockery();
        servletRequest = context.mock(HttpServletRequest.class);
        servletResponse = context.mock(HttpServletResponse.class);
        service = context.mock(IOnlineHelpResourceLocatorService.class);
    }

    @AfterMethod
    public final void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public final void testGenericHelpTitle() throws Exception
    {
        context.checking(new Expectations()
            {
                {
                    atLeast(1).of(servletRequest).getParameter(
                            GenericConstants.HELP_REDIRECT_PAGE_TITLE_KEY);
                    will(returnValue(PAGE_TITLE));
                }
            });
        String helpPageTitle = createServlet().tryGetHelpPageTitleForRequest(servletRequest);
        AssertJUnit.assertEquals(PAGE_TITLE, helpPageTitle);
        context.assertIsSatisfied();
    }

    @Test
    public final void testGenericHelpTitleWithoutParameters() throws Exception
    {
        context.checking(new Expectations()
            {
                {
                    atLeast(1).of(servletRequest).getParameter(
                            GenericConstants.HELP_REDIRECT_PAGE_TITLE_KEY);
                    will(returnValue(null));
                }
            });
        String helpPageTitle = createServlet().tryGetHelpPageTitleForRequest(servletRequest);
        AssertJUnit.assertEquals(null, helpPageTitle);
        context.assertIsSatisfied();
    }

    @Test
    public final void testGenericHelpPageRequest() throws Exception
    {
        final String urlForRedirect = String.format(GENERIC_PAGE_TEMPLATE, PAGE_TITLE);

        context.checking(new Expectations()
            {
                {
                    prepareServiceExpectations(this);
                    atLeast(1).of(servletRequest).getParameter(
                            GenericConstants.HELP_REDIRECT_PAGE_TITLE_KEY);
                    will(returnValue(PAGE_TITLE));
                    atLeast(1).of(servletRequest).getParameter(
                            GenericConstants.HELP_REDIRECT_SPECIFIC_KEY);
                    will(returnValue(null));
                    oneOf(servletResponse).sendRedirect(with(equal(urlForRedirect)));
                }
            });
        createServlet().handleRequestInternal(servletRequest, servletResponse);
        context.assertIsSatisfied();
    }

    @Test
    public final void testSpecificHelpPageRequest() throws Exception
    {
        context.checking(new Expectations()
            {
                {
                    prepareServiceExpectations(this);
                    atLeast(1).of(servletRequest).getParameter(
                            GenericConstants.HELP_REDIRECT_PAGE_TITLE_KEY);
                    will(returnValue(PAGE_TITLE));
                    atLeast(1).of(servletRequest).getParameter(
                            GenericConstants.HELP_REDIRECT_SPECIFIC_KEY);
                    will(returnValue("TRUE"));
                    oneOf(servletResponse).sendRedirect(
                            with(equal(String.format(SPECIFIC_PAGE_TEMPLATE, PAGE_TITLE))));
                }
            });
        createServlet().handleRequestInternal(servletRequest, servletResponse);
        context.assertIsSatisfied();
    }

    @Test
    public final void testSpecificHelpRootRequest() throws Exception
    {
        context.checking(new Expectations()
            {
                {
                    prepareServiceExpectations(this);
                    atLeast(1).of(servletRequest).getParameter(
                            GenericConstants.HELP_REDIRECT_PAGE_TITLE_KEY);
                    will(returnValue(null));
                    atLeast(1).of(servletRequest).getParameter(
                            GenericConstants.HELP_REDIRECT_SPECIFIC_KEY);
                    will(returnValue("true"));
                    oneOf(servletResponse).sendRedirect(with(equal(SPECIFIC_ROOT_URL)));
                }
            });
        createServlet().handleRequestInternal(servletRequest, servletResponse);
        context.assertIsSatisfied();
    }

    private final HelpRedirectServlet createServlet()
    {
        return new HelpRedirectServlet(service);
    }
}

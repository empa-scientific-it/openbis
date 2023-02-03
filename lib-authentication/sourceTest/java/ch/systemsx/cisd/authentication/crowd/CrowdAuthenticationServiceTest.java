/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.authentication.crowd;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.base.utilities.OSUtilities;
import ch.systemsx.cisd.common.logging.BufferedAppender;

/**
 * Tests for {@link CrowdAuthenticationService}.
 * 
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = CrowdAuthenticationService.class)
public class CrowdAuthenticationServiceTest
{

    private static final String URL = "url";

    private static final String APPLICATION = "appli & cation";

    private static final String APPLICATION_ESCAPED = "appli &amp; cation";

    private static final String APPLICATION_PASSWORD = "<password>";

    private static final String APPLICATION_PASSWORD_ESCAPED = "&lt;password&gt;";

    private static final String APPLICATION_TOKEN_ESACPED = "application&lt;&amp;&gt;token";

    private static final String USER = "<user>";

    private static final String USER_ESCAPED = "&lt;user&gt;";

    private static final String USER_PASSWORD = "pass\"word\"";

    private static final String USER_PASSWORD_ESCAPED = "pass&quot;word&quot;";

    private Mockery context;

    private IRequestExecutor executor;

    private IAuthenticationService authenticationService;

    private BufferedAppender logRecorder;

    @BeforeMethod
    public void setup()
    {
        context = new Mockery();
        executor = context.mock(IRequestExecutor.class);
        authenticationService =
                new CrowdAuthenticationService(URL, APPLICATION, APPLICATION_PASSWORD, executor);
        logRecorder = new BufferedAppender("%-5p %c - %m%n", Level.DEBUG);
    }

    @AfterMethod
    public void tearDown()
    {
        logRecorder.reset();
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testSuccessfullUserAuthentication()
    {
        context.checking(new Expectations()
            {
                {
                    Object[] parameters = new Object[]
                    { APPLICATION_ESCAPED, APPLICATION_PASSWORD_ESCAPED };
                    String message =
                            CrowdAuthenticationService.AUTHENTICATE_APPL.format(parameters);
                    one(executor).execute(URL, message);
                    will(returnValue(createXMLElement(CrowdSoapElements.TOKEN,
                            APPLICATION_TOKEN_ESACPED)));

                    parameters =
                            new Object[]
                            { APPLICATION_ESCAPED, APPLICATION_TOKEN_ESACPED, USER_ESCAPED,
                                    USER_PASSWORD_ESCAPED };
                    message = CrowdAuthenticationService.AUTHENTICATE_USER.format(parameters);
                    one(executor).execute(URL, message);
                    will(returnValue(createXMLElement("n:" + CrowdSoapElements.OUT,
                            APPLICATION_TOKEN_ESACPED)));
                }
            });
        final boolean result =
                authenticationService.authenticateUser(USER, USER_PASSWORD);
        assertEquals(true, result);
        assertEquals(createAuthenticatedSuccessfullyLog()
                + OSUtilities.LINE_SEPARATOR
                + createInfoLogEntry("CROWD: authentication of user '" + USER + "', application '"
                        + APPLICATION + "': SUCCESS."), logRecorder.getLogContent());

        context.assertIsSatisfied();
    }

    @Test
    public void testFailedUserAuthentication()
    {
        context.checking(new Expectations()
            {
                {
                    Object[] parameters = new Object[]
                    { APPLICATION_ESCAPED, APPLICATION_PASSWORD_ESCAPED };
                    String message =
                            CrowdAuthenticationService.AUTHENTICATE_APPL.format(parameters);
                    one(executor).execute(URL, message);
                    will(returnValue(createXMLElement(CrowdSoapElements.TOKEN,
                            APPLICATION_TOKEN_ESACPED)));

                    parameters =
                            new Object[]
                            { APPLICATION_ESCAPED, APPLICATION_TOKEN_ESACPED, USER_ESCAPED,
                                    USER_PASSWORD_ESCAPED };
                    message = CrowdAuthenticationService.AUTHENTICATE_USER.format(parameters);
                    one(executor).execute(URL, message);
                    will(returnValue("error"));
                }
            });
        final boolean result =
                authenticationService.authenticateUser(USER, USER_PASSWORD);
        assertEquals(false, result);
        assertEquals(createAuthenticatedSuccessfullyLog()
                + OSUtilities.LINE_SEPARATOR
                + createDebugLogEntry("Element '" + CrowdSoapElements.OUT
                        + "' could not be found in 'error'.")
                + OSUtilities.LINE_SEPARATOR
                + createInfoLogEntry("CROWD: authentication of user '" + USER + "', application '"
                        + APPLICATION + "': FAILED."), logRecorder.getLogContent());

        context.assertIsSatisfied();
    }

    @Test
    public void testSuccessfullPrincipalRetrieval()
    {
        context.checking(new Expectations()
            {
                {
                    Object[] parameters = new Object[]
                    { APPLICATION_ESCAPED, APPLICATION_PASSWORD_ESCAPED };
                    String message =
                            CrowdAuthenticationService.AUTHENTICATE_APPL.format(parameters);
                    one(executor).execute(URL, message);
                    will(returnValue(createXMLElement(CrowdSoapElements.TOKEN,
                            APPLICATION_TOKEN_ESACPED)));

                    parameters = new Object[]
                    { APPLICATION_ESCAPED, APPLICATION_TOKEN_ESACPED, USER_ESCAPED };
                    message = CrowdAuthenticationService.FIND_PRINCIPAL_BY_NAME.format(parameters);
                    one(executor).execute(URL, message);
                    String element = createSOAPAttribute("sn", "Stepka");
                    element += createSOAPAttribute("invalidPasswordAttempts", "0");
                    element += createSOAPAttribute("requiresPasswordChange", "false");
                    element += createSOAPAttribute("mail", "justen.stepka@atlassian.com");
                    element += createSOAPAttribute("lastAuthenticated", "1169440408520");
                    element += createSOAPAttribute("givenName", "Justen");
                    element += createSOAPAttribute("passwordLastChanged", "1168995491407");
                    will(returnValue("<a>" + element + "</a>"));
                }

                private String createSOAPAttribute(final String name, final String value)
                {
                    return "<SOAPAttribute><name>" + name
                            + "</name><values><ns1:string xmlns:ns1=\"urn:SecurityServer\">"
                            + value + "</ns1:string></values></SOAPAttribute>";
                }
            });
        final Principal result = authenticationService.getPrincipal(USER);
        assertEquals("Justen", result.getFirstName());
        assertEquals("Stepka", result.getLastName());
        assertEquals("justen.stepka@atlassian.com", result.getEmail());
        assertEquals("0", result.getProperty("invalidPasswordAttempts"));
        assertEquals("false", result.getProperty("requiresPasswordChange"));
        assertEquals("1169440408520", result.getProperty("lastAuthenticated"));
        assertEquals("1168995491407", result.getProperty("passwordLastChanged"));
        assertEquals(createAuthenticatedSuccessfullyLog(), logRecorder.getLogContent());
        context.assertIsSatisfied();
    }

    @Test
    public void testFailedPrincipalRetrieval()
    {
        context.checking(new Expectations()
            {
                {
                    Object[] parameters = new Object[]
                    { APPLICATION_ESCAPED, APPLICATION_PASSWORD_ESCAPED };
                    String message =
                            CrowdAuthenticationService.AUTHENTICATE_APPL.format(parameters);
                    one(executor).execute(URL, message);
                    will(returnValue(createXMLElement(CrowdSoapElements.TOKEN,
                            APPLICATION_TOKEN_ESACPED)));

                    parameters = new Object[]
                    { APPLICATION_ESCAPED, APPLICATION_TOKEN_ESACPED, USER_ESCAPED };
                    message = CrowdAuthenticationService.FIND_PRINCIPAL_BY_NAME.format(parameters);
                    one(executor).execute(URL, message);
                    will(returnValue("<a></a>"));
                }
            });
        try
        {
            authenticationService.getPrincipal(USER);
            fail("EnvironmentFailureException expected");
        } catch (final IllegalArgumentException e)
        {
            assertEquals("Cannot find user '" + USER + "'.", e.getMessage());
        }

        assertEquals(
                createAuthenticatedSuccessfullyLog()
                        + OSUtilities.LINE_SEPARATOR
                        + createDebugLogEntry("No SOAPAttribute element could be found in the SOAP XML response."),
                logRecorder.getLogContent());

        context.assertIsSatisfied();
    }

    private String createAuthenticatedSuccessfullyLog()
    {
        final String authenticateLogs =
                createInfoLogEntry("CROWD: Attempting to authenticate as application "
                        + APPLICATION + "...")
                        + OSUtilities.LINE_SEPARATOR
                        + createInfoLogEntry("CROWD: application '" + APPLICATION
                                + "' successfully authenticated.");
        return authenticateLogs;
    }

    private String createDebugLogEntry(final String message)
    {
        return createLogEntry("DEBUG", message);
    }

    private String createInfoLogEntry(final String message)
    {
        return createLogEntry("INFO ", message);
    }

    private String createLogEntry(final String level, final String message)
    {
        return level + " OPERATION." + authenticationService.getClass().getSimpleName() + " - "
                + message;
    }

    private String createXMLElement(final String element, final String content)
    {
        return "<" + element + ">" + content + "</" + element + ">";
    }
}

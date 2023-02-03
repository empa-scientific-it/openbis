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

import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.http.JettyHttpClientFactory;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * This <code>IAuthenticationService</code> implementation first registers the application on the <i>Crowd</i> server, then authenticates the user.
 * <p>
 * The modus operandi is based on information found at <a href="http://confluence.atlassian.com/display/CROWD/SOAP+API"
 * >http://confluence.atlassian.com/display/CROWD/SOAP+API</a>
 * </p>
 * 
 * @author Franz-Josef Elmer
 */
public class CrowdAuthenticationService implements IAuthenticationService
{
    private static final int DEFAULT_CONNECTION_TIMEOUT_MILLIS =
            (int) (5 * DateUtils.MILLIS_PER_MINUTE);

    private static final String DUMMY_TOKEN_STR = "DUMMY-TOKEN";

    private static final String EMAIL_PROPERTY_KEY = "mail";

    private static final String LAST_NAME_PROPERTY_KEY = "sn";

    private static final String FIRST_NAME_PROPERTY_KEY = "givenName";

    private static final String ERROR_MSG_WITH_INVALID_APPLICATION_TOKEN =
            "The application.name or application.password in the crowd.properties file does not match the password in Crowd.";

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, CrowdAuthenticationService.class);

    /** The template to authenticate the application. */
    @Private
    static final MessageFormat AUTHENTICATE_APPL =
            new MessageFormat(
                    "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" "
                            + "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" "
                            + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
                            + " <soap:Body>\n"
                            + "   <authenticateApplication xmlns=\"urn:SecurityServer\">\n"
                            + "     <in0>\n"
                            + "       <credential xmlns=\"http://authentication.integration.crowd.atlassian.com\">\n"
                            + "         <credential>{1}</credential>\n"
                            + "       </credential>\n"
                            + "       <name xmlns=\"http://authentication.integration.crowd.atlassian.com\">{0}</name>\n"
                            + "       <validationFactors xmlns=\"http://authentication.integration.crowd.atlassian.com\" "
                            + "                          xsi:nil=\"true\" />\n" + "     </in0>\n"
                            + "   </authenticateApplication>\n" + " </soap:Body>\n"
                            + "</soap:Envelope>\n");

    /** The template to authenticate the user. */
    @Private
    static final MessageFormat AUTHENTICATE_USER =
            new MessageFormat(
                    "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" "
                            + "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" "
                            + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
                            + " <soap:Body>\n"
                            + "   <authenticatePrincipal xmlns=\"urn:SecurityServer\">\n"
                            + "     <in0>\n"
                            + "       <name xmlns=\"http://authentication.integration.crowd.atlassian.com\">{0}</name>\n"
                            + "       <token xmlns=\"http://authentication.integration.crowd.atlassian.com\">{1}</token>\n"
                            + "     </in0>\n"
                            + "     <in1>\n"
                            + "       <application xmlns=\"http://authentication.integration.crowd.atlassian.com\">{0}</application>\n"
                            + "       <credential xmlns=\"http://authentication.integration.crowd.atlassian.com\">\n"
                            + "         <credential>{3}</credential>\n"
                            + "       </credential>\n"
                            + "       <name xmlns=\"http://authentication.integration.crowd.atlassian.com\">{2}</name>\n"
                            + "       <validationFactors xmlns=\"http://authentication.integration.crowd.atlassian.com\" />\n"
                            + "     </in1>\n" + "   </authenticatePrincipal>\n" + " </soap:Body>\n"
                            + "</soap:Envelope>\n");

    /** The template to find a principal by token or by name. */
    @Private
    static final MessageFormat FIND_PRINCIPAL_BY_NAME =
            new MessageFormat(
                    "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
                            + "   <soap:Body>\n"
                            + "       <findPrincipalByName xmlns=\"urn:SecurityServer\">\n"
                            + "           <in0>\n"
                            + "               <name xmlns=\"http://authentication.integration.crowd.atlassian.com\">{0}</name>\n"
                            + "               <token xmlns=\"http://authentication.integration.crowd.atlassian.com\">{1}</token>\n"
                            + "           </in0>\n"
                            + "           <in1>{2}</in1>\n"
                            + "       </findPrincipalByName>\n"
                            + "   </soap:Body>\n"
                            + "</soap:Envelope>\n");

    private static IRequestExecutor createExecutor(final int timeoutMillis)
    {
        return new IRequestExecutor()
            {
                /**
                 * Makes a POST request with "application/soap+xml" as content type.
                 * 
                 * @return The server's response to the request.
                 */
                @Override
                public String execute(final String serviceUrl, final String message)
                {
                    try
                    {
                        Request request = JettyHttpClientFactory.getHttpClient().newRequest(serviceUrl).method(HttpMethod.POST);
                        request.timeout(timeoutMillis, TimeUnit.MILLISECONDS);
                        request.content(new StringContentProvider(message), "application/soap+xml");
                        ContentResponse response = request.send();
                        return response.getContentAsString();
                    } catch (final Exception ex)
                    {
                        throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                    }
                }

            };
    }

    private final String url;

    private final String application;

    private final String applicationPassword;

    private final boolean configured;

    private final IRequestExecutor requestExecutor;

    private final AtomicReference<String> applicationTokenHolder = new AtomicReference<String>();

    public CrowdAuthenticationService(CrowdConfiguration configuration)
    {
        this.url = configuration.getServerURL();
        this.application = configuration.getApplication();
        this.applicationPassword = configuration.getApplicationPassword();
        this.configured = configuration.isConfigured();
        this.requestExecutor = createExecutor(configuration.getTimeout());
        if (operationLog.isDebugEnabled())
        {
            final String msg =
                    "A new CrowdAuthenticationService instance has been created for [" + "url="
                            + url + ", application=" + application + "], timeout: "
                            + ((configuration.getTimeout() == 0) ? "-." : (configuration.getTimeout()
                                    / 1000 + " s."));
            operationLog.debug(msg);
        }
    }

    // Keep this constructor for backward compatibility with old Spring application context files.
    public CrowdAuthenticationService(final String host, final String port,
            final String application, final String applicationPassword)
    {
        this("https://" + host + ":" + checkPort(port) + "/crowd/services/SecurityServer",
                application, applicationPassword, createExecutor(DEFAULT_CONNECTION_TIMEOUT_MILLIS));
    }

    CrowdAuthenticationService(final String url, final String application,
            final String applicationPassword, final IRequestExecutor requestExecutor)
    {
        this.url = url;
        this.application = application;
        this.applicationPassword = applicationPassword;
        this.requestExecutor = requestExecutor;
        this.configured = true;
        if (operationLog.isDebugEnabled())
        {
            final String msg =
                    "A new CrowdAuthenticationService instance has been created for [" + "url="
                            + url + ", application=" + application + "]";
            operationLog.debug(msg);
        }
    }

    private static String checkPort(String portStr) throws ConfigurationFailureException
    {
        try
        {
            // '${' means we have an unresolved Spring variable
            if (portStr != null && portStr.startsWith("${") == false)
            {
                if (Integer.parseInt(portStr) <= 0)
                {
                    throw ConfigurationFailureException.fromTemplate("Illegal port '%s'", portStr);
                }
            }
        } catch (NumberFormatException ex)
        {
            throw ConfigurationFailureException.fromTemplate("Illegal port '%s'", portStr);
        }
        return portStr;
    }

    //
    // ISelfTestable
    //

    @Override
    public boolean isRemote()
    {
        return true;
    }

    @Override
    public final void check() throws EnvironmentFailureException, ConfigurationFailureException
    {
        try
        {
            final String xmlResponse = executeAuthenticateApplication();
            final String applicationToken =
                    StringEscapeUtils.unescapeXml(pickElementContent(xmlResponse,
                            CrowdSoapElements.TOKEN));
            applicationTokenHolder.set(applicationToken);
            if (applicationToken == null)
            {
                throw new EnvironmentFailureException("Application '" + application
                        + "' couldn't be authenticated: " + xmlResponse);
            }
        } catch (final EnvironmentFailureException ex)
        {
            throw ex;
        } catch (final CheckedExceptionTunnel ex)
        {
            throw new EnvironmentFailureException(ex.getMessage(), CheckedExceptionTunnel
                    .unwrapIfNecessary(ex));
        } catch (final RuntimeException ex)
        {
            throw new EnvironmentFailureException(ex.getMessage(), ex);
        }
    }

    //
    // IAuthenticationService
    //

    @Override
    public final String authenticateApplication()
    {
        return DUMMY_TOKEN_STR;
    }

    @Override
    public final boolean authenticateUser(final String dummyToken, final String user,
            final String password)
    {
        return authenticateUser(user, password);
    }

    @Override
    public final boolean authenticateUser(final String user, final String password)
    {
        assert user != null;

        String userToken = null;
        while (true)
        {
            final String xmlResponse =
                    execute(AUTHENTICATE_USER, application, getApplicationToken(false), user,
                            password);
            userToken = extractUserToken(xmlResponse, user);
            if (userToken == null)
            {
                if (isApplicationNotAuthenticated(xmlResponse))
                {
                    if (getApplicationToken(true) == null)
                    {
                        operationLog.error("Cannot authenticate user '" + user
                                + "' because authentication of the application with the "
                                + "CROWD service failed.");
                        break;
                    }
                } else
                {
                    // The application is authenticated but the user credentials are not right.
                    break;
                }
            } else
            {
                // Everything is fine.
                break;
            }
        }
        logAuthentication(user, userToken != null);
        return userToken != null;
    }

    private void logAuthentication(final String user, final boolean authenticated)
    {
        if (operationLog.isInfoEnabled())
        {
            final String msg =
                    "CROWD: authentication of user '" + user + "', application '" + application
                            + "': ";
            operationLog.info(msg + (authenticated ? "SUCCESS." : "FAILED."));
        }
    }

    private String executeAuthenticateApplication()
    {
        operationLog
                .info("CROWD: Attempting to authenticate as application " + application + "...");
        return execute(AUTHENTICATE_APPL, application, applicationPassword);
    }

    private String getApplicationToken(boolean forceNewToken)
    {
        String applicationToken = applicationTokenHolder.get();
        if (applicationToken == null || forceNewToken)
        {
            final String xmlResponse = executeAuthenticateApplication();
            applicationToken =
                    StringEscapeUtils.unescapeXml(pickElementContent(xmlResponse,
                            CrowdSoapElements.TOKEN));
            if (applicationToken == null)
            {
                operationLog.error("CROWD: application '" + application
                        + "' failed to authenticate.");
            } else
            {
                operationLog.info("CROWD: application '" + application
                        + "' successfully authenticated.");
            }
            applicationTokenHolder.set(applicationToken);
        }
        return applicationToken;
    }

    private boolean isApplicationNotAuthenticated(final String xmlResponse)
    {
        return xmlResponse.indexOf(ERROR_MSG_WITH_INVALID_APPLICATION_TOKEN) >= 0;
    }

    private final String extractUserToken(String xmlResponse, String user)
    {
        final String userToken =
                StringEscapeUtils
                        .unescapeXml(pickElementContent(xmlResponse, CrowdSoapElements.OUT));
        return userToken;

    }

    @Override
    public Principal tryGetAndAuthenticateUser(String dummyToken, String user, String passwordOrNull)
    {
        return tryGetAndAuthenticateUser(user, passwordOrNull);
    }

    @Override
    public Principal tryGetAndAuthenticateUser(String user, String passwordOrNull)
    {
        String xmlResponse = null;
        try
        {
            Principal principal = null;
            while (true)
            {
                xmlResponse =
                        execute(FIND_PRINCIPAL_BY_NAME, application, getApplicationToken(false),
                                user);
                final Map<String, String> parseXmlResponse = parseXmlResponse(xmlResponse);
                if (parseXmlResponse.size() >= 1)
                {
                    principal = createPrincipal(user, parseXmlResponse);
                } else
                {
                    if (isApplicationNotAuthenticated(xmlResponse))
                    {
                        if (getApplicationToken(true) == null)
                        {
                            if (passwordOrNull != null)
                            {
                                operationLog.error("Cannot authenticate user '" + user
                                        + "' because authentication of the application with the "
                                        + "CROWD service failed.");
                            } else
                            {
                                operationLog.error("Cannot obtain details for user '" + user
                                        + "' because authentication of the application with the "
                                        + "CROWD service failed.");
                            }
                            break;
                        }
                    } else
                    {
                        // The application is authenticated, but the principal does not exist.
                        if (operationLog.isDebugEnabled())
                        {
                            operationLog
                                    .debug("No SOAPAttribute element could be found in the SOAP XML response.");
                        }
                        break;
                    }
                }
                if (principal != null && passwordOrNull != null)
                {
                    principal.setAuthenticated(authenticateUser(getApplicationToken(false), user,
                            passwordOrNull));
                }
                if (principal != null)
                {
                    break;
                }
            }
            if (passwordOrNull != null)
            {
                logAuthentication(user, Principal.isAuthenticated(principal));
            }
            return principal;
        } catch (final Exception ex) // SAXException, IOException
        {
            final String message =
                    "Parsing XML response '" + xmlResponse + "' throws an Exception.";
            throw new EnvironmentFailureException(message, ex);
        }
    }

    @Override
    public final Principal getPrincipal(final String applicationToken, final String user)
    {
        return getPrincipal(user);
    }

    @Override
    public final Principal getPrincipal(final String user)
    {
        final Principal principalOrNull = tryGetAndAuthenticateUser(user, null);
        if (principalOrNull == null)
        {
            throw new IllegalArgumentException("Cannot find user '" + user + "'.");
        }
        return principalOrNull;
    }

    /**
     * Parses given <i>Crowd</i> XML response and returns a map of found <code>SOAPAttribute</code> s.
     * <p>
     * Never returns <code>null</code> but could returns an empty <code>Map</code>.
     * </p>
     */
    private final static Map<String, String> parseXmlResponse(final String xmlResponse)
            throws SAXException, IOException
    {
        final XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        final SOAPAttributeContentHandler contentHandler = new SOAPAttributeContentHandler();
        xmlReader.setContentHandler(contentHandler);
        final StringReader stringReader = new StringReader(xmlResponse);
        xmlReader.parse(new InputSource(stringReader));
        stringReader.close();
        return contentHandler.getSoapAttributes();
    }

    /** Creates a <code>Principal</code> with found SOAP attributes. */
    private final static Principal createPrincipal(final String user,
            final Map<String, String> soapAttributes)
    {
        final String firstName = soapAttributes.get(FIRST_NAME_PROPERTY_KEY);
        final String lastName = soapAttributes.get(LAST_NAME_PROPERTY_KEY);
        final String email = soapAttributes.get(EMAIL_PROPERTY_KEY);
        return new Principal(user, firstName, lastName, email, false, soapAttributes);
    }

    /**
     * Constructs the POST message and does the HTTP request.
     * 
     * @return The <var>responseElement</var> in the server's response.
     */
    private final String execute(final MessageFormat template, final String... args)
    {
        final Object[] decodedArguments = new Object[args.length];
        for (int i = 0; i < args.length; i++)
        {
            decodedArguments[i] = StringEscapeUtils.escapeXml(args[i]);
        }
        return requestExecutor.execute(url, template.format(decodedArguments));
    }

    /**
     * Tries to find given <code>element</code> in <code>xmlString</code>.
     * <p>
     * Note that this is a special-purpose method not suitable for putting it into general utility classes. For example it does not find empty
     * elements.
     * 
     * @return The requested element, or <code>null</code> if it could not be found.
     */
    private static final String pickElementContent(final String xmlString, final String element)
    {
        if (xmlString == null)
        {
            operationLog
                    .error("Response of web service is invalid (null). We were looking for element '"
                            + element + "'.");
            return null;
        }

        int index = getIndex(xmlString, "<", element, 0);
        if (index < 0)
        {
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug("Element '" + element + "' could not be found in '"
                        + StringUtils.abbreviate(xmlString, 50) + "'.");
            }
            return null;
        }
        index = xmlString.indexOf(">", index);
        if (index < 0)
        {
            operationLog.error("Element '" + element
                    + "' seems to be present but XML is invalid: '"
                    + StringUtils.abbreviate(xmlString, 50) + "'.");
            return null;
        }
        final int endIndex = getIndex(xmlString, "</", element + ">", index);
        if (endIndex < 0)
        {
            operationLog.error("Start tag of element '" + element
                    + "' is present but end tag is missing: '"
                    + StringUtils.abbreviate(xmlString, 50) + "'.");
            return null;
        }
        return xmlString.substring(index + 1, endIndex);
    }

    private static int getIndex(final String xmlString, final String begin, final String element,
            final int startIndex)
    {
        final String regex = ".*(" + begin + "(\\w*:)?" + element + ").*";
        final Pattern p = Pattern.compile(regex);
        final Matcher matcher = p.matcher(xmlString);
        final boolean result = matcher.matches();
        if (result == false)
        {
            return -1;
        }
        final int index = matcher.start(1);
        return index;
    }

    @Override
    public List<Principal> listPrincipalsByEmail(String emailQuery)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Principal> listPrincipalsByEmail(String applicationToken, String emailQuery)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Principal tryGetAndAuthenticateUserByEmail(String email, String passwordOrNull)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Principal tryGetAndAuthenticateUserByEmail(String applicationToken, String email,
            String passwordOrNull)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Principal> listPrincipalsByLastName(String lastNameQuery)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Principal> listPrincipalsByLastName(String applicationToken, String lastNameQuery)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Principal> listPrincipalsByUserId(String userIdQuery)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Principal> listPrincipalsByUserId(String applicationToken, String userIdQuery)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsListingByEmail()
    {
        return false;
    }

    @Override
    public boolean supportsListingByLastName()
    {
        return false;
    }

    @Override
    public boolean supportsListingByUserId()
    {
        return false;
    }

    @Override
    public boolean supportsAuthenticatingByEmail()
    {
        return false;
    }

    @Override
    public boolean isConfigured()
    {
        return configured;
    }

}

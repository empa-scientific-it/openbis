/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.authentication.ldap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.concurrent.FailureRecord;
import ch.systemsx.cisd.common.concurrent.ITaskExecutor;
import ch.systemsx.cisd.common.concurrent.ParallelizedExecutor;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.InvalidAuthenticationException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.ISelfTestable;

/**
 * A class for querying an LDAP server for principals.
 * 
 * @author Bernd Rinn
 */
public final class LDAPPrincipalQuery implements ISelfTestable
{
    private static final String DISTINGUISHED_NAME_ATTRIBUTE_NAME = "distinguishedName";

    private static final String UID_NUMBER_ATTRIBUTE_NAME = "uidNumber";

    private static final String LOGIN_DN_MSG_TEMPLATE = "User '%s' <DN='%s'>: authentication %s";

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, LDAPPrincipalQuery.class);

    private static final String LDAP_CONTEXT_FACTORY_CLASSNAME = "com.sun.jndi.ldap.LdapCtxFactory";

    private static final String LDAP_CONTEXT_READ_TIMEOUT = "com.sun.jndi.ldap.read.timeout";

    private static final String LDAP_CONTEXT_CONNECT_TIMEOUT = "com.sun.jndi.ldap.connect.timeout";

    private static final String AUTHENTICATION_FAILURE_TEMPLATE =
            "Authentication failure connecting to LDAP server '%s'.";

    private static final String LDAP_ERROR_TEMPLATE = "Error connecting to LDAP server '%s'.";

    private final LDAPDirectoryConfiguration config;

    private final ThreadLocal<DirContext> contextHolder;

    public LDAPPrincipalQuery(LDAPDirectoryConfiguration config)
    {
        this.config = config;
        this.contextHolder = new ThreadLocal<DirContext>();
    }

    public Principal tryGetPrincipal(String userId) throws IllegalArgumentException
    {
        final List<Principal> principals = listPrincipalsByUserId(userId, 1);
        return tryGetPrincipal(principals, "User '%s' is not unique.", userId);
    }

    public Principal tryGetPrincipalByEmail(String email)
    {
        final List<Principal> principals = listPrincipalsByEmail(email, 1);
        return tryGetPrincipal(principals, "Email '%s' is not unique.", email);
    }

    private Principal tryGetPrincipal(final List<Principal> principals, final String msgTemplate,
            final String user)
    {
        if (principals.size() == 0)
        {
            return null;
        } else if (principals.size() == 1)
        {
            return principals.get(0);
        } else
        {
            // Cannot happen - we have limited the search to 1
            throw new IllegalArgumentException(String.format(msgTemplate, user));
        }
    }

    public List<Principal> listPrincipalsByUserId(String userId)
    {
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("listPrincipalsByUserId(%s)", userId));
        }
        return listPrincipalsByKeyValue(config.getUserIdAttributeName(), userId, null,
                Integer.MAX_VALUE);
    }

    private List<Principal> listPrincipalsByUserId(String userId, int limit)
    {
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("listPrincipalsByUserId(%s,%s)", userId, limit));
        }
        return listPrincipalsByKeyValue(config.getUserIdAttributeName(), userId, null, limit);
    }

    public List<Principal> listPrincipalsByEmail(String email, int limit)
    {
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("listPrincipalsByEmail(%s,%s)", email, limit));
        }
        return listPrincipalsByKeyValue(getEmailAttributeForQueries(),
                getEmailKeyForQueries(email), null, limit);
    }

    private String getEmailAttributeForQueries()
    {
        if (Boolean.parseBoolean(config.getQueryEmailForAliases()))
        {
            return config.getEmailAliasesAttributeName();
        } else
        {
            return config.getEmailAttributeName();
        }
    }

    private String getEmailKeyForQueries(String emailQuery)
    {
        if (Boolean.parseBoolean(config.getQueryEmailForAliases()))
        {
            return config.getEmailAttributePrefix() + emailQuery;
        } else
        {
            return emailQuery;
        }
    }

    public List<Principal> listPrincipalsByEmail(String email)
    {
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("listPrincipalsByEmail(%s)", email));
        }
        return listPrincipalsByKeyValue(getEmailAttributeForQueries(),
                getEmailKeyForQueries(email), null, Integer.MAX_VALUE);
    }

    public List<Principal> listPrincipalsByLastName(String lastName, int limit)
    {
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("listPrincipalsByLastName(%s,%s)", lastName, limit));
        }
        return listPrincipalsByKeyValue(config.getLastNameAttributeName(), lastName, null, limit);
    }

    public List<Principal> listPrincipalsByLastName(String lastName)
    {
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("listPrincipalsByLastName(%s)", lastName));
        }
        return listPrincipalsByKeyValue(config.getLastNameAttributeName(), lastName, null,
                Integer.MAX_VALUE);
    }

    public boolean authenticateUser(String userId, String password)
    {
        final Principal principal = tryGetAndAuthenticatePrincipal(userId, password);
        return (principal == null) ? false : principal.isAuthenticated();
    }

    public Principal tryGetAndAuthenticatePrincipal(String userId, String passwordOrNull)
    {
        final Principal principal = tryGetPrincipal(userId);
        if (principal == null)
        {
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug(String.format("User '%s' not found in LDAP directory.", userId));
            }
            return null;
        }
        authenticatePrincipal(principal, passwordOrNull);
        return principal;
    }

    public Principal tryGetAndAuthenticatePrincipalByEmail(String email, String passwordOrNull)
    {
        final Principal principal = tryGetPrincipalByEmail(email);
        if (principal == null)
        {
            return null;
        }
        authenticatePrincipal(principal, passwordOrNull);
        return principal;
    }

    private void authenticatePrincipal(final Principal principal, String passwordOrNull)
    {
        final String distinguishedName = principal.getProperty(DISTINGUISHED_NAME_ATTRIBUTE_NAME);
        final boolean authenticated =
                (passwordOrNull == null) ? false
                        : authenticateUserByDistinguishedName(
                                distinguishedName, passwordOrNull);
        principal.setAuthenticated(authenticated);
        if (operationLog.isDebugEnabled() && passwordOrNull != null)
        {
            operationLog.debug(String.format(LOGIN_DN_MSG_TEMPLATE, principal.getUserId(),
                    distinguishedName, getStatus(authenticated)));
        }
    }

    private String getStatus(final boolean status)
    {
        return status ? "OK" : "FAILURE";
    }

    private boolean authenticateUserByDistinguishedName(String dn, String password)
    {
        try
        {
            createContextForDistinguishedName(dn, password, false, true);
            return true;
        } catch (InvalidAuthenticationException ex)
        {
            return false;
        } catch (RuntimeException ex)
        {
            operationLog.error(
                    String.format("Error on creating context to authenticate dn=<%s>: %s", dn, ex));
            throw ex;
        }
    }

    public List<Principal> listPrincipalsByKeyValueOrQuery(String keyOrNull, String valueOrNull, String queryOrNull)
    {
        return listPrincipalsByKeyValueOrQuery(keyOrNull, valueOrNull, queryOrNull, null, Integer.MAX_VALUE);
    }

    public List<Principal> listPrincipalsByKeyValue(String keyOrNull, String valueOrNull, 
            Collection<String> additionalAttributesOrNull, int limit)
    {
        return listPrincipalsByKeyValueOrQuery(keyOrNull, valueOrNull, null, additionalAttributesOrNull, limit);
    }

    /**
     * Returns a list of principals matching a search query given as <var>keyOrNull</var> and <var>valueOrNull</var> 
     * where value may contain <code>*</code> as a wildcard character. 
     * The actual LDAP query is created from the queryTemplate of the configuration where '%' is replaced by 
     * <code>&lt;keyOrNull&gt;=&lt;valueOrNull&gt;</code>. 
     * If <var>queryOrNull</var> is directly specified <var>keyOrNull</var> and <var>valueOrNull</var> will be ignored.
     * 
     * @param keyOrNull The key to search for, e.g. <code>uid</code>
     * @param valueOrNull The value to query for, e.g. <code>may*</code>
     * @param queryOrNull The complete LDAP query. It will be used if not <code>null</code>.
     * @param additionalAttributesOrNull If not <code>null</code>, include the attributes with the given attribute names. If
     *            <var>additionalAttributesOrNull</var> is an empty collection, include all properties that the principal has in LDAP
     * @param limit The limit of users to return at most. Note that limiting the search to 1 gives usually a big performance boost.
     * @return The list of principals matching the query
     */
    @SuppressWarnings("null")
    private List<Principal> listPrincipalsByKeyValueOrQuery(String keyOrNull, String valueOrNull, String queryOrNull, 
            Collection<String> additionalAttributesOrNull, int limit)
    {
        String query = queryOrNull != null ? queryOrNull
                : String.format(config.getQueryTemplate(), String.format("%s=%s", keyOrNull, valueOrNull));
        RuntimeException firstException = null;
        for (int i = 0; i <= config.getMaxRetries(); ++i)
        {
            try
            {
                return primListPrinciplasByQuery(config.getSearchBase(), query, additionalAttributesOrNull, limit);
            } catch (RuntimeException ex)
            {
                contextHolder.set(null);
                if (firstException == null)
                {
                    firstException = ex;
                    if (operationLog.isDebugEnabled() && i < config.getMaxRetries())
                    {
                        operationLog.debug(String.format(
                                "Error listing principle by query '%s', retrying...", query), ex);
                    }
                }
                if (i < config.getMaxRetries())
                {
                    ConcurrencyUtilities.sleep(config.getTimeToWaitAfterFailure());
                }
            }
        }
        operationLog.error(String.format("Error on LDAP query '%s'", query), firstException);
        throw firstException;
    }

    private List<Principal> primListPrinciplasByQuery(String searchBase, final String query, Collection<String> additionalAttributesOrNull, int limit)
    {
        final List<Principal> principals = new ArrayList<Principal>();
        try
        {
            final DirContext context = createContext(false);
            final SearchControls ctrl = new SearchControls();
            ctrl.setSearchScope(SearchControls.SUBTREE_SCOPE);
            final NamingEnumeration<SearchResult> enumeration = context.search(searchBase, query, ctrl);
            int count = 0;
            while (count++ < limit && enumeration.hasMore())
            {
                final SearchResult result = enumeration.next();
                final Attributes attributes = result.getAttributes();
                final String userId = tryGetAttribute(attributes, config.getUserIdAttributeName());
                final String email = tryGetAttribute(attributes, config.getEmailAttributeName());
                final String distinguishedName = result.getNameInNamespace();
                if (userId != null && email != null && distinguishedName != null)
                {
                    final String firstName =
                            tryGetAttribute(attributes, config.getFirstNameAttributeName(), "?");
                    final String lastName =
                            tryGetAttribute(attributes, config.getLastNameAttributeName(), "?");
                    final String uidNumber = tryGetAttribute(attributes, UID_NUMBER_ATTRIBUTE_NAME);
                    final Principal principal =
                            new Principal(userId, firstName, lastName, email, false);
                    principal.getProperties().put(DISTINGUISHED_NAME_ATTRIBUTE_NAME,
                            distinguishedName);
                    if (uidNumber != null)
                    {
                        principal.getProperties().put(UID_NUMBER_ATTRIBUTE_NAME, uidNumber);
                    }
                    if (additionalAttributesOrNull != null)
                    {
                        if (additionalAttributesOrNull.isEmpty())
                        {
                            principal.setProperties(getAllAttributes(attributes));
                        } else
                        {
                            for (String attributeName : additionalAttributesOrNull)
                            {
                                final String attributeValue =
                                        tryGetAttribute(attributes, attributeName);
                                if (attributeValue != null)
                                {
                                    principal.getProperties().put(attributeName, attributeValue);
                                }
                            }
                        }
                    }
                    principals.add(principal);
                }
            }
            return principals;
        } catch (AuthenticationException ex)
        {
            throw ConfigurationFailureException.fromTemplate(ex, AUTHENTICATION_FAILURE_TEMPLATE,
                    config.getServerUrl());
        } catch (NamingException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private DirContext createContext(boolean retry)
    {
        return createContextForDistinguishedName(config.getSecurityPrincipalDistinguishedName(),
                config.getSecurityPrincipalPassword(), true, retry);
    }

    private DirContext createContextForDistinguishedName(String dn, String password, boolean useThreadContext, boolean retry)
    {
        final DirContext threadContext = useThreadContext ? contextHolder.get() : null;
        if (threadContext != null)
        {
            return threadContext;
        }
        if (password != null && password.isEmpty())
        {
            throw new RuntimeException("Try to login user '" + dn + "' with empty password.");
        }
        String serverUrl = config.getServerUrl();
        String[] urls = serverUrl.split(" ");
        ResultHolder<DirContext> resultHolder = new ResultHolder<DirContext>();
        Set<Thread> workerThreads = Collections.synchronizedSet(new HashSet<>());
        ITaskExecutor<String> taskExecutor = createTask(dn, password, resultHolder, workerThreads);
        Collection<FailureRecord<String>> failures = ParallelizedExecutor.process(Arrays.asList(urls), taskExecutor,
                0.5, urls.length, "ldap query", config.getMaxRetries(), false);
        DirContext dirContext = resultHolder.getResult();
        if (dirContext == null)
        {
            if (failures.isEmpty() == false)
            {
                String failureReport = ParallelizedExecutor.tryFailuresToString(failures);
                throw new InvalidAuthenticationException("Failed to create an LDAP dir context: " + failureReport);
            }
            throw new InvalidAuthenticationException("Failed to create an LDAP dir context: no failure report");
        }
        if (useThreadContext)
        {
            contextHolder.set(dirContext);
        }
        return dirContext;
    }

    private ITaskExecutor<String> createTask(String dn, String password, ResultHolder<DirContext> resultHolder, Set<Thread> workerThreads)
    {
        Thread masterThread = Thread.currentThread();
        return new ITaskExecutor<String>()
            {
                private ThreadLocal<AtomicInteger> numberOfTries = new ThreadLocal<>();

                @Override
                public Status execute(String url)
                {
                    workerThreads.add(Thread.currentThread());
                    if (resultHolder.hasResult())
                    {
                        return Status.OK;
                    }
                    getNumberOfTries().getAndIncrement();
                    Hashtable<String, String> env = createEnvironment(url, dn, password);
                    if (operationLog.isDebugEnabled())
                    {
                        operationLog.debug(String.format("Try to login to %s with dn=%s",
                                config.getServerUrl(), dn));
                    }
                    try
                    {
                        resultHolder.setResult(new InitialDirContext(env));
                        interruptWorkers(workerThreads, masterThread);
                        operationLog.info("Dir context successfully created with LDAP server '" + url + "'.");
                        return Status.OK;
                    } catch (Exception ex)
                    {
                        if (resultHolder.hasResult())
                        {
                            return Status.OK;
                        }
                        if (ex instanceof AuthenticationException)
                        {
                            String message = "Failed to authenticate dn=<" + dn + "> at LDAP service '" + url + "'.";
                            operationLog.debug(message, ex);
                            return Status.createError(message);
                        }
                        String message = "Error connecting to LDAP service '" + url
                                + "': cannot open a context for dn=<" + dn + ">";
                        if (getNumberOfTries().get() <= config.getMaxRetries())
                        {
                            operationLog.debug(message, ex);
                            ConcurrencyUtilities.sleep(config.getTimeToWaitAfterFailure());
                        } else
                        {
                            operationLog.error(message, ex);
                        }
                        return Status.createRetriableError(message);
                    }
                }

                private Hashtable<String, String> createEnvironment(String url, String dn, String password)
                {
                    Hashtable<String, String> env = new Hashtable<String, String>();
                    env.put(Context.INITIAL_CONTEXT_FACTORY, LDAP_CONTEXT_FACTORY_CLASSNAME);
                    env.put(Context.PROVIDER_URL, url);
                    env.put(Context.SECURITY_PROTOCOL, config.getSecurityProtocol());
                    env.put(Context.SECURITY_AUTHENTICATION, config.getSecurityAuthenticationMethod());
                    env.put(Context.REFERRAL, config.getReferral());
                    env.put(Context.SECURITY_PRINCIPAL, dn);
                    env.put(Context.SECURITY_CREDENTIALS, password);
                    env.put(LDAP_CONTEXT_READ_TIMEOUT, Long.toString(config.getTimeout()));
                    env.put(LDAP_CONTEXT_CONNECT_TIMEOUT, Long.toString(config.getTimeout()));
                    return env;
                }

                private AtomicInteger getNumberOfTries()
                {
                    AtomicInteger result = numberOfTries.get();
                    if (result == null)
                    {
                        result = new AtomicInteger();
                        numberOfTries.set(result);
                    }
                    return result;
                }

                private void interruptWorkers(Set<Thread> workerThreads, Thread masterThread)
                {
                    synchronized (workerThreads)
                    {
                        for (Iterator<Thread> iterator = workerThreads.iterator(); iterator.hasNext();)
                        {
                            Thread thread = iterator.next();
                            if (thread != masterThread)
                            {
                                thread.interrupt();
                            }
                        }
                    }
                }
            };
    }

    private static final class ResultHolder<T>
    {
        private T result;

        public T getResult()
        {
            return result;
        }

        public synchronized boolean hasResult()
        {
            return result != null;
        }

        public synchronized void setResult(T result)
        {
            this.result = result;
        }
    }

    private static String tryGetAttribute(Attributes attributes, String attributeName)
            throws NamingException
    {
        return tryGetAttribute(attributes, attributeName, null);
    }

    private static String tryGetAttribute(Attributes attributes, String attributeName,
            String defaultValue) throws NamingException
    {
        final BasicAttribute basicAttribute = (BasicAttribute) attributes.get(attributeName);
        if (basicAttribute == null)
        {
            return defaultValue;
        }
        final NamingEnumeration<?> values = basicAttribute.getAll();
        final StringBuilder builder = new StringBuilder();
        while (values.hasMore())
        {
            builder.append(values.next().toString());
            builder.append("::");
        }
        if (builder.length() == 0)
        {
            return defaultValue;
        } else
        {
            builder.setLength(builder.length() - 2);
            return builder.toString();
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> getAllAttributes(Attributes attributes)
            throws NamingException
    {
        final Map<String, String> result = new HashMap<String, String>();
        final NamingEnumeration<BasicAttribute> attributeEnum =
                (NamingEnumeration<BasicAttribute>) attributes.getAll();
        if (attributeEnum == null)
        {
            return result;
        }
        while (attributeEnum.hasMore())
        {
            final BasicAttribute attribute = attributeEnum.next();
            String attributeName = attribute.getID();
            final NamingEnumeration<?> values = attribute.getAll();
            final StringBuilder builder = new StringBuilder();
            while (values.hasMore())
            {
                builder.append(values.next().toString());
                builder.append("::");
            }
            if (builder.length() > 2)
            {
                builder.setLength(builder.length() - 2);
                result.put(attributeName, builder.toString());
            }
        }
        return result;
    }

    //
    // ISelfTestable
    //

    @Override
    public void check() throws EnvironmentFailureException, ConfigurationFailureException
    {
        try
        {
            createContext(true);
        } catch (InvalidAuthenticationException ex)
        {
            throw ConfigurationFailureException.fromTemplate(ex, AUTHENTICATION_FAILURE_TEMPLATE,
                    config.getServerUrl());
        } catch (RuntimeException ex)
        {
            throw EnvironmentFailureException.fromTemplate(
                    CheckedExceptionTunnel.unwrapIfNecessary(ex), LDAP_ERROR_TEMPLATE, config
                            .getServerUrl());
        }
    }

    @Override
    public boolean isRemote()
    {
        return config.getServerUrl().contains("localhost") == false
                && config.getServerUrl().contains("127.0.0.1") == false;
    }

}
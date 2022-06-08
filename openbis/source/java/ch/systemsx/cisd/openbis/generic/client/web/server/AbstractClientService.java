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

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.cache.ICacheManager;
import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.authentication.ISessionActionListener;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;
import ch.systemsx.cisd.openbis.BuildAndEnvironmentInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.IClientService;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientService;
import ch.systemsx.cisd.openbis.generic.client.web.client.IOnlineHelpResourceLocatorService;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ApplicationInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetFetchConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetWithEntityTypes;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SessionContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.User;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.InvalidSessionException;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.CacheManager;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.CacheManager.TokenBasedResultSetKeyGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.CachedResultSetManager;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.DataProviderAdapter;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.ICustomColumnsProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IOriginalDataProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IResultSetManager;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.ITableModelProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.TableDataCache;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.ResultSetTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.ResultSetTranslator.Escape;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.UserFailureExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.TableModelUtils;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.XMLPropertyTransformer;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.shared.Constants;
import ch.systemsx.cisd.openbis.generic.shared.IOpenBisSessionManager;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.WebClientConfigurationProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.SessionConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.WebAppsProperties;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CustomImport;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomColumn;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ReportRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.WebApp;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.WebClientConfiguration;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.displaysettings.IDisplaySettingsUpdate;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.util.ServerUtils;

/**
 * An <i>abstract</i> {@link IClientService} implementation.
 * 
 * @author Christian Ribeaud
 */
public abstract class AbstractClientService implements IClientService,
        IOnlineHelpResourceLocatorService, ApplicationContextAware
{
    protected static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            AbstractClientService.class);

    @Resource(name = "request-context-provider")
    @Private
    public IRequestContextProvider requestContextProvider;

    // @Resource(name = "common-service")
    protected ICommonClientService commonClientService;

    @Resource(name = ExposablePropertyPlaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME)
    private ExposablePropertyPlaceholderConfigurer configurer;

    @Resource(name = ComponentNames.SESSION_MANAGER)
    private IOpenBisSessionManager sessionManager;

    @Autowired
    private ICacheManager cacheManager;

    private final Map<String, HttpSession> httpSessionsBySessionToken = new HashMap<>();

    private ISessionActionListener sessionActionListener = new ISessionActionListener()
        {
            @Override
            public void sessionClosed(String sessionToken)
            {
                synchronized (httpSessionsBySessionToken)
                {
                    cacheManager.clearCacheOfUser(sessionToken);
                    HttpSession httpSession = httpSessionsBySessionToken.remove(sessionToken);
                    logout(null, true, httpSession, sessionToken);
                    if (httpSession != null)
                    {
                        operationLog.info("Session " + sessionToken + " closed. "
                                + "httpSessionsBySessionToken.size() = " + httpSessionsBySessionToken.size());
                    }
                }
            }
        };

    @Autowired
    private TableDataCache<String, Object> tableDataCache;

    private String cifexURL;

    private String cifexRecipient;

    private String onlineHelpGenericRootURL;

    private String onlineHelpGenericPageTemplate;

    private String onlineHelpSpecificRootURL;

    private String onlineHelpSpecificPageTemplate;

    @Resource(name = "web-client-configuration-provider")
    @Private
    public WebClientConfigurationProvider webClientConfigurationProvider;

    // This is to prevent infinite recursion when the commonClientService is actually a proxy to
    // myself.
    private int getApplicationInfoInvocationCount = 0;

    private ApplicationContext applicationContext;

    @PostConstruct
    private void init()
    {
        this.commonClientService = applicationContext.getBean("common-service", ICommonClientService.class);
    }

    protected AbstractClientService()
    {
    }

    protected AbstractClientService(final IRequestContextProvider requestContextProvider)
    {
        this.requestContextProvider = requestContextProvider;
    }

    protected Properties getServiceProperties()
    {
        return configurer == null ? new Properties() : configurer.getResolvedProps();
    }

    protected void transformXML(IEntityPropertiesHolder propertiesHolder)
    {
        new XMLPropertyTransformer().transformXMLProperties(Arrays.asList(propertiesHolder));
    }

    @SuppressWarnings("unchecked")
    protected final IResultSetManager<String> getResultSetManager()
    {
        HttpSession httpSession = getHttpSession();
        if (httpSession == null)
        {
            throw new ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException(
                    "Your session has expired, please log in again.");
        }
        return (IResultSetManager<String>) httpSession
                .getAttribute(SessionConstants.OPENBIS_RESULT_SET_MANAGER);
    }

    protected final <T extends IEntityInformationHolder> ResultSetWithEntityTypes<T> listEntitiesWithTypes(
            final IResultSetConfig<String, T> criteria, IOriginalDataProvider<T> dataProvider)
    {
        ResultSet<T> resultSet = listEntities(criteria, dataProvider);
        Set<BasicEntityType> entityTypes =
                fetchEntityTypes(dataProvider, resultSet.getResultSetKey());
        return new ResultSetWithEntityTypes<T>(resultSet, entityTypes);
    }

    protected final <T> IResultSet<String, T> getResultSet(
            IResultSetConfig<String, T> resultSetConfig, IOriginalDataProvider<T> dataProvider)
    {
        final IResultSetManager<String> resultSetManager = getResultSetManager();
        final IResultSet<String, T> result =
                resultSetManager.getResultSet(getSessionToken(), resultSetConfig, dataProvider);
        return result;
    }

    protected final <T extends IEntityInformationHolder> Set<BasicEntityType> fetchEntityTypes(
            IOriginalDataProvider<T> dataProvider, String resultSetKey)
    {
        DefaultResultSetConfig<String, T> criteria = DefaultResultSetConfig.createFetchAll();
        criteria.setCacheConfig(ResultSetFetchConfig.createFetchFromCache(resultSetKey));
        final IResultSet<String, T> allData = getResultSet(criteria, dataProvider);
        Set<BasicEntityType> result = new HashSet<BasicEntityType>();
        for (T row : allData.getList().extractOriginalObjects())
        {
            result.add(row.getEntityType());
        }
        return result;
    }

    protected final <T> ResultSet<T> listEntities(final IResultSetConfig<String, T> criteria,
            IOriginalDataProvider<T> dataProvider)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final IResultSet<String, T> result = getResultSet(criteria, dataProvider);
        return ResultSetTranslator.translate(result, Escape.YES);
    }

    protected static <T> IOriginalDataProvider<T> createDummyDataProvider()
    {
        return new AbstractOriginalDataProviderWithoutHeaders<T>()
            {
                @Override
                public List<T> getFullOriginalData() throws UserFailureException
                {
                    throw new IllegalStateException("Data not found in the cache");
                }
            };
    }

    public final void setCifexURL(String cifexURL)
    {
        this.cifexURL = cifexURL;
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Set CIFEX URL for client to '" + cifexURL + "'.");
        }
    }

    public final void setCifexRecipient(String cifexRecipient)
    {
        this.cifexRecipient = cifexRecipient;
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Set CIFEX recipient for client to '" + cifexRecipient + "'.");
        }
    }

    @Override
    public String getOnlineHelpGenericRootURL()
    {
        return onlineHelpGenericRootURL;
    }

    public void setOnlineHelpGenericRootURL(String onlineHelpGenericRootURL)
    {
        this.onlineHelpGenericRootURL = onlineHelpGenericRootURL;
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Set root ULR for generic online help to '"
                    + onlineHelpGenericRootURL + "'.");
        }
    }

    @Override
    public String getOnlineHelpGenericPageTemplate()
    {
        return onlineHelpGenericPageTemplate;
    }

    public void setOnlineHelpGenericPageTemplate(String onlineHelpGenericPageTemplate)
    {
        this.onlineHelpGenericPageTemplate = onlineHelpGenericPageTemplate;
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Set template for generic online help to '"
                    + onlineHelpGenericPageTemplate + "'.");
        }
    }

    @Override
    public String getOnlineHelpSpecificRootURL()
    {
        return onlineHelpSpecificRootURL;
    }

    public void setOnlineHelpSpecificRootURL(String onlineHelpSpecificRootURL)
    {
        this.onlineHelpSpecificRootURL = onlineHelpSpecificRootURL;
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Set root ULR for specific online help to '"
                    + onlineHelpSpecificRootURL + "'.");
        }
    }

    @Override
    public String getOnlineHelpSpecificPageTemplate()
    {
        return onlineHelpSpecificPageTemplate;
    }

    public void setOnlineHelpSpecificPageTemplate(String onlineHelpSpecificPageTemplate)
    {
        this.onlineHelpSpecificPageTemplate = onlineHelpSpecificPageTemplate;
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Set template for specific online help to '"
                    + onlineHelpSpecificPageTemplate + "'.");
        }
    }

    private final SessionContext createSessionContext(final SessionContextDTO session)
    {
        final SessionContext sessionContext = new SessionContext();
        sessionContext.setSessionID(session.getSessionToken());

        DisplaySettings displaySettings = session.getDisplaySettings();
        sessionContext.setDisplaySettings(displaySettings);

        final User user = new User();
        user.setUserName(session.getUserName());
        user.setHomeGroupCode(session.tryGetHomeGroupCode());
        user.setUserEmail(session.getUserEmail());
        user.setUserPersonObject(session.getUserPersonObject());
        user.setUserPersonRoles(session.getUserPersonRoles());
        sessionContext.setUser(user);
        sessionContext.setAnonymous(session.isAnonymous());

        return sessionContext;
    }

    protected final String getSessionToken()
    {
        final HttpSession httpSession = getHttpSession();
        String sessionToken = null;
        if (httpSession != null)
        {
            sessionToken =
                    (String) httpSession
                            .getAttribute(SessionConstants.OPENBIS_SESSION_TOKEN_ATTRIBUTE_KEY);
        }
        if (sessionToken == null)
        {
            if (operationLog.isDebugEnabled())
            {
                String sessionId = (httpSession != null) ? httpSession.getId() : null;
                String logMessage =
                        String.format(
                                "Unable to find session token in session [%s], sessionId=[%s]",
                                httpSession, sessionId);
                operationLog.debug(logMessage);
            }
            throw new InvalidSessionException("Session expired. Please login again.");
        }
        return sessionToken;
    }

    protected final HttpSession getHttpSession()
    {
        return getOrCreateHttpSession(false);
    }

    private final HttpSession createHttpSession()
    {
        return getOrCreateHttpSession(true);
    }

    private final HttpSession getOrCreateHttpSession(final boolean create)
    {
        return requestContextProvider.getHttpServletRequest().getSession(create);
    }

    @SuppressWarnings("unchecked")
    protected final <T> CacheManager<String, T> getExportManager()
    {
        return (CacheManager<String, T>) getHttpSession().getAttribute(
                SessionConstants.OPENBIS_EXPORT_MANAGER);
    }

    protected final <T> String prepareExportEntities(TableExportCriteria<T> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        // Not directly needed but this refreshes the session.
        getSessionToken();
        final CacheManager<String, TableExportCriteria<T>> exportManager = getExportManager();
        return exportManager.saveData(criteria);
    }

    private CachedResultSetManager<String> createCachedResultSetManager()
    {
        return new CachedResultSetManager<String>(tableDataCache, new TokenBasedResultSetKeyGenerator(),
                new ICustomColumnsProvider()
                    {
                        @Override
                        public List<GridCustomColumn> getGridCustomColumn(String sessionToken,
                                String gridDisplayId)
                        {
                            return getServer().listGridCustomColumns(sessionToken, gridDisplayId);
                        }
                    });
    }

    // Saves the specified rows in the cache.
    // Returns a key in the cache where the data were saved.
    protected String saveReportInCache(final TableModel reportTableModel)
    {
        DefaultResultSetConfig<String, TableModelRowWithObject<ReportRowModel>> criteria =
                new DefaultResultSetConfig<String, TableModelRowWithObject<ReportRowModel>>();
        criteria.setLimit(0); // we do not need any data now, just a key
        ResultSet<TableModelRowWithObject<ReportRowModel>> resultSet =
                listEntities(criteria,
                        new IOriginalDataProvider<TableModelRowWithObject<ReportRowModel>>()
                            {
                                @Override
                                public List<TableModelRowWithObject<ReportRowModel>> getOriginalData(
                                        int maxSize) throws UserFailureException
                                {
                                    return TableModelUtils
                                            .asTableModelRowsWithReportRowModels(reportTableModel
                                                    .getRows());
                                }

                                @Override
                                public List<TableModelColumnHeader> getHeaders()
                                {
                                    return reportTableModel.getHeader();
                                }
                            });
        return resultSet.getResultSetKey();
    }

    /** Returns the {@link IServer} implementation for this client service. */
    protected abstract IServer getServer();

    //
    // IClientService
    //

    @Override
    public final ApplicationInfo getApplicationInfo()
    {
        final ApplicationInfo applicationInfo = new ApplicationInfo();
        if (commonClientService == null || commonClientService == this
                || getApplicationInfoInvocationCount > 0)
        {
            applicationInfo.setCifexURL(cifexURL);
            applicationInfo.setCifexRecipient(cifexRecipient);
            applicationInfo.setWebClientConfiguration(getWebClientConfiguration());
        } else
        {
            getApplicationInfoInvocationCount++;
            ApplicationInfo commonApplicationInfo = commonClientService.getApplicationInfo();
            getApplicationInfoInvocationCount--;
            applicationInfo.setCifexURL(commonApplicationInfo.getCifexURL());
            applicationInfo.setCifexRecipient(commonApplicationInfo.getCifexRecipient());
            applicationInfo.setMaxResults(commonApplicationInfo.getMaxResults());
            applicationInfo.setWebClientConfiguration(commonApplicationInfo
                    .getWebClientConfiguration());
        }
        applicationInfo.setEnabledTechnologies(ServerUtils.extractSet(getServiceProperties()
                .getProperty(Constants.ENABLED_MODULES_KEY)));
        applicationInfo.setCustomImports(getCustomImports());
        applicationInfo.setWebapps(extractWebAppsProperties());
        applicationInfo.setArchivingConfigured(isArchivingConfigured());
        applicationInfo.setProjectSamplesEnabled(isProjectSamplesEnabled());
        applicationInfo.setProjectLevelAuthorizationEnabled(isProjectLevelAuthorizationEnabled());
        applicationInfo.setProjectLevelAuthorizationUser(isProjectLevelAuthorizationUser());
        applicationInfo.setVersion(getVersion());
        return applicationInfo;
    }

    private List<WebApp> extractWebAppsProperties()
    {
        WebAppsProperties webAppsProperties = new WebAppsProperties(getServiceProperties());
        return webAppsProperties.getWebApps();
    }

    @Override
    public final List<CustomImport> getCustomImports()
    {
        return ServerUtils.getCustomImportDescriptions(getServiceProperties());
    }

    @Override
    public void deactivatePersons(List<String> personsCodes) throws UserFailureException
    {
        final String sessionToken = getSessionToken();
        getServer().deactivatePersons(sessionToken, personsCodes);
    }

    @Override
    public int countActiveUsers() throws UserFailureException
    {
        final String sessionToken = getSessionToken();
        return getServer().countActivePersons(sessionToken);
    }

    protected WebClientConfiguration getWebClientConfiguration()
    {
        return webClientConfigurationProvider.getWebClientConfiguration();
    }

    protected boolean isTrashEnabled()
    {
        return getWebClientConfiguration().getEnableTrash();
    }

    protected String getVersion()
    {
        return BuildAndEnvironmentInfo.INSTANCE.getFullVersion();
    }

    private boolean isArchivingConfigured()
    {
        try
        {
            return getServer().isArchivingConfigured(getSessionToken());
        } catch (InvalidSessionException e)
        {
            // ignored
        }
        return false;
    }

    private boolean isProjectSamplesEnabled()
    {
        try
        {
            return getServer().isProjectSamplesEnabled(getSessionToken());
        } catch (InvalidSessionException e)
        {
            // ignored
        }
        return false;
    }

    private boolean isProjectLevelAuthorizationEnabled()
    {
        try
        {
            return getServer().isProjectLevelAuthorizationEnabled(getSessionToken());
        } catch (InvalidSessionException e)
        {
            // ignored
        }
        return false;
    }

    private boolean isProjectLevelAuthorizationUser()
    {
        try
        {
            return getServer().isProjectLevelAuthorizationUser(getSessionToken());
        } catch (InvalidSessionException e)
        {
            // ignored
        }
        return false;
    }

    @Override
    public final SessionContext tryToGetCurrentSessionContext(boolean anonymous, String sessionIdOrNull)
    {
        boolean sessionIdSpecified = sessionIdOrNull != null;
        try
        {
            final String sessionToken = sessionIdSpecified ? sessionIdOrNull : getSessionToken();
            final SessionContextDTO session = getServer().tryGetSession(sessionToken);
            if (session == null)
            {
                return null;
            } else if (anonymous == false && session.isAnonymous())
            {
                operationLog.debug("expected: " + anonymous + " found: " + session.isAnonymous());
                getServer().logout(sessionToken);
                return null;
            }
            return sessionIdSpecified ? tryToLogin(session) : createSessionContext(session);
        } catch (final InvalidSessionException e)
        {
            return null;
        }
    }

    @Override
    public final SessionContext tryToLoginAnonymously()
    {
        try
        {
            final SessionContextDTO session = getServer().tryAuthenticateAnonymously();
            return tryToLogin(session);
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        } catch (final IllegalStateException e)
        {
            operationLog.error("Session already invalidated.", e);
            return null;
        }
    }

    @Override
    public final SessionContext tryToLogin(final String userID, final String password)
    {
        try
        {
            final SessionContextDTO session = getServer().tryAuthenticate(userID, password);
            return tryToLogin(session);
        } catch (final IllegalStateException e)
        {
            operationLog.error("Session already invalidated.", e);
            return null;
        }
    }

    private SessionContext tryToLogin(final SessionContextDTO session)
    {
        if (session == null)
        {
            return null;
        }
        final HttpSession httpSession = createHttpSession();
        synchronized (httpSessionsBySessionToken)
        {
            httpSessionsBySessionToken.put(session.getSessionToken(), httpSession);
            operationLog.info("httpSessionsBySessionToken.size() = " + httpSessionsBySessionToken.size());
        }

        // Expiration time of httpSession is 10 seconds less than of session
        final int sessionExpirationTimeInMillis = session.getSessionExpirationTime();
        final int sessionExpirationTimeInSeconds = sessionExpirationTimeInMillis / 1000;
        if (sessionExpirationTimeInMillis < 0)
        {
            httpSession.setMaxInactiveInterval(-1);
        } else if (sessionExpirationTimeInSeconds < 10)
        {
            httpSession.setMaxInactiveInterval(0);
        } else
        {
            httpSession.setMaxInactiveInterval(sessionExpirationTimeInSeconds - 10);
        }
        httpSession.setAttribute(SessionConstants.OPENBIS_SESSION_TOKEN_ATTRIBUTE_KEY,
                session.getSessionToken());
        httpSession.setAttribute(SessionConstants.OPENBIS_SERVER_ATTRIBUTE_KEY, getServer());
        httpSession.setAttribute(SessionConstants.OPENBIS_RESULT_SET_MANAGER,
                createCachedResultSetManager());
        httpSession.setAttribute(SessionConstants.OPENBIS_EXPORT_MANAGER,
                CacheManager.createCacheManager());
        return createSessionContext(session);
    }

    @Override
    public void setBaseURL(String baseURL)
    {
        try
        {
            final String sessionToken = getSessionToken();
            IServer server = getServer();
            server.setBaseIndexURL(sessionToken, baseURL);
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("Set openBIS base URL for client to '" + baseURL + "'.");
            }
        } catch (InvalidSessionException e)
        {
            // ignored
        }
    }

    @Override
    public void saveDisplaySettings(DisplaySettings displaySettings)
    {
        try
        {
            final String sessionToken = getSessionToken();
            IServer server = getServer();
            int maxEntityVisits = getWebClientConfiguration().getMaxEntityVisits();
            server.saveDisplaySettings(sessionToken, displaySettings, maxEntityVisits);
        } catch (InvalidSessionException e)
        {
            // ignored
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    @Override
    public void updateDisplaySettings(IDisplaySettingsUpdate displaySettingsUpdate)
    {
        try
        {
            final String sessionToken = getSessionToken();
            IServer server = getServer();
            server.updateDisplaySettings(sessionToken, displaySettingsUpdate);
        } catch (InvalidSessionException e)
        {
            // ignored
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    @Override
    public DisplaySettings resetDisplaySettings()
    {
        try
        {
            final String sessionToken = getSessionToken();
            IServer server = getServer();
            final DisplaySettings defaultSettings = server.getDefaultDisplaySettings(sessionToken);
            saveDisplaySettings(defaultSettings);
            return defaultSettings;
        } catch (InvalidSessionException e)
        {
            // ignored
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
        return null;
    }

    @Override
    public void changeUserHomeSpace(TechId groupIdOrNull)
    {
        try
        {
            final String sessionToken = getSessionToken();
            IServer server = getServer();
            server.changeUserHomeSpace(sessionToken, groupIdOrNull);
        } catch (InvalidSessionException e)
        {
            // ignored
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    @Override
    public final void logout(DisplaySettings displaySettings, boolean simpleViewMode)
    {
        try
        {
            final HttpSession httpSession = getHttpSession();
            String sessionToken = getSessionToken();
            logout(displaySettings, simpleViewMode, httpSession, sessionToken);
        } catch (Exception e)
        {
            operationLog.info("logout exception: " + e);
        }
    }

    private void logout(DisplaySettings displaySettings, boolean simpleViewMode, final HttpSession httpSession, String sessionToken)
    {
        if (httpSession != null)
        {
            httpSession.removeAttribute(SessionConstants.OPENBIS_SESSION_TOKEN_ATTRIBUTE_KEY);
            httpSession.removeAttribute(SessionConstants.OPENBIS_SERVER_ATTRIBUTE_KEY);
            httpSession.removeAttribute(SessionConstants.OPENBIS_RESULT_SET_MANAGER);
            httpSession.removeAttribute(SessionConstants.OPENBIS_EXPORT_MANAGER);
            httpSession.invalidate();
            IServer server = getServer();
            if (simpleViewMode == false)
            {
                // only save settings for "normal" view
                int maxEntityVisits = getWebClientConfiguration().getMaxEntityVisits();
                server.saveDisplaySettings(sessionToken, displaySettings, maxEntityVisits);
            }
            server.logout(sessionToken);
        }
    }

    protected <T extends Serializable> TypedTableResultSet<T> listEntities(
            ITableModelProvider<T> provider,
            IResultSetConfig<String, TableModelRowWithObject<T>> criteria)
    {
        DataProviderAdapter<T> dataProvider = new DataProviderAdapter<T>(provider);
        ResultSet<TableModelRowWithObject<T>> resultSet = listEntities(criteria, dataProvider);
        return new TypedTableResultSet<T>(resultSet);
    }

    protected static void cleanUploadedFiles(final String sessionKey, HttpSession session,
            UploadedFilesBean uploadedFiles)
    {
        if (uploadedFiles != null)
        {
            uploadedFiles.deleteTransferredFiles();
        }
        if (session != null)
        {
            session.removeAttribute(sessionKey);
        }
    }

    protected static UploadedFilesBean getUploadedFiles(String sessionKey, HttpSession session)
    {
        if (session.getAttribute(sessionKey) == null
                || session.getAttribute(sessionKey) instanceof UploadedFilesBean == false)
        {
            throw new IllegalStateException(String.format(
                    "No UploadedFilesBean object as session attribute '%s' found.", sessionKey));
        }
        return (UploadedFilesBean) session.getAttribute(sessionKey);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
    {
        this.applicationContext = applicationContext;
        sessionManager.addListener(sessionActionListener);
    }
}

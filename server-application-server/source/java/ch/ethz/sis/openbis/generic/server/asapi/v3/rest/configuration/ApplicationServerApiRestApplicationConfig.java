/*
 *
 *
 * Copyright 2023 Simone Baffelli (simone.baffelli@empa.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.ethz.sis.openbis.generic.server.asapi.v3.rest.configuration;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.OperationListenerLoader;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.PostgresAuthorisationInformationProviderDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.dao.PostgresSearchDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.dao.PropertyAssignmentSearchDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.hibernate.IdentityMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.*;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql.HibernateSQLExecutor;
import ch.systemsx.cisd.authentication.DummyAuthenticationService;
import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.NullAuthenticationService;
import ch.systemsx.cisd.authentication.crowd.CrowdAuthenticationService;
import ch.systemsx.cisd.authentication.crowd.CrowdConfiguration;
import ch.systemsx.cisd.authentication.file.CachingAuthenticationConfiguration;
import ch.systemsx.cisd.authentication.ldap.LDAPDirectoryConfiguration;
import ch.systemsx.cisd.authentication.stacked.StackedAuthenticationService;
import ch.systemsx.cisd.common.jython.evaluator.JythonEvaluatorSpringComponent;
import ch.systemsx.cisd.common.mail.MailClientParameters;
import ch.systemsx.cisd.common.monitoring.JMXMemoryMonitorSpringBean;
import ch.systemsx.cisd.common.multiplexer.IMultiplexer;
import ch.systemsx.cisd.common.multiplexer.ThreadPoolMultiplexer;
import ch.systemsx.cisd.common.servlet.RequestContextProviderAdapter;
import ch.systemsx.cisd.common.servlet.SpringRequestContextProvider;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.openbis.common.spring.AnnotationBeanPostProcessorIgnoringMissingBeans;
import ch.systemsx.cisd.openbis.generic.client.web.server.CommonClientService;
import ch.systemsx.cisd.openbis.generic.client.web.server.queue.ConsumerQueue;
import ch.systemsx.cisd.openbis.generic.server.*;
import ch.systemsx.cisd.openbis.generic.server.authorization.NoAuthorization;
import ch.systemsx.cisd.openbis.generic.server.business.*;
import ch.systemsx.cisd.openbis.generic.server.business.bo.NextExceptionFallbackExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.coreplugin.CorePluginRegistrator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.DataStoreServerBasedDataSourceProvider;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.DynamicPropertyEvaluationScheduler;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.DAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.OpenBISHibernateTransactionManager;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.SequenceNameMapper;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.deletion.EntityHistoryCreator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.DynamicPropertyCalculatorFactory;
import ch.systemsx.cisd.openbis.generic.server.plugin.SampleServerPluginRegistry;
import ch.systemsx.cisd.openbis.generic.shared.IJythonEvaluatorPool;
import ch.systemsx.cisd.openbis.generic.shared.LogMessagePrefixGenerator;
import ch.systemsx.cisd.openbis.generic.shared.SessionWorkspaceProvider;
import ch.systemsx.cisd.openbis.generic.shared.WebClientConfigurationProvider;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.json.GenericObjectMapper;
import ch.systemsx.cisd.openbis.generic.shared.authorization.AuthorizationConfig;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LastModificationState;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.ManagedPropertyEvaluatorFactory;
import org.hibernate.validator.constraints.NotEmpty;
import org.jmock.lib.IdentityExpectationErrorTranslator;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.entity_validation.EntityValidatorFactory;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.persistence.Entity;
import javax.sql.DataSource;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Properties;








@Validated
//@Configuration
//@ConfigurationProperties(prefix = "database")
class DBConfig {

    private final String basicDatabaseName = "openbis";

    private final String readOnlyGroup = "openbis_readonly";

    @Value("${database.create-from-scratch}")
    private boolean createFromScratch = true;

    @Value("${database.script-single-step-mode}")
    private boolean scriptSingleStepMode = false;



    @NotEmpty
    @Value("${database.url-host-part}")
    private String urlHostPart;

    @NotEmpty
    @Value("${database.admin-user}")
    private String adminUser;

    @NotEmpty
    @Value("${database.owner}")
    private String owner;



    @NotEmpty
    @Value("${database.owner-password}")
    private String ownerPassword;

    @NotEmpty
    @Value("${database.admin-password}")
    private String adminPassword;

    @Value("${database.kind}")
    private String databaseKind;

    @NotEmpty
    @Value("${database.engine}")
    private String databaseEngineCode;

    @NotEmpty
    @Value("${database.valid-versions}")
    private String validVersions;


    @NotEmpty
    @Value("${script-folder}/sql")
    private String scriptFolder;

    @NotEmpty
    @Value("${database.script-folders}")
    private String scriptFolders;

    @NotEmpty
    @Value("${database.instance}")
    private String databaseInstance;

    @NotEmpty
    @Value("${database.max-wait-for-connection}")
    private String  maxWaitForConnection;

    @NotEmpty
    @Value("${database.max-active-connections}")
    private String maxActiveConnections = "20";

    @NotEmpty
    @Value("${database.max-idle-connections}")
    private String maxIdleConnections = "20";

    @NotEmpty
    @Value("${database.active-connections-log-interval}")
    private String activeConnectionsLogInterval = "3600";

    @PostConstruct
    public void init() {
        System.out.println("DatabaseProperties: " + this);
    }

    @Bean(name = "sequence-name-mapper")
    SequenceNameMapper sequenceNameMapper() {
        return new SequenceNameMapper();
    }


    @Bean(name = "db-configuration-context")
    DatabaseConfigurationContext dbConfigurationContext() {
        DatabaseConfigurationContext dbContext = new DatabaseConfigurationContext();
        dbContext.setBasicDatabaseName(basicDatabaseName);
        dbContext.setCreateFromScratch(createFromScratch);
        dbContext.setScriptSingleStepMode(scriptSingleStepMode);
        dbContext.setSequenceNameMapper(sequenceNameMapper());
        dbContext.setUrlHostPart(urlHostPart);
        dbContext.setAdminUser(adminUser);
        dbContext.setOwner(owner);
        dbContext.setReadOnlyGroup(readOnlyGroup);
        dbContext.setPassword(ownerPassword);
        dbContext.setAdminPassword(adminPassword);
        dbContext.setDatabaseKind(databaseKind);
        dbContext.setDatabaseEngineCode(databaseEngineCode);
        dbContext.setValidVersions(validVersions);
        dbContext.setScriptFolder(scriptFolder);
        dbContext.setScriptFolders(scriptFolders);
        dbContext.setDatabaseInstance(databaseInstance);
        dbContext.setMaxWaitForConnectionProp(maxWaitForConnection);
        dbContext.setMaxActiveConnectionsProp(maxActiveConnections);
        dbContext.setMaxIdleConnectionsProp(maxIdleConnections);
        dbContext.setActiveConnectionsLogIntervalProp(activeConnectionsLogInterval);
        return dbContext;
    }

    @Bean(name = "data-source")
    public DataSource dataSource(DatabaseConfigurationContext dbConfigurationContext) {
        return dbConfigurationContext.getDataSource();
    }
}


@Configuration
class CrowdConfig {

    @Value("${crowd.service.host}")
    private String host;

    @Value("${crowd.service.port}")
    private String port;

    @Value("${crowd.service.application.name}")
    private String applicationName;

    @Value("${crowd.service.application.password}")
    private String applicationPassword;

    @Value("${crowd.service.application.timeout}")
    private String applicationTimeout;


    @Bean(name="crowd-configuration")
    public CrowdConfiguration crowConfiguration() {
        var configuration = new CrowdConfiguration();
        configuration.setHost(host);
        configuration.setPortStr(port);
        configuration.setApplication(applicationName);
        configuration.setApplicationPassword(applicationPassword);
        configuration.setTimeoutStr(applicationTimeout);
        return configuration;
    }

    @Bean(name="crowd-authentication-service")
    public CrowdAuthenticationService crowdAuthenticationService() {
        return new CrowdAuthenticationService(crowConfiguration());
    }

}

@Configuration
class LDAPConfig {

    @Value("${ldap.server.url}")
    private String serverUrl;

    @Value("${ldap.security.protocol}")
    private String securityProtocol;

    @Value("${ldap.security.authentication-method}")
    private String securityAuthenticationMethod;

    @Value("${ldap.security.principal.distinguished.name}")
    private String securityPrincipalDistinguishedName;

    @Value("${ldap.security.principal.password}")
    private String securityPrincipalPassword;

    @Value("${ldap.referral}")
    private String referral;

    @Value("${ldap.searchBase}")
    private String searchBase;

    @Value("${ldap.attributenames.user.id}")
    private String userIdAttributeName;

    @Value("${ldap.attributenames.email}")
    private String emailAttributeName;

    @Value("${ldap.attributenames.first.name}")
    private String firstNameAttributeName;

    @Value("${ldap.attributenames.last.name}")
    private String lastNameAttributeName;

    @Value("${ldap.queryEmailForAliases}")
    private String queryEmailForAliases;

    @Value("${ldap.queryTemplate}")
    private String queryTemplate;

    @Value("${ldap.maxRetries}")
    private String maxRetriesStr;

    @Value("${ldap.timeout}")
    private String timeoutStr;

    @Value("${ldap.timeToWaitAfterFailure}")
    private String timeToWaitAfterFailureStr;

    @Bean(name = "ldap-directory-configuration")
    public LDAPDirectoryConfiguration ldapDirectoryConfiguration() {
        var configuration = new LDAPDirectoryConfiguration();
        configuration.setServerUrl(serverUrl);
        configuration.setSecurityProtocol(securityProtocol);
        configuration.setSecurityAuthenticationMethod(securityAuthenticationMethod);
        configuration.setSecurityPrincipalDistinguishedName(securityPrincipalDistinguishedName);
        configuration.setSecurityPrincipalPassword(securityPrincipalPassword);
        configuration.setReferral(referral);
        configuration.setSearchBase(searchBase);
        configuration.setUserIdAttributeName(userIdAttributeName);
        configuration.setEmailAttributeName(emailAttributeName);
        configuration.setFirstNameAttributeName(firstNameAttributeName);
        configuration.setLastNameAttributeName(lastNameAttributeName);
        configuration.setQueryEmailForAliases(queryEmailForAliases);
        configuration.setQueryTemplate(queryTemplate);
        configuration.setMaxRetriesStr(maxRetriesStr);
        configuration.setTimeoutStr(timeoutStr);
        configuration.setTimeToWaitAfterFailureStr(timeToWaitAfterFailureStr);
        return configuration;
    }

    @Bean("ldap-authentication-service")
    public ch.systemsx.cisd.authentication.ldap.LDAPAuthenticationService ldapAuthenticationService() {
        return new ch.systemsx.cisd.authentication.ldap.LDAPAuthenticationService(ldapDirectoryConfiguration());
    }
}

@Configuration
@Import({CrowdConfig.class, LDAPConfig.class})
class GenericConfiguration{

    @Autowired
    private CrowdConfig crowdConfig;

    @Autowired
    private LDAPConfig ldapConfig;

    @Value("${authentication.cache.time}")
    private String cacheTime;

    @Value("${authentication.cache.time.no-revalidation}")
    private String cacheTimeNoRevalidation;

    @Bean(name="propertyConfigurer")
    static public ExposablePropertyPlaceholderConfigurer propertyConfigurer() {
        ExposablePropertyPlaceholderConfigurer configurer = new ExposablePropertyPlaceholderConfigurer();
        configurer.setLocation(new ClassPathResource("service.properties"));
        return configurer;
    }

    @Bean(name="no-authentication-service")
    public IAuthenticationService noAuthenticationService() {
        return new NullAuthenticationService();
    }

    @Bean(name="file-authentication-service")
    public ch.systemsx.cisd.authentication.file.FileAuthenticationService fileAuthenticationService() {
        return new ch.systemsx.cisd.authentication.file.FileAuthenticationService("etc/passwd");
    }

    @Bean(name="file-crowd-authentication-service")
    public IAuthenticationService fileCrowdAuthenticationService() {
        return new StackedAuthenticationService(List.of(fileAuthenticationService(), crowdConfig.crowdAuthenticationService()));
    }

    @Bean(name="file-ldap-authentication-service")
    public IAuthenticationService fileLdapAuthenticationService() {
        return new StackedAuthenticationService(List.of(fileAuthenticationService(), ldapConfig.ldapAuthenticationService()));
    }

    @Bean(name="ldap-crowd-authentication-service")
    public IAuthenticationService ldapCrowdAuthenticationService() {
        return new StackedAuthenticationService(List.of(ldapConfig.ldapAuthenticationService(), crowdConfig.crowdAuthenticationService()));
    }

    @Bean(name="file-ldap-crowd-caching-authentication-service")
    public IAuthenticationService fileLdapCrowdCachingAuthenticationService() {
        return new StackedAuthenticationService(List.of(fileAuthenticationService(), ldapConfig.ldapAuthenticationService(), crowdConfig.crowdAuthenticationService()));
    }

    @Bean(name="authentication-cache-configuration")
    public CachingAuthenticationConfiguration authenticationCacheConfiguration() {
        var config = new CachingAuthenticationConfiguration();
        config.setDelegate(ldapCrowdAuthenticationService());
        config.setCacheTimeStr(cacheTime);
        config.setCacheTimeNoRevalidationStr(cacheTimeNoRevalidation);
        config.setPasswordCacheFile("etc/passwd_cache");

        return config;
    }

    @Bean(name="request-context-provider")
    public SpringRequestContextProvider requestContextProvider() {
        return new SpringRequestContextProvider();
    }


}



@Configuration
class SearchManagerConfig {

    @Autowired
    private PostgresSearchDAO postgresSearchDao;

    @Autowired
    private PostgresAuthorisationInformationProviderDAO informationProviderDao;

    @Autowired
    private IdentityMapper identityTranslator;

    @Autowired
    private HibernateSQLExecutor sqlExecutor;



    @Bean(name = "sample-search-manager")
    public SampleSearchManager sampleSearchManager() {
        return createSearchManager(postgresSearchDao, informationProviderDao, identityTranslator, SampleSearchManager.class);
    }

    @Bean(name = "sample-container-search-manager")
    public SampleContainerSearchManager sampleContainerSearchManager() {
        return createSearchManager(postgresSearchDao, informationProviderDao, identityTranslator, SampleContainerSearchManager.class);
    }

    @Bean(name="experiment-search-manager")
    public ExperimentSearchManager experimentSearchManager() {
        return createSearchManager(postgresSearchDao, informationProviderDao, identityTranslator, ExperimentSearchManager.class);
    }

    @Bean(name="project-search-manager")
    public ProjectSearchManager projectSearchManager() {
        return createSearchManager(postgresSearchDao, informationProviderDao, identityTranslator, ProjectSearchManager.class);
    }

    @Bean(name="space-search-manager")
    public SpaceSearchManager spaceSearchManager() {
        return createSearchManager(postgresSearchDao, informationProviderDao, identityTranslator, SpaceSearchManager.class);
    }

    @Bean(name="content-copy-search-manager")
    public ContentCopySearchManager contentCopySearchManager() {
        return createSearchManager(postgresSearchDao, informationProviderDao, identityTranslator, ContentCopySearchManager.class);
    }

    @Bean(name="dataset-search-manager")
    public DataSetSearchManager datasetSearchManager() {
        return createSearchManager(postgresSearchDao, informationProviderDao, identityTranslator, DataSetSearchManager.class);
    }

    @Bean(name="data-set-type-search-manager")
    public DataSetTypeSearchManager dataSetTypeSearchManager() {
        return createSearchManager(postgresSearchDao, informationProviderDao, identityTranslator, DataSetTypeSearchManager.class);
    }

    @Bean(name="experiment-type-search-manager")
    public ExperimentTypeSearchManager experimentTypeSearchManager() {
        return createSearchManager(postgresSearchDao, informationProviderDao, identityTranslator, ExperimentTypeSearchManager.class);
    }

    @Bean(name="person-search-manager")
    public PersonSearchManager personSearchManager() {
        return createSearchManager(postgresSearchDao, informationProviderDao, identityTranslator, PersonSearchManager.class);
    }

    @Bean(name="tag-search-manager")
    public TagSearchManager tagSearchManager() {
        return createSearchManager(postgresSearchDao, informationProviderDao, identityTranslator, TagSearchManager.class);
    }

    @Bean(name="semantic-annotation-search-manager")
    public SemanticAnnotationSearchManager semanticAnnotationSearchManager() {
        return createSearchManager(postgresSearchDao, informationProviderDao, identityTranslator, SemanticAnnotationSearchManager.class);
    }

    @Bean(name="property-type-search-manager")
    public PropertyTypeSearchManager propertyTypeSearchManager() {
        return createSearchManager(postgresSearchDao, informationProviderDao, identityTranslator, PropertyTypeSearchManager.class);
    }

    @Bean(name="linked-data-set-kind-search-manager")
    public LinkedDataSetKindSearchManager linkedDataSetKindSearchManager() {
        return createSearchManager(postgresSearchDao, informationProviderDao, identityTranslator, LinkedDataSetKindSearchManager.class);
    }

    @Bean(name="physical-data-set-kind-search-manager")
    public PhysicalDataSetKindSearchManager physicalDataSetKindSearchManager() {
        return createSearchManager(postgresSearchDao, informationProviderDao, identityTranslator, PhysicalDataSetKindSearchManager.class);
    }

    @Bean(name="external-dms-search-manager")
    public ExternalDmsSearchManager externalDmsSearchManager() {
        return createSearchManager(postgresSearchDao, informationProviderDao, identityTranslator, ExternalDmsSearchManager.class);
    }

    @Bean(name="ffty-search-manager")
    public FileFormatTypeSearchManager fileFormatTypeSearchManager() {
        return createSearchManager(postgresSearchDao, informationProviderDao, identityTranslator, FileFormatTypeSearchManager.class);
    }

    @Bean(name="locator-type-search-manager")
    public LocatorTypeSearchManager locatorTypeSearchManager() {
        return createSearchManager(postgresSearchDao, informationProviderDao, identityTranslator, LocatorTypeSearchManager.class);
    }

    @Bean(name="storage-format-search-manager")
    public StorageFormatSearchManager storageFormatSearchManager() {
        return createSearchManager(postgresSearchDao, informationProviderDao, identityTranslator, StorageFormatSearchManager.class);
    }

    @Bean(name="material-search-manager")
    public MaterialSearchManager materialSearchManager() {
        return createSearchManager(postgresSearchDao, informationProviderDao, identityTranslator, MaterialSearchManager.class);
    }

    @Bean(name="material-type-search-manager")
    public MaterialTypeSearchManager materialTypeSearchManager() {
        return createSearchManager(postgresSearchDao, informationProviderDao, identityTranslator, MaterialTypeSearchManager.class);
    }

    @Bean(name="entity-type-search-manager")
    public EntityTypeSearchManager entityTypeSearchManager() {
        return createSearchManager(postgresSearchDao, informationProviderDao, identityTranslator, EntityTypeSearchManager.class);
    }

    @Bean(name="global-search-manager")
    public GlobalSearchManager globalSearchManager() {
        return new GlobalSearchManager(informationProviderDao, postgresSearchDao);
    }

    @Bean(name="property-assignment-search-dao")
    public PropertyAssignmentSearchDAO propertyAssignmentSearchDAO() {
        return new PropertyAssignmentSearchDAO(sqlExecutor);
    }

    @Bean(name="property-assignment-search-manager")
    public PropertyAssignmentSearchManager propertyAssignmentSearchManager() {
        return new PropertyAssignmentSearchManager(postgresSearchDao, informationProviderDao, identityTranslator, propertyAssignmentSearchDAO());
    }

    // Add more @Bean methods for other search managers as needed

    private <T extends AbstractSearchManager> T createSearchManager(PostgresSearchDAO postgresSearchDao,
                                                                    PostgresAuthorisationInformationProviderDAO informationProviderDao,
                                                                    IdentityMapper identityTranslator,
                                                                    Class<T> searchManagerClass) {
        try {
            java.lang.reflect.Constructor<T> constructor = searchManagerClass.getConstructor(PostgresSearchDAO.class, PostgresAuthorisationInformationProviderDAO.class, IdentityExpectationErrorTranslator.class);
            return constructor.newInstance(postgresSearchDao, informationProviderDao, identityTranslator);
        } catch (Exception e) {
            // Handle or log the exception
            throw new RuntimeException("Error creating search manager", e);
        }
    }
}


@Configuration
@ComponentScan(basePackages = "ch.systemsx.cisd.openbis.generic.shared.dto", includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Entity.class))
class HibernateConfig {

    @Autowired
    private DataSource dataSource;

    @Bean(name = "hibernate-session-factory")
    public SessionFactory hibernateSessionFactory() {
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        // Omit setAnnotatedClasses when using component scanning
        sessionFactory.setPackagesToScan("ch.systemsx.cisd.openbis.generic.shared.dto");
        sessionFactory.setHibernateProperties(hibernateProperties());
        return (SessionFactory) sessionFactory;
    }

    @Bean
    public PlatformTransactionManager hibernateTransactionManager() {
        return new HibernateTransactionManager(hibernateSessionFactory());
    }

    @Bean
    DynamicPropertyEvaluationScheduler dynamicPropertyEvaluationScheduler() {
        return new DynamicPropertyEvaluationScheduler();
    }

    private Properties hibernateProperties() {
        Properties properties = new Properties();
        properties.setProperty("hibernate.current_session_context_class", "org.springframework.orm.hibernate5.SpringSessionContext");
        properties.setProperty("hibernate.cache.use_second_level_cache", "false");
        properties.setProperty("hibernate.cache.provider_class", "org.hibernate.cache.EhCacheProvider");
        properties.setProperty("hibernate.max_fetch_depth", "4");
        // Uncomment the following lines for debugging Hibernate SQL queries
        // properties.setProperty("hibernate.show_sql", "true");
        // properties.setProperty("hibernate.use_sql_comments", "true");
        // properties.setProperty("hibernate.format_sql", "true");
        return properties;
    }

    @Bean(name = "dynamic-property-scheduler")
    public DynamicPropertyEvaluationScheduler dynamicPropertyScheduler() {
        return new DynamicPropertyEvaluationScheduler();
    }
}

@Configuration
@Import({GenericConfiguration.class})
class AuthenticationConfig {
    @Autowired
    private  BeanFactory beanFactory;
//    @Resource
//    private IAuthenticationService fileLdapCrowdCachingAuthenticationService;
//
//    @Resource
//    private IAuthenticationService fileLdapAuthenticationService;
//
//    @Resource
//    private IAuthenticationService fileCrowdAuthenticationService;
//
//    @Resource
//    private IAuthenticationService ldapCrowdAuthenticationService;
//
//    @Resource
//    private IAuthenticationService ldapAuthenticationService;


    @Value("${authentication-service}")
    private String authenticationServiceBeanName;

    @Bean(name = "dummy-authentication-service")
    public IAuthenticationService dummyAuthenticationService() {
        return new DummyAuthenticationService();
    }

    @Bean(name = "no-authentication-service")
    public IAuthenticationService noAuthenticationService() {
        return new NullAuthenticationService();
    }

    @Bean(name = "authentication-service")
    public IAuthenticationService authenticationService() {
        return beanFactory.getBean(authenticationServiceBeanName, IAuthenticationService.class);
    }


}



@Configuration
@Import({DBConfig.class, HibernateConfig.class, AuthorizationConfig.class, AuthenticationConfig.class, SearchManagerConfig.class, GenericConfiguration.class})
public class ApplicationServerApiRestApplicationConfig {


    @Value("${entity-validation-plugins-directory}")
    private String entityValidationPluginsDirectory;

    @Value("${jython-evaluator-pool-size}")
    private String jythonEvaluatorPoolSize;

    @Value("${entity-history.enabled}")
    private String entityHistoryEnabled;

    @Value("${authentication-service}")
    private String authenticationServiceReference;

    @Value("${session-timeout}")
    private int sessionTimeout;

    @Value("${session-timeout-no-login}")
    private String sessionTimeoutNoLogin;

    @Value("${user-for-anonymous-login}")
    private String userForAnonymousLogin;

    @Value("${max-number-of-sessions-per-user}")
    private String maxNumberOfSessionsPerUser;

    @Value("${users-with-unrestricted-number-of-sessions}")
    private String usersWithUnrestrictedNumberOfSessions;


    @Value("${accepted-remote-hosts-for-identity-change}")
    private String acceptedRemoteHostsForIdentityChange;

    @Value("${dss-data-source-mapping-file-path}")
    private String dssDataSourceMappingFilePath = "etc/dss-datasource-mapping";


    @Value("${openbis.support.email}")
    private String openbisSupportEmail;

    @Value("${dss-rpc.put.dss-code}")
    private String defaultPutDataStoreServerCode;


    @Value("${web-client.configuration.file}")
    private String webClientConfigurationFile;


    @Value("${cifex-url}")
    private String cifexURL;

    @Value("${cifex-recipient}")
    private String cifexRecipient;

    @Value("${onlinehelp.generic.root-url}")
    private String onlineHelpGenericRootURL;

    @Value("${onlinehelp.generic.page-template}")
    private String onlineHelpGenericPageTemplate;

    @Value("${onlinehelp.specific.root-url}")
    private String onlineHelpSpecificRootURL;

    @Value("${onlinehelp.specific.page-template}")
    private String onlineHelpSpecificPageTemplate;

    @Value("${mail.from}")
    private String mailFrom;

    @Value("${mail.smtp.host}")
    private String smtpHost;

    @Value("${mail.smtp.port}")
    private String smtpPort;

    @Value("${mail.smtp.user}")
    private String smtpUser;

    @Value("${mail.smtp.password}")
    private String smtpPassword;

    @Value("${mail.smtp.address}")
    private String testAddress;


    @Value("${core-plugins-folder}")
    private String pluginsFolderName;

    @Value("${enabled-modules}")
    private String enabledTechnologies;

    @Value("${disabled-master-data-initialization}")
    private String disabledMasterDataInitialization;

    @Value("${trusted-cross-origin-domains}")
    private String trustedCrossOriginDomains;
    @Value("${server-timeout-in-minutes}")
    private String serverTimeoutInMinutes;

    @Value("${dynamic-property-evaluator-plugins-directory}")
    private String dynamicPropertyEvaluatorPluginsDirectory;

    @Value("${managed-property-plugins-directory}")
    private String managedPropertyPluginsDirectory;

    @Autowired
    private IAuthenticationService authenticationService;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private DatabaseConfigurationContext dbConfigurationContext;

    @Autowired
    private AuthorizationConfig authorizationConfig;

    @Autowired
    @NotNull
    private SessionFactory hibernateSessionFactory;

    @Autowired
    private DynamicPropertyEvaluationScheduler dynamicPropertyScheduler;

    @Autowired
    private ConcurrentOperationLimiter concurrentOperationLimiter;

    @Autowired
    private SessionWorkspaceProvider sessionWorkspaceProvider;


    @Bean(name="propertyConfigurer")
    public ExposablePropertyPlaceholderConfigurer propertyConfigurer() {
        ExposablePropertyPlaceholderConfigurer configurer = new ExposablePropertyPlaceholderConfigurer();
        configurer.setLocation(new ClassPathResource("service.properties"));
        configurer.setSystemPropertiesModeName("SYSTEM_PROPERTIES_MODE_OVERRIDE");
        configurer.setIgnoreUnresolvablePlaceholders(true);
        return configurer;
    }

    @Bean(name = "properties-batch-manager")
    public PropertiesBatchManager propertiesBatchManager() {
        return new PropertiesBatchManager(managedPropertyEvaluatorFactory());
    }

    @Bean(name = "log-message-prefix-generator")
    public LogMessagePrefixGenerator logMessagePrefixGenerator() {
        return new LogMessagePrefixGenerator();
    }


    @Bean(name = "session-manager")
    public OpenBisSessionManager sessionManager() {
        OpenBisSessionManager manager = new OpenBisSessionManager(sessionFactory(),
                logMessagePrefixGenerator(),
                authenticationService,
                new RequestContextProviderAdapter(requestContextProvider()),
                sessionTimeout,
                sessionTimeoutNoLogin,
                daoFactory());
        manager.setUserForAnonymousLogin(userForAnonymousLogin);
        manager.setMaxNumberOfSessionsPerUser(maxNumberOfSessionsPerUser);
        manager.setUsersWithUnrestrictedNumberOfSessions(usersWithUnrestrictedNumberOfSessions);
        return manager;
    }

    @Bean(name="remote-host-validator")
    public WhiteListBasedRemoteHostValidator remoteHostValidator() {
        return new WhiteListBasedRemoteHostValidator(acceptedRemoteHostsForIdentityChange);
    }


    @Bean(name = "transaction-manager")
    public OpenBISHibernateTransactionManager transactionManager() {
        return new OpenBISHibernateTransactionManager(daoFactory(),
                entityValidationFactory(), dynamicPropertyCalculatorFactory(), managedPropertyEvaluatorFactory(), sessionManager());
    }

    @Bean(name = "no-authorization")
    public NoAuthorization noAuthorization() {
        return new NoAuthorization();
    }

    @Bean
    public DatabaseLastModificationAdvisor databaseLastModificationAdvisor() {
        return new DatabaseLastModificationAdvisor(lastModificationState());
    }

    @Bean(name = "exception-translator")
    public NextExceptionFallbackExceptionTranslator exceptionTranslator() {
        return new NextExceptionFallbackExceptionTranslator();
    }

    @Bean(name = "memory-monitor")
    public JMXMemoryMonitorSpringBean memoryMonitor() {
        return new JMXMemoryMonitorSpringBean();
    }

    @Bean(name = "dss-factory")
    public IDataStoreServiceFactory dssFactory() {
        return new DataStoreServiceFactory();
    }



    @Bean(name = "request-context-provider")
    public SpringRequestContextProvider requestContextProvider() {
        return new SpringRequestContextProvider();
    }

    @Bean(name = "display-settings-provider")
    public DisplaySettingsProvider displaySettingsProvider() {
        return new DisplaySettingsProvider();
    }

    @Bean(name = "last-modification-state")
    public LastModificationState lastModificationState() {
        return new LastModificationState();
    }

    @Bean(name = "sql-executor")
    public HibernateSQLExecutor sqlExecutor() {
        return new HibernateSQLExecutor();
    }

    @Bean(name = "postgres-search-dao")
    public PostgresSearchDAO postgresSearchDAO() {
        return new PostgresSearchDAO(sqlExecutor());
    }

    @Bean(name = "information-provider-dao")
    public PostgresAuthorisationInformationProviderDAO informationProviderDAO() {
        return new PostgresAuthorisationInformationProviderDAO(sqlExecutor());
    }

    @Bean(name = "identity-translator")
    public IdentityMapper identityTranslator() {
        return new IdentityMapper();
    }

    @Bean(name = "entity-history-creator")
    public EntityHistoryCreator entityHistoryCreator() {
        EntityHistoryCreator ec = new EntityHistoryCreator();
        ec.setEnabled(entityHistoryEnabled);
        return ec;
    }


//
//    @Bean(name = "concurrent-operation-limiter")
//    public ConcurrentOperationLimiter concurrentOperationLimiter() {
//        return new ConcurrentOperationLimiter();
//    }

    @Bean(name="relationship-service")
    public IRelationshipService relationshipService() {
        RelationshipService service =  new RelationshipService();
        service.setDaoFactory((DAOFactory) daoFactory());
        return service;
    }

    @Bean(name="entity-operation-checker")
    public IEntityOperationChecker entityOperationChecker() {
        return new EntityOperationChecker();
    }

    @Bean(name="etl-entity-operation-checker")
    public IETLEntityOperationChecker etlEntityOperationChecker() {
        return new ETLEntityOperationChecker();
    }

    @Bean(name="tracking-server")
    public TrackingServer trackingServer() {
        return new TrackingServer(sessionManager(), daoFactory(), commonBusinessObjectFactory());
    }

    @Bean(name="service-conversation-client-manager")
    public IServiceConversationClientManagerLocal serviceConversationClientManager() {
        return new ServiceConversationClientManager();
    }

    @Bean(name="service-conversation-server-manager")
    public ServiceConversationServerManager serviceConversationServerManager() {
        ServiceConversationServerManager manager = new ServiceConversationServerManager();
        manager.setEtlService(etlService());
        return manager;
    }

    @Bean(name="etl-service")
    public ServiceForDataStoreServer etlService() {
        ServiceForDataStoreServer service =  new ServiceForDataStoreServer(authenticationService,
                sessionManager(),
                daoFactory(),
                commonBusinessObjectFactory(),
                dssFactory(),
                trustedOriginDomainProvider(),
                etlEntityOperationChecker(),
                dataStoreServiceRegistrator(),
                dssBasedDataSourceProvider(),
                managedPropertyEvaluatorFactory());
        service.setConversationClient(serviceConversationClientManager());
        service.setConversationServer(serviceConversationServerManager());
        service.setTimeout(serverTimeoutInMinutes);
        return service;
    }


    @Bean(name="jython-evaluator")
    public JythonEvaluatorSpringComponent jythonEvaluator() {
        return new JythonEvaluatorSpringComponent(propertyConfigurer());
    }

    @Bean(name="common-business-object-factory")
    public CommonBusinessObjectFactory commonBusinessObjectFactory() {
        return new CommonBusinessObjectFactory(
                daoFactory(),
                dssFactory(),
                relationshipService(),
                entityOperationChecker(),
                serviceConversationClientManager(),
                managedPropertyEvaluatorFactory(),
                multiplexer(),
                jythonEvaluatorPool(),
                entityHistoryCreator());
    }

    @Bean(name="data-store-service-registrator")
    public DataStoreServiceRegistrator dataStoreServiceRegistrator() {
        return new DataStoreServiceRegistrator(daoFactory());
    }

    @Bean(name="dss-based-data-source-provider")
    public DataStoreServerBasedDataSourceProvider dssBasedDataSourceProvider() {
        return new DataStoreServerBasedDataSourceProvider(daoFactory(), dssDataSourceMappingFilePath);
    }

    @Bean(name = "dao-factory")
    public IDAOFactory daoFactory() {
        System.out.println("Creating DAO factory");
        var factory = new DAOFactory(dbConfigurationContext, hibernateSessionFactory, dynamicPropertyScheduler, entityHistoryCreator(), authorizationConfig);
        return factory;
    }

    @Bean(name = "jython-evaluator-pool")
    public IJythonEvaluatorPool jythonEvaluatorPool() {
        return new JythonEvaluatorPool(daoFactory(), jythonEvaluatorPoolSize);
    }

    @Bean(name = "session-factory")
    public ch.systemsx.cisd.openbis.generic.server.SessionFactory sessionFactory() {
        return new ch.systemsx.cisd.openbis.generic.server.SessionFactory(daoFactory(), dssFactory(), sessionWorkspaceProvider);
    }

    @Bean(name = "entity-validation-factory")
    public EntityValidatorFactory entityValidationFactory() {
        return new EntityValidatorFactory(entityValidationPluginsDirectory, jythonEvaluatorPool());
    }

    @Bean(name = "hot-deployment-plugin-container")
    public HotDeploymentController hotDeploymentController() {
        return new HotDeploymentController(commonServer(), entityValidationFactory(), dynamicPropertyCalculatorFactory(), managedPropertyEvaluatorFactory());
    }


    @Bean(name="common-server")
    public CommonServer commonServer() {
        CommonServer server = new CommonServer(
                authenticationService,
                sessionManager(),
                daoFactory(),
                commonBusinessObjectFactory(),
                dataStoreServiceRegistrator(),
                lastModificationState(),
                entityValidationFactory(),
                dynamicPropertyCalculatorFactory(),
                managedPropertyEvaluatorFactory(),
                concurrentOperationLimiter);
        server.setOpenbisSupportEmail(openbisSupportEmail);
        server.setDefaultPutDataStoreServerCode(defaultPutDataStoreServerCode);
        return server;
    }

    @Bean(name="managed-property-evaluator-factory")
    public ManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory() {
        return new ManagedPropertyEvaluatorFactory(managedPropertyPluginsDirectory, jythonEvaluatorPool());
    }

    @Bean(name="dynamic-property-calculator-factory")
    public DynamicPropertyCalculatorFactory dynamicPropertyCalculatorFactory() {
        return new DynamicPropertyCalculatorFactory(dynamicPropertyEvaluatorPluginsDirectory, jythonEvaluatorPool());
    }


    @Bean(name="web-client-configuration-provider")
    public WebClientConfigurationProvider webClientConfigurationProvider() {
        return new WebClientConfigurationProvider(webClientConfigurationFile);
    }


    @Bean(name="common-service")
    public CommonClientService commonService() {
        CommonClientService service =  new CommonClientService(commonServer(), requestContextProvider());
        service.setCifexURL(cifexURL);
        service.setCifexRecipient(cifexRecipient);
        service.setOnlineHelpGenericRootURL(onlineHelpGenericRootURL);
        service.setOnlineHelpGenericPageTemplate(onlineHelpGenericPageTemplate);
        service.setOnlineHelpSpecificRootURL(onlineHelpSpecificRootURL);
        service.setOnlineHelpSpecificPageTemplate(onlineHelpSpecificPageTemplate);
        return service;

    }



    @Bean(name="registration-queue")
    public ConsumerQueue registrationQueue() {
        return new ConsumerQueue(mailClientParameters());
    }


    @Bean(name = "mail-client-parameters")
    public MailClientParameters mailClientParameters() {
        MailClientParameters parameters = new MailClientParameters();
        parameters.setFrom(mailFrom);
        parameters.setSmtpHost(smtpHost);
        parameters.setSmtpPort(smtpPort);
        parameters.setSmtpUser(smtpUser);
        parameters.setSmtpPassword(smtpPassword);
        parameters.setTestAddress(testAddress);
        return parameters;
    }

    @Bean(name="maintenance-task-starter")
    public MaintenanceTaskStarter maintenanceTaskStarter() {
        return new MaintenanceTaskStarter();
    }

    @Bean(name="operation-listener-loader")
    public OperationListenerLoader operationListenerLoader() {
        return new OperationListenerLoader();
    }

    @Bean(name = "core-plugin-registrator")
    public CorePluginRegistrator corePluginRegistrator() {
        CorePluginRegistrator registrator = new CorePluginRegistrator();
        registrator.setCommonServer(commonServer()); // Assuming commonServer is another bean defined in your configuration
        registrator.setPluginsFolderName(pluginsFolderName);
        registrator.setEnabledTechnologies(enabledTechnologies);
        registrator.setDisabledMasterDataInitialization(disabledMasterDataInitialization);
        return registrator;
    }

    @Bean(name="trusted-origin-domain-provider")
    public TrustedCrossOriginDomainsProvider trustedOriginDomainProvider() {
        return new TrustedCrossOriginDomainsProvider(trustedCrossOriginDomains);
    }

    @Bean(name = "objectMapper-v1")
    public GenericObjectMapper objectMapperV1() {
        return new GenericObjectMapper();
    }

    @Bean(name = "objectMapper-v3")
    @Primary
    public ch.ethz.sis.openbis.generic.server.sharedapi.v3.json.GenericObjectMapper objectMapperV3() {
        return new ch.ethz.sis.openbis.generic.server.sharedapi.v3.json.GenericObjectMapper();
    }


    @Bean
    public AutowiredAnnotationBeanPostProcessor autowiredAnnotationBeanPostProcessor() {
        return new AutowiredAnnotationBeanPostProcessor();
    }

    @Bean
    public AnnotationBeanPostProcessorIgnoringMissingBeans annotationBeanPostProcessorIgnoringMissingBeans() {
        return new AnnotationBeanPostProcessorIgnoringMissingBeans();
    }

    @Bean(name="multiplexer")
    public IMultiplexer multiplexer() {
        return new ThreadPoolMultiplexer("multiplexer-thread-pool");
    }

    @Bean(name="cache-manager")
    public EhCacheCacheManager cacheManager() {
        EhCacheCacheManager manager =  new EhCacheCacheManager();
        manager.setCacheManager(ehCache());
        return manager;
    }

    @Bean(name="ehcache")
    public net.sf.ehcache.CacheManager ehCache() {
        return net.sf.ehcache.CacheManager.create();
    }




}

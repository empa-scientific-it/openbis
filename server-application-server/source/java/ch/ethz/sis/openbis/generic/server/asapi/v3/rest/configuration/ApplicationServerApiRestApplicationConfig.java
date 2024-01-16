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

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
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
import ch.systemsx.cisd.dbmigration.ISqlScriptProvider;
import ch.systemsx.cisd.openbis.common.spring.AnnotationBeanPostProcessorIgnoringMissingBeans;
import ch.systemsx.cisd.openbis.generic.client.web.server.CommonClientService;
import ch.systemsx.cisd.openbis.generic.client.web.server.queue.ConsumerQueue;
import ch.systemsx.cisd.openbis.generic.server.*;
import ch.systemsx.cisd.openbis.generic.server.authorization.NoAuthorization;
import ch.systemsx.cisd.openbis.generic.server.business.*;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.NextExceptionFallbackExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.managed_property.EntityInformationProvider;
import ch.systemsx.cisd.openbis.generic.server.coreplugin.CorePluginRegistrator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.DataStoreServerBasedDataSourceProvider;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.DynamicPropertyEvaluationScheduler;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.DAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.DatabaseVersionHolder;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.OpenBISHibernateTransactionManager;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.SequenceNameMapper;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.deletion.EntityHistoryCreator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.IDynamicPropertyCalculatorFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.DynamicPropertyCalculatorFactory;
import ch.systemsx.cisd.openbis.generic.server.plugin.SampleServerPluginRegistry;
import ch.systemsx.cisd.openbis.generic.shared.IJythonEvaluatorPool;
import ch.systemsx.cisd.openbis.generic.shared.LogMessagePrefixGenerator;
import ch.systemsx.cisd.openbis.generic.shared.SessionWorkspaceProvider;
import ch.systemsx.cisd.openbis.generic.shared.WebClientConfigurationProvider;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.json.GenericObjectMapper;
import ch.systemsx.cisd.openbis.generic.shared.authorization.AuthorizationConfig;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LastModificationState;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.ManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IEntityInformationProvider;
import org.hibernate.validator.constraints.NotEmpty;
import org.jmock.lib.IdentityExpectationErrorTranslator;
import org.slf4j.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.ConstructorBinding;
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
import org.springframework.orm.hibernate5.LocalSessionFactoryBuilder;
import org.springframework.orm.jpa.vendor.HibernateJpaSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.entity_validation.EntityValidatorFactory;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.persistence.Entity;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Properties;

import static org.python27.bouncycastle.asn1.x500.style.RFC4519Style.owner;

//
//@Configuration
//@Validated
//class DBParameters{
//    private boolean createFromScratch = true;
//    private boolean scriptSingleStepMode = false;
//
//    private final String basicDatabaseName = "openbis";
//
//    private final String readOnlyGroup = "openbis_readonly";
//
//    @NotEmpty
//    @Value("${database.url-host-part}")
//    private String urlHostPart;
//
//    @NotEmpty
//    @Value("${database.admin-user}")
//    private String adminUser;
//
//    @NotEmpty
//    @Value("${database.owner}")
//    private String owner;
//
//
//
//    @NotEmpty
//    @Value("${database.owner-password}")
//    private String ownerPassword;
//
//    @NotEmpty
//    @Value("${database.admin-password}")
//    private String adminPassword;
//
//    @Value("${database.kind}")
//    private String databaseKind;
//
//    @NotEmpty
//    @Value("${database.engine}")
//    private String databaseEngineCode;
//
//    @NotEmpty
//    @Value("${database.valid-versions}")
//    private String validVersions;
//
//
//    @NotEmpty
//    @Value("${script-folder}/sql")
//    private String scriptFolder;
//
//    @NotEmpty
//    @Value("${database.script-folders}")
//    private String scriptFolders;
//
//    @NotEmpty
//    @Value("${database.instance}")
//    private String databaseInstance;
//
//    @NotEmpty
//    @Value("${database.max-wait-for-connection}")
//    private String  maxWaitForConnection;
//
//    @NotEmpty
//    @Value("${database.max-active-connections}")
//    private String maxActiveConnections = "20";
//
//    @NotEmpty
//    @Value("${database.max-idle-connections}")
//    private String maxIdleConnections = "20";
//
//    @NotEmpty
//    @Value("${database.active-connections-log-interval}")
//    private String activeConnectionsLogInterval = "3600";
//
//
//    public boolean isCreateFromScratch() {
//        return createFromScratch;
//    }
//
//    public boolean isScriptSingleStepMode() {
//        return scriptSingleStepMode;
//    }
//
//    public String getBasicDatabaseName() {
//        return basicDatabaseName;
//    }
//
//    public String getReadOnlyGroup() {
//        return readOnlyGroup;
//    }
//
//    public String getUrlHostPart() {
//        return urlHostPart;
//    }
//
//    public String getAdminUser() {
//        return adminUser;
//    }
//
//    public String getOwner() {
//        return owner;
//    }
//
//    public String getOwnerPassword() {
//        return ownerPassword;
//    }
//
//    public String getAdminPassword() {
//        return adminPassword;
//    }
//
//    public String getDatabaseKind() {
//        return databaseKind;
//    }
//
//    public String getDatabaseEngineCode() {
//        return databaseEngineCode;
//    }
//
//    public String getValidVersions() {
//        return validVersions;
//    }
//
//    public String getScriptFolder() {
//        return scriptFolder;
//    }
//
//    public String getScriptFolders() {
//        return scriptFolders;
//    }
//
//    public String getDatabaseInstance() {
//        return databaseInstance;
//    }
//
//    public String getMaxWaitForConnection() {
//        return maxWaitForConnection;
//    }
//
//    public String getMaxActiveConnections() {
//        return maxActiveConnections;
//    }
//
//    public String getMaxIdleConnections() {
//        return maxIdleConnections;
//    }
//
//    public String getActiveConnectionsLogInterval() {
//        return activeConnectionsLogInterval;
//    }
//
//    public boolean getCreateFromScratch() {
//        return createFromScratch;
//    }
//
//    public boolean getScriptSingleStepMode() {
//        return scriptSingleStepMode;
//    }
//}
//
//
//
//
//class DBConfig {
//
//
//
//    private final DBParameters dbParameters;
//
//    private final SequenceNameMapper sequenceNameMapper;
//
//    public DBConfig(DBParameters dbParameters, SequenceNameMapper sequenceNameMapper) {
//        this.dbParameters = dbParameters;
//        this.sequenceNameMapper = sequenceNameMapper;
//    }
//
//    @PostConstruct
//    public void init() {
//        System.out.println("DatabaseProperties: " + this);
//    }
//
//    @Bean
//    SequenceNameMapper sequenceNameMapper() {
//        return new SequenceNameMapper();
//    }
//
//
//    @Bean
//    DatabaseConfigurationContext dbConfigurationContext() {
//        DatabaseConfigurationContext dbContext = new DatabaseConfigurationContext();
//        dbContext.setBasicDatabaseName(dbParameters.getBasicDatabaseName());
//        dbContext.setCreateFromScratch(dbParameters.getCreateFromScratch());
//        dbContext.setScriptSingleStepMode(dbParameters.getScriptSingleStepMode());
//        dbContext.setSequenceNameMapper(sequenceNameMapper());
//        dbContext.setUrlHostPart(dbParameters.getUrlHostPart());
//        dbContext.setAdminUser(dbParameters.getAdminUser());
//        dbContext.setOwner(dbParameters.getOwner());
//        dbContext.setReadOnlyGroup(dbParameters.getReadOnlyGroup());
//        dbContext.setPassword(dbParameters.getOwnerPassword());
//        dbContext.setAdminPassword(dbParameters.getAdminPassword());
//        dbContext.setDatabaseKind(dbParameters.getDatabaseKind());
//        dbContext.setDatabaseEngineCode(dbParameters.getDatabaseEngineCode());
//        dbContext.setValidVersions(dbParameters.getValidVersions());
//        dbContext.setScriptFolder(dbParameters.getScriptFolder());
//        dbContext.setScriptFolders(dbParameters.getScriptFolders());
//        dbContext.setDatabaseInstance(dbParameters.getDatabaseInstance());
//        dbContext.setMaxWaitForConnectionProp(dbParameters.getMaxWaitForConnection());
//        dbContext.setMaxActiveConnectionsProp(dbParameters.getMaxActiveConnections());
//        dbContext.setMaxIdleConnectionsProp(dbParameters.getMaxIdleConnections());
//        dbContext.setActiveConnectionsLogIntervalProp(dbParameters.getActiveConnectionsLogInterval());
//        return dbContext;
//    }
//
//    @Bean
//    public DataSource dataSource(DatabaseConfigurationContext dbConfigurationContext) {
//        return dbConfigurationContext.getDataSource();
//    }
//
//
//    @Bean
//    public ISqlScriptProvider sqlScriptProvider() {
//       return  ch.systemsx.cisd.dbmigration.DBMigrationEngine.createOrMigrateDatabaseAndGetScriptProvider(dbConfigurationContext(), DatabaseVersionHolder.getDatabaseVersion(), DatabaseVersionHolder.getDatabaseFullTextSearchDocumentVersion(), DatabaseVersionHolder.getReleasePatchesVersion());
//    }
//}
//
//
//@Configuration
//class CrowdConfig {
//
//    @Value("${crowd.service.host}")
//    private String host;
//
//    @Value("${crowd.service.port}")
//    private String port;
//
//    @Value("${crowd.service.application.name}")
//    private String applicationName;
//
//    @Value("${crowd.service.application.password}")
//    private String applicationPassword;
//
//    @Value("${crowd.service.application.timeout}")
//    private String applicationTimeout;
//
//
//    @Bean
//    public CrowdConfiguration crowConfiguration() {
//        var configuration = new CrowdConfiguration();
//        configuration.setHost(host);
//        configuration.setPortStr(port);
//        configuration.setApplication(applicationName);
//        configuration.setApplicationPassword(applicationPassword);
//        configuration.setTimeoutStr(applicationTimeout);
//        return configuration;
//    }
//
//    @Bean
//    public CrowdAuthenticationService crowdAuthenticationService() {
//        return new CrowdAuthenticationService(crowConfiguration());
//    }
//
//}
//
//@Configuration
//class LDAPConfig {
//
//    @Value("${ldap.server.url}")
//    private String serverUrl;
//
//    @Value("${ldap.security.protocol}")
//    private String securityProtocol;
//
//    @Value("${ldap.security.authentication-method}")
//    private String securityAuthenticationMethod;
//
//    @Value("${ldap.security.principal.distinguished.name}")
//    private String securityPrincipalDistinguishedName;
//
//    @Value("${ldap.security.principal.password}")
//    private String securityPrincipalPassword;
//
//    @Value("${ldap.referral}")
//    private String referral;
//
//    @Value("${ldap.searchBase}")
//    private String searchBase;
//
//    @Value("${ldap.attributenames.user.id}")
//    private String userIdAttributeName;
//
//    @Value("${ldap.attributenames.email}")
//    private String emailAttributeName;
//
//    @Value("${ldap.attributenames.first.name}")
//    private String firstNameAttributeName;
//
//    @Value("${ldap.attributenames.last.name}")
//    private String lastNameAttributeName;
//
//    @Value("${ldap.queryEmailForAliases}")
//    private String queryEmailForAliases;
//
//    @Value("${ldap.queryTemplate}")
//    private String queryTemplate;
//
//    @Value("${ldap.maxRetries}")
//    private String maxRetriesStr;
//
//    @Value("${ldap.timeout}")
//    private String timeoutStr;
//
//    @Value("${ldap.timeToWaitAfterFailure}")
//    private String timeToWaitAfterFailureStr;
//
//    @Bean
//    public LDAPDirectoryConfiguration ldapDirectoryConfiguration() {
//        var configuration = new LDAPDirectoryConfiguration();
//        configuration.setServerUrl(serverUrl);
//        configuration.setSecurityProtocol(securityProtocol);
//        configuration.setSecurityAuthenticationMethod(securityAuthenticationMethod);
//        configuration.setSecurityPrincipalDistinguishedName(securityPrincipalDistinguishedName);
//        configuration.setSecurityPrincipalPassword(securityPrincipalPassword);
//        configuration.setReferral(referral);
//        configuration.setSearchBase(searchBase);
//        configuration.setUserIdAttributeName(userIdAttributeName);
//        configuration.setEmailAttributeName(emailAttributeName);
//        configuration.setFirstNameAttributeName(firstNameAttributeName);
//        configuration.setLastNameAttributeName(lastNameAttributeName);
//        configuration.setQueryEmailForAliases(queryEmailForAliases);
//        configuration.setQueryTemplate(queryTemplate);
//        configuration.setMaxRetriesStr(maxRetriesStr);
//        configuration.setTimeoutStr(timeoutStr);
//        configuration.setTimeToWaitAfterFailureStr(timeToWaitAfterFailureStr);
//        return configuration;
//    }
//
//    @Bean
//    public ch.systemsx.cisd.authentication.ldap.LDAPAuthenticationService ldapAuthenticationService() {
//        return new ch.systemsx.cisd.authentication.ldap.LDAPAuthenticationService(ldapDirectoryConfiguration());
//    }
//}
//
//@Configuration
//@Import({CrowdConfig.class, LDAPConfig.class})
//class GenericConfiguration{
//
//    private final CrowdConfig crowdConfig;
//
//    private final LDAPConfig ldapConfig;
//
//    @Value("${authentication.cache.time}")
//    private String cacheTime;
//
//    @Value("${authentication.cache.time.no-revalidation}")
//    private String cacheTimeNoRevalidation;
//
//    @Bean
//    static public ExposablePropertyPlaceholderConfigurer propertyConfigurer() {
//        ExposablePropertyPlaceholderConfigurer configurer = new ExposablePropertyPlaceholderConfigurer();
//        configurer.setLocation(new ClassPathResource("service.properties"));
//        return configurer;
//    }
//
//    public GenericConfiguration(CrowdConfig crowdConfig, LDAPConfig ldapConfig) {
//        this.crowdConfig = crowdConfig;
//        this.ldapConfig = ldapConfig;
//    }
//
//
//    @Bean(name="file-authentication-service")
//    public ch.systemsx.cisd.authentication.file.FileAuthenticationService fileAuthenticationService() {
//        return new ch.systemsx.cisd.authentication.file.FileAuthenticationService("etc/passwd");
//    }
//
//    @Bean(name="file-crowd-authentication-service")
//    public IAuthenticationService fileCrowdAuthenticationService() {
//        return new StackedAuthenticationService(List.of(fileAuthenticationService(), crowdConfig.crowdAuthenticationService()));
//    }
//
//    @Bean(name="file-ldap-authentication-service")
//    public IAuthenticationService fileLdapAuthenticationService() {
//        return new StackedAuthenticationService(List.of(fileAuthenticationService(), ldapConfig.ldapAuthenticationService()));
//    }
//
//    @Bean(name="ldap-crowd-authentication-service")
//    public IAuthenticationService ldapCrowdAuthenticationService() {
//        return new StackedAuthenticationService(List.of(ldapConfig.ldapAuthenticationService(), crowdConfig.crowdAuthenticationService()));
//    }
//
//    @Bean(name="file-ldap-crowd-caching-authentication-service")
//    public IAuthenticationService fileLdapCrowdCachingAuthenticationService() {
//        return new StackedAuthenticationService(List.of(fileAuthenticationService(), ldapConfig.ldapAuthenticationService(), crowdConfig.crowdAuthenticationService()));
//    }
//
//    @Bean(name="authentication-cache-configuration")
//    public CachingAuthenticationConfiguration authenticationCacheConfiguration() {
//        var config = new CachingAuthenticationConfiguration();
//        config.setDelegate(ldapCrowdAuthenticationService());
//        config.setCacheTimeStr(cacheTime);
//        config.setCacheTimeNoRevalidationStr(cacheTimeNoRevalidation);
//        config.setPasswordCacheFile("etc/passwd_cache");
//
//        return config;
//    }
//
//    @Bean
//    public SpringRequestContextProvider requestContextProvider() {
//        return new SpringRequestContextProvider();
//    }
//
//
//}
//
//
//
//@Configuration
//class SearchManagerConfig {
//
//    private PostgresSearchDAO postgresSearchDao;
//
//    private PostgresAuthorisationInformationProviderDAO informationProviderDao;
//
//    private IdentityMapper identityTranslator;
//
//    private HibernateSQLExecutor sqlExecutor;
//
//
//    public SearchManagerConfig(PostgresSearchDAO postgresSearchDao, PostgresAuthorisationInformationProviderDAO informationProviderDao, IdentityMapper identityTranslator, HibernateSQLExecutor sqlExecutor) {
//        this.postgresSearchDao = postgresSearchDao;
//        this.informationProviderDao = informationProviderDao;
//        this.identityTranslator = identityTranslator;
//        this.sqlExecutor = sqlExecutor;
//    }
//
//
//    @Bean
//    public SampleSearchManager sampleSearchManager() {
//        return createSearchManager(postgresSearchDao, informationProviderDao, identityTranslator, SampleSearchManager.class);
//    }
//
//    @Bean
//    public SampleContainerSearchManager sampleContainerSearchManager() {
//        return createSearchManager(postgresSearchDao, informationProviderDao, identityTranslator, SampleContainerSearchManager.class);
//    }
//
//    @Bean
//    public ExperimentSearchManager experimentSearchManager() {
//        return createSearchManager(postgresSearchDao, informationProviderDao, identityTranslator, ExperimentSearchManager.class);
//    }
//
//    @Bean
//    public ProjectSearchManager projectSearchManager() {
//        return createSearchManager(postgresSearchDao, informationProviderDao, identityTranslator, ProjectSearchManager.class);
//    }
//
//    @Bean
//    public SpaceSearchManager spaceSearchManager() {
//        return createSearchManager(postgresSearchDao, informationProviderDao, identityTranslator, SpaceSearchManager.class);
//    }
//
//    @Bean
//    public ContentCopySearchManager contentCopySearchManager() {
//        return createSearchManager(postgresSearchDao, informationProviderDao, identityTranslator, ContentCopySearchManager.class);
//    }
//
//    @Bean
//    public DataSetSearchManager datasetSearchManager() {
//        return createSearchManager(postgresSearchDao, informationProviderDao, identityTranslator, DataSetSearchManager.class);
//    }
//
//    @Bean
//    public DataSetTypeSearchManager dataSetTypeSearchManager() {
//        return createSearchManager(postgresSearchDao, informationProviderDao, identityTranslator, DataSetTypeSearchManager.class);
//    }
//
//    @Bean
//    public ExperimentTypeSearchManager experimentTypeSearchManager() {
//        return createSearchManager(postgresSearchDao, informationProviderDao, identityTranslator, ExperimentTypeSearchManager.class);
//    }
//
//    @Bean
//    public PersonSearchManager personSearchManager() {
//        return createSearchManager(postgresSearchDao, informationProviderDao, identityTranslator, PersonSearchManager.class);
//    }
//
//    @Bean
//    public TagSearchManager tagSearchManager() {
//        return createSearchManager(postgresSearchDao, informationProviderDao, identityTranslator, TagSearchManager.class);
//    }
//
//    @Bean
//    public SemanticAnnotationSearchManager semanticAnnotationSearchManager() {
//        return createSearchManager(postgresSearchDao, informationProviderDao, identityTranslator, SemanticAnnotationSearchManager.class);
//    }
//
//    @Bean
//    public PropertyTypeSearchManager propertyTypeSearchManager() {
//        return createSearchManager(postgresSearchDao, informationProviderDao, identityTranslator, PropertyTypeSearchManager.class);
//    }
//
//    @Bean
//    public LinkedDataSetKindSearchManager linkedDataSetKindSearchManager() {
//        return createSearchManager(postgresSearchDao, informationProviderDao, identityTranslator, LinkedDataSetKindSearchManager.class);
//    }
//
//    @Bean
//    public PhysicalDataSetKindSearchManager physicalDataSetKindSearchManager() {
//        return createSearchManager(postgresSearchDao, informationProviderDao, identityTranslator, PhysicalDataSetKindSearchManager.class);
//    }
//
//    @Bean
//    public ExternalDmsSearchManager externalDmsSearchManager() {
//        return createSearchManager(postgresSearchDao, informationProviderDao, identityTranslator, ExternalDmsSearchManager.class);
//    }
//
//    @Bean
//    public FileFormatTypeSearchManager fileFormatTypeSearchManager() {
//        return createSearchManager(postgresSearchDao, informationProviderDao, identityTranslator, FileFormatTypeSearchManager.class);
//    }
//
//    @Bean
//    public LocatorTypeSearchManager locatorTypeSearchManager() {
//        return createSearchManager(postgresSearchDao, informationProviderDao, identityTranslator, LocatorTypeSearchManager.class);
//    }
//
//    @Bean
//    public StorageFormatSearchManager storageFormatSearchManager() {
//        return createSearchManager(postgresSearchDao, informationProviderDao, identityTranslator, StorageFormatSearchManager.class);
//    }
//
//    @Bean
//    public MaterialSearchManager materialSearchManager() {
//        return createSearchManager(postgresSearchDao, informationProviderDao, identityTranslator, MaterialSearchManager.class);
//    }
//
//    @Bean
//    public MaterialTypeSearchManager materialTypeSearchManager() {
//        return createSearchManager(postgresSearchDao, informationProviderDao, identityTranslator, MaterialTypeSearchManager.class);
//    }
//
//    @Bean
//    public EntityTypeSearchManager entityTypeSearchManager() {
//        return createSearchManager(postgresSearchDao, informationProviderDao, identityTranslator, EntityTypeSearchManager.class);
//    }
//
//    @Bean
//    public GlobalSearchManager globalSearchManager() {
//        return new GlobalSearchManager(informationProviderDao, postgresSearchDao);
//    }
//
//    @Bean
//    public PropertyAssignmentSearchDAO propertyAssignmentSearchDAO() {
//        return new PropertyAssignmentSearchDAO(sqlExecutor);
//    }
//
//    @Bean
//    public PropertyAssignmentSearchManager propertyAssignmentSearchManager() {
//        return new PropertyAssignmentSearchManager(postgresSearchDao, informationProviderDao, identityTranslator, propertyAssignmentSearchDAO());
//    }
//
//    // Add more @Bean methods for other search managers as needed
//
//    private <T extends AbstractSearchManager> T createSearchManager(PostgresSearchDAO postgresSearchDao,
//                                                                    PostgresAuthorisationInformationProviderDAO informationProviderDao,
//                                                                    IdentityMapper identityTranslator,
//                                                                    Class<T> searchManagerClass) {
//        try {
//            java.lang.reflect.Constructor<T> constructor = searchManagerClass.getConstructor(PostgresSearchDAO.class, PostgresAuthorisationInformationProviderDAO.class, IdentityExpectationErrorTranslator.class);
//            return constructor.newInstance(postgresSearchDao, informationProviderDao, identityTranslator);
//        } catch (Exception e) {
//            // Handle or log the exception
//            throw new RuntimeException("Error creating search manager", e);
//        }
//    }
//}
//
//
//@Configuration
//@ComponentScan(basePackages = "ch.systemsx.cisd.openbis.generic.shared.dto", includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Entity.class))
//class HibernateConfig {
//
//    private DataSource dataSource;
//
//    private EntityManagerFactory entityManagerFactory;
//
//
//    public HibernateConfig(DataSource dataSource, EntityManagerFactory entityManagerFactory) {
//        this.dataSource = dataSource;
//        this.entityManagerFactory = entityManagerFactory;
//    }
//
//    @Bean(name="hibernate-session-factory")
//    public SessionFactory hibernateSessionFactory() {
//        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
//        sessionFactory.setDataSource(dataSource);
//        sessionFactory.setPackagesToScan("ch.systemsx.cisd.openbis.generic.shared.dto");
//        sessionFactory.setHibernateProperties(hibernateProperties());
//        return (SessionFactory) sessionFactory;
//    }
//
//
//
//    @Bean
//    public PlatformTransactionManager hibernateTransactionManager() {
//        return new HibernateTransactionManager((SessionFactory) hibernateSessionFactory());
//    }
//
//    @Bean
//    DynamicPropertyEvaluationScheduler dynamicPropertyEvaluationScheduler() {
//        return new DynamicPropertyEvaluationScheduler();
//    }
//
//    private Properties hibernateProperties() {
//        Properties properties = new Properties();
//        properties.setProperty("hibernate.current_session_context_class", "org.springframework.orm.hibernate5.SpringSessionContext");
//        properties.setProperty("hibernate.cache.use_second_level_cache", "false");
//        properties.setProperty("hibernate.cache.provider_class", "org.hibernate.cache.EhCacheProvider");
//        properties.setProperty("hibernate.max_fetch_depth", "4");
//        // Uncomment the following lines for debugging Hibernate SQL queries
//        // properties.setProperty("hibernate.show_sql", "true");
//        // properties.setProperty("hibernate.use_sql_comments", "true");
//        // properties.setProperty("hibernate.format_sql", "true");
//        return properties;
//    }
//
//    @Bean
//    public DynamicPropertyEvaluationScheduler dynamicPropertyScheduler() {
//        return new DynamicPropertyEvaluationScheduler();
//    }
//}
//
//@Configuration
//@Import({GenericConfiguration.class})
//class AuthenticationConfig {
////    @Autowired
//    private  BeanFactory beanFactory;
////    @Resource
////    private IAuthenticationService fileLdapCrowdCachingAuthenticationService;
////
////    @Resource
////    private IAuthenticationService fileLdapAuthenticationService;
////
////    @Resource
////    private IAuthenticationService fileCrowdAuthenticationService;
////
////    @Resource
////    private IAuthenticationService ldapCrowdAuthenticationService;
////
////    @Resource
////    private IAuthenticationService ldapAuthenticationService;
//
//    public AuthenticationConfig(BeanFactory beanFactory) {
//        this.beanFactory = beanFactory;
//    }
//
//
//
//    @Value("${authentication-service}")
//    private String authenticationServiceBeanName;
//
//    @Bean(name = "dummy-authentication-service")
//    public IAuthenticationService dummyAuthenticationService() {
//        return new DummyAuthenticationService();
//    }
//
//    @Bean(name = "no-authentication-service")
//    public IAuthenticationService noAuthenticationService() {
//        return new NullAuthenticationService();
//    }
//
//    @Bean(name = "authentication-service")
//    public IAuthenticationService authenticationService() {
//        return beanFactory.getBean(authenticationServiceBeanName, IAuthenticationService.class);
//    }
//
//
//}
//
//
//@Configuration
//@Validated
//class ApplicationParameters {
//    @Value("${entity-validation-plugins-directory}")
//    private String entityValidationPluginsDirectory;
//
//    @Value("${jython-evaluator-pool-size}")
//    private String jythonEvaluatorPoolSize;
//
//    @Value("${entity-history.enabled}")
//    private String entityHistoryEnabled;
//
//    @Value("${authentication-service}")
//    private String authenticationServiceReference;
//
//    @Value("${session-timeout}")
//    private int sessionTimeout;
//
//    @Value("${session-timeout-no-login}")
//    private String sessionTimeoutNoLogin;
//
//    @Value("${user-for-anonymous-login}")
//    private String userForAnonymousLogin;
//
//    @Value("${max-number-of-sessions-per-user}")
//    private String maxNumberOfSessionsPerUser;
//
//    @Value("${users-with-unrestricted-number-of-sessions}")
//    private String usersWithUnrestrictedNumberOfSessions;
//
//
//    @Value("${accepted-remote-hosts-for-identity-change}")
//    private String acceptedRemoteHostsForIdentityChange;
//
//    @Value("${dss-data-source-mapping-file-path}")
//    private String dssDataSourceMappingFilePath = "etc/dss-datasource-mapping";
//
//
//    @Value("${openbis.support.email}")
//    private String openbisSupportEmail;
//
//    @Value("${dss-rpc.put.dss-code}")
//    private String defaultPutDataStoreServerCode;
//
//
//    @Value("${web-client.configuration.file}")
//    private String webClientConfigurationFile;
//
//
//    @Value("${cifex-url}")
//    private String cifexURL;
//
//    @Value("${cifex-recipient}")
//    private String cifexRecipient;
//
//    @Value("${onlinehelp.generic.root-url}")
//    private String onlineHelpGenericRootURL;
//
//    @Value("${onlinehelp.generic.page-template}")
//    private String onlineHelpGenericPageTemplate;
//
//    @Value("${onlinehelp.specific.root-url}")
//    private String onlineHelpSpecificRootURL;
//
//    @Value("${onlinehelp.specific.page-template}")
//    private String onlineHelpSpecificPageTemplate;
//
//    @Value("${mail.from}")
//    private String mailFrom;
//
//    @Value("${mail.smtp.host}")
//    private String smtpHost;
//
//    @Value("${mail.smtp.port}")
//    private String smtpPort;
//
//    @Value("${mail.smtp.user}")
//    private String smtpUser;
//
//    @Value("${mail.smtp.password}")
//    private String smtpPassword;
//
//    @Value("${mail.smtp.address}")
//    private String testAddress;
//
//
//    @Value("${core-plugins-folder}")
//    private String pluginsFolderName;
//
//    @Value("${enabled-modules}")
//    private String enabledTechnologies;
//
//    @Value("${disabled-master-data-initialization}")
//    private String disabledMasterDataInitialization;
//
//    @Value("${trusted-cross-origin-domains}")
//    private String trustedCrossOriginDomains;
//    @Value("${server-timeout-in-minutes}")
//    private String serverTimeoutInMinutes;
//
//    @Value("${dynamic-property-evaluator-plugins-directory}")
//    private String dynamicPropertyEvaluatorPluginsDirectory;
//
//    @Value("${managed-property-plugins-directory}")
//    private String managedPropertyPluginsDirectory;
//
//    public ApplicationParameters(String entityValidationPluginsDirectory, String jythonEvaluatorPoolSize, String entityHistoryEnabled, String authenticationServiceReference, int sessionTimeout, String sessionTimeoutNoLogin, String userForAnonymousLogin, String maxNumberOfSessionsPerUser, String usersWithUnrestrictedNumberOfSessions, String acceptedRemoteHostsForIdentityChange, String dssDataSourceMappingFilePath, String openbisSupportEmail, String defaultPutDataStoreServerCode, String webClientConfigurationFile, String cifexURL, String cifexRecipient, String onlineHelpGenericRootURL, String onlineHelpGenericPageTemplate, String onlineHelpSpecificRootURL, String onlineHelpSpecificPageTemplate, String mailFrom, String smtpHost, String smtpPort, String smtpUser, String smtpPassword, String testAddress, String pluginsFolderName, String enabledTechnologies, String disabledMasterDataInitialization, String trustedCrossOriginDomains, String serverTimeoutInMinutes, String dynamicPropertyEvaluatorPluginsDirectory, String managedPropertyPluginsDirectory) {
//        this.entityValidationPluginsDirectory = entityValidationPluginsDirectory;
//        this.jythonEvaluatorPoolSize = jythonEvaluatorPoolSize;
//        this.entityHistoryEnabled = entityHistoryEnabled;
//        this.authenticationServiceReference = authenticationServiceReference;
//        this.sessionTimeout = sessionTimeout;
//        this.sessionTimeoutNoLogin = sessionTimeoutNoLogin;
//        this.userForAnonymousLogin = userForAnonymousLogin;
//        this.maxNumberOfSessionsPerUser = maxNumberOfSessionsPerUser;
//        this.usersWithUnrestrictedNumberOfSessions = usersWithUnrestrictedNumberOfSessions;
//        this.acceptedRemoteHostsForIdentityChange = acceptedRemoteHostsForIdentityChange;
//        this.dssDataSourceMappingFilePath = dssDataSourceMappingFilePath;
//        this.openbisSupportEmail = openbisSupportEmail;
//        this.defaultPutDataStoreServerCode = defaultPutDataStoreServerCode;
//        this.webClientConfigurationFile = webClientConfigurationFile;
//        this.cifexURL = cifexURL;
//        this.cifexRecipient = cifexRecipient;
//        this.onlineHelpGenericRootURL = onlineHelpGenericRootURL;
//        this.onlineHelpGenericPageTemplate = onlineHelpGenericPageTemplate;
//        this.onlineHelpSpecificRootURL = onlineHelpSpecificRootURL;
//        this.onlineHelpSpecificPageTemplate = onlineHelpSpecificPageTemplate;
//        this.mailFrom = mailFrom;
//        this.smtpHost = smtpHost;
//        this.smtpPort = smtpPort;
//        this.smtpUser = smtpUser;
//        this.smtpPassword = smtpPassword;
//        this.testAddress = testAddress;
//        this.pluginsFolderName = pluginsFolderName;
//        this.enabledTechnologies = enabledTechnologies;
//        this.disabledMasterDataInitialization = disabledMasterDataInitialization;
//        this.trustedCrossOriginDomains = trustedCrossOriginDomains;
//        this.serverTimeoutInMinutes = serverTimeoutInMinutes;
//        this.dynamicPropertyEvaluatorPluginsDirectory = dynamicPropertyEvaluatorPluginsDirectory;
//        this.managedPropertyPluginsDirectory = managedPropertyPluginsDirectory;
//    }
//
//    public String getEntityValidationPluginsDirectory() {
//        return entityValidationPluginsDirectory;
//    }
//
//    public String getJythonEvaluatorPoolSize() {
//        return jythonEvaluatorPoolSize;
//    }
//
//    public String getEntityHistoryEnabled() {
//        return entityHistoryEnabled;
//    }
//
//    public String getAuthenticationServiceReference() {
//        return authenticationServiceReference;
//    }
//
//    public int getSessionTimeout() {
//        return sessionTimeout;
//    }
//
//    public String getSessionTimeoutNoLogin() {
//        return sessionTimeoutNoLogin;
//    }
//
//    public String getUserForAnonymousLogin() {
//        return userForAnonymousLogin;
//    }
//
//    public String getMaxNumberOfSessionsPerUser() {
//        return maxNumberOfSessionsPerUser;
//    }
//
//    public String getUsersWithUnrestrictedNumberOfSessions() {
//        return usersWithUnrestrictedNumberOfSessions;
//    }
//
//    public String getAcceptedRemoteHostsForIdentityChange() {
//        return acceptedRemoteHostsForIdentityChange;
//    }
//
//    public String getDssDataSourceMappingFilePath() {
//        return dssDataSourceMappingFilePath;
//    }
//
//    public String getOpenbisSupportEmail() {
//        return openbisSupportEmail;
//    }
//
//    public String getDefaultPutDataStoreServerCode() {
//        return defaultPutDataStoreServerCode;
//    }
//
//    public String getWebClientConfigurationFile() {
//        return webClientConfigurationFile;
//    }
//
//    public String getCifexURL() {
//        return cifexURL;
//    }
//
//    public String getCifexRecipient() {
//        return cifexRecipient;
//    }
//
//    public String getOnlineHelpGenericRootURL() {
//        return onlineHelpGenericRootURL;
//    }
//
//    public String getOnlineHelpGenericPageTemplate() {
//        return onlineHelpGenericPageTemplate;
//    }
//
//    public String getOnlineHelpSpecificRootURL() {
//        return onlineHelpSpecificRootURL;
//    }
//
//    public String getOnlineHelpSpecificPageTemplate() {
//        return onlineHelpSpecificPageTemplate;
//    }
//
//    public String getMailFrom() {
//        return mailFrom;
//    }
//
//    public String getSmtpHost() {
//        return smtpHost;
//    }
//
//    public String getSmtpPort() {
//        return smtpPort;
//    }
//
//    public String getSmtpUser() {
//        return smtpUser;
//    }
//
//    public String getSmtpPassword() {
//        return smtpPassword;
//    }
//
//    public String getTestAddress() {
//        return testAddress;
//    }
//
//    public String getPluginsFolderName() {
//        return pluginsFolderName;
//    }
//
//    public String getEnabledTechnologies() {
//        return enabledTechnologies;
//    }
//
//    public String getDisabledMasterDataInitialization() {
//        return disabledMasterDataInitialization;
//    }
//
//    public String getTrustedCrossOriginDomains() {
//        return trustedCrossOriginDomains;
//    }
//
//    public String getServerTimeoutInMinutes() {
//        return serverTimeoutInMinutes;
//    }
//
//    public String getDynamicPropertyEvaluatorPluginsDirectory() {
//        return dynamicPropertyEvaluatorPluginsDirectory;
//    }
//
//    public String getManagedPropertyPluginsDirectory() {
//        return managedPropertyPluginsDirectory;
//    }
//}
//
//
//@Configuration
//@Import({DBConfig.class, HibernateConfig.class, AuthorizationConfig.class, AuthenticationConfig.class, SearchManagerConfig.class, GenericConfiguration.class})
//@ConstructorBinding
//class ApplicationConfig {
//
//
//    @NotNull
//    private final IAuthenticationService authenticationService;
//    @NotNull
//    private final DataSource dataSource;
//    @NotNull
//    private final DatabaseConfigurationContext dbConfigurationContext;
//    @NotNull
//    private final AuthorizationConfig authorizationConfig;
//
//    @NotNull
//    private final SessionFactory hibernateSessionFactory;
//
//    @NotNull
//    private final DynamicPropertyEvaluationScheduler dynamicPropertyScheduler;
//
//    @NotNull
//    private final ConcurrentOperationLimiter concurrentOperationLimiter;
//
//    @NotNull
//    private final SessionWorkspaceProvider sessionWorkspaceProvider;
//
//    @NotNull
//    private final IEntityInformationProvider entityInformationProvider;
//
//    @NotNull
//    private final ExposablePropertyPlaceholderConfigurer propertyConfigurer;
//
//    @NotNull
//    private final SpringRequestContextProvider requestContextProvider;
//
//    @NotNull
//    private final ApplicationParameters applicationParameters;
//
//    public ApplicationConfig(IAuthenticationService authenticationService, DataSource dataSource, DatabaseConfigurationContext dbConfigurationContext, AuthorizationConfig authorizationConfig, SessionFactory hibernateSessionFactory, DynamicPropertyEvaluationScheduler dynamicPropertyScheduler, ConcurrentOperationLimiter concurrentOperationLimiter, SessionWorkspaceProvider sessionWorkspaceProvider, IEntityInformationProvider entityInformationProvider, ExposablePropertyPlaceholderConfigurer propertyConfigurer, SpringRequestContextProvider requestContextProvider, ApplicationParameters applicationParameters) {
//        this.authenticationService = authenticationService;
//        this.dataSource = dataSource;
//        this.dbConfigurationContext = dbConfigurationContext;
//        this.authorizationConfig = authorizationConfig;
//        this.hibernateSessionFactory = hibernateSessionFactory;
//        this.dynamicPropertyScheduler = dynamicPropertyScheduler;
//        this.concurrentOperationLimiter = concurrentOperationLimiter;
//        this.sessionWorkspaceProvider = sessionWorkspaceProvider;
//        this.entityInformationProvider = entityInformationProvider;
//        this.propertyConfigurer = propertyConfigurer;
//        this.requestContextProvider = requestContextProvider;
//        this.applicationParameters = applicationParameters;
//    }
//
//
//
//    @Bean
//    public Logger logger() {
//        return org.slf4j.LoggerFactory.getLogger(this.getClass());
//    }
//
//
//    @Bean
//    public PropertiesBatchManager propertiesBatchManager() {
//        return new PropertiesBatchManager(managedPropertyEvaluatorFactory());
//    }
//
//    @Bean
//    public LogMessagePrefixGenerator logMessagePrefixGenerator() {
//        return new LogMessagePrefixGenerator();
//    }
//
//
//    @Bean
//    public OpenBisSessionManager sessionManager() {
//        OpenBisSessionManager manager = new OpenBisSessionManager(sessionFactory(),
//                logMessagePrefixGenerator(),
//                authenticationService,
//                new RequestContextProviderAdapter(requestContextProvider),
//                applicationParameters.getSessionTimeout(),
//                applicationParameters.getSessionTimeoutNoLogin(),
//                daoFactory());
//        manager.setUserForAnonymousLogin(applicationParameters.getUserForAnonymousLogin());
//        manager.setMaxNumberOfSessionsPerUser(applicationParameters.getMaxNumberOfSessionsPerUser());
//        manager.setUsersWithUnrestrictedNumberOfSessions(applicationParameters.getUsersWithUnrestrictedNumberOfSessions());
//        return manager;
//    }
//
//    @Bean
//    public WhiteListBasedRemoteHostValidator remoteHostValidator() {
//        return new WhiteListBasedRemoteHostValidator(applicationParameters.getAcceptedRemoteHostsForIdentityChange());
//    }
//
//
//    @Bean
//    public OpenBISHibernateTransactionManager transactionManager() {
//        return new OpenBISHibernateTransactionManager(daoFactory(),
//                entityValidationFactory(), dynamicPropertyCalculatorFactory(), managedPropertyEvaluatorFactory(), sessionManager());
//    }
//
//    @Bean
//    public NoAuthorization noAuthorization() {
//        return new NoAuthorization();
//    }
//
//    @Bean
//    public DatabaseLastModificationAdvisor databaseLastModificationAdvisor() {
//        return new DatabaseLastModificationAdvisor(lastModificationState());
//    }
//
//    @Bean
//    public NextExceptionFallbackExceptionTranslator exceptionTranslator() {
//        return new NextExceptionFallbackExceptionTranslator();
//    }
//
//    @Bean
//    public JMXMemoryMonitorSpringBean memoryMonitor() {
//        return new JMXMemoryMonitorSpringBean();
//    }
//
//    @Bean
//    public IDataStoreServiceFactory dssFactory() {
//        return new DataStoreServiceFactory();
//    }
//
//
//
//    @Bean
//    public DisplaySettingsProvider displaySettingsProvider() {
//        return new DisplaySettingsProvider();
//    }
//
//    @Bean
//    public LastModificationState lastModificationState() {
//        return new LastModificationState();
//    }
//
//    @Bean
//    public HibernateSQLExecutor sqlExecutor() {
//        return new HibernateSQLExecutor();
//    }
//
//    @Bean
//    public PostgresSearchDAO postgresSearchDAO() {
//        return new PostgresSearchDAO(sqlExecutor());
//    }
//
//    @Bean
//    public PostgresAuthorisationInformationProviderDAO informationProviderDAO() {
//        return new PostgresAuthorisationInformationProviderDAO(sqlExecutor());
//    }
//
//    @Bean
//    public IdentityMapper identityTranslator() {
//        return new IdentityMapper();
//    }
//
//    @Bean
//    public EntityHistoryCreator entityHistoryCreator() {
//        EntityHistoryCreator ec = new EntityHistoryCreator();
//        ec.setEnabled(applicationParameters.getEntityHistoryEnabled());
//        return ec;
//    }
//
//
////
////    @Bean(name = "concurrent-operation-limiter")
////    public ConcurrentOperationLimiter concurrentOperationLimiter() {
////        return new ConcurrentOperationLimiter();
////    }
//
//    @Bean
//    public IRelationshipService relationshipService() {
//        RelationshipService service =  new RelationshipService();
//        service.setDaoFactory((DAOFactory) daoFactory());
//        return service;
//    }
//
//    @Bean
//    public IEntityOperationChecker entityOperationChecker() {
//        return new EntityOperationChecker();
//    }
//
//    @Bean(name="etl-entity-operation-checker")
//    public IETLEntityOperationChecker etlEntityOperationChecker() {
//        return new ETLEntityOperationChecker();
//    }
//
//    @Bean(name="tracking-server")
//    public TrackingServer trackingServer() {
//        return new TrackingServer(sessionManager(), daoFactory(), commonBusinessObjectFactory());
//    }
//
//    @Bean(name="service-conversation-client-manager")
//    public IServiceConversationClientManagerLocal serviceConversationClientManager() {
//        return new ServiceConversationClientManager();
//    }
//
//    @Bean(name="service-conversation-server-manager")
//    public ServiceConversationServerManager serviceConversationServerManager() {
//        ServiceConversationServerManager manager = new ServiceConversationServerManager();
//        manager.setEtlService(etlService());
//        return manager;
//    }
//
//
//    @Bean(name="etl-service")
//    public ServiceForDataStoreServer etlService() {
//        ServiceForDataStoreServer service =  new ServiceForDataStoreServer(authenticationService,
//                sessionManager(),
//                daoFactory(),
//                commonBusinessObjectFactory(),
//                dssFactory(),
//                trustedOriginDomainProvider(),
//                etlEntityOperationChecker(),
//                dataStoreServiceRegistrator(),
//                dssBasedDataSourceProvider(),
//                managedPropertyEvaluatorFactory());
//        service.setConversationClient(serviceConversationClientManager());
//        service.setConversationServer(serviceConversationServerManager());
//        service.setTimeout(applicationParameters.getServerTimeoutInMinutes());
//        return service;
//    }
//
//
//    @Bean(name="jython-evaluator")
//    public JythonEvaluatorSpringComponent jythonEvaluator() {
//        return new JythonEvaluatorSpringComponent(propertyConfigurer);
//    }
//
////    @Bean(name="entity-information-provider")
////    public IEntityInformationProvider entityInformationProvider() {
////        return new EntityInformationProvider(daoFactory());
////    }
//
//    @Bean(name="common-business-object-factory")
//    public ICommonBusinessObjectFactory commonBusinessObjectFactory() {
//        return new CommonBusinessObjectFactory(
//                daoFactory(),
//                dssFactory(),
//                relationshipService(),
//                entityOperationChecker(),
//                serviceConversationClientManager(),
//                entityInformationProvider,
//                managedPropertyEvaluatorFactory(),
//                multiplexer(),
//                jythonEvaluatorPool(),
//                entityHistoryCreator());
//    }
//
//    @Bean(name="data-store-service-registrator")
//    public DataStoreServiceRegistrator dataStoreServiceRegistrator() {
//        return new DataStoreServiceRegistrator(daoFactory());
//    }
//
//    @Bean(name="dss-based-data-source-provider")
//    public DataStoreServerBasedDataSourceProvider dssBasedDataSourceProvider() {
//        return new DataStoreServerBasedDataSourceProvider(daoFactory(), applicationParameters.getDssDataSourceMappingFilePath());
//    }
//
//    @Bean(name = "dao-factory")
//    public IDAOFactory daoFactory() {
//        logger().info("Creating DAO factory with session factory: {}", hibernateSessionFactory);
//
//        var factory = new DAOFactory(dbConfigurationContext, (SessionFactory) hibernateSessionFactory, dynamicPropertyScheduler, entityHistoryCreator(), authorizationConfig);
//        return factory;
//    }
//
//    @Bean(name = "jython-evaluator-pool")
//    public IJythonEvaluatorPool jythonEvaluatorPool() {
//        return new JythonEvaluatorPool(daoFactory(), applicationParameters.getJythonEvaluatorPoolSize());
//    }
//
//    @Bean(name = "session-factory")
//    public ch.systemsx.cisd.openbis.generic.server.SessionFactory sessionFactory() {
//        return new ch.systemsx.cisd.openbis.generic.server.SessionFactory(daoFactory(), dssFactory(), sessionWorkspaceProvider);
//    }
//
//    @Bean(name = "entity-validation-factory")
//    public EntityValidatorFactory entityValidationFactory() {
//        return new EntityValidatorFactory(applicationParameters.getDssDataSourceMappingFilePath(), jythonEvaluatorPool());
//    }
//
//    @Bean(name = "hot-deployment-plugin-container")
//    public HotDeploymentController hotDeploymentController() {
//        return new HotDeploymentController(commonServer(), entityValidationFactory(), dynamicPropertyCalculatorFactory(), managedPropertyEvaluatorFactory());
//    }
//
//
//    @Bean(name="common-server")
//    public CommonServer commonServer() {
//        CommonServer server = new CommonServer(
//                authenticationService,
//                sessionManager(),
//                daoFactory(),
//                commonBusinessObjectFactory(),
//                dataStoreServiceRegistrator(),
//                lastModificationState(),
//                entityValidationFactory(),
//                dynamicPropertyCalculatorFactory(),
//                managedPropertyEvaluatorFactory(),
//                concurrentOperationLimiter);
//        server.setOpenbisSupportEmail(applicationParameters.getOpenbisSupportEmail());
//        server.setDefaultPutDataStoreServerCode(applicationParameters.getDefaultPutDataStoreServerCode());
//        return server;
//    }
//
//    @Bean(name="managed-property-evaluator-factory")
//    public IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory() {
//        return new ManagedPropertyEvaluatorFactory(applicationParameters.getManagedPropertyPluginsDirectory(), jythonEvaluatorPool());
//    }
//
//    @Bean(name="dynamic-property-calculator-factory")
//    public IDynamicPropertyCalculatorFactory dynamicPropertyCalculatorFactory() {
//        return new DynamicPropertyCalculatorFactory(applicationParameters.getDynamicPropertyEvaluatorPluginsDirectory(), jythonEvaluatorPool());
//    }
//
//
//    @Bean(name="web-client-configuration-provider")
//    public WebClientConfigurationProvider webClientConfigurationProvider() {
//        return new WebClientConfigurationProvider(applicationParameters.getWebClientConfigurationFile());
//    }
//
//
//    @Bean(name="common-service")
//    public CommonClientService commonService() {
//        CommonClientService service =  new CommonClientService(commonServer(), requestContextProvider);
//        service.setCifexURL(applicationParameters.getCifexURL());
//        service.setCifexRecipient(applicationParameters.getCifexRecipient());
//        service.setOnlineHelpGenericRootURL(applicationParameters.getOnlineHelpGenericRootURL());
//        service.setOnlineHelpGenericPageTemplate(applicationParameters.getOnlineHelpGenericPageTemplate());
//        service.setOnlineHelpSpecificRootURL(applicationParameters.getOnlineHelpSpecificRootURL());
//        service.setOnlineHelpSpecificPageTemplate(applicationParameters.getOnlineHelpSpecificPageTemplate());
//        return service;
//
//    }
//
//
//
//    @Bean(name="registration-queue")
//    public ConsumerQueue registrationQueue() {
//        return new ConsumerQueue(mailClientParameters());
//    }
//
//
//    @Bean(name = "mail-client-parameters")
//    public MailClientParameters mailClientParameters() {
//        MailClientParameters parameters = new MailClientParameters();
//        parameters.setFrom(applicationParameters.getMailFrom());
//        parameters.setSmtpHost(applicationParameters.getSmtpHost());
//        parameters.setSmtpPort(applicationParameters.getSmtpPort());
//        parameters.setSmtpUser(applicationParameters.getSmtpUser());
//        parameters.setSmtpPassword(applicationParameters.getSmtpPassword());
//        parameters.setTestAddress(applicationParameters.getTestAddress());
//        return parameters;
//    }
//
//    @Bean
//    public MaintenanceTaskStarter maintenanceTaskStarter() {
//        return new MaintenanceTaskStarter();
//    }
//
//    @Bean
//    public OperationListenerLoader operationListenerLoader() {
//        return new OperationListenerLoader();
//    }
//
//    @Bean
//    public CorePluginRegistrator corePluginRegistrator() {
//        CorePluginRegistrator registrator = new CorePluginRegistrator();
//        registrator.setCommonServer(commonServer()); // Assuming commonServer is another bean defined in your configuration
//        registrator.setPluginsFolderName(applicationParameters.getPluginsFolderName());
//        registrator.setEnabledTechnologies(applicationParameters.getEnabledTechnologies());
//        registrator.setDisabledMasterDataInitialization(applicationParameters.getDisabledMasterDataInitialization());
//        return registrator;
//    }
//
//    @Bean
//    public TrustedCrossOriginDomainsProvider trustedOriginDomainProvider() {
//        return new TrustedCrossOriginDomainsProvider(applicationParameters.getTrustedCrossOriginDomains());
//    }
//
//    @Bean
//    public GenericObjectMapper objectMapperV1() {
//        return new GenericObjectMapper();
//    }
//
//    @Bean
//    @Primary
//    public ch.ethz.sis.openbis.generic.server.sharedapi.v3.json.GenericObjectMapper objectMapperV3() {
//        return new ch.ethz.sis.openbis.generic.server.sharedapi.v3.json.GenericObjectMapper();
//    }
//
//
//    @Bean
//    public AutowiredAnnotationBeanPostProcessor autowiredAnnotationBeanPostProcessor() {
//        return new AutowiredAnnotationBeanPostProcessor();
//    }
//
//    @Bean
//    public AnnotationBeanPostProcessorIgnoringMissingBeans annotationBeanPostProcessorIgnoringMissingBeans() {
//        return new AnnotationBeanPostProcessorIgnoringMissingBeans();
//    }
//
//    @Bean
//    public IMultiplexer multiplexer() {
//        return new ThreadPoolMultiplexer("multiplexer-thread-pool");
//    }
//
//    @Bean
//    public EhCacheCacheManager cacheManager() {
//        EhCacheCacheManager manager =  new EhCacheCacheManager();
//        manager.setCacheManager(ehCache());
//        return manager;
//    }
//
//    @Bean
//    public net.sf.ehcache.CacheManager ehCache() {
//        return net.sf.ehcache.CacheManager.create();
//    }
//
//
//
//
//}

@Configuration
@Lazy
@ImportResource({"classpath:genericCommonContext.xml"})
@PropertySource(value="classpath:service.properties", ignoreResourceNotFound=true)
public class ApplicationServerApiRestApplicationConfig{
    @NotNull
    private IApplicationServerApi api;

    @Autowired
    ApplicationServerApiRestApplicationConfig(IApplicationServerApi api){
        this.api = api;
    }



}
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

package ch.systemsx.cisd.dbmigration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.jdbc.support.lob.LobHandler;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.db.ISequenceNameMapper;
import ch.systemsx.cisd.common.db.ISequencerHandler;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * Configuration context for database operations.
 * 
 * @author Franz-Josef Elmer
 */
public class DatabaseConfigurationContext implements DisposableBean
{
    private final static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DatabaseConfigurationContext.class);

    private ISequenceNameMapper sequenceNameMapper;

    private boolean sequenceUpdateNeeded;

    private String adminUser;

    private String adminPassword;

    private String scriptFolder;

    private List<String> scriptFolders;

    private String databaseKind;

    private DatabaseEngine databaseEngine;
    
    private String validVersions;

    private boolean createFromScratch;

    private boolean scriptSingleStepMode;

    private IDataSourceFactory dataSourceFactory = new BasicDataSourceFactory();

    private DataSource dataSource;

    private DataSource adminDataSource;

    private String owner;

    private String readOnlyGroup;

    private String readWriteGroup;

    private String password;

    private String basicDatabaseName;

    private String databaseName;

    private String urlHostPart;

    private String databaseInstance;

    public DatabaseConfigurationContext()
    {
        setOwner(null);
        setPassword("");
        setSequenceUpdateNeeded(true);
    }

    @PostConstruct
    private void setTestEnvironmentHostOrConfigured() {
        this.urlHostPart = DatabaseEngine.getTestEnvironmentHostOrConfigured(this.urlHostPart);
    }

    public final void initDataSourceFactory(final IDataSourceFactory factory)
    {
        this.dataSourceFactory = factory;
    }

    private final static void closeConnection(final DataSource dataSource)
    {
        if (dataSource != null)
        {
            try
            {
                if (dataSource instanceof BasicDataSource)
                {
                    ((BasicDataSource) dataSource).close();
                }
                if (dataSource instanceof DisposableBean)
                {
                    ((DisposableBean) dataSource).destroy();
                }
            } catch (final Exception ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }
    }

    private void checkDatabaseEngine()
    {
        if (databaseEngine == null)
        {
            throw new ConfigurationFailureException("No db engine defined.");
        }
    }

    /**
     * Creates a <code>DataSource</code> for this context.
     */
    private final DataSource createDataSource()
    {
        final String dsDriver = getDriver();
        final String url = getDatabaseURL();
        final String validationQuery = getValidationQuery();
        logParameters();
        return dataSourceFactory.createDataSource(dsDriver, url, owner, password, validationQuery);
    }

    private final void logParameters()
    {
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("driver = %s", getDriver()));
            operationLog.info(String.format("databaseURL = %s", getDatabaseURL()));
            operationLog.info(String.format("validationQuery = %s", getValidationQuery()));
            operationLog.info(String.format("basicDatabaseName = %s", getBasicDatabaseName()));
            operationLog.info(String.format("kind = %s", getDatabaseKind()));
            operationLog.info(String.format("instance = %s", getDatabaseInstance()));
            operationLog.info(String.format("owner = %s", getOwner()));
            operationLog.info(String.format("scriptFolder = %s", getScriptFolder()));
            operationLog.info(String.format("createFromScratch = %s", isCreateFromScratch()));
            operationLog.info(String.format("scriptSingleStepMode = %s", isScriptSingleStepMode()));
            operationLog.info(String.format("maxActiveConnections = %d",
                    dataSourceFactory.getMaxActive()));
            operationLog.info(String.format("maxIdleConnections = %d",
                    dataSourceFactory.getMaxIdle()));
            operationLog.info(String.format("maxWaitForConnectionsMillis = %d",
                    dataSourceFactory.getMaxWait()));
            operationLog.info(String.format("activeConnectionsLogIntervalMillis = %d",
                    dataSourceFactory.getActiveConnectionsLogInterval()));
        }
    }

    /**
     * Returns the template to created the URL of the database to be created/migrated.
     * 
     * @param dsDatabaseName The name of the database to get the URL for.
     * @throws ConfigurationFailureException If undefined.
     */
    private final String getUrl(final String dsDatabaseName) throws ConfigurationFailureException
    {
        checkDatabaseEngine();
        return databaseEngine.getURL(this.urlHostPart, dsDatabaseName);
    }

    /**
     * Returns the fully-qualified class name of the JDBC driver.
     * 
     * @throws ConfigurationFailureException If undefined.
     */
    private final String getDriver() throws ConfigurationFailureException
    {
        checkDatabaseEngine();
        return databaseEngine.getDriverClass();
    }

    private final String getValidationQuery() throws ConfigurationFailureException
    {
        checkDatabaseEngine();
        return databaseEngine.getValidationQuery();
    }

    /**
     * Returns user name of the administrator.
     * 
     * @return The default admin user of the database engine when undefined.
     * @throws ConfigurationFailureException If neither the admin user nor the database engine are defined.
     */
    private final String getAdminUser() throws ConfigurationFailureException
    {
        if (adminUser == null)
        {
            checkDatabaseEngine();
            return databaseEngine.getDefaultAdminUser();
        } else
        {
            return adminUser;
        }
    }

    /**
     * Returns password of the administrator.
     * 
     * @return <code>null</code> when undefined.
     */
    private final String getAdminPassword()
    {
        return adminPassword;
    }

    /**
     * Returns database kind.
     * 
     * @return <code>null</code> when undefined.
     */
    private final String getDatabaseKind()
    {
        return databaseKind;
    }

    /** Returns the complete database URL. */
    public final String getDatabaseURL()
    {
        final String dsDatabaseName = getDatabaseName();
        if (dsDatabaseName == null)
        {
            throw new ConfigurationFailureException("No db name defined.");
        }
        final String url = getUrl(dsDatabaseName);
        return url;
    }

    /**
     * Returns the {@link DataSource} of this data configuration.
     */
    public final DataSource getDataSource()
    {
        if (dataSource == null)
        {
            dataSource = createDataSource();
        }
        return dataSource;
    }

    /**
     * Returns the basic name of the database. The kind of database will be added to this to create the full database name.
     */
    public String getBasicDatabaseName()
    {
        return basicDatabaseName;
    }

    /**
     * Returns data source for admin purposes.
     * 
     * @throws ConfigurationFailureException If not all relevant information has been defined that is needed for the admin data source.
     */
    public final DataSource getAdminDataSource() throws ConfigurationFailureException
    {
        if (adminDataSource == null)
        {
            adminDataSource =
                    dataSourceFactory.createDataSource(getDriver(), getAdminURL(), getAdminUser(),
                            getAdminPassword(), getValidationQuery());
        }
        return adminDataSource;
    }

    /**
     * Returns the user name of the owner of the database.
     */
    public final String getOwner()
    {
        return owner;
    }

    /**
     * Sets the user name of the owner of the database. If <var>owner</var> is <code>null</code> or empty, the OS user running the VM will be set
     * instead.
     */
    public final void setOwner(final String owner)
    {
        if (StringUtils.isBlank(owner))
        {
            this.owner = System.getProperty("user.name").toLowerCase();
        } else
        {
            this.owner = owner;
        }
    }

    /**
     * Sets the name of group that gets read-only access to all database objects. If <var>readOnlyGroup</var> is <code>null</code> or empty, then no
     * read-only group will be created.
     */
    public String getReadOnlyGroup()
    {
        return readOnlyGroup;
    }

    /**
     * Sets the name of the group that should be granted read-only access.
     */
    public void setReadOnlyGroup(String readOnlyGroup)
    {
        this.readOnlyGroup = readOnlyGroup;
    }

    /**
     * Sets the name of group that gets read-write access to all database objects. If <var>readWriteGroup</var> is <code>null</code> or empty, then no
     * read-write group will be created.
     */
    public String getReadWriteGroup()
    {
        return readWriteGroup;
    }

    /**
     * Sets the name of the group that should be granted read-write access.
     */
    public void setReadWriteGroup(String readWriteGroup)
    {
        this.readWriteGroup = readWriteGroup;
    }

    /**
     * @return The password part of the database credentials for the db owner.
     */
    public final String getPassword()
    {
        return password;
    }

    /**
     * Sets the password part of the database credentials for the db owner. A <code>null</code> password will be replaced by an empty string.
     */
    public final void setPassword(final String password)
    {
        if (password == null)
        {
            this.password = "";
        } else
        {
            this.password = password;
        }
    }

    /**
     * Sets user name of the administrator.
     * 
     * @param adminUser New value. Can be <code>null</code>. For convenience when using with Spring property place holders, an empty string will be
     *            replaced by <code>null</code>.
     */
    public final void setAdminUser(final String adminUser)
    {
        if (adminUser != null && adminUser.length() == 0)
        {
            this.adminUser = null;
        } else
        {
            this.adminUser = StringUtils.trim(adminUser);
        }
    }

    /**
     * Sets the basic name of the database. The kind of database will be added to this to create the full database name.
     * 
     * @param basicDatabaseName The basic name of the database. Must not be <code>null</code>.
     */
    public void setBasicDatabaseName(final String basicDatabaseName)
    {
        this.basicDatabaseName = StringUtils.trim(basicDatabaseName);
    }

    public String getDatabaseName()
    {
        if (databaseName == null)
        {
            databaseName = getBasicDatabaseName() + "_" + getDatabaseKind();
        }
        return databaseName;
    }

    /**
     * Sets password of the administrator.
     * 
     * @param adminPassword New value. Can be <code>null</code>.
     */
    public final void setAdminPassword(final String adminPassword)
    {
        this.adminPassword = adminPassword;
    }

    /**
     * @return The host part of the URL or <code>null</code>, if it is not set.
     */
    public final String getUrlHostPart()
    {
        return this.urlHostPart;
    }

    /**
     * Sets the host part of the URL. <var>urlHostPart</var> can be <code>null</code>. For convenience when using with Spring property place holders,
     * an empty string will be replaced by <code>null</code>.
     */
    public final void setUrlHostPart(final String urlHostPart)
    {
        if (isSet(urlHostPart))
        {
            this.urlHostPart = StringUtils.trim(urlHostPart);
        } else
        {
            this.urlHostPart = null;
        }
        setTestEnvironmentHostOrConfigured();
    }

    private boolean isSet(String propertyValueStr)
    {
        return StringUtils.isNotBlank(propertyValueStr)
                && propertyValueStr.startsWith("${") == false;
    }

    /**
     * The maximum number of seconds that the pool will wait (when there are no available connections) for a connection to be returned before throwing
     * an exception, or -1 (by default) to wait indefinitely.
     */
    public void setMaxWaitForConnectionProp(String maxWaitStr)
    {
        if (isSet(maxWaitStr))
        {
            this.dataSourceFactory.setMaxWait(Integer.parseInt(maxWaitStr) * 1000L);
        }
    }

    /**
     * Sets the maximum number of idle connections in the pool (default is 20).
     */
    public void setMaxIdleConnectionsProp(String maxIdleStr)
    {
        if (isSet(maxIdleStr))
        {
            this.dataSourceFactory.setMaxIdle(Integer.parseInt(maxIdleStr));
        }
    }

    /**
     * Sets the maximum number of active connections that can be allocated at the same time (default is 20).
     */
    public void setMaxActiveConnectionsProp(String maxActiveStr)
    {
        if (isSet(maxActiveStr))
        {
            this.dataSourceFactory.setMaxActive(Integer.parseInt(maxActiveStr));
        }
    }

    /**
     * Set the interval (in seconds) between two regular log entries of currently active database connections if more than one connection is active.
     * Set to a negative value to disable this feature.
     */
    public void setActiveConnectionsLogIntervalProp(String activeConnectionLogIntervalStr)
    {
        if (isSet(activeConnectionLogIntervalStr))
        {
            this.dataSourceFactory.setActiveConnectionsLogInterval(Integer
                    .parseInt(activeConnectionLogIntervalStr) * 1000L);
        }
    }

    /**
     * Returns the URL of the database server which allows to create a new database.
     * 
     * @return <code>null</code> when undefined.
     */
    private final String getAdminURL() throws ConfigurationFailureException
    {
        checkDatabaseEngine();
        return databaseEngine.getAdminURL(this.urlHostPart, getDatabaseName());
    }

    /**
     * Returns <code>lobHandler</code>.
     * 
     * @throws ConfigurationFailureException If the database engine is not defined.
     */
    public final LobHandler getLobHandler() throws ConfigurationFailureException
    {
        checkDatabaseEngine();
        return databaseEngine.getLobHandler();
    }

    /**
     * Returns <code>sequencerHandler</code>.
     * 
     * @throws ConfigurationFailureException If the database engine is not defined.
     */
    public final ISequencerHandler getSequencerHandler() throws ConfigurationFailureException
    {
        checkDatabaseEngine();
        return databaseEngine.getSequenceHandler();
    }

    /**
     * Returns the mapper from table names to sequencer names.
     */
    public final ISequenceNameMapper getSequenceNameMapper()
    {
        return sequenceNameMapper;
    }

    /**
     * Sets the mapper from table names to sequencer names.
     */
    public final void setSequenceNameMapper(final ISequenceNameMapper sequenceNameMapper)
    {
        this.sequenceNameMapper = sequenceNameMapper;
    }

    public final boolean isSequenceUpdateNeeded()
    {
        return sequenceUpdateNeeded;
    }

    public final void setSequenceUpdateNeeded(boolean sequenceUpdateNeeded)
    {
        this.sequenceUpdateNeeded = sequenceUpdateNeeded;
    }

    /**
     * Returns <code>true</code> if the current database should be dropped and (re)created from scratch.
     * 
     * @return <code>false</code> when the database should only be migrated if necessary.
     */
    public final boolean isCreateFromScratch()
    {
        return createFromScratch;
    }

    /**
     * Sets the database should be dropped and (re)created from scratch or not.
     */
    public final void setCreateFromScratch(final boolean createFromScratch)
    {
        this.createFromScratch = createFromScratch;
    }

    /**
     * Sets the database should be dropped and (re)created from scratch or not.
     */
    public final void setCreateFromScratchProp(final String createFromScratchStr)
    {
        if (isSet(createFromScratchStr))
        {
            setCreateFromScratch(Boolean.parseBoolean(createFromScratchStr));
        }
    }

    /**
     * Returns <code>true</code> if scripts in the db migration engine should be executed statement by statement. This mode gives better error
     * messages on where the faulty SQL is but on the other hand it is a lot slower.
     */
    public final boolean isScriptSingleStepMode()
    {
        return scriptSingleStepMode;
    }

    /**
     * Sets the db migration engine to single step mode for scripts.
     */
    public final void setScriptSingleStepMode(final boolean singleStepMode)
    {
        this.scriptSingleStepMode = singleStepMode;
    }

    /**
     * Sets the db migration engine to single step mode for scripts.
     */
    public final void setScriptSingleStepModeProp(final String singleStepModeStr)
    {
        if (isSet(singleStepModeStr))
        {
            setScriptSingleStepMode(Boolean.parseBoolean(singleStepModeStr));
        }
    }

    /**
     * Sets database kind. This will be append to the name of the database. It allows to have different database instances in parallel (for
     * developing, testing, etc.).
     * 
     * @param databaseKind New value. Can be <code>null</code>.
     */
    public final void setDatabaseKind(final String databaseKind)
    {
        this.databaseKind = StringUtils.trim(databaseKind);
    }

    /**
     * Returns the {@link DatabaseEngine}.
     */
    public DatabaseEngine getDatabaseEngine() throws ConfigurationFailureException
    {
        if (databaseEngine == null)
        {
            throw new ConfigurationFailureException("No database engine defined.");
        }
        return databaseEngine;
    }

    /**
     * Returns the code of the database engine.
     * 
     * @throws ConfigurationFailureException If undefined.
     */
    public final String getDatabaseEngineCode() throws ConfigurationFailureException
    {
        if (databaseEngine == null)
        {
            throw new ConfigurationFailureException("No database engine defined.");
        }
        return databaseEngine.getCode();
    }

    /**
     * Sets the code of the database engine.
     * 
     * @param databaseEngineCode New value.
     * @throws ConfigurationFailureException If there is no such database engine.
     */
    public final void setDatabaseEngineCode(final String databaseEngineCode)
            throws ConfigurationFailureException
    {
        if (isSet(databaseEngineCode))
        {
            this.databaseEngine =
                    DatabaseEngine.getEngineForCode(StringUtils.trim(databaseEngineCode));
        } else
        {
            this.databaseEngine = DatabaseEngine.POSTGRESQL;
        }
    }
    
    public final String getValidVersions()
    {
        return validVersions;
    }

    public final void setValidVersions(String validVersions)
    {
        if (isSet(validVersions))
        {
            this.validVersions = validVersions;
        }
    }

    /**
     * @return A new {@link IDAOFactory} that fits this database configuration.
     */
    public final IDAOFactory createDAOFactory()
    {
        return databaseEngine.createDAOFactory(this);
    }

    /**
     * Returns the folder which contains all SQL scripts.
     * 
     * @return <code>null</code> when undefined.
     */
    public final String getScriptFolder()
    {
        return scriptFolder;
    }

    /**
     * Sets the folder which contains all SQL scripts.
     * 
     * @param scriptFolder New value. Can be <code>null</code>.
     */
    public final void setScriptFolder(final String scriptFolder)
    {
        this.scriptFolder = scriptFolder;
    }

    public List<String> getSqlScriptFolders()
    {
        if (scriptFolder != null && variablesResolved(scriptFolder))
        {
            return Arrays.asList(scriptFolder);
        }
        if (scriptFolders != null && scriptFolders.size() > 0
                && variablesResolved(scriptFolders.get(0)))
        {
            return scriptFolders;
        }
        return Collections.emptyList();
    }

    private boolean variablesResolved(String value)
    {
        return value.indexOf("${") < 0;
    }

    public void setScriptFolders(String scriptFolders)
    {
        this.scriptFolders = new ArrayList<String>();
        StringTokenizer tokenizer = new StringTokenizer(scriptFolders, ",");
        while (tokenizer.hasMoreTokens())
        {
            this.scriptFolders.add(tokenizer.nextToken().trim());
        }
    }

    public final String getDatabaseInstance()
    {
        return StringUtils.trim(databaseInstance);
    }

    public final void setDatabaseInstance(final String databaseInstance)
    {
        this.databaseInstance = databaseInstance;
    }

    /** Closes opened database connections. */
    public final void closeConnections()
    {
        closeConnection(dataSource);
        dataSource = null;
        closeConnection(adminDataSource);
        adminDataSource = null;
    }

    //
    // DisposableBean
    //

    @Override
    public final void destroy() throws Exception
    {
        closeConnections();
    }
}

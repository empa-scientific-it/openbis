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

import java.lang.reflect.Constructor;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.db.ISequencerHandler;
import ch.systemsx.cisd.common.db.PostgreSQLSequencerHandler;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.dbmigration.h2.H2DAOFactory;
import ch.systemsx.cisd.dbmigration.postgresql.PostgreSQLDAOFactory;

/**
 * An enumeration to keep the configuration setting for different database engines.
 * 
 * @author Bernd Rinn
 */
public enum DatabaseEngine
{

    // FORCE_OPENBIS_POSTGRES_HOST is used for servers that don't have the Postgres installation locally and require no change on config files (test servers)
    POSTGRESQL("postgresql", "org.postgresql.Driver", PostgreSQLDAOFactory.class,
            new DefaultLobHandler(), new PostgreSQLSequencerHandler(), "jdbc:postgresql://{0}/",
            "jdbc:postgresql://{0}/{1}", getTestEnvironmentHostOrConfigured("localhost"), "postgres", "SELECT 1"),

    H2("h2", "org.h2.Driver", H2DAOFactory.class, new DefaultLobHandler(),
            new PostgreSQLSequencerHandler(), "jdbc:h2:{0}{1};DB_CLOSE_DELAY=-1",
            "jdbc:h2:{0}{1};DB_CLOSE_DELAY=-1", "file:db/", "sa", null);

    public static String getTestEnvironmentHostOrConfigured(String configured) {
        return System.getenv().getOrDefault("FORCE_OPENBIS_POSTGRES_HOST", configured);
    }

    public static String getTestEnvironmentURLOrConfigured(String configured) {
        if (System.getenv().containsKey("FORCE_OPENBIS_POSTGRES_HOST")) {
            int dbNameStart = configured.lastIndexOf("/");
            String dbName = configured.substring(dbNameStart + 1);
            configured = "jdbc:postgresql://" + System.getenv().get("FORCE_OPENBIS_POSTGRES_HOST") + "/" + dbName;
        }
        return configured;
    }

    private static Map<String, DatabaseEngine> engines = initEngineMap();

    private static Map<String, DatabaseEngine> enginesByDriverClass = initEnginesByDriverClassMap();

    private final String code;

    private final String driverClass;

    private final Class<ch.systemsx.cisd.dbmigration.IDAOFactory> daoFactoryClass;

    private final Constructor<ch.systemsx.cisd.dbmigration.IDAOFactory> daoFactoryConstructor;

    private final LobHandler lobHandler;

    private final ISequencerHandler sequenceHandler;

    private final String adminUrlTemplate;

    private final String urlTemplate;

    private final String defaultURLHostPart;

    private final String defaultAdminUser;

    private final String validationQuery;

    @SuppressWarnings("unchecked")
    DatabaseEngine(String code, String driver, Class<?> daoFactoryClass, LobHandler lobHandler,
            ISequencerHandler sequenceHandler, String adminUrlTemplate, String urlTemplate,
            String defaultURLHostPart, String defaultAdminUser, String validationQuery)
    {
        assert code != null;
        assert driver != null;
        assert daoFactoryClass != null;
        assert lobHandler != null;
        assert sequenceHandler != null;
        assert adminUrlTemplate != null;
        assert urlTemplate != null;
        assert defaultURLHostPart != null;
        assert defaultAdminUser != null;

        this.code = code.toLowerCase();
        this.adminUrlTemplate = adminUrlTemplate;
        this.urlTemplate = urlTemplate;
        this.defaultURLHostPart = defaultURLHostPart;
        this.defaultAdminUser = defaultAdminUser;
        this.driverClass = driver;
        this.lobHandler = lobHandler;
        this.sequenceHandler = sequenceHandler;
        this.validationQuery = validationQuery;
        this.daoFactoryClass = (Class<ch.systemsx.cisd.dbmigration.IDAOFactory>) daoFactoryClass;
        try
        {
            this.daoFactoryConstructor =
                    this.daoFactoryClass.getConstructor(DatabaseConfigurationContext.class);
        } catch (NoSuchMethodException ex)
        {
            throw new Error("No constructor", ex);
        }
    }

    /**
     * @return The code of this engine.
     */
    public final String getCode()
    {
        return code;
    }

    /**
     * Creates a new {@link IDAOFactory} for this engine
     */
    public IDAOFactory createDAOFactory(DatabaseConfigurationContext context)
    {
        try
        {
            return daoFactoryConstructor.newInstance(context);
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    /**
     * @return the class of the driver.
     */
    public final String getDriverClass()
    {
        return driverClass;
    }

    public final LobHandler getLobHandler()
    {
        return lobHandler;
    }

    public final ISequencerHandler getSequenceHandler()
    {
        return sequenceHandler;
    }

    /**
     * @param urlHostPartOrNull The host part of the URL, or <code>null</code>, if the default host part should be used.
     * @param databaseName The name of the database (may be ignored for the admin URL, depending on the database engine)
     * @return The admin URL of the db.
     */
    public final String getAdminURL(String urlHostPartOrNull, String databaseName)
    {
        final String hostPartUrl =
                (urlHostPartOrNull == null) ? defaultURLHostPart : urlHostPartOrNull;
        final String url = MessageFormat.format(adminUrlTemplate, hostPartUrl, databaseName);
        return url;
    }

    public final String getDefaultAdminUser()
    {
        return defaultAdminUser;
    }

    public String getValidationQuery()
    {
        return validationQuery;
    }

    /**
     * @param urlHostPartOrNull The host part of the URL, or <code>null</code>, if the default host part should be used.
     * @param databaseName The name of the database.
     * @return The URL of the db.
     */
    public final String getURL(String urlHostPartOrNull, String databaseName)
    {
        final String hostPartUrl =
                (urlHostPartOrNull == null) ? defaultURLHostPart : urlHostPartOrNull;
        final String url = MessageFormat.format(urlTemplate, hostPartUrl, databaseName);
        return url;
    }

    /**
     * @return The database engine for <var>code</var>, or <code>null</code>
     * @throws ConfigurationFailureException If no engine is found for this code.
     */
    public static DatabaseEngine getEngineForCode(String code) throws ConfigurationFailureException
    {
        final DatabaseEngine engine = engines.get(code.toLowerCase());
        if (engine == null)
        {
            throw new ConfigurationFailureException("No database engine '" + code + "' found.");
        }
        return engine;
    }

    /**
     * Returns the database engine for specified driver class.
     * 
     * @throws IllegalArgumentException if no engine could be found.
     */
    public static DatabaseEngine getEngineForDriverClass(String driverClassName)
    {
        final DatabaseEngine engine = enginesByDriverClass.get(driverClassName);
        if (engine == null)
        {
            throw new IllegalArgumentException("No database engine with driver class "
                    + driverClassName + " found.");
        }
        return engine;
    }

    /**
     * Returns <code>true</code> if a {@link DatabaseEngine} for specified driver class exists.
     */
    public static boolean hasEngineForDriverClass(String driverClassName)
    {
        return enginesByDriverClass.get(driverClassName) != null;
    }

    private static Map<String, DatabaseEngine> initEngineMap()
    {
        final Map<String, DatabaseEngine> map = new HashMap<String, DatabaseEngine>();
        for (DatabaseEngine engine : values())
        {
            map.put(engine.code, engine);
        }
        return map;
    }

    private static Map<String, DatabaseEngine> initEnginesByDriverClassMap()
    {
        final Map<String, DatabaseEngine> map = new HashMap<String, DatabaseEngine>();
        for (DatabaseEngine engine : values())
        {
            map.put(engine.driverClass, engine);
        }
        return map;
    }

}

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

import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

import static ch.systemsx.cisd.common.properties.PropertyUtils.*;

/**
 * Configuration context for database operations.
 * <p>
 * It is a simple counterpart of {@link DatabaseConfigurationContext} that uses only a few basic JDBC configuration parameters:
 * <li>driver class name
 * <li>url
 * <li>username
 * <li>password
 * 
 * @author Piotr Buczek
 */
public class SimpleDatabaseConfigurationContext implements DisposableBean
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, SimpleDatabaseConfigurationContext.class);

    public static final String DRIVER_KEY = "database-driver";

    public static final String URL_KEY = "database-url";

    public static final String USER_KEY = "database-username";

    public static final String PASSWORD_KEY = "database-password";

    static final String MAX_IDLE_KEY = "database-max-idle-connections";

    static final String MAX_ACTIVE_KEY = "database-max-active-connections";

    static final String MAX_WAIT_FOR_CONNECTION = "database-max-wait-for-connection";

    static final String ACTIVE_CONNECTIONS_LOG_INTERVAL =
            "database-active-connections-log-interval";

    static final String VALIDATION_QUERY_KEY = "validation-query";

    private IDataSourceFactory dataSourceFactory = new BasicDataSourceFactory();

    private DataSource dataSource;

    private final String driverClassName;

    private final String url;

    private final String username;

    private final String password;

    private final String validationQuery;

    // for testing
    public SimpleDatabaseConfigurationContext(String driverClassName, String url, String username,
            String password, String validationQuery)
    {
        this.driverClassName = driverClassName;
        this.url = DatabaseEngine.getTestEnvironmentURLOrConfigured(url);
        this.username = username;
        this.password = password;
        this.validationQuery = validationQuery;
    }

    public SimpleDatabaseConfigurationContext(Properties properties)
    {
        this.driverClassName = getMandatoryProperty(properties, DRIVER_KEY);
        this.url = DatabaseEngine.getTestEnvironmentURLOrConfigured(getMandatoryProperty(properties, URL_KEY));
        this.username =
                getProperty(properties, USER_KEY, System.getProperty("user.name")
                        .toLowerCase());
        this.password = getProperty(properties, PASSWORD_KEY);

        if (hasProperty(properties, MAX_ACTIVE_KEY))
        {
            dataSourceFactory.setMaxActive(getInt(properties, MAX_ACTIVE_KEY, -1));
        }

        if (hasProperty(properties, MAX_IDLE_KEY))
        {
            dataSourceFactory.setMaxIdle(getInt(properties, MAX_IDLE_KEY, -1));
        }

        if (hasProperty(properties, MAX_WAIT_FOR_CONNECTION))
        {
            dataSourceFactory.setMaxWait(getInt(properties, MAX_WAIT_FOR_CONNECTION, -1));
        }

        if (hasProperty(properties, ACTIVE_CONNECTIONS_LOG_INTERVAL))
        {
            dataSourceFactory.setActiveConnectionsLogInterval(getInt(properties,
                    ACTIVE_CONNECTIONS_LOG_INTERVAL, -1));
        }

        this.validationQuery = getProperty(properties, VALIDATION_QUERY_KEY);
        operationLog.info("Database configuration for URL '" + url + "' created.");
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
     * Creates a <code>DataSource</code> for this context.
     */
    private final DataSource createDataSource()
    {
        return dataSourceFactory.createDataSource(driverClassName, url, username, password,
                validationQuery);
    }

    /** Closes opened database connections. */
    public final void closeConnections()
    {
        closeConnection(dataSource);
        dataSource = null;
    }

    //
    // DisposableBean
    //

    @Override
    public final void destroy() throws Exception
    {
        closeConnections();
    }

    //

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

}

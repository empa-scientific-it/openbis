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

package ch.systemsx.cisd.openbis.plugin.query.server;

import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.DisposableBean;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.dbmigration.BasicDataSourceFactory;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.dbmigration.IDataSourceFactory;

/**
 * Configuration context for database operations.
 * <p>
 * It is a simple counterpart of {@link DatabaseConfigurationContext} that uses only a few basic
 * JDBC configuration parameters:
 * <li>driver class name
 * <li>url
 * <li>username
 * <li>password
 * 
 * @author Piotr Buczek
 */
public class SimpleDatabaseConfigurationContext implements DisposableBean
{

    static final String DRIVER_KEY = "database-driver";

    static final String URL_KEY = "database-url";

    static final String USER_KEY = "database-username";

    static final String PASSWORD_KEY = "database-password";

    private IDataSourceFactory dataSourceFactory = new BasicDataSourceFactory();

    private DataSource dataSource;

    private final String driverClassName;

    private final String url;

    private final String username;

    private final String password;

    public SimpleDatabaseConfigurationContext(Properties properties)
    {
        this.driverClassName = PropertyUtils.getMandatoryProperty(properties, DRIVER_KEY);
        this.url = PropertyUtils.getMandatoryProperty(properties, URL_KEY);
        this.username =
                PropertyUtils.getProperty(properties, USER_KEY, System.getProperty("user.name")
                        .toLowerCase());
        this.password = PropertyUtils.getProperty(properties, PASSWORD_KEY);
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
        return dataSourceFactory.createDataSource(driverClassName, url, username, password);
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

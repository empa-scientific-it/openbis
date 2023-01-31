/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.shared.utils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.filesystem.HostAwareFile;
import ch.systemsx.cisd.common.filesystem.IFreeSpaceProvider;
import ch.systemsx.cisd.common.filesystem.SimpleFreeSpaceProvider;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;

/**
 * PostgreSQL database files are designed to grow until they take up the entire available disk space. It is therefore impossible to estimate what part
 * of a hard disk is "free" by just asking the file system.
 * <p>
 * The {@link PostgresPlusFileSystemFreeSpaceProvider} estimates the free space on a drive as the sum of the free disk space and the free space of a
 * PostgreSQL database as returned by its "pgstattuple" extension.
 * 
 * <pre>
 * IMPORTANT: The class requires that the extension 'pgstattuple' is installed on the target
 * PostgreSQL database. For PostgreSQL 9.1 this can be done by executing :
 *     
 *     psql -d DB_NAME -c "CREATE EXTENSION pgstattuple;"
 * 
 * @author Kaloyan Enimanev
 */
public class PostgresPlusFileSystemFreeSpaceProvider implements IFreeSpaceProvider
{

    static final String EXECUTE_VACUUM_KEY = "execute-vacuum";

    static final String DATA_SOURCE_KEY = "monitored-data-source";

    private static final String VACUUM_QUERY = "VACUUM;";

    private static final String CREATE_TMP_FREE_SPACE_TABLE =
            "CREATE TEMPORARY TABLE freespace ON COMMIT DROP AS "
                    + "   (SELECT relname, (SELECT free_space FROM pgstattuple(relid)) "
                    + "       AS free_space FROM pg_catalog.pg_statio_user_tables);";

    private static final String SELECT_FREE_SPACE_QUERY = "SELECT sum(free_space) FROM freespace;";

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            PostgresPlusFileSystemFreeSpaceProvider.class);

    private final boolean executeVacuum;

    private final DataSource dataSource;

    private final IFreeSpaceProvider fileSystemFreeSpaceProvider;

    public PostgresPlusFileSystemFreeSpaceProvider(Properties properties)
    {
        this(properties, new SimpleFreeSpaceProvider());
    }

    PostgresPlusFileSystemFreeSpaceProvider(Properties properties,
            IFreeSpaceProvider fileSystemFreeSpaceProvider)
    {
        this.executeVacuum = PropertyUtils.getBoolean(properties, EXECUTE_VACUUM_KEY, false);

        String dataSourceName = PropertyUtils.getMandatoryProperty(properties, DATA_SOURCE_KEY);
        this.dataSource = ServiceProvider.getDataSourceProvider().getDataSource(dataSourceName);
        this.fileSystemFreeSpaceProvider = fileSystemFreeSpaceProvider;
    }

    @Override
    public long freeSpaceKb(HostAwareFile path) throws IOException
    {
        long dataSourceFreeSpace = calculateDataSourceFreeSpace() / 1024L;
        long fsFreeSpace = fileSystemFreeSpaceProvider.freeSpaceKb(path);
        return dataSourceFreeSpace + fsFreeSpace;
    }

    private long calculateDataSourceFreeSpace()
    {
        Connection connection = null;
        try
        {
            connection = createConnection();
            if (executeVacuum)
            {
                executeVacuumQuery(connection);
            }

            return calculateFreeSpace(connection);

        } catch (SQLException sqlEx)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(sqlEx);
        } finally
        {
            closeConnection(connection);
        }
    }

    private void executeVacuumQuery(Connection connection) throws SQLException
    {
        connection.createStatement().execute(VACUUM_QUERY);
    }

    private long calculateFreeSpace(Connection connection) throws SQLException
    {
        connection.setAutoCommit(false);
        try
        {
            connection.createStatement().execute(CREATE_TMP_FREE_SPACE_TABLE);
            ResultSet result = connection.createStatement().executeQuery(SELECT_FREE_SPACE_QUERY);
            result.next();
            return result.getLong(1);
        } finally
        {
            connection.setAutoCommit(true);
        }
    }

    private Connection createConnection() throws SQLException
    {
        return dataSource.getConnection();
    }

    private void closeConnection(Connection connection)
    {
        if (connection != null)
        {
            try
            {
                connection.close();
            } catch (SQLException ex)
            {
                // suppress this exception
                operationLog.error(ex);
            }
        }
    }

}

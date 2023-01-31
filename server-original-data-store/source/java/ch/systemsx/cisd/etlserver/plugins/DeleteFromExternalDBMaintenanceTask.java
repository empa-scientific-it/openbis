/*
 * Copyright 2009 ETH Zuerich, CISD
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
package ch.systemsx.cisd.etlserver.plugins;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSet;

/**
 * Maintenance task deleting from a custom-specific database data sets which have been deleted from openbis.
 * 
 * @author Izabela Adamczyk
 */
public class DeleteFromExternalDBMaintenanceTask extends
        AbstractDataSetDeletionPostProcessingMaintenanceTask
{

    private static final String DEFAULT_DATA_SET_PERM_ID = "PERM_ID";

    private static final String DATA_SET_PERM_ID_KEY = "data-set-perm-id";

    private static final String DATA_SET_TABLE_NAME_KEY = "data-set-table-name";

    private static final String DEFAULT_DATA_SET_TABLE_NAME = "image_data_sets, analysis_data_sets";

    private static final String LAST_SEEN_EVENT_ID_COLUMN_KEY = "last-seen-event-id-column";

    private static final String SYNCHRONIZATION_TABLE_KEY = "synchronization-table";

    private static final String DEFAULT_SYNCHRONIZATION_TABLE = "EVENTS";

    private static final String DEFAULT_LAST_SEEN_EVENT_ID = "LAST_SEEN_DELETION_EVENT_ID";

    private DataSource dataSource;

    private String synchronizationTable;

    private String lastSeenEventID;

    protected String[] dataSetTableNames;

    protected String permIDColumn;

    protected Connection connection;

    @Override
    public void setUp(String pluginName, Properties properties)
    {
        super.setUp(pluginName, properties);
        synchronizationTable =
                properties.getProperty(SYNCHRONIZATION_TABLE_KEY, DEFAULT_SYNCHRONIZATION_TABLE);
        lastSeenEventID =
                properties.getProperty(LAST_SEEN_EVENT_ID_COLUMN_KEY, DEFAULT_LAST_SEEN_EVENT_ID);
        dataSetTableNames =
                properties.getProperty(DATA_SET_TABLE_NAME_KEY, DEFAULT_DATA_SET_TABLE_NAME).split(
                        ",");
        permIDColumn = properties.getProperty(DATA_SET_PERM_ID_KEY, DEFAULT_DATA_SET_PERM_ID);
        this.dataSource = ServiceProvider.getDataSourceProvider().getDataSource(properties);
        checkDatabaseConnection();
    }

    /**
     * method overriden to create an underlying database connection.
     */
    @Override
    public void execute()
    {
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("Synchronizing data set information");
        }

        connection = null;
        try
        {
            connection = createConnection();
            super.execute();

        } catch (Exception e)
        {
            operationLog.error(e);
        } finally
        {
            closeConnection(connection);
        }
    }

    @Override
    protected Long getLastSeenEventId()
    {
        try
        {
            return tryGetPreviousLastSeenEventId(connection);
        } catch (SQLException sqlEx)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(sqlEx);
        }
    }

    @Override
    protected void updateLastSeenEventId(Long newLastSeenEventId)
    {
        try
        {
            executeSql("delete from " + synchronizationTable);
            executeSql("INSERT INTO " + synchronizationTable + " (" + lastSeenEventID
                    + ") VALUES('" + newLastSeenEventId + "')");
        } catch (SQLException sqlEx)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(sqlEx);
        }
    }

    @Override
    protected void execute(List<DeletedDataSet> datasets)
    {
        try
        {
            boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            long t0 = System.currentTimeMillis();
            deleteDatasets(datasets);
            connection.commit();
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("Synchronization task took "
                        + ((System.currentTimeMillis() - t0 + 500) / 1000) + " seconds.");
            }
            connection.setAutoCommit(autoCommit);
        } catch (SQLException sqlEx)
        {
            operationLog.error(sqlEx);
        }
    }

    private void checkDatabaseConnection()
    {
        Connection c = null;
        try
        {
            c = createConnection();
            tryGetPreviousLastSeenEventId(c);
        } catch (SQLException sqlEx)
        {
            throw new ConfigurationFailureException("Initialization failed", sqlEx);
        } finally
        {
            closeConnection(c);
        }
    }

    private void closeConnection(Connection connectionOrNull)
    {
        if (connectionOrNull != null)
        {
            try
            {
                connectionOrNull.close();
            } catch (SQLException ex)
            {
                // suppress this exception
                operationLog.error(ex);
            }
        }
    }

    private Connection createConnection() throws SQLException
    {
        return dataSource.getConnection();
    }

    protected void deleteDatasets(List<DeletedDataSet> deletedDataSets) throws SQLException
    {
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format(
                    "Synchronizing deletions of %d datasets with the database.",
                    deletedDataSets.size()));
        }
        for (String dataSetTableName : dataSetTableNames)
        {
            connection.createStatement().execute(
                    String.format("DELETE FROM " + dataSetTableName.trim() + " WHERE "
                            + permIDColumn + " IN (%s)", joinIds(deletedDataSets)));
        }
    }

    private void executeSql(String sql) throws SQLException
    {
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.executeUpdate();
    }

    protected static String joinIds(List<DeletedDataSet> deletedDatasetCodes)
    {
        StringBuilder sb = new StringBuilder();
        for (DeletedDataSet dds : deletedDatasetCodes)
        {
            if (sb.length() != 0)
            {
                sb.append(", ");
            }
            sb.append("'" + StringUtils.replace(dds.getIdentifier(), "'", "''") + "'");
        }
        String ids = sb.toString();
        return ids;
    }

    private Long tryGetPreviousLastSeenEventId(Connection c) throws SQLException
    {
        Long maxLastSeenEventId = null;
        ResultSet result =
                c.createStatement().executeQuery(
                        "SELECT MAX(" + lastSeenEventID + ") AS " + lastSeenEventID + " FROM "
                                + synchronizationTable);
        while (result.next())
        {
            long newLastSeenEventId = result.getLong(lastSeenEventID);
            if (maxLastSeenEventId == null || maxLastSeenEventId < newLastSeenEventId)
            {
                maxLastSeenEventId = newLastSeenEventId;
            }
        }
        return maxLastSeenEventId;
    }

}

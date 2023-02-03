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

package ch.systemsx.cisd.dbmigration;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.springframework.jdbc.core.support.JdbcDaoSupport;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.db.ISqlScriptExecutor;

/**
 * Common base class of {@link IDatabaseAdminDAO} implementations.
 * 
 * @author Franz-Josef Elmer
 */
public abstract class AbstractDatabaseAdminDAO extends JdbcDaoSupport implements
        IDatabaseAdminDAO
{
    protected final ISqlScriptExecutor scriptExecutor;

    protected final IMassUploader massUploader;

    protected final String owner;

    protected final String readOnlyGroupOrNull;

    protected final String readWriteGroupOrNull;

    protected final String databaseName;

    protected final String databaseURL;

    /**
     * Creates an instance.
     * 
     * @param dataSource Data source able to create/drop the specified database.
     * @param scriptExecutor An executor for SQL scripts.
     * @param massUploader A class that can perform mass (batch) uploads into database tables.
     * @param owner Owner to be created if it doesn't exist.
     * @param readOnlyGroupOrNull Group name that gets read-only access.
     * @param readWriteGroupOrNull Group that should be granted read-write access.
     * @param databaseName Name of the database.
     * @param databaseURL URL of the database.
     */
    public AbstractDatabaseAdminDAO(DataSource dataSource, ISqlScriptExecutor scriptExecutor,
            IMassUploader massUploader, String owner, String readOnlyGroupOrNull,
            String readWriteGroupOrNull, String databaseName, String databaseURL)
    {
        this.scriptExecutor = scriptExecutor;
        this.massUploader = massUploader;
        this.owner = owner;
        this.readOnlyGroupOrNull = readOnlyGroupOrNull;
        this.readWriteGroupOrNull = readWriteGroupOrNull;
        this.databaseName = databaseName;
        this.databaseURL = databaseURL;
        setDataSource(dataSource);
    }

    @Override
    public String getDatabaseName()
    {
        return databaseName;
    }

    @Override
    public String getDatabaseURL()
    {
        return databaseURL;
    }

    @Override
    public DatabaseDefinition getDatabaseDefinition()
    {
        DatabaseDefinition databaseDefinition = new DatabaseDefinition();
        try
        {
            Connection connection = getDataSource().getConnection();
            Statement statement = connection.createStatement();
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet rs = metaData.getTables(null, null, null, new String[] { "TABLE" });
            while (rs.next())
            {
                databaseDefinition.add(new TableDefinition(rs.getString("TABLE_NAME")));
            }
            addColumns(databaseDefinition, metaData);
            addConnections(databaseDefinition, metaData);
            setLargestPrimaryKeys(databaseDefinition, statement);
            statement.close();
            connection.close();
        } catch (SQLException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
        return databaseDefinition;
    }

    private void addColumns(DatabaseDefinition databaseDefinition, DatabaseMetaData metaData)
            throws SQLException
    {
        for (TableDefinition tableDefinition : databaseDefinition)
        {
            String tableName = tableDefinition.getTableName();
            ResultSet rs = metaData.getColumns(null, "%", tableName, "%");
            while (rs.next())
            {
                TableColumnDefinition columnDefinition = new TableColumnDefinition(tableDefinition);
                columnDefinition.setColumnName(rs.getString("COLUMN_NAME"));
                columnDefinition.setDataTypeName(rs.getString("TYPE_NAME"));
                tableDefinition.add(columnDefinition);
            }
        }
    }

    private void addConnections(DatabaseDefinition databaseDefinition, DatabaseMetaData metaData)
            throws SQLException
    {
        for (TableDefinition tableDefinition : databaseDefinition)
        {
            String tableName = tableDefinition.getTableName();
            ResultSet rs = metaData.getPrimaryKeys(null, null, tableName);
            while (rs.next())
            {
                tableDefinition.defineColumnAsPrimaryKey(rs.getString("COLUMN_NAME"));
            }
            rs = metaData.getExportedKeys(null, null, tableName);
            while (rs.next())
            {
                String pkTableName = rs.getString("PKTABLE_NAME");
                String pkColumnName = rs.getString("PKCOLUMN_NAME");
                String fkTableName = rs.getString("FKTABLE_NAME");
                String fkColumnName = rs.getString("FKCOLUMN_NAME");
                databaseDefinition.connect(pkTableName, pkColumnName, fkTableName, fkColumnName);
            }
        }
    }

    private void setLargestPrimaryKeys(DatabaseDefinition databaseDefinition, Statement statement)
            throws SQLException
    {
        for (TableDefinition tableDefinition : databaseDefinition)
        {
            for (TableColumnDefinition columnDefinition : tableDefinition)
            {
                if (columnDefinition.isPrimaryKey())
                {
                    String tableName = tableDefinition.getTableName();
                    String columnName = columnDefinition.getColumnName();
                    String sql = "select max(" + columnName + ") from " + tableName;
                    ResultSet rs = statement.executeQuery(sql);
                    rs.next();
                    columnDefinition.setLargestPrimaryKey(rs.getLong(1));
                }
            }
        }
    }

}

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

package ch.systemsx.cisd.dbmigration.postgresql;

import static ch.systemsx.cisd.dbmigration.MassUploadFileType.CSV;
import static ch.systemsx.cisd.dbmigration.MassUploadFileType.TSV;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.postgresql.PGConnection;
import org.postgresql.copy.CopyManager;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.db.ISequenceNameMapper;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.dbmigration.IMassUploader;

/**
 * A {@link IMassUploader} for the PostgreSQL database.
 * 
 * @author Bernd Rinn
 */
public class PostgreSQLMassUploader extends JdbcDaoSupport implements IMassUploader
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, PostgreSQLMassUploader.class);

    private final DataSource dataSourceOrNull;

    private final Connection connectionOrNull;

    private CopyManager copyManager;

    private final ISequenceNameMapper sequenceNameMapper;

    private final boolean sequenceUpdateNeeded;

    /**
     * Creates an instance for the specified data source and sequence mapper.
     */
    public PostgreSQLMassUploader(final DataSource dataSource,
            final ISequenceNameMapper sequenceNameMapper, boolean sequenceUpdateNeeded)
            throws SQLException
    {
        this.dataSourceOrNull = dataSource;
        this.connectionOrNull = null;
        this.sequenceNameMapper = sequenceNameMapper;
        this.sequenceUpdateNeeded = sequenceUpdateNeeded;
        setDataSource(dataSource);
    }

    /**
     * Creates an instance for the specified data source and sequence mapper.
     */
    public PostgreSQLMassUploader(final Connection conn,
            final ISequenceNameMapper sequenceNameMapper, boolean sequenceUpdateNeeded)
            throws SQLException
    {
        this.dataSourceOrNull = null;
        this.connectionOrNull = conn;
        this.sequenceNameMapper = sequenceNameMapper;
        this.sequenceUpdateNeeded = sequenceUpdateNeeded;
        setDataSource(dataSourceOrNull);
    }

    private final CopyManager getCopyManager() throws SQLException, NoSuchFieldException,
            IllegalAccessException
    {
        if (copyManager == null)
        {
            copyManager = getPGConnection().getCopyAPI();
        }
        return copyManager;
    }

    @Override
    public void performMassUpload(String tableName, String data)
    {
        performMassUpload(tableName, null, data);
    }

    @Override
    public void performMassUpload(String tableName, String[] columnNamesOrNull, String data)
    {
        final StringBuilder copySql = new StringBuilder();
        copySql.append("COPY ");
        copySql.append(tableName);
        if (columnNamesOrNull != null)
        {
            copySql.append(" (");
            for (int i = 0; i < columnNamesOrNull.length; ++i)
            {
                copySql.append(columnNamesOrNull[i]);
                if (i < columnNamesOrNull.length - 1)
                {
                    copySql.append(',');
                }
            }
            copySql.append(')');
        }
        copySql.append(" FROM STDIN");
        final InputStream is = new ByteArrayInputStream(data.getBytes());
        try
        {
            getCopyManager().copyIn(copySql.toString(), is);
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public final void performMassUpload(final File[] massUploadFiles)
    {
        final Set<String> tables = new LinkedHashSet<String>();
        for (final File file : massUploadFiles)
        {
            performMassUpload(file, tables);
        }
        if (sequenceUpdateNeeded)
        {
            boolean successful = true;
            for (final String name : tables)
            {
                successful &= fixSequence(name);
            }
            if (successful == false)
            {
                throw new EnvironmentFailureException("At least one sequence couldn't be updated.");
            }
        }
    }

    private final void performMassUpload(final File massUploadFile, final Set<String> tables)
    {
        try
        {
            final String[] splitName = StringUtils.split(massUploadFile.getName(), "=");
            assert splitName.length == 2 : "Missing '=' in name of file '"
                    + massUploadFile.getName() + "'.";
            final String tableNameWithExtension = splitName[1];
            final boolean csvFileType = CSV.isOfType(tableNameWithExtension);
            final boolean tsvFileType = TSV.isOfType(tableNameWithExtension);
            assert tsvFileType || csvFileType : "Non of expected file types [" + TSV.getFileType()
                    + ", " + CSV.getFileType() + "]: " + massUploadFile.getName();
            final String tableName =
                    tableNameWithExtension.substring(0, tableNameWithExtension.lastIndexOf('.'));
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("Perform mass upload of file '" + massUploadFile + "' to table '"
                        + tableName + "'.");
            }
            final InputStream is = new FileInputStream(massUploadFile);
            try
            {
                if (tsvFileType)
                {
                    getCopyManager().copyIn("COPY " + tableName + " FROM STDIN", is);
                } else
                {
                    getCopyManager()
                            .copyIn("COPY " + tableName + " FROM STDIN WITH CSV HEADER", is);
                }
                tables.add(tableName);
            } finally
            {
                IOUtils.closeQuietly(is);
            }
        } catch (final Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private final boolean fixSequence(final String tableName)
    {
        final String sequenceName = sequenceNameMapper.getSequencerForTable(tableName);
        if (sequenceName == null)
        {
            return true;
        }
        try
        {
            // The result returned by setval is just the value of its second argument.
            final long newSequenceValue =
                    getJdbcTemplate().queryForObject(
                            String.format("select setval('%s', max(id)) from %s", sequenceName,
                                    tableName),
                            Long.class);
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("Updating sequence " + sequenceName + " for table " + tableName
                        + " to value " + newSequenceValue);
            }
            return true;
        } catch (final DataAccessException ex)
        {
            operationLog.error("Failed to set new value for sequence '" + sequenceName
                    + "' of table '" + tableName + "'.", ex);
            return false;
        }
    }

    private final PGConnection getPGConnection() throws SQLException, NoSuchFieldException,
            IllegalAccessException
    {
        if (dataSourceOrNull == null)
        {
            return getPGConnection(connectionOrNull);
        } else
        {
            return getPGConnection(dataSourceOrNull.getConnection());
        }
    }

    private final PGConnection getPGConnection(final Connection conn) throws SQLException,
            NoSuchFieldException, IllegalAccessException
    {
        if (conn instanceof PGConnection)
        {
            return (PGConnection) conn;
        }
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("Found connection of type '" + conn.getClass().getCanonicalName()
                    + "'.");
        }
        final Field delegateField = getField(conn.getClass(), "_conn");
        if (delegateField == null)
        {
            throw new RuntimeException("No PostgreSQL driver found - cannot perform mass upload.");
        }
        delegateField.setAccessible(true);
        return getPGConnection((Connection) delegateField.get(conn));
    }

    private final static Field getField(final Class<?> clazz, final String fieldName)
    {
        assert fieldName != null;
        if (clazz == null)
        {
            return null;
        }

        for (final Field field : clazz.getDeclaredFields())
        {
            if (fieldName.equals(field.getName()))
            {
                return field;
            }
        }
        return getField(clazz.getSuperclass(), fieldName);
    }

}

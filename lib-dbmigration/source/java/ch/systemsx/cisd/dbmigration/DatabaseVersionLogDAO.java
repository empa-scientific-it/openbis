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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.AbstractLobCreatingPreparedStatementCallback;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.db.Script;

/**
 * Class which logs database migration steps in the database.
 * 
 * @author Franz-Josef Elmer
 */
public class DatabaseVersionLogDAO extends JdbcDaoSupport implements IDatabaseVersionLogDAO
{
    public static final String DB_VERSION_LOG = "database_version_logs";

    private static final String ENCODING = "utf8";

    private static final String RUN_EXCEPTION = "run_exception";

    private static final String MODULE_CODE = "module_code";

    private static final String RUN_STATUS_TIMESTAMP = "run_status_timestamp";

    private static final String RUN_STATUS = "run_status";

    private static final String MODULE_NAME = "module_name";

    private static final String DB_VERSION = "db_version";

    private static final String SELECT_LAST_ENTRY =
            "select * from " + DB_VERSION_LOG + " where " + RUN_STATUS_TIMESTAMP
                    + " in (select max(" + RUN_STATUS_TIMESTAMP + ") from " + DB_VERSION_LOG + ")";

    private static final class LogEntryRowMapper implements RowMapper<LogEntry>
    {
        private final LobHandler lobHandler;

        LogEntryRowMapper(LobHandler lobHandler)
        {
            this.lobHandler = lobHandler;
        }

        @Override
        public LogEntry mapRow(ResultSet rs, int rowNum) throws SQLException
        {
            final LogEntry logEntry = new LogEntry();
            logEntry.setVersion(rs.getString(DB_VERSION));
            logEntry.setModuleName(rs.getString(MODULE_NAME));
            logEntry.setRunStatus(rs.getString(RUN_STATUS));
            logEntry.setRunStatusTimestamp(rs.getDate(RUN_STATUS_TIMESTAMP));
            try
            {
                final byte[] moduleCodeOrNull = lobHandler.getBlobAsBytes(rs, MODULE_CODE);
                final String moduleCodeString =
                        (moduleCodeOrNull != null) ? new String(moduleCodeOrNull, ENCODING) : "";
                logEntry.setModuleCode(moduleCodeString);
            } catch (UnsupportedEncodingException ex)
            {
                throw new CheckedExceptionTunnel(ex);
            }
            logEntry.setRunException(lobHandler.getClobAsString(rs, RUN_EXCEPTION));
            return logEntry;
        }
    }

    private static byte[] getAsByteArray(String string)
    {
        try
        {
            return string.getBytes(ENCODING);
        } catch (UnsupportedEncodingException ex)
        {
            throw new CheckedExceptionTunnel(ex);
        }
    }

    final LobHandler lobHandler;

    public DatabaseVersionLogDAO(DataSource dataSource, LobHandler lobHandler)
    {
        setDataSource(dataSource);
        this.lobHandler = lobHandler;
    }

    @Override
    public boolean canConnectToDatabase()
    {
        try
        {
            getLastEntry();
            return true;
        } catch (BadSqlGrammarException ex)
        {
            return false;
        } catch (CannotGetJdbcConnectionException ex)
        {
            return false;
        }
    }

    @Override
    public void createTable(Script script)
    {
        JdbcTemplate template = getJdbcTemplate();
        template.execute(script.getContent());
    }

    @Override
    public LogEntry getLastEntry()
    {
        JdbcTemplate template = getJdbcTemplate();
        List<LogEntry> entries =
                template.query(SELECT_LAST_ENTRY, new LogEntryRowMapper(lobHandler));

        return entries.size() == 0 ? null : entries.get(entries.size() - 1);
    }

    /**
     * Inserts a new entry into the version log with {@link LogEntry.RunStatus#START}.
     * 
     * @param moduleScript The script of the module to be logged.
     */
    @Override
    public void logStart(final Script moduleScript)
    {
        JdbcTemplate template = getJdbcTemplate();
        PreparedStatementCallback callback =
                new AbstractLobCreatingPreparedStatementCallback(this.lobHandler)
                    {
                        @Override
                        protected void setValues(PreparedStatement ps, LobCreator lobCreator)
                                throws SQLException
                        {
                            ps.setString(1, moduleScript.getVersion());
                            ps.setString(2, moduleScript.getName());
                            ps.setString(3, LogEntry.RunStatus.START.toString());
                            ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
                            lobCreator.setBlobAsBytes(ps, 5, getAsByteArray(moduleScript
                                    .getContent()));
                        }
                    };
        template.execute("insert into " + DB_VERSION_LOG + " (" + DB_VERSION + "," + MODULE_NAME
                + "," + RUN_STATUS + "," + RUN_STATUS_TIMESTAMP + "," + MODULE_CODE
                + ") values (?,?,?,?,?)", callback);
    }

    /**
     * Update log entry specified by version and module name to {@link LogEntry.RunStatus#SUCCESS}.
     * 
     * @param moduleScript The script of the successfully applied module.
     */
    @Override
    public void logSuccess(final Script moduleScript)
    {
        JdbcTemplate template = getJdbcTemplate();
        template.update("update " + DB_VERSION_LOG + " SET " + RUN_STATUS + " = ? , "
                + RUN_STATUS_TIMESTAMP + " = ? " + "where " + DB_VERSION + " = ? and "
                + MODULE_NAME + " = ?", LogEntry.RunStatus.SUCCESS.toString(), new Date(System
                        .currentTimeMillis()),
                moduleScript.getVersion(), moduleScript.getName());
    }

    /**
     * Update log entry specified by version and module name to {@link LogEntry.RunStatus#FAILED}.
     * 
     * @param moduleScript Script of the failed module.
     * @param runException Exception causing the failure.
     */
    @Override
    public void logFailure(final Script moduleScript, Throwable runException)
    {
        final StringWriter stringWriter = new StringWriter();
        runException.printStackTrace(new PrintWriter(stringWriter));
        JdbcTemplate template = getJdbcTemplate();
        PreparedStatementCallback callback =
                new AbstractLobCreatingPreparedStatementCallback(this.lobHandler)
                    {
                        @Override
                        protected void setValues(PreparedStatement ps, LobCreator lobCreator)
                                throws SQLException
                        {
                            ps.setString(1, LogEntry.RunStatus.FAILED.toString());
                            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                            lobCreator.setBlobAsBytes(ps, 3,
                                    getAsByteArray(stringWriter.toString()));
                            ps.setString(4, moduleScript.getVersion());
                            ps.setString(5, moduleScript.getName());
                        }
                    };
        template.execute("update " + DB_VERSION_LOG + " SET " + RUN_STATUS + " = ?, "
                + RUN_STATUS_TIMESTAMP + " = ?, " + RUN_EXCEPTION + " = ? where " + DB_VERSION
                + " = ? and " + MODULE_NAME + " = ?", callback);
    }

}

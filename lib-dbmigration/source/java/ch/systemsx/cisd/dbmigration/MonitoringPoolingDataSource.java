/*
 * Copyright 2012 ETH Zuerich, CISD
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

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.dbcp.DelegatingCallableStatement;
import org.apache.commons.dbcp.DelegatingConnection;
import org.apache.commons.dbcp.DelegatingPreparedStatement;
import org.apache.commons.dbcp.DelegatingStatement;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.ObjectPool;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.event.BooleanEvent;
import ch.systemsx.cisd.common.logging.event.LongEvent;
import ch.systemsx.cisd.dbmigration.logging.DbConnectionLogConfiguration;

/**
 * A {@link PoolingDataSource} that can log usage of active connections to help investigating connection leaks.
 * 
 * @author Bernd Rinn
 */
class MonitoringPoolingDataSource extends PoolingDataSource
{
    private static final long DEFAULT_PERIOD_TIMER_MILLIS = 10 * 1000L;

    private final static Logger machineLog =
            LogFactory.getLogger(LogCategory.MACHINE, MonitoringPoolingDataSource.class);

    // static fields for inter-datasource connection logging

    private static final Timer infoControlTimer = new Timer("db connection info", true);

    private static DatabaseConnectionInfoController infoController =
            new DatabaseConnectionInfoController();

    static final Map<Integer, BorrowedConnectionRecord> activeConnections =
            new ConcurrentHashMap<Integer, BorrowedConnectionRecord>();

    private static volatile boolean logStackTrace;

    private static volatile Level configuredLogLevel;

    private final long activeConnectionsLogInterval;

    private final String url;

    private long lastLogged;

    private int maxActiveSinceLastLogged;

    static
    {
        infoControlTimer.schedule(infoController, DEFAULT_PERIOD_TIMER_MILLIS,
                DEFAULT_PERIOD_TIMER_MILLIS);
    }

    public MonitoringPoolingDataSource(ObjectPool pool, String url,
            long activeConnectionsLogInterval)
    {
        super(pool);
        this.activeConnectionsLogInterval = activeConnectionsLogInterval;
        this.url = url;
    }

    /**
     * A record of information on a connection borrowed from the pool.
     */
    private static class BorrowedConnectionRecord
    {
        final long timeOfBorrowing;

        final String dbUrl;

        final String threadName;

        final StackTraceElement[] borrowStackTraceOrNull;

        BorrowedConnectionRecord(long timeOfBorrowing, String dbUrl, String threadName,
                StackTraceElement[] borrowStackTraceOrNull)
        {
            this.timeOfBorrowing = timeOfBorrowing;
            this.dbUrl = dbUrl;
            this.threadName = threadName;
            this.borrowStackTraceOrNull = borrowStackTraceOrNull;
        }
    }

    /**
     * A class that controls printing of information about active database connections.
     */
    private static class DatabaseConnectionInfoController extends TimerTask
    {

        @Override
        public void run()
        {
            LongEvent printActiveEvent = DbConnectionLogConfiguration.getInstance().getDbConnectionsPrintActiveEvent();
            if (printActiveEvent != null)
            {
                final long oldActiveConnTimeMillis = printActiveEvent.getValue() != null ? printActiveEvent.getValue() : 0;
                logActiveDatabaseConnections(oldActiveConnTimeMillis);
            }

            BooleanEvent stacktraceEvent = DbConnectionLogConfiguration.getInstance().getDbConnectionsStacktraceEvent();
            if (stacktraceEvent != null && stacktraceEvent.getValue() != null)
            {
                setLogStackTrace(stacktraceEvent.getValue());
            }

            BooleanEvent debugEvent = DbConnectionLogConfiguration.getInstance().getDbConnectionsDebugEvent();
            if (debugEvent != null && debugEvent.getValue() != null)
            {
                if (debugEvent.getValue())
                {
                    configuredLogLevel = machineLog.getLevel();
                    machineLog.setLevel(Level.DEBUG);
                    machineLog.info("Enable debug log for database connections.");
                } else
                {
                    final Level level = configuredLogLevel;
                    machineLog.setLevel((level == null) ? Level.INFO : level);
                    machineLog.info("Disable debug log for database connections.");

                }
            }
        }
    }

    /**
     * Re-schedules the database info controller with a new period.
     */
    public synchronized static void rescheduleInfoController(long periodInMillis)
    {
        infoController.cancel();
        infoControlTimer.purge();
        infoController = new DatabaseConnectionInfoController();
        infoControlTimer.schedule(infoController, periodInMillis, periodInMillis);
    }

    /**
     * Return <code>true</code>, if recording stack traces for database connection borrowing is switched on.
     */
    public static boolean isLogStackTrace()
    {
        return logStackTrace;
    }

    /**
     * Switches recording stack traces for database connection borrowing on and off.
     */
    public static void setLogStackTrace(boolean logStackTrace)
    {
        MonitoringPoolingDataSource.logStackTrace = logStackTrace;
        machineLog.info((logStackTrace ? "Enable" : "Disable")
                + " stacktrace recording for database connections.");
    }

    /**
     * Sets the log level for connection logging.
     */
    public static void setLogLevel(Level logLevel)
    {
        MonitoringPoolingDataSource.machineLog.setLevel(logLevel);
        configuredLogLevel = logLevel;
        machineLog.info("Set loglevel to " + logLevel + ".");
    }

    static void logActiveDatabaseConnections(long oldActiveConnTimeMillis)
    {
        long now = System.currentTimeMillis();
        StringBuilder out = new StringBuilder();

        final List<BorrowedConnectionRecord> records =
                new ArrayList<BorrowedConnectionRecord>(activeConnections.values());
        Collections.sort(records, new Comparator<BorrowedConnectionRecord>()
            {
                @Override
                public int compare(BorrowedConnectionRecord o1,
                        BorrowedConnectionRecord o2)
                {
                    return (int) (o2.timeOfBorrowing - o1.timeOfBorrowing);
                }
            });
        if (records.isEmpty()
                || now - records.get(0).timeOfBorrowing < oldActiveConnTimeMillis)
        {
            out.append(String.format("There are no active database connections older than %d ms.\n",
                    oldActiveConnTimeMillis));
            return;
        }
        out.append(String.format("\n>>>--- %1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL " +
                "---------------------------------->>>\n", now));
        if (oldActiveConnTimeMillis > 0)
        {
            out.append(String.format("Active database connection overview older than %d ms (sorted by age)\n",
                    oldActiveConnTimeMillis));
        } else
        {
            out.append(String.format("Active database connection overview (sorted by age)\n"));

        }
        out.append(String.format("%-25s\t%-25s\t%-25s\n", "Time", "Thread", "Database"));
        boolean hasStackTraces = false;
        for (BorrowedConnectionRecord record : records)
        {
            if (now - record.timeOfBorrowing > oldActiveConnTimeMillis)
            {
                out.append(String.format(
                        "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL  \t%2$-25s\t%3$-25s\n",
                        record.timeOfBorrowing, record.threadName, record.dbUrl));
                hasStackTraces |= (record.borrowStackTraceOrNull != null);
            }
        }

        if (hasStackTraces)
        {
            out.append(String.format("\nActive database connection stacktraces"));
            // Print stacktraces
            boolean noNewline = true;
            for (BorrowedConnectionRecord record : records)
            {
                if (now - record.timeOfBorrowing > oldActiveConnTimeMillis
                        && record.borrowStackTraceOrNull != null)
                {
                    if (noNewline)
                    {
                        noNewline = false;
                    } else
                    {
                        out.append("\n");
                    }
                    out.append(String.format("Time: %1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL\n",
                            record.timeOfBorrowing));
                    out.append(String.format("Database: %s\n", record.dbUrl));
                    out.append(String.format("Thread: %s\n", record.threadName));
                    out.append(String.format("Stacktrace:\n" + traceToString(record.borrowStackTraceOrNull)));
                }
            }
        }
        out.append(String.format("<<<--- %1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL " +
                "----------------------------------<<<\n", now));
        machineLog.info(out.toString());
    }

    /**
     * Return a {@link java.sql.Connection} from my pool, according to the contract specified by {@link ObjectPool#borrowObject}.
     */
    @SuppressWarnings("deprecation")
    @Override
    public Connection getConnection() throws SQLException
    {
        try
        {
            Connection conn = (Connection) (_pool.borrowObject());
            final long now = System.currentTimeMillis();
            final int numActive = _pool.getNumActive();
            maxActiveSinceLastLogged = Math.max(maxActiveSinceLastLogged, numActive);
            if ((activeConnectionsLogInterval > 0)
                    && (now - lastLogged > activeConnectionsLogInterval)
                    && maxActiveSinceLastLogged > 1)
            {
                if (machineLog.isInfoEnabled())
                {
                    machineLog.info(String.format(
                            "Active database connections [%s]: current: %d, peak: %d.", url,
                            numActive, maxActiveSinceLastLogged));
                }
                lastLogged = now;
                maxActiveSinceLastLogged = 0;
            }
            if (conn != null)
            {
                conn = new PoolGuardConnectionWrapper(conn);
                activeConnections.put(System.identityHashCode(conn),
                        new BorrowedConnectionRecord(now, url,
                                Thread.currentThread().getName(), logStackTrace ? getStackTrace()
                                        : null));
            }
            return conn;
        } catch (SQLException e)
        {
            throw e;
        } catch (NoSuchElementException e)
        {
            throw new org.apache.commons.dbcp.SQLNestedException(
                    "Cannot get a connection, pool error " + e.getMessage(), e);
        } catch (RuntimeException e)
        {
            throw e;
        } catch (Exception e)
        {
            throw new org.apache.commons.dbcp.SQLNestedException(
                    "Cannot get a connection, general error", e);
        }
    }

    static StackTraceElement[] getStackTrace()
    {
        final Throwable th = new Throwable();
        th.fillInStackTrace();
        return th.getStackTrace();
    }

    static String tryGetServiceMethodName(StackTraceElement[] stackTrace)
    {
        String innerMethodName = null;
        String outerMethodName = null;
        for (StackTraceElement e : stackTrace)
        {
            if (e.getClassName().contains("$Proxy"))
            {
                if (innerMethodName == null)
                {
                    innerMethodName = e.getMethodName();
                }
                outerMethodName = e.getMethodName();
            }
        }
        if (innerMethodName == null)
        {
            return null;
        }
        if (innerMethodName.equals(outerMethodName))
        {
            return outerMethodName;
        } else
        {
            return outerMethodName + " / " + innerMethodName;
        }
    }

    static String traceToString(StackTraceElement[] trace)
    {
        final StringBuilder builder = new StringBuilder();
        traceToString(builder, trace);
        return builder.toString();
    }

    static void traceToString(StringBuilder builder, StackTraceElement[] trace)
    {
        boolean skip = true;
        for (StackTraceElement te : trace)
        {
            if (skip)
            {
                skip = false;
                continue;
            }
            builder.append("\tat ");
            builder.append(te.toString());
            builder.append('\n');
        }
    }

    /**
     * PoolGuardConnectionWrapper is a Connection wrapper that makes sure a closed connection cannot be used anymore.
     */
    private class PoolGuardConnectionWrapper extends DelegatingConnection
    {
        PoolGuardConnectionWrapper(Connection delegate)
        {
            super(delegate);
            log("Hand out database connection");
        }

        void log(String action)
        {
            if (machineLog.isDebugEnabled())
            {
                final int numActive = _pool.getNumActive();
                final StackTraceElement[] stackTrace = getStackTrace();
                final String serviceMethod = tryGetServiceMethodName(stackTrace);
                final StringBuilder b = new StringBuilder();
                b.append('[');
                b.append(url);
                b.append("] ");
                b.append(action);
                b.append(", id=");
                b.append(hashCode());
                b.append(", active=");
                b.append(numActive);
                if (serviceMethod != null)
                {
                    b.append(", service method: ");
                    b.append(serviceMethod);
                }
                if (logStackTrace)
                {
                    b.append(", stacktrace:\n");
                    traceToString(b, stackTrace);
                } else
                {
                    b.append('.');
                }
                machineLog.debug(b.toString());
            }
        }

        @Override
        protected void checkOpen() throws SQLException
        {
            if (_conn == null)
            {
                throw new SQLException("Connection is closed.");
            }
        }

        @Override
        public void close() throws SQLException
        {
            if (_conn != null)
            {
                log("Return database connection");
                this._conn.close();
                super.setDelegate(null);
                activeConnections.remove(System.identityHashCode(this));
            }
        }

        @Override
        public boolean isClosed() throws SQLException
        {
            if (_conn == null)
            {
                return true;
            }
            return _conn.isClosed();
        }

        @Override
        public void clearWarnings() throws SQLException
        {
            checkOpen();
            _conn.clearWarnings();
        }

        @Override
        public void commit() throws SQLException
        {
            checkOpen();
            _conn.commit();
        }

        @Override
        public Statement createStatement() throws SQLException
        {
            checkOpen();
            return new DelegatingStatement(this, _conn.createStatement());
        }

        @Override
        public Statement createStatement(int resultSetType, int resultSetConcurrency)
                throws SQLException
        {
            checkOpen();
            return new DelegatingStatement(this, _conn.createStatement(resultSetType,
                    resultSetConcurrency));
        }

        @Override
        public boolean innermostDelegateEquals(Connection c)
        {
            Connection innerCon = super.getInnermostDelegate();
            if (innerCon == null)
            {
                return c == null;
            } else
            {
                return innerCon.equals(c);
            }
        }

        @Override
        public boolean getAutoCommit() throws SQLException
        {
            checkOpen();
            return _conn.getAutoCommit();
        }

        @Override
        public String getCatalog() throws SQLException
        {
            checkOpen();
            return _conn.getCatalog();
        }

        @Override
        public DatabaseMetaData getMetaData() throws SQLException
        {
            checkOpen();
            return _conn.getMetaData();
        }

        @Override
        public int getTransactionIsolation() throws SQLException
        {
            checkOpen();
            return _conn.getTransactionIsolation();
        }

        @SuppressWarnings(
        { "rawtypes", "unchecked" })
        @Override
        public Map getTypeMap() throws SQLException
        {
            checkOpen();
            return _conn.getTypeMap();
        }

        @Override
        public SQLWarning getWarnings() throws SQLException
        {
            checkOpen();
            return _conn.getWarnings();
        }

        @Override
        public boolean isReadOnly() throws SQLException
        {
            checkOpen();
            return _conn.isReadOnly();
        }

        @Override
        public String nativeSQL(String sql) throws SQLException
        {
            checkOpen();
            return _conn.nativeSQL(sql);
        }

        @Override
        public CallableStatement prepareCall(String sql) throws SQLException
        {
            checkOpen();
            return new DelegatingCallableStatement(this, _conn.prepareCall(sql));
        }

        @Override
        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency)
                throws SQLException
        {
            checkOpen();
            return new DelegatingCallableStatement(this, _conn.prepareCall(sql, resultSetType,
                    resultSetConcurrency));
        }

        @Override
        public PreparedStatement prepareStatement(String sql) throws SQLException
        {
            checkOpen();
            return new DelegatingPreparedStatement(this, _conn.prepareStatement(sql));
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int resultSetType,
                int resultSetConcurrency) throws SQLException
        {
            checkOpen();
            return new DelegatingPreparedStatement(this, _conn.prepareStatement(sql,
                    resultSetType, resultSetConcurrency));
        }

        @Override
        public void rollback() throws SQLException
        {
            checkOpen();
            _conn.rollback();
        }

        @Override
        public void setAutoCommit(boolean autoCommit) throws SQLException
        {
            checkOpen();
            _conn.setAutoCommit(autoCommit);
        }

        @Override
        public void setCatalog(String catalog) throws SQLException
        {
            checkOpen();
            _conn.setCatalog(catalog);
        }

        @Override
        public void setReadOnly(boolean readOnly) throws SQLException
        {
            checkOpen();
            _conn.setReadOnly(readOnly);
        }

        @Override
        public void setTransactionIsolation(int level) throws SQLException
        {
            checkOpen();
            _conn.setTransactionIsolation(level);
        }

        @Override
        @SuppressWarnings(
        { "rawtypes", "unchecked" })
        public void setTypeMap(Map map) throws SQLException
        {
            checkOpen();
            _conn.setTypeMap(map);
        }

        @Override
        public String toString()
        {
            if (_conn == null)
            {
                return "NULL";
            }
            return _conn.toString();
        }

        @Override
        public int getHoldability() throws SQLException
        {
            checkOpen();
            return _conn.getHoldability();
        }

        @Override
        public void setHoldability(int holdability) throws SQLException
        {
            checkOpen();
            _conn.setHoldability(holdability);
        }

        @Override
        public java.sql.Savepoint setSavepoint() throws SQLException
        {
            checkOpen();
            return _conn.setSavepoint();
        }

        @Override
        public java.sql.Savepoint setSavepoint(String name) throws SQLException
        {
            checkOpen();
            return _conn.setSavepoint(name);
        }

        @Override
        public void releaseSavepoint(java.sql.Savepoint savepoint) throws SQLException
        {
            checkOpen();
            _conn.releaseSavepoint(savepoint);
        }

        @Override
        public void rollback(java.sql.Savepoint savepoint) throws SQLException
        {
            checkOpen();
            _conn.rollback(savepoint);
        }

        @Override
        public Statement createStatement(int resultSetType, int resultSetConcurrency,
                int resultSetHoldability) throws SQLException
        {
            checkOpen();
            return new DelegatingStatement(this, _conn.createStatement(resultSetType,
                    resultSetConcurrency, resultSetHoldability));
        }

        @Override
        public CallableStatement prepareCall(String sql, int resultSetType,
                int resultSetConcurrency, int resultSetHoldability) throws SQLException
        {
            checkOpen();
            return new DelegatingCallableStatement(this, _conn.prepareCall(sql, resultSetType,
                    resultSetConcurrency, resultSetHoldability));
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
                throws SQLException
        {
            checkOpen();
            return new DelegatingPreparedStatement(this, _conn.prepareStatement(sql,
                    autoGeneratedKeys));
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int resultSetType,
                int resultSetConcurrency, int resultSetHoldability) throws SQLException
        {
            checkOpen();
            return new DelegatingPreparedStatement(this, _conn.prepareStatement(sql,
                    resultSetType, resultSetConcurrency, resultSetHoldability));
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
                throws SQLException
        {
            checkOpen();
            return new DelegatingPreparedStatement(this, _conn.prepareStatement(sql,
                    columnIndexes));
        }

        @Override
        public PreparedStatement prepareStatement(String sql, String[] columnNames)
                throws SQLException
        {
            checkOpen();
            return new DelegatingPreparedStatement(this,
                    _conn.prepareStatement(sql, columnNames));
        }

        /**
         * @see org.apache.commons.dbcp.DelegatingConnection#getDelegate()
         */
        @Override
        public Connection getDelegate()
        {
            if (isAccessToUnderlyingConnectionAllowed())
            {
                return super.getDelegate();
            } else
            {
                return null;
            }
        }

        /**
         * @see org.apache.commons.dbcp.DelegatingConnection#getInnermostDelegate()
         */
        @Override
        public Connection getInnermostDelegate()
        {
            if (isAccessToUnderlyingConnectionAllowed())
            {
                return super.getInnermostDelegate();
            } else
            {
                return null;
            }
        }
    }
}

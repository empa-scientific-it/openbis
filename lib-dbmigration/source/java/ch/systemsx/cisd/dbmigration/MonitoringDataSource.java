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

import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * A data source that performs monitoring of active connections to help track load and connection leaks.
 * 
 * @author Bernd Rinn
 */
public class MonitoringDataSource extends BasicDataSource
{
    private final static Logger machineLog =
            LogFactory.getLogger(LogCategory.MACHINE, MonitoringPoolingDataSource.class);

    private long activeConnectionsLogInterval;

    @Override
    public synchronized void setUrl(String url)
    {
        if (machineLog.isDebugEnabled())
        {
            final Throwable th = new Throwable();
            th.fillInStackTrace();
            machineLog.debug("Creating data source '" + url + "'.", th);
        }
        super.setUrl(url);
    }

    /**
     * Returns the interval (in ms) between two regular log entries of currently active database connections if more than one connection is active.
     */
    public long getActiveConnectionsLogInterval()
    {
        return activeConnectionsLogInterval;
    }

    /**
     * Set the interval (in ms) between two regular log entries of currently active database connections if more than one connection is active.
     */
    public void setActiveConnectionsLogInterval(long activeConnectionLogInterval)
    {
        this.activeConnectionsLogInterval = activeConnectionLogInterval;
    }

    @Override
    protected void createDataSourceInstance() throws SQLException
    {
        final MonitoringPoolingDataSource pds =
                new MonitoringPoolingDataSource(connectionPool, url, activeConnectionsLogInterval);
        pds.setAccessToUnderlyingConnectionAllowed(isAccessToUnderlyingConnectionAllowed());
        pds.setLogWriter(logWriter);
        dataSource = pds;
    }

    // Remove once we switched to commons dbcp 1.4 or later.

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException
    {
        throw new SQLException("BasicDataSource is not a wrapper.");
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException
    {
        return false;
    }

    @Override
    public synchronized void close() throws SQLException
    {
        if (machineLog.isDebugEnabled())
        {
            final Throwable th = new Throwable();
            th.fillInStackTrace();
            machineLog.debug("Closing data source '" + getUrl() + "'.", th);
        }
        super.close();
    }

}

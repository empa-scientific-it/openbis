/*
 * Copyright 2010 ETH Zuerich, CISD
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

import javax.sql.DataSource;

import org.apache.commons.pool.impl.GenericObjectPool;

/**
 * @author Piotr Buczek
 */
public class BasicDataSourceFactory implements IDataSourceFactory
{

    /** @see GenericObjectPool#DEFAULT_MAX_ACTIVE */
    private static final int DEFAULT_MAX_ACTIVE = 20;

    /** @see GenericObjectPool#DEFAULT_MAX_IDLE */
    private static final int DEFAULT_MAX_IDLE = DEFAULT_MAX_ACTIVE;

    private static final int DEFAULT_MAX_WAIT = 60 * 1000;

    private static final int DEFAULT_ACTIVE_CONNECTIONS_LOG_INTERVAL = 3600 * 1000;

    private long maxWaitMillis = DEFAULT_MAX_WAIT;

    private int maxIdle = DEFAULT_MAX_IDLE;

    private int maxActive = DEFAULT_MAX_ACTIVE;

    private long activeConnectionsLogIntervalMillis = DEFAULT_ACTIVE_CONNECTIONS_LOG_INTERVAL;

    private boolean maxIdleIsDefault = true;

    //
    // IDataSourceFactory
    //

    @Override
    public final DataSource createDataSource(final String driver, final String url,
            final String owner, final String password, final String validationQuery)
    {
        final MonitoringDataSource dataSource = new MonitoringDataSource();
        dataSource.setAccessToUnderlyingConnectionAllowed(true);
        dataSource.setDriverClassName(driver);
        dataSource.setUrl(url);
        dataSource.setUsername(owner);
        dataSource.setPassword(password);
        dataSource.setMaxIdle(maxIdle);
        dataSource.setMaxActive(maxActive);
        dataSource.setMaxWait(maxWaitMillis * 1000L);
        dataSource.setActiveConnectionsLogInterval(activeConnectionsLogIntervalMillis);
        dataSource.setValidationQuery(validationQuery);
        return dataSource;
    }

    @Override
    public long getMaxWait()
    {
        return maxWaitMillis;
    }

    @Override
    public void setMaxWait(long maxWait)
    {
        this.maxWaitMillis = maxWait;
    }

    @Override
    public int getMaxIdle()
    {
        return maxIdle;
    }

    @Override
    public void setMaxIdle(int maxIdle)
    {
        this.maxIdle = maxIdle;
        this.maxIdleIsDefault = false;
    }

    @Override
    public int getMaxActive()
    {
        return maxActive;
    }

    @Override
    public void setMaxActive(int maxActive)
    {
        this.maxActive = maxActive;
        if (maxIdleIsDefault)
        {
            this.maxIdle = maxActive;
        }
    }

    @Override
    public long getActiveConnectionsLogInterval()
    {
        return activeConnectionsLogIntervalMillis;
    }

    @Override
    public void setActiveConnectionsLogInterval(long activeConnectionLogIntervalMillis)
    {
        this.activeConnectionsLogIntervalMillis = activeConnectionLogIntervalMillis;
    }

}

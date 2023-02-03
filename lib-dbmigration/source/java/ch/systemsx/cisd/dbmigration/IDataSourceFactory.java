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

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;

/**
 * Factory for a {@link DataSource}.
 * 
 * @author Franz-Josef Elmer
 */
public interface IDataSourceFactory
{
    /**
     * Creates a data source for the specified database credentials.
     */
    public DataSource createDataSource(String driver, String url, String owner, String password,
            String validationQuery);

    public int getMaxIdle();

    /**
     * @see BasicDataSource#setMaxIdle(int)
     * @see GenericObjectPool#DEFAULT_MAX_IDLE
     */
    public void setMaxIdle(int maxIdle);

    public int getMaxActive();

    /**
     * @see BasicDataSource#setMaxActive(int)
     * @see GenericObjectPool#DEFAULT_MAX_ACTIVE
     */
    public void setMaxActive(int maxActive);

    public long getMaxWait();

    /**
     * @see BasicDataSource#setMaxWait(long)
     * @see GenericObjectPool#DEFAULT_MAX_WAIT
     */
    public void setMaxWait(long maxWait);

    public long getActiveConnectionsLogInterval();

    /**
     * Set the interval (in ms) between two regular log entries of currently active database connections if more than one connection is active. Set to
     * a negative value to disable this feature.
     */
    public void setActiveConnectionsLogInterval(long activeConnectionLogIntervalMillis);

}

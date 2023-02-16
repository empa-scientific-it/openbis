/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.dbmigration.migration;

import javax.sql.DataSource;

import ch.systemsx.cisd.dbmigration.IDataSourceFactory;

public class SqlMigrationDataSourceFactory implements IDataSourceFactory
{

    @Override
    public DataSource createDataSource(String driver, String url, String owner, String password,
            String validationQuery)
    {
        return new SqlMigrationDataSource(driver, url, owner, password);
    }

    @Override
    public void setMaxActive(int maxActive)
    {
    }

    @Override
    public void setMaxIdle(int maxIdle)
    {
    }

    @Override
    public void setMaxWait(long maxWait)
    {
    }

    @Override
    public void setActiveConnectionsLogInterval(long activeConnectionLogInterval)
    {
    }

    @Override
    public int getMaxIdle()
    {
        return 0;
    }

    @Override
    public int getMaxActive()
    {
        return 0;
    }

    @Override
    public long getMaxWait()
    {
        return 0;
    }

    @Override
    public long getActiveConnectionsLogInterval()
    {
        return 0;
    }

}
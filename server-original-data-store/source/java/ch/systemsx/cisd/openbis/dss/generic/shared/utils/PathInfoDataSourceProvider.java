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

import javax.sql.DataSource;

import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;

/**
 * Helper method for providing data source to pathinfo database.
 * 
 * @author Franz-Josef Elmer
 */
public class PathInfoDataSourceProvider
{
    public static final String DATA_SOURCE_NAME = "path-info-db";

    public static DataSource getDataSource()
    {
        return ServiceProvider.getDataSourceProvider().getDataSource(DATA_SOURCE_NAME);
    }

    public static boolean isDataSourceDefined()
    {
        try
        {
            PathInfoDataSourceProvider.getDataSource();
            return true;
        } catch (IllegalArgumentException ex)
        {
            return false;
        }
    }

}

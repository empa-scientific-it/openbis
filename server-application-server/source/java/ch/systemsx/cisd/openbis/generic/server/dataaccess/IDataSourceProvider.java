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

package ch.systemsx.cisd.openbis.generic.server.dataaccess;

import javax.sql.DataSource;

import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * Interface for providing a {@link DataSource} for a technology specific database based on data set code or data store server code
 * 
 * @author Franz-Josef Elmer
 */
public interface IDataSourceProvider
{
    /**
     * Returns an appropriated data source for specified data store server code and technology.
     * 
     * @throws IllegalArgumentException if getting data source by data store server code isn't supported for the specified technology.
     * @throws UserFailureException if the specified data store server doesn't exist.
     */
    public DataSource getDataSourceByDataStoreServerCode(String dssCode, String technology);
}

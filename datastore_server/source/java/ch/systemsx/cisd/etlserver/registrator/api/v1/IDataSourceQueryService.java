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

package ch.systemsx.cisd.etlserver.registrator.api.v1;

import java.util.Map;

import net.lemnik.eodsql.DataSet;

/**
 * A service that supports executing queries on a data source configured in the DSS
 * service.properties.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public interface IDataSourceQueryService
{
    /**
     * Execute a query against the data source with the specified name.
     * 
     * @param dataSourceName The name of the data source to query against, as declared in the
     *            service.properties file.
     * @param query The SQL query to execute, possibly including parameters marked by '?'.
     * @param parameters The values for filling in the query parameters.
     * @return A List of Maps with the data. Do not forget to close the result when done!
     * @throw IllegalArgumentException Throws if there is no data source with the given name.
     */
    DataSet<Map<String, Object>> select(String dataSourceName, String query, Object... parameters)
            throws IllegalArgumentException;
}

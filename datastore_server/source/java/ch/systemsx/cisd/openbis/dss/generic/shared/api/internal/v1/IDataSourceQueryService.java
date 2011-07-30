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

package ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1;

import java.util.Map;

import net.lemnik.eodsql.DataSet;

/**
 * A service that supports executing queries on a data source configured in the DSS
 * service.properties.
 * <p>
 * Jython usage example:
 * 
 * <pre>
 * results = query_service.select("data-source-name", "SELECT * FROM table_name WHERE id > 1") 
 * [... do stuff with results]
 * results.close()
 * </pre>
 * <p>
 * If you need to do this frequently, you may want to extract this into a function
 * 
 * <pre>
 * def execute_query(query_service, block, query, params=None):
 *     if params is None:
 *         result = query_service.select("data-source-name", query)
 *     else:
 *         result = query_service.select("data-source-name", query, params)
 *     block(result)
 *     result.close()
 * </pre>
 * 
 * {@link #update(String, String, Object...)} and {@link #insert(String, String, Object...)} are for
 * performing statements that don't return a result. Use {@link #insert(String, String, Object...)}
 * if you need to get an auto-generated key of an insert statement, that is if you have a key that
 * is doing an <code>AUTO_INCREMENT</code> of some sort.
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
     * @param query The SQL query to execute, possibly including parameters marked by '?{X}' where X
     *            is the parameter number.
     * @return A List of Maps with the data. Do not forget to close the result when done!
     * @throw IllegalArgumentException Throws if there is no data source with the given name.
     * @throw InvalidQueryException Thrown the given query string cannot be parsed, or doesn't match
     *        the given parameters.
     */
    DataSet<Map<String, Object>> select(String dataSourceName, String query)
            throws IllegalArgumentException;

    /**
     * Execute a query against the data source with the specified name.
     * 
     * @param dataSourceName The name of the data source to query against, as declared in the
     *            service.properties file.
     * @param query The SQL query to execute, possibly including parameters marked by '?{X}' where X
     *            is the parameter number.
     * @param parameters The values for filling in the query parameters.
     * @return A List of Maps with the data. Do not forget to close the result when done!
     * @throw IllegalArgumentException Thrown if there is no data source with the given name.
     * @throw InvalidQueryException Thrown the given query string cannot be parsed, or doesn't match
     *        the given parameters.
     */
    DataSet<Map<String, Object>> select(String dataSourceName, String query, Object... parameters)
            throws IllegalArgumentException;

    /**
     * Execute an update against the data source with the specified name.
     * 
     * @param dataSourceName The name of the data source to query against, as declared in the
     *            service.properties file.
     * @param query The SQL query to execute, possibly including parameters marked by '?{X}' where X
     *            is the parameter number.
     * @return Either the row count (for <code>INSERT</code>, <code>UPDATE</code>, or
     *         <code>DELETE</code> statements) or 0 for SQL statements that return nothing
     * @throw IllegalArgumentException Throws if there is no data source with the given name.
     * @throw InvalidQueryException Thrown the given query string cannot be parsed, or doesn't match
     *        the given parameters.
     */
    int update(String dataSourceName, String query) throws IllegalArgumentException;

    /**
     * Execute an update against the data source with the specified name.
     * 
     * @param dataSourceName The name of the data source to query against, as declared in the
     *            service.properties file.
     * @param query The SQL query to execute, possibly including parameters marked by '?{X}' where X
     *            is the parameter number.
     * @param parameters The values for filling in the query parameters.
     * @return Either the row count (for <code>INSERT</code>, <code>UPDATE</code>, or
     *         <code>DELETE</code> statements) or 0 for SQL statements that return nothing
     * @throw IllegalArgumentException Thrown if there is no data source with the given name.
     * @throw InvalidQueryException Thrown the given query string cannot be parsed, or doesn't match
     *        the given parameters.
     */
    int update(String dataSourceName, String query, Object... parameters)
            throws IllegalArgumentException;

    /**
     * Execute an insert against the data source with the specified name.
     * 
     * @param dataSourceName The name of the data source to query against, as declared in the
     *            service.properties file.
     * @param query The SQL query to execute, possibly including parameters marked by '?{X}' where X
     *            is the parameter number.
     * @return The generated key of the insert statement, or -1, of no key was generated
     * @throw IllegalArgumentException Throws if there is no data source with the given name.
     * @throw InvalidQueryException Thrown the given query string cannot be parsed, or doesn't match
     *        the given parameters.
     */
    long insert(String dataSourceName, String query) throws IllegalArgumentException;

    /**
     * Execute an insert against the data source with the specified name.
     * 
     * @param dataSourceName The name of the data source to query against, as declared in the
     *            service.properties file.
     * @param query The SQL query to execute, possibly including parameters marked by '?{X}' where X
     *            is the parameter number.
     * @param parameters The values for filling in the query parameters.
     * @return The generated key of the insert statement, or -1, of no key was generated
     * @throw IllegalArgumentException Thrown if there is no data source with the given name.
     * @throw InvalidQueryException Thrown the given query string cannot be parsed, or doesn't match
     *        the given parameters.
     */
    long insert(String dataSourceName, String query, Object... parameters)
            throws IllegalArgumentException;
}

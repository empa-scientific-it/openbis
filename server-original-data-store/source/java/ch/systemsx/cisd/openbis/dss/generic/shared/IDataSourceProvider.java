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

package ch.systemsx.cisd.openbis.dss.generic.shared;

import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import ch.systemsx.cisd.openbis.generic.shared.dto.DataSourceDefinition;

/**
 * A provider for data sources.
 * 
 * @author Kaloyan Enimanev
 */
public interface IDataSourceProvider
{

    /**
     * Returns data source configured with given name or throws {@link IllegalArgumentException} if not configured.
     */
    public DataSource getDataSource(String name);

    /**
     * Extracts the data source name from the specified properties and returns the requested data source by calling {@link #getDataSource(String)}.
     */
    public DataSource getDataSource(Properties properties);

    /**
     * Returns all data source definitions. Note, that not all data sources have a definition.
     */
    public List<DataSourceDefinition> getAllDataSourceDefinitions();

}

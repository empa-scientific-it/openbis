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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import javax.sql.DataSource;

/**
 * Bean for a {@link DataSource} together with its {@link DataSourceDefinition}.
 *
 * @author Franz-Josef Elmer
 */
public class DataSourceWithDefinition
{
    private final DataSource dataSource;

    private final DataSourceDefinition definition;

    public DataSourceWithDefinition(DataSource dataSource, DataSourceDefinition definitionOrNull)
    {
        if (dataSource == null)
        {
            throw new IllegalArgumentException("Unspecified data source.");
        }
        this.dataSource = dataSource;
        this.definition = definitionOrNull;
    }

    public DataSource getDataSource()
    {
        return dataSource;
    }

    public DataSourceDefinition getDefinitionOrNull()
    {
        return definition;
    }

}

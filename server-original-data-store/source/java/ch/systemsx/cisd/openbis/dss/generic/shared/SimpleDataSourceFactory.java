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

import java.util.Properties;

import javax.sql.DataSource;

import ch.systemsx.cisd.dbmigration.SimpleDatabaseConfigurationContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSourceWithDefinition;
import ch.systemsx.cisd.openbis.generic.shared.util.IDataSourceFactory;

/**
 * Creates a commons-dbcp {@link DataSource} using its standard properties.
 * 
 * @author Kaloyan Enimanev
 */
public class SimpleDataSourceFactory implements IDataSourceFactory
{

    @Override
    public DataSourceWithDefinition create(Properties dbProps)
    {
        SimpleDatabaseConfigurationContext context =
                new SimpleDatabaseConfigurationContext(dbProps);

        return new DataSourceWithDefinition(context.getDataSource(), null);
    }
}

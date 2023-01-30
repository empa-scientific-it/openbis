/*
 * Copyright 2018 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.query.server;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.plugin.query.shared.DatabaseDefinition;
import ch.systemsx.cisd.openbis.plugin.query.shared.IQueryDatabaseDefinitionProvider;
import ch.systemsx.cisd.openbis.plugin.query.shared.IQueryDatabaseDefinitionProviderAutoInitialized;

/**
 * @author pkupczyk
 */
@Component
public class QueryDatabaseDefinitionProviderAutoInitialized implements IQueryDatabaseDefinitionProviderAutoInitialized
{

    @Autowired
    private IQueryDatabaseDefinitionProvider databaseProvider;

    @Override
    public DatabaseDefinition getDefinition(String dbKey) throws UserFailureException
    {
        databaseProvider.initDatabaseDefinitions();
        return databaseProvider.getDefinition(dbKey);
    }

    @Override
    public Collection<DatabaseDefinition> getAllDefinitions() throws UserFailureException
    {
        databaseProvider.initDatabaseDefinitions();
        return databaseProvider.getAllDefinitions();
    }

}

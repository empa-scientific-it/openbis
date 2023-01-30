/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.query;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.IQueryDatabaseId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.QueryDatabaseName;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnsupportedObjectIdException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.openbis.plugin.query.shared.DatabaseDefinition;
import ch.systemsx.cisd.openbis.plugin.query.shared.IQueryDatabaseDefinitionProviderAutoInitialized;

/**
 * @author pkupczyk
 */
@Component
public class MapQueryDatabaseByIdExecutor implements IMapQueryDatabaseByIdExecutor
{

    @Autowired
    private IQueryDatabaseAuthorizationExecutor authorizationExecutor;

    @Autowired
    private IQueryDatabaseDefinitionProviderAutoInitialized databaseProvider;

    @Override
    public Map<IQueryDatabaseId, DatabaseDefinition> map(IOperationContext context, Collection<? extends IQueryDatabaseId> ids)
    {
        authorizationExecutor.canGet(context);

        Map<IQueryDatabaseId, DatabaseDefinition> result = new LinkedHashMap<IQueryDatabaseId, DatabaseDefinition>();

        for (IQueryDatabaseId id : ids)
        {
            if (id instanceof QueryDatabaseName)
            {
                String name = ((QueryDatabaseName) id).getName();
                DatabaseDefinition definition = databaseProvider.getDefinition(name);

                if (definition != null)
                {
                    result.put(id, definition);
                }
            } else
            {
                throw new UnsupportedObjectIdException(id);
            }
        }

        return result;
    }

    @Override
    public Map<IQueryDatabaseId, DatabaseDefinition> map(IOperationContext context, Collection<? extends IQueryDatabaseId> ids, boolean checkAccess)
    {
        return map(context, ids);
    }

}

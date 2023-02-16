/*
 * Copyright ETH 2018 - 2023 Zürich, Scientific IT Services
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

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.FieldUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.IQueryDatabaseId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.QueryDatabaseName;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.update.QueryUpdate;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractUpdateEntityToOneRelationExecutor;
import ch.systemsx.cisd.openbis.generic.shared.dto.QueryPE;
import ch.systemsx.cisd.openbis.plugin.query.shared.DatabaseDefinition;
import ch.systemsx.cisd.openbis.plugin.query.shared.IQueryDatabaseDefinitionProviderAutoInitialized;

/**
 * @author pkupczyk
 */
@Component
public class UpdateQueryDatabaseExecutor extends AbstractUpdateEntityToOneRelationExecutor<QueryUpdate, QueryPE, IQueryDatabaseId, DatabaseDefinition>
        implements IUpdateQueryDatabaseExecutor
{

    @Autowired
    private IMapQueryDatabaseByIdExecutor mapQueryDatabaseByIdExecutor;

    @Autowired
    private IQueryDatabaseDefinitionProviderAutoInitialized databaseDefinitionProvider;

    @Override
    protected String getRelationName()
    {
        return "query-database";
    }

    @Override
    protected IQueryDatabaseId getRelatedId(DatabaseDefinition related)
    {
        return new QueryDatabaseName(related.getKey());
    }

    @Override
    protected DatabaseDefinition getCurrentlyRelated(QueryPE entity)
    {
        return databaseDefinitionProvider.getDefinition(entity.getQueryDatabaseKey());
    }

    @Override
    protected FieldUpdateValue<IQueryDatabaseId> getRelatedUpdate(QueryUpdate update)
    {
        return update.getDatabaseId();
    }

    @Override
    protected Map<IQueryDatabaseId, DatabaseDefinition> map(IOperationContext context, List<IQueryDatabaseId> relatedIds)
    {
        return mapQueryDatabaseByIdExecutor.map(context, relatedIds);
    }

    @Override
    protected void check(IOperationContext context, QueryPE entity, IQueryDatabaseId relatedId, DatabaseDefinition related)
    {
        // nothing to do
    }

    @Override
    protected void update(IOperationContext context, QueryPE entity, DatabaseDefinition related)
    {
        entity.setQueryDatabaseKey(related.getKey());
    }
}

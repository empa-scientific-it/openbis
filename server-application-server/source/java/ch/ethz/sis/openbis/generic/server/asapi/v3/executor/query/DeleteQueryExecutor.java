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
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.delete.QueryDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.IQueryId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractDeleteEntityExecutor;
import ch.systemsx.cisd.openbis.generic.shared.dto.QueryPE;

/**
 * @author pkupczyk
 */
@Component
public class DeleteQueryExecutor extends AbstractDeleteEntityExecutor<Void, IQueryId, QueryPE, QueryDeletionOptions> implements IDeleteQueryExecutor
{

    @Autowired
    private IMapQueryByIdExecutor mapQueryByIdExecutor;

    @Autowired
    private IQueryAuthorizationExecutor authorizationExecutor;

    @Override
    protected Map<IQueryId, QueryPE> map(IOperationContext context, List<? extends IQueryId> entityIds, QueryDeletionOptions deletionOptions)
    {
        return mapQueryByIdExecutor.map(context, entityIds);
    }

    @Override
    protected void checkAccess(IOperationContext context, IQueryId entityId, QueryPE entity)
    {
        authorizationExecutor.canDelete(context, entityId, entity);
    }

    @Override
    protected void updateModificationDateAndModifier(IOperationContext context, QueryPE entity)
    {
        // nothing to do
    }

    @Override
    protected Void delete(IOperationContext context, Collection<QueryPE> queries, QueryDeletionOptions deletionOptions)
    {
        for (QueryPE query : queries)
        {
            daoFactory.getQueryDAO().delete(query);
        }
        return null;
    }

}

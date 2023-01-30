/*
 * Copyright 2014 ETH Zuerich, CISD
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

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.QueryType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.IQueryId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.QueryTechId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.update.QueryUpdate;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractUpdateEntityExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.QueryPE;
import ch.systemsx.cisd.openbis.plugin.query.server.DAO;

/**
 * @author pkupczyk
 */
@Component
public class UpdateQueryExecutor extends AbstractUpdateEntityExecutor<QueryUpdate, QueryPE, IQueryId, QueryTechId> implements
        IUpdateQueryExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IMapQueryByIdExecutor mapQueryByIdExecutor;

    @Autowired
    private IUpdateQueryDatabaseExecutor updateQueryDatabaseExecutor;

    @Autowired
    private IQueryAuthorizationExecutor authorizationExecutor;

    @Override
    protected IQueryId getId(QueryUpdate update)
    {
        return update.getQueryId();
    }

    @Override
    protected QueryTechId getPermId(QueryPE entity)
    {
        return new QueryTechId(entity.getId());
    }

    @Override
    protected void checkData(IOperationContext context, QueryUpdate update)
    {
        if (update.getQueryId() == null)
        {
            throw new UserFailureException("Query id cannot be null.");
        }
        if (update.getName() != null && update.getName().isModified() && StringUtils.isEmpty(update.getName().getValue()))
        {
            throw new UserFailureException("Name cannot be empty.");
        }
        if (update.getDatabaseId() != null && update.getDatabaseId().isModified() && update.getDatabaseId().getValue() == null)
        {
            throw new UserFailureException("Database id cannot be null.");
        }
        if (update.getSql() != null && update.getSql().isModified())
        {
            if (StringUtils.isEmpty(update.getSql().getValue()))
            {
                throw new UserFailureException("Sql cannot be empty.");
            }

            DAO.checkQuery(update.getSql().getValue());
        }
        if (update.getQueryType() != null && update.getQueryType().isModified() && update.getQueryType().getValue() == null)
        {
            throw new UserFailureException("Query type cannot be null.");
        }
    }

    @Override
    protected void checkAccess(IOperationContext context, IQueryId id, QueryPE entity)
    {
        authorizationExecutor.canUpdate(context, id, entity);
    }

    @Override
    protected void updateBatch(IOperationContext context, MapBatch<QueryUpdate, QueryPE> batch)
    {
        updateQueryDatabaseExecutor.update(context, batch);

        for (Map.Entry<QueryUpdate, QueryPE> entry : batch.getObjects().entrySet())
        {
            QueryUpdate update = entry.getKey();
            QueryPE query = entry.getValue();

            query.setName(getNewValue(update.getName(), query.getName()));
            query.setDescription(getNewValue(update.getDescription(), query.getDescription()));
            query.setEntityTypeCodePattern(getNewValue(update.getEntityTypeCodePattern(), query.getEntityTypeCodePattern()));
            query.setExpression(getNewValue(update.getSql(), query.getExpression()));
            query.setPublic(getNewValue(update.isPublic(), query.isPublic()));

            if (update.getQueryType() != null && update.getQueryType().isModified())
            {
                query.setQueryType(ch.systemsx.cisd.openbis.generic.shared.basic.dto.QueryType.valueOf(update.getQueryType().getValue().name()));
            }

            if (ch.systemsx.cisd.openbis.generic.shared.basic.dto.QueryType.GENERIC.equals(query.getQueryType())
                    && false == StringUtils.isEmpty(query.getEntityTypeCodePattern()))
            {
                throw new UserFailureException(
                        "Entity type code pattern cannot be specified for a query with type " + QueryType.GENERIC.name() + ".");
            }
        }
    }

    @Override
    protected void updateAll(IOperationContext context, MapBatch<QueryUpdate, QueryPE> batch)
    {
        // nothing to do
    }

    @Override
    protected Map<IQueryId, QueryPE> map(IOperationContext context, Collection<IQueryId> ids)
    {
        return mapQueryByIdExecutor.map(context, ids);
    }

    @Override
    protected List<QueryPE> list(IOperationContext context, Collection<Long> ids)
    {
        return daoFactory.getQueryDAO().listByIDs(ids);
    }

    @Override
    protected void save(IOperationContext context, List<QueryPE> entities, boolean clearCache)
    {
        for (QueryPE entity : entities)
        {
            daoFactory.getQueryDAO().validateAndSaveUpdatedEntity(entity);
        }
    }

    @Override
    protected void handleException(DataAccessException e)
    {
        DataAccessExceptionTranslator.throwException(e, "query", null);
    }

}

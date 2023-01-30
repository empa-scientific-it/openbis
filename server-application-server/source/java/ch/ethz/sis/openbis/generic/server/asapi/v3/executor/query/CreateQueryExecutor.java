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
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.QueryType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.create.QueryCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.QueryName;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.QueryTechId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractCreateEntityExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatchProcessor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.progress.CreateProgress;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.QueryPE;
import ch.systemsx.cisd.openbis.plugin.query.server.DAO;

/**
 * @author pkupczyk
 */
@Component
public class CreateQueryExecutor extends AbstractCreateEntityExecutor<QueryCreation, QueryPE, QueryTechId> implements
        ICreateQueryExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private ISetQueryDatabaseExecutor setQueryDatabaseExecutor;

    @Autowired
    private IQueryAuthorizationExecutor authorizationExecutor;

    @Override
    protected List<QueryPE> createEntities(final IOperationContext context, CollectionBatch<QueryCreation> batch)
    {
        final List<QueryPE> queries = new LinkedList<QueryPE>();

        new CollectionBatchProcessor<QueryCreation>(context, batch)
            {
                @Override
                public void process(QueryCreation object)
                {
                    QueryPE query = new QueryPE();
                    query.setName(object.getName());
                    query.setDescription(object.getDescription());
                    if (object.getQueryType() != null)
                    {
                        query.setQueryType(ch.systemsx.cisd.openbis.generic.shared.basic.dto.QueryType.valueOf(object.getQueryType().name()));
                    }
                    query.setEntityTypeCodePattern(object.getEntityTypeCodePattern());
                    query.setExpression(object.getSql());
                    query.setPublic(object.isPublic());
                    query.setRegistrator(context.getSession().tryGetPerson());
                    queries.add(query);
                }

                @Override
                public IProgress createProgress(QueryCreation object, int objectIndex, int totalObjectCount)
                {
                    return new CreateProgress(object, objectIndex, totalObjectCount);
                }
            };

        return queries;
    }

    @Override
    protected QueryTechId createPermId(IOperationContext context, QueryPE entity)
    {
        return new QueryTechId(entity.getId());
    }

    @Override
    protected void checkData(IOperationContext context, QueryCreation creation)
    {
        if (StringUtils.isEmpty(creation.getName()))
        {
            throw new UserFailureException("Name cannot be empty.");
        }
        if (creation.getDatabaseId() == null)
        {
            throw new UserFailureException("Database id cannot be null.");
        }
        if (StringUtils.isEmpty(creation.getSql()))
        {
            throw new UserFailureException("Sql cannot be empty.");
        }

        DAO.checkQuery(creation.getSql());

        if (creation.getQueryType() == null)
        {
            throw new UserFailureException("Query type cannot be null.");
        }
        if (QueryType.GENERIC.equals(creation.getQueryType()) && false == StringUtils.isEmpty(creation.getEntityTypeCodePattern()))
        {
            throw new UserFailureException("Entity type code pattern cannot be specified for a query with type " + QueryType.GENERIC.name() + ".");
        }
    }

    @Override
    protected void checkAccess(IOperationContext context)
    {

    }

    @Override
    protected void checkAccess(IOperationContext context, QueryPE entity)
    {
        authorizationExecutor.canCreate(context, entity);
    }

    @Override
    protected void updateBatch(IOperationContext context, MapBatch<QueryCreation, QueryPE> batch)
    {
        setQueryDatabaseExecutor.set(context, batch);
    }

    @Override
    protected void updateAll(IOperationContext context, MapBatch<QueryCreation, QueryPE> batch)
    {
        // nothing to do
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
            daoFactory.getQueryDAO().createQuery(entity);
        }
    }

    @Override
    protected void handleException(DataAccessException e)
    {
        DataAccessExceptionTranslator.throwException(e, "query", null);
    }

    @Override
    protected IObjectId getId(QueryPE entity)
    {
        return new QueryName(entity.getName());
    }

}

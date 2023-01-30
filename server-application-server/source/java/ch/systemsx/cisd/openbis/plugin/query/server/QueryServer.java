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

package ch.systemsx.cisd.openbis.plugin.query.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.server.AbstractServer;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.ReturnValueFilter;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.ExpressionValidator;
import ch.systemsx.cisd.openbis.generic.server.business.IPropertiesBatchManager;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IQueryDAO;
import ch.systemsx.cisd.openbis.generic.server.plugin.IDataSetTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.server.plugin.ISampleTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.shared.IOpenBisSessionManager;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.QueryType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.QueryPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.query.server.authorization.QueryAccessController;
import ch.systemsx.cisd.openbis.plugin.query.shared.DatabaseDefinition;
import ch.systemsx.cisd.openbis.plugin.query.shared.IQueryDatabaseDefinitionProvider;
import ch.systemsx.cisd.openbis.plugin.query.shared.IQueryServer;
import ch.systemsx.cisd.openbis.plugin.query.shared.ResourceNames;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.IQueryUpdates;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.NewQuery;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryDatabase;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryExpression;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryParameterBindings;
import ch.systemsx.cisd.openbis.plugin.query.shared.translator.QueryTranslator;

/**
 * @author Franz-Josef Elmer
 */
@Component(ResourceNames.QUERY_PLUGIN_SERVER)
public class QueryServer extends AbstractServer<IQueryServer> implements IQueryServer
{
    @Resource(name = ResourceNames.QUERY_DATABASE_DEFINITION_PROVIDER)
    private IQueryDatabaseDefinitionProvider dbDefinitionProvider;

    /**
     * map from dbKey to IDAO
     * 
     * @deprecated don't use it directly - use getter instead
     */
    @Deprecated
    private final Map<String, IDAO> daos = new HashMap<String, IDAO>();

    public QueryServer()
    {
    }

    @Private
    QueryServer(final IOpenBisSessionManager sessionManager, final IDAOFactory daoFactory,
            IPropertiesBatchManager propertiesBatchManager,
            final ISampleTypeSlaveServerPlugin sampleTypeSlaveServerPlugin,
            final IDataSetTypeSlaveServerPlugin dataSetTypeSlaveServerPlugin,
            IQueryDatabaseDefinitionProvider dbDefinitionProvider)
    {
        super(sessionManager, daoFactory, propertiesBatchManager, sampleTypeSlaveServerPlugin,
                dataSetTypeSlaveServerPlugin);
        this.dbDefinitionProvider = dbDefinitionProvider;
    }

    @Override
    public IQueryServer createLogger(IInvocationLoggerContext context)
    {
        return new QueryServerLogger(getSessionManager(), context);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public int initDatabases(String sessionToken)
    {
        checkSession(sessionToken);
        dbDefinitionProvider.initDatabaseDefinitions();
        return dbDefinitionProvider.getAllDefinitions().size();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<QueryDatabase> listQueryDatabases(String sessionToken)
    {
        checkSession(sessionToken);

        final List<QueryDatabase> results = new ArrayList<QueryDatabase>();
        for (DatabaseDefinition definition : dbDefinitionProvider.getAllDefinitions())
        {
            results.add(new QueryDatabase(definition.getKey(), definition.getLabel()));
        }
        Collections.sort(results);
        return results;
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    @ReturnValueFilter(validatorClass = ExpressionValidator.class)
    public List<QueryExpression> listQueries(String sessionToken, QueryType queryType,
            BasicEntityType entityTypeOrNull)
    {
        checkSession(sessionToken);

        try
        {
            List<QueryPE> queries = getDAOFactory().getQueryDAO().listQueries(queryType);
            List<QueryPE> filtered = new ArrayList<QueryPE>();
            // filter queries by entity type if one was specified
            if (entityTypeOrNull != null)
            {
                final String entityTypeCode = entityTypeOrNull.getCode();
                for (QueryPE query : queries)
                {
                    final String queryEntityTypeCodePatternOrNull =
                            query.getEntityTypeCodePattern();
                    if (StringUtils.isEmpty(queryEntityTypeCodePatternOrNull)
                            || entityTypeCode.matches(queryEntityTypeCodePatternOrNull))
                    {
                        filtered.add(query);
                    }
                }
            } else
            {
                filtered.addAll(queries);
            }
            return QueryTranslator.translate(filtered, dbDefinitionProvider);
        } catch (DataAccessException ex)
        {
            throw new UserFailureException(ex.getMostSpecificCause().getMessage(), ex);
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public void registerQuery(String sessionToken, NewQuery expression)
    {
        Session session = getSession(sessionToken);
        QueryAccessController.checkWriteAccess(session, expression.getQueryDatabase().getKey(),
                "create");

        QueryPE query = new QueryPE();
        query.setName(expression.getName());
        query.setDescription(expression.getDescription());
        query.setExpression(expression.getExpression());
        query.setPublic(expression.isPublic());
        query.setRegistrator(session.tryGetPerson());
        query.setQueryType(expression.getQueryType());
        query.setEntityTypeCodePattern(expression.getEntityTypeCode());
        query.setQueryDatabaseKey(expression.getQueryDatabase().getKey());
        try
        {
            getDAOFactory().getQueryDAO().createQuery(query);
        } catch (DataAccessException ex)
        {
            DataAccessExceptionTranslator.throwException(ex,
                    "Query definition '" + expression.getName() + "'", null);
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public void deleteQueries(String sessionToken, List<TechId> queryIds)
    {
        Session session = getSession(sessionToken);

        IQueryDAO queryDAO = getDAOFactory().getQueryDAO();
        try
        {
            for (TechId techId : queryIds)
            {
                QueryPE query = queryDAO.getByTechId(techId);
                QueryAccessController.checkWriteAccess(session, query.getQueryDatabaseKey(),
                        "delete");
                queryDAO.delete(query);
            }
        } catch (DataAccessException ex)
        {
            DataAccessExceptionTranslator.throwException(ex, "Query definition", null);
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public void updateQuery(String sessionToken, IQueryUpdates updates)
    {
        Session session = getSession(sessionToken);
        QueryAccessController.checkWriteAccess(session, updates.getQueryDatabase().getKey(),
                "update");

        try
        {
            IQueryDAO queryDAO = getDAOFactory().getQueryDAO();
            QueryPE query = queryDAO.getByTechId(TechId.create(updates));
            if (query.getModificationDate().equals(updates.getModificationDate()) == false)
            {
                throw new UserFailureException("Unfortunately this query definition "
                        + "has been modified in the meantime.\n\n"
                        + "Please, refresh the data and try it again.");
            }

            query.setName(updates.getName());
            query.setDescription(updates.getDescription());
            query.setExpression(updates.getExpression());
            query.setPublic(updates.isPublic());
            query.setQueryType(updates.getQueryType());
            query.setEntityTypeCodePattern(updates.getEntityTypeCode());
            query.setQueryDatabaseKey(updates.getQueryDatabase().getKey());

            queryDAO.validateAndSaveUpdatedEntity(query);
        } catch (DataAccessException ex)
        {
            DataAccessExceptionTranslator.throwException(ex,
                    "Query definition '" + updates.getName() + "'", null);
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public TableModel queryDatabase(String sessionToken, QueryDatabase database, String sqlQuery,
            QueryParameterBindings bindings, boolean onlyPerform)
    {
        Session session = getSession(sessionToken);
        try
        {
            String dbKey = database.getKey();
            if (onlyPerform)
            {
                QueryAccessController.checkReadAccess(session, dbKey);
            } else
            {
                QueryAccessController.checkWriteAccess(session, dbKey, "create and perform");
            }
            return QueryAccessController.filterResults(session.tryGetPerson(), dbKey,
                    getDAOFactory(), queryDatabaseWithKey(dbKey, sqlQuery, bindings));
        } catch (DataAccessException ex)
        {
            throw new UserFailureException(ex.getMostSpecificCause().getMessage(), ex);
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public TableModel queryDatabase(String sessionToken, TechId queryId,
            QueryParameterBindings bindings)
    {
        Session session = getSession(sessionToken);
        try
        {
            IQueryDAO queryDAO = getDAOFactory().getQueryDAO();
            QueryPE query = queryDAO.getByTechId(queryId);
            String dbKey = query.getQueryDatabaseKey();
            String expression = StringEscapeUtils.unescapeHtml4(query.getExpression());
            QueryAccessController.checkReadAccess(session, dbKey);
            return QueryAccessController.filterResults(session.tryGetPerson(), dbKey,
                    getDAOFactory(), queryDatabaseWithKey(dbKey, expression, bindings));
        } catch (DataAccessException ex)
        {
            throw new UserFailureException(ex.getMostSpecificCause().getMessage(), ex);
        }
    }

    private TableModel queryDatabaseWithKey(String dbKey, String sqlQuery,
            QueryParameterBindings bindings)
    {
        return getDAO(dbKey).query(sqlQuery, bindings);
    }

    private IDAO getDAO(String dbKey)
    {
        IDAO result = daos.get(dbKey);
        if (result == null)
        {
            DatabaseDefinition definition = dbDefinitionProvider.getDefinition(dbKey);
            if (definition == null)
            {
                throw new UnsupportedOperationException("Undefined query database '" + dbKey + "'");
            }
            result = new DAO(definition.getConfigurationContext().getDataSource());
        }
        return result;
    }

}

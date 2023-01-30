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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.query;

import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.TableModel;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.IQueryDatabaseId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.IQueryId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.ObjectNotFoundException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.TableModelTranslator;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.QueryPE;
import ch.systemsx.cisd.openbis.plugin.query.server.DAO;
import ch.systemsx.cisd.openbis.plugin.query.server.authorization.QueryAccessController;
import ch.systemsx.cisd.openbis.plugin.query.shared.DatabaseDefinition;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryParameterBindings;

/**
 * @author pkupczyk
 */
public class AbstractExecuteExecutor
{

    @Autowired
    protected IQueryAuthorizationExecutor authorizationExecutor;

    @Autowired
    protected IMapQueryByIdExecutor mapQueryByIdExecutor;

    @Autowired
    protected IMapQueryDatabaseByIdExecutor mapQueryDatabaseByIdExecutor;

    @Autowired
    protected IDAOFactory daoFactory;

    protected QueryPE getQuery(IOperationContext context, IQueryId queryId)
    {
        Map<IQueryId, QueryPE> queries = mapQueryByIdExecutor.map(context, Arrays.asList(queryId));
        QueryPE query = queries.get(queryId);

        if (query == null)
        {
            throw new ObjectNotFoundException(queryId);
        }

        return query;
    }

    protected DatabaseDefinition getDatabase(IOperationContext context, IQueryDatabaseId databaseId)
    {
        Map<IQueryDatabaseId, DatabaseDefinition> databases = mapQueryDatabaseByIdExecutor.map(context, Arrays.asList(databaseId));
        DatabaseDefinition database = databases.get(databaseId);

        if (database == null)
        {
            throw new ObjectNotFoundException(databaseId);
        }

        return database;
    }

    protected TableModel doExecute(IOperationContext context, String sql, DatabaseDefinition database, Map<String, String> parameters)
    {
        try
        {
            DAO databaseDAO = new DAO(database.getConfigurationContext().getDataSource());

            QueryParameterBindings bindings = new QueryParameterBindings();
            if (parameters != null)
            {
                bindings.setBindings(parameters);
            }

            ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel tableModel = databaseDAO.query(sql, bindings);
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel filteredTableModel =
                    QueryAccessController.filterResults(context.getSession().tryGetPerson(), database.getKey(), daoFactory, tableModel);

            return new TableModelTranslator().translate(filteredTableModel);
        } catch (DataAccessException ex)
        {
            throw new UserFailureException(ex.getMostSpecificCause().getMessage(), ex);
        }
    }

}

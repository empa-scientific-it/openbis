/*
 * Copyright 2016 ETH Zuerich, CISD
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.IQueryId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.QueryDatabaseName;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.ObjectNotFoundException;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnauthorizedObjectAccessException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.roleassignment.RoleAssignmentUtils;
import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.Capability;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.shared.DatabaseCreateOrDeleteModification;
import ch.systemsx.cisd.openbis.generic.shared.DatabaseUpdateModification;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.dto.QueryPE;
import ch.systemsx.cisd.openbis.plugin.query.server.authorization.QueryAccessController;
import ch.systemsx.cisd.openbis.plugin.query.shared.DatabaseDefinition;
import ch.systemsx.cisd.openbis.plugin.query.shared.IQueryDatabaseDefinitionProviderAutoInitialized;

/**
 * @author pkupczyk
 */
@Component
public class QueryAuthorizationExecutor implements IQueryAuthorizationExecutor
{

    @Autowired
    private IQueryDatabaseDefinitionProviderAutoInitialized databaseProvider;

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("CREATE_QUERY")
    @DatabaseCreateOrDeleteModification(value = ObjectKind.QUERY)
    public void canCreate(IOperationContext context, QueryPE query)
    {
        canWrite(context, null, query, "create");
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("UPDATE_QUERY")
    @DatabaseUpdateModification(value = ObjectKind.QUERY)
    public void canUpdate(IOperationContext context, IQueryId id, QueryPE query)
    {
        canWrite(context, id, query, "update");
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("DELETE_QUERY")
    @DatabaseCreateOrDeleteModification(value = ObjectKind.QUERY)
    public void canDelete(IOperationContext context, IQueryId id, QueryPE query)
    {
        canWrite(context, id, query, "delete");
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public void canExecute(IOperationContext context, IQueryId id, QueryPE query)
    {
        checkDatabaseExists(query.getQueryDatabaseKey());

        if (query.isPublic() || context.getSession().tryGetPerson().equals(query.getRegistrator())
                || RoleAssignmentUtils.isInstanceAdmin(context.getSession().tryGetPerson()))
        {
            try
            {
                QueryAccessController.checkReadAccess(context.getSession(), query.getQueryDatabaseKey());
            } catch (AuthorizationFailureException e)
            {
                throw new UnauthorizedObjectAccessException(new QueryDatabaseName(query.getQueryDatabaseKey()));
            }
        } else
        {
            throw new UnauthorizedObjectAccessException(id);
        }
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("EXECUTE_QUERY")
    public void canExecute(IOperationContext context, String sql, DatabaseDefinition database)
    {
        checkDatabaseExists(database.getKey());

        try
        {
            QueryAccessController.checkWriteAccess(context.getSession(), database.getKey(), "create and perform");
        } catch (AuthorizationFailureException e)
        {
            throw new UnauthorizedObjectAccessException(new QueryDatabaseName(database.getKey()));
        }
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("GET_QUERY")
    public void canGet(IOperationContext context)
    {
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("SEARCH_QUERY")
    public void canSearch(IOperationContext context)
    {
    }

    private void canWrite(IOperationContext context, IQueryId id, QueryPE query, String operation)
    {
        checkDatabaseExists(query.getQueryDatabaseKey());

        if (query.isPublic() || context.getSession().tryGetPerson().equals(query.getRegistrator())
                || RoleAssignmentUtils.isInstanceAdmin(context.getSession().tryGetPerson()))
        {
            try
            {
                QueryAccessController.checkWriteAccess(context.getSession(), query.getQueryDatabaseKey(), operation);
            } catch (AuthorizationFailureException e)
            {
                throw new UnauthorizedObjectAccessException(new QueryDatabaseName(query.getQueryDatabaseKey()));
            }
        } else
        {
            throw new UnauthorizedObjectAccessException(id);
        }
    }

    private void checkDatabaseExists(String databaseKey)
    {
        DatabaseDefinition database = databaseProvider.getDefinition(databaseKey);

        if (database == null)
        {
            throw new ObjectNotFoundException(new QueryDatabaseName(databaseKey));
        }
    }

}

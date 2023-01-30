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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.id.IOperationExecutionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.id.OperationExecutionPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnauthorizedObjectAccessException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationServiceUtils;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.Capability;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.shared.DatabaseCreateOrDeleteModification;
import ch.systemsx.cisd.openbis.generic.shared.DatabaseUpdateModification;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.OperationExecutionPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * @author pkupczyk
 */
@Component
public class OperationExecutionAuthorizationExecutor implements IOperationExecutionAuthorizationExecutor
{

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("GET_OPERATION_EXECUTION")
    public void canGet(IOperationContext context)
    {
    }

    @Override
    public boolean canGet(IOperationContext context, OperationExecutionPE execution)
    {
        PersonPE person = context.getSession().tryGetPerson();

        // users can see their own executions
        if (person.getId().equals(execution.getOwner().getId()))
        {
            return true;
        }

        // instance observer users and stronger can see all the executions
        AuthorizationServiceUtils authorization = new AuthorizationServiceUtils(null, context.getSession().tryGetPerson());
        return authorization.doesUserHaveRole(RoleCode.OBSERVER.toString(), null);
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("UPDATE_OPERATION_EXECUTION")
    @DatabaseUpdateModification(value = ObjectKind.OPERATION_EXECUTION)
    public void canUpdate(IOperationContext context, IOperationExecutionId id, OperationExecutionPE execution)
    {
        if (false == canGet(context, execution))
        {
            throw new UnauthorizedObjectAccessException(new OperationExecutionPermId(execution.getCode()));
        }
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("DELETE_OPERATION_EXECUTION")
    @DatabaseCreateOrDeleteModification(value = { ObjectKind.OPERATION_EXECUTION, ObjectKind.DELETION })
    public void canDelete(IOperationContext context, IOperationExecutionId id, OperationExecutionPE execution)
    {
        if (false == canGet(context, execution))
        {
            throw new UnauthorizedObjectAccessException(new OperationExecutionPermId(execution.getCode()));
        }
    }

}

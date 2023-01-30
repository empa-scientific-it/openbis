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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.space;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnauthorizedObjectAccessException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.Capability;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SpacePEPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SimpleSpaceValidator;
import ch.systemsx.cisd.openbis.generic.shared.DatabaseCreateOrDeleteModification;
import ch.systemsx.cisd.openbis.generic.shared.DatabaseUpdateModification;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
@Component
public class SpaceAuthorizationExecutor implements ISpaceAuthorizationExecutor
{

    @Override
    @RolesAllowed({ RoleWithHierarchy.SPACE_ADMIN, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("CREATE_SPACE")
    @DatabaseCreateOrDeleteModification(value = ObjectKind.SPACE)
    public void canCreate(IOperationContext context, SpacePE space)
    {
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.SPACE_ADMIN, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("UPDATE_SPACE")
    @DatabaseUpdateModification(value = ObjectKind.SPACE)
    public void canUpdate(IOperationContext context, ISpaceId id, @AuthorizationGuard(guardClass = SpacePEPredicate.class) SpacePE space)
    {
        if (false == new SimpleSpaceValidator().doValidation(context.getSession().tryGetPerson(), space))
        {
            throw new UnauthorizedObjectAccessException(id);
        }
    }

    @Override
    @DatabaseCreateOrDeleteModification(value = { ObjectKind.SPACE, ObjectKind.DELETION })
    @RolesAllowed({ RoleWithHierarchy.SPACE_ADMIN, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("DELETE_SPACE")
    public void canDelete(IOperationContext context, ISpaceId id, @AuthorizationGuard(guardClass = SpacePEPredicate.class) SpacePE space)
    {
        if (false == new SimpleSpaceValidator().doValidation(context.getSession().tryGetPerson(), space))
        {
            throw new UnauthorizedObjectAccessException(id);
        }
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("GET_SPACE")
    public void canGet(IOperationContext context)
    {
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("SEARCH_SPACE")
    public void canSearch(IOperationContext context)
    {
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ADMIN)
    @Capability("FREEZE_SPACE")
    public void canFreeze(IOperationContext context, @AuthorizationGuard(guardClass = SpacePEPredicate.class) SpacePE space)
    {
    }

}

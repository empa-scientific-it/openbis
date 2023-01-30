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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.deletion;

import java.util.List;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.Capability;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.V3DeletionIdPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.V3RevertDeletionPredicate;
import ch.systemsx.cisd.openbis.generic.shared.DatabaseCreateOrDeleteModification;
import ch.systemsx.cisd.openbis.generic.shared.DatabaseUpdateModification;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;

/**
 * @author pkupczyk
 */
@Component
public class DeletionAuthorizationExecutor implements IDeletionAuthorizationExecutor
{

    @Override
    @DatabaseCreateOrDeleteModification(value = { ObjectKind.DELETION, ObjectKind.EXPERIMENT, ObjectKind.SAMPLE, ObjectKind.DATA_SET })
    @RolesAllowed({ RoleWithHierarchy.PROJECT_ADMIN, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("CONFIRM_DELETION")
    public void canConfirm(IOperationContext context, @AuthorizationGuard(guardClass = V3DeletionIdPredicate.class) List<? extends IDeletionId> ids)
    {
    }

    @Override
    @DatabaseCreateOrDeleteModification(value = { ObjectKind.DELETION, ObjectKind.EXPERIMENT, ObjectKind.SAMPLE, ObjectKind.DATA_SET })
    @RolesAllowed(RoleWithHierarchy.INSTANCE_DISABLED)
    @Capability("CONFIRM_DELETION_FORCED")
    public void canConfirmForced(IOperationContext context,
            @AuthorizationGuard(guardClass = V3DeletionIdPredicate.class) List<? extends IDeletionId> ids)
    {
    }

    @Override
    @DatabaseCreateOrDeleteModification(value = ObjectKind.DELETION)
    @DatabaseUpdateModification(value = { ObjectKind.EXPERIMENT, ObjectKind.SAMPLE, ObjectKind.DATA_SET })
    @RolesAllowed({ RoleWithHierarchy.PROJECT_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("REVERT_DELETION")
    public void canRevert(IOperationContext context,
            @AuthorizationGuard(guardClass = V3RevertDeletionPredicate.class) List<? extends IDeletionId> ids)
    {
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("GET_DELETION")
    public void canGet(IOperationContext context)
    {

    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("SEARCH_DELETION")
    public void canSearch(IOperationContext context)
    {
    }

}

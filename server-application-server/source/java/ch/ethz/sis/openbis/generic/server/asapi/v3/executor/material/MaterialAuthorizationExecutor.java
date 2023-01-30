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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.material;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.IMaterialId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.Capability;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.shared.DatabaseCreateOrDeleteModification;
import ch.systemsx.cisd.openbis.generic.shared.DatabaseUpdateModification;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;

/**
 * @author pkupczyk
 */
@Component
public class MaterialAuthorizationExecutor implements IMaterialAuthorizationExecutor
{

    @Override
    @RolesAllowed({ RoleWithHierarchy.INSTANCE_ADMIN, RoleWithHierarchy.INSTANCE_ETL_SERVER })
    @Capability("CREATE_MATERIAL")
    @DatabaseCreateOrDeleteModification(value = ObjectKind.MATERIAL)
    public void canCreate(IOperationContext context, MaterialPE material)
    {
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.INSTANCE_ADMIN, RoleWithHierarchy.INSTANCE_ETL_SERVER })
    @Capability("UPDATE_MATERIAL")
    @DatabaseUpdateModification(value = ObjectKind.MATERIAL)
    public void canUpdate(IOperationContext context, IMaterialId id, MaterialPE material)
    {
    }

    @Override
    @DatabaseCreateOrDeleteModification(value = { ObjectKind.MATERIAL, ObjectKind.DELETION })
    @RolesAllowed({ RoleWithHierarchy.INSTANCE_ADMIN, RoleWithHierarchy.INSTANCE_ETL_SERVER })
    @Capability("DELETE_MATERIAL")
    public void canDelete(IOperationContext context, IMaterialId id, MaterialPE material)
    {
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("GET_MATERIAL")
    public void canGet(IOperationContext context)
    {
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("SEARCH_MATERIAL")
    public void canSearch(IOperationContext context)
    {
    }

}

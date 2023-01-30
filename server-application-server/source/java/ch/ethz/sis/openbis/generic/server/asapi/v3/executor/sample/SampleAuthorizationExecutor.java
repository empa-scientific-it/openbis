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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.sample;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.Capability;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SamplePEPredicate;
import ch.systemsx.cisd.openbis.generic.shared.DatabaseCreateOrDeleteModification;
import ch.systemsx.cisd.openbis.generic.shared.DatabaseUpdateModification;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author pkupczyk
 */
@Component
public class SampleAuthorizationExecutor implements ISampleAuthorizationExecutor
{

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("CREATE_SAMPLE")
    @DatabaseCreateOrDeleteModification(value = ObjectKind.SAMPLE)
    public void canCreate(IOperationContext context, @AuthorizationGuard(guardClass = SamplePEPredicate.class) SamplePE sample)
    {
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("UPDATE_SAMPLE")
    @DatabaseUpdateModification(value = ObjectKind.SAMPLE)
    public void canUpdate(IOperationContext context, ISampleId id,
            @AuthorizationGuard(guardClass = SamplePEPredicate.class) SamplePE sample)
    {
    }

    @Override
    @DatabaseCreateOrDeleteModification(value = { ObjectKind.SAMPLE, ObjectKind.DELETION })
    @RolesAllowed({ RoleWithHierarchy.PROJECT_POWER_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("DELETE_SAMPLE")
    public void canDelete(IOperationContext context, ISampleId id,
            @AuthorizationGuard(guardClass = SamplePEPredicate.class) SamplePE sample)
    {
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("GET_SAMPLE")
    public void canGet(IOperationContext context)
    {
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("SEARCH_SAMPLE")
    public void canSearch(IOperationContext context)
    {
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ADMIN)
    @Capability("FREEZE_SAMPLE")
    public void canFreeze(IOperationContext context, @AuthorizationGuard(guardClass = SamplePEPredicate.class) SamplePE sample)
    {
    }
}

/*
 * Copyright 2017 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.authorizationgroup;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.Capability;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Component
public class AuthorizationGroupAuthorizationExecutor implements IAuthorizationGroupAuthorizationExecutor
{

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    @Capability("CREATE_AUTHORIZATION_GROUP")
    public void canCreate(IOperationContext context)
    {
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_ADMIN)
    @Capability("GET_AUTHORIZATION_GROUP")
    public void canGet(IOperationContext context)
    {
    }

    
    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_ADMIN)
    @Capability("SEARCH_AUTHORIZATION_GROUP")
    public void canSearch(IOperationContext context)
    {
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    @Capability("UPDATE_AUTHORIZATION_GROUP")
    public void canUpdate(IOperationContext context)
    {
    }
    
    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    @Capability("DELETE_AUTHORIZATION_GROUP")
    public void canDelete(IOperationContext context)
    {
    }

}

/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.property;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.IPropertyTypeId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.Capability;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.server.business.bo.InternalPropertyTypeAuthorization;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;

/**
 * @author pkupczyk
 */
@Component
public class PropertyTypeAuthorizationExecutor implements IPropertyTypeAuthorizationExecutor
{

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("GET_PROPERTY_TYPE")
    public void canGet(IOperationContext context)
    {
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("SEARCH_PROPERTY_TYPE")
    public void canSearch(IOperationContext context)
    {
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    @Capability("CREATE_PROPERTY_TYPE")
    public void canCreate(IOperationContext context, PropertyTypePE entity)
    {
        new InternalPropertyTypeAuthorization().canCreatePropertyType(context.getSession(), entity);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    @Capability("UPDATE_PROPERTY_TYPE")
    public void canUpdate(IOperationContext context, IPropertyTypeId id, PropertyTypePE entity)
    {
        new InternalPropertyTypeAuthorization().canUpdatePropertyType(context.getSession(), entity);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    @Capability("DELETE_PROPERTY_TYPE")
    public void canDelete(IOperationContext context, IPropertyTypeId entityId, PropertyTypePE entity)
    {
        new InternalPropertyTypeAuthorization().canDeletePropertyType(context.getSession(), entity);
    }

}

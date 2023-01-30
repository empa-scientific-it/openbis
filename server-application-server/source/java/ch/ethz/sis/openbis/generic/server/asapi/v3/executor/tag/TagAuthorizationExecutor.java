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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.tag;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.ITagId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnauthorizedObjectAccessException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.tag.TagAuthorization;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.Capability;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.shared.DatabaseCreateOrDeleteModification;
import ch.systemsx.cisd.openbis.generic.shared.DatabaseUpdateModification;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;

/**
 * @author pkupczyk
 */
@Component
public class TagAuthorizationExecutor implements ITagAuthorizationExecutor
{

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("CREATE_TAG")
    @DatabaseCreateOrDeleteModification(value = ObjectKind.METAPROJECT)
    public void canCreate(IOperationContext context, MetaprojectPE tag)
    {
        if (false == new TagAuthorization(context).canAccess(tag))
        {
            throw new UnauthorizedObjectAccessException(new TagPermId(tag.getOwner().getUserId(), tag.getCode()));
        }
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("UPDATE_TAG")
    @DatabaseUpdateModification(value = ObjectKind.METAPROJECT)
    public void canUpdate(IOperationContext context, ITagId id, MetaprojectPE tag)
    {
        if (false == new TagAuthorization(context).canAccess(tag))
        {
            throw new UnauthorizedObjectAccessException(id);
        }
    }

    @Override
    @DatabaseCreateOrDeleteModification(value = { ObjectKind.METAPROJECT, ObjectKind.DELETION })
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("DELETE_TAG")
    public void canDelete(IOperationContext context, ITagId id, MetaprojectPE tag)
    {
        if (false == new TagAuthorization(context).canAccess(tag))
        {
            throw new UnauthorizedObjectAccessException(id);
        }
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("GET_TAG")
    public void canGet(IOperationContext context)
    {
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("SEARCH_TAG")
    public void canSearch(IOperationContext context)
    {
    }

}

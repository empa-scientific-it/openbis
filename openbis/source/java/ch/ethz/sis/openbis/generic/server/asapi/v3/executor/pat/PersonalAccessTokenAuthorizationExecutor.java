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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.pat;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.id.IPersonalAccessTokenId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnauthorizedObjectAccessException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.roleassignment.RoleAssignmentUtils;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonalAccessToken;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.Capability;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.shared.DatabaseCreateOrDeleteModification;
import ch.systemsx.cisd.openbis.generic.shared.DatabaseUpdateModification;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * @author pkupczyk
 */
@Component
public class PersonalAccessTokenAuthorizationExecutor implements IPersonalAccessTokenAuthorizationExecutor
{

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("CREATE_PERSONAL_ACCESS_TOKEN")
    @DatabaseCreateOrDeleteModification(value = ObjectKind.PERSONAL_ACCESS_TOKEN)
    public void canCreate(IOperationContext context, PersonalAccessToken pat)
    {
        if (!canWrite(context, null, pat))
        {
            throw new UserFailureException(
                    String.format("User '%s' is not allowed to create a personal access token for user '%s'",
                            context.getSession().tryGetPerson().getUserId(), pat.getOwnerId()));
        }
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("UPDATE_PERSONAL_ACCESS_TOKEN")
    @DatabaseUpdateModification(value = ObjectKind.PERSONAL_ACCESS_TOKEN)
    public void canUpdate(IOperationContext context, IPersonalAccessTokenId id, PersonalAccessToken pat)
    {
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("DELETE_PERSONAL_ACCESS_TOKEN")
    @DatabaseCreateOrDeleteModification(value = ObjectKind.PERSONAL_ACCESS_TOKEN)
    public void canDelete(IOperationContext context, IPersonalAccessTokenId id, PersonalAccessToken pat)
    {
        if (!canWrite(context, id, pat))
        {
            throw new UnauthorizedObjectAccessException(id);
        }
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("GET_PERSONAL_ACCESS_TOKEN")
    public void canGet(IOperationContext context)
    {
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("SEARCH_PERSONAL_ACCESS_TOKEN")
    public void canSearch(IOperationContext context)
    {
    }

    private boolean canWrite(IOperationContext context, IPersonalAccessTokenId id, PersonalAccessToken pat)
    {
        PersonPE person = context.getSession().tryGetPerson();

        return person.isSystemUser() || RoleAssignmentUtils.isInstanceAdmin(person) || RoleAssignmentUtils.isETLServer(person) || person.getUserId()
                .equals(pat.getOwnerId());
    }

}

/*
 * Copyright 2017 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.role;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.data.role.IRole;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.data.role.RoleFromRoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;

/**
 * @author pkupczyk
 */
public class RolesProviderFromPersonPE implements IRolesProvider
{

    private PersonPE person;

    public RolesProviderFromPersonPE(PersonPE person)
    {
        this.person = person;
    }

    @Override
    public Collection<IRole> getRoles(IAuthorizationDataProvider dataProvider)
    {
        if (person != null)
        {
            Collection<IRole> roles = new ArrayList<IRole>();
            Collection<RoleAssignmentPE> rolesPE = person.getAllPersonRoles();

            if (rolesPE != null)
            {
                for (RoleAssignmentPE rolePE : rolesPE)
                {
                    roles.add(new RoleFromRoleAssignmentPE(rolePE));
                }
            }

            return roles;
        } else
        {
            return Collections.emptySet();
        }
    }

}

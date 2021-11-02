/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth;

import ch.systemsx.cisd.openbis.generic.shared.authorization.AuthorizationConfig;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;

import java.util.HashSet;
import java.util.Set;

public class AuthorisationInformation
{

    /** If an instance role is found, the user can query everything. */
    private final boolean instanceRole;

    /** If a spaceId is found, the user can query that space. */
    private final Set<Long> spaceIds;

    /** If a spaceId is found, the user can query that project. */
    private final Set<Long> projectIds;

    public AuthorisationInformation(final boolean instanceRole,
            final Set<Long> spaceIds, final Set<Long> projectIds)
    {
        this.instanceRole = instanceRole;
        this.spaceIds = spaceIds;
        this.projectIds = projectIds;
    }

    public boolean isInstanceRole()
    {
        return instanceRole;
    }

    public Set<Long> getSpaceIds()
    {
        return spaceIds;
    }

    public Set<Long> getProjectIds()
    {
        return projectIds;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        AuthorisationInformation that = (AuthorisationInformation) o;

        if (!spaceIds.equals(that.spaceIds))
        {
            return false;
        }
        return projectIds.equals(that.projectIds);

    }

    @Override
    public int hashCode()
    {
        int result = spaceIds.hashCode();
        result = 31 * result + projectIds.hashCode();
        return result;
    }

    public static AuthorisationInformation getInstance(final PersonPE personPE,
                                                       final AuthorizationConfig authorizationConfig) {
        final Set<Long> spaceIds = new HashSet<>();
        final Set<Long> projectIds = new HashSet<>();
        boolean instanceRole = personPE.isSystemUser();
        final boolean projectLevelAuthorizationEnabled = authorizationConfig.isProjectLevelEnabled()
                && authorizationConfig.isProjectLevelUser(personPE.getUserId());
        for (final RoleAssignmentPE roleAssignmentPE : personPE.getAllPersonRoles())
        {
            if (roleAssignmentPE.getSpace() != null)
            {
                spaceIds.add(roleAssignmentPE.getSpace().getId());
            }
            if (roleAssignmentPE.getProject() != null && projectLevelAuthorizationEnabled)
            {
                projectIds.add(roleAssignmentPE.getProject().getId());
            }
            instanceRole = instanceRole || (roleAssignmentPE.getRoleLevel() == RoleWithHierarchy.RoleLevel.INSTANCE);
        }

        return new AuthorisationInformation(instanceRole, spaceIds, projectIds);
    }
}

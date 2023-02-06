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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.create;

import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.id.IAuthorizationGroupId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.ICreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.IObjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.IPersonId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.Role;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author Franz-Josef Elmer
 */
@JsonObject("as.dto.roleassignment.create.RoleAssignmentCreation")
public class RoleAssignmentCreation implements ICreation, IObjectCreation
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private IPersonId userId;

    @JsonProperty
    private IAuthorizationGroupId authorizationGroupId;

    @JsonProperty
    private Role role;

    @JsonProperty
    private ISpaceId spaceId;

    @JsonProperty
    private IProjectId projectId;

    public IPersonId getUserId()
    {
        return userId;
    }

    public void setUserId(IPersonId userId)
    {
        this.userId = userId;
    }

    public IAuthorizationGroupId getAuthorizationGroupId()
    {
        return authorizationGroupId;
    }

    public void setAuthorizationGroupId(IAuthorizationGroupId authorizationGroupId)
    {
        this.authorizationGroupId = authorizationGroupId;
    }

    public Role getRole()
    {
        return role;
    }

    public void setRole(Role role)
    {
        this.role = role;
    }

    public ISpaceId getSpaceId()
    {
        return spaceId;
    }

    public void setSpaceId(ISpaceId spaceId)
    {
        this.spaceId = spaceId;
    }

    public IProjectId getProjectId()
    {
        return projectId;
    }

    public void setProjectId(IProjectId projectId)
    {
        this.projectId = projectId;
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("groupId", authorizationGroupId).append("userId", userId).append("spaceId", spaceId)
                .append("projectId", projectId).append("role", role).toString();
    }

}

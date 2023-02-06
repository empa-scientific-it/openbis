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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.AuthorizationGroup;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IRegistrationDateHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IRegistratorHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ISpaceHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.fetchoptions.RoleAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.id.IRoleAssignmentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.NotFetchedException;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author Franz-Josef Elmer
 */
@JsonObject("as.dto.roleassignment.RoleAssignment")
public class RoleAssignment implements Serializable, ISpaceHolder, IRegistrationDateHolder, IRegistratorHolder
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private RoleAssignmentFetchOptions fetchOptions;

    @JsonProperty
    private IRoleAssignmentId id;

    @JsonProperty
    private Person user;

    @JsonProperty
    private AuthorizationGroup authorizationGroup;

    @JsonProperty
    private Role role;

    @JsonProperty
    private RoleLevel roleLevel;

    @JsonProperty
    private Space space;

    @JsonProperty
    private Project project;

    @JsonProperty
    private Date registrationDate;

    @JsonProperty
    private Person registrator;

    @JsonIgnore
    public RoleAssignmentFetchOptions getFetchOptions()
    {
        return fetchOptions;
    }

    public void setFetchOptions(RoleAssignmentFetchOptions fetchOptions)
    {
        this.fetchOptions = fetchOptions;
    }

    @JsonIgnore
    public IRoleAssignmentId getId()
    {
        return id;
    }

    public void setId(IRoleAssignmentId id)
    {
        this.id = id;
    }

    @JsonIgnore
    public Person getUser()
    {
        if (getFetchOptions() != null && getFetchOptions().hasUser())
        {
            return user;
        }
        throw new NotFetchedException("User has not been fetched.");
    }

    public void setUser(Person user)
    {
        this.user = user;
    }

    @JsonIgnore
    public AuthorizationGroup getAuthorizationGroup()
    {
        if (getFetchOptions() != null && getFetchOptions().hasAuthorizationGroup())
        {
            return authorizationGroup;
        }
        throw new NotFetchedException("Authorization group has not been fetched.");
    }

    public void setAuthorizationGroup(AuthorizationGroup authorizationGroup)
    {
        this.authorizationGroup = authorizationGroup;
    }

    @JsonIgnore
    public Role getRole()
    {
        return role;
    }

    public void setRole(Role role)
    {
        this.role = role;
    }

    @JsonIgnore
    public RoleLevel getRoleLevel()
    {
        return roleLevel;
    }

    public void setRoleLevel(RoleLevel roleLevel)
    {
        this.roleLevel = roleLevel;
    }

    @JsonIgnore
    @Override
    public Space getSpace()
    {
        if (getFetchOptions() != null && getFetchOptions().hasSpace())
        {
            return space;
        } else
        {
            throw new NotFetchedException("Space has not been fetched.");
        }
    }

    public void setSpace(Space space)
    {
        this.space = space;
    }

    @JsonIgnore
    public Project getProject()
    {
        if (getFetchOptions() != null && getFetchOptions().hasProject())
        {
            return project;
        } else
        {
            throw new NotFetchedException("Project has not been fetched.");
        }
    }

    public void setProject(Project project)
    {
        this.project = project;
    }

    @JsonIgnore
    @Override
    public Date getRegistrationDate()
    {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    @JsonIgnore
    @Override
    public Person getRegistrator()
    {
        if (getFetchOptions() != null && getFetchOptions().hasRegistrator())
        {
            return registrator;
        } else
        {
            throw new NotFetchedException("Registrator has not been fetched.");
        }
    }

    public void setRegistrator(Person registrator)
    {
        this.registrator = registrator;
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("id", id).toString();
    }

}

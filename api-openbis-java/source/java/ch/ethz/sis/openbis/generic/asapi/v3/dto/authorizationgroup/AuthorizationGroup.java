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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.fetchoptions.AuthorizationGroupFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.id.AuthorizationGroupPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ICodeHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IDescriptionHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IModificationDateHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPermIdHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IRegistrationDateHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IRegistratorHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.RoleAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.NotFetchedException;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author Franz-Josef Elmer
 */
@JsonObject("as.dto.authorizationgroup.AuthorizationGroup")
public class AuthorizationGroup implements Serializable, IPermIdHolder, ICodeHolder, IDescriptionHolder,
        IRegistrationDateHolder, IRegistratorHolder, IModificationDateHolder
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private AuthorizationGroupFetchOptions fetchOptions;

    @JsonProperty
    private AuthorizationGroupPermId permId;

    @JsonProperty
    private String code;

    @JsonProperty
    private String description;

    @JsonProperty
    private Date registrationDate;

    @JsonProperty
    private Person registrator;

    @JsonProperty
    private Date modificationDate;

    @JsonProperty
    private List<Person> users;

    @JsonProperty
    private List<RoleAssignment> roleAssignments;

    @JsonIgnore
    public AuthorizationGroupFetchOptions getFetchOptions()
    {
        return fetchOptions;
    }

    public void setFetchOptions(AuthorizationGroupFetchOptions fetchOptions)
    {
        this.fetchOptions = fetchOptions;
    }

    @JsonIgnore
    @Override
    public AuthorizationGroupPermId getPermId()
    {
        return permId;
    }

    public void setPermId(AuthorizationGroupPermId permId)
    {
        this.permId = permId;
    }

    @JsonIgnore
    @Override
    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    @JsonIgnore
    @Override
    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
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

    @JsonIgnore
    @Override
    public Date getModificationDate()
    {
        return modificationDate;
    }

    public void setModificationDate(Date modificationDate)
    {
        this.modificationDate = modificationDate;
    }

    @JsonIgnore
    public List<Person> getUsers()
    {
        if (getFetchOptions() != null && getFetchOptions().hasUsers())
        {
            return users;
        } else
        {
            throw new NotFetchedException("Users have not been fetched.");
        }
    }

    public void setUsers(List<Person> users)
    {
        this.users = users;
    }

    @JsonIgnore
    public List<RoleAssignment> getRoleAssignments()
    {
        if (getFetchOptions() != null && getFetchOptions().hasRoleAssignments())
        {
            return roleAssignments;
        } else
        {
            throw new NotFetchedException("Role assignments have not been fetched.");
        }
    }

    public void setRoleAssignments(List<RoleAssignment> roleAssignments)
    {
        this.roleAssignments = roleAssignments;
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("permId", permId).toString();
    }

}

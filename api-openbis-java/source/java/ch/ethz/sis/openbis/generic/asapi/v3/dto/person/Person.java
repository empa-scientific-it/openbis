/*
 * Copyright 2014 ETH Zuerich, CISD
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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.person;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPermIdHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IRegistrationDateHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IRegistratorHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ISpaceHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.PersonPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.RoleAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.webapp.WebAppSettings;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.NotFetchedException;
import ch.systemsx.cisd.base.annotation.JsonObject;

/*
 * Class automatically generated with DtoGenerator
 */
@JsonObject("as.dto.person.Person")
public class Person implements Serializable, IPermIdHolder, IRegistrationDateHolder, IRegistratorHolder, ISpaceHolder
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private PersonFetchOptions fetchOptions;

    @JsonProperty
    private PersonPermId permId;

    @JsonProperty
    private String userId;

    @JsonProperty
    private String firstName;

    @JsonProperty
    private String lastName;

    @JsonProperty
    private String email;

    @JsonProperty
    private Date registrationDate;

    @JsonProperty
    private Boolean active;

    @JsonProperty
    private Space space;

    @JsonProperty
    private Person registrator;

    @JsonProperty
    private List<RoleAssignment> roleAssignments;

    @JsonProperty
    private Map<String, WebAppSettings> webAppSettings;

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public PersonFetchOptions getFetchOptions()
    {
        return fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public void setFetchOptions(PersonFetchOptions fetchOptions)
    {
        this.fetchOptions = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public PersonPermId getPermId()
    {
        return permId;
    }

    // Method automatically generated with DtoGenerator
    public void setPermId(PersonPermId permId)
    {
        this.permId = permId;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public String getUserId()
    {
        return userId;
    }

    // Method automatically generated with DtoGenerator
    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public String getFirstName()
    {
        return firstName;
    }

    // Method automatically generated with DtoGenerator
    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public String getLastName()
    {
        return lastName;
    }

    // Method automatically generated with DtoGenerator
    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public String getEmail()
    {
        return email;
    }

    // Method automatically generated with DtoGenerator
    public void setEmail(String email)
    {
        this.email = email;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public Date getRegistrationDate()
    {
        return registrationDate;
    }

    // Method automatically generated with DtoGenerator
    public void setRegistrationDate(Date registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Boolean isActive()
    {
        return active;
    }

    // Method automatically generated with DtoGenerator
    public void setActive(Boolean active)
    {
        this.active = active;
    }

    // Method automatically generated with DtoGenerator
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

    // Method automatically generated with DtoGenerator
    public void setSpace(Space space)
    {
        this.space = space;
    }

    // Method automatically generated with DtoGenerator
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

    // Method automatically generated with DtoGenerator
    public void setRegistrator(Person registrator)
    {
        this.registrator = registrator;
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

    @JsonIgnore
    public WebAppSettings getWebAppSettings(String webAppId)
    {
        if (getFetchOptions() != null && (getFetchOptions().hasAllWebAppSettings() || getFetchOptions().hasWebAppSettings(webAppId)))
        {
            return webAppSettings != null ? webAppSettings.get(webAppId) : null;
        } else
        {
            throw new NotFetchedException("Settings for web app '" + webAppId + "' have not been fetched.");
        }
    }

    @JsonIgnore
    public Map<String, WebAppSettings> getWebAppSettings()
    {
        if (getFetchOptions() != null && (getFetchOptions().hasAllWebAppSettings()
                || (getFetchOptions().getWebAppSettings() != null && false == getFetchOptions().getWebAppSettings().isEmpty())))
        {
            return webAppSettings;
        } else
        {
            throw new NotFetchedException("Settings have not been fetched.");
        }
    }

    public void setWebAppSettings(Map<String, WebAppSettings> webAppSettings)
    {
        this.webAppSettings = webAppSettings;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public String toString()
    {
        return "Person " + userId;
    }

}

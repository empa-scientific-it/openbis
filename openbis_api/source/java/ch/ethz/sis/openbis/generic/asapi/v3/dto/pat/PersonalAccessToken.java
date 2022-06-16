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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.pat;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IModificationDateHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IModifierHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IOwnerHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPermIdHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IRegistrationDateHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IRegistratorHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.fetchoptions.PersonalAccessTokenFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.id.PersonalAccessTokenPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.NotFetchedException;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Date;

/*
 * Class automatically generated with DtoGenerator
 */
@JsonObject("as.dto.pat.PersonalAccessToken")
public class PersonalAccessToken implements Serializable, IModificationDateHolder, IModifierHolder, IOwnerHolder, IPermIdHolder, IRegistrationDateHolder, IRegistratorHolder
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private PersonalAccessTokenFetchOptions fetchOptions;

    @JsonProperty
    private PersonalAccessTokenPermId permId;

    @JsonProperty
    private String hash;

    @JsonProperty
    private String sessionName;

    @JsonProperty
    private Date validFromDate;

    @JsonProperty
    private Date validToDate;

    @JsonProperty
    private Person owner;

    @JsonProperty
    private Person registrator;

    @JsonProperty
    private Person modifier;

    @JsonProperty
    private Date registrationDate;

    @JsonProperty
    private Date modificationDate;

    @JsonProperty
    private Date accessDate;

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public PersonalAccessTokenFetchOptions getFetchOptions()
    {
        return fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public void setFetchOptions(PersonalAccessTokenFetchOptions fetchOptions)
    {
        this.fetchOptions = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public PersonalAccessTokenPermId getPermId()
    {
        return permId;
    }

    // Method automatically generated with DtoGenerator
    public void setPermId(PersonalAccessTokenPermId permId)
    {
        this.permId = permId;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public String getHash()
    {
        return hash;
    }

    // Method automatically generated with DtoGenerator
    public void setHash(String hash)
    {
        this.hash = hash;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public String getSessionName()
    {
        return sessionName;
    }

    // Method automatically generated with DtoGenerator
    public void setSessionName(String sessionName)
    {
        this.sessionName = sessionName;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Date getValidFromDate()
    {
        return validFromDate;
    }

    // Method automatically generated with DtoGenerator
    public void setValidFromDate(Date validFromDate)
    {
        this.validFromDate = validFromDate;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Date getValidToDate()
    {
        return validToDate;
    }

    // Method automatically generated with DtoGenerator
    public void setValidToDate(Date validToDate)
    {
        this.validToDate = validToDate;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public Person getOwner()
    {
        if (getFetchOptions() != null && getFetchOptions().hasOwner())
        {
            return owner;
        }
        else
        {
            throw new NotFetchedException("Owner has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setOwner(Person owner)
    {
        this.owner = owner;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public Person getRegistrator()
    {
        if (getFetchOptions() != null && getFetchOptions().hasRegistrator())
        {
            return registrator;
        }
        else
        {
            throw new NotFetchedException("Registrator has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setRegistrator(Person registrator)
    {
        this.registrator = registrator;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public Person getModifier()
    {
        if (getFetchOptions() != null && getFetchOptions().hasModifier())
        {
            return modifier;
        }
        else
        {
            throw new NotFetchedException("Modifier has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setModifier(Person modifier)
    {
        this.modifier = modifier;
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
    @Override
    public Date getModificationDate()
    {
        return modificationDate;
    }

    // Method automatically generated with DtoGenerator
    public void setModificationDate(Date modificationDate)
    {
        this.modificationDate = modificationDate;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Date getAccessDate()
    {
        return accessDate;
    }

    // Method automatically generated with DtoGenerator
    public void setAccessDate(Date accessDate)
    {
        this.accessDate = accessDate;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public String toString()
    {
        return "Personal Access Token " + permId;
    }

}

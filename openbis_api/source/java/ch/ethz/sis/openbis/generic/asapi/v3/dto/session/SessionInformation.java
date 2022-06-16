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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.session;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.fetchoptions.SessionInformationFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.NotFetchedException;
import ch.systemsx.cisd.base.annotation.JsonObject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/*
 * Class automatically generated with DtoGenerator
 */
@JsonObject("as.dto.session.SessionInformation")
public class SessionInformation implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private SessionInformationFetchOptions fetchOptions;

    @JsonProperty
    private boolean personalAccessTokenSession;

    @JsonProperty
    private String sessionName;

    @JsonProperty
    private String sessionToken;

    @JsonProperty
    private String userName;

    @JsonProperty
    private String homeGroupCode;

    @JsonProperty
    private Person person;

    @JsonProperty
    private Person creatorPerson;

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public SessionInformationFetchOptions getFetchOptions()
    {
        return fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public void setFetchOptions(SessionInformationFetchOptions fetchOptions)
    {
        this.fetchOptions = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public boolean isPersonalAccessTokenSession()
    {
        return personalAccessTokenSession;
    }

    // Method automatically generated with DtoGenerator
    public void setPersonalAccessTokenSession(boolean personalAccessTokenSession)
    {
        this.personalAccessTokenSession = personalAccessTokenSession;
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
    public String getSessionToken()
    {
        return sessionToken;
    }

    // Method automatically generated with DtoGenerator
    public void setSessionToken(String sessionToken)
    {
        this.sessionToken = sessionToken;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public String getUserName()
    {
        return userName;
    }

    // Method automatically generated with DtoGenerator
    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public String getHomeGroupCode()
    {
        return homeGroupCode;
    }

    // Method automatically generated with DtoGenerator
    public void setHomeGroupCode(String homeGroupCode)
    {
        this.homeGroupCode = homeGroupCode;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Person getPerson()
    {
        if (getFetchOptions() != null && getFetchOptions().hasPerson())
        {
            return person;
        } else
        {
            throw new NotFetchedException("Person has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setPerson(Person person)
    {
        this.person = person;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Person getCreatorPerson()
    {
        if (getFetchOptions() != null && getFetchOptions().hasCreatorPerson())
        {
            return creatorPerson;
        } else
        {
            throw new NotFetchedException("CreatorPerson has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setCreatorPerson(Person creatorPerson)
    {
        this.creatorPerson = creatorPerson;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public String toString()
    {
        return "Session Information " + userName;
    }

}

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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.event;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IRegistrationDateHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IRegistratorHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.EntityType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.EventType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.fetchoptions.EventFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.id.IEventId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.NotFetchedException;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Date;

/*
 * Class automatically generated with DtoGenerator
 */
@JsonObject("as.dto.event.Event")
public class Event implements Serializable, IRegistrationDateHolder, IRegistratorHolder
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private EventFetchOptions fetchOptions;

    @JsonProperty
    private IEventId id;

    @JsonProperty
    private EventType eventType;

    @JsonProperty
    private EntityType entityType;

    @JsonProperty
    private String entitySpace;

    @JsonProperty
    private ISpaceId entitySpaceId;

    @JsonProperty
    private String entityProject;

    @JsonProperty
    private IProjectId entityProjectId;

    @JsonProperty
    private String entityRegistrator;

    @JsonProperty
    private Date entityRegistrationDate;

    @JsonProperty
    private String identifier;

    @JsonProperty
    private String description;

    @JsonProperty
    private String reason;

    @JsonProperty
    private String content;

    @JsonProperty
    private Person registrator;

    @JsonProperty
    private Date registrationDate;

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public EventFetchOptions getFetchOptions()
    {
        return fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public void setFetchOptions(EventFetchOptions fetchOptions)
    {
        this.fetchOptions = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public IEventId getId()
    {
        return id;
    }

    // Method automatically generated with DtoGenerator
    public void setId(IEventId id)
    {
        this.id = id;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public EventType getEventType()
    {
        return eventType;
    }

    // Method automatically generated with DtoGenerator
    public void setEventType(EventType eventType)
    {
        this.eventType = eventType;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public EntityType getEntityType()
    {
        return entityType;
    }

    // Method automatically generated with DtoGenerator
    public void setEntityType(EntityType entityType)
    {
        this.entityType = entityType;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public String getEntitySpace()
    {
        return entitySpace;
    }

    // Method automatically generated with DtoGenerator
    public void setEntitySpace(String entitySpace)
    {
        this.entitySpace = entitySpace;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public ISpaceId getEntitySpaceId()
    {
        return entitySpaceId;
    }

    // Method automatically generated with DtoGenerator
    public void setEntitySpaceId(ISpaceId entitySpaceId)
    {
        this.entitySpaceId = entitySpaceId;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public String getEntityProject()
    {
        return entityProject;
    }

    // Method automatically generated with DtoGenerator
    public void setEntityProject(String entityProject)
    {
        this.entityProject = entityProject;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public IProjectId getEntityProjectId()
    {
        return entityProjectId;
    }

    // Method automatically generated with DtoGenerator
    public void setEntityProjectId(IProjectId entityProjectId)
    {
        this.entityProjectId = entityProjectId;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public String getEntityRegistrator()
    {
        return entityRegistrator;
    }

    // Method automatically generated with DtoGenerator
    public void setEntityRegistrator(String entityRegistrator)
    {
        this.entityRegistrator = entityRegistrator;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Date getEntityRegistrationDate()
    {
        return entityRegistrationDate;
    }

    // Method automatically generated with DtoGenerator
    public void setEntityRegistrationDate(Date entityRegistrationDate)
    {
        this.entityRegistrationDate = entityRegistrationDate;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public String getIdentifier()
    {
        return identifier;
    }

    // Method automatically generated with DtoGenerator
    public void setIdentifier(String identifier)
    {
        this.identifier = identifier;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public String getDescription()
    {
        return description;
    }

    // Method automatically generated with DtoGenerator
    public void setDescription(String description)
    {
        this.description = description;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public String getReason()
    {
        return reason;
    }

    // Method automatically generated with DtoGenerator
    public void setReason(String reason)
    {
        this.reason = reason;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public String getContent()
    {
        return content;
    }

    // Method automatically generated with DtoGenerator
    public void setContent(String content)
    {
        this.content = content;
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
    @Override
    public String toString()
    {
        return "Event " + id;
    }

}

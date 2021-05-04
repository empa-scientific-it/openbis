/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import ch.systemsx.cisd.common.reflection.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.Id;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * @author pkupczyk
 */
@Entity
@Table(name = TableNames.EVENTS_SEARCH_TABLE)
public class EventsSearchPE implements IIdHolder, Serializable
{

    private static final long serialVersionUID = IServer.VERSION;

    private transient Long id;

    private EventType eventType;

    private EntityType entityType;

    private String entitySpace;

    private String entitySpacePermId;

    private String entityProject;

    private String entityProjectPermId;

    private String entityRegisterer;

    private Date entityRegistrationTimestamp;

    private String identifier;

    private String description;

    private String reason;

    private String content;

    private Long attachmentContent;

    private PersonPE registerer;

    private Date registrationTimestamp;

    public final void setId(final Long id)
    {
        this.id = id;
    }

    @Override
    @Id
    @SequenceGenerator(name = SequenceNames.EVENTS_SEARCH_SEQUENCE, sequenceName = SequenceNames.EVENTS_SEARCH_SEQUENCE, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.EVENTS_SEARCH_SEQUENCE)
    public final Long getId()
    {
        return id;
    }

    @NotNull(message = ValidationMessages.EVENT_TYPE_NOT_NULL_MESSAGE)
    @Column(name = ColumnNames.EVENT_TYPE)
    @Enumerated(EnumType.STRING)
    public EventType getEventType()
    {
        return eventType;
    }

    public void setEventType(final EventType eventType)
    {
        this.eventType = eventType;
    }

    @NotNull(message = ValidationMessages.ENTITY_TYPE_NOT_NULL_MESSAGE)
    @Column(name = ColumnNames.ENTITY_TYPE)
    @Enumerated(EnumType.STRING)
    public EntityType getEntityType()
    {
        return entityType;
    }

    public void setEntityType(EntityType entityType)
    {
        this.entityType = entityType;
    }

    @NotNull(message = ValidationMessages.IDENTIFIER_NOT_NULL_MESSAGE)
    public String getIdentifier()
    {
        return identifier;
    }

    public void setIdentifier(String identifier)
    {
        this.identifier = identifier;
    }

    @Column(name = ColumnNames.ENTITY_SPACE)
    public String getEntitySpace()
    {
        return entitySpace;
    }

    public void setEntitySpace(String entitySpace)
    {
        this.entitySpace = entitySpace;
    }

    @Column(name = ColumnNames.ENTITY_SPACE_PERM_ID)
    public String getEntitySpacePermId()
    {
        return entitySpacePermId;
    }

    public void setEntitySpacePermId(String entitySpacePermId)
    {
        this.entitySpacePermId = entitySpacePermId;
    }

    @Column(name = ColumnNames.ENTITY_PROJECT)
    public String getEntityProject()
    {
        return entityProject;
    }

    public void setEntityProject(String entityProject)
    {
        this.entityProject = entityProject;
    }

    @Column(name = ColumnNames.ENTITY_PROJECT_PERM_ID)
    public String getEntityProjectPermId()
    {
        return entityProjectPermId;
    }

    public void setEntityProjectPermId(String entityProjectPermId)
    {
        this.entityProjectPermId = entityProjectPermId;
    }

    @Column(name = ColumnNames.ENTITY_REGISTERER)
    public String getEntityRegisterer()
    {
        return entityRegisterer;
    }

    public void setEntityRegisterer(String entityRegisterer)
    {
        this.entityRegisterer = entityRegisterer;
    }

    @Column(name = ColumnNames.ENTITY_REGISTRATION_TIMESTAMP)
    public Date getEntityRegistrationTimestamp()
    {
        return entityRegistrationTimestamp;
    }

    public void setEntityRegistrationTimestamp(Date entityRegistrationTimestamp)
    {
        this.entityRegistrationTimestamp = entityRegistrationTimestamp;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getReason()
    {
        return reason;
    }

    public void setReason(final String reason)
    {
        this.reason = reason;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    @Column(name = ColumnNames.ATTACHMENT_CONTENT_COLUMN)
    public Long getAttachmentContent()
    {
        return attachmentContent;
    }

    public void setAttachmentContent(final Long attachmentContent)
    {
        this.attachmentContent = attachmentContent;
    }

    @Column(name = ColumnNames.REGISTRATION_TIMESTAMP_COLUMN, nullable = false, updatable = false)
    public Date getRegistrationTimestamp()
    {
        return registrationTimestamp;
    }

    public void setRegistrationTimestamp(final Date registrationTimestamp)
    {
        this.registrationTimestamp = registrationTimestamp;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = ColumnNames.PERSON_REGISTERER_COLUMN, nullable = false, updatable = false)
    public PersonPE getRegisterer()
    {
        return registerer;
    }

    public void setRegisterer(final PersonPE registerer)
    {
        this.registerer = registerer;
    }

    //
    // Object
    //

    @Override
    public final String toString()
    {
        return ToStringBuilder.reflectionToString(ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
    }

    @Override
    public boolean equals(Object obj)
    {
        return EqualsBuilder.reflectionEquals(this, obj, true);
    }

    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this, true);
    }

}

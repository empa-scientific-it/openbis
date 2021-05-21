package ch.systemsx.cisd.openbis.generic.server.task.events_search;

import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventsSearchPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

class NewEvent
{
    public Long id;

    public EventType eventType;

    public EntityType entityType;

    public String entitySpaceCode;

    public String entitySpacePermId;

    public String entityProject;

    public String entityProjectPermId;

    public String entityExperimentPermId;

    public String entitySamplePermId;

    public String entityUnknownPermId;

    public String entityRegisterer;

    public Date entityRegistrationTimestamp;

    public String identifier;

    public String description;

    public String reason;

    public String content;

    public Long attachmentContent;

    public PersonPE registerer;

    public Date registrationTimestamp;

    public static Set<String> getEntityExperimentPermIdsOrUnknown(Collection<NewEvent> newEvents)
    {
        Set<String> result = new HashSet<>();
        for (NewEvent newEvent : newEvents)
        {
            if (newEvent.entityExperimentPermId != null)
            {
                result.add(newEvent.entityExperimentPermId);
            } else if (newEvent.entityUnknownPermId != null)
            {
                result.add(newEvent.entityUnknownPermId);
            }
        }
        return result;
    }

    public static Set<String> getEntitySamplePermIdsOrUnknown(Collection<NewEvent> newEvents)
    {
        Set<String> result = new HashSet<>();
        for (NewEvent newEvent : newEvents)
        {
            if (newEvent.entitySamplePermId != null)
            {
                result.add(newEvent.entitySamplePermId);
            } else if (newEvent.entityUnknownPermId != null)
            {
                result.add(newEvent.entityUnknownPermId);
            }
        }
        return result;
    }

    public static NewEvent fromOldEventPE(EventPE oldEvent)
    {
        NewEvent newEvent = new NewEvent();
        newEvent.id = oldEvent.getId();
        newEvent.eventType = oldEvent.getEventType();
        newEvent.entityType = oldEvent.getEntityType();
        newEvent.description = oldEvent.getDescription();
        newEvent.reason = oldEvent.getReason();
        newEvent.attachmentContent = oldEvent.getAttachmentContent() != null ? HibernateUtils.getId(oldEvent.getAttachmentContent()) : null;
        newEvent.registerer = oldEvent.getRegistrator();
        newEvent.registrationTimestamp = oldEvent.getRegistrationDateInternal();
        return newEvent;
    }

    public EventsSearchPE toNewEventPE()
    {
        EventsSearchPE newEventPE = new EventsSearchPE();
        newEventPE.setEventType(eventType);
        newEventPE.setEntityType(entityType);
        newEventPE.setEntitySpace(entitySpaceCode);
        newEventPE.setEntitySpacePermId(entitySpacePermId);
        newEventPE.setEntityProject(entityProject);
        newEventPE.setEntityProjectPermId(entityProjectPermId);
        newEventPE.setEntityRegisterer(entityRegisterer);
        newEventPE.setEntityRegistrationTimestamp(entityRegistrationTimestamp);
        newEventPE.setIdentifier(identifier);
        newEventPE.setDescription(description);
        newEventPE.setReason(reason);
        newEventPE.setContent(content);
        newEventPE.setAttachmentContent(attachmentContent);
        newEventPE.setRegisterer(registerer);
        newEventPE.setRegistrationTimestamp(registrationTimestamp);
        return newEventPE;
    }

    @Override public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}
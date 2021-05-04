package ch.systemsx.cisd.openbis.generic.server.task.events_search;

import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import org.apache.commons.lang3.mutable.MutableObject;
import org.springframework.transaction.support.TransactionCallback;

import java.util.Date;
import java.util.List;

public class GenericEventProcessor extends EventProcessor
{
    private final EventType eventType;

    private final EntityType entityType;

    GenericEventProcessor(IDataSource dataSource, EventType eventType)
    {
        super(dataSource);
        this.eventType = eventType;
        this.entityType = null;
    }

    GenericEventProcessor(IDataSource dataSource, EventType eventType, EntityType entityType)
    {
        super(dataSource);
        this.eventType = eventType;
        this.entityType = entityType;
    }

    @Override final public void process(LastTimestamps lastTimestamps, SnapshotsFacade snapshots)
    {
        final Date lastSeenTimestampOrNull = entityType != null ?
                lastTimestamps.getLatestOrNull(eventType, entityType) :
                lastTimestamps.getLatestOrNull(eventType, EntityType.values());

        final MutableObject<Date> latestLastSeenTimestamp = new MutableObject<>(lastSeenTimestampOrNull);

        while (true)
        {
            final List<EventPE> events = dataSource.loadEvents(eventType, entityType, latestLastSeenTimestamp.getValue());

            if (events.isEmpty())
            {
                break;
            }

            dataSource.executeInNewTransaction((TransactionCallback<Void>) status ->
            {
                for (EventPE event : events)
                {
                    try
                    {
                        NewEvent newEvent = NewEvent.fromOldEventPE(event);
                        newEvent.identifier = event.getIdentifiers() != null ? String.join(", ", event.getIdentifiers()) : null;

                        process(lastTimestamps, snapshots, event, newEvent);

                        if (latestLastSeenTimestamp.getValue() == null || event.getRegistrationDateInternal()
                                .after(latestLastSeenTimestamp.getValue()))
                        {
                            latestLastSeenTimestamp.setValue(event.getRegistrationDateInternal());
                        }
                    } catch (Exception e)
                    {
                        throw new RuntimeException(String.format("Processing of deletion failed: %s", event), e);
                    }
                }

                return null;
            });
        }
    }

    protected void process(LastTimestamps lastTimestamps, SnapshotsFacade snapshots, EventPE oldEvent, NewEvent newEvent) throws Exception
    {
        dataSource.createEventsSearch(newEvent.toNewEventPE());
    }
}

/*
 * Copyright ETH 2021 - 2023 Zürich, Scientific IT Services
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
            final List<EventPE> events = dataSource.loadEvents(eventType, entityType, latestLastSeenTimestamp.getValue(), DEFAULT_BATCH_SIZE);

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

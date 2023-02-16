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

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSetLocation;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;

import java.util.*;

class DataSetDeletionProcessor extends DeletionEventProcessor
{
    DataSetDeletionProcessor(IDataSource dataSource)
    {
        super(dataSource);
    }

    @Override protected EntityType getEntityType()
    {
        return EntityType.DATASET;
    }

    @Override protected Set<EntityType> getAscendantEntityTypes()
    {
        return EnumSet.of(EntityType.EXPERIMENT, EntityType.SAMPLE);
    }

    @Override protected Set<EntityType> getDescendantEntityTypes()
    {
        return Collections.emptySet();
    }

    @Override protected int getBatchSize()
    {
        return 1;
    }

    @Override protected void processDeletion(LastTimestamps lastTimestamps, EventPE deletion, List<NewEvent> newEvents, List<Snapshot> newSnapshots)
            throws Exception
    {
        List<NewEvent> events = new LinkedList<>();
        super.processDeletion(lastTimestamps, deletion, events, newSnapshots);
        List<DeletedDataSetLocation> locations = DeletedDataSetLocation.parse(deletion.getDescription());

        if (locations.size() == events.size())
        {
            Iterator<DeletedDataSetLocation> locationIterator = locations.iterator();
            Iterator<NewEvent> eventIterator = events.iterator();

            while (locationIterator.hasNext())
            {
                NewEvent event = eventIterator.next();
                DeletedDataSetLocation location = locationIterator.next();
                event.description = DeletedDataSetLocation.format(Collections.singletonList(location));
            }
        }

        newEvents.addAll(events);
    }

    @Override protected void processDeletions(LastTimestamps lastTimestamps, SnapshotsFacade snapshots, List<NewEvent> newEvents,
            List<Snapshot> newSnapshots)
    {
        snapshots.putDataSets(newSnapshots);

        for (NewEvent newEvent : newEvents)
        {
            try
            {
                snapshots.fillByDataSetPermId(newEvent.identifier, newEvent);
                dataSource.createEventsSearch(newEvent.toNewEventPE());

            } catch (Exception e)
            {
                throw new RuntimeException(String.format("Processing of deletion failed: %s", newEvent), e);
            }
        }
    }
}

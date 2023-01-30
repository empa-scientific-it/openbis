package ch.systemsx.cisd.openbis.generic.server.task.events_search;

import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

class SpaceDeletionProcessor extends DeletionEventProcessor
{

    SpaceDeletionProcessor(IDataSource dataSource)
    {
        super(dataSource);
    }

    @Override protected EntityType getEntityType()
    {
        return EntityType.SPACE;
    }

    @Override protected Set<EntityType> getAscendantEntityTypes()
    {
        return Collections.emptySet();
    }

    @Override protected Set<EntityType> getDescendantEntityTypes()
    {
        return EnumSet.of(EntityType.PROJECT, EntityType.EXPERIMENT, EntityType.SAMPLE, EntityType.DATASET);
    }

    @Override protected void processDeletions(LastTimestamps lastTimestamps, SnapshotsFacade snapshots, List<NewEvent> newEvents,
            List<Snapshot> newSnapshots)
    {
        for (Snapshot newSnapshot : newSnapshots)
        {
            newSnapshot.entityCode = newSnapshot.entityPermId;
            newSnapshot.entityPermId = null;
        }

        snapshots.putSpaces(newSnapshots);

        for (NewEvent newEvent : newEvents)
        {
            try
            {
                snapshots.fillBySpaceCode(newEvent.identifier, newEvent);
                dataSource.createEventsSearch(newEvent.toNewEventPE());

            } catch (Exception e)
            {
                throw new RuntimeException(String.format("Processing of deletion failed: %s", newEvent), e);
            }
        }
    }

}
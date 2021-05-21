package ch.systemsx.cisd.openbis.generic.server.task.events_search;

import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

class ExperimentDeletionProcessor extends DeletionEventProcessor
{
    ExperimentDeletionProcessor(IDataSource dataSource)
    {
        super(dataSource);
    }

    @Override protected EntityType getEntityType()
    {
        return EntityType.EXPERIMENT;
    }

    @Override protected Set<EntityType> getAscendantEntityTypes()
    {
        return EnumSet.of(EntityType.PROJECT);
    }

    @Override protected Set<EntityType> getDescendantEntityTypes()
    {
        return EnumSet.of(EntityType.SAMPLE, EntityType.DATASET, EntityType.ATTACHMENT);
    }

    @Override protected void processDeletion(LastTimestamps lastTimestamps, EventPE deletion, List<NewEvent> newEvents, List<Snapshot> newSnapshots)
            throws Exception
    {
        List<NewEvent> events = new LinkedList<>();
        super.processDeletion(lastTimestamps, deletion, events, newSnapshots);

        for (NewEvent event : events)
        {
            event.description = event.identifier;
        }

        newEvents.addAll(events);
    }

    @Override protected void processDeletions(LastTimestamps lastTimestamps, SnapshotsFacade snapshots, List<NewEvent> newEvents,
            List<Snapshot> newSnapshots)
    {
        snapshots.putExperiments(newSnapshots);

        for (NewEvent newEvent : newEvents)
        {
            try
            {
                snapshots.fillByExperimentPermId(newEvent.identifier, newEvent);
                dataSource.createEventsSearch(newEvent.toNewEventPE());

            } catch (Exception e)
            {
                throw new RuntimeException(String.format("Processing of deletion failed: %s", newEvent), e);
            }
        }
    }
}
package ch.systemsx.cisd.openbis.generic.server.task.events_search;

import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

class AttachmentDeletionProcessor extends DeletionEventProcessor
{

    AttachmentDeletionProcessor(IDataSource dataSource)
    {
        super(dataSource);
    }

    @Override protected EntityType getEntityType()
    {
        return EntityType.ATTACHMENT;
    }

    @Override protected Set<EntityType> getAscendantEntityTypes()
    {
        return Collections.emptySet();
    }

    @Override protected Set<EntityType> getDescendantEntityTypes()
    {
        return Collections.emptySet();
    }

    @Override protected void processDeletion(LastTimestamps lastTimestamps, EventPE deletion, List<NewEvent> newEvents, List<Snapshot> newSnapshots)
            throws Exception
    {
        if (deletion.getContent() == null || deletion.getContent().trim().isEmpty())
        {
            for (String attachmentPath : deletion.getIdentifiers())
            {
                NewEvent newEvent = NewEvent.fromOldEventPE(deletion);
                newEvent.identifier = attachmentPath;
                newEvents.add(newEvent);
            }
            return;
        }

        @SuppressWarnings("unchecked") Map<String, Object> parsedContent =
                (Map<String, Object>) OBJECT_MAPPER.readValue(deletion.getContent(), Object.class);

        for (String attachmentPath : parsedContent.keySet())
        {
            @SuppressWarnings("unchecked") List<Map<String, String>> entries =
                    (List<Map<String, String>>) parsedContent.get(attachmentPath);

            for (Map<String, String> entry : entries)
            {
                String type = entry.get(ENTRY_TYPE);
                String key = entry.get(ENTRY_KEY);

                if (ENTRY_TYPE_ATTACHMENT.equals(type) && ENTRY_KEY_OWNED.equals(key))
                {
                    String entityType = entry.get(ENTRY_ENTITY_TYPE);
                    String value = entry.get(ENTRY_VALUE);

                    Snapshot snapshot = new Snapshot();
                    if (EntityType.PROJECT.name().equals(entityType))
                    {
                        snapshot.projectPermId = value;
                    } else if (EntityType.EXPERIMENT.name().equals(entityType))
                    {
                        snapshot.experimentPermId = value;
                    } else if (EntityType.SAMPLE.name().equals(entityType))
                    {
                        snapshot.samplePermId = value;
                    }
                    newSnapshots.add(snapshot);

                    NewEvent newEvent = NewEvent.fromOldEventPE(deletion);
                    newEvent.identifier = attachmentPath;
                    newEvent.entityProjectPermId = snapshot.projectPermId;
                    newEvent.entityExperimentPermId = snapshot.experimentPermId;
                    newEvent.entitySamplePermId = snapshot.samplePermId;
                    newEvent.entityRegisterer = entry.get(ENTRY_USER_ID);
                    newEvent.content = OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(entries);

                    String validFrom = entry.get(ENTRY_VALID_FROM);
                    if (validFrom != null)
                    {
                        newEvent.entityRegistrationTimestamp = ENTRY_VALID_TIMESTAMP_FORMAT.parse(validFrom);
                    }

                    newEvents.add(newEvent);
                }
            }
        }
    }

    @Override protected void processDeletions(LastTimestamps lastTimestamps, SnapshotsFacade snapshots, List<NewEvent> newEvents,
            List<Snapshot> newSnapshots)
    {
        snapshots.loadExistingProjects(SnapshotsFacade.getProjectPermIdsOrUnknown(newSnapshots));
        snapshots.loadExistingExperiments(SnapshotsFacade.getExperimentPermIdsOrUnknown(newSnapshots));
        snapshots.loadExistingSamples(SnapshotsFacade.getSamplePermIdsOrUnknown(newSnapshots));

        for (NewEvent newEvent : newEvents)
        {
            try
            {
                if (newEvent.entityProjectPermId != null)
                {
                    snapshots.fillByProjectPermId(newEvent.entityProjectPermId, newEvent);
                } else if (newEvent.entityExperimentPermId != null)
                {
                    snapshots.fillByExperimentPermId(newEvent.entityExperimentPermId, newEvent);
                } else if (newEvent.entitySamplePermId != null)
                {
                    snapshots.fillBySamplePermId(newEvent.entitySamplePermId, newEvent);
                }

                dataSource.createEventsSearch(newEvent.toNewEventPE());

            } catch (Exception e)
            {
                throw new RuntimeException(String.format("Processing of deletion failed: %s", newEvent), e);
            }
        }
    }

}
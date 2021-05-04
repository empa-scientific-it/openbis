package ch.systemsx.cisd.openbis.generic.server.task.events_search;

import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.mutable.MutableObject;
import org.springframework.transaction.support.TransactionCallback;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

abstract class DeletionEventProcessor extends EventProcessor
{

    protected static final SimpleDateFormat ENTRY_REGISTRATION_TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

    protected static final SimpleDateFormat ENTRY_VALID_TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    protected static final String ENTRY_TYPE = "type";

    protected static final String ENTRY_TYPE_ATTRIBUTE = "ATTRIBUTE";

    protected static final String ENTRY_TYPE_RELATIONSHIP = "RELATIONSHIP";

    protected static final String ENTRY_TYPE_ATTACHMENT = "ATTACHMENT";

    protected static final String ENTRY_KEY = "key";

    protected static final String ENTRY_KEY_CODE = "CODE";

    protected static final String ENTRY_KEY_REGISTRATOR = "REGISTRATOR";

    protected static final String ENTRY_KEY_REGISTRATION_TIMESTAMP = "REGISTRATION_TIMESTAMP";

    protected static final String ENTRY_KEY_OWNED = "OWNED";

    protected static final String ENTRY_VALUE = "value";

    protected static final String ENTRY_ENTITY_TYPE = "entityType";

    protected static final String ENTRY_ENTITY_TYPE_UNKNOWN = "UNKNOWN";

    protected static final String ENTRY_VALID_FROM = "validFrom";

    protected static final String ENTRY_VALID_UNTIL = "validUntil";

    protected static final String ENTRY_USER_ID = "userId";

    DeletionEventProcessor(IDataSource dataSource)
    {
        super(dataSource);
    }

    protected abstract EntityType getEntityType();

    protected abstract Set<EntityType> getAscendantEntityTypes();

    protected abstract Set<EntityType> getDescendantEntityTypes();

    protected abstract void processDeletions(LastTimestamps lastTimestamps, SnapshotsFacade snapshots, List<NewEvent> newEvents,
            List<Snapshot> newSnapshots);

    @Override final public void process(LastTimestamps lastTimestamps, SnapshotsFacade snapshots)
    {
        final Collection<EntityType> lastSeenEntityTypes = new HashSet<>();
        lastSeenEntityTypes.add(getEntityType());
        lastSeenEntityTypes.addAll(getDescendantEntityTypes());

        final Date lastSeenTimestampOrNull =
                lastTimestamps.getEarliestOrNull(EventType.DELETION, lastSeenEntityTypes.toArray(new EntityType[0]));
        final MutableObject<Date> latestLastSeenTimestamp = new MutableObject<>(lastSeenTimestampOrNull);

        while (true)
        {
            final List<EventPE> deletions =
                    dataSource.loadEvents(EventType.DELETION, getEntityType(), latestLastSeenTimestamp.getValue());

            if (deletions.isEmpty())
            {
                break;
            }

            dataSource.executeInNewTransaction((TransactionCallback<Void>) status -> {
                List<NewEvent> newEvents = new LinkedList<>();
                List<Snapshot> newSnapshots = new LinkedList<>();

                for (EventPE deletion : deletions)
                {
                    try
                    {
                        processDeletion(lastTimestamps, deletion, newEvents, newSnapshots);

                        if (latestLastSeenTimestamp.getValue() == null || deletion.getRegistrationDateInternal()
                                .after(latestLastSeenTimestamp.getValue()))
                        {
                            latestLastSeenTimestamp.setValue(deletion.getRegistrationDateInternal());
                        }
                    } catch (Exception e)
                    {
                        throw new RuntimeException(String.format("Processing of deletion failed: %s", deletion), e);
                    }
                }

                processDeletions(lastTimestamps, snapshots, newEvents, newSnapshots);

                return null;
            });
        }
    }

    protected void processDeletion(LastTimestamps lastTimestamps, EventPE deletion, List<NewEvent> newEvents,
            List<Snapshot> newSnapshots)
            throws Exception
    {
        final Date lastSeenTimestampOrNull = lastTimestamps.getEarliestOrNull(EventType.DELETION, getEntityType());

        if (deletion.getContent() == null || deletion.getContent().trim().isEmpty())
        {
            for (String entityPermId : deletion.getIdentifiers())
            {
                Snapshot snapshot = new Snapshot();
                snapshot.entityPermId = entityPermId;
                snapshot.from = new Date(0);
                snapshot.to = deletion.getRegistrationDateInternal();
                newSnapshots.add(snapshot);

                if (lastSeenTimestampOrNull == null || deletion.getRegistrationDateInternal()
                        .after(lastSeenTimestampOrNull))
                {
                    NewEvent newEvent = NewEvent.fromOldEventPE(deletion);
                    newEvent.identifier = entityPermId;
                    newEvents.add(newEvent);
                }
            }

            return;
        }

        @SuppressWarnings("unchecked") Map<String, Object> parsedContent =
                (Map<String, Object>) OBJECT_MAPPER.readValue(deletion.getContent(), Object.class);

        for (String entityPermId : parsedContent.keySet())
        {
            @SuppressWarnings("unchecked") List<Map<String, String>> entries =
                    (List<Map<String, String>>) parsedContent.get(entityPermId);

            String entityCode = null;
            String registerer = null;
            Date registrationTimestamp = null;

            for (Map<String, String> entry : entries)
            {
                String type = entry.get(ENTRY_TYPE);
                String key = entry.get(ENTRY_KEY);
                String value = entry.get(ENTRY_VALUE);

                if (ENTRY_TYPE_ATTRIBUTE.equals(type))
                {
                    if (ENTRY_KEY_CODE.equals(key))
                    {
                        entityCode = value;
                    } else if (ENTRY_KEY_REGISTRATOR.equals(key))
                    {
                        registerer = value;
                    } else if (ENTRY_KEY_REGISTRATION_TIMESTAMP.equals(key))
                    {
                        registrationTimestamp = ENTRY_REGISTRATION_TIMESTAMP_FORMAT.parse(value);
                    }
                }
            }

            Set<String> relationshipEntityTypes = getAscendantEntityTypes().stream().map(Enum::name).collect(Collectors.toSet());
            Snapshot lastSnapshot = null;

            for (Map<String, String> entry : entries)
            {
                String type = entry.get(ENTRY_TYPE);
                String key = entry.get(ENTRY_KEY);

                if (ENTRY_TYPE_RELATIONSHIP.equals(type) && ENTRY_KEY_OWNED.equals(key))
                {
                    String entityType = entry.get(ENTRY_ENTITY_TYPE);

                    if (relationshipEntityTypes.contains(entityType) || ENTRY_ENTITY_TYPE_UNKNOWN.equals(entityType))
                    {
                        Snapshot snapshot = new Snapshot();
                        snapshot.entityCode = entityCode;
                        snapshot.entityPermId = entityPermId;

                        String value = entry.get(ENTRY_VALUE);
                        if (EntityType.SPACE.name().equals(entityType))
                        {
                            snapshot.spaceCode = value;
                        } else if (EntityType.PROJECT.name().equals(entityType))
                        {
                            snapshot.projectPermId = value;
                        } else if (EntityType.EXPERIMENT.name().equals(entityType))
                        {
                            snapshot.experimentPermId = value;
                        } else if (EntityType.SAMPLE.name().equals(entityType))
                        {
                            snapshot.samplePermId = value;
                        } else
                        {
                            snapshot.unknownPermId = value;
                        }

                        String validFrom = entry.get(ENTRY_VALID_FROM);
                        if (validFrom != null)
                        {
                            snapshot.from = ENTRY_VALID_TIMESTAMP_FORMAT.parse(validFrom);
                        }

                        String validUntil = entry.get(ENTRY_VALID_UNTIL);
                        if (validUntil != null)
                        {
                            snapshot.to = ENTRY_VALID_TIMESTAMP_FORMAT.parse(validUntil);
                        } else
                        {
                            snapshot.to = deletion.getRegistrationDateInternal();
                            lastSnapshot = snapshot;
                        }

                        newSnapshots.add(snapshot);
                    }
                }
            }

            if (lastSnapshot == null)
            {
                Snapshot snapshot = new Snapshot();
                snapshot.entityCode = entityCode;
                snapshot.entityPermId = entityPermId;
                snapshot.from = registrationTimestamp;
                snapshot.to = deletion.getRegistrationDateInternal();

                newSnapshots.add(snapshot);
                lastSnapshot = snapshot;
            }

            if (lastSeenTimestampOrNull == null || deletion.getRegistrationDateInternal()
                    .after(lastSeenTimestampOrNull))
            {
                NewEvent newEvent = NewEvent.fromOldEventPE(deletion);
                newEvent.entitySpaceCode = lastSnapshot.spaceCode;
                newEvent.entityProjectPermId = lastSnapshot.projectPermId;
                newEvent.entityExperimentPermId = lastSnapshot.experimentPermId;
                newEvent.entitySamplePermId = lastSnapshot.samplePermId;
                newEvent.entityUnknownPermId = lastSnapshot.unknownPermId;
                newEvent.entityRegisterer = registerer;
                newEvent.entityRegistrationTimestamp = registrationTimestamp;
                newEvent.identifier = entityPermId;
                newEvent.content = OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(entries);
                newEvents.add(newEvent);
            }
        }
    }

}

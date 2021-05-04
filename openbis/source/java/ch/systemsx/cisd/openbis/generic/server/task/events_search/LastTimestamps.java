package ch.systemsx.cisd.openbis.generic.server.task.events_search;

import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

class LastTimestamps
{
    private final Map<Pair<EventType, EntityType>, Date> timestamps = new HashMap<>();

    public LastTimestamps(IDataSource dataSource)
    {
        for (EventType eventType : EventType.values())
        {
            for (EntityType entityType : EntityType.values())
            {
                Date lastTimestamp = dataSource.loadLastEventsSearchTimestamp(eventType, entityType);
                timestamps.put(new ImmutablePair<>(eventType, entityType), lastTimestamp);
            }
        }
    }

    public Date getEarliestOrNull(EventType eventType, EntityType... entityTypes)
    {
        Date earliest = null;

        for (EntityType entityType : entityTypes)
        {
            Date timestamp = timestamps.get(new ImmutablePair<>(eventType, entityType));

            if (timestamp == null)
            {
                return null;
            }

            if (earliest == null || timestamp.before(earliest))
            {
                earliest = timestamp;
            }
        }

        return earliest;
    }

    public Date getLatestOrNull(EventType eventType, EntityType... entityTypes)
    {
        Date latest = null;

        for (EntityType entityType : entityTypes)
        {
            Date timestamp = timestamps.get(new ImmutablePair<>(eventType, entityType));

            if (timestamp == null)
            {
                continue;
            }

            if (latest == null || timestamp.after(latest))
            {
                latest = timestamp;
            }
        }

        return latest;
    }

}
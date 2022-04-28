package ch.systemsx.cisd.openbis.generic.server.task.events_search;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;

class LastTimestamps
{
    private final Map<Pair<EventType, EntityType>, Date> lastEventsSearchTimestamps = new HashMap<>();

    private final Map<Pair<EventType, EntityType>, Date> firstEventsTimestamps = new HashMap<>();

    public LastTimestamps(IDataSource dataSource)
    {
        for (EventType eventType : EventType.values())
        {
            for (EntityType entityType : EntityType.values())
            {
                Date lastEventsSearchTimestamp = dataSource.loadLastEventsSearchTimestamp(eventType, entityType);
                lastEventsSearchTimestamps.put(new ImmutablePair<>(eventType, entityType), lastEventsSearchTimestamp);

                if (lastEventsSearchTimestamp == null)
                {
                    List<EventPE> firstEvents = dataSource.loadEvents(eventType, entityType, null, 1);
                    if (firstEvents != null && firstEvents.size() > 0)
                    {
                        Date firstEventsTimestamp = new Date(firstEvents.get(0).getRegistrationDateInternal().getTime());
                        firstEventsTimestamps.put(new ImmutablePair<>(eventType, entityType), firstEventsTimestamp);
                    }
                }
            }
        }
    }

    public Date getEarliestOrNull(EventType eventType, EntityType... entityTypes)
    {
        Date earliest = null;

        for (EntityType entityType : entityTypes)
        {
            ImmutablePair<EventType, EntityType> key = new ImmutablePair<>(eventType, entityType);

            Date timestamp = lastEventsSearchTimestamps.get(key);
            if (timestamp == null)
            {
                timestamp = firstEventsTimestamps.get(key);
                if (timestamp != null)
                {
                    timestamp = new Date(timestamp.getTime() - DateUtils.MILLIS_PER_MINUTE);
                }
            }

            if (timestamp == null)
            {
                continue;
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
            Date timestamp = lastEventsSearchTimestamps.get(new ImmutablePair<>(eventType, entityType));

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
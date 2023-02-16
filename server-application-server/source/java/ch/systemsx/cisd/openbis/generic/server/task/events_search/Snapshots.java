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

import java.util.*;

class Snapshots
{
    private final Map<String, TreeMap<Date, Snapshot>> snapshots = new HashMap<>();

    public void put(String key, Snapshot snapshot)
    {
        TreeMap<Date, Snapshot> snapshotsForKey = snapshots.computeIfAbsent(key, k -> new TreeMap<>());
        snapshotsForKey.put(snapshot.from, snapshot);
    }

    public Collection<Snapshot> get(Collection<String> keys)
    {
        Collection<Snapshot> snapshotsForKeys = new ArrayList<>();

        for (String key : keys)
        {
            TreeMap<Date, Snapshot> snapshotsForKey = snapshots.get(key);

            if (snapshotsForKey != null)
            {
                snapshotsForKeys.addAll(snapshotsForKey.values());
            }
        }

        return snapshotsForKeys;
    }

    public Snapshot get(String key, Date date)
    {
        TreeMap<Date, Snapshot> snapshotsForKey = snapshots.get(key);

        if (snapshotsForKey != null)
        {
            Map.Entry<Date, Snapshot> potentialEntry = snapshotsForKey.floorEntry(date);

            if (potentialEntry != null)
            {
                Snapshot potentialSnapshot = potentialEntry.getValue();
                if (potentialSnapshot.to == null || date.compareTo(potentialSnapshot.to) <= 0)
                {
                    return potentialSnapshot;
                }
            }
        }

        return null;
    }
}
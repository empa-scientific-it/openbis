/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.shared;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import ch.systemsx.cisd.etlserver.ETLDaemon;

/**
 * Provider of IDs of incoming shares. This is a helper class to avoid direct connection between {@link ETLDaemon} (which determines incoming share
 * IDs) and consumers of these IDs.
 *
 * @author Franz-Josef Elmer
 */
public class IncomingShareIdProvider
{
    private static Set<String> incomingShareIds = new LinkedHashSet<String>();

    public static Set<String> getIdsOfIncomingShares()
    {
        return Collections.unmodifiableSet(incomingShareIds);
    }

    public static void add(Collection<String> ids)
    {
        incomingShareIds.addAll(ids);
    }

    public static void removeAllShareIds()
    {
        incomingShareIds.clear();
    }
}

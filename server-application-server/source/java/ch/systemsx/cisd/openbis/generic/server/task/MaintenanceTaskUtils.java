/*
 * Copyright 2022 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.generic.server.task;

import java.io.File;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.DataStore;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.fetchoptions.DataStoreFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.search.DataStoreSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;

/**
 * @author Franz-Josef Elmer
 */
public class MaintenanceTaskUtils
{
    private static final File STARTED_FILE = new File("SERVER_STARTED");

    public static boolean areAllDataStoreServersRunning(IApplicationServerInternalApi service, String sessionToken)
    {
        if (STARTED_FILE.exists() == false)
        {
            return false;
        }
        long startUpTimestampOfAS = STARTED_FILE.lastModified();
        DataStoreSearchCriteria searchCriteria = new DataStoreSearchCriteria();
        DataStoreFetchOptions fetchOptions = new DataStoreFetchOptions();
        List<DataStore> dataStores = service.searchDataStores(sessionToken, searchCriteria, fetchOptions).getObjects();
        for (DataStore dataStore : dataStores)
        {
            if (dataStore.getModificationDate().getTime() < startUpTimestampOfAS)
            {
                return false;
            }
        }
        return true;
    }
}

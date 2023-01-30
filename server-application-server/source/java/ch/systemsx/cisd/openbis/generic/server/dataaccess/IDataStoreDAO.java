/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;

/**
 * <i>Data Access Object</i> for {@link DataStorePE}.
 * 
 * @author Franz-Josef Elmer
 */
public interface IDataStoreDAO
{
    /**
     * Creates or updates specified data store.
     */
    public void createOrUpdateDataStore(DataStorePE dataStore);

    /**
     * Tries to returns specified data store or <code>null</code> if not found.
     */
    public DataStorePE tryToFindDataStoreByCode(String dataStoreCode);

    /** Lists all data stores in the home database */
    public List<DataStorePE> listDataStores();
}

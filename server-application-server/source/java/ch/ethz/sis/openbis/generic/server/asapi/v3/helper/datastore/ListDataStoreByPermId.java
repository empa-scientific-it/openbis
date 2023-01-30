/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.datastore;

import java.util.LinkedList;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.id.DataStorePermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.AbstractListObjectById;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataStoreDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;

/**
 * @author pkupczyk
 */
public class ListDataStoreByPermId extends AbstractListObjectById<DataStorePermId, DataStorePE>
{

    private IDataStoreDAO dataStoreDAO;

    public ListDataStoreByPermId(IDataStoreDAO dataStoreDAO)
    {
        this.dataStoreDAO = dataStoreDAO;
    }

    @Override
    public Class<DataStorePermId> getIdClass()
    {
        return DataStorePermId.class;
    }

    @Override
    public DataStorePermId createId(DataStorePE dataStore)
    {
        return new DataStorePermId(dataStore.getCode());
    }

    @Override
    public List<DataStorePE> listByIds(IOperationContext context, List<DataStorePermId> ids)
    {
        List<DataStorePE> dataStores = new LinkedList<DataStorePE>();

        for (DataStorePermId id : ids)
        {
            DataStorePE dataStore = dataStoreDAO.tryToFindDataStoreByCode(id.getPermId());
            if (dataStore != null)
            {
                dataStores.add(dataStore);
            }
        }

        return dataStores;
    }

}

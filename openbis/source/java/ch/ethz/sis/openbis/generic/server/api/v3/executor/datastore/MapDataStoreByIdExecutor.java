/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.datastore;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.common.AbstractMapObjectByIdExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.helper.common.IListObjectById;
import ch.ethz.sis.openbis.generic.server.api.v3.helper.datastore.ListDataStoreByPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.datastore.IDataStoreId;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataStoreDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;

/**
 * @author pkupczyk
 */
@Component
public class MapDataStoreByIdExecutor extends AbstractMapObjectByIdExecutor<IDataStoreId, DataStorePE> implements IMapDataStoreByIdExecutor
{

    private IDataStoreDAO dataStoreDAO;

    @Override
    protected void addListers(IOperationContext context, List<IListObjectById<? extends IDataStoreId, DataStorePE>> listers)
    {
        listers.add(new ListDataStoreByPermId(dataStoreDAO));
    }

    @Autowired
    private void setDAOFactory(IDAOFactory daoFactory)
    {
        dataStoreDAO = daoFactory.getDataStoreDAO();
    }

}

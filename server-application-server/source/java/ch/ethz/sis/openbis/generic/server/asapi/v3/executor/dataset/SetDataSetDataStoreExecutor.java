/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.id.IDataStoreId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.datastore.IMapDataStoreByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractSetEntityToOneRelationExecutor;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;

/**
 * @author pkupczyk
 */
@Component
public class SetDataSetDataStoreExecutor extends AbstractSetEntityToOneRelationExecutor<DataSetCreation, DataPE, IDataStoreId, DataStorePE>
        implements ISetDataSetDataStoreExecutor
{

    @Override
    protected String getRelationName()
    {
        return "dataset-datastore";
    }

    @Autowired
    private IMapDataStoreByIdExecutor mapDataStoreByIdExecutor;

    @Override
    protected IDataStoreId getRelatedId(DataSetCreation creation)
    {
        return creation.getDataStoreId();
    }

    @Override
    protected Map<IDataStoreId, DataStorePE> map(IOperationContext context, List<IDataStoreId> relatedIds)
    {
        return mapDataStoreByIdExecutor.map(context, relatedIds);
    }

    @Override
    protected void check(IOperationContext context, DataPE entity, IDataStoreId relatedId, DataStorePE related)
    {
        if (relatedId == null)
        {
            throw new UserFailureException("Data store id cannot be null.");
        }
    }

    @Override
    protected void set(IOperationContext context, DataPE entity, DataStorePE related)
    {
        entity.setDataStore(related);
    }

}
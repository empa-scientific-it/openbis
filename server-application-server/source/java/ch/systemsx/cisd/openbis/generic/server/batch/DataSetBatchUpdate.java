/*
 * Copyright ETH 2010 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.server.batch;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.server.business.bo.IDataSetTable;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetBatchUpdatesDTO;

/**
 * {@link IBatchOperation} updating data sets.
 * 
 * @author Izabela Adamczyk
 */
public class DataSetBatchUpdate extends AbstractBatchOperation<DataSetBatchUpdatesDTO>
{
    private final IDataSetTable businessTable;

    private final List<DataSetBatchUpdatesDTO> entities;

    public DataSetBatchUpdate(IDataSetTable businessTable, List<DataSetBatchUpdatesDTO> entities)
    {
        this(businessTable, entities, null);
    }

    public DataSetBatchUpdate(IDataSetTable businessTable, List<DataSetBatchUpdatesDTO> entities,
            IBatchOperationDelegate<DataSetBatchUpdatesDTO> delegate)
    {
        super(delegate);
        this.businessTable = businessTable;
        this.entities = entities;
    }

    @Override
    public void execute(List<DataSetBatchUpdatesDTO> updates)
    {
        businessTable.update(updates);

        batchOperationWillSave();
        businessTable.save();
        batchOperationDidSave();
    }

    @Override
    public List<DataSetBatchUpdatesDTO> getAllEntities()
    {
        return entities;
    }

    @Override
    public String getEntityName()
    {
        return "data set";
    }

    @Override
    public String getOperationName()
    {
        return "update";
    }

}
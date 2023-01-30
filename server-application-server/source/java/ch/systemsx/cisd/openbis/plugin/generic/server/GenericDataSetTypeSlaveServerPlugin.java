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

package ch.systemsx.cisd.openbis.plugin.generic.server;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import ch.systemsx.cisd.openbis.generic.server.batch.BatchOperationExecutor;
import ch.systemsx.cisd.openbis.generic.server.batch.DataSetBatchUpdate;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IDataSetTable;
import ch.systemsx.cisd.openbis.generic.server.plugin.IDataSetTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetBatchUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.generic.shared.ResourceNames;

/**
 * The <i>generic</i> data set slave server.
 * 
 * @author Franz-Josef Elmer
 */
@Component(ch.systemsx.cisd.openbis.generic.shared.ResourceNames.GENERIC_DATA_SET_TYPE_SLAVE_SERVER_PLUGIN)
public class GenericDataSetTypeSlaveServerPlugin implements IDataSetTypeSlaveServerPlugin
{
    @Resource(name = ResourceNames.GENERIC_BUSINESS_OBJECT_FACTORY)
    private IGenericBusinessObjectFactory businessObjectFactory;

    public GenericDataSetTypeSlaveServerPlugin()
    {
    }

    @Override
    public void permanentlyDeleteDataSets(Session session, List<DataPE> dataSets, String reason,
            boolean forceDisallowedTypes)
    {
        IDataSetTable dataSetTable = businessObjectFactory.createDataSetTable(session);
        dataSetTable.setDataSets(dataSets);
        dataSetTable.deleteLoadedDataSets(reason, forceDisallowedTypes);
    }

    @Override
    public void updateDataSets(Session session, List<DataSetBatchUpdatesDTO> dataSets)
    {
        assert session != null : "Unspecified session.";
        assert dataSets != null && dataSets.size() > 0 : "Unspecified data set or empty data sets.";

        BatchOperationExecutor.executeInBatches(new DataSetBatchUpdate(businessObjectFactory
                .createDataSetTable(session), dataSets));
    }

}

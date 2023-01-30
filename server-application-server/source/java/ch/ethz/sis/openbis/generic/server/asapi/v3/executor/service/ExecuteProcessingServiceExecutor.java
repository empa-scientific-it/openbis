/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.service;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.id.DataStorePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.id.IDataStoreId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.ProcessingServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.id.DssServicePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.id.IDssServiceId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IDataSetTable;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class ExecuteProcessingServiceExecutor
        extends AbstractDataSetBasedDssServiceExecutor<ProcessingServiceExecutionOptions>
        implements IExecuteProcessingServiceExecutor
{
    @Autowired
    private IProcessingServiceAuthorizationExecutor authorizationExecutor;

    @Resource(name = ComponentNames.COMMON_BUSINESS_OBJECT_FACTORY)
    protected ICommonBusinessObjectFactory businessObjectFactory;

    @Override
    public void execute(IOperationContext context, IDssServiceId serviceId, ProcessingServiceExecutionOptions options)
    {
        checkData(serviceId, options);
        List<String> dataSetCodes = options.getDataSetCodes();
        authorizationExecutor.canExecute(context, dataSetCodes);

        IDataSetTable dataSetTable = businessObjectFactory.createDataSetTable(context.getSession());
        DssServicePermId permId = (DssServicePermId) serviceId;
        String key = permId.getPermId();
        IDataStoreId dataStoreId = permId.getDataStoreId();
        Map<String, String> parameters = options.getParameters();
        if (dataStoreId instanceof DataStorePermId)
        {
            dataSetTable.processDatasets(key, ((DataStorePermId) dataStoreId).getPermId(), dataSetCodes, parameters);
        } else
        {
            dataSetTable.processDatasets(key, dataSetCodes, parameters);
        }
    }
}

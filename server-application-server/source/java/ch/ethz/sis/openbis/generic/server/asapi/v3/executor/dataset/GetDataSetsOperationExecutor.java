/*
 * Copyright 2016 ETH Zuerich, CISD
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

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.get.GetObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.get.GetObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.get.GetDataSetsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.get.GetDataSetsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.get.GetObjectsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.get.IMapObjectByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.dataset.IDataSetTranslator;

/**
 * @author pkupczyk
 */
@Component
public class GetDataSetsOperationExecutor extends GetObjectsOperationExecutor<IDataSetId, DataSet, DataSetFetchOptions>
        implements IGetDataSetsOperationExecutor
{

    @Autowired
    private IMapDataSetTechIdByIdExecutor mapExecutor;

    @Autowired
    private IDataSetTranslator translator;

    @Override
    protected Class<? extends GetObjectsOperation<IDataSetId, DataSetFetchOptions>> getOperationClass()
    {
        return GetDataSetsOperation.class;
    }

    @Override
    protected IMapObjectByIdExecutor<IDataSetId, Long> getExecutor()
    {
        return mapExecutor;
    }

    @Override
    protected ITranslator<Long, DataSet, DataSetFetchOptions> getTranslator()
    {
        return translator;
    }

    @Override
    protected GetObjectsOperationResult<IDataSetId, DataSet> getOperationResult(Map<IDataSetId, DataSet> objectMap)
    {
        return new GetDataSetsOperationResult(objectMap);
    }

}

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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.get.GetDataSetTypesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.get.GetDataSetTypesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractGetEntityTypesOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.dataset.IDataSetTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author pkupczyk
 */
@Component
public class GetDataSetTypesOperationExecutor extends AbstractGetEntityTypesOperationExecutor<DataSetType, DataSetTypeFetchOptions>
        implements IGetDataSetTypesOperationExecutor
{

    @Autowired
    private IDataSetTypeTranslator translator;

    @Override
    protected EntityKind getEntityKind()
    {
        return EntityKind.DATA_SET;
    }

    @Override
    protected Class<? extends GetObjectsOperation<IEntityTypeId, DataSetTypeFetchOptions>> getOperationClass()
    {
        return GetDataSetTypesOperation.class;
    }

    @Override
    protected ITranslator<Long, DataSetType, DataSetTypeFetchOptions> getTranslator()
    {
        return translator;
    }

    @Override
    protected GetObjectsOperationResult<IEntityTypeId, DataSetType> getOperationResult(Map<IEntityTypeId, DataSetType> objectMap)
    {
        return new GetDataSetTypesOperationResult(objectMap);
    }

}

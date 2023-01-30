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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.get;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.get.GetObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.get.GetObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecution;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.fetchoptions.OperationExecutionFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.get.GetOperationExecutionsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.get.GetOperationExecutionsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.id.IOperationExecutionId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.get.AbstractGetObjectsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.store.IOperationExecutionStore;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.NopTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;

/**
 * @author pkupczyk
 */
@Component
public class GetOperationExecutionsOperationExecutor
        extends AbstractGetObjectsOperationExecutor<IOperationExecutionId, OperationExecution, OperationExecution, OperationExecutionFetchOptions>
        implements IGetOperationExecutionsOperationExecutor
{

    @Autowired
    private IOperationExecutionStore store;

    @Override
    protected Class<? extends GetObjectsOperation<IOperationExecutionId, OperationExecutionFetchOptions>> getOperationClass()
    {
        return GetOperationExecutionsOperation.class;
    }

    @Override
    protected Map<IOperationExecutionId, OperationExecution> map(IOperationContext context, List<? extends IOperationExecutionId> ids,
            OperationExecutionFetchOptions fetchOptions)
    {
        Map<IOperationExecutionId, OperationExecution> map = new LinkedHashMap<IOperationExecutionId, OperationExecution>();
        for (IOperationExecutionId id : ids)
        {
            map.put(id, store.getExecution(context, id, fetchOptions));
        }
        return map;
    }

    @Override
    protected Map<OperationExecution, OperationExecution> translate(TranslationContext context, Collection<OperationExecution> objects,
            OperationExecutionFetchOptions fetchOptions)
    {
        return new NopTranslator<OperationExecution, OperationExecutionFetchOptions>().translate(context, objects, fetchOptions);
    }

    @Override
    protected GetObjectsOperationResult<IOperationExecutionId, OperationExecution> getOperationResult(
            Map<IOperationExecutionId, OperationExecution> objectMap)
    {
        return new GetOperationExecutionsOperationResult(objectMap);
    }

}

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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.material;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.UpdateObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.UpdateObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.IMaterialId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.update.MaterialUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.update.UpdateMaterialsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.update.UpdateMaterialsOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.update.UpdateObjectsOperationExecutor;

/**
 * @author pkupczyk
 */
@Component
public class UpdateMaterialsOperationExecutor extends UpdateObjectsOperationExecutor<MaterialUpdate, IMaterialId> implements
        IUpdateMaterialsOperationExecutor
{

    @Autowired
    private IUpdateMaterialExecutor executor;

    @Override
    protected Class<? extends UpdateObjectsOperation<MaterialUpdate>> getOperationClass()
    {
        return UpdateMaterialsOperation.class;
    }

    @Override
    protected UpdateObjectsOperationResult<? extends IMaterialId> doExecute(IOperationContext context,
            UpdateObjectsOperation<MaterialUpdate> operation)
    {
        return new UpdateMaterialsOperationResult(executor.update(context, operation.getUpdates()));
    }

}

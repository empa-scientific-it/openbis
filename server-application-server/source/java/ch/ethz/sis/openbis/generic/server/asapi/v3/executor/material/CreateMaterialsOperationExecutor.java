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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.CreateObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.CreateObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.create.CreateMaterialsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.create.CreateMaterialsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.create.MaterialCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.MaterialPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.create.CreateObjectsOperationExecutor;

/**
 * @author pkupczyk
 */
@Component
public class CreateMaterialsOperationExecutor extends CreateObjectsOperationExecutor<MaterialCreation, MaterialPermId> implements
        ICreateMaterialsOperationExecutor
{

    @Autowired
    private ICreateMaterialExecutor executor;

    @Override
    protected Class<? extends CreateObjectsOperation<MaterialCreation>> getOperationClass()
    {
        return CreateMaterialsOperation.class;
    }

    @Override
    protected CreateObjectsOperationResult<MaterialPermId> doExecute(IOperationContext context, CreateObjectsOperation<MaterialCreation> operation)
    {
        return new CreateMaterialsOperationResult(executor.create(context, operation.getCreations()));
    }

}

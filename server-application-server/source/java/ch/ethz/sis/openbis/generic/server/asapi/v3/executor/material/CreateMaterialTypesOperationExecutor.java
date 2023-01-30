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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.create.CreateMaterialTypesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.create.CreateMaterialTypesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.create.MaterialTypeCreation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.create.CreateObjectsOperationExecutor;

/**
 * @author pkupczyk
 */
@Component
public class CreateMaterialTypesOperationExecutor extends CreateObjectsOperationExecutor<MaterialTypeCreation, EntityTypePermId> implements
        ICreateMaterialTypesOperationExecutor
{

    @Autowired
    private ICreateMaterialTypeExecutor executor;

    @Override
    protected Class<? extends CreateObjectsOperation<MaterialTypeCreation>> getOperationClass()
    {
        return CreateMaterialTypesOperation.class;
    }

    @Override
    protected CreateObjectsOperationResult<EntityTypePermId> doExecute(IOperationContext context,
            CreateObjectsOperation<MaterialTypeCreation> operation)
    {
        return new CreateMaterialTypesOperationResult(executor.create(context, operation.getCreations()));
    }

}

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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.property;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.delete.DeleteObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.delete.DeleteObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.delete.DeletePropertyTypesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.delete.DeletePropertyTypesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.delete.PropertyTypeDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.IPropertyTypeId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.delete.DeleteObjectsOperationExecutor;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class DeletePropertyTypesOperationExecutor
        extends DeleteObjectsOperationExecutor<IPropertyTypeId, PropertyTypeDeletionOptions>
        implements IDeletePropertyTypesOperationExecutor
{
    @Autowired
    private IDeletePropertyTypeExecutor executor;

    @Override
    protected Class<? extends DeleteObjectsOperation<IPropertyTypeId, PropertyTypeDeletionOptions>> getOperationClass()
    {
        return DeletePropertyTypesOperation.class;
    }

    @Override
    protected DeleteObjectsOperationResult doExecute(IOperationContext context,
            DeleteObjectsOperation<IPropertyTypeId, PropertyTypeDeletionOptions> operation)
    {
        executor.delete(context, operation.getObjectIds(), operation.getOptions());
        return new DeletePropertyTypesOperationResult();
    }


}

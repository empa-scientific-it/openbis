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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.space;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.UpdateObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.UpdateObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.update.SpaceUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.update.UpdateSpacesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.update.UpdateSpacesOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.update.UpdateObjectsOperationExecutor;

/**
 * @author pkupczyk
 */
@Component
public class UpdateSpacesOperationExecutor extends UpdateObjectsOperationExecutor<SpaceUpdate, ISpaceId> implements IUpdateSpacesOperationExecutor
{

    @Autowired
    private IUpdateSpaceExecutor executor;

    @Override
    protected Class<? extends UpdateObjectsOperation<SpaceUpdate>> getOperationClass()
    {
        return UpdateSpacesOperation.class;
    }

    @Override
    protected UpdateObjectsOperationResult<? extends ISpaceId> doExecute(IOperationContext context, UpdateObjectsOperation<SpaceUpdate> operation)
    {
        return new UpdateSpacesOperationResult(executor.update(context, operation.getUpdates()));
    }

}

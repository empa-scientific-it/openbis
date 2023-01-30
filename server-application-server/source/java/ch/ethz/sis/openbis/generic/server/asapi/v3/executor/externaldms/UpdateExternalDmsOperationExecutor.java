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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.externaldms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.UpdateObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.UpdateObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.IExternalDmsId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.update.ExternalDmsUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.update.UpdateExternalDmsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.update.UpdateExternalDmsOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.update.UpdateObjectsOperationExecutor;

/**
 * @author pkupczyk
 */
@Component
public class UpdateExternalDmsOperationExecutor extends UpdateObjectsOperationExecutor<ExternalDmsUpdate, IExternalDmsId> implements
        IUpdateExternalDmsOperationExecutor
{

    @Autowired
    private IUpdateExternalDmsExecutor executor;

    @Override
    protected Class<? extends UpdateObjectsOperation<ExternalDmsUpdate>> getOperationClass()
    {
        return UpdateExternalDmsOperation.class;
    }

    @Override
    protected UpdateObjectsOperationResult<? extends IExternalDmsId> doExecute(IOperationContext context,
            UpdateObjectsOperation<ExternalDmsUpdate> operation)
    {
        return new UpdateExternalDmsOperationResult(executor.update(context, operation.getUpdates()));
    }

}

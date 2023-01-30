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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.CreateObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.CreateObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.create.CreateExternalDmsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.create.CreateExternalDmsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.create.ExternalDmsCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.ExternalDmsPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.create.CreateObjectsOperationExecutor;

/**
 * @author anttil
 */
@Component
public class CreateExternalDmsOperationExecutor extends CreateObjectsOperationExecutor<ExternalDmsCreation, ExternalDmsPermId> implements
        ICreateExternalDmsOperationExecutor
{

    @Autowired
    private ICreateExternalDmsExecutor executor;

    @Override
    protected Class<? extends CreateObjectsOperation<ExternalDmsCreation>> getOperationClass()
    {
        return CreateExternalDmsOperation.class;
    }

    @Override
    protected CreateObjectsOperationResult<ExternalDmsPermId> doExecute(IOperationContext context,
            CreateObjectsOperation<ExternalDmsCreation> operation)
    {
        return new CreateExternalDmsOperationResult(executor.create(context, operation.getCreations()));
    }

}

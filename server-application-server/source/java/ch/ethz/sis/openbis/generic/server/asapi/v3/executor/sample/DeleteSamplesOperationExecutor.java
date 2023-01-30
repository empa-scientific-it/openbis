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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.sample;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.delete.DeleteObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.delete.DeleteObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete.DeleteSamplesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete.DeleteSamplesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete.SampleDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.delete.DeleteObjectsOperationExecutor;

/**
 * @author pkupczyk
 */
@Component
public class DeleteSamplesOperationExecutor extends DeleteObjectsOperationExecutor<ISampleId, SampleDeletionOptions> implements
        IDeleteSamplesOperationExecutor
{

    @Autowired
    private IDeleteSampleExecutor executor;

    @Override
    protected Class<? extends DeleteObjectsOperation<ISampleId, SampleDeletionOptions>> getOperationClass()
    {
        return DeleteSamplesOperation.class;
    }

    @Override
    protected DeleteObjectsOperationResult doExecute(IOperationContext context, DeleteObjectsOperation<ISampleId, SampleDeletionOptions> operation)
    {
        return new DeleteSamplesOperationResult(executor.delete(context, operation.getObjectIds(), operation.getOptions()));
    }

}

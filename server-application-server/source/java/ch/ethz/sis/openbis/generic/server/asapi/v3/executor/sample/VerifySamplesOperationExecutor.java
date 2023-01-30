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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.CreateObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.UpdateObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.CreateSamplesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.UpdateSamplesOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.verify.VerifyObjectsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatch;

/**
 * @author pkupczyk
 */
@Component
public class VerifySamplesOperationExecutor extends VerifyObjectsOperationExecutor<ISampleId> implements
        IVerifySamplesOperationExecutor
{

    @Autowired
    private IVerifySampleExecutor executor;

    @Override
    protected Class<? extends CreateObjectsOperationResult<? extends ISampleId>> getCreateObjectsOperationResultClass()
    {
        return CreateSamplesOperationResult.class;
    }

    @Override
    protected Class<? extends UpdateObjectsOperationResult<? extends ISampleId>> getUpdateObjectsOperationResultClass()
    {
        return UpdateSamplesOperationResult.class;
    }

    @Override
    protected void doVerify(IOperationContext context, CollectionBatch<ISampleId> ids)
    {
        executor.verify(context, ids);
    }

}

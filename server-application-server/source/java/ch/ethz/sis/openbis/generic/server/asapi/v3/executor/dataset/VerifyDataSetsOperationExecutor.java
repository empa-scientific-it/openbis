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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.CreateObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.UpdateObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.CreateDataSetsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.UpdateDataSetsOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.verify.VerifyObjectsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatch;

/**
 * @author pkupczyk
 */
@Component
public class VerifyDataSetsOperationExecutor extends VerifyObjectsOperationExecutor<IDataSetId> implements
        IVerifyDataSetsOperationExecutor
{

    @Autowired
    private IVerifyDataSetExecutor executor;

    @Override
    protected Class<? extends CreateObjectsOperationResult<? extends IDataSetId>> getCreateObjectsOperationResultClass()
    {
        return CreateDataSetsOperationResult.class;
    }

    @Override
    protected Class<? extends UpdateObjectsOperationResult<? extends IDataSetId>> getUpdateObjectsOperationResultClass()
    {
        return UpdateDataSetsOperationResult.class;
    }

    @Override
    protected void doVerify(IOperationContext context, CollectionBatch<IDataSetId> ids)
    {
        executor.verify(context, ids);
    }

}

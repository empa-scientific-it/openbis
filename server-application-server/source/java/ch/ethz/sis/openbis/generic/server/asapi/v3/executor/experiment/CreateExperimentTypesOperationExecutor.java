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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.experiment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.CreateObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.CreateObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.CreateExperimentTypesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.CreateExperimentTypesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentTypeCreation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.create.CreateObjectsOperationExecutor;

/**
 * @author pkupczyk
 */
@Component
public class CreateExperimentTypesOperationExecutor extends CreateObjectsOperationExecutor<ExperimentTypeCreation, EntityTypePermId> implements
        ICreateExperimentTypesOperationExecutor
{

    @Autowired
    private ICreateExperimentTypeExecutor executor;

    @Override
    protected Class<? extends CreateObjectsOperation<ExperimentTypeCreation>> getOperationClass()
    {
        return CreateExperimentTypesOperation.class;
    }

    @Override
    protected CreateObjectsOperationResult<EntityTypePermId> doExecute(IOperationContext context,
            CreateObjectsOperation<ExperimentTypeCreation> operation)
    {
        return new CreateExperimentTypesOperationResult(executor.create(context, operation.getCreations()));
    }

}

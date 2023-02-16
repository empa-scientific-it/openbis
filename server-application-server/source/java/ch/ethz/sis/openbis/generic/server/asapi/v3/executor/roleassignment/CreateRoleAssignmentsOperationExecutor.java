/*
 * Copyright ETH 2016 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.roleassignment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.CreateObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.CreateObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.create.CreateRoleAssignmentsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.create.CreateRoleAssignmentsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.create.RoleAssignmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.id.RoleAssignmentTechId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.create.CreateObjectsOperationExecutor;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Component
public class CreateRoleAssignmentsOperationExecutor 
        extends CreateObjectsOperationExecutor<RoleAssignmentCreation, RoleAssignmentTechId> 
        implements ICreateRoleAssignmentsOperationExecutor
{
    @Autowired
    private ICreateRoleAssignmentExecutor executor;

    @Override
    protected Class<? extends CreateObjectsOperation<RoleAssignmentCreation>> getOperationClass()
    {
        return CreateRoleAssignmentsOperation.class;
    }

    @Override
    protected CreateObjectsOperationResult<RoleAssignmentTechId> doExecute(IOperationContext context,
            CreateObjectsOperation<RoleAssignmentCreation> operation)
    {
        return new CreateRoleAssignmentsOperationResult(executor.create(context, operation.getCreations()));
    }

}

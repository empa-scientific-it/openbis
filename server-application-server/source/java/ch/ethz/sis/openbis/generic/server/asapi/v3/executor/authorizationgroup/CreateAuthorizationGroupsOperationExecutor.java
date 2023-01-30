/*
 * Copyright 2017 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.authorizationgroup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.create.AuthorizationGroupCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.create.CreateAuthorizationGroupsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.create.CreateAuthorizationGroupsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.id.AuthorizationGroupPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.CreateObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.CreateObjectsOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.create.CreateObjectsOperationExecutor;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Component
public class CreateAuthorizationGroupsOperationExecutor 
        extends CreateObjectsOperationExecutor<AuthorizationGroupCreation, AuthorizationGroupPermId> 
        implements ICreateAuthorizationGroupsOperationExecutor
{
    @Autowired
    private ICreateAuthorizationGroupExecutor executor;

    @Override
    protected Class<? extends CreateObjectsOperation<AuthorizationGroupCreation>> getOperationClass()
    {
        return CreateAuthorizationGroupsOperation.class;
    }

    @Override
    protected CreateObjectsOperationResult<AuthorizationGroupPermId> doExecute(IOperationContext context,
            CreateObjectsOperation<AuthorizationGroupCreation> operation)
    {
        return new CreateAuthorizationGroupsOperationResult(executor.create(context, operation.getCreations()));
    }
}

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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entity.create.CreatePermIdsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entity.create.CreatePermIdsOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.OperationExecutor;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class CreatePermIdsOperationExecutor
        extends OperationExecutor<CreatePermIdsOperation, CreatePermIdsOperationResult>
        implements ICreatePermIdsOperationExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IEntityAuthorizationExecutor authorizationExecutor;

    @Override
    protected Class<? extends CreatePermIdsOperation> getOperationClass()
    {
        return CreatePermIdsOperation.class;
    }

    @Override
    protected CreatePermIdsOperationResult doExecute(IOperationContext context, CreatePermIdsOperation operation)
    {
        int count = operation.getCount();
        if (count <= 0)
        {
            throw new UserFailureException("Count cannot be <= 0");
        }
        if (count > 100)
        {
            throw new UserFailureException("Cannot create more than 100 ids in one call (" + count + " requested)");
        }

        authorizationExecutor.canCreatePermIds(context);

        return new CreatePermIdsOperationResult(daoFactory.getPermIdDAO().createPermIds(count));
    }

}

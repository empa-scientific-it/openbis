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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entity.create.CreateCodesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entity.create.CreateCodesOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.OperationExecutor;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.EntityCodeGenerator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;

/**
 * @author pkupczyk
 */
@Component
public class CreateCodesOperationExecutor extends OperationExecutor<CreateCodesOperation, CreateCodesOperationResult>
        implements ICreateCodesOperationExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IEntityAuthorizationExecutor authorizationExecutor;

    @Override
    protected Class<? extends CreateCodesOperation> getOperationClass()
    {
        return CreateCodesOperation.class;
    }

    @Override
    protected CreateCodesOperationResult doExecute(IOperationContext context, CreateCodesOperation operation)
    {
        if (operation.getEntityKind() == null)
        {
            throw new UserFailureException("Entity kind cannot be null");
        }
        if (operation.getCount() <= 0)
        {
            throw new UserFailureException("Count cannot be <= 0");
        }

        authorizationExecutor.canCreateCodes(context);

        List<String> codes = new EntityCodeGenerator(daoFactory).generateCodes(operation.getPrefix() != null ? operation.getPrefix() : "",
                ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind.valueOf(operation.getEntityKind().name()), operation.getCount());

        return new CreateCodesOperationResult(codes);
    }

}

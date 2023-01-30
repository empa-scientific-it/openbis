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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.openbis.generic.server.ConcurrentOperation;
import ch.systemsx.cisd.openbis.generic.server.IConcurrentOperationLimiter;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;

/**
 * @author pkupczyk
 */
@Component
public class InternalOperationExecutor implements IInternalOperationExecutor
{

    @Autowired
    private IConcurrentOperationLimiter operationLimiter;

    @Override
    public Map<IOperation, IOperationResult> execute(IOperationContext context, List<? extends IOperation> operations)
    {
        boolean isInstanceAdmin = isInstanceAdmin(context);

        Map<IOperation, IOperationResult> results = new HashMap<IOperation, IOperationResult>();

        for (IOperation operation : operations)
        {
            if (operation instanceof IInternalOperation && isInstanceAdmin)
            {
                IOperationResult result = null;

                if (context.isAsync())
                {
                    operationLimiter.executeLimitedWithTimeoutAsync(operation.getClass().getSimpleName(),
                            new ConcurrentOperation<IOperationResult>()
                            {
                                @Override
                                public IOperationResult execute()
                                {
                                    return ((IInternalOperation) operation).execute();
                                }
                            });
                } else
                {
                    operationLimiter.executeLimitedWithTimeout(operation.getClass().getSimpleName(),
                            new ConcurrentOperation<IOperationResult>()
                            {
                                @Override
                                public IOperationResult execute()
                                {
                                    return ((IInternalOperation) operation).execute();
                                }
                            });
                }

                results.put(operation, result);
            }
        }

        return results;
    }

    private boolean isInstanceAdmin(IOperationContext context)
    {
        Set<RoleAssignmentPE> roles = context.getSession() != null && context.getSession().tryGetPerson() != null ?
                context.getSession().tryGetPerson().getAllPersonRoles() :
                Collections.emptySet();

        for (RoleAssignmentPE role : roles)
        {
            if (RoleCode.ADMIN.equals(role.getRole()) && role.getRoleWithHierarchy().isInstanceLevel())
            {
                return true;
            }
        }

        return false;
    }

}

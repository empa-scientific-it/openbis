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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.CustomASServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.id.CustomASServiceCode;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.id.ICustomASServiceId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.ObjectNotFoundException;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnsupportedObjectIdException;
import ch.ethz.sis.openbis.generic.asapi.v3.plugin.service.ICustomASServiceExecutor;
import ch.ethz.sis.openbis.generic.asapi.v3.plugin.service.context.CustomASServiceContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;

/**
 * @author pkupczyk
 */
@Component
public class ExecuteCustomASServiceExecutor implements IExecuteCustomASServiceExecutor
{

    @Autowired
    private ICustomASServiceProvider serviceProvider;

    @Autowired
    private ICustomASServiceAuthorizationExecutor authorizationExecutor;

    @Override
    public Object execute(IOperationContext context, ICustomASServiceId serviceId, CustomASServiceExecutionOptions options)
    {
        authorizationExecutor.canExecute(context);

        if (serviceId instanceof CustomASServiceCode == false)
        {
            throw new UnsupportedObjectIdException(serviceId);
        }
        CustomASServiceCode serviceCode = (CustomASServiceCode) serviceId;
        ICustomASServiceExecutor serviceExecutor = serviceProvider.tryGetCustomASServiceExecutor(serviceCode.getPermId());
        if (serviceExecutor == null)
        {
            throw new ObjectNotFoundException(serviceId);
        }
        CustomASServiceContext serviceContext = new CustomASServiceContext();
        serviceContext.setSessionToken(context.getSession().getSessionToken());
        return serviceExecutor.executeService(serviceContext, options);
    }

}

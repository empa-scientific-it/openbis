/*
 *  Copyright ETH 2023 Zürich, Scientific IT Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ch.ethz.sis.openbis.generic.server.dssapi.v3.executor;

import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.ObjectNotFoundException;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnsupportedObjectIdException;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.service.CustomDSSServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.service.execute.ExecuteCustomDSSServiceOperationResult;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.service.id.CustomDssServiceCode;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.service.id.ICustomDSSServiceId;
import ch.ethz.sis.openbis.generic.server.dssapi.v3.executor.service.ICustomDSSServiceExecutor;
import ch.ethz.sis.openbis.generic.server.dssapi.v3.executor.service.ICustomDSSServiceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class ExecuteCustomDSSServiceExecutor implements IExecuteCustomDSSServiceExecutor
{

    @Autowired
    private ICustomDSSServiceProvider serviceProvider;

    @Override
    public Serializable execute(String sessionToken, ICustomDSSServiceId serviceId,
            CustomDSSServiceExecutionOptions options)
    {
        if (serviceId instanceof CustomDssServiceCode == false)
        {
            throw new UnsupportedObjectIdException(serviceId);
        }
        CustomDssServiceCode serviceCode = (CustomDssServiceCode) serviceId;
        ICustomDSSServiceExecutor serviceExecutor = serviceProvider.tryGetCustomDSSServiceExecutor(serviceCode.getPermId());
        if (serviceExecutor == null)
        {
            throw new ObjectNotFoundException(serviceId);
        }

        return serviceExecutor.executeService(sessionToken, serviceId, options);
    }

}

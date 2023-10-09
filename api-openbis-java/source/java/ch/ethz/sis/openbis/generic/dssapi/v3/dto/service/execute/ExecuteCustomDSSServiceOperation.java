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

package ch.ethz.sis.openbis.generic.dssapi.v3.dto.service.execute;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.service.CustomDSSServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.service.id.ICustomDSSServiceId;
import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("as.dto.service.execute.ExecuteCustomDSSServiceOperation")
public class ExecuteCustomDSSServiceOperation implements IOperation
{
    private static final long serialVersionUID = 1L;

    private ICustomDSSServiceId serviceId;

    private CustomDSSServiceExecutionOptions options;

    @SuppressWarnings("unused")
    private ExecuteCustomDSSServiceOperation()
    {
    }

    public ExecuteCustomDSSServiceOperation(ICustomDSSServiceId serviceId, CustomDSSServiceExecutionOptions options)
    {
        this.serviceId = serviceId;
        this.options = options;
    }

    public ICustomDSSServiceId getServiceId()
    {
        return serviceId;
    }

    public CustomDSSServiceExecutionOptions getOptions()
    {
        return options;
    }

    @Override
    public String getMessage()
    {
        return toString();
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + (serviceId != null ? " " + serviceId : "");
    }
}

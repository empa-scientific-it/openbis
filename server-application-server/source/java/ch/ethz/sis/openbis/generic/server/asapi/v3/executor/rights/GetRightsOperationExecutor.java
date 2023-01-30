/*
 * Copyright 2019 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.rights;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.rights.Rights;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.rights.get.GetRightsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.rights.get.GetRightsOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.OperationExecutor;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class GetRightsOperationExecutor extends OperationExecutor<GetRightsOperation, GetRightsOperationResult>
        implements IGetRightsOperationExecutor
{
    @Autowired
    private IGetRightsExecutor executor;

    @Override
    protected Class<? extends GetRightsOperation> getOperationClass()
    {
        return GetRightsOperation.class;
    }

    @Override
    protected GetRightsOperationResult doExecute(IOperationContext context, GetRightsOperation operation)
    {
        Map<IObjectId, Rights> map = executor.getRights(context, operation.getObjectIds(), operation.getFetchOptions());
        return new GetRightsOperationResult(map);
    }

}

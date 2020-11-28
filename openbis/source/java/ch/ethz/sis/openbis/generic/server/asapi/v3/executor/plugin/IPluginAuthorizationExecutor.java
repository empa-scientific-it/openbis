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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.plugin;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.IPluginId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.IObjectAuthorizationExecutor;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;

/**
 * @author pkupczyk
 */
public interface IPluginAuthorizationExecutor extends IObjectAuthorizationExecutor
{

    void canGet(IOperationContext context);

    void canCreate(IOperationContext context);

    void canUpdate(IOperationContext context, IPluginId id, ScriptPE entity);

    void canSearch(IOperationContext context);

    void canDelete(IOperationContext context, IPluginId entityId, ScriptPE entity);

    void canEvaluate(IOperationContext context);

}

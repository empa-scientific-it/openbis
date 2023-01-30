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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.plugin;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.get.GetObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.get.GetObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.Plugin;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.fetchoptions.PluginFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.get.GetPluginsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.get.GetPluginsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.IPluginId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.get.GetObjectsPEOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.get.IMapObjectByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.plugin.IPluginTranslator;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class GetPluginsOperationExecutor
        extends GetObjectsPEOperationExecutor<IPluginId, ScriptPE, Plugin, PluginFetchOptions>
        implements IGetPluginsOperationExecutor
{
    @Autowired
    private IMapPluginByIdExecutor mapExecutor;
    
    @Autowired
    private IPluginTranslator translator;

    @Override
    protected IMapObjectByIdExecutor<IPluginId, ScriptPE> getExecutor()
    {
        return mapExecutor;
    }

    @Override
    protected ITranslator<Long, Plugin, PluginFetchOptions> getTranslator()
    {
        return translator;
    }

    @Override
    protected GetObjectsOperationResult<IPluginId, Plugin> getOperationResult(Map<IPluginId, Plugin> objectMap)
    {
        return new GetPluginsOperationResult(objectMap);
    }

    @Override
    protected Class<? extends GetObjectsOperation<IPluginId, PluginFetchOptions>> getOperationClass()
    {
        return GetPluginsOperation.class;
    }
}

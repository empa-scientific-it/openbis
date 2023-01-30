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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.IPluginId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.ObjectNotFoundException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.plugin.IMapPluginByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatchProcessor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.progress.EntityProgress;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Component
public class SetEntityTypeValidationScriptExecutor 
{
    @Autowired
    private IMapPluginByIdExecutor mapPluginByIdExecutor;

    public <T, PE extends EntityTypePE> void setValidationPlugin(IOperationContext context, 
            MapBatch<T, PE> batch, IPluginIdProvider<T> pluginIdProvider, EntityKind entityKind)
    {
        Map<IPluginId, ScriptPE> pluginsById = getPlugins(context, batch, pluginIdProvider);
        new MapBatchProcessor<T, PE>(context, batch)
        {
            @Override
            public void process(T key, PE value)
            {
                IPluginId validationPluginId = pluginIdProvider.getPluginId(key);
                if (pluginIdProvider.isModified(key))
                {
                    ScriptPE pluginPE = null;
                    if (validationPluginId != null)
                    {
                        pluginPE = pluginsById.get(validationPluginId);
                        if (pluginPE == null)
                        {
                            throw new ObjectNotFoundException(validationPluginId);
                        }
                        checkScriptType(pluginPE, validationPluginId);
                        checkEntityKind(pluginPE, entityKind);
                    }
                    value.setValidationScript(pluginPE);
                }
            }

            @Override
            public IProgress createProgress(T key, PE value, int objectIndex, int totalObjectCount)
            {
                return new EntityProgress("setting validation script", value, objectIndex, totalObjectCount);
            }
            
        };
    }

    private <T, PE extends EntityTypePE> Map<IPluginId, ScriptPE> getPlugins(IOperationContext context, 
            MapBatch<T, PE> batch, IPluginIdProvider<T> pluginIdProvider)
    {
        Set<IPluginId> pluginIds = new HashSet<>();
        new MapBatchProcessor<T, PE>(context, batch)
            {
                @Override
                public void process(T key, PE value)
                {
                    IPluginId validationPluginId = pluginIdProvider.getPluginId(key);
                    if (validationPluginId != null)
                    {
                        pluginIds.add(validationPluginId);
                    }
                }

                @Override
                public IProgress createProgress(T key, PE value, int objectIndex, int totalObjectCount)
                {
                    return new EntityProgress("getting validation script", value, objectIndex, totalObjectCount);
                }
            };
        return mapPluginByIdExecutor.map(context, pluginIds);
    }

    private void checkScriptType(ScriptPE pluginPE, IPluginId validationPluginId)
    {
        if (false == ScriptType.ENTITY_VALIDATION.equals(pluginPE.getScriptType()))
        {
            throw new UserFailureException("Entity type validation plugin has to be of type '"
                    + ScriptType.ENTITY_VALIDATION + "'. The specified plugin with id '"
                    + validationPluginId + "' is of type '" + pluginPE.getScriptType() + "'.");
        }
    }

    private void checkEntityKind(ScriptPE pluginPE, EntityKind entityKind)
    {
        if (pluginPE.getEntityKind() != null && false == pluginPE.getEntityKind().equals(entityKind))
        {
            throw new UserFailureException("Entity type validation plugin has entity kind set to '"
                    + pluginPE.getEntityKind() + "'. Expected a plugin where entity kind is either '"
                    + entityKind + "' or null.");
        }
    }

}

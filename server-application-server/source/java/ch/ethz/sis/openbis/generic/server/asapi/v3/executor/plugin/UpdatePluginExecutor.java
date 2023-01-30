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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.FieldUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.IPluginId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.PluginPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.update.PluginUpdate;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractUpdateEntityExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatchProcessor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.progress.UpdateRelationProgress;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.util.PluginUtils;
import ch.systemsx.cisd.openbis.generic.shared.IJythonEvaluatorPool;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class UpdatePluginExecutor
        extends AbstractUpdateEntityExecutor<PluginUpdate, ScriptPE, IPluginId, PluginPermId>
        implements IUpdatePluginExecutor
{
    @Resource(name = ComponentNames.JYTHON_EVALUATOR_POOL)
    private IJythonEvaluatorPool jythonEvaluatorPool;
    
    @Autowired
    private IDAOFactory daoFactory;
    
    @Autowired
    private IMapPluginByIdExecutor mapPluginByIdExecutor;
    
    @Autowired
    private IPluginAuthorizationExecutor authorizationExecutor;

    @Override
    protected IPluginId getId(PluginUpdate update)
    {
        return update.getPluginId();
    }

    @Override
    protected PluginPermId getPermId(ScriptPE entity)
    {
        return new PluginPermId(entity.getName());
    }

    @Override
    protected void checkData(IOperationContext context, PluginUpdate update)
    {
        if (update.getPluginId() == null)
        {
            throw new UserFailureException("Plugin id cannot be null.");
        }
        if (update.getScript().isModified() && StringUtils.isBlank(update.getScript().getValue()))
        {
            throw new UserFailureException("New script cannot be undefined.");
        }
    }

    @Override
    protected void checkAccess(IOperationContext context, IPluginId id, ScriptPE entity)
    {
        authorizationExecutor.canUpdate(context, id, entity);
    }

    @Override
    protected void updateBatch(IOperationContext context, MapBatch<PluginUpdate, ScriptPE> batch)
    {
        new MapBatchProcessor<PluginUpdate, ScriptPE>(context, batch)
        {
            @Override
            public void process(PluginUpdate update, ScriptPE script)
            {
                script.setDescription(getNewValue(update.getDescription(), script.getDescription()));
                FieldUpdateValue<String> scriptField = update.getScript();
                if (scriptField != null && scriptField.isModified())
                {
                    script.setScript(scriptField.getValue());
                    PluginUtils.checkScriptCompilation(script, jythonEvaluatorPool);
                }
                script.setAvailable(getNewValue(update.getAvailable(), script.isAvailable()));
            }

            @Override
            public IProgress createProgress(PluginUpdate update, ScriptPE script, int objectIndex, int totalObjectCount)
            {
                return new UpdateRelationProgress(update, script, "plugin", objectIndex, totalObjectCount);
            }
        };
    }

    @Override
    protected void updateAll(IOperationContext context, MapBatch<PluginUpdate, ScriptPE> batch)
    {
        new MapBatchProcessor<PluginUpdate, ScriptPE>(context, batch)
        {
            @Override
            public void process(PluginUpdate update, ScriptPE script)
            {
                FieldUpdateValue<String> scriptField = update.getScript();
                if (scriptField != null && scriptField.isModified() && script.getScriptType() == ScriptType.DYNAMIC_PROPERTY)
                {
                    for (EntityTypePropertyTypePE assignment : script.getPropertyAssignments())
                    {
                        daoFactory.getEntityPropertyTypeDAO(assignment.getEntityType().getEntityKind())
                                .scheduleDynamicPropertiesEvaluation(assignment);
                    }
                }
            }

            @Override
            public IProgress createProgress(PluginUpdate update, ScriptPE script, int objectIndex, int totalObjectCount)
            {
                return new UpdateRelationProgress(update, script, "plugin (scheduling dynamic properties evaluation)", 
                        objectIndex, totalObjectCount);
            }
        };
    }

    @Override
    protected Map<IPluginId, ScriptPE> map(IOperationContext context, Collection<IPluginId> ids)
    {
        return mapPluginByIdExecutor.map(context, ids);
    }

    @Override
    protected List<ScriptPE> list(IOperationContext context, Collection<Long> ids)
    {
        return daoFactory.getScriptDAO().listAllEntities();
    }

    @Override
    protected void save(IOperationContext context, List<ScriptPE> entities, boolean clearCache)
    {
        for (ScriptPE script : entities)
        {
            daoFactory.getScriptDAO().validateAndSaveUpdatedEntity(script);
        }
    }

    @Override
    protected void handleException(DataAccessException e)
    {
        DataAccessExceptionTranslator.throwException(e, "plugin", null);
    }

}

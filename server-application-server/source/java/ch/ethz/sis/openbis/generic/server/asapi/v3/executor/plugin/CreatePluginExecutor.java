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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.PluginType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.create.PluginCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.PluginPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractCreateEntityExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatchProcessor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.progress.CreateProgress;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.util.PluginUtils;
import ch.systemsx.cisd.openbis.generic.shared.IJythonEvaluatorPool;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class CreatePluginExecutor
        extends AbstractCreateEntityExecutor<PluginCreation, ScriptPE, PluginPermId>
        implements ICreatePluginExecutor
{
    @Resource(name = ComponentNames.JYTHON_EVALUATOR_POOL)
    private IJythonEvaluatorPool evaluatorPool;
    
    @Autowired
    private IDAOFactory daoFactory;
    
    @Autowired
    private IPluginAuthorizationExecutor authorizationExecutor;

    @Override
    protected IObjectId getId(ScriptPE entity)
    {
        return new PluginPermId(entity.getName());
    }

    @Override
    protected PluginPermId createPermId(IOperationContext context, ScriptPE entity)
    {
        return new PluginPermId(entity.getName());
    }
    
    @Override
    protected void checkData(IOperationContext context, PluginCreation creation)
    {
        if (StringUtils.isEmpty(creation.getName()))
        {
            throw new UserFailureException("Name cannot be empty.");
        }
        if (creation.getPluginType() == null)
        {
            throw new UserFailureException("Plugin type cannot be unspecified.");
        }
        if (StringUtils.isEmpty(creation.getScript()))
        {
            throw new UserFailureException("Script cannot be empty.");
        }
    }

    @Override
    protected void checkAccess(IOperationContext context)
    {
        authorizationExecutor.canCreate(context);
    }

    @Override
    protected void checkAccess(IOperationContext context, ScriptPE entity)
    {
    }

    @Override
    protected List<ScriptPE> createEntities(IOperationContext context, CollectionBatch<PluginCreation> batch)
    {
        List<ScriptPE> scripts = new ArrayList<>();
        PersonPE person = context.getSession().tryGetPerson();
        new CollectionBatchProcessor<PluginCreation>(context, batch)
        {
            @Override
            public void process(PluginCreation creation)
            {
                ScriptPE script = new ScriptPE();
                script.setName(creation.getName());
                script.setDescription(creation.getDescription());
                EntityKind entityKind = creation.getEntityKind();
                if (entityKind != null)
                {
                    script.setEntityKind(translate(entityKind));
                }
                if (creation.getPluginType() != null)
                {
                    script.setScriptType(translate(creation.getPluginType()));
                }
                script.setPluginType(ch.systemsx.cisd.openbis.generic.shared.basic.dto.PluginType.JYTHON);
                script.setScript(creation.getScript());
                PluginUtils.checkScriptCompilation(script, evaluatorPool);
                script.setAvailable(creation.isAvailable());
                script.setRegistrator(person);
                scripts.add(script);
            }

            @Override
            public IProgress createProgress(PluginCreation object, int objectIndex, int totalObjectCount)
            {
                return new CreateProgress(object, objectIndex, totalObjectCount);
            }
        };
        return scripts;
    }
    
    private ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind translate(EntityKind entityKind)
    {
        return ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind.valueOf(entityKind.name());
    }

    private ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType translate(PluginType scriptType)
    {
        return ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType.valueOf(scriptType.name());
    }

    @Override
    protected void updateBatch(IOperationContext context, MapBatch<PluginCreation, ScriptPE> batch)
    {
    }

    @Override
    protected void updateAll(IOperationContext context, MapBatch<PluginCreation, ScriptPE> batch)
    {
    }

    @Override
    protected List<ScriptPE> list(IOperationContext context, Collection<Long> ids)
    {
        return daoFactory.getScriptDAO().listAllEntities();
    }

    @Override
    protected void save(IOperationContext context, List<ScriptPE> scripts, boolean clearCache)
    {
        for (ScriptPE script : scripts)
        {
            daoFactory.getScriptDAO().createOrUpdate(script);
        }
    }

    @Override
    protected void handleException(DataAccessException e)
    {
        DataAccessExceptionTranslator.throwException(e, "plugin", null);
    }

}

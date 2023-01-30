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
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.delete.PluginDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.IPluginId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractDeleteEntityExecutor;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.IHotDeploymentController;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IScriptDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.entity_validation.IEntityValidatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PluginType;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class DeletePluginExecutor
        extends AbstractDeleteEntityExecutor<Void, IPluginId, ScriptPE, PluginDeletionOptions>
        implements IDeletePluginExecutor
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, DeletePluginExecutor.class);

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IPluginAuthorizationExecutor authorizationExecutor;

    @Autowired
    private IMapPluginByIdExecutor mapPluginByIdExecutor;

    @Resource(name = "entity-validation-factory")
    private IEntityValidatorFactory entityValidationFactory;

    @Override
    protected Map<IPluginId, ScriptPE> map(IOperationContext context, List<? extends IPluginId> entityIds, PluginDeletionOptions deletionOptions)
    {
        return mapPluginByIdExecutor.map(context, entityIds);
    }

    @Override
    protected void checkAccess(IOperationContext context, IPluginId entityId, ScriptPE entity)
    {
        authorizationExecutor.canDelete(context, entityId, entity);
    }

    @Override
    protected void updateModificationDateAndModifier(IOperationContext context, ScriptPE entity)
    {
    }

    @Override
    protected Void delete(IOperationContext context, Collection<ScriptPE> entities, PluginDeletionOptions deletionOptions)
    {
        IScriptDAO scriptDAO = daoFactory.getScriptDAO();
        List<String> namesOfPredeployedPlugins = new ArrayList<String>();
        for (ScriptPE script : entities)
        {
            if (script.getPluginType() == PluginType.PREDEPLOYED)
            {
                namesOfPredeployedPlugins.add(script.getName());
            }
            scriptDAO.delete(script);
        }
        if (namesOfPredeployedPlugins.isEmpty() == false)
        {
            IHotDeploymentController hotDeploymentController =
                    entityValidationFactory.getHotDeploymentController();
            if (hotDeploymentController == null)
            {
                operationLog.warn("Can not disable pre-deployed plugins because of missing controller.");
            } else
            {
                for (String name : namesOfPredeployedPlugins)
                {
                    hotDeploymentController.disablePlugin(name);
                }
            }
        }
        return null;
    }

}

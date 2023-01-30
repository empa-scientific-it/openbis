/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.plugin;

import java.util.ArrayList;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.PluginPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.AbstractListObjectById;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IScriptDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;

/**
 * @author pkupczyk
 */
public class ListPluginByPermId extends AbstractListObjectById<PluginPermId, ScriptPE>
{

    private IScriptDAO scriptDAO;

    public ListPluginByPermId(IScriptDAO scriptDAO)
    {
        this.scriptDAO = scriptDAO;
    }

    @Override
    public Class<PluginPermId> getIdClass()
    {
        return PluginPermId.class;
    }

    @Override
    public PluginPermId createId(ScriptPE plugin)
    {
        return new PluginPermId(plugin.getName());
    }

    @Override
    public List<ScriptPE> listByIds(IOperationContext context, List<PluginPermId> ids)
    {
        List<ScriptPE> plugins = new ArrayList<ScriptPE>();

        for (PluginPermId id : ids)
        {
            ScriptPE plugin = scriptDAO.tryFindByName(id.getPermId());
            if (plugin != null)
            {
                plugins.add(plugin);
            }
        }

        return plugins;
    }

}

/*
 * Copyright 2011 ETH Zuerich, CISD

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

package ch.systemsx.cisd.openbis.generic.shared.translator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CorePlugin;
import ch.systemsx.cisd.openbis.generic.shared.dto.CorePluginPE;

/**
 * @author Kaloyan Enimanev
 */
public final class CorePluginTranslator
{
    public final static List<CorePlugin> translate(Collection<CorePluginPE> pluginPEs)
    {
        List<CorePlugin> result = new ArrayList<CorePlugin>();
        for (CorePluginPE pluginPE : pluginPEs)
        {
            result.add(translate(pluginPE));
        }
        return result;
    }

    public final static CorePlugin translate(final CorePluginPE pluginPE)
    {
        return new CorePlugin(pluginPE.getName(), pluginPE.getVersion());
    }

    public final static CorePluginPE translate(final CorePlugin plugin, String masterDataScript)
    {
        CorePluginPE pluginPE = new CorePluginPE();
        pluginPE.setName(plugin.getName());
        pluginPE.setVersion(plugin.getVersion());
        pluginPE.setMasterDataRegistrationScript(masterDataScript);
        return pluginPE;
    }

    private CorePluginTranslator()
    {
        // Can not be instantiated.
    }

}

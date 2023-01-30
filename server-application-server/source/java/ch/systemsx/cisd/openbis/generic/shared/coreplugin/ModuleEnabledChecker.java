/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.coreplugin;

import static ch.systemsx.cisd.openbis.generic.shared.coreplugin.CorePluginsInjector.INITIALIZE_MASTER_DATA_CORE_PLUGIN_NAME;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import ch.systemsx.cisd.common.collection.IKeyExtractor;
import ch.systemsx.cisd.common.collection.TableMap;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CorePlugin;

/**
 * Helper class for checking enabled module.
 * 
 * @author Franz-Josef Elmer
 */
public class ModuleEnabledChecker
{
    private static final IKeyExtractor<String, CorePlugin> CORE_PLUGIN_NAME_EXTRACTOR = new IKeyExtractor<String, CorePlugin>()
        {
            @Override
            public String getKey(CorePlugin corePlugin)
            {
                return corePlugin.getName();
            }
        };

    private final List<Pattern> enabledModulesPatterns;

    public ModuleEnabledChecker(Properties properties, String key)
    {
        this(PropertyUtils.getList(properties, key));
    }

    public ModuleEnabledChecker(List<String> moduleRegExs)
    {
        enabledModulesPatterns = new ArrayList<Pattern>();
        for (String regex : moduleRegExs)
        {
            try
            {
                enabledModulesPatterns.add(Pattern.compile(regex));
            } catch (PatternSyntaxException ex)
            {
                throw new ConfigurationFailureException("Invalid regular expression: "
                        + ex.getMessage());
            }
        }
    }

    public Set<CorePlugin> getModuleWithEnabledMasterDataInitializations(List<CorePlugin> corePlugins)
    {
        Set<CorePlugin> result = new LinkedHashSet<>();
        TableMap<String, CorePlugin> pluginsByName = new TableMap<>(corePlugins, CORE_PLUGIN_NAME_EXTRACTOR);
        for (String enabledPlugin : getEnabledPlugins(corePlugins))
        {
            CorePlugin plugin = getPluginByFullRequiredPluginName(pluginsByName, enabledPlugin, null);
            String name = plugin.getName();
            if (enabledPlugin.equals(name) 
                    || enabledPlugin.equals(name + ":" + INITIALIZE_MASTER_DATA_CORE_PLUGIN_NAME))
            {
                result.add(plugin);
            }
        }
        return result;
    }
    
    Set<String> getEnabledPlugins(List<CorePlugin> corePlugins)
    {
        TableMap<String, CorePlugin> pluginsByName = new TableMap<>(corePlugins, CORE_PLUGIN_NAME_EXTRACTOR);
        Set<String> result = new LinkedHashSet<>();
        for (CorePlugin corePlugin : getListOfEnabledPlugins(corePlugins))
        {
            addRequiredPlugins(result, new HashSet<CorePlugin>(), pluginsByName, corePlugin);
            result.add(corePlugin.getName());
        }
        return result;
    }

    private void addRequiredPlugins(Set<String> result, Set<CorePlugin> visitedPlugins, 
            TableMap<String, CorePlugin> pluginsByName, CorePlugin corePlugin)
    {
        if (visitedPlugins.contains(corePlugin))
        {
            return;
        }
        visitedPlugins.add(corePlugin);
        for (String requiredPlugin : corePlugin.getRequiredPlugins())
        {
            CorePlugin referredPlugin = getPluginByFullRequiredPluginName(pluginsByName, requiredPlugin, corePlugin);
            addRequiredPlugins(result, visitedPlugins, pluginsByName, referredPlugin);
            result.add(requiredPlugin);
        }
    }

    private CorePlugin getPluginByFullRequiredPluginName(TableMap<String, CorePlugin> pluginsByName, 
            String requiredPlugin, CorePlugin corePlugin)
    {
        FullPluginName fullPluginName = new FullPluginName(requiredPlugin);
        String moduleName = fullPluginName.getModule();
        CorePlugin referredPlugin = pluginsByName.tryGet(moduleName);
        if (referredPlugin == null)
        {
            throw new ConfigurationFailureException("Required plugin '" + requiredPlugin 
                    + "' specified by core plugin '" + corePlugin + "' refers to the unknown module '" 
                    + moduleName + "'." );
        }
        return referredPlugin;
    }
    
    List<CorePlugin> getListOfEnabledPlugins(List<CorePlugin> corePlugins)
    {
        ArrayList<CorePlugin> result = new ArrayList<>();

        ArrayList<String> codes = new ArrayList<>();
        HashMap<String, CorePlugin> codeToPlugin = new HashMap<>();
        for (CorePlugin plugin : corePlugins)
        {
            String name = plugin.getName();
            codes.add(name);
            codeToPlugin.put(name, plugin);
        }
        for (String enabledPluginCode : getListOfEnabledModules(codes))
        {
            result.add(codeToPlugin.get(enabledPluginCode));
        }
        return result;
    }
    
    public List<String> getListOfEnabledModules(List<String> moduleNames)
    {
        List<String> remainingModules = new LinkedList<>(moduleNames);
        List<String> modulesInTheRightOrder = new ArrayList<>();
        for (Pattern pattern : enabledModulesPatterns)
        {
            Iterator<String> it = remainingModules.iterator();
            while (it.hasNext())
            {
                String module = it.next();
                if (pattern.matcher(module).matches())
                {
                    modulesInTheRightOrder.add(module);
                    it.remove();
                }
            }
        }
        return modulesInTheRightOrder;
    }

    public boolean isModuleEnabled(String module)
    {
        for (Pattern pattern : enabledModulesPatterns)
        {
            if (pattern.matcher(module).matches())
            {
                return true;
            }
        }
        return false;
    }
    
    private static final class FullPluginName
    {
        private final String module;
        private final String pluginType;
        private final String name;
        
        FullPluginName(String nameAsString)
        {
            String[] splittedName = nameAsString.split(":");
            module = splittedName[0];
            if (splittedName.length > 1)
            {
                pluginType = splittedName[1];
                if (splittedName.length > 2)
                {
                    name = splittedName[2];
                } else
                {
                    name = null;
                }
            } else
            {
                pluginType = null;
                name = null;
            }
        }

        public String getModule()
        {
            return module;
        }

        @Override
        public String toString()
        {
            StringBuilder builder = new StringBuilder(module);
            if (pluginType != null)
            {
                builder.append(':').append(pluginType);
                if (name != null)
                {
                    builder.append(':').append(name);
                }
            }
            return builder.toString();
        }
        
    }
}

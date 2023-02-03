/*
 * Copyright 2020 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.installer.izpack;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.PanelActionConfiguration;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.data.PanelAction;

/**
 * @author Franz-Josef Elmer
 */
public class UnsupportedModulesCheckAction implements PanelAction
{
    private static final String[] UNSUPPORTED_MODULES = { "screening", "proteomics" };

    @Override
    public void executeAction(AutomatedInstallData data, AbstractUIHandler handler)
    {
        List<Pattern> enabledModulesPatterns = getEnabledModulesPattern();
        List<String> enabledUnsupportedModules = new ArrayList<>();
        for (String unsupportedModule : UNSUPPORTED_MODULES)
        {
            if (isEnabled(enabledModulesPatterns, unsupportedModule))
            {
                enabledUnsupportedModules.add(unsupportedModule);
            }
        }
        if (enabledUnsupportedModules.isEmpty() == false)
        {
            String message = "The following modules are no longer supported: "
                    + enabledUnsupportedModules + "\nThey were deprecated and have been removed.\n"
                    + "Please follow up evaluating if you need these modules and take an informed decision\n"
                    + "before disabling them because you may loose access to data and functionality.";
            if (handler == null)
            {
                System.err.println(message);
                System.exit(1);
            }
            handler.emitErrorAndBlockNext("Unsupported Module", message);
        }
    }

    private List<Pattern> getEnabledModulesPattern()
    {
        List<Pattern> enabledModulesPatterns = new ArrayList<Pattern>();
        String modules = Utils.tryToGetCorePluginsProperty(GlobalInstallationContext.installDir,
                SetTechnologyCheckBoxesAction.ENABLED_TECHNOLOGIES_KEY);
        if (modules != null)
        {
            for (String module : modules.split(","))
            {
                try
                {
                    enabledModulesPatterns.add(Pattern.compile(module.trim()));
                } catch (PatternSyntaxException ex)
                {
                    // Ignore invalid regex patterns silently
                }
            }
        }
        return enabledModulesPatterns;
    }

    private boolean isEnabled(List<Pattern> enabledModulesPatterns, String module)
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

    @Override
    public void initialize(PanelActionConfiguration arg0)
    {
    }

}

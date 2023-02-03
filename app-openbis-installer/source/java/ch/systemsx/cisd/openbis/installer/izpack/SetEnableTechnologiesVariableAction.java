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

package ch.systemsx.cisd.openbis.installer.izpack;

import static ch.systemsx.cisd.openbis.installer.izpack.SetTechnologyCheckBoxesAction.ENABLED_TECHNOLOGIES_KEY;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.PanelActionConfiguration;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.data.PanelAction;

import ch.systemsx.cisd.common.shared.basic.string.CommaSeparatedListBuilder;

/**
 * Action which sets the variable <code>ENABLED_TECHNOLOGIES_VARNAME</code> or updates service.properties of AS.
 * 
 * @author Franz-Josef Elmer
 */
public class SetEnableTechnologiesVariableAction implements PanelAction
{
    static final String[] MODULES = { "monitoring-support", "dropbox-monitor", "dataset-uploader", 
            "dataset-file-search", "xls-import", "search-store", "openbis-sync", "openbis-ng-ui" };

    static final String ENABLED_TECHNOLOGIES_VARNAME = "ENABLED_TECHNOLOGIES";

    private final SetTechnologyCheckBoxesAction technologyCheckBoxesAction = new SetTechnologyCheckBoxesAction();

    @Override
    public void initialize(PanelActionConfiguration configuration)
    {
    }

    @Override
    public void executeAction(AutomatedInstallData data, AbstractUIHandler handler)
    {
        // The technologyCheckBoxesAction is a 'preactivate' action to populate the technology check
        // boxes.
        // But in case of console installation the 'preactivate' action isn't executed. We can
        // execute it here without any check whether this is a console-based or GUI-based
        // installation
        // because the GUI sets the technology flags and nothing will be populated from existing
        // service.properties.
        technologyCheckBoxesAction.executeAction(data, handler);
        boolean isFirstTimeInstallation = GlobalInstallationContext.isFirstTimeInstallation;
        File installDir = GlobalInstallationContext.installDir;
        updateEnabledTechnologyProperty(data, isFirstTimeInstallation, installDir);
    }

    void updateEnabledTechnologyProperty(AutomatedInstallData data,
            boolean isFirstTimeInstallation, File installDir)
    {
        String newTechnologyList = createListOfEnabledTechnologies(data);
        data.setVariable(ENABLED_TECHNOLOGIES_VARNAME, newTechnologyList);
        File corePluginPropertiesFile = new File(installDir, Utils.CORE_PLUGINS_PROPERTIES_PATH);
        corePluginPropertiesFile.getParentFile().mkdirs();
        modifyPropertyEnabledTechnologies(corePluginPropertiesFile, data);
    }

    private String createListOfEnabledTechnologies(AutomatedInstallData data)
    {
        CommaSeparatedListBuilder builder = new CommaSeparatedListBuilder();
        for (String technology : GlobalInstallationContext.TECHNOLOGIES)
        {
            String technologyFlag = data.getVariable(technology);
            if (Boolean.TRUE.toString().equalsIgnoreCase(technologyFlag))
            {
                builder.append(technology.toLowerCase());
            }
        }
        return builder.toString();
    }

    private void modifyPropertyEnabledTechnologies(File configFile, AutomatedInstallData data)
    {
        Set<String> allTechnologies = new HashSet<String>();
        Set<String> enabledModules = new LinkedHashSet<>();
        for (String module : MODULES)
        {
            enabledModules.add(module);
            allTechnologies.add(module);
        }

        for (String technology : GlobalInstallationContext.TECHNOLOGIES)
        {
            String lowerCasedTechnology = technology.toLowerCase();
            allTechnologies.add(lowerCasedTechnology);
            String technologyFlag = data.getVariable(technology);
            if (Boolean.TRUE.toString().equalsIgnoreCase(technologyFlag))
            {
                enabledModules.add(lowerCasedTechnology);

                if (technology == GlobalInstallationContext.TECHNOLOGY_MICROSCOPY ||
                        technology == GlobalInstallationContext.TECHNOLOGY_FLOW_CYTOMETRY)
                {
                    String lowerCasedTechnologyShared = GlobalInstallationContext.TECHNOLOGY_SHARED_MICROSCOPY_FLOW_CYTOMETRY.toLowerCase();
                    enabledModules.add(lowerCasedTechnologyShared);
                    allTechnologies.add(lowerCasedTechnologyShared);
                }
            }
        }

        Properties properties = Utils.tryToGetProperties(configFile);
        if (properties != null)
        {
            String property = properties.getProperty(ENABLED_TECHNOLOGIES_KEY);
            if (property != null && property.trim().length() > 0)
            {
                String[] splittedProperty = property.split(",");
                for (String term : splittedProperty)
                {
                    String trimmedTerm = term.trim();
                    if (allTechnologies.contains(trimmedTerm) == false)
                    {
                        enabledModules.add(trimmedTerm);
                    }
                }
            }
        }
        Utils.updateOrAppendProperty(configFile, ENABLED_TECHNOLOGIES_KEY, String.join(", ", enabledModules));
    }

}

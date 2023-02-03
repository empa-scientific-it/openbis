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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.PanelActionConfiguration;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.data.PanelAction;

/**
 * Action which sets the variables which are the values of check boxes on the technology page. If the variable is already set nothing is done.
 * Otherwise the behavior depends on whether this is installation or upgrading. In case of installation the flag will be <code>false</code>. In case
 * of upgrading <code>service.properties</code> file of AS is scanned in order to check whether a certain technology is enabled or not.
 * 
 * @author Franz-Josef Elmer
 */
public class SetTechnologyCheckBoxesAction implements PanelAction
{
    static final String ENABLED_TECHNOLOGIES_KEY_LEGACY = "enabled-technologies";

    static final String ENABLED_TECHNOLOGIES_KEY = "enabled-modules";

    private static interface ITechnologyChecker
    {
        String getTechnologyName();

        boolean isTechnologyEnabled(File installDir);
    }

    private static final class SimpleTechnologyChecker implements ITechnologyChecker
    {

        private final String technologyName;

        SimpleTechnologyChecker(String technologyName)
        {
            this.technologyName = technologyName;
        }

        @Override
        public String getTechnologyName()
        {
            return technologyName;
        }

        @Override
        public boolean isTechnologyEnabled(File installDir)
        {
            String technologies =
                    Utils.tryToGetCorePluginsProperty(installDir, ENABLED_TECHNOLOGIES_KEY);
            if (technologies == null)
            {
                technologies =
                        Utils.tryToGetCorePluginsProperty(installDir,
                                ENABLED_TECHNOLOGIES_KEY_LEGACY);
            }
            if (technologies == null)
            {
                technologies =
                        Utils.tryToGetServicePropertyOfAS(installDir, ENABLED_TECHNOLOGIES_KEY);
            }
            if (technologies == null)
            {
                technologies =
                        Utils.tryToGetServicePropertyOfAS(installDir,
                                ENABLED_TECHNOLOGIES_KEY_LEGACY);
            }
            if (technologies != null)
            {
                return technologies.contains(technologyName.toLowerCase());
            }
            return false;
        }
    }

    private final Map<String, ITechnologyChecker> technologyCheckers =
            new HashMap<String, ITechnologyChecker>();

    public SetTechnologyCheckBoxesAction()
    {
        for (String technology : GlobalInstallationContext.TECHNOLOGIES)
        {
            registerTechnologyChecker(new SimpleTechnologyChecker(technology));
        }
    }

    private void registerTechnologyChecker(ITechnologyChecker checker)
    {
        technologyCheckers.put(checker.getTechnologyName(), checker);
    }

    @Override
    public void initialize(PanelActionConfiguration configuration)
    {
    }

    @Override
    public void executeAction(AutomatedInstallData data, AbstractUIHandler handler)
    {
        for (String technology : GlobalInstallationContext.TECHNOLOGIES)
        {
            String variable = data.getVariable(technology);
            if (variable == null)
            {
                boolean technologyFlag = false;
                if (GlobalInstallationContext.isUpdateInstallation)
                {
                    technologyFlag =
                            isTechnologyEnabled(GlobalInstallationContext.installDir, technology);
                }
                data.setVariable(technology, Boolean.toString(technologyFlag));
            }
        }
    }

    boolean isTechnologyEnabled(File installDir, String technology)
    {
        ITechnologyChecker technologyChecker = technologyCheckers.get(technology);
        if (technologyChecker == null)
        {
            return false;
        }
        return technologyChecker.isTechnologyEnabled(installDir);
    }
}

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

package ch.systemsx.cisd.openbis.installer.izpack;

import java.util.HashMap;
import java.util.Map;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.PanelActionConfiguration;

/**
 * Executes a script that creates a backup of the existing installation.
 * 
 * @author Kaloyan Enimanev
 */
public class ExecuteBackupAction extends AbstractScriptExecutor
{
    /**
     * a script that creates a installation backup.
     */
    private static final String CREATE_BACKUP_SCRIPT = "backup-installation.sh";

    @Override
    public synchronized void executeAction(AutomatedInstallData data)
    {
        String script = getAdminScript(data, CREATE_BACKUP_SCRIPT);
        String backupFolder = data.getVariable(GlobalInstallationContext.BACKUP_FOLDER_VARNAME);
        String dataBasesToBackup = data.getVariable(SetDatabasesToBackupAction.DATABASES_TO_BACKUP_VARNAME);
        String console = data.getVariable("SYSTEM_CONSOLE");

        String password =
                Utils.tryToGetServicePropertyOfAS(GlobalInstallationContext.installDir,
                        "database.owner-password");

        Map<String, String> env = new HashMap<String, String>();
        env.put("PGPASSWORD", password == null ? "" : password);

        if (dataBasesToBackup == null)
        {
            if (console == null)
            {
                executeAdminScript(env, script, backupFolder);
            } else
            {
                executeAdminScript(env, script, backupFolder, "", console);
            }
        } else
        {
            if (console == null)
            {
                executeAdminScript(env, script, backupFolder, dataBasesToBackup);
            } else
            {
                executeAdminScript(env, script, backupFolder, dataBasesToBackup, console);
            }
        }
    }

    @Override
    public void initialize(PanelActionConfiguration arg0)
    {
    }

}

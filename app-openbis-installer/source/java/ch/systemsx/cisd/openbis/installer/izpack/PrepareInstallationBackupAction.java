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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.PanelActionConfiguration;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.data.PanelAction;

/**
 * An action that creates a backup folder and sets a global variable with the folder's name.
 * 
 * @author Kaloyan Enimanev
 */
public class PrepareInstallationBackupAction implements PanelAction
{
    private static final String BACK_FOLDER_PATTERN = "yyMMdd-HHmm";

    private static final String BACKUP_ROOT = "backup";

    private File installDir;

    private File backupDir;

    @Override
    public synchronized void executeAction(AutomatedInstallData data, AbstractUIHandler arg1)
    {
        if (GlobalInstallationContext.isUpdateInstallation)
        {
            installDir = GlobalInstallationContext.installDir;
            backupDir = generateUniqueBackupDirName();

            setInstallationWideBackupDirVariable(data);
        }
    }

    @Override
    public void initialize(PanelActionConfiguration arg0)
    {
    }

    /**
     * sets a global variable in the installation context that will be visible by other panels/executables (or any kind of collaborators) in the
     * installation process.
     */
    private String setInstallationWideBackupDirVariable(AutomatedInstallData data)
    {
        String backupDirPath = backupDir.getAbsolutePath();
        data.setVariable(GlobalInstallationContext.BACKUP_FOLDER_VARNAME, backupDirPath);
        return backupDirPath;
    }

    /**
     * generates an unique backup folder name. The returned value is a non-existing directory.
     */
    private File generateUniqueBackupDirName()
    {
        File backupRootDir = new File(installDir, BACKUP_ROOT);
        Date now = new Date();
        String backupDirName = new SimpleDateFormat(BACK_FOLDER_PATTERN).format(now);
        File result = new File(backupRootDir, backupDirName);

        // check if previous backup exists
        int uniqueSuffix = 1;
        while (result.exists())
        {
            result = new File(backupRootDir, backupDirName + "-" + uniqueSuffix);
            uniqueSuffix++;
        }
        return result;
    }
}

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

import com.izforge.izpack.api.data.AutomatedInstallData;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.security.PasswordGenerator;

/**
 * @author Kaloyan Enimanev
 */
public class GlobalInstallationContext
{
    public static final String POSTGRES_BIN_VARNAME = "POSTGRES_BIN";

    public static final String ADMIN_PASSWORD_VARNAME = "ADMIN_PASSWORD";

    public static final String ETL_SERVER_PASSWORD_VARNAME = "ETLSERVER_PASSWORD";

    public static final String KEY_STORE_FILE_VARNAME = "KEY_STORE_FILE";

    public static final String KEY_STORE_PASSWORD_VARNAME = "KEY_STORE_PASSWORD";

    public static final String KEY_PASSWORD_VARNAME = "KEY_PASSWORD";

    public static final String DATA_DIR_VARNAME = "DSS_ROOT_DIR";

    public static final String BACKUP_FOLDER_VARNAME = "BACKUP_FOLDER";

    public static final String TECHNOLOGY_ILLUMINA_NGS = "ILLUMINA-NGS";

    public static final String TECHNOLOGY_ELN_LIMS = "ELN-LIMS";

    public static final String TECHNOLOGY_ELN_LIMS_LIFE_SCIENCES = "ELN-LIMS-LIFE-SCIENCES";

    public static final String TECHNOLOGY_MICROSCOPY = "MICROSCOPY";

    public static final String TECHNOLOGY_FLOW_CYTOMETRY = "FLOW";

    public static final String TECHNOLOGY_SHARED_MICROSCOPY_FLOW_CYTOMETRY = "SHARED";

    public static final String[] TECHNOLOGIES =
            { TECHNOLOGY_ILLUMINA_NGS, TECHNOLOGY_ELN_LIMS, TECHNOLOGY_MICROSCOPY,
                    TECHNOLOGY_FLOW_CYTOMETRY, TECHNOLOGY_ELN_LIMS_LIFE_SCIENCES };

    /**
     * set to true if the installation process is trying to update an existing openBIS installation.
     */
    public static boolean isUpdateInstallation = false;

    public static boolean isUpdateInstallationWithoutDatabaseSelection = false;

    public static boolean isUpdateInstallationWithDatabaseSelection = false;

    /**
     * set to true if this is the first openBIS installation on the machine.
     */
    public static boolean isFirstTimeInstallation = true;

    /**
     * this variable is set to true if 'psql' or 'pg_dump' are not on path.
     * <p>
     * used by the installation xml config, not meant to be used by Java code.
     */
    public static boolean noPsqlToolsOnPath = (false == PostgresInstallationDetectorUtils
            .areCommandLineToolsOnPath());

    public static boolean presentKeyStoreFile = false;

    public static File installDir;

    public static void initialize(AutomatedInstallData data)
    {
        String installPath = data.getInstallPath();
        installDir = new File(installPath);
        isUpdateInstallation = installationExists();
        isFirstTimeInstallation = isUpdateInstallation == false;

        String postgresBinPath = "";
        if (isFirstTimeInstallation == false)
        {
            File pathFile = new File(installDir, "bin/postgres_bin_path.txt");
            if (pathFile.isFile())
            {
                postgresBinPath = FileUtilities.loadToString(pathFile).trim();
            }
            String backupScript =
                    FileUtilities.loadToString(new File(installDir, "bin/backup-installation.sh"));
            boolean canBackupDatabasesSelectively =
                    backupScript.contains(SetDatabasesToBackupAction.DATABASES_TO_BACKUP_VARNAME);
            if (canBackupDatabasesSelectively)
            {
                isUpdateInstallationWithDatabaseSelection = true;
            } else
            {
                isUpdateInstallationWithoutDatabaseSelection = true;
            }
        }
        data.setVariable(POSTGRES_BIN_VARNAME, postgresBinPath);

        if (isFirstTimeInstallation)
        {
            populateFirstTimeInstallVariables(data);
        }
    }
    
    // Note: folder servers/openBIS-server might not exists in an installation without AS
    private static final String[] FOLDERS_OF_INSTALLATION 
            = { "bin", "servers/core-plugins", "servers/datastore_server" };

    private static boolean installationExists()
    {
        if (installDir.exists() == false)
        {
            return false;
        }
        for (String folder : FOLDERS_OF_INSTALLATION)
        {
            if (new File(installDir, folder).exists() == false)
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Return the data directory chosen for this installation.
     */
    public static String getDataDir(AutomatedInstallData data)
    {
        return data.getVariable(DATA_DIR_VARNAME);
    }

    /**
     * populates variables need for fist time installations.
     */
    private static void populateFirstTimeInstallVariables(AutomatedInstallData data)
    {
        data.setVariable(ETL_SERVER_PASSWORD_VARNAME,
                new PasswordGenerator(true).generatePassword());
    }

}

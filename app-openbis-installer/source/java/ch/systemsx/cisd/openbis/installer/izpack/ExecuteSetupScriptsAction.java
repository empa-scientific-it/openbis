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

import static ch.systemsx.cisd.openbis.installer.izpack.GlobalInstallationContext.ADMIN_PASSWORD_VARNAME;
import static ch.systemsx.cisd.openbis.installer.izpack.GlobalInstallationContext.ETL_SERVER_PASSWORD_VARNAME;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.PanelActionConfiguration;
import com.izforge.izpack.api.handler.AbstractUIHandler;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.filesystem.FileUtilities;

/**
 * Executes a script that configures the installation, copies key store (if specified) and inject passwords.
 * 
 * @author Kaloyan Enimanev
 * @author Franz-Josef Elmer
 */
public class ExecuteSetupScriptsAction extends AbstractScriptExecutor
{
    private static final String OPENBIS_JETTY_VERSION_KEY = "openbis.jetty.version";

    private static final String JETTY_BEFORE_9_2 = "pre-9.2";

    private static final String JETTY_9_2 = "9.2";
    
    static final String DATA_SOURCES_KEY = "data-sources";

    static final String PATHINFO_DB_DATA_SOURCE = "path-info-db";

    static final String POST_REGISTRATION_TASKS_KEY = "post-registration.post-registration-tasks";

    static final String PATHINFO_DB_FEEDING_TASK = "pathinfo-feeding";

    static final String MAINTENANCE_PLUGINS_KEY = "maintenance-plugins";

    static final String PATHINFO_DB_DELETION_TASK = "path-info-deletion";

    static final String PROCESSING_PLUGINS_KEY = "processing-plugins";

    static final String PATHINFO_DB_CHECK = "path-info-db-consistency-check";

    private final String[] KEYS = { DATA_SOURCES_KEY, POST_REGISTRATION_TASKS_KEY, MAINTENANCE_PLUGINS_KEY, PROCESSING_PLUGINS_KEY };

    private final String[] TERMS = { PATHINFO_DB_DATA_SOURCE, PATHINFO_DB_FEEDING_TASK, PATHINFO_DB_DELETION_TASK, PATHINFO_DB_CHECK };

    private final String[] CLASS_POSTFIX = { "version-holder-class", "class", "class", "class" };

    /**
     * executed for first time installations.
     */
    private static final String POST_INSTALLATION_SCRIPT = "post-installation.sh";

    /**
     * executed for upgrade installations to restore backed up the configuration files.
     */
    private static final String RESTORE_CONFIG_FROM_BACKUP_SCRIPT = "restore-config-from-backup.sh";

    @Override
    public synchronized void executeActionWithHandler(AutomatedInstallData data, AbstractUIHandler handler)
    {
        File installDir = GlobalInstallationContext.installDir;
        String newJettyVersion = getJettyVersion(installDir);
        if (GlobalInstallationContext.isFirstTimeInstallation)
        {
            executePostInstallationScript(data);
        } else
        {
            executRestoreConfigScript(data);
        }
        String keyStoreFileName =
                data.getVariable(GlobalInstallationContext.KEY_STORE_FILE_VARNAME);
        String keyStorePassword =
                data.getVariable(GlobalInstallationContext.KEY_STORE_PASSWORD_VARNAME);
        String certificatePassword =
                data.getVariable(GlobalInstallationContext.KEY_PASSWORD_VARNAME);
        enablePathinfoDB(installDir);
        installKeyStore(keyStoreFileName, installDir);
        String previousJettyVersion = getJettyVersion(installDir);
        if (previousJettyVersion.equals(JETTY_BEFORE_9_2))
        {
            return;
        }
        if (newJettyVersion.equals(previousJettyVersion) == false)
        {
            String message;
            if (previousJettyVersion.equals(JETTY_9_2))
            {
                message = handleJetty92ToNewerVersion(installDir, previousJettyVersion, newJettyVersion);
            } else
            {
                message = "Different jetty version: " + previousJettyVersion + " -> " + newJettyVersion;
            }
            showWarning(handler, newJettyVersion, message);
        }
        injectPasswords(keyStorePassword, certificatePassword, installDir);
    }

    private String handleJetty92ToNewerVersion(File installDir, String previousJettyVersion, String newJettyVersion)
    {
        String migratedIniFiles = "";
        if (Utils.isASInstalled(installDir))
        {
            File sslIniFile = new File(installDir, Utils.AS_PATH + Utils.JETTY_SSL_INI_PATH);
            if (sslIniFile.exists())
            {
                try
                {
                    String sslIni = FileUtils.readFileToString(sslIniFile);
                    sslIni = sslIni.replaceAll("jetty\\.secure\\.port=(.*)", "jetty.ssl.port=$1");
                    sslIni = sslIni.replaceAll("jetty\\.keystore\\.password=(.*)", "jetty.sslContext.keyStorePassword=$1");
                    sslIni = sslIni.replaceAll("jetty\\.keymanager\\.password=(.*)", "jetty.sslContext.keyManagerPassword=$1");
                    sslIni = sslIni.replaceAll("jetty\\.truststore\\.password=(.*)", "jetty.sslContext.trustStorePassword=$1");
                    FileUtils.writeStringToFile(sslIniFile, sslIni);
                    migratedIniFiles = sslIniFile.getName();
                } catch (IOException ex)
                {
                    throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                }
            }
            File deployIniFile = new File(installDir, Utils.AS_PATH + Utils.JETTY_DEPLOY_INI_PATH);
            if (deployIniFile.exists())
            {
                try
                {
                    String deployIni = FileUtils.readFileToString(deployIniFile);
                    deployIni = deployIni.replaceAll("jetty\\.deploy\\.monitoredDirName=(.*)", "jetty.deploy.monitoredDir=$1");
                    deployIni += "\n" + OPENBIS_JETTY_VERSION_KEY + "=" + newJettyVersion + "\n";
                    FileUtils.writeStringToFile(deployIniFile, deployIni);
                    migratedIniFiles += " and " + deployIniFile.getName();
                } catch (IOException ex)
                {
                    throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                }
            }
        }
        if (migratedIniFiles.isEmpty())
        {
            return "Automatic migration of Jetty from version " + previousJettyVersion + " to " + newJettyVersion 
                    + " wasn't possible.\nPlease, adapt the ini files in "
                    + "<installation folder>/openBIS-server/jetty/start.d accordingly.\n"
                    + "See http://www.eclipse.org/jetty/documentation/current/quick-start-configure.html "
                    + "for more details."; 
        }
        return "Automatic migration of Jetty from version " + previousJettyVersion + " to " + newJettyVersion 
                + " has been performed for " + migratedIniFiles 
                + " in <installation folder>/openBIS-server/jetty/start.d.\n"
                + "Nevertheless all ini files should be checked after installation and adapted accordingly "
                + "especially in case of some custom configuration.\n"
                + "See http://www.eclipse.org/jetty/documentation/current/quick-start-configure.html "
                + "for more details."; 
    }
    
    private void showWarning(AbstractUIHandler handler, String newJettyVersion, String message)
    {
        System.out.println("========== WARNING: New Jetty Version ========");
        System.out.println(message);
        System.out.println("==============================================");
        if (handler != null)
        {
            handler.emitWarning("New Jetty Version " + newJettyVersion, message);
        }
    }

    void installKeyStore(String keyStoreFileName, File installDir)
    {
        if (keyStoreFileName != null && keyStoreFileName.length() > 0)
        {
            try
            {
                File keyStoreFile = new File(keyStoreFileName);
                if (Utils.isASInstalled(installDir))
                {
                    File keystoreFileAS = Utils.getKeystoreFileForAS(installDir);
                    FileUtils.copyFile(keyStoreFile, keystoreFileAS);
                }
                File keystoreFileDSS = Utils.getKeystoreFileForDSS(installDir);
                FileUtils.copyFile(keyStoreFile, keystoreFileDSS);
            } catch (IOException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }
    }

    void enablePathinfoDB(File installDir)
    {
        File dssServicePropertiesFile =
                new File(installDir, Utils.DSS_PATH + Utils.SERVICE_PROPERTIES_PATH);
        for (int i = 0; i < KEYS.length; i++)
        {
            String key = KEYS[i];
            String newTerm = TERMS[i];
            Utils.removeTermFromPropertyList(dssServicePropertiesFile, key, newTerm);
            String classProperty = newTerm + "." + CLASS_POSTFIX[i];
            int indexOfDot = key.indexOf('.');
            if (indexOfDot >= 0)
            {
                classProperty = key.substring(0, indexOfDot) + "." + classProperty;
            }
            String className =
                    Utils.tryToGetProperties(dssServicePropertiesFile).getProperty(
                            classProperty);
            if (className != null)
            {
                Utils.addTermToPropertyList(dssServicePropertiesFile, key, newTerm);
            }
        }
    }

    void injectPasswords(String keyStorePassword, String keyPassword, File installDir)
    {
        File dssServicePropertiesFile =
                new File(installDir, Utils.DSS_PATH + Utils.SERVICE_PROPERTIES_PATH);
        Utils.updateOrAppendProperty(dssServicePropertiesFile, Utils.DSS_KEYSTORE_PASSWORD_KEY,
                keyStorePassword);
        Utils.updateOrAppendProperty(dssServicePropertiesFile, Utils.DSS_KEYSTORE_KEY_PASSWORD_KEY,
                keyPassword);

        if (Utils.isASInstalled(installDir))
        {
            File jettySSLIniFile = new File(installDir, Utils.AS_PATH + Utils.JETTY_SSL_INI_PATH);
            if (jettySSLIniFile.exists() == false)
            {
                // ssl not configured
                return;
            }
            try
            {
                String jettySSLIni = FileUtils.readFileToString(jettySSLIniFile);
                jettySSLIni =
                        jettySSLIni.replaceAll("jetty\\.sslContext\\.keyStorePassword=.*", "jetty.sslContext.keyStorePassword=" + keyStorePassword)
                                .replaceAll("jetty\\.sslContext\\.keyManagerPassword=.*", "jetty.sslContext.keyManagerPassword=" + keyPassword)
                                .replaceAll("jetty\\.sslContext\\.trustStorePassword=.*", "jetty.sslContext.trustStorePassword=" + keyStorePassword);

                FileUtils.writeStringToFile(jettySSLIniFile, jettySSLIni);
            } catch (IOException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }
    }
    
    private String getJettyVersion(File installDir)
    {
        if (GlobalInstallationContext.isFirstTimeInstallation)
        {
            return "";
        }
        File jettyDeployIniFile = new File(installDir, Utils.AS_PATH + Utils.JETTY_DEPLOY_INI_PATH);
        if (jettyDeployIniFile.exists() == false)
        {
            return JETTY_BEFORE_9_2;
        }
        List<String> list = FileUtilities.loadToStringList(jettyDeployIniFile);
        for (String line : list)
        {
            if (line.trim().startsWith(OPENBIS_JETTY_VERSION_KEY))
            {
                String[] splittedLine = line.split("=");
                if (splittedLine.length > 1)
                {
                    return splittedLine[1].trim();
                }
                break;
            }
        }
        return JETTY_9_2;
    }
    
    private void executRestoreConfigScript(AutomatedInstallData data)
    {
        String script = getAdminScript(data, RESTORE_CONFIG_FROM_BACKUP_SCRIPT);
        String backupConfigFolder = data.getVariable(GlobalInstallationContext.BACKUP_FOLDER_VARNAME) + "/config-backup";
        executeAdminScript(null, script, backupConfigFolder);
    }

    private void executePostInstallationScript(AutomatedInstallData data)
    {
        String script = getAdminScript(data, POST_INSTALLATION_SCRIPT);
        Map<String, String> customEnvironment = new HashMap<String, String>();
        customEnvironment.put(ADMIN_PASSWORD_VARNAME, data.getVariable(ADMIN_PASSWORD_VARNAME));
        customEnvironment.put(ETL_SERVER_PASSWORD_VARNAME,
                data.getVariable(ETL_SERVER_PASSWORD_VARNAME));
        executeAdminScript(customEnvironment, script);
    }

    @Override
    public void initialize(PanelActionConfiguration arg0)
    {
    }

}

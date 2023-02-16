/*
 * Copyright ETH 2010 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.dss.generic.shared.utils;

import java.io.File;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;

import ch.ethz.cisd.hotdeploy.PluginContainer;
import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.common.filesystem.highwatermark.HostAwareFileWithHighwaterMark;
import ch.systemsx.cisd.common.io.PropertyIOUtils;
import ch.systemsx.cisd.common.properties.ExtendedProperties;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.string.Template;
import ch.systemsx.cisd.etlserver.ThreadParameters;
import ch.systemsx.cisd.openbis.generic.shared.coreplugin.CorePluginScanner.ScannerType;
import ch.systemsx.cisd.openbis.generic.shared.coreplugin.CorePluginsInjector;
import ch.systemsx.cisd.openbis.generic.shared.coreplugin.CorePluginsUtils;

/**
 * Utility class to load properties.
 * 
 * @author Tomasz Pylak
 */
public class DssPropertyParametersUtil
{

    private static final String LOG_REGISTRATIONS_KEY = "log-registrations";

    private static final String RECOVERY_STATE_KEY = "recovery-state";

    private static final String RECOVERY_MARKER_DIR_KEY = "recovery-marker-dir";

    private static final String DSS_TMP_KEY = "dss-tmp";

    /** Prefix of system properties which may override service.properties. */
    public static final String OPENBIS_DSS_SYSTEM_PROPERTIES_PREFIX = "openbis.dss.";

    public static final String DSS_CODE_KEY = "data-store-server-code";

    public static final String STOREROOT_DIR_KEY = "storeroot-dir";

    public static final String DOWNLOAD_URL_KEY = "download-url";

    public static final String SERVER_URL_KEY = "server-url";

    public static final String RSYNC_OPTIONS = "rsync-options";

    public static final String DATA_STREAM_TIMEOUT = "data-stream-timeout";

    public static final String DATA_STREAM_MAX_TIMEOUT = "data-stream-max-timeout";

    public static final int MINIMUM_TIME_TO_KEEP_STREAMS_DEFAULT = 5;

    public static final int MAXIMUM_TIME_TO_KEEP_STREAMS_DEFAULT = 60 * 60 * 4; // 4 hours

    /**
     * Temp directory for dss usage.
     */
    static final String DSS_TEMP_DIR_PATH = "dss-temp-dir";

    @Private
    static final String EMPTY_TEST_FILE_NAME = "an-empty-test-file";

    /**
     * Directory for registration log files.
     */
    public static final String DSS_REGISTRATION_LOG_DIR_PATH = "dss-registration-log-dir";

    /**
     * Directory for recovery state files.
     */
    public static final String DSS_RECOVERY_STATE_DIR_PATH = "dss-recovery-state-dir";

    /**
     * Directory for recovery marker files.
     */
    public static final String DSS_RECOVERY_MARKER_DIR_PATH = "dss-recovery-marker-dir";

    /** Location of service properties file. */
    public static final String SERVICE_PROPERTIES_FILE = "etc/service.properties";

    private static final String EXPLANATION =
            "Please make sure this directory exists on the local file system and is writable "
                    + "by the data store server or provide such a directory "
                    + "by the configuration parameter '${path-key}'.";

    private static final Template NON_EXISTING_DIR_TEMPLATE = new Template(
            "Could not create ${dir-description} at path: ${path}. " + EXPLANATION);

    private static final Template NON_SAME_VOLUME_TEMPLATE =
            new Template(
                    "By the configuration parameter '${path-a-key}' directory '${path-a}' and by the configuration parameter'${path-b-key}' directory '${path-b}' are not on the same file system. ");

    private static final Template NON_WRITABLE_TEMPLATE =
            new Template("By the configuration parameter '${path-a-key}' directory '${path-a}' is not writable. ");

    private static final Template NON_VOLUME_TEMPLATE =
            new Template("Volume information from the configuration parameter '${path-a-key}' directory '${path-a}' is not retrivable. ");

    private static final Template NON_MOVE_TEMPLATE = new Template(
            "Move operation failed from the configuration parameter '${path-a-key}' directory '${path-a}' to from the configuration parameter '${path-b-key}' directory '${path-b}'. ");

    private static File dssTmp;

    private static File recoveryState;

    private static File recoveryMarkerDir;

    private static File logRegistrations;

    private static ExtendedProperties fullServiceProperties;

    /** loads server configuration */
    public static ExtendedProperties loadServiceProperties()
    {
        if (fullServiceProperties == null)
        {
            ExtendedProperties properties = loadProperties(SERVICE_PROPERTIES_FILE);
            CorePluginsUtils.addCorePluginsProperties(properties, ScannerType.DSS);
            ExtendedProperties serviceProperties = extendProperties(properties);
            CorePluginsInjector injector =
                    new CorePluginsInjector(ScannerType.DSS, DssPluginType.values());
            Map<String, File> pluginFolders = injector.injectCorePlugins(serviceProperties);

            if (PluginContainer.tryGetInstance() == null)
            {
                PluginContainer.initHotDeployment();
            }

            for (String name : pluginFolders.keySet())
            {
                File mainFolder = pluginFolders.get(name);
                File hotDeployFolder = new File(mainFolder, "plugin");
                if (hotDeployFolder.exists() && hotDeployFolder.isDirectory()
                        && PluginContainer.tryGetInstance(name) == null)
                {
                    PluginContainer pluginContainer = PluginContainer.initHotDeployment(name);
                    pluginContainer.addPluginDirectory(hotDeployFolder);
                    pluginContainer.refresh(true);
                }
            }
            fullServiceProperties = serviceProperties;
        }
        return fullServiceProperties;
    }

    public static ExtendedProperties loadProperties(String filePath)
    {
        return extendProperties(PropertyIOUtils.loadProperties(filePath));
    }

    static ExtendedProperties extendProperties(Properties properties)
    {
        Properties systemProperties = System.getProperties();
        ExtendedProperties dssSystemProperties =
                ExtendedProperties.getSubset(systemProperties,
                        OPENBIS_DSS_SYSTEM_PROPERTIES_PREFIX, true);
        Set<Entry<Object, Object>> entrySet = dssSystemProperties.entrySet();
        for (Entry<Object, Object> entry : entrySet)
        {
            properties.put(entry.getKey(), entry.getValue());
        }
        return ExtendedProperties.createWith(properties);
    }

    public static long getHighwaterMark(ThreadParameters threadParameters, final Properties properties)
    {
        Long incomingShareMinimumFreeSpace = threadParameters.getIncomingShareMinimumFreeSpace();
        if (incomingShareMinimumFreeSpace != null)
        {
            return incomingShareMinimumFreeSpace / FileUtils.ONE_KB;
        }
        return PropertyUtils.getLong(properties, HostAwareFileWithHighwaterMark.HIGHWATER_MARK_PROPERTY_KEY, -1L);
    }

    public static String getDataStoreCode(Properties serviceProperties)
    {
        return PropertyUtils.getMandatoryProperty(serviceProperties, DSS_CODE_KEY).toUpperCase();
    }

    public final static File getStoreRootDir(final Properties properties)
    {
        return FileUtilities.normalizeFile(new File(PropertyUtils.getMandatoryProperty(properties,
                STOREROOT_DIR_KEY)));
    }

    public static List<String> getRsyncOptions(Properties serviceProperties)
    {
        String rsyncOptions = serviceProperties.getProperty(RSYNC_OPTIONS, null);
        if (rsyncOptions == null)
        {
            return null;
        } else
        {
            return Arrays.asList(rsyncOptions.split(" "));
        }
    }

    public static String getOpenBisServerUrl(Properties serviceProperties)
    {
        return PropertyUtils.getMandatoryProperty(serviceProperties, SERVER_URL_KEY);
    }

    public static String getDownloadUrl(Properties serviceProperties)
    {
        return PropertyUtils.getProperty(serviceProperties, DOWNLOAD_URL_KEY, "");
    }

    public static int getDataStreamTimeout(Properties serviceProperties)
    {
        return PropertyUtils.getPosInt(serviceProperties, DATA_STREAM_TIMEOUT,
                MINIMUM_TIME_TO_KEEP_STREAMS_DEFAULT);
    }

    public static int getDataStreamMaxTimeout(Properties serviceProperties)
    {
        return PropertyUtils.getPosInt(serviceProperties, DATA_STREAM_MAX_TIMEOUT,
                MAXIMUM_TIME_TO_KEEP_STREAMS_DEFAULT);
    }

    public static File getDssInternalTempDir(final Properties properties)
    {
        return getDssInternalTempDir(FileOperations.getInstance(), properties);
    }

    @Private
    static File getDssInternalTempDir(IFileOperations fileOperations, final Properties properties)
    {
        if (dssTmp == null)
        {
            createAndTestSpecialDirectories(fileOperations, properties);
        }
        return dssTmp;
    }

    public static File getDssRegistrationLogDir(final Properties properties)
    {
        return getDssRegistrationLogDir(FileOperations.getInstance(), properties);
    }

    @Private
    static File getDssRegistrationLogDir(IFileOperations fileOperations, final Properties properties)
    {
        if (logRegistrations == null)
        {
            createAndTestSpecialDirectories(fileOperations, properties);
        }
        return logRegistrations;
    }

    public static File getDssRecoveryStateDir(final Properties properties)
    {
        return getDssRecoveryStateDir(FileOperations.getInstance(), properties);
    }

    public static File getDssRecoveryMarkerDir(final Properties properties)
    {
        if (recoveryMarkerDir == null)
        {
            createAndTestSpecialDirectories(FileOperations.getInstance(), properties);
        }
        return recoveryMarkerDir;
    }

    @Private
    static File getDssRecoveryStateDir(IFileOperations fileOperations, final Properties properties)
    {
        if (recoveryState == null)
        {
            createAndTestSpecialDirectories(fileOperations, properties);
        }
        return recoveryState;
    }

    private static File getDir(IFileOperations fileOperations, final Properties properties, String defaultDirName, String dirDescription,
            String pathKey)
    {
        String defaultRegistrationLogDirPath =
                new File(System.getProperty("user.dir"), defaultDirName).getAbsolutePath();
        String registrationLogDirPath =
                PropertyUtils.getProperty(properties, pathKey, defaultRegistrationLogDirPath);
        File registrationLogDir = new File(registrationLogDirPath);
        fileOperations.mkdirs(registrationLogDir);
        assertDirExists(fileOperations, registrationLogDir, dirDescription, pathKey);
        return registrationLogDir;
    }

    private static AtomicInteger atomicEmptyFileIndex = new AtomicInteger();

    private static boolean isWritable(IFileOperations fileOperations, Path path)
    {
        boolean created = false;
        File file = null;
        try
        {
            String threadSafeEmptyTestFileName = EMPTY_TEST_FILE_NAME + atomicEmptyFileIndex.incrementAndGet();
            file = Paths.get(path.toString(), threadSafeEmptyTestFileName).toFile();
            created = fileOperations.createNewFile(file);
        } catch (Exception ex)
        {
            created = false;
        } finally
        {
            if (created && file != null)
            {
                fileOperations.delete(file);
            }
        }
        return created;
    }

    private static FileStore getVolumeInfo(Path path, String key)
    {
        FileStore info = null;
        try
        {
            info = Files.getFileStore(path);
        } catch (Exception ex)
        {
            createException(NON_VOLUME_TEMPLATE, key, path, null, null);
        }
        return info;
    }

    private static boolean isMoveFromTo(IFileOperations fileOperations, Path pathA, Path pathB)
    {
        boolean created = false;
        boolean moved = false;
        File originalfile = null;
        File toMove = null;
        try
        {
            String threadSafeEmptyTestFileName = EMPTY_TEST_FILE_NAME + atomicEmptyFileIndex.incrementAndGet();
            originalfile = Paths.get(pathA.toString(), threadSafeEmptyTestFileName).toFile();
            created = fileOperations.createNewFile(originalfile);
            if (created)
            {
                toMove = Paths.get(pathB.toString(), threadSafeEmptyTestFileName).toFile();
                fileOperations.move(originalfile, toMove);
                moved = true;
            }
        } catch (Exception ex)
        {
            moved = false;
        } finally
        {
            if (moved && toMove != null)
            {
                fileOperations.delete(toMove);
            } else if (created && originalfile != null)
            {
                fileOperations.delete(originalfile);
            }
        }
        return moved;
    }

    private static void createAndTestSpecialDirectories(IFileOperations fileOperations, final Properties properties)
    {
        dssTmp = getDir(fileOperations, properties, DSS_TMP_KEY, "an internal temp directory for the data store server", DSS_TEMP_DIR_PATH);
        Path dssTmpPath = dssTmp.toPath();
        recoveryState = getDir(fileOperations, properties, RECOVERY_STATE_KEY, "a directory for storing recovery state for the dss",
                DSS_RECOVERY_STATE_DIR_PATH);
        Path recoveryStatePath = recoveryState.toPath();
        recoveryMarkerDir = getDir(fileOperations, properties, RECOVERY_MARKER_DIR_KEY, "a directory for storing recovery marker for the dss",
                DSS_RECOVERY_MARKER_DIR_PATH);
        Path recoveryMarkerPath = recoveryMarkerDir.toPath();
        logRegistrations = getDir(fileOperations, properties, LOG_REGISTRATIONS_KEY, "a directory for storing registration logs",
                DSS_REGISTRATION_LOG_DIR_PATH);
        Path logRegistrationsPath = logRegistrations.toPath();

        FileStore dssTmpStore = getVolumeInfo(dssTmpPath, DSS_TMP_KEY);
        FileStore recoveryStateStore = getVolumeInfo(recoveryStatePath, RECOVERY_STATE_KEY);
        FileStore recoveryMarkerStore = getVolumeInfo(recoveryMarkerPath, RECOVERY_MARKER_DIR_KEY);
        FileStore logRegistrationsState = getVolumeInfo(logRegistrationsPath, LOG_REGISTRATIONS_KEY);

        // Volume info obtained
        if (dssTmpStore == null)
        {
            throw createException(NON_VOLUME_TEMPLATE, DSS_TMP_KEY, dssTmpPath, null, null);
        } else if (recoveryStateStore == null)
        {
            throw createException(NON_VOLUME_TEMPLATE, RECOVERY_STATE_KEY, recoveryStatePath, null, null);
        } else if (logRegistrationsState == null)
        {
            throw createException(NON_VOLUME_TEMPLATE, LOG_REGISTRATIONS_KEY, logRegistrationsPath, null, null);
        }
        // Same volume tests
        else if (dssTmpStore.equals(recoveryStateStore) == false)
        {
            throw createException(NON_SAME_VOLUME_TEMPLATE, DSS_TMP_KEY, dssTmpPath, RECOVERY_STATE_KEY, recoveryStatePath);
        } else if (dssTmpStore.equals(logRegistrationsState) == false)
        {
            throw createException(NON_SAME_VOLUME_TEMPLATE, DSS_TMP_KEY, dssTmpPath, LOG_REGISTRATIONS_KEY, logRegistrationsPath);
        }
        // Writable folders tests
        else if (isWritable(fileOperations, dssTmpPath) == false)
        {
            throw createException(NON_WRITABLE_TEMPLATE, DSS_TMP_KEY, dssTmpPath, null, null);
        } else if (isWritable(fileOperations, recoveryStatePath) == false)
        {
            throw createException(NON_WRITABLE_TEMPLATE, RECOVERY_STATE_KEY, recoveryStatePath, null, null);
        } else if (isWritable(fileOperations, logRegistrationsPath) == false)
        {
            throw createException(NON_WRITABLE_TEMPLATE, LOG_REGISTRATIONS_KEY, logRegistrationsPath, null, null);
        }
        // Move command tests
        else if (isMoveFromTo(fileOperations, dssTmpPath, recoveryStatePath) == false)
        {
            throw createException(NON_MOVE_TEMPLATE, DSS_TMP_KEY, dssTmpPath, RECOVERY_STATE_KEY, recoveryStatePath);
        } else if (isMoveFromTo(fileOperations, dssTmpPath, logRegistrationsPath) == false)
        {
            throw createException(NON_MOVE_TEMPLATE, DSS_TMP_KEY, dssTmpPath, LOG_REGISTRATIONS_KEY, logRegistrationsPath);
        }
    }

    private static void assertDirExists(IFileOperations fileOperations, File dir,
            String dirDescription, String pathKey)
    {
        if (fileOperations.exists(dir) == false)
        {
            throw createException(NON_EXISTING_DIR_TEMPLATE.createFreshCopy(), dir, dirDescription,
                    pathKey);
        }
    }

    private static ConfigurationFailureException createException(Template template, String pathAKey, Path pathA, String pathBKey, Path pathB)
    {
        template.bind("path-a-key", pathAKey);
        template.bind("path-a", pathA.toString());
        if (pathBKey != null && pathB != null)
        {
            template.bind("path-b-key", pathBKey);
            template.bind("path-b", pathB.toString());
        }
        ConfigurationFailureException e = new ConfigurationFailureException(template.createText());
        return e;
    }

    private static ConfigurationFailureException createException(Template template, File dir,
            String dirName, String pathKey)
    {
        template.attemptToBind("dir-description", dirName);
        template.bind("path", dir.getPath());
        template.bind("path-key", pathKey);
        ConfigurationFailureException e = new ConfigurationFailureException(template.createText());
        return e;
    }
}

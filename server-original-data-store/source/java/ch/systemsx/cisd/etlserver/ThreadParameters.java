/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver;

import java.io.File;
import java.util.Arrays;
import java.util.Properties;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationPreStagingBehavior;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.SegmentedStoreUtils;

/**
 * <i>ETL</i> thread specific parameters.
 * 
 * @author Tomasz Pylak
 */
public final class ThreadParameters
{

    /**
     * A path to a script which should be called from command line before data set registration. The script gets two parameters: data set code and
     * absolute path to the data set in the data store.
     */
    @Private
    static final String PRE_REGISTRATION_SCRIPT_KEY = "pre-registration-script";

    /**
     * A path to a script which should be called from command line after successful data set registration. The script gets two parameters: data set
     * code and absolute path to the data set in the data store.
     */
    @Private
    static final String POST_REGISTRATION_SCRIPT_KEY = "post-registration-script";

    /**
     * A path to a script which should be invoked to validate the data set.
     */
    @Private
    public static final String VALIDATION_SCRIPT_KEY = "validation-script-path";

    @Private
    static final String GROUP_CODE_KEY = "group-code";

    @Private
    public static final String INCOMING_DATA_COMPLETENESS_CONDITION =
            "incoming-data-completeness-condition";

    @Private
    public static final String INCOMING_DATA_COMPLETENESS_CONDITION_MARKER_FILE = "marker-file";

    @Private
    static final String INCOMING_DATA_COMPLETENESS_CONDITION_AUTODETECTION = "auto-detection";

    @Private
    public static final String TOP_LEVEL_DATA_SET_HANDLER = "top-level-data-set-handler";

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            ThreadParameters.class);

    @Private
    public static final String INCOMING_DIR = "incoming-dir";

    private static final String INCOMING_DIR_CREATE = "incoming-dir-create";

    @Private
    public static final String DELETE_UNIDENTIFIED_KEY = "delete-unidentified";

    public static final String REPROCESS_FAULTY_DATASETS_NAME = "reprocess-faulty-datasets";

    @Private
    public static final String ON_ERROR_DECISION_KEY = "on-error-decision";

    public static final String DATASET_REGISTRATION_PRE_STAGING_BEHAVIOR =
            "dataset-registration-prestaging-behavior";

    @Private
    public static final String INCOMING_SHARE_ID = "incoming-share-id";

    private static final String INCOMING_SHARE_MINIMUM_FREE_SPACE_IN_GB = "incoming-share-minimum-free-space-in-gb";

    /*
     * The properties that control the process of retrying registration by jython dropboxes
     */
    public static final String DATASET_REGISTRATION_MAX_RETRY_COUNT =
            "metadata-registration-max-retry-count";

    public static final String DATASET_REGISTRATION_RETRY_PAUSE_IN_SEC =
            "metadata-registration-retry-pause-in-sec";

    /*
     * The properties that control the process of retrying registration by jython dropboxes
     */
    public static final String PROCESS_MAX_RETRY_COUNT = "process-max-retry-count";

    public static final String PROCESS_RETRY_PAUSE_IN_SEC = "process-retry-pause-in-sec";

    /*
     * Recovery related properties
     */

    public static final String RECOVERY_MAX_RETRY_COUNT = "recovery-max-retry-count";

    public static final String RECOVERY_MIN_RETRY_PERIOD = "recovery-min-retry-period";

    public static final String RECOVERY_DEVELOPMENT_MODE = "development-mode";

    private static final String H5_FOLDERS = "h5-folders";

    private static final String H5AR_FOLDERS = "h5ar-folders";

    /**
     * The (local) directory to monitor for new files and directories to move to the remote side. The directory where data to be processed by the ETL
     * server become available.
     */
    private final File incomingDataDirectory;

    private final boolean createIncomingDirectories;

    private final Properties threadProperties;

    private final Class<?> topLevelDataSetRegistratorClassOrNull;

    private final Class<?> onErrorDecisionClassOrNull;

    private final String threadName;

    private final String groupCode;

    private final String preRegistrationScript;

    private final String postRegistrationScript;

    private final String[] validationScripts;

    private final boolean useIsFinishedMarkerFile;

    private final boolean deleteUnidentified;

    private final boolean reprocessFaultyDatasets;

    private final int dataSetRegistrationMaxRetryCount;

    private final int dataSetRegistrationRetryPauseInSec;

    private final int processMaxRetryCount;

    private final int processRetryPauseInSec;

    private final int maximumRecoveryCount;

    private final int minimumRecoveryPeriod;

    private final DataSetRegistrationPreStagingBehavior dataSetRegistrationPreStagingBehavior;

    private final Integer incomingShareId;

    private final Long incomingShareMinimumFreeSpace;

    private final boolean h5Folders;

    private final boolean h5arFolders;

    /**
     * @param threadProperties parameters for one processing thread together with general parameters.
     */
    public ThreadParameters(final Properties threadProperties, final String threadName)
    {
        this.incomingDataDirectory = extractIncomingDataDir(threadProperties, threadName);
        this.h5Folders = PropertyUtils.getBoolean(threadProperties, H5_FOLDERS, false);
        this.h5arFolders = PropertyUtils.getBoolean(threadProperties, H5AR_FOLDERS, true);
        this.createIncomingDirectories =
                PropertyUtils.getBoolean(threadProperties, INCOMING_DIR_CREATE, true);
        this.threadProperties = threadProperties;
        String registratorClassName =
                PropertyUtils.getProperty(threadProperties, TOP_LEVEL_DATA_SET_HANDLER);

        Class<?> registratorClass;
        try
        {
            registratorClass =
                    (null == registratorClassName) ? null : Class.forName(registratorClassName);
        } catch (ClassNotFoundException ex)
        {
            throw ConfigurationFailureException.fromTemplate("Wrong '%s' property: %s",
                    TOP_LEVEL_DATA_SET_HANDLER, ex.getMessage());
        }
        this.topLevelDataSetRegistratorClassOrNull = registratorClass;

        this.groupCode = tryGetGroupCode(threadProperties);
        this.preRegistrationScript = tryGetPreRegistrationScript(threadProperties);
        this.postRegistrationScript = tryGetPostRegistartionScript(threadProperties);
        this.validationScripts = tryGetValidationScripts(threadProperties);
        String completenessCondition =
                PropertyUtils.getProperty(threadProperties, INCOMING_DATA_COMPLETENESS_CONDITION,
                        INCOMING_DATA_COMPLETENESS_CONDITION_MARKER_FILE);
        this.useIsFinishedMarkerFile = parseCompletenessCondition(completenessCondition);
        this.deleteUnidentified =
                "true".equals(threadProperties.getProperty(DELETE_UNIDENTIFIED_KEY, "false"));
        this.reprocessFaultyDatasets =
                Boolean.parseBoolean(threadProperties.getProperty(REPROCESS_FAULTY_DATASETS_NAME,
                        "false"));
        this.dataSetRegistrationPreStagingBehavior =
                getOriginalnputDataSetBehaviour(threadProperties);
        this.incomingShareId = tryGetIncomingShareId(threadProperties);
        this.incomingShareMinimumFreeSpace = tryGetIncomingShareMinimumFreeSpace(threadProperties);

        boolean developmentMode =
                PropertyUtils.getBoolean(threadProperties, RECOVERY_DEVELOPMENT_MODE, false);
        if (developmentMode)
        {
            this.dataSetRegistrationMaxRetryCount = 0;
            this.dataSetRegistrationRetryPauseInSec = 0;
            this.processMaxRetryCount = 0;
            this.processRetryPauseInSec = 0;
            this.maximumRecoveryCount = 0;
            this.minimumRecoveryPeriod = 0;
        } else
        {
            this.dataSetRegistrationMaxRetryCount =
                    Integer.parseInt(threadProperties.getProperty(
                            DATASET_REGISTRATION_MAX_RETRY_COUNT, "6"));
            this.dataSetRegistrationRetryPauseInSec =
                    Integer.parseInt(threadProperties.getProperty(
                            DATASET_REGISTRATION_RETRY_PAUSE_IN_SEC, "300"));
            this.processMaxRetryCount =
                    Integer.parseInt(threadProperties.getProperty(PROCESS_MAX_RETRY_COUNT, "6"));
            this.processRetryPauseInSec =
                    Integer.parseInt(threadProperties
                            .getProperty(PROCESS_RETRY_PAUSE_IN_SEC, "300"));
            this.maximumRecoveryCount =
                    PropertyUtils.getInt(threadProperties, RECOVERY_MAX_RETRY_COUNT, 50);
            this.minimumRecoveryPeriod =
                    PropertyUtils.getInt(threadProperties, RECOVERY_MIN_RETRY_PERIOD, 60);
        }

        this.threadName = threadName;

        String onErrorClassName =
                PropertyUtils.getProperty(threadProperties, ON_ERROR_DECISION_KEY + ".class");
        Class<?> onErrorClass;
        try
        {
            onErrorClass = (null == onErrorClassName) ? null : Class.forName(onErrorClassName);
        } catch (ClassNotFoundException ex)
        {
            throw ConfigurationFailureException.fromTemplate("Wrong '%s' property: %s",
                    ON_ERROR_DECISION_KEY + ".class", ex.getMessage());
        }
        this.onErrorDecisionClassOrNull = onErrorClass;

    }

    private DataSetRegistrationPreStagingBehavior getOriginalnputDataSetBehaviour(
            final Properties threadProperties1)
    {
        String property =
                threadProperties1.getProperty(DATASET_REGISTRATION_PRE_STAGING_BEHAVIOR, "default");
        DataSetRegistrationPreStagingBehavior retVal =
                DataSetRegistrationPreStagingBehavior.fromString(property);
        if (null == retVal)
        {
            throw new ConfigurationFailureException(DATASET_REGISTRATION_PRE_STAGING_BEHAVIOR
                    + " setting for a dropbox is invalid. Incorrect value " + property);
        }
        return retVal;
    }

    public Integer getIncomingShareId()
    {
        return incomingShareId;
    }

    public Long getIncomingShareMinimumFreeSpace()
    {
        return incomingShareMinimumFreeSpace;
    }

    // true if marker file should be used, false if autodetection should be used, exceprion when the
    // value is invalid.
    private static boolean parseCompletenessCondition(String completenessCondition)
    {
        if (completenessCondition
                .equalsIgnoreCase(INCOMING_DATA_COMPLETENESS_CONDITION_MARKER_FILE))
        {
            return true;
        } else if (completenessCondition
                .equalsIgnoreCase(INCOMING_DATA_COMPLETENESS_CONDITION_AUTODETECTION))
        {
            return false;
        } else
        {
            throw new ConfigurationFailureException(String.format(
                    "Invalid value '%s' for the option '%s'. Allowed values are: '%s', '%s'.",
                    completenessCondition, INCOMING_DATA_COMPLETENESS_CONDITION,
                    INCOMING_DATA_COMPLETENESS_CONDITION_MARKER_FILE,
                    INCOMING_DATA_COMPLETENESS_CONDITION_AUTODETECTION));
        }
    }

    final void check()
    {
        if (createIncomingDirectories && incomingDataDirectory.exists() == false)
        {
            incomingDataDirectory.mkdirs();
            operationLog.info("Created incoming directory '" + incomingDataDirectory + "'.");
        }
        if (incomingDataDirectory.isDirectory() == false)
        {
            throw new ConfigurationFailureException("Incoming directory '" + incomingDataDirectory
                    + "' is not a directory.");
        }
    }

    @Private
    static File extractIncomingDataDir(final Properties threadProperties, String threadName2)
    {
        final String incomingDir = threadProperties.getProperty(INCOMING_DIR);
        if (StringUtils.isNotBlank(incomingDir))
        {
            return FileUtilities.normalizeFile(new File(incomingDir));
        } else
        {
            throw new ConfigurationFailureException("No '" + INCOMING_DIR + "' defined for input ["
                    + threadName2 + "].");
        }
    }

    @Private
    static final String tryGetGroupCode(final Properties properties)
    {
        return nullIfEmpty(PropertyUtils.getProperty(properties, GROUP_CODE_KEY));
    }

    @Private
    static final String tryGetPreRegistrationScript(final Properties properties)
    {
        return nullIfEmpty(PropertyUtils.getProperty(properties, PRE_REGISTRATION_SCRIPT_KEY));
    }

    @Private
    static final String tryGetPostRegistartionScript(final Properties properties)
    {
        return nullIfEmpty(PropertyUtils.getProperty(properties, POST_REGISTRATION_SCRIPT_KEY));
    }

    @Private
    static final String[] tryGetValidationScripts(final Properties properties)
    {
        String pathsString =
                nullIfEmpty(PropertyUtils.getProperty(properties, VALIDATION_SCRIPT_KEY));
        if (pathsString == null)
        {
            return null;
        }

        String[] paths = pathsString.split(",");
        for (int i = 0; i < paths.length; i++)
        {
            String path = paths[i].trim();
            if (StringUtils.isBlank(path))
            {
                throw new ConfigurationFailureException(i
                        + "-th path to validation script (property '" + VALIDATION_SCRIPT_KEY
                        + "') is blank.");
            } else
            {
                paths[i] = path;
            }
        }
        return paths;
    }

    @Private
    static final Integer tryGetIncomingShareId(final Properties properties)
    {
        String shareId = PropertyUtils.getProperty(properties, INCOMING_SHARE_ID);
        if (StringUtils.isBlank(shareId))
        {
            return null;
        }
        if (SegmentedStoreUtils.SHARE_ID_PATTERN.matcher(shareId).matches() == false)
        {
            throw new ConfigurationFailureException("Invalid incoming share Id:" + shareId);
        }
        return Integer.parseInt(shareId);
    }

    private static Long tryGetIncomingShareMinimumFreeSpace(Properties properties)
    {
        double min = PropertyUtils.getDouble(properties, INCOMING_SHARE_MINIMUM_FREE_SPACE_IN_GB, -1);
        return min < 0 ? null : (long) (min * FileUtils.ONE_GB);
    }

    private static String nullIfEmpty(String value)
    {
        return StringUtils.defaultIfEmpty(value, null);
    }

    /**
     * Returns the <code>group-code</code> property specified for this thread.
     */
    final String tryGetGroupCode()
    {
        return groupCode;
    }

    public final String tryGetPreRegistrationScript()
    {
        return preRegistrationScript;
    }

    public final String tryGetPostRegistrationScript()
    {
        return postRegistrationScript;
    }

    public String[] tryGetValidationScripts()
    {
        return validationScripts;
    }

    public boolean useIsFinishedMarkerFile()
    {
        return useIsFinishedMarkerFile;
    }

    /**
     * Returns The directory to monitor for incoming data.
     */
    public final File getIncomingDataDirectory()
    {
        return incomingDataDirectory;
    }

    public Class<?> getTopLevelDataSetRegistratorClass(Class<?> defaultClass)
    {
        return (topLevelDataSetRegistratorClassOrNull == null) ? defaultClass
                : topLevelDataSetRegistratorClassOrNull;
    }

    public Class<?> getOnErrorActionDecisionClass(Class<?> defaultClass)
    {
        return (onErrorDecisionClassOrNull == null) ? defaultClass : onErrorDecisionClassOrNull;
    }

    public Properties getThreadProperties()
    {
        return threadProperties;
    }

    /**
     * Logs the current parameters to the {@link LogCategory#OPERATION} log.
     */
    final void log()
    {
        if (operationLog.isInfoEnabled())
        {
            logLine("Top-level registrator: '%s'",
                    (null == topLevelDataSetRegistratorClassOrNull) ? TransferredDataSetHandler.class
                            .getName() : topLevelDataSetRegistratorClassOrNull.getName());
            if (null == topLevelDataSetRegistratorClassOrNull)
            {
                IETLServerPlugin plugin = ETLServerPluginFactory.getPluginForThread(this);
                logLine("Code extractor: '%s'", plugin.getDataSetInfoExtractor().getClass()
                        .getName());
                logLine("Type extractor: '%s'", plugin.getTypeExtractor().getClass().getName());
            }
            logLine("Incoming data directory: '%s'.", getIncomingDataDirectory().getAbsolutePath());
            if (groupCode != null)
            {
                logLine("Space code: '%s'.", groupCode);
            }
            String completenessCond =
                    useIsFinishedMarkerFile ? "marker file exists"
                            : "no write access for some period";
            logLine("Condition of incoming data completeness: %s.", completenessCond);
            logLine("Delete unidentified: '%s'.", deleteUnidentified);
            if (postRegistrationScript != null)
            {
                logLine("Post registration script: '%s'.", postRegistrationScript);
            }
        }
    }

    private void logLine(String format, Object... params)
    {
        Vector<Object> allParams = new Vector<Object>();
        allParams.add(threadName);
        allParams.addAll(Arrays.asList(params));
        operationLog.info(String.format("[%s] " + format, allParams.toArray(new Object[0])));
    }

    public String getThreadName()
    {
        return threadName;
    }

    public boolean deleteUnidentified()
    {
        return deleteUnidentified;
    }

    public boolean reprocessFaultyDatasets()
    {
        return reprocessFaultyDatasets;
    }

    public DataSetRegistrationPreStagingBehavior getDataSetRegistrationPreStagingBehavior()
    {
        return dataSetRegistrationPreStagingBehavior;
    }

    public int getDataSetRegistrationMaxRetryCount()
    {
        return dataSetRegistrationMaxRetryCount;
    }

    public int getDataSetRegistrationPauseInSec()
    {
        return dataSetRegistrationRetryPauseInSec;
    }

    public int getProcessMaxRetryCount()
    {
        return processMaxRetryCount;
    }

    public int getProcessRetryPauseInSec()
    {
        return processRetryPauseInSec;
    }

    public int getMaximumRecoveryCount()
    {
        return maximumRecoveryCount;
    }

    public int getMinimumRecoveryPeriod()
    {
        return minimumRecoveryPeriod;
    }

    public boolean hasH5AsFolders()
    {
        return h5Folders;
    }

    public boolean hasH5ArAsFolders()
    {
        return h5arFolders;
    }
}

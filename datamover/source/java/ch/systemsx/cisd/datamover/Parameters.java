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

package ch.systemsx.cisd.datamover;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.ExampleMode;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.LongOptionHandler;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Setter;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.HighLevelException;
import ch.systemsx.cisd.common.highwatermark.FileWithHighwaterMark;
import ch.systemsx.cisd.common.highwatermark.HighwaterMarkWatcher;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.BuildAndEnvironmentInfo;
import ch.systemsx.cisd.common.utilities.IExitHandler;
import ch.systemsx.cisd.common.utilities.OSUtilities;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.common.utilities.SystemExit;
import ch.systemsx.cisd.datamover.filesystem.FileStoreFactory;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileSysOperationsFactory;
import ch.systemsx.cisd.datamover.intf.IFileSysParameters;
import ch.systemsx.cisd.datamover.intf.ITimingParameters;

/**
 * The class to process the command line parameters.
 * 
 * @author Bernd Rinn
 */
public final class Parameters implements ITimingParameters, IFileSysParameters
{
    static final String DATA_COMPLETED_SCRIPT_NOT_FOUND_TEMPLATE =
            "Cannot find script '%s' for data completed filter.";

    static final int DEFAULT_DATA_COMPLETED_SCRIPT_TIMEOUT = 600;

    private final static String HIGHWATER_MARK_TEXT = "(high water mark in KB after the ':')";

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, Parameters.class);

    private static final Logger notificationLog =
            LogFactory.getLogger(LogCategory.NOTIFY, Parameters.class);

    @Option(longName = PropertyNames.DATA_COMPLETED_SCRIPT, metaVar = "EXEC", usage = "Optional script "
            + "which checks whether incoming data is complete or not.")
    private String dataCompletedScript;

    @Option(longName = PropertyNames.DATA_COMPLETED_SCRIPT_TIMEOUT, usage = "Timeout (in seconds) after which data completed script will be stopped "
            + "[default: " + DEFAULT_DATA_COMPLETED_SCRIPT_TIMEOUT + "]", handler = MillisecondConversionOptionHandler.class)
    private long dataCompletedScriptTimeout = DEFAULT_DATA_COMPLETED_SCRIPT_TIMEOUT;

    /**
     * The name of the <code>rsync</code> executable to use for copy operations.
     */
    @Option(longName = PropertyNames.RSYNC_EXECUTABLE, metaVar = "EXEC", usage = "The rsync executable to use for "
            + "copy operations.")
    private String rsyncExecutable = null;

    /**
     * Default of whether rsync should use overwrite or append mode, if append mode is available.
     */
    private static final boolean DEFAULT_RSYNC_OVERWRITE = false;

    /**
     * If set to <code>true</code>, rsync is called in such a way to files that already exist are
     * overwritten rather than appended to.
     */
    @Option(longName = PropertyNames.RSYNC_OVERWRITE, usage = "If true, files that already exist on the remote side are always "
            + "overwritten rather than appended.")
    private boolean rsyncOverwrite = DEFAULT_RSYNC_OVERWRITE;

    /**
     * The name of the <code>ssh</code> executable to use for creating tunnels.
     */
    @Option(longName = PropertyNames.SSH_EXECUTABLE, metaVar = "EXEC", usage = "The ssh executable to use for creating tunnels.")
    private String sshExecutable = null;

    /**
     * The path to the <code>ln</code> executable file for creating hard links.
     */
    @Option(longName = PropertyNames.HARD_LINK_EXECUTABLE, metaVar = "EXEC", usage = "The executable to use for creating hard links.")
    private String hardLinkExecutable = null;

    /**
     * The path to the <code>find</code> executable file on the incoming host.
     */
    @Option(longName = PropertyNames.INCOMING_HOST_FIND_EXECUTABLE, metaVar = "EXEC", usage = "The executable to use for checking the last modification time of files on the remote incoming host."
            + " It should be a GNU find supporting -printf option."
            + " Useful only in SSH tunneling mode when incoming-host is specified.")
    private String incomingHostFindExecutableOrNull = null;

    /**
     * The path to the <code>find</code> executable file on the incoming host.
     */
    @Option(longName = PropertyNames.OUTGOING_HOST_FIND_EXECUTABLE, metaVar = "EXEC", usage = "The executable to use for checking the last modification time of files on the remote outgoing host."
            + " It should be a GNU find supporting -printf option."
            + " Useful only in SSH tunneling mode when outgoing-host is specified.")
    private String outgoingHostFindExecutableOrNull = null;

    /**
     * Default interval to wait between two checks for activity (in seconds)
     */
    static final int DEFAULT_CHECK_INTERVAL = 60;

    /**
     * The interval to wait between two checks for activity (in milliseconds).
     */
    @Option(name = "c", longName = PropertyNames.CHECK_INTERVAL, usage = "The interval to wait between two checks (in seconds) "
            + "[default: 60]", handler = MillisecondConversionOptionHandler.class)
    private long checkIntervalMillis = DEFAULT_CHECK_INTERVAL;

    /**
     * Default interval to wait between two checks for activity for the internal processing queues
     * (in seconds)
     */
    static final int DEFAULT_CHECK_INTERVAL_INTERNAL = 10;

    /**
     * The interval to wait between two checks for activity for the internal processing queues (in
     * milliseconds).
     */
    @Option(longName = PropertyNames.CHECK_INTERVAL_INTERNAL, usage = "The interval to wait between two checks for the internal processing queues (in seconds) "
            + "[default: 10]", handler = MillisecondConversionOptionHandler.class)
    private long checkIntervalInternalMillis = DEFAULT_CHECK_INTERVAL_INTERNAL;

    /**
     * Default period to wait before a file or directory is considered "inactive" or "stalled" (in
     * seconds).
     */
    static final int DEFAULT_INACTIVITY_PERIOD = 600;

    /**
     * The period to wait before a file or directory is considered "inactive" or "stalled" (in
     * milliseconds). This setting is used when deciding whether a copy operation of a file or
     * directory is "stalled".
     */
    @Option(name = "i", longName = PropertyNames.INACTIVITY_PERIOD, usage = "The period to wait before a file or directory is "
            + "considered \"inactive\" or \"stalled\" (in seconds) [default: 600].", handler = MillisecondConversionOptionHandler.class)
    private long inactivityPeriodMillis = DEFAULT_INACTIVITY_PERIOD;

    /**
     * Default period to wait before a file or directory is considered "quiet" (in seconds).
     */
    static final int DEFAULT_QUIET_PERIOD = 300;

    /**
     * The period to wait before a file or directory is considered "quiet" (in milliseconds). This
     * setting is used when deciding whether a file or directory is ready to be moved to the remote
     * side.
     */
    @Option(name = "q", longName = PropertyNames.QUIET_PERIOD, usage = "The period that needs to pass before a path item is "
            + "considered quiet (in seconds) [default: 300].", handler = MillisecondConversionOptionHandler.class)
    private long quietPeriodMillis = DEFAULT_QUIET_PERIOD;

    /**
     * Default period to wait before a file or directory is considered "quiet" (in seconds).
     */
    static final int DEFAULT_INTERVAL_TO_WAIT_AFTER_FAILURES = 1800;

    /**
     * The time interval to wait after a failure has occurred before the operation is retried (in
     * milliseconds).
     */
    @Option(name = "f", longName = PropertyNames.FAILURE_INTERVAL, usage = "The interval to wait after a failure has occurred "
            + "before retrying the operation (in seconds) [default: 1800].", handler = MillisecondConversionOptionHandler.class)
    private long intervalToWaitAfterFailureMillis = DEFAULT_INTERVAL_TO_WAIT_AFTER_FAILURES;

    /**
     * Default treatment of the incoming data directory - should it be treated as on a remote share?
     */
    private static final boolean DEFAULT_TREAT_INCOMING_AS_REMOTE = false;

    /**
     * If set to true, than directory with incoming data is supposed to be on a remote share. It
     * implies that a special care will be taken when coping is performed from that directory.
     */
    @Option(name = "r", longName = PropertyNames.TREAT_INCOMING_AS_REMOTE, usage = "If flag is set, then directory with incoming data "
            + "is supposed to be on a remote share.")
    private boolean treatIncomingAsRemote = DEFAULT_TREAT_INCOMING_AS_REMOTE;

    /**
     * Default number of retries after a failure has occurred.
     */
    static final int DEFAULT_MAXIMAL_NUMBER_OF_RETRIES = 10;

    /**
     * The number of times a failed operation is retried (note that this means that the total number
     * that the operation is tried is one more).
     */
    @Option(name = "m", longName = PropertyNames.MAX_RETRIES, usage = "The number of retries of a failed operation before the "
            + "Datamover gives up on it. [default: 10].")
    private int maximalNumberOfRetries = DEFAULT_MAXIMAL_NUMBER_OF_RETRIES;

    /**
     * The remote host to copy the data from or null if data are available on a local/remote share
     */
    @Option(longName = PropertyNames.INCOMING_HOST, metaVar = "HOST", usage = "The remote host to move the data from  using ssh tunneling")
    private String incomingHost = null;

    /**
     * The directory to monitor for new files and directories to move to outgoing.
     */
    @Option(longName = PropertyNames.INCOMING_DIR, metaVar = "DIR", usage = "The directory where the data producer writes to.")
    private File incomingDirectory = null;

    /**
     * The directory for local files and directories manipulations.
     */
    @Option(longName = PropertyNames.BUFFER_DIR, usage = "The local directory to "
            + "store the paths to be transfered temporarily " + HIGHWATER_MARK_TEXT + ".", handler = FileWithHighwaterMarkHandler.class)
    private FileWithHighwaterMark bufferDirectory = null;

    /**
     * The directory to move files and directories to that have been quiet in the incoming directory
     * for long enough and that need manual intervention. Note that this directory needs to be on
     * the same file system than {@link #bufferDirectory}.
     */
    @Option(longName = PropertyNames.MANUAL_INTERVENTION_DIR, metaVar = "DIR", usage = "The local directory to "
            + "store paths that need manual intervention.")
    private File manualInterventionDirectoryOrNull = null;

    /**
     * The directory on the remote side to move the paths to from the buffer directory.
     */
    @Option(longName = PropertyNames.OUTGOING_DIR, usage = "The remote directory to move the data to "
            + HIGHWATER_MARK_TEXT + ".", handler = FileWithHighwaterMarkHandler.class)
    FileWithHighwaterMark outgoingDirectory = null;

    /**
     * The remote host to copy the data to (only with rsync, will use an ssh tunnel).
     */
    @Option(longName = PropertyNames.OUTGOING_HOST, metaVar = "HOST", usage = "The remote host to "
            + "move the data to using ssh tunneling.")
    private String outgoingHost = null;

    /**
     * The local directory where we create additional copy of the incoming data (if and only if the
     * directory is specified)
     */
    @Option(longName = PropertyNames.EXTRA_COPY_DIR, metaVar = "DIR", usage = "The local directory where we create additional "
            + "copy of the incoming data.")
    private File extraCopyDirectory = null;

    /**
     * The regular expression to use for cleansing on the incoming directory before moving it to the
     * buffer.
     */
    @Option(longName = PropertyNames.CLEANSING_REGEX, usage = "The regular expression to use for cleansing before "
            + "moving to outgoing.")
    private Pattern cleansingRegex = null;

    /**
     * The regular expression to use for deciding whether a path in the incoming directory needs
     * manual intervention.
     */
    @Option(longName = PropertyNames.MANUAL_INTERVENTION_REGEX, usage = "The regular expression to use for deciding whether an "
            + "incoming paths needs manual intervention. ")
    private Pattern manualInterventionRegex = null;

    /**
     * A prefix for all incoming items. Note that '%t' will be replaced with the current timestamp
     * in format 'yyyyMMddHHmmss'.
     */
    @Option(longName = PropertyNames.PREFIX_FOR_INCOMING, usage = "A string that all incoming items will be prepended with, "
            + "'%t' will be replaced with the current time stamp.")
    private String prefixForIncoming = "";

    /**
     * The command line parser.
     */
    private final CmdLineParser parser = new CmdLineParser(this);

    @Option(longName = "help", skipForExample = true, usage = "Prints out a description of the options.")
    void printHelp(final boolean exit)
    {
        parser.printHelp("Datamover", "<required options> [option [...]]", "", ExampleMode.ALL);
        if (exit)
        {
            System.exit(0);
        }
    }

    @Option(longName = "version", skipForExample = true, usage = "Prints out the version information.")
    void printVersion(final boolean exit)
    {
        System.err
                .println("Datamover version " + BuildAndEnvironmentInfo.INSTANCE.getFullVersion());
        if (exit)
        {
            System.exit(0);
        }
    }

    @Option(longName = "test-notify", skipForExample = true, usage = "Tests the notify log (i.e. that an email is "
            + "sent out).")
    void sendTestNotification(final boolean exit)
    {
        notificationLog
                .info("This is a test notification given due to specifying the --test-notify option.");
        if (exit)
        {
            System.exit(0);
        }
    }

    static final String INCOMING_KIND_DESC = "incoming";

    static final String MANUAL_INTERVENTION_KIND_DESC = "manual intervention";

    static final String BUFFER_KIND_DESC = "buffer";

    static final String OUTGOING_KIND_DESC = "outgoing";

    Parameters(final String[] args)
    {
        this(args, SystemExit.SYSTEM_EXIT);
    }

    Parameters(final String[] args, final IExitHandler systemExitHandler)
    {
        initParametersFromProperties();
        try
        {
            parser.parseArgument(args);
            if (incomingDirectory == null)
            {
                throw createConfigurationFailureException(PropertyNames.INCOMING_DIR);
            }
            if (bufferDirectory == null)
            {
                throw createConfigurationFailureException(PropertyNames.BUFFER_DIR);
            }
            if (outgoingDirectory == null)
            {
                throw createConfigurationFailureException(PropertyNames.OUTGOING_DIR);
            }
            if (manualInterventionDirectoryOrNull == null && manualInterventionRegex != null)
            {
                throw ConfigurationFailureException.fromTemplate("No '%s' defined, but '%s'.",
                        PropertyNames.MANUAL_INTERVENTION_DIR,
                        PropertyNames.MANUAL_INTERVENTION_REGEX);
            }
            if (getDataCompletedScript() != null)
            {
                if (OSUtilities.executableExists(dataCompletedScript) == false)
                {
                    throw ConfigurationFailureException.fromTemplate(
                            DATA_COMPLETED_SCRIPT_NOT_FOUND_TEMPLATE, dataCompletedScript);
                }
            }
        } catch (final Exception ex)
        {
            outputException(ex);
            systemExitHandler.exit(1);
            // Only reached in unit tests.
            throw new AssertionError(ex.getMessage());
        }
    }

    private final static ConfigurationFailureException createConfigurationFailureException(
            final String propertyKey)
    {
        return ConfigurationFailureException.fromTemplate("No '%s' defined.", propertyKey);
    }

    private final void outputException(final Exception ex)
    {
        if (ex instanceof HighLevelException || ex instanceof CmdLineException)
        {
            System.err.println(ex.getMessage());
        } else
        {
            System.err.println("An exception occurred.");
            ex.printStackTrace();
        }
        if (ex instanceof CmdLineException)
        {
            printHelp(false);
        }
    }

    private final static long toMillis(final long seconds)
    {
        return seconds * DateUtils.MILLIS_PER_SECOND;
    }

    private final void initParametersFromProperties()
    {
        final Properties serviceProperties = loadServiceProperties();
        dataCompletedScript =
                PropertyUtils.getProperty(serviceProperties, PropertyNames.DATA_COMPLETED_SCRIPT,
                        dataCompletedScript);
        dataCompletedScriptTimeout =
                toMillis(PropertyUtils.getPosLong(serviceProperties,
                        PropertyNames.DATA_COMPLETED_SCRIPT_TIMEOUT, dataCompletedScriptTimeout));
        rsyncExecutable =
                PropertyUtils.getProperty(serviceProperties, PropertyNames.RSYNC_EXECUTABLE,
                        rsyncExecutable);
        rsyncOverwrite =
                PropertyUtils.getBoolean(serviceProperties, PropertyNames.RSYNC_OVERWRITE,
                        rsyncOverwrite);
        sshExecutable =
                PropertyUtils.getProperty(serviceProperties, PropertyNames.SSH_EXECUTABLE,
                        sshExecutable);
        hardLinkExecutable =
                PropertyUtils.getProperty(serviceProperties, PropertyNames.HARD_LINK_EXECUTABLE,
                        hardLinkExecutable);
        incomingHostFindExecutableOrNull =
                PropertyUtils.getProperty(serviceProperties,
                        PropertyNames.INCOMING_HOST_FIND_EXECUTABLE,
                        incomingHostFindExecutableOrNull);
        outgoingHostFindExecutableOrNull =
                PropertyUtils.getProperty(serviceProperties,
                        PropertyNames.OUTGOING_HOST_FIND_EXECUTABLE,
                        outgoingHostFindExecutableOrNull);
        checkIntervalMillis =
                toMillis(PropertyUtils.getPosLong(serviceProperties, PropertyNames.CHECK_INTERVAL,
                        checkIntervalMillis));
        checkIntervalInternalMillis =
                toMillis(PropertyUtils.getPosLong(serviceProperties,
                        PropertyNames.CHECK_INTERVAL_INTERNAL, checkIntervalInternalMillis));
        inactivityPeriodMillis =
                toMillis(PropertyUtils.getPosLong(serviceProperties,
                        PropertyNames.INACTIVITY_PERIOD, inactivityPeriodMillis));
        quietPeriodMillis =
                toMillis(PropertyUtils.getPosLong(serviceProperties, PropertyNames.QUIET_PERIOD,
                        quietPeriodMillis));
        intervalToWaitAfterFailureMillis =
                toMillis(PropertyUtils.getPosLong(serviceProperties,
                        PropertyNames.FAILURE_INTERVAL, intervalToWaitAfterFailureMillis));
        maximalNumberOfRetries =
                PropertyUtils.getPosInt(serviceProperties, PropertyNames.MAX_RETRIES,
                        maximalNumberOfRetries);
        treatIncomingAsRemote =
                PropertyUtils.getBoolean(serviceProperties, PropertyNames.TREAT_INCOMING_AS_REMOTE,
                        treatIncomingAsRemote);
        prefixForIncoming =
                PropertyUtils.getProperty(serviceProperties, PropertyNames.PREFIX_FOR_INCOMING,
                        prefixForIncoming);
        incomingDirectory =
                tryCreateFile(serviceProperties, PropertyNames.INCOMING_DIR, incomingDirectory);
        incomingHost = PropertyUtils.getProperty(serviceProperties, PropertyNames.INCOMING_HOST);
        if (serviceProperties.getProperty(PropertyNames.BUFFER_DIR) != null)
        {
            bufferDirectory =
                    FileWithHighwaterMark.fromProperties(serviceProperties,
                            PropertyNames.BUFFER_DIR);
        }
        manualInterventionDirectoryOrNull =
                tryCreateFile(serviceProperties, PropertyNames.MANUAL_INTERVENTION_DIR,
                        manualInterventionDirectoryOrNull);
        if (serviceProperties.getProperty(PropertyNames.OUTGOING_DIR) != null)
        {
            outgoingDirectory =
                    FileWithHighwaterMark.fromProperties(serviceProperties,
                            PropertyNames.OUTGOING_DIR);
        }
        outgoingHost = serviceProperties.getProperty(PropertyNames.OUTGOING_HOST);
        extraCopyDirectory =
                tryCreateFile(serviceProperties, PropertyNames.EXTRA_COPY_DIR, extraCopyDirectory);
        if (serviceProperties.getProperty(PropertyNames.CLEANSING_REGEX) != null)
        {
            cleansingRegex =
                    Pattern.compile(serviceProperties.getProperty(PropertyNames.CLEANSING_REGEX));
        }
        if (serviceProperties.getProperty(PropertyNames.MANUAL_INTERVENTION_REGEX) != null)
        {
            manualInterventionRegex =
                    Pattern.compile(serviceProperties
                            .getProperty(PropertyNames.MANUAL_INTERVENTION_REGEX));
        }
    }

    private final File tryCreateFile(final Properties serviceProperties, final String propertyKey,
            final File defaultValue)
    {
        final String propertyValue = PropertyUtils.getProperty(serviceProperties, propertyKey);
        if (propertyValue != null)
        {
            return new File(propertyValue);
        } else
        {
            return defaultValue;
        }
    }

    /**
     * Returns the service property.
     * 
     * @throws ConfigurationFailureException If an exception occurs when loading the service
     *             properties.
     */
    private final static Properties loadServiceProperties()
    {
        final Properties properties = new Properties();
        try
        {
            final InputStream is = new FileInputStream(PropertyNames.SERVICE_PROPERTIES_FILE);
            try
            {
                properties.load(is);
                return properties;
            } finally
            {
                IOUtils.closeQuietly(is);
            }
        } catch (final Exception ex)
        {
            final String msg =
                    "Could not load the service properties from resource '"
                            + PropertyNames.SERVICE_PROPERTIES_FILE + "'.";
            operationLog.warn(msg, ex);
            throw new ConfigurationFailureException(msg, ex);
        }
    }

    public final String getDataCompletedScript()
    {
        return StringUtils.defaultIfEmpty(dataCompletedScript, null);
    }

    public final long getDataCompletedScriptTimeout()
    {
        return dataCompletedScriptTimeout;
    }

    /**
     * @return The name of the <code>rsync</code> executable to use for copy operations.
     */
    public final String getRsyncExecutable()
    {
        return rsyncExecutable;
    }

    /**
     * @return <code>true</code>, if rsync is called in such a way to files that already exist
     *         are overwritten rather than appended to.
     */
    public final boolean isRsyncOverwrite()
    {
        return rsyncOverwrite;
    }

    /**
     * @return The name of the <code>ssh</code> executable to use for creating tunnels.
     */
    public final String getSshExecutable()
    {
        return sshExecutable;
    }

    /**
     * @return The name of the <code>ln</code> executable to use for creating hard links or
     *         <code>null</code> if not specified.
     */
    public final String getHardLinkExecutable()
    {
        return hardLinkExecutable;
    }

    /**
     * @return The interval to wait between two checks for activity (in milliseconds).
     */
    public final long getCheckIntervalMillis()
    {
        return checkIntervalMillis;
    }

    /**
     * @return The interval to wait between two checks for activity for the internal threads (in
     *         milliseconds).
     */
    public final long getCheckIntervalInternalMillis()
    {
        return checkIntervalInternalMillis;
    }

    /**
     * @return The period to wait before a file or directory is considered "inactive" (in
     *         milliseconds). This setting is used when deciding whether a copy operation of a file
     *         or directory is "stalled".
     */
    public final long getInactivityPeriodMillis()
    {
        return inactivityPeriodMillis;
    }

    /**
     * @return The period to wait before a file or directory is considered "quiet" (in
     *         milliseconds). This setting is used when deciding whether a file or directory is
     *         ready to be moved to the remote side.
     */
    public final long getQuietPeriodMillis()
    {
        return quietPeriodMillis;
    }

    /**
     * @return The time interval to wait after a failure has occurred before the operation is
     *         retried.
     */
    public final long getIntervalToWaitAfterFailure()
    {
        return intervalToWaitAfterFailureMillis;
    }

    /**
     * @return The number of times a failed operation is retried (note that this means that the
     *         total number that the operation is tried is one more).
     */
    public final int getMaximalNumberOfRetries()
    {
        return maximalNumberOfRetries;
    }

    /**
     * @return The store to monitor for new files and directories to move to the buffer.
     */
    public final IFileStore getIncomingStore(final IFileSysOperationsFactory factory)
    {
        return FileStoreFactory.createStore(incomingDirectory, INCOMING_KIND_DESC, incomingHost,
                treatIncomingAsRemote, factory, incomingHostFindExecutableOrNull);
    }

    /**
     * @return The directory for local files and directories manipulations.
     */
    public final FileWithHighwaterMark getBufferDirectoryPath()
    {
        return bufferDirectory;
    }

    /**
     * @return The store to copy the data to.
     */
    public final IFileStore getOutgoingStore(final IFileSysOperationsFactory factory)
    {
        return FileStoreFactory.createStore(outgoingDirectory, OUTGOING_KIND_DESC, outgoingHost,
                true, factory, outgoingHostFindExecutableOrNull);
    }

    /**
     * @return The directory to move files and directories to that have been quiet in the local data
     *         directory for long enough and that need manual intervention. Note that this directory
     *         needs to be on the same file system as {@link #getBufferDirectoryPath}.
     */
    public final File tryGetManualInterventionDir()
    {
        return manualInterventionDirectoryOrNull;
    }

    /**
     * @return The directory where we create an additional copy of incoming data or
     *         <code>null</code> if it is not specified. Note that this directory needs to be on
     *         the same file system as {@link #getBufferDirectoryPath}.
     */
    public final File tryGetExtraCopyDir()
    {
        return extraCopyDirectory;
    }

    /**
     * @return The regular expression to use for cleansing on the incoming directory before moving
     *         it to the buffer or <code>null</code>, if no regular expression for cleansing has
     *         been provided.
     */
    public final Pattern tryGetCleansingRegex()
    {
        return cleansingRegex;
    }

    /**
     * @return The regular expression to use for deciding whether a path in the incoming directory
     *         requires manual intervention or <code>null</code>, if no regular expression for
     *         manual intervention paths has been provided.
     */
    public final Pattern tryGetManualInterventionRegex()
    {
        return manualInterventionRegex;
    }

    /**
     * @return The prefix string to put in front of all incoming items. Note that '%t' will be
     *         replaced with the current time stamp.
     */
    public final String getPrefixForIncoming()
    {
        return prefixForIncoming;
    }

    /**
     * Logs the current parameters to the {@link LogCategory#OPERATION} log.
     */
    public final void log()
    {
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("Incoming directory: '%s'.", incomingDirectory
                    .getAbsolutePath()));
            if (null != incomingHost)
            {
                operationLog.info(String.format("Incoming host: '%s'.", incomingHost));
            }
            operationLog.info(String.format("Is incoming directory remote: %b.",
                    treatIncomingAsRemote));
            operationLog.info(String.format("Buffer directory: '%s' [high water mark: %s].",
                    bufferDirectory.getCanonicalPath(), HighwaterMarkWatcher
                            .displayKilobyteValue(bufferDirectory.getHighwaterMark())));
            operationLog.info(String.format("Outgoing directory: '%s' [high water mark: %s].",
                    outgoingDirectory.getCanonicalPath(), HighwaterMarkWatcher
                            .displayKilobyteValue(outgoingDirectory.getHighwaterMark())));
            if (null != outgoingHost)
            {
                operationLog.info(String.format("Outgoing host: '%s'.", outgoingHost));
            }
            if (null != tryGetManualInterventionDir())
            {
                operationLog.info(String.format("Manual interventions directory: '%s'.",
                        tryGetManualInterventionDir().getAbsolutePath()));
            }
            if (null != extraCopyDirectory)
            {
                operationLog.info(String.format("Extra copy directory: '%s'.", extraCopyDirectory
                        .getAbsolutePath()));
            }
            operationLog.info(String.format("Check intervall (external): %s s.",
                    DurationFormatUtils.formatDuration(getCheckIntervalMillis(), "s")));
            operationLog.info(String.format("Check intervall (internal): %s s.",
                    DurationFormatUtils.formatDuration(getCheckIntervalInternalMillis(), "s")));
            operationLog.info(String.format("Quiet period: %s s.", DurationFormatUtils
                    .formatDuration(getQuietPeriodMillis(), "s")));
            operationLog.info(String.format("Inactivity (stall) period: %s s.", DurationFormatUtils
                    .formatDuration(getInactivityPeriodMillis(), "s")));
            operationLog.info(String.format("Intervall to wait after failure: %s s.",
                    DurationFormatUtils.formatDuration(getIntervalToWaitAfterFailure(), "s")));
            operationLog.info(String.format("Maximum number of retries: %d.",
                    getMaximalNumberOfRetries()));
            if (tryGetCleansingRegex() != null)
            {
                operationLog.info(String.format(
                        "Regular expression used for cleansing before moving: '%s'",
                        tryGetCleansingRegex().pattern()));
            }
            if (tryGetManualInterventionRegex() != null)
            {
                operationLog.info(String.format(
                        "Regular expression used for deciding whether a path "
                                + "needs manual intervention: '%s'",
                        tryGetManualInterventionRegex().pattern()));
            }
            if (getDataCompletedScript() != null)
            {
                operationLog.info(String.format("Data completed script: '%s' [timeout: %d s.].",
                        getDataCompletedScript(), getDataCompletedScriptTimeout()));
            }
        }
    }

    //
    // Helper classes
    //

    /**
     * A class which converts <code>long</code> options given in seconds to milli-seconds.
     */
    public final static class MillisecondConversionOptionHandler extends LongOptionHandler
    {
        public MillisecondConversionOptionHandler(final Option option,
                final Setter<? super Long> setter)
        {
            super(option, setter);
        }

        //
        // LongOptionHandler
        //

        @Override
        public final void set(final long value) throws CmdLineException
        {
            setter.addValue(toMillis(value));
        }

    }

    public final static class FileWithHighwaterMarkHandler extends
            OptionHandler<FileWithHighwaterMark>
    {

        static final char SEP = ':';

        private String argument;

        public FileWithHighwaterMarkHandler(final Option option,
                final Setter<FileWithHighwaterMark> setter)
        {
            super(option, setter);
        }

        //
        // OptionHandler
        //

        @Override
        public final String getDefaultMetaVariable()
        {
            return "DIR[" + SEP + "KB]";
        }

        @Override
        public final int parseArguments(final org.kohsuke.args4j.spi.Parameters params)
                throws CmdLineException
        {
            argument = params.getOptionName();
            set(params.getParameter(0));
            return 1;
        }

        @Override
        public final void set(final String value) throws CmdLineException
        {
            final File file;
            final long highwaterMark;
            final int indexOf = value.indexOf(SEP);
            if (indexOf > -1)
            {
                file = new File(value.substring(0, indexOf));
                String substring = null;
                try
                {
                    substring = value.substring(indexOf + 1);
                    highwaterMark = Long.valueOf(substring);
                } catch (final NumberFormatException ex)
                {
                    throw new CmdLineException(String.format(
                            "Wrong format for argument '%s': '%s' is not a number.", argument,
                            substring));
                }
            } else
            {
                file = new File(value);
                highwaterMark = FileWithHighwaterMark.DEFAULT_HIGHWATER_MARK;
            }
            setter.addValue(new FileWithHighwaterMark(file, highwaterMark));
        }
    }
}

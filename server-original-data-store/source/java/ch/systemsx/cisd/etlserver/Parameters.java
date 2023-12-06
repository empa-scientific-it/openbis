/*
 * Copyright ETH 2009 - 2023 Zürich, Scientific IT Services
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.args4j.CmdLineException;
import ch.systemsx.cisd.args4j.CmdLineParser;
import ch.systemsx.cisd.args4j.ExampleMode;
import ch.systemsx.cisd.args4j.Option;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.HighLevelException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.JavaMailProperties;
import ch.systemsx.cisd.common.maintenance.MaintenanceTaskParameters;
import ch.systemsx.cisd.common.maintenance.MaintenanceTaskUtils;
import ch.systemsx.cisd.common.properties.ExtendedProperties;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil.SectionProperties;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.time.TimingParameters;
import ch.systemsx.cisd.common.utilities.IExitHandler;
import ch.systemsx.cisd.common.utilities.SystemExit;
import ch.systemsx.cisd.common.utilities.VoidExitHandler;
import ch.systemsx.cisd.openbis.dss.BuildAndEnvironmentInfo;
import ch.systemsx.cisd.openbis.dss.generic.shared.Constants;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DssPropertyParametersUtil;

/**
 * The class to process the command line parameters and service properties.
 * 
 * @author Bernd Rinn
 */
public class Parameters
{
    private static final String CHECK_INTERVAL_NAME = "check-interval";

    private static final String QUIET_PERIOD_NAME = "quiet-period";

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, Parameters.class);

    private static final Logger notificationLog =
            LogFactory.getLogger(LogCategory.NOTIFY, Parameters.class);

    @Option(name = "s", longName = "server-url", metaVar = "URL", usage = "URL of the server")
    private String serverURL;

    /**
     * The interval to wait between to checks for activity (in milliseconds).
     */
    @Option(name = "c", longName = CHECK_INTERVAL_NAME, usage = "The interval to wait between two checks (in seconds) "
            + "[default: 120]")
    private long checkIntervalSeconds;

    /**
     * Valid only when {@link ThreadParameters#INCOMING_DATA_COMPLETENESS_CONDITION} is false.<br>
     * The period to wait before a file or directory is considered "quiet" (in milliseconds). This setting is used when deciding whether a file or
     * directory is ready to be processed.
     */
    @Option(name = "q", longName = QUIET_PERIOD_NAME, usage = "The period of no write access that needs to pass before an incoming data item is "
            + "considered complete and ready to be processed (in seconds) [default: 300]. "
            + "Valid only when auto-detection method is used to determine if an incoming data are ready to be processed.")
    private long quietPeriodMillis;

    /**
     * The time-out for clean up work in the shutdown sequence (in seconds).
     * <p>
     * Note that that the maximal time for the shutdown sequence to complete can be as large as twice this time.
     */
    @Option(name = "t", longName = "shutdown-timeout", usage = "The time-out for clean up work "
            + "in the shutdown sequence (in seconds) [default: 30]")
    private long shutdownTimeOutSeconds;

    /**
     * The username to access the LIMS server with.
     */
    @Option(name = "u", longName = "username", usage = "User login name")
    private String username;

    /**
     * The password to access the LIMS server with.
     */
    @Option(name = "p", longName = "password", usage = "User login password")
    private String password;

    /**
     * The command line parser.
     */
    private final CmdLineParser parser = new CmdLineParser(this);

    private final TimingParameters timingParameters;

    private final Properties serviceProperties;

    private final ThreadParameters[] threads;

    private MaintenanceTaskParameters[] maintenancePlugins;

    @Option(longName = "help", skipForExample = true, usage = "Prints out a description of the options.")
    void printHelp(final boolean exit)
    {
        parser.printHelp("etlserver", "<required options> [option [...]]", "", ExampleMode.ALL);
        if (exit)
        {
            System.exit(0);
        }
    }

    @Option(longName = "version", skipForExample = true, usage = "Prints out the version information.")
    void printVersion(final boolean exit)
    {
        System.err
                .println("etlserver version " + BuildAndEnvironmentInfo.INSTANCE.getFullVersion());
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
                .error("This is a test notification given due to specifying the --test-notify option.");
        if (exit)
        {
            System.exit(0);
        }
    }

    /**
     * This factory method is used by the DSS API to get information about the threads that have been configured. Since the validity of the parameters
     * has already been verified by the ETLServer startup, it does not need to be verified again.
     */
    public static Parameters createParametersForApiUse()
    {
        return new Parameters(new String[0], VoidExitHandler.INSTANCE, false);
    }

    Parameters(final String[] args)
    {
        this(args, SystemExit.SYSTEM_EXIT);
    }

    Parameters(final String[] args, final IExitHandler systemExitHandler)
    {
        this(args, systemExitHandler, true);
    }

    Parameters(final String[] args, final IExitHandler systemExitHandler, boolean checkParameters)
    {
        try
        {
            this.serviceProperties = DssPropertyParametersUtil.loadServiceProperties();
            PropertyUtils.trimProperties(serviceProperties);
            this.threads = createThreadParameters(serviceProperties);
            this.timingParameters = TimingParameters.create(serviceProperties);
            this.maintenancePlugins =
                    MaintenanceTaskUtils.createMaintenancePlugins(serviceProperties);

            initCommandLineParametersFromProperties();

            parser.parseArgument(args);
            if (checkParameters)
            {
                ensureParametersCorrect();
            }
        } catch (final Exception ex)
        {
            operationLog.error(ex.getMessage(), ex);

            outputException(ex);

            if (SystemExit.SYSTEM_EXIT.equals(systemExitHandler))
            {
                operationLog.error("DSS is shutting down.");
            }

            systemExitHandler.exit(1);
            // Only reached in unit tests.
            throw new AssertionError(ex.getMessage());
        }
    }

    public MaintenanceTaskParameters[] getMaintenancePlugins()
    {
        return maintenancePlugins;
    }

    private void ensureParametersCorrect()
    {
        for (final ThreadParameters thread : threads)
        {
            thread.check();
        }

        if (serverURL == null)
        {
            throw new ConfigurationFailureException("No 'server-url' defined.");
        }
    }

    private void initCommandLineParametersFromProperties()
    {
        serverURL = serviceProperties.getProperty("server-url");
        username = serviceProperties.getProperty("username");
        password = serviceProperties.getProperty("password");
        checkIntervalSeconds =
                Long.parseLong(serviceProperties.getProperty(CHECK_INTERVAL_NAME, "120"));
        quietPeriodMillis = Long.parseLong(serviceProperties.getProperty(QUIET_PERIOD_NAME, "300"));
        shutdownTimeOutSeconds =
                Long.parseLong(serviceProperties.getProperty("shutdown-timeout", "30"));
    }

    private static ThreadParameters[] createThreadParameters(final Properties serviceProperties)
    {
        SectionProperties[] sectionsProperties =
                PropertyParametersUtil.extractSectionProperties(serviceProperties,
                        Constants.INPUT_THREAD_NAMES, true);
        if (sectionsProperties.length == 0)
        {
            return new ThreadParameters[0];
        } else
        {
            return asThreadParameters(sectionsProperties);
        }
    }

    private static ThreadParameters[] asThreadParameters(SectionProperties[] sectionProperties)
    {
        List<ThreadParameters> threadParameters = new ArrayList<>();
        for (SectionProperties section : sectionProperties)
        {
            String incomingDirs = section.getProperties().getProperty("incoming-dirs");
            if (incomingDirs == null)
            {
                operationLog.info("Create parameters for thread '" + section.getKey() + "'.");
                threadParameters.add(new ThreadParameters(section.getProperties(), section.getKey()));
            } else
            {
                List<String> dirs = new ArrayList<>(new LinkedHashSet<>(Arrays.asList(incomingDirs.split(","))));
                for (int i = 0; i < dirs.size(); i++)
                {
                    Properties p = (Properties) section.getProperties().clone();
                    String incomingDir = dirs.get(i).trim();
                    p.setProperty(ThreadParameters.INCOMING_DIR, incomingDir);
                    String key = section.getKey() + "[" + (i + 1) + "]";
                    operationLog.info("Create parameters for thread '" + key 
                            + "' (incoming-dir: " + incomingDir + ").");
                    threadParameters.add(new ThreadParameters(p, key));
                }
            }
        }
        return threadParameters.toArray(new ThreadParameters[0]);
    }

    public final static Properties createMailProperties(final Properties serviceProperties)
    {
        final Properties properties =
                ExtendedProperties.getSubset(serviceProperties, "mail", false);
        if (properties.getProperty(JavaMailProperties.MAIL_SMTP_HOST) == null)
        {
            properties.setProperty(JavaMailProperties.MAIL_SMTP_HOST, "localhost");
        }
        if (properties.getProperty(JavaMailProperties.MAIL_FROM) == null)
        {
            properties.setProperty(JavaMailProperties.MAIL_FROM, "etlserver@localhost");
        }
        return properties;
    }

    private void outputException(final Exception ex)
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

    /**
     * Returns The interval to wait between to checks for activity (in milliseconds).
     */
    public long getCheckIntervalMillis()
    {
        return checkIntervalSeconds * DateUtils.MILLIS_PER_SECOND;
    }

    public long getQuietPeriodMillis()
    {
        return quietPeriodMillis * DateUtils.MILLIS_PER_SECOND;
    }

    /**
     * Returns the time-out for clean up work in the shutdown sequence (in seconds).
     * <p>
     * Note that that the maximal time for the shutdown sequence to complete can be as large as twice this time.
     */
    public long getShutdownTimeOutMillis()
    {
        return shutdownTimeOutSeconds * DateUtils.MILLIS_PER_SECOND;
    }

    /**
     * Returns the password to access the LIMS server with.
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * Returns the username to access the LIMS server with.
     */
    public String getUsername()
    {
        return username;
    }

    /**
     * Returns all properties.
     */
    public final Properties getProperties()
    {
        return serviceProperties;
    }

    /**
     * Returns the timing parameters for monitored operations.
     */
    public TimingParameters getTimingParameters()
    {
        return timingParameters;
    }

    /**
     * Logs the current parameters to the {@link LogCategory#OPERATION} log.
     */
    public void log()
    {
        if (operationLog.isInfoEnabled())
        {
            for (final ThreadParameters threadParameters : threads)
            {
                threadParameters.log();
            }

            operationLog.info(String.format("Check intervall: %d s.",
                    getCheckIntervalMillis() / 1000));
            operationLog
                    .info(String
                            .format(
                                    "Quiet period (valid if auto-detection method is used to determine incoming data completeness): %d s.",
                                    getQuietPeriodMillis() / 1000));
        }
    }

    /**
     * Returns the URL of the LIMS server.
     */
    public String getServerURL()
    {
        return serverURL;
    }

    public ThreadParameters[] getThreads()
    {
        return threads;
    }

}

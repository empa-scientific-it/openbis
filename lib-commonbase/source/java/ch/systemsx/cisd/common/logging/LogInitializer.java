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

package ch.systemsx.cisd.common.logging;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * Initializes the logging system. The {@link #init()} method needs to be called once at system startup.
 * 
 * @author Bernd Rinn
 */
public class LogInitializer
{
    private static final String LOG_DIRECTORY = "etc";

    private static final String LOG_FILENAME = "log.xml";

    private static final String FILE_URL_PREFIX = "file:";

    static
    {
        // Do not let log4j configure itself. We will do it our own way.
        // Look at page 84 of the manual to get to know the default initialization.
        System.setProperty("log4j.defaultInitOverride", "true");
    }

    private static boolean initialized = false;

    private final static URL createURL(final String configurationOrNull)
    {
        try
        {
            if (configurationOrNull != null)
            {
                return new URL(configurationOrNull);
            }
        } catch (final MalformedURLException ex)
        {
            ex.printStackTrace();
        }
        final URL resource =
                LogInitializer.class.getResource("/" + LOG_DIRECTORY + "/" + LOG_FILENAME);
        return resource;
    }

    /**
     * Tries to find the <code>log4j.configuration</code> in system properties.
     */
    private final static String tryFindConfigurationInSystemProperties()
    {
        final String configuration = System.getProperty("log4j.configuration");
        if (configuration != null)
        {
            final String trimmed = configuration.trim();
            if (trimmed.length() > 0)
            {
                return trimmed;
            }
        }
        return null;
    }

    private final static File createLogFile(final String configurationOrNull)
    {
        if (configurationOrNull == null)
        {
            return new File(LOG_DIRECTORY, LOG_FILENAME);
        }
        return new File(configurationOrNull);
    }

    private final static void configureFromFile(final File logFile)
    {
        assert logFile != null && logFile.exists() : "Given log file must be not null and must exist.";
        // For non-XML files, you will use
        // <code>PropertyConfigurator.configureAndWatch(String)</code>
        DOMConfigurator.configureAndWatch(logFile.getPath());
        LogLog.debug(String.format("Log configured from file '%s' (watching).",
                logFile.getAbsolutePath()));
    }

    private final static void configureFromURL(final URL url)
    {
        assert url != null : "Given url can not be null.";
        try
        {
            final File logFile = new File(url.toURI());
            if (logFile.exists())
            {
                configureFromFile(logFile);
                return;
            }
        } catch (final URISyntaxException ex)
        {
            LogLog.warn(String.format("Given url '%s' could not be parsed.", url), ex);
        }
        DOMConfigurator.configure(url);
        LogLog.debug(String.format("Log configured from URL '%s' (NOT watching).", url));
    }

    /**
     * Finishes the initialization process.
     */
    private final static void finishInit()
    {
        initialized = true;
        LogLog.setQuietMode(true);
    }

    /**
     * Initializes logging system. Does nothing if already initialized.
     * <p>
     * Logging configuration file is assumed to be in <code>&lt;working directory&gt;/etc/log.xml</code>. If not found we look for a classpath
     * resource named <code>/etc/log.xml</code>.<br>
     * If nothing found in both locations <code>org.apache.log4j.BaseConfigurator.configure()</code> is used.
     * </p>
     */
    public final static synchronized void init()
    {
        if (initialized)
        {
            return;
        }
        final String configuration = tryFindConfigurationInSystemProperties();
        if (configuration == null || configuration.startsWith(FILE_URL_PREFIX) == false)
        {
            final File logFile = createLogFile(configuration);
            if (logFile.exists())
            {
                configureFromFile(logFile);
                finishInit();
                return;
            }
        }
        if (configuration == null || configuration.startsWith(FILE_URL_PREFIX))
        {
            final URL url = createURL(configuration);
            if (url != null)
            {
                configureFromURL(url);
                finishInit();
                return;
            }
        }
        BasicConfigurator.configure();
        finishInit();
    }
}

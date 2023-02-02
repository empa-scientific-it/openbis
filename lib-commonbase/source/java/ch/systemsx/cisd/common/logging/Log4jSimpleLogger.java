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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

/**
 * A {@link ISimpleLogger} that is based on log4j.
 * 
 * @author Bernd Rinn
 */
public class Log4jSimpleLogger implements ISimpleLogger
{
    private final Priority log4jOverridePriorityOrNull;

    private final Logger log4jLogger;

    static final Level toLog4jPriority(final LogLevel level)
    {
        switch (level)
        {
            case OFF:
                return org.apache.log4j.Level.OFF;
            case TRACE:
                return org.apache.log4j.Level.TRACE;
            case DEBUG:
                return org.apache.log4j.Level.DEBUG;
            case INFO:
                return org.apache.log4j.Level.INFO;
            case WARN:
                return org.apache.log4j.Level.WARN;
            case ERROR:
                return org.apache.log4j.Level.ERROR;
            default:
                throw new IllegalArgumentException("Illegal log level " + level);
        }
    }

    /**
     * Creates a logger that uses <var>log4jLogger<var> to do the real logging.
     * 
     * @param log4jLogger The log4j logger to use.
     * @param log4jOverridePriorityOrNull If not <code>null</code>, use this log level instead of the one provided to the
     *            {@link ISimpleLogger#log(ch.systemsx.cisd.common.logging.LogLevel, String)}.
     */
    public Log4jSimpleLogger(final Logger log4jLogger, final Priority log4jOverridePriorityOrNull)
    {
        assert log4jLogger != null : "Unspecified log4j logger";
        this.log4jOverridePriorityOrNull = log4jOverridePriorityOrNull;
        this.log4jLogger = log4jLogger;
    }

    /**
     * Creates a logger that uses <var>log4jLogger<var> to do the real logging.
     * 
     * @param log4jLogger The log4j logger to use.
     */
    public Log4jSimpleLogger(final Logger log4jLogger)
    {
        this(log4jLogger, null);
    }

    //
    // ISimpleLogger
    //

    @Override
    public void log(final LogLevel level, final String message)
    {
        log(level, message, null);
    }

    @Override
    public void log(LogLevel level, String message, Throwable throwableOrNull)
    {
        if (log4jOverridePriorityOrNull != null)
        {
            log4jLogger.log(log4jOverridePriorityOrNull, message, throwableOrNull);
        } else
        {
            log4jLogger.log(toLog4jPriority(level), message, throwableOrNull);
        }
    }

}

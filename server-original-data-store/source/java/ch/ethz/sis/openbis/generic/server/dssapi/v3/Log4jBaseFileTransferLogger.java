/*
 * Copyright 2019 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dssapi.v3;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import ch.ethz.sis.filetransfer.ILogger;
import ch.ethz.sis.filetransfer.LogLevel;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * @author Franz-Josef Elmer
 */
public class Log4jBaseFileTransferLogger implements ILogger
{

    private static final Level toLog4jPriority(final LogLevel level)
    {
        switch (level)
        {
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

    @Override
    public boolean isEnabled(LogLevel level)
    {
        return true;
    }

    @Override
    public void log(Class<?> clazz, LogLevel level, String message)
    {
        getLogger(clazz).log(toLog4jPriority(level), message);
    }

    @Override
    public void log(Class<?> clazz, LogLevel level, String message, Throwable throwable)
    {
        getLogger(clazz).log(toLog4jPriority(level), message, throwable);
    }

    private Logger getLogger(Class<?> clazz)
    {
        return LogFactory.getLogger(LogCategory.OPERATION, clazz);
    }

}

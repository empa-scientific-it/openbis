/*
 * Copyright 2012 ETH Zuerich, CISD
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
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.common.utilities.SystemTimeProvider;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IDSSRegistrationLogger;

/**
 * Interface for logging into the dss registration log.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DssRegistrationLogger implements IDSSRegistrationLogger
{
    private static final Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY,
            DssRegistrationLogger.class);

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DssRegistrationLogger.class);

    private File file;

    private final DssRegistrationLogDirectoryHelper helper;

    private final IFileOperations fileOperations;

    private final ITimeProvider timeProvider;

    /**
     * gets the handle to the file where this logger is logging
     */
    public File getFile()
    {
        return file;
    }

    public DssRegistrationLogger(File file, DssRegistrationLogDirectoryHelper helper,
            IFileOperations fileOperations)
    {
        this(file, helper, fileOperations, SystemTimeProvider.SYSTEM_TIME_PROVIDER);
    }

    public DssRegistrationLogger(File file, DssRegistrationLogDirectoryHelper helper,
            IFileOperations fileOperations, ITimeProvider timeProvider)
    {
        this.file = file;
        this.helper = helper;
        this.fileOperations = fileOperations;
        this.timeProvider = timeProvider;
    }

    /**
     * Change the state to Failed. The log content is also copied to the notification log.
     */
    public void registerFailure()
    {
        // No need to do this again.
        if (isInFailureState())
        {
            return;
        }

        moveToDir(helper.getFailedDir());
        notificationLog.error("Data set registration failed. See log for details : "
                + file.getAbsolutePath());
    }

    private boolean isInFailureState()
    {
        return helper.getFailedDir().equals(file.getParentFile());
    }

    /**
     * Change the state to Succeeded
     */
    public void registerSuccess()
    {
        moveToDir(helper.getSucceededDir());
    }

    private static FastDateFormat simpleNoISODateFormat = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");

    /**
     * Logs a message.
     */
    public void log(String message)
    {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append(simpleNoISODateFormat.format(new Date(timeProvider.getTimeInMilliseconds())));
        logMessage.append(" ");
        logMessage.append(message);
        logMessage.append("\n");
        FileUtilities.appendToFile(file, logMessage.toString(), false);
    }

    public void info(Logger logger, String message)
    {
        logger.info(message);
        log(message);
    }

    public void warn(Logger logger, String message)
    {
        logger.warn(message);
        log(message);
    }

    public void error(Logger logger, String message)
    {
        logger.error(message);
        log(message);
    }

    public void info(Logger logger, String message, Throwable ex)
    {
        logger.info(message, ex);
        log(ex, message);
    }

    public void warn(Logger logger, String message, Throwable ex)
    {
        logger.warn(message, ex);
        log(ex, message);
    }

    public void error(Logger logger, String message, Throwable ex)
    {
        logger.error(message, ex);
        log(ex, message);
    }

    @Override
    public void info(String message)
    {
        info(operationLog, message);
    }

    @Override
    public void warn(String message)
    {
        warn(operationLog, message);
    }

    @Override
    public void error(String message)
    {
        error(operationLog, message);
    }

    @Override
    public void info(String message, Throwable ex)
    {
        info(operationLog, message, ex);
    }

    @Override
    public void warn(String message, Throwable ex)
    {
        warn(operationLog, message, ex);
    }

    @Override
    public void error(String message, Throwable ex)
    {
        error(operationLog, message, ex);
    }

    /**
     * Logs class and message of exception.
     */
    private void log(Throwable ex, String message)
    {
        log(message + ": " + ex.toString());
    }

    /**
     * Logs a message, truncating the content if it exceeds the length limit.
     */
    public void logTruncatingIfNecessary(String message)
    {
        String truncatedMessage = message;
        if (message.length() > 100)
        {
            truncatedMessage = message.substring(0, 96) + "...";
        }
        log(truncatedMessage);
    }

    private void moveToDir(File dir)
    {
        assert dir.isDirectory();

        fileOperations.move(file, dir);
        file = new File(dir, file.getName());
    }

    /**
     * Log either success or the failure with error details. Registers success only if <code>encounteredErrors</code> is empty.
     */
    public void logDssRegistrationResult(List<Throwable> encounteredErrors)
    {
        if (0 == encounteredErrors.size())
        {
            registerSuccess();
        } else
        {
            // Construct a message to add to the registration log
            StringBuilder logMessage = new StringBuilder();
            logMessage.append("Registration failed with the following errors\n");
            for (Throwable error : encounteredErrors)
            {
                logMessage.append("\t");
                logMessage.append(error.toString());
            }
            log(logMessage.toString());
            registerFailure();
        }
    }
}

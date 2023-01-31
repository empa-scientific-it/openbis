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
import java.io.IOException;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.common.utilities.SystemTimeProvider;

/**
 * A utility class for working with the directory structure of the "log-registrations" directory which stores log information about data set
 * registrations.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DssRegistrationLogDirectoryHelper
{
    private final static String IN_PROCESS_DIR_NAME = "in-process";

    private final static String SUCCEEDED_DIR_NAME = "succeeded";

    private final static String FAILED_DIR_NAME = "failed";

    private final File dssRegistrationLogDir;

    private final ITimeProvider timeProvider;

    public DssRegistrationLogDirectoryHelper(File dssRegistrationLogDir)
    {
        this(dssRegistrationLogDir, SystemTimeProvider.SYSTEM_TIME_PROVIDER);
    }

    public DssRegistrationLogDirectoryHelper(File dssRegistrationLogDir, ITimeProvider timeProvider)
    {
        this.dssRegistrationLogDir = dssRegistrationLogDir;
        this.timeProvider = timeProvider;
    }

    /**
     * Initialize the subdirectory structure for the logs
     */
    public void initializeSubdirectories()
    {
        createDirectoryIfNecessary(getInProcessDir());
        createDirectoryIfNecessary(getSucceededDir());
        createDirectoryIfNecessary(getFailedDir());
    }

    /**
     * Return the directory used for files that are still in process.
     */
    public File getInProcessDir()
    {
        return new File(dssRegistrationLogDir, IN_PROCESS_DIR_NAME);
    }

    /**
     * Return the directory used for files that were successfully registered.
     */
    public File getSucceededDir()
    {
        return new File(dssRegistrationLogDir, SUCCEEDED_DIR_NAME);
    }

    /**
     * Return the directory used for files that were not successfully registered.
     */
    public File getFailedDir()
    {
        return new File(dssRegistrationLogDir, FAILED_DIR_NAME);
    }

    /**
     * Create a new log file located in the inProcessDir.
     */
    public DssRegistrationLogger createNewLogFile(String name, String threadName, IFileOperations fileOperations)
    {
        String logFilename = generateLogFileName(name, threadName);
        File logFile = new File(getInProcessDir(), logFilename);
        try
        {
            logFile.createNewFile();
        } catch (IOException e)
        {
            throw new IOExceptionUnchecked(e);
        }
        return new DssRegistrationLogger(logFile, this, fileOperations, timeProvider);
    }

    /**
     * Generate a new log file name. Has default visibility for testing.
     */
    String generateLogFileName(String name, String threadName)
    {
        return new DssUniqueFilenameGenerator(timeProvider, threadName, name, ".log").generateFilename();
    }

    private void createDirectoryIfNecessary(File dir)
    {
        if (false == dir.exists())
        {
            dir.mkdir();
        }
    }
}
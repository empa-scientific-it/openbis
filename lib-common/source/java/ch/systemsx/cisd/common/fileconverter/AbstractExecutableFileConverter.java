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

package ch.systemsx.cisd.common.fileconverter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.utilities.OSUtilities;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.process.ProcessExecutionHelper;
import ch.systemsx.cisd.common.process.ProcessResult;

/**
 * An abstract super-class for {@link IFileConversionMethod}s that use external executable to perform the conversion.
 * 
 * @author Bernd Rinn
 */
abstract class AbstractExecutableFileConverter implements IFileConversionMethod
{

    protected final Logger machineLog;

    protected final Logger operationLog;

    private final File executable = OSUtilities.findExecutable(getExecutableName());

    protected AbstractExecutableFileConverter(Logger machineLog, Logger operationLog)
    {
        this.machineLog = machineLog;
        this.operationLog = operationLog;
    }

    /**
     * Returns the name of the executable that performs the conversion.
     */
    protected abstract String getExecutableName();

    /**
     * Returns the absolute path of the executable.
     */
    protected String getExecutablePath()
    {
        return executable == null ? ("? (" + getExecutableName() + ")") : executable
                .getAbsolutePath();
    }

    /**
     * Returns the command line to the executable (excluding the executable itself).
     */
    protected abstract List<String> getCommandLine(File inFile, File outFile);

    /**
     * Returns <code>false</code>.
     */
    @Override
    public boolean isRemote()
    {
        return false;
    }

    @Override
    public void check() throws EnvironmentFailureException, ConfigurationFailureException
    {
        if (executable == null)
        {
            throw new ConfigurationFailureException("Cannot find executable of the "
                    + getExecutableName() + " utility.");
        }
    }

    @Override
    public boolean convert(File inFile, File outFile)
    {
        final ProcessResult processResult = runExecutable(getCommandLine(inFile, outFile));
        final boolean processOK = ProcessExecutionHelper.log(processResult);
        final boolean exists = outFile.exists() && (outFile.length() > 0);
        if (exists == false)
        {
            operationLog.error("Outfile '" + outFile.getAbsolutePath()
                    + "' does not exist after processing.");
        }
        return processOK && exists;
    }

    protected ProcessResult runExecutable(List<String> commandLine)
    {
        final List<String> fullCommandLine = new ArrayList<String>();
        fullCommandLine.add(executable.getAbsolutePath());
        fullCommandLine.addAll(commandLine);
        final ProcessResult processResult =
                ProcessExecutionHelper.run(fullCommandLine, operationLog, machineLog, false);
        return processResult;
    }

}

/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.datamover.console.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;


import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.process.ProcessExecutionHelper;
import ch.systemsx.cisd.common.process.ProcessResult;
import ch.systemsx.cisd.datamover.console.client.EnvironmentFailureException;
import ch.systemsx.cisd.datamover.console.client.dto.DatamoverStatus;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ScriptBasedDatamoverConsole implements IDatamoverConsole
{
    private static final String SCRIPT_FILE = "datamover.sh";
    
    private static final Logger operationLog =
        LogFactory.getLogger(LogCategory.OPERATION, ScriptBasedDatamoverConsole.class);
    
    private static final Logger machineLog =
        LogFactory.getLogger(LogCategory.MACHINE, ScriptBasedDatamoverConsole.class);

    private final String name;
    
    private final String scriptPath;
    
    public ScriptBasedDatamoverConsole(String name, String scriptWorkingDirectory)
    {
        this.name = name;
        scriptPath = scriptWorkingDirectory + "/" + SCRIPT_FILE;
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Shutdown signal sent to datamover '" + name + "'.");
        }
        
    }
    
    public DatamoverStatus obtainStatus()
    {
        List<String> output = execute("mstatus").getOutput();
        if (output.isEmpty())
        {
            throw new EnvironmentFailureException(
                    "Nothing returned when obtaining statuc of datamover '" + name + "'.");
        }
        String value = output.get(0);
        DatamoverStatus status = DatamoverStatus.valueOf(value);
        if (status == null)
        {
            throw new EnvironmentFailureException("Unkown status obtained for datamover '" + name
                    + "': " + value);
        }
        return status;
    }

    public String tryToObtainTargetPath()
    {
        List<String> output = execute("target").getOutput();
        return output.isEmpty() ? null : output.get(0);
    }

    public void shutdown()
    {
        ProcessResult result = execute("shutdown");
        if (result.isOK())
        {
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("Shutdown signal sent to datamover '" + name + "'.");
            }
        } else
        {
            operationLog.error("A problem occured after sending to datamover '" + name
                    + "' a shutdown signale: ");
            List<String> output = result.getOutput();
            for (String line : output)
            {
                operationLog.error("   " + line);
            }
        }
    }

    public void start(String targetHostOrNull, String targetPathOrNull,
            long highwaterMarkInKByteOrNull)
    {
        // TODO Auto-generated method stub

    }
    
    private ProcessResult execute(String commandName, String... options)
    {
        List<String> command = new ArrayList<String>();
        command.add("sh");
        command.add(scriptPath);
        command.add(commandName);
        command.addAll(Arrays.asList(options));
        ProcessResult result = ProcessExecutionHelper.run(command, operationLog, machineLog,
                ConcurrencyUtilities.NO_TIMEOUT,
                ProcessExecutionHelper.OutputReadingStrategy.ALWAYS, true);
        return result;
    }
    
    

}

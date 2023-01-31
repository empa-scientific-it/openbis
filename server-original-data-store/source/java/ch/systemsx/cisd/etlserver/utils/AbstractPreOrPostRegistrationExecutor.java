/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.common.process.ProcessExecutionHelper;
import ch.systemsx.cisd.common.shared.basic.string.StringUtils;

/**
 * The common behavior for the pre and post registration actions.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
abstract class AbstractPreOrPostRegistrationExecutor
{

    protected static class EmptyScriptExecutor
    {
        public boolean execute(String dataSetCode, String path)
        {
            return true;
        }
    }

    static protected void checkScriptIsAccessible(String script, String adjectivalDescription)
    {
        File file = new File(script);
        IFileOperations fileOperations = FileOperations.getInstance();
        if (fileOperations.isFile(file) == false || fileOperations.canRead(file) == false)
        {
            throw new ConfigurationFailureException(String.format("Cannot access "
                    + adjectivalDescription + " script '%s'.", script));
        }
    }

    protected static boolean callScript(Logger operationLog, Logger machineLog, String scriptPath,
            String... args)
    {
        List<String> cmd = new ArrayList<String>();
        cmd.add(scriptPath);
        cmd.addAll(Arrays.asList(args));
        boolean result =
                ProcessExecutionHelper.runAndLog(cmd, operationLog, machineLog,
                        ConcurrencyUtilities.NO_TIMEOUT);
        if (result == false)
        {
            operationLog.warn(String.format("Script execution failed: SCRIPT(%s) PARAMETERS(%s)",
                    scriptPath, StringUtils.join(args, ",")));
        } else
        {
            operationLog.info(String.format(
                    "Successful script execution: SCRIPT(%s) PARAMETERS(%s)", scriptPath,
                    StringUtils.join(args, ",")));
        }
        return result;
    }

    protected final String scriptPath;

    /**
     * Constructor
     */
    protected AbstractPreOrPostRegistrationExecutor(String script)
    {
        super();
        this.scriptPath = script;
        checkScriptIsAccessible(script, getExecutorAdjectivalDescription());
    }

    /**
     * Return a description of a concrete subclass as an adjective, e.g., pre-regitration or post-registration.
     * <p>
     * The implementation should not refer any instance state, since it is used in the constructor.
     */
    protected abstract String getExecutorAdjectivalDescription();
}
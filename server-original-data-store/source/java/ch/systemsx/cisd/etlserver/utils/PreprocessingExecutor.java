/*
 * Copyright 2009 ETH Zuerich, CISD
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.process.CallableExecutor;
import ch.systemsx.cisd.common.process.ProcessExecutionHelper;
import ch.systemsx.cisd.common.properties.PropertyUtils;

/**
 * A class which is configured from properties and is able to execute a script from the command line using the configured path
 * {@link #PREPROCESSING_SCRIPT_PATH}.
 * 
 * @author Tomasz Pylak
 */
public class PreprocessingExecutor
{
    private final static Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, PreprocessingExecutor.class);

    private final static Logger machineLog =
            LogFactory.getLogger(LogCategory.MACHINE, PreprocessingExecutor.class);

    private final static PreprocessingExecutor DUMMY = new PreprocessingExecutor(null, 0, 0L, -1)
        {
            @Override
            public boolean execute(String filePath)
            {
                return true;
            }

            @Override
            public boolean executeOnce(String filePath)
            {
                return true;
            }
        };

    /**
     * A path to a script which should be called from command line for every dataset batch before it is processed. Can be used e.g. to change file
     * permissions. The script gets one parameter, the path to the dataset file, relative to the incoming directory.
     */
    public final static String PREPROCESSING_SCRIPT_PATH = "preprocessing-script";

    public final static String PREPROCESSING_SCRIPT_RETRIES = "preprocessing-script-max-retries";

    public final static String PREPROCESSING_SCRIPT_FAILURE_INTERVAL_IN_SEC =
            "preprocessing-script-failure-interval";

    public final static String PREPROCESSING_SCRIPT_TIMEOUT = "preprocessing-script-timeout-sec";

    public static PreprocessingExecutor create(Properties properties)
    {
        String preprocessingScriptPath = properties.getProperty(PREPROCESSING_SCRIPT_PATH);
        int maxRetriesOnFailure = PropertyUtils.getInt(properties, PREPROCESSING_SCRIPT_RETRIES, 0);
        long millisToSleepOnFailure =
                PropertyUtils.getInt(properties, PREPROCESSING_SCRIPT_FAILURE_INTERVAL_IN_SEC, 0) * 1000;
        long millisToWaitForCompletion = extractMillisToWaitForCompletion(properties);
        if (preprocessingScriptPath != null)
        {
            return new PreprocessingExecutor(preprocessingScriptPath, maxRetriesOnFailure,
                    millisToSleepOnFailure, millisToWaitForCompletion);
        } else
        {
            operationLog.info("No preprocessing script found, skipping preprocessing.");
            // Return a dummy that always return true;
            return DUMMY;
        }
    }

    private static long extractMillisToWaitForCompletion(Properties properties)
    {
        long timeoutSec =
                PropertyUtils.getLong(properties, PREPROCESSING_SCRIPT_TIMEOUT,
                        ConcurrencyUtilities.NO_TIMEOUT);
        if (timeoutSec == ConcurrencyUtilities.NO_TIMEOUT)
        {
            return ConcurrencyUtilities.NO_TIMEOUT;
        }
        return timeoutSec * 1000;
    }

    private final String preprocessingScriptPath;

    private final int maxRetriesOnFailure;

    private final long millisToSleepOnFailure;

    private final long millisToWaitForCompletion;

    private PreprocessingExecutor(String preprocessingScriptPath, int maxRetriesOnFailure,
            long millisToSleepOnFailure, long millisToWaitForCompletion)
    {
        this.preprocessingScriptPath = preprocessingScriptPath;
        this.maxRetriesOnFailure = maxRetriesOnFailure;
        this.millisToSleepOnFailure = millisToSleepOnFailure;
        this.millisToWaitForCompletion = millisToWaitForCompletion;
    }

    public boolean execute(final String filePath)
    {
        Object result =
                new CallableExecutor(maxRetriesOnFailure, millisToSleepOnFailure)
                        .executeCallable(new Callable<Object>()
                            {
                                // returns null on error, non-null on success
                                @Override
                                public Object call() throws Exception
                                {
                                    boolean ok = executeOnce(filePath);
                                    return ok ? true : null;
                                }
                            });
        return (result != null);
    }

    public boolean executeOnce(final String filePath)
    {
        return callScript(preprocessingScriptPath, millisToWaitForCompletion, filePath);
    }

    private static boolean callScript(String scriptPath, long millisToWaitForCompletion,
            String... args)
    {
        List<String> cmd = new ArrayList<String>();
        cmd.add(scriptPath);
        cmd.addAll(Arrays.asList(args));
        return ProcessExecutionHelper.runAndLog(cmd, operationLog, machineLog,
                millisToWaitForCompletion);
    }

}
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

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.etlserver.IPostRegistrationAction;

/**
 * A class which is configured from properties and is able to execute a script from the command line using the configured path.
 * 
 * @author Izabela Adamczyk
 */
public class PostRegistrationExecutor extends AbstractPreOrPostRegistrationExecutor implements
        IPostRegistrationAction
{

    private final static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            PostRegistrationExecutor.class);

    private final static Logger machineLog = LogFactory.getLogger(LogCategory.MACHINE,
            PostRegistrationExecutor.class);

    private static class PostRegistrationEmptyScriptExecutor extends EmptyScriptExecutor implements
            IPostRegistrationAction
    {
    }

    public static IPostRegistrationAction create(String scriptPath)
    {
        if (scriptPath != null)
        {
            return new PostRegistrationExecutor(scriptPath);
        } else
        {
            operationLog.debug("No post-registration script found, skipping execution.");
            return new PostRegistrationEmptyScriptExecutor();
        }
    }

    private PostRegistrationExecutor(String script)
    {
        super(script);
        operationLog.info(getExecutorAdjectivalDescription() + " script: " + script);
    }

    @Override
    public boolean execute(final String dataSetCode, final String dataSetAbsolutePathInStore)
    {
        return callScript(operationLog, machineLog, scriptPath, dataSetCode,
                dataSetAbsolutePathInStore);
    }

    @Override
    protected String getExecutorAdjectivalDescription()
    {
        return "post-registration";
    }

}
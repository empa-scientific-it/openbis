/*
 *  Copyright ETH 2023 Zürich, Scientific IT Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ch.ethz.sis.openbis.generic.server.dssapi.v3.helper;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.service.CustomDSSServiceExecutionOptions;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.ExceptionUtils;
import ch.systemsx.cisd.common.jython.JythonUtils;
import ch.systemsx.cisd.common.jython.evaluator.Evaluator;
import ch.systemsx.cisd.common.jython.evaluator.EvaluatorException;
import ch.systemsx.cisd.common.jython.evaluator.IJythonEvaluator;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import org.apache.log4j.Logger;

import java.io.Serializable;

public class ScriptRunnerFactory implements IDssScriptRunnerFactory
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            ScriptRunnerFactory.class);

    private final String scriptPath;

    private final IApplicationServerApi applicationService;

    private final IDataStoreServerApi dataStoreServerService;

    public ScriptRunnerFactory(String scriptPath, IApplicationServerApi applicationService,
            IDataStoreServerApi dataStoreServerApi)
    {
        this.scriptPath = scriptPath;
        this.applicationService = applicationService;
        this.dataStoreServerService = dataStoreServerApi;
        Evaluator.getFactory().initialize();
    }

    @Override
    public String getScriptPath()
    {
        return scriptPath;
    }

    @Override
    public IDssServiceScriptRunner createServiceRunner(String sessionToken)
    {
        String scriptString = JythonUtils.extractScriptFromPath(scriptPath);
        String[] pythonPath = JythonUtils.getScriptDirectoryPythonPath(scriptPath);

        try
        {
            IJythonEvaluator
                    evaluator = Evaluator.getFactory().create("", pythonPath, scriptPath, null, scriptString, false);
            ExecutionContext
                    executionContext = new ExecutionContext(sessionToken, applicationService, dataStoreServerService);
            return new ServiceScriptRunner(evaluator, executionContext);
        } catch (EvaluatorException ex)
        {
            throw new EvaluatorException(ex.getMessage() + " [" + scriptPath + "]");
        }
    }

    private static final class ServiceScriptRunner implements IDssServiceScriptRunner
    {
        private static final String PROCESS_FUNCTION_NAME = "process";

        private IJythonEvaluator evaluator;

        private ExecutionContext context;

        ServiceScriptRunner(IJythonEvaluator evaluator, ExecutionContext context)
        {
            this.evaluator = evaluator;
            this.context = context;
            if (evaluator.hasFunction(PROCESS_FUNCTION_NAME) == false)
            {
                throw new EvaluatorException("Function '" + PROCESS_FUNCTION_NAME
                        + "' was not defined in the processing plugin script");
            }

        }

        @Override
        public Serializable process(CustomDSSServiceExecutionOptions options)
        {
            Object result = null;
            try
            {
                result = evaluator.evalFunction(PROCESS_FUNCTION_NAME, context, options.getParameters());
            } catch (EvaluatorException e)
            {
                operationLog.error("Evaluation failed", e);
                Throwable throwable = ExceptionUtils.getEndOfChain(e);
                throw CheckedExceptionTunnel.wrapIfNecessary(throwable);
            }
            if (result == null || result instanceof Serializable)
            {
                return (Serializable) result;
            }
            throw new EvaluatorException("Function '" + PROCESS_FUNCTION_NAME
                    + "' dosn't return a serializable object. Object type: " + result.getClass());
        }
    }

    public static final class ExecutionContext
    {
        private final String sessionToken;

        private final IApplicationServerApi applicationService;

        private final IDataStoreServerApi dataStoreServerService;

        ExecutionContext(String sessionToken, IApplicationServerApi applicationService, IDataStoreServerApi dataStoreServerService)
        {
            this.sessionToken = sessionToken;
            this.applicationService = applicationService;
            this.dataStoreServerService = dataStoreServerService;
        }

        public String getSessionToken()
        {
            return sessionToken;
        }

        public IApplicationServerApi getApplicationService()
        {
            return applicationService;
        }

        public IDataStoreServerApi getDataStoreServerService()
        {
            return dataStoreServerService;
        }
    }
}

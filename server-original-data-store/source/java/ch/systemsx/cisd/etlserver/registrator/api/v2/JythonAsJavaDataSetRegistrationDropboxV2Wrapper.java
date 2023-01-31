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

package ch.systemsx.cisd.etlserver.registrator.api.v2;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.NotImplementedException;
import ch.systemsx.cisd.common.jython.IJythonFunction;
import ch.systemsx.cisd.common.jython.IJythonInterpreter;
import ch.systemsx.cisd.common.jython.IJythonObject;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationContext;
import ch.systemsx.cisd.etlserver.registrator.api.v2.JythonTopLevelDataSetHandlerV2.JythonHookFunction;

/**
 * @author Pawel Glyzewski
 */
public class JythonAsJavaDataSetRegistrationDropboxV2Wrapper implements
        IJavaDataSetRegistrationDropboxV2
{
    protected static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            JythonAsJavaDataSetRegistrationDropboxV2Wrapper.class);

    private IJythonInterpreter interpreter;

    private Boolean retryDefined;

    public JythonAsJavaDataSetRegistrationDropboxV2Wrapper(IJythonInterpreter interpreter)
    {
        this.interpreter = interpreter;
    }

    @Override
    public void process(IDataSetRegistrationTransactionV2 transaction)
    {
        try
        {
            IJythonFunction function = interpreter.tryJythonFunction(JythonHookFunction.PROCESS_FUNCTION.name);

            if (function == null)
            {
                throw new IllegalStateException("Undefined process() function");
            }
            function.invoke(transaction);
        } catch (Exception e)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(e);
        }
    }

    @Override
    public void postStorage(DataSetRegistrationContext context)
    {
        IJythonFunction function = getInterpreter().tryJythonFunction(
                JythonHookFunction.POST_STORAGE_FUNCTION_NAME.name);
        if (function != null)
        {
            function.invoke(context);
        } else
        {
            throw new NotImplementedException("postStorage is not implemented.");
        }
    }

    @Override
    public void preMetadataRegistration(DataSetRegistrationContext context)
    {
        IJythonFunction function = getInterpreter().tryJythonFunction(
                JythonHookFunction.PRE_REGISTRATION_FUNCTION_NAME.name);

        if (null != function)
        {
            function.invoke(context);
        } else
        {
            throw new NotImplementedException("preMetadataRegistration is not implemented.");
        }
    }

    @Override
    public void postMetadataRegistration(DataSetRegistrationContext context)
    {
        IJythonFunction function = getInterpreter().tryJythonFunction(JythonHookFunction.POST_REGISTRATION_FUNCTION_NAME.name);
        if (function != null)
        {
            function.invoke(context);
        } else
        {
            throw new NotImplementedException("postMetadataRegistration is not implemented.");
        }
    }

    @Override
    public void rollbackPreRegistration(DataSetRegistrationContext context, Throwable throwable)
    {
        IJythonFunction function = getInterpreter().tryJythonFunction(
                JythonHookFunction.ROLLBACK_PRE_REGISTRATION_FUNCTION_NAME.name);
        if (function != null)
        {
            function.invoke(context, throwable);
        } else
        {
            throw new NotImplementedException("rollbackPreRegistration is not implemented.");
        }
    }

    @Override
    public boolean isRetryFunctionDefined()
    {
        if (retryDefined == null)
        {
            IJythonFunction function = getInterpreter().tryJythonFunction(
                    JythonHookFunction.SHOULD_RETRY_PROCESS_FUNCTION_NAME.name);
            if (function == null)
            {
                retryDefined = false;
            } else
            {
                retryDefined = true;
            }
        }
        return retryDefined;
    }

    @Override
    public boolean shouldRetryProcessing(DataSetRegistrationContext context, Exception problem)
    {
        if (false == isRetryFunctionDefined())
        {
            throw new NotImplementedException("shouldRetryProcessing is not implemented.");
        }

        IJythonFunction retryFunction = getInterpreter().tryJythonFunction(JythonHookFunction.SHOULD_RETRY_PROCESS_FUNCTION_NAME.name);
        IJythonObject retryFunctionResult = null;
        try
        {
            retryFunctionResult = retryFunction.invoke(context, problem);
        } catch (Exception ex)
        {
            operationLog.error("The retry function has failed. Rolling back.", ex);
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }

        if (retryFunctionResult == null)
        {
            operationLog
                    .error("The should_retry_processing function did not return anything. Will not retry.");
            throw CheckedExceptionTunnel.wrapIfNecessary(problem);
        }

        if (false == retryFunctionResult.isInteger())
        { // the python booleans are returned as PyIntegers
            operationLog
                    .error("The should_retry_processing function returned object of non-boolean type "
                            + retryFunctionResult.getClass() + ". Will not retry.");
            throw CheckedExceptionTunnel.wrapIfNecessary(problem);
        }

        if (retryFunctionResult.asInteger() != 0)
        {
            return true;
        }

        return false;
    }

    private IJythonInterpreter getInterpreter()
    {
        return interpreter;
    }
}

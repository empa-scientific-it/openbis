/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.jython.v25;

import ch.systemsx.cisd.common.jython.evaluator.EvaluatorException;
import ch.systemsx.cisd.common.jython.evaluator.IJythonEvaluator;
import ch.systemsx.cisd.common.jython.evaluator.IJythonEvaluatorFactory;

/**
 * @author Jakub Straszewski
 */
public class Jython25EvaluatorFactory implements IJythonEvaluatorFactory
{

    @Override
    public void initialize()
    {
        Evaluator25.initialize();
    }

    @Override
    public IJythonEvaluator create(String expression) throws EvaluatorException
    {
        return new Evaluator25(expression);
    }

    @Override
    public IJythonEvaluator create(String expression, Class<?> supportFunctionsOrNull, String initialScriptOrNull) throws EvaluatorException
    {
        return new Evaluator25(expression, supportFunctionsOrNull, initialScriptOrNull);
    }

    @Override
    public IJythonEvaluator create(String expression, String[] pythonPath, String scriptPath, Class<?> supportFunctionsOrNull,
            String initialScriptOrNull,
            boolean blockFileAccess) throws EvaluatorException
    {
        return new Evaluator25(expression, pythonPath, scriptPath, supportFunctionsOrNull, initialScriptOrNull, blockFileAccess);
    }

}

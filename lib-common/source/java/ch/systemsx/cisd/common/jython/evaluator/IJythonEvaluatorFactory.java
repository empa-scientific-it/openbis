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

package ch.systemsx.cisd.common.jython.evaluator;

import ch.systemsx.cisd.common.jython.v25.Evaluator25;

/**
 * @author Jakub Straszewski
 */
public interface IJythonEvaluatorFactory
{
    /**
     * Call once before using the object to initialize.
     */
    public void initialize();

    /**
     * Creates a new {@link Evaluator25} with file system access blocked.
     * 
     * @param expression The expression to evaluate.
     */
    public IJythonEvaluator create(String expression) throws EvaluatorException;

    /**
     * Creates a new {@link IJythonEvaluator} with file system access blocked.
     * 
     * @param expression The expression to evaluate.
     * @param supportFunctionsOrNull If not <code>null</code>, all public static methods of the given class will be available to the evaluator as
     *            "supporting functions".
     * @param initialScriptOrNull If not <code>null</code>, this has to be a valid (Python) script which is evaluated initially, e.g. to define some
     *            new functions. Note: this script is trusted, so don't run any unvalidated code here!
     */
    public IJythonEvaluator create(String expression, Class<?> supportFunctionsOrNull, String initialScriptOrNull)
            throws EvaluatorException;

    /**
     * Creates a new {@link Evaluator25}.
     * 
     * @param expression The expression to evaluate.
     * @param supportFunctionsOrNull If not <code>null</code>, all public static methods of the given class will be available to the evaluator as
     *            "supporting functions".
     * @param initialScriptOrNull If not <code>null</code>, this has to be a valid (Python) script which is evaluated initially, e.g. to define some
     *            new functions. Note: this script is trusted, so don't run any unvalidated code here!
     * @param blockFileAccess If <code>true</code> the script will not be able to open files.
     */
    public IJythonEvaluator create(String expression, String[] pythonPath, String scriptPath, Class<?> supportFunctionsOrNull,
            String initialScriptOrNull, boolean blockFileAccess) throws EvaluatorException;

}

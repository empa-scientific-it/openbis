/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.db;

/**
 * Executor of SQL scripts.
 * 
 * @author Franz-Josef Elmer
 */
public interface ISqlScriptExecutor
{
    /**
     * Executes the specified SQL script.
     * 
     * @param sqlScript The script to execute.
     * @param honorSingleStepMode If <code>false</code>, the single step mode will be ignored for this script, even if configured in the properties.
     * @param loggerOrNull A logger to log the execution of the <var>sqlScript</var>, or <code>null</code>, if no logging should be performed.
     */
    public void execute(Script sqlScript, boolean honorSingleStepMode,
            ISqlScriptExecutionLogger loggerOrNull);
}

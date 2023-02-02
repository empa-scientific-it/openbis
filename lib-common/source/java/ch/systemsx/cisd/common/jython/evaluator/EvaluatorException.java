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

package ch.systemsx.cisd.common.jython.evaluator;

import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * An exception which occurs when {@link Evaluator} evaluates an expression.
 *
 * @author Bernd Rinn
 */
public class EvaluatorException extends UserFailureException
{

    private static final long serialVersionUID = 1L;

    public EvaluatorException(String msg)
    {
        super(msg);
    }

    public EvaluatorException(String message, Throwable cause)
    {
        super(message, cause);
    }

}

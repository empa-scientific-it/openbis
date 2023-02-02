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
package ch.systemsx.cisd.common.exceptions;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.HighLevelException;

/**
 * The <code>EnvironmentFailureException</code> is the super class of all exceptions that have their cause in the software or hardware environment of
 * the system failing.
 * 
 * @author Bernd Rinn
 */
public class EnvironmentFailureException extends HighLevelException
{

    private static final long serialVersionUID = 1L;

    public EnvironmentFailureException(String message)
    {
        super(message);
    }

    public EnvironmentFailureException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Creates an {@link EnvironmentFailureException} using a {@link java.util.Formatter}.
     */
    public static EnvironmentFailureException fromTemplate(String messageTemplate, Object... args)
    {
        return new EnvironmentFailureException(String.format(messageTemplate, args));
    }

    /**
     * Creates an {@link EnvironmentFailureException} using a {@link java.util.Formatter}.
     */
    public static EnvironmentFailureException fromTemplate(Throwable cause, String messageTemplate,
            Object... args)
    {
        return new EnvironmentFailureException(String.format(messageTemplate, args), cause);
    }

    /**
     * Returns the assessment of the subsystem throwing the exception whether the failure could be temporarily and thus retrying the operation (on a
     * higher level) could possibly help to cure the problem.
     * <p>
     * This class will always return <code>true</code>, but sub classes can override the method.
     * 
     * @return Whether retrying the operation can possibly rectify the situation or not.
     */
    @Override
    public boolean isRetriable()
    {
        return true;
    }

}

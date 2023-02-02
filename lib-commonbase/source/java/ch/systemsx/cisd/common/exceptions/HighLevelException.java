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

import ch.systemsx.cisd.common.exceptions.HighLevelException;

/**
 * An exception that is "high-level" in the sense that we have a pretty good understanding what the failure means in the context where the exception
 * has been thrown.
 * 
 * @author Bernd Rinn
 */
public abstract class HighLevelException extends RuntimeException
{

    private static final long serialVersionUID = 1L;

    protected HighLevelException(String message)
    {
        super(message);
    }

    protected HighLevelException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Returns the assessment of the subsystem throwing the exception whether the failure could be temporarily and thus retrying the operation (on a
     * higher level) could possibly help to cure the problem.
     * <p>
     * This class will always return <code>false</code>, but sub classes can override the method.
     * 
     * @return Whether retrying the operation can possibly rectify the situation or not.
     */
    public boolean isRetriable()
    {
        return false;
    }

    /**
     * Returns the assessment of the subsystem throwing the exception whether the failure could be temporarily and thus retrying the operation (on a
     * higher level) could possibly help to cure the problem.
     * <p>
     * This class will always return <code>false</code>, but sub classes can override the method.
     * 
     * @return Whether retrying the operation can possibly rectify the situation or not.
     */
    public static boolean isRetriable(Throwable th)
    {
        if (th instanceof HighLevelException)
        {
            return ((HighLevelException) th).isRetriable();
        } else
        {
            return false;
        }
    }

}

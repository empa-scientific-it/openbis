/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.apache.commons.lang3.builder.ToStringBuilder;

import ch.systemsx.cisd.common.reflection.ModifiedShortPrefixToStringStyle;

/**
 * A class that contains the result of the execution of a {@link Runnable} (or {@link Callable}) in an {@link ExecutorService}.
 * 
 * @author Bernd Rinn
 */
public final class ExecutionResult<T>
{
    private final ExecutionStatus status;

    private final T resultOrNull;

    private final Throwable exceptionOrNull;

    private ExecutionResult(final ExecutionStatus status, final T resultOrNull,
            final Throwable exceptionOrNull)
    {
        this.status = status;
        this.resultOrNull = resultOrNull;
        this.exceptionOrNull = exceptionOrNull;
    }

    /**
     * Creates an {@link ExecutionResult} that corresponds to a "real" result. Since a {@link Runnable} can also provide a <code>null</code> result,
     * <code>null</code> is an accepted value for the result.
     */
    public static final <T> ExecutionResult<T> create(final T resultOrNull)
    {
        return new ExecutionResult<T>(ExecutionStatus.COMPLETE, resultOrNull, null);
    }

    /**
     * Creates an {@link ExecutionResult} that corresponds to an exception.
     */
    public static final <T> ExecutionResult<T> createExceptional(final Throwable exception)
    {
        assert exception != null;

        return new ExecutionResult<T>(ExecutionStatus.EXCEPTION, null, exception);
    }

    /**
     * Creates an {@link ExecutionResult} that corresponds to a time out.
     */
    public static final <T> ExecutionResult<T> createTimedOut()
    {
        return new ExecutionResult<T>(ExecutionStatus.TIMED_OUT, null, null);
    }

    /**
     * Creates an {@link ExecutionResult} that corresponds to an interruption.
     */
    public static final <T> ExecutionResult<T> createInterrupted()
    {
        return new ExecutionResult<T>(ExecutionStatus.INTERRUPTED, null, null);
    }

    /**
     * Returns the {@link ExecutionStatus} of the execution.
     */
    public ExecutionStatus getStatus()
    {
        return status;
    }

    /**
     * Returns <code>true</code>, if the execution status is {@link ExecutionStatus#COMPLETE}.
     */
    public boolean isOK()
    {
        return ExecutionStatus.COMPLETE == status;
    }

    /**
     * Returns the returned result of the execution, or <code>null</code>, if either the status is not {@link ExecutionStatus#COMPLETE} or if the
     * execution didn't provide a result.
     */
    public T tryGetResult()
    {
        return resultOrNull;
    }

    /**
     * Returns the thrown exception (or error) of the execution, or <code>null</code>, if the status is not {@link ExecutionStatus#EXCEPTION}.
     */
    public Throwable tryGetException()
    {
        return exceptionOrNull;
    }

    //
    // Object
    //

    @Override
    public final String toString()
    {
        return ToStringBuilder.reflectionToString(this,
                ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
    }
}
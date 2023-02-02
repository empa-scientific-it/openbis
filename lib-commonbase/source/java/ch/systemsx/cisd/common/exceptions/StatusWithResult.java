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

package ch.systemsx.cisd.common.exceptions;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * A {@link Status} that can also hold a result.
 * 
 * @author Bernd Rinn
 */
public class StatusWithResult<T> extends Status
{

    private final T resultOrNull;

    /**
     * Creates a new result with status {@link StatusFlag#OK} and <var>resultOrNull</var>.
     */
    public static <T> StatusWithResult<T> create(T resultOrNull)
    {
        return new StatusWithResult<T>(StatusFlag.OK, null, resultOrNull);
    }

    /**
     * Create an error.
     * 
     * @param retriable If <code>true</code>, the error will be marked 'retriable'.
     */
    public static <T> StatusWithResult<T> createErrorWithResult(boolean retriable)
    {
        return new StatusWithResult<T>(getErrorFlag(retriable), "", null);
    }

    public static <T> StatusWithResult<T> createErrorx(boolean retriable, String message)
    {
        assert message != null;

        return new StatusWithResult<T>(getErrorFlag(retriable), message, null);
    }

    public static <T> StatusWithResult<T> createErrorWithResult()
    {
        return new StatusWithResult<T>(StatusFlag.ERROR, "", null);
    }

    public static <T> StatusWithResult<T> createErrorWithResult(String message)
    {
        assert message != null;

        return new StatusWithResult<T>(StatusFlag.ERROR, message, null);
    }

    public static <T> StatusWithResult<T> createRetriableErrorWithResult()
    {
        return new StatusWithResult<T>(StatusFlag.RETRIABLE_ERROR, "", null);
    }

    public static <T> StatusWithResult<T> createRetriableErrorWithResult(String message)
    {
        assert message != null;

        return new StatusWithResult<T>(StatusFlag.RETRIABLE_ERROR, message, null);
    }

    protected StatusWithResult(StatusFlag flag, String messageOrNull, T resultOrNull)
    {
        super(flag, messageOrNull);
        this.resultOrNull = resultOrNull;
    }

    /**
     * Returns the result of the operation (may be <code>null</code>).
     */
    public final T tryGetResult()
    {
        return resultOrNull;
    }

    //
    // Object
    //

    @SuppressWarnings("unchecked")
    private StatusWithResult<T> toStatusWithResult(Object obj)
    {
        return (StatusWithResult<T>) obj;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj == null || obj instanceof StatusWithResult<?> == false)
        {
            return false;
        }
        final StatusWithResult<T> that = toStatusWithResult(obj);
        return getFlag() == that.getFlag()
                && ObjectUtils.equals(this.tryGetErrorMessage(), that.tryGetErrorMessage())
                && ObjectUtils.equals(this.tryGetResult(), that.tryGetResult());
    }

    @Override
    public int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getFlag());
        builder.append(tryGetErrorMessage());
        builder.append(tryGetResult());
        return builder.toHashCode();
    }

    @Override
    public String toString()
    {
        final String messageOrNull = tryGetErrorMessage();
        if (StringUtils.isNotBlank(messageOrNull))
        {
            return getFlag().toString() + ": \"" + messageOrNull + "\"";
        } else if (resultOrNull != null)
        {
            return getFlag().toString() + ": result is \"" + resultOrNull + "\"";
        } else
        {
            return getFlag().toString();
        }
    }

}

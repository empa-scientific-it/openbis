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

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.StatusFlag;

/**
 * A class that holds the information about the status of an operation. To be used whenever a failure of an operation is signalled back via a return
 * value rather than an exception.
 * 
 * @author Bernd Rinn
 */
public class Status
{

    private final StatusFlag flag;

    private final String errorMessageOrNull;

    /** The status indicating that the operation went fine. */
    public static final Status OK = new Status(StatusFlag.OK, null);

    /**
     * Create an error.
     * 
     * @param retriable If <code>true</code>, the error will be marked 'retriable'.
     */
    public static Status createError(final boolean retriable)
    {
        return new Status(getErrorFlag(retriable), "");
    }

    public static Status createError(final boolean retriable, final String message)
    {
        assert message != null;

        return new Status(getErrorFlag(retriable), message);
    }

    public static Status createError()
    {
        return new Status(StatusFlag.ERROR, "");
    }

    public static Status createError(final String message)
    {
        assert message != null;

        return new Status(StatusFlag.ERROR, message);
    }

    public static Status createError(final String messageTemplate, final Object... args)
    {
        assert messageTemplate != null;

        return new Status(StatusFlag.ERROR, String.format(messageTemplate, args));
    }

    public static Status createRetriableError()
    {
        return new Status(StatusFlag.RETRIABLE_ERROR, "");
    }

    public static Status createRetriableError(final String message)
    {
        assert message != null;

        return new Status(StatusFlag.RETRIABLE_ERROR, message);
    }

    public static Status createRetriableError(final String messageTemplate, final Object... args)
    {
        assert messageTemplate != null;

        return new Status(StatusFlag.RETRIABLE_ERROR, String.format(messageTemplate, args));
    }

    protected static StatusFlag getErrorFlag(final boolean retriable)
    {
        return retriable ? StatusFlag.RETRIABLE_ERROR : StatusFlag.ERROR;
    }

    protected Status(final StatusFlag flag, final String message)
    {
        assert flag != null;
        assert StatusFlag.OK.equals(flag) || message != null;

        this.flag = flag;
        this.errorMessageOrNull = message;
    }

    /**
     * @return The status flag of the operation.
     */
    public StatusFlag getFlag()
    {
        return flag;
    }

    /**
     * @return <code>true</code> if this status represents an OK status.
     */
    public final boolean isOK()
    {
        return flag == StatusFlag.OK;
    }

    /**
     * @return <code>true</code> if this status represents an error.
     */
    public final boolean isError()
    {
        return flag != StatusFlag.OK;
    }

    /**
     * @return <code>true</code> if this status represents an error where it makes sense to retry the operation.
     */
    public final boolean isRetriableError()
    {
        return flag == StatusFlag.RETRIABLE_ERROR;
    }

    /**
     * @return <code>true</code> if this status represents an error where it does not make sense to retry the operation.
     */
    public final boolean isNonRetriableError()
    {
        return flag == StatusFlag.ERROR;
    }

    /**
     * @return The error message of the operation if <code>getFlag() != OK</code> (can be empty), or <code>null</code> otherwise.
     */
    public String tryGetErrorMessage()
    {
        return errorMessageOrNull;
    }

    //
    // Object
    //

    @Override
    public boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj == null || obj instanceof Status == false)
        {
            return false;
        }
        final Status that = (Status) obj;
        return getFlag() == that.getFlag()
                && ObjectUtils.equals(this.tryGetErrorMessage(), that.tryGetErrorMessage());
    }

    @Override
    public int hashCode()
    {
        return (17 + flag.hashCode()) * 37 + ObjectUtils.hashCode(tryGetErrorMessage());
    }

    @Override
    public String toString()
    {
        if (StringUtils.isNotBlank(errorMessageOrNull))
        {
            return flag.toString() + ": \"" + errorMessageOrNull + "\"";
        } else
        {
            return flag.toString();
        }
    }

}
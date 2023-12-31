/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.StatusFlag;

/**
 * A class that holds the information about a failure during the operation. Stores the failure status, exception and an item which caused problems.
 * 
 * @author Bernd Rinn
 * @author Tomasz Pylak
 */
public class FailureRecord<T>
{
    private final T failedItem;

    private final Status failureStatus;

    private final Throwable throwableOrNull;

    FailureRecord(T failedItem, Status failureStatus)
    {
        this.failedItem = failedItem;
        this.failureStatus = failureStatus;
        this.throwableOrNull = null;
    }

    FailureRecord(T failedItem, Throwable throwableOrNull)
    {
        this.failedItem = failedItem;
        this.failureStatus =
                Status.createError("Exceptional condition: "
                        + throwableOrNull.getClass().getSimpleName());
        this.throwableOrNull = throwableOrNull;
    }

    /**
     * Returns the item that caused the failure.
     */
    public final T getFailedItem()
    {
        return failedItem;
    }

    /**
     * Returns the {@link Status} of the failure. Can have a {@link StatusFlag} of {@link StatusFlag#RETRIABLE_ERROR} if retrying the operation did
     * not help.
     */
    public final Status getFailureStatus()
    {
        return failureStatus;
    }

    /**
     * Returns the {@link Throwable}, if any has occurred in the compression method.
     */
    public final Throwable tryGetThrowable()
    {
        return throwableOrNull;
    }
}
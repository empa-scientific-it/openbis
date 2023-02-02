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

/**
 * The status of an execution of a {@link Runnable} (or {@link Callable}) in an {@link ExecutorService}.
 * 
 * @author Bernd Rinn
 */
public enum ExecutionStatus
{
    /** The {@link Runnable} ran to completion and returned its result if any. */
    COMPLETE,

    /** The {@link Runnable} didn't run to completion in the specified time but got a timeout. */
    TIMED_OUT,

    /** The {@link Runnable} didn't run to completion because its thread was interrupted. */
    INTERRUPTED,

    /** The {@link Runnable} didn't run to completion because it threw an exception or error. */
    EXCEPTION;
}

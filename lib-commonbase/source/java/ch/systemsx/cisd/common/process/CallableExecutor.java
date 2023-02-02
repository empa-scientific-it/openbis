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

package ch.systemsx.cisd.common.process;

import java.util.concurrent.Callable;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.common.time.TimingParameters;

/**
 * Executes given {@link Callable}.
 * 
 * @author Christian Ribeaud
 */
public final class CallableExecutor
{
    private final int maxRetriesOnFailure;

    private final long millisToSleepOnFailure;

    public CallableExecutor()
    {
        this(TimingParameters.DEFAULT_MAXIMUM_RETRY_COUNT, TimingParameters.DEFAULT_MILLIS_TO_SLEEP_BEFORE_RETRYING);
    }

    public CallableExecutor(final int maxRetriesOnFailure, final long millisToSleepOnFailure)
    {
        assert millisToSleepOnFailure > -1 : "Negative value";
        assert maxRetriesOnFailure > -1 : "Negative value";
        this.maxRetriesOnFailure = maxRetriesOnFailure;
        this.millisToSleepOnFailure = millisToSleepOnFailure;
    }

    /**
     * Executes given <var>callable</var> until it returns a non-<code>null</code> value (or until <code>maxInvocationsOnFailure</code> is reached).
     */
    public final <T> T executeCallable(final Callable<T> callable) throws InterruptedExceptionUnchecked
    {
        int counter = 0;
        T result = null;
        try
        {
            do
            {
                InterruptedExceptionUnchecked.check();
                result = callable.call();
                if (result == null)
                {
                    ++counter;
                    if (counter < maxRetriesOnFailure && millisToSleepOnFailure > 0)
                    {
                        Thread.sleep(millisToSleepOnFailure);
                    }
                }
            } while (result == null && counter < maxRetriesOnFailure);
        } catch (final Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
        return result;
    }
}

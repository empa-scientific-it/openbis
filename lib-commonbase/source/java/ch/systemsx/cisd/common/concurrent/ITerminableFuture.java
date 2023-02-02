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

import java.util.concurrent.Future;

import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.common.action.ITerminable;

/**
 * An interface that combines {@link Future} and {@link ITerminable} and that can be used to control a {@link TerminableCallable}.
 * 
 * @author Bernd Rinn
 */
public interface ITerminableFuture<V> extends Future<V>, ITerminable
{
    /**
     * Returns <code>true</code>, if the task of the future is currently running and <code>false</code> otherwise.
     */
    public boolean isRunning();

    /**
     * Returns <code>true</code>, if the task of the future has already started running.
     */
    public boolean hasStarted();

    /**
     * Waits for the task of the future to finish running. The method waits at most <var>timeoutMillis</var> milli-seconds.
     * 
     * @return <code>true</code>, if the task of the future has finished running when the method returns.
     */
    public boolean waitForFinished(long timeoutMillis) throws InterruptedExceptionUnchecked;

    /**
     * Returns <code>true</code>, if the task of the future has already finished running.
     */
    public boolean hasFinished();

    /**
     * Terminates the task of the future if it has already started running. If it has not yet started running, this method cancels it using
     * {@link Future#cancel(boolean)}. Blocks until the task of the future has terminated if it already has started running.
     * <p>
     * Note that there is a semantic difference in the return value of this method to the return value of {@link Future#cancel(boolean)} in that it
     * returns <code>true</code> if the task is no longer running, regardless of whether it was <i>this</i> call that terminated it or not.
     * 
     * @return <code>true</code> if and only if the task of the future has terminated successfully or never been started.
     */
    @Override
    public boolean terminate() throws InterruptedExceptionUnchecked;

    /**
     * Terminates the task of the future if it has already started running. If it has not yet started running, this method cancels it using
     * {@link Future#cancel(boolean)}. Blocks until the task of the future has terminated or a timeout occurs if it already has started running.
     * <p>
     * Note that there is a semantic difference in the return value of this method to the return value of {@link Future#cancel(boolean)} in that it
     * returns <code>true</code> if the task is no longer running, regardless of whether it was <i>this</i> call that terminated it or not.
     * 
     * @param timeoutMillis The time (in milli-seconds) to wait at the most for the future to terminate.
     * @return <code>true</code> if and only if the task of the future has terminated successfully or never been started.
     */
    public boolean terminate(long timeoutMillis) throws InterruptedExceptionUnchecked;
}

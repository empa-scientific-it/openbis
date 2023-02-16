/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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

import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.common.action.ITerminable;

/**
 * Handler to a running process. Allows to wait for the result and stop the process.
 * 
 * @author Bernd Rinn
 */
public interface IProcessHandler extends ITerminable
{
    /**
     * Blocks until the result of the process is available and returns it.
     * 
     * @throws InterruptedExceptionUnchecked If the thread got interrupted.
     */
    ProcessResult getResult() throws InterruptedExceptionUnchecked;

    /**
     * Blocks until the result of the process is available and returns it, or returns a time out if the result is not available after
     * <var>millisToWaitForCompletion</var> milli-seconds.
     * @param doNotTimeoutWhenIO If <code>true</code>, do not consider it a timeout as long as there is I/O activity on either <code>stdout</code> or
     *            <code>stderr</code>, i.e. <code>millisToWaitForCompletion</code> will be considered the time to wait for I/O rather than the time to
     *            wait for the process to complete.
     * 
     * @throws InterruptedExceptionUnchecked If the thread got interrupted.
     */
    ProcessResult getResult(final long millisToWaitForCompletion, boolean doNotTimeOutWhenIO);
}
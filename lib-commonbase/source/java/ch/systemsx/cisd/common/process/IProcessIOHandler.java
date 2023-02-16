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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import ch.systemsx.cisd.common.concurrent.IActivityObserver;

/**
 * Role for handling the process I/O.
 * <p>
 * Recommended pattern for output-only processes in {@link IProcessIOHandler#handle(AtomicBoolean, IActivityObserver, OutputStream, InputStream, InputStream)}:
 * 
 * <pre>
 * while (processRunning.get())
 * {
 *     ProcessExecutionHelper.readBytesIfAvailable(stdout, out, buf, -1, false);
 *     ProcessExecutionHelper.readTextIfAvailable(bufStderr, errLines, false);
 *     if (...some data read...) activityObserver.update();
 * }
 * </pre>
 * 
 * @author Bernd Rinn
 */
public interface IProcessIOHandler
{
    /**
     * Method that gets the process' <code>stdin</code>, <code>stdout</code> and <code>stderr</code> and is expected to handlt the I/O of the process.
     * If <var>processRunning</var> is <code>false</code>, the process has been finished or terminated.
     * Implementations are supposed to call {@link IActivityObserver#update()} on <code>activityObserver</code> whenever it can read
     * data from either <code>stdout</code> or <code>stderr</code>.
     */
    public void handle(AtomicBoolean processRunning, IActivityObserver activityObserver, OutputStream stdin,
            InputStream stdout, InputStream stderr) throws IOException;
}
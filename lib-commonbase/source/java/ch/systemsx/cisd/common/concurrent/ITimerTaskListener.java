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

import java.util.TimerTask;

/**
 * Listener for {@link TimerTask} events.
 * 
 * @author Franz-Josef Elmer
 */
public interface ITimerTaskListener
{
    /**
     * Starts running. This method is invoked before {@link TimerTask#run()}.
     */
    public void startRunning();

    /**
     * Finishes running. This method is invoked after {@link TimerTask#run()} even if an exception is thrown.
     * 
     * @param statusProviderOrNull The status provider for the timer task, or <code>null</code>, if no status provider is available.
     */
    public void finishRunning(ITimerTaskStatusProvider statusProviderOrNull);

    /**
     * Canceling the task. This method is invoked before {@link TimerTask#cancel()}.
     */
    public void canceling();
}

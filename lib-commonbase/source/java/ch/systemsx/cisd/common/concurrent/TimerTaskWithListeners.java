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

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TimerTask;

/**
 * Decorator of a {@link TimerTask} objects which allows to add {@link ITimerTaskListener} objects which will be notified for certain type of events.
 *
 * @author Franz-Josef Elmer
 */
public class TimerTaskWithListeners extends TimerTask
{
    private final TimerTask timerTask;

    private final ITimerTaskStatusProvider statusProviderOrNull;

    private final Set<ITimerTaskListener> listeners = new LinkedHashSet<ITimerTaskListener>();

    /**
     * Creates an instance for the specified timer task.
     * 
     * @throws IllegalArgumentException if the argument is <code>null</code>
     */
    public TimerTaskWithListeners(TimerTask timerTask)
    {
        if (timerTask == null)
        {
            throw new IllegalArgumentException("Unspecified timer task.");
        }
        this.timerTask = timerTask;
        if (timerTask instanceof ITimerTaskStatusProvider)
        {
            this.statusProviderOrNull = (ITimerTaskStatusProvider) timerTask;
        } else
        {
            this.statusProviderOrNull = null;
        }
    }

    /**
     * Adds the specified listener. Listeners will be informed in the order they had been added.
     * 
     * @throws IllegalArgumentException if the argument is <code>null</code>
     */
    public void addListener(ITimerTaskListener listener)
    {
        if (listener == null)
        {
            throw new IllegalArgumentException("Unspecified timer task listener.");
        }
        listeners.add(listener);
    }

    @Override
    public void run()
    {
        for (ITimerTaskListener listener : listeners)
        {
            listener.startRunning();
        }
        try
        {
            timerTask.run();
        } finally
        {
            for (ITimerTaskListener listener : listeners)
            {
                listener.finishRunning(statusProviderOrNull);
            }
        }
    }

    @Override
    public boolean cancel()
    {
        for (ITimerTaskListener listener : listeners)
        {
            listener.canceling();
        }
        return timerTask.cancel();
    }

    @Override
    public long scheduledExecutionTime()
    {
        return timerTask.scheduledExecutionTime();
    }

}

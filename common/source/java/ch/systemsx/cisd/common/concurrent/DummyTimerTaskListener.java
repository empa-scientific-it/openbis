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


/**
 * Dummy implementation of the timer task listener interface. Useful for subclassing.
 *
 * @author Franz-Josef Elmer
 */
public class DummyTimerTaskListener implements ITimerTaskListener
{
    /** Does nothing. */
    @Override
    public void startRunning()
    {
    }
    
    /** Does nothing. */
    @Override
    public void finishRunning(ITimerTaskStatusProvider statusProviderOrNull)
    {
    }

    /** Does nothing. */
    @Override
    public void canceling()
    {
    }
    
}

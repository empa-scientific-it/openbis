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
 * A provider for status information about the last run of a {@link java.util.TimerTask}.
 * 
 * @author Bernd Rinn
 */
public interface ITimerTaskStatusProvider
{

    /**
     * Returns <code>true</code> if the last execution of the timer task found some useful work to do.
     */
    public boolean hasPerformedMeaningfulWork();

    /**
     * Returns <code>true</code> if during the last execution of the timer task errors have occurred.
     */
    public boolean hasErrors();

    /**
     * Returns the error log of the last execution, or <code>null</code>, if no errors have occurred.
     */
    public String tryGetErrorLog();

}

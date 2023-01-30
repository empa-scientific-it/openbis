/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.util.log;

import java.util.List;

/**
 * Interface for profiling and debugging by developers.
 * 
 * @author Tomasz Pylak
 */
public interface IProfilingTable
{
    /** true if logging has been enabled */
    public boolean isLoggingEnabled();

    /**
     * Logs the event. It's cheap to call this method.
     * 
     * @return id of the logged task. Use this id in {@link #logStop} method to measure the time between event start and stop of the event.
     */
    public int log(String description);

    /**
     * Logs the event with the specified id. Use {@link #log(String)} if you do not want to manage the events ids.
     */
    public void log(int taskId, String description);

    /**
     * Logs end of the already started event. It's cheap to call this method. Call this method to log the time from a call of a particular
     * {@link #log} method.
     */
    public void logStop(int taskId);

    /** clears all the logged events */
    public void clearLog();

    /** returns description of all the events */
    public List<String> getLoggedEvents();

}
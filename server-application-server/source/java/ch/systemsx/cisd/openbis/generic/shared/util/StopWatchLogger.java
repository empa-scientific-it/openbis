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

package ch.systemsx.cisd.openbis.generic.shared.util;

import org.apache.commons.lang3.time.StopWatch;

/**
 * Wrapper over {@link StopWatch} making it more simple to debug performance.
 * 
 * @author Piotr Buczek
 */
public class StopWatchLogger
{
    public static StopWatchLogger create()
    {
        return new StopWatchLogger();
    }

    public static StopWatchLogger createAndStart()
    {
        StopWatchLogger result = create();
        result.start();
        return result;
    }

    private StopWatchLogger()
    {
    }

    private StopWatch watch = new StopWatch();

    private boolean started = false;

    public void start()
    {
        started = true;
        watch.start();
    }

    public void log(String method)
    {
        if (started == false)
        {
            start();
        }
        System.err.println(watch.toString() + " - " + method);
    }

    public void logAndReset(String method)
    {
        log(method);
        watch.reset();
        watch.start();
    }

}

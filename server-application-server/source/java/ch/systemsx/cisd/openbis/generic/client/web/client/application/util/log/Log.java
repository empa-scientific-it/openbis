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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.util.log;

import com.google.gwt.core.client.Duration;

/**
 * Static class for logging.
 * 
 * @author Christian Ribeaud
 */
public final class Log
{
    /** Changing this value enables resp. disables the logging workflow. */
    static boolean enabled = true;

    private static LogImpl impl = LogImpl.getImpl();
    static
    {
        if (enabled)
        {
            impl.show();
        } else
        {
            impl.hide();
        }
    }

    private Log()
    {
        // Can not be instantiated
    }

    /** Enables the logging. */
    public final static void enable()
    {
        Log.enabled = true;
        impl.show();
    }

    /** Disables the logging. */
    public final static void disable()
    {
        Log.enabled = false;
        impl.hide();
    }

    /** Logs given <var>message</var>. */
    public static void log(final String message)
    {
        if (enabled)
        {
            impl.log(message);
        }
    }

    /** Logs the time given <var>task</var> took. */
    public static void logTimeTaken(final Duration duration, final String task)
    {
        if (enabled)
        {
            impl.logTimeTaken(duration, task);
        }
    }
}

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
import com.google.gwt.core.client.GWT;

/**
 * Logging implementation.
 * 
 * @author Christian Ribeaud
 */
abstract class LogImpl
{

    /** Logs given <var>message</var>. */
    public abstract void log(final String message);

    /** Logs the time given <var>task</var> took. */
    public abstract void logTimeTaken(final Duration duration, final String taskName);

    /** Shows this <code>LogImpl</code> implementation. */
    public abstract void show();

    /** Hides this <code>LogImpl</code> implementation. */
    public abstract void hide();

    /** Returns log implementation for this mode (<i>Web</i> or <i>Hosted</i>). */
    public final static LogImpl getImpl()
    {
        return GWT.isScript() ? (LogImpl) new WebModeLog() : new HostedModeLog();
    }
}

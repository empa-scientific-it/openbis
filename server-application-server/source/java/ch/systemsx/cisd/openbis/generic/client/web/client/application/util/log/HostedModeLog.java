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
 * {@link LogImpl} extension for the <i>Hosted Mode</i>.
 * 
 * @author Christian Ribeaud
 */
final class HostedModeLog extends LogImpl
{

    HostedModeLog()
    {
    }

    private final static StackTraceElement getStackElement()
    {
        return new Throwable().getStackTrace()[3];
    }

    private final static String toLine(final StackTraceElement line)
    {
        final String className = line.getClassName();
        return className.substring(className.lastIndexOf(".") + 1) + "(" + line.getLineNumber()
                + ")";
    }

    //
    // LogImpl
    //

    @Override
    public final void log(final String message)
    {
        GWT.log(toLine(getStackElement()) + ": " + message, null);
    }

    @Override
    public final void logTimeTaken(final Duration duration, final String taskName)
    {
        final String message = taskName + " took " + duration.elapsedMillis() / 1000F + "s";
        GWT.log(toLine(getStackElement()) + ": " + message, null);
    }

    @Override
    public final void hide()
    {
        // Nothing to do here
    }

    @Override
    public final void show()
    {
        // Nothing to do here
    }
}

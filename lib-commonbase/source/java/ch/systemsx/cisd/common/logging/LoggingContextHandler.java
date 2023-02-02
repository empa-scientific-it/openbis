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

package ch.systemsx.cisd.common.logging;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.MDC;

import ch.systemsx.cisd.common.server.IRemoteHostProvider;

/**
 * A class that handles the log4j context information.
 * 
 * @author Bernd Rinn
 */
public final class LoggingContextHandler
{

    private static final String MDC_KEY = "contextInfo";

    private final Map<String, String> loggingContextMap = new HashMap<String, String>();

    private final IRemoteHostProvider remoteHostProvider;

    public LoggingContextHandler(IRemoteHostProvider remoteHostProvider)
    {
        this.remoteHostProvider = remoteHostProvider;
    }

    /**
     * Adds specified context.
     */
    public void addContext(final String contextID, final String context)
    {
        loggingContextMap.put(contextID, context);
    }

    /** Destroys the logging context information for the session identified by <var>contextID</var>. */
    public final void destroyContext(String contextID)
    {
        loggingContextMap.remove(contextID);
    }

    /** Sets the logging context information for the <var>contextID</var> in the {@link MDC}. */
    public final void setMDC(String contextID)
    {
        final String context = loggingContextMap.get(contextID);
        String remoteHost = remoteHostProvider.getRemoteHost();
        if (context == null)
        {
            MDC.put(MDC_KEY, String.format(" {UNKNOWN_TOKEN='%s', ip=%s}", contextID, remoteHost));
        } else
        {
            MDC.put(MDC_KEY, String.format(" {%s, ip=%s}", context, remoteHost));
        }
    }

}

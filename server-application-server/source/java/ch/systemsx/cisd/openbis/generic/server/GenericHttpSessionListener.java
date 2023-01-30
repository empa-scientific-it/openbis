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

package ch.systemsx.cisd.openbis.generic.server;

import java.io.Serializable;
import java.util.Enumeration;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.SessionConstants;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * Implements {@link HttpSessionListener} and allows to perform chosen actions when session is being created or destroyed.
 * <p>
 * This ensures that the corresponding {@link Session} gets removed when a {@link HttpSession} gets destroyed.
 * </p>
 * 
 * @author Izabela Adamczyk
 */
public final class GenericHttpSessionListener implements HttpSessionListener
{
    protected static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            GenericHttpSessionListener.class);

    private static final String LOGGING_ACTIVATION_PARAM = "opennis.log.activation.parameter";

    private final HttpSessionActivationListener loggingActivationListener =
            new LoggingActivationListener();

    private class LoggingActivationListener implements HttpSessionActivationListener, Serializable
    {

        private static final long serialVersionUID = 1L;

        @Override
        public void sessionWillPassivate(HttpSessionEvent arg0)
        {
            logSessionEvent("sessionWillPassivate", arg0);
        }

        @Override
        public void sessionDidActivate(HttpSessionEvent arg0)
        {
            logSessionEvent("sessionDidActivate", arg0);
        }
    }

    //
    // HttpSessionListener
    //

    @Override
    public final void sessionCreated(final HttpSessionEvent sessionEvent)
    {
        logSessionEvent("sessionCreated", sessionEvent);
        sessionEvent.getSession().setAttribute(LOGGING_ACTIVATION_PARAM, loggingActivationListener);
    }

    @Override
    public final void sessionDestroyed(final HttpSessionEvent sessionEvent)
    {
        final HttpSession httpSession = sessionEvent.getSession();
        final String sessionToken =
                (String) httpSession
                        .getAttribute(SessionConstants.OPENBIS_SESSION_TOKEN_ATTRIBUTE_KEY);

        httpSession.removeAttribute(LOGGING_ACTIVATION_PARAM);
        logSessionEvent("sessionDestroyed", sessionEvent);

        if (sessionToken != null)
        {
            final IServer server =
                    (IServer) httpSession
                            .getAttribute(SessionConstants.OPENBIS_SERVER_ATTRIBUTE_KEY);

            if (server != null)
            {
                if (isExpiration(sessionEvent))
                {
                    server.expireSession(sessionToken);
                } else
                {
                    server.logout(sessionToken);
                }
            }
        }
    }

    private boolean isExpiration(HttpSessionEvent event)
    {
        final long now = System.currentTimeMillis();
        final HttpSession session = event.getSession();
        return (now - session.getLastAccessedTime()) > session.getMaxInactiveInterval();
    }

    private void logSessionEvent(String methodName, HttpSessionEvent event)
    {
        HttpSession session = event.getSession();
        StringBuilder message = new StringBuilder();
        message.append(methodName).append(": ");

        message.append("id=");
        message.append(session.getId());
        message.append(", lastAccessedTime=");
        message.append(session.getLastAccessedTime());
        message.append(", maxInactiveInterval=");
        message.append(session.getMaxInactiveInterval());

        message.append(", attributes=[");

        Enumeration<?> attrNames = session.getAttributeNames();

        while (attrNames.hasMoreElements())
        {
            String attrName = (String) attrNames.nextElement();
            Object formattedPropValue =
                    String.format("'%s'='%s', ", attrName, session.getAttribute(attrName));
            message.append(formattedPropValue);
        }
        message.append("]");
        operationLog.info(message.toString());
    }

}

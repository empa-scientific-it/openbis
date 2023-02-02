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

package ch.systemsx.cisd.common.logging;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

/**
 * An <code>ISimpleLogger</code> implementation which sends an email (via logger) when the number of log errors reaches a given number.
 * <p>
 * Note that this class sends one and only one notification email. To reset the state of this class, use {@link #reset(String)}.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class ConditionalNotificationLogger extends Log4jSimpleLogger
{
    private final Logger notificationLog;

    private final int ignoredErrorCountBeforeNotification;

    private int errorCount;

    private boolean notified;

    /**
     * @param log4jLogger The (normal, e.g. non-notify) logger that is called when <var>ignoredErrorCountBeforeNotification</var> is not reached yet.
     * @param notificationLog logger that is conditionally called.
     * @param ignoredErrorCountBeforeNotification the number of errors that are ignored before sending a notification email.
     */
    public ConditionalNotificationLogger(final Logger log4jLogger, final Logger notificationLog,
            final int ignoredErrorCountBeforeNotification)
    {
        this(log4jLogger, null, notificationLog, ignoredErrorCountBeforeNotification);
        assert ignoredErrorCountBeforeNotification > -1 : "Negative ignored error "
                + "count before notification";
    }

    /**
     * @param log4jLogger logger that is called when <var>ignoredErrorCountBeforeNotification</var> is not reached yet.
     * @param notificationLog the log that is conditionally called.
     * @param ignoredErrorCountBeforeNotification the number of errors that are ignored before sending a notification email.
     */
    public ConditionalNotificationLogger(final Logger log4jLogger,
            final Priority log4jOverridePriorityOrNull, final Logger notificationLog,
            final int ignoredErrorCountBeforeNotification)
    {
        super(log4jLogger, log4jOverridePriorityOrNull);
        assert notificationLog != null : "Notification log not specified";
        assert ignoredErrorCountBeforeNotification > -1 : "Negative ignored error "
                + "count before notification";
        this.ignoredErrorCountBeforeNotification = ignoredErrorCountBeforeNotification;
        this.notificationLog = notificationLog;
    }

    /**
     * Resets counting of errors and <code>notified</code> flag.
     * <p>
     * As side effect, it sends a notification log to inform that we are "green" again (if and only if an email has already been sent to inform the
     * "bad" state and if log info is enabled).
     * </p>
     * 
     * @param messageOrNull the info log message or <code>null</code>, if no message should be sent.
     */
    public final void reset(final String messageOrNull)
    {
        if (notified && notificationLog.isInfoEnabled() && messageOrNull != null)
        {
            notificationLog.info(messageOrNull);
        }
        errorCount = 0;
        notified = false;
    }

    //
    // ISimpleLogger
    //

    @Override
    public final void log(final LogLevel level, final String message)
    {
        if (LogLevel.ERROR.equals(level))
        {
            if (errorCount < ignoredErrorCountBeforeNotification)
            {
                super.log(level, message);
            } else if (notified == false)
            {
                notificationLog.error(message);
                notified = true;
            }
            errorCount++;
        } else
        {
            if (level != null)
            {
                super.log(level, message);
            }
        }
    }
}
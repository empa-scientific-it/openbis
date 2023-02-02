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

package ch.systemsx.cisd.common.servlet;

import javax.servlet.http.HttpSession;

/**
 * Super interface of all action log classes. Has action methods concerning authentication.
 *
 * @author Franz-Josef Elmer
 */
public interface IActionLog
{
    public enum LogoutReason
    {
        SESSION_LOGOUT(""), SESSION_TIMEOUT(" (session timed out)"), USER_DELETED(
                " (user was removed)");

        private final String logText;

        LogoutReason(String logText)
        {
            this.logText = logText;
        }

        public String getLogText()
        {
            return logText;
        }

    }

    /**
     * Logs a failed authentication attempt for the specified user code.
     */
    public void logFailedLoginAttempt(String userCode);

    /**
     * Logs success authentication.
     */
    public void logSuccessfulLogin();

    /**
     * Logs a logout.
     * 
     * @param httpSession Session objects which might contain information useful to be logged (e.g. user to be logged out).
     */
    public void logLogout(HttpSession httpSession);

    /**
     * Logs a call to set a new session user.
     */
    public void logSetSessionUser(String oldUserCode, String newUserCode, final boolean success);

}

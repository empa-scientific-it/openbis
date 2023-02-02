/*
 * Copyright 2018 ETH Zuerich, CISD
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

package ch.ethz.sis.filetransfer;

/**
 * A user session manager interface. A session manager is responsible for keeping track of user sessions. Depending on a use case an actual
 * implementation may dispatch the user session management tasks to some other already existing user session management mechanism or may store and
 * manage the user sessions itself. What user session ids are supported also depends on the actual user session manager implementation.
 * 
 * @author pkupczyk
 */
public interface IUserSessionManager
{

    /**
     * Validates a user's session right before a download is started. If the session is invalid throws {@link InvalidUserSessionException}.
     */
    public void validateBeforeDownload(IUserSessionId userSessionId) throws InvalidUserSessionException;

    /**
     * Validates a user's session during a download. Thanks to this method we can decide if a long-running download may continue even if the user's
     * session timed out. If the session is invalid throws {@link InvalidUserSessionException}.
     */
    public void validateDuringDownload(IUserSessionId userSessionId) throws InvalidUserSessionException;

}

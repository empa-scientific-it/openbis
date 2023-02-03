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

package ch.systemsx.cisd.authentication;

/**
 * Interface for a factory of {@link BasicSession} objects.
 *
 * @author Franz-Josef Elmer
 */
public interface ISessionFactory<T extends BasicSession>
{
    /**
     * Creates a session from the specified session token, user name, principal, remoteHost, session start (in milliseconds since start of the epoch),
     * and expiration time (in milliseconds).
     */
    public T create(String sessionToken, String userName, Principal principal,
            String remoteHost, long sessionStart, long sessionExpirationTime);

    public T create(String sessionToken, String userName, Principal principal,
            String remoteHost, long sessionStart, long sessionExpirationTime, boolean isPersonalAccessTokenSession,
            String personalAccessTokenSessionName);

}

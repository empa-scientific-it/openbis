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
 * Generator of a prefix for authentication log messages. The prefix contains user information. Minimum information is user name or ID and remote host
 * (i.e. IP address of the user client computer).
 * 
 * @author Franz-Josef Elmer
 */
public interface ILogMessagePrefixGenerator<T extends BasicSession>
{
    /**
     * Creates a prefix based on the specified session.
     */
    public String createPrefix(T session);

    /**
     * Creates a prefix for specified user and remote host.
     */
    public String createPrefix(String user, String remoteHost);
}

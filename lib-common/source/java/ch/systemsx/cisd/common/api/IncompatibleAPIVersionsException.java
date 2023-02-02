/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.api;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;

/**
 * Exception that is thrown if the client can not talk to the server because of API version incompatibility.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class IncompatibleAPIVersionsException extends EnvironmentFailureException
{
    private static final long serialVersionUID = 1L;

    public IncompatibleAPIVersionsException(int clientVersion, int serverVersion)
    {
        super(String.format("This client version is not the same as the server version "
                + "(client API version: %d, server " + "API version: %d", clientVersion,
                serverVersion));
    }
}

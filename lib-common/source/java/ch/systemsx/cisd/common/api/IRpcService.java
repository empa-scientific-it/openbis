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

/**
 * The most generic interface for RPC invocations into openBIS.
 * <p>
 * This interface defines a minimal interface presented by RPC services. It lets clients determine which version of the interface the server supports.
 * To do anything interesting, clients need to get a reference to a specific interface using the {@link IRpcServiceNameServer}.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public interface IRpcService
{
    //
    // Protocol versioning
    //
    /**
     * Returns the major version of the server side interface. Different major versions are incompatible with one another.
     */
    public int getMajorVersion();

    /**
     * Returns the minor version of this server side interface. Different minor versions, within the same major version, are compatible with one
     * another.
     */
    public int getMinorVersion();

}

/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.serviceconversation.server;

/**
 * A factory for services.
 * 
 * @author Bernd Rinn
 */
public interface IServiceFactory
{
    /**
     * Returns id for this service type.
     */
    public String getServiceTypeId();

    /**
     * Create a new service.
     */
    public IService create();

    /**
     * Returns the suggested timeout (in milli-seconds) of the client when waiting for a message from this service.
     */
    public int getClientTimeoutMillis();

    /**
     * Returns <code>true</code> if the service method is supposed to be cancelled when the client has an exception during service conversation. An
     * exception can e.g. happen when the client has a timeout waiting for the next server message.
     */
    public boolean interruptServiceOnClientException();
}

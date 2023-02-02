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

package ch.systemsx.cisd.common.serviceconversation;

import java.io.Serializable;

import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.base.exceptions.TimeoutExceptionUnchecked;
import ch.systemsx.cisd.common.serviceconversation.client.ServiceExecutionException;

/**
 * A messaging interface for a service of the service conversation framework.
 * 
 * @author Bernd Rinn
 */
public interface IServiceMessenger
{
    /**
     * Returns the service conversation id.
     */
    public String getId();

    /**
     * Send a message to the counter part.
     * 
     * @param message The message to send.
     * @throws InterruptedExceptionUnchecked If the client signaled termination (server-side only).
     * @throws ServiceExecutionException If the server signaled an exception (client-side only).
     */
    public void send(Serializable message) throws InterruptedExceptionUnchecked,
            ServiceExecutionException;

    /**
     * Receive a message from the counter part.
     * 
     * @param messageClass The class of the message to receive.
     * @return The message.
     * @throws UnexpectedMessagePayloadException If the next message is not compatible with <var>messageClass</var>.
     * @throws TimeoutExceptionUnchecked If no message arrived in the time specified in the settings.
     * @throws InterruptedExceptionUnchecked If the client signaled termination (server-side only).
     * @throws ServiceExecutionException If the server signaled an exception (client-side only).
     */
    public <T extends Serializable> T receive(Class<T> messageClass)
            throws UnexpectedMessagePayloadException, TimeoutExceptionUnchecked,
            InterruptedExceptionUnchecked, ServiceExecutionException;

    /**
     * Receive a message from the counter part.
     * 
     * @param messageClass The class of the message to receive.
     * @param timeoutMillis The timeout (in milli-seconds) to wait for a message to arrive, if no message is queued.
     * @return The message, or <code>null</code>, if no message become available during the period given by <var>timeoutMillis</var>.
     * @throws UnexpectedMessagePayloadException If the next message is not compatible with <var>messageClass</var>.
     * @throws TimeoutExceptionUnchecked If no message arrived in the time specified in the settings.
     * @throws InterruptedExceptionUnchecked If the client signaled termination (server-side only).
     * @throws ServiceExecutionException If the server signaled an exception (client-side only).
     */
    public <T extends Serializable> T tryReceive(Class<T> messageClass, int timeoutMillis);
}

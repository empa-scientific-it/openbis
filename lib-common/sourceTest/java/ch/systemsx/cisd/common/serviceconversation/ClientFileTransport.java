/*
 * Copyright 2012 ETH Zuerich, CISD
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

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.serviceconversation.client.IRemoteServiceConversationServer;

/**
 * The client specialization of {@link FileTransport}.
 * 
 * @author Bernd Rinn
 */
public class ClientFileTransport extends FileTransport implements IRemoteServiceConversationServer
{
    // This may be a bit over the top for this simple example, but so we are fully prepared for a
    // multi-threaded client that needs to ensure that one thread is not overwriting the
    // conversation start request of another thread within the same client.
    private AtomicInteger conversationIdx = new AtomicInteger();

    ClientFileTransport(String clientId)
    {
        super(PREFIX_CLIENT, clientId);
    }

    /**
     * Send a connection request.
     */
    void connect()
    {
        FileUtilities.writeToFile(new File(WD, CONNECT_PREFIX + getClientId()), "");
    }

    /**
     * Send a disconnection request.
     */
    void disconnect()
    {
        FileUtilities.writeToFile(new File(WD, DISCONNECT_PREFIX + getClientId()), "");
    }

    //
    // IRemoteServiceConversationServer
    //

    @Override
    public ServiceConversationDTO startConversation(String conversationTypeId)
    {
        final File file =
                new File(WD, START_PREFIX + getClientId() + "__" + conversationTypeId + "__"
                        + Integer.toString(conversationIdx.getAndIncrement()));
        FileUtilities.writeToFile(file, "");
        while (true)
        {
            if (file.length() > 0)
            {
                try
                {
                    ServiceConversationDTO dto =
                            FileUtilities.loadToObject(file, ServiceConversationDTO.class);
                    file.delete();
                    return dto;
                } catch (IOExceptionUnchecked ex)
                {
                    // Assume the object was not yet fully written and wait a bit...
                }
            }
            ConcurrencyUtilities.sleep(200L);
        }
    }

    /**
     * Returns the messages that are received within service conversations for this client.
     */
    ServiceMessage[] receive()
    {
        return receive(PREFIX_SERVER, getClientId());
    }
}

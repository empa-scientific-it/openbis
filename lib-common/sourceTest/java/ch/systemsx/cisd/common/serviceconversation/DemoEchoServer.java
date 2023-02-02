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

import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.serviceconversation.ServerFileTransport.StartConversationRequest;
import ch.systemsx.cisd.common.serviceconversation.server.ServiceConversationServer;

/**
 * A demo server for service conversations providing an echo service.
 * <p>
 * Messages are just files in the current working directory that this server gets started up at.
 * <p>
 * There are four types of messages:
 * <ol>
 * <li>client connects</li>
 * <li>client disconnects</li>
 * <li>client starts service conversation</li>
 * <li>client or server sends a message which is part of a service conversation.</li>
 * </ol>
 * 
 * @author Bernd Rinn
 */
public class DemoEchoServer
{

    private final int TIMEOUT = 1000;

    private final ServiceConversationServer server;

    /**
     * Create a new EchoServer.
     */
    DemoEchoServer()
    {
        server = new ServiceConversationServer();

        // Here is the right place to add any number of service types.
        // A 'service type' corresponds to a 'remote procedure' in the conventional RPC model.
        server.addServiceType(EchoService.createFactory(TIMEOUT));

        // Cleanup any left-overs from previous executions.
        ServerFileTransport.init();
    }

    void serve()
    {
        while (true)
        {
            processConnectionRequests();
            processDisconnectionRequests();
            processStartConversationRequests();
            processConversationMessages();

            ConcurrencyUtilities.sleep(200L);
        }
    }

    private void processConnectionRequests()
    {
        for (String clientId : ServerFileTransport.getConnectionRequests())
        {
            server.addClientResponseTransport(clientId, new ServerFileTransport(clientId));
        }
    }

    private void processDisconnectionRequests()
    {
        for (String clientId : ServerFileTransport.getDisconnectionRequests())
        {
            server.removeClientResponseTransport(clientId);
        }
    }

    private void processStartConversationRequests()
    {
        for (StartConversationRequest request : ServerFileTransport.getStartConversationRequests())
        {
            request.respond(server.startConversation(request.getConversationTypeId(),
                    request.getClientId(), TIMEOUT));
        }
    }

    private void processConversationMessages()
    {
        for (ServiceMessage msg : ServerFileTransport.receive())
        {
            server.getIncomingMessageTransport().send(msg);
        }
    }

    public static void main(String[] args)
    {
        final DemoEchoServer server = new DemoEchoServer();
        server.serve();
    }

}

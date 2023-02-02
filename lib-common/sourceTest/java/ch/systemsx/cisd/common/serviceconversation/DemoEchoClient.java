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
import ch.systemsx.cisd.common.serviceconversation.client.IServiceConversation;
import ch.systemsx.cisd.common.serviceconversation.client.ServiceConversationClient;

/**
 * A simple client using the {@link DemoEchoServer}.
 * 
 * @author Bernd Rinn
 */
public class DemoEchoClient
{
    private final String name;

    private final ClientFileTransport server;

    private final ServiceConversationClient client;

    DemoEchoClient(String name)
    {
        this.name = name;
        this.server = new ClientFileTransport(name);
        this.client = new ServiceConversationClient(server, server);
        init();
    }

    /**
     * Inform server that we wish to connect and ensure that we feed his messages into the client's incoming message queue.
     */
    private void init()
    {
        server.connect();
        Thread incomingMessagesPollingThread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    while (true)
                    {
                        for (ServiceMessage msg : server.receive())
                        {
                            client.getIncomingResponseMessageTransport().send(msg);
                        }
                        ConcurrencyUtilities.sleep(200L);
                    }
                }
            });
        incomingMessagesPollingThread.setDaemon(true);
        incomingMessagesPollingThread.start();
    }

    /**
     * Inform the server that we are disconnecting.
     */
    void exit()
    {
        server.disconnect();
    }

    /**
     * Do whatever you want to do with the server.
     */
    void run()
    {
        final IServiceConversation conversation = client.startConversation("echo");
        final String send1 = "Hello " + name;
        conversation.send(send1);
        final String received1 = conversation.receive(String.class);
        System.out.println("Sent: '" + send1 + "', received: '" + received1 + "': "
                + (received1.equals(send1) ? "OK" : "FAILURE"));

        final String send2 = "Hello " + name + " again";
        conversation.send(send2);
        final String received2 = conversation.receive(String.class);
        System.out.println("Sent: '" + send2 + "', received: '" + received2 + "': "
                + (received2.equals(send2) ? "OK" : "FAILURE"));

        // This service waits until terminated, so terminate it now. If this call is missed, the
        // service will run into a timeout.
        // Other services than EchoServer might work with a predefined number of messages from the
        // client and then will not need termination.
        conversation.terminate();
    }

    public static void main(String[] args)
    {
        final String name = (args.length == 0) ? "default" : args[0];
        final DemoEchoClient client = new DemoEchoClient(name);
        client.run();
        client.exit();
    }

}

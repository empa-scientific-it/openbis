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

import ch.systemsx.cisd.common.filesystem.FileUtilities;

/**
 * The server specialization of {@link FileTransport}.
 * 
 * @author Bernd Rinn
 */
class ServerFileTransport extends FileTransport
{
    ServerFileTransport(String clientId)
    {
        super(PREFIX_SERVER, clientId);
    }

    /**
     * Run once before using the transport. Cleans up any left-overs from previous executions.
     */
    static void init()
    {
        deleteFiles(getMessageFiles("MSG_"));
    }

    /**
     * Returns the client ids that send connection requests.
     */
    static String[] getConnectionRequests()
    {
        final File[] messageFiles = getMessageFiles(CONNECT_PREFIX);
        try
        {
            return stripPrefix(CONNECT_PREFIX,
                    FileUtilities.toFileNames(messageFiles));
        } finally
        {
            deleteFiles(messageFiles);
        }
    }

    /**
     * Returns the client ids that send disconnection requests.
     */
    static String[] getDisconnectionRequests()
    {
        final File[] messageFiles = getMessageFiles(DISCONNECT_PREFIX);
        try
        {
            return stripPrefix(DISCONNECT_PREFIX,
                    FileUtilities.toFileNames(messageFiles));
        } finally
        {
            deleteFiles(messageFiles);
        }
    }

    /**
     * A class to store a request for a new service conversation.
     */
    static class StartConversationRequest
    {
        private final File requestFile;

        private final String clientId;

        private final String conversationTypeId;

        StartConversationRequest(File requestFile)
        {
            this.requestFile = requestFile;
            final String[] parts = requestFile.getName().substring(START_PREFIX.length()).split("__");
            assert parts.length == 3;
            this.clientId = parts[0];
            this.conversationTypeId = parts[1];
        }

        String getClientId()
        {
            return clientId;
        }

        String getConversationTypeId()
        {
            return conversationTypeId;
        }

        void respond(ServiceConversationDTO conversationDTO)
        {
            FileUtilities.writeToFile(requestFile, conversationDTO);
        }
    }

    /**
     * Returns the client requests to start a conversation.
     */
    static StartConversationRequest[] getStartConversationRequests()
    {
        final File[] requestFiles = getMessageFiles(START_PREFIX);
        final StartConversationRequest[] requests = new StartConversationRequest[requestFiles.length];
        for (int i = 0; i < requestFiles.length; ++i)
        {
            requests[i] = new StartConversationRequest(requestFiles[i]);
        }
        return requests;
    }

    /**
     * Returns the messages that are received from any client within a running service conversation.
     */
    static ServiceMessage[] receive()
    {
        return receive(PREFIX_CLIENT, "");
    }
}

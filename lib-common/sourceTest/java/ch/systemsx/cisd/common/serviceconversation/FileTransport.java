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
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;

import ch.systemsx.cisd.common.filesystem.FileUtilities;

/**
 * An implementation of {@link IServiceMessageTransport} that works with files in the current working directory.
 * <p>
 * The message files sent by the client are:
 * <ul>
 * <li>MSG_CLIENT_CONNECT_&lt;CLIENT_ID&gt;</li>
 * <li>MSG_CLIENT_DISCONNECT_&lt;CLIENT_ID&gt;</li>
 * <li>MSG_CLIENT_START_CONVERSATION_&lt;CLIENT_ID&gt;__&lt;CONVERSATION_TYPE_ID&gt__&lt; UNIQUE_CONVERSATION_ID&gt;</li>
 * <li>MSG_CLIENT_CONVERSATION_&lt;CLIENT_ID&gt;__&lt;CONVERSATION_ID_&gt;__&lt; MESSAGE_IDX&gt;</li>
 * </ul>
 * <p>
 * <p>
 * The messages sent by the server are:
 * <ul>
 * <li>Fill ServiceConversationDTO into MSG_CLIENT_START_CONVERSATION_&lt;CLIENT_ID&gt;__&lt;CONVERSATION_TYPE_ID &gt;__&lt;UNIQUE_SUFFIX&gt;</li>
 * <li>MSG_SERVER_CONVERSATION_&lt;CLIENT_ID&gt;__&lt;CONVERSATION_ID&gt;_&gt;__&lt ;MESSAGE_IDX&gt;</li>
 * </ul>
 * 
 * @author Bernd Rinn
 */
class FileTransport implements IServiceMessageTransport
{
    static final String PREFIX_SERVER = "SERVER";

    static final String PREFIX_CLIENT = "CLIENT";

    static final String START_PREFIX = "MSG_CLIENT_START_CONVERSATION_";

    static final String CONNECT_PREFIX = "MSG_CLIENT_CONNECT_";

    static final String DISCONNECT_PREFIX = "MSG_CLIENT_DISCONNECT_";

    static final File WD = new File(".");

    private final String prefix;

    private final String clientId;

    FileTransport(String prefix, String clientId)
    {
        this.prefix = prefix;
        this.clientId = clientId;
    }

    static ServiceMessage[] receive(String prefix1, String prefix2)
    {
        final File[] messageFiles =
                getMessageFiles(String.format("MSG_%s_CONVERSATION_%s", prefix1, prefix2));
        final ServiceMessage[] messages = new ServiceMessage[messageFiles.length];
        int i = 0;
        for (File f : messageFiles)
        {
            messages[i++] = FileUtilities.loadToObject(f, ServiceMessage.class);
        }
        deleteFiles(messageFiles);
        return messages;
    }

    static File[] getMessageFiles(final String prefix)
    {
        final File[] messages = WD.listFiles(new FilenameFilter()
            {
                @Override
                public boolean accept(File dir, String name)
                {
                    return name.startsWith(prefix);
                }
            });
        // Sort from oldest to newest message so that the oldest message gets processed first.
        Arrays.sort(messages, 0, messages.length, new Comparator<File>()
            {
                @Override
                public int compare(File o1, File o2)
                {
                    return (int) (o1.lastModified() - o2.lastModified());
                }
            });
        return messages;
    }

    static String[] stripPrefix(String prefix, String[] strings)
    {
        final String[] strippedStrings = new String[strings.length];
        for (int i = 0; i < strings.length; ++i)
        {
            strippedStrings[i] = strings[i].substring(prefix.length());
        }
        return strippedStrings;
    }

    static void deleteFiles(File[] files)
    {
        for (File f : files)
        {
            f.delete();
        }
    }

    String getClientId()
    {
        return clientId;
    }

    //
    // IServiceMessageTransport
    //

    @Override
    public void send(ServiceMessage message)
    {
        final File msgFile =
                new File(String.format("MSG_%s_CONVERSATION_%s__%s__%s", prefix, clientId,
                        message.getConversationId(), message.getMessageIdx()));
        FileUtilities.writeToFile(msgFile, message);
    }
}
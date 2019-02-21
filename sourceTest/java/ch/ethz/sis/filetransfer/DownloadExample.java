/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.ethz.sis.filetransfer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author Franz-Josef Elmer
 *
 */
public class DownloadExample
{
    public static void main(String[] args) throws DownloadException, IOException
    {
        DownloadItemId iid1 = new DownloadItemId("abc");
        DownloadItemId iid2 = new DownloadItemId("def");
        UserSessionId sid1 = new UserSessionId("abc");
        UserSessionId sid2 = new UserSessionId("def");
        System.out.println(iid1+" == "+iid1 + " is " + iid1.equals(iid1));
        System.out.println(iid1+" == "+sid1 + " is " + iid1.equals(sid1)+" "+sid1.equals(iid1));
        System.out.println(iid1+" == "+iid2 + " is " + iid1.equals(iid2)+" "+iid2.equals(iid1));
//        ILogger logger = new ConsoleLogger(LogLevel.INFO);
        ILogger logger = new NullLogger();
 
        // 0. create a download server and client
        DownloadServer server = createServer(logger);
        DownloadClient client = createClient(logger, server);
 
        // 1. create a download
        DownloadClientDownload download = client.createDownload(new UserSessionId("example-session"));
 
        // 2. specify items to download
        download.addItem(new DownloadItemId("example-file1.txt"));
        download.addItem(new DownloadItemId("example-file2.txt"));
        download.addItem(new DownloadItemId("example-folder"));
 
        // 3. request for 2 download streams
        download.setPreferences(new DownloadPreferences(2));
        download.addListener(new DownloadListenerAdapter()
            {
                @Override
                public void onDownloadFinished(Map<IDownloadItemId, Path> results)
                {
                    // 5a. register an asynchronous listener that gets executed once the download is finished
                    logger.log(getClass(), LogLevel.INFO, "Download finished " + results);
                }
            });
 
        // 4. start the download (runs in the background)
        long t0 = System.currentTimeMillis();
        download.start();
 
        // 5b. wait until the download is finished and get the results
        download.await();
        
        System.out.println((System.currentTimeMillis() - t0) + " msec");
        Set<Entry<IDownloadItemId, Path>> entrySet = download.getResults().entrySet();
        for (Entry<IDownloadItemId, Path> entry : entrySet)
        {
            System.out.println(entry);
        }
    }
 
    private static DownloadServer createServer(ILogger logger)
    {
        IUserSessionManager sessionManager = new IUserSessionManager()
            {
                @Override
                public void validateBeforeDownload(IUserSessionId userSessionId) throws InvalidUserSessionException
                {
                    // do not check a user's session before a download (i.e. allow all users)
                }
 
 
                @Override
                public void validateDuringDownload(IUserSessionId userSessionId) throws InvalidUserSessionException
                {
                    // do not check a user's session during a download (i.e. allow all users)
                }
            };
 
        IChunkProvider chunkProvider = new FileSystemChunkProvider(logger, 1024 * 1024L)
            {
                @Override
                protected Path getFilePath(IDownloadItemId itemId)
                {
                    // load items from "example-items" folder
                    return Paths.get("targets/example-store/example-items", itemId.getId());
                }
            };
 
        ISerializerProvider serializerProvider = new DefaultSerializerProvider(logger, new IDownloadItemIdSerializer()
            {
                @Override
                public ByteBuffer serialize(IDownloadItemId itemId) throws DownloadException
                {
                    // just serialize "id" field
                    return ByteBuffer.wrap(itemId.getId().getBytes());
                }
            });
 
        IConcurrencyProvider concurrencyProvider = new IConcurrencyProvider()
            {
                @Override
                public int getAllowedNumberOfStreams(IUserSessionId userSessionId, Integer wishedNumberOfStreams, List<DownloadState> downloadStates)
                        throws DownloadException
                {
                    // allow the wished number of streams but not more than 5
                    return Math.min(wishedNumberOfStreams, 5);
                }
            };
 
        DownloadServerConfig config = new DownloadServerConfig();
        config.setLogger(logger);
        config.setSessionManager(sessionManager);
        config.setChunkProvider(chunkProvider);
        config.setSerializerProvider(serializerProvider);
        config.setConcurrencyProvider(concurrencyProvider);
 
        return new DownloadServer(config);
    }
 
    private static DownloadClient createClient(ILogger logger, IDownloadServer server)
    {
        IDeserializerProvider deserializerProvider = new DefaultDeserializerProvider(logger, new IDownloadItemIdDeserializer()
            {
                @Override
                public IDownloadItemId deserialize(ByteBuffer buffer) throws DownloadException
                {
                    // deserialize "id" field and recreate item id object
                    return new DownloadItemId(new String(buffer.array(), buffer.position(), buffer.limit()));
                }
            });
 
        // store downloaded items in "example-store" folder
        IDownloadStore store = new FileSystemDownloadStore(logger, Paths.get("targets/example-destination"));
 
        // retry 3 times; wait 1000 ms between the retries; double the waiting time on each retry
        IRetryProvider retryProvider = new DefaultRetryProvider(logger, 3, 1000, 2);
 
        DownloadClientConfig config = new DownloadClientConfig();
        config.setLogger(logger);
        config.setServer(server);
        config.setStore(store);
        config.setDeserializerProvider(deserializerProvider);
        config.setRetryProvider(retryProvider);
 
        return new DownloadClient(config);
    }

}

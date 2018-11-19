/*
 * Copyright 2018 ETH Zuerich, CISD
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
import java.io.InputStream;
import java.util.List;

/**
 * @author pkupczyk
 */
public class FailingDownloadServer implements IDownloadServer
{

    public static final String OPERATION_START_DOWNLOAD_SESSION = "IDownloadServer.startDownloadSession";

    public static final String OPERATION_QUEUE = "IDownloadServer.queue";

    public static final String OPERATION_DOWNLOAD = "IDownloadServer.download";

    public static final String OPERATION_DOWNLOAD_READ = "IDownloadServer.download.read";

    public static final String OPERATION_FINISH_DOWNLOAD_SESSION = "IDownloadServer.finishDownloadSession";

    private IDownloadServer server;

    private FailureGenerator generator;

    public FailingDownloadServer(IDownloadServer server, FailureGenerator generator)
    {
        this.server = server;
        this.generator = generator;
    }

    @Override
    public DownloadSession startDownloadSession(IUserSessionId userSessionId, List<IDownloadItemId> itemIds, DownloadPreferences preferences)
            throws DownloadItemNotFoundException, InvalidUserSessionException, DownloadException
    {
        generator.maybeFail(OPERATION_START_DOWNLOAD_SESSION);
        return server.startDownloadSession(userSessionId, itemIds, preferences);
    }

    @Override
    public void queue(DownloadSessionId downloadSessionId, List<DownloadRange> ranges)
            throws InvalidUserSessionException, InvalidDownloadSessionException, DownloadException
    {
        generator.maybeFail(OPERATION_QUEUE);
        server.queue(downloadSessionId, ranges);
    }

    @Override
    public InputStream download(DownloadSessionId downloadSessionId, DownloadStreamId streamId, Integer numberOfChunksOrNull)
            throws InvalidUserSessionException, InvalidDownloadSessionException, InvalidDownloadStreamException, DownloadException
    {
        generator.maybeFail(OPERATION_DOWNLOAD);
        InputStream stream = server.download(downloadSessionId, streamId, null);
        return new InputStream()
            {
                @Override
                public int read() throws IOException
                {
                    int b = stream.read();
                    try
                    {
                        generator.maybeFail(OPERATION_DOWNLOAD_READ);
                    } catch (DownloadException e)
                    {
                        throw new RuntimeException(e);
                    }
                    return b;
                }
            };
    }

    @Override
    public void finishDownloadSession(DownloadSessionId downloadSessionId) throws DownloadException
    {
        generator.maybeFail(OPERATION_FINISH_DOWNLOAD_SESSION);
        server.finishDownloadSession(downloadSessionId);
    }

}

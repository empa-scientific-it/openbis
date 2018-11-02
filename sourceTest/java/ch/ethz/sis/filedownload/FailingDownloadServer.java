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

package ch.ethz.sis.filedownload;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import ch.ethz.sis.filedownload.DownloadException;
import ch.ethz.sis.filedownload.DownloadItemNotFoundException;
import ch.ethz.sis.filedownload.DownloadPreferences;
import ch.ethz.sis.filedownload.DownloadSession;
import ch.ethz.sis.filedownload.DownloadSessionId;
import ch.ethz.sis.filedownload.DownloadStreamId;
import ch.ethz.sis.filedownload.IDownloadItemId;
import ch.ethz.sis.filedownload.IDownloadServer;
import ch.ethz.sis.filedownload.IUserSessionId;
import ch.ethz.sis.filedownload.InvalidDownloadSessionException;
import ch.ethz.sis.filedownload.InvalidDownloadStreamException;
import ch.ethz.sis.filedownload.InvalidUserSessionException;
import ch.ethz.sis.filedownload.DownloadRange;

/**
 * @author pkupczyk
 */
public class FailingDownloadServer implements IDownloadServer
{

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
        generator.maybeFail("IDownloadServer.startDownloadSession");
        return server.startDownloadSession(userSessionId, itemIds, preferences);
    }

    @Override
    public InputStream download(DownloadSessionId downloadSessionId, DownloadStreamId streamId, Integer numberOfChunksOrNull)
            throws InvalidUserSessionException, InvalidDownloadSessionException, InvalidDownloadStreamException, DownloadException
    {
        generator.maybeFail("IDownloadServer.download");
        InputStream stream = server.download(downloadSessionId, streamId, null);
        return new InputStream()
            {
                @Override
                public int read() throws IOException
                {
                    int b = stream.read();
                    try
                    {
                        generator.maybeFail("IDownloadServer.download.read");
                    } catch (DownloadException e)
                    {
                        throw new RuntimeException(e);
                    }
                    return b;
                }
            };
    }

    @Override
    public void queue(DownloadSessionId downloadSessionId, List<DownloadRange> ranges)
            throws InvalidUserSessionException, InvalidDownloadSessionException, DownloadException
    {
        generator.maybeFail("IDownloadServer.requeue");
        server.queue(downloadSessionId, ranges);
    }

    @Override
    public void finishDownloadSession(DownloadSessionId downloadSessionId) throws DownloadException
    {
        generator.maybeFail("IDownloadServer.finishDownloadSession");
        server.finishDownloadSession(downloadSessionId);
    }

}

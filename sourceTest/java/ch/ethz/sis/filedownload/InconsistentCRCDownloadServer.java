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

/**
 * @author pkupczyk
 */
public class InconsistentCRCDownloadServer implements IDownloadServer
{

    private IDownloadServer server;

    private int downloadIndex;

    private int downloadIndexToFail;

    private int byteIndexToFail;

    public InconsistentCRCDownloadServer(IDownloadServer server, int downloadIndexToFail, int byteIndexToFail)
    {
        this.server = server;
        this.downloadIndexToFail = downloadIndexToFail;
        this.byteIndexToFail = byteIndexToFail;
    }

    @Override
    public DownloadSession startDownloadSession(IUserSessionId userSessionId, List<IDownloadItemId> itemIds, DownloadPreferences preferences)
            throws DownloadItemNotFoundException, InvalidUserSessionException, DownloadException
    {
        return server.startDownloadSession(userSessionId, itemIds, preferences);
    }

    @Override
    public void queue(DownloadSessionId downloadSessionId, List<DownloadRange> ranges)
            throws InvalidUserSessionException, InvalidDownloadSessionException, DownloadException
    {
        server.queue(downloadSessionId, ranges);
    }

    @Override
    public InputStream download(DownloadSessionId downloadSessionId, DownloadStreamId streamId, Integer numberOfChunksOrNull)
            throws InvalidUserSessionException, InvalidDownloadSessionException, InvalidDownloadStreamException, DownloadException
    {
        InputStream stream = server.download(downloadSessionId, streamId, numberOfChunksOrNull);

        if (downloadIndex++ == downloadIndexToFail)
        {
            return new InputStream()
                {
                    private int byteIndex;

                    @Override
                    public int read() throws IOException
                    {
                        if (byteIndex++ == byteIndexToFail)
                        {
                            return stream.read() + 1;
                        } else
                        {
                            return stream.read();
                        }
                    }
                };
        } else
        {
            return stream;
        }
    }

    @Override
    public void finishDownloadSession(DownloadSessionId downloadSessionId) throws DownloadException
    {
        server.finishDownloadSession(downloadSessionId);
    }

}

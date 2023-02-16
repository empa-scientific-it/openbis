/*
 * Copyright ETH 2018 - 2023 Zürich, Scientific IT Services
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

import java.io.InputStream;
import java.util.List;

/**
 * A download server interface. The interface defines methods a download client can use to download files from the server. Methods in this interface
 * have been designed specifically with reliability and high-performance in mind. To enable effective downloads of very large files, the files are cut
 * into pieces called chunks. Each chunk is downloaded individually and can be re-downloaded in case of any problems without a need of restarting the
 * whole file. To increase the performance of downloads multiple chunks can be downloaded in parallel via multiple download streams (each download
 * stream can be controlled by a separate thread in a download client). Separate download streams can also increase the overall performance in
 * networks with bandwidth throttling. Usage:
 * <ul>
 * <li>start a download session for given download items using {@link #startDownloadSession(IUserSessionId, List, DownloadPreferences)} method</li>
 * <li>queue chunks to be downloaded using {@link #queue(DownloadSessionId, List)} method</li>
 * <li>download the queued chunks using {@link #download(DownloadSessionId, DownloadStreamId, Integer)} method</li>
 * <li>in case the download of some chunks fails {@link #queue(DownloadSessionId, List)} and
 * {@link #download(DownloadSessionId, DownloadStreamId, Integer)} methods can be called again to re-queue and re-download problematic chunks
 * respectively</li>
 * <li>finish a download session using {@link #finishDownloadSession(DownloadSessionId)} method</li>
 * </ul>
 * 
 * @author pkupczyk
 */
public interface IDownloadServer
{

    /**
     * Starts a download session to fetch items with given ids. Basing on the ids items are first found and then sliced into chunks (the way items are
     * found as well as the chunk size depends on the actual server implementation). Each created chunk is assigned a sequence number. Chunks that
     * constitute a single item always have consecutive sequence numbers. Therefore an item can be represented as a range of chunks (limited by
     * "start" and "end" sequence numbers). Ranges of all items are returned as part of {@link DownloadSession}. Before chunks can be downloaded
     * (using {@link #download(DownloadSessionId, DownloadStreamId, Integer)}) they have to be first queued (using
     * {@link #queue(DownloadSessionId, List)}).
     * 
     * @param userSessionId User session id (what session ids are supported and how they are validated depends on the actual download server
     *            implementation)
     * @param itemIds Ids of items to be downloaded (what item ids are supported and how the corresponding items are found depends on the actual
     *            download server implementation)
     * @param preferences Can be used to set additional download preferences, e.g. specify a wished number of download streams. Increasing the number
     *            of download streams usually increases the overall download performance, still it requires additional resources at both download
     *            client and download server sides. Therefore, a server may ignore a wish and permit fewer streams than requested (e.g. in case it is
     *            under a heavy load). Ids of permitted download streams are returned as part of the {@link DownloadSession}.
     * @throws DownloadItemNotFoundException In case no item can be found for a given item id
     * @throws InvalidUserSessionException In case user session is not valid
     * @throws DownloadException In case of other problems
     */
    public DownloadSession startDownloadSession(IUserSessionId userSessionId, List<IDownloadItemId> itemIds,
            DownloadPreferences preferences) throws DownloadItemNotFoundException, InvalidUserSessionException, DownloadException;

    /**
     * Adds the specified ranges of chunks to the download queue (chunks with sequence numbers that belong to [range.start, range.end] are added). The
     * download queue is shared among all download streams of the same download session. The queue never contains duplicates (adding a chunk that
     * already belongs to the queue has no effect). An attempt to add an unknown chunk (with a sequence number that does not belong to the download
     * session - see ranges returned by @{link {@link #startDownloadSession(IUserSessionId, List, DownloadPreferences)}) fails with an exception.
     * 
     * @param downloadSessionId Download session id returned by {@link #startDownloadSession(IUserSessionId, List, DownloadPreferences)} method
     * @param ranges Ranges of chunks to be added to the download queue
     * @throws InvalidUserSessionException In case user session is not valid
     * @throws InvalidDownloadSessionException In case download session is not valid
     * @throws DownloadException In case of other problems
     */
    public void queue(DownloadSessionId downloadSessionId, List<DownloadRange> ranges)
            throws InvalidUserSessionException, InvalidDownloadSessionException, DownloadException;

    /**
     * Returns an input stream to read chunks from the download queue (chunks have to be first added to the queue using @{link
     * {@link #queue(DownloadSessionId, List)}} method). How chunks are serialized into bytes depends on the actual server implementation. The
     * returned input stream is bound to the given download stream id. Only stream ids returned by @{link
     * {@link #startDownloadSession(IUserSessionId, List, DownloadPreferences)}} method are allowed. This method can be called multiple times with the
     * same download stream id (e.g. to restart an interrupted download). In such case the input stream that was previously bound to the same download
     * stream id is automatically closed.
     * 
     * @param downloadSessionId Download session id returned by {@link #startDownloadSession(IUserSessionId, List, DownloadPreferences)} method
     * @param streamId One of the download stream ids returned by {@link #startDownloadSession(IUserSessionId, List, DownloadPreferences)} method
     * @param numberOfChunksOrNull Number of chunks that can be read from the input stream. If specified, the input stream will only return the
     *            requested number of chunks even if the download queue contains more. If <code>null</code>, the input stream will allow to read all chunks from
     *            the download queue. Use always <code>null</code> if it isn't evident what to choose as a concrete value. 
     * @throws InvalidUserSessionException In case user session is not valid
     * @throws InvalidDownloadSessionException In case download session is not valid
     * @throws InvalidDownloadStreamException In case download stream id is not valid
     * @throws DownloadException In case of other problems
     */
    public InputStream download(DownloadSessionId downloadSessionId, DownloadStreamId streamId, Integer numberOfChunksOrNull)
            throws InvalidUserSessionException, InvalidDownloadSessionException, InvalidDownloadStreamException, DownloadException;

    /**
     * Finishes a download session and releases all the related resources. If the specified download session has been already finished before, then
     * nothing is done.
     * 
     * @param downloadSessionId Download session id returned by {@link #startDownloadSession(IUserSessionId, List, DownloadPreferences)} method
     * @throws DownloadException In case of any problems
     */
    public void finishDownloadSession(DownloadSessionId downloadSessionId) throws DownloadException;

}

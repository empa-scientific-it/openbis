package ch.ethz.sis.filetransfer;

import java.io.InputStream;
import java.util.List;

/**
 * The API of the server-side. Apart from it a client-side API should be created. The client side should handle all corner-cases and guarantee
 * reliability even if a connection to the server breaks, server restarts etc. The client should be able to restart a broken download. It can store
 * what has been downloaded up to now in some additional file in the download folder which keep a list of downloaded chunks etc.
 */
public interface IDownloadServer
{

    /**
     * Obtains the list of all files with their sizes to download in memory. Pre-calculates the number of chunks to return the range on the
     * DownloadSessionInformation object. Important Note: The algorithm used to come with the list of chunks should guarantee to give always the same
     * order for the chunks given the same List<DownloadItemId>. Important Note: The algorithm to calculate the number of chunks should allow
     * concurrency, the level of concurrency should be configurable on the server. Important Note: Max chunk size can be configured on the server, to
     * split larger files for a higher level of concurrency.
     */

    /**
     * Exceptions: DownloadItemIdDontExists InvalidUserSession
     */

    /**
     * Items list consists of objects that uniquely identify what is about to be downloaded (e.g. identifiers of files, identifiers of the whole data
     * sets etc.). Basing on the list of items we should always return chunks in the same order, e.g. given an item which is a data set, we should
     * always sort data sets files and then return their chunks. This way after a server restart we can restart a download and continue from the place
     * it was interrupted. With different order of chunks that would be impossible.
     */

    /**
     * Passed preferences contain a "wished concurrency" (a number of threads that can be used for the download), while the returned session
     * information contains an "allowed concurrency" (i.e. a server, depending on its current load, may decide to disallow too high concurrency and
     * instead of wished concurrency allow a lower number of concurrent threads).
     */

    /**
     * Configurable on the server-side: max allowed concurrency per download, max allowed concurrency per all downloads, max chunk size.
     */

    public DownloadSession startDownloadSession(IUserSessionId userSessionId, List<IDownloadItemId> itemIds,
            DownloadPreferences preferences) throws DownloadItemNotFoundException, InvalidUserSessionException, DownloadException;

    /**
     * Method for adding a range of chunks to the download queue. Question: shouldn't it take userSessionToken and downloadSessionToken as parameters?
     */
    public void queue(DownloadSessionId downloadSessionId, List<DownloadRange> ranges)
            throws InvalidUserSessionException, InvalidDownloadSessionException, DownloadException;

    /**
     * Each client thread receives an OutputStream containing List<Chunk> Important Note: Chunk download order is random, the server feeds all threads
     * from the same queue until finish, the client is supposed to put the parts together. Implementation Detail: To avoid writing things twice due to
     * a random order an implementation on the client using NIO2 and channels that extend files is recommended since file sizes are unknown.
     * Implementation Detail: Clients should calculate the checksums as they download and verify every separate chunk, if a checksum fails they can
     * requeue the chunks. Implementation Detail: Clients when the download is finish validate to have all chunks from the initial range, if any chunk
     * is missing, they can requeue the chunk. Implementation Detail: To handle correctly both download and requeue methods the Chunk objects should
     * be not only on a queue but also on a Map or array that allows them to be retrieved by index.
     */

    /**
     * Exceptions: InvalidUserSession InvalidDownloadSession NumberOfDownloadThreadsExceeded
     */

    /**
     * Given a download session token the method returns an output stream with chunks. The chunks that are taken from the queue that was created
     * during startDownloadSession method call. If we call the download method multiple times (e.g. from separate threads) then the queue of chunks is
     * consumed by these multiple threads simultaneously and each output stream will contain some subset of all chunks (there should be no duplicates
     * of chunks among different output streams).
     */

    /**
     * A number of times the download method can be called is limited by the "allowed concurrency" set during startDownloadSession method call. If a
     * user tries to download with a higher concurrency (i.e. with more threads) then we should probably throw an exception. We should somehow monitor
     * if all download threads are still alive. If some of them seem to be unused shall we then maybe close them on the server-side and allow a user
     * to call the "download" method again.
     */

    /**
     * The "rangeToDownload" can be used to specify range of chunks to download. Can be useful, if we want to restart a download after a server crash.
     * In such case, we call startDownloadSession and then explicitly ask to download a range that failed. If null, then we take the chunks from the
     * queue. Question: if there is more than one call with the same range to download then shall we split between across the multiple output streams?
     * Maybe it would be better to move the rangeToDownload to the startDownloadSession method?
     */

    /**
     * The returned output stream consists of chunks, i.e. fields of chunk header followed by fields of chunk body. We do not send serialized java
     * objects but just "plain" bytes to save space and improve the speed.
     */

    public InputStream download(DownloadSessionId downloadSessionId, DownloadStreamId streamId, Integer numberOfChunksOrNull)
            throws InvalidUserSessionException, InvalidDownloadSessionException, InvalidDownloadStreamException, DownloadException;

    /**
     * Releases resources from the session Important Note: Sessions should also finish after a long time of inactivity that can be configured on the
     * Server. Exceptions: InvalidUserSession InvalidDownloadSession
     */
    public void finishDownloadSession(DownloadSessionId downloadSessionId) throws DownloadException;

}

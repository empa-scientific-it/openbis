package ch.ethz.sis.filetransfer;

import java.io.InputStream;
import java.util.List;

public interface IDownloadServer
{

    public DownloadSession startDownloadSession(IUserSessionId userSessionId, List<IDownloadItemId> itemIds,
            DownloadPreferences preferences) throws DownloadItemNotFoundException, InvalidUserSessionException, DownloadException;

    public void queue(DownloadSessionId downloadSessionId, List<DownloadRange> ranges)
            throws InvalidUserSessionException, InvalidDownloadSessionException, DownloadException;

    public InputStream download(DownloadSessionId downloadSessionId, DownloadStreamId streamId, Integer numberOfChunksOrNull)
            throws InvalidUserSessionException, InvalidDownloadSessionException, InvalidDownloadStreamException, DownloadException;

    public void finishDownloadSession(DownloadSessionId downloadSessionId) throws DownloadException;

}

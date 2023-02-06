/*
 * Copyright 2019 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.dssapi.v3.fastdownload;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import ch.ethz.sis.filetransfer.DefaultDeserializerProvider;
import ch.ethz.sis.filetransfer.DefaultRetryProvider;
import ch.ethz.sis.filetransfer.DownloadClient;
import ch.ethz.sis.filetransfer.DownloadClientConfig;
import ch.ethz.sis.filetransfer.DownloadClientDownload;
import ch.ethz.sis.filetransfer.DownloadException;
import ch.ethz.sis.filetransfer.DownloadItemId;
import ch.ethz.sis.filetransfer.DownloadItemNotFoundException;
import ch.ethz.sis.filetransfer.DownloadPreferences;
import ch.ethz.sis.filetransfer.DownloadSessionId;
import ch.ethz.sis.filetransfer.FileSystemDownloadStore;
import ch.ethz.sis.filetransfer.IDownloadItemId;
import ch.ethz.sis.filetransfer.IDownloadItemIdDeserializer;
import ch.ethz.sis.filetransfer.IDownloadListener;
import ch.ethz.sis.filetransfer.IDownloadServer;
import ch.ethz.sis.filetransfer.ILogger;
import ch.ethz.sis.filetransfer.IRetryProvider;
import ch.ethz.sis.filetransfer.IRetryProviderFactory;
import ch.ethz.sis.filetransfer.IUserSessionId;
import ch.ethz.sis.filetransfer.NullLogger;
import ch.ethz.sis.filetransfer.UserSessionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fastdownload.FastDownloadSession;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fastdownload.FastDownloadSessionOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.id.DataSetFilePermId;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.id.IDataSetFileId;

/**
 * Helper class for downloading files by using an instance of a {@link FastDownloadSession} instance. Typical usage:
 * 
 * <pre>
 * List<IDataSetFileId> fileIds = ...;
 * FastDownloadSessionOptions options = new FastDownloadSessionOptions().wishedNumberOfStreams(3);
 * FastDownloadSession downloadSession = dataStoreServer.createFastDownloadSession(sessionToken, fileIds, options);
 * File target = ...;
 * FastDownloadResult result = new FastDownloader(downloadSession).downloadTo(target);
 * if (result.getStatus() == DownloadStatus.FINISHED) {
 *     // success
 * }
 * </pre>
 * 
 * @author Franz-Josef Elmer
 */
public class FastDownloader
{
    private IDownloadServer downloadServer;

    private ILogger logger;

    private List<IDownloadListener> listeners = new ArrayList<>();

    private IRetryProviderFactory retryProviderFactory;

    private UserSessionId userSessionId;

    private FastDownloadSessionOptions downLoadOptions;

    private List<IDownloadItemId> downloadItems;

    private String downLoadUrl;

    /**
     * Creates an instance for the specified download session object.
     */
    public FastDownloader(FastDownloadSession session)
    {
        this(new RemoteFastDownloadServer(session.getDownloadUrl()), session.getDownloadUrl(),
                session.getFileTransferUserSessionId(), session.getOptions(), session.getFiles());
    }

    FastDownloader(IDownloadServer downloadServer, String downLoadUrl, String FileTransferUserSessionId,
            FastDownloadSessionOptions downLoadOptions, List<IDataSetFileId> files)
    {
        this.downloadServer = downloadServer;
        this.downLoadUrl = downLoadUrl;
        this.downLoadOptions = downLoadOptions;
        userSessionId = new UserSessionId(FileTransferUserSessionId);
        downloadItems = files.stream().map(fid -> createId(fid)).collect(Collectors.toList());
    }

    private static final IDownloadItemId createId(IDataSetFileId dataSetFileId)
    {
        if (dataSetFileId instanceof DataSetFilePermId == false)
        {
            throw new IllegalArgumentException("Unsupported fileId: " + dataSetFileId);
        }
        DataSetFilePermId filePermId = (DataSetFilePermId) dataSetFileId;
        IDataSetId dataSetId = filePermId.getDataSetId();
        if (dataSetId instanceof DataSetPermId == false)
        {
            throw new IllegalArgumentException("Unsupported dataSetId: " + dataSetId);
        }
        return new DownloadItemId(((DataSetPermId) dataSetId).getPermId() + "/" + filePermId.getFilePath());
    }

    /**
     * Returns the remote fast download server. This is useful in case of direct communication with the server.
     */
    public IDownloadServer getDownloadServer()
    {
        if (downloadServer == null)
        {
            downloadServer = new RemoteFastDownloadServer(downLoadUrl);
        }
        return downloadServer;
    }

    /**
     * Sets the logger used by the internal download client. The default logger is an instance of {@link NullLogger} which logs nothing.
     * 
     * @return this instance
     */
    public FastDownloader withLogger(ILogger logger)
    {
        this.logger = logger;
        return this;
    }

    /**
     * Adds a download listener. The internal download client will send events to the listener during the download session.
     * 
     * @return this instance
     */
    public FastDownloader withListener(IDownloadListener listener)
    {
        listeners.add(listener);
        return this;
    }

    /**
     * Sets the factory for an {@link IRetryProvider}. The default is {@link DefaultRetryProvider} with maximumNumberOfRetries = 3,
     * waitingTimeBetweenRetries = 1 sec, waitingTimeBetweenRetriesIncreasingFactor = 2.
     * 
     * @return this instance
     */
    public FastDownloader withRetryProviderFactory(IRetryProviderFactory retryProviderFactory)
    {
        this.retryProviderFactory = retryProviderFactory;
        return this;
    }

    /**
     * Runs the download session an stores all downloaded files in the specified folder.
     * 
     * @return the result of the download session.
     */
    public FastDownloadResult downloadTo(File folder)
    {
        DownloadClient client = createDownloadClient(folder.toPath());
        DownloadClientDownload download = client.createDownload(userSessionId);
        for (IDownloadListener listener : listeners)
        {
            download.addListener(listener);
        }
        download.addItems(downloadItems);
        download.setPreferences(createPreferences(downLoadOptions));
        download.start();
        download.await();
        return new FastDownloadResult(download);
    }

    private DownloadClient createDownloadClient(Path root)
    {
        ILogger actualLogger = logger != null ? logger : new NullLogger();
        DownloadClientConfig config = new DownloadClientConfig();
        config.setServer(getDownloadServer());
        config.setLogger(actualLogger);
        config.setStore(new FileSystemDownloadStore(actualLogger, root)
            {
                @Override
                protected Path getItemDirectory(IUserSessionId userSessionId, DownloadSessionId downloadSessionId,
                        IDownloadItemId itemId) throws DownloadException
                {
                    String[] splitted = itemId.getId().split("/", 2);
                    String dataSetCode = splitted[0];
                    return root.resolve(dataSetCode);
                }

                @Override
                public Path getItemPath(IUserSessionId userSessionId, DownloadSessionId downloadSessionId, IDownloadItemId itemId)
                        throws DownloadException
                {
                    Path itemDirectory = getItemDirectory(userSessionId, downloadSessionId, itemId);
                    String[] splitted = itemId.getId().split("/", 2);
                    Path itemPath = itemDirectory.resolve(splitted[1]);
                    if (itemPath.toFile().exists())
                    {
                        return itemPath;
                    }
                    throw new DownloadItemNotFoundException("Store does not contain any files for download item id: " + itemId);
                }
            });
        config.setDeserializerProvider(new DefaultDeserializerProvider(actualLogger,
                new IDownloadItemIdDeserializer()
                    {
                        @Override
                        public IDownloadItemId deserialize(ByteBuffer buffer) throws DownloadException
                        {
                            return new DownloadItemId(new String(buffer.array(), buffer.position(), buffer.limit()));
                        }
                    }));
        config.setRetryProvider(retryProviderFactory != null ? retryProviderFactory.createRetryProvider(actualLogger)
                : new DefaultRetryProvider(actualLogger, 3, 1000, 2));
        return new DownloadClient(config);
    }

    private DownloadPreferences createPreferences(FastDownloadSessionOptions options)
    {
        return new DownloadPreferences(options.getWishedNumberOfStreams());
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("downLoadUrl", downLoadUrl).append("userSessionId", userSessionId).toString();
    }
}

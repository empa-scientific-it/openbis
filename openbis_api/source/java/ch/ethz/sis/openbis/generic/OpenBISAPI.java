package ch.ethz.sis.openbis.generic;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.dataset.create.UploadedDataSetCreation;
import ch.systemsx.cisd.common.http.JettyHttpClientFactory;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.http.HttpMethod;

public class OpenBISAPI {

    private static final Logger OPERATION_LOG = LogFactory.getLogger(LogCategory.OPERATION, OpenBISAPI.class);

    private static final int DEFAULT_TIMEOUT_IN_MILLIS = 30000; //30 seconds

    private static final int CHUNK_SIZE = 1048576; // 1 MiB

    private static final Collection<Integer> ACCEPTABLE_STATUSES = List.of(200, 502);

    private final IApplicationServerApi asFacade;

    private final IDataStoreServerApi dssFacade;

    private String sessionToken;

    private final int timeout;

    private final String asURL;

    private final String dssURL;

    public OpenBISAPI(final String asURL, final String dssURL)
    {
        this(asURL, dssURL, DEFAULT_TIMEOUT_IN_MILLIS);
    }

    public OpenBISAPI(final String asURL, final String dssURL, final int timeout)
    {
        this.timeout = timeout;
        this.asURL = asURL;
        asFacade = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, this.asURL + IApplicationServerApi.SERVICE_URL, timeout);
        this.dssURL = dssURL;
        dssFacade = HttpInvokerUtils.createServiceStub(IDataStoreServerApi.class, this.dssURL + IDataStoreServerApi.SERVICE_URL, timeout);
    }

    public String getSessionToken()
    {
        return sessionToken;
    }

    public void setSessionToken(final String sessionToken)
    {
        this.sessionToken = sessionToken;
    }

    public String login(String userId, String password) {
        String sessionToken = asFacade.login(userId, password);
        setSessionToken(sessionToken);
        return sessionToken;
    }

    public void logout() {
        asFacade.logout(sessionToken);
    }

    private List<File> contentOf(final File item)
    {
        if (item.getName().startsWith("."))
        {
            return Collections.emptyList();
        } else if (item.isFile())
        {
            return Collections.singletonList(item);
        } else
        {
            return Arrays.stream(Objects.requireNonNull(item.listFiles()))
                    .flatMap(file -> contentOf(file).stream())
                    .collect(Collectors.toList());
        }
    }
    @SuppressWarnings("resource")
    private Iterable<byte[]> streamFile(final File file, final int chunkSize) throws FileNotFoundException
    {
        final InputStream inputStream = new FileInputStream(file);

        return () -> new Iterator<>()
        {
            public boolean hasMore = true;

            public boolean hasNext()
            {
                return hasMore;
            }

            public byte[] next()
            {
                try
                {
                    byte[] bytes = inputStream.readNBytes(chunkSize);
                    if (bytes.length < chunkSize)
                    {
                        hasMore = false;
                        inputStream.close();
                    }
                    return bytes;
                } catch (final IOException e)
                {
                    try
                    {
                        inputStream.close();
                    } catch (final IOException ex)
                    {
                        throw new RuntimeException(ex);
                    }
                    throw new RuntimeException(e);
                }
            }
        };

    }

    /**
     * Upload file or folder to the DSS SessionWorkspaceFileUploadServlet and return the ID to be used by createUploadedDataSet
     * This method hides the complexities of uploading a folder with many files and does the uploads in chunks.
     */
    public String uploadFileWorkspaceDSS(final Path fileOrFolder)
    {
        //        final ServiceFinder serviceFinder = new ServiceFinder("openbis", "session_workspace_file_upload");
        //        serviceFinder.computeServerUrl()

        Objects.requireNonNull(sessionToken);

        final List<File> content = contentOf(fileOrFolder.toFile());
        if (content.isEmpty())
        {
            throw new RuntimeException("The directory " + fileOrFolder + " is empty");
        }

        final long totalSize = content.stream().reduce(0L, (size, f) ->
        {
            try
            {
                return size + Files.size(f.toPath());
            } catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }, Long::sum);

        final String uploadId = UUID.randomUUID().toString();
        final org.eclipse.jetty.client.HttpClient client = JettyHttpClientFactory.getHttpClient();
        int id = 1;
        try
        {
            for (final File file : content)
            {
                final String fileName = file.getName();
                final long fileSize = file.length();
                final String prefix = "(" + id + "/" + content.size() + ") " + fileName + ": ";
                final long size = Files.size(file.toPath());
                final long totalChunks = (size / CHUNK_SIZE) + (size % CHUNK_SIZE == 0 ? 0 : 1);

                OPERATION_LOG.info(prefix + "Starting upload of " + size + " bytes");

                long start = 0;
                for (var chunk : streamFile(file, CHUNK_SIZE))
                {
                    if (chunk.length == 0)
                    {
                        continue;
                    }
                    final long end = start + chunk.length;

                    int status = 0;
                    while (status != 200)
                    {
                        final ContentProvider contentProvider = new BytesContentProvider(chunk);

                        final Request httpRequest = client.newRequest(dssURL + "/session_workspace_file_upload")
                                .method(HttpMethod.POST);
                        httpRequest.param("sessionID", sessionToken);
                        httpRequest.param("id", Integer.toString(id++));
                        httpRequest.param("filename", fileName);
                        httpRequest.param("startByte", Long.toString(start));
                        httpRequest.param("endByte", Long.toString(end));
                        httpRequest.param("size", Long.toString(fileSize));
                        httpRequest.param("uploadID", uploadId);
                        httpRequest.content(contentProvider);
                        final ContentResponse response = httpRequest.send();

                        status = response.getStatus();
                        OPERATION_LOG.info(prefix + "Chunk " + (start / CHUNK_SIZE + 1) + "/" + totalChunks
                                + " uploaded with status " + status);

                        if (!ACCEPTABLE_STATUSES.contains(status))
                        {
                            throw new IOException(response.getContentAsString());
                        }
                    }
                    start += CHUNK_SIZE;
                }
                OPERATION_LOG.info(prefix + "Upload complete");
            }
        } catch (final IOException | TimeoutException | InterruptedException | ExecutionException e)
        {
            throw new RuntimeException(e);
        }

        return uploadId;
    }

    public DataSetPermId createUploadedDataSet(final UploadedDataSetCreation newDataSet)
    {
        return dssFacade.createUploadedDataSet(sessionToken, newDataSet);
    }

}

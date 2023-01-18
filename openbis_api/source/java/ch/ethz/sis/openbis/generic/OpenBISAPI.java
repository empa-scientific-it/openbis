package ch.ethz.sis.openbis.generic;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.dataset.create.UploadedDataSetCreation;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
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
import java.util.*;
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

    private static final Collection<Integer> ACCEPTABLE_STATUSES = List.of(200);

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

    public DataSetPermId createUploadedDataSet(final UploadedDataSetCreation newDataSet)
    {
        return dssFacade.createUploadedDataSet(sessionToken, newDataSet);
    }


    private Iterable<byte[]> streamFile(final File file, final int chunkSize) throws FileNotFoundException
    {
        final InputStream inputStream = new FileInputStream(file);

        return new Iterable<byte[]>() {
            @Override
            public Iterator<byte[]> iterator() {
                return new Iterator<>() {
                    public boolean hasMore = true;

                    public boolean hasNext() {
                        return hasMore;
                    }

                    public byte[] next() {
                        try {
                            byte[] bytes = inputStream.readNBytes(chunkSize);
                            if (bytes.length < chunkSize) {
                                hasMore = false;
                                inputStream.close();
                            }
                            return bytes;
                        } catch (final IOException e) {
                            try {
                                inputStream.close();
                            } catch (final IOException ex) {
                                throw new RuntimeException(ex);
                            }
                            throw new RuntimeException(e);
                        }
                    }
                };
            }
        };
    }

    private String uploadFileWorkspaceDSSEmptyDir(String pathToDir) {
        final org.eclipse.jetty.client.HttpClient client = JettyHttpClientFactory.getHttpClient();
        final Request httpRequest = client.newRequest(dssURL + "/session_workspace_file_upload")
                .method(HttpMethod.POST);
        httpRequest.param("sessionID", sessionToken);
        httpRequest.param("id", "1");
        httpRequest.param("filename", pathToDir);
        httpRequest.param("startByte", Long.toString(0));
        httpRequest.param("endByte", Long.toString(0));
        httpRequest.param("size", Long.toString(0));
        httpRequest.param("emptyFolder", Boolean.TRUE.toString());

        try {
            final ContentResponse response = httpRequest.send();
            final int status = response.getStatus();
            if (status != 200)
            {
                throw new IOException(response.getContentAsString());
            }
        } catch (final IOException | TimeoutException | InterruptedException | ExecutionException e)
        {
            throw new RuntimeException(e);
        }
        return pathToDir;
    }

    private String uploadFileWorkspaceDSSFile(String pathToFile, File file) {
        try {
            long start = 0;
            for (byte[] chunk : streamFile(file, CHUNK_SIZE)) {
                final long end = start + chunk.length;

                final org.eclipse.jetty.client.HttpClient client = JettyHttpClientFactory.getHttpClient();
                final Request httpRequest = client.newRequest(dssURL + "/session_workspace_file_upload")
                        .method(HttpMethod.POST);
                httpRequest.param("sessionID", sessionToken);
                httpRequest.param("id", "1");
                httpRequest.param("filename", pathToFile);
                httpRequest.param("startByte", Long.toString(start));
                httpRequest.param("endByte", Long.toString(end));
                httpRequest.param("size", Long.toString(file.length()));

                final ContentResponse response = httpRequest.send();
                final int status = response.getStatus();
                if (status != 200) {
                    throw new IOException(response.getContentAsString());
                }
            }
        } catch (final IOException | TimeoutException | InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        return pathToFile;
    }

    /**
     * Upload file or folder to the DSS SessionWorkspaceFileUploadServlet and return the ID to be used by createUploadedDataSet
     * This method hides the complexities of uploading a folder with many files and does the uploads in chunks.
     */
    public String uploadFileWorkspaceDSS(final File fileOrFolder, final String... parents)
    {
        System.out.println("fileOrFolder: " + fileOrFolder);

        if (fileOrFolder.exists() == false)
        {
            throw new UserFailureException("Path doesn't exist: " + fileOrFolder);
        }
        String fileNameOrFolderName = "";
        if (parents.length == 1)
        {
            fileNameOrFolderName = parents[0] + "/";
        }
        fileNameOrFolderName += fileOrFolder.getName();

        if (fileOrFolder.isDirectory())
        {
            uploadFileWorkspaceDSSEmptyDir(fileNameOrFolderName);
            for (File file:fileOrFolder.listFiles())
            {
                uploadFileWorkspaceDSS(file, fileNameOrFolderName);
            }
        } else {
            uploadFileWorkspaceDSSFile(fileNameOrFolderName, fileOrFolder);
        }
        return fileNameOrFolderName;
    }

}

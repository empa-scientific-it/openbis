package ch.ethz.sis.openbis.generic;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.dataset.create.UploadedDataSetCreation;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;

import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;

public class OpenBISAPI {

    private static final int DEFAULT_TIMEOUT_IN_MILLIS = 30000; //30 seconds

    private static final int CHUNK_SIZE = 1048576;

    private final IApplicationServerApi asFacade;

    private final IDataStoreServerApi dssFacade;

    private String sessionToken;

    private final int timeout;

    private final String asURL;

    private final String dssURL;

    public OpenBISAPI(final String asURL, final String dssURL) {
        this(asURL, dssURL, DEFAULT_TIMEOUT_IN_MILLIS);
    }

    public OpenBISAPI(final String asURL, final String dssURL, final int timeout) {
        this.timeout = timeout;
        this.asURL = asURL;
        asFacade = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, this.asURL, timeout);
        this.dssURL = dssURL;
        dssFacade = HttpInvokerUtils.createServiceStub(IDataStoreServerApi.class, this.dssURL, timeout);
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(final String sessionToken) {
        this.sessionToken = sessionToken;
    }

    /**
     * Upload file or folder to the DSS SessionWorkspaceFileUploadServlet and return the ID to be used by createUploadedDataSet
     * This method hides the complexities of uploading a folder with many files and does the uploads in chunks.
     */
    public String uploadFileWorkspaceDSS(final Path fileOrFolder) {
        //        final ServiceFinder serviceFinder = new ServiceFinder("openbis", "session_workspace_file_upload");
        //        serviceFinder.computeServerUrl()

        Objects.requireNonNull(sessionToken);

        try
        {
            final File file = fileOrFolder.toFile();

            if (!file.isFile())
            {
                throw new UserFailureException("File must be a file for now.");
            }

            final String fileName = file.getName();

            final String response = request("POST", new URI(dssURL + "/session_workspace_file_upload"),
                    Map.of(
                            "sessionID", sessionToken,
                            "filename", fileName,
                            "id", "0",
                            "startByte", "0",
                            "endByte", String.valueOf(file.length()),
                            "size", String.valueOf(file.length())
                    ), Files.readAllBytes(file.toPath()));
            System.out.println(response);

            @SuppressWarnings("unchecked")
            final Map<String, String> values = new ObjectMapper().readValue(response, Map.class);

            return values.get("uploadID");
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public DataSetPermId createUploadedDataSet(final UploadedDataSetCreation newDataSet) {
        return dssFacade.createUploadedDataSet(sessionToken, newDataSet);
    }

    @SuppressWarnings({ "OptionalGetWithoutIsPresent", "unchecked" })
    private String request(final String httpMethod, final URI serverUri,
            final Map<String, String> parameters, final byte [] body) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofMillis(timeout))
                .build();

        final String query = parameters.entrySet().stream()
                .map(entry -> urlEncode(entry.getKey()) + "=" + urlEncode(entry.getValue()))
                .reduce((s1, s2) -> s1 + "&" + s2).get();

        final URI uri = new URI(serverUri.getScheme(), null, serverUri.getHost(), serverUri.getPort(),
                serverUri.getPath(), query, null);

        final HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(uri)
                .version(HttpClient.Version.HTTP_1_1)
                .timeout(Duration.ofMillis(timeout))
                .method(httpMethod, HttpRequest.BodyPublishers.ofByteArray(body));

        final HttpRequest request = builder.build();

        final HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());

        final int statusCode = httpResponse.statusCode();
        if (statusCode >= 200 && statusCode < 300) {
            return httpResponse.body();
        } else if (statusCode >= 400 && statusCode < 500) {
            throw new UserFailureException("User failure. Received status code: " + statusCode + ". Body: " +
                    new String(httpResponse.body()));
        } else if (statusCode >= 500 && statusCode < 600) {
            throw new RuntimeException("Server failure. Received status code: " + statusCode);
        } else {
            throw new RuntimeException("Unknown failure. Received status code: " + statusCode);
        }
    }

    private static String urlEncode(final String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

}

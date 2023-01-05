package ch.ethz.sis.openbis.generic.server.asapi.v3;

import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.dataset.create.UploadedDataSetCreation;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

public class OpenBisFacade
{

    public static final int TIMEOUT_IN_MILLIS = 30000;

    public static String uploadFileWorkspaceDss(final File[] filesOrFolders)
    {
//        final ServiceFinder serviceFinder = new ServiceFinder("openbis", "session_workspace_file_upload");
//        serviceFinder.computeServerUrl()
        final IApplicationServerApi v3 = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class,
                "http://localhost:8888/openbis/openbis" + IApplicationServerApi.SERVICE_URL, 10000);

        final String sessionToken = v3.login("admin", "cahngeit");
        System.out.println("Session token: " + sessionToken);

        try
        {
            final File file = filesOrFolders[0];

            if (!file.isFile())
            {
                throw new UserFailureException("File must me a file for now.");
            }

            final String fileName = file.getName();

            final byte[] respose = request("POST", new URI("http://localhost:8889/datastore_server/session_workspace_file_upload"),
                    Map.of(
                            "sessionID", sessionToken,
                            "filename", fileName,
                            "id", "0",
                            "startByte", "0",
                            "endByte", String.valueOf(file.length()),
                            "size", String.valueOf(file.length())
                            ), Files.readAllBytes(file.toPath()));
            System.out.println(new String(respose));
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        } finally
        {
            v3.logout(sessionToken);
        }

        // TODO: implement.
        return null;
    }

    public static DataSetPermId createUploadedDataSet(final String sessionToken, final UploadedDataSetCreation creation)
    {
        // TODO: implement.
        return null;
    }

    @SuppressWarnings({ "OptionalGetWithoutIsPresent", "unchecked" })
    private static byte[] request(final String httpMethod, final URI serverUri,
            final Map<String, String> parameters, final byte [] body) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofMillis(TIMEOUT_IN_MILLIS))
                .build();

        final String query = parameters.entrySet().stream()
                .map(entry -> urlEncode(entry.getKey()) + "=" + urlEncode(entry.getValue()))
                .reduce((s1, s2) -> s1 + "&" + s2).get();

        final URI uri = new URI(serverUri.getScheme(), null, serverUri.getHost(), serverUri.getPort(),
                serverUri.getPath(), query, null);

        final HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(uri)
                .version(HttpClient.Version.HTTP_1_1)
                .timeout(Duration.ofMillis(TIMEOUT_IN_MILLIS))
                .method(httpMethod, HttpRequest.BodyPublishers.ofByteArray(body));

        final HttpRequest request = builder.build();

        final HttpResponse<byte[]> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

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

    public static void main(String[] args)
    {
        final File file = new File("/home/viktor/Work/Projects/ETH/openbis/settings.gradle");
        uploadFileWorkspaceDss(new File[]{file});
    }

}

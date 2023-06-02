package ch.ethz.sis.afsclient.client;

import ch.ethz.sis.afsapi.api.PublicAPI;
import ch.ethz.sis.afsapi.dto.ApiResponse;
import ch.ethz.sis.afsapi.dto.File;
import ch.ethz.sis.afsclient.client.exception.ClientExceptions;
import ch.ethz.sis.afsjson.JsonObjectMapper;
import ch.ethz.sis.afsjson.jackson.JacksonObjectMapper;
import lombok.NonNull;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public final class AfsClientV2 implements PublicAPI
{

    private static final int DEFAULT_PACKAGE_SIZE_IN_BYTES = 1024;

    private static final int DEFAULT_TIMEOUT_IN_MILLIS = 30000;

    private final int maxReadSizeInBytes;

    private final int timeout;

    private String sessionToken;

    private String interactiveSessionKey;

    private String transactionManagerKey;

    private final URI serverUri;

    private final JsonObjectMapper jsonObjectMapper;

    public AfsClientV2(final URI serverUri)
    {
        this(serverUri, DEFAULT_PACKAGE_SIZE_IN_BYTES, DEFAULT_TIMEOUT_IN_MILLIS);
    }

    public AfsClientV2(final URI serverUri, final int maxReadSizeInBytes, final int timeout)
    {
        this.maxReadSizeInBytes = maxReadSizeInBytes;
        this.timeout = timeout;
        this.serverUri = serverUri;
        this.jsonObjectMapper = new JacksonObjectMapper();
    }

    public URI getServerUri()
    {
        return serverUri;
    }

    public int getMaxReadSizeInBytes()
    {
        return maxReadSizeInBytes;
    }

    public String getSessionToken()
    {
        return sessionToken;
    }

    public void setSessionToken(final String sessionToken)
    {
        this.sessionToken = sessionToken;
    }

    public String getInteractiveSessionKey()
    {
        return interactiveSessionKey;
    }

    public void setInteractiveSessionKey(String interactiveSessionKey)
    {
        this.interactiveSessionKey = interactiveSessionKey;
    }

    public String getTransactionManagerKey()
    {
        return transactionManagerKey;
    }

    public void setTransactionManagerKey(String transactionManagerKey)
    {
        this.transactionManagerKey = transactionManagerKey;
    }

    private static String urlEncode(final String s)
    {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    @Override
    public @NonNull String login(@NonNull final String userId, @NonNull final String password)
            throws Exception
    {
        String result = request("POST",
                "login", String.class, Map.of("userId", userId, "password", password));
        setSessionToken(result);
        return result;
    }

    @Override
    public @NonNull Boolean isSessionValid() throws Exception
    {
        validateSessionToken();
        return request("GET", "isSessionValid", Boolean.class,Map.of());
    }

    @Override
    public @NonNull Boolean logout() throws Exception
    {
        validateSessionToken();
        Boolean result = request("POST", "logout", Boolean.class, Map.of());
        setSessionToken(null);
        return result;
    }

    @Override
    public @NonNull List<File> list(@NonNull final String owner, @NonNull final String source,
            @NonNull final Boolean recursively) throws Exception
    {
        validateSessionToken();
        return request("GET", "list", List.class,
                Map.of("owner", owner, "source", source, "recursively",
                        recursively.toString()));
    }

    @Override
    public @NonNull byte[] read(@NonNull final String owner, @NonNull final String source,
            @NonNull final Long offset, @NonNull final Integer limit) throws Exception
    {
        validateSessionToken();
        return request("GET", "read", byte[].class,
                Map.of("owner", owner, "source", source, "offset",
                        offset.toString(), "limit", limit.toString()));
    }

    @Override
    public @NonNull Boolean write(@NonNull final String owner, @NonNull final String source,
            @NonNull final Long offset, final byte @NonNull [] data,
            final byte @NonNull [] md5Hash) throws Exception
    {
        validateSessionToken();
        return request("POST", "write", Boolean.class, Map.of("owner", owner, "source", source,
                "offset", offset.toString(), "md5Hash", getMd5HexString(md5Hash)), data);
    }

    @Override
    public @NonNull Boolean delete(@NonNull final String owner, @NonNull final String source)
            throws Exception
    {
        validateSessionToken();
        return request("DELETE", "delete", Boolean.class, Map.of("owner", owner, "source", source));
    }

    @Override
    public @NonNull Boolean copy(@NonNull final String sourceOwner, @NonNull final String source,
            @NonNull final String targetOwner,
            @NonNull final String target)
            throws Exception
    {
        validateSessionToken();
        return request("POST", "copy", Boolean.class, Map.of("sourceOwner", sourceOwner, "source", source,
                "targetOwner", targetOwner, "target", target));
    }

    @Override
    public @NonNull Boolean move(@NonNull final String sourceOwner, @NonNull final String source,
            @NonNull final String targetOwner,
            @NonNull final String target)
            throws Exception
    {
        validateSessionToken();
        return request("POST", "move", Boolean.class, Map.of("sourceOwner", sourceOwner, "source", source,
                "targetOwner", targetOwner, "target", target));
    }

    @Override
    public void begin(final UUID transactionId) throws Exception
    {
        validateSessionToken();
        request("POST", "begin", null, Map.of("transactionId", transactionId.toString()));
    }

    @Override
    public Boolean prepare() throws Exception
    {
        validateSessionToken();
        return request("POST", "prepare", Boolean.class, Map.of());
    }

    @Override
    public void commit() throws Exception
    {
        validateSessionToken();
        request("POST", "commit", null, Map.of());
    }

    @Override
    public void rollback() throws Exception
    {
        validateSessionToken();
        request("POST", "rollback", null, Map.of());
    }

    @Override
    public List<UUID> recover() throws Exception
    {
        validateSessionToken();
        return request("POST", "recover", List.class, Map.of());
    }

    private <T> T request(@NonNull final String httpMethod, @NonNull final String apiMethod, Class<T> responseType,
            @NonNull final Map<String, String> parameters) throws Exception
    {
        return request(httpMethod, apiMethod, responseType, parameters, new byte[0]);
    }

    @SuppressWarnings({ "OptionalGetWithoutIsPresent", "unchecked" })
    private <T> T request(@NonNull final String httpMethod, @NonNull final String apiMethod, Class<T> responseType,
            @NonNull final Map<String, String> params, final byte @NonNull [] body)
            throws Exception
    {
        HttpClient.Builder clientBuilder = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofMillis(timeout));

        HttpClient client = clientBuilder.build();

        if (sessionToken != null)
        {
            params.put("sessionToken", sessionToken);
        }

        if(interactiveSessionKey != null)
        {
            params.put("interactiveSessionKey", interactiveSessionKey);
        }

        if (transactionManagerKey != null)
        {
            params.put("transactionManagerKey", transactionManagerKey);
        }

        final String query = Stream.concat(
                        Stream.of(new AbstractMap.SimpleImmutableEntry<>("method", apiMethod)),
                        params.entrySet().stream())
                .map(entry-> {
                    return urlEncode(entry.getKey()) + "=" + urlEncode(entry.getValue());
                })
                .reduce((s1, s2) -> s1 + "&" + s2).get();

        final URI uri =
                new URI(serverUri.getScheme(), null, serverUri.getHost(), serverUri.getPort(),
                        serverUri.getPath(), query, null);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(uri)
                .version(HttpClient.Version.HTTP_1_1)
                .timeout(Duration.ofMillis(timeout))
                .method(httpMethod, HttpRequest.BodyPublishers.ofByteArray(body));

        final HttpRequest request = builder.build();

        final HttpResponse<byte[]> httpResponse =
                client.send(request, HttpResponse.BodyHandlers.ofByteArray());

        final int statusCode = httpResponse.statusCode();
        if (statusCode >= 200 && statusCode < 300)
        {
            if (!httpResponse.headers().map().containsKey("content-type"))
            {
                throw new IllegalArgumentException(
                        "Server error HTTP response. Missing content-type");
            }
            String content = httpResponse.headers().map().get("content-type").get(0);

            switch (content)
            {
                case "text/plain":
                    return parseFormDataResponse(responseType, httpResponse);
                case "application/json":
                    return parseJsonResponse(httpResponse);
                case "application/octet-stream":
                    return (T) httpResponse.body();
                default:
                    throw new IllegalArgumentException(
                            "Client error HTTP response. Unsupported content-type received.");
            }
        } else if (statusCode >= 400 && statusCode < 500)
        {
            // jsonObjectMapper can't deserialize immutable lists sent in the error message.
            String res = new String(httpResponse.body(), StandardCharsets.UTF_8);
            throw ClientExceptions.API_ERROR.getInstance(res);
        } else if (statusCode >= 500 && statusCode < 600)
        {
            throw ClientExceptions.SERVER_ERROR.getInstance(statusCode);
        } else
        {
            throw ClientExceptions.OTHER_ERROR.getInstance(statusCode);
        }
    }

    private <T> T parseFormDataResponse(Class<T> responseType, HttpResponse<byte[]> httpResponse)
    {
        if (responseType == null) {
            return null;
        } else if (responseType == String.class) {
            return responseType.cast(new String(httpResponse.body()));
        } else if (responseType == Boolean.class) {
            return  responseType.cast(Boolean.parseBoolean(new String(httpResponse.body())));
        }

        throw new IllegalStateException("Unreachable statement!");
    }

    private <T> T parseJsonResponse(final HttpResponse<byte[]> httpResponse) throws Exception
    {
        final ApiResponse response =
                jsonObjectMapper.readValue(new ByteArrayInputStream(httpResponse.body()),
                        ApiResponse.class);

        if (response.getError() != null)
        {
            throw ClientExceptions.API_ERROR.getInstance(response.getError());
        } else
        {
            return (T) response.getResult();
        }
    }

    private void validateSessionToken()
    {
        if (getSessionToken() == null)
        {
            throw new IllegalStateException("No session information detected!");
        }
    }

    private String getMd5HexString(byte[] md5) {
        BigInteger no = new BigInteger(1, md5);
        String hashtext = no.toString(16);
        while (hashtext.length() < 32) {
            hashtext = "0" + hashtext;
        }
        return hashtext;
    }
}

package ch.ethz.sis.afsclient.client;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import ch.ethz.sis.afsapi.api.ClientAPI;
import ch.ethz.sis.afsapi.api.PublicAPI;
import ch.ethz.sis.afsapi.dto.ApiResponse;
import ch.ethz.sis.afsapi.dto.File;
import ch.ethz.sis.afsclient.client.exception.ClientExceptions;
import ch.ethz.sis.afsjson.JsonObjectMapper;
import ch.ethz.sis.afsjson.jackson.JacksonObjectMapper;
import lombok.NonNull;

public final class AfsClient implements PublicAPI, ClientAPI
{

    private static final int DEFAULT_PACKAGE_SIZE_IN_BYTES = 1024;

    private static final int DEFAULT_TIMEOUT_IN_MILLIS = 30000;

    private static final String MD5 = "MD5";

    private final int maxReadSizeInBytes;

    private final int timeout;

    private String sessionToken;

    private String interactiveSessionKey;

    private String transactionManagerKey;

    private final URI serverUri;

    private static final JsonObjectMapper jsonObjectMapper = new JacksonObjectMapper();

    public AfsClient(final URI serverUri)
    {
        this(serverUri, DEFAULT_PACKAGE_SIZE_IN_BYTES, DEFAULT_TIMEOUT_IN_MILLIS);
    }

    public AfsClient(final URI serverUri, final int maxReadSizeInBytes, final int timeout)
    {
        this.maxReadSizeInBytes = maxReadSizeInBytes;
        this.timeout = timeout;
        this.serverUri = serverUri;
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
        return request("GET", "isSessionValid", Boolean.class, Map.of());
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
            @NonNull final Long offset, @NonNull final byte[] data,
            @NonNull final byte[] md5Hash) throws Exception
    {
        validateSessionToken();
        return request("POST", "write", Boolean.class, Map.of("owner", owner, "source", source,
                "offset", offset.toString(), "data", Base64.getEncoder().encodeToString(data),
                "md5Hash", Base64.getEncoder().encodeToString(md5Hash)));
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
        return request("POST", "copy", Boolean.class,
                Map.of("sourceOwner", sourceOwner, "source", source,
                        "targetOwner", targetOwner, "target", target));
    }

    @Override
    public @NonNull Boolean move(@NonNull final String sourceOwner, @NonNull final String source,
            @NonNull final String targetOwner,
            @NonNull final String target)
            throws Exception
    {
        validateSessionToken();
        return request("POST", "move", Boolean.class,
                Map.of("sourceOwner", sourceOwner, "source", source,
                        "targetOwner", targetOwner, "target", target));
    }

    @Override
    public @NonNull Boolean create(@NonNull final String owner, @NonNull final String source, @NonNull final Boolean directory)
            throws Exception
    {
        validateSessionToken();
        return request("POST", "create", Boolean.class, Map.of("owner", owner, "source", source, "directory", String.valueOf(directory)));
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


    public String getName(String path) {
        int indexOf = path.lastIndexOf('/');
        if(indexOf == -1) {
            return path;
        } else {
            return path.substring(indexOf + 1);
        }
    }

    @Override
    public void resumeRead(@NonNull String owner, @NonNull String source, @NonNull Path destination,
            @NonNull Long offset) throws Exception
    {

        final List<File> infos = list(owner, source, false);
        if (infos.isEmpty())
        {
            throw ClientExceptions.API_ERROR.getInstance("File not found '" + source + "'");
        }
        File sourceFile = null;
        for (File info : infos)
        {
            if (info.getName().equals(getName(source)))
            {
                sourceFile = info;
                break;
            }
        }
        if (sourceFile == null)
        {
            throw ClientExceptions.API_ERROR.getInstance("File not found '" + source + "'");
        }

        final Long sourceFileSize = sourceFile.getSize();
        final CountDownLatch latch = new CountDownLatch((int) ((sourceFileSize - 1) / DEFAULT_PACKAGE_SIZE_IN_BYTES) + 1);
        final AtomicBoolean hasError = new AtomicBoolean(false);

        try (final AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(destination, StandardOpenOption.CREATE,
                StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING))
        {
            while (offset < sourceFileSize)
            {
                final long remainingSize = sourceFileSize - offset;
                final int limit = DEFAULT_PACKAGE_SIZE_IN_BYTES <= remainingSize ? DEFAULT_PACKAGE_SIZE_IN_BYTES : (int) remainingSize;
                final byte[] bufferArray = read(owner, source, offset, limit);
                final ByteBuffer byteBuffer = ByteBuffer.wrap(bufferArray);

                fileChannel.write(byteBuffer, offset, byteBuffer, new ChannelWriteCompletionHandler(latch, hasError));
                offset += bufferArray.length;
        }

            latch.await();

            if (hasError.get())
            {
                throw new Exception("Failed to write all chunks");
            }
        } catch (final Exception e)
        {
            throw ClientExceptions.API_ERROR.getInstance("Error writing to file from source '" + source + "'");
        }
    }

    @Override
    public @NonNull Boolean resumeWrite(@NonNull final String owner, @NonNull final String destination,
            @NonNull final Path source, @NonNull Long offset) throws Exception
    {
        final java.io.File sourceFile = source.toFile();
        if (!sourceFile.exists())
        {
            throw ClientExceptions.API_ERROR.getInstance("File not found '" + source + "'");
        }

        final long sourceFileSize = sourceFile.length();

        try (final AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(source, StandardOpenOption.READ))
        {
            final long remainingFileSize = sourceFileSize - offset;
            final AtomicBoolean hasError = new AtomicBoolean(false);

            if (remainingFileSize > 0)
            {
                final CountDownLatch latch = new CountDownLatch((int) ((remainingFileSize - 1) / DEFAULT_PACKAGE_SIZE_IN_BYTES) + 1);

                while (offset < sourceFileSize)
                {
                    final ByteBuffer byteBuffer = ByteBuffer.allocate(DEFAULT_PACKAGE_SIZE_IN_BYTES);
                    fileChannel.read(byteBuffer, offset, byteBuffer,
                            new ChannelReadCompletionHandler(owner, destination, offset, latch, hasError));
                    offset += DEFAULT_PACKAGE_SIZE_IN_BYTES;
                }

                latch.await();
            }

            return !hasError.get();
        } catch (final Exception e)
        {
            return false;
        }
    }

    private static byte[] getMD5(final byte[] data)
    {
        try
        {
            return MessageDigest.getInstance(MD5).digest(data);
        } catch (Exception exception)
        {
            throw new RuntimeException(exception);
        }
    }

    @SuppressWarnings({ "OptionalGetWithoutIsPresent", "unchecked" })
    private <T> T request(@NonNull final String httpMethod, @NonNull final String apiMethod,
            Class<T> responseType,
            @NonNull Map<String, String> params)
            throws Exception
    {
        //
        // General Parameter Handling
        //

        HashMap<String, String> mutableParams = new HashMap<>(params);
        params = mutableParams;

        if (sessionToken != null)
        {
            params.put("sessionToken", sessionToken);
        }

        if (interactiveSessionKey != null)
        {
            params.put("interactiveSessionKey", interactiveSessionKey);
        }

        if (transactionManagerKey != null)
        {
            params.put("transactionManagerKey", transactionManagerKey);
        }

        String parameters = Stream.concat(
                        Stream.of(new AbstractMap.SimpleImmutableEntry<>("method", apiMethod)),
                        params.entrySet().stream())
                .map(entry -> urlEncode(entry.getKey()) + "=" + urlEncode(entry.getValue()))
                .reduce((s1, s2) -> s1 + "&" + s2).get();

        //
        // GET Request - Parameters on the query string
        //

        String queryParameters = null;
        if (httpMethod.equals("GET"))
        {
            queryParameters = parameters;
        }

        //
        // POST and DELETE Request - Parameters on body
        //

        byte[] body = null;
        if (httpMethod.equals("POST") || httpMethod.equals("DELETE"))
        {
            body = parameters.getBytes(StandardCharsets.UTF_8);
        } else
        {
            body = new byte[0];
        }

        //
        // HTTP Client
        //
        final URI uri =
                new URI(serverUri.getScheme(), null, serverUri.getHost(), serverUri.getPort(),
                        serverUri.getPath(), queryParameters, null);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(uri)
                .version(HttpClient.Version.HTTP_1_1)
                .timeout(Duration.ofMillis(timeout))
                .method(httpMethod, HttpRequest.BodyPublishers.ofByteArray(body));

        final HttpRequest request = builder.build();

        HttpClient.Builder clientBuilder = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofMillis(timeout));

        HttpClient client = clientBuilder.build();

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
            String contentType = httpResponse.headers().map().get("content-type").get(0);
            byte[] responseBody = httpResponse.body();

            return getResponseResult(responseType, contentType, responseBody);
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

    public static <T> T getResponseResult(Class<T> responseType, String contentType,
            byte[] responseBody)
            throws Exception
    {
        switch (contentType)
        {
            case "text/plain":
                return AfsClient.parseFormDataResponse(responseType, responseBody);
            case "application/json":
                return AfsClient.parseJsonResponse(responseBody);
            case "application/octet-stream":
                return (T) responseBody;
            default:
                throw new IllegalArgumentException(
                        "Client error HTTP response. Unsupported content-type received.");
        }
    }

    private static <T> T parseFormDataResponse(Class<T> responseType, byte[] responseBody)
    {
        if (responseType == null)
        {
            return null;
        } else if (responseType == String.class)
        {
            return responseType.cast(new String(responseBody, StandardCharsets.UTF_8));
        } else if (responseType == Boolean.class)
        {
            return responseType.cast(
                    Boolean.parseBoolean(new String(responseBody, StandardCharsets.UTF_8)));
        }

        throw new IllegalStateException("Unreachable statement!");
    }

    private static <T> T parseJsonResponse(byte[] responseBody) throws Exception
    {
        final ApiResponse response =
                jsonObjectMapper.readValue(new ByteArrayInputStream(responseBody),
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

    private static class ChannelWriteCompletionHandler implements CompletionHandler<Integer, ByteBuffer>
    {

        private final CountDownLatch latch;

        private final AtomicBoolean hasError;

        public ChannelWriteCompletionHandler(final CountDownLatch latch, final AtomicBoolean hasError)
        {
            this.latch = latch;
            this.hasError = hasError;
        }

        @Override
        public void completed(final Integer result, final ByteBuffer attachment)
        {
            latch.countDown();
        }

        @Override
        public void failed(final Throwable exc, final ByteBuffer attachment)
        {
            hasError.set(true);
            latch.countDown();
        }

    }

    private class ChannelReadCompletionHandler implements CompletionHandler<Integer, ByteBuffer>
    {

        private final @NonNull String owner;

        private final @NonNull String destination;

        private final CountDownLatch latch;

        private final AtomicBoolean hasError;

        private final Long offset;

        public ChannelReadCompletionHandler(final @NonNull String owner, final @NonNull String destination, final Long offset,
                final CountDownLatch latch, final AtomicBoolean hasError)
        {
            this.owner = owner;
            this.destination = destination;
            this.offset = offset;
            this.latch = latch;
            this.hasError = hasError;
        }

        @Override
        public void completed(final Integer result, final ByteBuffer attachment)
        {
            final byte[] fullBuffer = attachment.array();
            final byte[] data = result < fullBuffer.length ? Arrays.copyOf(fullBuffer, result) : fullBuffer;
            try
            {
                final Boolean writeSuccessful = write(owner, destination, this.offset, data, getMD5(data));
                if (!writeSuccessful)
                {
                    hasError.set(true);
                }
            } catch (final Exception e)
            {
                hasError.set(true);
            } finally
            {
                latch.countDown();
            }
        }

        @Override
        public void failed(final Throwable exc, final ByteBuffer attachment)
        {
            hasError.set(true);
            latch.countDown();
        }

    }

}

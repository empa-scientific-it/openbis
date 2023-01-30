package ch.ethz.sis.afsserver.http;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class HttpResponse {
    public static final String CONTENT_TYPE_BINARY_DATA = "application/octet-stream";
    public static final String CONTENT_TYPE_JSON = "application/json";

    private final boolean error;
    private final String contentType;
    private final byte[] body;
}

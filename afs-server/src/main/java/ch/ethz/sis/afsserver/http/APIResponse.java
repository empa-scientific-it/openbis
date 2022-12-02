package ch.ethz.sis.afsserver.http;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class APIResponse {

    public static String CONTENT_TYPE_BINARY_DATA = "application/octet-stream";
    public static String CONTENT_TYPE_JSON = "application/json";

    private boolean isOk;
    private String contentType;
    private byte[] body;
}

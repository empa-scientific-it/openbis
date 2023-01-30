package ch.ethz.sis.afsserver.server.impl;

import ch.ethz.sis.afsserver.server.Response;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class ApiResponse implements Response {
    private final String id;
    private final Object result;
    private final Object error;
}

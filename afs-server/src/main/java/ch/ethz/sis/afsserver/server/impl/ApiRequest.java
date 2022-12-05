package ch.ethz.sis.afsserver.server.impl;

import ch.ethz.sis.afsserver.server.Request;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class ApiRequest implements Request {
    private final String id;
    private final String method;
    private final Map<String, Object> params;
    private final String sessionToken;
    private final String interactiveSessionKey;
    private final String transactionManagerKey;
}

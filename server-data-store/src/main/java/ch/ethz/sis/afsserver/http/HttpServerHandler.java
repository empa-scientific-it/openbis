package ch.ethz.sis.afsserver.http;

import io.netty.handler.codec.http.HttpMethod;

import java.util.List;
import java.util.Map;

public interface HttpServerHandler {
    public HttpResponse process(HttpMethod method, Map<String, List<String>> uriParameters, byte[] requestBody);
}

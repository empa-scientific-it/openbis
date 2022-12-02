package ch.ethz.sis.afsserver.http;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface HttpServerHandler {
    public APIResponse process(InputStream requestBody, Map<String, List<String>> parameters);
}

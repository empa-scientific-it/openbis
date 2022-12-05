package ch.ethz.sis.afsserver.http;

import ch.ethz.sis.afsserver.server.Response;
import ch.ethz.sis.afsserver.server.ResponseBuilder;

public class HttpResponseBuilder implements ResponseBuilder {
    @Override
    public Response build(String id, Object result) {
        if (result instanceof byte[]) {
            return new ApiResponse(id, null, null);
        } else {
            return new ApiResponse(id, result, null);
        }
    }
}

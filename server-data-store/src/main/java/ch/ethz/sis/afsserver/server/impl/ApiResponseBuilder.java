package ch.ethz.sis.afsserver.server.impl;

import ch.ethz.sis.afsserver.server.Response;
import ch.ethz.sis.afsserver.server.ResponseBuilder;

public class ApiResponseBuilder implements ResponseBuilder {
    @Override
    public Response build(String id, Object result) {
        return new ApiResponse(id, result, null);
    }
}

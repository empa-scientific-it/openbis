package ch.ethz.sis.afsserver.server.impl;

import ch.ethz.sis.afsserver.exception.HTTPExceptions;
import ch.ethz.sis.afsserver.http.*;
import ch.ethz.sis.afsserver.server.*;
import ch.ethz.sis.afsserver.server.performance.Event;
import ch.ethz.sis.afsserver.server.performance.PerformanceAuditor;
import ch.ethz.sis.shared.json.JSONObjectMapper;
import ch.ethz.sis.shared.log.LogManager;
import ch.ethz.sis.shared.log.Logger;
import io.netty.handler.codec.http.HttpMethod;

import java.util.*;

/*
 * This class is supposed to be called by a TCP or HTTP transport class
 */
public class ApiServerAdapter<CONNECTION, API> implements HttpServerHandler {

    private static final Logger logger = LogManager.getLogger(ApiServerAdapter.class);

    private final APIServer<CONNECTION, Request, Response, API> server;
    private final JSONObjectMapper jsonObjectMapper;
    private final ApiResponseBuilder apiResponseBuilder;


    public ApiServerAdapter(
            APIServer<CONNECTION, Request, Response, API> server,
            JSONObjectMapper jsonObjectMapper) {
        this.server = server;
        this.jsonObjectMapper = jsonObjectMapper;
        this.apiResponseBuilder = new ApiResponseBuilder();
    }

    public HttpResponse process(HttpMethod httpMethod, Map<String, List<String>> uriParameters, byte[] requestBody) {
        try {
            logger.traceAccess(null);
            PerformanceAuditor performanceAuditor = new PerformanceAuditor();

            String method = null;
            String sessionToken = null;
            String interactiveSessionKey = null;
            String transactionManagerKey = null;
            Map<String, Object> methodParameters = new HashMap<>();
            for (Map.Entry<String, List<String>> entry:uriParameters.entrySet()) {
                Object value = null;
                if (entry.getValue() != null) {
                    if (entry.getValue().size() == 1) {
                        value = entry.getValue().get(0);
                    } else if (entry.getValue().size() > 1) {
                        return getHTTPResponse(new ApiResponse("1", null, HTTPExceptions.INVALID_PARAMETERS.getCause()));
                    }
                }

                switch (entry.getKey()) {
                    case "method":
                        method = (String) value;
                        break;
                    case "sessionToken":
                        sessionToken = (String) value;
                        break;
                    case "interactiveSessionKey":
                        interactiveSessionKey = (String) value;
                        break;
                    case "transactionManagerKey":
                        transactionManagerKey = (String) value;
                        break;
                    default:
                        methodParameters.put(entry.getKey(), value);
                        break;
                }
            }

            ApiRequest apiRequest = new ApiRequest("1", method, methodParameters, sessionToken, interactiveSessionKey, transactionManagerKey);
            Response response = server.processOperation(apiRequest, apiResponseBuilder, performanceAuditor);
            HttpResponse httpResponse = getHTTPResponse(response);
            performanceAuditor.audit(Event.WriteResponse);
            logger.traceExit(performanceAuditor);
            logger.traceExit(httpResponse);
            return httpResponse;
        } catch (APIServerException e) {
            logger.catching(e);
            switch (e.getType()) {
                case MethodNotFound:
                case IncorrectParameters:
                case InternalError:
                    try {
                        return getHTTPResponse(new ApiResponse("1", null, e.getData()));
                    } catch (Exception ex) {
                        logger.catching(ex);
                    }
            }
        } catch (Exception e) {
            logger.catching(e);
            try {
                return getHTTPResponse(new ApiResponse("1", null, HTTPExceptions.UNKNOWN.getCause(e.getClass().getSimpleName(), e.getMessage())));
            } catch (Exception ex) {
                logger.catching(ex);
            }
        }
        return null; // This should never happen, it would mean an error writing the Unknown error happened.
    }

    private HttpResponse getHTTPResponse(Response response) throws Exception {
        boolean error = response.getError() != null;
        String contentType = null;
        byte[] body = null;
        if (response.getResult() instanceof byte[]) {
            contentType = HttpResponse.CONTENT_TYPE_BINARY_DATA;
            body = (byte[]) response.getResult();
        } else {
            contentType = HttpResponse.CONTENT_TYPE_JSON;
            body = jsonObjectMapper.writeValue(response);
        }
        return new HttpResponse(error, contentType, body);
    }

}
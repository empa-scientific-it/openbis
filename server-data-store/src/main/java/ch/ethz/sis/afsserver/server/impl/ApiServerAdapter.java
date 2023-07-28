/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.ethz.sis.afsserver.server.impl;

import ch.ethz.sis.afsjson.JsonObjectMapper;
import ch.ethz.sis.afsserver.exception.HTTPExceptions;
import ch.ethz.sis.afsserver.http.HttpResponse;
import ch.ethz.sis.afsserver.http.HttpServerHandler;
import ch.ethz.sis.afsserver.server.APIServer;
import ch.ethz.sis.afsserver.server.APIServerException;
import ch.ethz.sis.afsserver.server.Request;
import ch.ethz.sis.afsserver.server.Response;
import ch.ethz.sis.afsserver.server.performance.Event;
import ch.ethz.sis.afsserver.server.performance.PerformanceAuditor;
import ch.ethz.sis.shared.log.LogManager;
import ch.ethz.sis.shared.log.Logger;
import io.netty.handler.codec.http.HttpMethod;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static io.netty.handler.codec.http.HttpMethod.*;

/*
 * This class is supposed to be called by a TCP or HTTP transport class
 */
public class ApiServerAdapter<CONNECTION, API> implements HttpServerHandler
{

    private static final Logger logger = LogManager.getLogger(ApiServerAdapter.class);

    private final APIServer<CONNECTION, Request, Response, API> server;

    private final JsonObjectMapper jsonObjectMapper;

    private final ApiResponseBuilder apiResponseBuilder;

    public ApiServerAdapter(
            APIServer<CONNECTION, Request, Response, API> server,
            JsonObjectMapper jsonObjectMapper)
    {
        this.server = server;
        this.jsonObjectMapper = jsonObjectMapper;
        this.apiResponseBuilder = new ApiResponseBuilder();
    }

    public static HttpMethod getHttpMethod(String apiMethod)
    {
        switch (apiMethod)
        {
            case "list":
            case "read":
            case "isSessionValid":
                return GET; // all parameters from GET methods come on the query string
            case "create":
            case "write":
            case "move":
            case "copy":
            case "login":
            case "logout":
            case "begin":
            case "prepare":
            case "commit":
            case "rollback":
            case "recover":
                return POST; // all parameters from POST methods come on the body
            case "delete":
                return HttpMethod.DELETE; // all parameters from DELETE methods come on the body
        }
        throw new UnsupportedOperationException("This line SHOULD NOT be unreachable!");
    }

    public boolean isValidMethod(HttpMethod givenMethod, String apiMethod)
    {
        HttpMethod correctMethod = getHttpMethod(apiMethod);
        return correctMethod == givenMethod;
    }

    public HttpResponse process(HttpMethod httpMethod, Map<String, List<String>> parameters,
            byte[] requestBody)
    {
        try
        {
            logger.traceAccess(null);
            PerformanceAuditor performanceAuditor = new PerformanceAuditor();


            if (httpMethod != GET && httpMethod != POST && httpMethod != DELETE)
            {
                return getHTTPResponse(new ApiResponse("1", null,
                        HTTPExceptions.INVALID_HTTP_METHOD.getCause()));
            }

            String method = null;
            String sessionToken = null;
            String interactiveSessionKey = null;
            String transactionManagerKey = null;
            Map<String, Object> methodParameters = new HashMap<>();

            for (Map.Entry<String, List<String>> entry : parameters.entrySet())
            {
                String value = null;
                if (entry.getValue() != null)
                {
                    if (entry.getValue().size() == 1)
                    {
                        value = entry.getValue().get(0);
                    } else if (entry.getValue().size() > 1)
                    {
                        return getHTTPResponse(new ApiResponse("1", null,
                                HTTPExceptions.INVALID_PARAMETERS.getCause()));
                    }
                }

                try
                {
                    switch (entry.getKey())
                    {
                        case "method":
                            method = value;
                            if (!isValidMethod(httpMethod, method))
                            {
                                return getHTTPResponse(new ApiResponse("1", null,
                                        HTTPExceptions.INVALID_HTTP_METHOD.getCause()));
                            }
                            break;
                        case "sessionToken":
                            sessionToken = value;
                            break;
                        case "interactiveSessionKey":
                            interactiveSessionKey = value;
                            break;
                        case "transactionManagerKey":
                            transactionManagerKey = value;
                            break;
                        case "transactionId":
                            methodParameters.put(entry.getKey(), UUID.fromString(value));
                            break;
                        case "directory":
                        case "recursively":
                            methodParameters.put(entry.getKey(), Boolean.valueOf(value));
                            break;
                        case "offset":
                            methodParameters.put(entry.getKey(), Long.valueOf(value));
                            break;
                        case "limit":
                            methodParameters.put(entry.getKey(), Integer.valueOf(value));
                            break;
                        case "data":
                            methodParameters.put(entry.getKey(), Base64.getDecoder().decode(value));
                            break;
                        case "md5Hash":
                            methodParameters.put(entry.getKey(), Base64.getDecoder().decode(value));
                            break;
                        default:
                            methodParameters.put(entry.getKey(), value);
                            break;
                    }
                } catch (Exception e)
                {
                    logger.catching(e);
                    return getHTTPResponse(new ApiResponse("1", null,
                            HTTPExceptions.INVALID_PARAMETERS.getCause(
                                    e.getClass().getSimpleName(),
                                    e.getMessage())));
                }
            }

            ApiRequest apiRequest = new ApiRequest("1", method, methodParameters, sessionToken,
                    interactiveSessionKey, transactionManagerKey);
            Response response = server.processOperation(apiRequest, apiResponseBuilder, performanceAuditor);
            HttpResponse httpResponse = getHTTPResponse(response);
            performanceAuditor.audit(Event.WriteResponse);
            logger.traceExit(performanceAuditor);
            logger.traceExit(httpResponse);
            return httpResponse;
        } catch (APIServerException e)
        {
            logger.catching(e);
            switch (e.getType())
            {
                case MethodNotFound:
                case IncorrectParameters:
                case InternalError:
                    try
                    {
                        return getHTTPResponse(new ApiResponse("1", null, e.getData()));
                    } catch (Exception ex)
                    {
                        logger.catching(ex);
                    }
            }
        } catch (Exception e)
        {
            logger.catching(e);
            try
            {
                return getHTTPResponse(new ApiResponse("1", null,
                        HTTPExceptions.UNKNOWN.getCause(e.getClass().getSimpleName(),
                                e.getMessage())));
            } catch (Exception ex)
            {
                logger.catching(ex);
            }
        }
        return null; // This should never happen, it would mean an error writing the Unknown error happened.
    }

    public HttpResponse getHTTPResponse(Response response)
            throws Exception
    {
        boolean error = response.getError() != null;
        String contentType = null;
        byte[] body = null;
        if (error) {
            contentType = HttpResponse.CONTENT_TYPE_JSON;
            body = jsonObjectMapper.writeValue(response);
        } else if (response.getResult() instanceof List) {
            contentType = HttpResponse.CONTENT_TYPE_JSON;
            body = jsonObjectMapper.writeValue(response);
        } else if(response.getResult() instanceof byte[]) {
            contentType = HttpResponse.CONTENT_TYPE_BINARY_DATA;
            body = (byte[]) response.getResult();
        } else {
            contentType = HttpResponse.CONTENT_TYPE_TEXT;
            body = String.valueOf(response.getResult()).getBytes(StandardCharsets.UTF_8);
        }
        return new HttpResponse(error, contentType, body);
    }

}
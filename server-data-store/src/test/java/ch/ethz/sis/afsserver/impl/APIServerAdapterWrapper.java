package ch.ethz.sis.afsserver.impl;


import ch.ethz.sis.afsserver.core.AbstractPublicAPIWrapper;
import ch.ethz.sis.afsserver.http.HttpResponse;
import ch.ethz.sis.afsserver.server.impl.ApiRequest;
import ch.ethz.sis.afsserver.server.impl.ApiResponse;
import ch.ethz.sis.afsserver.server.impl.ApiServerAdapter;
import ch.ethz.sis.afsserver.server.performance.PerformanceAuditor;
import ch.ethz.sis.shared.io.IOUtils;
import ch.ethz.sis.shared.json.JSONObjectMapper;
import ch.ethz.sis.shared.log.LogManager;
import ch.ethz.sis.shared.log.Logger;
import io.netty.handler.codec.http.HttpMethod;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static ch.ethz.sis.afsserver.http.HttpResponse.CONTENT_TYPE_BINARY_DATA;
import static ch.ethz.sis.afsserver.http.HttpResponse.CONTENT_TYPE_JSON;

public class APIServerAdapterWrapper extends AbstractPublicAPIWrapper {

    private static final Logger logger = LogManager.getLogger(APIServerAdapterWrapper.class);

    private ApiServerAdapter apiServerAdapter;
    private JSONObjectMapper jsonObjectMapper;

    public APIServerAdapterWrapper(ApiServerAdapter apiServerAdapter, JSONObjectMapper jsonObjectMapper) {
        this.apiServerAdapter = apiServerAdapter;
        this.jsonObjectMapper = jsonObjectMapper;
    }

    public Map<String, List<String>> getURIParameters(Map<String, Object> args) {
        Map<String, List<String>> result = new HashMap<>(args.size());
        for (Map.Entry<String, Object> entry:args.entrySet()) {
            if (entry.getKey().equals("data") && entry.getValue() instanceof byte[]) {
                continue; // Skip
            } else if(entry.getValue() instanceof byte[]) {
                result.put(entry.getKey(), List.of(IOUtils.encodeBase64((byte[]) entry.getValue())));
            } else {
                result.put(entry.getKey(), List.of(String.valueOf(entry.getValue())));
            }
        }
        return result;
    }

    public <E> E process(String apiMethod, Map<String, Object> args) {
        try {
            HttpMethod httpMethod = ApiServerAdapter.getHttpMethod(apiMethod);

            Map<String, List<String>> uriParameters = getURIParameters(args);
            uriParameters.put("method", List.of(apiMethod));
            uriParameters.put("sessionToken", List.of(UUID.randomUUID().toString()));

            byte[] requestBody = null;
            if (apiMethod.equals("write")) {
                requestBody = (byte[]) args.get("data");
            }

            HttpResponse response = apiServerAdapter.process(httpMethod, uriParameters, requestBody);
            switch (response.getContentType()) {
                case CONTENT_TYPE_BINARY_DATA:
                    return (E) response.getBody();
                case CONTENT_TYPE_JSON:
                    ApiResponse apiResponse = jsonObjectMapper.readValue(new ByteArrayInputStream(response.getBody()), ApiResponse.class);
                    return  (E) apiResponse.getResult();
            }
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
        throw new RuntimeException("This line should be unreachable");
    }

}

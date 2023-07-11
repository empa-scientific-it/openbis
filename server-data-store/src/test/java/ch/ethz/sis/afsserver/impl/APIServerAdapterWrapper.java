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
package ch.ethz.sis.afsserver.impl;

import ch.ethz.sis.afsclient.client.AfsClient;
import ch.ethz.sis.afsserver.core.AbstractPublicAPIWrapper;
import ch.ethz.sis.afsserver.http.HttpResponse;
import ch.ethz.sis.afsserver.server.impl.ApiServerAdapter;
import ch.ethz.sis.shared.io.IOUtils;
import ch.ethz.sis.shared.log.LogManager;
import ch.ethz.sis.shared.log.Logger;
import io.netty.handler.codec.http.HttpMethod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class APIServerAdapterWrapper extends AbstractPublicAPIWrapper
{

    private static final Logger logger = LogManager.getLogger(APIServerAdapterWrapper.class);

    private ApiServerAdapter apiServerAdapter;

    private String interactiveSessionKey;

    private String transactionManagerKey;

    private String sessionToken;

    public APIServerAdapterWrapper(ApiServerAdapter apiServerAdapter)
    {
        this.apiServerAdapter = apiServerAdapter;
    }

    public APIServerAdapterWrapper(ApiServerAdapter apiServerAdapter, String interactiveSessionKey,
            String transactionManagerKey, String sessionToken)
    {
        this.apiServerAdapter = apiServerAdapter;
        this.interactiveSessionKey = interactiveSessionKey;
        this.transactionManagerKey = transactionManagerKey;
        this.sessionToken = sessionToken;
    }

    public Map<String, List<String>> getURIParameters(Map<String, Object> args)
    {
        Map<String, List<String>> result = new HashMap<>(args.size());
        for (Map.Entry<String, Object> entry : args.entrySet())
        {
            if (entry.getValue() instanceof byte[])
            {
                result.put(entry.getKey(), List.of(IOUtils.encodeBase64((byte[]) entry.getValue())));
            } else
            {
                result.put(entry.getKey(), List.of(String.valueOf(entry.getValue())));
            }
        }
        return result;
    }

    public <E> E process(Class<E> responseType, String apiMethod, Map<String, Object> params)
    {
        try
        {
            HttpMethod httpMethod = ApiServerAdapter.getHttpMethod(apiMethod);
            Map<String, List<String>> requestParameters = getURIParameters(params);
            if (interactiveSessionKey != null)
            {
                requestParameters.put("interactiveSessionKey", List.of(interactiveSessionKey));
            }
            if (transactionManagerKey != null)
            {
                requestParameters.put("transactionManagerKey", List.of(transactionManagerKey));
            }
            if (sessionToken != null)
            {
                requestParameters.put("sessionToken", List.of(sessionToken));
            }
            requestParameters.put("method", List.of(apiMethod));

            byte[] requestBody = null;

            if (HttpMethod.GET.equals(httpMethod))
            {
                // Do nothing
            } else if (HttpMethod.POST.equals(httpMethod) || HttpMethod.DELETE.equals(httpMethod))
            {
                // Do nothing
            } else
            {
                throw new IllegalArgumentException("Not supported HTTP method type!");
            }



            HttpResponse response = apiServerAdapter.process(httpMethod, requestParameters, requestBody);
            String contentType = response.getContentType();
            byte[] body = response.getBody();

            return AfsClient.getResponseResult(responseType, contentType, body);
        } catch (Throwable throwable)
        {
            throw new RuntimeException(throwable);
        }
    }

}

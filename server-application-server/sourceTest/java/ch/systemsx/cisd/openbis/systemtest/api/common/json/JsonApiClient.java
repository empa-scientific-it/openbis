/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.systemtest.api.common.json;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author pkupczyk
 */
public class JsonApiClient
{

    private final String USER_AGENT = "Mozilla/5.0";

    private String apiUrl;

    public JsonApiClient(String apiUrl)
    {
        this.apiUrl = apiUrl;
    }

    public JsonNode sendRequest(String json, Object... params) throws Exception
    {
        /*
         * String jsonWithParams = String.format(json, params); HttpClient client = new DefaultHttpClient(); HttpPost post = new HttpPost(apiUrl); //
         * add header post.setHeader("User-Agent", USER_AGENT); post.setEntity(new StringEntity(jsonWithParams)); HttpResponse response =
         * client.execute(post); System.out.println(">>>>>"); System.out.println("Sending 'POST' request to URL : " + apiUrl);
         * System.out.println("Post parameters : " + jsonWithParams.trim()); System.out.println("Response Code : " +
         * response.getStatusLine().getStatusCode()); BufferedReader rd = new BufferedReader( new
         * InputStreamReader(response.getEntity().getContent())); StringBuffer result = new StringBuffer(); String line = ""; while ((line =
         * rd.readLine()) != null) { result.append(line); } System.out.println("Response : " + result.toString()); System.out.println("<<<<<"); return
         * new ObjectMapper().readTree(result.toString());
         */
        return null;
    }

}

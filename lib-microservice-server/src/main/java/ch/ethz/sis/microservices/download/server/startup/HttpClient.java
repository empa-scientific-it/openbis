/*
 * Copyright ETH 2018 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.microservices.download.server.startup;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Map;

public class HttpClient
{
    public static byte[] doGet(final String urlAsString, final Map<String, String> parameters) throws Exception
    {
        return doAction(urlAsString, parameters, "GET", null);
    }

    public static byte[] doPost(final String urlAsString, final Map<String, String> parameters, final byte[] body)
            throws Exception
    {
        return doAction(urlAsString, parameters, "POST", body);
    }

    public static byte[] doAction(final String urlAsString, final Map<String, String> parameters, final String method,
            final byte[] body) throws Exception
    {
        StringBuilder parametersAsString = new StringBuilder();
        boolean first = true;
        parametersAsString.append("?");
        for (Map.Entry<String, String> entry : parameters.entrySet())
        {
            if (first)
            {
                first = false;
            } else
            {
                parametersAsString.append("&");
            }
            parametersAsString.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            parametersAsString.append("=");
            parametersAsString.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        URL url = new URL(urlAsString + parametersAsString);
        URLConnection con = url.openConnection();
        con.setUseCaches(false);

        HttpURLConnection http = (HttpURLConnection) con;
        http.setRequestMethod(method);
        http.setDoOutput(true);

        if (body != null)
        {
            try (final BufferedOutputStream bos = new BufferedOutputStream(con.getOutputStream()))
            {
                bos.write(body);
            }
        }

        http.connect();

        int responseCode = http.getResponseCode();

        byte[] response;
        if (responseCode == HttpURLConnection.HTTP_OK)
        {
            response = getBytesFromInputStream(http.getInputStream());
        } else
        {
            throw new RuntimeException("Response Code: " + responseCode);
        }
        return response;
    }

    private static byte[] getBytesFromInputStream(InputStream is) throws IOException
    {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream())
        {
            byte[] buffer = new byte[0xFFF];

            int len;
            while ((len = is.read(buffer)) != -1)
            {
                os.write(buffer, 0, len);
            }

            os.flush();

            return os.toByteArray();
        }
    }
}

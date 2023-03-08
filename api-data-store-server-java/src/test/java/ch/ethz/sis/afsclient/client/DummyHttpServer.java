/*
 *  Copyright ETH 2023 Zürich, Scientific IT Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ch.ethz.sis.afsclient.client;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public final class DummyHttpServer
{
    private HttpServer httpServer;

    private final int httpServerPort; // 8085

    private final String httpServerPath; // "/fileserver"

    private static final String DEFAULT_RESPONSE = "{\"result\": \"success\"}";

    private byte[] nextResponse = DEFAULT_RESPONSE.getBytes();
    private String nextResponseType = "application/json";

    private HttpExchange httpExchange;

    public DummyHttpServer(int httpServerPort, String httpServerPath) throws IOException
    {
        this.httpServerPort = httpServerPort;
        this.httpServerPath = httpServerPath;
        httpServer = HttpServer.create(new InetSocketAddress(httpServerPort), 0);
        httpServer.createContext(httpServerPath, new HttpHandler()
        {
            public void handle(HttpExchange exchange) throws IOException
            {
                byte[] response = nextResponse;
                exchange.getResponseHeaders().set("content-type", nextResponseType);
                exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);

                exchange.getResponseBody().write(response);
                exchange.close();
                httpExchange = exchange;
            }
        });
    }

    public void start()
    {
        httpServer.start();
    }

    public void stop()
    {
        httpServer.stop(0);
    }

    public void setNextResponse(String response)
    {
        this.nextResponse = response.getBytes();
        this.nextResponseType = "application/json";
    }

    public void setNextResponse(byte[] response)
    {
        this.nextResponse = response;
        this.nextResponseType = "application/octet-stream";
    }

    public HttpExchange getHttpExchange()
    {
        return httpExchange;
    }

}

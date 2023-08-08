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
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

public final class DummyHttpServer
{
    private HttpServer httpServer;

    private final int httpServerPort; // 8085

    private final String httpServerPath; // "/fileserver"

    private static final String DEFAULT_RESPONSE = "{\"result\": \"success\"}";

    private Queue<byte[]> nextResponses = new LinkedList<>(List.of(DEFAULT_RESPONSE.getBytes()));

    private byte[] lastRequestBody = null;

    private Queue<String> nextResponseTypes = new LinkedList<>(List.of("application/json"));

    private HttpExchange httpExchange;

    public DummyHttpServer(int httpServerPort, String httpServerPath) throws IOException
    {
        this.httpServerPort = httpServerPort;
        this.httpServerPath = httpServerPath;
        httpServer = HttpServer.create(new InetSocketAddress(httpServerPort), 0);
        httpServer.createContext(httpServerPath, exchange ->
        {
            byte[] response = nextResponses.remove();
            exchange.getResponseHeaders().set("content-type", nextResponseTypes.remove());
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);

            exchange.getResponseBody().write(response);
            lastRequestBody = exchange.getRequestBody().readAllBytes();

            exchange.close();
            httpExchange = exchange;
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
        setNextResponses(new byte[][] {response.getBytes()}, new String[] {"application/json"});
    }

    public void setNextResponse(byte[] response)
    {
        setNextResponses(new byte[][] {response}, new String[] {"application/octet-stream"});
    }

    public void setNextResponses(byte[][] responses, String[] responseTypes)
    {
        this.nextResponses = new LinkedList<>(List.of(responses));
        this.nextResponseTypes = new LinkedList<>(List.of(responseTypes));
    }

    public byte[] getLastRequestBody()
    {
        return lastRequestBody;
    }

    public HttpExchange getHttpExchange()
    {
        return httpExchange;
    }

}

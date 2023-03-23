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

package ch.ethz.sis.afsserver.client;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.rights.Right;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.rights.Rights;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.springframework.remoting.httpinvoker.SimpleHttpInvokerServiceExporter;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationResult;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class DummyOpenBisServer
{
    private final HttpServer httpServer;

    private Map<String, Object> responses = Map.of();

    public DummyOpenBisServer(int httpServerPort, String httpServerPath) throws IOException
    {
        httpServer = HttpServer.create(new InetSocketAddress(httpServerPort), 0);
        httpServer.createContext(httpServerPath, exchange ->
        {
            DummyInvoker inv = new DummyInvoker(responses);
            inv.handle(exchange);
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

    public void setResponses(Map<String, Object> responses)
    {
        this.responses = responses;
    }

    private static class DummyInvoker extends SimpleHttpInvokerServiceExporter
    {
        private final Map<String, Object> result;

        public DummyInvoker(Map<String, Object> result)
        {
            super();
            this.result = result;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException
        {
            try
            {
                RemoteInvocation invocation = super.readRemoteInvocation(exchange);
                final String method = invocation.getMethodName();
                Object resultObj;
                if (result.containsKey(method))
                {
                    if("getRights".equals(method)) {
                        Object param = ((List<?>) invocation.getArguments()[1]).get(0);
                        resultObj = Map.of(param, (Rights) result.get(method));
                    } else {
                        resultObj = result.get(method);
                    }
                } else
                {
                    switch (method)
                    {
                        case "login":
                            resultObj = "test-login-token";
                            break;
                        case "logout":
                        case "isSessionActive":
                            resultObj = true;
                            break;
                        case "getSamples":
                            resultObj = Map.of(new SampleIdentifier(""), new Sample());
                            break;
                        case "getRights":
                            Object param = ((List<?>) invocation.getArguments()[1]).get(0);
                            resultObj = Map.of(param, new Rights(Set.of(Right.UPDATE)));
                            break;
                        default:
                            throw new IllegalStateException(
                                    "Unknown method: " + invocation.getMethodName());
                    }
                }
                RemoteInvocationResult remoteInvocationResult =
                        new RemoteInvocationResult(resultObj);
                this.writeRemoteInvocationResult(exchange, remoteInvocationResult);
                exchange.close();
            } catch (ClassNotFoundException var4)
            {
                exchange.sendResponseHeaders(500, -1L);
                this.logger.error("Class not found during deserialization", var4);
            }
        }

    }

}

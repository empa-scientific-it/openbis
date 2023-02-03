/*
 * Copyright 2018 ETH Zuerich, CISD
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

package ch.ethz.sis.filetransfer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;

/**
 * @author pkupczyk
 */
public class HttpDownloadServer implements IDownloadServer
{

    private static ILogger logger;

    private static IDownloadServer downloadServer;

    private static HttpClient httpClient;

    private static Server httpServer;

    private static int httpPort = 10300;

    public HttpDownloadServer(ILogger logger, IDownloadServer downloadServer)
    {
        HttpDownloadServer.logger = logger;
        HttpDownloadServer.downloadServer = downloadServer;
        init();
    }

    private static void init()
    {
        try
        {
            if (httpClient == null)
            {
                httpClient = new HttpClient();
                httpClient.start();
            }

            if (httpServer == null)
            {
                httpServer = new Server();
                httpServer.setHandler(new AbstractHandler()
                    {
                        public void handle(String target, org.eclipse.jetty.server.Request baseRequest, HttpServletRequest request,
                                HttpServletResponse response) throws IOException, ServletException
                        {
                            try
                            {
                                Object object = deserialize(IOUtils.toByteArray(request.getInputStream()));

                                if (logger.isEnabled(LogLevel.DEBUG))
                                {
                                    logger.log(getClass(), LogLevel.DEBUG, "Received HTTP request with params: " + object);
                                }

                                response.setStatus(HttpServletResponse.SC_OK);
                                response.setContentType("application/octet-stream");

                                Serializable result = null;

                                if (object instanceof StartDownloadSessionParams)
                                {
                                    StartDownloadSessionParams params = (StartDownloadSessionParams) object;
                                    DownloadSession downloadSession =
                                            downloadServer.startDownloadSession(params.userSessionId, params.itemIds, params.preferences);
                                    result = downloadSession;
                                } else if (object instanceof QueueParams)
                                {
                                    QueueParams params = (QueueParams) object;
                                    downloadServer.queue(params.downloadSessionId, params.ranges);
                                } else if (object instanceof DownloadParams)
                                {
                                    DownloadParams params = (DownloadParams) object;
                                    InputStream stream =
                                            downloadServer.download(params.downloadSessionId, params.streamId, params.numberOfChunksOrNull);
                                    result = new DownloadResult(IOUtils.toByteArray(stream));
                                } else if (object instanceof FinishDownloadSessionParams)
                                {
                                    FinishDownloadSessionParams params = (FinishDownloadSessionParams) object;
                                    downloadServer.finishDownloadSession(params.downloadSessionId);
                                } else
                                {
                                    throw new IllegalArgumentException("Unsupported request: " + object);
                                }

                                byte[] bytes = serialize(result);
                                response.setContentLength(bytes.length);
                                response.getOutputStream().write(bytes);

                            } catch (Exception e)
                            {
                                response.setStatus(500);
                                byte[] bytes = serialize(e);
                                response.setContentLength(bytes.length);
                                response.getOutputStream().write(bytes);
                            }
                        };
                    });

                ServerConnector connector = new ServerConnector(httpServer, new HttpConnectionFactory(new HttpConfiguration()));
                connector.setPort(httpPort);

                httpServer.addConnector(connector);
                httpServer.start();
            }
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DownloadSession startDownloadSession(IUserSessionId userSessionId, List<IDownloadItemId> itemIds, DownloadPreferences preferences)
            throws DownloadItemNotFoundException, InvalidUserSessionException, DownloadException
    {
        StartDownloadSessionParams params = new StartDownloadSessionParams();
        params.userSessionId = userSessionId;
        params.itemIds = itemIds;
        params.preferences = preferences;

        return sendRequest(params);
    }

    @Override
    public void queue(DownloadSessionId downloadSessionId, List<DownloadRange> ranges)
            throws InvalidUserSessionException, InvalidDownloadSessionException, DownloadException
    {
        QueueParams params = new QueueParams();
        params.downloadSessionId = downloadSessionId;
        params.ranges = ranges;

        sendRequest(params);
    }

    @Override
    public InputStream download(DownloadSessionId downloadSessionId, DownloadStreamId streamId, Integer numberOfChunksOrNull)
            throws InvalidUserSessionException, InvalidDownloadSessionException, InvalidDownloadStreamException, DownloadException
    {
        return new InputStream()
            {

                InputStream currentChunkStream = null;

                int currentChunkNumber = 0;

                @Override
                public int read() throws IOException
                {
                    if (currentChunkStream == null)
                    {
                        if (numberOfChunksOrNull == null || currentChunkNumber < numberOfChunksOrNull)
                        {
                            DownloadParams params = new DownloadParams();
                            params.downloadSessionId = downloadSessionId;
                            params.streamId = streamId;
                            params.numberOfChunksOrNull = 1;

                            try
                            {
                                DownloadResult response = sendRequest(params);

                                if (response.bytes.length > 0)
                                {
                                    currentChunkStream = new ByteArrayInputStream(response.bytes);
                                    currentChunkNumber++;
                                    return read();
                                } else
                                {
                                    return -1;
                                }
                            } catch (DownloadException e)
                            {
                                throw new RuntimeException(e);
                            }
                        } else
                        {
                            return -1;
                        }
                    } else
                    {
                        int b = currentChunkStream.read();

                        if (b == -1)
                        {
                            currentChunkStream = null;
                            return read();
                        } else
                        {
                            return b;
                        }
                    }
                }
            };
    }

    @Override
    public void finishDownloadSession(DownloadSessionId downloadSessionId) throws DownloadException
    {
        FinishDownloadSessionParams params = new FinishDownloadSessionParams();
        params.downloadSessionId = downloadSessionId;

        sendRequest(params);
    }

    private static class StartDownloadSessionParams implements Serializable
    {

        private static final long serialVersionUID = 1L;

        IUserSessionId userSessionId;

        List<IDownloadItemId> itemIds;

        DownloadPreferences preferences;

    }

    private static class QueueParams implements Serializable
    {

        private static final long serialVersionUID = 1L;

        DownloadSessionId downloadSessionId;

        List<DownloadRange> ranges;

    }

    private static class DownloadParams implements Serializable
    {

        private static final long serialVersionUID = 1L;

        DownloadSessionId downloadSessionId;

        DownloadStreamId streamId;

        Integer numberOfChunksOrNull;

    }

    private static class DownloadResult implements Serializable
    {

        private static final long serialVersionUID = 1L;

        byte[] bytes;

        public DownloadResult(byte[] bytes)
        {
            this.bytes = bytes;
        }

    }

    private static class FinishDownloadSessionParams implements Serializable
    {

        private static final long serialVersionUID = 1L;

        DownloadSessionId downloadSessionId;

    }

    @SuppressWarnings("unchecked")
    private <T> T sendRequest(Serializable params) throws DownloadException
    {
        try
        {
            Request request = httpClient.POST("http://localhost:" + httpPort);
            request.content(new BytesContentProvider(serialize(params)));

            if (logger.isEnabled(LogLevel.DEBUG))
            {
                logger.log(getClass(), LogLevel.DEBUG, "Sending HTTP request to a download server with params: " + params);
            }

            ContentResponse response = request.send();

            if (response.getStatus() == 500)
            {
                Exception e = (Exception) deserialize(response.getContent());
                throw e;
            } else
            {
                return (T) deserialize(response.getContent());
            }
        } catch (DownloadException e)
        {
            throw e;
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private static Object deserialize(byte[] bytes)
    {
        try
        {
            ObjectInputStream stream = new ObjectInputStream(new ByteArrayInputStream(bytes));
            Object object = stream.readObject();
            stream.close();
            return object;
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private static byte[] serialize(Serializable object)
    {
        try
        {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
            objectStream.writeObject(object);
            objectStream.close();
            return byteStream.toByteArray();
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static void stop()
    {
        if (httpClient != null)
        {
            try
            {
                httpClient.stop();
            } catch (Exception e)
            {
                if (logger.isEnabled(LogLevel.WARN))
                {
                    logger.log(HttpDownloadServer.class, LogLevel.WARN, "Couldn't stop an http client that connects to an http download server",
                            e);
                }
            }
        }

        if (httpServer != null)
        {
            try
            {
                httpServer.stop();
            } catch (Exception e)
            {
                if (logger.isEnabled(LogLevel.WARN))
                {
                    logger.log(HttpDownloadServer.class, LogLevel.WARN, "Couldn't stop an http download server", e);
                }
            }
        }

    }

}

/*
 * Copyright 2019 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.dssapi.v3.fastdownload;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.util.Callback;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.ethz.sis.filetransfer.AbstractBulkInputStream;
import ch.ethz.sis.filetransfer.DownloadException;
import ch.ethz.sis.filetransfer.DownloadItemId;
import ch.ethz.sis.filetransfer.DownloadItemNotFoundException;
import ch.ethz.sis.filetransfer.DownloadPreferences;
import ch.ethz.sis.filetransfer.DownloadRange;
import ch.ethz.sis.filetransfer.DownloadSession;
import ch.ethz.sis.filetransfer.DownloadSessionId;
import ch.ethz.sis.filetransfer.DownloadStreamId;
import ch.ethz.sis.filetransfer.IDownloadItemId;
import ch.ethz.sis.filetransfer.IDownloadServer;
import ch.ethz.sis.filetransfer.IUserSessionId;
import ch.ethz.sis.filetransfer.InvalidDownloadSessionException;
import ch.ethz.sis.filetransfer.InvalidDownloadStreamException;
import ch.ethz.sis.filetransfer.InvalidUserSessionException;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fastdownload.FastDownloadMethod;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fastdownload.FastDownloadParameter;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.http.JettyHttpClientFactory;
import ch.systemsx.cisd.common.reflection.ClassUtils;
import ch.systemsx.cisd.common.shared.basic.string.CommaSeparatedListBuilder;

/**
 * @author Franz-Josef Elmer
 */
class RemoteFastDownloadServer implements IDownloadServer
{
    private String url;

    private int requestTimeoutInSeconds;

    private ObjectMapper objectMapper;

    RemoteFastDownloadServer(String baseUrl)
    {
        this(baseUrl, 300);
    }

    RemoteFastDownloadServer(String baseUrl, int requestTimeoutInSeconds)
    {
        url = baseUrl;
        this.requestTimeoutInSeconds = requestTimeoutInSeconds;
        objectMapper = new ObjectMapper();
    }

    @Override
    public DownloadSession startDownloadSession(IUserSessionId userSessionId, List<IDownloadItemId> itemIds, DownloadPreferences preferences)
            throws DownloadItemNotFoundException, InvalidUserSessionException, DownloadException
    {
        Request request = createRequest(new ParameterBuilder()
                .method(FastDownloadMethod.START_DOWNLOAD_SESSION_METHOD)
                .session(userSessionId)
                .downloadItemIds(itemIds)
                .wishedNumberOfStreams(preferences.getWishedNumberOfStreams()).parameters);
        try
        {
            ContentResponse response = sendAndCheckResponse(request);
            String contentAsString = response.getContentAsString();
            JsonNode tree = objectMapper.readTree(contentAsString);
            DownloadSessionId downloadSessionId = createDownloadSessionId(getScalarValue(tree, "downloadSessionId"));
            Map<IDownloadItemId, DownloadRange> ranges = getRanges(tree);
            List<DownloadStreamId> streamIds = getStreamIds(tree);
            return new DownloadSession(downloadSessionId, ranges, streamIds);
        } catch (Exception e)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(e);
        }
    }

    @Override
    public void queue(DownloadSessionId downloadSessionId, List<DownloadRange> ranges)
            throws InvalidUserSessionException, InvalidDownloadSessionException, DownloadException
    {
        Request request = createRequest(new ParameterBuilder()
                .method(FastDownloadMethod.QUEUE_METHOD)
                .downloadSession(downloadSessionId)
                .ranges(ranges).parameters);
        sendAndCheckResponse(request);
    }

    @Override
    public InputStream download(DownloadSessionId downloadSessionId, DownloadStreamId streamId, Integer numberOfChunksOrNull)
            throws InvalidUserSessionException, InvalidDownloadSessionException, InvalidDownloadStreamException, DownloadException
    {
        ParameterBuilder builder = new ParameterBuilder()
                .method(FastDownloadMethod.DOWNLOAD_METHOD)
                .downloadSession(downloadSessionId)
                .numberOfChunks(numberOfChunksOrNull)
                .downloadStream(streamId);
        Request request = createRequest(builder.parameters);
        PipedInputStream pipedInputStream = new PipedInputStream((int) FileUtils.ONE_MB);
        try
        {
            PipedOutputStream pipedOutputStream = new PipedOutputStream(pipedInputStream);
            WritableByteChannel channel = Channels.newChannel(pipedOutputStream);
            request.send(new Response.Listener.Adapter()
                {
                    @Override
                    public void onContent(Response response, ByteBuffer content, Callback callback)
                    {
                        try
                        {
                            channel.write(content);
                            callback.succeeded();
                        } catch (IOException e)
                        {
                            callback.failed(e);
                        }
                    }

                    @Override
                    public void onSuccess(Response response)
                    {
                        try
                        {
                            channel.close();

                        } catch (IOException e)
                        {
                            throw CheckedExceptionTunnel.wrapIfNecessary(e);
                        }
                    }

                    @Override
                    public void onFailure(Response response, Throwable failure)
                    {
                        try
                        {
                            channel.close();
                        } catch (IOException e)
                        {
                            throw CheckedExceptionTunnel.wrapIfNecessary(e);
                        }
                    }
                });
        } catch (Exception e)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(e);
        }
        return new AbstractBulkInputStream()
            {
                @Override
                public int read(byte[] b, int off, int len) throws IOException
                {
                    int numberOfBytesRead = 0;
                    int offset = off;
                    int numberOfBytesToRead = len;
                    while (numberOfBytesRead < len)
                    {
                        int numberOBytes = pipedInputStream.read(b, offset, numberOfBytesToRead);
                        if (numberOBytes < 0)
                        {
                            return -1;
                        }
                        numberOfBytesRead += numberOBytes;
                        offset += numberOBytes;
                        numberOfBytesToRead -= numberOBytes;
                    }
                    return numberOfBytesRead;
                }
            };
    }

    @Override
    public void finishDownloadSession(DownloadSessionId downloadSessionId) throws DownloadException
    {
        Request request = createRequest(new ParameterBuilder()
                .method(FastDownloadMethod.FINISH_DOWNLOAD_SESSION_METHOD)
                .downloadSession(downloadSessionId).parameters);
        sendAndCheckResponse(request);
    }

    private ContentResponse sendAndCheckResponse(Request request)
    {
        try
        {
            ContentResponse response = request.send();
            String mediaType = response.getMediaType();
            if (mediaType != null && mediaType.equals("application/json"))
            {
                JsonNode tree = objectMapper.readTree(response.getContentAsString());
                RuntimeException exception = FastDownloadUtils.createExceptionFromJson(tree);
                if (exception != null)
                {
                    throw exception;
                }
            }
            return response;
        } catch (Exception e)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(e);
        }
    }

    private DownloadSessionId createDownloadSessionId(String id)
    {
        DownloadSessionId downloadSessionId = new DownloadSessionId();
        ClassUtils.setFieldValue(downloadSessionId, "id", id);
        return downloadSessionId;
    }

    private Map<IDownloadItemId, DownloadRange> getRanges(JsonNode node)
    {
        Map<String, JsonNode> map = getMap(node, "ranges");
        Map<IDownloadItemId, DownloadRange> ranges = new LinkedHashMap<>();
        for (Entry<String, JsonNode> entry : map.entrySet())
        {
            DownloadItemId downloadItemId = new DownloadItemId(entry.getKey());
            String rangeString = entry.getValue().asText();
            try
            {
                String[] splitted = rangeString.split(":");
                int start = Integer.parseInt(splitted[0]);
                int end = splitted.length == 1 ? start : Integer.parseInt(splitted[1]);
                ranges.put(downloadItemId, new DownloadRange(start, end));
            } catch (NumberFormatException e)
            {
                throw new IllegalArgumentException("Invalid range definition for download item '"
                        + downloadItemId.getId() + "': " + rangeString);
            }

        }
        return ranges;
    }

    private List<DownloadStreamId> getStreamIds(JsonNode node)
    {
        List<DownloadStreamId> ids = new ArrayList<>();
        for (JsonNode idNode : getArray(node, "streamIds"))
        {
            String streamIdString = idNode.asText();
            DownloadStreamId streamId = new DownloadStreamId();
            ClassUtils.setFieldValue(streamId, "id", streamIdString);
            ids.add(streamId);
        }
        return ids;
    }

    private List<JsonNode> getArray(JsonNode node, String fieldName)
    {
        JsonNode fieldNode = getFieldNode(node, fieldName);
        if (fieldNode.isArray() == false)
        {
            throw new IllegalArgumentException("Field '" + fieldName + "' is "
                    + (fieldNode.isObject() ? "an object" : "a value") + " node instead of an array node.");
        }
        List<JsonNode> result = new ArrayList<>();
        for (Iterator<JsonNode> iterator = fieldNode.elements(); iterator.hasNext();)
        {
            result.add(iterator.next());
        }
        return result;
    }

    private Map<String, JsonNode> getMap(JsonNode node, String fieldName)
    {
        JsonNode fieldNode = getFieldNode(node, fieldName);
        if (fieldNode.isObject() == false)
        {
            throw new IllegalArgumentException("Field '" + fieldName + "' is "
                    + (fieldNode.isArray() ? "an array" : "a value") + " node instead of a object node.");
        }

        Map<String, JsonNode> result = new LinkedHashMap<>();
        for (Iterator<Entry<String, JsonNode>> iterator = fieldNode.fields(); iterator.hasNext();)
        {
            Entry<String, JsonNode> field = iterator.next();
            result.put(field.getKey(), field.getValue());
        }
        return result;
    }

    private String getScalarValue(JsonNode node, String fieldName)
    {
        JsonNode fieldNode = getFieldNode(node, fieldName);
        if (fieldNode.isValueNode() == false)
        {
            throw new IllegalArgumentException("Field '" + fieldName + "' is an "
                    + (fieldNode.isArray() ? "array" : "object") + " node instead of a value node.");
        }
        return fieldNode.asText();
    }

    private JsonNode getFieldNode(JsonNode node, String fieldName)
    {
        JsonNode fieldNode = node.get(fieldName);
        if (fieldNode == null)
        {
            throw new IllegalArgumentException("No field '" + fieldName + "'.");
        }
        return fieldNode;
    }

    private Request createRequest(Map<FastDownloadParameter, String> parameters)
    {
        HttpClient httpClient = JettyHttpClientFactory.getHttpClient();
        return httpClient.newRequest(createUri(parameters)).timeout(requestTimeoutInSeconds, TimeUnit.SECONDS);
    }

    private String createUri(Map<FastDownloadParameter, String> parameters)
    {
        StringBuilder builder = new StringBuilder(url);
        char delim = '?';
        for (Entry<FastDownloadParameter, String> entry : parameters.entrySet())
        {
            builder.append(delim).append(encode(entry.getKey().getParameterName()));
            builder.append('=').append(encode(entry.getValue()));
            delim = '&';
        }
        String uri = builder.toString();
        return uri;
    }

    private String encode(String string)
    {
        try
        {
            return URLEncoder.encode(string, "UTF-8");
        } catch (UnsupportedEncodingException e)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(e);
        }
    }

    private static final class ParameterBuilder
    {
        private Map<FastDownloadParameter, String> parameters = new TreeMap<>();

        ParameterBuilder method(FastDownloadMethod method)
        {
            parameters.put(FastDownloadParameter.METHOD_PARAMETER, method.getMethodName());
            return this;
        }

        ParameterBuilder session(IUserSessionId sessionId)
        {
            parameters.put(FastDownloadParameter.USER_SESSION_ID_PARAMETER, sessionId.getId());
            return this;
        }

        ParameterBuilder downloadSession(DownloadSessionId sessionId)
        {
            parameters.put(FastDownloadParameter.DOWNLOAD_SESSION_ID_PARAMETER, sessionId.getId());
            return this;
        }

        ParameterBuilder downloadStream(DownloadStreamId streamId)
        {
            parameters.put(FastDownloadParameter.DOWNLOAD_STREAM_ID_PARAMETER, streamId.getId());
            return this;
        }

        ParameterBuilder wishedNumberOfStreams(Integer wishedNumberOfStreams)
        {
            if (wishedNumberOfStreams != null)
            {
                parameters.put(FastDownloadParameter.WISHED_NUMBER_OF_STREAMS_PARAMETER, wishedNumberOfStreams.toString());
            }
            return this;
        }

        ParameterBuilder numberOfChunks(Integer numberOfChunks)
        {
            if (numberOfChunks != null)
            {
                parameters.put(FastDownloadParameter.NUMBER_OF_CHUNKS_PARAMETER, numberOfChunks.toString());
            }
            return this;
        }

        ParameterBuilder downloadItemIds(List<IDownloadItemId> ids)
        {
            CommaSeparatedListBuilder builder = new CommaSeparatedListBuilder();
            for (IDownloadItemId itemId : ids)
            {
                builder.append(itemId.getId());
            }
            parameters.put(FastDownloadParameter.DOWNLOAD_ITEM_IDS_PARAMETER, builder.toString());
            return this;
        }

        ParameterBuilder ranges(List<DownloadRange> ranges)
        {
            CommaSeparatedListBuilder builder = new CommaSeparatedListBuilder();
            for (DownloadRange range : ranges)
            {
                builder.append(range.getStart() + ":" + range.getEnd());
            }
            parameters.put(FastDownloadParameter.RANGES_PARAMETER, builder.toString());
            return this;
        }
    }

}

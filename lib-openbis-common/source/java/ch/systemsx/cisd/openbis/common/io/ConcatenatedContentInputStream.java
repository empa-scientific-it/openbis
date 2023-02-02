/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.common.io;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;

/**
 * Special <code>InputStream</code> that will concatenate the contents of an array of {@link IHierarchicalContentNode} instances into one stream. Each
 * content will be preceded by one long which tells what is the size of the content in bytes.
 * 
 * @author Tomasz Pylak
 */
public class ConcatenatedContentInputStream extends InputStream
{
    private static final int EOF = -1;

    enum StreamType
    {
        HEADER_SIZE, HEADER, CONTENT_SIZE, CONTENT
    }

    /**
     * Wraps specified {@link File} objects into {@link FileBasedContentNode} objects.
     */
    public static IHierarchicalContentNode[] wrap(List<File> files)
    {
        List<IHierarchicalContentNode> list = new ArrayList<IHierarchicalContentNode>();
        for (File file : files)
        {
            list.add(new FileBasedContentNode(file));
        }
        return list.toArray(new IHierarchicalContentNode[files.size()]);
    }

    private int currentIndex = -1;

    private boolean closed = false;

    private IHierarchicalContentNode[] contents;

    private StreamType currentStreamType;

    private InputStream currentStream;

    private final boolean ignoreNonExistingContents;

    /**
     * @param ignoreNonExistingContents If <code>true</code> non-existing contents or elements on the list which are null will be handled like empty
     *            contents. Otherwise an exception is thrown for a non-existing content.
     * @param contents InputStreams provided by these contents will be concatenated into one stream.
     */
    public ConcatenatedContentInputStream(boolean ignoreNonExistingContents,
            IHierarchicalContentNode... contents)
    {
        this.ignoreNonExistingContents = ignoreNonExistingContents;
        this.contents = contents;
    }

    /**
     * @param ignoreNonExistingContents If <code>true</code> non-existing contents or elements on the list which are null will be handled like empty
     *            contents. Otherwise an exception is thrown for a non-existing content.
     * @param contents InputStreams provided by these contents will be concatenated into one stream.
     */
    public ConcatenatedContentInputStream(boolean ignoreNonExistingContents,
            List<IHierarchicalContentNode> contents)
    {
        this(ignoreNonExistingContents, contents.toArray(new IHierarchicalContentNode[contents
                .size()]));
    }

    @Override
    public void close() throws IOException
    {
        closeCurrentStream();
        closed = true;
    }

    @Override
    public int read() throws IOException
    {
        int result = readCurrent();

        // we have a loop instead of an if to ignore contents which are empty
        while (result == EOF && !closed)
        {
            closeCurrentStream();

            if (StreamType.HEADER_SIZE.equals(currentStreamType))
            {
                currentStreamType = StreamType.HEADER;
                currentStream = createHeaderStream(tryGetCurrentContent());
            } else if (StreamType.HEADER.equals(currentStreamType))
            {
                currentStreamType = StreamType.CONTENT_SIZE;
                currentStream = createContentSizeStream(tryGetCurrentContent());
            } else if (StreamType.CONTENT_SIZE.equals(currentStreamType))
            {
                currentStreamType = StreamType.CONTENT;
                currentStream = createContentStream(tryGetCurrentContent());
            } else
            {
                currentIndex++;

                if (hasCurrentContent())
                {
                    currentStreamType = StreamType.HEADER_SIZE;
                    currentStream = createHeaderSizeStream(tryGetCurrentContent());
                } else
                {
                    closed = true;
                    return EOF;
                }
            }

            result = currentStream.read();
        }
        return result;
    }

    // null means that the current content has not been specified. This case will be treated in the
    // same way as an empty content if ignoreNonExistingContents is true
    private IHierarchicalContentNode tryGetCurrentContent()
    {
        if (hasCurrentContent() == false)
        {
            throw new IllegalStateException("there are no more content to read");
        }
        return contents[currentIndex]; // can be null
    }

    // returns true if there is a content to read
    private boolean hasCurrentContent()
    {
        return (contents != null && currentIndex < contents.length);
    }

    private int readCurrent() throws IOException
    {
        return (closed || currentStream == null) ? EOF : currentStream.read();
    }

    private void closeCurrentStream()
    {
        close(currentStream);
        currentStream = null;
    }

    // -------------- static helper ---------------

    protected static ByteArrayInputStream createEmptyStream()
    {
        return new ByteArrayInputStream(new byte[0]);
    }

    protected InputStream createHeaderSizeStream(IHierarchicalContentNode contentOrNull)
            throws IOException
    {
        return createEmptyStream();
    }

    protected InputStream createHeaderStream(IHierarchicalContentNode contentOrNull)
            throws IOException
    {
        return createEmptyStream();
    }

    protected InputStream createContentSizeStream(IHierarchicalContentNode contentOrNull)
            throws IOException
    {
        long size = (contentOrNull == null || contentOrNull.isDirectory()) ? 0 : contentOrNull.getFileLength();
        byte[] data = longToBytes(size);
        return new ByteArrayInputStream(data);
    }

    protected InputStream createContentStream(IHierarchicalContentNode currentContentOrNull)
    {
        InputStream stream;
        if (ignoreNonExistingContents
                && (currentContentOrNull == null || currentContentOrNull.exists() == false || currentContentOrNull.isDirectory()))
        {
            stream = createEmptyStream();
        } else
        {
            // exception will be thrown if the content is null or does not exist
            stream = currentContentOrNull.getInputStream();
        }
        return new BufferedInputStream(stream);
    }

    protected byte[] longToBytes(long size) throws IOException
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeLong(size);
        dos.flush();
        byte[] data = bos.toByteArray();
        dos.close();
        return data;
    }

    protected byte[] objectToBytes(Object obj) throws IOException
    {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutputStream o = new ObjectOutputStream(b);
        o.writeObject(obj);
        o.flush();
        o.close();
        return b.toByteArray();
    }

    /**
     * Close a stream without throwing any exception if something went wrong. Do not attempt to close it if the argument is null.
     */
    private static void close(InputStream streamOrNull)
    {
        if (streamOrNull != null)
        {
            try
            {
                streamOrNull.close();
            } catch (IOException ioex)
            {
                // ignore
            }
        }
    }
}

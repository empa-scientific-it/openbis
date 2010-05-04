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

package ch.systemsx.cisd.common.io;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Special <code>InputStream</code> that will concatenate the contents of an array of files into one
 * stream. Content of each file will be preceded by one long which tells what is the size of the
 * file in bytes.<BR>
 * 
 * @author Tomasz Pylak
 */
public class ConcatFileInputStream extends InputStream
{
    private static final int EOF = -1;

    private int currentIndex = -1;

    private boolean eof = false;

    private File[] files;

    // If true then currentStream is not the content of the file, but a short stream
    // which encodes one long number which describes file size.
    // This stream is created every time before a new file content is appended to the stream.
    private boolean readingFileSize;

    private InputStream currentStream;

    /**
     * @files content of these files will be concatenated into one stream.
     */
    public ConcatFileInputStream(File... files)
    {
        this.files = files;
        this.readingFileSize = false;
    }

    /**
     * @files content of these files will be concatenated into one stream.
     */
    public ConcatFileInputStream(List<File> files)
    {
        this(files.toArray(new File[files.size()]));
    }

    @Override
    public void close() throws IOException
    {
        closeCurrentStream();
        eof = true;
    }

    @Override
    public int read() throws IOException
    {
        int result = readCurrent();
        // we have a loop instead of an if to ignore files which are empty
        while (result == EOF && !eof)
        {
            closeCurrentStream();
            if (readingFileSize)
            {
                currentStream = createFileStream(getCurrentFile());
                readingFileSize = false;
            } else
            {
                File nextFile = tryGetNextFile();
                if (nextFile == null)
                {
                    eof = true;
                    return EOF;
                }
                currentStream = createFileSizeStream(nextFile);
                readingFileSize = true;
            }
            result = currentStream.read();
        }
        return result;
    }

    private File getCurrentFile()
    {
        return files[currentIndex];
    }

    private int readCurrent() throws IOException
    {
        return (eof || currentStream == null) ? EOF : currentStream.read();
    }

    // returns the next file to read
    private File tryGetNextFile() throws IOException
    {
        currentIndex++;
        if (files != null && currentIndex < files.length)
        {
            return getCurrentFile();
        } else
        {
            return null;
        }
    }

    private void closeCurrentStream()
    {
        close(currentStream);
        currentStream = null;
    }

    // -------------- static helper ---------------

    private static InputStream createFileStream(File currentFile) throws FileNotFoundException
    {
        return new BufferedInputStream(new FileInputStream(currentFile));
    }

    private static InputStream createFileSizeStream(File file) throws IOException
    {
        long fileSize = file.length();
        byte[] data = longToBytes(fileSize);
        return new ByteArrayInputStream(data);
    }

    private static byte[] longToBytes(long fileSize) throws IOException
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeLong(fileSize);
        dos.flush();
        byte[] data = bos.toByteArray();
        dos.close();
        return data;
    }

    /**
     * Close a stream without throwing any exception if something went wrong. Do not attempt to
     * close it if the argument is null.
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

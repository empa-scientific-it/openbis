/*
 * Copyright 2008 ETH Zuerich, CISD
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

import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;

/**
 * A {@link NamedReader} extension which delegates calls to the internal {@link Reader} and defines a reader name.
 * 
 * @author Christian Ribeaud
 */
public final class DelegatedReader extends NamedReader
{
    private final Reader reader;

    private final String readerName;

    public DelegatedReader(final Reader reader, final String readerName)
    {
        assert reader != null : "Unspecified reader.";
        assert readerName != null : "Unspecified reader name.";
        assert reader instanceof NamedReader == false : "Can not be an instance of NamedReader.";
        this.reader = reader;
        this.readerName = readerName;
    }

    //
    // NamedReader
    //

    @Override
    public final void close() throws IOException
    {
        reader.close();
    }

    @Override
    public final void mark(final int readAheadLimit) throws IOException
    {
        reader.mark(readAheadLimit);
    }

    @Override
    public final boolean markSupported()
    {
        return reader.markSupported();
    }

    @Override
    public final int read() throws IOException
    {
        return reader.read();
    }

    @Override
    public final int read(final char[] cbuf, final int off, final int len) throws IOException
    {
        return reader.read(cbuf, off, len);
    }

    @Override
    public final int read(final char[] cbuf) throws IOException
    {
        return reader.read(cbuf);
    }

    @Override
    public final int read(final CharBuffer target) throws IOException
    {
        return reader.read(target);
    }

    @Override
    public final boolean ready() throws IOException
    {
        return reader.ready();
    }

    @Override
    public final void reset() throws IOException
    {
        reader.reset();
    }

    @Override
    public final long skip(final long n) throws IOException
    {
        return reader.skip(n);
    }

    @Override
    public final String getReaderName()
    {
        return readerName;
    }

    @Override
    public final boolean equals(final Object obj)
    {
        return reader.equals(obj);
    }

    @Override
    public final int hashCode()
    {
        return reader.hashCode();
    }

    @Override
    public final String toString()
    {
        return reader.toString();
    }

}

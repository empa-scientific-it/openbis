/*
 * Copyright 2013 ETH Zuerich, CISD
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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A stream decorator that always returns a requested number of bytes even though the decorated stream may return this information in smaller chunks
 * (i.e. read method always reads as many bytes as were specified by the length parameter unless the end of the stream is reached).
 * 
 * @author pkupczyk
 */
public class FullLengthReadingStream extends FilterInputStream
{

    public FullLengthReadingStream(InputStream in)
    {
        super(in);
    }

    @Override
    public int read(byte[] b, int offset, int length) throws IOException
    {
        int read = 0;

        while (read < length)
        {
            int count = in.read(b, offset + read, length - read);

            if (count < 0)
            {
                return read > 0 ? read : -1;
            } else
            {
                read += count;
            }
        }

        return read;
    }

}

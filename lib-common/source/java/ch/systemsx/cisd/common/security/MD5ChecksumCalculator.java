/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.security;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;

import org.apache.commons.io.IOUtils;

import com.twmacinta.util.MD5;
import com.twmacinta.util.MD5InputStream;

/**
 * A {@link IChecksumCalculator} implementation based on <i>MD5</i>.
 * 
 * @author Christian Ribeaud
 */
public final class MD5ChecksumCalculator implements IChecksumCalculator
{
    @Override
    public String calculateChecksum(InputStream inputStream) throws IOException
    {
        return doCalculation(inputStream, 4096);
    }

    private static String calculate(InputStream inputStream, int bufferSize)
    {
        try
        {
            return doCalculation(inputStream, bufferSize);
        } catch (IOException ex)
        {
            throw new IllegalStateException("This should not happen: " + ex.getMessage());
        }
    }

    static String doCalculation(InputStream inputStream, int bufferSize) throws IOException
    {
        byte[] buf = new byte[bufferSize];
        MD5InputStream in = new MD5InputStream(inputStream);
        try
        {
            while (in.read(buf) != -1)
            {
            }
            return MD5.asHex(in.hash());
        } finally
        {
            in.close();
        }
    }

    /** Calculates a checksum for specified byte array. */
    public static String calculate(byte[] bytes)
    {
        assert bytes != null : "Unspecified byte array.";
        return calculate(new ByteArrayInputStream(bytes), bytes.length);
    }

    /** Calculates a checksum for specified stream. */
    public static String calculate(InputStream stream) throws IOException
    {
        MD5InputStream md5InputStream = new MD5InputStream(stream);
        IOUtils.copyLarge(md5InputStream, new OutputStream()
            {
                @Override
                public void write(int b) throws IOException
                {
                    // ignore
                }
            });
        return MD5.asHex(md5InputStream.hash());
    }

    /** Calculates a checksum for a given String */
    public static String calculate(String value)
    {
        assert value != null && value.length() > 0 : "Value cannot be blank.";

        final StringReader reader = new StringReader(value);
        InputStream inputStream = new InputStream()
            {
                @Override
                public int read() throws IOException
                {
                    return reader.read();
                }

            };
        return calculate(inputStream, value.length());
    }
}
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

package ch.systemsx.cisd.common.string;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;

/**
 * Utilities for dealing with file encoding.
 * 
 * @author Piotr Buczek
 */
public final class UnicodeUtils
{

    public static final String UTF_8 = "UTF-8";

    public static final String UTF_16 = "UTF-16";

    public static final String UTF_32 = "UTF-32";

    /** default Unicode encoding used for strings - UTF-8 */
    public static final String DEFAULT_UNICODE_CHARSET = UTF_8;

    private static final Map<String, byte[]> supportedEncodings = new HashMap<String, byte[]>();
    static
    {
        supportedEncodings.put("UTF-8", new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF });
        supportedEncodings.put("UTF-16 LE", new byte[] { (byte) 0xFF, (byte) 0xFE });
        supportedEncodings.put("UTF-16 BE", new byte[] { (byte) 0xFE, (byte) 0xFF });
        supportedEncodings.put("UTF-32 LE", new byte[] { (byte) 0xFE, (byte) 0xFF, (byte) 0x00, (byte) 0x00 });
        supportedEncodings.put("UTF-32 BE", new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0xFE, (byte) 0xFF });
    }

    private UnicodeUtils()
    {
        // Can not be instantiated.
    }

    /** @returns {@link Charset} for {@link #DEFAULT_UNICODE_CHARSET} */
    public final static Charset getDefaultUnicodeCharset()
    {
        return Charset.forName(UnicodeUtils.DEFAULT_UNICODE_CHARSET);
    }

    /** @return A valid supported encoding or null */
    private final static String getEncoding(BufferedInputStream bufferedInputStream)
    {
        try
        {
            if (bufferedInputStream.markSupported())
            {
                for (String encoding : supportedEncodings.keySet())
                {
                    byte[] BOM = supportedEncodings.get(encoding);
                    byte[] BOMTest = new byte[BOM.length];
                    bufferedInputStream.mark(BOM.length);
                    bufferedInputStream.read(BOMTest, 0, BOM.length);
                    bufferedInputStream.reset();
                    if (Arrays.equals(BOM, BOMTest))
                    {
                        String javaEncoding = encoding.split(" ")[0];
                        return javaEncoding;
                    }
                }
            }
        } catch (Exception ex)
        {

        }

        return null;
    }

    /**
     * @return {@link Reader} that uses the default Unicode encoding.
     * @throws EnvironmentFailureException if the encoding is not supported (shouldn't happen).
     * @see #DEFAULT_UNICODE_CHARSET #UTF_8 #UTF_16 #UTF_32
     */
    public final static Reader createReader(InputStream inputStream)
            throws EnvironmentFailureException
    {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        String encoding = getEncoding(bufferedInputStream);
        if (encoding == null)
        {
            encoding = DEFAULT_UNICODE_CHARSET;
        }

        return createReader(bufferedInputStream, encoding);
    }

    /**
     * @return {@link Reader} that uses the given encoding.
     * @throws EnvironmentFailureException if the encoding is not supported.
     */
    public final static Reader createReader(InputStream inputStream, String encoding)
            throws EnvironmentFailureException
    {
        try
        {
            return new InputStreamReader(inputStream, encoding);
        } catch (UnsupportedEncodingException ex)
        {
            throw new EnvironmentFailureException(ex.getMessage());
        }
    }
}

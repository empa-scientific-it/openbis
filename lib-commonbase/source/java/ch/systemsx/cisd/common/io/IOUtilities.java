/*
 * Copyright 2012 ETH Zuerich, CISD
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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.Checksum;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;

/**
 * Useful utility methods.
 *
 * @author Franz-Josef Elmer
 */
public class IOUtilities
{
    private static final char[] HEX_CHARACTERS =
    { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', };

    /**
     * Calculates the CRC32 checksum of specified input stream. Note, that the input stream is closed after invocation of this method.
     */
    
    public static int getChecksumCRC32(InputStream inputStream)
    {
        return copyAndGetChecksumCRC32(inputStream, new NullOutputStream());
    }

    /**
     * Copies the input stream into the output stream while calculating the CRC32 checksum. Note also that, the input and output streams are closed
     * after the invocation of this method.
     */
    public static int copyAndGetChecksumCRC32(InputStream inputStream, OutputStream out)
    {
        Checksum checksummer = new CRC32();
        InputStream in = new CheckedInputStream(inputStream, checksummer);
        try
        {
            IOUtils.copy(in, out);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }
        return (int) checksummer.getValue();
    }
    
    /**
     * Converts a CRC32 checksum to a string representation.
     */
    public static String crc32ToString(final int checksum)
    {
        final char buf[] = new char[8];
        int w = checksum;
        for (int i = 0, x = 7; i < 4; i++)
        {
            buf[x--] = HEX_CHARACTERS[w & 0xf];
            buf[x--] = HEX_CHARACTERS[(w >>> 4) & 0xf];
            w >>= 8;
        }
        return new String(buf);
    }

}

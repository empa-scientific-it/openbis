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

package ch.ethz.sis.filetransfer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.CRC32;

/**
 * @author Franz-Josef Elmer
 */
class TestInputStream extends ByteArrayInputStream
{
    private boolean closed;

    private long checksum;

    private String content;

    TestInputStream(String content)
    {
        super(content.getBytes());
        this.content = content;
        CRC32 crc32 = new CRC32();
        crc32.update(content.getBytes());
        checksum = crc32.getValue();
    }

    String getContent()
    {
        return content;
    }

    long getChecksum()
    {
        return checksum;
    }

    boolean isClosed()
    {
        return closed;
    }

    @Override
    public void close() throws IOException
    {
        closed = true;
    }

}

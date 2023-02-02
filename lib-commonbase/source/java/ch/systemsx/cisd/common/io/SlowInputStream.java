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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.time.DateUtils;

/**
 * A <code>ProxyInputStream</code> extension which simulates a slow input stream reading.
 * 
 * @author Christian Ribeaud
 */
public final class SlowInputStream extends FilterInputStream
{
    private final long speed;

    /**
     * @param speed bytes per second to simulate.
     */
    public SlowInputStream(final InputStream inputStream, final long speed)
    {
        super(inputStream);
        this.speed = speed;
    }

    /**
     * Pause for an appropriate time according to the number of bytes being transferred.
     * 
     * @param bytes number of bytes being transferred.
     */
    private final void pause(final int bytes)
    {
        final long sleepMillis = (bytes * DateUtils.MILLIS_PER_SECOND) / speed;
        try
        {
            Thread.sleep(sleepMillis);
        } catch (final InterruptedException ignored)
        {
        }
    }

    //
    // ProxyInputStream
    //

    @Override
    public final int read() throws IOException
    {
        pause(1);
        return in.read();
    }

    // Also handles read(byte[])
    @Override
    public final int read(final byte[] b, final int off, final int len) throws IOException
    {
        pause(len);
        return in.read(b, off, len);
    }
}

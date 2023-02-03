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

import java.io.IOException;
import java.io.InputStream;

/**
 * Abstract sub class of {@link InputStream} which implements {@link InputStream#read()} but declares {@link InputStream#read(byte[], int, int)}
 * abstract.
 * 
 * @author Franz-Josef Elmer
 */
public abstract class AbstractBulkInputStream extends InputStream
{

    @Override
    public int read() throws IOException
    {
        byte[] onebyte = new byte[1];
        int n = read(onebyte);
        return n < 0 ? -1 : onebyte[0] & 0xff;
    }

    public abstract int read(byte[] b, int off, int len) throws IOException;

}

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

import java.io.IOException;
import java.io.InputStream;

/**
 * Implementations know how to compute a <a href="http://en.wikipedia.org/wiki/Checksum">checksum</a> from a file.
 * 
 * @author Christian Ribeaud
 */
public interface IChecksumCalculator
{

    /**
     * Returns the checksum of the bytes read from the specified input stream.
     * 
     * @param inputStream Input stream from whom the bytes are read to calculate checksum.
     * @throws IOException if reading from <code>inputStream</code> causes an <code>IOException</code>.
     */
    String calculateChecksum(InputStream inputStream) throws IOException;
}

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

package ch.systemsx.cisd.openbis.generic.shared.parser;

import java.io.InputStream;
import java.io.Reader;

import ch.systemsx.cisd.common.string.UnicodeUtils;

/**
 * @author Izabela Adamczyk
 * @author Piotr Buczek
 */
public class NamedInputStream
{

    private final InputStream stream;

    private final String originalName;

    public NamedInputStream(InputStream stream, String originalName)
    {
        this.stream = stream;
        this.originalName = originalName;
    }

    /**
     * NOTE: use {@link #getUnicodeReader()} to get Unicode characters.
     */
    public InputStream getInputStream()
    {
        return stream;
    }

    public String getOriginalFilename()
    {
        return originalName;
    }

    /**
     * @return Reader of unicode characters from this input stream
     *         <p>
     *         Use {@link #getInputStream()} instead to get raw input stream.
     */
    public Reader getUnicodeReader()
    {
        return UnicodeUtils.createReader(stream);
    }
}

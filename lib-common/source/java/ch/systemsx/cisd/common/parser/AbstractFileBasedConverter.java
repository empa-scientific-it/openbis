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

package ch.systemsx.cisd.common.parser;

import java.io.File;

import org.apache.commons.io.FilenameUtils;

import ch.systemsx.cisd.common.converter.Converter;

/**
 * A <code>Converter</code> implementation which converts a file into appropriate bean.
 * 
 * @author Franz-Josef Elmer
 */
public abstract class AbstractFileBasedConverter<T> implements Converter<T>
{
    private final File rootDir;

    public AbstractFileBasedConverter(final File rootDir)
    {
        this.rootDir = rootDir;
    }

    public static final boolean isIncludeFile(final String valueOrNull)
    {
        if (valueOrNull == null)
        {
            return false;
        }
        final String trimmed = valueOrNull.trim();
        return trimmed.startsWith(">") && trimmed.length() > 1;
    }

    protected abstract T convert(final File file);

    //
    // Converter
    //

    @Override
    public final T convert(final String valueOrNull)
    {
        if (isIncludeFile(valueOrNull))
        {
            final String fileName = valueOrNull.substring(1).trim();
            final int prefixLength = FilenameUtils.getPrefixLength(fileName);
            return convert(prefixLength == 0 ? new File(rootDir, fileName) : new File(fileName));
        }
        return null;
    }
}
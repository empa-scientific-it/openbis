/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.filesystem;

import java.io.File;

import ch.systemsx.cisd.common.exceptions.StatusWithResult;
import ch.systemsx.cisd.common.exceptions.UnknownLastChangedException;

/**
 * @author Tomasz Pylak
 */
public class LastModificationChecker implements ILastModificationChecker
{
    private final File parentDir;

    public LastModificationChecker(File parentDir)
    {
        this.parentDir = parentDir;
    }

    @Override
    public final StatusWithResult<Long> lastChanged(final StoreItem item,
            final long stopWhenFindYounger)
    {
        try
        {
            long lastChanged =
                    FileUtilities.lastChanged(getChildFile(item), true, stopWhenFindYounger);
            return StatusWithResult.<Long> create(lastChanged);
        } catch (UnknownLastChangedException ex)
        {
            return createLastChangedError(item, ex);
        }
    }

    @Override
    public final StatusWithResult<Long> lastChangedRelative(final StoreItem item,
            final long stopWhenFindYoungerRelative)
    {
        try
        {
            long lastChanged =
                    FileUtilities.lastChangedRelative(getChildFile(item), true,
                            stopWhenFindYoungerRelative);
            return StatusWithResult.<Long> create(lastChanged);
        } catch (UnknownLastChangedException ex)
        {
            return createLastChangedError(item, ex);
        }
    }

    private static StatusWithResult<Long> createLastChangedError(final StoreItem item,
            UnknownLastChangedException ex)
    {
        String errorMsg =
                String.format("Could not determine \"last changed time\" of '%s'.", item);
        return StatusWithResult.<Long> createErrorWithResult(errorMsg);
    }

    protected final File getChildFile(final StoreItem item)
    {
        return new File(parentDir, item.getName());
    }

}

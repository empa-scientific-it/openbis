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

import ch.systemsx.cisd.common.exceptions.StatusWithResult;

public interface ILastModificationChecker
{
    /**
     * Returns the last time when there was a write access to <var>item</var>.
     * 
     * @param item The {@link StoreItem} to check.
     * @param stopWhenFindYounger The time measured from the beginning of the epoch. If &gt; 0, the recursive search for younger file will be stopped
     *            when a file or directory is found that is younger than the time specified in this parameter. Supposed to be used when one does not
     *            care about the absolutely youngest entry, but only, if there are entries that are "young enough".
     * @return The time (in milliseconds since the start of the epoch) when <var>resource</var> was last changed or error status if checking failed.
     */
    public StatusWithResult<Long> lastChanged(StoreItem item, long stopWhenFindYounger);

    /**
     * Returns the last time when there was a write access to <var>item</var>.
     * 
     * @param item The {@link StoreItem} to check.
     * @param stopWhenFindYoungerRelative The age of the item. If &gt; 0, the recursive search for younger file will be stopped when a file or
     *            directory is found that is younger than the specified age (in other words is smaller than
     *            <code>System.currentTimeMillis() - stopWhenYoungerRelative</code>).
     * @return The time (in milliseconds since the start of the epoch) when <var>resource</var> was last changed or error status if checking failed.
     */
    public StatusWithResult<Long> lastChangedRelative(StoreItem item,
            long stopWhenFindYoungerRelative);

}
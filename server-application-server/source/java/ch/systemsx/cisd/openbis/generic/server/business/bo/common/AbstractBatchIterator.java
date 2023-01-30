/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo.common;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.Iterator;

import ch.systemsx.cisd.common.exceptions.NotImplementedException;

/**
 * Allows to convert an memory-inefficient iterator into a one which has lower memory consumption. Divides the items used to produce iterated elements
 * into batches and calls the original iterator on each batch. The outside interface make the division into batches invisible, but the inefficient
 * iterator is never used with a big number of items.
 * 
 * @author Tomasz Pylak
 */
abstract public class AbstractBatchIterator<T> implements Iterable<T>
{
    /**
     * Creates an iterator which is not very memory efficient and should not be called with too many items at the same time.
     */
    abstract protected Iterable<T> createUnefficientIterator(LongSet itemsBatch);

    private final LongSet items;

    protected final int chunkSize;

    protected AbstractBatchIterator(LongSet items, int chunkSize)
    {
        this.chunkSize = chunkSize;
        this.items = items;
    }

    @Override
    public Iterator<T> iterator()
    {
        final Iterator<Long> unprocessedItems = items.iterator();
        return new Iterator<T>()
            {
                private Iterator<T> fetchedResults = null;

                @Override
                public boolean hasNext()
                {
                    return fetchNextPortionIfNeeded();
                }

                @Override
                public T next()
                {
                    fetchNextPortionIfNeeded();
                    return fetchedResults.next();
                }

                private boolean fetchNextPortionIfNeeded()
                {
                    // Special care was taken here to ensure that fetchedResults.hasNext() is not
                    // called again if it returns false. Some implementations do not like that.
                    while (true)
                    {
                        boolean fetchedHasNext =
                                (fetchedResults != null && fetchedResults.hasNext());
                        // there is a next element or we have processed everything
                        if (fetchedHasNext || unprocessedItems.hasNext() == false)
                        {
                            return fetchedHasNext;
                        }

                        assert fetchedHasNext == false && unprocessedItems.hasNext();
                        final LongSet nextPortion = new LongOpenHashSet();
                        while (unprocessedItems.hasNext() && nextPortion.size() < chunkSize)
                        {
                            Long next = unprocessedItems.next();
                            nextPortion.add(next);
                        }
                        fetchedResults = createUnefficientIterator(nextPortion).iterator();
                    }
                }

                @Override
                public void remove()
                {
                    throw new NotImplementedException();
                }
            };
    }
}

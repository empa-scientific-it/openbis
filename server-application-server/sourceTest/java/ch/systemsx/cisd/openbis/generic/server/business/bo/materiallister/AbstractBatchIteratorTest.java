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

package ch.systemsx.cisd.openbis.generic.server.business.bo.materiallister;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.Collections;
import java.util.Iterator;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.business.bo.common.AbstractBatchIterator;

/**
 * @author Tomasz Pylak
 */
public class AbstractBatchIteratorTest extends AssertJUnit
{
    private static class TestPartialIterator extends AbstractBatchIterator<Long>
    {
        protected TestPartialIterator(final LongSet ids, int chunkSize)
        {
            super(ids, chunkSize);
        }

        @Override
        protected Iterable<Long> createUnefficientIterator(final LongSet ids)
        {
            if (ids.size() > chunkSize)
            {
                fail(String.format(
                        "Chunk of the wrong size requested, expected at most %d, was %d",
                        chunkSize, ids.size()));
            }
            return new Iterable<Long>()
                {
                    @Override
                    public Iterator<Long> iterator()
                    {
                        return ids.iterator();
                    }
                };
        }
    }

    @Test
    public void test()
    {
        LongSet request = createRequest();
        TestPartialIterator iter = new TestPartialIterator(request, 3);

        Iterator<Long> efficientIter = iter.iterator();
        LongSet result = new LongOpenHashSet();
        while (efficientIter.hasNext())
        {
            result.add(efficientIter.next());
        }
        assertEquals(request, result);
    }

    @Test
    public void testEmpty()
    {
        LongSet request = createRequest();
        AbstractBatchIterator<Long> iter = new AbstractBatchIterator<Long>(request, 3)
            {
                @Override
                protected Iterable<Long> createUnefficientIterator(LongSet ids)
                {
                    return Collections.emptyList();
                }
            };

        Iterator<Long> efficientIter = iter.iterator();
        assertFalse(efficientIter.hasNext());
    }

    private LongSet createRequest()
    {
        LongSet request = new LongOpenHashSet();
        for (int i = 0; i < 100; i++)
        {
            request.add(i * 100);
        }
        return request;
    }

}

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

package ch.systemsx.cisd.common.collection;

import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import ch.systemsx.cisd.common.collection.IExtendedBlockingQueue;

/**
 * A {@link IExtendedBlockingQueue} implementation based on {@link LinkedBlockingQueue}.
 *
 * @author Bernd Rinn
 */
public class ExtendedLinkedBlockingQueue<E> extends LinkedBlockingQueue<E> implements
        IExtendedBlockingQueue<E>
{

    public ExtendedLinkedBlockingQueue()
    {
        super();
    }

    public ExtendedLinkedBlockingQueue(Collection<? extends E> c)
    {
        super(c);
    }

    public ExtendedLinkedBlockingQueue(int capacity)
    {
        super(capacity);
    }

    private static final long serialVersionUID = -6903933977591709194L;

    private static final long PEEK_INTERVAL_MILLIS = 100L;

    @Override
    public E peekWait() throws InterruptedException
    {
        while (true)
        {
            final E item = peek();
            if (item != null)
            {
                return item;
            }
            Thread.sleep(PEEK_INTERVAL_MILLIS);
        }
    }

    @Override
    public E peekWait(long timeout, TimeUnit unit) throws InterruptedException
    {
        final long timeoutMillis = TimeUnit.MILLISECONDS.convert(timeout, unit);
        final long maxCount = Math.max(1, timeoutMillis / PEEK_INTERVAL_MILLIS);
        for (int i = 0; i < maxCount; ++i)
        {
            final E item = peek();
            if (item != null)
            {
                return item;
            }
            Thread.sleep(PEEK_INTERVAL_MILLIS);
        }
        return null;
    }

}

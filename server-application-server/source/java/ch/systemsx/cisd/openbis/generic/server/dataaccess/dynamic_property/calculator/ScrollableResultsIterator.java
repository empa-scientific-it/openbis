/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator;

import java.util.Iterator;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.ScrollableResults;

import ch.systemsx.cisd.common.reflection.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.common.resource.IReleasable;
import ch.systemsx.cisd.common.resource.ReleasableIterator;
import ch.systemsx.cisd.common.resource.Resources;

/**
 * @author anttil
 */
public abstract class ScrollableResultsIterator<T> implements Iterable<T>, IReleasable
{

    private final ScrollableResults scroll;

    private ReleasableIterator<T> releasableIterator;

    public ScrollableResultsIterator(ScrollableResults scroll)
    {
        this.scroll = scroll;
    }

    public abstract T parseValue(Object[] result);

    @Override
    public Iterator<T> iterator()
    {
        if (releasableIterator != null)
        {
            return releasableIterator;
        }
        releasableIterator = new ReleasableIterator<T>(new Iterator<T>()
            {
                Object[] current = null;

                @Override
                public boolean hasNext()
                {
                    if (current != null)
                    {
                        return true;
                    }

                    if (scroll.next())
                    {
                        current = scroll.get();
                        return true;
                    } else
                    {
                        return false;
                    }

                }

                @Override
                public T next()
                {
                    if (current == null)
                    {
                        if (scroll.next() == false)
                        {
                            return null;
                        }
                        current = scroll.get();
                    }

                    Object[] o = current;
                    current = null;
                    return parseValue(o);
                }

                @Override
                public void remove()
                {
                    throw new UnsupportedOperationException();
                }
            });
        return releasableIterator;
    }

    @Override
    public void release()
    {
        Resources resources = new Resources();

        if (scroll != null)
        {
            resources.add(new ScrollableResultReleasable(scroll));
        }

        if (releasableIterator != null)
        {
            resources.add(releasableIterator);
        }

        resources.release();
    }

    private static class ScrollableResultReleasable implements IReleasable
    {

        private ScrollableResults results;

        public ScrollableResultReleasable(ScrollableResults results)
        {
            this.results = results;
        }

        @Override
        public void release()
        {
            results.close();
        }

        @Override
        public String toString()
        {
            final ToStringBuilder builder =
                    new ToStringBuilder(this,
                            ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
            return builder.toString();
        }

    }

}

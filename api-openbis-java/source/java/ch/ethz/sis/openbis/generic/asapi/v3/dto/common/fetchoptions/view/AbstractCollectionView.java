/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.view;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author pkupczyk
 */
public abstract class AbstractCollectionView<T> implements Collection<T>, Serializable
{

    private static final long serialVersionUID = 1L;

    private transient Collection<T> originalCollection;

    private Collection<T> limitedCollection;

    public AbstractCollectionView(Collection<T> originalCollection, Integer from, Integer count)
    {
        this.originalCollection = originalCollection;
        this.limitedCollection = createLimited(originalCollection, from, count);
    }

    @SuppressWarnings("hiding")
    protected abstract Collection<T> createLimited(Collection<T> originalCollection, Integer fromOrNull, Integer countOrNull);

    protected static <T> void copyItems(Collection<T> fromCollection, Collection<T> toCollection, Integer fromOrNull, Integer countOrNull)
    {
        Integer from = fromOrNull;
        Integer count = countOrNull;

        if (from == null)
        {
            from = 0;
        }

        if (count == null)
        {
            count = fromCollection.size();
        }

        int index = 0;
        for (T item : fromCollection)
        {
            if (index >= from && index < from + count)
            {
                toCollection.add(item);
            }
            index++;
        }
    }

    @Override
    public int size()
    {
        return limitedCollection.size();
    }

    @Override
    public boolean isEmpty()
    {
        return limitedCollection.isEmpty();
    }

    @Override
    public boolean contains(Object o)
    {
        return limitedCollection.contains(o);
    }

    @Override
    public Iterator<T> iterator()
    {
        return limitedCollection.iterator();
    }

    @Override
    public Object[] toArray()
    {
        return limitedCollection.toArray();
    }

    @Override
    public <R> R[] toArray(R[] a)
    {
        return limitedCollection.toArray(a);
    }

    @Override
    public boolean add(T e)
    {
        return limitedCollection.add(e);
    }

    @Override
    public boolean remove(Object o)
    {
        return limitedCollection.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c)
    {
        return limitedCollection.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c)
    {
        return limitedCollection.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c)
    {
        return limitedCollection.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c)
    {
        return limitedCollection.retainAll(c);
    }

    @Override
    public void clear()
    {
        limitedCollection.clear();
    }

    @Override
    public boolean equals(Object o)
    {
        return limitedCollection.equals(o);
    }

    @Override
    public int hashCode()
    {
        return limitedCollection.hashCode();
    }

    public Collection<T> getLimitedCollection()
    {
        return limitedCollection;
    }

    @Override
    public String toString()
    {
        return limitedCollection.toString();
    }

    public Collection<T> getOriginalCollection()
    {
        return originalCollection;
    }

}

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
import java.util.Collections;
import java.util.Iterator;

/**
 * A decorator for a {@link Collection} that does the same as {@link Collections#unmodifiableCollection(Collection)} but with a 'back-door'.
 * 
 * @author Bernd Rinn
 */
public class UnmodifiableCollectionDecorator<E> implements Collection<E>
{
    private final Collection<E> collection;

    public UnmodifiableCollectionDecorator(Collection<E> collection)
    {
        this.collection = collection;
    }

    /**
     * Returns the decorated (wrapped) collection.
     */
    public Collection<E> getDecorated()
    {
        return collection;
    }

    @Override
    public boolean contains(Object o)
    {
        return collection.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c)
    {
        return collection.containsAll(c);
    }

    @Override
    public boolean equals(Object o)
    {
        return collection.equals(o);
    }

    @Override
    public int hashCode()
    {
        return collection.hashCode();
    }

    @Override
    public String toString()
    {
        return collection.toString();
    }

    @Override
    public boolean isEmpty()
    {
        return collection.isEmpty();
    }

    @Override
    public int size()
    {
        return collection.size();
    }

    @Override
    public Object[] toArray()
    {
        return collection.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a)
    {
        return collection.toArray(a);
    }

    @Override
    public Iterator<E> iterator()
    {
        return new Iterator<E>()
            {
                Iterator<E> i = collection.iterator();

                @Override
                public boolean hasNext()
                {
                    return i.hasNext();
                }

                @Override
                public E next()
                {
                    return i.next();
                }

                @Override
                public void remove()
                {
                    throw new UnsupportedOperationException();
                }
            };
    }

    //
    // Not supported
    //

    @Override
    public boolean add(E o)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends E> c)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c)
    {
        throw new UnsupportedOperationException();
    }

}

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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;

import ch.systemsx.cisd.common.collection.IKeyExtractor;
import ch.systemsx.cisd.common.collection.IMultiKeyExtractor;

/**
 * A table of rows of type <code>E</code> with random access via a key of type <code>K</code> where the key does not have to be unique.
 * <p>
 * Note that the <i>values</i> still need to be unique (according to the {@link Object#equals(Object)} contract), only duplicate <i>keys</i> are
 * acceptable for this map.
 * 
 * @author Franz-Josef Elmer
 * @author Bernd Rinn
 */
public class TableMapNonUniqueKey<K, E> implements Iterable<E>
{
    /** Strategy on how to handle unique value constraint violations. */
    public enum UniqueValueViolationStrategy
    {
        KEEP_FIRST, KEEP_LAST, ERROR
    }

    /**
     * Exception indicating a violation of the unique value constraint.
     */
    public static class UniqueValueViolationException extends RuntimeException
    {
        private static final long serialVersionUID = 1L;

        UniqueValueViolationException(String msg)
        {
            super(msg);
        }
    }

    private final Map<K, Set<E>> map = new LinkedHashMap<K, Set<E>>();

    private final IMultiKeyExtractor<K, E> extractor;

    private final UniqueValueViolationStrategy uniqueValueViolationStrategy;

    /**
     * Creates a new instance for the specified rows and key extractor.
     * 
     * @param rows Collection of rows of type <code>E</code>.
     * @param extractor Strategy to extract a key of type <code>E</code> for an object of type <code>E</code>.
     */
    public TableMapNonUniqueKey(final Iterable<E> rows, final IKeyExtractor<K, E> extractor)
    {
        this(rows, extractor, UniqueValueViolationStrategy.ERROR);
    }

    /**
     * Creates a new instance for the specified rows and key extractor.
     * 
     * @param rows Collection of rows of type <code>E</code>.
     * @param extractor Strategy to extract a key of type <code>E</code> for an object of type <code>E</code>.
     */
    public TableMapNonUniqueKey(final Iterable<E> rows, final IMultiKeyExtractor<K, E> extractor)
    {
        this(rows, extractor, UniqueValueViolationStrategy.ERROR);
    }

    /**
     * Creates a new instance for the specified rows and key extractor.
     * 
     * @param rows Collection of rows of type <code>E</code>.
     * @param extractor Strategy to extract a key of type <code>E</code> for an object of type <code>E</code>.
     */
    public TableMapNonUniqueKey(final Iterable<E> rows, final IKeyExtractor<K, E> extractor,
            UniqueValueViolationStrategy uniqueValueViolationStrategy)
    {
        assert rows != null : "Unspecified collection of rows.";
        assert extractor != null : "Unspecified key extractor.";
        assert uniqueValueViolationStrategy != null : "Unspecified unique value violation strategy.";
        this.extractor = new IMultiKeyExtractor<K, E>()
            {
                @Override
                public Collection<K> getKey(E e)
                {
                    return Collections.singleton(extractor.getKey(e));
                }
            };
        this.uniqueValueViolationStrategy = uniqueValueViolationStrategy;
        for (final E row : rows)
        {
            add(row);
        }
    }

    /**
     * Creates a new instance for the specified rows and key extractor.
     * 
     * @param rows Collection of rows of type <code>E</code>.
     * @param extractor Strategy to extract a key of type <code>E</code> for an object of type <code>E</code>.
     */
    public TableMapNonUniqueKey(final Iterable<E> rows, final IMultiKeyExtractor<K, E> extractor,
            UniqueValueViolationStrategy uniqueValueViolationStrategy)
    {
        assert rows != null : "Unspecified collection of rows.";
        assert extractor != null : "Unspecified key extractor.";
        assert uniqueValueViolationStrategy != null : "Unspecified unique value violation strategy.";
        this.extractor = extractor;
        this.uniqueValueViolationStrategy = uniqueValueViolationStrategy;
        for (final E row : rows)
        {
            add(row);
        }
    }

    /**
     * Adds the specified row to this table. What the method will do when a row is provided that is equals to a row that is already in the map
     * (according to {@link Object#equals(Object)}, depends on the unique value violation strategy as given to the constructor:
     * <ul>
     * <li>For {@link UniqueValueViolationStrategy#KEEP_FIRST} the first inserted row will be kept and all later ones will be ignored.</li>
     * <li>For {@link UniqueValueViolationStrategy#KEEP_LAST} the last inserted row will replace all the others.</li>
     * <li>For {@link UniqueValueViolationStrategy#ERROR} a {@link UniqueValueViolationException} will be thrown when trying to insert a row with a
     * key that is already in the map. <i>This is the default.</i>.</li>
     * </ul>
     * 
     * @throws UniqueValueViolationException If a row that equals the <var>row</var> is already in the map and a unique value violation strategy of
     *             {@link UniqueValueViolationStrategy#ERROR} has been chosen.
     */
    public final void add(final E row) throws UniqueValueViolationException
    {
        final Collection<K> keys = extractor.getKey(row);
        for (K key : keys)
        {
            Set<E> set = map.get(key);
            if (set == null)
            {
                set = new LinkedHashSet<E>();
                map.put(key, set);
                set.add(row);
            } else if (uniqueValueViolationStrategy == UniqueValueViolationStrategy.KEEP_FIRST
                    || set.contains(row) == false)
            {
                set.add(row);
            } else if (uniqueValueViolationStrategy == UniqueValueViolationStrategy.KEEP_LAST)
            {
                set.remove(row);
                set.add(row);
            } else if (uniqueValueViolationStrategy == UniqueValueViolationStrategy.ERROR)
            {
                throw new UniqueValueViolationException("Row '" + row.toString()
                        + "' already stored in the map.");
            }
        }
    }

    /**
     * Gets the row set for the specified <var>key</var>.
     * 
     * @return The set, given in the order of addition, or <code>null</code> if the <var>key</var> is not found.
     */
    public final Set<E> tryGet(final K key)
    {
        return map.get(key);
    }

    //
    // Object
    //

    @Override
    public final String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }

    /**
     * Creates an iterator of the rows. Removing is not supported.
     * <p>
     * The order is:
     * <ol>
     * <li>Order of addition of the key</li>
     * <li>Order of the addition of the value for the value's key</li>
     * </ol>
     */
    @Override
    public final Iterator<E> iterator()
    {
        return new Iterator<E>()
            {
                private Iterator<Map.Entry<K, Set<E>>> mapSetIterator = map.entrySet().iterator();

                private Iterator<E> setIterator;

                private boolean setHasNext()
                {
                    return (setIterator != null) && setIterator.hasNext();
                }

                @Override
                public boolean hasNext()
                {
                    if (setHasNext() == false)
                    {
                        if (mapSetIterator.hasNext())
                        {
                            setIterator = mapSetIterator.next().getValue().iterator();
                        }
                    }
                    return setHasNext();
                }

                @Override
                public E next()
                {
                    if (hasNext() == false)
                    {
                        throw new NoSuchElementException("No more elements.");
                    }
                    return setIterator.next();
                }

                @Override
                public void remove()
                {
                    throw new UnsupportedOperationException("Can not remove an element.");
                }

            };
    }
}

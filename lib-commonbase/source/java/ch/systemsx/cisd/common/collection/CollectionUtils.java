/*
 * Copyright 2007 ETH Zuerich, CISD
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Some convenience methods/utilities around {@link Collection}.
 * 
 * @author Christian Ribeaud
 */
public final class CollectionUtils
{
    private CollectionUtils()
    {
        // Can not be instantiated
    }

    /**
     * Abbreviates a given array of <code>Object</code>.
     * <p>
     * By default it shows the number of items left, {@link CollectionStyle#DEFAULT} and {@link ToStringDefaultConverter} are used.
     * </p>
     * 
     * @param maxLength the maximum number of items that should be shown. If <code>-1</code> then all items will be displayed.
     */
    public final static <T> String abbreviate(final T[] objects, final int maxLength)
    {
        return abbreviate(objects, maxLength, true);
    }

    /**
     * Abbreviates a given <code>Collection</code>.
     * <p>
     * By default it shows the number of items left, {@link CollectionStyle#DEFAULT} and {@link ToStringDefaultConverter} are used.
     * </p>
     * 
     * @param maxLength the maximum number of items that should be shown. If <code>-1</code> then all items will be displayed.
     */
    public final static <T> String abbreviate(final Collection<T> collection, final int maxLength)
    {
        return abbreviate(collection, maxLength, true);
    }

    /**
     * Abbreviates a given array of <code>Object</code>.
     * <p>
     * By default it shows the number of items left and {@link ToStringDefaultConverter} is used.
     * </p>
     * 
     * @param maxLength the maximum number of items that should be shown. If <code>-1</code> then all items will be displayed.
     */
    public final static <T> String abbreviate(final T[] objects, final int maxLength,
            final CollectionStyle style)
    {
        return abbreviate(objects, maxLength, true, ToStringDefaultConverter.getInstance(), style);
    }

    /**
     * Abbreviates a given <code>Collection</code>.
     * <p>
     * By default it shows the number of items left and {@link ToStringDefaultConverter} is used.
     * </p>
     * 
     * @param maxLength the maximum number of items that should be shown. If <code>-1</code> then all items will be displayed.
     */
    public final static <T> String abbreviate(final Collection<T> collection, final int maxLength,
            final CollectionStyle style)
    {
        return abbreviate(collection, maxLength, ToStringDefaultConverter.getInstance(), style);
    }

    /**
     * Abbreviates a given <code>Collection</code>.
     * <p>
     * By default it shows the number of items left.
     * </p>
     * 
     * @param maxLength the maximum number of items that should be shown. If <code>-1</code> then all items will be displayed.
     */
    public final static <T> String abbreviate(final Collection<T> collection, final int maxLength,
            final IToStringConverter<? super T> converter)
    {
        return abbreviate(collection, maxLength, converter, CollectionStyle.DEFAULT);
    }

    /**
     * Abbreviates a given <code>Collection</code>.
     * <p>
     * By default it shows the number of items left.
     * </p>
     * 
     * @param maxLength the maximum number of items that should be shown. If <code>-1</code> then all items will be displayed.
     */
    public final static <T> String abbreviate(final Collection<T> collection, final int maxLength,
            final IToStringConverter<? super T> converter, final CollectionStyle style)
    {
        return abbreviate(collection, maxLength, true, converter, style);
    }

    /**
     * Abbreviates a given array of <code>Object</code>.
     * <p>
     * By default {@link CollectionStyle#DEFAULT} and {@link ToStringDefaultConverter} are used.
     * </p>
     * 
     * @param maxLength the maximum number of items that should be shown. If <code>-1</code> then all items will be displayed.
     */
    public final static <T> String abbreviate(final T[] objects, final int maxLength,
            final boolean showLeft)
    {
        return abbreviate(objects, maxLength, showLeft, ToStringDefaultConverter.getInstance());
    }

    /**
     * Abbreviates a given <code>Collection</code>.
     * <p>
     * By default {@link CollectionStyle#DEFAULT} and {@link ToStringDefaultConverter} are used.
     * </p>
     * 
     * @param maxLength the maximum number of items that should be shown. If <code>-1</code> then all items will be displayed.
     */
    public final static <T> String abbreviate(final Collection<T> collection, final int maxLength,
            final boolean showLeft)
    {
        return abbreviate(collection, maxLength, showLeft, ToStringDefaultConverter.getInstance());
    }

    /**
     * Abbreviates a given array of <code>Object</code>.
     * <p>
     * By default {@link CollectionStyle#DEFAULT} is used.
     * </p>
     * 
     * @param maxLength the maximum number of items that should be shown. If <code>-1</code> then all items will be displayed.
     */
    public final static <T> String abbreviate(final T[] objects, final int maxLength,
            final boolean showLeft, final IToStringConverter<? super T> converter)
    {
        return abbreviate(objects, maxLength, showLeft, converter, CollectionStyle.DEFAULT);
    }

    /**
     * Abbreviates a given array of <code>Object</code>.
     * <p>
     * {@link CollectionStyle#DEFAULT} is used and all items is displayed.
     * </p>
     */
    public final static <T> String abbreviate(final T[] objects,
            final IToStringConverter<? super T> converter)
    {
        return abbreviate(objects, -1, false, converter, CollectionStyle.DEFAULT);
    }

    /**
     * Abbreviates a given <code>Collection</code>.
     * <p>
     * By default {@link CollectionStyle#DEFAULT} is used.
     * </p>
     * 
     * @param maxLength the maximum number of items that should be shown. If <code>-1</code> then all items will be displayed.
     */
    public final static <T> String abbreviate(final Collection<T> collection, final int maxLength,
            final boolean showLeft, final IToStringConverter<? super T> converter)
    {
        return abbreviate(collection, maxLength, showLeft, converter, CollectionStyle.DEFAULT);
    }

    /**
     * Abbreviates a given array of <code>Object</code>.
     * 
     * <pre>
     * CollectionUtils.abbreviate(new String[] { &quot;1&quot;, &quot;2&quot;, &quot;3&quot;, &quot;4&quot;, &quot;5&quot; }, 3, false) = &quot;[1, 2, 3, ...]&quot;
     * CollectionUtils.abbreviate(new String[] { &quot;1&quot;, &quot;2&quot;, &quot;3&quot;, &quot;4&quot;, &quot;5&quot; }, 3, true) = &quot;[1, 2, 3, ... (2 left)]&quot;
     * </pre>
     * 
     * @param maxLength the maximum number of items that should be shown. If <code>-1</code> then all items will be displayed.
     * @param showLeft whether the number of items left should be displayed at the end of the output. This is only relevant if you limit the number of
     *            items displayed.
     * @param style the style that should be applied to the output.
     */
    public final static <T> String abbreviate(final T[] objects, final int maxLength,
            final boolean showLeft, final IToStringConverter<? super T> converter,
            final CollectionStyle style)
    {
        assert objects != null : "Given objects can not be null.";
        return abbreviate(Arrays.asList(objects), maxLength, showLeft, converter, style);
    }

    /**
     * Abbreviates a given array of <code>Object</code>.
     * 
     * <pre>
     * CollectionUtils.abbreviate(new String[] { &quot;1&quot;, &quot;2&quot;, &quot;3&quot;, &quot;4&quot;, &quot;5&quot; }, 3, false) = &quot;[1, 2, 3, ...]&quot;
     * CollectionUtils.abbreviate(new String[] { &quot;1&quot;, &quot;2&quot;, &quot;3&quot;, &quot;4&quot;, &quot;5&quot; }, 3, true) = &quot;[1, 2, 3, ... (2 left)]&quot;
     * </pre>
     * 
     * @param maxLength the maximum number of items that should be shown. If <code>-1</code> then all items will be displayed.
     * @param style the style that should be applied to the output.
     */
    public final static <T> String abbreviate(final T[] objects, final int maxLength,
            final IToStringConverter<? super T> converter, final CollectionStyle style)
    {
        assert objects != null : "Given objects can not be null.";
        return abbreviate(Arrays.asList(objects), maxLength, true, converter, style);
    }

    /**
     * Abbreviates a given <code>Collection</code>.
     * 
     * @param maxLength the maximum number of items that should be shown. If <code>-1</code> then all items will be displayed.
     * @param showLeft whether the number of items left should be displayed at the end of the output. This is only relevant if you limit the number of
     *            items displayed.
     * @param style the style that should be applied to the output.
     */
    public final static <T> String abbreviate(final Collection<T> collection, final int maxLength,
            final boolean showLeft, final IToStringConverter<? super T> converter,
            final CollectionStyle style)
    {
        assert collection != null : "Given collection can not be null.";
        assert converter != null : "Given converter can not be null.";
        assert style != null : "Given style can not be null.";
        final StringBuilder builder = new StringBuilder(style.getCollectionStart());
        final Iterator<T> iterator = collection.iterator();
        for (int i = 0; iterator.hasNext() && (i < maxLength || maxLength < 0); i++)
        {
            if (i > 0)
            {
                builder.append(style.getCollectionSeparator());
            }
            builder.append(converter.toString(iterator.next()));
        }
        int size = collection.size();
        if (maxLength > 0 && maxLength < size)
        {
            builder.append(style.getCollectionSeparator());
            builder.append("...");
            if (showLeft)
            {
                builder.append(" (").append(size - maxLength).append(" left)");
            }
        }
        builder.append(style.getCollectionEnd());
        return builder.toString();
    }

    /**
     * converts {@link Iterator} to {@link List}.
     */
    public final static <T> List<T> asList(Iterator<T> iterator)
    {
        assert iterator != null : "Given iterator can not be null.";

        ArrayList<T> result = new ArrayList<T>();
        while (iterator.hasNext())
        {
            result.add(iterator.next());
        }
        return result;
    }

    public static interface ICollectionFilter<V>
    {
        boolean isPresent(V element);
    }

    /** Creates a new collection from elements which are accepted by the filter. */
    public static <V> List<V> filter(Collection<V> list, ICollectionFilter<V> filter)
    {
        List<V> filtered = new ArrayList<V>();
        for (V elem : list)
        {
            if (filter.isPresent(elem))
            {
                filtered.add(elem);
            }
        }
        return filtered;
    }

    /**
     * For a given collection C returns
     * 
     * <pre>
     * 1) C when is not null
     * 2) Empty collection when C is null
     * </pre>
     * 
     * This allows e.g. iterating over the collections elements without redundant null checks.
     */
    public static <E> Collection<E> nullSafe(Collection<E> list)
    {
        return (list != null) ? list : Collections.<E> emptyList();
    }

    /**
     * Sorts a collection of objects with a key extractor providing sort criteria.
     * <p>
     * The method is useful when we would like to sort a list of non-Comparable objects.
     */
    public static <E, C extends Comparable<C>> void sort(List<E> list,
            final IKeyExtractor<C, E> sortCriteriaKeyExtractor)
    {
        Collections.sort(list, new Comparator<E>()
            {
                @Override
                public int compare(E o1, E o2)
                {
                    C key1 = sortCriteriaKeyExtractor.getKey(o1);
                    C key2 = sortCriteriaKeyExtractor.getKey(o2);
                    return key1.compareTo(key2);
                }
            });
    }

    /**
     * Transform the original array using given map.
     * 
     * @return the list of items from the input collection transformed with the specified map.
     */
    public static <I, O> List<O> map(Collection<? extends I> input, Map<I, O> map)
    {
        List<O> output = new ArrayList<O>();
        for (I i : input)
        {
            O value = map.get(i);

            if (value == null)
            {
                throw new IllegalArgumentException("Element " + i + " is not specified in the transforming map");
            }
            output.add(value);
        }
        return output;
    }

}
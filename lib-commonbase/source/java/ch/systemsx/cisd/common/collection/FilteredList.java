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
import java.util.List;
import java.util.ListIterator;

import ch.systemsx.cisd.common.collection.FilteredCollection;
import ch.systemsx.cisd.common.collection.FilteredList;
import ch.systemsx.cisd.common.collection.IValidator;

/**
 * Decorates another <code>List</code> to validate that all additions match a specified <code>IValidator</code>.
 * <p>
 * This list exists to provide validation for the decorated list. This class is not <code>Serializable</code>.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class FilteredList<E> extends FilteredCollection<E> implements List<E>
{

    /**
     * Constructor that filters given <code>List</code>.
     * 
     * @param list the list to decorate. Must not be <code>null</code>
     * @param validator the <code>IValidator</code> to use for validation. Must not be <code>null</code>
     */
    protected FilteredList(final List<E> list, final IValidator<E> validator)
    {
        super(list, validator);
    }

    /**
     * Factory method to create a filtered (validating) list.
     * <p>
     * If there are any elements already in the list being decorated, they are validated.
     * </p>
     * 
     * @param list the list to decorate. Must not be <code>null</code>
     * @param validator the <code>IValidator</code> to use for validation. Must not be <code>null</code>
     */
    public static <E> List<E> decorate(final List<E> list, final IValidator<E> validator)
    {
        return new FilteredList<E>(list, validator);
    }

    /**
     * Factory method to create a filtered (validating) list.
     * <p>
     * If there are any elements already in the array being decorated, they are validated.
     * </p>
     * 
     * @param array the array to decorate. Must not be <code>null</code>
     * @param validator the <code>IValidator</code> to use for validation. Must not be <code>null</code>
     */
    public static <E> List<E> decorate(final E[] array, final IValidator<E> validator)
    {
        return new FilteredList<E>(new ArrayList<E>(Arrays.asList(array)), validator);
    }

    /**
     * Gets the list being decorated.
     * 
     * @return the decorated list
     */
    protected List<E> getList()
    {
        return (List<E>) getCollection();
    }

    //
    // List
    //

    @Override
    public E get(final int index)
    {
        return getList().get(index);
    }

    @Override
    public int indexOf(final Object object)
    {
        return getList().indexOf(object);
    }

    @Override
    public int lastIndexOf(final Object object)
    {
        return getList().lastIndexOf(object);
    }

    @Override
    public E remove(final int index)
    {
        return getList().remove(index);
    }

    @Override
    public ListIterator<E> listIterator()
    {
        return getList().listIterator();
    }

    @Override
    public ListIterator<E> listIterator(final int i)
    {
        return getList().listIterator(i);
    }

    @Override
    public E set(final int index, final E object)
    {
        if (isValid(object))
        {
            return getList().set(index, object);
        }
        return null;
    }

    @Override
    public List<E> subList(final int fromIndex, final int toIndex)
    {
        return new FilteredList<E>(getList().subList(fromIndex, toIndex), validator);
    }

    @Override
    public void add(final int index, final E object)
    {
        if (isValid(object))
        {
            getList().add(index, object);
        }
    }

    @Override
    public boolean addAll(final int index, final Collection<? extends E> collection)
    {
        return getList().addAll(index, filterCollection(collection, validator));
    }
}
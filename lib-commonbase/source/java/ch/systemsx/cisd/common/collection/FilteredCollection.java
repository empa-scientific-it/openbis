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

import java.util.Collection;
import java.util.Iterator;

import ch.systemsx.cisd.common.collection.AbstractCollectionDecorator;
import ch.systemsx.cisd.common.collection.FilteredCollection;
import ch.systemsx.cisd.common.collection.IValidator;

/**
 * Decorates another <code>Collection</code> to validate that additions match a specified <code>IValidator</code>.
 * <p>
 * This collection exists to provide validation for the decorated collection.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public class FilteredCollection<E> extends AbstractCollectionDecorator<E>
{

    /** The validator to use. */
    protected final IValidator<E> validator;

    /**
     * Factory method to create a filtered (validating) collection.
     * <p>
     * If there are any elements already in the collection being decorated, they are validated.
     * </p>
     * 
     * @param coll the collection to decorate. Must not be <code>null</code>.
     * @param validator the <code>IValidator</code> to use for validation. Must not be <code>null</code>.
     * @return a new filtered collection.
     */
    public static <E> Collection<E> decorate(final Collection<E> coll, final IValidator<E> validator)
    {
        return new FilteredCollection<E>(coll, validator);
    }

    /**
     * Constructor that filters given <code>Collection</code>.
     * 
     * @param collection the collection to decorate. Must not be <code>null</code>.
     * @param validator the <code>IValidator</code> to use for validation. Must not be <code>null</code>.
     */
    protected FilteredCollection(final Collection<E> collection, final IValidator<E> validator)
    {
        super(filterCollection(collection, validator));
        assert validator != null;

        this.validator = validator;
    }

    /**
     * Filters given <var>collection</var> with given <var>validator</var>.
     * <p>
     * Note that this operation changes passed <code>Collection</code> if some invalid elements are found. The original collection does not get cloned
     * before.
     * </p>
     */
    @SuppressWarnings("unchecked")
    protected final static <E> Collection<E> filterCollection(
            final Collection<? extends E> collection, final IValidator<E> validator)
    {
        if (collection == null)
        {
            return null;
        }
        for (final Iterator<? extends E> iter = collection.iterator(); iter.hasNext();)
        {
            if (validator.isValid(iter.next()) == false)
            {
                iter.remove();
            }
        }
        return (Collection<E>) collection;
    }

    /**
     * Validates the object being added.
     * 
     * @param object the object being added
     */
    protected boolean isValid(final E object)
    {
        return validator.isValid(object);
    }

    //
    // AbstractCollectionDecorator
    //

    @Override
    public final boolean add(final E object)
    {
        if (isValid(object))
        {
            return getCollection().add(object);
        }
        return false;
    }

    @Override
    public final boolean addAll(final Collection<? extends E> collection)
    {
        boolean changed = false;
        for (final E e : collection)
        {
            if (add(e) && changed == false)
            {
                changed = true;
            }
        }
        return changed;
    }
}

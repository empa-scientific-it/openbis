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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

/**
 * @author pkupczyk
 */
public class ListView<T> extends AbstractCollectionView<T> implements List<T>
{

    private static final long serialVersionUID = 1L;

    public ListView(Collection<T> originalList, Integer from, Integer count)
    {
        super(originalList, from, count);
    }

    @Override
    protected Collection<T> createLimited(Collection<T> originalCollection, Integer fromOrNull, Integer countOrNull)
    {
        List<T> limited = new ArrayList<T>();
        copyItems(originalCollection, limited, fromOrNull, countOrNull);
        return Collections.unmodifiableList(limited);
    }

    @Override
    public T get(int index)
    {
        return getLimitedCollection().get(index);
    }

    @Override
    public T set(int index, T element)
    {
        return getLimitedCollection().set(index, element);
    }

    @Override
    public void add(int index, T element)
    {
        getLimitedCollection().add(index, element);
    }

    @Override
    public T remove(int index)
    {
        return getLimitedCollection().remove(index);
    }

    @Override
    public int indexOf(Object o)
    {
        return getLimitedCollection().indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o)
    {
        return getLimitedCollection().lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator()
    {
        return getLimitedCollection().listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index)
    {
        return getLimitedCollection().listIterator(index);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex)
    {
        return getLimitedCollection().subList(fromIndex, toIndex);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c)
    {
        return getLimitedCollection().addAll(index, c);
    }

    @Override
    public List<T> getLimitedCollection()
    {
        return (List<T>) super.getLimitedCollection();
    }

}

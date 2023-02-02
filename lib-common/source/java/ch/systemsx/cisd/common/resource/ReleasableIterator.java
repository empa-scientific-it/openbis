/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.resource;

import java.util.Iterator;

import ch.systemsx.cisd.common.collection.NullIterator;

/**
 * @author pkupczyk
 */
public class ReleasableIterator<T> implements Iterator<T>, IReleasable
{

    private Iterator<T> originalIterator;

    private Resources resources;

    public ReleasableIterator(Iterator<T> originalIterator)
    {
        if (originalIterator == null)
        {
            this.originalIterator = new NullIterator<T>();
        } else
        {
            if (originalIterator instanceof IReleasable)
            {
                getResources().add(originalIterator);
            }
            this.originalIterator = originalIterator;
        }
    }

    @Override
    public boolean hasNext()
    {
        return originalIterator.hasNext();
    }

    @Override
    public T next()
    {
        if (originalIterator instanceof IReleasable)
        {
            return originalIterator.next();
        } else
        {
            T item = originalIterator.next();
            if (item instanceof IReleasable)
            {
                getResources().add(item);
            }
            return item;
        }
    }

    @Override
    public void remove()
    {
        originalIterator.remove();
    }

    @Override
    public void release()
    {
        if (resources != null)
        {
            resources.release();
        }
    }

    private Resources getResources()
    {
        if (resources == null)
        {
            resources = new Resources();
        }
        return resources;
    }

}

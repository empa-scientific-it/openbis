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

import ch.systemsx.cisd.common.collection.NullIterable;
import ch.systemsx.cisd.common.collection.NullIterator;

/**
 * @author pkupczyk
 */
public class ReleasableIterable<T> implements Iterable<T>, IReleasable
{

    private Iterable<T> originalIterable;

    private Resources resources;

    public ReleasableIterable(Iterable<T> originalIterable)
    {
        if (originalIterable == null)
        {
            this.originalIterable = new NullIterable<T>();
        } else
        {
            if (originalIterable instanceof IReleasable)
            {
                getResources().add(originalIterable);
            }
            this.originalIterable = originalIterable;
        }
    }

    @Override
    public Iterator<T> iterator()
    {
        if (originalIterable instanceof IReleasable)
        {
            return originalIterable.iterator();
        } else
        {
            Iterator<T> originalIterator = originalIterable.iterator();

            if (originalIterator != null)
            {
                if (originalIterator instanceof IReleasable)
                {
                    getResources().add(originalIterator);
                    return originalIterator;
                } else
                {
                    ReleasableIterator<T> releasableIterator = new ReleasableIterator<T>(originalIterator);
                    getResources().add(releasableIterator);
                    return releasableIterator;
                }
            } else
            {
                return new NullIterator<T>();
            }
        }
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

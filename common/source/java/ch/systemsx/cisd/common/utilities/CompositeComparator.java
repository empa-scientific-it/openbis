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

package ch.systemsx.cisd.common.utilities;

import java.util.Comparator;

/**
 * Comparator that makes comparison using an ordered list of individual comparators.
 * 
 * @author Christian Ribeaud
 */
public final class CompositeComparator<T> implements Comparator<T>
{
    private final Comparator<T>[] comparators;

    public CompositeComparator(final Comparator<T>... comparators)
    {
        this.comparators = comparators;
    }

    //
    // Comparator
    //

    public final int compare(final T o1, final T o2)
    {
        for (Comparator<T> comparator : comparators)
        {
            int c = comparator.compare(o1, o2);
            if (c != 0)
            {
                return c;
            }
        }
        return 0;
    }
}

/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author pkupczyk
 */
public class CompositeComparator<OBJECT> implements Comparator<OBJECT>
{

    private List<Comparator<OBJECT>> comparators = new LinkedList<Comparator<OBJECT>>();

    @SafeVarargs
    public CompositeComparator(Comparator<OBJECT>... comparators)
    {
        if (comparators == null || comparators.length == 0)
        {
            throw new IllegalArgumentException("Comparators cannot be null or empty");
        }
        this.comparators = Arrays.asList(comparators);
    }

    @Override
    public int compare(OBJECT o1, OBJECT o2)
    {
        int result = 0;
        Iterator<Comparator<OBJECT>> iter = comparators.iterator();

        while (result == 0 && iter.hasNext())
        {
            Comparator<OBJECT> comparator = iter.next();
            result = comparator.compare(o1, o2);
        }

        return result;
    }

}

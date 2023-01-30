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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort;

import java.util.Comparator;

/**
 * @author pkupczyk
 */
public abstract class AbstractComparator<OBJECT, VALUE extends Comparable<VALUE>> implements Comparator<OBJECT>
{

    @Override
    public int compare(OBJECT o1, OBJECT o2)
    {
        VALUE value1 = getValue(o1);
        VALUE value2 = getValue(o2);

        if (value1 != null && value2 != null)
        {
            return doCompare(value1, value2);
        } else if (value1 != null)
        {
            return 1;
        } else if (value2 != null)
        {
            return -1;
        } else
        {
            return 0;
        }
    }

    protected int doCompare(VALUE value1, VALUE value2)
    {
        return value1.compareTo(value2);
    }

    protected abstract VALUE getValue(OBJECT o);

}

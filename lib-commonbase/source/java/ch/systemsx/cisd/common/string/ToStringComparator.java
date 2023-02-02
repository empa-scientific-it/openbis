/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.string;

import java.util.Comparator;

/**
 * A comparator based on the string representation of the objects.
 * 
 * @author Kaloyan Enimanev
 */
public class ToStringComparator implements Comparator<Object>
{

    @Override
    public int compare(Object o1, Object o2)
    {
        String o1String = o1.toString();
        String o2String = o2.toString();
        return o1String.compareTo(o2String);
    }

}

/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.webapp;

import java.util.Comparator;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.WebApp;

/**
 * @author pkupczyk
 */
public class WebAppSortingAndCodeComparator implements Comparator<WebApp>
{

    @Override
    public int compare(WebApp o1, WebApp o2)
    {
        int compareBySorting = compareWithNullsLast(o1.getSorting(), o2.getSorting());

        if (compareBySorting == 0)
        {
            return compareWithNullsLast(o1.getCode(), o2.getCode());
        } else
        {
            return compareBySorting;
        }
    }

    @SuppressWarnings("unchecked")
    private <T> int compareWithNullsLast(Comparable<T> o1, Comparable<T> o2)
    {
        if (o1 == null)
        {
            if (o2 == null)
            {
                return 0;
            } else
            {
                return 1;
            }
        } else
        {
            if (o2 == null)
            {
                return -1;
            } else
            {
                return o1.compareTo((T) o2);
            }
        }
    }

}

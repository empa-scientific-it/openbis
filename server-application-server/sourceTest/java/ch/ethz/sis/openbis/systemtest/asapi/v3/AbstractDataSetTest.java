/*
 * Copyright ETH 2014 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static ch.systemsx.cisd.common.test.AssertionUtil.assertCollectionContainsOnly;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.systemsx.cisd.common.collection.SimpleComparator;

public class AbstractDataSetTest extends AbstractTest
{
    protected static final SimpleComparator<DataSet, String> DATA_SET_COMPARATOR = new SimpleComparator<DataSet, String>()
        {
            @Override
            public String evaluate(DataSet item)
            {
                return item.getCode();
            }
        };

    protected static void assertIdentifiers(Collection<DataSet> dataSets, String... expectedCodesIdentifiers)
    {
        Set<String> actualSet = new HashSet<String>();
        for (DataSet dataSet : dataSets)
        {
            actualSet.add(dataSet.getPermId().getPermId());
        }

        assertCollectionContainsOnly(actualSet, expectedCodesIdentifiers);
    }

}

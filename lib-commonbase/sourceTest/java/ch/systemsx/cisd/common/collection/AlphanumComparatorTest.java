/*
 * Copyright 2010 ETH Zuerich, CISD
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

import java.util.Arrays;

import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.collection.AlphanumComparator;

/**
 * @author Piotr Buczek
 */
public class AlphanumComparatorTest extends AssertJUnit
{

    @DataProvider(name = "arrays")
    protected Object[][] arrays()
    {
        // initial order is natural sorting order
        return new Object[][]
        {
                { new String[]
                { "12", "123", "13" }, "[12, 13, 123]" },
                { new String[]
                { "A12", "A123", "A13" }, "[A12, A13, A123]" },
                { new String[]
                { "A", "AB", "ABC" }, "[A, AB, ABC]" },
                { new String[]
                { "A1", "A2", "A3" }, "[A1, A2, A3]" },
                { new String[]
                { "A", "A1", "A2", "AA" }, "[A, A1, A2, AA]" },
                { new String[]
                { "12AB1", "12AB12", "12AB2", "1AB1", "2AB12", "2AB3" },
                        "[1AB1, 2AB3, 2AB12, 12AB1, 12AB2, 12AB12]" },
                {
                        new String[]
                        { "1_1", "1_2", "1_10", "2_1", "2_2", "2_10", "10_1", "10_2",
                                "10_10" },
                        "[1_1, 1_2, 1_10, 2_1, 2_2, 2_10, 10_1, 10_2, 10_10]" },

        };

    }

    @Test(dataProvider = "arrays")
    public void testArraySort(String[] array, String expectedAsString)
    {
        Arrays.sort(array, new AlphanumComparator());
        assertEquals(expectedAsString, Arrays.toString(array));
    }

}

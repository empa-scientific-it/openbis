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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.filter;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetBuilder;

/**
 * @author Franz-Josef Elmer
 */
public class TypeBasedDataSetFilterTest extends AssertJUnit
{
    @Test
    public void test()
    {
        IDataSetFilter filter = new TypeBasedDataSetFilter(".*ANA");
        DataSetBuilder builder = new DataSetBuilder().code("ds1").experiment("/S/P/E");
        assertEquals(true, filter.pass(builder.type("MY-ANA").getDataSet()));
        assertEquals(false, filter.pass(builder.type("MY-ANA2").getDataSet()));
        assertEquals(false, filter.pass(builder.type("HELLO").getDataSet()));
    }
}

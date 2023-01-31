/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.utils;

import java.util.Arrays;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * @author Franz-Josef Elmer
 */
public class TableBuilderTest extends AssertJUnit
{
    @Test
    public void test()
    {
        TableBuilder builder = new TableBuilder("alpha", "beta");
        builder.addRow("11");
        builder.addRow(Arrays.asList("21", "22"));
        builder.addRow(Arrays.asList("31", "32", "33"));

        List<Column> columns = builder.getColumns();
        assertEquals(2, columns.size());
        assertEquals("alpha", columns.get(0).getHeader());
        assertEquals("[11, 21, 31]", columns.get(0).getValues().toString());
        assertEquals("beta", columns.get(1).getHeader());
        assertEquals("[, 22, 32]", columns.get(1).getValues().toString());
    }
}

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

package ch.systemsx.cisd.openbis.generic.client.web.server.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.client.web.server.calculator.GridExpressionUtils;
import ch.systemsx.cisd.openbis.generic.client.web.server.calculator.ITableDataProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.PrimitiveValue;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomColumn;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class CustomColumnUtilsTest extends AssertJUnit
{
    private static final class MockDataProvider implements ITableDataProvider
    {
        private final String expectedColumnID;

        private final List<List<? extends Comparable<?>>> rows;

        MockDataProvider(String expectedColumnID, List<? extends Comparable<?>>... rows)
        {
            this.expectedColumnID = expectedColumnID;
            this.rows = Arrays.asList(rows);
        }

        @Override
        public List<List<? extends Comparable<?>>> getRows()
        {
            return rows;
        }

        @Override
        public Comparable<?> getValue(String columnID, List<? extends Comparable<?>> rowValues)
        {
            assertEquals(expectedColumnID, columnID);
            assertEquals(1, rowValues.size());
            return rowValues.get(0);
        }

        @Override
        public Collection<String> getAllColumnIDs()
        {
            return null;
        }

        @Override
        public List<String> getAllColumnTitles()
        {
            return null;
        }

        @Override
        public String tryToGetProperty(String columnID, String key)
        {
            return null;
        }
    }

    GridCustomColumn customColumn;

    List<GridCustomColumn> customColumns;

    private ITableDataProvider dataProvider;

    @SuppressWarnings("unchecked")
    @BeforeMethod
    public void setUp()
    {
        dataProvider = new MockDataProvider("VALUE", Arrays.asList(42));
        // Define a custom column
        customColumn = new GridCustomColumn();
        customColumn.setCode("TEST");

        // We need to associate the column with someone for error messages
        Person registrator = new Person();
        registrator.setFirstName("Jane");
        registrator.setLastName("Doe");
        registrator.setEmail("jane.doe@nowhere.com");
        customColumn.setRegistrator(registrator);

        customColumns = new ArrayList<GridCustomColumn>();
        customColumns.add(customColumn);
    }

    @Test
    public void testCorrectExpression()
    {
        customColumn.setExpression("row.col('VALUE') + 10");

        List<PrimitiveValue> values =
                GridExpressionUtils.evalCustomColumn(dataProvider, customColumn, false);

        assertEquals("[52]", values.toString());

    }

    @Test
    public void testShortErrorMessage()
    {
        customColumn.setCode("TEST");

        // incorrect expression
        customColumn.setExpression("junk + 10");

        List<PrimitiveValue> values =
                GridExpressionUtils.evalCustomColumn(dataProvider, customColumn, false);

        assertEquals(
                "[Error. Please contact 'Jane Doe <jane.doe@nowhere.com>', who defined this column.]",
                values.toString());
    }

    @Test
    public void testLongErrorMessage()
    {
        // incorrect expression
        customColumn.setExpression("junk + 10");

        List<PrimitiveValue> values =
                GridExpressionUtils.evalCustomColumn(dataProvider, customColumn, true);

        assertEquals(
                "[Error: (Error occurred in line 1 of the script when evaluating 'junk + 10': "
                        + "NameError: name 'junk' is not defined).]", values.toString());
    }

}

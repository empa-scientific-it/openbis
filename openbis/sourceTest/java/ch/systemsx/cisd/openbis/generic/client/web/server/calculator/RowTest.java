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

package ch.systemsx.cisd.openbis.generic.client.web.server.calculator;

import java.util.Arrays;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Franz-Josef Elmer
 */
public class RowTest extends AssertJUnit
{
    private static final List<Comparable<?>> EXAMPLE_ROW = Arrays.<Comparable<?>> asList("hello", Double.valueOf(42.5));

    private static final String COL1 = "col1";

    private static final String COL2 = "col2";

    private static final String PROPERTY_KEY = "key";

    private static final String PROPERTY_VALUE = "value";

    private static final String PROPERTY_VALUE2 = "value2";

    private Mockery context;

    private Row row;

    private ITableDataProvider dataProvider;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        dataProvider = context.mock(ITableDataProvider.class);
        context.checking(new Expectations()
            {
                {
                    allowing(dataProvider).getValue(COL1, EXAMPLE_ROW);
                    will(returnValue(EXAMPLE_ROW.get(0)));

                    allowing(dataProvider).getValue(COL2, EXAMPLE_ROW);
                    will(returnValue(EXAMPLE_ROW.get(1)));
                }
            });

        row = new Row(dataProvider);
        row.setRowData(EXAMPLE_ROW);
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one does not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testCol()
    {
        row.setRowData(EXAMPLE_ROW);

        assertSame(EXAMPLE_ROW.get(0), row.col(COL1));
        assertSame(EXAMPLE_ROW.get(1), row.col(COL2));

        context.assertIsSatisfied();
    }

    @Test
    public void testColDefs()
    {
        prepareColDefs(null, PROPERTY_VALUE);

        row.colDefs(PROPERTY_KEY);
        List<ColumnDefinition> defs = row.colDefs(PROPERTY_KEY); // second call tests caching

        context.checking(new Expectations()
            {
                {
                    one(dataProvider).tryToGetProperty(COL2, PROPERTY_KEY);
                    will(returnValue(PROPERTY_VALUE));
                }
            });

        assertEquals(1, defs.size());
        assertEquals(COL2, defs.get(0).id());
        assertEquals(PROPERTY_VALUE, defs.get(0).property(PROPERTY_KEY));
        assertUnmodifiable(defs);

        context.assertIsSatisfied();
    }

    @Test
    public void testColDefsWithNullArgument()
    {
        prepareColDefs(PROPERTY_VALUE, PROPERTY_VALUE2, 0);

        List<ColumnDefinition> defs = row.colDefs(null);

        assertEquals(2, defs.size());
        assertUnmodifiable(defs);

        context.assertIsSatisfied();
    }

    @Test
    public void testCols()
    {
        prepareColDefs(null, PROPERTY_VALUE);
        context.checking(new Expectations()
            {
                {
                    one(dataProvider).tryToGetProperty(COL2, PROPERTY_KEY);
                    will(returnValue(PROPERTY_VALUE));
                }
            });
        row.setRowData(EXAMPLE_ROW);

        List<Object> values = row.cols(PROPERTY_KEY, PROPERTY_VALUE);

        assertEquals(1, values.size());
        assertSame(EXAMPLE_ROW.get(1), values.get(0));
        assertUnmodifiable(values);

        context.assertIsSatisfied();
    }

    @Test
    public void testColsNoMatchingPropertyKey()
    {
        prepareColDefs(null, null);

        List<Object> values = row.cols(PROPERTY_KEY, "unknown");

        assertEquals(0, values.size());
        assertUnmodifiable(values);

        context.assertIsSatisfied();
    }

    @Test
    public void testColsNoMatchingPropertyValue()
    {
        prepareColDefs(null, PROPERTY_VALUE);
        context.checking(new Expectations()
            {
                {
                    one(dataProvider).tryToGetProperty(COL2, PROPERTY_KEY);
                    will(returnValue(PROPERTY_VALUE));
                }
            });

        List<Object> values = row.cols(PROPERTY_KEY, "unknown");

        assertEquals(0, values.size());
        assertUnmodifiable(values);

        context.assertIsSatisfied();
    }

    @Test
    public void testColsGroupedByNoMatchingPropertyKey()
    {
        prepareColDefs(null, null);

        List<ColumnGroup> groups = row.colsGroupedBy(PROPERTY_KEY);

        assertEquals(0, groups.size());
        assertUnmodifiable(groups);

        context.assertIsSatisfied();
    }

    @Test
    public void testColsGroupedByWithOneGroup()
    {
        prepareColDefs(PROPERTY_VALUE, PROPERTY_VALUE, 2);

        List<ColumnGroup> groups = row.colsGroupedBy(PROPERTY_KEY);

        assertEquals(1, groups.size());
        assertEquals(PROPERTY_VALUE, groups.get(0).propertyValue());
        assertEquals(2, groups.get(0).values().size());
        assertEquals(EXAMPLE_ROW.get(0), groups.get(0).values().get(0));
        assertEquals(EXAMPLE_ROW.get(1), groups.get(0).values().get(1));
        assertUnmodifiable(groups);

        context.assertIsSatisfied();
    }

    @Test
    public void testColsGroupedByWithTwoGroups()
    {
        prepareColDefs(PROPERTY_VALUE, PROPERTY_VALUE2, 2);

        List<ColumnGroup> groups = row.colsGroupedBy(PROPERTY_KEY);

        assertEquals(2, groups.size());
        assertEquals(PROPERTY_VALUE, groups.get(0).propertyValue());
        assertEquals(1, groups.get(0).values().size());
        assertEquals(EXAMPLE_ROW.get(0), groups.get(0).values().get(0));
        assertEquals(PROPERTY_VALUE2, groups.get(1).propertyValue());
        assertEquals(1, groups.get(1).values().size());
        assertEquals(EXAMPLE_ROW.get(1), groups.get(1).values().get(0));
        assertUnmodifiable(groups);

        context.assertIsSatisfied();
    }

    private void prepareColDefs(final String propertyValueOfFirstDefinition,
            final String propertyValueOfSecondDefinition)
    {
        prepareColDefs(propertyValueOfFirstDefinition, propertyValueOfSecondDefinition, 1);
    }

    private void prepareColDefs(final String propertyValueOfFirstDefinition,
            final String propertyValueOfSecondDefinition, final int numberOfTryToGetPropertyInvocations)
    {
        context.checking(new Expectations()
            {
                {
                    one(dataProvider).getAllColumnIDs();
                    will(returnValue(Arrays.asList(COL1, COL2)));

                    exactly(numberOfTryToGetPropertyInvocations).of(dataProvider).tryToGetProperty(COL1, PROPERTY_KEY);
                    will(returnValue(propertyValueOfFirstDefinition));

                    exactly(numberOfTryToGetPropertyInvocations).of(dataProvider).tryToGetProperty(COL2, PROPERTY_KEY);
                    will(returnValue(propertyValueOfSecondDefinition));
                }
            });
    }

    private void assertUnmodifiable(List<?> list)
    {
        try
        {
            list.clear();
            fail("unmodifable list expected");
        } catch (UnsupportedOperationException ex)
        {
            // ignored
        }
    }
}

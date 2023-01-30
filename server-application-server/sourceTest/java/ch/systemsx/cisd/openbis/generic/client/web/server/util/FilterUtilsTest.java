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

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.CustomFilterInfo;
import ch.systemsx.cisd.openbis.generic.client.web.server.calculator.GridExpressionUtils;
import ch.systemsx.cisd.openbis.generic.client.web.server.calculator.ITableDataProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ParameterWithValue;

/**
 * @author Franz-Josef Elmer
 */
public class FilterUtilsTest extends AssertJUnit
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

    private ITableDataProvider dataProvider;

    @SuppressWarnings("unchecked")
    @BeforeMethod
    public void setUp()
    {
        dataProvider = new MockDataProvider("VALUE", Arrays.asList(50), Arrays.asList(40));
    }

    @Test
    public void test()
    {
        @SuppressWarnings(value = "rawtypes")
        CustomFilterInfo<?> filterInfo = new CustomFilterInfo();

        filterInfo.setExpression("row.col('VALUE') < ${threshold}");
        ParameterWithValue parameter = new ParameterWithValue();
        parameter.setParameter("threshold");
        parameter.setValue("42");
        filterInfo.setParameters(new LinkedHashSet<ParameterWithValue>(Arrays.asList(parameter)));

        List<Integer> indices = GridExpressionUtils.applyCustomFilter(dataProvider, filterInfo);

        assertEquals("[1]", indices.toString());
    }

}

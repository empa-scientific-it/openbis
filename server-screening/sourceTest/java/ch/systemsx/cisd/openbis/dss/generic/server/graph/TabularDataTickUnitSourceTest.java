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

package ch.systemsx.cisd.openbis.dss.generic.server.graph;

import org.jfree.chart.axis.TickUnit;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class TabularDataTickUnitSourceTest extends AssertJUnit
{
    TabularDataTickUnitSource source;

    @BeforeMethod
    public void setUp()
    {
        source = new TabularDataTickUnitSource();
    }

    @Test
    public void testGreaterThan1TickUnitComputation()
    {
        verify(1., 1., 10, "10");
        verify(2., 5., 20, "20");
        verify(5., 5., 20, "20");
        verify(9., 10., 20, "20");
        verify(1000., 1000., 3000., "3000");
        verify(1000000., 1000000., 6000000., "6000000");
        verify(10000000.0, 10000000., 70000000., "7.0E7");
        verify(1000000000.0, 1000000000., 9000000000., "9.0E9");
        verify(10000000000.0, 10000000000., 11230000000., "1.123E10");
    }

    @Test
    public void testLessThan1TickUnitComputation()
    {
        verify(0.5, 0.5, 1.5, "1.5");
        verify(0.1, 0.5, 1.5, "1.5");
        verify(0.09, 0.1, 0.2, "0.2");
        verify(0.009, 0.01, 0.03, "0.03");
        verify(0.0009, 0.001, 0.004, "0.004");
        verify(0.00009, 0.0001, 0.00051, "5.1E-4");
    }

    private void verify(double value, double size, double formatValue, String formatResult)
    {
        TickUnit unit;
        unit = source.getCeilingTickUnit(value);
        assertEquals(size, unit.getSize());
        assertEquals(formatResult, unit.valueToString(formatValue));
    }
}

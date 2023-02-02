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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.model;

import java.util.Arrays;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.Range;

/**
 * @author Franz-Josef Elmer
 */
public class PercentileRangeCalculatorTest extends AssertJUnit
{
    @Test
    public void test()
    {
        List<Float> data = Arrays.asList(40f, 50f, 15f, 20f, 35f);

        Range range = new PercentileRangeCalculator(30, 40).calculate(data);

        assertEquals(20f, range.getFrom(), 1e-3);
        assertEquals(27.5f, range.getUntil(), 1e-3);

        range = new PercentileRangeCalculator(35, 40).calculate(data);

        assertEquals(23.75f, range.getFrom(), 1e-3);
        assertEquals(27.5f, range.getUntil(), 1e-3);
    }

    @Test
    public void testSingleElementData()
    {
        List<Float> data = Arrays.asList(40f);

        Range range = new PercentileRangeCalculator(30, 40).calculate(data);

        assertEquals(40f, range.getFrom(), 1e-3);
        assertEquals(40f, range.getUntil(), 1e-3);
    }

    @Test
    public void testMinMax()
    {
        List<Float> data = Arrays.asList(40f, 41f, -12f, 23f);

        Range range = new PercentileRangeCalculator(0, 100).calculate(data);

        assertEquals(-12f, range.getFrom(), 1e-3);
        assertEquals(41f, range.getUntil(), 1e-3);
    }
}

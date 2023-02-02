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

package ch.systemsx.cisd.openbis.dss.etl.dynamix;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.dss.etl.dynamix.WellLocationMappingUtils.DynamixWellPosition;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;

/**
 * Tests of {@link WellLocationMappingUtils}
 * 
 * @author Tomasz Pylak
 */
public class WellLocationMappingUtilsTest extends AssertJUnit
{
    @Test
    public void testMapping() throws IOException
    {
        Map<DynamixWellPosition, WellLocation> map =
                WellLocationMappingUtils.parseWellLocationMap(new File(
                        "sourceTest/java/ch/systemsx/cisd/openbis/dss/etl/dynamix/pos2loc.tsv"));
        DynamixWellPosition pos = new DynamixWellPosition(100, true);

        WellLocation wellLocation = map.get(pos);
        assertEquals(wellLocation.getRow(), 5);
        assertEquals(wellLocation.getColumn(), 17);
    }

    @Test
    public void testPositionParsing() throws IOException
    {
        DynamixWellPosition expectedPos = new DynamixWellPosition(100, true);
        DynamixWellPosition pos = WellLocationMappingUtils.parseWellPosition("Right", "100");
        assertEquals(expectedPos, pos);

        expectedPos = new DynamixWellPosition(5, false);
        pos = WellLocationMappingUtils.parseWellPosition("Left", "5");
        assertEquals(expectedPos, pos);

    }

}

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

package ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto;

import java.util.HashMap;
import java.util.Map;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * Test of {@link WellFeatureVectorReference}
 * 
 * @author Tomasz Pylak
 */
public class WellFeatureVectorReferenceTest extends AssertJUnit
{
    @Test
    public void testHashCodeAndEquals()
    {
        Map<WellFeatureVectorReference, String> map =
                new HashMap<WellFeatureVectorReference, String>();
        WellFeatureVectorReference r1 =
                new WellFeatureVectorReference("ds1", new WellLocation(1, 1));
        assertEquals(r1, r1);

        PlateWellFeatureVectorReference r1bis =
                new PlateWellFeatureVectorReference("ds1", new WellLocation(1, 1), "plate");
        assertEquals(r1, r1bis);

        WellFeatureVectorReference r2 =
                new WellFeatureVectorReference("ds2", new WellLocation(2, 2));
        assertFalse(r1.equals(r2));

        map.put(r1, "r1");
        map.put(r2, "r2");
        assertEquals("r1", map.get(r1));
        assertEquals("r2", map.get(r2));
    }
}

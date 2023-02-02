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

package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.util.TestInstanceHostUtils;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class PlateImageReferenceTest extends AssertJUnit
{
    @Test
    public void testEquality()
    {
        PlateImageReference plate1 =
                new PlateImageReference(1, 2, 3, "channel1", getDatasetIdentifier());
        PlateImageReference plate2 =
                new PlateImageReference(1, 2, 3, "channel1", getDatasetIdentifier());
        assertEquals(plate1, plate2);
        assertEquals(plate1.hashCode(), plate2.hashCode());
    }

    @Test
    public void testNonEquality()
    {
        PlateImageReference plate1 =
                new PlateImageReference(1, 2, 3, "channel1", getDatasetIdentifier());
        PlateImageReference plate2 =
                new PlateImageReference(1, 2, 3, "channel2", getDatasetIdentifier());
        assertFalse(plate1.equals(plate2));
    }

    private IDatasetIdentifier getDatasetIdentifier()
    {
        return new IDatasetIdentifier()
            {

                @Override
                public String getDatasetCode()
                {
                    return "9834598723-9834";
                }

                @Override
                public String getPermId()
                {
                    return getDatasetCode();
                }

                @Override
                public String getDatastoreServerUrl()
                {
                    return TestInstanceHostUtils.getOpenBISUrl();
                }

            };
    }
}

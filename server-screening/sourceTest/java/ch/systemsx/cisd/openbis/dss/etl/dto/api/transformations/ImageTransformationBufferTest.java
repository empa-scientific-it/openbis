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

package ch.systemsx.cisd.openbis.dss.etl.dto.api.transformations;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.transformations.ImageTransformationBuffer;

/**
 * Test of {@link ImageTransformationBuffer}
 * 
 * @author Tomasz Pylak
 */
@Friend(toClasses = ImageTransformationBuffer.class)
public class ImageTransformationBufferTest extends AssertJUnit
{
    @Test
    public void testCreateAutoRescaleIntensityTransformationLabel()
    {
        String label =
                ImageTransformationBuffer
                        .createAutoRescaleIntensityTransformationLabel(null, 0.01f);
        assertEquals("Optimal (image, 1% cut)", label);

        label =
                ImageTransformationBuffer.createAutoRescaleIntensityTransformationLabel(null,
                        0.005f);
        assertEquals("Optimal (image, 0.5% cut)", label);
    }
}
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

package ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * Tests of {@link ImgChannelStackDTO}
 * 
 * @author Tomasz Pylak
 */
@Test(groups =
{ "screening" })
public class ImgChannelStackDTOTest
{
    @Test
    public void testEquals()
    {
        AssertJUnit.assertEquals(createStackChannel(), createStackChannel());
    }

    @Test
    public void testHashCode()
    {
        AssertJUnit.assertEquals(createStackChannel().hashCode(), createStackChannel().hashCode());
    }

    @Test
    public void testSpotlessHashCode()
    {
        AssertJUnit.assertEquals(createStackChannel(null).hashCode(), createStackChannel(null)
                .hashCode());
    }

    private ImgChannelStackDTO createStackChannel()
    {
        return createStackChannel(Long.valueOf(1));
    }

    private ImgChannelStackDTO createStackChannel(Long spotId)
    {
        return new ImgChannelStackDTO(0, 1, 1, 1, spotId, 123F, null, null, false);
    }

}

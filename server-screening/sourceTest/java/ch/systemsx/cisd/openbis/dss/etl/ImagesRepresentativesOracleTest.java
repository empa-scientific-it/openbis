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

package ch.systemsx.cisd.openbis.dss.etl;

import java.util.HashSet;
import java.util.Set;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.dss.etl.AbstractImageDatasetUploader.ChannelStackRepresentativesOracle;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgChannelStackDTO;

/**
 * @author Tomasz Pylak
 */
@Friend(toClasses = ChannelStackRepresentativesOracle.class)
public class ImagesRepresentativesOracleTest extends AssertJUnit
{
    @Test
    public void testWithSpots()
    {
        ImgChannelStackDTO img1 = createStack(2, 2, 2L, 1f, 1f);
        ImgChannelStackDTO img2 = createStack(1, 1, 1L, 200f, 200f);
        ImgChannelStackDTO img3 = createStack(1, 1, 1L, 1f, 1f);

        Set<ImgChannelStackDTO> representatives = calculateRepresentatives(img1, img2, img3);
        assertTrue(representatives.contains(img1));
        assertFalse(representatives.contains(img2));
        assertTrue(representatives.contains(img3));
    }

    private Set<ImgChannelStackDTO> calculateRepresentatives(ImgChannelStackDTO... stacks)
    {
        Set<ImgChannelStackDTO> stacksSet = new HashSet<ImgChannelStackDTO>();
        for (ImgChannelStackDTO stack : stacks)
        {
            stacksSet.add(stack);
        }
        Set<ImgChannelStackDTO> representatives =
                ChannelStackRepresentativesOracle.calculateRepresentatives(stacksSet);
        return representatives;
    }

    @Test
    public void testWithoutSpots()
    {
        ImgChannelStackDTO img1 = createStack(2, 2, null, 1f, 1f);
        ImgChannelStackDTO img2 = createStack(1, 1, null, 200f, 200f);
        ImgChannelStackDTO img3 = createStack(1, 1, null, 1f, 1f);

        Set<ImgChannelStackDTO> representatives = calculateRepresentatives(img1, img2, img3);
        assertFalse(representatives.contains(img1));
        assertFalse(representatives.contains(img2));
        assertTrue(representatives.contains(img3));
    }

    private ImgChannelStackDTO createStack(int row, int column, Long spotIdOrNull, Float tOrNull,
            Float zOrNull)
    {
        return new ImgChannelStackDTO(0L, row, column, 0L, spotIdOrNull, tOrNull, zOrNull, null,
                false);
    }
}

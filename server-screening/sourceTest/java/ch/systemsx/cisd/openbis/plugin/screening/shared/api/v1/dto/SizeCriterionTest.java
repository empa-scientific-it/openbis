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

package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

import java.util.Arrays;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * @author Franz-Josef Elmer
 */
public class SizeCriterionTest extends AssertJUnit
{
    @Test
    public void testMatchingExactly()
    {
        SizeCriterion criterion = new SizeCriterion(10, 20, SizeCriterion.Type.EXACTLY);
        List<ImageRepresentationFormat> result =
                criterion
                        .getMatching(Arrays.asList(format(10, 20), format(10, 21), format(11, 20)));
        assertEquals("[ImageRepresentationFormat[true,10,20,8,png,[]]]", result.toString());
    }

    @Test
    public void testMatchingCoveringBoundingBox()
    {
        SizeCriterion criterion =
                new SizeCriterion(10, 20, SizeCriterion.Type.COVERING_BOUNDING_BOX);
        List<ImageRepresentationFormat> result =
                criterion.getMatching(Arrays.asList(format(10, 20), format(10, 21), format(11, 20),
                        format(9, 20), format(10, 19)));
        assertEquals("[ImageRepresentationFormat[true,10,20,8,png,[]], "
                + "ImageRepresentationFormat[true,10,21,8,png,[]], "
                + "ImageRepresentationFormat[true,11,20,8,png,[]]]", result.toString());
    }

    @Test
    public void testMatchingSamllestCoveringBoundingBox()
    {
        SizeCriterion criterion =
                new SizeCriterion(10, 20, SizeCriterion.Type.SMALLEST_COVERING_BOUNDING_BOX);
        List<ImageRepresentationFormat> result =
                criterion.getMatching(Arrays.asList(format(20, 20), format(9, 21), format(21, 20)));
        assertEquals("[ImageRepresentationFormat[true,20,20,8,png,[]]]", result.toString());
    }

    @Test
    public void testMatchingInsideBoundingBox()
    {
        SizeCriterion criterion = new SizeCriterion(10, 20, SizeCriterion.Type.INSIDE_BOUNDING_BOX);
        List<ImageRepresentationFormat> result =
                criterion.getMatching(Arrays.asList(format(10, 20), format(10, 21), format(11, 20),
                        format(9, 20), format(10, 19)));
        assertEquals("[ImageRepresentationFormat[true,10,20,8,png,[]], "
                + "ImageRepresentationFormat[true,9,20,8,png,[]], "
                + "ImageRepresentationFormat[true,10,19,8,png,[]]]", result.toString());
    }

    @Test
    public void testMatchingLargestInBoundingBox()
    {
        SizeCriterion criterion =
                new SizeCriterion(10, 20, SizeCriterion.Type.LARGEST_IN_BOUNDING_BOX);
        List<ImageRepresentationFormat> result =
                criterion.getMatching(Arrays.asList(format(10, 21), format(1, 2), format(9, 20),
                        format(10, 19)));
        assertEquals("[ImageRepresentationFormat[true,10,19,8,png,[]]]", result.toString());
    }

    private ImageRepresentationFormat format(Integer width, Integer height)
    {
        return new ImageRepresentationFormat("ds1", 1, true, width, height, 8, "png", null);
    }
}

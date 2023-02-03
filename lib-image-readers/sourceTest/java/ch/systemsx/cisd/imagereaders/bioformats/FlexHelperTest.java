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

package ch.systemsx.cisd.imagereaders.bioformats;

import java.io.File;
import java.util.Map;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.geometry.SpatialPoint;
import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.imagereaders.ImageReaderTestCase;

/**
 * @author Kaloyan Enimanev
 */
public class FlexHelperTest extends ImageReaderTestCase
{

    @Test
    public void testSimpleFile() throws Exception
    {
        File imageFile = getFlexFile("032045000.flex");
        FlexHelper helper = new FlexHelper(imageFile.getAbsolutePath());

        assertEquals(1, helper.getTileNumber(0));
        assertEquals("Exp1Cam1", helper.getChannelCode(0));
        assertEquals("488", helper.getExcitationTag(0));
        assertEquals("565/40", helper.getEmissionTag(0));

        Map<Integer, SpatialPoint> tileCoordinates = helper.getTileCoordinates();
        assertEquals(1, tileCoordinates.size());
        assertEquals(new SpatialPoint(0, 0), tileCoordinates.get(1));

        String xml = helper.getMetadata();
        assertFalse(StringUtils.isBlank(xml));
    }

    @Test
    public void testComplexFile() throws Exception
    {
        File imageFile = getFlexFile("001001000.flex");
        // File imageFile = getImageFileForLibrary(libraryName, "032045000.flex");
        FlexHelper helper = new FlexHelper(imageFile.getAbsolutePath());

        final String[] EXPOSURES = new String[]
            { "Exp1", "Exp1", "Exp2" };
        final String[] CAMS = new String[]
            { "Cam1", "Cam3", "Cam2" };
        final String[] EXCITATION = new String[]
            { "561,405", "561,405", "488" };
        final String[] EMISSION = new String[]
            { "450/50", "690/70", "565/40" };

        for (int i = 0; i < 12; i++)
        {
            int parsedTile = helper.getTileNumber(i);
            assertEquals((i / 3) + 1, parsedTile);
            String channelName = EXPOSURES[i % 3] + CAMS[i % 3];
            assertEquals(channelName, helper.getChannelCode(i));
            assertEquals(EXCITATION[i % 3], helper.getExcitationTag(i));
            assertEquals(EMISSION[i % 3], helper.getEmissionTag(i));
        }

        Map<Integer, SpatialPoint> tileCoordinates = helper.getTileCoordinates();
        assertEquals(4, tileCoordinates.size());
        assertEquals(new SpatialPoint(4.44E-4, 6.61E-4), tileCoordinates.get(1));
        assertEquals(new SpatialPoint(-4.44E-4, 6.61E-4), tileCoordinates.get(2));
        assertEquals(new SpatialPoint(-4.44E-4, -6.61E-4), tileCoordinates.get(3));
        assertEquals(new SpatialPoint(4.44E-4, -6.61E-4), tileCoordinates.get(4));

        String xml = helper.getMetadata();
        assertFalse(StringUtils.isBlank(xml));
    }

    private File getFlexFile(String fileName)
    {
        File flexImagesDir = new File(IMAGES_DIR, "flex-helper");
        return new File(flexImagesDir, fileName);
    }
}

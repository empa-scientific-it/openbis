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

import java.awt.image.BufferedImage;
import java.io.File;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.image.IImageTransformer;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.transformations.ConvertToolImageTransformerFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ImageUtilTest;

/**
 * @author Kaloyan Enimanev
 */
public class ConvertToolImageTransformerTest extends AssertJUnit
{
    private final String IMAGE_FOLDER = "./resource/test-data/" + getClass().getSimpleName();

    private final String CONVERT_PARAMS =
            " -contrast-stretch 0 -edge 1 -threshold 1 -transparent white ";

    @Test(groups = "slow")
    public void testTransformation()
    {
        BufferedImage templateImage = readImage("pond.png");
        BufferedImage expected = readImage("pond-transformed.png");

        BufferedImage transformationResult = createTransformer().transform(templateImage);

        assertNotNull(transformationResult);
        assertEqualImages(expected, transformationResult);

    }

    private IImageTransformer createTransformer()
    {
        ConvertToolImageTransformerFactory factory =
                new ConvertToolImageTransformerFactory(CONVERT_PARAMS);
        return factory.createTransformer();
    }

    private void assertEqualImages(BufferedImage expected, BufferedImage actual)
    {

        int columns = expected.getWidth();
        int rows = expected.getHeight();

        String notEqualError = "Converted image not equal to expected result";
        assertEquals(notEqualError, columns, actual.getWidth());
        assertEquals(notEqualError, rows, actual.getHeight());
    }

    private BufferedImage readImage(String image)
    {
        File imageFile = new File(IMAGE_FOLDER, image);
        return ImageUtilTest.loadImage(imageFile);
    }

}

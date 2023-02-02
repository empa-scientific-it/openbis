/*
 * Copyright 2014 ETH Zuerich, SIS
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

package ch.systemsx.cisd.common.image;

import static org.testng.AssertJUnit.assertEquals;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.image.ImageHistogram;

/**
 * @author Franz-Josef Elmer
 */
public class ImageHistogramTest
{
    @Test
    public void testImageTypeIntRGB()
    {
        ImageHistogram histogram = testImageType(BufferedImage.TYPE_INT_RGB, false);

        assertEquals("+-----+-----+-----+\n"
                + "|*    |*    |*    |\n"
                + "|*    |*    |*    |\n"
                + "|*   *|*  * |*    |\n"
                + "|*___*|*__*_|*____|\n"
                + "  red  green blue  \n", histogram.renderAsASCIIChart(19, 6));
    }

    @Test
    public void testImageTypeIntARGB()
    {
        testImageType(BufferedImage.TYPE_INT_ARGB, false);
    }

    @Test
    public void testImageTypeIntARGBPRE()
    {
        testImageType(BufferedImage.TYPE_INT_ARGB_PRE, false);
    }

    @Test
    public void testImageTypeIntBGR()
    {
        testImageType(BufferedImage.TYPE_INT_BGR, false);
    }

    @Test
    public void testImageType3ByteBGR()
    {
        testImageType(BufferedImage.TYPE_3BYTE_BGR, false);
    }

    @Test
    public void testImageType4ByteABGR()
    {
        testImageType(BufferedImage.TYPE_4BYTE_ABGR, false);
    }

    @Test
    public void testImageType4ByteABGRPRE()
    {
        testImageType(BufferedImage.TYPE_4BYTE_ABGR_PRE, false);
    }

    @Test
    public void testImageTypeByteGray()
    {
        testImageType(BufferedImage.TYPE_BYTE_GRAY, true);
    }

    @Test
    public void testImageTypeShortGray()
    {
        ImageHistogram histogram = testImageType(BufferedImage.TYPE_USHORT_GRAY, true);

        assertEquals("+-----------------+\n"
                + "|*                |\n"
                + "|*                |\n"
                + "|*           *    |\n"
                + "|*___________*____|\n"
                + "       gray        \n", histogram.renderAsASCIIChart(19, 6));
    }

    @Test
    public void testImageTypeShort555RGB()
    {
        ImageHistogram histogram = ImageHistogram.calculateHistogram(createImage(BufferedImage.TYPE_USHORT_555_RGB));

        assertHistogram("[0=9, 31=6]", histogram.getRedHistogram());
        assertHistogram("[0=9, 25=6]", histogram.getGreenHistogram());
        assertHistogram("[0=15]", histogram.getBlueHistogram());
    }

    @Test
    public void testImageTypeShort565RGB()
    {
        ImageHistogram histogram = ImageHistogram.calculateHistogram(createImage(BufferedImage.TYPE_USHORT_565_RGB));

        assertHistogram("[0=9, 31=6]", histogram.getRedHistogram());
        assertHistogram("[0=9, 50=6]", histogram.getGreenHistogram());
        assertHistogram("[0=15]", histogram.getBlueHistogram());
    }

    @Test
    public void testImageTypeByteIndexed()
    {
        ImageHistogram histogram = ImageHistogram.calculateHistogram(createImage(BufferedImage.TYPE_BYTE_INDEXED));

        assertHistogram("[0=9, 255=6]", histogram.getRedHistogram());
        assertHistogram("[0=9, 204=6]", histogram.getGreenHistogram());
        assertHistogram("[0=15]", histogram.getBlueHistogram());
    }

    private ImageHistogram testImageType(int imageType, boolean gray)
    {
        BufferedImage image = createImage(imageType);
        ImageHistogram histogram = ImageHistogram.calculateHistogram(image);
        if (gray)
        {
            assertHistogram("[0=9, 194=6]", histogram.getRedHistogram());
            assertHistogram("[0=9, 194=6]", histogram.getGreenHistogram());
            assertHistogram("[0=9, 194=6]", histogram.getBlueHistogram());
        } else
        {
            assertHistogram("[0=9, 255=6]", histogram.getRedHistogram());
            assertHistogram("[0=9, 200=6]", histogram.getGreenHistogram());
            assertHistogram("[0=15]", histogram.getBlueHistogram());
        }
        return histogram;
    }

    private BufferedImage createImage(int imageType)
    {
        BufferedImage image = new BufferedImage(5, 3, imageType);
        Graphics graphics = image.getGraphics();
        graphics.setColor(Color.ORANGE);
        graphics.fillRect(0, 0, 2, 3);
        return image;
    }

    private void assertHistogram(String renderedExpectValues, int[] histogram)
    {
        assertEquals(renderedExpectValues, renderHistogram(histogram));
    }

    private String renderHistogram(int[] histogram)
    {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < histogram.length; i++)
        {
            int value = histogram[i];
            if (value > 0)
            {
                if (builder.length() > 0)
                {
                    builder.append(", ");
                }
                builder.append(i).append("=").append(value);
            }
        }
        return "[" + builder.toString() + "]";
    }

}

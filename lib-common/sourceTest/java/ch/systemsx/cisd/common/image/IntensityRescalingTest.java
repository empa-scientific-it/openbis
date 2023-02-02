/*
 * Copyright 2013 ETH Zuerich, CISD
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
import java.awt.image.ColorModel;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.image.IntensityRescaling.Channel;
import ch.systemsx.cisd.common.image.IntensityRescaling.Levels;
import ch.systemsx.cisd.common.image.IntensityRescaling.Pixels;

/**
 * @author Jakub Straszewski
 */
@Test
public class IntensityRescalingTest
{

    public void testComputeLevelsRegularCaseOfIntensityRescaling()
    {
        int[] histogramArray =
                new int[]
                { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
        Levels result = IntensityRescaling.computeLevels(5, histogramArray);
        assertEquals(5, result.minLevel);
        assertEquals(histogramArray.length - 6, result.maxLevel);
    }

    @Test
    public void testComputeLevelsBorderCaseOfIntensityRescaling()
    {
        Levels result = IntensityRescaling.computeLevels(14, new int[]
        { 1098, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 });
        assertEquals(0, result.minLevel);
        assertEquals(1, result.maxLevel);
    }

    @Test
    public void testComputeLevelsBorderCaseOfIntensityRescalingOtherWay()
    {
        Levels result = IntensityRescaling.computeLevels(14, new int[]
        { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 19876 });
        assertEquals(11, result.minLevel);
        assertEquals(12, result.maxLevel);
    }

    @Test
    public void testComputeLevelsAnotherBorderCase()
    {
        Levels result = IntensityRescaling.computeLevels(14, new int[]
        { 19876, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
        assertEquals(0, result.minLevel);
        assertEquals(1, result.maxLevel);
    }

    @Test
    public void testRescaleIntensityLevelTo8BitsForRGBExampleAndTwoColors()
    {
        BufferedImage image = new BufferedImage(6, 5, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = image.getGraphics();
        graphics.setColor(Color.ORANGE);
        graphics.fillRect(0, 0, 4, 3);
        graphics.setColor(Color.PINK);
        graphics.fillRect(1, 1, 4, 3);

        BufferedImage rescaledImage = IntensityRescaling.rescaleIntensityLevelTo8Bits(new Pixels(image),
                new Levels(75, 190), Channel.GREEN, Channel.BLUE);

        ImageHistogram histogram = ImageHistogram.calculateHistogram(rescaledImage);

        assertEquals("[0=30]", renderHistogram(histogram.getRedHistogram()));
        assertEquals("[0=12, 222=12, 255=6]", renderHistogram(histogram.getGreenHistogram()));
        assertEquals("[0=18, 222=12]", renderHistogram(histogram.getBlueHistogram()));
    }

    @Test
    public void testRescaleIntensityLevelTo8BitsForRGBExampleAndAllColors()
    {
        BufferedImage image = new BufferedImage(6, 5, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = image.getGraphics();
        graphics.setColor(Color.ORANGE);
        graphics.fillRect(0, 0, 4, 3);
        graphics.setColor(Color.PINK);
        graphics.fillRect(1, 1, 4, 3);

        BufferedImage rescaledImage = IntensityRescaling.rescaleIntensityLevelTo8Bits(new Pixels(image),
                new Levels(75, 190), Channel.values());

        ImageHistogram histogram = ImageHistogram.calculateHistogram(rescaledImage);

        assertEquals("[0=12, 255=18]", renderHistogram(histogram.getRedHistogram()));
        assertEquals("[0=12, 175=12, 200=6]", renderHistogram(histogram.getGreenHistogram()));
        assertEquals("[0=18, 175=12]", renderHistogram(histogram.getBlueHistogram()));
    }

    @Test
    public void testRescaleIntensityLevelForAnIndexColorModel()
    {
        BufferedImage image = new BufferedImage(6, 5, BufferedImage.TYPE_BYTE_INDEXED);
        ColorModel colorModel = image.getColorModel();
        assertEquals("IndexColorModel", colorModel.getClass().getSimpleName());
        Graphics graphics = image.getGraphics();
        graphics.setColor(Color.ORANGE);
        graphics.fillRect(0, 0, 4, 3);
        graphics.setColor(Color.PINK);
        graphics.fillRect(1, 1, 4, 3);

        BufferedImage rescaledImage = IntensityRescaling.rescaleIntensityLevelTo8Bits(new Pixels(image),
                new Levels(75, 190), Channel.values());

        ImageHistogram histogram = ImageHistogram.calculateHistogram(rescaledImage);

        assertEquals("[0=12, 255=18]", renderHistogram(histogram.getRedHistogram()));
        assertEquals("[0=12, 153=12, 204=6]", renderHistogram(histogram.getGreenHistogram()));
        assertEquals("[0=18, 153=12]", renderHistogram(histogram.getBlueHistogram()));
    }

    @Test
    public void testRescaleIntensityLevelTo8BitsForGrayExample()
    {
        BufferedImage image = new BufferedImage(6, 5, BufferedImage.TYPE_BYTE_GRAY);
        Graphics graphics = image.getGraphics();
        graphics.setColor(Color.DARK_GRAY);
        graphics.fillRect(0, 0, 4, 3);
        graphics.setColor(Color.LIGHT_GRAY);
        graphics.fillRect(1, 1, 4, 3);

        BufferedImage rescaledImage = IntensityRescaling.rescaleIntensityLevelTo8Bits(
                new Pixels(image), new Levels(75, 200), Channel.RED);

        ImageHistogram histogram = ImageHistogram.calculateHistogram(rescaledImage);

        assertEquals("[0=18, 239=12]", renderHistogram(histogram.getRedHistogram()));
        assertEquals("[0=18, 239=12]", renderHistogram(histogram.getGreenHistogram()));
        assertEquals("[0=18, 239=12]", renderHistogram(histogram.getBlueHistogram()));
    }

    @Test
    public void testRescaleIntensityLevelTo16BitsForGrayExample()
    {
        BufferedImage image = new BufferedImage(6, 5, BufferedImage.TYPE_USHORT_GRAY);
        Graphics graphics = image.getGraphics();
        graphics.setColor(Color.DARK_GRAY);
        graphics.fillRect(0, 0, 4, 3);
        graphics.setColor(Color.LIGHT_GRAY);
        graphics.fillRect(1, 1, 4, 3);

        BufferedImage rescaledImage = IntensityRescaling.rescaleIntensityLevelTo8Bits(
                new Pixels(image), new Levels(75, 200), Channel.RED);

        ImageHistogram histogram = ImageHistogram.calculateHistogram(rescaledImage);

        assertEquals("[0=12, 255=18]", renderHistogram(histogram.getRedHistogram()));
        assertEquals("[0=12, 255=18]", renderHistogram(histogram.getGreenHistogram()));
        assertEquals("[0=12, 255=18]", renderHistogram(histogram.getBlueHistogram()));
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

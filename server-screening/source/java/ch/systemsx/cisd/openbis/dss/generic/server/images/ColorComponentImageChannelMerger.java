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

package ch.systemsx.cisd.openbis.dss.generic.server.images;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ColorComponent;

/**
 * Handles the special case of merging image channels which in fact are encoded on the same image as different color components. It makes sense to use
 * this class only for exactly two channels.
 * 
 * @author Tomasz Pylak
 */
class ColorComponentImageChannelMerger
{
    public static BufferedImage mergeByExtractingComponents(BufferedImage[] images,
            ColorComponent[] colorComponents)
    {
        assert images.length > 1 : "more than 1 image expected, but found: " + images.length;

        BufferedImage newImage = createNewRGBImage(images[0]);
        int width = newImage.getWidth();
        int height = newImage.getHeight();
        int colorBuffer[] = new int[4];
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                int mergedRGB = mergeRGBColor(images, colorComponents, x, y, colorBuffer);
                newImage.setRGB(x, y, mergedRGB);
            }
        }
        return newImage;
    }

    private static int mergeRGBColor(BufferedImage[] images, ColorComponent[] colorComponents,
            int x, int y, int colorBuffer[])
    {
        Arrays.fill(colorBuffer, 0);
        for (int index = 0; index < images.length; index++)
        {
            int rgb = images[index].getRGB(x, y);
            Color singleColor = new Color(rgb, true);
            setColorComponents(colorBuffer, singleColor, colorComponents[index]);
            // merge alpha channel
            colorBuffer[3] = Math.max(colorBuffer[3], singleColor.getAlpha());
        }
        return asRGB(colorBuffer);
    }

    private static void setColorComponents(int[] colorBuffer, Color singleColor,
            ColorComponent colorComponent)
    {
        int index = getColorComponentIndex(colorComponent);
        colorBuffer[index] = colorComponent.getComponent(singleColor);
    }

    private static int getColorComponentIndex(ColorComponent colorComponent)
    {
        switch (colorComponent)
        {
            case RED:
                return 0;
            case GREEN:
                return 1;
            case BLUE:
                return 2;
            default:
                throw new IllegalStateException("Unknown color " + colorComponent);
        }
    }

    private static int asRGB(int[] rgb)
    {
        return new Color(rgb[0], rgb[1], rgb[2], rgb[3]).getRGB();
    }

    // NOTE: drawing on this image will not preserve transparency - but we do not need it and the
    // image is smaller
    private static BufferedImage createNewRGBImage(BufferedImage bufferedImage)
    {
        return new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(),
                BufferedImage.TYPE_INT_RGB);
    }

}

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

package ch.systemsx.cisd.openbis.plugin.screening.client.api.v1;

import java.awt.image.BufferedImage;

import ch.systemsx.cisd.base.annotation.JsonObject;

import ch.systemsx.cisd.base.image.IImageTransformer;
import ch.systemsx.cisd.base.image.IImageTransformerFactory;

/**
 * @author Franz-Josef Elmer
 */
@JsonObject("ExampleImageTransformerFactory")
public class ExampleImageTransformerFactory implements IImageTransformerFactory
{
    private static final long serialVersionUID = 1L;

    private static final class Color
    {
        private int red;

        private int green;

        private int blue;

        Color(int rgb)
        {
            red = (rgb >> 16) & 0xff;
            green = (rgb >> 8) & 0xff;
            blue = rgb & 0xff;
        }

        int getColor(char colorSymbol)
        {
            switch (colorSymbol)
            {
                case 'r':
                    return red;
                case 'g':
                    return green;
                case 'b':
                    return blue;
                default:
                    return 0;
            }
        }
    }

    private final String colorPattern;

    private final int brightnessDelta;

    public ExampleImageTransformerFactory(String colorPattern)
    {
        this(colorPattern, 0);
    }

    public ExampleImageTransformerFactory(int brightnessDelta)
    {
        this("rgb", brightnessDelta);
    }

    public ExampleImageTransformerFactory(String colorPattern, int brightnessDelta)
    {
        this.colorPattern = colorPattern;
        this.brightnessDelta = brightnessDelta;
    }

    @Override
    public IImageTransformer createTransformer()
    {
        return new IImageTransformer()
            {
                @Override
                public BufferedImage transform(BufferedImage input)
                {
                    int width = input.getWidth();
                    int height = input.getHeight();
                    BufferedImage output =
                            new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                    for (int x = 0; x < width; x++)
                    {
                        for (int y = 0; y < height; y++)
                        {
                            int rgb = input.getRGB(x, y);
                            Color color = new Color(rgb);
                            output.setRGB(x, y,
                                    (calcNewColor(color, 0) << 16) + (calcNewColor(color, 1) << 8)
                                            + calcNewColor(color, 2));
                        }
                    }
                    return output;
                }

                private int calcNewColor(Color color, int colorIndex)
                {
                    return color.getColor(colorPattern.charAt(colorIndex)) + brightnessDelta;
                }
            };
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + "[" + colorPattern + "]";
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + brightnessDelta;
        result = prime * result + ((colorPattern == null) ? 0 : colorPattern.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ExampleImageTransformerFactory other = (ExampleImageTransformerFactory) obj;
        if (brightnessDelta != other.brightnessDelta)
            return false;
        if (colorPattern == null)
        {
            if (other.colorPattern != null)
                return false;
        } else if (!colorPattern.equals(other.colorPattern))
            return false;
        return true;
    }

}

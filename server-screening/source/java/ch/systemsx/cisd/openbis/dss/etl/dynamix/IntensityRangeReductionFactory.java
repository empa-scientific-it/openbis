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

package ch.systemsx.cisd.openbis.dss.etl.dynamix;

import java.awt.image.BufferedImage;

import ch.systemsx.cisd.base.annotation.JsonObject;

import ch.systemsx.cisd.base.image.IImageTransformer;
import ch.systemsx.cisd.base.image.IImageTransformerFactory;

/**
 * Image transformation factory which reduces the intensity of the 8 bit grayscale images, so that only the colors from the given range are used.
 * <p>
 * Do not change this class, it is stored serialized in the database and the code should be considered frozen!
 * </p>
 * 
 * @author Tomasz Pylak
 */
@JsonObject("IntensityRangeReductionFactory")
public class IntensityRangeReductionFactory implements IImageTransformerFactory
{
    private static final long serialVersionUID = 1L;

    private final int precomputedColors[];

    public IntensityRangeReductionFactory(int min, int max)
    {
        if (min < 0 || max > 255)
        {
            throw new IllegalStateException(String.format(
                    "Invalid range [%d, %d]. Min should be >= 0 and max should be <= 255!", min,
                    max));
        }
        double rescalingFactor = (max - min) / 255.0;
        this.precomputedColors = new int[256];
        for (int i = 0; i < precomputedColors.length; i++)
        {
            precomputedColors[i] = (int) (rescalingFactor * i + min);
        }
    }

    @Override
    public IImageTransformer createTransformer()
    {
        return new IImageTransformer()
            {
                @Override
                public BufferedImage transform(BufferedImage image)
                {
                    return reduceIntensityRange(image);
                }
            };
    }

    private BufferedImage reduceIntensityRange(BufferedImage image)
    {
        if (image.getColorModel().getComponentSize(0) > 8)
        {
            return image; // more than 8 bits
        }
        BufferedImage newImage =
                new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        for (int x = 0; x < image.getWidth(); x++)
        {
            for (int y = 0; y < image.getHeight(); y++)
            {
                int color = image.getRaster().getSample(x, y, 0);
                int rescaledColor = precomputedColors[color];
                newImage.getRaster().setSample(x, y, 0, rescaledColor);
            }
        }
        return newImage;
    }
}

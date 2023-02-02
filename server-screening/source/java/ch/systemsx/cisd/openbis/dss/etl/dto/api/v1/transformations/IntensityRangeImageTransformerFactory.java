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

package ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.transformations;

import java.awt.image.BufferedImage;
import java.util.EnumSet;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.base.image.IImageTransformer;
import ch.systemsx.cisd.base.image.IImageTransformerFactory;
import ch.systemsx.cisd.common.image.IntensityRescaling;
import ch.systemsx.cisd.common.image.IntensityRescaling.Channel;
import ch.systemsx.cisd.common.image.IntensityRescaling.Levels;
import ch.systemsx.cisd.common.image.IntensityRescaling.Pixels;
import ch.systemsx.cisd.openbis.dss.shared.DssScreeningUtils;

/**
 * This class is obsolete, and should not be used. Use
 * {@link ch.systemsx.cisd.openbis.dss.etl.dto.api.transformations.IntensityRangeImageTransformerFactory} instead
 * 
 * @author Jakub Straszewski
 */
@JsonObject("IntensityRangeImageTransformerFactory_obsolete")
public class IntensityRangeImageTransformerFactory implements IImageTransformerFactory
{
    private static final long serialVersionUID = 1L;

    private final int blackPointIntensity;

    private final int whitePointIntensity;

    public IntensityRangeImageTransformerFactory(int blackPointIntensity, int whitePointIntensity)
    {
        this.blackPointIntensity = blackPointIntensity;
        this.whitePointIntensity = whitePointIntensity;
    }

    public int getBlackPointIntensity()
    {
        return blackPointIntensity;
    }

    public int getWhitePointIntensity()
    {
        return whitePointIntensity;
    }

    @Override
    public IImageTransformer createTransformer()
    {
        return new IImageTransformer()
            {
                @Override
                public BufferedImage transform(BufferedImage image)
                {
                    if (IntensityRescaling.isNotGrayscale(image))
                    {
                        EnumSet<Channel> channels = IntensityRescaling.getUsedRgbChannels(image);
                        if (channels.size() != 1)
                        {
                            return image;
                        } else
                        {
                            Levels levels = new Levels(blackPointIntensity, whitePointIntensity);
                            Pixels pixels = DssScreeningUtils.createPixels(image);
                            return IntensityRescaling.rescaleIntensityLevelTo8Bits(pixels, levels,
                                    channels.iterator().next());
                        }
                    }
                    Levels levels = new Levels(blackPointIntensity, whitePointIntensity);
                    Pixels pixels = DssScreeningUtils.createPixels(image);
                    return IntensityRescaling.rescaleIntensityLevelTo8Bits(pixels, levels, Channel.values());
                }
            };
    }
}

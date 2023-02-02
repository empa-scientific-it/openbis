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

package ch.systemsx.cisd.openbis.dss.etl;

import java.util.List;

import ch.systemsx.cisd.hcs.Location;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.ImageChannelStackReference;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.RequestedImageSize;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageChannelStack;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;

/**
 * @author Pawel Glyzewski
 */
public class ImagingLoaderStrategyFactory
{
    private static abstract class AbstractLoaderStrategy implements IImagingLoaderStrategy
    {
        protected final IImagingDatasetLoader imageAccessor;

        private AbstractLoaderStrategy(IImagingDatasetLoader imageAccessor)
        {
            this.imageAccessor = imageAccessor;
        }

        @Override
        public ImageDatasetParameters getImageParameters()
        {
            return imageAccessor.getImageParameters();
        }

        @Override
        public List<ImageChannelStack> listImageChannelStacks(WellLocation wellLocationOrNull)
        {
            return imageAccessor.listImageChannelStacks(wellLocationOrNull);
        }
    }

    public static IImagingLoaderStrategy createThumbnailLoaderStrategy(
            final IImagingDatasetLoader imageAccessor)
    {
        return new AbstractLoaderStrategy(imageAccessor)
            {
                @Override
                public AbsoluteImageReference tryGetImage(String channelCode,
                        ImageChannelStackReference channelStackReference,
                        RequestedImageSize imageSize, String transformationCodeOrNull)
                {
                    return this.imageAccessor.tryGetThumbnail(channelCode, channelStackReference,
                            imageSize, transformationCodeOrNull);
                }

                @Override
                public AbsoluteImageReference tryGetRepresentativeImage(String channelCode,
                        Location wellLocationOrNull, RequestedImageSize imageSize,
                        String transformationCodeOrNull)
                {
                    return imageAccessor.tryGetRepresentativeThumbnail(channelCode,
                            wellLocationOrNull, imageSize, transformationCodeOrNull);
                }

            };
    }

    public static IImagingLoaderStrategy createImageLoaderStrategy(
            final IImagingDatasetLoader imageAccessor)
    {
        return new AbstractLoaderStrategy(imageAccessor)
            {
                @Override
                public AbsoluteImageReference tryGetImage(String channelCode,
                        ImageChannelStackReference channelStackReference,
                        RequestedImageSize imageSize, String transformationCodeOrNull)
                {
                    return this.imageAccessor.tryGetImage(channelCode, channelStackReference,
                            imageSize, transformationCodeOrNull);
                }

                @Override
                public AbsoluteImageReference tryGetRepresentativeImage(String channelCode,
                        Location wellLocationOrNull, RequestedImageSize imageSize,
                        String transformationCodeOrNull)
                {
                    return imageAccessor.tryGetRepresentativeImage(channelCode, wellLocationOrNull,
                            imageSize, transformationCodeOrNull);
                }
            };
    }
}

/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.base.image.IImageTransformer;
import ch.systemsx.cisd.base.image.IImageTransformerFactory;
import ch.systemsx.cisd.common.image.IntensityRescaling;
import ch.systemsx.cisd.common.image.IntensityRescaling.Pixels;
import ch.systemsx.cisd.openbis.dss.shared.DssScreeningUtils;

/**
 * This class is obsolete, and should not be used. Use {@link ch.systemsx.cisd.openbis.dss.etl.dto.api.transformations.ConvertToolImageTransformer}
 * instead Warning: The serialized version of this class can be stored in the database for each image. Moving this class to a different package or
 * changing it in a backward incompatible way would make all the saved transformations invalid.
 * 
 * @author Jakub Straszewski
 */
@JsonObject("BitShiftingImageTransformerFactory_obsolete")
final class BitShiftingImageTransformerFactory implements IImageTransformerFactory
{
    private static final long serialVersionUID = 1L;

    private final int shiftBits;

    public BitShiftingImageTransformerFactory(int shiftBits)
    {
        this.shiftBits = shiftBits;
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
                        return image;
                    }
                    Pixels pixels = DssScreeningUtils.createPixels(image);
                    return IntensityRescaling.rescaleIntensityBitShiftTo8Bits(pixels, shiftBits);
                }
            };
    }
}
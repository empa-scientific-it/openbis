package ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.transformations;

import java.awt.image.BufferedImage;


import ch.systemsx.cisd.base.image.IImageTransformer;
import ch.systemsx.cisd.base.image.IImageTransformerFactory;
import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.common.image.IntensityRescaling;

/**
 * Transformation performed by
 * {@link IntensityRescaling#rescaleIntensityBitShiftTo8Bits(java.awt.image.BufferedImage, int)}
 * <p>
 * Warning: The serialized version of this class can be stored in the database for each image.
 * Moving this class to a different package or changing it in a backward incompatible way would make
 * all the saved transformations invalid.
 * 
 * @author Tomasz Pylak
 */
@JsonObject("BitShiftingImageTransformerFactory")
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
                    return IntensityRescaling.rescaleIntensityBitShiftTo8Bits(image, shiftBits);
                }
            };
    }
}
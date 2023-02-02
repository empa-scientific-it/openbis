package ch.systemsx.cisd.openbis.dss.etl.dto.api.transformations;

import java.awt.image.BufferedImage;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.base.image.IImageTransformer;
import ch.systemsx.cisd.base.image.IImageTransformerFactory;
import ch.systemsx.cisd.common.image.IntensityRescaling;
import ch.systemsx.cisd.common.image.IntensityRescaling.Pixels;
import ch.systemsx.cisd.openbis.dss.shared.DssScreeningUtils;

/**
 * Transformation performed by {@link IntensityRescaling#rescaleIntensityBitShiftTo8Bits(Pixels, int)}
 * <p>
 * Warning: The serialized version of this class can be stored in the database for each image. Moving this class to a different package or changing it
 * in a backward incompatible way would make all the saved transformations invalid.
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
                    Pixels pixels = DssScreeningUtils.createPixels(image);
                    return IntensityRescaling.rescaleIntensityBitShiftTo8Bits(pixels, shiftBits);
                }
            };
    }
}
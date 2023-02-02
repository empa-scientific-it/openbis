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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import ch.systemsx.cisd.base.image.IImageTransformerFactory;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.etl.dynamix.IntensityRangeReductionFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.ColorRangeCalculator.ImagePixelsRange;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ImageUtil;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageEnrichedDTO;

/**
 * Rescales colors so that all images of one well have comparable brightness. Used by DynamiX project.
 * <p>
 * Currently openBIS converts 16 bit grayscale images to 8 bit. This class has to revert this conversion and apply the correct one (taking all images
 * of a well into account). We cannot avoid double convertions before openBIS supports 16bit images better.
 * <p>
 * Limitations:<br>
 * 1) works on the first color component of the images (perfect for e.g. grayscale images).<br>
 * 2) Can process only tiff images which can be read by JAI library.
 * 
 * @author Tomasz Pylak
 */
public class DynamixWellBrightnessEqualizerProcessingPlugin extends
        AbstractSpotImagesTransformerProcessingPlugin
{
    private static final long serialVersionUID = 1L;

    // Colors which are smaller or larger will be ignored when the rescaling parameters are
    // calculated.
    // If the encountered minimum and maximum is equal to the spcified, no rescaling happens.
    private final int minRescaledColor, maxRescaledColor;

    public DynamixWellBrightnessEqualizerProcessingPlugin(Properties properties, File storeRoot)
    {
        super(properties, storeRoot);
        this.minRescaledColor = PropertyUtils.getInt(properties, "min-color", 1);
        this.maxRescaledColor = PropertyUtils.getInt(properties, "max-color", 16384 - 1);
    }

    @Override
    protected IImageTransformerFactoryProvider getTransformationProvider(
            List<ImgImageEnrichedDTO> spotImages, IHierarchicalContent hierarchicalContent)
    {
        if (spotImages.size() < 2)
        {
            return NO_TRANSFORMATION_PROVIDER;
        }
        final Map<ImgImageEnrichedDTO, ImagePixelsRange> rangeMap =
                new HashMap<ImgImageEnrichedDTO, ImagePixelsRange>();
        for (ImgImageEnrichedDTO image : spotImages)
        {
            BufferedImage bufferedImage = loadImage(hierarchicalContent, image);
            ImagePixelsRange imageRange =
                    ColorRangeCalculator.calculatePixelsRange(bufferedImage, minRescaledColor,
                            maxRescaledColor);
            rangeMap.put(image, imageRange);
        }
        return createImageTransformerFactoryProvider(rangeMap);
    }

    // --- DynamiX specific part ----------------

    private static BufferedImage loadImage(IHierarchicalContent hierarchicalContent,
            ImgImageEnrichedDTO image)
    {
        IHierarchicalContentNode contentNode = hierarchicalContent.getNode(image.getFilePath());
        return ImageUtil.loadJavaAdvancedImagingTiff(contentNode.getFileContent(),
                ImageUtil.parseImageID(image.getImageID(), null));
    }

    private static IImageTransformerFactoryProvider createImageTransformerFactoryProvider(
            final Map<ImgImageEnrichedDTO, ImagePixelsRange> rangeMap)
    {
        final ImagePixelsRange globalRange =
                ColorRangeCalculator.calculateOverlapRange(rangeMap.values());
        return new IImageTransformerFactoryProvider()
            {
                @Override
                public IImageTransformerFactory tryGetTransformationFactory(
                        ImgImageEnrichedDTO image)
                {
                    ImagePixelsRange imageRange = rangeMap.get(image);
                    ImagePixelsRange rescaledRange =
                            ColorRangeCalculator.rescaleRange(imageRange, globalRange);
                    operationLog.info(image.getFilePath() + " global: " + globalRange + ", local: "
                            + imageRange + ", rescaled: " + rescaledRange);
                    return new IntensityRangeReductionFactory(rescaledRange.getMin(),
                            rescaledRange.getMax());
                }
            };
    }
}

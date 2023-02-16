/*
 * Copyright ETH 2014 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.dss.etl.dto.api.impl;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSet;
import ch.systemsx.cisd.etlserver.registrator.api.v2.impl.DataSet;
import ch.systemsx.cisd.openbis.common.io.FileBasedContentNode;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.DefaultFileBasedHierarchicalContentFactory;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.etl.Hdf5ThumbnailGenerator;
import ch.systemsx.cisd.openbis.dss.etl.IImageProvider;
import ch.systemsx.cisd.openbis.dss.etl.dto.ImageLibraryInfo;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.Channel;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColorComponent;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.IImageGenerationAlgorithm;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.ImageFileInfo;
import ch.systemsx.cisd.openbis.dss.generic.server.images.ImageChannelsUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.Size;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ColorComponent;

/**
 * Implementation of {@link IImageGenerationAlgorithm} which generates from a bunch of images a representative image where at each pixel the brightest
 * pixel from the images are taken.
 *
 * @author Antti Luomi
 * @author Franz-Josef Elmer
 */
public class MaximumIntensityProjectionGenerationAlgorithm implements IImageGenerationAlgorithm, Serializable
{
    private static final class ChannelAndColorComponent
    {
        private Channel channel;

        private ColorComponent colorComponent;

        ChannelAndColorComponent(Channel channel, ColorComponent colorComponent)
        {
            this.channel = channel;
            this.colorComponent = colorComponent;
        }
    }

    public static final String DEFAULT_FILE_NAME = "maximum_intensity_projection.png";

    private static final long serialVersionUID = IServer.VERSION;

    private transient BufferedImage result = null;

    private String dataSetTypeCode;

    private String filename;

    private int width;

    private int height;

    private boolean useThumbnails;
    
    private IHierarchicalContent content;

    /**
     * Creates an instance for the specified data set type. The generated image will have the same size as the original images. The file name will be
     * <code>maximum_intensity_projection.png</code>.
     */
    public MaximumIntensityProjectionGenerationAlgorithm(String dataSetTypeCode)
    {
        this(dataSetTypeCode, 0, 0);
    }

    /**
     * Creates an instance for the specified data set type and file name. The generated image will have the same size as the original images.
     */
    public MaximumIntensityProjectionGenerationAlgorithm(String dataSetTypeCode, String filename)
    {
        this(dataSetTypeCode, 0, 0, filename);
    }

    /**
     * Creates an instance for the specified data set type and specified image size. The file name will be
     * <code>maximum_intensity_projection.png</code>.
     */
    public MaximumIntensityProjectionGenerationAlgorithm(String dataSetTypeCode, int width, int height)
    {
        this(dataSetTypeCode, width, height, DEFAULT_FILE_NAME);
    }

    /**
     * Creates an instance for the specified data set type, specified image size and specified file name.
     */
    public MaximumIntensityProjectionGenerationAlgorithm(String dataSetTypeCode, int width, int height, String filename)
    {
        this.dataSetTypeCode = dataSetTypeCode;
        this.width = width;
        this.height = height;
        this.filename = filename;
    }

    /**
     * Uses thumbnails (if present) instead of original images.
     */
    public MaximumIntensityProjectionGenerationAlgorithm useThumbnails()
    {
        useThumbnails = true;
        return this;
    }

    @Override
    public String getDataSetTypeCode()
    {
        return dataSetTypeCode;
    }

    @Override
    public List<BufferedImage> generateImages(ImageDataSetInformation information,
            List<IDataSet> thumbnailDatasets, IImageProvider imageProvider)
    {
        ImageDataSetStructure structure = information.getImageDataSetStructure();
        ImageLibraryInfo library = structure.getImageStorageConfiguraton().tryGetImageLibrary();
        List<ImageFileInfo> images = structure.getImages();
        Map<String, ChannelAndColorComponent> channelsByCode = getChannelsByCode(structure);
        File incomingDirectory = information.getIncomingDirectory();
        File thumbnailDataSet = tryFindMatchingThumbnailDataSet(information, thumbnailDatasets);
        int maxIntensity = 0;
        for (ImageFileInfo imageFileInfo : images)
        {
            if (imageToBeIgnored(imageFileInfo))
            {
                continue;
            }
            String channelCode = imageFileInfo.getChannelCode();
            ChannelAndColorComponent channelAndColorComponent = channelsByCode.get(channelCode);
            BufferedImage image = loadImage(imageProvider, library, images, incomingDirectory,
                    thumbnailDataSet, imageFileInfo, channelCode);
            image = ImageChannelsUtils.rescaleIfNot8Bit(image, 0f, channelAndColorComponent.channel.tryGetChannelColor());
            image = ImageChannelsUtils.extractChannel(image, channelAndColorComponent.colorComponent);
            image = ImageChannelsUtils.transformGrayToColor(image, channelAndColorComponent.channel.tryGetChannelColor());
            maxIntensity = addImage(image);
        }
        if (result == null)
        {
            return Collections.emptyList();
        } else
        {
            for (int y = 0; y < result.getHeight(); y++)
            {
                for (int x = 0; x < result.getWidth(); x++)
                {
                    result.setRGB(x, y, adjust(result.getRGB(x, y), maxIntensity));
                }
            }
            if (width > 0 && height > 0)
            {
                BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                AffineTransform at = new AffineTransform();
                at.scale((double) width / (double) result.getWidth(), (double) height / (double) result.getHeight());
                AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
                result = scaleOp.filter(result, scaled);
            }

            return Collections.singletonList(result);
        }
    }

    private BufferedImage loadImage(IImageProvider imageProvider, ImageLibraryInfo library,
            List<ImageFileInfo> images, File incomingDirectory,
            File thumbnailDataSet, ImageFileInfo imageFileInfo, String channelCode)
    {
        String imagePath = imageFileInfo.getImageRelativePath();
        String identifier = imageFileInfo.tryGetUniqueStringIdentifier();
        BufferedImage image = null;
        if (thumbnailDataSet != null && useThumbnails)
        {
            IHierarchicalContent content = new DefaultFileBasedHierarchicalContentFactory()
                    .asHierarchicalContent(thumbnailDataSet, null);
            String thumbnailPath = Hdf5ThumbnailGenerator.createThumbnailPath(imageFileInfo, channelCode);
            IHierarchicalContentNode rootNode = content.getRootNode();
            String containerPath = rootNode.getChildNodes().get(0).getRelativePath();
            IHierarchicalContentNode node = content.tryGetNode(containerPath + "/" + thumbnailPath + ".png");
            if (node != null)
            {
                image = imageProvider.getImage(node, images.get(0).tryGetUniqueStringIdentifier(), null);
            }
        }
        if (image == null)
        {
            image = loadImage(imageProvider, incomingDirectory, imagePath, identifier, library);
        }
        return image;
    }

    private File tryFindMatchingThumbnailDataSet(ImageDataSetInformation information, List<IDataSet> thumbnailDatasets)
    {
        ThumbnailsInfo thumbnailsInfos = information.getThumbnailsInfos();
        if (thumbnailsInfos != null)
        {
            for (String permId : thumbnailsInfos.getThumbnailPhysicalDatasetsPermIds())
            {
                Size dimension = thumbnailsInfos.tryGetDimension(permId);
                if (dimension != null && (dimension.getWidth() == width || dimension.getHeight() == height))
                {
                    IDataSet thumbnailDataSet = tryFindDataSetByPermId(thumbnailDatasets, permId);
                    if (thumbnailDataSet instanceof DataSet)
                    {
                        return ((DataSet<?>) thumbnailDataSet).getDataSetStagingFolder();
                    }
                }
            }
        }
        return null;
    }

    private IDataSet tryFindDataSetByPermId(List<IDataSet> dataSets, String permId)
    {
        for (IDataSet dataSet : dataSets)
        {
            if (dataSet.getDataSetCode().equals(permId))
            {
                return dataSet;
            }
        }
        return null;
    }

    @Private
    BufferedImage loadImage(IImageProvider imageProvider, File incomingDirectory, String imagePath,
            String identifier, ImageLibraryInfo library)
    {
        IHierarchicalContentNode contentNode = createContentNode(incomingDirectory, imagePath);
        return imageProvider.getImage(contentNode, identifier, library);
    }

    private IHierarchicalContentNode createContentNode(File incomingDirectory, String imagePath)
    {
        if (content == null)
        {
            return new FileBasedContentNode(new File(incomingDirectory, imagePath));
        }
        return content.getNode(imagePath);
    }

    @Override
    public void setContent(IHierarchicalContent content)
    {
        this.content = content;
    }

    /**
     * Returns <code>true</code> if the specified image should be ignored. Can be overridden.
     */
    protected boolean imageToBeIgnored(ImageFileInfo image)
    {
        return image.tryGetTimepoint() == null || image.tryGetTimepoint() != 0;
    }

    private Map<String, ChannelAndColorComponent> getChannelsByCode(ImageDataSetStructure structure)
    {
        Map<String, ChannelAndColorComponent> channelsByCode = new HashMap<String, ChannelAndColorComponent>();
        List<Channel> channels = structure.getChannels();
        for (int i = 0; i < channels.size(); i++)
        {
            Channel channel = channels.get(i);
            ColorComponent colorComponent = tryGetColorComponent(structure, i);
            channelsByCode.put(channel.getCode(), new ChannelAndColorComponent(channel, colorComponent));
        }
        return channelsByCode;
    }

    private ColorComponent tryGetColorComponent(ImageDataSetStructure structure, int i)
    {
        List<ChannelColorComponent> channelColorComponents = structure.getChannelColorComponents();
        if (channelColorComponents == null || channelColorComponents.isEmpty())
        {
            return null;
        }
        return ChannelColorComponent.getColorComponent(channelColorComponents.get(i));
    }

    private int adjust(int rgb, int maximumIntensity)
    {
        int maxIntensity = Math.min(255, maximumIntensity);
        int r = rescale(getRed(rgb), maxIntensity);
        int g = rescale(getGreen(rgb), maxIntensity);
        int b = rescale(getBlue(rgb), maxIntensity);
        return (r << 16) + (g << 8) + b;
    }

    private int rescale(int intensity, int maxIntensity)
    {
        return maxIntensity == 0 ? 0 : (intensity * 255 + maxIntensity / 2) / maxIntensity;
    }

    private int addImage(BufferedImage image)
    {
        int w = image.getWidth();
        int h = image.getHeight();
        if (result == null)
        {
            result = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            for (int y = 0; y < h; y++)
            {
                for (int x = 0; x < w; x++)
                {
                    result.setRGB(x, y, 0);
                }
            }
        }
        int maxIntensity = 0;
        for (int y = 0; y < h; y++)
        {
            for (int x = 0; x < w; x++)
            {
                int rgb1 = result.getRGB(x, y);
                int rgb2 = image.getRGB(x, y);

                int intensity1 = intensity(rgb1);
                int intensity2 = intensity(rgb2);
                maxIntensity = Math.max(Math.max(maxIntensity, intensity1), intensity2);

                result.setRGB(x, y, intensity1 > intensity2 ? rgb1 : rgb2);
            }
        }
        return maxIntensity;
    }

    private int intensity(int rgb)
    {
        double r = getRed(rgb);
        double g = getGreen(rgb);
        double b = getBlue(rgb);
        return Double.valueOf(Math.sqrt(r * r + g * g + b * b)).intValue();
    }

    private int getBlue(int rgb)
    {
        return rgb & 0xff;
    }

    private int getGreen(int rgb)
    {
        return (rgb >> 8) & 0xff;
    }

    private int getRed(int rgb)
    {
        return (rgb >> 16) & 0xff;
    }

    @Override
    public String getImageFileName(int index)
    {
        return filename;
    }

}

/*
 * Copyright 2012 ETH Zuerich, CISD
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

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSet;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSetRegistrationTransactionV2;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.etl.dto.ImageLibraryInfo;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.Channel;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColorRGB;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.IImageGenerationAlgorithm;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.ImageFileInfo;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.ImageStorageConfiguraton;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.ImageDataSetStructure;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.transformations.ImageTransformation;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.Size;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ImageUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Geometry;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingReadonlyQueryDAO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgAcquiredImageEnrichedDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgChannelDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageDatasetDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageTransformationDTO;

/**
 * Collection of helper functions.
 * 
 * @author Franz-Josef Elmer
 */
public class Utils
{
    final static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, ImageUtil.class);

    public static BufferedImage loadUnchangedImage(IHierarchicalContentNode contentNode,
            String imageIdOrNull, ImageLibraryInfo imageLibraryOrNull)
    {
        String imageLibraryNameOrNull = null;
        String imageLibraryReaderNameOrNull = null;
        if (imageLibraryOrNull != null)
        {
            imageLibraryNameOrNull = imageLibraryOrNull.getName();
            imageLibraryReaderNameOrNull = imageLibraryOrNull.getReaderName();
        }
        BufferedImage image = ImageUtil.loadUnchangedImage(contentNode, imageIdOrNull, imageLibraryNameOrNull,
                imageLibraryReaderNameOrNull, null);
        return image;
    }

    public static Size loadUnchangedImageSize(IHierarchicalContentNode contentNode,
            String imageIdOrNull, ImageLibraryInfo imageLibraryOrNull)
    {
        try
        {
            operationLog.debug("Trying to process file: " + contentNode.getRelativePath());
        } catch (Exception e)
        {
            // do nothing
        }

        String imageLibraryNameOrNull = null;
        String imageLibraryReaderNameOrNull = null;
        if (imageLibraryOrNull != null)
        {
            imageLibraryNameOrNull = imageLibraryOrNull.getName();
            imageLibraryReaderNameOrNull = imageLibraryOrNull.getReaderName();
        }
        Dimension dimension =
                ImageUtil.loadUnchangedImageDimension(contentNode, imageIdOrNull,
                        imageLibraryNameOrNull, imageLibraryReaderNameOrNull);
        return new Size(dimension.width, dimension.height);
    }

    public static int loadUnchangedImageColorDepth(IHierarchicalContentNode contentNode,
            String imageIdOrNull, ImageLibraryInfo imageLibraryOrNull)
    {
        try
        {
            operationLog.debug("Trying to process file: " + contentNode.getRelativePath());
        } catch (Exception e)
        {
            // do nothing
        }

        String imageLibraryNameOrNull = null;
        String imageLibraryReaderNameOrNull = null;
        if (imageLibraryOrNull != null)
        {
            imageLibraryNameOrNull = imageLibraryOrNull.getName();
            imageLibraryReaderNameOrNull = imageLibraryOrNull.getReaderName();
        }
        return ImageUtil.loadUnchangedImageColorDepth(contentNode, imageIdOrNull,
                imageLibraryNameOrNull, imageLibraryReaderNameOrNull);
    }

    public static ImageDataSetStructure getImageDataSetStructure(IImagingReadonlyQueryDAO query, String containerCode)
    {
        List<ImageFileInfo> images = new ArrayList<>();
        List<Channel> channels = new ArrayList<>();
        Geometry geometry = gatherImageFileInfos(query, containerCode, images, channels);
        ImageDataSetStructure imageStruct = new ImageDataSetStructure();
        imageStruct.setImages(images);
        imageStruct.setChannels(channels);
        imageStruct.setTileGeometry(geometry.getNumberOfRows(), geometry.getNumberOfColumns());
        ImageStorageConfiguraton imageStorageConfiguraton = new ImageStorageConfiguraton();
        ImageLibraryInfo imageLibrary = tryGetImageLibrary(query.tryGetImageDatasetByPermId(containerCode), false);
        imageStorageConfiguraton.setImageLibrary(imageLibrary);
        imageStruct.setImageStorageConfiguraton(imageStorageConfiguraton);
        return imageStruct;
    }

    public static Geometry gatherImageFileInfos(IImagingReadonlyQueryDAO query, String permId,
            List<ImageFileInfo> images, List<Channel> channels)
    {
        ImgImageDatasetDTO container = query.tryGetImageDatasetByPermId(permId);
        if (container == null)
        {
            return null;
        }

        List<ImgAcquiredImageEnrichedDTO> acquiredImages =
                query.listAllEnrichedAcquiredImagesForDataSet(container.getId());
        for (ImgAcquiredImageEnrichedDTO acquiredImage : acquiredImages)
        {
            images.add(createImageFileInfo(container, acquiredImage));
        }

        List<ImgChannelDTO> channelDTOs = query.getChannelsByDatasetId(container.getId());
        for (ImgChannelDTO channelDTO : channelDTOs)
        {
            channels.add(createChannel(query, channelDTO));
        }

        return Geometry.createFromRowColDimensions(container.getFieldNumberOfRows(),
                container.getFieldNumberOfColumns());
    }

    private static ImageFileInfo createImageFileInfo(ImgImageDatasetDTO container,
            ImgAcquiredImageEnrichedDTO acquiredImage)
    {
        ImageFileInfo img =
                new ImageFileInfo(acquiredImage.getChannelCode(), acquiredImage.getTileRow(),
                        acquiredImage.getTileColumn(), acquiredImage.getImageFilePath());
        img.setTimepoint(acquiredImage.getT());
        img.setDepth(acquiredImage.getZ());
        img.setSeriesNumber(acquiredImage.getSeriesNumber());

        if (acquiredImage.getSpotRow() != null && acquiredImage.getSpotColumn() != null)
        {
            img.setWell(acquiredImage.getSpotRow(), acquiredImage.getSpotColumn());
        }
        img.setUniqueImageIdentifier(acquiredImage.getImageIdOrNull());
        img.setContainerDataSetCode(container.getPermId());

        return img;
    }

    private static Channel createChannel(IImagingReadonlyQueryDAO query, ImgChannelDTO channelDTO)
    {
        Channel channel =
                new Channel(channelDTO.getCode(), channelDTO.getLabel(), new ChannelColorRGB(
                        channelDTO.getRedColorComponent(), channelDTO.getGreenColorComponent(),
                        channelDTO.getBlueColorComponent()));
        channel.setDescription(channelDTO.getDescription());
        channel.setWavelength(channelDTO.getWavelength());

        List<ImgImageTransformationDTO> transformationDTOs =
                query.listImageTransformations(channelDTO.getId());
        ImageTransformation[] transformations = new ImageTransformation[transformationDTOs.size()];
        int counter = 0;
        for (ImgImageTransformationDTO transformationDTO : transformationDTOs)
        {
            ImageTransformation transformation =
                    new ImageTransformation(transformationDTO.getCode(),
                            transformationDTO.getLabel(), transformationDTO.getDescription(),
                            transformationDTO.getImageTransformerFactory());
            transformations[counter++] = transformation;
        }
        channel.setAvailableTransformations(transformations);

        return channel;
    }

    public static ImageLibraryInfo tryGetImageLibrary(ImgImageDatasetDTO dataset, boolean isThumbnail)
    {
        if (isThumbnail)
        {
            // we do not use special libraries for thumbnails, they are always in the PNG format
            return null;
        }
        String imageLibraryName = dataset.getImageLibraryName();
        return imageLibraryName == null ? null : new ImageLibraryInfo(imageLibraryName, dataset.getImageReaderName());
    }

    public static IDataSet createDataSetAndImageFiles(IDataSetRegistrationTransactionV2 transaction, IImageGenerationAlgorithm algorithm, List<BufferedImage> images)
    {
        IDataSet representative = transaction.createNewDataSet(algorithm.getDataSetTypeCode(), DataSetKind.PHYSICAL);
        for (int i = 0; i < images.size(); i++)
        {
            BufferedImage imageData = images.get(i);
            String imageFile = transaction.createNewFile(representative, algorithm.getImageFileName(i));
            File f = new File(imageFile);
            try
            {
                ImageIO.write(imageData, "png", f);
            } catch (IOException e)
            {
                throw new EnvironmentFailureException("Can not save representative thumbnail to file '"
                        + f + "': " + e, e);
            }
        }
        return representative;
    }
}

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

package ch.systemsx.cisd.openbis.dss.etl.dataaccess;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.base.image.IImageTransformerFactory;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.hcs.Location;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.etl.AbsoluteImageReference;
import ch.systemsx.cisd.openbis.dss.etl.IImagingDatasetLoader;
import ch.systemsx.cisd.openbis.dss.etl.PrefixedImage;
import ch.systemsx.cisd.openbis.dss.etl.Utils;
import ch.systemsx.cisd.openbis.dss.etl.dto.ImageLibraryInfo;
import ch.systemsx.cisd.openbis.dss.etl.dto.ImageTransfomationFactories;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColorRGB;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.ImageChannelStackReference;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.ImageChannelStackReference.HCSChannelStackByLocationReference;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.ImageChannelStackReference.MicroscopyChannelStackByLocationReference;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.RequestedImageSize;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.Size;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.HCSDatasetLoader;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ColorComponent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingReadonlyQueryDAO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgChannelDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgContainerDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageDatasetDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageTransformationDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageZoomLevelDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageZoomLevelTransformationEnrichedDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgSpotDTO;

/**
 * {@link HCSDatasetLoader} extension with code for handling images.
 * 
 * @author Tomasz Pylak
 * @author Piotr Buczek
 * @author Pawel Glyzewski
 */
public class ImagingDatasetLoader extends HCSDatasetLoader implements IImagingDatasetLoader
{
    private final IHierarchicalContent content;

    /**
     * @return null if the dataset is not found in the imaging database
     */
    public static ImagingDatasetLoader tryCreate(IImagingReadonlyQueryDAO query,
            String datasetPermId, IHierarchicalContent content)
    {
        ImgImageDatasetDTO dataset = query.tryGetImageDatasetByPermId(datasetPermId);
        if (dataset == null)
        {
            operationLog.warn(String.format(
                    "No dataset with code '%s' found in the imaging database.", datasetPermId));
            return null;
        } else
        {
            return new ImagingDatasetLoader(query, dataset, content);
        }
    }

    protected ImagingDatasetLoader(IImagingReadonlyQueryDAO query, ImgImageDatasetDTO dataset,
            IHierarchicalContent content)
    {
        super(query, dataset);
        this.content = content;
    }

    /**
     * @param chosenChannelCode
     * @return image (with absolute path, page and color)
     */
    @Override
    public AbsoluteImageReference tryGetImage(String chosenChannelCode,
            ImageChannelStackReference channelStackReference, RequestedImageSize imageSize,
            String singleChannelTransformationCodeOrNull)
    {
        if (StringUtils.isBlank(chosenChannelCode))
        {
            throw new UserFailureException("Unspecified channel.");
        }
        validateChannelStackReference(channelStackReference);

        ImgChannelDTO channel = tryLoadChannel(chosenChannelCode);
        if (channel == null)
        {
            return null;
        }

        long datasetId = getDataset().getId();
        long chosenChannelId = channel.getId();
        PrefixedImage imageDTO = null;
        if (imageSize.isThumbnailRequired())
        {
            imageDTO =
                    tryGetThumbnail(chosenChannelId, channelStackReference, datasetId, imageSize,
                            singleChannelTransformationCodeOrNull);
        }
        boolean thumbnailFetched = (imageDTO != null);
        if (imageDTO == null)
        {
            // get the image content from the original image
            imageDTO = tryGetOriginalImage(chosenChannelId, channelStackReference, datasetId);
            // Thumbnail is not required, but original image is not there.
            // We have to fetch the thumbnail anyway.
            if (imageDTO == null && imageSize.isThumbnailRequired() == false)
            {
                imageDTO =
                        tryGetThumbnail(chosenChannelId, channelStackReference, datasetId,
                                imageSize, singleChannelTransformationCodeOrNull);
                thumbnailFetched = (imageDTO != null);
            }
        }
        if (imageDTO == null)
        {
            return null;
        }
        AbsoluteImageReference imgRef =
                createAbsoluteImageReference(imageDTO, channel, imageSize, thumbnailFetched);
        if (thumbnailFetched && imageSize.isThumbnailRequired()
                && isThumbnailsTooSmall(imageSize, imgRef.getUnchangedImage()))
        {
            imageDTO = tryGetOriginalImage(channel.getId(), channelStackReference, datasetId);
            if (imageDTO != null)
            {
                thumbnailFetched = false;
                imgRef =
                        createAbsoluteImageReference(imageDTO, channel, imageSize, thumbnailFetched);
            }
        }

        return imgRef;
    }

    private boolean isThumbnailsTooSmall(RequestedImageSize imageSize, BufferedImage image)
    {
        Size requestedThumbnailSize = imageSize.tryGetThumbnailSize();
        double width = 1.5 * image.getWidth();
        double height = 1.5 * image.getHeight();
        boolean thumbnailTooSmall =
                requestedThumbnailSize.getWidth() > width
                        || requestedThumbnailSize.getHeight() > height;
        return thumbnailTooSmall;
    }

    private AbsoluteImageReference createAbsoluteImageReference(PrefixedImage image,
            ImgChannelDTO channel, RequestedImageSize imageSize, boolean useNativeImageLibrary)
    {
        String path = image.getFilePath();
        IHierarchicalContentNode contentNode = content.getNode(path);
        ColorComponent colorComponent = image.getColorComponent();
        ImageTransfomationFactories imageTransfomationFactories =
                createImageTransfomationFactories(image, channel);
        ImageLibraryInfo imageLibrary = Utils.tryGetImageLibrary(dataset, useNativeImageLibrary);
        return new AbsoluteImageReference(contentNode, path, image.getImageID(), colorComponent,
                imageSize, getColor(channel), imageTransfomationFactories, imageLibrary,
                image.tryGetSingleChannelTransformationCode(), channel.getCode());
    }

    private static ChannelColorRGB getColor(ImgChannelDTO channel)
    {
        return new ChannelColorRGB(channel.getRedColorComponent(),
                channel.getGreenColorComponent(), channel.getBlueColorComponent());
    }

    private ImageTransfomationFactories createImageTransfomationFactories(PrefixedImage imageDTO,
            ImgChannelDTO channel)
    {
        ImageTransfomationFactories imageTransfomationFactories = new ImageTransfomationFactories();
        imageTransfomationFactories
                .setForMergedChannels(tryGetImageTransformerFactoryForMergedChannels());

        Map<String, IImageTransformerFactory> singleChannelMap =
                getAvailableImageTransformationsForChannel(channel);
        imageTransfomationFactories.setForChannel(singleChannelMap);

        ImgImageTransformationDTO defaultTransformationOrNull =
                tryGetDefaultTransformation(channel);
        if (defaultTransformationOrNull != null)
        {
            imageTransfomationFactories.setDefaultTransformation(
                    defaultTransformationOrNull.getCode(),
                    defaultTransformationOrNull.getImageTransformerFactory());
        }

        imageTransfomationFactories.setForImage(imageDTO.tryGetImageTransformerFactory());
        return imageTransfomationFactories;
    }

    private Map<String, IImageTransformerFactory> getAvailableImageTransformationsForChannel(
            ImgChannelDTO channel)
    {
        List<ImgImageTransformationDTO> availableImageTransformations =
                availableImageTransformationsMap.get(channel.getId());
        Map<String, IImageTransformerFactory> singleChannelMap =
                new HashMap<String, IImageTransformerFactory>();
        if (availableImageTransformations != null)
        {
            for (ImgImageTransformationDTO transformation : availableImageTransformations)
            {
                singleChannelMap.put(transformation.getCode(),
                        transformation.getImageTransformerFactory());
            }
        }
        return singleChannelMap;
    }

    private ImgImageTransformationDTO tryGetDefaultTransformation(ImgChannelDTO channel)
    {
        List<ImgImageTransformationDTO> availableImageTransformations =
                availableImageTransformationsMap.get(channel.getId());

        if (availableImageTransformations != null)
        {
            for (ImgImageTransformationDTO transformation : availableImageTransformations)
            {
                if (transformation.getIsDefault())
                {
                    return transformation;
                }
            }
        }

        return null;
    }

    private IImageTransformerFactory tryGetImageTransformerFactoryForMergedChannels()
    {
        IImageTransformerFactory imageTransformerFactory = dataset.tryGetImageTransformerFactory();
        if (imageTransformerFactory == null && experimentOrNull != null)
        {
            imageTransformerFactory = experimentOrNull.tryGetImageTransformerFactory();
        }
        return imageTransformerFactory;
    }

    private ImgChannelDTO tryLoadChannel(String chosenChannelCode)
    {
        // first we check if there are some channels defined at the dataset level (even for HCS one
        // can decide in configuration about that)
        ImgChannelDTO channel = query.tryGetChannelForDataset(dataset.getId(), chosenChannelCode);
        // if not, we check at the experiment level
        if (channel == null && containerOrNull != null)
        {
            channel =
                    query.tryGetChannelForExperiment(containerOrNull.getExperimentId(),
                            chosenChannelCode);
        }
        return channel;
    }

    private void validateChannelStackReference(ImageChannelStackReference channelStackReference)
    {
        HCSChannelStackByLocationReference hcsRef = channelStackReference.tryGetHCSChannelStack();
        MicroscopyChannelStackByLocationReference micRef =
                channelStackReference.tryGetMicroscopyChannelStack();
        if (hcsRef != null)
        {
            validateTileReference(hcsRef.getTileLocation());
            ImgContainerDTO container = tryGetContainer();
            if (container != null)
            {
                Location wellLocation = hcsRef.getWellLocation();
                if (wellLocation.getX() > container.getNumberOfColumns())
                {
                    throw new IllegalArgumentException("Well column coordinate "
                            + wellLocation.getX()
                            + " is bigger then the number of plate's columns "
                            + container.getNumberOfColumns());
                }
                if (wellLocation.getY() > container.getNumberOfRows())
                {
                    throw new IllegalArgumentException("Well row coordinate " + wellLocation.getY()
                            + " is bigger then the number of plate's rows "
                            + container.getNumberOfRows());
                }
            }
        } else if (micRef != null)
        {
            validateTileReference(micRef.getTileLocation());
        }
    }

    private void validateTileReference(Location tileLocation)
    {
        if (tileLocation.getX() > getDataset().getFieldNumberOfColumns())
        {
            throw new IllegalArgumentException("Tile x coordinate " + tileLocation.getX()
                    + " is bigger then number of well's columns "
                    + getDataset().getFieldNumberOfColumns());
        }
        if (tileLocation.getY() > getDataset().getFieldNumberOfRows())
        {
            throw new IllegalArgumentException("Tile y coordinate " + tileLocation.getY()
                    + " is bigger then number of well's rows "
                    + getDataset().getFieldNumberOfRows());
        }
    }

    private PrefixedImage tryGetOriginalImage(long channelId,
            ImageChannelStackReference channelStackReference, long datasetId)
    {
        HCSChannelStackByLocationReference hcsRef = channelStackReference.tryGetHCSChannelStack();
        MicroscopyChannelStackByLocationReference micRef =
                channelStackReference.tryGetMicroscopyChannelStack();
        ImgImageDTO image;
        if (hcsRef != null)
        {
            image =
                    query.tryGetHCSImage(channelId, datasetId, hcsRef.getTileLocation(),
                            hcsRef.getWellLocation());
        } else if (micRef != null)
        {
            image = query.tryGetMicroscopyImage(channelId, datasetId, micRef.getTileLocation());
        } else
        {
            Long channelStackId = channelStackReference.tryGetChannelStackId();
            assert channelStackId != null : "invalid specification of the channel stack: "
                    + channelStackReference;
            image = query.tryGetImage(channelId, channelStackId, datasetId);
        }
        return checkAccessability("", "", null, image);
    }

    private static ImgImageZoomLevelDTO selectBestZoomLevel(ImgImageZoomLevelDTO current,
            ImgImageZoomLevelDTO candidate, RequestedImageSize imageSize)
    {
        Size size = imageSize == null ? null : imageSize.tryGetThumbnailSize();
        Integer height = candidate.getHeight();
        Integer width = candidate.getWidth();
        if (size == null || width == null || height == null)
        {
            return current;
        }

        if (size.getHeight() <= height && size.getWidth() <= width)
        {
            if (current == null || current.getWidth() == null || current.getHeight() == null)
            {
                return candidate;
            }

            if (width <= current.getWidth() && height <= current.getHeight())
            {
                return candidate;
            }
        }

        return current;
    }

    private static ImgImageZoomLevelDTO selectHighestZoomLevel(ImgImageZoomLevelDTO current,
            ImgImageZoomLevelDTO candidate)
    {
        Integer height = candidate.getHeight();
        Integer width = candidate.getWidth();

        if (current == null || current.getWidth() == null || current.getHeight() == null)
        {
            return candidate;
        }

        if (width == null || height == null)
        {
            return current;
        }

        if (width >= current.getWidth() && height >= current.getHeight())
        {
            return candidate;
        }

        return current;
    }

    private PrefixedImage tryGetThumbnail(long channelId,
            ImageChannelStackReference channelStackReference, long datasetId,
            RequestedImageSize imageSize, String singleChannelTransformationCodeOrNullOrNull)
    {
        HCSChannelStackByLocationReference hcsRef = channelStackReference.tryGetHCSChannelStack();
        MicroscopyChannelStackByLocationReference micRef =
                channelStackReference.tryGetMicroscopyChannelStack();
        ImgImageDTO image;
        String[] pathPrefixAndSuffix =
                findPathPrefixAndSuffix(datasetId, channelId, imageSize,
                        singleChannelTransformationCodeOrNullOrNull);

        if (hcsRef != null)
        {
            image =
                    query.tryGetHCSThumbnail(channelId, datasetId, hcsRef.getTileLocation(),
                            hcsRef.getWellLocation());
        } else if (micRef != null)
        {
            image = query.tryGetMicroscopyThumbnail(channelId, datasetId, micRef.getTileLocation());
        } else
        {
            Long channelStackId = channelStackReference.tryGetChannelStackId();
            assert channelStackId != null : "invalid specification of the channel stack: "
                    + channelStackReference;
            image = query.tryGetThumbnail(channelId, channelStackId, datasetId);
        }
        return checkAccessability(pathPrefixAndSuffix[0], pathPrefixAndSuffix[1],
                pathPrefixAndSuffix[2], image);
    }

    private String[] findPathPrefixAndSuffix(long datasetId, long channelId,
            RequestedImageSize imageSize, String singleChannelTransformationCodeOrNull)
    {
        String[] ret = new String[]
        { "", "", null };
        ImgImageZoomLevelDTO bestZoomLevel = null;
        List<ImgImageZoomLevelDTO> zoomLevels = query.listImageZoomLevels(datasetId);
        if (zoomLevels != null && zoomLevels.size() > 0)
        {
            if (singleChannelTransformationCodeOrNull != null)
            {
                List<ImgImageZoomLevelTransformationEnrichedDTO> zoomLevelTransformations =
                        query.findImageZoomLevelTransformations(datasetId, channelId,
                                singleChannelTransformationCodeOrNull);

                if (false == zoomLevelTransformations.isEmpty())
                {

                    Set<Long> zoomLevelsWithTransformedImages = new HashSet<Long>();
                    for (ImgImageZoomLevelTransformationEnrichedDTO zoomLevelTransformation : zoomLevelTransformations)
                    {
                        zoomLevelsWithTransformedImages.add(zoomLevelTransformation
                                .getImageZoomLevelId());
                    }

                    for (ImgImageZoomLevelDTO zoomLevel : zoomLevels)
                    {
                        if (false == zoomLevel.getIsOriginal()
                                && zoomLevelsWithTransformedImages.contains(zoomLevel.getId()))
                        {
                            bestZoomLevel =
                                    selectBestZoomLevel(bestZoomLevel, zoomLevel, imageSize);
                        }
                    }
                }

                if (bestZoomLevel != null)
                {
                    ret[2] = singleChannelTransformationCodeOrNull;
                }
            }

            if (bestZoomLevel == null)
            {
                zoomLevels = query.listImageZoomLevelsWithNoTransformations(datasetId);
                for (ImgImageZoomLevelDTO zoomLevel : zoomLevels)
                {
                    if (false == zoomLevel.getIsOriginal())
                    {
                        bestZoomLevel = selectBestZoomLevel(bestZoomLevel, zoomLevel, imageSize);
                    }
                }
            }

            if (bestZoomLevel == null)
            {
                for (ImgImageZoomLevelDTO zoomLevel : zoomLevels)
                {
                    if (false == zoomLevel.getIsOriginal())
                    {
                        bestZoomLevel = selectHighestZoomLevel(bestZoomLevel, zoomLevel);
                    }
                }
            }
        }

        if (bestZoomLevel != null && bestZoomLevel.getRootPath() != null)
        {
            ret[0] = bestZoomLevel.getRootPath();
            ret[1] = bestZoomLevel.getFileType();
        }
        return ret;
    }

    private PrefixedImage checkAccessability(String pathPrefix, String pathSuffix,
            String singleChannelTransformationCodeOrNull, ImgImageDTO imageOrNull)
    {
        if (imageOrNull == null)
        {
            return null;
        }
        return isFileAccessible(pathPrefix, pathSuffix, imageOrNull) ? new PrefixedImage(
                pathPrefix, pathSuffix, singleChannelTransformationCodeOrNull, imageOrNull) : null;
    }

    private boolean isFileAccessible(String pathPrefix, String pathSuffix, ImgImageDTO image)
    {
        String filePath = image.getFilePath();
        if (ch.systemsx.cisd.common.shared.basic.string.StringUtils.isNotBlank(pathPrefix))
        {
            filePath =
                    pathPrefix
                            + "/"
                            + filePath
                            + (ch.systemsx.cisd.common.shared.basic.string.StringUtils
                                    .isBlank(pathSuffix) ? "" : "." + pathSuffix);
        }

        try
        {
            content.getNode(filePath);
            return true;
        } catch (IllegalArgumentException ex)
        {
            operationLog
                    .warn(String
                            .format("Path '%s' is unaccessible, probably the dataset has been deleted and the imaging db is not yet synchronized.",
                                    filePath));
            return false;
        }
    }

    private PrefixedImage tryGetRepresentativeImageDTO(long channelId, Location wellLocationOrNull,
            boolean thumbnailWanted)
    {
        long datasetId = dataset.getId();
        ImgImageDTO image = null;
        if (wellLocationOrNull == null)
        {
            if (thumbnailWanted)
            {
                image = query.tryGetMicroscopyRepresentativeThumbnail(datasetId, channelId);
            }
            if (image == null)
            {
                image = query.tryGetMicroscopyRepresentativeImage(datasetId, channelId);
            }
        } else
        {
            if (thumbnailWanted)
            {
                image =
                        query.tryGetHCSRepresentativeThumbnail(datasetId, wellLocationOrNull,
                                channelId);
            }
            if (image == null)
            {
                image =
                        query.tryGetHCSRepresentativeImage(datasetId, wellLocationOrNull, channelId);
            }
        }
        return checkAccessability("", "", null, image);
    }

    @Override
    public AbsoluteImageReference tryFindAnyOriginalImage()
    {
        List<ImgSpotDTO> wells = query.listWellsWithAnyImages(dataset.getId());
        List<String> channelCodes = getImageParameters().getChannelsCodes();
        RequestedImageSize originalOrThumbnail = RequestedImageSize.createOriginal();

        for (ImgSpotDTO well : wells)
        {
            for (String channelCode : channelCodes)
            {
                AbsoluteImageReference image =
                        tryGetRepresentativeImage(channelCode,
                                new Location(well.getColumn(), well.getRow()), originalOrThumbnail,
                                null);
                if (image != null)
                {
                    return image;
                }
            }
        }
        return null;
    }

    @Override
    public AbsoluteImageReference tryGetRepresentativeImage(String channelCode,
            Location wellLocationOrNull, RequestedImageSize imageSize,
            String singleChannelTransformationCodeOrNull)
    {
        ImgChannelDTO channel = tryLoadChannel(channelCode);
        if (channel == null)
        {
            return null;
        }
        PrefixedImage imageDTO =
                tryGetRepresentativeImageDTO(channel.getId(), wellLocationOrNull,
                        imageSize.isThumbnailRequired());
        if (imageDTO == null)
        {
            return null;
        }
        boolean useNativeImageLibrary = false;
        return createAbsoluteImageReference(imageDTO, channel, imageSize, useNativeImageLibrary);
    }

    private PrefixedImage tryGetRepresentativeThumbnailImageDTO(long channelId,
            Location wellLocationOrNull, RequestedImageSize imageSize,
            String singleChannelTransformationCodeOrNull)
    {
        long datasetId = getDataset().getId();
        String[] pathPrefixAndSuffix =
                findPathPrefixAndSuffix(datasetId, channelId, imageSize,
                        singleChannelTransformationCodeOrNull);

        ImgImageDTO image;
        if (wellLocationOrNull == null)
        {
            image = query.tryGetMicroscopyRepresentativeThumbnail(dataset.getId(), channelId);
        } else
        {
            image =
                    query.tryGetHCSRepresentativeThumbnail(dataset.getId(), wellLocationOrNull,
                            channelId);
        }
        return checkAccessability(pathPrefixAndSuffix[0], pathPrefixAndSuffix[1],
                pathPrefixAndSuffix[2], image);
    }

    @Override
    public AbsoluteImageReference tryFindAnyThumbnail()
    {
        List<ImgSpotDTO> wells = query.listWellsWithAnyThumbnails(dataset.getId());
        List<String> channelCodes = getImageParameters().getChannelsCodes();
        for (ImgSpotDTO well : wells)
        {
            for (String channelCode : channelCodes)
            {
                AbsoluteImageReference image =
                        tryGetRepresentativeThumbnail(channelCode, new Location(well.getColumn(),
                                well.getRow()), null, null);
                if (image != null)
                {
                    return image;
                }
            }
        }
        return null;
    }

    @Override
    public AbsoluteImageReference tryGetRepresentativeThumbnail(String channelCode,
            Location wellLocationOrNull, RequestedImageSize imageSize,
            String singleChannelTransformationCodeOrNull)
    {
        ImgChannelDTO channel = tryLoadChannel(channelCode);
        if (channel == null)
        {
            return null;
        }
        PrefixedImage imageDTO =
                tryGetRepresentativeThumbnailImageDTO(channel.getId(), wellLocationOrNull,
                        imageSize, singleChannelTransformationCodeOrNull);
        if (imageDTO == null)
        {
            return null;
        }
        return createAbsoluteImageReference(imageDTO, channel, new RequestedImageSize(
                Size.NULL_SIZE, false), true);
    }

    @Override
    public AbsoluteImageReference tryGetThumbnail(String channelCode,
            ImageChannelStackReference channelStackReference, RequestedImageSize imageSize,
            String singleChannelTransformationCodeOrNull)
    {
        if (StringUtils.isBlank(channelCode))
        {
            throw new UserFailureException("Unspecified channel.");
        }
        validateChannelStackReference(channelStackReference);

        final ImgChannelDTO channel = tryLoadChannel(channelCode);
        if (channel == null)
        {
            return null;
        }

        long datasetId = getDataset().getId();
        final PrefixedImage thumbnailDTO =
                tryGetThumbnail(channel.getId(), channelStackReference, datasetId, imageSize,
                        singleChannelTransformationCodeOrNull);
        if (thumbnailDTO == null)
        {
            return null;
        }

        return createAbsoluteImageReference(thumbnailDTO, channel,
                RequestedImageSize.createOriginal(), true);
    }

}
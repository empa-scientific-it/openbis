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

package ch.systemsx.cisd.openbis.dss.etl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.base.image.IImageTransformerFactory;
import ch.systemsx.cisd.openbis.dss.etl.ImagingDatabaseHelper.ImagingChannelsMap;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.IImagingQueryDAO;
import ch.systemsx.cisd.openbis.dss.etl.dto.ImageDatasetInfo;
import ch.systemsx.cisd.openbis.dss.etl.dto.ImageLibraryInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeNormalizer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgAcquiredImageDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgChannelStackDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageDatasetDTO;

/**
 * Abstract superclass for uploaders of image datasets into the imaging database.
 * 
 * @author Tomasz Pylak
 */
abstract class AbstractImageDatasetUploader
{
    protected final IImagingQueryDAO dao;

    private static final long DUMMY_ID = 0;

    protected AbstractImageDatasetUploader(IImagingQueryDAO dao)
    {
        this.dao = dao;
    }

    protected static class AcquiredImageInStack
    {
        private final String channelCode;

        private final RelativeImageReference imageFilePath;

        private final RelativeImageReference thumbnailPathOrNull;

        private final IImageTransformerFactory imageTransformerFactoryOrNull;

        public AcquiredImageInStack(String channelCode, RelativeImageReference imageFilePath,
                RelativeImageReference thumbnailPathOrNull,
                IImageTransformerFactory imageTransformerFactoryOrNull)
        {
            this.channelCode = CodeNormalizer.normalize(channelCode);
            this.imageFilePath = imageFilePath;
            this.thumbnailPathOrNull = thumbnailPathOrNull;
            this.imageTransformerFactoryOrNull = imageTransformerFactoryOrNull;
        }

        public String getChannelCode()
        {
            return channelCode;
        }

        public RelativeImageReference getImageFilePath()
        {
            return imageFilePath;
        }

        public final RelativeImageReference getThumbnailPathOrNull()
        {
            return thumbnailPathOrNull;
        }

        public IImageTransformerFactory getImageTransformerFactoryOrNull()
        {
            return imageTransformerFactoryOrNull;
        }
    }

    protected static interface ISpotProvider
    {
        Long tryGetSpotId(AcquiredSingleImage image);
    }

    protected final void createImages(List<AcquiredSingleImage> images, ISpotProvider spotProvider,
            ImagingChannelsMap channelsMap, long datasetId)
    {
        Map<ImgChannelStackDTO, List<AcquiredImageInStack>> stackImagesMap =
                makeStackImagesMap(images, spotProvider, datasetId);

        createImages(stackImagesMap, channelsMap, datasetId);
    }

    private Map<ImgChannelStackDTO, List<AcquiredImageInStack>> makeStackImagesMap(
            List<AcquiredSingleImage> images, ISpotProvider spotProvider, long datasetId)
    {
        Map<ImgChannelStackDTO, List<AcquiredImageInStack>> map =
                new HashMap<ImgChannelStackDTO, List<AcquiredImageInStack>>();

        Set<ImgChannelStackDTO> newChannelStacks = new HashSet<ImgChannelStackDTO>();

        for (AcquiredSingleImage image : images)
        {
            ImgChannelStackDTO stackDTO =
                    getOrCreateStackDtoWithouId(image, spotProvider, datasetId, newChannelStacks);
            List<AcquiredImageInStack> stackImages = map.get(stackDTO);
            if (stackImages == null)
            {
                stackImages = new ArrayList<AcquiredImageInStack>();
            }
            stackImages.add(makeAcquiredImageInStack(image));
            map.put(stackDTO, stackImages);
        }
        Set<ImgChannelStackDTO> channelStacks = map.keySet();
        setChannelStackIds(newChannelStacks);
        setChannelStackRepresentatives(channelStacks);

        dao.addChannelStacks(newChannelStacks);

        return map;
    }

    private void setChannelStackRepresentatives(Set<ImgChannelStackDTO> channelStacks)
    {
        Set<ImgChannelStackDTO> representatives =
                ChannelStackRepresentativesOracle.calculateRepresentatives(channelStacks);
        for (ImgChannelStackDTO channelStack : channelStacks)
        {
            boolean isRepresentative = representatives.contains(channelStack);
            channelStack.setRepresentative(isRepresentative);
        }
    }

    private void setChannelStackIds(Collection<ImgChannelStackDTO> channelStacks)
    {
        for (ImgChannelStackDTO channelStack : channelStacks)
        {
            if (channelStack.getId() == DUMMY_ID)
            {
                channelStack.setId(dao.createChannelStackId());
            }
        }
    }

    private static AcquiredImageInStack makeAcquiredImageInStack(AcquiredSingleImage image)
    {
        return new AcquiredImageInStack(image.getChannelCode(), image.getImageReference(),
                image.getThumbnailFilePathOrNull(), image.tryGetImageTransformerFactory());
    }

    private ImgChannelStackDTO getOrCreateStackDtoWithouId(AcquiredSingleImage image,
            ISpotProvider spotProvider, long datasetId,
            Collection<ImgChannelStackDTO> newChannelStacks)
    {
        Long spotId = spotProvider.tryGetSpotId(image);

        ImgChannelStackDTO newChannelStack =
                new ImgChannelStackDTO(DUMMY_ID, image.getTileRow(), image.getTileColumn(),
                        datasetId, spotId, image.tryGetTimePoint(), image.tryGetDepth(),
                        image.tryGetSeriesNumber(), false);

        List<ImgChannelStackDTO> channelStacks = null;
        if (spotId != null)
        {
            channelStacks = dao.listChannelStacks(datasetId, spotId);
        } else
        {
            channelStacks = dao.listChannelStacks(datasetId);
        }

        for (ImgChannelStackDTO channelStack : channelStacks)
        {
            if (newChannelStack.equals(channelStack))
            {
                return channelStack;
            }
        }

        newChannelStacks.add(newChannelStack);
        return newChannelStack;
    }

    private void createImages(Map<ImgChannelStackDTO, List<AcquiredImageInStack>> stackImagesMap,
            ImagingChannelsMap channelsMap, long datasetId)
    {
        ImagesToCreate imagesToCreate =
                new ImagesToCreate(new ArrayList<ImgImageDTO>(),
                        new ArrayList<ImgAcquiredImageDTO>(), new ArrayList<ImgAcquiredImageDTO>());

        List<ImgAcquiredImageDTO> acquiredImages = dao.listAllAcquiredImagesForDataSet(datasetId);
        Map<Long, List<ImgAcquiredImageDTO>> acquiredImagesInStacks =
                new HashMap<Long, List<ImgAcquiredImageDTO>>();
        for (ImgAcquiredImageDTO acquiredImage : acquiredImages)
        {
            List<ImgAcquiredImageDTO> acquiredImagesInStack =
                    acquiredImagesInStacks.get(acquiredImage.getChannelStackId());
            if (acquiredImagesInStack == null)
            {
                acquiredImagesInStack = new ArrayList<ImgAcquiredImageDTO>();
                acquiredImagesInStacks
                        .put(acquiredImage.getChannelStackId(), acquiredImagesInStack);
            }
            acquiredImagesInStack.add(acquiredImage);
        }

        for (Entry<ImgChannelStackDTO, List<AcquiredImageInStack>> entry : stackImagesMap
                .entrySet())
        {
            long stackId = entry.getKey().getId();
            addImagesToCreate(imagesToCreate, stackId, channelsMap, entry.getValue(),
                    acquiredImagesInStacks.get(stackId));
        }
        dao.addImages(imagesToCreate.getImages());
        dao.addAcquiredImages(imagesToCreate.getAcquiredImages());
        dao.updateAcquiredImagesThumbnails(imagesToCreate.getAcquiredImagesToUpdate());
    }

    /**
     * Because we can have millions of images, we have to create them in batches. That is why we create all the DTOs first and generate ids for them
     * before they are created in the database. Then we can save everything in one go.
     * 
     * @param list
     */
    private void addImagesToCreate(ImagesToCreate imagesToCreate, long stackId,
            ImagingChannelsMap channelsMap, List<AcquiredImageInStack> images,
            List<ImgAcquiredImageDTO> alreadyRegisteredImages)
    {
        List<ImgImageDTO> imageDTOs = imagesToCreate.getImages();
        List<ImgAcquiredImageDTO> acquiredImageDTOs = imagesToCreate.getAcquiredImages();
        for (AcquiredImageInStack image : images)
        {
            long channelTechId = channelsMap.getChannelId(image.getChannelCode());

            ImgAcquiredImageDTO alreadyRegisteredImage = null;
            if (alreadyRegisteredImages != null)
            {
                for (ImgAcquiredImageDTO alreadyRegisteredImageCandidate : alreadyRegisteredImages)
                {
                    if (alreadyRegisteredImageCandidate.getChannelId() == channelTechId)
                    {
                        alreadyRegisteredImage = alreadyRegisteredImageCandidate;
                        break;
                    }
                }
            }

            if (alreadyRegisteredImage == null)
            {
                ImgImageDTO imageDTO = mkImageWithIdDTO(image.getImageFilePath());
                imageDTOs.add(imageDTO);

                ImgImageDTO thumbnailDTO = tryMkImageWithIdDTO(image.getThumbnailPathOrNull());
                Long thumbnailId = null;
                if (thumbnailDTO != null)
                {
                    thumbnailId = thumbnailDTO.getId();
                    imageDTOs.add(thumbnailDTO);
                }

                ImgAcquiredImageDTO acquiredImage =
                        mkAcquiredImage(stackId, channelTechId, imageDTO.getId(), thumbnailId,
                                image.getImageTransformerFactoryOrNull());

                acquiredImageDTOs.add(acquiredImage);
            } else if (alreadyRegisteredImage.getThumbnailId() == null)
            {
                ImgImageDTO thumbnailDTO = tryMkImageWithIdDTO(image.getThumbnailPathOrNull());
                Long thumbnailId = null;
                if (thumbnailDTO != null)
                {
                    thumbnailId = thumbnailDTO.getId();
                    imageDTOs.add(thumbnailDTO);
                    alreadyRegisteredImage.setThumbnailId(thumbnailId);
                    imagesToCreate.getAcquiredImagesToUpdate().add(alreadyRegisteredImage);
                }
            }
        }
    }

    private static class ImagesToCreate
    {
        private final List<ImgImageDTO> images;

        private final List<ImgAcquiredImageDTO> acquiredImages;

        private final List<ImgAcquiredImageDTO> acquiredImagesToUpdate;

        public ImagesToCreate(List<ImgImageDTO> images, List<ImgAcquiredImageDTO> acquiredImages,
                List<ImgAcquiredImageDTO> acquiredImagesToUpdate)
        {
            super();
            this.images = images;
            this.acquiredImages = acquiredImages;
            this.acquiredImagesToUpdate = acquiredImagesToUpdate;
        }

        public List<ImgImageDTO> getImages()
        {
            return images;
        }

        public List<ImgAcquiredImageDTO> getAcquiredImages()
        {
            return acquiredImages;
        }

        public List<ImgAcquiredImageDTO> getAcquiredImagesToUpdate()
        {
            return acquiredImagesToUpdate;
        }
    }

    private ImgAcquiredImageDTO mkAcquiredImage(long stackId, long channelTechId, long imageId,
            Long thumbnailId, IImageTransformerFactory transformerFactoryOrNull)
    {
        ImgAcquiredImageDTO acquiredImage = new ImgAcquiredImageDTO();
        acquiredImage.setImageId(imageId);
        acquiredImage.setThumbnailId(thumbnailId);
        acquiredImage.setChannelStackId(stackId);
        acquiredImage.setChannelId(channelTechId);
        acquiredImage.setImageTransformerFactory(transformerFactoryOrNull);
        return acquiredImage;
    }

    private ImgImageDTO tryMkImageWithIdDTO(RelativeImageReference imageReferenceOrNull)
    {
        if (imageReferenceOrNull == null)
        {
            return null;
        }
        return mkImageWithIdDTO(imageReferenceOrNull);
    }

    private ImgImageDTO mkImageWithIdDTO(RelativeImageReference imageReference)
    {

        ImgImageDTO dto =
                new ImgImageDTO(dao.createImageId(), imageReference.getImageRelativePath(),
                        imageReference.tryGetImageID(), imageReference.tryGetColorComponent());
        return dto;
    }

    protected final long getOrCreateImageDataset(String datasetPermId,
            ImageDatasetInfo imageDatasetInfo, Long containerIdOrNull)
    {
        ImgImageDatasetDTO imageDataSet = dao.tryGetImageDatasetByPermId(datasetPermId);
        if (imageDataSet == null)
        {
            ImgImageDatasetDTO dataset =
                    createImageDatasetDTO(datasetPermId, imageDatasetInfo, containerIdOrNull);
            long imageContainerDatasetId = dao.addImageDataset(dataset);

            return imageContainerDatasetId;
        } else
        {
            return imageDataSet.getId();
        }
    }

    private static ImgImageDatasetDTO createImageDatasetDTO(String datasetPermId,
            ImageDatasetInfo imageDatasetInfo, Long containerIdOrNull)
    {
        ImageLibraryInfo imageLibrary = imageDatasetInfo.tryGetImageLibrary();
        String imageLibraryName = null;
        String imageReaderName = null;
        if (imageLibrary != null)
        {
            imageLibraryName = imageLibrary.getName();
            imageReaderName = imageLibrary.getReaderName();
        }
        return new ImgImageDatasetDTO(datasetPermId, imageDatasetInfo.getTileRows(),
                imageDatasetInfo.getTileColumns(), containerIdOrNull,
                imageDatasetInfo.hasImageSeries(), imageLibraryName, imageReaderName);
    }

    @Private
    static final class ChannelStackRepresentativesOracle
    {
        /**
         * For all images of one spot chooses the 'smallest' element as an representative. If there are no spots than the smallest element of all
         * specified images is chosen.
         */
        public static Set<ImgChannelStackDTO> calculateRepresentatives(
                Set<ImgChannelStackDTO> images)
        {
            Map<Long/* spot or null */, List<ImgChannelStackDTO>> mapBySpot =
                    createMapBySpot(images);
            Comparator<? super ImgChannelStackDTO> spotChannelStacksComparator =
                    createChannelStacksComparator();
            for (List<ImgChannelStackDTO> spotChannelStacks : mapBySpot.values())
            {
                Collections.sort(spotChannelStacks, spotChannelStacksComparator);
            }

            Set<ImgChannelStackDTO> representatives = new HashSet<ImgChannelStackDTO>();
            for (List<ImgChannelStackDTO> spotChannelStacks : mapBySpot.values())
            {
                representatives.add(spotChannelStacks.get(0));
            }
            return representatives;
        }

        private static Comparator<? super ImgChannelStackDTO> createChannelStacksComparator()
        {
            return new Comparator<ImgChannelStackDTO>()
                {
                    @Override
                    public int compare(ImgChannelStackDTO o1, ImgChannelStackDTO o2)
                    {
                        int cmp = compareNullable(o1.getRow(), o2.getRow());
                        if (cmp != 0)
                            return cmp;
                        cmp = compareNullable(o1.getColumn(), o2.getColumn());
                        if (cmp != 0)
                            return cmp;
                        cmp = compareNullable(o1.getT(), o2.getT());
                        if (cmp != 0)
                            return cmp;
                        cmp = compareNullable(o1.getZ(), o2.getZ());
                        if (cmp != 0)
                            return cmp;
                        cmp = compareNullable(o1.getSeriesNumber(), o2.getSeriesNumber());
                        return cmp;
                    }

                    private <T extends Comparable<T>> int compareNullable(T v1OrNull, T v2OrNull)
                    {
                        if (v1OrNull == null)
                        {
                            return v2OrNull == null ? 0 : -1;
                        } else
                        {
                            return v2OrNull == null ? 1 : v1OrNull.compareTo(v2OrNull);
                        }
                    }
                };
        }

        private static Map<Long/* spot or null */, List<ImgChannelStackDTO>> createMapBySpot(
                Set<ImgChannelStackDTO> channelStacks)
        {
            Map<Long, List<ImgChannelStackDTO>> mapBySpot =
                    new HashMap<Long, List<ImgChannelStackDTO>>();
            for (ImgChannelStackDTO channelStack : channelStacks)
            {
                Long spotId = channelStack.getSpotId();
                List<ImgChannelStackDTO> spotChannelStacks = mapBySpot.get(spotId);
                if (spotChannelStacks == null)
                {
                    spotChannelStacks = new ArrayList<ImgChannelStackDTO>();
                }
                spotChannelStacks.add(channelStack);
                mapBySpot.put(spotId, spotChannelStacks);
            }
            return mapBySpot;
        }
    }
}

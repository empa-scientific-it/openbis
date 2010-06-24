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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.openbis.dss.etl.ScreeningContainerDatasetInfoHelper.ExperimentWithChannelsAndContainer;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.IImagingUploadDAO;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.ImgAcquiredImageDTO;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.ImgChannelStackDTO;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.ImgImageDTO;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.ImgSpotDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * @author Tomasz Pylak
 */
public class HCSDatasetUploader
{
    public static void upload(IImagingUploadDAO dao, ScreeningContainerDatasetInfo info,
            List<AcquiredPlateImage> images, Set<HCSImageFileExtractionResult.Channel> channels)
    {
        new HCSDatasetUploader(dao).upload(info, images, channels);
    }

    private final IImagingUploadDAO dao;

    private HCSDatasetUploader(IImagingUploadDAO dao)
    {
        this.dao = dao;
    }

    private void upload(ScreeningContainerDatasetInfo info, List<AcquiredPlateImage> images,
            Set<HCSImageFileExtractionResult.Channel> channels)
    {
        ExperimentWithChannelsAndContainer basicStruct =
                ScreeningContainerDatasetInfoHelper.getOrCreateExperimentWithChannelsAndContainer(
                        dao, info, channels);
        long contId = basicStruct.getContainerId();
        Map<String, Long/* (tech id */> channelsMap = basicStruct.getChannelsMap();
        Long[][] spotIds = getOrCreateSpots(contId, info, images);
        long datasetId = createDataset(contId, info);

        createImages(images, spotIds, channelsMap, datasetId);
    }

    private static class AcquiredImageInStack
    {
        private final String channelName;

        private final RelativeImageReference imageFilePath;

        private final RelativeImageReference thumbnailPathOrNull;

        public AcquiredImageInStack(String channelName, RelativeImageReference imageFilePath,
                RelativeImageReference thumbnailPathOrNull)
        {
            this.channelName = channelName;
            this.imageFilePath = imageFilePath;
            this.thumbnailPathOrNull = thumbnailPathOrNull;
        }

        public String getChannelName()
        {
            return channelName;
        }

        public RelativeImageReference getImageFilePath()
        {
            return imageFilePath;
        }

        public final RelativeImageReference getThumbnailPathOrNull()
        {
            return thumbnailPathOrNull;
        }
    }

    private void createImages(List<AcquiredPlateImage> images, Long[][] spotIds,
            Map<String, Long> channelsMap, long datasetId)
    {
        Map<ImgChannelStackDTO, List<AcquiredImageInStack>> stackImagesMap =
                makeStackImagesMap(images, spotIds, datasetId);
        createChannelStacks(stackImagesMap.keySet());
        createImages(stackImagesMap, channelsMap);
    }

    private static Map<ImgChannelStackDTO, List<AcquiredImageInStack>> makeStackImagesMap(
            List<AcquiredPlateImage> images, Long[][] spotIds, long datasetId)
    {
        Map<ImgChannelStackDTO, List<AcquiredImageInStack>> map =
                new HashMap<ImgChannelStackDTO, List<AcquiredImageInStack>>();
        for (AcquiredPlateImage image : images)
        {
            ImgChannelStackDTO stackDTO = makeStackDTO(image, spotIds, datasetId);
            List<AcquiredImageInStack> stackImages = map.get(stackDTO);
            if (stackImages == null)
            {
                stackImages = new ArrayList<AcquiredImageInStack>();
            }
            stackImages.add(makeAcquiredImageInStack(image));
            map.put(stackDTO, stackImages);
        }
        return map;
    }

    private static AcquiredImageInStack makeAcquiredImageInStack(AcquiredPlateImage image)
    {
        return new AcquiredImageInStack(image.getChannelName(), image.getImageReference(), image
                .getThumbnailFilePathOrNull());
    }

    private static ImgChannelStackDTO makeStackDTO(AcquiredPlateImage image, Long[][] spotIds,
            long datasetId)
    {
        long spotId = getSpotId(image, spotIds);
        return new ImgChannelStackDTO(image.getTileRow(), image.getTileColumn(), datasetId, spotId);
    }

    private static long getSpotId(AcquiredPlateImage image, Long[][] spotIds)
    {
        int wellRow = image.getWellRow();
        int wellColumn = image.getWellColumn();
        Long spotId = spotIds[wellRow - 1][wellColumn - 1];
        assert spotId != null : "no spot for " + image;
        return spotId;
    }

    private void createImages(Map<ImgChannelStackDTO, List<AcquiredImageInStack>> stackImagesMap,
            Map<String, Long> channelsMap)
    {
        for (Entry<ImgChannelStackDTO, List<AcquiredImageInStack>> entry : stackImagesMap
                .entrySet())
        {
            long stackId = entry.getKey().getId();
            createImages(stackId, channelsMap, entry.getValue());
        }
    }

    private void createImages(long stackId, Map<String, Long> channelsMap,
            List<AcquiredImageInStack> images)
    {
        for (AcquiredImageInStack image : images)
        {
            String channelName = image.getChannelName().toUpperCase();
            Long channelTechId = channelsMap.get(channelName);
            if (channelTechId == null)
            {
                throw new EnvironmentFailureException("Invalid channel name " + channelName
                        + ". Available channels: " + channelsMap.keySet());
            }
            createImage(stackId, channelTechId, image);
        }
    }

    private void createImage(long stackId, long channelTechId, AcquiredImageInStack image)
    {
        long imageId = addImage(image.getImageFilePath());
        Long thumbnailId = addImage(image.getThumbnailPathOrNull());
        ImgAcquiredImageDTO acquiredImage = new ImgAcquiredImageDTO();
        acquiredImage.setImageId(imageId);
        acquiredImage.setThumbnailId(thumbnailId);
        acquiredImage.setChannelStackId(stackId);
        acquiredImage.setChannelId(channelTechId);
        dao.addAcquiredImage(acquiredImage);
    }

    private Long addImage(RelativeImageReference imageReferenceOrNull)
    {
        if (imageReferenceOrNull == null)
        {
            return null;
        }
        return dao.addImage(new ImgImageDTO(imageReferenceOrNull.getRelativeImagePath(),
                imageReferenceOrNull.tryGetPage(), imageReferenceOrNull.tryGetColorComponent()));
    }

    private void createChannelStacks(Set<ImgChannelStackDTO> stacks)
    {
        for (ImgChannelStackDTO stack : stacks)
        {
            long id = dao.addChannelStack(stack);
            stack.setId(id);
        }
    }

    // returns a matrix of spot tech ids. The matrix[row][col] contains null is spot at (row,col)
    // does not exist. Spot coordinates are 0-based in the matrix.
    private Long[][] getOrCreateSpots(long contId, ScreeningContainerDatasetInfo info,
            List<AcquiredPlateImage> images)
    {
        List<ImgSpotDTO> oldSpots = dao.listSpots(contId);
        List<ImgSpotDTO> newSpots =
                createNewSpots(contId, images, oldSpots, info.getContainerRows(), info
                        .getContainerColumns(), info.getContainerPermId());
        newSpots.addAll(oldSpots);
        return makeTechIdMatrix(newSpots, info.getContainerRows(), info.getContainerColumns());
    }

    private List<ImgSpotDTO> createNewSpots(long contId, List<AcquiredPlateImage> images,
            List<ImgSpotDTO> existingSpots, int rows, int columns, String containerPermId)
    {
        Boolean[][] newSpotMatrix = extractNewSpots(rows, columns, images, existingSpots);
        List<ImgSpotDTO> newSpots = makeSpotDTOs(newSpotMatrix, contId);
        enrichWithPermIds(newSpots, containerPermId);
        for (ImgSpotDTO spot : newSpots)
        {
            long id = dao.addSpot(spot);
            spot.setId(id);
        }
        return newSpots;
    }

    private void enrichWithPermIds(List<ImgSpotDTO> newSpots, String containerPermId)
    {
        Map<String, String> permIds = getOrCreateWells(newSpots, containerPermId);
        for (ImgSpotDTO spot : newSpots)
        {
            spot.setPermId(permIds.get(createCoordinate(spot)));
        }
    }

    private Map<String, String> getOrCreateWells(List<ImgSpotDTO> newSpots, String containerPermId)
    {
        IEncapsulatedOpenBISService server = ServiceProvider.getOpenBISService();
        Set<String> codes = new HashSet<String>();
        for (ImgSpotDTO spot : newSpots)
        {
            codes.add(createCoordinate(spot));
        }
        return server.listOrRegisterComponents(containerPermId, codes,
                ScreeningConstants.OLIGO_WELL_TYPE_CODE);
    }

    private static String createCoordinate(ImgSpotDTO spot)
    {
        return Location.tryCreateMatrixCoordinateFromLocation(new Location(spot.getColumn(), spot
                .getRow()));
    }

    private static Boolean[][] extractNewSpots(int rows, int columns,
            List<AcquiredPlateImage> images, List<ImgSpotDTO> existingSpots)
    {
        Boolean[][] spots = extractExistingSpots(rows, columns, images);
        unmarkSpots(existingSpots, spots);
        return spots;
    }

    private static Boolean[][] extractExistingSpots(int rows, int columns,
            List<AcquiredPlateImage> images)
    {
        Boolean[][] spots = new Boolean[rows][columns];
        for (AcquiredPlateImage image : images)
        {
            spots[image.getWellRow() - 1][image.getWellColumn() - 1] = true;
        }
        return spots;
    }

    private static Long[][] makeTechIdMatrix(List<ImgSpotDTO> existingSpots, int rows, int columns)
    {
        Long[][] matrix = new Long[rows][columns];
        for (ImgSpotDTO spot : existingSpots)
        {
            matrix[spot.getRow() - 1][spot.getColumn() - 1] = spot.getId();
        }
        return matrix;
    }

    private static List<ImgSpotDTO> makeSpotDTOs(Boolean[][] spots, long contId)
    {

        List<ImgSpotDTO> newSpots = new ArrayList<ImgSpotDTO>();
        for (int row = 0; row < spots.length; row++)
        {
            Boolean[] spotRow = spots[row];
            for (int col = 0; col < spotRow.length; col++)
            {
                Boolean wanted = spotRow[col];
                if (wanted != null && wanted)
                {
                    newSpots.add(new ImgSpotDTO(row + 1, col + 1, contId));
                }
            }
        }
        return newSpots;
    }

    private static void unmarkSpots(List<ImgSpotDTO> existingSpots, Boolean[][] spotMatrix)
    {
        for (ImgSpotDTO existingSpot : existingSpots)
        {
            spotMatrix[existingSpot.getRow() - 1][existingSpot.getColumn() - 1] = false;
        }
    }

    private long createDataset(long contId, ScreeningContainerDatasetInfo info)
    {
        return ScreeningContainerDatasetInfoHelper.createDataset(dao, info, contId);
    }
}

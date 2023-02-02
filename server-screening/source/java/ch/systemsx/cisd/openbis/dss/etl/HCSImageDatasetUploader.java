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
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.dss.etl.ImagingDatabaseHelper.ExperimentWithChannelsAndContainer;
import ch.systemsx.cisd.openbis.dss.etl.ImagingDatabaseHelper.ImagingChannelsMap;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.IImagingQueryDAO;
import ch.systemsx.cisd.openbis.dss.etl.dto.ImageZoomLevel;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.Channel;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageZoomLevelDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgSpotDTO;

/**
 * Uploads HCS images (with spots in the container) into imaging database.
 * 
 * @author Tomasz Pylak
 */
public class HCSImageDatasetUploader extends AbstractImageDatasetUploader
{
    public static void upload(IImagingQueryDAO dao, HCSImageDatasetInfo info,
            List<AcquiredSingleImage> images, List<Channel> channels)
    {
        new HCSImageDatasetUploader(dao).upload(info, images, channels);
    }

    private HCSImageDatasetUploader(IImagingQueryDAO dao)
    {
        super(dao);
    }

    @SuppressWarnings("null")
    private void upload(HCSImageDatasetInfo info, List<AcquiredSingleImage> images,
            List<Channel> channels)
    {
        long contId;
        ImagingChannelsMap channelsMap = null;
        if (info.isStoreChannelsOnExperimentLevel())
        {
            ExperimentWithChannelsAndContainer basicStruct =
                    ImagingDatabaseHelper.getOrCreateExperimentWithChannelsAndContainer(dao, info,
                            channels);
            contId = basicStruct.getContainerId();
            channelsMap = basicStruct.getChannelsMap();
        } else
        {
            contId = ImagingDatabaseHelper.getOrCreateExperimentAndContainer(dao, info);
        }

        Long[][] spotIds = getOrCreateSpots(contId, info, images);
        ISpotProvider spotProvider = getSpotProvider(spotIds);
        long datasetId = getOrCreateDataset(contId, info);

        if (info.isStoreChannelsOnExperimentLevel() == false)
        {
            channelsMap = ImagingDatabaseHelper.getOrCreateDatasetChannels(dao, datasetId, channels);
        }
        assert channelsMap != null;

        for (ImageZoomLevel imageZoomLevel : info.getImageDatasetInfo().getImageZoomLevels())
        {
            long zoomLevelId = dao.addImageZoomLevel(convert(datasetId, imageZoomLevel));
            for (Map.Entry<String, String> entry : imageZoomLevel.getTransformation().entrySet())
            {
                dao.addImageZoomLevelTransformation(zoomLevelId,
                        channelsMap.getChannelId(entry.getKey()), entry.getValue());
            }
        }

        createImages(images, spotProvider, channelsMap, datasetId);
    }

    private ImgImageZoomLevelDTO convert(long imageContainerDatasetId, ImageZoomLevel imageZoomLevel)
    {
        return new ImgImageZoomLevelDTO(imageZoomLevel.getPhysicalDatasetPermId(),
                imageZoomLevel.isOriginal(), imageZoomLevel.getRootPath(),
                imageZoomLevel.getWidth(), imageZoomLevel.getHeight(),
                imageZoomLevel.getColorDepth(), imageZoomLevel.getFileType(),
                imageContainerDatasetId);
    }

    private static ISpotProvider getSpotProvider(final Long[][] spotIds)
    {
        return new ISpotProvider()
            {
                @Override
                public Long tryGetSpotId(AcquiredSingleImage image)
                {
                    return findSpotId(image, spotIds);
                }
            };
    }

    private static long findSpotId(AcquiredSingleImage image, Long[][] spotIds)
    {
        int wellRow = image.getWellRow();
        int wellColumn = image.getWellColumn();
        Long spotId = spotIds[wellRow - 1][wellColumn - 1];
        assert spotId != null : "no spot for " + image;
        return spotId;
    }

    // returns a matrix of spot tech ids. The matrix[row][col] contains null is
    // spot at (row,col)
    // does not exist. Spot coordinates are 0-based in the matrix.
    private Long[][] getOrCreateSpots(long contId, HCSContainerDatasetInfo info,
            List<AcquiredSingleImage> images)
    {
        List<ImgSpotDTO> oldSpots = dao.listSpots(contId);
        List<ImgSpotDTO> newSpots =
                createNewSpots(contId, images, oldSpots, info.getContainerRows(),
                        info.getContainerColumns(), info.getContainerSamplePermId());
        newSpots.addAll(oldSpots);
        return makeTechIdMatrix(newSpots, info.getContainerRows(), info.getContainerColumns());
    }

    private List<ImgSpotDTO> createNewSpots(long contId, List<AcquiredSingleImage> images,
            List<ImgSpotDTO> existingSpots, int rows, int columns, String containerPermId)
    {
        Boolean[][] newSpotMatrix = extractNewSpots(rows, columns, images, existingSpots);
        List<ImgSpotDTO> newSpots = makeSpotDTOs(newSpotMatrix, contId);
        for (ImgSpotDTO spot : newSpots)
        {
            long id = dao.addSpot(spot);
            spot.setId(id);
        }
        return newSpots;
    }

    private static Boolean[][] extractNewSpots(int rows, int columns,
            List<AcquiredSingleImage> images, List<ImgSpotDTO> existingSpots)
    {
        Boolean[][] spots = extractExistingSpots(rows, columns, images);
        unmarkSpots(existingSpots, spots);
        return spots;
    }

    private static Boolean[][] extractExistingSpots(int rows, int columns,
            List<AcquiredSingleImage> images)
    {
        Boolean[][] spots = new Boolean[rows][columns];
        for (AcquiredSingleImage image : images)
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

    private long getOrCreateDataset(long containerId, HCSImageDatasetInfo info)
    {
        return getOrCreateImageDataset(info.getDatasetPermId(), info.getImageDatasetInfo(),
                containerId);
    }
}

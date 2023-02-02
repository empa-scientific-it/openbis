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

import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.dss.etl.ImagingDatabaseHelper.ImagingChannelsMap;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.IImagingQueryDAO;
import ch.systemsx.cisd.openbis.dss.etl.dto.ImageZoomLevel;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.Channel;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageZoomLevelDTO;

/**
 * Uploads microscopy imagaes (no spots, no container) into imaging database.
 * 
 * @author Tomasz Pylak
 */
public class MicroscopyImageDatasetUploader extends AbstractImageDatasetUploader
{
    public static void upload(IImagingQueryDAO dao, MicroscopyImageDatasetInfo dataset,
            List<AcquiredSingleImage> images, List<Channel> channels)
    {
        new MicroscopyImageDatasetUploader(dao).upload(dataset, images, channels);
    }

    private MicroscopyImageDatasetUploader(IImagingQueryDAO dao)
    {
        super(dao);
    }

    private void upload(MicroscopyImageDatasetInfo dataset, List<AcquiredSingleImage> images,
            List<Channel> channels)
    {
        long datasetId = getOrCreateMicroscopyDataset(dataset);
        ImagingChannelsMap channelsMap =
                ImagingDatabaseHelper.getOrCreateDatasetChannels(dao, datasetId, channels);

        for (ImageZoomLevel imageZoomLevel : dataset.getImageDatasetInfo().getImageZoomLevels())
        {
            long zoomLevelId = dao.addImageZoomLevel(convert(datasetId, imageZoomLevel));
            for (Map.Entry<String, String> entry : imageZoomLevel.getTransformation().entrySet())
            {
                dao.addImageZoomLevelTransformation(zoomLevelId,
                        channelsMap.getChannelId(entry.getKey()), entry.getValue());
            }
        }

        createImages(images, createDummySpotProvider(), channelsMap, datasetId);
    }

    private ImgImageZoomLevelDTO convert(long imageContainerDatasetId, ImageZoomLevel imageZoomLevel)
    {
        return new ImgImageZoomLevelDTO(imageZoomLevel.getPhysicalDatasetPermId(),
                imageZoomLevel.isOriginal(), imageZoomLevel.getRootPath(),
                imageZoomLevel.getWidth(), imageZoomLevel.getHeight(),
                imageZoomLevel.getColorDepth(), imageZoomLevel.getFileType(),
                imageContainerDatasetId);
    }

    private static ISpotProvider createDummySpotProvider()
    {
        return new ISpotProvider()
            {
                @Override
                public Long tryGetSpotId(AcquiredSingleImage image)
                {
                    return null;
                }
            };
    }

    private long getOrCreateMicroscopyDataset(MicroscopyImageDatasetInfo dataset)
    {
        return getOrCreateImageDataset(dataset.getDatasetPermId(), dataset.getImageDatasetInfo(),
                null);
    }
}

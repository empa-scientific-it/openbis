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

package ch.systemsx.cisd.openbis.plugin.screening.shared.imaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageResolution;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingReadonlyQueryDAO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageDatasetDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageZoomLevelDTO;

/**
 * @author pkupczyk
 */
public class HCSImageResolutionLoader implements IImageResolutionLoader
{

    static protected final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            HCSImageResolutionLoader.class);

    protected final IImagingReadonlyQueryDAO query;

    protected final ImgImageDatasetDTO dataset;

    public static HCSImageResolutionLoader tryCreate(IImagingReadonlyQueryDAO query,
            String datasetPermId)
    {
        ImgImageDatasetDTO dataset = query.tryGetImageDatasetByPermId(datasetPermId);
        if (dataset == null)
        {
            operationLog.warn(String.format(
                    "No dataset with code '%s' found in the imaging database.", datasetPermId));
            return null;
        } else
        {
            return new HCSImageResolutionLoader(query, dataset);
        }
    }

    private HCSImageResolutionLoader(IImagingReadonlyQueryDAO query, ImgImageDatasetDTO dataset)
    {
        this.query = query;
        this.dataset = dataset;
    }

    @Override
    public List<ImageResolution> getImageResolutions()
    {
        List<ImgImageZoomLevelDTO> zoomLevels =
                query.listImageZoomLevelsWithNoTransformations(dataset.getId());

        if (zoomLevels == null || zoomLevels.isEmpty())
        {
            return Collections.emptyList();
        } else
        {
            List<ImageResolution> resolutions = new ArrayList<ImageResolution>(zoomLevels.size());

            for (ImgImageZoomLevelDTO zoomLevel : zoomLevels)
            {
                ImageResolution resolution =
                        new ImageResolution(zoomLevel.getWidth(), zoomLevel.getHeight(), zoomLevel.getIsOriginal());
                resolutions.add(resolution);
            }

            return resolutions;
        }
    }
}

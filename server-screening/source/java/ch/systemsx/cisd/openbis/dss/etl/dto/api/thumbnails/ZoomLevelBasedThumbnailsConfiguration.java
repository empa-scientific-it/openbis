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

package ch.systemsx.cisd.openbis.dss.etl.dto.api.thumbnails;

import ch.systemsx.cisd.openbis.dss.etl.dto.api.SimpleImageDataConfig;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.ThumbnailsStorageFormat;

/**
 * @author Pawel Glyzewski
 */
public class ZoomLevelBasedThumbnailsConfiguration extends AbstractThumbnailsConfiguration
{
    private final double zoomLevel;

    public ZoomLevelBasedThumbnailsConfiguration(double zoomLevel)
    {
        this.zoomLevel = zoomLevel;
    }

    @Override
    public ThumbnailsStorageFormat getThumbnailsStorageFormat(SimpleImageDataConfig config)
    {
        ThumbnailsStorageFormat thumbnailsStorageFormat = super.getThumbnailsStorageFormat(config);
        thumbnailsStorageFormat.setZoomLevel(zoomLevel);
        return thumbnailsStorageFormat;
    }

    @Override
    protected String getDefaultFileName()
    {
        return String.format("thumbnails_%.0fpct%s.h5ar", zoomLevel * 100.0,
                getFirstTransformationCode());
    }
}

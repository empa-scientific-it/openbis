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

package ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess;

import net.lemnik.eodsql.ResultColumn;

/**
 * HCS image with additional information about the spot and acquired image id.
 * 
 * @author Tomasz Pylak
 */
public class ImgImageEnrichedDTO extends ImgImageDTO
{
    @ResultColumn("spot_id")
    private long spotId;

    @ResultColumn("acquired_image_id")
    private long acquiredImageId;

    @SuppressWarnings("unused")
    private ImgImageEnrichedDTO()
    {
        // All Data-Object classes must have a default constructor.
        super();
    }

    public ImgImageEnrichedDTO(long id, String filePath, String imageIdOrNull,
            ColorComponent colorComponentOrNull)
    {
        super(id, filePath, imageIdOrNull, colorComponentOrNull);
    }

    public long getSpotId()
    {
        return spotId;
    }

    public long getAcquiredImageId()
    {
        return acquiredImageId;
    }

}

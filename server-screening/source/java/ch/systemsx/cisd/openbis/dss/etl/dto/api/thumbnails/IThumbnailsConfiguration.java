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
public interface IThumbnailsConfiguration
{
    public void setFileName(String fileName);

    public void setFileFormat(String fileFormat);

    public String setTransformation(String channelCode, String transformarionCode);

    /**
     * Gets a storage format that describes how thumbnails should be generated. Please note that changes made to the returned object will NOT change
     * the parameters of thumbnails generation process. To change the way thumbnails should be generated (e.g. set file name, file format or
     * transformations) please use {@link #setFileName(String)}, {@link #setFileFormat(String)} and {@link #setTransformation(String, String)}
     * methods.
     */
    public ThumbnailsStorageFormat getThumbnailsStorageFormat(SimpleImageDataConfig config);

}

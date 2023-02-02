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

package ch.systemsx.cisd.openbis.dss.etl.dto;

import java.io.Serializable;

import ch.systemsx.cisd.common.reflection.AbstractHashable;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.ImageFileInfo;

/**
 * Points to one image on the file system.
 * 
 * @author Tomasz Pylak
 */
public class RelativeImageFile extends AbstractHashable implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final String imageRelativePath;

    private final String imageIDOrNull;

    public static RelativeImageFile create(ImageFileInfo imageFileInfo)
    {
        return new RelativeImageFile(imageFileInfo.getImageRelativePath(),
                imageFileInfo.tryGetUniqueStringIdentifier());
    }

    public RelativeImageFile(String imageRelativePath, String imageIDOrNull)
    {
        assert imageRelativePath != null : "imageRelativePath is null";

        this.imageRelativePath = imageRelativePath;
        this.imageIDOrNull = imageIDOrNull;
    }

    /** Path relative to the folder with all the images. */
    public String getImageRelativePath()
    {
        return imageRelativePath;
    }

    /**
     * Not null if the file pointed by {@link #getImageRelativePath()} is an image container with potentially many images inside. In this case image
     * id determines the image in the container.
     */
    public String tryGetImageID()
    {
        return imageIDOrNull;
    }
}

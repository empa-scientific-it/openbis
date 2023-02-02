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

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ColorComponent;

/**
 * Reference to the image with a path relative to the folder with all the images.
 * 
 * @author Tomasz Pylak
 */
public class RelativeImageReference extends AbstractImageReference
{
    private String imageRelativePath;

    public RelativeImageReference(String relativePath, String imageIdOrNull,
            ColorComponent colorComponentOrNull)
    {
        super(imageIdOrNull, colorComponentOrNull);
        this.imageRelativePath = relativePath;
    }

    public String getImageRelativePath()
    {
        return imageRelativePath;
    }

    public final void setRelativeImageFolder(String folderPathPrefix)
    {
        this.imageRelativePath =
                StringUtils.isBlank(imageRelativePath) ? folderPathPrefix.substring(0,
                        folderPathPrefix.length() - 1) : folderPathPrefix + imageRelativePath;
    }
}
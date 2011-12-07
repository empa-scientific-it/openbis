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

/**
 * Describes an image dataset with 1. all images resized in the same way or 2. original images.
 * 
 * @author Tomasz Pylak
 */
public class ImageZoomLevel
{
    private final String physicalDatasetPermId;

    private final boolean isOriginal;

    private final String rootPath;

    private final Integer width;

    private final Integer height;

    public ImageZoomLevel(String physicalDatasetPermId, boolean isOriginal, String rootPath,
            Integer width, Integer height)
    {
        this.physicalDatasetPermId = physicalDatasetPermId;
        this.isOriginal = isOriginal;
        this.rootPath = rootPath;
        this.width = width;
        this.height = height;
    }

    public String getPhysicalDatasetPermId()
    {
        return physicalDatasetPermId;
    }

    public boolean isOriginal()
    {
        return isOriginal;
    }

    public Integer getWidth()
    {
        return width;
    }

    public Integer getHeight()
    {
        return height;
    }

    public String getRootPath()
    {
        return rootPath;
    }
}

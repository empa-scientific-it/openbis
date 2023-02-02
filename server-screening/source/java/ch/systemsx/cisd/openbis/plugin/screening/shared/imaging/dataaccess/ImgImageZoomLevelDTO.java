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
 * Describes an image dataset with 1. all images resized in the same way or 2. original images.
 * 
 * @author Tomasz Pylak
 */
public class ImgImageZoomLevelDTO extends AbstractImgIdentifiable
{
    @ResultColumn("physical_dataset_perm_id")
    private String physicalDatasetPermId;

    @ResultColumn("is_original")
    private boolean isOriginal;

    @ResultColumn("container_dataset_id")
    private long containerDatasetId;

    @ResultColumn("path")
    private String rootPath;

    @ResultColumn("width")
    private Integer width;

    @ResultColumn("height")
    private Integer height;

    @ResultColumn("color_depth")
    private Integer colorDepth;

    @ResultColumn("file_type")
    private String fileType;

    @SuppressWarnings("unused")
    private ImgImageZoomLevelDTO()
    {
        // All Data-Object classes must have a default constructor.
    }

    public ImgImageZoomLevelDTO(String physicalDatasetPermId, boolean isOriginal, String rootPath,
            Integer width, Integer height, Integer colorDepth, String fileType,
            long containerDatasetId)
    {
        this.physicalDatasetPermId = physicalDatasetPermId;
        this.isOriginal = isOriginal;
        this.containerDatasetId = containerDatasetId;
        this.rootPath = rootPath;
        this.width = width;
        this.height = height;
        this.colorDepth = colorDepth;
        this.fileType = fileType;
    }

    public String getPhysicalDatasetPermId()
    {
        return physicalDatasetPermId;
    }

    public void setPhysicalDatasetPermId(String physicalDatasetPermId)
    {
        this.physicalDatasetPermId = physicalDatasetPermId;
    }

    public boolean getIsOriginal()
    {
        return isOriginal;
    }

    public void setOriginal(boolean isOriginal)
    {
        this.isOriginal = isOriginal;
    }

    public long getContainerDatasetId()
    {
        return containerDatasetId;
    }

    public void setContainerDatasetId(long containerDatasetId)
    {
        this.containerDatasetId = containerDatasetId;
    }

    public String getRootPath()
    {
        return rootPath;
    }

    public void setRootPath(String rootPath)
    {
        this.rootPath = rootPath;
    }

    public Integer getWidth()
    {
        return width;
    }

    public void setWidth(Integer width)
    {
        this.width = width;
    }

    public Integer getHeight()
    {
        return height;
    }

    public void setHeight(Integer height)
    {
        this.height = height;
    }

    public Integer getColorDepth()
    {
        return colorDepth;
    }

    public void setColorDepth(Integer colorDepth)
    {
        this.colorDepth = colorDepth;
    }

    public String getFileType()
    {
        return fileType;
    }

    public void setFileType(String fileType)
    {
        this.fileType = fileType;
    }

}

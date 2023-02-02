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

import java.util.List;

/**
 * Information about the dataset specific for the image datasets (hcs and microscopy).
 * 
 * @author Tomasz Pylak
 */
public class ImageDatasetInfo
{
    private final int tileRows, tileColumns;

    // has any well timepoints or depth stack images?
    private final boolean hasImageSeries;

    private final ImageLibraryInfo imageLibraryOrNull;

    private final List<ImageZoomLevel> imageZoomLevels;

    public ImageDatasetInfo(int tileRows, int tileColumns, boolean hasImageSeries,
            ImageLibraryInfo imageLibraryOrNull, List<ImageZoomLevel> imageZoomLevels)
    {
        this.tileRows = tileRows;
        this.tileColumns = tileColumns;
        this.hasImageSeries = hasImageSeries;
        this.imageLibraryOrNull = imageLibraryOrNull;
        this.imageZoomLevels = imageZoomLevels;
    }

    public int getTileRows()
    {
        return tileRows;
    }

    public int getTileColumns()
    {
        return tileColumns;
    }

    public boolean hasImageSeries()
    {
        return hasImageSeries;
    }

    public ImageLibraryInfo tryGetImageLibrary()
    {
        return imageLibraryOrNull;
    }

    public List<ImageZoomLevel> getImageZoomLevels()
    {
        return imageZoomLevels;
    }
}

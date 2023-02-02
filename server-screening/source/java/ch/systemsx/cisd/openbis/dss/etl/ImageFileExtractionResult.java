/*
 * Copyright 2008 ETH Zuerich, CISD
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

import java.io.File;
import java.util.List;

import ch.systemsx.cisd.hcs.Geometry;
import ch.systemsx.cisd.openbis.dss.etl.dto.ImageLibraryInfo;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.Channel;

/**
 * Class which contains the image extraction process results.
 * 
 * @author Tomasz Pylak
 */
public final class ImageFileExtractionResult
{
    /** The images files with description. */
    private final List<AcquiredSingleImage> images;

    /**
     * Path to the incoming folder with images, relative to the dataset directory. E.g. if the incoming folder name is X and the transaction's dataset
     * registration code put it inside 'original' folder, then this path points to "original/X'.
     */
    private File datasetRelativeImagesFolderPath;

    /** The invalid files found. */
    private final List<File> invalidFiles;

    private final List<Channel> channels;

    /** How many tile rows and columns are there on each spot? */
    private final Geometry tileGeometry;

    private final Boolean storeChannelsOnExperimentLevelOrNull;

    private final ImageLibraryInfo imageLibraryOrNull;

    public ImageFileExtractionResult(List<AcquiredSingleImage> images,
            File datasetRelativeImagesFolderPath, List<File> invalidFiles, List<Channel> channels,
            Geometry tileGeometry, Boolean storeChannelsOnExperimentLevelOrNull,
            ImageLibraryInfo imageLibraryOrNull)
    {
        this.images = images;
        this.datasetRelativeImagesFolderPath = datasetRelativeImagesFolderPath;
        this.invalidFiles = invalidFiles;
        this.channels = channels;
        this.tileGeometry = tileGeometry;
        this.storeChannelsOnExperimentLevelOrNull = storeChannelsOnExperimentLevelOrNull;
        this.imageLibraryOrNull = imageLibraryOrNull;
    }

    public List<AcquiredSingleImage> getImages()
    {
        return images;
    }

    public File getDatasetRelativeImagesFolderPath()
    {
        return datasetRelativeImagesFolderPath;
    }

    public List<File> getInvalidFiles()
    {
        return invalidFiles;
    }

    public List<Channel> getChannels()
    {
        return channels;
    }

    public Geometry getTileGeometry()
    {
        return tileGeometry;
    }

    public Boolean tryStoreChannelsOnExperimentLevel()
    {
        return storeChannelsOnExperimentLevelOrNull;
    }

    public ImageLibraryInfo tryGetImageLibrary()
    {
        return imageLibraryOrNull;
    }
}
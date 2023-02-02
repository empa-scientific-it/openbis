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

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.hcs.Geometry;
import ch.systemsx.cisd.hcs.Location;
import ch.systemsx.cisd.openbis.dss.etl.PlateStorageProcessor.DatasetOwnerInformation;
import ch.systemsx.cisd.openbis.dss.etl.PlateStorageProcessor.ImageDatasetOwnerInformation;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.IImagingQueryDAO;
import ch.systemsx.cisd.openbis.dss.etl.dto.ImageDatasetInfo;
import ch.systemsx.cisd.openbis.dss.etl.dto.ImageLibraryInfo;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.ImageFileInfo;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ChannelDescription;

/**
 * Storage processor which stores microscopy images in a special-purpose imaging database. Image files do not have to adhere to any naming convention,
 * it's assumed that there is exactly one tile, one channel, no time or depth dimensions. Images are sorted lexicographically and subsequent series
 * numbers are assigned to them.
 * <p>
 * In this storage processor one can set neither well geometry, channels nor image file extractor.<br>
 * See {@link AbstractImageStorageProcessor} documentation to check how to configure thumbnails generation.
 * 
 * @author Tomasz Pylak
 */
public class MicroscopyBlackboxSeriesStorageProcessor extends AbstractImageStorageProcessor
{
    private static final String DEFAULT_CHANNEL_CODE = "DEFAULT";

    private static final String DEFAULT_CHANNEL_LABEL = "Default";

    private static final Location DEFAULT_TILE = new Location(1, 1);

    private static final Geometry DEFAULT_TILE_GEOMETRY = new Geometry(1, 1);

    private static final List<ChannelDescription> DEFAULT_CHANNELS = Arrays
            .asList(new ChannelDescription(DEFAULT_CHANNEL_CODE, DEFAULT_CHANNEL_LABEL));

    public MicroscopyBlackboxSeriesStorageProcessor(Properties properties)
    {
        super(new BlackboxSeriesImageFileExtractor(properties), properties);
    }

    private static class BlackboxSeriesImageFileExtractor extends AbstractImageFileExtractor
    {
        protected BlackboxSeriesImageFileExtractor(Properties properties)
        {
            super(DEFAULT_CHANNELS, DEFAULT_TILE_GEOMETRY, true, properties);
        }

        @Override
        protected ImageFileInfo tryExtractImageInfo(File imageFile, File incomingDataSetDirectory,
                SampleIdentifier datasetSample)
        {
            String imageRelativePath = getRelativeImagePath(incomingDataSetDirectory, imageFile);
            // we postpone assigning series numbers until all images are extracted
            ImageFileInfo info =
                    new ImageFileInfo(DEFAULT_CHANNEL_CODE, DEFAULT_TILE.getY(),
                            DEFAULT_TILE.getX(), imageRelativePath);
            return info;
        }
    }

    @Override
    protected void storeInDatabase(IImagingQueryDAO dao,
            ImageDatasetOwnerInformation dataSetInformation,
            ImageFileExtractionResult extractedImages, boolean thumbnailsOnly)
    {
        List<AcquiredSingleImage> images = extractedImages.getImages();
        setSeriesNumber(images);
        MicroscopyImageDatasetInfo dataset =
                createMicroscopyImageDatasetInfo(dataSetInformation, images,
                        extractedImages.getTileGeometry(), extractedImages.tryGetImageLibrary());

        MicroscopyImageDatasetUploader.upload(dao, dataset, images, extractedImages.getChannels());
    }

    private void setSeriesNumber(List<AcquiredSingleImage> images)
    {
        Collections.sort(images, createPathComparator());
        int seriesNumber = 1;
        for (AcquiredSingleImage image : images)
        {
            image.setSeriesNumber(seriesNumber++);
        }
    }

    private Comparator<AcquiredSingleImage> createPathComparator()
    {
        return new Comparator<AcquiredSingleImage>()
            {
                @Override
                public int compare(AcquiredSingleImage o1, AcquiredSingleImage o2)
                {
                    return getPath(o1).compareTo(getPath(o2));
                }

                private String getPath(AcquiredSingleImage o1)
                {
                    return o1.getImageReference().getImageRelativePath();
                }
            };
    }

    private MicroscopyImageDatasetInfo createMicroscopyImageDatasetInfo(
            ImageDatasetOwnerInformation dataSetInformation, List<AcquiredSingleImage> images,
            Geometry tileGeometry, ImageLibraryInfo imageLibraryInfoOrNull)
    {
        boolean hasImageSeries = hasImageSeries(images);
        ImageDatasetInfo imageDatasetInfo =
                new ImageDatasetInfo(tileGeometry.getRows(), tileGeometry.getColumns(),
                        hasImageSeries, imageLibraryInfoOrNull,
                        dataSetInformation.getImageZoomLevels());
        return new MicroscopyImageDatasetInfo(dataSetInformation.getDataSetCode(), imageDatasetInfo);
    }

    @Override
    protected boolean validateImages(DatasetOwnerInformation dataSetInformation,
            IMailClient mailClient, File incomingDataSetDirectory,
            ImageFileExtractionResult extractionResult)
    {
        // do nothing - for now we do not have good examples of real data
        return true;
    }

}

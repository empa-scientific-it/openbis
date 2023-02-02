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
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.hcs.Geometry;
import ch.systemsx.cisd.openbis.dss.etl.PlateStorageProcessor.DatasetOwnerInformation;
import ch.systemsx.cisd.openbis.dss.etl.PlateStorageProcessor.ImageDatasetOwnerInformation;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.IImagingQueryDAO;
import ch.systemsx.cisd.openbis.dss.etl.dto.ImageDatasetInfo;
import ch.systemsx.cisd.openbis.dss.etl.dto.ImageLibraryInfo;

/**
 * Storage processor which stores microscopy images in a special-purpose imaging database.
 * <p>
 * See {@link AbstractImageStorageProcessor} documentation.
 * 
 * @author Tomasz Pylak
 */
public class MicroscopyStorageProcessor extends AbstractImageStorageProcessor
{

    public MicroscopyStorageProcessor(Properties properties)
    {
        super(properties);
    }

    @Override
    protected void storeInDatabase(IImagingQueryDAO dao,
            ImageDatasetOwnerInformation dataSetInformation,
            ImageFileExtractionResult extractedImages, boolean thumbnailsOnly)
    {
        List<AcquiredSingleImage> images = extractedImages.getImages();
        MicroscopyImageDatasetInfo dataset =
                createMicroscopyImageDatasetInfo(dataSetInformation, images,
                        extractedImages.getTileGeometry(), extractedImages.tryGetImageLibrary());

        MicroscopyImageDatasetUploader.upload(dao, dataset, images, extractedImages.getChannels());
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
        ImageValidator validator =
                new ImageValidator(dataSetInformation, mailClient, incomingDataSetDirectory,
                        extractionResult, operationLog, notificationLog, false);
        validator.validateImages();
        return true;
    }
}

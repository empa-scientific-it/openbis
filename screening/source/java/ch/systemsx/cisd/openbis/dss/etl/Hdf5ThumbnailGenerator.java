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

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.concurrent.ITaskExecutor;
import ch.systemsx.cisd.common.concurrent.ParallelizedExecutor;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.etlserver.hdf5.Hdf5Container.IHdf5WriterClient;
import ch.systemsx.cisd.hdf5.IHDF5SimpleWriter;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.ThumbnailsStorageFormat;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ImageUtil;

/**
 * Utility class for generating thumbnails into an HDF5 container.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class Hdf5ThumbnailGenerator implements IHdf5WriterClient
{
    private static final int MAX_RETRY_OF_FAILED_GENERATION = 3;

    private final List<AcquiredSingleImage> plateImages;

    private final File imagesInStoreFolder;

    private final ThumbnailsStorageFormat thumbnailsStorageFormat;

    private final String relativeThumbnailFilePath;

    private final Logger operationLog;

    Hdf5ThumbnailGenerator(List<AcquiredSingleImage> plateImages, File imagesInStoreFolder,
            ThumbnailsStorageFormat thumbnailsStorageFormat, String relativeThumbnailFilePath,
            Logger operationLog)
    {
        this.plateImages = plateImages;
        this.imagesInStoreFolder = imagesInStoreFolder;
        this.thumbnailsStorageFormat = thumbnailsStorageFormat;
        this.relativeThumbnailFilePath = relativeThumbnailFilePath;
        this.operationLog = operationLog;
    }

    /**
     * @param bufferOutputStream auxiliary stream which can be used as a temporary buffer to save
     *            the thumbnail. Using it allows not to allocate memory each time when a thumbnail
     *            is generated.
     */
    private Status generateThumbnail(IHDF5SimpleWriter writer, AcquiredSingleImage plateImage,
            ByteArrayOutputStream bufferOutputStream)
    {
        long start = System.currentTimeMillis();
        RelativeImageReference imageReference = plateImage.getImageReference();
        String imagePath = imageReference.getRelativeImagePath();
        File img = new File(imagesInStoreFolder, imagePath);
        BufferedImage image = ImageUtil.loadImage(img);
        BufferedImage thumbnail =
                ImageUtil.rescale(image, thumbnailsStorageFormat.getMaxWidth(),
                        thumbnailsStorageFormat.getMaxHeight(), false,
                        thumbnailsStorageFormat.isHighQuality());
        String thumbnailPath = replaceExtensionToPng(imagePath);
        try
        {
            ImageIO.write(thumbnail, "png", bufferOutputStream);

            String path =
                    relativeThumbnailFilePath + ContentRepository.ARCHIVE_DELIMITER + thumbnailPath;
            plateImage.setThumbnailFilePathOrNull(new RelativeImageReference(path, imageReference
                    .tryGetPage(), imageReference.tryGetColorComponent()));
            byte[] byteArray = bufferOutputStream.toByteArray();
            if (operationLog.isDebugEnabled())
            {
                long now = System.currentTimeMillis();
                operationLog.debug(Thread.currentThread().getName() + " thumbnail " + thumbnailPath
                        + " (" + byteArray.length + " bytes) generated in " + (now - start)
                        + " msec");
            }

            synchronized (writer)
            {
                writer.writeByteArray(thumbnailPath, byteArray);
            }
        } catch (IOException ex)
        {
            operationLog
                    .warn("Retriable error when creating thumbnail '" + thumbnailPath + "'", ex);
            return Status.createRetriableError(String.format(
                    "Could not generate a thumbnail '%s': %s", thumbnailPath, ex.getMessage()));
        }
        return Status.OK;
    }

    private static String replaceExtensionToPng(String imagePath)
    {
        String newImagePath = imagePath;
        int lastIndex = imagePath.lastIndexOf('.');
        if (lastIndex > 0)
        {
            newImagePath = imagePath.substring(0, lastIndex);
        }
        newImagePath += ".png";
        return newImagePath;
    }

    private ITaskExecutor<AcquiredSingleImage> createThumbnailGenerator(
            final IHDF5SimpleWriter writer)
    {
        return new ITaskExecutor<AcquiredSingleImage>()
            {
                private ThreadLocal<ByteArrayOutputStream> outputStreamBuffers =
                        new ThreadLocal<ByteArrayOutputStream>()
                            {
                                @Override
                                protected ByteArrayOutputStream initialValue()
                                {
                                    return new ByteArrayOutputStream();
                                }
                            };

                public Status execute(AcquiredSingleImage plateImage)
                {
                    // each thread will get its own buffer to avoid allocating memory for the
                    // internal array each time
                    ByteArrayOutputStream outputStreamBuffer = outputStreamBuffers.get();
                    outputStreamBuffer.reset();
                    return generateThumbnail(writer, plateImage, outputStreamBuffer);
                }
            };
    }

    public void runWithSimpleWriter(IHDF5SimpleWriter writer)
    {
        ParallelizedExecutor.process(plateImages, createThumbnailGenerator(writer),
                thumbnailsStorageFormat.getAllowedMachineLoadDuringGeneration(), 100,
                "Thumbnails generation", MAX_RETRY_OF_FAILED_GENERATION);
    }
}

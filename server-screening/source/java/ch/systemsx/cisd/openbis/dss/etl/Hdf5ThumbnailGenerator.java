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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.image.IImageTransformer;
import ch.systemsx.cisd.base.utilities.OSUtilities;
import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.concurrent.FailureRecord;
import ch.systemsx.cisd.common.concurrent.ITaskExecutor;
import ch.systemsx.cisd.common.concurrent.ParallelizedExecutor;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.image.IntensityRescaling;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.process.ProcessExecutionHelper;
import ch.systemsx.cisd.common.process.ProcessIOStrategy;
import ch.systemsx.cisd.common.process.ProcessResult;
import ch.systemsx.cisd.openbis.common.hdf5.HDF5Container;
import ch.systemsx.cisd.openbis.common.hdf5.HDF5Container.IHDF5WriterClient;
import ch.systemsx.cisd.openbis.common.hdf5.IHDF5ContainerWriter;
import ch.systemsx.cisd.openbis.common.io.ByteArrayBasedContentNode;
import ch.systemsx.cisd.openbis.common.io.FileBasedContentNode;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.etl.dto.ImageLibraryInfo;
import ch.systemsx.cisd.openbis.dss.etl.dto.RelativeImageFile;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.Channel;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColorComponent;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColorRGB;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.ImageFileInfo;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.ImageStorageConfiguraton;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.ThumbnailsStorageFormat;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.ImageDataSetStructure;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.ThumbnailsInfo;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.transformations.ImageTransformation;
import ch.systemsx.cisd.openbis.dss.generic.server.images.ImageChannelsUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.Size;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ImageUtil;
import ch.systemsx.cisd.openbis.dss.shared.DssScreeningUtils;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ColorComponent;

/**
 * Utility class for generating thumbnails into an HDF5 container.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class Hdf5ThumbnailGenerator implements IHDF5WriterClient
{
    private static class ThumbnailData
    {
        private final byte[] data;

        private final int width;

        private final int height;

        private final int colorDepth;

        private final String channelCode;

        private ThumbnailData(byte[] data, int width, int height, int colorDepth, String channelCode)
        {
            this.data = data;
            this.width = width;
            this.height = height;
            this.colorDepth = colorDepth;
            this.channelCode = channelCode;
        }
    }

    /**
     * Generates thumbnails of specified images whose paths are relative to the specified image parent directory.
     * 
     * @param imageDataSetStructure the images dataset structure, including paths relative to the imagesParentDirectory
     * @param thumbnailFilePath absolute path to the file where thumbnails will be saved
     * @param imageStorageConfiguraton describes how the thumbnails should be generated
     * @param content
     */
    public static void tryGenerateThumbnails(ImageDataSetStructure imageDataSetStructure,
            File imagesParentDirectory, String thumbnailFilePath,
            ImageStorageConfiguraton imageStorageConfiguraton,
            String thumbnailPhysicalDatasetPermId,
            ThumbnailsStorageFormat thumbnailsStorageFormatOrNull, ThumbnailsInfo thumbnailPaths,
            boolean registerOriginalImageAsThumbnail, IHierarchicalContent content, ImageCache imageCache)
    {
        if (thumbnailsStorageFormatOrNull != null)
        {
            thumbnailPaths.putDataSet(thumbnailPhysicalDatasetPermId,
                    thumbnailsStorageFormatOrNull.getThumbnailsFileName(),
                    thumbnailsStorageFormatOrNull.getFileFormat(),
                    thumbnailsStorageFormatOrNull.getTransformations());
            File thumbnailsFile = new File(thumbnailFilePath);

            HDF5Container container = new HDF5Container(thumbnailsFile);
            ImageLibraryInfo imageLibrary = imageStorageConfiguraton.tryGetImageLibrary();
            Hdf5ThumbnailGenerator thumbnailsGenerator =
                    new Hdf5ThumbnailGenerator(imageDataSetStructure, imagesParentDirectory,
                            thumbnailPhysicalDatasetPermId, thumbnailsStorageFormatOrNull,
                            imageLibrary, thumbnailPaths, registerOriginalImageAsThumbnail,
                            content, imageCache, operationLog);
            container.runWriterClient(thumbnailsStorageFormatOrNull.isStoreCompressed(),
                    thumbnailsGenerator);
        }
    }

    private static final File convertUtilityOrNull = OSUtilities.findExecutable("convert");

    private static final Logger machineLog = LogFactory.getLogger(LogCategory.MACHINE,
            Hdf5ThumbnailGenerator.class);

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            Hdf5ThumbnailGenerator.class);

    private static final int MAX_RETRY_OF_FAILED_GENERATION = 3;

    private final ImageDataSetStructure imageDataSetStructure;

    private final Map<String, Map<String, ImageTransformation>> transformationsForChannels =
            new HashMap<String, Map<String, ImageTransformation>>();

    private final File imagesParentDirectory;

    private String thumbnailPhysicalDatasetPermId;

    private final ThumbnailsStorageFormat thumbnailsStorageFormat;

    private final ImageLibraryInfo imageLibraryOrNull;

    private final ThumbnailsInfo thumbnailPathCollector;

    private final Logger logger;

    private final Map<String, ColorComponent> channelColors = new HashMap<String, ColorComponent>();

    private final Map<String, IntensityRescaling.Channel> representativeChannels = new HashMap<String, IntensityRescaling.Channel>();

    private final boolean registerOriginalImageAsThumbnail;

    private IHierarchicalContent contentOrNull;

    private ImageCache imageCache;

    private Hdf5ThumbnailGenerator(ImageDataSetStructure imageDataSetStructure,
            File imagesParentDirectory, String thumbnailPhysicalDatasetPermId,
            ThumbnailsStorageFormat thumbnailsStorageFormat, ImageLibraryInfo imageLibraryOrNull,
            ThumbnailsInfo thumbnailPathCollector, boolean registerOriginalImageAsThumbnail,
            IHierarchicalContent contentOrNull, ImageCache imageCache, Logger operationLog)
    {
        this.imageDataSetStructure = imageDataSetStructure;
        this.imagesParentDirectory = imagesParentDirectory;
        this.thumbnailPhysicalDatasetPermId = thumbnailPhysicalDatasetPermId;
        this.thumbnailsStorageFormat = thumbnailsStorageFormat;
        this.imageLibraryOrNull = imageLibraryOrNull;
        this.thumbnailPathCollector = thumbnailPathCollector;
        this.registerOriginalImageAsThumbnail = registerOriginalImageAsThumbnail;
        this.contentOrNull = contentOrNull;
        this.imageCache = imageCache;
        this.logger = operationLog;

        for (Channel ch : imageDataSetStructure.getChannels())
        {
            Map<String, ImageTransformation> transformations =
                    new HashMap<String, ImageTransformation>();
            transformationsForChannels.put(ch.getCode(), transformations);
            representativeChannels.put(ch.getCode(), getRepresentativeColorChannelOrNull(ch));
            if (ch.getAvailableTransformations() != null)
            {
                for (ImageTransformation it : ch.getAvailableTransformations())
                {
                    transformations.put(it.getCode().toUpperCase(), it);
                }
            }
        }

        if (imageDataSetStructure.getChannelColorComponents() != null
                && imageDataSetStructure.getChannelColorComponents().size() > 0)
        {
            for (int i = 0; i < imageDataSetStructure.getChannelColorComponents().size(); i++)
            {
                channelColors.put(imageDataSetStructure.getChannels().get(i).getCode(),
                        ChannelColorComponent.getColorComponent(imageDataSetStructure
                                .getChannelColorComponents().get(i)));
            }
        }
    }

    private IntensityRescaling.Channel getRepresentativeColorChannelOrNull(Channel ch)
    {
        ChannelColorRGB channelColor = ch.tryGetChannelColor();
        if (channelColor == null)
        {
            return null;
        }
        IntensityRescaling.Channel channel = IntensityRescaling.Channel.RED;
        int max = channelColor.getR();
        if (max < channelColor.getG())
        {
            channel = IntensityRescaling.Channel.GREEN;
            max = channelColor.getG();
        }
        if (max < channelColor.getB())
        {
            channel = IntensityRescaling.Channel.BLUE;
        }
        return channel;
    }

    /**
     * @param bufferOutputStream auxiliary stream which can be used as a temporary buffer to save the thumbnail. Using it allows not to allocate
     *            memory each time when a thumbnail is generated.
     */
    private Status generateThumbnail(IHDF5ContainerWriter writer, ImageFileInfo image,
            ByteArrayOutputStream bufferOutputStream)
    {
        List<String> thumbnailPaths = new ArrayList<String>();
        for (String channelCode : getChannelsToProcess(image.getChannelCode()))
        {
            thumbnailPaths.add(createThumbnailPath(image, channelCode));
        }

        try
        {
            long start = System.currentTimeMillis();
            String imageIdOrNull = image.tryGetUniqueStringIdentifier();

            List<ThumbnailData> thumbnailData =
                    generateThumbnail(bufferOutputStream, image, imageIdOrNull);

            for (int i = 0; i < thumbnailData.size(); i++)
            {
                thumbnailPathCollector.saveThumbnailPath(thumbnailPhysicalDatasetPermId,
                        RelativeImageFile.create(image),
                        channelColors.get(thumbnailData.get(i).channelCode), thumbnailPaths.get(i),
                        thumbnailData.get(i).width, thumbnailData.get(i).height,
                        thumbnailData.get(i).colorDepth);
            }

            if (logger.isDebugEnabled())
            {
                int size = 0;
                for (ThumbnailData td : thumbnailData)
                {
                    size += td.data.length;
                }

                long now = System.currentTimeMillis();
                logger.debug(Thread.currentThread().getName() + " thumbnail "
                        + thumbnailPaths.toString() + " (" + size + " bytes) generated in "
                        + (now - start) + " msec");
            }
            synchronized (writer)
            {
                for (int i = 0; i < thumbnailData.size(); i++)
                {
                    writer.writeToHDF5Container(thumbnailPaths.get(i) + "."
                            + thumbnailsStorageFormat.getFileFormat().getFileExtension(),
                            new ByteArrayInputStream(thumbnailData.get(i).data),
                            thumbnailData.get(i).data.length);
                }
            }
        } catch (IOException ex)
        {
            return createStatus(thumbnailPaths.toString(), ex);
        }
        return Status.OK;
    }

    public static String createThumbnailPath(ImageFileInfo plateImage, String channelCode)
    {
        StringBuilder newImagePath = new StringBuilder();
        if (plateImage.hasWellLocation())
        {
            newImagePath.append("w").append(plateImage.tryGetWellLocation().toWellIdString());
        }
        newImagePath.append(underscoreIfNeeded(newImagePath)).append("d")
                .append(plateImage.getTileColumn()).append("-").append(plateImage.getTileRow());
        if (plateImage.tryGetTimepoint() != null)
        {
            newImagePath.append("_t").append(plateImage.tryGetTimepoint());
        }
        if (plateImage.tryGetSeriesNumber() != null)
        {
            newImagePath.append("_s").append(plateImage.tryGetSeriesNumber());
        }
        if (plateImage.tryGetDepth() != null)
        {
            newImagePath.append("_h").append(plateImage.tryGetDepth());
        }
        newImagePath.append("_c").append(channelCode);

        String imageIdOrNull = plateImage.tryGetUniqueStringIdentifier();
        if (imageIdOrNull != null)
        {
            newImagePath.append(imageIdOrNull);
        }
        return newImagePath.toString();
    }

    private static String underscoreIfNeeded(StringBuilder sb)
    {
        return sb.length() > 0 ? "_" : "";
    }

    private List<ThumbnailData> generateThumbnail(ByteArrayOutputStream bufferOutputStream,
            ImageFileInfo img, String imageIdOrNull) throws IOException
    {
        List<ThumbnailData> thumbnailData;

        if (registerOriginalImageAsThumbnail)
        {
            thumbnailData = registerOriginalImageAsThumbnail(img);
        } else if (thumbnailsStorageFormat.isGenerateWithImageMagic())
        {
            thumbnailData = generateThumbnailWithImageMagic(img);
        } else
        {
            thumbnailData = generateThumbnailInternally(img, imageIdOrNull, bufferOutputStream);
        }
        return thumbnailData;
    }

    private List<ThumbnailData> registerOriginalImageAsThumbnail(ImageFileInfo imageFileInfo)
    {
        final File imageFile =
                new File(imagesParentDirectory, imageFileInfo.getImageRelativePath());
        Size originalSize = loadUnchangedImageDimension(imageFileInfo.getImageRelativePath(), null);
        int colorDepth = loadUnchangedImageColorDepth(imageFileInfo.getImageRelativePath(), null);

        return Collections.singletonList(new ThumbnailData(
                FileUtilities.loadToByteArray(imageFile), originalSize.getWidth(), originalSize
                        .getHeight(), colorDepth, imageFileInfo.getChannelCode()));
    }

    private List<ThumbnailData> generateThumbnailWithImageMagic(ImageFileInfo imageFileInfo)
            throws IOException
    {
        int width = thumbnailsStorageFormat.getMaxWidth();
        int height = thumbnailsStorageFormat.getMaxHeight();

        if (thumbnailsStorageFormat.getZoomLevel() != null)
        {
            Size originalSize =
                    loadUnchangedImageDimension(imageFileInfo.getImageRelativePath(), null);
            double zoomLevel = thumbnailsStorageFormat.getZoomLevel();
            width = (int) Math.round(zoomLevel * originalSize.getWidth());
            height = (int) Math.round(zoomLevel * originalSize.getHeight());
        }
        String size = width + "x" + height;

        int colorDepth = loadUnchangedImageColorDepth(imageFileInfo.getImageRelativePath(), null);

        String imageFilePath = null;
        if (contentOrNull == null)
        {
            imageFilePath =
                    new File(imagesParentDirectory, imageFileInfo.getImageRelativePath()).getPath();
        } else
        {
            imageFilePath =
                    contentOrNull.getNode(imageFileInfo.getImageRelativePath()).getFile().getPath();
        }

        List<String> params = new ArrayList<String>();

        if (convertUtilityOrNull == null)
        {
            throw new EnvironmentFailureException("Trying to generate thimbnails with ImageMagic, but 'convert' executable not found on the OS path.");
        }

        params.addAll(Arrays.asList(convertUtilityOrNull.getPath(), imageFilePath, "-scale", size));
        List<String> additionalParams = thumbnailsStorageFormat.getImageMagicParams();
        if (additionalParams != null)
        {
            params.addAll(additionalParams);
        }
        params.add(thumbnailsStorageFormat.getFileFormat().getImageMagickParam() + ":-");
        final ProcessResult result =
                ProcessExecutionHelper.run(params, logger, machineLog,
                        ConcurrencyUtilities.NO_TIMEOUT,
                        ProcessIOStrategy.BINARY_DISCARD_STDERR_IO_STRATEGY, false);
        if (result.isOK() == false)
        {
            throw new IOException(String.format(
                    "Error calling 'convert' with parameters %s for image '%s'. Exit value: %d, I/O status: %s",
                    params, imageFilePath, result.getExitValue(), result.getProcessIOResult().getStatus()));
        } else
        {
            if ((imageDataSetStructure.getChannelColorComponents() != null && imageDataSetStructure
                    .getChannelColorComponents().size() > 0)
                    || isTransformationAvailable(imageFileInfo.getChannelCode()))
            {
                List<ThumbnailData> thumbnails = new ArrayList<ThumbnailData>();
                for (String channelCode : getChannelsToProcess(imageFileInfo.getChannelCode()))
                {
                    BufferedImage thumbnail =
                            Utils.loadUnchangedImage(
                                    new ByteArrayBasedContentNode(result.getBinaryOutput(), null),
                                    null, imageLibraryOrNull);

                    thumbnail =
                            applyTransformationsChain(thumbnail, channelCode,
                                    channelColors.get(channelCode));

                    ByteArrayOutputStream bufferOutputStream = new ByteArrayOutputStream();
                    thumbnailsStorageFormat.getFileFormat().writeImage(thumbnail,
                            bufferOutputStream);
                    thumbnails.add(new ThumbnailData(bufferOutputStream.toByteArray(), width,
                            height, colorDepth, channelCode));

                }
                return thumbnails;
            } else
            {
                return Collections.singletonList(new ThumbnailData(result.getBinaryOutput(), width,
                        height, colorDepth, imageFileInfo.getChannelCode()));
            }
        }
    }

    private boolean isTransformationAvailable(String channelCode)
    {
        Map<String, ImageTransformation> transformationsForChannel =
                transformationsForChannels.get(channelCode);

        return transformationsForChannel != null
                && transformationsForChannel.containsKey(thumbnailsStorageFormat
                        .getTransformationCode(channelCode));
    }

    private List<ThumbnailData> generateThumbnailInternally(ImageFileInfo imageFileInfo,
            String imageIdOrNull, ByteArrayOutputStream bufferOutputStream) throws IOException
    {
        BufferedImage image = loadUnchangedImage(imageFileInfo.getImageRelativePath(), imageIdOrNull);
        int widht = thumbnailsStorageFormat.getMaxWidth();
        int height = thumbnailsStorageFormat.getMaxHeight();
        if (thumbnailsStorageFormat.getZoomLevel() != null)
        {
            widht = (int) Math.round(thumbnailsStorageFormat.getZoomLevel() * image.getWidth());
            height = (int) Math.round(thumbnailsStorageFormat.getZoomLevel() * image.getHeight());
        }

        boolean highQuality = thumbnailsStorageFormat.isHighQuality();
        IntensityRescaling.Channel representativeChannelOrNull = representativeChannels.get(imageFileInfo.getChannelCode());
        BufferedImage rescaledImage = ImageUtil.rescale(image, widht, height, false, highQuality,
                representativeChannelOrNull, DssScreeningUtils.CONVERTER);

        final List<ThumbnailData> thumbnails = new ArrayList<ThumbnailData>();
        for (String channelCode : getChannelsToProcess(imageFileInfo.getChannelCode()))
        {
            ColorComponent colorComponent = channelColors.get(channelCode);
            BufferedImage thumbnail = applyTransformationsChain(rescaledImage, channelCode, colorComponent);
            bufferOutputStream.reset();
            thumbnailsStorageFormat.getFileFormat().writeImage(thumbnail, bufferOutputStream);
            thumbnails.add(new ThumbnailData(bufferOutputStream.toByteArray(),
                    thumbnail.getWidth(), thumbnail.getHeight(), thumbnail.getColorModel()
                            .getPixelSize(), channelCode));
        }
        return thumbnails;
    }

    private Collection<String> getChannelsToProcess(String channelCode)
    {
        if (imageDataSetStructure.getChannelColorComponents() != null
                && imageDataSetStructure.getChannelColorComponents().size() > 0)
        {
            return channelColors.keySet();
        } else
        {
            return Collections.singleton(channelCode);
        }
    }

    private BufferedImage applyTransformationsChain(BufferedImage image,
            ColorComponent colorComponent, IImageTransformer transformer)
    {
        BufferedImage extractedChannelImage = ImageChannelsUtils.extractChannel(image, colorComponent);
        return applyTransformationIfNeeded(extractedChannelImage, transformer);
    }

    private BufferedImage applyTransformationsChain(BufferedImage image, String channelCode,
            ColorComponent colorComponent)
    {
        return applyTransformationsChain(image, colorComponent, tryCreateImageTransformer(channelCode));
    }

    private static BufferedImage applyTransformationIfNeeded(BufferedImage image,
            IImageTransformer transformer)
    {
        if (transformer != null)
        {
            return transformer.transform(image);
        }

        return image;
    }

    private IImageTransformer tryCreateImageTransformer(String channelCode)
    {
        String transformationCodeOrNull =
                thumbnailsStorageFormat.getTransformationCode(channelCode);
        if (transformationCodeOrNull != null)
        {
            Map<String, ImageTransformation> transformationsForChannel =
                    transformationsForChannels.get(channelCode);

            ImageTransformation it =
                    transformationsForChannel == null ? null : transformationsForChannel
                            .get(transformationCodeOrNull.toUpperCase());
            if (it != null)
            {
                return it.getImageTransformerFactory().createTransformer();
            }
        }

        return null;
    }

    private BufferedImage loadUnchangedImage(final String imageRelativePath, final String imageIdOrNull)
    {
        try
        {
            IHierarchicalContentNode node;
            if (contentOrNull == null)
            {
                node = new FileBasedContentNode(new File(imagesParentDirectory, imageRelativePath));
            } else
            {
                node = contentOrNull.getNode(imageRelativePath);
            }
            return imageCache.getImage(node, imageIdOrNull, imageLibraryOrNull);
        } catch (Exception ex)
        {
            throw new UserFailureException("Failed to load image " + imageRelativePath, ex);
        }
    }

    private Size loadUnchangedImageDimension(String imageRelativePath, String imageIdOrNull)
    {
        if (contentOrNull == null)
        {
            return Utils.loadUnchangedImageSize(new FileBasedContentNode(new File(
                    imagesParentDirectory, imageRelativePath)), imageIdOrNull, imageLibraryOrNull);
        } else
        {
            return Utils.loadUnchangedImageSize(contentOrNull.getNode(imageRelativePath),
                    imageIdOrNull, imageLibraryOrNull);
        }
    }

    private int loadUnchangedImageColorDepth(String imageRelativePath, String imageIdOrNull)
    {
        if (contentOrNull == null)
        {
            return Utils.loadUnchangedImageColorDepth(new FileBasedContentNode(new File(
                    imagesParentDirectory, imageRelativePath)), imageIdOrNull, imageLibraryOrNull);
        } else
        {
            return Utils.loadUnchangedImageColorDepth(contentOrNull.getNode(imageRelativePath),
                    imageIdOrNull, imageLibraryOrNull);
        }
    }

    private Status createStatus(String thumbnailPath, IOException ex)
    {
        logger.warn("Retriable error when creating thumbnail '" + thumbnailPath + "'", ex);
        return Status.createRetriableError(String.format("Could not generate a thumbnail '%s': %s",
                thumbnailPath, ex.getMessage()));
    }

    private ITaskExecutor<ImageFileInfo> createThumbnailGenerator(final IHDF5ContainerWriter writer, final String thumbnailGenertionSessionId)
    {
        return new ITaskExecutor<ImageFileInfo>()
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

                @Override
                public Status execute(ImageFileInfo image)
                {
                    // For the purpose of reusing image readers for subsequent files if the same worker thread
                    // is processing them - we set thread local variable on image readers indicating that
                    // invokations with the same process ID can share readers
                    ImageUtil.setThreadLocalSessionId(thumbnailGenertionSessionId);
                    try
                    {
                        // each thread will get its own buffer to avoid allocating memory for the
                        // internal array each time
                        ByteArrayOutputStream outputStreamBuffer = outputStreamBuffers.get();
                        outputStreamBuffer.reset();
                        return generateThumbnail(writer, image, outputStreamBuffer);
                    } finally
                    {
                        ImageUtil.setThreadLocalSessionId(null);
                    }
                }
            };
    }

    @Override
    public void runWithSimpleWriter(IHDF5ContainerWriter writer)
    {
        final String thumbnailsName = " (" + thumbnailsStorageFormat.getThumbnailsFileName() + ")";

        String sessionId = ImageUtil.getThreadLocalSessionId();

        List<ImageFileInfo> images = imageDataSetStructure.getImages();
        Collection<FailureRecord<ImageFileInfo>> errors =
                ParallelizedExecutor.process(images,
                        createThumbnailGenerator(writer, sessionId),
                        thumbnailsStorageFormat.getAllowedMachineLoadDuringGeneration(), 100,
                        "Thumbnails generation" + thumbnailsName, MAX_RETRY_OF_FAILED_GENERATION,
                        true);

        if (errors.size() > 0)
        {
            throw new IllegalStateException(String.format(
                    "There were errors when generating %d thumbnails" + thumbnailsName
                            + ", the whole thumbnails generation process fails.", errors.size()));
        }
    }
}

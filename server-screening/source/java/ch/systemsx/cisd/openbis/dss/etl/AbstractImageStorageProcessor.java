/*
 * Copyright 2007 ETH Zuerich, CISD
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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.sql.DataSource;

import net.lemnik.eodsql.QueryTool;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.image.IImageTransformerFactory;
import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.common.filesystem.SoftLinkMaker;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.reflection.ClassUtils;
import ch.systemsx.cisd.etlserver.AbstractStorageProcessor;
import ch.systemsx.cisd.etlserver.AbstractStorageProcessorTransaction;
import ch.systemsx.cisd.etlserver.DispatcherStorageProcessor.IDispatchableStorageProcessor;
import ch.systemsx.cisd.etlserver.IDataSetInfoExtractor;
import ch.systemsx.cisd.etlserver.ITypeExtractor;
import ch.systemsx.cisd.etlserver.utils.Unzipper;
import ch.systemsx.cisd.hcs.Geometry;
import ch.systemsx.cisd.openbis.common.hdf5.HDF5Container;
import ch.systemsx.cisd.openbis.common.hdf5.HierarchicalStructureDuplicatorFileToHDF5;
import ch.systemsx.cisd.openbis.dss.Constants;
import ch.systemsx.cisd.openbis.dss.etl.PlateStorageProcessor.DatasetOwnerInformation;
import ch.systemsx.cisd.openbis.dss.etl.PlateStorageProcessor.ImageDatasetOwnerInformation;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.IImagingQueryDAO;
import ch.systemsx.cisd.openbis.dss.etl.dto.ImageSeriesPoint;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.Channel;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColorComponent;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.ImageFileInfo;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.ImageStorageConfiguraton;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.OriginalDataStorageFormat;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.ImageDataSetInformation;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.ImageDataSetStructure;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.ThumbnailsInfo;
import ch.systemsx.cisd.openbis.dss.etl.jython.v1.JythonPlateDataSetHandler;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ChannelDescription;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ColorComponent;

/**
 * Abstract superclass for storage processor which stores images in a special-purpose imaging database besides putting it into the store. It has
 * ability to compress the whole dataset as an HDF5 container. It can also generate thumbnails for each image.
 * <p>
 * Accepts following properties:
 * <ul>
 * <li>generate-thumbnails - should the thumbnails be generated? It slows down the dataset registration, but increases the performance when the user
 * wants to see the image. Can be 'true' or 'false', 'false' is the default value
 * <li>compress-thumbnails - should the thumbnails be compressed? Used if generate-thumbnails is true, otherwise ignored
 * <li>thumbnail-max-width, thumbnail-max-height - thumbnails size in pixels
 * <li>[deprecated] channel-names - names of the channels in which images have been acquired
 * <li>channel-codes - codes of the channels in which images have been acquired
 * <li>channel-labels - labels of the channels in which images have been acquired
 * <li>well_geometry - format: [width]>x[height], e.g. 3x4. Specifies the grid into which a microscope divided the well to acquire images.
 * <li>file-extractor - implementation of the {@link IImageFileExtractor} interface which maps images to the location on the plate and particular
 * channel
 * <li>data-source - specification of the imaging db
 * <li>extract-single-image-channels - optional comma separated list of color components. Available values: RED, GREEN or BLUE. If specified then the
 * channels are extracted from the color components and override 'file-extractor' results.
 * <li>move-unregistered-datasets-to-error-dir - Optional property, true by default. If set to false then the dataset whcih cannot be registered will
 * be left in the incoming folder and will be mentioned in the .faulty_paths file.
 * </p>
 * <p>
 * Subclasses of this storage processor can be used in the context of {@link IDispatchableStorageProcessor} only if the given
 * {@link DataSetInformation} can be casted to {@link ImageDataSetInformation}. This requires using special {@link IDataSetInfoExtractor} extension or
 * {@link JythonPlateDataSetHandler}.
 * </p>
 * 
 * @author Tomasz Pylak
 */
abstract class AbstractImageStorageProcessor extends AbstractStorageProcessor implements
        IDispatchableStorageProcessor
{
    public static final String ARCHIVE_DELIMITER = "/";

    /**
     * Stores the references to the extracted images in the imaging database.
     * 
     * @param dao should not be commited or rollbacked, it's done outside of this method.
     */
    abstract protected void storeInDatabase(IImagingQueryDAO dao,
            ImageDatasetOwnerInformation dataSetInformation,
            ImageFileExtractionResult extractedImages, boolean thumbnailsOnly);

    /**
     * Additional image validation (e.g. are there all images that were expected?). Prints warnings to the log, does not throw exceptions.
     * 
     * @return true if the images are 'complete'.
     */
    abstract protected boolean validateImages(DatasetOwnerInformation dataSetInformation,
            IMailClient mailClient, File incomingDataSetDirectory,
            ImageFileExtractionResult extractionResult);

    // --------------------------------------------

    protected static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            PlateStorageProcessor.class);

    protected static final Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY,
            PlateStorageProcessor.class);

    protected static final String FILE_EXTRACTOR_PROPERTY = "file-extractor";

    // --- storage configuration properties

    private final static String ORIGINAL_DATA_STORAGE_FORMAT_PROPERTY =
            "original-data-storage-format";

    // ---

    private final DataSource dataSource;

    /**
     * Default configuration for all datasets, can be changed by {@link ImageDataSetInformation}.
     */
    private final ImageStorageConfiguraton globalImageStorageConfiguraton;

    // --- protected --------

    protected final IImageFileExtractor imageFileExtractorOrNull;

    // ---

    public AbstractImageStorageProcessor(final Properties properties)
    {
        this(tryCreateImageExtractor(properties), properties);
    }

    protected AbstractImageStorageProcessor(IImageFileExtractor imageFileExtractorOrNull,
            Properties properties)
    {
        super(properties);
        this.imageFileExtractorOrNull = imageFileExtractorOrNull;
        this.globalImageStorageConfiguraton = getGlobalImageStorageConfiguraton(properties);

        this.dataSource = ServiceProvider.getDataSourceProvider().getDataSource(properties);
    }

    // --- ImageStorageConfiguraton ---

    private static ImageStorageConfiguraton getGlobalImageStorageConfiguraton(Properties properties)
    {
        ImageStorageConfiguraton storageFormatParameters = new ImageStorageConfiguraton();
        storageFormatParameters
                .setOriginalDataStorageFormat(getOriginalDataStorageFormat(properties));
        return storageFormatParameters;
    }

    private static OriginalDataStorageFormat getOriginalDataStorageFormat(
            final Properties properties)
    {
        String defaultValue = OriginalDataStorageFormat.UNCHANGED.name();
        String textValue =
                PropertyUtils.getProperty(properties, ORIGINAL_DATA_STORAGE_FORMAT_PROPERTY,
                        defaultValue);
        return OriginalDataStorageFormat.valueOf(textValue.toUpperCase());
    }

    // ----

    private static IImageFileExtractor tryCreateImageExtractor(final Properties properties)
    {
        String fileExtractorClass = PropertyUtils.getProperty(properties, FILE_EXTRACTOR_PROPERTY);
        if (fileExtractorClass != null)
        {
            return ClassUtils.create(IImageFileExtractor.class, fileExtractorClass, properties);
        } else
        {
            return null;
        }
    }

    private IImagingQueryDAO createQuery()
    {
        return QueryTool.getQuery(dataSource, IImagingQueryDAO.class);
    }

    // ---------------------------------
    protected static class AbstractImageStorageProcessorTransaction extends
            AbstractStorageProcessorTransaction
    {

        private static final long serialVersionUID = 1L;

        private final UnstoreDataAction unstoreAction;

        private final transient AbstractImageStorageProcessor processor;

        private transient IImagingQueryDAO dbTransaction;

        // used when HDF5 is used to store original data
        private transient boolean shouldDeleteOriginalDataOnCommit;

        private transient List<File> generatedFiles;

        public AbstractImageStorageProcessorTransaction(
                StorageProcessorTransactionParameters parameters,
                AbstractImageStorageProcessor processor)
        {
            super(parameters);
            this.processor = processor;
            this.unstoreAction = processor.getDefaultUnstoreDataAction(null);
            this.generatedFiles = new ArrayList<File>();
        }

        @Override
        public final File executeStoreData(final ITypeExtractor typeExtractor,
                final IMailClient mailClient)
        {
            assert rootDirectory != null : "Root directory can not be null.";
            assert incomingDataSetDirectory != null : "Incoming data set directory can not be null.";
            assert typeExtractor != null : "Unspecified IProcedureAndDataTypeExtractor implementation.";

            File unzipedFolder = tryUnzipToFolder(incomingDataSetDirectory);
            if (unzipedFolder != null)
            {
                this.incomingDataSetDirectory = unzipedFolder;
                return getStoredDataDirectory();
            }
            if (isImageDataset() == false)
            {
                plainMoveToStore();
                return rootDirectory;
            }
            dbTransaction = processor.createQuery();

            handleImagesAndMoveToStoreIfPrimaryDataSet(dataSetInformation, true, mailClient);
            if (dataSetInformation instanceof ImageDataSetInformation)
            {
                List<DataSetInformation> secondaryDataSets = ((ImageDataSetInformation) dataSetInformation).getSecondaryDataSets();
                for (DataSetInformation secondaryDataSet : secondaryDataSets)
                {
                    handleImagesAndMoveToStoreIfPrimaryDataSet(secondaryDataSet, false, mailClient);
                }
            }

            return rootDirectory;
        }

        private void handleImagesAndMoveToStoreIfPrimaryDataSet(DataSetInformation dataSetInfo,
                boolean primaryDataSet, final IMailClient mailClient)
        {
            ImageFileExtractionWithConfig extractionResultWithConfig =
                    processor.extractImages(dataSetInfo, incomingDataSetDirectory);
            ImageFileExtractionResult extractionResult =
                    extractionResultWithConfig.getExtractionResult();

            validateImages(mailClient, dataSetInfo, extractionResultWithConfig, extractionResult);

            List<AcquiredSingleImage> plateImages = extractionResult.getImages();
            ImageStorageConfiguraton imageStorageConfiguraton =
                    extractionResultWithConfig.getImageStorageConfiguraton();

            File datasetRelativeImagesFolderPath =
                    extractionResultWithConfig.getExtractionResult().getDatasetRelativeImagesFolderPath();

            boolean isOverviewImageDataSet = getRegisterAsOverviewImageDataSet(dataSetInfo);
            if (primaryDataSet)
            {
                plainMoveToStore();
                shouldDeleteOriginalDataOnCommit =
                        imageStorageConfiguraton.getOriginalDataStorageFormat().isHdf5()
                                && false == isOverviewImageDataSet;
            }
            if (false == isOverviewImageDataSet)
            {
                processImages(plateImages, datasetRelativeImagesFolderPath,
                        imageStorageConfiguraton);
            }

            processor.storeInDatabase(dbTransaction,
                    extractionResultWithConfig.getImageDatasetOwner(), extractionResult,
                    isOverviewImageDataSet);
        }

        private boolean getRegisterAsOverviewImageDataSet(DataSetInformation dataSetInfo)
        {
            if (dataSetInfo instanceof ImageDataSetInformation == false)
            {
                return false;
            }
            return ((ImageDataSetInformation) dataSetInfo).getRegisterAsOverviewImageDataSet();
        }

        private void processImages(List<AcquiredSingleImage> images,
                File datasetRelativeImagesFolderPath,
                ImageStorageConfiguraton imageStorageConfiguraton)
        {
            String relativeImagesDirectory;
            OriginalDataStorageFormat originalDataStorageFormat =
                    imageStorageConfiguraton.getOriginalDataStorageFormat();
            if (originalDataStorageFormat.isHdf5())
            {
                File hdf5OriginalContainer = getHdf5OriginalContainer(rootDirectory);
                relativeImagesDirectory =
                        compressToHdf5(rootDirectory, datasetRelativeImagesFolderPath,
                                originalDataStorageFormat, hdf5OriginalContainer);
                this.generatedFiles.add(hdf5OriginalContainer);
            } else
            {
                relativeImagesDirectory = datasetRelativeImagesFolderPath.getPath() + "/";
            }
            // add a prefix before each image path to create a path relative to the dataset folder
            updateImagesRelativePath(relativeImagesDirectory, images);
        }

        private void validateImages(final IMailClient mailClient, DataSetInformation dataSetInfo,
                ImageFileExtractionWithConfig extractionResultWithConfig,
                ImageFileExtractionResult extractionResult)
        {
            boolean isComplete =
                    processor.validateImages(extractionResultWithConfig.getImageDatasetOwner(),
                            mailClient, incomingDataSetDirectory, extractionResult);
            dataSetInfo.setComplete(isComplete);
        }

        private boolean isImageDataset()
        {
            return (processor.imageFileExtractorOrNull != null)
                    || dataSetInformation instanceof ImageDataSetInformation;
        }

        // moves the incoming folder to the store
        private File plainMoveToStore()
        {
            File destDir = rootDirectory;
            if (processor.imageFileExtractorOrNull != null)
            {
                // Wrap the data in the original foder.
                // It is a backward compatible mode for non-jython dropboxes, where the transaction
                // code moves to original dir.
                destDir = getOriginalFolder(destDir);
                destDir.mkdirs();
            }
            File newLocationDir =
                    AbstractImageStorageProcessor.moveFileToDirectory(incomingDataSetDirectory,
                            destDir);
            this.storedDataDirectory = rootDirectory;
            return newLocationDir;
        }

        @Override
        protected void executeCommit()
        {
            if (shouldDeleteOriginalDataOnCommit)
            {
                commitHdf5StorageFormatChanges(storedDataDirectory);
            }

            // commit the database transaction
            if (dbTransaction != null)
            {
                dbTransaction.close(true);
            }
        }

        @Override
        protected UnstoreDataAction executeRollback(Throwable exception)
        {
            moveFilesBackFromStore();
            if (dbTransaction != null)
            {
                rollbackDatabaseChanges();
            }
            return unstoreAction;
        }

        private void rollbackDatabaseChanges()
        {
            try
            {
                dbTransaction.rollback();
            } finally
            {
                dbTransaction.close();
            }
        }

        @Override
        public final File tryGetProprietaryData()
        {
            return tryGetSingleChild(storedDataDirectory);
        }

        private static final File tryGetSingleChild(File parentDirectory)
        {
            assert parentDirectory != null : "Unspecified parentDirectory";

            File[] content = parentDirectory.listFiles();
            if (content == null || content.length == 0)
            {
                return null;
            }
            if (content.length > 1)
            {
                operationLog.error("There should be exactly one folder inside '" + parentDirectory
                        + "', but " + parentDirectory.length() + " has been found.");
                return null;
            }
            File childFile = content[0];
            if (childFile.exists() == false)
            {
                operationLog.error("The child file '" + childFile.getAbsolutePath()
                        + "' does not exist.");
                return null;
            }
            return childFile;
        }

        private final void moveFilesBackFromStore()
        {
            if (storedDataDirectory == null)
            {
                storedDataDirectory = rootDirectory;
            }
            checkParameters(incomingDataSetDirectory, storedDataDirectory);

            final IFileOperations fileOps = FileOperations.getMonitoredInstanceForCurrentThread();
            if (generatedFiles != null)
            {
                for (File generatedFile : this.generatedFiles)
                {
                    deleteRecursively(fileOps, generatedFile);
                }
            }
            final File originalDataFile = tryGetSingleChild(storedDataDirectory);
            if (originalDataFile == null)
            {
                // nothing has been stored in the file system yet,
                // e.g. because images could not be validated
                return;
            }
            // Move the data from the 'original' directory back to the 'incoming' directory.
            final File incomingDirectory = incomingDataSetDirectory.getParentFile();
            try
            {
                moveFileToDirectory(originalDataFile, incomingDirectory);
                if (operationLog.isInfoEnabled())
                {
                    operationLog
                            .info(String
                                    .format("Storage operation rollback: directory '%s' has moved to incoming directory '%s'.",
                                            originalDataFile, incomingDirectory.getAbsolutePath()));
                }
            } catch (final EnvironmentFailureException ex)
            {
                notificationLog.error(String.format("Could not move '%s' to the directory '%s'.",
                        originalDataFile, incomingDirectory.getAbsolutePath()), ex);
                return;
            }
            // Remove the dataset directory from the store
            if (fileOps.exists(incomingDataSetDirectory))
            {
                deleteRecursively(fileOps, storedDataDirectory);
            } else
            {
                notificationLog.error(String.format("Incoming data set directory '%s' does not "
                        + "exist, keeping store directory '%s'.", incomingDataSetDirectory,
                        storedDataDirectory));
            }
        }

        private static void deleteRecursively(final IFileOperations fileOps, File file)
        {
            if (fileOps.removeRecursivelyQueueing(file) == false)
            {
                operationLog.error("Cannot delete '" + file.getAbsolutePath() + "'.");
            }
        }

        private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException
        {
            ois.defaultReadObject();
            // pretend that a move operation has succeeded before serialization
            // the rollback logic will handle the non-null "storedDataDirectory" appropriately
            this.storedDataDirectory = this.rootDirectory;
        }

    }

    @Override
    public final IStorageProcessorTransaction createTransaction(
            StorageProcessorTransactionParameters parameters)
    {
        return new AbstractImageStorageProcessorTransaction(parameters, this);
    }

    private final class ImageFileExtractionWithConfig
    {
        private final ImageDatasetOwnerInformation imageDatasetOwner;

        private final ImageFileExtractionResult extractionResult;

        private final ImageStorageConfiguraton imageStorageConfiguraton;

        public ImageFileExtractionWithConfig(ImageDatasetOwnerInformation imageDatasetOwner,
                ImageFileExtractionResult extractionResult,
                ImageStorageConfiguraton imageStorageConfiguraton)
        {
            assert imageDatasetOwner != null : "imageDatasetOwner is null";
            assert extractionResult != null : "extractionResult is null";
            assert imageStorageConfiguraton != null : "imageStorageConfiguraton is null";

            this.imageDatasetOwner = imageDatasetOwner;
            this.extractionResult = extractionResult;
            this.imageStorageConfiguraton = imageStorageConfiguraton;
        }

        public ImageDatasetOwnerInformation getImageDatasetOwner()
        {
            return imageDatasetOwner;
        }

        public ImageFileExtractionResult getExtractionResult()
        {
            return extractionResult;
        }

        public ImageStorageConfiguraton getImageStorageConfiguraton()
        {
            return imageStorageConfiguraton;
        }
    }

    static File tryUnzipToFolder(File incomingDataSetDirectory)
    {
        if (isZipFile(incomingDataSetDirectory) == false)
        {
            return null;
        }
        String outputDirName = FilenameUtils.getBaseName(incomingDataSetDirectory.getName());
        File output = new File(incomingDataSetDirectory.getParentFile(), outputDirName);
        Status status = Unzipper.unzip(incomingDataSetDirectory, output, true);
        if (status.isError())
        {
            throw EnvironmentFailureException.fromTemplate("Cannot unzip '%s': %s",
                    incomingDataSetDirectory.getName(), status);
        }
        return output;
    }

    private static String compressToHdf5(final File rootDirectory, File imagesInStoreFolder,
            OriginalDataStorageFormat originalDataStorageFormat, File hdf5OriginalContainer)
    {
        File absolutePath = new File(rootDirectory, imagesInStoreFolder.getPath());
        if (hdf5OriginalContainer.isDirectory())
        {
            throw ConfigurationFailureException
                    .fromTemplate(
                            "Cannot compress the dataset to HDF5 format during registration, "
                                    + "probably the compression has been configured to be done in a separate step"
                                    + " ('%s' is an existing directory). Check and switch on only one of them!",
                            hdf5OriginalContainer);
        }
        boolean isDataCompressed =
                originalDataStorageFormat == OriginalDataStorageFormat.HDF5_COMPRESSED;
        String pathInHdf5Container = "/" + absolutePath.getName() + "/";
        saveInHdf5(absolutePath, pathInHdf5Container, hdf5OriginalContainer, isDataCompressed);
        String hdf5ArchivePathPrefix = hdf5OriginalContainer.getName() + ARCHIVE_DELIMITER;
        return hdf5ArchivePathPrefix + pathInHdf5Container;
    }

    private static File getHdf5OriginalContainer(final File rootDirectory)
    {
        return new File(rootDirectory, Constants.HDF5_CONTAINER_ORIGINAL_FILE_NAME);
    }

    private static void saveInHdf5(File sourceFolder, String pathInHdf5Container,
            File hdf5DestinationFile, boolean compressFiles)
    {
        HDF5Container container = new HDF5Container(hdf5DestinationFile);
        container.runWriterClient(compressFiles,
                new HierarchicalStructureDuplicatorFileToHDF5.DuplicatorWriterClient(sourceFolder,
                        pathInHdf5Container));
    }

    private static void updateImagesRelativePath(String pathPrefixToAdd,
            final List<AcquiredSingleImage> plateImages)
    {
        for (AcquiredSingleImage plateImage : plateImages)
        {
            RelativeImageReference imageReference = plateImage.getImageReference();
            imageReference.setRelativeImageFolder(pathPrefixToAdd);
        }
    }

    /**
     * @return true if the dataset has been enriched before and already contains all the information about images.
     */
    @Override
    public boolean accepts(DataSetInformation dataSetInformation, File incomingDataSet)
    {
        String dataSetTypeCode = dataSetInformation.getDataSetType().getCode().toUpperCase();
        return dataSetInformation instanceof ImageDataSetInformation
                || dataSetTypeCode.matches(ScreeningConstants.ANY_HCS_IMAGE_DATASET_TYPE_PATTERN)
                || dataSetTypeCode
                        .matches(ScreeningConstants.ANY_MICROSCOPY_IMAGE_DATASET_TYPE_PATTERN)
                || dataSetTypeCode.matches(ScreeningConstants.ANALYSIS_FEATURE_LIST);
    }

    private ImageFileExtractionWithConfig extractImages(
            final DataSetInformation dataSetInformation, final File incomingDataSetDirectory)
    {
        if (imageFileExtractorOrNull == null)
        {
            return extractImagesFromDatasetInfoOrDie(dataSetInformation);
        } else
        {
            return deprecatedExtractImages(dataSetInformation, incomingDataSetDirectory,
                    imageFileExtractorOrNull);
        }
    }

    // handle deprecated non-jython way of importing images
    private ImageFileExtractionWithConfig deprecatedExtractImages(
            final DataSetInformation dataSetInformation, final File incomingDataSetDirectory,
            IImageFileExtractor extractor)
    {
        ImageFileExtractionResult result =
                extractor.extract(incomingDataSetDirectory, dataSetInformation);
        if (result.getImages().size() == 0)
        {
            throw new UserFailureException("No images found in the incoming diretcory: "
                    + incomingDataSetDirectory);
        }
        // no container dataset will be created in this case, having thumbnails will also not be
        // allowed
        ImageDatasetOwnerInformation imageDatasetOwner =
                ImageDatasetOwnerInformation.create(dataSetInformation.getDataSetCode(),
                        dataSetInformation, null);
        return new ImageFileExtractionWithConfig(imageDatasetOwner, result,
                globalImageStorageConfiguraton);
    }

    private ImageFileExtractionWithConfig extractImagesFromDatasetInfoOrDie(
            final DataSetInformation dataSetInformation)
    {
        if (dataSetInformation instanceof ImageDataSetInformation == false)
        {
            throw ConfigurationFailureException
                    .fromTemplate(
                            "File extractor '%s' has not been configured or jython script in 'top-level-data-set-handler' is not '%s'.",
                            FILE_EXTRACTOR_PROPERTY,
                            JythonPlateDataSetHandler.class.getCanonicalName());
        } else
        {
            return extractImagesFromDatasetInfo((ImageDataSetInformation) dataSetInformation);
        }
    }

    private ImageFileExtractionWithConfig extractImagesFromDatasetInfo(
            ImageDataSetInformation dataSetInformation)
    {
        ImageDataSetStructure imageDataSetStructure = dataSetInformation.getImageDataSetStructure();
        if (imageDataSetStructure.isValid() == false)
        {
            throw new ConfigurationFailureException(
                    "Invalid image dataset info object, check if your jython script fills all the required fields. "
                            + "Or maybe the recognized files extensions is set incorrectly? Dataset: "
                            + imageDataSetStructure);
        }
        Geometry tileGeometry =
                new Geometry(imageDataSetStructure.getTileRowsNumber(),
                        imageDataSetStructure.getTileColumnsNumber());

        ThumbnailsInfo thumbnailsInfo = dataSetInformation.getThumbnailsInfos();

        List<AcquiredSingleImage> images = convertImages(imageDataSetStructure, thumbnailsInfo);

        List<File> invalidFiles = new ArrayList<File>(); // handles in an earlier phase
        ImageStorageConfiguraton imageStorageConfiguraton =
                imageDataSetStructure.getImageStorageConfiguraton();
        if (imageStorageConfiguraton == null)
        {
            imageStorageConfiguraton = globalImageStorageConfiguraton;
        }

        setPerImageTransformationIfNeeded(images, imageStorageConfiguraton);

        ImageFileExtractionResult extractionResult =
                new ImageFileExtractionResult(images,
                        dataSetInformation.getDatasetRelativeImagesFolderPath(), invalidFiles,
                        imageDataSetStructure.getChannels(), tileGeometry,
                        imageStorageConfiguraton.getStoreChannelsOnExperimentLevel(),
                        imageStorageConfiguraton.tryGetImageLibrary());

        ImageDatasetOwnerInformation imageDatasetOwner =
                ImageDatasetOwnerInformation.create(
                        dataSetInformation.tryGetContainerDatasetPermId(), dataSetInformation,
                        thumbnailsInfo);
        return new ImageFileExtractionWithConfig(imageDatasetOwner, extractionResult,
                imageStorageConfiguraton);
    }

    private static List<AcquiredSingleImage> convertImages(
            ImageDataSetStructure imageDataSetStructure, ThumbnailsInfo thumbnailFilePathsOrNull)
    {
        List<ImageFileInfo> imageInfos = imageDataSetStructure.getImages();
        List<ChannelColorComponent> channelColorComponentsOrNull =
                imageDataSetStructure.getChannelColorComponents();
        List<Channel> channels = imageDataSetStructure.getChannels();

        List<AcquiredSingleImage> images = new ArrayList<AcquiredSingleImage>();
        for (ImageFileInfo imageInfo : imageInfos)
        {
            if (channelColorComponentsOrNull != null)
            {
                for (int i = 0; i < channelColorComponentsOrNull.size(); i++)
                {
                    ColorComponent colorComponent =
                            asColorComponent(channelColorComponentsOrNull.get(i));
                    Channel channel = channels.get(i);
                    AcquiredSingleImage image =
                            AbstractImageFileExtractor.createImage(imageInfo, channel.getCode(),
                                    colorComponent, thumbnailFilePathsOrNull);
                    images.add(image);
                }
            } else
            {
                images.addAll(AbstractImageFileExtractor.createImagesWithNoColorComponent(
                        imageInfo, thumbnailFilePathsOrNull));
            }
        }
        return images;
    }

    private static ColorComponent asColorComponent(ChannelColorComponent channelColorComponent)
    {
        return ColorComponent.valueOf(channelColorComponent.name());
    }

    private void setPerImageTransformationIfNeeded(List<AcquiredSingleImage> images,
            ImageStorageConfiguraton imageStorageConfiguraton)
    {
        if (imageStorageConfiguraton != null
                && imageStorageConfiguraton.getImageTransformerFactory() != null)
        {
            IImageTransformerFactory imgTransformerFactory =
                    imageStorageConfiguraton.getImageTransformerFactory();
            for (AcquiredSingleImage image : images)
            {
                image.setImageTransformerFactory(imgTransformerFactory);
            }
        }
    }

    private static void commitHdf5StorageFormatChanges(File storedDataDirectory)
    {
        File originalFolder = getOriginalFolder(storedDataDirectory);
        File hdf5OriginalContainer = getHdf5OriginalContainer(storedDataDirectory);
        if (hdf5OriginalContainer.exists()) // this should be always true
        {
            final IFileOperations fileOps = FileOperations.getMonitoredInstanceForCurrentThread();
            if (fileOps.removeRecursivelyQueueing(originalFolder) == false)
            {
                operationLog.error("Cannot delete original data '"
                        + originalFolder.getAbsolutePath() + "'.");
            }
        } else
        {
            notificationLog.error(String.format(
                    "HDF5 container with original data '%s' could not be found, this should not happen! "
                            + "Dataset should be registered again! "
                            + "Keeping the original directory '%s'.", hdf5OriginalContainer,
                    originalFolder));
        }
    }

    private static File getOriginalFolder(File storedDataDirectory)
    {
        return new File(storedDataDirectory, ScreeningConstants.ORIGINAL_DATA_DIR);
    }

    /**
     * Moves source file/folder to the destination directory. If the source is a symbolic links to the original data then we do not move any data.
     * Instead we create symbolic link to original data which points to the same place as the source link.
     * 
     * @return
     */
    private static File moveFileToDirectory(final File source, final File directory)
            throws EnvironmentFailureException
    {
        assert source != null;
        IFileOperations fileOperations = FileOperations.getMonitoredInstanceForCurrentThread();
        assert directory != null && fileOperations.isDirectory(directory);
        final String newName = source.getName();
        final File destination = new File(directory, newName);
        if (fileOperations.exists(destination) == false)
        {
            if (FileUtilities.isSymbolicLink(source))
            {
                moveSymbolicLink(source, destination);
            } else
            {
                final boolean successful = fileOperations.rename(source, destination);
                if (successful == false)
                {
                    throw EnvironmentFailureException.fromTemplate(
                            "Can not move file '%s' to directory '%s'.", source.getAbsolutePath(),
                            directory.getAbsolutePath());
                }
            }
            return destination;
        } else
        {
            throw EnvironmentFailureException
                    .fromTemplate(
                            "Can not move file '%s' to directory '%s' because the destination directory already exists.",
                            source.getAbsolutePath(), directory.getAbsolutePath());
        }
    }

    // WORKAROUND there were cases where it was impossible to move an absolute symbolic link
    // It happened on a CIFS share. So instead of moving the link we create a file which points to
    // the same place and delete the link.
    private static void moveSymbolicLink(File source, File destination)
    {
        File referencedSource;
        try
        {
            referencedSource = source.getCanonicalFile();
        } catch (IOException ex)
        {
            throw new EnvironmentFailureException("cannot get the canonical path of " + source);
        }
        boolean ok = SoftLinkMaker.createSymbolicLink(referencedSource, destination);
        if (ok == false)
        {
            throw EnvironmentFailureException.fromTemplate(
                    "Can not create symbolic link to '%s' in '%s'.", referencedSource.getPath(),
                    destination.getPath());
        }
        ok = source.delete();
        if (ok == false)
        {
            throw EnvironmentFailureException.fromTemplate("Can not delete symbolic link '%s'.",
                    source.getPath());
        }
    }

    protected static List<String> extractChannelCodes(final List<ChannelDescription> descriptions)
    {
        List<String> channelCodes = new ArrayList<String>();
        for (ChannelDescription cd : descriptions)
        {
            channelCodes.add(cd.getCode());
        }
        return channelCodes;
    }

    protected static List<String> extractChannelLabels(final List<ChannelDescription> descriptions)
    {
        List<String> channelLabels = new ArrayList<String>();
        for (ChannelDescription cd : descriptions)
        {
            channelLabels.add(cd.getLabel());
        }
        return channelLabels;
    }

    protected static String getChannelCodeOrLabel(final List<String> channelCodes, int channelId)
    {
        if (channelId > channelCodes.size())
        {
            throw UserFailureException.fromTemplate(
                    "Too large channel number %d, configured channels: %s.", channelId,
                    CollectionUtils.abbreviate(channelCodes, -1));
        }
        return channelCodes.get(channelId - 1);
    }

    protected static boolean hasImageSeries(List<AcquiredSingleImage> images)
    {
        Set<ImageSeriesPoint> points = new HashSet<ImageSeriesPoint>();
        for (AcquiredSingleImage image : images)
        {
            if (image.tryGetTimePoint() != null || image.tryGetDepth() != null
                    || image.tryGetSeriesNumber() != null)
            {
                points.add(new ImageSeriesPoint(image.tryGetTimePoint(), image.tryGetDepth(), image
                        .tryGetSeriesNumber()));
            }
        }
        return points.size() > 1;
    }

}

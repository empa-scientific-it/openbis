/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.etl.jython.v1;

import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants.MICROSCOPY_CONTAINER_TYPE_SUBSTRING;
import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants.MICROSCOPY_IMAGE_TYPE_SUBSTRING;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationDetails;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSet;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSetUpdatable;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSet;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSetRegistrationTransaction;
import ch.systemsx.cisd.etlserver.registrator.v1.DataSetRegistrationService;
import ch.systemsx.cisd.etlserver.registrator.v1.IDataSetRegistrationDetailsFactory;
import ch.systemsx.cisd.openbis.common.io.FileBasedContentNode;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.Constants;
import ch.systemsx.cisd.openbis.dss.etl.Hdf5ThumbnailGenerator;
import ch.systemsx.cisd.openbis.dss.etl.ImageCache;
import ch.systemsx.cisd.openbis.dss.etl.Utils;
import ch.systemsx.cisd.openbis.dss.etl.dto.ImageLibraryInfo;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.ImageFileInfo;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.SimpleImageDataConfig;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.ThumbnailsStorageFormat;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.FeatureDefinition;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.FeatureVectorDataSetInformation;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.FeaturesBuilder;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.ImageDataSetInformation;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.ImageDataSetStructure;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.ThumbnailsInfo;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.FeatureListDataConfig;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.IImageDataSet;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.IImagingDataSetRegistrationTransaction;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.SimpleFeatureVectorDataConfig;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.impl.FeatureVectorContainerDataSet;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.impl.FeatureVectorDataSet;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.impl.ImageContainerDataSet;
import ch.systemsx.cisd.openbis.dss.etl.featurevector.CsvFeatureVectorParser;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IDataSetImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.Size;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetKind;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchSubCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * Imaging-specific transaction. Handles image datasets in a special way, other datasets are registered using a standard procedure.
 * <p>
 * Note that this transaction is not parametrized by a concrete {@link DataSetInformation} subclass. It has to deal with
 * {@link ImageDataSetInformation}, {@link FeatureVectorDataSetInformation} and {@link DataSetInformation} at the same time.
 */
@SuppressWarnings("rawtypes")
public class ImagingDataSetRegistrationTransaction extends DataSetRegistrationTransaction implements
        IImagingDataSetRegistrationTransaction
{
    private final IDataSetRegistrationDetailsFactory<ImageDataSetInformation> imageDatasetFactory;

    private final IDataSetRegistrationDetailsFactory<DataSetInformation> imageContainerDatasetFactory;

    private final String originalDirName;

    private final JythonPlateDatasetFactory factory;

    @SuppressWarnings("unchecked")
    public ImagingDataSetRegistrationTransaction(File rollBackStackParentFolder,
            File workingDirectory, File stagingDirectory,
            DataSetRegistrationService<DataSetInformation> registrationService,
            IDataSetRegistrationDetailsFactory<DataSetInformation> registrationDetailsFactory,
            String originalDirName)
    {
        super(rollBackStackParentFolder, workingDirectory, stagingDirectory, registrationService,
                registrationDetailsFactory);

        assert registrationDetailsFactory instanceof JythonPlateDatasetFactory : "JythonPlateDatasetFactory expected, but got: "
                + registrationDetailsFactory.getClass().getCanonicalName();

        factory = (JythonPlateDatasetFactory) registrationDetailsFactory;
        this.imageDatasetFactory = factory.imageDatasetFactory;
        this.imageContainerDatasetFactory = factory.imageContainerDatasetFactory;
        this.originalDirName = originalDirName;
    }

    public JythonPlateDatasetFactory getFactory()
    {
        return factory;
    }

    @Override
    public IImageDataSet createNewImageDataSet(SimpleImageDataConfig imageDataSet,
            File incomingFolderWithImages)
    {
        DataSetRegistrationDetails<ImageDataSetInformation> details =
                SimpleImageDataSetRegistrator.createImageDatasetDetails(imageDataSet,
                        incomingFolderWithImages, imageDatasetFactory);
        return createNewImageDataSet(details);
    }

    @Override
    public IDataSet createNewOverviewImageDataSet(SimpleImageDataConfig imageDataSet,
            File incomingFolderWithImages)
    {
        DataSetRegistrationDetails<ImageDataSetInformation> details =
                SimpleImageDataSetRegistrator.createImageDatasetDetails(imageDataSet,
                        incomingFolderWithImages, imageDatasetFactory);
        return createNewOverviewImageDataSet(details);
    }

    public IDataSet createNewFeatureListDataSet(FeatureListDataConfig config)
    {
        IDataSet dataSet = createNewDataSet();

        dataSet.setDataSetType(ScreeningConstants.ANALYSIS_FEATURE_LIST);

        IDataSetUpdatable container = config.getContainerDataSet();

        verifyFeatureVectorContainer(container);

        addNewDataSetToContainer(dataSet, container);

        dataSet.setExperiment(container.getExperiment());

        storeFeatureListInDataset(config, dataSet);

        return dataSet;
    }

    private void storeFeatureListInDataset(FeatureListDataConfig config, IDataSet dataSet)
    {
        try
        {
            File directory =
                    new File(getIncoming().getParentFile().getAbsolutePath(),
                            ScreeningConstants.ANALYSIS_FEATURE_LIST_TOP_LEVEL_DIRECTORY_NAME);
            directory.mkdirs();

            File file = new File(directory, config.getName());
            file.createNewFile();

            BufferedWriter bw = new BufferedWriter(new FileWriter(file));

            for (String feature : config.getFeatureList())
            {
                bw.append(feature);
                bw.newLine();
            }

            bw.close();

            moveFile(directory.getAbsolutePath(), dataSet);

        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private void addNewDataSetToContainer(IDataSet dataSet, IDataSetUpdatable container)
    {
        List<String> contained = new LinkedList<String>(container.getContainedDataSetCodes());
        contained.add(dataSet.getDataSetCode());
        container.setContainedDataSetCodes(contained);
    }

    private void verifyFeatureVectorContainer(IDataSetUpdatable container)
    {
        if (container == null)
        {
            throw new UserFailureException(
                    "Setting of container dataset for feature list data set is obligatory");
        }

        if (false == container.getDataSetType().startsWith(
                ScreeningConstants.HCS_ANALYSIS_ANY_CONTAINER_DATASET_TYPE_PREFIX))
        {
            throw new UserFailureException(
                    "Container for feature list must be of HCS_ANALYSIS_CONTAINER.* type");
        }
    }

    /**
     * Creates new container dataset which contains one feature vector dataset.
     */
    public FeatureVectorContainerDataSet createNewFeatureVectorDataSet(
            SimpleFeatureVectorDataConfig featureDataSetConfig, File featureVectorFileOrNull)
    {
        DataSetRegistrationDetails<FeatureVectorDataSetInformation> registrationDetails =
                createFeatureVectorDataSetRegistrationDetails(featureDataSetConfig,
                        featureVectorFileOrNull);
        return createFeatureVectorDataSet(registrationDetails);
    }

    private FeatureVectorContainerDataSet createFeatureVectorDataSet(
            DataSetRegistrationDetails<FeatureVectorDataSetInformation> registrationDetails)
    {
        @SuppressWarnings("unchecked")
        DataSet<FeatureVectorDataSetInformation> dataSet =
                (DataSet<FeatureVectorDataSetInformation>) super
                        .createNewDataSet(registrationDetails);

        FeatureVectorDataSet featureDataset =
                new FeatureVectorDataSet(dataSet, getGlobalState().getOpenBisService());

        // create container
        FeatureVectorContainerDataSet containerDataset =
                createFeatureVectorContainerDataSet(featureDataset);

        registrationDetails.getDataSetInformation().setContainerDatasetPermId(
                containerDataset.getDataSetCode());

        return containerDataset;
    }

    private DataSetRegistrationDetails<FeatureVectorDataSetInformation> createFeatureVectorDataSetRegistrationDetails(
            SimpleFeatureVectorDataConfig featureDataSetConfig, File featureVectorFileOrNull)
    {
        List<FeatureDefinition> featureDefinitions;
        Properties properties = featureDataSetConfig.getProperties();
        if (properties == null)
        {
            featureDefinitions =
                    ((FeaturesBuilder) featureDataSetConfig.getFeaturesBuilder())
                            .getFeatureDefinitionValuesList();
        } else
        {
            try
            {
                featureDefinitions =
                        CsvFeatureVectorParser.parse(featureVectorFileOrNull, properties);
            } catch (IOException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }
        DataSetRegistrationDetails<FeatureVectorDataSetInformation> registrationDetails =
                factory.createFeatureVectorRegistrationDetails(featureDefinitions);
        return registrationDetails;
    }

    /**
     * Creates container dataset which contains dataset with original images (created on the fly). If thumbnails are required they are generated and
     * moved to a thumbnail dataset which becomes a part of the container as well.
     * <p>
     * The original images dataset is special - it contains description of what should be saved in imaging database by the storage processor.
     * 
     * @return container dataset.
     */
    @Override
    public IImageDataSet createNewImageDataSet(
            DataSetRegistrationDetails<ImageDataSetInformation> imageRegistrationDetails)
    {
        ImageDataSetInformation imageDataSetInformation =
                imageRegistrationDetails.getDataSetInformation();
        ImageDataSetStructure imageDataSetStructure =
                imageDataSetInformation.getImageDataSetStructure();
        File incomingDirectory = imageDataSetInformation.getIncomingDirectory();
        List<String> containedDataSetCodes = new ArrayList<String>();

        // Compute the bounding box of the images -- needs to happen before thumbnail
        // generation, since thumbnails may want to know the bounding box
        calculateBoundingBox(imageDataSetInformation, imageDataSetStructure, incomingDirectory);

        // create thumbnails dataset if needed
        List<IDataSet> thumbnailDatasets = new ArrayList<IDataSet>();
        boolean generateThumbnails = imageDataSetStructure.areThumbnailsGenerated();
        if (generateThumbnails)
        {
            imageDataSetStructure
                    .validateImageRepresentationGenerationParameters(imageDataSetInformation);

            List<ThumbnailsStorageFormat> thumbnailsStorageFormatList =
                    imageDataSetStructure.getImageStorageConfiguraton()
                            .getThumbnailsStorageFormat();

            ThumbnailsInfo thumbnailsInfo = new ThumbnailsInfo();
            ImageCache imageCache = new ImageCache();
            for (ThumbnailsStorageFormat thumbnailsStorageFormat : thumbnailsStorageFormatList)
            {
                IDataSet thumbnailDataset =
                        createThumbnailDataset(imageDataSetInformation, thumbnailsStorageFormat);
                thumbnailDatasets.add(thumbnailDataset);

                generateThumbnails(imageDataSetStructure, incomingDirectory, thumbnailDataset,
                        thumbnailsStorageFormat, thumbnailsInfo, false, null, imageCache);
                containedDataSetCodes.add(thumbnailDataset.getDataSetCode());
            }
            imageDataSetInformation.setThumbnailsInfo(thumbnailsInfo);
        }

        // create main dataset (with original images)
        @SuppressWarnings("unchecked")
        DataSet<ImageDataSetInformation> mainDataset =
                (DataSet<ImageDataSetInformation>) super.createNewDataSet(imageRegistrationDetails);
        containedDataSetCodes.add(mainDataset.getDataSetCode());

        for (IDataSet thumbnailDataset : thumbnailDatasets)
        {
            setSameDatasetOwner(mainDataset, thumbnailDataset);
        }
        ImageContainerDataSet containerDataset =
                createImageContainerDataset(mainDataset, imageDataSetInformation,
                        containedDataSetCodes);
        containerDataset.setOriginalDataset(mainDataset);
        containerDataset.setThumbnailDatasets(thumbnailDatasets);
        imageDataSetInformation.setContainerDatasetPermId(containerDataset.getDataSetCode());

        return containerDataset;
    }

    @SuppressWarnings("unchecked")
    private IDataSet createNewOverviewImageDataSet(
            DataSetRegistrationDetails<ImageDataSetInformation> imageRegistrationDetails)
    {
        ImageDataSetInformation imageDataSetInformation =
                imageRegistrationDetails.getDataSetInformation();
        ImageDataSetStructure imageDataSetStructure =
                imageDataSetInformation.getImageDataSetStructure();
        File incomingDirectory = imageDataSetInformation.getIncomingDirectory();

        String containerCode = imageDataSetInformation.tryGetContainerDatasetPermId();

        IDataSetUpdatable container = getDataSetForUpdate(containerCode);

        if (container == null || false == container.isContainerDataSet())
        {
            throw UserFailureException.fromTemplate("Container data set %s coudn't be found.",
                    container);
        }

        calculateBoundingBox(imageDataSetInformation, imageDataSetStructure, incomingDirectory);

        SearchCriteria searchCriteria = new SearchCriteria();
        SearchCriteria searchSubCriteria = new SearchCriteria();
        searchSubCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.CODE, containerCode));
        searchCriteria.addSubCriteria(SearchSubCriteria
                .createDataSetContainerCriteria(searchSubCriteria));

        List<IDataSetImmutable> containedDataSets =
                getSearchService().searchForDataSets(searchCriteria);

        IDataSetImmutable exampleDataSet = containedDataSets.iterator().next();

        imageDataSetStructure
                .validateImageRepresentationGenerationParameters(imageDataSetInformation);

        ThumbnailsInfo thumbnailsInfo = new ThumbnailsInfo();
        List<String> containedDataSetCodes = new ArrayList<String>();
        containedDataSetCodes.addAll(container.getContainedDataSetCodes());

        List<IDataSet> thumbnailDatasets = new ArrayList<IDataSet>();
        ImageCache imageCache = new ImageCache();
        if (imageDataSetInformation.isGenerateOverviewImagesFromRegisteredImages())
        {
            IHierarchicalContent content =
                    ServiceProvider.getHierarchicalContentProvider().asContent(containerCode);
            try
            {
                imageDataSetStructure
                        .validateImageRepresentationGenerationParameters(imageDataSetInformation);

                List<ThumbnailsStorageFormat> thumbnailsStorageFormatList =
                        imageDataSetStructure.getImageStorageConfiguraton()
                                .getThumbnailsStorageFormat();

                boolean isFirst = true;
                for (ThumbnailsStorageFormat thumbnailsStorageFormat : thumbnailsStorageFormatList)
                {
                    IDataSet thumbnailDataset = null;
                    if (isFirst)
                    {
                        thumbnailDataset = super.createNewDataSet(imageRegistrationDetails);
                        thumbnailDataset.setFileFormatType(thumbnailsStorageFormat.getFileFormat()
                                .getOpenBISFileType());
                        thumbnailDataset.setMeasuredData(false);
                        isFirst = false;
                    } else
                    {
                        thumbnailDataset =
                                createThumbnailDataset(imageDataSetInformation,
                                        thumbnailsStorageFormat);

                    }
                    thumbnailDatasets.add(thumbnailDataset);

                    generateThumbnails(imageDataSetStructure, incomingDirectory, thumbnailDataset,
                            thumbnailsStorageFormat, thumbnailsInfo, false, content, imageCache);
                    containedDataSetCodes.add(thumbnailDataset.getDataSetCode());
                }
            } finally
            {
                if (content != null)
                {
                    content.close();
                }
            }
        } else
        {
            DataSet<ImageDataSetInformation> thumbnailDataset =
                    (DataSet<ImageDataSetInformation>) super
                            .createNewDataSet(imageRegistrationDetails);
            thumbnailDataset.setFileFormatType(imageDataSetInformation.getFileFormatTypeCode());
            thumbnailDataset.setMeasuredData(false);
            thumbnailDatasets.add(thumbnailDataset);

            generateThumbnails(imageDataSetStructure, incomingDirectory, thumbnailDataset,
                    createThumbnailsStorageFormat(imageDataSetInformation), thumbnailsInfo, true,
                    null, imageCache);
            containedDataSetCodes.add(thumbnailDataset.getDataSetCode());
        }

        imageDataSetInformation.setThumbnailsInfo(thumbnailsInfo);

        for (IDataSet thumbnailDataset : thumbnailDatasets)
        {
            setSameDatasetOwner(exampleDataSet, thumbnailDataset);
        }

        container.setContainedDataSetCodes(containedDataSetCodes);

        return thumbnailDatasets.iterator().next();
    }

    private static ThumbnailsStorageFormat createThumbnailsStorageFormat(
            ImageDataSetInformation imageDataSetInformation)
    {
        ThumbnailsStorageFormat thumbnailsStorageFormat = new ThumbnailsStorageFormat();

        thumbnailsStorageFormat.setFileFormat(imageDataSetInformation.getFileFormatTypeCode());
        thumbnailsStorageFormat.setThumbnailsFileName(String.format("thumbnails_%dx%d.h5ar",
                imageDataSetInformation.getMaximumImageWidth(),
                imageDataSetInformation.getMaximumImageHeight()));

        return thumbnailsStorageFormat;
    }

    private void calculateBoundingBox(ImageDataSetInformation imageDataSetInformation,
            ImageDataSetStructure imageDataSetStructure, File incomingDirectory)
    {
        ImageLibraryInfo imageLibrary =
                imageDataSetStructure.getImageStorageConfiguraton().tryGetImageLibrary();
        List<ImageFileInfo> images = imageDataSetStructure.getImages();

        if (imageDataSetInformation.isGenerateOverviewImagesFromRegisteredImages())
        {
            IHierarchicalContent content =
                    ServiceProvider.getHierarchicalContentProvider().asContent(
                            imageDataSetInformation.tryGetContainerDatasetPermId());
            try
            {
                for (ImageFileInfo imageFileInfo : images)
                {
                    setBoundingBox(imageDataSetInformation,
                            content.getNode(imageFileInfo.getImageRelativePath()), imageLibrary);
                }
            } finally
            {
                if (content != null)
                {
                    content.close();
                }
            }
        } else
        {
            for (ImageFileInfo imageFileInfo : images)
            {
                File file = new File(incomingDirectory, imageFileInfo.getImageRelativePath());
                setBoundingBox(imageDataSetInformation, new FileBasedContentNode(file),
                        imageLibrary);
            }
        }
    }

    private void setBoundingBox(ImageDataSetInformation imageDataSetInformation,
            IHierarchicalContentNode content, ImageLibraryInfo imageLibrary)
    {
        Size size = Utils.loadUnchangedImageSize(content, null, imageLibrary);
        imageDataSetInformation.setMaximumImageWidth(Math.max(
                imageDataSetInformation.getMaximumImageWidth(), size.getWidth()));
        imageDataSetInformation.setMaximumImageHeight(Math.max(
                imageDataSetInformation.getMaximumImageHeight(), size.getHeight()));
        if (imageDataSetInformation.getColorDepth() == null)
        {
            imageDataSetInformation.setColorDepth(Utils.loadUnchangedImageColorDepth(content, null,
                    imageLibrary));
        }
    }

    private File prependOriginalDirectory(String directoryPath)
    {
        return new File(originalDirName + File.separator + directoryPath);
    }

    private void generateThumbnails(ImageDataSetStructure imageDataSetStructure,
            File incomingDirectory, IDataSet thumbnailDataset,
            ThumbnailsStorageFormat thumbnailsStorageFormatOrNull, ThumbnailsInfo thumbnailPaths,
            boolean registerOriginalImageAsThumbnail, IHierarchicalContent content, ImageCache imageCache)
    {
        String thumbnailFile;
        if (thumbnailsStorageFormatOrNull == null)
        {
            thumbnailFile =
                    createNewFile(thumbnailDataset, Constants.HDF5_CONTAINER_THUMBNAILS_FILE_NAME);
        } else
        {
            thumbnailFile =
                    createNewFile(thumbnailDataset,
                            thumbnailsStorageFormatOrNull.getThumbnailsFileName());
        }

        Hdf5ThumbnailGenerator.tryGenerateThumbnails(imageDataSetStructure, incomingDirectory,
                thumbnailFile, imageDataSetStructure.getImageStorageConfiguraton(),
                thumbnailDataset.getDataSetCode(), thumbnailsStorageFormatOrNull, thumbnailPaths,
                registerOriginalImageAsThumbnail, content, imageCache);
        enhanceWithResolution(thumbnailDataset, thumbnailPaths);
    }

    private static void enhanceWithResolution(IDataSet thumbnailDataset,
            ThumbnailsInfo thumbnailPaths)
    {
        Size size = thumbnailPaths.tryGetDimension(thumbnailDataset.getDataSetCode());
        if (size != null)
        {
            thumbnailDataset.setPropertyValue(ScreeningConstants.RESOLUTION, size.getWidth() + "x"
                    + size.getHeight());
        }
    }

    private IDataSet createThumbnailDataset(DataSetInformation imageDataSetInformation,
            ThumbnailsStorageFormat thumbnailsStorageFormat)
    {
        String thumbnailsDatasetTypeCode = findThumbnailsDatasetTypeCode(imageDataSetInformation);
        IDataSet thumbnailDataset = createNewDataSet(thumbnailsDatasetTypeCode);
        thumbnailDataset.setFileFormatType(thumbnailsStorageFormat.getFileFormat()
                .getOpenBISFileType());
        thumbnailDataset.setMeasuredData(false);

        return thumbnailDataset;
    }

    private ImageContainerDataSet createImageContainerDataset(IDataSet mainDataset,
            DataSetInformation imageDataSetInformation, List<String> containedDataSetCodes)
    {
        String containerDatasetTypeCode = findContainerDatasetTypeCode(imageDataSetInformation);
        @SuppressWarnings("unchecked")
        ImageContainerDataSet containerDataset =
                (ImageContainerDataSet) createNewDataSet(imageContainerDatasetFactory,
                        containerDatasetTypeCode);
        containerDataset.setDataSetKind(DataSetKind.CONTAINER);
        setSameDatasetOwner(mainDataset, containerDataset);
        moveDatasetRelations(mainDataset, containerDataset);

        containerDataset.setContainedDataSetCodes(containedDataSetCodes);
        return containerDataset;
    }

    private FeatureVectorContainerDataSet createFeatureVectorContainerDataSet(
            FeatureVectorDataSet mainDataset)
    {
        String containerDatasetTypeCode =
                FeatureVectorContainerDataSet
                        .getContainerAnalysisType(mainDataset.getDataSetType());

        @SuppressWarnings("unchecked")
        FeatureVectorContainerDataSet containerDataSet =
                (FeatureVectorContainerDataSet) createNewDataSet(
                        factory.featureVectorContainerDatasetFactory, containerDatasetTypeCode);
        containerDataSet.setContainedDataSetCodes(Collections.singletonList(mainDataset
                .getDataSetCode()));
        containerDataSet.setDataSetKind(DataSetKind.CONTAINER);

        containerDataSet.setOriginalDataSet(mainDataset);

        return containerDataSet;
    }

    // Copies properties and relations to datasets from the main dataset to the container and
    // resets them in the main dataset.
    private static void moveDatasetRelations(IDataSet mainDataset, IDataSet containerDataset)
    {
        containerDataset.setParentDatasets(mainDataset.getParentDatasets());
        mainDataset.setParentDatasets(Collections.<String> emptyList());

        for (String propertyCode : mainDataset.getAllPropertyCodes())
        {
            containerDataset.setPropertyValue(propertyCode,
                    mainDataset.getPropertyValue(propertyCode));
            mainDataset.setPropertyValue(propertyCode, null);
        }
    }

    private static boolean isHCSImageDataSetType(String mainDatasetTypeCode)
    {
        String prefix = ScreeningConstants.HCS_IMAGE_DATASET_TYPE_PREFIX;
        if (mainDatasetTypeCode.startsWith(prefix))
        {
            if (mainDatasetTypeCode
                    .contains(ScreeningConstants.IMAGE_CONTAINER_DATASET_TYPE_MARKER))
            {
                throw UserFailureException
                        .fromTemplate(
                                "The specified image dataset type '%s' should not be of container type, but contains '%s' in the type code.",
                                mainDatasetTypeCode,
                                ScreeningConstants.IMAGE_CONTAINER_DATASET_TYPE_MARKER);
            }
            return true;
        } else
        {
            return false;
        }
    }

    private static boolean isMicroscopyImageDataSetType(String dataSetTypeCode)
    {
        return dataSetTypeCode.contains(MICROSCOPY_IMAGE_TYPE_SUBSTRING)
                && false == dataSetTypeCode.contains(MICROSCOPY_CONTAINER_TYPE_SUBSTRING);
    }

    private static String findContainerDatasetTypeCode(DataSetInformation imageDataSetInformation)
    {
        String dataSetTypeCode = imageDataSetInformation.getDataSetType().getCode().toUpperCase();
        String prefix = ScreeningConstants.HCS_IMAGE_DATASET_TYPE_PREFIX;
        if (isHCSImageDataSetType(dataSetTypeCode))
        {
            return prefix + ScreeningConstants.IMAGE_CONTAINER_DATASET_TYPE_MARKER
                    + dataSetTypeCode.substring(prefix.length());
        } else if (isMicroscopyImageDataSetType(dataSetTypeCode))
        {
            return dataSetTypeCode.replace(MICROSCOPY_IMAGE_TYPE_SUBSTRING,
                    MICROSCOPY_CONTAINER_TYPE_SUBSTRING);
        } else
        {
            throw UserFailureException
                    .fromTemplate(
                            "The image dataset type '%s' is neither standard HCS type (starts with '%s') nor a microscopy type (contains '%s').",
                            dataSetTypeCode, prefix,
                            ScreeningConstants.MICROSCOPY_IMAGE_SAMPLE_TYPE_PATTERN);
        }
    }

    private static String findThumbnailsDatasetTypeCode(DataSetInformation imageDataSetInformation)
    {
        String dataSetTypeCode = imageDataSetInformation.getDataSetType().getCode().toUpperCase();

        if (isHCSImageDataSetType(dataSetTypeCode))
        {
            return ScreeningConstants.HCS_IMAGE_DATASET_TYPE_PREFIX
                    + ScreeningConstants.IMAGE_THUMBNAIL_DATASET_TYPE_MARKER;
        } else if (isMicroscopyImageDataSetType(dataSetTypeCode))
        {
            return dataSetTypeCode.replace(ScreeningConstants.MICROSCOPY_IMAGE_TYPE_SUBSTRING,
                    ScreeningConstants.MICROSCOPY_THUMBNAIL_TYPE_SUBSTRING);
        } else
        {
            throw UserFailureException
                    .fromTemplate(
                            "The image dataset type '%s' is neither standard HCS type (starts with '%s') nor a microscopy type (contains '%s').",
                            dataSetTypeCode, ScreeningConstants.HCS_IMAGE_DATASET_TYPE_PREFIX,
                            ScreeningConstants.MICROSCOPY_IMAGE_SAMPLE_TYPE_PATTERN);
        }
    }

    private static void setSameDatasetOwner(IDataSetImmutable templateDataset,
            IDataSet destinationDataset)
    {
        destinationDataset.setExperiment(templateDataset.getExperiment());
        destinationDataset.setSample(templateDataset.getSample());

    }

    @SuppressWarnings(
    { "cast", "unchecked" })
    @Override
    public IDataSet createNewDataSet(DataSetRegistrationDetails registrationDetails)
    {
        if (registrationDetails.getDataSetInformation() instanceof ImageDataSetInformation)
        {
            DataSetRegistrationDetails<ImageDataSetInformation> imageRegistrationDetails =
                    (DataSetRegistrationDetails<ImageDataSetInformation>) registrationDetails;
            return createNewImageDataSet(imageRegistrationDetails);
        } else if (registrationDetails.getDataSetInformation() instanceof FeatureVectorDataSetInformation)
        {
            DataSetRegistrationDetails<FeatureVectorDataSetInformation> featureRegistrationDetails =
                    (DataSetRegistrationDetails<FeatureVectorDataSetInformation>) registrationDetails;
            return createFeatureVectorDataSet(featureRegistrationDetails);
        } else
        {
            return super.createNewDataSet(registrationDetails);
        }
    }

    /**
     * If we are dealing with the image dataset container then the move operation is delegated to the original dataset. Otherwise a default
     * implementation is used.
     */
    @Override
    public String moveFile(String src, IDataSet dst)
    {
        return moveFile(src, dst, new File(src).getName());
    }

    /**
     * If we are dealing with the image dataset container then the move operation is delegated to the original dataset. Otherwise a default
     * implementation is used.
     */
    @Override
    public String moveFile(String src, IDataSet dst, String dstInDataset)
    {
        ImageContainerDataSet imageContainerDataset = tryAsImageContainerDataset(dst);

        if (imageContainerDataset != null)
        {
            String destination = getDestinationInOriginal(dstInDataset);
            DataSet<ImageDataSetInformation> originalDataset =
                    imageContainerDataset.getOriginalDataset();
            if (originalDataset == null)
            {
                throw new UserFailureException(
                        "Cannot move the files because the original dataset is missing: " + src);
            }
            originalDataset.getRegistrationDetails().getDataSetInformation()
                    .setDatasetRelativeImagesFolderPath(new File(destination));

            return super.moveFile(src, originalDataset, destination);
        }

        FeatureVectorContainerDataSet featureContainer = tryAsFeatureVectorContainerDataset(dst);
        if (featureContainer != null)
        {
            IDataSet originalDataSet = featureContainer.getOriginalDataset();

            if (originalDataSet == null)
            {
                throw new UserFailureException(
                        "Cannot move the files because the original dataset is missing: " + src);
            }

            return super.moveFile(src, originalDataSet, dstInDataset);
        }

        return super.moveFile(src, dst, dstInDataset);
    }

    private String getDestinationInOriginal(String dstInDataset)
    {
        String destination = dstInDataset;
        if (destination.startsWith(originalDirName) == false)
        {
            destination = prependOriginalDirectory(destination).getPath();
        }
        return destination;
    }

    private static FeatureVectorContainerDataSet tryAsFeatureVectorContainerDataset(IDataSet dataset)
    {
        if (dataset instanceof FeatureVectorContainerDataSet)
        {
            return (FeatureVectorContainerDataSet) dataset;
        }
        return null;
    }

    private static ImageContainerDataSet tryAsImageContainerDataset(IDataSet dataset)
    {
        if (dataset instanceof ImageContainerDataSet)
        {
            return (ImageContainerDataSet) dataset;
        } else
        {
            return null;
        }
    }
}

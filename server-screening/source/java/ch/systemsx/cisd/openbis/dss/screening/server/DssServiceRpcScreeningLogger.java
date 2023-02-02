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

package ch.systemsx.cisd.openbis.dss.screening.server;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import ch.systemsx.cisd.base.image.IImageTransformerFactory;
import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.dss.screening.shared.api.v1.IDssServiceRpcScreening;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerLogger;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.DatasetImageRepresentationFormats;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureInformation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetWellReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorWithDescription;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IFeatureVectorDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IImageDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IImageRepresentationFormatSelectionCriterion;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageRepresentationFormat;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageSize;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.MicroscopyImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellPosition;

/**
 * @author Franz-Josef Elmer
 */
public class DssServiceRpcScreeningLogger extends AbstractServerLogger implements
        IDssServiceRpcScreening
{

    DssServiceRpcScreeningLogger(IInvocationLoggerContext context)
    {
        super(null, context);
    }

    @Override
    public int getMajorVersion()
    {
        return 0;
    }

    @Override
    public int getMinorVersion()
    {
        return 0;
    }

    @Override
    public List<String> listAvailableFeatureNames(String sessionToken,
            List<? extends IFeatureVectorDatasetIdentifier> featureDatasets)
    {
        logAccess(sessionToken, "load_available_feature_codes", "DATASET_REFERENCES(%s)",
                featureDatasets);
        return null;
    }

    @Override
    public List<String> listAvailableFeatureCodes(String sessionToken,
            List<? extends IFeatureVectorDatasetIdentifier> featureDatasets)
    {
        logAccess(sessionToken, "load_available_feature_names", "DATASET_REFERENCES(%s)",
                featureDatasets);
        return null;
    }

    @Override
    public List<FeatureInformation> listAvailableFeatures(String sessionToken,
            List<? extends IFeatureVectorDatasetIdentifier> featureDatasets)
    {
        logAccess(sessionToken, "load_available_features", "DATASET_REFERENCES(%s)",
                featureDatasets);
        return null;
    }

    @Override
    public List<String> listAvailableFeatureLists(String sessionToken,
            IFeatureVectorDatasetIdentifier featureDataset)
    {
        logAccess(sessionToken, "list_available_feature_lists", "DATASET_REFERENCE(%s)",
                featureDataset);
        return null;
    }

    @Override
    public List<String> getFeatureList(String sessionToken,
            IFeatureVectorDatasetIdentifier featureDataset, String featureListCode)
    {
        logAccess(sessionToken, "get_feature_list", "DATASET_REFERENCE(%s) FEATURE_LIST(%s)",
                featureDataset, featureListCode);
        return null;
    }

    @Override
    public List<FeatureVectorDataset> loadFeatures(String sessionToken,
            List<FeatureVectorDatasetReference> featureDatasets, List<String> featureCodes)
    {
        logAccess(sessionToken, "load_features", "DATASET_REFERENCES(%s) FEATURES(%s)",
                featureDatasets, featureCodes);
        return null;
    }

    @Override
    public List<FeatureVectorWithDescription> loadFeaturesForDatasetWellReferences(
            String sessionToken, List<FeatureVectorDatasetWellReference> datasetWellReferences,
            List<String> featureCodes)
    {
        logAccess(sessionToken, "load_feature_for_dataset_well_references",
                "WELL_REFERENCES(%s) FEATURES(%s)", datasetWellReferences, featureCodes);
        return null;
    }

    @Override
    public InputStream loadImages(String sessionToken, List<PlateImageReference> imageReferences,
            boolean convertToPng)
    {
        logAccess(sessionToken, "load_images", "IMAGE_REFERENCES(%s) CONVERT(%s)", imageReferences,
                convertToPng);
        return null;
    }

    @Override
    public List<String> loadImagesBase64(String sessionToken, List<PlateImageReference> references,
            boolean convertToPng)
    {
        logAccess(sessionToken, "load_images_base64", "IMAGE_REFERENCES(%s) CONVERT(%s)",
                references, convertToPng);
        return null;
    }

    @Override
    public InputStream loadImages(String sessionToken, List<PlateImageReference> imageReferences,
            ImageSize thumbnailSizeOrNull)
    {
        logAccess(sessionToken, "load_images", "IMAGE_REFERENCES(%s) SIZE(%s)", imageReferences,
                thumbnailSizeOrNull);
        return null;
    }

    @Override
    public List<String> loadImagesBase64(String sessionToken,
            List<PlateImageReference> imageReferences, ImageSize size)
    {
        logAccess(sessionToken, "load_images_base64", "IMAGE_REFERENCES(%s) SIZE(%s)",
                imageReferences, size);
        return null;
    }

    @Override
    public InputStream loadImages(String sessionToken, List<PlateImageReference> imageReferences)
    {
        logAccess(sessionToken, "load_images", "IMAGE_REFERENCES(%s)", imageReferences);
        return null;
    }

    @Override
    public List<String> loadImagesBase64(String sessionToken,
            List<PlateImageReference> imageReferences)
    {
        logAccess(sessionToken, "load_images_base64", "IMAGE_REFERENCES(%s)", imageReferences);
        return null;
    }

    @Override
    public InputStream loadImages(String sessionToken, IDatasetIdentifier dataSetIdentifier,
            List<WellPosition> wellPositions, String channel, ImageSize thumbnailSizeOrNull)
    {
        logAccess(sessionToken, "load_images", "DATA_SET(%s) CHANNEL(%s) IMAGE_SIZE(%s)",
                dataSetIdentifier, channel, thumbnailSizeOrNull);
        return null;
    }

    @Override
    public List<String> loadImagesBase64(String sessionToken, IDatasetIdentifier dataSetIdentifier,
            List<WellPosition> wellPositions, String channel, ImageSize thumbnailSizeOrNull)
    {
        logAccess(sessionToken, "load_images_base64", "DATA_SET(%s) CHANNEL(%s) IMAGE_SIZE(%s)",
                dataSetIdentifier, channel, thumbnailSizeOrNull);
        return null;
    }

    @Override
    public InputStream loadImages(String sessionToken, IDatasetIdentifier dataSetIdentifier,
            String channel, ImageSize thumbnailSizeOrNull)
    {
        logAccess(sessionToken, "load_images microscopy ",
                "DATA_SET(%s) CHANNEL(%s) IMAGE_SIZE(%s)", dataSetIdentifier, channel,
                thumbnailSizeOrNull);
        return null;
    }

    @Override
    public List<String> loadImagesBase64(String sessionToken, IDatasetIdentifier dataSetIdentifier,
            String channel, ImageSize thumbnailSizeOrNull)
    {
        logAccess(sessionToken, "load_images microscopy base64 ",
                "DATA_SET(%s) CHANNEL(%s) IMAGE_SIZE(%s)", dataSetIdentifier, channel,
                thumbnailSizeOrNull);
        return null;
    }

    @Override
    public List<MicroscopyImageReference> listImageReferences(String sessionToken,
            IDatasetIdentifier dataSetIdentifier, String channel)
    {
        logAccess(sessionToken, "list_image_references", "DATA_SET(%s) CHANNEL(%s)",
                dataSetIdentifier, channel);
        return null;
    }

    @Override
    public List<MicroscopyImageReference> listImageReferences(String sessionToken,
            IDatasetIdentifier dataSetIdentifier, List<String> channels)
    {
        logAccess(sessionToken, "list_image_references", "DATA_SET(%s) CHANNELS(%s)",
                dataSetIdentifier, channels);
        return null;
    }

    @Override
    public List<PlateImageReference> listPlateImageReferences(String sessionToken,
            IDatasetIdentifier dataSetIdentifier, List<WellPosition> wellPositions, String channel)
    {
        logAccess(sessionToken, "list_plate_image_references", "DATA_SET(%s) CHANNEL(%s)",
                dataSetIdentifier, channel);
        return null;
    }

    @Override
    public List<PlateImageReference> listPlateImageReferences(String sessionToken,
            IDatasetIdentifier dataSetIdentifier, List<WellPosition> wellPositions,
            List<String> channels)
    {
        logAccess(sessionToken, "list_plate_image_references", "DATA_SET(%s) CHANNELS(%s)",
                dataSetIdentifier, channels);
        return null;
    }

    @Override
    public void saveImageTransformerFactory(String sessionToken,
            List<IDatasetIdentifier> dataSetIdentifiers, String channel,
            IImageTransformerFactory transformerFactory)
    {
        logTracking(sessionToken, "save_image_transformer_factory",
                "DATA_SETS(%s) CHANNEL(%s) FACTORY(%s)", dataSetIdentifiers, channel,
                transformerFactory);
    }

    @Override
    public IImageTransformerFactory getImageTransformerFactoryOrNull(String sessionToken,
            List<IDatasetIdentifier> dataSetIdentifiers, String channel)
    {
        logAccess(sessionToken, "get_image_transformer_factory", "DATA_SETS(%s) CHANNEL(%s)",
                dataSetIdentifiers, channel);
        return null;
    }

    @Override
    public List<ImageDatasetMetadata> listImageMetadata(String sessionToken,
            List<? extends IImageDatasetIdentifier> imageDatasets)
    {
        logAccess(sessionToken, "load_image_metadata", "DATA_SETS(%s)", imageDatasets);
        return null;
    }

    public void checkDatasetsAuthorizationForIDatasetIdentifier(String sessionToken,
            List<? extends IDatasetIdentifier> featureDatasets)
    {
        // server already logs
    }

    public InputStream loadThumbnailImages(String sessionToken,
            IDatasetIdentifier dataSetIdentifier, List<WellPosition> wellPositions,
            List<String> channels)
    {
        logAccess(sessionToken, "load_thumbnail_images", "DATA_SET(%s) CHANNELS(%s)",
                dataSetIdentifier, channels);
        return null;
    }

    public List<String> loadThumbnailImagesBase64(String sessionToken,
            IDatasetIdentifier dataSetIdentifier, List<WellPosition> wellPositions,
            List<String> channels)
    {
        logAccess(sessionToken, "load_thumbnail_images_base64", "DATA_SET(%s) CHANNELS(%s)",
                dataSetIdentifier, channels);
        return null;
    }

    @Override
    public InputStream loadThumbnailImages(String sessionToken,
            List<PlateImageReference> imageReferences)
    {
        logAccess(sessionToken, "load_thumbnail_images", "IMAGE_REFERENCES(%s)", imageReferences);
        return null;
    }

    @Override
    public List<String> loadThumbnailImagesBase64(String sessionToken,
            List<PlateImageReference> imageReferences)
    {
        logAccess(sessionToken, "load_thumbnail_images_base64", "IMAGE_REFERENCES(%s)",
                imageReferences);
        return null;
    }

    @Override
    public InputStream loadThumbnailImages(String sessionToken,
            IDatasetIdentifier dataSetIdentifier, List<String> channels)
    {
        logAccess(sessionToken, "load_thumbnail_images microscopy ", "DATA_SET(%s) CHANNELS(%s)",
                dataSetIdentifier, channels);
        return null;
    }

    @Override
    public List<String> loadThumbnailImagesBase64(String sessionToken,
            IDatasetIdentifier dataSetIdentifier, List<String> channels)
    {
        logAccess(sessionToken, "load_thumbnail_images microscopy base64 ",
                "DATA_SET(%s) CHANNELS(%s)", dataSetIdentifier, channels);
        return null;
    }

    @Override
    public InputStream loadImages(
            String sessionToken,
            List<PlateImageReference> imageReferences,
            ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.LoadImageConfiguration configuration)
    {
        logAccess(sessionToken, "load_images", "IMAGE_REFERENCES(%s) CONFIGURATION(%s)",
                imageReferences, configuration);
        return null;
    }

    @Override
    public List<String> loadImagesBase64(
            String sessionToken,
            List<PlateImageReference> imageReferences,
            ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.LoadImageConfiguration configuration)
    {
        logAccess(sessionToken, "load_images_base64", "IMAGE_REFERENCES(%s) CONFIGURATION(%s)",
                imageReferences, configuration);
        return null;
    }

    @Override
    public InputStream loadImages(String sessionToken, List<PlateImageReference> imageReferences,
            ImageRepresentationFormat format)
    {
        logAccess(sessionToken, "load_images", "IMAGE_REFERENCES(%s) FORMAT(%s)", imageReferences,
                format);
        return null;
    }

    @Override
    public List<String> loadImagesBase64(String sessionToken,
            List<PlateImageReference> imageReferences, ImageRepresentationFormat format)
    {
        logAccess(sessionToken, "load_images_base64", "IMAGE_REFERENCES(%s) FORMAT(%s)",
                imageReferences, format);
        return null;
    }

    @Override
    public InputStream loadImages(String sessionToken, List<PlateImageReference> imageReferences,
            IImageRepresentationFormatSelectionCriterion... criteria)
    {
        logAccess(sessionToken, "load_images", "IMAGE_REFERENCES(%s) CRITERIA(%s)",
                imageReferences, Arrays.asList(criteria));
        return null;
    }

    @Override
    public List<String> loadImagesBase64(String sessionToken,
            List<PlateImageReference> imageReferences,
            IImageRepresentationFormatSelectionCriterion... criteria)
    {
        logAccess(sessionToken, "load_images_base64", "IMAGE_REFERENCES(%s) CRITERIA(%s)",
                imageReferences, Arrays.asList(criteria));
        return null;
    }

    @Override
    public List<DatasetImageRepresentationFormats> listAvailableImageRepresentationFormats(
            String sessionToken, List<? extends IDatasetIdentifier> imageDatasets)
    {
        logAccess(sessionToken, "list_available_image_representation_formats", "DATA_SETS(%s)",
                imageDatasets);
        return null;
    }

    @Override
    public List<String> loadPhysicalThumbnailsBase64(String sessionToken,
            List<PlateImageReference> imageReferences, ImageRepresentationFormat format)
    {
        logAccess(sessionToken, "load_physical_thumbnails_base64",
                "IMAGE_REFERENCES(%s) FORMAT(%s)", imageReferences, format);
        return null;
    }

    @Override
    public InputStream loadPhysicalThumbnails(String sessionToken,
            List<PlateImageReference> imageReferences, ImageRepresentationFormat format)
    {
        logAccess(sessionToken, "load_physical_thumbnails", "IMAGE_REFERENCES(%s) FORMAT(%s)",
                imageReferences, format);
        return null;
    }

}

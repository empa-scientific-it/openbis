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

package ch.systemsx.cisd.openbis.dss.screening.server;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ch.systemsx.cisd.base.image.IImageTransformerFactory;
import ch.systemsx.cisd.common.collection.IModifiable;
import ch.systemsx.cisd.openbis.dss.screening.shared.api.v1.IDssServiceRpcScreening;
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
 * @author pkupczyk
 */
public class DssServiceRpcScreeningJson implements IDssServiceRpcScreening
{

    private IDssServiceRpcScreening service;

    public DssServiceRpcScreeningJson(IDssServiceRpcScreening service)
    {
        if (service == null)
        {
            throw new IllegalArgumentException("Service was null");
        }
        this.service = service;
    }

    @Override
    public int getMajorVersion()
    {
        return service.getMajorVersion();
    }

    @Override
    public int getMinorVersion()
    {
        return service.getMinorVersion();
    }

    @Override
    @SuppressWarnings("deprecation")
    public List<String> listAvailableFeatureNames(String sessionToken,
            List<? extends IFeatureVectorDatasetIdentifier> featureDatasets)
    {
        return service.listAvailableFeatureNames(sessionToken, featureDatasets);
    }

    @Override
    public List<String> listAvailableFeatureCodes(String sessionToken,
            List<? extends IFeatureVectorDatasetIdentifier> featureDatasets)
    {
        return service.listAvailableFeatureCodes(sessionToken, featureDatasets);
    }

    @Override
    public List<FeatureInformation> listAvailableFeatures(String sessionToken,
            List<? extends IFeatureVectorDatasetIdentifier> featureDatasets)
    {
        return new FeatureInformationList(service.listAvailableFeatures(sessionToken,
                featureDatasets));
    }

    @Override
    public List<String> listAvailableFeatureLists(String sessionToken,
            IFeatureVectorDatasetIdentifier featureDataset)
    {
        return service.listAvailableFeatureLists(sessionToken, featureDataset);
    }

    @Override
    public List<String> getFeatureList(String sessionToken,
            IFeatureVectorDatasetIdentifier featureDataset, String featureListCode)
    {
        return service.getFeatureList(sessionToken, featureDataset, featureListCode);
    }

    @Override
    public List<FeatureVectorDataset> loadFeatures(String sessionToken,
            List<FeatureVectorDatasetReference> featureDatasets, List<String> featureCodes)
    {
        return new FeatureVectorDatasetList(service.loadFeatures(sessionToken, featureDatasets,
                featureCodes));
    }

    @Override
    public List<FeatureVectorWithDescription> loadFeaturesForDatasetWellReferences(
            String sessionToken, List<FeatureVectorDatasetWellReference> datasetWellReferences,
            List<String> featureCodes)
    {
        return new FeatureVectorWithDescriptionList(service.loadFeaturesForDatasetWellReferences(
                sessionToken, datasetWellReferences, featureCodes));
    }

    @Override
    public InputStream loadImages(String sessionToken, List<PlateImageReference> imageReferences,
            boolean convertToPng)
    {
        return handleNotSupportedMethod();
    }

    @Override
    public List<String> loadImagesBase64(String sessionToken,
            List<PlateImageReference> imageReferences, boolean convertToPng)
    {
        return service.loadImagesBase64(sessionToken, imageReferences, convertToPng);
    }

    @Override
    public InputStream loadThumbnailImages(String sessionToken,
            List<PlateImageReference> imageReferences)
    {
        return handleNotSupportedMethod();
    }

    @Override
    public List<String> loadThumbnailImagesBase64(String sessionToken,
            List<PlateImageReference> imageReferences)
    {
        return service.loadThumbnailImagesBase64(sessionToken, imageReferences);
    }

    @Override
    public InputStream loadImages(String sessionToken, List<PlateImageReference> imageReferences,
            ImageSize size)
    {
        return handleNotSupportedMethod();
    }

    @Override
    public List<String> loadImagesBase64(String sessionToken,
            List<PlateImageReference> imageReferences, ImageSize size)
    {
        return service.loadImagesBase64(sessionToken, imageReferences, size);
    }

    @Override
    public InputStream loadImages(String sessionToken, List<PlateImageReference> imageReferences)
    {
        return handleNotSupportedMethod();
    }

    @Override
    public List<String> loadImagesBase64(String sessionToken,
            List<PlateImageReference> imageReferences)
    {
        return service.loadImagesBase64(sessionToken, imageReferences);
    }

    @Override
    public InputStream loadImages(String sessionToken, IDatasetIdentifier dataSetIdentifier,
            List<WellPosition> wellPositions, String channel, ImageSize thumbnailSizeOrNull)
    {
        return handleNotSupportedMethod();
    }

    @Override
    public List<String> loadImagesBase64(String sessionToken, IDatasetIdentifier dataSetIdentifier,
            List<WellPosition> wellPositions, String channel, ImageSize thumbnailSizeOrNull)
    {
        return service.loadImagesBase64(sessionToken, dataSetIdentifier, wellPositions, channel,
                thumbnailSizeOrNull);
    }

    @Override
    public InputStream loadImages(String sessionToken, IDatasetIdentifier dataSetIdentifier,
            String channel, ImageSize thumbnailSizeOrNull)
    {
        return handleNotSupportedMethod();
    }

    @Override
    public List<String> loadImagesBase64(String sessionToken, IDatasetIdentifier dataSetIdentifier,
            String channel, ImageSize thumbnailSizeOrNull)
    {
        return service.loadImagesBase64(sessionToken, dataSetIdentifier, channel,
                thumbnailSizeOrNull);
    }

    @Override
    public InputStream loadImages(
            String sessionToken,
            List<PlateImageReference> imageReferences,
            ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.LoadImageConfiguration configuration)
    {
        return handleNotSupportedMethod();
    }

    @Override
    public List<String> loadImagesBase64(
            String sessionToken,
            List<PlateImageReference> imageReferences,
            ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.LoadImageConfiguration configuration)
    {
        return service.loadImagesBase64(sessionToken, imageReferences, configuration);
    }

    @Override
    public InputStream loadImages(String sessionToken, List<PlateImageReference> imageReferences,
            ImageRepresentationFormat format)
    {
        return handleNotSupportedMethod();
    }

    @Override
    public List<String> loadImagesBase64(String sessionToken,
            List<PlateImageReference> imageReferences, ImageRepresentationFormat format)
    {
        return service.loadImagesBase64(sessionToken, imageReferences, format);
    }

    @Override
    public InputStream loadImages(String sessionToken, List<PlateImageReference> imageReferences,
            IImageRepresentationFormatSelectionCriterion... criteria)
    {
        return handleNotSupportedMethod();
    }

    @Override
    public List<String> loadImagesBase64(String sessionToken,
            List<PlateImageReference> imageReferences,
            IImageRepresentationFormatSelectionCriterion... criteria)
    {
        return service.loadImagesBase64(sessionToken, imageReferences, criteria);
    }

    @Override
    public InputStream loadThumbnailImages(String sessionToken,
            IDatasetIdentifier dataSetIdentifier, List<String> channels)
    {
        return handleNotSupportedMethod();
    }

    @Override
    public List<String> loadThumbnailImagesBase64(String sessionToken,
            IDatasetIdentifier dataSetIdentifier, List<String> channels)
    {
        return service.loadThumbnailImagesBase64(sessionToken, dataSetIdentifier, channels);
    }

    @Override
    public List<PlateImageReference> listPlateImageReferences(String sessionToken,
            IDatasetIdentifier dataSetIdentifier, List<WellPosition> wellPositions, String channel)
    {
        return new PlateImageReferenceList(service.listPlateImageReferences(sessionToken,
                dataSetIdentifier, wellPositions, channel));
    }

    @Override
    public List<PlateImageReference> listPlateImageReferences(String sessionToken,
            IDatasetIdentifier dataSetIdentifier, List<WellPosition> wellPositions,
            List<String> channels)
    {
        return new PlateImageReferenceList(service.listPlateImageReferences(sessionToken,
                dataSetIdentifier, wellPositions, channels));
    }

    @Override
    public List<MicroscopyImageReference> listImageReferences(String sessionToken,
            IDatasetIdentifier dataSetIdentifier, String channel)
    {
        return new MicroscopyImageReferenceList(service.listImageReferences(sessionToken,
                dataSetIdentifier, channel));
    }

    @Override
    public List<MicroscopyImageReference> listImageReferences(String sessionToken,
            IDatasetIdentifier dataSetIdentifier, List<String> channels)
    {
        return new MicroscopyImageReferenceList(service.listImageReferences(sessionToken,
                dataSetIdentifier, channels));
    }

    @Override
    public void saveImageTransformerFactory(String sessionToken,
            List<IDatasetIdentifier> dataSetIdentifiers, String channel,
            IImageTransformerFactory transformerFactory)
    {
        handleNotSupportedMethod();
    }

    @Override
    public IImageTransformerFactory getImageTransformerFactoryOrNull(String sessionToken,
            List<IDatasetIdentifier> dataSetIdentifiers, String channel)
    {
        return handleNotSupportedMethod();
    }

    @Override
    public List<ImageDatasetMetadata> listImageMetadata(String sessionToken,
            List<? extends IImageDatasetIdentifier> imageDatasets)
    {
        return new ImageDatasetMetadataList(service.listImageMetadata(sessionToken, imageDatasets));
    }

    @Override
    public List<DatasetImageRepresentationFormats> listAvailableImageRepresentationFormats(
            String sessionToken, List<? extends IDatasetIdentifier> imageDatasets)
    {
        return new DatasetImageRepresentationFormatsList(
                service.listAvailableImageRepresentationFormats(sessionToken, imageDatasets));
    }

    @Override
    public List<String> loadPhysicalThumbnailsBase64(String sessionToken,
            List<PlateImageReference> imageReferences, ImageRepresentationFormat format)
    {
        return service.loadPhysicalThumbnailsBase64(sessionToken, imageReferences, format);
    }

    @Override
    public InputStream loadPhysicalThumbnails(String sessionToken,
            List<PlateImageReference> imageReferences, ImageRepresentationFormat format)
    {
        return handleNotSupportedMethod();
    }

    private <T> T handleNotSupportedMethod()
    {
        throw new UnsupportedOperationException("This method is not supported in JSON API yet");
    }

    /*
     * The collections listed below have been created to help Jackson library embed/detect types of the collection's items during JSON
     * serialization/deserialization. (see http://wiki.fasterxml.com/JacksonPolymorphicDeserialization#A5._Known_Issues)
     */

    private static class FeatureInformationList extends ArrayList<FeatureInformation> implements
            IModifiable
    {
        private static final long serialVersionUID = 1L;

        public FeatureInformationList(Collection<? extends FeatureInformation> c)
        {
            super(c);
        }
    }

    private static class FeatureVectorDatasetList extends ArrayList<FeatureVectorDataset> implements
            IModifiable
    {
        private static final long serialVersionUID = 1L;

        public FeatureVectorDatasetList(Collection<? extends FeatureVectorDataset> c)
        {
            super(c);
        }
    }

    private static class FeatureVectorWithDescriptionList extends
            ArrayList<FeatureVectorWithDescription> implements IModifiable
    {
        private static final long serialVersionUID = 1L;

        public FeatureVectorWithDescriptionList(Collection<? extends FeatureVectorWithDescription> c)
        {
            super(c);
        }
    }

    private static class PlateImageReferenceList extends ArrayList<PlateImageReference> implements
            IModifiable
    {
        private static final long serialVersionUID = 1L;

        public PlateImageReferenceList(Collection<? extends PlateImageReference> c)
        {
            super(c);
        }
    }

    private static class MicroscopyImageReferenceList extends ArrayList<MicroscopyImageReference>
            implements IModifiable
    {
        private static final long serialVersionUID = 1L;

        public MicroscopyImageReferenceList(Collection<? extends MicroscopyImageReference> c)
        {
            super(c);
        }
    }

    private static class ImageDatasetMetadataList extends ArrayList<ImageDatasetMetadata> implements
            IModifiable
    {
        private static final long serialVersionUID = 1L;

        public ImageDatasetMetadataList(Collection<? extends ImageDatasetMetadata> c)
        {
            super(c);
        }
    }

    private static class DatasetImageRepresentationFormatsList extends
            ArrayList<DatasetImageRepresentationFormats> implements IModifiable
    {
        private static final long serialVersionUID = 1L;

        public DatasetImageRepresentationFormatsList(
                Collection<? extends DatasetImageRepresentationFormats> c)
        {
            super(c);
        }
    }
}

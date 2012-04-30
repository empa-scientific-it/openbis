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
import ch.systemsx.cisd.common.collections.Modifiable;
import ch.systemsx.cisd.openbis.dss.screening.shared.api.v1.IDssServiceRpcScreening;
import ch.systemsx.cisd.openbis.dss.screening.shared.api.v1.LoadImageConfiguration;
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

    public int getMajorVersion()
    {
        return service.getMajorVersion();
    }

    public int getMinorVersion()
    {
        return service.getMinorVersion();
    }

    @SuppressWarnings("deprecation")
    public List<String> listAvailableFeatureNames(String sessionToken,
            List<? extends IFeatureVectorDatasetIdentifier> featureDatasets)
    {
        return service.listAvailableFeatureNames(sessionToken, featureDatasets);
    }

    public List<String> listAvailableFeatureCodes(String sessionToken,
            List<? extends IFeatureVectorDatasetIdentifier> featureDatasets)
    {
        return service.listAvailableFeatureCodes(sessionToken, featureDatasets);
    }

    public List<FeatureInformation> listAvailableFeatures(String sessionToken,
            List<? extends IFeatureVectorDatasetIdentifier> featureDatasets)
    {
        return new FeatureInformationList(service.listAvailableFeatures(sessionToken,
                featureDatasets));
    }

    public List<FeatureVectorDataset> loadFeatures(String sessionToken,
            List<FeatureVectorDatasetReference> featureDatasets, List<String> featureCodes)
    {
        return new FeatureVectorDatasetList(service.loadFeatures(sessionToken, featureDatasets,
                featureCodes));
    }

    public List<FeatureVectorWithDescription> loadFeaturesForDatasetWellReferences(
            String sessionToken, List<FeatureVectorDatasetWellReference> datasetWellReferences,
            List<String> featureCodes)
    {
        return new FeatureVectorWithDescriptionList(service.loadFeaturesForDatasetWellReferences(
                sessionToken, datasetWellReferences, featureCodes));
    }

    public InputStream loadImages(String sessionToken, List<PlateImageReference> imageReferences,
            boolean convertToPng)
    {
        return handleNotSupportedMethod();
    }
    
    
    public List<String> loadImagesBase64(String sessionToken, List<PlateImageReference> imageReferences, boolean convertToPng) {
        return service.loadImagesBase64(sessionToken, imageReferences, convertToPng);       
    }

    public InputStream loadThumbnailImages(String sessionToken,
            List<PlateImageReference> imageReferences)
    {
        return handleNotSupportedMethod();
    }

    public InputStream loadImages(String sessionToken, List<PlateImageReference> imageReferences,
            ImageSize size)
    {
        return handleNotSupportedMethod();
    }

    public InputStream loadImages(String sessionToken, List<PlateImageReference> imageReferences)
    {
        return handleNotSupportedMethod();
    }

    public InputStream loadImages(String sessionToken, IDatasetIdentifier dataSetIdentifier,
            List<WellPosition> wellPositions, String channel, ImageSize thumbnailSizeOrNull)
    {
        return handleNotSupportedMethod();
    }

    public InputStream loadImages(String sessionToken, IDatasetIdentifier dataSetIdentifier,
            String channel, ImageSize thumbnailSizeOrNull)
    {
        return handleNotSupportedMethod();
    }

    public InputStream loadImages(String sessionToken, List<PlateImageReference> imageReferences,
            LoadImageConfiguration configuration)
    {
        return handleNotSupportedMethod();
    }

    public InputStream loadImages(String sessionToken, List<PlateImageReference> imageReferences,
            ImageRepresentationFormat format)
    {
        return handleNotSupportedMethod();
    }

    public InputStream loadImages(String sessionToken, List<PlateImageReference> imageReferences,
            IImageRepresentationFormatSelectionCriterion... criteria)
    {
        return handleNotSupportedMethod();
    }

    public InputStream loadThumbnailImages(String sessionToken,
            IDatasetIdentifier dataSetIdentifier, List<String> channels)
    {
        return handleNotSupportedMethod();
    }

    public List<PlateImageReference> listPlateImageReferences(String sessionToken,
            IDatasetIdentifier dataSetIdentifier, List<WellPosition> wellPositions, String channel)
    {
        return new PlateImageReferenceList(service.listPlateImageReferences(sessionToken,
                dataSetIdentifier, wellPositions, channel));
    }

    public List<PlateImageReference> listPlateImageReferences(String sessionToken,
            IDatasetIdentifier dataSetIdentifier, List<WellPosition> wellPositions,
            List<String> channels)
    {
        return new PlateImageReferenceList(service.listPlateImageReferences(sessionToken,
                dataSetIdentifier, wellPositions, channels));
    }

    public List<MicroscopyImageReference> listImageReferences(String sessionToken,
            IDatasetIdentifier dataSetIdentifier, String channel)
    {
        return new MicroscopyImageReferenceList(service.listImageReferences(sessionToken,
                dataSetIdentifier, channel));
    }

    public List<MicroscopyImageReference> listImageReferences(String sessionToken,
            IDatasetIdentifier dataSetIdentifier, List<String> channels)
    {
        return new MicroscopyImageReferenceList(service.listImageReferences(sessionToken,
                dataSetIdentifier, channels));
    }

    public void saveImageTransformerFactory(String sessionToken,
            List<IDatasetIdentifier> dataSetIdentifiers, String channel,
            IImageTransformerFactory transformerFactory)
    {
        handleNotSupportedMethod();
    }

    public IImageTransformerFactory getImageTransformerFactoryOrNull(String sessionToken,
            List<IDatasetIdentifier> dataSetIdentifiers, String channel)
    {
        return handleNotSupportedMethod();
    }

    public List<ImageDatasetMetadata> listImageMetadata(String sessionToken,
            List<? extends IImageDatasetIdentifier> imageDatasets)
    {
        return new ImageDatasetMetadataList(service.listImageMetadata(sessionToken, imageDatasets));
    }

    public List<DatasetImageRepresentationFormats> listAvailableImageRepresentationFormats(
            String sessionToken, List<? extends IDatasetIdentifier> imageDatasets)
    {
        return new DatasetImageRepresentationFormatsList(
                service.listAvailableImageRepresentationFormats(sessionToken, imageDatasets));
    }

    private <T> T handleNotSupportedMethod()
    {
        throw new UnsupportedOperationException("This method is not supported in JSON API yet");
    }

    /*
     * The collections listed below have been created to help Jackson library embed/detect types of
     * the collection's items during JSON serialization/deserialization. (see
     * http://wiki.fasterxml.com/JacksonPolymorphicDeserialization#A5._Known_Issues)
     */

    private static class FeatureInformationList extends ArrayList<FeatureInformation> implements
            Modifiable
    {
        private static final long serialVersionUID = 1L;

        public FeatureInformationList(Collection<? extends FeatureInformation> c)
        {
            super(c);
        }
    }

    private static class FeatureVectorDatasetList extends ArrayList<FeatureVectorDataset> implements
            Modifiable
    {
        private static final long serialVersionUID = 1L;

        public FeatureVectorDatasetList(Collection<? extends FeatureVectorDataset> c)
        {
            super(c);
        }
    }

    private static class FeatureVectorWithDescriptionList extends
            ArrayList<FeatureVectorWithDescription> implements Modifiable
    {
        private static final long serialVersionUID = 1L;

        public FeatureVectorWithDescriptionList(Collection<? extends FeatureVectorWithDescription> c)
        {
            super(c);
        }
    }

    private static class PlateImageReferenceList extends ArrayList<PlateImageReference> implements
            Modifiable
    {
        private static final long serialVersionUID = 1L;

        public PlateImageReferenceList(Collection<? extends PlateImageReference> c)
        {
            super(c);
        }
    }

    private static class MicroscopyImageReferenceList extends ArrayList<MicroscopyImageReference>
            implements Modifiable
    {
        private static final long serialVersionUID = 1L;

        public MicroscopyImageReferenceList(Collection<? extends MicroscopyImageReference> c)
        {
            super(c);
        }
    }

    private static class ImageDatasetMetadataList extends ArrayList<ImageDatasetMetadata> implements
            Modifiable
    {
        private static final long serialVersionUID = 1L;

        public ImageDatasetMetadataList(Collection<? extends ImageDatasetMetadata> c)
        {
            super(c);
        }
    }

    private static class DatasetImageRepresentationFormatsList extends
            ArrayList<DatasetImageRepresentationFormats> implements Modifiable
    {
        private static final long serialVersionUID = 1L;

        public DatasetImageRepresentationFormatsList(
                Collection<? extends DatasetImageRepresentationFormats> c)
        {
            super(c);
        }
    }
}

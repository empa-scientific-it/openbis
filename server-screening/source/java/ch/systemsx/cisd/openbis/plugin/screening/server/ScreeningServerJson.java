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

package ch.systemsx.cisd.openbis.plugin.screening.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.common.collection.IModifiable;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.IScreeningApiServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.DatasetImageRepresentationFormats;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentImageMetadata;
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
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageRepresentationFormat;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageResolution;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageSize;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.LoadImageConfiguration;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.LogicalImageInfo;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.MaterialTypeIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Plate;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateWellMaterialMapping;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateWellReferenceWithDatasets;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellIdentifier;

/**
 * @author pkupczyk
 */
public class ScreeningServerJson implements IScreeningApiServer
{

    private IScreeningApiServer server;

    public ScreeningServerJson(IScreeningApiServer server)
    {
        if (server == null)
        {
            throw new IllegalArgumentException("Server was null");
        }
        this.server = server;
    }

    @Override
    public int getMajorVersion()
    {
        return server.getMajorVersion();
    }

    @Override
    public int getMinorVersion()
    {
        return server.getMinorVersion();
    }

    @Override
    public String tryLoginScreening(String userId, String userPassword)
            throws IllegalArgumentException
    {
        return server.tryLoginScreening(userId, userPassword);
    }

    @Override
    public void logoutScreening(String sessionToken) throws IllegalArgumentException
    {
        server.logoutScreening(sessionToken);
    }

    @Override
    public List<Plate> listPlates(String sessionToken) throws IllegalArgumentException
    {
        return new PlateList(server.listPlates(sessionToken));
    }

    @Override
    public List<Plate> listPlates(String sessionToken, ExperimentIdentifier experiment)
            throws IllegalArgumentException
    {
        return new PlateList(server.listPlates(sessionToken, experiment));
    }

    @Override
    public List<PlateMetadata> getPlateMetadataList(String sessionToken,
            List<? extends PlateIdentifier> plates) throws IllegalArgumentException
    {
        return new PlateMetadataList(server.getPlateMetadataList(sessionToken, plates));
    }

    @Override
    public List<ExperimentIdentifier> listExperiments(String sessionToken)
    {
        return new ExperimentIdentifierList(server.listExperiments(sessionToken));
    }

    @Override
    public List<ExperimentIdentifier> listExperiments(String sessionToken, String userId)
    {
        return new ExperimentIdentifierList(server.listExperiments(sessionToken, userId));
    }

    @Override
    public List<FeatureVectorDatasetReference> listFeatureVectorDatasets(String sessionToken,
            List<? extends PlateIdentifier> plates) throws IllegalArgumentException
    {
        return new FeatureVectorDatasetReferenceList(server.listFeatureVectorDatasets(sessionToken,
                plates));
    }

    @Override
    public List<ImageDatasetReference> listImageDatasets(String sessionToken,
            List<? extends PlateIdentifier> plates) throws IllegalArgumentException
    {
        return new ImageDatasetReferenceList(server.listImageDatasets(sessionToken, plates));
    }

    @Override
    public List<ImageDatasetReference> listRawImageDatasets(String sessionToken,
            List<? extends PlateIdentifier> plates) throws IllegalArgumentException
    {
        return new ImageDatasetReferenceList(server.listRawImageDatasets(sessionToken, plates));
    }

    @Override
    public List<ImageDatasetReference> listSegmentationImageDatasets(String sessionToken,
            List<? extends PlateIdentifier> plates) throws IllegalArgumentException
    {
        return new ImageDatasetReferenceList(server.listSegmentationImageDatasets(sessionToken,
                plates));
    }

    @Override
    public List<IDatasetIdentifier> getDatasetIdentifiers(String sessionToken,
            List<String> datasetCodes)
    {
        return new IDatasetIdentifierList(server.getDatasetIdentifiers(sessionToken, datasetCodes));
    }

    @Override
    public List<PlateWellReferenceWithDatasets> listPlateWells(String sessionToken,
            ExperimentIdentifier experimentIdentifer, MaterialIdentifier materialIdentifier,
            boolean findDatasets)
    {
        return new PlateWellReferenceWithDatasetsList(server.listPlateWells(sessionToken,
                experimentIdentifer, materialIdentifier, findDatasets));
    }

    @Override
    public List<PlateWellReferenceWithDatasets> listPlateWells(String sessionToken,
            MaterialIdentifier materialIdentifier, boolean findDatasets)
    {
        return new PlateWellReferenceWithDatasetsList(server.listPlateWells(sessionToken,
                materialIdentifier, findDatasets));
    }

    @Override
    public List<WellIdentifier> listPlateWells(String sessionToken, PlateIdentifier plateIdentifier)
    {
        return new WellIdentifierList(server.listPlateWells(sessionToken, plateIdentifier));
    }

    @Override
    public Sample getWellSample(String sessionToken, WellIdentifier wellIdentifier)
    {
        return server.getWellSample(sessionToken, wellIdentifier);
    }

    @Override
    public Sample getPlateSample(String sessionToken, PlateIdentifier plateIdentifier)
    {
        return server.getPlateSample(sessionToken, plateIdentifier);
    }

    @Override
    public List<PlateWellMaterialMapping> listPlateMaterialMapping(String sessionToken,
            List<? extends PlateIdentifier> plates,
            MaterialTypeIdentifier materialTypeIdentifierOrNull)
    {
        return new PlateWellMaterialMappingList(server.listPlateMaterialMapping(sessionToken,
                plates, materialTypeIdentifierOrNull));
    }

    @Override
    public ExperimentImageMetadata getExperimentImageMetadata(String sessionToken,
            ExperimentIdentifier experimentIdentifer)
    {
        return server.getExperimentImageMetadata(sessionToken, experimentIdentifer);
    }

    @Override
    public List<String> listAvailableFeatureCodes(String sessionToken,
            List<? extends IFeatureVectorDatasetIdentifier> featureDatasets)
    {
        return server.listAvailableFeatureCodes(sessionToken, featureDatasets);
    }

    @Override
    public List<FeatureInformation> listAvailableFeatures(String sessionToken,
            List<? extends IFeatureVectorDatasetIdentifier> featureDatasets)
    {
        return new FeatureInformationList(server.listAvailableFeatures(sessionToken,
                featureDatasets));
    }

    @Override
    public List<FeatureVectorDataset> loadFeatures(String sessionToken,
            List<FeatureVectorDatasetReference> featureDatasets, List<String> featureCodes)
    {
        return new FeatureVectorDatasetList(server.loadFeatures(sessionToken, featureDatasets,
                featureCodes));
    }

    @Override
    public List<FeatureVectorWithDescription> loadFeaturesForDatasetWellReferences(
            String sessionToken, List<FeatureVectorDatasetWellReference> datasetWellReferences,
            List<String> featureCodes)
    {
        return new FeatureVectorWithDescriptionList(server.loadFeaturesForDatasetWellReferences(
                sessionToken, datasetWellReferences, featureCodes));
    }

    @Override
    public List<String> loadImagesBase64(String sessionToken,
            List<PlateImageReference> imageReferences, boolean convertToPng)
    {
        return server.loadImagesBase64(sessionToken, imageReferences, convertToPng);
    }

    @Override
    public List<String> loadThumbnailImagesBase64(String sessionToken,
            List<PlateImageReference> imageReferences)
    {
        return server.loadThumbnailImagesBase64(sessionToken, imageReferences);
    }

    @Override
    public List<String> loadImagesBase64(String sessionToken,
            List<PlateImageReference> imageReferences, ImageSize size)
    {
        return server.loadImagesBase64(sessionToken, imageReferences, size);
    }

    @Override
    public List<String> loadImagesBase64(String sessionToken,
            List<PlateImageReference> imageReferences)
    {
        return server.loadImagesBase64(sessionToken, imageReferences);
    }

    @Override
    public List<String> loadImagesBase64(String sessionToken,
            List<PlateImageReference> imageReferences, LoadImageConfiguration configuration)
    {
        return server.loadImagesBase64(sessionToken, imageReferences, configuration);
    }

    @Override
    public List<String> loadImagesBase64(String sessionToken,
            List<PlateImageReference> imageReferences, ImageRepresentationFormat format)
    {
        return server.loadImagesBase64(sessionToken, imageReferences, format);
    }

    @Override
    public List<String> loadImagesBase64(String sessionToken,
            List<PlateImageReference> imageReferences,
            IImageRepresentationFormatSelectionCriterion... criteria)
    {
        return server.loadImagesBase64(sessionToken, imageReferences, criteria);
    }

    @Override
    public List<ImageDatasetMetadata> listImageMetadata(String sessionToken,
            List<? extends IImageDatasetIdentifier> imageDatasets)
    {
        return new ImageDatasetMetadataList(server.listImageMetadata(sessionToken, imageDatasets));
    }

    @Override
    public List<DatasetImageRepresentationFormats> listAvailableImageRepresentationFormats(
            String sessionToken, List<? extends IDatasetIdentifier> imageDatasets)
    {
        return new DatasetImageRepresentationFormatsList(
                server.listAvailableImageRepresentationFormats(sessionToken, imageDatasets));
    }

    @Override
    public List<String> loadPhysicalThumbnailsBase64(String sessionToken,
            List<PlateImageReference> imageReferences, ImageRepresentationFormat format)
    {
        return server.loadPhysicalThumbnailsBase64(sessionToken, imageReferences, format);
    }

    @Override
    public Map<String, LogicalImageInfo> getImageInfo(String sessionToken, List<String> datasetCodes)
    {
        return server.getImageInfo(sessionToken, datasetCodes);
    }

    @Override
    public Map<String, List<ImageResolution>> getImageResolutions(String sessionToken, List<String> datasetCodes)
    {
        Map<String, List<ImageResolution>> map = server.getImageResolutions(sessionToken, datasetCodes);
        for (Map.Entry<String, List<ImageResolution>> entry : map.entrySet())
        {
            map.put(entry.getKey(), new ImageResolutionList(entry.getValue()));
        }
        return map;
    }

    /*
     * The collections listed below have been created to help Jackson library embed/detect types of the collection's items during JSON
     * serialization/deserialization. (see http://wiki.fasterxml.com/JacksonPolymorphicDeserialization#A5._Known_Issues)
     */

    private static class FeatureVectorDatasetReferenceList extends
            ArrayList<FeatureVectorDatasetReference> implements IModifiable
    {
        private static final long serialVersionUID = 1L;

        public FeatureVectorDatasetReferenceList(
                Collection<? extends FeatureVectorDatasetReference> c)
        {
            super(c);
        }
    }

    private static class PlateList extends ArrayList<Plate> implements IModifiable
    {
        private static final long serialVersionUID = 1L;

        public PlateList(Collection<? extends Plate> c)
        {
            super(c);
        }
    }

    private static class PlateMetadataList extends ArrayList<PlateMetadata> implements IModifiable
    {
        private static final long serialVersionUID = 1L;

        public PlateMetadataList(Collection<? extends PlateMetadata> c)
        {
            super(c);
        }
    }

    private static class ExperimentIdentifierList extends ArrayList<ExperimentIdentifier> implements
            IModifiable
    {
        private static final long serialVersionUID = 1L;

        public ExperimentIdentifierList(Collection<? extends ExperimentIdentifier> c)
        {
            super(c);
        }
    }

    private static class ImageDatasetReferenceList extends ArrayList<ImageDatasetReference>
            implements IModifiable
    {
        private static final long serialVersionUID = 1L;

        public ImageDatasetReferenceList(Collection<? extends ImageDatasetReference> c)
        {
            super(c);
        }
    }

    private static class IDatasetIdentifierList extends ArrayList<IDatasetIdentifier> implements
            IModifiable
    {
        private static final long serialVersionUID = 1L;

        public IDatasetIdentifierList(Collection<? extends IDatasetIdentifier> c)
        {
            super(c);
        }
    }

    private static class PlateWellReferenceWithDatasetsList extends
            ArrayList<PlateWellReferenceWithDatasets> implements IModifiable
    {
        private static final long serialVersionUID = 1L;

        public PlateWellReferenceWithDatasetsList(
                Collection<? extends PlateWellReferenceWithDatasets> c)
        {
            super(c);
        }
    }

    private static class WellIdentifierList extends ArrayList<WellIdentifier> implements
            IModifiable
    {
        private static final long serialVersionUID = 1L;

        public WellIdentifierList(Collection<? extends WellIdentifier> c)
        {
            super(c);
        }
    }

    private static class PlateWellMaterialMappingList extends ArrayList<PlateWellMaterialMapping>
            implements IModifiable
    {
        private static final long serialVersionUID = 1L;

        public PlateWellMaterialMappingList(Collection<? extends PlateWellMaterialMapping> c)
        {
            super(c);
        }
    }

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

    private static class ImageResolutionList extends
            ArrayList<ImageResolution> implements IModifiable
    {
        private static final long serialVersionUID = 1L;

        public ImageResolutionList(
                Collection<? extends ImageResolution> c)
        {
            super(c);
        }
    }

}

/*
 * Copyright 2008 ETH Zuerich, CISD
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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerLogger;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeAndLabel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.screening.shared.IScreeningServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.IScreeningApiServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.DatasetImageRepresentationFormats;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentImageMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureInformation;
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
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageSize;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.LoadImageConfiguration;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.MaterialTypeIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Plate;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateWellMaterialMapping;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateWellReferenceWithDatasets;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.AnalysisProcedures;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ExperimentFeatureVectorSummary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureVectorDataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureVectorValues;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetEnrichedReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageResolution;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageSampleContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.LogicalImageInfo;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialReplicaFeatureSummaryResult;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialSimpleFeatureVectorSummary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.NewLibrary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImages;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellReplicaImage;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.AnalysisProcedureCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.MaterialFeaturesManyExpCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.MaterialFeaturesOneExpCriteria;

/**
 * The <i>screening</i> specific {@link AbstractServerLogger} extension.
 * 
 * @author Tomasz Pylak
 */
final class ScreeningServerLogger extends AbstractServerLogger implements IScreeningServer,
        IScreeningApiServer
{
    ScreeningServerLogger(final ISessionManager<Session> sessionManager,
            IInvocationLoggerContext context)
    {
        super(sessionManager, context);
    }

    @Override
    public final SampleParentWithDerived getSampleInfo(final String sessionToken,
            final TechId sampleId)
    {
        logAccess(sessionToken, "getSampleInfo", "ID(%s)", sampleId);
        return null;
    }

    public void registerSample(final String sessionToken, final NewSample newSample,
            final Collection<NewAttachment> attachments)
    {
        logTracking(sessionToken, "registerSample", "SAMPLE_TYPE(%s) SAMPLE(%s) ATTACHMENTS(%s)",
                newSample.getSampleType(), newSample.getIdentifier(), attachments.size());
    }

    @Override
    public PlateContent getPlateContent(String sessionToken, TechId plateId)
    {
        logAccess(sessionToken, "getPlateContent", "PLATE(%s)", plateId.getId());
        return null;
    }

    @Override
    public FeatureVectorDataset getFeatureVectorDataset(String sessionToken,
            DatasetReference dataset, CodeAndLabel featureName)
    {
        logAccess(sessionToken, "getFeatureVectorDataset", "DATA_SET(%s) FEATURE(%s)",
                dataset.getCode(), featureName);
        return null;
    }

    @Override
    public PlateImages getPlateContentForDataset(String sessionToken, TechId datasetId)
    {
        logAccess(sessionToken, "getPlateContentForDataset", "DATASET(%s)", datasetId.getId());
        return null;
    }

    @Override
    public List<WellContent> listPlateWells(String sessionToken, WellSearchCriteria materialCriteria)
    {
        logAccess(sessionToken, "listPlateWells", "criteria(%s)", materialCriteria);
        return null;
    }

    @Override
    public List<WellReplicaImage> listWellImages(String sessionToken, TechId materialId,
            TechId experimentId)
    {
        logAccess(sessionToken, "listWellImages", "material(%s) experiment(%s)", materialId,
                experimentId);
        return null;
    }

    @Override
    public List<Material> listMaterials(String sessionToken, WellSearchCriteria materialCriteria)
    {
        logAccess(sessionToken, "listMaterials", "criteria(%s)", materialCriteria);
        return null;
    }

    @Override
    public FeatureVectorValues getWellFeatureVectorValues(String sessionToken, String datasetCode,
            String datastoreCode, WellLocation wellLocation)
    {
        logAccess(sessionToken, "getWellFeatureVectorValues", "dataset(%s) well(%s)", datasetCode,
                datasetCode);
        return null;
    }

    @Override
    public LogicalImageInfo getImageDatasetInfo(String sessionToken, String datasetCode,
            String datastoreCode, WellLocation wellLocationOrNull)
    {
        logAccess(sessionToken, "getImageDatasetInfo", "dataset(%s) well(%s)", datasetCode,
                wellLocationOrNull);
        return null;
    }

    @Override
    public Map<String, ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.LogicalImageInfo> getImageInfo(String sessionToken,
            List<String> datasetCodes)
    {
        logAccess(sessionToken, "getImageInfo", "datasets(%s)", abbreviate(datasetCodes));
        return null;
    }

    @Override
    public Map<String, List<ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageResolution>> getImageResolutions(String sessionToken,
            List<String> datasetCodes)
    {
        logAccess(sessionToken, "getImageResolutions", "datasets(%s)", abbreviate(datasetCodes));
        return null;
    }

    @Override
    public ImageDatasetEnrichedReference getImageDatasetReference(String sessionToken,
            String datasetCode, String datastoreCode)
    {
        logAccess(sessionToken, "getImageDatasetReference", "dataset(%s) datastore(%s)",
                datasetCode, datastoreCode);
        return null;
    }

    @Override
    public List<ImageResolution> getImageDatasetResolutions(String sessionToken,
            String datasetCode, String datastoreCode)
    {
        logAccess(sessionToken, "getImageDatasetResolutions", "dataset(%s) datastore(%s)",
                datasetCode, datastoreCode);
        return null;
    }

    @Override
    public ImageSampleContent getImageDatasetInfosForSample(String sessionToken, TechId sampleId,
            WellLocation wellLocationOrNull)
    {
        logAccess(sessionToken, "getImageDatasetInfosForSample", "sample(%s) well(%s)", sampleId,
                wellLocationOrNull);
        return null;
    }

    @Override
    public AbstractExternalData getDataSetInfo(String sessionToken, TechId datasetId)
    {
        logAccess(sessionToken, "getDataSetInfo", "datasetId(%s)", datasetId.getId());
        return null;
    }

    @Override
    public Material getMaterialInfo(String sessionToken, TechId materialId)
    {
        logAccess(sessionToken, "getMaterialInfo", "datasetId(%s)", materialId.getId());
        return null;
    }

    public TableModel loadImageAnalysisForExperiment(String sessionToken, TechId experimentId)
    {
        logAccess(sessionToken, "loadImageAnalysisForExperiment", "EXPERIMENT(%s)",
                experimentId.getId());
        return null;
    }

    public TableModel loadImageAnalysisForPlate(String sessionToken, TechId plateId)
    {
        logAccess(sessionToken, "loadImageAnalysisForPlate", "PLATE(%s)", plateId.getId());
        return null;
    }

    @Override
    public Vocabulary getVocabulary(String sessionToken, String code) throws UserFailureException
    {
        logAccess(sessionToken, "getVocabulary", "CODE(%s)", code);
        return null;
    }

    // --- IScreeningApiServer

    @Override
    public void logoutScreening(String sessionToken)
    {
        // No logging because already done by the session manager
    }

    @Override
    public String tryLoginScreening(String userId, String userPassword)
    {
        // No logging because already done by the session manager
        return null;
    }

    @Override
    public List<FeatureVectorDatasetReference> listFeatureVectorDatasets(String sessionToken,
            List<? extends PlateIdentifier> plates)
    {
        logAccess(sessionToken, "listFeatureVectorDatasets", "#plates: %s", plates.size());
        return null;
    }

    @Override
    public List<ImageDatasetReference> listImageDatasets(String sessionToken,
            List<? extends PlateIdentifier> plates)
    {
        logAccess(sessionToken, "listImageDatasets", "#plates: %s", plates.size());
        return null;
    }

    @Override
    public List<ImageDatasetReference> listRawImageDatasets(String sessionToken,
            List<? extends PlateIdentifier> plates) throws IllegalArgumentException
    {
        logAccess(sessionToken, "listRawImageDatasets", "#plates: %s", plates.size());
        return null;
    }

    @Override
    public List<ImageDatasetReference> listSegmentationImageDatasets(String sessionToken,
            List<? extends PlateIdentifier> plates) throws IllegalArgumentException
    {
        logAccess(sessionToken, "listSegmentationImageDatasets", "#plates: %s", plates.size());
        return null;
    }

    @Override
    public List<PlateWellReferenceWithDatasets> listPlateWells(
            String sessionToken,
            ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentIdentifier experimentIdentifer,
            MaterialIdentifier materialIdentifier, boolean findDatasets)
    {
        logAccess(sessionToken, "listPlateWells", "experiment: %s, material: %s",
                experimentIdentifer, materialIdentifier);
        return null;
    }

    @Override
    public List<PlateWellReferenceWithDatasets> listPlateWells(String sessionToken,
            MaterialIdentifier materialIdentifier, boolean findDatasets)
    {
        logAccess(sessionToken, "listPlateWells", "material: %s", materialIdentifier);
        return null;
    }

    @Override
    public List<WellIdentifier> listPlateWells(String sessionToken, PlateIdentifier plateIdentifier)
    {
        logAccess(sessionToken, "listPlateWells", "plate: %s", plateIdentifier);
        return null;
    }

    @Override
    public Sample getWellSample(String sessionToken, WellIdentifier wellIdentifier)
    {
        logAccess(sessionToken, "getWellSample", "%s", wellIdentifier);
        return null;
    }

    @Override
    public Sample getPlateSample(String sessionToken, PlateIdentifier plateIdentifier)
    {
        logAccess(sessionToken, "getPlateSample", "%s", plateIdentifier);
        return null;
    }

    @Override
    public List<Plate> listPlates(String sessionToken)
    {
        logAccess(sessionToken, "listPlates");
        return null;
    }

    @Override
    public List<Plate> listPlates(String sessionToken, ExperimentIdentifier experiment)
            throws IllegalArgumentException
    {
        logAccess(sessionToken, "listPlates", "%s", experiment);
        return null;
    }

    @Override
    public List<ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentIdentifier> listExperiments(
            String sessionToken)
    {
        logAccess(sessionToken, "listExperiments");
        return null;
    }

    @Override
    public List<ExperimentIdentifier> listExperiments(String sessionToken, String userId)
    {
        logAccess(sessionToken, "listExperiments", "user(%s)", userId);
        return null;
    }

    @Override
    public List<IDatasetIdentifier> getDatasetIdentifiers(String sessionToken,
            List<String> datasetCodes)
    {
        logAccess(sessionToken, "getDatasetIdentifiers", "datasets(%s)", datasetCodes);
        return null;
    }

    @Override
    public List<PlateWellMaterialMapping> listPlateMaterialMapping(String sessionToken,
            List<? extends PlateIdentifier> plates,
            MaterialTypeIdentifier materialTypeIdentifierOrNull)
    {
        if (materialTypeIdentifierOrNull != null)
        {
            logAccess(sessionToken, "listPlateMaterialMapping", "plates(%s), materialType(%s)",
                    plates, materialTypeIdentifierOrNull);
        } else
        {
            logAccess(sessionToken, "listPlateMaterialMapping", "plates(%s)", plates);
        }
        return null;
    }

    @Override
    public List<Material> listExperimentMaterials(String sessionToken, TechId experimentId,
            MaterialType materialType)
    {
        return null;
    }

    @Override
    public int getMajorVersion()
    {
        return MAJOR_VERSION;
    }

    @Override
    public int getMinorVersion()
    {
        return ScreeningServer.MINOR_VERSION;
    }

    @Override
    public void registerLibraries(String sessionToken, List<NewLibrary> newLibraries)
    {
        logAccess(sessionToken, "registerLibraries", "LIBRARIES(%s)", newLibraries != null ? newLibraries.size() : 0);
    }

    @Override
    public void registerLibrariesAsync(String sessionToken, List<NewLibrary> newLibraries, String userEmail)
    {
        logAccess(sessionToken, "registerLibrariesAsync", "LIBRARIES(%s) EMAIL(%s)", newLibraries != null ? newLibraries.size() : 0, userEmail);
    }

    @Override
    public ExperimentFeatureVectorSummary getExperimentFeatureVectorSummary(String sessionToken,
            TechId experimentId, AnalysisProcedureCriteria analysisProcedureCriteria)
    {
        logAccess(sessionToken, "getExperimentFeatureVectorSummary",
                "sessionToken(%s), experimentId(%s), analysisProcedureCriteria(%s)", sessionToken,
                experimentId, analysisProcedureCriteria);
        return null;
    }

    @Override
    public MaterialReplicaFeatureSummaryResult getMaterialFeatureVectorSummary(String sessionToken,
            MaterialFeaturesOneExpCriteria criteria)
    {
        logAccess(sessionToken, "getFeatureVectorReplicaSummary",
                "sessionToken(%s), experimentId(%s), materialId(%s) analysisProcedure(%s)",
                sessionToken, criteria.getExperimentId(), criteria.getMaterialId(),
                criteria.getAnalysisProcedureCriteria());
        return null;
    }

    @Override
    public List<MaterialSimpleFeatureVectorSummary> getMaterialFeatureVectorsFromAllExperiments(
            String sessionToken, MaterialFeaturesManyExpCriteria criteria)
    {
        logAccess(sessionToken, "getMaterialFeatureVectorsFromAllExperiments",
                "sessionToken(%s), materialId(%s), experiments(%s) analysisProcedure(%s)",
                sessionToken, criteria.getMaterialId(), criteria.getExperimentSearchCriteria(),
                criteria.getAnalysisProcedureCriteria());
        return null;
    }

    @Override
    public AnalysisProcedures listNumericalDatasetsAnalysisProcedures(String sessionToken,
            ExperimentSearchCriteria experimentSearchCriteria)
    {
        logAccess(sessionToken, "listAnalysisProcedures",
                "sessionToken(%s), experimentSearchCriteria(%s)", sessionToken,
                experimentSearchCriteria);
        return null;
    }

    @Override
    public List<PlateMetadata> getPlateMetadataList(String sessionToken,
            List<? extends PlateIdentifier> plates) throws IllegalArgumentException
    {
        logAccess(sessionToken, "getPlateMetadataList", "#plates: %s", plates.size());
        return null;
    }

    @Override
    public ExperimentImageMetadata getExperimentImageMetadata(String sessionToken,
            ExperimentIdentifier experimentIdentifer)
    {
        logAccess(sessionToken, "getExperimentImageMetadata", "experimentIdentifer(%s)",
                experimentIdentifer);
        return null;
    }

    @Override
    public List<String> listAvailableFeatureCodes(String sessionToken,
            List<? extends IFeatureVectorDatasetIdentifier> featureDatasets)
    {
        logAccess(sessionToken, "listAvailableFeatureCodes", "#featureDatasets(%s)",
                size(featureDatasets));
        return null;
    }

    @Override
    public List<FeatureInformation> listAvailableFeatures(String sessionToken,
            List<? extends IFeatureVectorDatasetIdentifier> featureDatasets)
    {
        logAccess(sessionToken, "listAvailableFeatures", "#featureDatasets(%s)",
                size(featureDatasets));
        return null;
    }

    @Override
    public List<ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDataset> loadFeatures(
            String sessionToken, List<FeatureVectorDatasetReference> featureDatasets,
            List<String> featureCodes)
    {
        logAccess(sessionToken, "loadFeatures", "#featureDatasets(%s), featureCodes(%s)",
                size(featureDatasets), abbreviate(featureCodes));
        return null;
    }

    @Override
    public List<FeatureVectorWithDescription> loadFeaturesForDatasetWellReferences(
            String sessionToken, List<FeatureVectorDatasetWellReference> datasetWellReferences,
            List<String> featureCodes)
    {
        logAccess(sessionToken, "loadFeaturesForDatasetWellReferences",
                "#datasetWellReferences(%s), featureCodes(%s)", size(datasetWellReferences),
                abbreviate(featureCodes));
        return null;
    }

    @Override
    public List<String> loadImagesBase64(String sessionToken,
            List<PlateImageReference> imageReferences, boolean convertToPng)
    {
        logAccess(sessionToken, "loadImagesBase64", "#imageReferences(%s), convertToPng(%s)",
                size(imageReferences), convertToPng);
        return null;
    }

    @Override
    public List<String> loadThumbnailImagesBase64(String sessionToken,
            List<PlateImageReference> imageReferences)
    {
        logAccess(sessionToken, "loadThumbnailImagesBase64", "#imageReferences(%s)",
                size(imageReferences));
        return null;
    }

    @Override
    public List<String> loadImagesBase64(String sessionToken,
            List<PlateImageReference> imageReferences, ImageSize size)
    {
        logAccess(sessionToken, "loadImagesBase64", "#imageReferences(%s), size(%s)",
                size(imageReferences), size);
        return null;
    }

    @Override
    public List<String> loadImagesBase64(String sessionToken,
            List<PlateImageReference> imageReferences)
    {
        logAccess(sessionToken, "loadImagesBase64", "#imageReferences(%s)", size(imageReferences));
        return null;
    }

    @Override
    public List<String> loadImagesBase64(String sessionToken,
            List<PlateImageReference> imageReferences, LoadImageConfiguration configuration)
    {
        logAccess(sessionToken, "loadImagesBase64", "#imageReferences(%s), configuration(%s)",
                size(imageReferences), configuration);
        return null;
    }

    @Override
    public List<String> loadImagesBase64(String sessionToken,
            List<PlateImageReference> imageReferences, ImageRepresentationFormat format)
    {
        logAccess(sessionToken, "loadImagesBase64", "#imageReferences(%s), format(%s)",
                size(imageReferences), format);
        return null;
    }

    @Override
    public List<String> loadImagesBase64(String sessionToken,
            List<PlateImageReference> imageReferences,
            IImageRepresentationFormatSelectionCriterion... criteria)
    {
        logAccess(sessionToken, "loadImagesBase64", "#imageReferences(%s), #criteria(%s)",
                size(imageReferences), size(criteria));
        return null;
    }

    @Override
    public List<ImageDatasetMetadata> listImageMetadata(String sessionToken,
            List<? extends IImageDatasetIdentifier> imageDatasets)
    {
        logAccess(sessionToken, "listImageMetadata", "#imageDatasets(%s)", size(imageDatasets));
        return null;
    }

    @Override
    public List<DatasetImageRepresentationFormats> listAvailableImageRepresentationFormats(
            String sessionToken, List<? extends IDatasetIdentifier> imageDatasets)
    {
        logAccess(sessionToken, "listAvailableImageRepresentationFormats", "#imageDatasets(%s)",
                size(imageDatasets));
        return null;
    }

    @Override
    public List<String> loadPhysicalThumbnailsBase64(String sessionToken,
            List<PlateImageReference> imageReferences, ImageRepresentationFormat format)
    {
        logAccess(sessionToken, "loadPhysicalThumbnailsBase64", "#imageReferences(%s), format(%s)",
                size(imageReferences), format);
        return null;
    }

}

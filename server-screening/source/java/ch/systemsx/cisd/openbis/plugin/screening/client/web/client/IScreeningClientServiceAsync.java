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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.IClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListMaterialDisplayCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchRegistrationResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeAndLabel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.AnalysisProcedures;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureVectorDataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureVectorValues;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetEnrichedReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageResolution;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageSampleContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.LibraryRegistrationInfo;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.LogicalImageInfo;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialFeatureVectorSummary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialReplicaFeatureSummary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialSimpleFeatureVectorSummary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImages;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellReplicaImage;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.AnalysisProcedureCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.MaterialFeaturesManyExpCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.MaterialFeaturesOneExpCriteria;

/**
 * Service interface for the <i>screening</i> <i>GWT</i> client.
 * 
 * @author Christian Ribeaud
 */
public interface IScreeningClientServiceAsync extends IClientServiceAsync
{

    /** @see IScreeningClientService#getSampleGenerationInfo(TechId) */
    public void getSampleGenerationInfo(final TechId sampleId,
            final AsyncCallback<SampleParentWithDerived> callback);

    /** @see IScreeningClientService#getPlateContent(TechId) */
    public void getPlateContent(TechId sampleId, final AsyncCallback<PlateContent> callback);

    /** @see IScreeningClientService#getFeatureVectorDataset(DatasetReference, CodeAndLabel) */
    public void getFeatureVectorDataset(DatasetReference dataset, CodeAndLabel featureName,
            AsyncCallback<FeatureVectorDataset> callback);

    /** @see IScreeningClientService#getWellFeatureVectorValues(String, String, WellLocation) */
    public void getWellFeatureVectorValues(String datasetCode, String datastoreCode,
            WellLocation location, AsyncCallback<FeatureVectorValues> callback);

    /** @see IScreeningClientService#getPlateContentForDataset(TechId) */
    public void getPlateContentForDataset(TechId datasetId,
            AsyncCallback<PlateImages> createDisplayPlateCallback);

    /** @see IScreeningClientService#getDataSetInfo(TechId) */
    public void getDataSetInfo(TechId datasetTechId, AsyncCallback<AbstractExternalData> callback);

    /** @see IScreeningClientService#getMaterialInfo(TechId) */
    public void getMaterialInfo(TechId materialTechId, AsyncCallback<Material> callback)
            throws UserFailureException;

    /**
     * @see IScreeningClientService#listPlateWells(IResultSetConfig, WellSearchCriteria)
     */
    public void listPlateWells(
            IResultSetConfig<String, TableModelRowWithObject<WellContent>> gridCriteria,
            WellSearchCriteria materialCriteria,
            AsyncCallback<TypedTableResultSet<WellContent>> callback);

    /**
     * @see IScreeningClientService#listWellImages(TechId, TechId)
     */
    public void listWellImages(TechId materialId, TechId experimentId,
            AsyncCallback<List<WellReplicaImage>> callback);

    /**
     * @see IScreeningClientService#prepareExportPlateWells(TableExportCriteria)
     */
    public void prepareExportPlateWells(
            TableExportCriteria<TableModelRowWithObject<WellContent>> criteria,
            AsyncCallback<String> callback);

    /**
     * @see IScreeningClientService#listMaterials(IResultSetConfig, WellSearchCriteria)
     */
    public void listMaterials(
            IResultSetConfig<String, TableModelRowWithObject<Material>> gridCriteria,
            WellSearchCriteria materialCriteria,
            AsyncCallback<TypedTableResultSet<Material>> callback);

    /**
     * @see IScreeningClientService#prepareExportMaterials(TableExportCriteria)
     */
    public void prepareExportMaterials(
            TableExportCriteria<TableModelRowWithObject<Material>> criteria,
            AsyncCallback<String> callback);

    /**
     * @see IScreeningClientService#listPlateMetadata(IResultSetConfig, TechId)
     */
    public void listPlateMetadata(
            IResultSetConfig<String, TableModelRowWithObject<WellMetadata>> resultSetConfig,
            TechId sampleId, AsyncCallback<TypedTableResultSet<WellMetadata>> callback);

    /**
     * @see IScreeningClientService#prepareExportPlateMetadata(TableExportCriteria)
     */
    public void prepareExportPlateMetadata(
            TableExportCriteria<TableModelRowWithObject<WellMetadata>> exportCriteria,
            AsyncCallback<String> callback);

    /**
     * @see IScreeningClientService#getImageDatasetInfo(String, String, WellLocation)
     */
    public void getImageDatasetInfo(String datasetCode, String datastoreCode,
            WellLocation wellLocationOrNull, AsyncCallback<LogicalImageInfo> abstractAsyncCallback);

    /**
     * @see IScreeningClientService#getImageDatasetReference(String, String)
     */
    public void getImageDatasetReference(String datasetCode, String datastoreCode,
            AsyncCallback<ImageDatasetEnrichedReference> abstractAsyncCallback);

    /**
     * @see IScreeningClientService#getImageDatasetResolutions(String, String)
     */
    public void getImageDatasetResolutions(String datasetCode, String datastoreCode,
            AsyncCallback<List<ImageResolution>> abstractAsyncCallback);

    /**
     * @see IScreeningClientService#getImageDatasetInfosForSample(TechId, WellLocation)
     */
    public void getImageDatasetInfosForSample(TechId sampleId, WellLocation wellLocationOrNull,
            AsyncCallback<ImageSampleContent> abstractAsyncCallback);

    /**
     * @see IScreeningClientService#registerLibrary(LibraryRegistrationInfo, String, boolean, String)
     */
    public void registerLibrary(LibraryRegistrationInfo newLibraryInfo,
            String sessionKey, boolean async, String userEmail,
            final AsyncCallback<List<BatchRegistrationResult>> asyncCallback);

    /**
     * @see IScreeningClientService#getPlateGeometryVocabulary()
     */
    public void getPlateGeometryVocabulary(AsyncCallback<Vocabulary> callback);

    /**
     * @see IScreeningClientService#listExperimentMaterials(TechId, ListMaterialDisplayCriteria)
     */
    public void listExperimentMaterials(TechId experimentId, ListMaterialDisplayCriteria criteria,
            AsyncCallback<TypedTableResultSet<Material>> callback);

    /**
     * @see IScreeningClientService#listExperimentFeatureVectorSummary(IResultSetConfig, TechId, AnalysisProcedureCriteria)
     */
    public void listExperimentFeatureVectorSummary(
            IResultSetConfig<String, TableModelRowWithObject<MaterialFeatureVectorSummary>> resultSetConfig,
            TechId experimentId, AnalysisProcedureCriteria analysisProcedureCriteria,
            AsyncCallback<TypedTableResultSet<MaterialFeatureVectorSummary>> callback);

    /**
     * @see IScreeningClientService#prepareExportFeatureVectorSummary(TableExportCriteria)
     */
    public void prepareExportFeatureVectorSummary(
            TableExportCriteria<TableModelRowWithObject<MaterialFeatureVectorSummary>> criteria,
            AsyncCallback<String> callback);

    /**
     * @see IScreeningClientService#listMaterialReplicaFeatureSummary(IResultSetConfig, MaterialFeaturesOneExpCriteria)
     */
    public void listMaterialReplicaFeatureSummary(
            IResultSetConfig<String, TableModelRowWithObject<MaterialReplicaFeatureSummary>> resultSetConfig,
            MaterialFeaturesOneExpCriteria criteria,
            AsyncCallback<TypedTableResultSet<MaterialReplicaFeatureSummary>> callback);

    /**
     * @see IScreeningClientService#prepareExportMaterialReplicaFeatureSummary(TableExportCriteria)
     */
    public void prepareExportMaterialReplicaFeatureSummary(
            TableExportCriteria<TableModelRowWithObject<MaterialReplicaFeatureSummary>> criteria,
            AsyncCallback<String> callback);

    /**
     * @see IScreeningClientService#listMaterialFeaturesFromAllExperiments(IResultSetConfig, MaterialFeaturesManyExpCriteria)
     */
    public void listMaterialFeaturesFromAllExperiments(
            IResultSetConfig<String, TableModelRowWithObject<MaterialSimpleFeatureVectorSummary>> resultSetConfig,
            MaterialFeaturesManyExpCriteria criteria,
            AsyncCallback<TypedTableResultSet<MaterialSimpleFeatureVectorSummary>> callback);

    /**
     * @see IScreeningClientService#prepareExportMaterialFeaturesFromAllExperiments(TableExportCriteria)
     */
    public void prepareExportMaterialFeaturesFromAllExperiments(
            TableExportCriteria<TableModelRowWithObject<MaterialSimpleFeatureVectorSummary>> criteria,
            AsyncCallback<String> callback);

    /**
     * @see IScreeningClientService#listNumericalDatasetsAnalysisProcedures(ExperimentSearchCriteria)
     */
    public void listNumericalDatasetsAnalysisProcedures(
            ExperimentSearchCriteria experimentSearchCriteria,
            AsyncCallback<AnalysisProcedures> callback);

}

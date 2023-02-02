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

import ch.systemsx.cisd.openbis.generic.client.web.client.IClientService;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientService;
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
 * <p>
 * Each method should declare throwing {@link UserFailureException}. The authorisation framework can throw it when the user has insufficient
 * privileges. If it is not marked, the GWT client will report unexpected exception.
 * </p>
 * 
 * @author Tomasz Pylak
 */
public interface IScreeningClientService extends IClientService
{

    /**
     * For given {@link TechId} returns corresponding {@link SampleParentWithDerived}.
     */
    public SampleParentWithDerived getSampleGenerationInfo(final TechId sampleId)
            throws UserFailureException;

    /**
     * For given {@link TechId} returns corresponding {@link AbstractExternalData}.
     */
    public AbstractExternalData getDataSetInfo(TechId datasetTechId) throws UserFailureException;

    /**
     * For given {@link TechId} returns corresponding {@link Material}.
     */
    public Material getMaterialInfo(TechId materialTechId) throws UserFailureException;

    /**
     * Fetches information about wells on a plate and their content.
     */
    public PlateContent getPlateContent(TechId sampleId) throws UserFailureException;

    /**
     * Fetches feature vector of specified dataset with one feature specified by name.
     */
    public FeatureVectorDataset getFeatureVectorDataset(DatasetReference dataset,
            CodeAndLabel featureName);

    /**
     * Fetches feature vector of specified dataset with one feature specified by name.
     */
    public FeatureVectorValues getWellFeatureVectorValues(String datasetCode, String datastoreCode,
            WellLocation location);

    /**
     * Fetches information about a plate: metadata and images for wells. The specified dataset is supposed to be in BDS-HCS format.
     */
    public PlateImages getPlateContentForDataset(TechId datasetId) throws UserFailureException;

    /**
     * @return well locations which belong to a parent plate connected to a specified experiment(s) and have specified material(s) inside.
     */
    public TypedTableResultSet<WellContent> listPlateWells(
            IResultSetConfig<String, TableModelRowWithObject<WellContent>> gridCriteria,
            WellSearchCriteria materialCriteria) throws UserFailureException;

    public String prepareExportPlateWells(
            TableExportCriteria<TableModelRowWithObject<WellContent>> criteria)
            throws UserFailureException;

    /**
     * Finds wells containing the specified material and belonging to the specified experiment. Loads wells metadata and single image dataset for each
     * well. If there are many image datasets for the well, all but the first one are ignored. If there is no image dataset for the well, the whole
     * well is ignored.
     */
    public List<WellReplicaImage> listWellImages(TechId materialId, TechId experimentId)
            throws UserFailureException;

    /**
     * @return materials with codes or properties matching to the query. If the experiment is specified, only materials inside well locations
     *         connected through the plate to this specified experiment(s) will be returned.
     */
    public TypedTableResultSet<Material> listMaterials(
            IResultSetConfig<String, TableModelRowWithObject<Material>> gridCriteria,
            WellSearchCriteria materialCriteria) throws UserFailureException;

    public String prepareExportMaterials(
            TableExportCriteria<TableModelRowWithObject<Material>> criteria)
            throws UserFailureException;

    /**
     * Returns {@link TypedTableResultSet} containing plate metadata.
     */
    public TypedTableResultSet<WellMetadata> listPlateMetadata(
            IResultSetConfig<String, TableModelRowWithObject<WellMetadata>> resultSetConfig,
            TechId sampleId) throws UserFailureException;

    /**
     * Lists {@link Material}s of specified type in experiment with specified id.
     */
    public TypedTableResultSet<Material> listExperimentMaterials(TechId experimentId,
            ListMaterialDisplayCriteria criteria) throws UserFailureException;

    /**
     * Like {@link ICommonClientService#prepareExportSamples(TableExportCriteria)}, but for TypedTableResultSet.
     */
    public String prepareExportPlateMetadata(
            TableExportCriteria<TableModelRowWithObject<WellMetadata>> exportCriteria)
            throws UserFailureException;

    /**
     * Returns information about logical image in the given dataset. In HCS case the well location should be specified.
     */
    public LogicalImageInfo getImageDatasetInfo(String datasetCode, String datastoreCode,
            WellLocation wellLocationOrNull) throws UserFailureException;

    /**
     * Returns information about image dataset for a given image dataset. Used to refresh information about the dataset.
     */
    public ImageDatasetEnrichedReference getImageDatasetReference(String datasetCode,
            String datastoreCode);

    /**
     * Returns information about available image resolutions for a given image dataset.
     */
    public List<ImageResolution> getImageDatasetResolutions(String datasetCode, String datastoreCode);

    /**
     * Loads information about datasets connected to specified sample (microscopy) or a container sample (HCS). In particular loads the logical images
     * in datasets belonging to the specified sample (restricted to one well in HCS case).
     */
    public ImageSampleContent getImageDatasetInfosForSample(TechId sampleId,
            WellLocation wellLocationOrNull);

    /**
     * Registers a new library.
     */
    public List<BatchRegistrationResult> registerLibrary(LibraryRegistrationInfo newLibraryInfo,
            String sessionKey, boolean async, String userEmail) throws UserFailureException;

    /**
     * Returns plate geometry vocabulary.
     */
    public Vocabulary getPlateGeometryVocabulary() throws UserFailureException;

    /**
     * Return the selected {@link MaterialFeatureVectorSummary}-s for a given experiment.
     */
    public TypedTableResultSet<MaterialFeatureVectorSummary> listExperimentFeatureVectorSummary(
            IResultSetConfig<String, TableModelRowWithObject<MaterialFeatureVectorSummary>> resultSetConfig,
            TechId experimentId, AnalysisProcedureCriteria analysisProcedureCriteria)
            throws UserFailureException;

    public String prepareExportFeatureVectorSummary(
            TableExportCriteria<TableModelRowWithObject<MaterialFeatureVectorSummary>> criteria)
            throws UserFailureException;

    /**
     * Return the selected {@link MaterialReplicaFeatureSummary}-s for a given experiment and material.
     */
    public TypedTableResultSet<MaterialReplicaFeatureSummary> listMaterialReplicaFeatureSummary(
            IResultSetConfig<String, TableModelRowWithObject<MaterialReplicaFeatureSummary>> resultSetConfig,
            MaterialFeaturesOneExpCriteria criteria) throws UserFailureException;

    public String prepareExportMaterialReplicaFeatureSummary(
            TableExportCriteria<TableModelRowWithObject<MaterialReplicaFeatureSummary>> criteria)
            throws UserFailureException;

    /**
     * Return material feature vector summaries from all experiments for a give material tech id.
     */
    public TypedTableResultSet<MaterialSimpleFeatureVectorSummary> listMaterialFeaturesFromAllExperiments(
            IResultSetConfig<String, TableModelRowWithObject<MaterialSimpleFeatureVectorSummary>> resultSetConfig,
            MaterialFeaturesManyExpCriteria criteria) throws UserFailureException;

    public String prepareExportMaterialFeaturesFromAllExperiments(
            TableExportCriteria<TableModelRowWithObject<MaterialSimpleFeatureVectorSummary>> criteria)
            throws UserFailureException;

    /**
     * Return all analysis procedures for an experiment criteria.
     */
    public AnalysisProcedures listNumericalDatasetsAnalysisProcedures(
            ExperimentSearchCriteria experimentSearchCriteria);

}

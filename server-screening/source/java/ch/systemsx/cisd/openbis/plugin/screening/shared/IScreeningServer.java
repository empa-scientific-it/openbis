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

package ch.systemsx.cisd.openbis.plugin.screening.shared;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeAndLabel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
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
 * The <i>screening</i> server. Used internally.
 * 
 * @author Tomasz Pylak
 */
public interface IScreeningServer extends IServer
{
    /**
     * Loads data about the plate for a specified sample id. Attaches information about images and image analysis only if one dataset with such a data
     * exist.
     */
    @Transactional(readOnly = true)
    public PlateContent getPlateContent(String sessionToken,
            TechId plateId);

    /**
     * Loads feature vector of specified dataset with one feature specified by name.
     */
    @Transactional(readOnly = true)
    public FeatureVectorDataset getFeatureVectorDataset(String sessionToken,
            DatasetReference dataset, CodeAndLabel featureName);

    /**
     * Loads all feature vector values for specified well.
     */
    // TODO can return null
    @Transactional(readOnly = true)
    public FeatureVectorValues getWellFeatureVectorValues(String sessionToken,
            String datasetCode, String datastoreCode, WellLocation wellLocation);

    /**
     * Returns plate content for a specified HCS_IMAGE dataset. Loads data about the plate for a specified dataset, which is supposed to contain
     * images in BDS-HCS format.
     */
    @Transactional(readOnly = true)
    public PlateImages getPlateContentForDataset(String sessionToken,
            TechId datasetId);

    /**
     * Finds wells matching the specified criteria. Loads wells content: metadata and (if available) image dataset and feature vectors.
     */
    @Transactional(readOnly = true)
    public List<WellContent> listPlateWells(String sessionToken,
            WellSearchCriteria materialCriteria);

    /**
     * Finds wells containing the specified material and belonging to the specified experiment. Loads wells metadata and single image dataset for each
     * well. If there are many image datasets for the well, all but the first one are ignored. If there is no image dataset for the well, the whole
     * well is ignored.
     */
    @Transactional(readOnly = true)
    public List<WellReplicaImage> listWellImages(String sessionToken, TechId materialId,
            TechId experimentId);

    /**
     * @return materials with codes or properties matching to the query. If the experiment is specified, only materials inside well locations
     *         connected through the plate to this specified experiment(s) will be returned.
     */
    @Transactional(readOnly = true)
    public List<Material> listMaterials(String sessionToken,
            WellSearchCriteria materialCriteria);

    /**
     * Loads all materials of specified type connected with the specified experiment.
     * 
     * @param materialType
     */
    @Transactional(readOnly = true)
    public List<Material> listExperimentMaterials(String sessionToken,
            TechId experimentId, MaterialType materialType);

    @Transactional
    public LogicalImageInfo getImageDatasetInfo(String sessionToken,
            String datasetCode, String datastoreCode, WellLocation wellLocationOrNull);

    @Transactional
    public ImageDatasetEnrichedReference getImageDatasetReference(String sessionToken,
            String datasetCode, String datastoreCode);

    @Transactional
    public List<ImageResolution> getImageDatasetResolutions(String sessionToken,
            String datasetCode, String datastoreCode);

    @Transactional
    public ImageSampleContent getImageDatasetInfosForSample(String sessionToken,
            TechId sampleId, WellLocation wellLocationOrNull);

    /**
     * For given {@link TechId} returns the {@link Sample} and its derived (child) samples.
     * 
     * @return never <code>null</code>.
     * @throws UserFailureException if given <var>sessionToken</var> is invalid or whether sample uniquely identified by given <var>sampleId</var>
     *             does not exist.
     */
    @Transactional(readOnly = true)
    public SampleParentWithDerived getSampleInfo(final String sessionToken,
            final TechId sampleId) throws UserFailureException;

    /**
     * For given {@link TechId} returns the corresponding {@link AbstractExternalData}.
     */
    @Transactional(readOnly = true)
    public AbstractExternalData getDataSetInfo(String sessionToken,
            TechId datasetId);

    /**
     * For given {@link TechId} returns the corresponding {@link Material}.
     */
    @Transactional(readOnly = true)
    public Material getMaterialInfo(String sessionToken, TechId materialId);

    /**
     * Returns vocabulary with given code.
     */
    @Transactional
    public Vocabulary getVocabulary(String sessionToken, String code) throws UserFailureException;

    /**
     * Registers the contents of uploaded libraries.
     */
    @Transactional
    public void registerLibraries(String sessionToken, List<NewLibrary> newLibraries);

    /**
     * Asynchronously registers the contents of uploaded libraries.
     */
    @Transactional
    public void registerLibrariesAsync(String sessionToken, List<NewLibrary> newLibraries, String userEmail);

    /**
     * Returns aggregated feature vectors with their rankings in the specified experiment for all materials.
     */
    @Transactional(readOnly = true)
    public ExperimentFeatureVectorSummary getExperimentFeatureVectorSummary(String sessionToken,
            TechId experimentId, AnalysisProcedureCriteria analysisProcedureCriteria);

    /**
     * Returns a feature vector summary (with details for each replica) for the given experiment and material.
     */
    @Transactional(readOnly = true)
    public MaterialReplicaFeatureSummaryResult getMaterialFeatureVectorSummary(String sessionToken,
            MaterialFeaturesOneExpCriteria criteria);

    /**
     * Returns feature vectors from all experiments for a specified material.
     */
    @Transactional(readOnly = true)
    public List<MaterialSimpleFeatureVectorSummary> getMaterialFeatureVectorsFromAllExperiments(
            String sessionToken, MaterialFeaturesManyExpCriteria criteria);

    /**
     * Return a list of all different analysis procedures applied to the well analysis data sets of an experiment.
     * <p>
     * Note that analysis procedures of segmentation image datasets are not returned by this method!
     * </p>
     * <p>
     * The result contains unique values. It can contain NULL (which can be used for data sets having no ANALYSIS_PROCEDURE value specified).
     * </p>
     */
    @Transactional(readOnly = true)
    public AnalysisProcedures listNumericalDatasetsAnalysisProcedures(String sessionToken,
            ExperimentSearchCriteria experimentSearchCriteria);

}

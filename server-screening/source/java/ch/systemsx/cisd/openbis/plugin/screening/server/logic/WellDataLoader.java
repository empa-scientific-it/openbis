/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.server.logic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.lemnik.eodsql.DataIterator;

import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang3.time.StopWatch;

import ch.systemsx.cisd.openbis.generic.server.business.bo.IMaterialBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.materiallister.IMaterialLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleLister;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeAndLabel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.screening.server.IScreeningBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.BasicWellContentQueryResult;
import ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.IScreeningQuery;
import ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.IWellReference;
import ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.PatternMatchingUtils;
import ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.WellContentQueryResult;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.IWellData;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.MaterialIdFeatureVectorSummary;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.WellData;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.WellExtendedData;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ExperimentReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureVectorValues;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialSimpleFeatureVectorSummary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialSummarySettings;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.AnalysisProcedureCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.MaterialFeaturesOneExpCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.FeatureVectorLoader.WellFeatureCollection;

/**
 * Loads well's feature vectors and materials and calculates summaries.
 * 
 * @author Tomasz Pylak
 */
class WellDataLoader extends AbstractContentLoader
{
    private final MaterialSummarySettings settings;

    public WellDataLoader(Session session, IScreeningBusinessObjectFactory businessObjectFactory,
            IDAOFactory daoFactory, IScreeningQuery screeningQuery, MaterialSummarySettings settings)
    {
        super(session, businessObjectFactory, daoFactory, screeningQuery);
        this.settings = settings;
    }

    /**
     * Loads feature vectors of each well with the specified material in the specified experiment.
     */
    public List<WellExtendedData> tryLoadWellData(MaterialFeaturesOneExpCriteria criteria)
    {
        TechId materialId = criteria.getMaterialId();
        List<WellContentQueryResult> wells =
                getPlateLocationsForMaterialId(materialId, criteria.getExperimentId());
        List<WellData> wellsData =
                tryCreateWellDataForMaterial(wells, materialId,
                        criteria.getAnalysisProcedureCriteria());
        if (wellsData == null)
        {
            return null;
        }
        Map<WellReference, Sample> wellRefToSampleMap = loadEnrichedWellSamples(wells);
        return createWellExtendedData(wellsData, wellRefToSampleMap);
    }

    private List<WellContentQueryResult> getPlateLocationsForMaterialId(TechId materialId,
            TechId experimentId)
    {
        DataIterator<WellContentQueryResult> wellsIterator =
                getScreeningDAO().getPlateLocationsForMaterialId(materialId.getId(),
                        experimentId.getId());

        List<WellContentQueryResult> wells = new ArrayList<WellContentQueryResult>();
        for (WellContentQueryResult well : wellsIterator)
        {
            wells.add(well);
        }
        return wells;
    }

    private Map<WellReference, Sample> loadEnrichedWellSamples(
            Iterable<WellContentQueryResult> wells)
    {
        Set<Long> wellIds = extractWellIds(wells);
        List<Sample> wellSamples = loadSamplesWithMaterialPropertiesEnriched(wellIds);
        return asWellRefToSampleMap(wellSamples, wells);
    }

    private List<WellData> tryCreateWellDataForMaterial(Iterable<WellContentQueryResult> wells,
            TechId materialId, AnalysisProcedureCriteria analysisProcedureCriteria)
    {
        WellFeatureCollection<FeatureVectorValues> allWellFeatures =
                tryLoadWellSingleFeatureVectors(wells, analysisProcedureCriteria);
        if (allWellFeatures == null)
        {
            return null;
        }
        List<FeatureVectorValues> features = allWellFeatures.getFeatures();
        Map<WellReference, Long> dummyMaterialMap = createDummyMaterialMap(wells, materialId);
        return createWellData(dummyMaterialMap, features);
    }

    private Map<WellReference, Long/* material id */> createDummyMaterialMap(
            Iterable<WellContentQueryResult> wells, TechId materialId)
    {
        Map<WellReference, Long> experimentWellToMaterialMap = new HashMap<WellReference, Long>();
        for (WellContentQueryResult well : wells)
        {
            WellReference wellReference = well.getWellReference();
            experimentWellToMaterialMap.put(wellReference, materialId.getId());
        }
        return experimentWellToMaterialMap;
    }

    private static Map<WellReference, Sample> asWellRefToSampleMap(List<Sample> wellSamples,
            Iterable<WellContentQueryResult> wells)
    {
        Map<WellReference, Sample> wellRefToSampleMap = new HashMap<WellReference, Sample>();
        Map<Long, Sample> idToSampleMap = asSampleIdMap(wellSamples);
        for (WellContentQueryResult well : wells)
        {
            Sample sample = idToSampleMap.get(well.well_id);
            wellRefToSampleMap.put(well.getWellReference(), sample);
        }
        return wellRefToSampleMap;
    }

    private List<WellExtendedData> createWellExtendedData(List<WellData> wellsData,
            final Map<WellReference, Sample> wellRefToSampleMap)
    {
        Collection<WellExtendedData> data =
                org.apache.commons.collections4.CollectionUtils
                        .collect(
                                wellsData,
                                new org.apache.commons.collections4.Transformer<WellData, WellExtendedData>()
                                    {
                                        @Override
                                        public WellExtendedData transform(WellData wellData)
                                        {
                                            WellReference wellReference =
                                                    wellData.tryGetWellReference();
                                            assert wellReference != null : "wellReference not available for "
                                                    + wellData;
                                            Sample wellSample =
                                                    wellRefToSampleMap.get(wellReference);
                                            assert wellSample != null : "Cannot find a sample for "
                                                    + wellReference;

                                            return new WellExtendedData(wellData, wellSample);
                                        }
                                    });
        return new LinkedList<WellExtendedData>(data);
    }

    private static Map<Long, Sample> asSampleIdMap(List<Sample> samples)
    {
        Map<Long/* sample id */, Sample> map = new HashMap<Long, Sample>();
        for (Sample sample : samples)
        {
            map.put(sample.getId(), sample);
        }
        return map;
    }

    private List<Sample> loadSamplesWithMaterialPropertiesEnriched(Set<Long> wellIds)
    {
        ListOrSearchSampleCriteria criteria = new ListOrSearchSampleCriteria(wellIds);
        ISampleLister sampleLister = businessObjectFactory.createSampleLister(session);
        List<Sample> wellSamples = sampleLister.list(criteria);

        IMaterialLister materialLister = businessObjectFactory.createMaterialLister(session);
        List<Material> containedMaterials = getMaterialsWithDuplicates(wellSamples);
        materialLister.enrichWithProperties(containedMaterials);

        return wellSamples;
    }

    /**
     * Return *all* material objects contained in the wells as list. The list can contained different objects representing the same entity in the
     * database, but we need the duplication to be able to populate an object graph with wells correctly.
     */
    private static List<Material> getMaterialsWithDuplicates(List<Sample> samples)
    {
        List<Material> materials = new ArrayList<Material>();
        for (Sample sample : samples)
        {
            for (IEntityProperty property : sample.getProperties())
            {
                Material materialOrNull = property.getMaterial();
                if (materialOrNull != null)
                {
                    materials.add(materialOrNull);
                }
            }
        }
        return materials;
    }

    private static Set<Long> extractWellIds(Iterable<WellContentQueryResult> wells)
    {
        Set<Long> ids = new HashSet<Long>();
        for (WellContentQueryResult well : wells)
        {
            ids.add(well.well_id);
        }
        return ids;
    }

    /**
     * Calculates summaries of feature vectors for each experiment and a specified material. Uses a constant number of selects.
     */
    public List<MaterialSimpleFeatureVectorSummary> loadMaterialFeatureVectorsFromAllAssaysBatch(
            TechId materialId, AnalysisProcedureCriteria analysisProcedureCriteria,
            boolean computeRanks, List<ExperimentReference> experiments)
    {
        List<MaterialSimpleFeatureVectorSummary> summaries =
                new ArrayList<MaterialSimpleFeatureVectorSummary>();
        StopWatch watch = createWatchAndStart();
        int totalWellsLoaded = 0;
        int totalFeatureVectorsLoaded = 0;

        List<BasicWellContentQueryResult> allWells;

        if (computeRanks)
        {
            allWells = fetchWellLocations(materialId, extractIds(experiments));
        } else
        {
            allWells = fetchWellLocationsForMaterial(materialId, extractIds(experiments));
        }

        totalWellsLoaded += allWells.size();
        WellFeatureCollection<FeatureVectorValues> allWellFeaturesOrNull =
                tryLoadWellSingleFeatureVectors(allWells, analysisProcedureCriteria);

        if (allWellFeaturesOrNull == null)
        {
            addEmptySummaries(summaries, experiments);
        } else
        {
            for (ExperimentReference experiment : experiments)
            {
                totalFeatureVectorsLoaded += allWellFeaturesOrNull.getFeatures().size();
                List<BasicWellContentQueryResult> experimentWells =
                        filterExperimentWells(allWells, experiment.getPermId());
                List<MaterialIdFeatureVectorSummary> experimentSummaries =
                        calculateExperimentFeatureVectorSummaries(experimentWells,
                                allWellFeaturesOrNull, false);

                MaterialSimpleFeatureVectorSummary summary =
                        tryFindExperimentFeatureVectorSummary(materialId, experiment,
                                allWellFeaturesOrNull.getFeatureCodesAndLabels(),
                                experimentSummaries);
                if (summary != null)
                {
                    summaries.add(summary);
                } else
                {
                    summaries.add(new MaterialSimpleFeatureVectorSummary(experiment));
                }
            }
        }
        operationLog.info(String.format(
                "[%d msec] Experiments batch: %d, wells loaded: %d, feature vectors: %d.",
                watch.getTime(), experiments.size(), totalWellsLoaded, totalFeatureVectorsLoaded));
        return summaries;
    }

    private List<BasicWellContentQueryResult> fetchWellLocationsForMaterial(TechId materialId,
            long[] experimentIds)
    {
        StopWatch watch = createWatchAndStart();
        List<BasicWellContentQueryResult> wells =
                getScreeningDAO().getPlateLocationsForExperiment(experimentIds, materialId.getId());
        operationLog.info("[" + watch.getTime() + " msec] Fetching " + wells.size()
                + " wells for 1 material.");
        return wells;
    }

    private static long[] extractIds(List<ExperimentReference> experiments)
    {
        long[] ids = new long[experiments.size()];
        int i = 0;
        for (ExperimentReference experiment : experiments)
        {
            ids[i++] = experiment.getId();
        }
        return ids;
    }

    private void addEmptySummaries(List<MaterialSimpleFeatureVectorSummary> summaries,
            List<ExperimentReference> experiments)
    {
        for (ExperimentReference experiment : experiments)
        {
            summaries.add(new MaterialSimpleFeatureVectorSummary(experiment));
        }
    }

    /**
     * Fetches wells containing materials of the same type as the specified one in chosen experiment.
     */
    private List<BasicWellContentQueryResult> fetchWellLocations(TechId materialId,
            TechId experimentId)
    {
        return fetchWellLocations(materialId, new long[]
        { experimentId.getId() });
    }

    /**
     * Fetches wells containing materials of the same type as the specified one in chosen experiments.
     */
    private List<BasicWellContentQueryResult> fetchWellLocations(TechId materialId,
            long[] experimentIds)
    {
        String materialTypeCode = fetchMaterialTypeCode(materialId);
        return fetchWellLocations(materialTypeCode, experimentIds);
    }

    private List<BasicWellContentQueryResult> fetchWellLocations(String materialTypeCodePattern,
            long[] experimentIds)
    {
        StopWatch watch = createWatchAndStart();
        List<BasicWellContentQueryResult> wells =
                getScreeningDAO().getPlateLocationsForExperiment(experimentIds,
                        materialTypeCodePattern);
        operationLog.info("[" + watch.getTime() + " msec] Fetching " + wells.size() + " wells.");
        return wells;
    }

    private List<MaterialIdFeatureVectorSummary> calculateExperimentFeatureVectorSummaries(
            Iterable<BasicWellContentQueryResult> wells,
            WellFeatureCollection<FeatureVectorValues> allWellFeatures, boolean calculateDeviations)
    {
        List<? extends IWellData> experimentWellData = createWellData(wells, allWellFeatures);

        // We have to calculate summaries for all materials in the experiment to get the
        // right ranking of the specified material.
        return calculateExperimentFeatureVectorSummaries(experimentWellData, calculateDeviations);
    }

    private List<WellData> createWellData(Iterable<BasicWellContentQueryResult> wells,
            WellFeatureCollection<FeatureVectorValues> allWellFeatures)
    {
        Map<WellReference, Long/* material id */> wellToMaterialMap =
                createWellToMaterialMap(wells);
        return createWellData(wellToMaterialMap, allWellFeatures.getFeatures());
    }

    private String fetchMaterialTypeCode(TechId materialId)
    {
        IMaterialBO materialBO = businessObjectFactory.createMaterialBO(session);
        materialBO.loadDataByTechId(materialId);
        return materialBO.getMaterial().getEntityType().getCode();
    }

    // NOTE: if there are 2 materials in a well, a random one will be chosen
    private static Map<WellReference, Long/* material id */> createWellToMaterialMap(
            Iterable<BasicWellContentQueryResult> wells)
    {
        Map<WellReference, Long> wellToMaterialMap = new HashMap<WellReference, Long>();
        for (BasicWellContentQueryResult well : wells)
        {
            WellReference wellReference = well.getWellReference();
            wellToMaterialMap.put(wellReference, well.material_content_id);
        }
        return wellToMaterialMap;
    }

    private static List<BasicWellContentQueryResult> filterExperimentWells(
            List<BasicWellContentQueryResult> wells, final String experimentPermId)
    {

        Collection<BasicWellContentQueryResult> filtered =
                org.apache.commons.collections4.CollectionUtils.select(wells,
                        new Predicate<BasicWellContentQueryResult>()
                            {
                                @Override
                                public boolean evaluate(BasicWellContentQueryResult well)
                                {
                                    return belongsToExperiment(well, experimentPermId);
                                }
                            });
        return new LinkedList<BasicWellContentQueryResult>(filtered);
    }

    private static boolean belongsToExperiment(BasicWellContentQueryResult well,
            String experimentPermId)
    {
        return well.exp_perm_id.equals(experimentPermId);
    }

    private MaterialSimpleFeatureVectorSummary tryFindExperimentFeatureVectorSummary(
            TechId materialId, ExperimentReference experiment, List<CodeAndLabel> features,
            List<MaterialIdFeatureVectorSummary> experimentSummaries)
    {
        // select the summary of the right material
        MaterialIdFeatureVectorSummary materialSummary =
                tryFindMaterialSummary(materialId, experimentSummaries);
        if (materialSummary == null)
        {
            return null; // there are no feature vectors for this material
        }
        return new MaterialSimpleFeatureVectorSummary(experiment, features,
                materialSummary.getFeatureVectorSummary(), materialSummary.getFeatureVectorRanks());
    }

    private List<MaterialIdFeatureVectorSummary> calculateExperimentFeatureVectorSummaries(
            List<? extends IWellData> experimentWellData, boolean calculateDeviations)
    {
        return WellReplicaSummaryCalculator.calculateReplicasFeatureVectorSummaries(
                experimentWellData, settings.getAggregationType(), calculateDeviations);
    }

    private static MaterialIdFeatureVectorSummary tryFindMaterialSummary(TechId materialId,
            List<MaterialIdFeatureVectorSummary> summaries)
    {
        for (MaterialIdFeatureVectorSummary summary : summaries)
        {
            if (summary.getMaterial().equals(materialId.getId()))
            {
                return summary;
            }
        }
        return null;
    }

    // connects each well with a material with its feature vector
    private static List<WellData> createWellData(
            Map<WellReference, Long/* material id */> experimentWellToMaterialMap,
            List<FeatureVectorValues> allFeatures)
    {
        List<WellData> experimentWellDataList = new ArrayList<WellData>();
        for (FeatureVectorValues feature : allFeatures)
        {
            WellReference wellReference = feature.getWellReference();
            Long materialId = experimentWellToMaterialMap.get(wellReference);
            if (materialId != null)
            {
                float[] values = WellFeatureCollectionLoader.asFeatureVectorValues(feature);
                WellData wellData = new WellData(materialId, values, wellReference);
                experimentWellDataList.add(wellData);
            }
        }
        return experimentWellDataList;
    }

    private static Set<PlateIdentifier> extractPlates(Iterable<? extends IWellReference> allWells)
    {
        Set<PlateIdentifier> plates = new HashSet<PlateIdentifier>();
        for (IWellReference well : allWells)
        {
            String platePermId = well.getPlatePermId();
            PlateIdentifier plateIdent = PlateIdentifier.createFromPermId(platePermId);
            plates.add(plateIdent);
        }
        return plates;
    }

    private WellFeatureCollection<FeatureVectorValues> tryLoadWellSingleFeatureVectors(
            Iterable<? extends IWellReference> allWells,
            AnalysisProcedureCriteria analysisProcedureCriteria)
    {
        Set<PlateIdentifier> plates = extractPlates(allWells);
        return tryLoadWellSingleFeatureVectors(plates, analysisProcedureCriteria);
    }

    private WellFeatureCollection<FeatureVectorValues> tryLoadWellSingleFeatureVectors(
            Set<PlateIdentifier> plates, AnalysisProcedureCriteria analysisProcedureCriteria)
    {
        return new WellFeatureCollectionLoader(session, businessObjectFactory, daoFactory, null)
                .tryLoadWellSingleFeatureVectors(plates, settings.getFeatureCodes(),
                        analysisProcedureCriteria);
    }

    /**
     * Calculates summaries of feature vectors for each material of the specified type in the given experiment.
     */
    public MaterialIdSummariesAndFeatures tryCalculateExperimentFeatureVectorSummaries(
            TechId experimentId, String[] replicaMaterialTypeSubstrings,
            AnalysisProcedureCriteria analysisProcedureCriteria, boolean calculateDeviations)
    {
        String typePatterns =
                PatternMatchingUtils.asPostgresSimilarExpression(settings
                        .getReplicaMaterialTypeSubstrings());
        List<BasicWellContentQueryResult> wells = fetchWellLocations(typePatterns, new long[]
        { experimentId.getId() });
        return tryCalculateExperimentFeatureVectorSummaries(wells, analysisProcedureCriteria,
                calculateDeviations);
    }

    /**
     * Calculates summaries of feature vectors for each material in the given experiment. A material of one well is chosen by filtering materials of
     * the same type as the specified one.
     */
    public MaterialIdSummariesAndFeatures tryCalculateExperimentFeatureVectorSummaries(
            MaterialFeaturesOneExpCriteria criteria, boolean calculateDeviations)
    {
        List<BasicWellContentQueryResult> wells =
                fetchWellLocations(criteria.getMaterialId(), criteria.getExperimentId());
        return tryCalculateExperimentFeatureVectorSummaries(wells,
                criteria.getAnalysisProcedureCriteria(), calculateDeviations);
    }

    private MaterialIdSummariesAndFeatures tryCalculateExperimentFeatureVectorSummaries(
            List<BasicWellContentQueryResult> wells,
            AnalysisProcedureCriteria analysisProcedureCriteria, boolean calculateDeviations)
    {
        WellFeatureCollection<FeatureVectorValues> allWellFeatures =
                tryLoadWellSingleFeatureVectors(wells, analysisProcedureCriteria);

        if (allWellFeatures == null)
        {
            return null;
        }
        List<MaterialIdFeatureVectorSummary> featureSummaries =
                calculateExperimentFeatureVectorSummaries(wells, allWellFeatures,
                        calculateDeviations);
        return new MaterialIdSummariesAndFeatures(featureSummaries,
                allWellFeatures.getFeatureCodesAndLabels());
    }

    static class MaterialIdSummariesAndFeatures
    {
        private final List<MaterialIdFeatureVectorSummary> featureSummaries;

        private final List<CodeAndLabel> featureNames;

        public MaterialIdSummariesAndFeatures(
                List<MaterialIdFeatureVectorSummary> featureSummaries,
                List<CodeAndLabel> featureNames)
        {
            this.featureSummaries = featureSummaries;
            this.featureNames = featureNames;
        }

        public List<MaterialIdFeatureVectorSummary> getFeatureSummaries()
        {
            return featureSummaries;
        }

        public List<CodeAndLabel> getFeatureNames()
        {
            return featureNames;
        }
    }

}

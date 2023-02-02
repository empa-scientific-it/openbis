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
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.time.StopWatch;

import ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister.IDatasetLister;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.screening.server.IScreeningBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.ExperimentReferenceQueryResult;
import ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.IScreeningQuery;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ExperimentReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialSimpleFeatureVectorSummary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialSummarySettings;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.AnalysisProcedureCriteria;

/**
 * Finds all experiments where the given material is present and calculates summaries for feature vectors (if analysis data are available) .
 * 
 * @author Tomasz Pylak
 */
public class MaterialFeaturesFromAllExperimentsLoader extends AbstractContentLoader
{
    private static final Integer MAX_ANALYSIS_DATASETS_IN_BATCH = 100;

    public static List<MaterialSimpleFeatureVectorSummary> loadMaterialFeatureVectorsFromAllAssays(
            Session session, IScreeningBusinessObjectFactory businessObjectFactory,
            IDAOFactory daoFactory, TechId materialId,
            AnalysisProcedureCriteria analysisProcedureCriteria, boolean computeRanks,
            TechId projectTechIdOrNull, MaterialSummarySettings settings)
    {
        MaterialFeaturesFromAllExperimentsLoader loader =
                new MaterialFeaturesFromAllExperimentsLoader(session, businessObjectFactory,
                        daoFactory, null, settings);
        List<ExperimentReference> experiments =
                loader.fetchExperiments(materialId, projectTechIdOrNull);
        return loader.loadMaterialFeatureVectorsFromAllAssays(materialId,
                analysisProcedureCriteria, computeRanks, experiments);
    }

    private List<ExperimentReference> fetchExperiments(TechId materialId, TechId projectTechIdOrNull)
    {
        IScreeningQuery dao = getScreeningDAO();
        List<ExperimentReferenceQueryResult> experimentRefs;
        if (projectTechIdOrNull == null)
        {
            experimentRefs = dao.getExperimentsWithMaterial(materialId.getId());
        } else
        {
            // load results only for experiments within a given project
            experimentRefs =
                    dao.getExperimentsWithMaterial(materialId.getId(), projectTechIdOrNull.getId());
        }
        return asExperimentReferences(experimentRefs);
    }

    private static List<ExperimentReference> asExperimentReferences(
            List<ExperimentReferenceQueryResult> experiments)
    {
        Collection<ExperimentReference> references =
                org.apache.commons.collections4.CollectionUtils
                        .collect(
                                experiments,
                                new org.apache.commons.collections4.Transformer<ExperimentReferenceQueryResult, ExperimentReference>()
                                    {
                                        @Override
                                        public ExperimentReference transform(
                                                ExperimentReferenceQueryResult experiment)
                                        {
                                            return createExperimentReference(experiment);
                                        }
                                    });
        return new LinkedList<ExperimentReference>(references);
    }

    private static ExperimentReference createExperimentReference(ExperimentReferenceQueryResult exp)
    {
        return new ExperimentReference(exp.exp_id, exp.exp_perm_id, exp.exp_code,
                exp.exp_type_code, exp.proj_code, exp.space_code);
    }

    private final WellDataLoader wellDataLoader;

    private MaterialFeaturesFromAllExperimentsLoader(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, IDAOFactory daoFactory,
            IScreeningQuery screeningQuery, MaterialSummarySettings settings)
    {
        super(session, businessObjectFactory, daoFactory, screeningQuery);
        this.wellDataLoader =
                new WellDataLoader(session, businessObjectFactory, daoFactory, screeningQuery,
                        settings);
    }

    /**
     * Note that different experiments can have different set of features!
     */
    private List<MaterialSimpleFeatureVectorSummary> loadMaterialFeatureVectorsFromAllAssays(
            TechId materialId, AnalysisProcedureCriteria analysisProcedureCriteria,
            boolean computeRanks, List<ExperimentReference> experiments)
    {
        List<MaterialSimpleFeatureVectorSummary> summaries =
                new ArrayList<MaterialSimpleFeatureVectorSummary>();
        StopWatch globalWatch = createWatchAndStart();

        List<ExperimentReference> experimentsBatch = new ArrayList<ExperimentReference>();
        long datasetsInBatch = 0;
        for (int i = 0; i < experiments.size(); i++)
        {
            ExperimentReference experiment = experiments.get(i);
            int datasetsNum = countAnalysisDatasets(experiment);
            operationLog.info(String.format("Experiment %s contains %d analysis datasets",
                    experiment, datasetsNum));
            if (datasetsNum == 0)
            {
                // avoid loading all the wells if there are no analysis datasets
                summaries.add(new MaterialSimpleFeatureVectorSummary(experiment));
            } else
            {
                datasetsInBatch += datasetsNum;
                experimentsBatch.add(experiment);
            }
            if (datasetsInBatch >= MAX_ANALYSIS_DATASETS_IN_BATCH
                    || (i == experiments.size() - 1 && experimentsBatch.isEmpty() == false))
            {
                operationLog.info(String.format("Processing %d experiments with %d datasets: %s",
                        experimentsBatch.size(), datasetsInBatch, experimentsBatch));
                List<MaterialSimpleFeatureVectorSummary> batchSummaries =
                        wellDataLoader.loadMaterialFeatureVectorsFromAllAssaysBatch(materialId,
                                analysisProcedureCriteria, computeRanks, experimentsBatch);
                summaries.addAll(batchSummaries);
                experimentsBatch.clear();
                datasetsInBatch = 0;
            }
        }
        operationLog.info(String.format(
                "[%d msec] Fetching and calculating analysis summaries for %d experiments.",
                globalWatch.getTime(), summaries.size()));
        return summaries;
    }

    private int countAnalysisDatasets(ExperimentReference experiment)
    {
        IDatasetLister lister = businessObjectFactory.createDatasetLister(session);
        List<AbstractExternalData> datasets =
                lister.listByExperimentTechId(new TechId(experiment.getId()), true);
        return ScreeningUtils.filterImageAnalysisDatasets(datasets).size();
    }

}

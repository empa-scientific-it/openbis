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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.common.collection.IKeyExtractor;
import ch.systemsx.cisd.common.collection.TableMap;
import ch.systemsx.cisd.common.collection.TableMap.UniqueKeyViolationStrategy;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IDataSetTable;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeAndLabel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListMaterialCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.screening.server.IScreeningBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.IScreeningQuery;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.WellDataLoader.MaterialIdSummariesAndFeatures;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.MaterialIdFeatureVectorSummary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ExperimentFeatureVectorSummary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ExperimentReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialFeatureVectorSummary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialSummarySettings;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.AnalysisProcedureCriteria;

/**
 * {@See #loadExperimentFeatureVectors}.
 * 
 * @author Tomasz Pylak
 */
public class ExperimentFeatureVectorSummaryLoader extends AbstractContentLoader
{
    protected final MaterialSummarySettings settings;

    private final WellDataLoader wellDataLoader;

    public ExperimentFeatureVectorSummaryLoader(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, IDAOFactory daoFactory,
            IScreeningQuery screeningQuery, MaterialSummarySettings settings)
    {
        super(session, businessObjectFactory, daoFactory, screeningQuery);
        this.settings = settings;
        this.wellDataLoader =
                new WellDataLoader(session, businessObjectFactory, daoFactory, screeningQuery,
                        settings);
    }

    public ExperimentFeatureVectorSummary loadExperimentFeatureVectors(TechId experimentId,
            AnalysisProcedureCriteria analysisProcedureCriteria, AnalysisSettings analysisSettings)
    {
        ExperimentReference experiment = loadExperimentByTechId(experimentId);
        if (analysisSettings.noAnalysisSettings() == false)
        {
            List<AbstractExternalData> matchingDataSets =
                    getMatchingDataSets(experimentId, analysisProcedureCriteria, analysisSettings);
            TableModel tabelModel =
                    new TableModel(Collections.<TableModelColumnHeader> emptyList(),
                            Collections.<TableModelRow> emptyList());
            if (matchingDataSets.size() == 1)
            {
                AbstractExternalData ds = matchingDataSets.get(0);
                String reportingPluginKey = analysisSettings.tryToGetReportingPluginKey(ds);
                String dataStore = ds.getDataStore().getCode();
                List<String> codes = Arrays.asList(ds.getCode());
                IDataSetTable dataSetTable = businessObjectFactory.createDataSetTable(session);
                try
                {
                    tabelModel =
                            dataSetTable.createReportFromDatasets(reportingPluginKey, dataStore,
                                    codes);
                } catch (UserFailureException ex)
                {
                    String message = ex.getMessage();
                    if (message.startsWith("Main "))
                    {
                        message +=
                                "\n\nHint: The file pattern for the data set type "
                                        + ds.getDataSetType().getCode() + " might be wrong.";
                    }
                    throw decorateException(ex, ds, "Reason: " + message);
                } catch (Exception ex)
                {
                    throw decorateException(ex, ds, "See server logs for the reason.");
                }
            }
            return new ExperimentFeatureVectorSummary(experiment,
                    Collections.<MaterialFeatureVectorSummary> emptyList(),
                    Collections.<CodeAndLabel> emptyList(), tabelModel);
        }

        return calculatedSummary(experimentId, analysisProcedureCriteria, experiment);
    }

    ExperimentFeatureVectorSummary calculatedSummary(TechId experimentId,
            AnalysisProcedureCriteria analysisProcedureCriteria, ExperimentReference experiment)
    {
        MaterialIdSummariesAndFeatures summaries =
                wellDataLoader.tryCalculateExperimentFeatureVectorSummaries(experimentId,
                        settings.getReplicaMaterialTypeSubstrings(), analysisProcedureCriteria,
                        false);
        if (summaries == null)
        {
            return createEmptySummary(experiment);
        }
        List<MaterialFeatureVectorSummary> enrichedFeatureSummaries =
                enrichWithMaterials(summaries.getFeatureSummaries());
        return new ExperimentFeatureVectorSummary(experiment, enrichedFeatureSummaries,
                summaries.getFeatureNames(), null);
    }

    private UserFailureException decorateException(Exception ex, AbstractExternalData dataSet,
            String message)
    {
        return new UserFailureException("Analysis summary for data set " + dataSet.getCode()
                + " couldn't retrieved from Data Store Server. " + message, ex);
    }

    private List<AbstractExternalData> getMatchingDataSets(TechId experimentId,
            AnalysisProcedureCriteria analysisProcedureCriteria, AnalysisSettings analysisSettings)
    {
        List<AbstractExternalData> dataSets =
                businessObjectFactory.createDatasetLister(session).listByExperimentTechId(
                        experimentId, true);
        List<AbstractExternalData> matchingDataSets = new ArrayList<AbstractExternalData>();
        for (AbstractExternalData dataSet : dataSets)
        {
            if (ScreeningUtils.isMatchingAnalysisProcedure(dataSet, analysisProcedureCriteria)
                    && analysisSettings.tryToGetReportingPluginKey(dataSet) != null)
            {
                matchingDataSets.add(dataSet);
            }
        }
        return matchingDataSets;
    }

    private List<MaterialFeatureVectorSummary> enrichWithMaterials(
            List<MaterialIdFeatureVectorSummary> summaries)
    {
        Set<Long> materialIds = extractMaterialIds(summaries);
        List<Material> materials = fetchMaterials(materialIds);
        return enrichWithMaterials(summaries, materials);
    }

    private static List<MaterialFeatureVectorSummary> enrichWithMaterials(
            List<MaterialIdFeatureVectorSummary> summaries, List<Material> materials)
    {
        final TableMap<Long, Material> materialMap = createMaterialMap(materials);

        Collection<MaterialFeatureVectorSummary> collection =
                org.apache.commons.collections4.CollectionUtils
                        .collect(
                                summaries,
                                new org.apache.commons.collections4.Transformer<MaterialIdFeatureVectorSummary, MaterialFeatureVectorSummary>()
                                    {
                                        @Override
                                        public MaterialFeatureVectorSummary transform(
                                                MaterialIdFeatureVectorSummary summary)
                                        {
                                            return convert(summary, materialMap);
                                        }
                                    });
        return new LinkedList<MaterialFeatureVectorSummary>(collection);
    }

    private List<Material> fetchMaterials(Set<Long> materialIds)
    {
        return businessObjectFactory.createMaterialLister(session).list(
                ListMaterialCriteria.createFromMaterialIds(materialIds), true);
    }

    private static Set<Long> extractMaterialIds(List<MaterialIdFeatureVectorSummary> summaries)
    {
        Set<Long> ids = new HashSet<Long>();
        for (MaterialIdFeatureVectorSummary summary : summaries)
        {
            ids.add(summary.getMaterial());
        }
        return ids;
    }

    private static MaterialFeatureVectorSummary convert(MaterialIdFeatureVectorSummary summary,
            TableMap<Long, Material> materialMap)
    {
        Material material = materialMap.getOrDie(summary.getMaterial());
        return summary.createWithMaterial(material);
    }

    private static TableMap<Long/* material id */, Material> createMaterialMap(
            List<Material> materials)
    {
        return new TableMap<Long, Material>(materials, new IKeyExtractor<Long, Material>()
            {
                @Override
                public Long getKey(Material material)
                {
                    return material.getId();
                }
            }, UniqueKeyViolationStrategy.ERROR);
    }

    private static ExperimentFeatureVectorSummary createEmptySummary(ExperimentReference experiment)
    {
        List<MaterialFeatureVectorSummary> materialsSummary = Collections.emptyList();
        List<CodeAndLabel> featureDescriptions = Collections.emptyList();
        return new ExperimentFeatureVectorSummary(experiment, materialsSummary,
                featureDescriptions, null);
    }
}

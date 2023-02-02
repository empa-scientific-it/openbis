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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.server.resultset;

import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.grids.MaterialFeatureVectorsFromAllExperimentsGridColumnIDs.EXPERIMENT;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.AbstractTableModelProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeAndLabel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;
import ch.systemsx.cisd.openbis.plugin.screening.shared.IScreeningServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ExperimentReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialSimpleFeatureVectorSummary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.MaterialFeaturesManyExpCriteria;

/**
 * A provider for material feature summaries from all experiments.
 * 
 * @author Kaloyan Enimanev
 */
public class MaterialFeatureVectorsFromAllExperimentsProvider extends
        AbstractTableModelProvider<MaterialSimpleFeatureVectorSummary>
{
    private final IScreeningServer server;

    private final String sessionToken;

    private final MaterialFeaturesManyExpCriteria criteria;

    public MaterialFeatureVectorsFromAllExperimentsProvider(IScreeningServer server,
            String sessionToken, MaterialFeaturesManyExpCriteria criteria)
    {
        this.server = server;
        this.sessionToken = sessionToken;
        this.criteria = criteria;
    }

    @Override
    public TypedTableModel<MaterialSimpleFeatureVectorSummary> createTableModel()
    {
        TypedTableModelBuilder<MaterialSimpleFeatureVectorSummary> builder =
                new TypedTableModelBuilder<MaterialSimpleFeatureVectorSummary>();
        List<MaterialSimpleFeatureVectorSummary> summaries =
                server.getMaterialFeatureVectorsFromAllExperiments(sessionToken, criteria);

        builder.addColumn(EXPERIMENT);

        createFeatureColumns(builder, summaries);
        for (MaterialSimpleFeatureVectorSummary row : summaries)
        {
            addRow(builder, row);
        }

        return builder.getModel();
    }

    private void createFeatureColumns(
            TypedTableModelBuilder<MaterialSimpleFeatureVectorSummary> builder,
            List<MaterialSimpleFeatureVectorSummary> summaries)
    {
        for (MaterialSimpleFeatureVectorSummary summary : summaries)
        {
            for (CodeAndLabel codeAndLabel : summary.getFeatureDescriptions())
            {
                builder.column(getFeatureValueColumnId(codeAndLabel)).withTitle(
                        codeAndLabel.getLabel());
                if (criteria.isComputeRanks())
                {
                    builder.column(getFeatureRankColumnId(codeAndLabel)).withTitle(
                            ScreeningProviderMessages.RANK_COLUMN_MSG);
                }

            }
        }
    }

    private void addRow(TypedTableModelBuilder<MaterialSimpleFeatureVectorSummary> builder,
            MaterialSimpleFeatureVectorSummary row)
    {
        builder.addRow(row);

        ExperimentReference experiment = row.getExperiment();
        builder.column(EXPERIMENT).addString(experiment.getCode());

        float[] features = row.getFeatureVectorSummary();
        int[] ranks = row.getFeatureVectorRanks();
        List<CodeAndLabel> descriptions = row.getFeatureDescriptions();
        for (int i = 0; i < features.length; i++)
        {
            CodeAndLabel description = descriptions.get(i);
            builder.column(getFeatureValueColumnId(description)).addDouble((double) features[i]);
            if (criteria.isComputeRanks())
            {
                builder.column(getFeatureRankColumnId(description)).addInteger(Long.valueOf(ranks[i]));
            }
        }
    }

    private String getFeatureValueColumnId(CodeAndLabel codeAndLabel)
    {
        return "value-" + codeAndLabel.getCode();
    }

    private String getFeatureRankColumnId(CodeAndLabel codeAndLabel)
    {
        return "rank-" + codeAndLabel.getCode();
    }

}

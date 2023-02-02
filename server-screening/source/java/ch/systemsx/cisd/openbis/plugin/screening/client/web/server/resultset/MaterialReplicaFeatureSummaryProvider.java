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

import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.grids.MaterialReplicaFeatureSummaryGridColumnIDs.DEVIATION;
import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.grids.MaterialReplicaFeatureSummaryGridColumnIDs.FEATURE;
import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.grids.MaterialReplicaFeatureSummaryGridColumnIDs.MEDIAN;
import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.grids.MaterialReplicaFeatureSummaryGridColumnIDs.RANK;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.AbstractTableModelProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.util.IColumnGroup;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;
import ch.systemsx.cisd.openbis.plugin.screening.shared.IScreeningServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialBiologicalReplicateFeatureSummary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialReplicaFeatureSummary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialReplicaFeatureSummaryResult;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialReplicaSummaryAggregationType;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.MaterialFeaturesOneExpCriteria;

/**
 * A provider for material replica feature summaries.
 * 
 * @author Kaloyan Enimanev
 */
public class MaterialReplicaFeatureSummaryProvider extends
        AbstractTableModelProvider<MaterialReplicaFeatureSummary>
{
    private static final String DEFAULT_SUBGROUP = "DEFAULT_SUBGROUP-";

    private final IScreeningServer server;

    private final String sessionToken;

    private final MaterialFeaturesOneExpCriteria criteria;

    public MaterialReplicaFeatureSummaryProvider(IScreeningServer server, String sessionToken,
            MaterialFeaturesOneExpCriteria criteria)
    {
        this.server = server;
        this.sessionToken = sessionToken;
        this.criteria = criteria;
    }

    @Override
    public TypedTableModel<MaterialReplicaFeatureSummary> createTableModel()
    {
        TypedTableModelBuilder<MaterialReplicaFeatureSummary> builder =
                new TypedTableModelBuilder<MaterialReplicaFeatureSummary>();
        MaterialReplicaFeatureSummaryResult replicaResult =
                server.getMaterialFeatureVectorSummary(sessionToken, criteria);

        List<MaterialReplicaFeatureSummary> rows = replicaResult.getFeatureSummaries();
        boolean displayMedianAndDeviation = shouldDisplayMedianAndDeviationColumns(rows);

        builder.addColumn(FEATURE);
        if (displayMedianAndDeviation)
        {
            builder.addColumn(MEDIAN).withDataType(DataTypeCode.REAL);
            builder.addColumn(DEVIATION).withDataType(DataTypeCode.REAL);
        }
        String rankTitle =
                ScreeningProviderMessages.getRankColumnHeader(replicaResult
                        .getNumberOfMaterialsInExperiment());
        builder.addColumn(RANK).withDataType(DataTypeCode.INTEGER).withTitle(rankTitle);

        List<String> subgroupLabels = replicaResult.getSubgroupLabels();
        if (rows.isEmpty())
        {
            // no results
            return builder.getModel();
        }

        for (String subgroup : subgroupLabels)
        {
            builder.columnGroup(subgroup);
        }

        if (rows.get(0).getDirectTechnicalReplicates() != null)
        {
            builder.columnGroup(DEFAULT_SUBGROUP);
        }

        for (MaterialReplicaFeatureSummary row : rows)
        {
            addRow(builder, row, subgroupLabels, displayMedianAndDeviation);
        }

        return builder.getModel();
    }

    private boolean shouldDisplayMedianAndDeviationColumns(List<MaterialReplicaFeatureSummary> rows)
    {
        return false == hasOnlyOneReplica(rows);
    }

    private boolean hasOnlyOneReplica(List<MaterialReplicaFeatureSummary> rows)
    {
        for (MaterialReplicaFeatureSummary row : rows)
        {
            List<MaterialBiologicalReplicateFeatureSummary> allSubgroups =
                    row.getBiologicalRelicates();
            MaterialBiologicalReplicateFeatureSummary defaultSubGroup =
                    row.getDirectTechnicalReplicates();
            allSubgroups.add(defaultSubGroup);

            if (allSubgroups.size() > 1)
            {
                return false;
            } else if (allSubgroups.size() == 1)
            {
                if (allSubgroups.get(0).getFeatureValues().length > 1)
                {
                    return false;
                }
            }
        }
        return true;
    }

    private void addRow(TypedTableModelBuilder<MaterialReplicaFeatureSummary> builder,
            MaterialReplicaFeatureSummary row, List<String> subgroupLabels,
            boolean displayMedianAndDeviation)
    {
        builder.addRow(row);

        builder.column(FEATURE).addString(row.getFeatureDescription().getLabel());
        builder.column(RANK).addInteger((long) row.getFeatureVectorRank());
        if (displayMedianAndDeviation)
        {
            builder.column(MEDIAN).addDouble(row.getFeatureVectorSummary());
            builder.column(DEVIATION).addDouble(row.getFeatureVectorDeviation());
        }

        MaterialBiologicalReplicateFeatureSummary defaultSubgroup =
                row.getDirectTechnicalReplicates();
        if (defaultSubgroup != null)
        {
            addSubgroup(builder, DEFAULT_SUBGROUP, "", defaultSubgroup);
        }

        int numSubgroups = subgroupLabels.size();
        List<MaterialBiologicalReplicateFeatureSummary> subgroups = row.getBiologicalRelicates();
        for (int i = 0; i < numSubgroups; i++)
        {
            String subgroupLabel = subgroupLabels.get(i);
            MaterialBiologicalReplicateFeatureSummary subgroup = subgroups.get(i);
            addSubgroup(builder, subgroupLabel, subgroupLabel, subgroup);
        }
    }

    private void addSubgroup(TypedTableModelBuilder<MaterialReplicaFeatureSummary> builder,
            String groupId, String groupLabel, MaterialBiologicalReplicateFeatureSummary subgroup)
    {
        IColumnGroup columnGroup = builder.columnGroup(groupId);

        float[] featureValues = subgroup.getFeatureValues();
        final int numberFeatures = featureValues.length;
        for (int i = 0; i < numberFeatures; i++)
        {
            String replicaColumnId = getReplicaColumnId(groupLabel, i);
            String replicaColumnTitle =
                    ScreeningProviderMessages.getReplicaColumnTitle(groupLabel, i + 1,
                            numberFeatures);
            columnGroup.column(replicaColumnId).withDataType(DataTypeCode.REAL)
                    .withTitle(replicaColumnTitle).addDouble((double) featureValues[i]);
        }

        // aggregates should be shown only for biological replicates which have more than one
        // technical replicate
        if (false == DEFAULT_SUBGROUP.equals(groupId) && numberFeatures > 1)
        {
            MaterialReplicaSummaryAggregationType aggregationType =
                    subgroup.getSummaryAggregationType();
            String aggreationColumnId = getAggreationColumnId(groupLabel, aggregationType);
            String aggreationColumnTitle = getAggreationColumnTitle(groupLabel, aggregationType);
            columnGroup.column(aggreationColumnId).withDataType(DataTypeCode.REAL)
                    .withTitle(aggreationColumnTitle)
                    .addDouble((double) subgroup.getAggregatedSummary());
        }

    }

    private String getAggreationColumnId(String group,
            MaterialReplicaSummaryAggregationType summaryAggregationType)
    {
        return group + ":" + summaryAggregationType.name().toLowerCase();
    }

    private String getAggreationColumnTitle(String group,
            MaterialReplicaSummaryAggregationType aggregationType)
    {
        return group + " " + aggregationType.name().toLowerCase();
    }

    private String getReplicaColumnId(String group, int replicaIdx)
    {
        return group + ":" + replicaIdx;
    }

}

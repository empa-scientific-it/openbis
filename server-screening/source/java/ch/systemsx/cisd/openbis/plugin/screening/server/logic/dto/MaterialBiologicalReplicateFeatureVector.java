/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto;

import java.io.Serializable;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialReplicaSummaryAggregationType;

/**
 * Feature vector summary for the subgroup of well replicas together with detailed feature vectors which were used to calculate the summary.
 * 
 * @author Tomasz Pylak
 */
public class MaterialBiologicalReplicateFeatureVector implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private List<MaterialTechnicalReplicateFeatureVector> technicalReplicatesValues;

    // e.g. average or median of all replica values in this supgroup
    // This is the aggregation of a subgroup of replicas for e.g. the same SIRNA
    private float[] aggregatedSummary;

    // aggregation type of the subgroup summary
    private MaterialReplicaSummaryAggregationType summaryAggregationType;

    private String subgroupLabel;

    // GWT only
    @SuppressWarnings("unused")
    private MaterialBiologicalReplicateFeatureVector()
    {
    }

    public MaterialBiologicalReplicateFeatureVector(
            List<MaterialTechnicalReplicateFeatureVector> singleReplicaValues,
            float[] aggregatedSummary,
            MaterialReplicaSummaryAggregationType summaryAggregationType, String subgroupLabel)
    {
        this.technicalReplicatesValues = singleReplicaValues;
        this.aggregatedSummary = aggregatedSummary;
        this.summaryAggregationType = summaryAggregationType;
        this.subgroupLabel = subgroupLabel;
    }

    public List<MaterialTechnicalReplicateFeatureVector> getTechnicalReplicatesValues()
    {
        return technicalReplicatesValues;
    }

    public float[] getAggregatedSummary()
    {
        return aggregatedSummary;
    }

    public MaterialReplicaSummaryAggregationType getSummaryAggregationType()
    {
        return summaryAggregationType;
    }

    public String getSubgroupLabel()
    {
        return subgroupLabel;
    }
}
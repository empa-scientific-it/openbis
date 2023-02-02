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

package ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto;

import java.io.Serializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * @author Kaloyan Enimanev
 */
public class MaterialBiologicalReplicateFeatureSummary implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    // i-th element is the value for (i+1) technical replicate
    private float[] featureValues;

    // e.g. average or median of all replica values in this subgroup
    // DISPLAY NOTE: this is e.g. the "median" column for each subgroup of 3 replicas for the same
    // SIRNA in the prototype
    private float aggregatedSummary;

    // DISPLAY NOTE: this decides about the header of the subgroup summary (aggregatedSummaryfield).
    // For now only "Median", but "Average" will be added in future.
    private MaterialReplicaSummaryAggregationType summaryAggregationType;

    // GWT only
    @SuppressWarnings("unused")
    private MaterialBiologicalReplicateFeatureSummary()
    {
    }

    public MaterialBiologicalReplicateFeatureSummary(float[] featureValues, float aggregatedSummary,
            MaterialReplicaSummaryAggregationType summaryAggregationType)
    {
        this.featureValues = featureValues;
        this.aggregatedSummary = aggregatedSummary;
        this.summaryAggregationType = summaryAggregationType;
    }

    public MaterialReplicaSummaryAggregationType getSummaryAggregationType()
    {
        return summaryAggregationType;
    }

    public float[] getFeatureValues()
    {
        return featureValues;
    }

    public float getAggregatedSummary()
    {
        return aggregatedSummary;
    }

}

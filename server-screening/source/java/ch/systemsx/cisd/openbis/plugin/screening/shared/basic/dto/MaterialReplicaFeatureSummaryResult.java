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
import java.util.Collections;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Info about feature vectors of all wells for one material in one experiment.
 * 
 * @author Kaloyan Enimanev
 */
public class MaterialReplicaFeatureSummaryResult implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private final List<String> subgroupLabels;

    // represents a single row in the table
    private final List<MaterialReplicaFeatureSummary> replicaSummaries;

    private final int numberOfMaterialsInExperiment;

    public MaterialReplicaFeatureSummaryResult(List<String> subgroupLabels,
            List<MaterialReplicaFeatureSummary> replicaSummaries, int numberOfMaterialsInExperiment)
    {
        this.subgroupLabels = Collections.unmodifiableList(subgroupLabels);
        this.replicaSummaries = Collections.unmodifiableList(replicaSummaries);
        this.numberOfMaterialsInExperiment = numberOfMaterialsInExperiment;
    }

    public List<String> getSubgroupLabels()
    {
        return subgroupLabels;
    }

    /** Can be empty if there are no analysis results. */
    public List<MaterialReplicaFeatureSummary> getFeatureSummaries()
    {
        return replicaSummaries;
    }

    public int getNumberOfMaterialsInExperiment()
    {
        return numberOfMaterialsInExperiment;
    }
}

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
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeAndLabel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * @author Kaloyan Enimanev
 */
public class MaterialReplicaFeatureSummary implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private CodeAndLabel featureDescription;

    private double featureVectorSummary;

    private double featureVectorDeviation;

    private int featureVectorRank;

    // DISPLAY NOTE: The header of each column should be:
    // <subgroupName> repl. <replicaSequenceNumber>
    // e.g. SIRNA XYZ repl. 2
    private List<MaterialBiologicalReplicateFeatureSummary> biologicalRelicates;

    // technical replicates which do not belong to any biological replicate
    private MaterialBiologicalReplicateFeatureSummary technicalReplicates;

    public CodeAndLabel getFeatureDescription()
    {
        return featureDescription;
    }

    public void setFeatureDescription(CodeAndLabel featureDescription)
    {
        this.featureDescription = featureDescription;
    }

    public double getFeatureVectorSummary()
    {
        return featureVectorSummary;
    }

    public void setFeatureVectorSummary(double featueVectorSummary)
    {
        this.featureVectorSummary = featueVectorSummary;
    }

    public double getFeatureVectorDeviation()
    {
        return featureVectorDeviation;
    }

    public void setFeatureVectorDeviation(double featueVectorDeviation)
    {
        this.featureVectorDeviation = featueVectorDeviation;
    }

    public int getFeatureVectorRank()
    {
        return featureVectorRank;
    }

    public void setFeatureVectorRank(int featureVectorRank)
    {
        this.featureVectorRank = featureVectorRank;
    }

    public List<MaterialBiologicalReplicateFeatureSummary> getBiologicalRelicates()
    {
        return biologicalRelicates;
    }

    public void setBiologicalRelicates(
            List<MaterialBiologicalReplicateFeatureSummary> replicaSubgroups)
    {
        this.biologicalRelicates = replicaSubgroups;
    }

    public MaterialBiologicalReplicateFeatureSummary getDirectTechnicalReplicates()
    {
        return technicalReplicates;
    }

    public void setDirectTechnicalReplicates(
            MaterialBiologicalReplicateFeatureSummary defaultReplica)
    {
        this.technicalReplicates = defaultReplica;
    }

}

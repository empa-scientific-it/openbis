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

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeAndLabel;

/**
 * Feature vectors (details and summaries) for one material.
 * 
 * @author Tomasz Pylak
 */
public class MaterialAllReplicasFeatureVectors
{
    // NOTE: has the same length as feature vectors in all summaries
    private final List<CodeAndLabel> featureDescriptions;

    private final MaterialIdFeatureVectorSummary generalSummary;

    // NOTE: Can be empty.
    private final List<MaterialBiologicalReplicateFeatureVector> biologicalReplicates;

    // NOTE: Can be empty. Used for replicas which have no subgroups
    private final List<MaterialTechnicalReplicateFeatureVector> directTechnicalReplicates;

    public MaterialAllReplicasFeatureVectors(List<CodeAndLabel> featureDescriptions,
            MaterialIdFeatureVectorSummary generalSummary,
            List<MaterialBiologicalReplicateFeatureVector> subgroups,
            List<MaterialTechnicalReplicateFeatureVector> replicas)
    {
        this.featureDescriptions = featureDescriptions;
        this.generalSummary = generalSummary;
        this.biologicalReplicates = subgroups;
        this.directTechnicalReplicates = replicas;
    }

    public List<CodeAndLabel> getFeatureDescriptions()
    {
        return featureDescriptions;
    }

    public MaterialIdFeatureVectorSummary getGeneralSummary()
    {
        return generalSummary;
    }

    public List<MaterialBiologicalReplicateFeatureVector> getBiologicalReplicates()
    {
        return biologicalReplicates;
    }

    public List<MaterialTechnicalReplicateFeatureVector> getDirectTechnicalReplicates()
    {
        return directTechnicalReplicates;
    }
}
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

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeAndLabel;

/**
 * Aggregated feature vectors with its rankings in one experiment for all material.
 * 
 * @author Tomasz Pylak
 */
public class ExperimentFeatureVectorSummary
{
    private ExperimentReference experiment;

    private List<MaterialFeatureVectorSummary> materialsSummary;

    // has the same length as feature vectors in all material summaries
    private List<CodeAndLabel> featureDescriptions;

    public ExperimentFeatureVectorSummary(ExperimentReference experiment,
            List<MaterialFeatureVectorSummary> materialsSummary,
            List<CodeAndLabel> featureDescriptions)
    {
        this.experiment = experiment;
        this.materialsSummary = materialsSummary;
        this.featureDescriptions = featureDescriptions;
    }

    public ExperimentReference getExperiment()
    {
        return experiment;
    }

    public List<MaterialFeatureVectorSummary> getMaterialsSummary()
    {
        return materialsSummary;
    }

    public List<CodeAndLabel> getFeatureDescriptions()
    {
        return featureDescriptions;
    }

}
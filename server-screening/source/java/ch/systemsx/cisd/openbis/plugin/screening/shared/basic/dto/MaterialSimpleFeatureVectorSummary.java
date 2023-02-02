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
import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeAndLabel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Stores feature vector summaries for one material and experiment.
 * 
 * @author Tomasz Pylak
 */
public class MaterialSimpleFeatureVectorSummary implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private ExperimentReference experiment;

    // Has the same length as feature vectors summary.
    // Note that it can be empty if there are no analysis results!
    private List<CodeAndLabel> featureDescriptions;

    private float[] featureVectorSummary;

    private int[] featureVectorRanks;

    // GTW
    @SuppressWarnings("unused")
    private MaterialSimpleFeatureVectorSummary()
    {
    }

    /**
     * Useful when there are no analysis results for a material in the experiment (but there can be still some images acquired).
     */
    public MaterialSimpleFeatureVectorSummary(ExperimentReference experiment)
    {
        this(experiment, new ArrayList<CodeAndLabel>(), new float[0], new int[0]);
    }

    public MaterialSimpleFeatureVectorSummary(ExperimentReference experiment,
            List<CodeAndLabel> featureDescriptions, float[] featureVectorSummary,
            int[] featureVectorRanks)
    {
        this.experiment = experiment;
        this.featureDescriptions = featureDescriptions;
        this.featureVectorSummary = featureVectorSummary;
        this.featureVectorRanks = featureVectorRanks;
    }

    public ExperimentReference getExperiment()
    {
        return experiment;
    }

    public List<CodeAndLabel> getFeatureDescriptions()
    {
        return featureDescriptions;
    }

    public float[] getFeatureVectorSummary()
    {
        return featureVectorSummary;
    }

    public int[] getFeatureVectorRanks()
    {
        return featureVectorRanks;
    }
}

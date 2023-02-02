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
 * Aggregated feature vector with its ranking in one experiment for one material (represented as any object T).
 * 
 * @author Tomasz Pylak
 */
public class MaterialFeatureVectorSummaryParametrized<T> implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private T material;

    private int numberOfMaterialsInExperiment;

    // --- one element for each feature: summary, deviation and rank

    private float[] featureVectorSummary;

    private float[] featureVectorDeviationsOrNull;

    private int[] featureVectorRanks;

    // ---

    // GTW
    protected MaterialFeatureVectorSummaryParametrized()
    {
    }

    public MaterialFeatureVectorSummaryParametrized(T material, float[] featureVectorSummary,
            float[] featureVectorDeviationsOrNull, int[] featureVectorRanks,
            int numberOfMaterialsInExperiment)
    {
        this.material = material;
        this.featureVectorSummary = featureVectorSummary;
        this.featureVectorDeviationsOrNull = featureVectorDeviationsOrNull;
        this.featureVectorRanks = featureVectorRanks;
        this.numberOfMaterialsInExperiment = numberOfMaterialsInExperiment;
    }

    public T getMaterial()
    {
        return material;
    }

    public float[] getFeatureVectorSummary()
    {
        return featureVectorSummary;
    }

    public float[] tryGetFeatureVectorDeviations()
    {
        return featureVectorDeviationsOrNull;
    }

    public int[] getFeatureVectorRanks()
    {
        return featureVectorRanks;
    }

    public int getNumberOfMaterialsInExperiment()
    {
        return numberOfMaterialsInExperiment;
    }
}
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

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Aggregated feature vector with its ranking in one experiment for one material.
 * 
 * @author Tomasz Pylak
 */
public class MaterialFeatureVectorSummary extends
        MaterialFeatureVectorSummaryParametrized<Material>
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    // GTW
    @SuppressWarnings("unused")
    private MaterialFeatureVectorSummary()
    {
    }

    public MaterialFeatureVectorSummary(Material material, float[] featureVectorSummary,
            float[] featureVectorDeviationsOrNull, int[] featureVectorRanks,
            int numberOfMaterialsInExperiment)
    {
        super(material, featureVectorSummary, featureVectorDeviationsOrNull, featureVectorRanks,
                numberOfMaterialsInExperiment);
    }
}
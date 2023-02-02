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

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * A result object delivering a material feature vector summaries for all experiments.
 * 
 * @author Kaloyan Enimanev
 */
public class MaterialFeatureVectorsFromAllExperimentsResult implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private Material material;

    private List<MaterialSimpleFeatureVectorSummary> summaries;

    // GWT only
    @SuppressWarnings("unused")
    private MaterialFeatureVectorsFromAllExperimentsResult()
    {
    }

    public MaterialFeatureVectorsFromAllExperimentsResult(Material material,
            List<MaterialSimpleFeatureVectorSummary> summaries)
    {
        this.material = material;
        this.summaries = summaries;
    }

    public Material getMaterial()
    {
        return material;
    }

    public List<MaterialSimpleFeatureVectorSummary> getSummaries()
    {
        return summaries;
    }

}

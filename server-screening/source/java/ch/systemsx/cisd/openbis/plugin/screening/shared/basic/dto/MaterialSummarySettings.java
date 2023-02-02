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
import java.util.Arrays;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * @author Tomasz Pylak
 */
public class MaterialSummarySettings implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    /**
     * The name of a material property type that can be rendered as a link to a details view when a material is shown.
     */
    private String materialDetailsPropertyType;

    private List<String> featureCodes;

    private String[] replicaMatrialTypeSubstrings;

    // well property codes which links to the material which determines which biological replicate
    // it is, e.g. siRNA or compound
    private List<String> biologicalReplicatePropertyTypeCodes;

    private MaterialReplicaSummaryAggregationType aggregationType;

    public List<String> getFeatureCodes()
    {
        return featureCodes;
    }

    public void setFeatureCodes(List<String> featureCodes)
    {
        this.featureCodes = featureCodes;
    }

    public String[] getReplicaMaterialTypeSubstrings()
    {
        return replicaMatrialTypeSubstrings;
    }

    public void setReplicaMatrialTypePatterns(String[] replicaMatrialTypeSubstrings)
    {
        this.replicaMatrialTypeSubstrings = replicaMatrialTypeSubstrings;
    }

    public List<String> getBiologicalReplicatePropertyTypeCodes()
    {
        return biologicalReplicatePropertyTypeCodes;
    }

    public void setBiologicalReplicatePropertyTypeCodes(
            String... biologicalReplicatePropertyTypeCodes)
    {
        this.biologicalReplicatePropertyTypeCodes =
                Arrays.asList(biologicalReplicatePropertyTypeCodes);
    }

    public MaterialReplicaSummaryAggregationType getAggregationType()
    {
        return aggregationType;
    }

    public void setAggregationType(MaterialReplicaSummaryAggregationType aggregationType)
    {
        this.aggregationType = aggregationType;
    }

    public String getMaterialDetailsPropertyType()
    {
        return materialDetailsPropertyType;
    }

    public void setMaterialDetailsPropertyType(String materialDetailsProperty)
    {
        this.materialDetailsPropertyType = materialDetailsProperty;
    }
}

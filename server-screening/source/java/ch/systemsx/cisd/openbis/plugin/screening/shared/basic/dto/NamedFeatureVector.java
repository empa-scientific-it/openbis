/*
 * Copyright 2010 ETH Zuerich, CISD
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
 * Feature vector values and metadata (code and label)
 * 
 * @author Tomasz Pylak
 */
public class NamedFeatureVector implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    // Feature vector values, null if images have not been analyzed.
    // Some features may not be available, then the value is Float.NaN.
    // External data structure should be used to figure out which value corresponds to which
    // feature.
    private FeatureValue[] featureVectorValues;

    // codes of the features, indices should match with featureVectorValuesOrNull
    private String[] featureCodes;

    // labels of the features, indices should match with featureVectorValuesOrNull
    private String[] featureLabels;

    // GWT
    @SuppressWarnings("unused")
    private NamedFeatureVector()
    {
    }

    public NamedFeatureVector(FeatureValue[] featureVectorValues, String[] featureCodes,
            String[] featureLabels)
    {
        assert featureVectorValues.length == featureCodes.length
                && featureCodes.length == featureLabels.length : "There should be the same number of values and codes/labels";
        this.featureVectorValues = featureVectorValues;
        this.featureCodes = featureCodes;
        this.featureLabels = featureLabels;
    }

    public FeatureValue[] getValues()
    {
        return featureVectorValues;
    }

    public String[] getFeatureCodes()
    {
        return featureCodes;
    }

    public String[] getFeatureLabels()
    {
        return featureLabels;
    }

}

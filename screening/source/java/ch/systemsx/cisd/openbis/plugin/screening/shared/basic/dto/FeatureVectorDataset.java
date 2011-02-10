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

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.ISerializable;
import ch.systemsx.cisd.openbis.generic.shared.basic.annotation.DoNotEscape;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeAndLabel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Feature vector dataset with all the feature vectors.
 * 
 * @author Tomasz Pylak
 */
@DoNotEscape
public class FeatureVectorDataset implements ISerializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private DatasetReference datasetReference;

    private List<CodeAndLabel> featureNames;

    private List<FeatureVectorValues> datasetFeatures;

    // GWT only
    @SuppressWarnings("unused")
    private FeatureVectorDataset()
    {
    }

    public FeatureVectorDataset(DatasetReference datasetReference,
            List<FeatureVectorValues> datasetFeatures, List<CodeAndLabel> featureNames)
    {
        this.datasetReference = datasetReference;
        this.datasetFeatures = datasetFeatures;
        this.featureNames = featureNames;
    }

    public DatasetReference getDatasetReference()
    {
        return datasetReference;
    }

    public List<CodeAndLabel> getFeatureNames()
    {
        return featureNames;
    }

    public List<? extends FeatureVectorValues> getDatasetFeatures()
    {
        return datasetFeatures;
    }

}

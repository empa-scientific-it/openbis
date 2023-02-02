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
import java.util.LinkedList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.annotation.DoNotEscape;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeAndLabel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Feature vector dataset with all the feature vectors.
 * 
 * @author Tomasz Pylak
 */
@DoNotEscape
public class FeatureVectorDataset implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private DatasetReference datasetReference;

    private String analysisProcedure;

    private List<CodeAndLabel> featureNames;

    private List<FeatureVectorValues> datasetFeatures;

    private List<FeatureList> featureLists;

    // GWT only
    @SuppressWarnings("unused")
    private FeatureVectorDataset()
    {
    }

    public FeatureVectorDataset(DatasetReference datasetReference,
            List<FeatureVectorValues> datasetFeatures, List<FeatureList> featureLists,
            String analysisProcedure)
    {
        this.datasetReference = datasetReference;
        this.datasetFeatures = datasetFeatures;
        this.featureLists = featureLists;
        this.analysisProcedure = analysisProcedure;

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

    public String getAnalysisProcedure()
    {
        return analysisProcedure;
    }

    public List<CodeAndLabel> getFeatureNames()
    {
        if (featureNames == null && datasetFeatures != null && datasetFeatures.isEmpty() == false)
        {
            featureNames = new LinkedList<CodeAndLabel>();

            for (CodeAndLabel cal : datasetFeatures.get(0).getCodesAndLabels())
            {
                featureNames.add(cal);
            }
        }
        return featureNames;
    }

    public List<? extends FeatureVectorValues> getDatasetFeatures()
    {
        return datasetFeatures;
    }

    public List<FeatureList> getFeatureLists()
    {
        return featureLists;
    }
}

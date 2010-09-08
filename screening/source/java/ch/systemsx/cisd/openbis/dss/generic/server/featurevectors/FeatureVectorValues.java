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

package ch.systemsx.cisd.openbis.dss.generic.server.featurevectors;

import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellPosition;

/**
 * Bean for one feature vector of the well. Contains data set code, well position and and array of
 * feature values. Double.NaN is used for unknown feature value in this array.
 * 
 * @author Tomasz Pylak
 */
public class FeatureVectorValues
{
    private WellFeatureVectorReference featureVectorReference = new WellFeatureVectorReference();

    private float[] featureValues;

    public float[] getFeatureValues()
    {
        return featureValues;
    }

    public double[] getFeatureValuesAsDouble()
    {
        double[] doubleValues = new double[featureValues.length];
        for (int i = 0; i < featureValues.length; ++i)
        {
            doubleValues[i] = featureValues[i];
        }
        return doubleValues;
    }

    public void setFeatureValues(float[] featureValues)
    {
        this.featureValues = featureValues;
    }

    public String getDataSetCode()
    {
        return featureVectorReference.getDatasetCode();
    }

    public void setDataSetCode(String dataSetCode)
    {
        featureVectorReference.setDatasetCode(dataSetCode);
    }

    public void setWellPosition(WellPosition wellPosition)
    {
        featureVectorReference.setWellPosition(wellPosition);
    }

    public WellPosition getWellPosition()
    {
        return featureVectorReference.getWellPosition();
    }

}

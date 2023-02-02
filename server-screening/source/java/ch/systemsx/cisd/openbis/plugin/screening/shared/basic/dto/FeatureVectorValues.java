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
import java.util.LinkedHashMap;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.shared.basic.annotation.DoNotEscape;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeAndLabel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Bean for one feature vector of the well. Contains data set code, well position and a map of feature values. Float.NaN is used for unknown feature
 * value in this array.
 * 
 * @author Tomasz Pylak
 */
@DoNotEscape
public class FeatureVectorValues implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private PlateWellFeatureVectorReference featureVectorReference;

    private CodeAndLabel[] codesAndLabels;

    private FeatureValue[] featureValues;

    // The map is lazily initialized
    private Map<String /* feature label */, FeatureValue /* value */> featureMap;

    /**
     * Convert the two arrays into a map. It is assumed that codesAndLabels has the same length as feature values (this is checked before calling this
     * method).
     */
    static private Map<String, FeatureValue> asValueMap(CodeAndLabel[] codesAndLabels,
            FeatureValue[] featureValues)
    {
        Map<String, FeatureValue> result = new LinkedHashMap<String, FeatureValue>();
        for (int i = 0; i < featureValues.length; i++)
        {
            result.put(codesAndLabels[i].getLabel(), featureValues[i]);
        }
        return result;
    }

    // GWT only
    @SuppressWarnings("unused")
    private FeatureVectorValues()
    {
    }

    /**
     * A copy constructor.
     * 
     * @param featureVector The object to copy.
     */
    public FeatureVectorValues(FeatureVectorValues featureVector)
    {
        this(featureVector.getFeatureVectorReference(), featureVector.getCodesAndLabels(),
                featureVector.getFeatureValues(), featureVector.tryFeatureMap());
    }

    public FeatureVectorValues(String dataSetCode, WellLocation wellLocation, String platePermId,
            CodeAndLabel[] codesAndLabels, FeatureValue[] featureValues)
    {
        this(new PlateWellFeatureVectorReference(dataSetCode, wellLocation, platePermId),
                codesAndLabels, featureValues);
    }

    public FeatureVectorValues(PlateWellFeatureVectorReference featureVectorReference,
            CodeAndLabel[] codesAndLabels, FeatureValue[] featureValues)
    {
        this(featureVectorReference, codesAndLabels, featureValues, null);
    }

    private FeatureVectorValues(PlateWellFeatureVectorReference featureVectorReference,
            CodeAndLabel[] codesAndLabels, FeatureValue[] featureValues,
            Map<String, FeatureValue> featureMap)
    {
        assert codesAndLabels.length == featureValues.length;
        this.featureVectorReference = featureVectorReference;
        this.codesAndLabels = codesAndLabels;
        this.featureValues = featureValues;
        this.featureMap = featureMap;

    }

    public CodeAndLabel[] getCodesAndLabels()
    {
        return codesAndLabels;
    }

    public FeatureValue[] getFeatureValues()
    {
        return featureValues;
    }

    public Map<String, FeatureValue> getFeatureMap()
    {
        // Lazily initialize the map
        if (null == featureMap)
        {
            featureMap = asValueMap(codesAndLabels, featureValues);
        }
        return featureMap;
    }

    /**
     * An internal method that returns the feature map without triggering an initialization. Used by the copy constructor.
     */
    private Map<String, FeatureValue> tryFeatureMap()
    {
        return featureMap;
    }

    public String getDataSetCode()
    {
        return featureVectorReference.getDatasetCode();
    }

    public WellLocation getWellLocation()
    {
        return featureVectorReference.getWellLocation();
    }

    public String getPlatePermId()
    {
        return featureVectorReference.getPlatePermId();
    }

    public PlateWellFeatureVectorReference getFeatureVectorReference()
    {
        return featureVectorReference;
    }

    public WellReference getWellReference()
    {
        return new WellReference(getWellLocation(), getPlatePermId());
    }
}

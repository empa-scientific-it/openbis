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

package ch.systemsx.cisd.openbis.dss.etl.dto.api.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ch.systemsx.cisd.openbis.dss.etl.dto.api.IFeatureDefinition;
import ch.systemsx.cisd.openbis.dss.etl.featurevector.CanonicalFeatureVector;
import ch.systemsx.cisd.openbis.dss.etl.featurevector.FeatureValuesMap;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Geometry;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.dto.PlateFeatureValues;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureDefDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureValuesDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureVocabularyTermDTO;

/**
 * Values of one feature for all wells + the way to build this structure.
 * 
 * @author Tomasz Pylak
 */
public class FeatureDefinition implements IFeatureDefinition, Serializable
{
    private static final long serialVersionUID = 1L;

    private final ImgFeatureDefDTO imgFeatureDefDTO;

    private final List<FeatureValuesMap> values;

    private FeatureValuesMap currentFeatureVector;

    public FeatureDefinition(ImgFeatureDefDTO imgFeatureDefDTO)
    {
        this(imgFeatureDefDTO, new FeatureValuesMap(null, null));
    }

    public FeatureDefinition(ImgFeatureDefDTO imgFeatureDefDTO,
            FeatureValuesMap currentFeatureVector)
    {
        assert imgFeatureDefDTO != null : "featureDefinition is null";
        this.imgFeatureDefDTO = imgFeatureDefDTO;
        this.values = new ArrayList<FeatureValuesMap>();
        this.currentFeatureVector = currentFeatureVector;
    }

    @Override
    public void changeSeries(Double timeOrNull, Double depthOrNull)
    {
        flushCurrent();
        currentFeatureVector = new FeatureValuesMap(timeOrNull, depthOrNull);
    }

    private void flushCurrent()
    {
        if (currentFeatureVector != null && currentFeatureVector.isEmpty() == false)
        {
            values.add(currentFeatureVector);
            currentFeatureVector = null;
        }
    }

    /** Optional. Sets the label of a feature. */
    @Override
    public void setFeatureLabel(String label)
    {
        this.imgFeatureDefDTO.setLabel(label);
    }

    public String getFeatureLabel()
    {
        return imgFeatureDefDTO.getLabel();
    }

    /** Optional. Sets description of a feature. */
    @Override
    public void setFeatureDescription(String description)
    {
        this.imgFeatureDefDTO.setDescription(description);
    }

    /**
     * @param well code of the well, e.g. A1
     * @param value value of the feature in the specified well
     */
    @Override
    public void addValue(String well, String value)
    {
        WellLocation wellPos = WellLocation.parseLocationStr(well);
        addValueToCurrent(value, wellPos);
    }

    /**
     * @param wellRow row coordinate of the well, top-left well has (1,1) coordinates.
     * @param wellColumn column coordinate of the well, top-left well has (1,1) coordinates.
     * @param value value of the feature in the specified well
     */
    @Override
    public void addValue(int wellRow, int wellColumn, String value)
    {
        WellLocation wellPos = new WellLocation(wellRow, wellColumn);
        addValueToCurrent(value, wellPos);
    }

    private void addValueToCurrent(String value, WellLocation wellPos)
    {
        currentFeatureVector.addValue(value, wellPos);
    }

    private void validate(Geometry plateGeometry)
    {
        for (FeatureValuesMap valuesMap : values)
        {
            valuesMap.validate(plateGeometry);
        }
    }

    // ----- converter

    /** @return feature vector in a canonical form with all the values added so far. */
    public CanonicalFeatureVector getCanonicalFeatureVector(Geometry plateGeometry)
    {
        flushCurrent();
        validate(plateGeometry);

        CanonicalFeatureVector canonicalFeatureVector = new CanonicalFeatureVector();
        canonicalFeatureVector.setFeatureDef(imgFeatureDefDTO);

        Set<String> uniqueValues = getUniqueAvailableValues();
        Map<String, Integer/* value sequence number */> termToSequanceMap =
                fixVocabularyTermSequences(uniqueValues);

        List<ImgFeatureVocabularyTermDTO> vocabularyTerms = null;
        List<Map<WellLocation, Float>> floatValuesList = tryCreateFloatValueList();
        if (floatValuesList == null)
        {
            floatValuesList = calculateWellTermsMappingList(termToSequanceMap);
            vocabularyTerms = tryCreateVocabularyTerms(termToSequanceMap);
        }
        List<ImgFeatureValuesDTO> featureDTOs = createValueDTOs(plateGeometry, floatValuesList);
        canonicalFeatureVector.setValues(featureDTOs);
        canonicalFeatureVector.setVocabularyTerms(vocabularyTerms);
        return canonicalFeatureVector;
    }

    private List<ImgFeatureValuesDTO> createValueDTOs(Geometry plateGeometry,
            List<Map<WellLocation, Float>> floatValuesList)
    {
        List<ImgFeatureValuesDTO> featureDTOs = new ArrayList<ImgFeatureValuesDTO>();
        for (int i = 0; i < values.size(); i++)
        {
            FeatureValuesMap featureValuesMap = values.get(i);
            Map<WellLocation, Float> floatValues = floatValuesList.get(i);
            ImgFeatureValuesDTO featureValuesDTO =
                    createFeatureValuesDTO(plateGeometry, featureValuesMap, floatValues);
            featureDTOs.add(featureValuesDTO);
        }
        return featureDTOs;
    }

    private static ImgFeatureValuesDTO createFeatureValuesDTO(Geometry plateGeometry,
            FeatureValuesMap featureValuesMap, Map<WellLocation, Float> floatValues)
    {
        final PlateFeatureValues valuesValues =
                convertColumnToByteArray(plateGeometry, floatValues);
        ImgFeatureValuesDTO featureValuesDTO =
                new ImgFeatureValuesDTO(featureValuesMap.tryGetTime(),
                        featureValuesMap.tryGetDepth(), valuesValues, 0);
        return featureValuesDTO;
    }

    private static PlateFeatureValues convertColumnToByteArray(Geometry geometry,
            Map<WellLocation, Float> values)
    {
        final PlateFeatureValues featureValues = new PlateFeatureValues(geometry);
        for (WellLocation loc : values.keySet())
        {
            final Float value = values.get(loc);
            featureValues.setForWellLocation(value, loc);
        }
        return featureValues;
    }

    private List<Map<WellLocation, Float>> calculateWellTermsMappingList(
            Map<String, Integer> termToSequanceMap)
    {
        List<Map<WellLocation, Float>> list = new ArrayList<Map<WellLocation, Float>>();
        for (FeatureValuesMap featureValuesMap : values)
        {
            Map<WellLocation, Float> floatValues =
                    featureValuesMap.calculateWellTermsMapping(termToSequanceMap);
            list.add(floatValues);
        }
        return list;
    }

    private List<Map<WellLocation, Float>> tryCreateFloatValueList()
    {
        List<Map<WellLocation, Float>> list = new ArrayList<Map<WellLocation, Float>>();
        for (FeatureValuesMap featureValuesMap : values)
        {
            Map<WellLocation, Float> floatValues = featureValuesMap.tryExtractFloatValues();
            if (floatValues == null)
            {
                return null;
            }
            list.add(floatValues);
        }
        return list;
    }

    private static List<ImgFeatureVocabularyTermDTO> tryCreateVocabularyTerms(
            Map<String, Integer> valueToSequanceMap)
    {
        if (valueToSequanceMap.isEmpty())
        {
            return null;
        }
        List<ImgFeatureVocabularyTermDTO> vocabularyTerms =
                new ArrayList<ImgFeatureVocabularyTermDTO>();
        for (Entry<String, Integer> entry : valueToSequanceMap.entrySet())
        {
            vocabularyTerms.add(new ImgFeatureVocabularyTermDTO(entry.getKey(), entry.getValue()));
        }
        return vocabularyTerms;
    }

    private static Map<String, Integer/* value sequence number */> fixVocabularyTermSequences(
            Set<String> uniqueValues)
    {
        Map<String, Integer> valueToSequanceMap = new HashMap<String, Integer>();
        int sequenceNumber = 0;
        for (String value : uniqueValues)
        {
            valueToSequanceMap.put(value, sequenceNumber++);
        }
        return valueToSequanceMap;
    }

    private Set<String> getUniqueAvailableValues()
    {
        List<FeatureValuesMap> valuesMaps = values;
        Set<String> uniqueValues = new HashSet<String>();
        for (FeatureValuesMap valuesMap : valuesMaps)
        {
            uniqueValues.addAll(valuesMap.getUniqueAvailableValues());
        }
        return uniqueValues;
    }

}

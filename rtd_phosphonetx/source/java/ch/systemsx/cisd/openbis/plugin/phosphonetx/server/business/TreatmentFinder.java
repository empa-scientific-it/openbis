/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.server.business;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityDataType;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.Treatment;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class TreatmentFinder
{
    private static final String TREATMENT_TYPE_CODE = "TREATMENT_TYPE";
    private static final String TREATMENT_VALUE_CODE = "TREATMENT_VALUE";
    
    public List<Treatment> findTreatmentsOf(SamplePE sample)
    {
        List<Treatment> treatments = new ArrayList<Treatment>();
        findAndAddTreatments(treatments, sample);
        return treatments;
    }
    
    private void findAndAddTreatments(List<Treatment> treatments, SamplePE sampleOrNull)
    {
        if (sampleOrNull == null)
        {
            return;
        }
        findAndAddTreatments(treatments, sampleOrNull.getGeneratedFrom());
        Set<SamplePropertyPE> properties = sampleOrNull.getProperties();
        Map<String, Treatment> codeTreatmentMap = new HashMap<String, Treatment>();
        for (SamplePropertyPE property : properties)
        {
            PropertyTypePE propertyType = property.getEntityTypePropertyType().getPropertyType();
            String code = propertyType.getCode();
            if (code.startsWith(TREATMENT_TYPE_CODE))
            {
                String treatmentCode = code.substring(TREATMENT_TYPE_CODE.length());
                VocabularyTermPE vocabularyTerm = property.getVocabularyTerm();
                if (vocabularyTerm == null)
                {
                    throw new UserFailureException("Data type of property type '" + code
                            + "' must be a vocabulary.");
                }
                String label = getLabelOrCode(vocabularyTerm);
                Treatment treatment = getOrCreateTreatment(codeTreatmentMap, treatmentCode);
                treatment.setType(label);
            } else if (code.startsWith(TREATMENT_VALUE_CODE))
            {
                String treatmentCode = code.substring(TREATMENT_VALUE_CODE.length());
                Treatment treatment = getOrCreateTreatment(codeTreatmentMap, treatmentCode);
                EntityDataType dataType = propertyType.getType().getCode();
                treatment.setValueType(dataType.toString());
                String value = getValue(property);
                treatment.setValue(value);
            }
        }
        treatments.addAll(codeTreatmentMap.values());
    }

    private String getValue(SamplePropertyPE property)
    {
        MaterialPE material = property.getMaterialValue();
        if (material != null)
        {
            return material.getCode();
        }
        VocabularyTermPE vocabularyTerm = property.getVocabularyTerm();
        return vocabularyTerm == null ? property.getValue() : getLabelOrCode(vocabularyTerm);
    }

    private String getLabelOrCode(VocabularyTermPE vocabularyTerm)
    {
        String label = vocabularyTerm.getLabel();
        if (StringUtils.isBlank(label))
        {
            label = vocabularyTerm.getCode();
        }
        return label;
    }

    private Treatment getOrCreateTreatment(Map<String, Treatment> codeTreatmentMap,
            String treatmentCode)
    {
        Treatment treatment = codeTreatmentMap.get(treatmentCode);
        if (treatment == null)
        {
            treatment = new Treatment();
            treatment.setType("");
            treatment.setValue("");
            codeTreatmentMap.put(treatmentCode, treatment);
        }
        return treatment;
    }
}

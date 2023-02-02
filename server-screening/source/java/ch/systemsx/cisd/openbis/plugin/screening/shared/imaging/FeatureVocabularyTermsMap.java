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

package ch.systemsx.cisd.openbis.plugin.screening.shared.imaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureValue;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureDefDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureVocabularyTermDTO;

/**
 * Internal utils class for operations on vocabulary terms of all dataset features.
 * 
 * @author Tomasz Pylak
 */
class FeatureVocabularyTermsMap
{
    /**
     * Constructs a {@link FeatureVocabularyTermsMap} from all the vocabulary terms definitions of the dataset. A constructed map can provide a term
     * definition for a given dataset feature definition and term sequence number.
     */
    public static FeatureVocabularyTermsMap createVocabularyTermsMap(
            List<ImgFeatureVocabularyTermDTO> allTerms, Set<ImgFeatureDefDTO> datasetFeatureDefs)
    {
        Map<Long, List<ImgFeatureVocabularyTermDTO>> featureToTermsMap =
                createFeatureToTermsMap(allTerms);
        return new FeatureVocabularyTermsMap(createSequenceNumberMap(featureToTermsMap),
                createFeatureCodeToIdMap(datasetFeatureDefs));
    }

    private static Map<String, Long> createFeatureCodeToIdMap(
            Set<ImgFeatureDefDTO> datasetFeatureDefs)
    {
        Map<String, Long> codeToIdMap = new HashMap<String, Long>();
        for (ImgFeatureDefDTO featureDef : datasetFeatureDefs)
        {
            codeToIdMap.put(featureDef.getCode(), featureDef.getId());
        }
        return codeToIdMap;
    }

    private static Map<Long, Map<Integer, FeatureValue>> createSequenceNumberMap(
            Map<Long, List<ImgFeatureVocabularyTermDTO>> featureToTermsMap)
    {
        Map<Long, Map<Integer, FeatureValue>> map = new HashMap<Long, Map<Integer, FeatureValue>>();

        for (Entry<Long, List<ImgFeatureVocabularyTermDTO>> entry : featureToTermsMap.entrySet())
        {
            Long featureDefId = entry.getKey();
            List<ImgFeatureVocabularyTermDTO> featureTerms = entry.getValue();
            Map<Integer, FeatureValue> sequenceToTermMap = new HashMap<Integer, FeatureValue>();
            for (ImgFeatureVocabularyTermDTO term : featureTerms)
            {
                FeatureValue vocabularyTermValue =
                        FeatureValue.createVocabularyTerm(term.getCode());
                sequenceToTermMap.put(term.getSequenceNumber(), vocabularyTermValue);
            }
            map.put(featureDefId, sequenceToTermMap);
        }
        return map;
    }

    private static Map<Long, List<ImgFeatureVocabularyTermDTO>> createFeatureToTermsMap(
            List<ImgFeatureVocabularyTermDTO> allTerms)
    {
        Map<Long, List<ImgFeatureVocabularyTermDTO>> map =
                new HashMap<Long, List<ImgFeatureVocabularyTermDTO>>();

        for (ImgFeatureVocabularyTermDTO term : allTerms)
        {
            long featureDefId = term.getFeatureDefId();
            List<ImgFeatureVocabularyTermDTO> featureTerms = map.get(featureDefId);
            if (featureTerms == null)
            {
                featureTerms = new ArrayList<ImgFeatureVocabularyTermDTO>();
            }
            featureTerms.add(term);
            map.put(featureDefId, featureTerms);
        }
        return map;
    }

    // --------------------

    private final Map<Long/* feature def id */, Map<Integer/* term sequence number */, FeatureValue>> featureDefToVocabularyTerms;

    private final Map<String/* feature code */, Long/* feature def id */> featureCodeToIdMap;

    private FeatureVocabularyTermsMap(
            Map<Long, Map<Integer, FeatureValue>> featureDefToVocabularyTerms,
            Map<String, Long> featureCodeToIdMap)
    {
        this.featureDefToVocabularyTerms = featureDefToVocabularyTerms;
        this.featureCodeToIdMap = featureCodeToIdMap;
    }

    public boolean hasVocabularyTerms(String featureCode)
    {
        Long featureId = featureCodeToIdMap.get(featureCode);
        if (featureId == null)
        {
            return false;
        }
        return hasVocabularyTerms(featureId);
    }

    public boolean hasVocabularyTerms(long featureDefId)
    {
        return featureDefToVocabularyTerms.get(featureDefId) != null;
    }

    /**
     * Fails if the specified feature has no vocabularies or a term with a specified sequence does not exist.
     */
    public FeatureValue getVocabularyTerm(long featureDefId, int termSequenceNumber)
    {
        Map<Integer, FeatureValue> termsMap = featureDefToVocabularyTerms.get(featureDefId);
        assert termsMap != null : "Feature def " + featureDefId + " has no vocabulary terms";

        FeatureValue term = termsMap.get(termSequenceNumber);
        assert term != null : "Cannot find term " + termSequenceNumber + " for feature def "
                + featureDefId;
        return term;
    }
}
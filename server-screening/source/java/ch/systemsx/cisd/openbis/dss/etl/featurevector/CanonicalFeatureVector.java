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

package ch.systemsx.cisd.openbis.dss.etl.featurevector;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureDefDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureValuesDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureVocabularyTermDTO;

/**
 * Image feature vectors stored in a standardized form.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class CanonicalFeatureVector
{
    private ImgFeatureDefDTO featureDef;

    private List<ImgFeatureValuesDTO> values = new ArrayList<ImgFeatureValuesDTO>();

    private List<ImgFeatureVocabularyTermDTO> vocabularyTerms =
            new ArrayList<ImgFeatureVocabularyTermDTO>();

    public CanonicalFeatureVector()
    {

    }

    /**
     * The feature def for this feature vector. If the feature def has not yet been commited to the DB, then it has no ID.
     */
    public ImgFeatureDefDTO getFeatureDef()
    {
        return featureDef;
    }

    public void setFeatureDef(ImgFeatureDefDTO featureDef)
    {
        this.featureDef = featureDef;
    }

    /**
     * The feature values for this feature. If the feature values have not yet been commited to the DB, then FK to the feature def will not be valid
     * and PK will not be valid as well.
     */
    public List<ImgFeatureValuesDTO> getValues()
    {
        return values;
    }

    /**
     * The vocabulary terms for this feature if it is not numerical, empty list otherwise. If the terms have not yet been commited to the DB, then FK
     * to the feature def will not be valid. The ids of the terms are NOT valid and are never set after the commit.
     */
    public List<ImgFeatureVocabularyTermDTO> getVocabularyTerms()
    {
        return vocabularyTerms;
    }

    public void setValues(List<ImgFeatureValuesDTO> values)
    {
        this.values = values;
    }

    public void setVocabularyTerms(List<ImgFeatureVocabularyTermDTO> vocabularyTerms)
    {
        this.vocabularyTerms = vocabularyTerms;
    }
}

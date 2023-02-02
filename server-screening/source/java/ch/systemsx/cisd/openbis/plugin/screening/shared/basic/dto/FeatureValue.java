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
 * Stores feature value: float or vocabulary term value.
 * 
 * @author Tomasz Pylak
 */
public class FeatureValue implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private static final FeatureValue EMPTY_VOCABULARY_TERM = new FeatureValue(null, null, true);

    private static final FeatureValue EMPTY_FLOAT_VALUE = new FeatureValue(Float.NaN, null, false);

    public static final FeatureValue createVocabularyTerm(String value)
    {
        assert value != null : "vocabulary value is null";
        return new FeatureValue(null, value, true);
    }

    public static final FeatureValue createEmptyVocabularyTerm()
    {
        return EMPTY_VOCABULARY_TERM;
    }

    public static final FeatureValue createEmptyFloat()
    {
        return EMPTY_FLOAT_VALUE;
    }

    public static final FeatureValue createFloat(float value)
    {
        return new FeatureValue(value, null, false);
    }

    // ----------------

    private Float floatValueOrNull;

    private String vocabularyTermValueOrNull;

    private boolean isVocabulary;

    // GWT only
    private FeatureValue()
    {
    }

    private FeatureValue(Float floatValueOrNull, String vocabularyTermValueOrNull,
            boolean isVocabulary)
    {
        assert (isVocabulary && floatValueOrNull == null)
                || (isVocabulary == false && floatValueOrNull != null && vocabularyTermValueOrNull == null) : "invalid FeatureValue";

        this.floatValueOrNull = floatValueOrNull;
        this.vocabularyTermValueOrNull = vocabularyTermValueOrNull;
        this.isVocabulary = isVocabulary;
    }

    public boolean isFloat()
    {
        return isVocabularyTerm() == false;
    }

    /** Call only if you are sure that you are dealing with the float value */
    public float asFloat()
    {
        assert isFloat() : "value is not a float";
        return floatValueOrNull;
    }

    public boolean isVocabularyTerm()
    {
        return isVocabulary;
    }

    /**
     * Call only if you are sure that you are dealing with the vocabulary term value
     * 
     * @return null if the value for vocabulary has not been specified
     */
    public String tryAsVocabularyTerm()
    {
        assert isVocabularyTerm() : "value is not a vocabulary";
        return vocabularyTermValueOrNull;
    }

    @Override
    public String toString()
    {
        if (isVocabularyTerm())
        {
            String term = tryAsVocabularyTerm();
            return term != null ? term : "";
        } else if (isFloat())
        {
            return "" + asFloat();
        } else
        {
            throw new IllegalStateException("unknown value");
        }
    }
}
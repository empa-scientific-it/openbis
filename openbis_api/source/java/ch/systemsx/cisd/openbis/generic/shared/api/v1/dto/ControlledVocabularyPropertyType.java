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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.dto;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;

/**
 * @author Chandrasekhar Ramakrishnan
 */
@SuppressWarnings("unused")
@JsonObject("ControlledVocabularyPropertyType")
public class ControlledVocabularyPropertyType extends PropertyType
{
    private static final long serialVersionUID = 1L;

    @JsonObject("VocabularyTermProperty")
    public static class VocabularyTerm extends
            ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.VocabularyTerm
    {
        private static final long serialVersionUID = 1L;

        public VocabularyTerm(String code, String label, String description, Long ordinal, Boolean isOfficial,
                EntityRegistrationDetails registrationDetails)
        {
            super(code, label, description, ordinal, isOfficial, registrationDetails);
        }

        //
        // JSON-RPC
        //
        private VocabularyTerm()
        {
        }

    }

    public static class ControlledVocabularyPropertyTypeInitializer extends PropertyTypeInitializer
    {
        private final ArrayList<VocabularyTerm> terms = new ArrayList<VocabularyTerm>();

        private Vocabulary vocabulary;

        public void setTerms(List<VocabularyTerm> validValues)
        {
            this.terms.clear();
            this.terms.addAll(validValues);
        }

        public void setVocabulary(Vocabulary vocabulary)
        {
            this.vocabulary = vocabulary;
        }
    }

    private ArrayList<VocabularyTerm> terms;

    private Vocabulary vocabulary;

    /**
     * @param initializer
     */
    public ControlledVocabularyPropertyType(ControlledVocabularyPropertyTypeInitializer initializer)
    {
        super(initializer);
        terms = initializer.terms;
        vocabulary = initializer.vocabulary;
    }

    public List<VocabularyTerm> getTerms()
    {
        return terms;
    }

    public Vocabulary getVocabulary()
    {
        return vocabulary;
    }

    @Override
    protected void appendFieldsToStringBuilder(ToStringBuilder builder)
    {
        builder.append(terms);
    }

    //
    // JSON-RPC
    //

    private ControlledVocabularyPropertyType()
    {
    }

    private void setTerms(ArrayList<VocabularyTerm> terms)
    {
        this.terms = terms;
    }

    private void setVocabulary(Vocabulary vocabulary)
    {
        this.vocabulary = vocabulary;
    }
}

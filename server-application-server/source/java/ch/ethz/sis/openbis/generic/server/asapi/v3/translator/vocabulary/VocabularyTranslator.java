/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.vocabulary;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.Vocabulary;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.VocabularyTerm;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyFetchOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationResults;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;

/**
 * @author pkupczyk
 */
@Component
public class VocabularyTranslator extends AbstractCachingTranslator<Long, Vocabulary, VocabularyFetchOptions> implements IVocabularyTranslator
{

    @Autowired
    private IVocabularyBaseTranslator baseTranslator;

    @Autowired
    private IVocabularyRegistratorTranslator registratorTranslator;
    
    @Autowired
    private IVocabularyVocabularyTermTranslator termTranslator;

    @Override
    protected Vocabulary createObject(TranslationContext context, Long vocabularyId, VocabularyFetchOptions fetchOptions)
    {
        Vocabulary vocabulary = new Vocabulary();
        vocabulary.setFetchOptions(new VocabularyFetchOptions());
        return vocabulary;
    }

    @Override
    protected Object getObjectsRelations(TranslationContext context, Collection<Long> vocabularyIds, VocabularyFetchOptions fetchOptions)
    {
        TranslationResults relations = new TranslationResults();

        relations.put(IVocabularyBaseTranslator.class, baseTranslator.translate(context, vocabularyIds, null));

        if (fetchOptions.hasRegistrator())
        {
            relations.put(IVocabularyRegistratorTranslator.class,
                    registratorTranslator.translate(context, vocabularyIds, fetchOptions.withRegistrator()));
        }
        if (fetchOptions.hasTerms())
        {
            relations.put(IVocabularyVocabularyTermTranslator.class, termTranslator.translate(context, vocabularyIds, fetchOptions.withTerms()));
        }

        return relations;
    }

    @Override
    protected void updateObject(TranslationContext context, Long vocabularyId, Vocabulary result, Object objectRelations,
            VocabularyFetchOptions fetchOptions)
    {
        TranslationResults relations = (TranslationResults) objectRelations;
        VocabularyBaseRecord baseRecord = relations.get(IVocabularyBaseTranslator.class, vocabularyId);

        result.setCode(CodeConverter.tryToBusinessLayer(baseRecord.code, baseRecord.isManagedInternally));
        result.setDescription(baseRecord.description);
        result.setModificationDate(baseRecord.modificationDate);
        result.setRegistrationDate(baseRecord.registrationDate);
        result.setManagedInternally(baseRecord.isManagedInternally);
        result.setChosenFromList(baseRecord.isChosenFromList);
        result.setUrlTemplate(baseRecord.urlTemplate);

        if (fetchOptions.hasRegistrator())
        {
            result.setRegistrator(relations.get(IVocabularyRegistratorTranslator.class, vocabularyId));
            result.getFetchOptions().withRegistratorUsing(fetchOptions.withRegistrator());
        }
        if (fetchOptions.hasTerms())
        {
            result.setTerms((List<VocabularyTerm>) relations.get(IVocabularyVocabularyTermTranslator.class, vocabularyId));
            result.getFetchOptions().withTermsUsing(fetchOptions.withTerms());
        }
    }

}

/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.vocabulary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.Validator;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CodeField.CodeFieldKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;

/**
 * A {@link Validator} implementation which validates vocabulary terms in a text area.
 * 
 * @author Christian Ribeaud
 */
final class VocabularyTermValidator implements Validator
{
    private final IMessageProvider messageProvider;

    private final Set<String> existingTerms;

    VocabularyTermValidator(final IMessageProvider messageProvider)
    {
        this(messageProvider, Collections.<VocabularyTerm> emptyList());
    }

    public VocabularyTermValidator(final IMessageProvider messageProvider,
            List<VocabularyTerm> terms)
    {
        this.messageProvider = messageProvider;
        existingTerms = new HashSet<String>();
        for (VocabularyTerm vocabularyTerm : terms)
        {
            existingTerms.add(vocabularyTerm.getCode());
        }
    }

    final static List<VocabularyTerm> getTerms(final String value)
    {
        final List<VocabularyTerm> terms = new ArrayList<VocabularyTerm>();
        if (StringUtils.isBlank(value) == false)
        {
            final String[] split = value.split("[,\n\r\t\f ]");
            for (final String text : split)
            {
                if (StringUtils.isBlank(text) == false)
                {
                    VocabularyTerm term = new VocabularyTerm();
                    term.setCode(text);
                    terms.add(term);
                }
            }
        }
        return terms;
    }

    @Override
    final public String validate(Field<?> field, String value)
    {
        final List<VocabularyTerm> terms = VocabularyTermValidator.getTerms(value);
        for (final VocabularyTerm term : terms)
        {
            CodeFieldKind codeKind = CodeFieldKind.CODE_WITH_COLON;
            if (term.getCode().matches(codeKind.getPattern()) == false)
            {
                return messageProvider.getMessage(Dict.INVALID_CODE_MESSAGE,
                        codeKind.getAllowedCharacters());
            }
            if (existingTerms.contains(term.getCode().toUpperCase()))
            {
                return messageProvider.getMessage(Dict.VOCABULARY_TERMS_VALIDATION_MESSAGE, term);
            }
        }
        return null;
    }
}

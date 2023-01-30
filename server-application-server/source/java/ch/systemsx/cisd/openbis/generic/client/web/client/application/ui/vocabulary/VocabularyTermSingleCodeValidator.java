package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.vocabulary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CodeField.CodeFieldKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;

import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.Validator;

/**
 * A {@link Validator} implementation which validates a vocabulary term code in a text area.
 * 
 * @author Juan Fuentes
 */
final class VocabularyTermSingleCodeValidator implements Validator
{
    private final IMessageProvider messageProvider;

    private final Set<String> existingTerms;

    VocabularyTermSingleCodeValidator(final IMessageProvider messageProvider)
    {
        this(messageProvider, Collections.<VocabularyTerm> emptyList());
    }

    public VocabularyTermSingleCodeValidator(final IMessageProvider messageProvider,
            List<VocabularyTerm> terms)
    {
        this.messageProvider = messageProvider;
        existingTerms = new HashSet<String>();
        for (VocabularyTerm vocabularyTerm : terms)
        {
            existingTerms.add(vocabularyTerm.getCode());
        }
    }

    final static VocabularyTerm getTerm(final String value)
    {
        final List<VocabularyTerm> terms = new ArrayList<VocabularyTerm>();
        if (StringUtils.isBlank(value) == false)
        {
            VocabularyTerm term = new VocabularyTerm();
            term.setCode(value);
            terms.add(term);
            return term;
        } else
        {
            return null;
        }
    }

    @Override
    final public String validate(Field<?> field, String value)
    {
        final VocabularyTerm term = VocabularyTermSingleCodeValidator.getTerm(value);
        if (term == null)
        {
            return messageProvider.getMessage(Dict.MISSING_VOCABULARY_TERMS);
        }
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
        return null;
    }
}

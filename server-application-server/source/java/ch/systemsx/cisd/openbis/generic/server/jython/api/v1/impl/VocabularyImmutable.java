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

package ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IVocabularyImmutable;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IVocabularyTermImmutable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;

/**
 * @author Kaloyan Enimanev
 */
public class VocabularyImmutable implements IVocabularyImmutable
{

    private final ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary vocabulary;

    VocabularyImmutable(String code)
    {
        this(new ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary());
        getVocabulary().setCode(code);
    }

    VocabularyImmutable(ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary vocabulary)
    {
        this.vocabulary = vocabulary;
    }

    ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary getVocabulary()
    {
        return vocabulary;
    }

    @Override
    public String getCode()
    {
        return getVocabulary().getCode();
    }

    @Override
    public String getDescription()
    {
        return getVocabulary().getDescription();
    }

    @Override
    public boolean isManagedInternally()
    {
        return getVocabulary().isManagedInternally();
    }

    /**
     * @deprecated use {@link #isManagedInternally()}
     */
    @Override
    @Deprecated
    public boolean isInternalNamespace()
    {
        return isManagedInternally();
    }

    @Override
    public boolean isChosenFromList()
    {
        return getVocabulary().isChosenFromList();
    }

    @Override
    public String getUrlTemplate()
    {
        return getVocabulary().getURLTemplate();
    }

    @Override
    public List<IVocabularyTermImmutable> getTerms()
    {
        List<IVocabularyTermImmutable> terms = new ArrayList<IVocabularyTermImmutable>();
        if (getVocabulary().getTerms() != null)
        {
            for (VocabularyTerm term : getVocabulary().getTerms())
            {
                terms.add(new VocabularyTermImmutable(term));
            }
        }
        return terms;
    }
}

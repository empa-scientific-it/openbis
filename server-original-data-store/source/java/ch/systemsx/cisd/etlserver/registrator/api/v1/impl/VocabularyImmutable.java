/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.registrator.api.v1.impl;

import java.util.LinkedList;
import java.util.List;

import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IVocabularyImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IVocabularyTermImmutable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;

/**
 * @author Jakub Straszewski
 */
public class VocabularyImmutable implements IVocabularyImmutable
{
    private final Vocabulary vocabulary;

    public VocabularyImmutable(Vocabulary vocabulary)
    {
        this.vocabulary = vocabulary;
    }

    protected Vocabulary getVocabulary()
    {
        return vocabulary;
    }

    @Override
    public String getCode()
    {
        return vocabulary.getCode();
    }

    @Override
    public String getDescription()
    {
        return vocabulary.getDescription();
    }

    @Override
    public boolean isManagedInternally()
    {
        return vocabulary.isManagedInternally();
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
        return vocabulary.isChosenFromList();
    }

    @Override
    public String getUrlTemplate()
    {
        return vocabulary.getURLTemplate();
    }

    @Override
    public List<IVocabularyTermImmutable> getTerms()
    {
        List<IVocabularyTermImmutable> results = new LinkedList<IVocabularyTermImmutable>();

        for (VocabularyTerm term : vocabulary.getTerms())
        {
            results.add(new VocabularyTermImmutable(term));
        }

        return results;
    }

    @Override
    public boolean containsTerm(String code)
    {
        for (IVocabularyTermImmutable term : getTerms())
        {
            if (term.getCode().equals(code))
            {
                return true;
            }
        }
        return false;
    }
}

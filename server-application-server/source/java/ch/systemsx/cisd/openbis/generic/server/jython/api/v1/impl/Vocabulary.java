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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IVocabulary;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IVocabularyTerm;

/**
 * @author Kaloyan Enimanev
 */
public class Vocabulary extends VocabularyImmutable implements IVocabulary
{
    private static final Comparator<ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm> TERM_COMPARATOR =
            new Comparator<ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm>()
                {
                    @Override
                    public int compare(
                            ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm t1,
                            ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm t2)
                    {
                        long o1 = getOrdinal(t1);
                        long o2 = getOrdinal(t2);
                        return o1 < o2 ? -1 : (o1 > o2 ? 1 : 0);
                    }

                    long getOrdinal(
                            ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm t1)
                    {
                        Long ordinal = t1.getOrdinal();
                        return ordinal == null ? Long.MAX_VALUE : ordinal;
                    }
                };

    Vocabulary(String code)
    {
        super(code);
    }

    @Override
    public void setDescription(String description)
    {
        getVocabulary().setDescription(description);
    }

    @Override
    public void setManagedInternally(boolean isManagedInternally)
    {
        getVocabulary().setManagedInternally(isManagedInternally);
    }

    /**
     * @deprecated use {@link #setManagedInternally(boolean)}
     */
    @Override
    @Deprecated
    public void setInternalNamespace(boolean isInternalNamespace)
    {
        setManagedInternally(isInternalNamespace);
    }

    @Override
    public void setChosenFromList(boolean isChosenFromList)
    {
        getVocabulary().setChosenFromList(isChosenFromList);
    }

    @Override
    public void setUrlTemplate(String urlTemplate)
    {
        getVocabulary().setURLTemplate(urlTemplate);
    }

    @Override
    public void addTerm(IVocabularyTerm term)
    {
        VocabularyTerm internalTerm = (VocabularyTerm) term;
        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm> terms =
                getVocabulary().getTerms();
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm vocabularyTerm =
                internalTerm.getVocabularyTerm();
        if (vocabularyTerm.getOrdinal() == null)
        {
            terms.add(vocabularyTerm);
            return;
        }
        int index = Collections.binarySearch(terms, vocabularyTerm, TERM_COMPARATOR);
        if (index < 0)
        {
            index = -(index + 1);
        }
        terms.add(index, vocabularyTerm);
    }

}

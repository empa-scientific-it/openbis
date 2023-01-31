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

package ch.systemsx.cisd.etlserver.registrator.api.v2.impl;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import ch.systemsx.cisd.etlserver.registrator.api.v2.IVocabulary;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IVocabularyTerm;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IVocabularyTermImmutable;

/**
 * @author Jakub Straszewski
 */
public class Vocabulary extends VocabularyImmutable implements IVocabulary
{
    public Vocabulary(ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary vocabulary)
    {
        super(vocabulary);
        newTerms = new LinkedList<IVocabularyTerm>();
    }

    private List<IVocabularyTerm> newTerms;

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
     * @deprecated use {@link #isManagedInternally()}
     */
    @Override
    @Deprecated
    public void setInternalNamespace(boolean isInternalNamespace)
    {
        getVocabulary().setManagedInternally(isInternalNamespace);
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
        newTerms.add(term);
    }

    List<IVocabularyTerm> getNewTerms()
    {
        return newTerms;
    }

    @Override
    public List<IVocabularyTermImmutable> getTerms()
    {
        List<IVocabularyTermImmutable> results = super.getTerms();
        for (IVocabularyTerm term : newTerms)
        {
            results.add(term);
        }
        return Collections.unmodifiableList(results);
    }

}

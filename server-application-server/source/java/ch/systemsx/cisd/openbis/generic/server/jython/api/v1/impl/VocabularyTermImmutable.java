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

import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IVocabularyTermImmutable;

/**
 * @author Kaloyan Enimanev
 */
public class VocabularyTermImmutable implements IVocabularyTermImmutable
{

    private final ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm vocabularyTerm;

    VocabularyTermImmutable(
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm vocabularyTerm)
    {
        this.vocabularyTerm = vocabularyTerm;
    }

    ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm getVocabularyTerm()
    {
        return vocabularyTerm;
    }

    @Override
    public String getCode()
    {
        return getVocabularyTerm().getCode();
    }

    @Override
    public String getDescription()
    {
        return getVocabularyTerm().getDescription();
    }

    @Override
    public String getLabel()
    {
        return getVocabularyTerm().getLabel();
    }

    @Override
    public String getUrl()
    {
        return getVocabularyTerm().getUrl();
    }

    @Override
    public Long getOrdinal()
    {
        return getVocabularyTerm().getOrdinal();
    }

}

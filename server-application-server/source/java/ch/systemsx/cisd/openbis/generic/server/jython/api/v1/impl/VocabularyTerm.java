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

import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IVocabularyTerm;

/**
 * @author Kaloyan Enimanev
 */
public class VocabularyTerm extends VocabularyTermImmutable implements IVocabularyTerm
{
    VocabularyTerm(ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm term)
    {
        super(term);
    }

    VocabularyTerm(String code)
    {
        super(new ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm());
        getVocabularyTerm().setCode(code);
    }

    @Override
    public void setDescription(String description)
    {
        getVocabularyTerm().setDescription(description);
    }

    @Override
    public void setLabel(String label)
    {
        getVocabularyTerm().setLabel(label);
    }

    /**
     * Keep for backward-compatibility reasons. Executing the method has no effect.
     */
    public void setUrl(String url)
    {
        getVocabularyTerm().setUrl(url);
    }

    @Override
    public void setOrdinal(Long ordinal)
    {
        getVocabularyTerm().setOrdinal(ordinal);
    }

}

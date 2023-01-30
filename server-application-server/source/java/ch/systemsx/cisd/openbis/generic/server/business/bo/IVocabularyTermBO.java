/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IVocabularyTermUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;

/**
 * Business object for handling vocabulary terms (see also {@link VocabularyBO}).
 * 
 * @author Piotr Buczek
 */
public interface IVocabularyTermBO
{
    /**
     * Returns the loaded {@link VocabularyTermPE}.
     */
    public VocabularyTermPE getVocabularyTerm();

    /**
     * Updates the vocabulary term.
     */
    public void update(IVocabularyTermUpdates updates);

    /**
     * Makes vocabulary terms official
     */
    public void makeOfficial(List<VocabularyTerm> termsToBeOfficial);
}

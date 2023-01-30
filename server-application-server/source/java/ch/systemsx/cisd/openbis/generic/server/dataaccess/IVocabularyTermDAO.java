/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess;

import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;

/**
 * <i>Data Access Object</i> for {@link VocabularyTermPE}.
 * 
 * @author Piotr Buczek
 */
public interface IVocabularyTermDAO extends IGenericDAO<VocabularyTermPE>
{

    @Override
    void validate(final VocabularyTermPE term);

    /**
     * All terms in specified vocabulary starting from specified ordinal will have the ordinal increased by specified increment.
     * <p>
     * After this change a collection of terms with size equal to <var>increment</var> can be put consecutively starting from <var>fromOrdinal</var>
     * and ordinals in the vocabulary will stay unique.
     */
    void increaseVocabularyTermOrdinals(final VocabularyPE vocabulary, final Long fromOrdinal,
            final int increment);

    /**
     * Returns the maximum ordinal over all terms of the specified vocabulary.
     */
    long getMaximumOrdinal(VocabularyPE vocabulary);

    /**
     * Updates the term's registrator.
     */
    void updateRegistrator(VocabularyTermPE term, PersonPE registrator);

}

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

package ch.systemsx.cisd.openbis.generic.server.dataaccess;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;

/**
 * An interface that contains all data access operations on {@link VocabularyTermPE} and {@link VocabularyPE}.
 * 
 * @author Christian Ribeaud
 */
public interface IVocabularyDAO extends IGenericDAO<VocabularyPE>
{

    /**
     * Creates or updates the specified vocabulary in the database.
     */
    void createOrUpdateVocabulary(final VocabularyPE vocabularyPE);

    /**
     * Returns the {@link VocabularyPE} object for the given <var>vocabularyCode</var>, or <code>null</code>, if it doesn't exist.<br>
     * <br>
     * NOTE: Only internally managed vocabularies have codes that can be used as business identifiers (cannot be edited).
     */
    VocabularyPE tryFindVocabularyByCode(final String vocabularyCode);

    /**
     * Return available vocabularies.
     */
    List<VocabularyPE> listVocabularies(boolean excludeInternal);

    /**
     * @return {@link VocabularyTermPE} with given <var>code</var> for given {@link VocabularyPE} or null if it does not exist.
     */
    VocabularyTermPE tryFindVocabularyTermByCode(VocabularyPE vocabulary, String code);

}

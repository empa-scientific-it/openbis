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
import java.util.Set;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IVocabularyUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewVocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermReplacement;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermWithStats;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyUpdatesDTO;

/**
 * Business object of a vocabulary. Holds an instance of {@link VocabularyPE}.
 * 
 * @author Christian Ribeaud
 */
public interface IVocabularyBO extends IEntityBusinessObject
{
    /**
     * Defines a new vocabulary.
     * <p>
     * After invocation of this method {@link IBusinessObject#save()} should be invoked to store the new vocabulary in the <i>Data Access Layer</i>.
     * </p>
     * 
     * @throws UserFailureException if given <var>vocabulary</var> does already exist.
     */
    void define(final NewVocabulary vocabulary) throws UserFailureException;

    /**
     * Returns the loaded {@link VocabularyPE}.
     */
    VocabularyPE getVocabulary();

    /**
     * Loads specified vocabulary from the database.
     * 
     * @deprecated Code of a user vocabulary can be modified so it cannot be used as a universal vocabulary business identifier.
     * @throws UserFailureException if no vocabulary found for <code>vocabularyCode</code>.
     */
    @Deprecated
    void load(String vocabularyCode) throws UserFailureException;

    /** @return terms with their usage statistics for the loaded vocabulary */
    List<VocabularyTermWithStats> countTermsUsageStatistics();

    /** enriches currently loaded vocabulary with its terms and returns them */
    Set<VocabularyTermPE> enrichWithTerms();

    /**
     * Add terms with specified codes to a loaded vocabulary.
     * 
     * @param previousTermOrdinal ordinal of term after which new terms should be added
     */
    List<VocabularyTermPE> addNewTerms(List<VocabularyTerm> newTerms, Long previousTermOrdinal);

    /**
     * Add unofficial terms with specified label and description to a loaded vocabulary.
     * 
     * @param code code of the vocabulary term
     * @param label label of the vocabulary term
     * @description description of the term
     * @param previousTermOrdinal ordinal of term after which new terms should be added
     */
    VocabularyTermPE addNewUnofficialTerm(String code, String label, String description,
            Long previousTermOrdinal);

    /**
     * Deletes the specified terms from a loaded vocabulary and replaces terms which are used.
     */
    public void delete(List<VocabularyTerm> termsToBeDeleted,
            List<VocabularyTermReplacement> termsToBeReplaced);

    /**
     * Updates the vocabulary.
     */
    public void update(IVocabularyUpdates updates);

    /**
     * Updates the vocabulary.
     */
    public void update(VocabularyUpdatesDTO updates);

    /**
     * Deletes vocabulary for specified reason.
     * 
     * @param vocabularyId vocabulary technical identifier
     * @throws UserFailureException if vocabulary with given technical identifier is not found.
     */
    void deleteByTechId(TechId vocabularyId, String reason);

    /**
     * Adds new terms, updates the details (label, description) of existing terms and updates the order.
     */
    void updateTerms(List<VocabularyTerm> terms);

}

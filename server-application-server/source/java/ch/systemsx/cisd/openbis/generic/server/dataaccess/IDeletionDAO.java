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

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletionPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IDeletablePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * An interface that contains all data access operations on {@link DeletionPE}s.
 * 
 * @author Piotr Buczek
 */
public interface IDeletionDAO extends IGenericDAO<DeletionPE>
{
    /** Inserts given {@link DeletionPE} into the database. */
    void create(final DeletionPE deletion) throws DataAccessException;

    /**
     * Moves entities with given ids to trash using specified deletion. Ignores ids of entities that don't exist or are already in the trash.
     * 
     * @param isOriginalDeletion if true than the specified entities are considered originally deleted entities
     * @return number of trashed entities
     */
    int trash(EntityKind entityKind, List<TechId> entityIds, DeletionPE deletion,
            boolean isOriginalDeletion) throws DataAccessException;

    /**
     * Moves entities with given ids to trash using specified deletion. Ignores ids of entities that don't exist or are already in the trash.
     * <p>
     * The entites are considered to be originally deleted
     * 
     * @return number of trashed entities
     */
    int trash(EntityKind entityKind, List<TechId> entityIds, DeletionPE deletion)
            throws DataAccessException;

    /**
     * Reverts given deletion for specified modifier. The deletion record will be removed from DB.
     */
    void revert(DeletionPE deletion, PersonPE modifier);

    /**
     * Returns list of ids of samples moved to trash in specified deletions.
     */
    // This method is kept for consistency with other find methods and for tests.
    List<TechId> findTrashedSampleIds(List<TechId> deletionIds);

    /**
     * Returns list of ids of non-comonent samples (having no container) moved to trash in specified deletions.
     */
    List<TechId> findTrashedNonComponentSampleIds(List<TechId> deletionIds);

    /**
     * Returns list of ids of component samples (samples with container) moved to trash in specified deletions.
     */
    List<TechId> findTrashedComponentSampleIds(List<TechId> deletionIds);

    /** Returns list of ids of experiments moved to trash in specified deletions. */
    List<TechId> findTrashedExperimentIds(List<TechId> deletionIds);

    /** Returns list of codes of data sets moved to trash in specified deletions. */
    List<String> findTrashedDataSetCodes(List<TechId> deletionIds);

    /** Returns list of codes of data sets moved to trash in specified deletions. */
    List<TechId> findTrashedDataSetIds(List<TechId> deletionIds);

    /**
     * Returns list of ids of samples originally moved to trash in specified deletions.
     */
    List<TechId> findOriginalTrashedSampleIds(List<TechId> deletionIds);

    /** Returns list of ids of experiments originally moved to trash in specified deletions. */
    List<TechId> findOriginalTrashedExperimentIds(List<TechId> deletionIds);

    /** Returns list of codes of data sets originally moved to trash in specified deletions. */
    List<TechId> findOriginalTrashedDataSetIds(List<TechId> deletionIds);

    /** Returns list of deletions with given ids */
    List<DeletionPE> findAllById(List<Long> ids);

    /**
     * Returns a list of all deleted entites by their IDs.
     */
    List<? extends IDeletablePE> listDeletedEntities(EntityKind entityKind, List<TechId> entityIds);

    /**
     * Returns a list of all deleted entities for a given type id.
     */
    List<TechId> listDeletedEntitiesForType(EntityKind entityKind, TechId entityTypeId);
    
    /**
     * Returns an ordered list of all deletions which dependent on the specified deletion ids. 
     * That is, the returned deletions have to be permanently deleted first before the specified deletions 
     * can be made permanently too. Note, that the result doesn't contain the deletions of the argument. 
     */
    List<TechId> listAllDependentDeletions(List<TechId> deletionIds);

}

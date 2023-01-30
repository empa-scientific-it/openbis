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

package ch.systemsx.cisd.openbis.generic.server.dataaccess;

import java.util.Collection;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityInformationHolderDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityInformationWithPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author Pawel Glyzewski
 */
public interface IMetaprojectDAO extends IGenericDAO<MetaprojectPE>
{
    /**
     * Finds a metaproject by the specified owner id and metaproject name. Returns null if no metaproject is found.
     */
    public MetaprojectPE tryFindByOwnerAndName(String ownerId, String metaprojectName);

    /**
     * Lists all metaprojects defined by given user.
     */
    public List<MetaprojectPE> listMetaprojects(PersonPE owner);

    /**
     * Creates a new metaproject or updates existing one.
     */
    public void createOrUpdateMetaproject(MetaprojectPE metaproject, PersonPE owner);

    /**
     * List entity ids for the entity kind and metaproject.
     */
    public Collection<Long> listMetaprojectEntityIds(Long metaprojectId, EntityKind entityKind);

    /**
     * Lists all metaprojects owned by given user, connected with given entity.
     */
    public Collection<MetaprojectPE> listMetaprojectsForEntity(PersonPE owner,
            IEntityInformationHolderDTO entity);

    /**
     * Lists all metaprojects owned by given user, connected with given entities.
     */
    public Collection<MetaprojectAssignmentPE> listMetaprojectAssignmentsForEntities(
            PersonPE owner, Collection<? extends IEntityInformationWithPropertiesHolder> entities,
            EntityKind entityKind);

    /**
     * Lists assignments for the given entity kind and metaproject.
     */
    Collection<MetaprojectAssignmentPE> listMetaprojectAssignments(Long metaprojectId,
            EntityKind entityKind);

    /**
     * Returns a map with counts of assignments for different entity kinds for the given metaproject.
     */
    public int getMetaprojectAssignmentsCount(Long metaprojectId, EntityKind entityKind);

    /**
     * List metaprojects by ids.
     */
    public List<MetaprojectPE> listByIDs(Collection<Long> ids);

}

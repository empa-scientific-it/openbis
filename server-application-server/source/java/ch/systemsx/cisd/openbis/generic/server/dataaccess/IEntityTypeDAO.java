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

import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * Interface to the data access layer for retrieving instances of {@link EntityTypePE}. Implementations may depend on the {@link EntityKind}.
 * 
 * @author Franz-Josef Elmer
 */
public interface IEntityTypeDAO extends IGenericDAO<EntityTypePE>
{

    /**
     * Tries to find the entity type with the specified code.
     * 
     * @return <code>null</code> if nothing found.
     */
    public EntityTypePE tryToFindEntityTypeByCode(String code) throws DataAccessException;

    /**
     * Tries to find entity types with the specified codes.
     * 
     * @return An empty list if nothing found.
     */
    public List<EntityTypePE> tryToFindEntityTypeByCodes(List<String> codes) throws DataAccessException;

    /**
     * Returns a list of all entity types.
     */
    public <T extends EntityTypePE> List<T> listEntityTypes() throws DataAccessException;

    /** Creates or update specified entity type */
    public <T extends EntityTypePE> void createOrUpdateEntityType(T entityType)
            throws DataAccessException;

    /** Deletes entity type */
    public <T extends EntityTypePE> void deleteEntityType(T entityType) throws DataAccessException;
}

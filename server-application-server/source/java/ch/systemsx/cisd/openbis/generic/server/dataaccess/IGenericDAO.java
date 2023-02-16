/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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

import org.hibernate.proxy.HibernateProxy;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;

import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * Generic interface for DAOs.
 * 
 * @author Piotr Buczek
 */
public interface IGenericDAO<T extends IIdHolder>
{
    /**
     * @param techId the entity technical identifier
     * @return entity with the given technical identifier (and no lazy connections initialized) or null if it is not found <br>
     *         NOTE: don't rely on T.getId() value because returned value can be a {@link HibernateProxy}. Use {@link HibernateUtils#getId(IIdHolder)}
     *         instead.
     * @throws DataRetrievalFailureException if the entity with given identifier do not exist in the database.
     */
    public T getByTechId(final TechId techId);

    /**
     * @param techId the entity technical identifier
     * @param connections the (lazy) connections to additionally initialize
     * @return entity with the given technical identifier or null if it is not found <br>
     *         NOTE: don't rely on T.getId() value because returned value can be a {@link HibernateProxy}. Use {@link HibernateUtils#getId(IIdHolder)}
     *         instead.
     */
    public T tryGetByTechId(final TechId techId, String... connections);

    /**
     * Updates given persistent (already saved) <var>entity</var> after successful validation.<br>
     * <br>
     * Useful especially instead of a save() method (used for making entity persistent) after BO update that does not flush.
     * 
     * @param entity the entity to be validated and updated
     */
    public void validateAndSaveUpdatedEntity(T entity);

    // Hibernate specific

    public void validate(T entity);

    //

    /**
     * Persists given entity.
     * 
     * @param entity the entity to be persisted
     * @throws DataAccessException if the entity cannot be persisted.
     */
    public void persist(final T entity) throws DataAccessException;

    /**
     * Deletes given entity from DB with some connected objects.
     * 
     * @param entity the entity to be deleted
     * @throws DataAccessException if the entity cannot be deleted.
     */
    public void delete(final T entity) throws DataAccessException;

    /**
     * Returns all entities.
     */
    public List<T> listAllEntities() throws DataAccessException;

    public void flush() throws DataAccessException;

    public void clear() throws DataAccessException;

}

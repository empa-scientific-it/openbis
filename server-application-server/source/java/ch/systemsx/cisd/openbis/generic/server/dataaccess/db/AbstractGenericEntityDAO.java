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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.orm.hibernate5.HibernateTemplate;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.reflection.MethodUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IGenericDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.deletion.EntityHistoryCreator;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;

/**
 * Abstract super class of DAOs using generic interface.
 * 
 * @author Piotr Buczek
 */
public abstract class AbstractGenericEntityDAO<T extends IIdHolder> extends AbstractDAO implements
        IGenericDAO<T>
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            AbstractGenericEntityDAO.class);

    private final Class<T> entityClass;

    protected final EntityHistoryCreator historyCreator;

    protected AbstractGenericEntityDAO(final SessionFactory sessionFactory, final Class<T> entityClass,
            EntityHistoryCreator historyCreator)
    {
        super(sessionFactory);
        this.entityClass = entityClass;
        this.historyCreator = historyCreator;
    }

    protected Class<T> getEntityClass()
    {
        return entityClass;
    }

    @Override
    public final T getByTechId(final TechId techId) throws DataAccessException
    {
        assert techId != null : "Technical identifier unspecified.";
        final Object entity = getHibernateTemplate().get(getEntityClass(), techId.getId());
        T result = null;
        if (entity == null)
        {
            throw new DataRetrievalFailureException(getEntityDescription() + " with ID "
                    + techId.getId() + " does not exist. Maybe someone has just deleted it.");
        } else
        {
            result = getEntity(entity);
        }
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(%s): '%s'.", MethodUtils.getCurrentMethod()
                    .getName(), techId, result));
        }
        return result;
    }

    public final T loadByTechId(final TechId techId) throws DataAccessException
    {
        assert techId != null : "Technical identifier unspecified.";
        return getEntity(getHibernateTemplate().load(getEntityClass(), techId.getId()));
    }

    private String getEntityDescription()
    {
        String nameWithoutPE = getEntityClass().getSimpleName().replace("PE", "");
        String words[] = StringUtils.splitByCharacterTypeCamelCase(nameWithoutPE);
        return StringUtils.join(words, " ");
    }

    // TODO 2009-05-22, Tomasz Pylak: remove connections, it forces BOs to use strings with field
    // paths
    @Override
    public final T tryGetByTechId(final TechId techId, String... connections)
            throws DataAccessException
    {
        assert techId != null : "Technical identifier unspecified.";
        final Criteria criteria = currentSession().createCriteria(getEntityClass());
        criteria.add(Restrictions.eq("id", techId.getId()));
        for (String connection : connections)
        {
            criteria.setFetchMode(connection, FetchMode.SELECT);
        }
        final T result = tryGetEntity(criteria.uniqueResult());
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(%s): '%s'.", MethodUtils.getCurrentMethod()
                    .getName(), techId, result));
        }
        return result;
    }

    protected <T> List<T> listByIDsOfName(Class<T> clazz, String idName, Collection<?> ids)
    {
        if (ids == null || ids.isEmpty())
        {
            return new ArrayList<T>();
        }
        final List<T> list = DAOUtils.listByCollection(getHibernateTemplate(), clazz, idName, ids);
        if (operationLog.isDebugEnabled())
        {
            String name = clazz.getSimpleName();
            if (name.endsWith("PE"))
            {
                name = name.substring(0, name.length() - 2);
            }
            operationLog.debug(String.format("%d " + name.toLowerCase() + "(s) have been found.", list.size()));
        }
        return list;
    }

    @Override
    public void validateAndSaveUpdatedEntity(T entity) throws DataAccessException
    {
        assert entity != null : "entity is null";

        // as long as CODE cannot be edited we don't have to translate it with a converter here
        // because the code set in updated entity should be the one already translated during save
        // but if we allow it this will have to be changed for entities with codes e.g.
        // like for experiment:
        // experiment.setCode(CodeConverter.tryToDatabase(experiment.getCode()));

        validatePE(entity);
        flushWithSqlExceptionHandling(getHibernateTemplate());
    }

    @Override
    public final void validate(T entity)
    {
        assert entity != null : "entity is null";

        // as long as CODE cannot be edited we don't have to translate it with a converter here
        // because the code set in updated entity should be the one already translated during save
        // but if we allow it this will have to be changed for entities with codes e.g.
        // like for experiment:
        // experiment.setCode(CodeConverter.tryToDatabase(experiment.getCode()));

        validatePE(entity);
    }

    public void clearSession()
    {
        getHibernateTemplate().clear();
    }

    public final void flush()
    {
        getHibernateTemplate().flush();
    }

    @Override
    public void persist(T entity)
    {
        assert entity != null : "entity unspecified";

        validatePE(entity);

        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.save(entity);
        hibernateTemplate.flush();

        if (operationLog.isInfoEnabled())
        {
            operationLog.debug(String.format("%s(%s)", MethodUtils.getCurrentMethod().getName(),
                    entity));
        }
    }

    @Override
    public void delete(T entity) throws DataAccessException
    {
        assert entity != null : "entity unspecified";

        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.delete(entity);
        hibernateTemplate.flush();

        if (operationLog.isInfoEnabled())
        {
            operationLog.debug(String.format("%s(%s)", MethodUtils.getCurrentMethod().getName(),
                    entity));
        }
    }

    @Override
    public List<T> listAllEntities() throws DataAccessException
    {
        return cast(getHibernateTemplate().loadAll(getEntityClass()));
    }

}

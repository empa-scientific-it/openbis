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
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.JdbcAccessor;
import org.springframework.orm.hibernate5.HibernateTemplate;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.reflection.MethodUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.deletion.EntityHistoryCreator;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.dto.AbstractTypePE;

/**
 * An abstract extension of <code>HibernateAbstractDAO</code> suitable for tables that contains <i>type</i> information. <br>
 * 
 * @author Izabela Adamczyk
 */
abstract class AbstractTypeDAO<T extends AbstractTypePE> extends AbstractGenericEntityDAO<T>
{

    /**
     * The <code>Logger</code> of this class.
     * <p>
     * This logger does not output any SQL statement. If you want to do so, you had better set an appropriate debugging level for class
     * {@link JdbcAccessor}.
     * </p>
     */
    private final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, getClass());

    public AbstractTypeDAO(final SessionFactory sessionFactory, final Class<T> entityClass, EntityHistoryCreator historyCreator)
    {
        super(sessionFactory, entityClass, historyCreator);
    }

    final T tryFindTypeByCode(final String code) throws DataAccessException
    {
        return tryFindTypeByCode(code, true);
    }

    final T tryFindTypeByCode(final String code, final boolean appendDatabaseInstance)
            throws DataAccessException
    {
        assert code != null : "Unspecified code";

        final DetachedCriteria criteria = DetachedCriteria.forClass(getEntityClass());
        criteria.add(Restrictions.eq("code", CodeConverter.tryToDatabase(code)));
        final List<T> list = cast(getHibernateTemplate().findByCriteria(criteria));
        final T entity = tryFindEntity(list, "type");
        if (operationLog.isDebugEnabled())
        {
            operationLog
                    .debug(String.format("%s(%s,%s): Entity type '%s' has been found.", MethodUtils
                            .getCurrentMethod().getName(), code, appendDatabaseInstance, entity));
        }
        return entity;
    }

    final List<T> tryFindTypeByCodes(final List<String> codes, final boolean appendDatabaseInstance)
            throws DataAccessException
    {
        assert codes != null : "Unspecified codes";

        List<String> dbCodes = new ArrayList<String>(codes.size());
        for (String code : codes)
        {
            dbCodes.add(CodeConverter.tryToDatabase(code));
        }

        final DetachedCriteria criteria = DetachedCriteria.forClass(getEntityClass());
        criteria.add(Restrictions.in("code", dbCodes));
        final List<T> list = cast(getHibernateTemplate().findByCriteria(criteria));

        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(): %d entity type(s) have been found.", MethodUtils
                    .getCurrentMethod().getName(), list.size()));

        }
        return list;
    }

    final List<T> listTypes() throws DataAccessException
    {
        return listTypes(true);
    }

    final List<T> listTypes(final boolean appendDatabaseInstance) throws DataAccessException
    {
        final DetachedCriteria criteria = DetachedCriteria.forClass(getEntityClass());
        final List<T> list = cast(getHibernateTemplate().findByCriteria(criteria));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(%s): %d entity type(s) have been found.",
                    MethodUtils.getCurrentMethod().getName(), appendDatabaseInstance, list.size()));
        }
        return list;
    }

    protected void createOrUpdateType(T type)
    {
        final HibernateTemplate hibernateTemplate = getHibernateTemplate();

        validatePE(type);
        type.setCode(CodeConverter.tryToDatabase(type.getCode()));
        hibernateTemplate.saveOrUpdate(type);
        hibernateTemplate.flush();
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("ADD/UPDATE: type '%s'.", type));
        }

    }

}

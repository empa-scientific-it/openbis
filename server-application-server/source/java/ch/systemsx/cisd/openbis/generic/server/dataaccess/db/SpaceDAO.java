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

import org.apache.log4j.Logger;
import org.hibernate.FetchMode;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.JdbcAccessor;
import org.springframework.orm.hibernate5.HibernateTemplate;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.reflection.MethodUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISpaceDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.deletion.EntityHistoryCreator;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * <i>Data Access Object</i> implementation for {@link SpacePE}.
 * 
 * @author Christian Ribeaud
 */
final class SpaceDAO extends AbstractGenericEntityDAO<SpacePE> implements ISpaceDAO
{

    /**
     * This logger does not output any SQL statement. If you want to do so, you had better set an appropriate debugging level for class
     * {@link JdbcAccessor}.
     * </p>
     */
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            SpaceDAO.class);

    SpaceDAO(final SessionFactory sessionFactory, EntityHistoryCreator historyCreator)
    {
        super(sessionFactory, SpacePE.class, historyCreator);
    }

    //
    // ISpaceDAO
    //

    @Override
    public final SpacePE tryFindSpaceByCode(final String spaceCode) throws DataAccessException
    {
        assert spaceCode != null : "Unspecified space code.";

        final List<SpacePE> list =
                cast(getHibernateTemplate().find(
                        String.format("select g from %s g where g.code = ?", getEntityClass().getSimpleName()),
                        toArray(CodeConverter.tryToDatabase(spaceCode))));
        final SpacePE entity = tryFindEntity(list, "space");
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(%s): '%s'.", MethodUtils.getCurrentMethod()
                    .getName(), spaceCode, entity));
        }
        return entity;
    }

    @Override
    public List<SpacePE> tryFindSpaceByCodes(List<String> spaceCodes) throws DataAccessException
    {
        final DetachedCriteria criteria = DetachedCriteria.forClass(getEntityClass());
        criteria.add(Restrictions.in("code", spaceCodes));

        final List<SpacePE> list = cast(getHibernateTemplate().findByCriteria(criteria));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(): %d space(s) have been found.", MethodUtils
                    .getCurrentMethod().getName(), list.size()));
        }
        return list;
    }

    @Override
    public final List<SpacePE> listSpaces() throws DataAccessException
    {
        final DetachedCriteria criteria = DetachedCriteria.forClass(getEntityClass());
        criteria.setFetchMode("registrator", FetchMode.JOIN);
        final List<SpacePE> list = cast(getHibernateTemplate().findByCriteria(criteria));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(): %d space(s) have been found.", MethodUtils
                    .getCurrentMethod().getName(), list.size()));
        }
        return list;
    }

    @Override
    public List<SpacePE> listByIDs(Collection<Long> ids)
    {
        return listByIDsOfName("id", ids);
    }

    private List<SpacePE> listByIDsOfName(String idName, Collection<?> ids)
    {
        if (ids == null || ids.isEmpty())
        {
            return new ArrayList<SpacePE>();
        }
        final List<SpacePE> list =
                DAOUtils.listByCollection(getHibernateTemplate(), SpacePE.class, idName, ids);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%d spaces(s) have been found.", list.size()));
        }
        return list;
    }

    @Override
    public final void createSpace(final SpacePE space) throws DataAccessException
    {
        assert space != null : "Unspecified space";
        validatePE(space);

        final HibernateTemplate template = getHibernateTemplate();
        space.setCode(CodeConverter.tryToDatabase(space.getCode()));
        template.save(space);
        template.flush();
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("ADD: space '%s'.", space));
        }
    }

}

/*
 * Copyright 2009 ETH Zuerich, CISD
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

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.reflection.MethodUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IGridCustomColumnDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.deletion.EntityHistoryCreator;
import ch.systemsx.cisd.openbis.generic.shared.dto.GridCustomColumnPE;

/**
 * Hibernate-based implementation of {@link IGridCustomColumnDAO}.
 * 
 * @author Tomasz Pylak
 */
public class GridCustomColumnDAO extends AbstractGenericEntityDAO<GridCustomColumnPE> implements
        IGridCustomColumnDAO
{

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, GridCustomColumnDAO.class);

    public GridCustomColumnDAO(SessionFactory sessionFactory, EntityHistoryCreator historyCreator)
    {
        super(sessionFactory, GridCustomColumnPE.class, historyCreator);
    }

    @Override
    public void createColumn(GridCustomColumnPE column)
    {
        assert column != null : "Unspecified column";
        persist(column);
    }

    @Override
    public List<GridCustomColumnPE> listColumns(String gridId)
    {
        assert gridId != null : "Unspecified grid ID.";

        final DetachedCriteria criteria = DetachedCriteria.forClass(getEntityClass());
        criteria.add(Restrictions.eq("gridId", gridId));
        final List<GridCustomColumnPE> list = cast(getHibernateTemplate().findByCriteria(criteria));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(%s): %d column(s) have been found.", MethodUtils
                    .getCurrentMethod().getName(), gridId, list.size()));
        }
        return list;
    }

}

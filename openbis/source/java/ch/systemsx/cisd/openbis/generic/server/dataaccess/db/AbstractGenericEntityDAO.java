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

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.MethodUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAbstractGenericDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;

/**
 * Abstract super class of DAOs using generic interface.
 * 
 * @author Piotr Buczek
 */
public abstract class AbstractGenericEntityDAO<T extends IIdHolder> extends AbstractDAO implements
        IAbstractGenericDAO<T>
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, AbstractGenericEntityDAO.class);

    protected AbstractGenericEntityDAO(SessionFactory sessionFactory,
            DatabaseInstancePE databaseInstance)
    {
        super(sessionFactory, databaseInstance);
    }

    abstract Class<T> getEntityClass();

    public final T getByTechId(final TechId techId) throws DataAccessException
    {
        final Object object = getHibernateTemplate().load(getEntityClass(), techId.getId());
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(%d): '%s'.", MethodUtils.getCurrentMethod()
                    .getName(), techId, object));
        }
        return getEntity(object);
    }

}

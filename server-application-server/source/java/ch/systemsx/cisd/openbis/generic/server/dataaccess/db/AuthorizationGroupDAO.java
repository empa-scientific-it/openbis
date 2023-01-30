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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate5.HibernateTemplate;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.reflection.MethodUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAuthorizationGroupDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.deletion.EntityHistoryCreator;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.dto.AuthorizationGroupPE;

/**
 * Implementation of {@link IAuthorizationGroupDAO}.
 * 
 * @author Izabela Adamczyk
 */
public class AuthorizationGroupDAO extends AbstractGenericEntityDAO<AuthorizationGroupPE> implements
        IAuthorizationGroupDAO
{
    public static final Class<AuthorizationGroupPE> ENTITY_CLASS = AuthorizationGroupPE.class;

    private static final String TABLE_NAME = ENTITY_CLASS.getSimpleName();

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, AuthorizationGroupDAO.class);

    protected AuthorizationGroupDAO(final SessionFactory sessionFactory, EntityHistoryCreator historyCreator)
    {
        super(sessionFactory, ENTITY_CLASS, historyCreator);
    }

    @Override
    public List<AuthorizationGroupPE> list()
    {
        final List<AuthorizationGroupPE> list =
                cast(getHibernateTemplate().find(
                        String.format("from %s a", TABLE_NAME)));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(): %d authorization group(s) have been found.",
                    MethodUtils.getCurrentMethod().getName(), list.size()));
        }
        return list;
    }

    @Override
    public List<AuthorizationGroupPE> listByIds(Collection<Long> ids)
    {
        return listByIDsOfName(AuthorizationGroupPE.class, "id", ids);
    }

    @Override
    public List<AuthorizationGroupPE> listByCodes(Collection<String> ids)
    {
        return listByIDsOfName(AuthorizationGroupPE.class, "code", ids);
    }

    @Override
    public void create(AuthorizationGroupPE authorizationGroup)
    {
        assert authorizationGroup != null : "Missing authorization group.";
        validatePE(authorizationGroup);

        authorizationGroup.setCode(CodeConverter.tryToDatabase(authorizationGroup.getCode()));
        final HibernateTemplate template = getHibernateTemplate();
        template.saveOrUpdate(authorizationGroup);
        template.flush();
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("SAVE: authorization group '%s'.", authorizationGroup));
        }
    }

    @Override
    public AuthorizationGroupPE tryFindByCode(String code)
    {
        final Criteria criteria = currentSession().createCriteria(ENTITY_CLASS);
        criteria.add(Restrictions.eq("code", CodeConverter.tryToDatabase(code)));
        return (AuthorizationGroupPE) criteria.uniqueResult();
    }

}

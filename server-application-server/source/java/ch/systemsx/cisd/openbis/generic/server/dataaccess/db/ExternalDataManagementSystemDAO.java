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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate5.HibernateTemplate;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExternalDataManagementSystemDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataManagementSystemPE;

/**
 * @author Pawel Glyzewski
 */
public class ExternalDataManagementSystemDAO extends AbstractDAO implements
        IExternalDataManagementSystemDAO
{
    private final static Class<ExternalDataManagementSystemPE> ENTITY_CLASS =
            ExternalDataManagementSystemPE.class;

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            ExternalDataManagementSystemDAO.class);

    public ExternalDataManagementSystemDAO(SessionFactory sessionFactory)
    {
        super(sessionFactory);
    }

    @Override
    public void createOrUpdateExternalDataManagementSystem(
            ExternalDataManagementSystemPE externalDataManagementSystem)
    {
        assert externalDataManagementSystem != null : "Unspecified external data management system.";

        HibernateTemplate template = getHibernateTemplate();

        externalDataManagementSystem.setCode(CodeConverter
                .tryToDatabase(externalDataManagementSystem.getCode()));
        template.saveOrUpdate(externalDataManagementSystem);
        template.flush();
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("SAVE/UPDATE: external data management system '%s'.",
                    externalDataManagementSystem));
        }
    }

    @Override
    public ExternalDataManagementSystemPE tryToFindExternalDataManagementSystemById(Long id)
    {
        assert id != null : "Unspecified external data management system id.";

        final Criteria criteria = currentSession().createCriteria(ENTITY_CLASS);
        criteria.add(Restrictions.eq("id", id));
        return (ExternalDataManagementSystemPE) criteria.uniqueResult();
    }

    @Override
    public ExternalDataManagementSystemPE tryToFindExternalDataManagementSystemByCode(
            String externalDataManagementSystemCode)
    {
        assert externalDataManagementSystemCode != null : "Unspecified external data management system code.";

        final Criteria criteria = currentSession().createCriteria(ENTITY_CLASS);
        criteria.add(Restrictions.eq("code",
                CodeConverter.tryToDatabase(externalDataManagementSystemCode)));
        return (ExternalDataManagementSystemPE) criteria.uniqueResult();
    }

    @Override
    public List<ExternalDataManagementSystemPE> listExternalDataManagementSystems()
    {
        final Criteria criteria = currentSession().createCriteria(ENTITY_CLASS);
        criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        final List<ExternalDataManagementSystemPE> list = cast(criteria.list());
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "%d external data management systems have been found.", list.size()));
        }
        return list;
    }

    @Override
    public void delete(Collection<ExternalDataManagementSystemPE> externalDms)
    {
        Session session = currentSession();

        String hql = "DELETE FROM ContentCopyPE WHERE externalDataManagementSystem IN :externalDms";
        session.createQuery(hql).setParameterList("externalDms", externalDms).executeUpdate();

        for (ExternalDataManagementSystemPE edms : externalDms)
        {
            session.delete(edms);
        }
    }

    @Override
    public List<ExternalDataManagementSystemPE> listExternalDataManagementSystems(Collection<Long> ids)
    {
        final Criteria criteria = currentSession().createCriteria(ENTITY_CLASS);
        criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        criteria.add(Restrictions.in("id", ids));

        final List<ExternalDataManagementSystemPE> list = cast(criteria.list());
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "%d external data management systems have been found.", list.size()));
        }
        return list;
    }
}

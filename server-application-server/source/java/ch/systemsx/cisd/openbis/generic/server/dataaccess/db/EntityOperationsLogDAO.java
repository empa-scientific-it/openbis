/*
 * Copyright ETH 2012 - 2023 Zürich, Scientific IT Services
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
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate5.HibernateTemplate;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityOperationsLogDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.deletion.EntityHistoryCreator;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityOperationsLogEntryPE;

public class EntityOperationsLogDAO extends AbstractGenericEntityDAO<EntityOperationsLogEntryPE>
        implements IEntityOperationsLogDAO
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            EntityOperationsLogDAO.class);

    protected EntityOperationsLogDAO(SessionFactory sessionFactory, EntityHistoryCreator historyCreator)
    {
        super(sessionFactory, EntityOperationsLogEntryPE.class, historyCreator);
    }

    @Override
    public void addLogEntry(Long registrationId)
    {
        EntityOperationsLogEntryPE logEntry = new EntityOperationsLogEntryPE();
        logEntry.setRegistrationId(registrationId);

        HibernateTemplate template = getHibernateTemplate();
        template.persist(logEntry);
        template.flush();

        operationLog.info(String.format("Add entity operation log entry for registration id '%d'.",
                registrationId));
    }

    @Override
    public EntityOperationsLogEntryPE tryFindLogEntry(Long registrationId)
    {
        assert registrationId != null : "Unspecified registration id.";

        final Criteria criteria = currentSession().createCriteria(getEntityClass());
        criteria.add(Restrictions.eq("registrationId", registrationId));
        EntityOperationsLogEntryPE result = (EntityOperationsLogEntryPE) criteria.uniqueResult();
        if (null != result)
        {
            operationLog.info(String.format("Found a log entry for registration id '%d'.",
                    registrationId));
        } else
        {
            operationLog.info(String.format("Did not find a log entry for registration id '%d'.",
                    registrationId));
        }
        return result;
    }

}

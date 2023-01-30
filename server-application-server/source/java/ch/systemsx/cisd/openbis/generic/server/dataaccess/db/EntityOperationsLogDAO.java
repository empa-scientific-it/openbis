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

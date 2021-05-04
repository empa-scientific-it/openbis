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

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.reflection.MethodUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEventDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.deletion.EntityHistoryCreator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.event.DeleteDataSetEventParser;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.jdbc.support.JdbcAccessor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Data access object for {@link EventPE}.
 *
 * @author Piotr Buczek
 */
public class EventDAO extends AbstractGenericEntityDAO<EventPE> implements IEventDAO
{
    private static final Class<EventPE> ENTITY_CLASS = EventPE.class;

    /**
     * This logger does not output any SQL statement. If you want to do so, you had better set an appropriate debugging level for class
     * {@link JdbcAccessor}. </p>
     */
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            EventPE.class);

    public EventDAO(SessionFactory sessionFactory, EntityHistoryCreator historyCreator)
    {
        super(sessionFactory, ENTITY_CLASS, historyCreator);
    }

    @Override
    public EventPE tryFind(String identifier, EntityType entityType, EventType eventType)
    {
        assert identifier != null : "Unspecified identifier.";
        assert entityType != null : "Unspecified entityType.";
        assert eventType != null : "Unspecified eventType.";

        final Criteria criteria = currentSession().createCriteria(EventPE.class);
        criteria.add(Restrictions.like("identifiersInternal", identifier, MatchMode.ANYWHERE));
        criteria.add(Restrictions.eq("entityType", entityType));
        criteria.add(Restrictions.eq("eventType", eventType));
        final EventPE result = tryGetEntity(criteria.uniqueResult());
        if (operationLog.isDebugEnabled())
        {
            String methodName = MethodUtils.getCurrentMethod().getName();
            operationLog.debug(String.format("%s: '%s'.", methodName, result));
        }
        return result;
    }

    @Override
    public List<DeletedDataSet> listDeletedDataSets(Long lastSeenDeletionEventIdOrNull,
            Date maxDeletionDataOrNull)
    {
        final DetachedCriteria criteria = DetachedCriteria.forClass(EventPE.class);
        if (lastSeenDeletionEventIdOrNull != null)
        {
            criteria.add(Restrictions.gt("id", lastSeenDeletionEventIdOrNull));
        }
        if (maxDeletionDataOrNull != null)
        {
            criteria.add(Restrictions.lt("registrationDate", maxDeletionDataOrNull));
        }
        criteria.add(Restrictions.eq("eventType", EventType.DELETION));
        criteria.add(Restrictions.eq("entityType", EntityType.DATASET));
        final List<EventPE> list = cast(getHibernateTemplate().findByCriteria(criteria));
        if (operationLog.isDebugEnabled())
        {
            String lastDesc =
                    lastSeenDeletionEventIdOrNull == null ? "all" : "id > "
                            + lastSeenDeletionEventIdOrNull;
            operationLog.debug(String.format(
                    "%s(%s): %d data set deletion events(s) have been found.", MethodUtils
                            .getCurrentMethod().getName(), lastDesc, list.size()));
        }

        ArrayList<DeletedDataSet> result = new ArrayList<DeletedDataSet>();
        for (EventPE event : list)
        {
            DeleteDataSetEventParser parser = new DeleteDataSetEventParser(event);
            result.addAll(parser.getDeletedDatasets());
        }
        return result;
    }

    @Override public List<EventPE> listEvents(EventType eventType, EntityType entityTypeOrNull, Date lastSeenTimestampOrNull)
    {
        final DetachedCriteria criteria = DetachedCriteria.forClass(EventPE.class);
        criteria.addOrder(Order.asc("registrationDate"));
        criteria.addOrder(Order.asc("id"));
        criteria.add(Restrictions.eq("eventType", eventType));

        if (entityTypeOrNull != null)
        {
            criteria.add(Restrictions.eq("entityType", entityTypeOrNull));
        }

        if (lastSeenTimestampOrNull != null)
        {
            criteria.add(Restrictions.gt("registrationDate", lastSeenTimestampOrNull));
        }

        List<EventPE> list = cast(getHibernateTemplate().findByCriteria(criteria, 0, 1));

        if (list.size() > 0)
        {
            Date registrationDate = list.get(0).getRegistrationDateInternal();
            criteria.add(Restrictions.le("registrationDate", registrationDate));
            list = cast(getHibernateTemplate().findByCriteria(criteria, 0, Integer.MAX_VALUE));
        }

        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "%s(%s, %s): %d events(s) have been found.", MethodUtils.getCurrentMethod().getName(), eventType, entityTypeOrNull, list.size()));
        }

        return list;
    }
}

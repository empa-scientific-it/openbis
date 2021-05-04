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
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEventsSearchDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventsSearchPE;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import java.util.Date;
import java.util.List;

/**
 * Data access object for {@link EventsSearchPE}.
 *
 * @author pkupczyk
 */
public class EventsSearchDAO extends AbstractGenericEntityDAO<EventsSearchPE> implements IEventsSearchDAO
{
    private static final Class<EventsSearchPE> ENTITY_CLASS = EventsSearchPE.class;

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, EventsSearchPE.class);

    public EventsSearchDAO(SessionFactory sessionFactory)
    {
        super(sessionFactory, ENTITY_CLASS, null);
    }

    @Override public void createOrUpdate(EventsSearchPE eventsSearchPE)
    {
        getHibernateTemplate().saveOrUpdate(eventsSearchPE);
    }

    @Override public Date getLastTimestamp(EventType eventType, EventPE.EntityType entityType)
    {
        DetachedCriteria criteria = DetachedCriteria.forClass(EventsSearchPE.class);
        criteria.setProjection(Projections.max("registrationTimestamp"));
        criteria.add(Restrictions.eq("eventType", eventType));
        criteria.add(Restrictions.eq("entityType", entityType));

        final List list = cast(getHibernateTemplate().findByCriteria(criteria));

        final Date lastTimestamp = list.isEmpty() ? null : (Date) list.get(0);

        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "%s(%s, %s): last timestamp = %s.", MethodUtils.getCurrentMethod().getName(), eventType, entityType, lastTimestamp));
        }

        return lastTimestamp;
    }
}

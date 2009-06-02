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
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.jdbc.support.JdbcAccessor;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEventDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;

/**
 * Data access object for {@link EventPE}.
 * 
 * @author Piotr Buczek
 */
public class EventDAO extends AbstractGenericEntityDAO<EventPE> implements IEventDAO
{
    private static final Class<EventPE> ENTITY_CLASS = EventPE.class;

    /**
     * This logger does not output any SQL statement. If you want to do so, you had better set an
     * appropriate debugging level for class {@link JdbcAccessor}. </p>
     */
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, EventPE.class);

    public EventDAO(SessionFactory sessionFactory, DatabaseInstancePE databaseInstance)
    {
        super(sessionFactory, databaseInstance, ENTITY_CLASS);
    }

    public List<EventPE> list(String identifier, EntityType entityType, EventType eventType)
    {
        assert identifier != null : "Unspecified identifier.";
        assert entityType != null : "Unspecified entityType.";
        assert eventType != null : "Unspecified eventType.";

        final Criteria criteria = getSession().createCriteria(EventPE.class);
        criteria.add(Restrictions.eq("identifier", identifier));
        criteria.add(Restrictions.eq("entityType", entityType));
        criteria.add(Restrictions.eq("eventType", eventType));
        final List<EventPE> list = cast(criteria.list());
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s event(s) have been found: '%s'.", list.size()));
        }
        return list;
    }

}

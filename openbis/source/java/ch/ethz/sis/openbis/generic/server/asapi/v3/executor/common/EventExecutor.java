/*
 * Copyright 2019 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.FreezingEvent;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;

/**
 * @author Franz-Josef Elmer
 *
 */
@Component
public class EventExecutor implements IEventExecutor
{
    @Autowired
    private IDAOFactory daoFactory;

    @Override
    public void persist(IOperationContext context, List<FreezingEvent> freezingEvents)
    {
        for (FreezingEvent freezingEvent : freezingEvents)
        {
            EventPE event = new EventPE();
            event.setEventType(EventType.FREEZING);
            event.setEntityType(freezingEvent.getEntityType());
            event.setIdentifiers(Collections.singletonList(freezingEvent.getIdentifier()));
            event.setRegistrator(context.getSession().tryGetPerson());
            event.setReason(freezingEvent.getFreezingFlags().asJson());
            
            daoFactory.getEventDAO().persist(event);
        }
    }

}

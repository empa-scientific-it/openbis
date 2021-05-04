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

package ch.systemsx.cisd.openbis.generic.server.dataaccess;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;

import java.util.Date;
import java.util.List;

/**
 * <i>Data Access Object</i> for {@link EventPE}.
 *
 * @author Piotr Buczek
 */
public interface IEventDAO extends IGenericDAO<EventPE>
{

    /**
     * Tries to find <var>eventType</var> event concerning an object of given <var>entityType</var> and <code>identifier</code>.
     */
    public EventPE tryFind(final String identifier, final EntityType entityType,
            final EventType eventType);

    /**
     * Lists deleted data sets with the deletion event id greater than the specified one.
     */
    public List<DeletedDataSet> listDeletedDataSets(Long lastSeenDeletionEventIdOrNull,
            Date maxDeletionDataOrNull);

    /**
     * Lists events with the specified eventType and entityType.
     */
    public List<EventPE> listEvents(EventType eventType, EntityType entityTypeOrNull, Date lastSeenTimestampOrNull);
}

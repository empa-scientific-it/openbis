/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.event;

import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectQuery;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectRelationRecord;
import ch.systemsx.cisd.common.db.mapper.LongSetMapper;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.lemnik.eodsql.Select;

import java.util.List;

/**
 * @author pkupczyk
 */
public interface EventQuery extends ObjectQuery
{
    @Select(sql = "select es.id, es.event_type as eventType, es.entity_type as entityType, "
            + "es.entity_space as entitySpace, es.entity_space_perm_id as entitySpaceId, "
            + "es.entity_project as entityProject, es.entity_project_perm_id as entityProjectId, "
            + "es.entity_registerer as entityRegistrator, es.entity_registration_timestamp as entityRegistrationDate, "
            + "es.identifier, es.description, es.reason, es.content, es.registration_timestamp as registrationDate "
            + "from events_search es "
            + "where es.id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<EventBaseRecord> getEvents(LongSet eventIds);

    @Select(sql = "select es.id as objectId, es.pers_id_registerer as relatedId from events_search es where es.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getRegistratorIds(LongSet eventIds);
}

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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.query;

import java.util.List;

import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectQuery;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectRelationRecord;
import ch.systemsx.cisd.common.db.mapper.LongSetMapper;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.lemnik.eodsql.Select;

/**
 * @author pkupczyk
 */
public interface QueryQuery extends ObjectQuery
{

    @Select(sql = "select q.id, q.name, q.description, q.db_key as database, q.query_type as queryType, q.entity_type_code as entityTypeCodePattern, q.expression as sql, q.is_public as isPublic, q.registration_timestamp as registrationDate, q.modification_timestamp as modificationDate from queries q where q.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<QueryBaseRecord> getQueries(LongSet queryIds);

    @Select(sql = "select q.id as objectId, q.pers_id_registerer as relatedId from queries q where q.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getRegistratorIds(LongSet queryIds);

    @Select(sql = "select q.id, q.is_public as isPublic, q.db_key as databaseKey, q.pers_id_registerer as registratorId from queries q where q.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<QueryAuthorizationRecord> getAuthorizations(LongSet queryIds);

}

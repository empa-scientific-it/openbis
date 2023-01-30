/*
 * Copyright 2017 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.authorizationgroup;

import java.util.List;

import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectQuery;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectRelationRecord;
import ch.systemsx.cisd.common.db.mapper.LongSetMapper;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.lemnik.eodsql.Select;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public interface AuthorizationGroupQuery extends ObjectQuery
{
    @Select(sql = "select id, code, description, registration_timestamp as registrationDate, "
            + "modification_timestamp as modificationDate "
            + "from authorization_groups where id = any(?{1})", 
            parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<AuthorizationGroupBaseRecord> getAuthorizationGroups(LongSet authorizationGroupIds);
    
    @Select(sql = "select id as objectId, pers_id_registerer as relatedId from authorization_groups where id = any(?{1})", 
            parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getRegistratorIds(LongSet authorizationGroupIds);
    
    @Select(sql = "select ag_id as objectId, pers_id as relatedId from authorization_group_persons where ag_id = any(?{1})",
            parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getUserIds(LongSet authorizationGroupIds);

    @Select(sql = "select ag_id_grantee as objectId, id as relatedId from role_assignments where ag_id_grantee = any(?{1})",
            parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getRoleAssignmentIds(LongSet authorizationGroupIds);
    
}

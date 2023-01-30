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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.roleassignment;

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
public interface RoleAssignmentQuery extends ObjectQuery
{
    @Select(sql = "select id, role_code, space_id, project_id, registration_timestamp as registrationDate"
            + " from role_assignments where id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<RoleAssignmentBaseRecord> getRoleAssignments(LongSet roleAssignmentIds);

    @Select(sql = "select id as objectId, space_id as relatedId"
            + " from role_assignments where id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getSpaceIds(LongSet roleAssignmentIds);
    
    @Select(sql = "select id as objectId, project_id as relatedId"
            + " from role_assignments where id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getProjectIds(LongSet roleAssignmentIds);

    @Select(sql = "select id as objectId, pers_id_grantee as relatedId"
            + " from role_assignments where id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getUserIds(LongSet roleAssignmentIds);
    
    @Select(sql = "select id as objectId, ag_id_grantee as relatedId"
            + " from role_assignments where id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getAuthorizationGroupIds(LongSet roleAssignmentIds);

    @Select(sql = "select id as objectId, pers_id_registerer as relatedId from role_assignments where id = any(?{1})", 
            parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getRegistratorIds(LongSet authorizationGroupIds);
    
}

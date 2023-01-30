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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.RoleAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.fetchoptions.RoleAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectRelationRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectToManyRelationTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.roleassignment.IRoleAssignmentTranslator;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.lemnik.eodsql.QueryTool;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Component
public class AuthorizationGroupRoleAssignmentTranslator extends ObjectToManyRelationTranslator<RoleAssignment, RoleAssignmentFetchOptions> implements IAuthorizationGroupRoleAssignmentTranslator
{
    @Autowired
    private IRoleAssignmentTranslator roleAssignmentTranslator;
    
    @Override
    protected List<ObjectRelationRecord> loadRecords(LongOpenHashSet objectIds)
    {
        AuthorizationGroupQuery query = QueryTool.getManagedQuery(AuthorizationGroupQuery.class);
        return query.getRoleAssignmentIds(objectIds);
    }

    @Override
    protected Map<Long, RoleAssignment> translateRelated(TranslationContext context, Collection<Long> relatedIds,
            RoleAssignmentFetchOptions relatedFetchOptions)
    {
        return roleAssignmentTranslator.translate(context, relatedIds, relatedFetchOptions);
    }

    @Override
    protected Collection<RoleAssignment> createCollection()
    {
        return new ArrayList<>();
    }
}

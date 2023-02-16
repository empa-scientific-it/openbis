/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectBaseTranslator;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.lemnik.eodsql.QueryTool;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Component
public class RoleAssignmentBaseTranslator extends ObjectBaseTranslator<RoleAssignmentBaseRecord> implements IRoleAssignmentBaseTranslator
{

    @Override
    protected List<RoleAssignmentBaseRecord> loadRecords(LongOpenHashSet objectIds)
    {
        RoleAssignmentQuery query = QueryTool.getManagedQuery(RoleAssignmentQuery.class);
        return query.getRoleAssignments(objectIds);
    }
}

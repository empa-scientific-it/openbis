/*
 * Copyright 2015 ETH Zuerich, CISD
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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.roleassignment.RoleAssignmentUtils;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.plugin.query.shared.DatabaseDefinition;
import ch.systemsx.cisd.openbis.plugin.query.shared.IQueryDatabaseDefinitionProviderAutoInitialized;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.lemnik.eodsql.QueryTool;

/**
 * @author pkupczyk
 */
@Component
public class QueryAuthorizationValidator implements IQueryAuthorizationValidator
{

    @Autowired
    private IQueryDatabaseDefinitionProviderAutoInitialized databaseProvider;

    @Override
    public Set<Long> validate(PersonPE person, Collection<Long> queryIds)
    {
        boolean isInstanceAdmin = RoleAssignmentUtils.isInstanceAdmin(person);
        QueryQuery query = QueryTool.getManagedQuery(QueryQuery.class);
        List<QueryAuthorizationRecord> records = query.getAuthorizations(new LongOpenHashSet(queryIds));
        Set<Long> result = new HashSet<Long>();

        for (QueryAuthorizationRecord record : records)
        {
            DatabaseDefinition database = databaseProvider.getDefinition(record.databaseKey);

            if (database != null)
            {
                if (record.isPublic || record.registratorId.equals(person.getId()) || isInstanceAdmin)
                {
                    result.add(record.id);
                }
            }
        }

        return result;
    }

}

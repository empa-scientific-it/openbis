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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.authorizationgroup;

import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.id.AuthorizationGroupPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.AbstractListTechIdByPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.TechIdStringIdentifierRecord;

import net.lemnik.eodsql.QueryTool;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ListAuthorizationGroupTechIdByPermId extends AbstractListTechIdByPermId<AuthorizationGroupPermId>
{
    
    @Override
    protected Class<AuthorizationGroupPermId> getIdClass()
    {
        return AuthorizationGroupPermId.class;
    }

    @Override
    protected List<TechIdStringIdentifierRecord> queryTechIds(String[] permIds)
    {
        AuthorizationGroupQuery query = QueryTool.getManagedQuery(AuthorizationGroupQuery.class);
        return query.listAuthorizationGroupTechIdsByPermIds(permIds);
    }

    @Override
    protected AuthorizationGroupPermId createPermId(String permIdAsString)
    {
        return new AuthorizationGroupPermId(permIdAsString);
    }
}

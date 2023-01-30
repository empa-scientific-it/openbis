/*
 * Copyright 2018 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.query;

import java.util.LinkedList;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.QueryTechId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.AbstractListObjectById;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IQueryDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.QueryPE;

/**
 * @author pkupczyk
 */
public class ListQueryByTechId extends AbstractListObjectById<QueryTechId, QueryPE>
{

    private IQueryDAO queryDAO;

    public ListQueryByTechId(IQueryDAO queryDAO)
    {
        this.queryDAO = queryDAO;
    }

    @Override
    public Class<QueryTechId> getIdClass()
    {
        return QueryTechId.class;
    }

    @Override
    public QueryTechId createId(QueryPE query)
    {
        return new QueryTechId(query.getId());
    }

    @Override
    public List<QueryPE> listByIds(IOperationContext context, List<QueryTechId> ids)
    {
        List<Long> techIds = new LinkedList<Long>();

        for (QueryTechId id : ids)
        {
            techIds.add(id.getTechId());
        }

        return queryDAO.listByIDs(techIds);
    }

}

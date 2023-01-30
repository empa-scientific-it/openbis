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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.QueryName;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.AbstractListObjectById;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IQueryDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.QueryPE;

/**
 * @author pkupczyk
 */
public class ListQueryByName extends AbstractListObjectById<QueryName, QueryPE>
{

    private IQueryDAO queryDAO;

    public ListQueryByName(IQueryDAO queryDAO)
    {
        this.queryDAO = queryDAO;
    }

    @Override
    public Class<QueryName> getIdClass()
    {
        return QueryName.class;
    }

    @Override
    public QueryName createId(QueryPE query)
    {
        return new QueryName(query.getName());
    }

    @Override
    public List<QueryPE> listByIds(IOperationContext context, List<QueryName> ids)
    {
        List<String> names = new LinkedList<String>();

        for (QueryName id : ids)
        {
            names.add(id.getName());
        }

        return queryDAO.listByNames(names);
    }

}

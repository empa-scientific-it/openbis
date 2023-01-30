/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess;

import java.util.Collection;
import java.util.List;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.QueryType;
import ch.systemsx.cisd.openbis.generic.shared.dto.QueryPE;

/**
 * @author Franz-Josef Elmer
 */
public interface IQueryDAO extends IGenericDAO<QueryPE>
{

    /**
     * Lists all queries defined of specified type in the home database instance.
     */
    public List<QueryPE> listQueries(QueryType queryType);

    /**
     * Creates a query in home database instance.
     */
    public void createQuery(QueryPE query) throws DataAccessException;

    /**
     * List queries by ids.
     */
    List<QueryPE> listByIDs(Collection<Long> ids);

    /**
     * List queries by names.
     */
    List<QueryPE> listByNames(Collection<String> names);

}

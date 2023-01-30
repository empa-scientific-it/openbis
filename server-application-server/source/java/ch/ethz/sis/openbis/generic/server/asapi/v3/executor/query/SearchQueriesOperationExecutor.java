/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.query;

import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ILocalSearchManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.Query;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.fetchoptions.QueryFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.search.QuerySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.search.SearchQueriesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.search.SearchQueriesOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.ISearchObjectExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.SearchObjectsPEOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.query.IQueryTranslator;
import ch.systemsx.cisd.openbis.generic.shared.dto.QueryPE;

/**
 * @author pkupczyk
 */
@Component
public class SearchQueriesOperationExecutor extends SearchObjectsPEOperationExecutor<Query, QueryPE, QuerySearchCriteria, QueryFetchOptions>
        implements ISearchQueriesOperationExecutor
{

    @Autowired
    private ISearchQueryExecutor searchExecutor;

    @Autowired
    private IQueryTranslator translator;

    @Override
    protected Class<? extends SearchObjectsOperation<QuerySearchCriteria, QueryFetchOptions>> getOperationClass()
    {
        return SearchQueriesOperation.class;
    }

    @Override
    protected ISearchObjectExecutor<QuerySearchCriteria, QueryPE> getExecutor()
    {
        return searchExecutor;
    }

    @Override
    protected ITranslator<Long, Query, QueryFetchOptions> getTranslator()
    {
        return translator;
    }

    @Override
    protected SearchObjectsOperationResult<Query> getOperationResult(SearchResult<Query> searchResult)
    {
        return new SearchQueriesOperationResult(searchResult);
    }

    @Override
    protected ILocalSearchManager<QuerySearchCriteria, Query, Long> getSearchManager() {
        throw new RuntimeException("This method is not implemented yet.");
    }

}

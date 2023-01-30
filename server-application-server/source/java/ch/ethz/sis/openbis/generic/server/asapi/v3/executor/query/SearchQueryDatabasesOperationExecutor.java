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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.QueryDatabase;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.fetchoptions.QueryDatabaseFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.search.QueryDatabaseSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.search.SearchQueryDatabasesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.search.SearchQueryDatabasesOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.AbstractSearchObjectsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ILocalSearchManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.query.IQueryDatabaseTranslator;
import ch.systemsx.cisd.openbis.plugin.query.shared.DatabaseDefinition;

/**
 * @author pkupczyk
 */
@Component
public class SearchQueryDatabasesOperationExecutor extends
        AbstractSearchObjectsOperationExecutor<QueryDatabase, DatabaseDefinition, QueryDatabaseSearchCriteria, QueryDatabaseFetchOptions>
        implements ISearchQueryDatabasesOperationExecutor
{

    @Autowired
    private ISearchQueryDatabaseExecutor searchExecutor;

    @Autowired
    private IQueryDatabaseTranslator translator;

    @Override
    protected Class<? extends SearchObjectsOperation<QueryDatabaseSearchCriteria, QueryDatabaseFetchOptions>> getOperationClass()
    {
        return SearchQueryDatabasesOperation.class;
    }

    @Override
    protected List<DatabaseDefinition> doSearch(IOperationContext context, QueryDatabaseSearchCriteria criteria,
            QueryDatabaseFetchOptions fetchOptions)
    {
        return searchExecutor.search(context, criteria);
    }

    @Override
    protected Map<DatabaseDefinition, QueryDatabase> doTranslate(TranslationContext translationContext,
            Collection<DatabaseDefinition> objects, QueryDatabaseFetchOptions fetchOptions)
    {
        return translator.translate(translationContext, objects, fetchOptions);
    }

    @Override
    protected SearchObjectsOperationResult<QueryDatabase> getOperationResult(SearchResult<QueryDatabase> searchResult)
    {
        return new SearchQueryDatabasesOperationResult(searchResult);
    }

    @Override
    protected ILocalSearchManager<QueryDatabaseSearchCriteria, QueryDatabase, DatabaseDefinition> getSearchManager()
    {
        throw new RuntimeException("This method is not implemented yet.");
    }

}

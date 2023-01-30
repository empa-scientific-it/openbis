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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.SearchDataSetsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.SearchDataSetsOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.ISearchObjectExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.SearchObjectsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DataSetSearchManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ILocalSearchManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.dataset.IDataSetTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author pkupczyk
 */
@Component("search-data-sets-operation-executor")
public class SearchDataSetsOperationExecutor extends SearchObjectsOperationExecutor<DataSet, Long, DataSetSearchCriteria, DataSetFetchOptions>
        implements ISearchDataSetsOperationExecutor
{

    @Autowired
    private ISearchDataSetExecutor searchExecutor;

    @Autowired
    private IDataSetTranslator translator;

    @Autowired
    private DataSetSearchManager dataSetSearchManager;

    @Override
    protected Class<? extends SearchObjectsOperation<DataSetSearchCriteria, DataSetFetchOptions>> getOperationClass()
    {
        return SearchDataSetsOperation.class;
    }

    @Override
    protected ISearchObjectExecutor<DataSetSearchCriteria, Long> getExecutor()
    {
        return searchExecutor;
    }

    @Override
    protected ITranslator<Long, DataSet, DataSetFetchOptions> getTranslator()
    {
        return translator;
    }

    @Override
    protected ILocalSearchManager<DataSetSearchCriteria, DataSet, Long> getSearchManager()
    {
        return dataSetSearchManager;
    }

    @Override
    protected SearchObjectsOperationResult<DataSet> getOperationResult(SearchResult<DataSet> searchResult)
    {
        return new SearchDataSetsOperationResult(searchResult);
    }

    @Override
    protected SearchObjectsOperationResult<DataSet> doExecute(IOperationContext context, SearchObjectsOperation<DataSetSearchCriteria, DataSetFetchOptions> operation)
    {
        return executeDirectSQLSearch(context, operation);
    }
}

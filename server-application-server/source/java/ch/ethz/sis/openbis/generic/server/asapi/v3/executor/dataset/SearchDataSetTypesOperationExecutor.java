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

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DataSetTypeSearchManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ILocalSearchManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.SearchDataSetTypesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.SearchDataSetTypesOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.ISearchObjectExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.SearchObjectsPEOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.dataset.IDataSetTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;

/**
 * @author pkupczyk
 */
@Component
public class SearchDataSetTypesOperationExecutor
        extends SearchObjectsPEOperationExecutor<DataSetType, DataSetTypePE, DataSetTypeSearchCriteria, DataSetTypeFetchOptions>
        implements ISearchDataSetTypesOperationExecutor
{

    @Autowired
    private ISearchDataSetTypeExecutor searchExecutor;

    @Autowired
    private IDataSetTypeTranslator translator;

    @Autowired
    private DataSetTypeSearchManager dataSetTypeSearchManager;

    @Override
    protected Class<? extends SearchObjectsOperation<DataSetTypeSearchCriteria, DataSetTypeFetchOptions>> getOperationClass()
    {
        return SearchDataSetTypesOperation.class;
    }

    @Override
    protected ISearchObjectExecutor<DataSetTypeSearchCriteria, DataSetTypePE> getExecutor()
    {
        return searchExecutor;
    }

    @Override
    protected ITranslator<Long, DataSetType, DataSetTypeFetchOptions> getTranslator()
    {
        return translator;
    }

    @Override
    protected SearchObjectsOperationResult<DataSetType> getOperationResult(SearchResult<DataSetType> searchResult)
    {
        return new SearchDataSetTypesOperationResult(searchResult);
    }

    @Override
    protected SearchObjectsOperationResult<DataSetType> doExecute(final IOperationContext context, final SearchObjectsOperation<DataSetTypeSearchCriteria, DataSetTypeFetchOptions> operation)
    {
        return executeDirectSQLSearch(context, operation);
    }

    @Override
    protected ILocalSearchManager<DataSetTypeSearchCriteria, DataSetType, Long> getSearchManager() {
        return dataSetTypeSearchManager;
    }

}

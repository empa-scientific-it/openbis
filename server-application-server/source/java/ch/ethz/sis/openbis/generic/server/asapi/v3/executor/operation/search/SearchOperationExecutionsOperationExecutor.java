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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.search;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ILocalSearchManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecution;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.fetchoptions.OperationExecutionFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.search.OperationExecutionSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.search.SearchOperationExecutionsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.search.SearchOperationExecutionsOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.AbstractSearchObjectsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.store.IOperationExecutionStore;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.NopTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;

/**
 * @author pkupczyk
 */
@Component
public class SearchOperationExecutionsOperationExecutor extends
        AbstractSearchObjectsOperationExecutor<OperationExecution, OperationExecution, OperationExecutionSearchCriteria, OperationExecutionFetchOptions>
        implements ISearchOperationExecutionsOperationExecutor
{

    @Autowired
    private IOperationExecutionStore executionStore;

    @Override
    protected Class<? extends SearchObjectsOperation<OperationExecutionSearchCriteria, OperationExecutionFetchOptions>> getOperationClass()
    {
        return SearchOperationExecutionsOperation.class;
    }

    @Override
    protected List<OperationExecution> doSearch(IOperationContext context, OperationExecutionSearchCriteria criteria,
            OperationExecutionFetchOptions fetchOptions)
    {
        return executionStore.getExecutions(context, fetchOptions);
    }

    @Override
    protected Map<OperationExecution, OperationExecution> doTranslate(TranslationContext translationContext,
            Collection<OperationExecution> objects, OperationExecutionFetchOptions fetchOptions)
    {
        return new NopTranslator<OperationExecution, OperationExecutionFetchOptions>().translate(translationContext, objects, fetchOptions);
    }

    @Override
    protected SearchObjectsOperationResult<OperationExecution> getOperationResult(SearchResult<OperationExecution> searchResult)
    {
        return new SearchOperationExecutionsOperationResult(searchResult);
    }

    @Override
    protected ILocalSearchManager<OperationExecutionSearchCriteria, OperationExecution, OperationExecution> getSearchManager() {
        throw new RuntimeException("This method is not implemented yet.");
    }

}

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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.sample;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ISearchManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ILocalSearchManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SampleTypeSearchManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SearchSampleTypesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SearchSampleTypesOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.ISearchObjectExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.SearchObjectsPEOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.sample.ISampleTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;

/**
 * @author pkupczyk
 */
@Component
public class SearchSampleTypesOperationExecutor
        extends SearchObjectsPEOperationExecutor<SampleType, SampleTypePE, SampleTypeSearchCriteria, SampleTypeFetchOptions>
        implements ISearchSampleTypesOperationExecutor
{

    @Autowired
    private ISearchSampleTypeExecutor searchExecutor;

    @Autowired
    private ISampleTypeTranslator translator;

    @Autowired
    private SampleTypeSearchManager sampleTypeSearchManager;

    @Override
    protected Class<? extends SearchObjectsOperation<SampleTypeSearchCriteria, SampleTypeFetchOptions>> getOperationClass()
    {
        return SearchSampleTypesOperation.class;
    }

    @Override
    protected ISearchObjectExecutor<SampleTypeSearchCriteria, SampleTypePE> getExecutor()
    {
        return searchExecutor;
    }

    @Override
    protected ITranslator<Long, SampleType, SampleTypeFetchOptions> getTranslator()
    {
        return translator;
    }

    @Override
    protected SearchObjectsOperationResult<SampleType> getOperationResult(SearchResult<SampleType> searchResult)
    {
        return new SearchSampleTypesOperationResult(searchResult);
    }

    @Override
    protected ILocalSearchManager<SampleTypeSearchCriteria, SampleType, Long> getSearchManager() {
        return sampleTypeSearchManager;
    }

    @Override
    protected SearchObjectsOperationResult<SampleType> doExecute(final IOperationContext context,
            final SearchObjectsOperation<SampleTypeSearchCriteria, SampleTypeFetchOptions> operation)
    {
        return executeDirectSQLSearch(context, operation);
    }

}

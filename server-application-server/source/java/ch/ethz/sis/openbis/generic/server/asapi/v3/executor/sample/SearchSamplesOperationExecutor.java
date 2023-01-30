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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SearchSamplesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SearchSamplesOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.ISearchObjectExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.SearchObjectsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ILocalSearchManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SampleSearchManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.sample.ISampleTranslator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author pkupczyk
 */
@Component("search-samples-operation-executor")
public class SearchSamplesOperationExecutor extends SearchObjectsOperationExecutor<Sample, Long, SampleSearchCriteria, SampleFetchOptions>
        implements ISearchSamplesOperationExecutor
{

    @Autowired
    private ISearchSampleExecutor searchExecutor;

    @Autowired
    private ISampleTranslator translator;

    @Autowired
    private SampleSearchManager sampleSearchManager;

    @Autowired
    protected IDAOFactory daoFactory;

    @Override
    protected Class<? extends SearchObjectsOperation<SampleSearchCriteria, SampleFetchOptions>> getOperationClass()
    {
        return SearchSamplesOperation.class;
    }

    @Override
    protected ISearchObjectExecutor<SampleSearchCriteria, Long> getExecutor()
    {
        return searchExecutor;
    }

    @Override
    protected ITranslator<Long, Sample, SampleFetchOptions> getTranslator()
    {
        return translator;
    }

    @Override
    protected ILocalSearchManager<SampleSearchCriteria, Sample, Long> getSearchManager()
    {
        return sampleSearchManager;
    }

    @Override
    protected SearchObjectsOperationResult<Sample> getOperationResult(SearchResult<Sample> searchResult)
    {
        return new SearchSamplesOperationResult(searchResult);
    }

    @Override
    protected SearchObjectsOperationResult<Sample> doExecute(final IOperationContext context,
            final SearchObjectsOperation<SampleSearchCriteria, SampleFetchOptions> operation)
    {
        return executeDirectSQLSearch(context, operation);
    }

}

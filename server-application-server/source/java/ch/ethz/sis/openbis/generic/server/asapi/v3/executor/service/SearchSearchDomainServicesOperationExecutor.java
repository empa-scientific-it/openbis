/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.service;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.SearchDomainService;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.fetchoptions.SearchDomainServiceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.search.SearchDomainServiceSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.search.SearchSearchDomainServicesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.search.SearchSearchDomainServicesOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.ISearchObjectExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.SearchObjectsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ILocalSearchManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.AbstractTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class SearchSearchDomainServicesOperationExecutor
        extends SearchObjectsOperationExecutor<SearchDomainService, SearchDomainService, SearchDomainServiceSearchCriteria, SearchDomainServiceFetchOptions>
        implements ISearchSearchDomainServicesOperationExecutor
{
    @Autowired
    private ISearchSearchDomainServiceExecutor searchExecutor;
    
    @Override
    protected Class<? extends SearchObjectsOperation<SearchDomainServiceSearchCriteria, SearchDomainServiceFetchOptions>> getOperationClass()
    {
        return SearchSearchDomainServicesOperation.class;
    }

    @Override
    protected ISearchObjectExecutor<SearchDomainServiceSearchCriteria, SearchDomainService> getExecutor()
    {
        return searchExecutor;
    }

    @Override
    protected ITranslator<SearchDomainService, SearchDomainService, SearchDomainServiceFetchOptions> getTranslator()
    {
        return new AbstractTranslator<SearchDomainService, SearchDomainService, SearchDomainServiceFetchOptions>()
        {
            @Override
            protected SearchDomainService doTranslate(TranslationContext context, SearchDomainService object,
                    SearchDomainServiceFetchOptions fetchOptions)
            {
                object.setFetchOptions(fetchOptions);
                return object;
            }
        };
    }

    @Override
    protected ILocalSearchManager<SearchDomainServiceSearchCriteria, SearchDomainService, SearchDomainService>
    getSearchManager()
    {
        throw new RuntimeException("This method is not implemented yet.");
    }

    @Override
    protected SearchObjectsOperationResult<SearchDomainService> getOperationResult(SearchResult<SearchDomainService> searchResult)
    {
        return new SearchSearchDomainServicesOperationResult(searchResult);
    }

}

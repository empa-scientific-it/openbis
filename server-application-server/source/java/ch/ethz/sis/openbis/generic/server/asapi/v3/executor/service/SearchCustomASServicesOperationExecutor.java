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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.service;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.CustomASService;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.fetchoptions.CustomASServiceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.search.CustomASServiceSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.search.SearchCustomASServicesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.search.SearchCustomASServicesOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.ISearchObjectExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.SearchObjectsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ILocalSearchManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.AbstractTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author pkupczyk
 */
@Component
public class SearchCustomASServicesOperationExecutor
        extends SearchObjectsOperationExecutor<CustomASService, CustomASService, CustomASServiceSearchCriteria, CustomASServiceFetchOptions>
        implements ISearchCustomASServicesOperationExecutor
{

    @Autowired
    private ISearchCustomASServiceExecutor searchExecutor;

    @Override
    protected Class<? extends SearchObjectsOperation<CustomASServiceSearchCriteria, CustomASServiceFetchOptions>> getOperationClass()
    {
        return SearchCustomASServicesOperation.class;
    }

    @Override
    protected ISearchObjectExecutor<CustomASServiceSearchCriteria, CustomASService> getExecutor()
    {
        return searchExecutor;
    }

    @Override
    protected ITranslator<CustomASService, CustomASService, CustomASServiceFetchOptions> getTranslator()
    {
        return new AbstractTranslator<CustomASService, CustomASService, CustomASServiceFetchOptions>()
            {
                @Override
                protected CustomASService doTranslate(TranslationContext context, CustomASService object,
                        CustomASServiceFetchOptions fetchOptions)
                {
                    object.setFetchOptions(fetchOptions);
                    return object;
                }
            };
    }

    @Override
    protected ILocalSearchManager<CustomASServiceSearchCriteria, CustomASService, CustomASService> getSearchManager()
    {
        throw new RuntimeException("This method is not implemented yet.");
    }

    @Override
    protected SearchObjectsOperationResult<CustomASService> getOperationResult(SearchResult<CustomASService> searchResult)
    {
        return new SearchCustomASServicesOperationResult(searchResult);
    }

}

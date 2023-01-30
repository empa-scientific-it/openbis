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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.ReportingService;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.fetchoptions.ReportingServiceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.search.ReportingServiceSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.search.SearchReportingServicesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.search.SearchReportingServicesOperationResult;
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
public class SearchReportingServicesOperationExecutor
        extends
        SearchObjectsOperationExecutor<ReportingService, ReportingService, ReportingServiceSearchCriteria, ReportingServiceFetchOptions>
        implements ISearchReportingServicesOperationExecutor
{
    @Autowired
    private ISearchReportingServiceExecutor searchExecutor;

    @Override
    protected ISearchObjectExecutor<ReportingServiceSearchCriteria, ReportingService> getExecutor()
    {
        return searchExecutor;
    }

    @Override
    protected ITranslator<ReportingService, ReportingService, ReportingServiceFetchOptions> getTranslator()
    {
        return new AbstractTranslator<ReportingService, ReportingService, ReportingServiceFetchOptions>()
            {
                @Override
                protected ReportingService doTranslate(TranslationContext context, ReportingService object,
                        ReportingServiceFetchOptions fetchOptions)
                {
                    object.setFetchOptions(fetchOptions);
                    return object;
                }
            };
    }

    @Override
    protected ILocalSearchManager<ReportingServiceSearchCriteria, ReportingService, ReportingService> getSearchManager()
    {
        throw new RuntimeException("This method is not implemented yet.");
    }

    @Override
    protected SearchObjectsOperationResult<ReportingService> getOperationResult(SearchResult<ReportingService> searchResult)
    {
        return new SearchReportingServicesOperationResult(searchResult);
    }

    @Override
    protected Class<? extends SearchObjectsOperation<ReportingServiceSearchCriteria, ReportingServiceFetchOptions>> getOperationClass()
    {
        return SearchReportingServicesOperation.class;
    }

}

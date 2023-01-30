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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.externaldms;

import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ILocalSearchManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.ExternalDms;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.fetchoptions.ExternalDmsFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.search.ExternalDmsSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.search.SearchExternalDmsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.search.SearchExternalDmsOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.ISearchObjectExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.SearchObjectsPEOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.externaldms.IExternalDmsTranslator;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataManagementSystemPE;

/**
 * @author pkupczyk
 */
@Component
public class SearchExternalDmsOperationExecutor
        extends SearchObjectsPEOperationExecutor<ExternalDms, ExternalDataManagementSystemPE, ExternalDmsSearchCriteria, ExternalDmsFetchOptions>
        implements ISearchExternalDmsOperationExecutor
{

    @Autowired
    private ISearchExternalDmsExecutor searchExecutor;

    @Autowired
    private IExternalDmsTranslator translator;

    @Override
    protected Class<? extends SearchObjectsOperation<ExternalDmsSearchCriteria, ExternalDmsFetchOptions>> getOperationClass()
    {
        return SearchExternalDmsOperation.class;
    }

    @Override
    protected ISearchObjectExecutor<ExternalDmsSearchCriteria, ExternalDataManagementSystemPE> getExecutor()
    {
        return searchExecutor;
    }

    @Override
    protected ITranslator<Long, ExternalDms, ExternalDmsFetchOptions> getTranslator()
    {
        return translator;
    }

    @Override
    protected SearchObjectsOperationResult<ExternalDms> getOperationResult(SearchResult<ExternalDms> searchResult)
    {
        return new SearchExternalDmsOperationResult(searchResult);
    }

    @Override
    protected ILocalSearchManager<ExternalDmsSearchCriteria, ExternalDms, Long> getSearchManager() {
        throw new RuntimeException("This method is not implemented yet.");
    }

}

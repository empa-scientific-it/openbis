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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.datastore;

import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ILocalSearchManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.DataStore;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.fetchoptions.DataStoreFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.search.DataStoreSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.search.SearchDataStoresOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.search.SearchDataStoresOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.ISearchObjectExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.SearchObjectsPEOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.datastore.IDataStoreTranslator;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;

/**
 * @author pkupczyk
 */
@Component
public class SearchDataStoresOperationExecutor
        extends SearchObjectsPEOperationExecutor<DataStore, DataStorePE, DataStoreSearchCriteria, DataStoreFetchOptions>
        implements ISearchDataStoresOperationExecutor
{

    @Autowired
    private ISearchDataStoreExecutor searchExecutor;

    @Autowired
    private IDataStoreTranslator translator;

    @Override
    protected Class<? extends SearchObjectsOperation<DataStoreSearchCriteria, DataStoreFetchOptions>> getOperationClass()
    {
        return SearchDataStoresOperation.class;
    }

    @Override
    protected ISearchObjectExecutor<DataStoreSearchCriteria, DataStorePE> getExecutor()
    {
        return searchExecutor;
    }

    @Override
    protected ITranslator<Long, DataStore, DataStoreFetchOptions> getTranslator()
    {
        return translator;
    }

    @Override
    protected SearchObjectsOperationResult<DataStore> getOperationResult(SearchResult<DataStore> searchResult)
    {
        return new SearchDataStoresOperationResult(searchResult);
    }

    @Override
    protected ILocalSearchManager<DataStoreSearchCriteria, DataStore, Long> getSearchManager() {
        throw new RuntimeException("This method is not implemented yet.");
    }

}

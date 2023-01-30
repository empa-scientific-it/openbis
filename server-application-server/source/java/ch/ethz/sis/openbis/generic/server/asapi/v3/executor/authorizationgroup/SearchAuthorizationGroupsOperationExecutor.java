/*
 * Copyright 2017 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.authorizationgroup;

import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ILocalSearchManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.AuthorizationGroup;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.fetchoptions.AuthorizationGroupFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.search.AuthorizationGroupSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.search.SearchAuthorizationGroupsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.search.SearchAuthorizationGroupsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.ISearchObjectExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.SearchObjectsPEOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.systemsx.cisd.openbis.generic.shared.dto.AuthorizationGroupPE;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Component
public class SearchAuthorizationGroupsOperationExecutor 
        extends SearchObjectsPEOperationExecutor<AuthorizationGroup, AuthorizationGroupPE, AuthorizationGroupSearchCriteria, AuthorizationGroupFetchOptions> 
        implements ISearchAuthorizationGroupsOperationExecutor
{
    @Autowired
    private ISearchAuthorizationGroupExecutor searchExecutor;
    
    @Autowired
    private IAuthorizationGroupTranslator translator;

    @Override
    protected Class<? extends SearchObjectsOperation<AuthorizationGroupSearchCriteria, AuthorizationGroupFetchOptions>> getOperationClass()
    {
        return SearchAuthorizationGroupsOperation.class;
    }
    
    @Override
    protected ISearchObjectExecutor<AuthorizationGroupSearchCriteria, AuthorizationGroupPE> getExecutor()
    {
        return searchExecutor;
    }

    @Override
    protected ITranslator<Long, AuthorizationGroup, AuthorizationGroupFetchOptions> getTranslator()
    {
        return translator;
    }

    @Override
    protected SearchObjectsOperationResult<AuthorizationGroup> getOperationResult(SearchResult<AuthorizationGroup> searchResult)
    {
        return new SearchAuthorizationGroupsOperationResult(searchResult);
    }

    @Override
    protected ILocalSearchManager<AuthorizationGroupSearchCriteria, AuthorizationGroup, Long> getSearchManager() {
        throw new RuntimeException("This method is not implemented yet.");
    }


}

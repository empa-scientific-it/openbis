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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.roleassignment;

import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ILocalSearchManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.RoleAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.fetchoptions.RoleAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.search.RoleAssignmentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.search.SearchRoleAssignmentsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.search.SearchRoleAssignmentsOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.ISearchObjectExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.SearchObjectsPEOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.roleassignment.IRoleAssignmentTranslator;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Component
public class SearchRoleAssignmentsOperationExecutor
        extends SearchObjectsPEOperationExecutor<RoleAssignment, RoleAssignmentPE, RoleAssignmentSearchCriteria, RoleAssignmentFetchOptions> 
        implements ISearchRoleAssignmentsOperationExecutor
{
    @Autowired
    private ISearchRoleAssignmentExecutor searchExecutor;
    
    @Autowired
    private IRoleAssignmentTranslator translator;

    @Override
    protected ISearchObjectExecutor<RoleAssignmentSearchCriteria, RoleAssignmentPE> getExecutor()
    {
        return searchExecutor;
    }

    @Override
    protected ITranslator<Long, RoleAssignment, RoleAssignmentFetchOptions> getTranslator()
    {
        return translator;
    }

    @Override
    protected SearchObjectsOperationResult<RoleAssignment> getOperationResult(SearchResult<RoleAssignment> searchResult)
    {
        return new SearchRoleAssignmentsOperationResult(searchResult);
    }

    @Override
    protected ILocalSearchManager<RoleAssignmentSearchCriteria, RoleAssignment, Long> getSearchManager() {
        throw new RuntimeException("This method is not implemented yet.");
    }

    @Override
    protected Class<? extends SearchObjectsOperation<RoleAssignmentSearchCriteria, RoleAssignmentFetchOptions>> getOperationClass()
    {
        return SearchRoleAssignmentsOperation.class;
    }

}

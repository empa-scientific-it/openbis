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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.project;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ISearchManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ProjectSearchManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ILocalSearchManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.ProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.SearchProjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.SearchProjectsOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.ISearchObjectExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.SearchObjectsPEOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.project.IProjectTranslator;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;

/**
 * @author pkupczyk
 */
@Component
public class SearchProjectsOperationExecutor extends SearchObjectsPEOperationExecutor<Project, ProjectPE, ProjectSearchCriteria, ProjectFetchOptions>
        implements ISearchProjectsOperationExecutor
{

    @Autowired
    private ISearchProjectExecutor searchExecutor;

    @Autowired
    private IProjectTranslator translator;

    @Autowired
    private ProjectSearchManager manager;

    @Override
    protected Class<? extends SearchObjectsOperation<ProjectSearchCriteria, ProjectFetchOptions>> getOperationClass()
    {
        return SearchProjectsOperation.class;
    }

    @Override
    protected ISearchObjectExecutor<ProjectSearchCriteria, ProjectPE> getExecutor()
    {
        return searchExecutor;
    }

    @Override
    protected ITranslator<Long, Project, ProjectFetchOptions> getTranslator()
    {
        return translator;
    }

    @Override
    protected SearchObjectsOperationResult<Project> getOperationResult(SearchResult<Project> searchResult)
    {
        return new SearchProjectsOperationResult(searchResult);
    }

    @Override
    protected ILocalSearchManager<ProjectSearchCriteria, Project, Long> getSearchManager() {
        return manager;
    }

    @Override
    protected SearchObjectsOperationResult<Project> doExecute(final IOperationContext context,
            final SearchObjectsOperation<ProjectSearchCriteria, ProjectFetchOptions> operation)
    {
        return executeDirectSQLSearch(context, operation);
    }

}

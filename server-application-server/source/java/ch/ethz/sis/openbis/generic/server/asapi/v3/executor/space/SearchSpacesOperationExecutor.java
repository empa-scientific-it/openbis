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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.space;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.ProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ISearchManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SpaceSearchManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ILocalSearchManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SearchSpacesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SearchSpacesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.ISearchObjectExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.SearchObjectsPEOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.space.ISpaceTranslator;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
@Component
public class SearchSpacesOperationExecutor extends SearchObjectsPEOperationExecutor<Space, SpacePE, SpaceSearchCriteria, SpaceFetchOptions>
        implements ISearchSpacesOperationExecutor
{

    @Autowired
    private ISearchSpaceExecutor searchExecutor;

    @Autowired
    private ISpaceTranslator translator;

    @Autowired
    private SpaceSearchManager spaceSearchManager;

    @Override
    protected Class<? extends SearchObjectsOperation<SpaceSearchCriteria, SpaceFetchOptions>> getOperationClass()
    {
        return SearchSpacesOperation.class;
    }

    @Override
    protected ISearchObjectExecutor<SpaceSearchCriteria, SpacePE> getExecutor()
    {
        return searchExecutor;
    }

    @Override
    protected ITranslator<Long, Space, SpaceFetchOptions> getTranslator()
    {
        return translator;
    }

    @Override
    protected SearchObjectsOperationResult<Space> getOperationResult(SearchResult<Space> searchResult)
    {
        return new SearchSpacesOperationResult(searchResult);
    }

    @Override
    protected ILocalSearchManager<SpaceSearchCriteria, Space, Long> getSearchManager() {
        return spaceSearchManager;
    }

    @Override
    protected SearchObjectsOperationResult<Space> doExecute(final IOperationContext context,
            final SearchObjectsOperation<SpaceSearchCriteria, SpaceFetchOptions> operation)
    {
        return executeDirectSQLSearch(context, operation);
    }

}

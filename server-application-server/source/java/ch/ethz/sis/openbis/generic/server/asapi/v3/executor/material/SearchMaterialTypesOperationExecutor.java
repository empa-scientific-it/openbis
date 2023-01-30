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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.material;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ILocalSearchManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.MaterialTypeSearchManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.MaterialType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.SearchMaterialTypesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.SearchMaterialTypesOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.ISearchObjectExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.SearchObjectsPEOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.material.IMaterialTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;

/**
 * @author pkupczyk
 */
@Component
public class SearchMaterialTypesOperationExecutor
        extends SearchObjectsPEOperationExecutor<MaterialType, MaterialTypePE, MaterialTypeSearchCriteria, MaterialTypeFetchOptions>
        implements ISearchMaterialTypesOperationExecutor
{

    @Autowired
    private ISearchMaterialTypeExecutor searchExecutor;

    @Autowired
    private IMaterialTypeTranslator translator;

    @Autowired
    private MaterialTypeSearchManager materialTypeSearchManager;

    @Override
    protected Class<? extends SearchObjectsOperation<MaterialTypeSearchCriteria, MaterialTypeFetchOptions>> getOperationClass()
    {
        return SearchMaterialTypesOperation.class;
    }

    @Override
    protected ISearchObjectExecutor<MaterialTypeSearchCriteria, MaterialTypePE> getExecutor()
    {
        return searchExecutor;
    }

    @Override
    protected ITranslator<Long, MaterialType, MaterialTypeFetchOptions> getTranslator()
    {
        return translator;
    }

    @Override
    protected SearchObjectsOperationResult<MaterialType> getOperationResult(SearchResult<MaterialType> searchResult)
    {
        return new SearchMaterialTypesOperationResult(searchResult);
    }

    @Override
    protected SearchObjectsOperationResult<MaterialType> doExecute(final IOperationContext context, final SearchObjectsOperation<MaterialTypeSearchCriteria, MaterialTypeFetchOptions> operation)
    {
        return executeDirectSQLSearch(context, operation);
    }

    @Override
    protected ILocalSearchManager<MaterialTypeSearchCriteria, MaterialType, Long> getSearchManager() {
        return materialTypeSearchManager;
    }

}

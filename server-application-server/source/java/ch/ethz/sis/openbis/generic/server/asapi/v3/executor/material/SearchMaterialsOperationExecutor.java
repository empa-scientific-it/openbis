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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.SearchMaterialsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.SearchMaterialsOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.ISearchObjectExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.SearchObjectsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ILocalSearchManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.MaterialSearchManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.material.IMaterialTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author pkupczyk
 */
@Component
public class SearchMaterialsOperationExecutor extends SearchObjectsOperationExecutor<Material, Long, MaterialSearchCriteria, MaterialFetchOptions>
        implements ISearchMaterialsOperationExecutor
{

    @Autowired
    private ISearchMaterialExecutor searchExecutor;

    @Autowired
    private IMaterialTranslator translator;

    @Autowired
    private MaterialSearchManager materialSearchManager;

    @Override
    protected Class<? extends SearchObjectsOperation<MaterialSearchCriteria, MaterialFetchOptions>> getOperationClass()
    {
        return SearchMaterialsOperation.class;
    }

    @Override
    protected ISearchObjectExecutor<MaterialSearchCriteria, Long> getExecutor()
    {
        return searchExecutor;
    }

    @Override
    protected ITranslator<Long, Material, MaterialFetchOptions> getTranslator()
    {
        return translator;
    }

    @Override
    protected ILocalSearchManager<MaterialSearchCriteria, Material, Long> getSearchManager()
    {
        return materialSearchManager;
    }

    @Override
    protected SearchObjectsOperationResult<Material> getOperationResult(SearchResult<Material> searchResult)
    {
        return new SearchMaterialsOperationResult(searchResult);
    }

    @Override
    protected SearchObjectsOperationResult<Material> doExecute(IOperationContext context, SearchObjectsOperation<MaterialSearchCriteria, MaterialFetchOptions> operation)
    {
        return executeDirectSQLSearch(context, operation);
    }
}

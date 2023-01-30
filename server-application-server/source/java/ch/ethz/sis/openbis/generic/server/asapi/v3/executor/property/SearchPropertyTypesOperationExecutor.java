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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.property;

import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ILocalSearchManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search.PropertyTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search.SearchPropertyTypesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search.SearchPropertyTypesOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.ISearchObjectExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.SearchObjectsPEOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.property.IPropertyTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;

/**
 * @author pkupczyk
 */
@Component
public class SearchPropertyTypesOperationExecutor extends
        SearchObjectsPEOperationExecutor<PropertyType, PropertyTypePE, PropertyTypeSearchCriteria, PropertyTypeFetchOptions>
        implements ISearchPropertyTypesOperationExecutor
{

    @Autowired
    private ISearchPropertyTypeExecutor searchExecutor;

    @Autowired
    private IPropertyTypeTranslator translator;

    @Override
    protected Class<? extends SearchObjectsOperation<PropertyTypeSearchCriteria, PropertyTypeFetchOptions>> getOperationClass()
    {
        return SearchPropertyTypesOperation.class;
    }

    @Override
    protected ISearchObjectExecutor<PropertyTypeSearchCriteria, PropertyTypePE> getExecutor()
    {
        return searchExecutor;
    }

    @Override
    protected ITranslator<Long, PropertyType, PropertyTypeFetchOptions> getTranslator()
    {
        return translator;
    }

    @Override
    protected SearchObjectsOperationResult<PropertyType> getOperationResult(SearchResult<PropertyType> searchResult)
    {
        return new SearchPropertyTypesOperationResult(searchResult);
    }

    @Override
    protected ILocalSearchManager<PropertyTypeSearchCriteria, PropertyType, Long> getSearchManager() {
        throw new RuntimeException("This method is not implemented yet.");
    }

}

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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.objectkindmodification;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ILocalSearchManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.ObjectKindModification;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.fetchoptions.ObjectKindModificationFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.search.ObjectKindModificationSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.search.SearchObjectKindModificationsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.search.SearchObjectKindModificationsOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.AbstractSearchObjectsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.NopTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;

/**
 * @author pkupczyk
 */
@Component
public class SearchObjectKindModificationsOperationExecutor extends
        AbstractSearchObjectsOperationExecutor<ObjectKindModification, ObjectKindModification, ObjectKindModificationSearchCriteria, ObjectKindModificationFetchOptions>
        implements ISearchObjectKindModificationsOperationExecutor
{

    @Autowired
    private ISearchObjectKindModificationExecutor searchExecutor;

    @Override
    protected Class<? extends SearchObjectsOperation<ObjectKindModificationSearchCriteria, ObjectKindModificationFetchOptions>> getOperationClass()
    {
        return SearchObjectKindModificationsOperation.class;
    }

    @Override
    protected List<ObjectKindModification> doSearch(IOperationContext context, ObjectKindModificationSearchCriteria criteria,
            ObjectKindModificationFetchOptions fetchOptions)
    {
        return searchExecutor.search(context, criteria, fetchOptions);
    }

    @Override
    protected Map<ObjectKindModification, ObjectKindModification> doTranslate(TranslationContext translationContext,
            Collection<ObjectKindModification> objects, ObjectKindModificationFetchOptions fetchOptions)
    {
        return new NopTranslator<ObjectKindModification, ObjectKindModificationFetchOptions>().translate(translationContext, objects, fetchOptions);
    }

    @Override
    protected SearchObjectsOperationResult<ObjectKindModification> getOperationResult(SearchResult<ObjectKindModification> searchResult)
    {
        return new SearchObjectKindModificationsOperationResult(searchResult);
    }

    @Override
    protected ILocalSearchManager<ObjectKindModificationSearchCriteria, ObjectKindModification, ObjectKindModification> getSearchManager() {
        throw new RuntimeException("This method is not implemented yet.");
    }

}

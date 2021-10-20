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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search;

import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.ID_COLUMN;

import java.util.*;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ILocalSearchManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;

/**
 * @author pkupczyk
 */
public abstract class SearchObjectsOperationExecutor<OBJECT, OBJECT_PE, CRITERIA extends AbstractSearchCriteria,
        FETCH_OPTIONS extends FetchOptions<OBJECT>> extends AbstractSearchObjectsOperationExecutor<OBJECT, OBJECT_PE, CRITERIA, FETCH_OPTIONS>
{

    protected abstract ISearchObjectExecutor<CRITERIA, OBJECT_PE> getExecutor();

    protected abstract ITranslator<OBJECT_PE, OBJECT, FETCH_OPTIONS> getTranslator();

    @Override
    protected List<OBJECT_PE> doSearch(IOperationContext context, CRITERIA criteria, FETCH_OPTIONS fetchOptions)
    {
        return getExecutor().search(context, criteria);
    }

    @Override
    protected Map<OBJECT_PE, OBJECT> doTranslate(final TranslationContext translationContext, final Collection<OBJECT_PE> objectPes,
            final FETCH_OPTIONS fetchOptions)
    {
        return getTranslator().translate(translationContext, objectPes, fetchOptions);
    }

    @Override
    protected ILocalSearchManager<CRITERIA, OBJECT, OBJECT_PE> getSearchManager() {
        throw new RuntimeException("This method is not implemented yet.");
    }

}

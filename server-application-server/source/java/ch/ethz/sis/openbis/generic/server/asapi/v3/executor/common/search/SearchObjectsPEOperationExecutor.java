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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ILocalSearchManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;

/**
 * @author pkupczyk
 */
public abstract class SearchObjectsPEOperationExecutor<OBJECT, OBJECT_PE extends IIdHolder, CRITERIA extends AbstractSearchCriteria, FETCH_OPTIONS extends FetchOptions<OBJECT>>
        extends AbstractSearchObjectsOperationExecutor<OBJECT, Long, CRITERIA, FETCH_OPTIONS>
{

    protected abstract ISearchObjectExecutor<CRITERIA, OBJECT_PE> getExecutor();

    protected abstract ITranslator<Long, OBJECT, FETCH_OPTIONS> getTranslator();

    @Override
    protected List<Long> doSearch(IOperationContext context, CRITERIA criteria, FETCH_OPTIONS fetchOptions)
    {
        List<OBJECT_PE> objectPEs = getExecutor().search(context, criteria);
        List<Long> ids = new ArrayList<Long>();

        for (OBJECT_PE objectPE : objectPEs)
        {
            ids.add(objectPE.getId());
        }

        return ids;
    }

    @Override
    protected final Map<Long, OBJECT> doTranslate(TranslationContext translationContext, Collection<Long> ids, FETCH_OPTIONS fetchOptions)
    {
        return getTranslator().translate(translationContext, ids, fetchOptions);
    }

    @Override
    protected ILocalSearchManager<CRITERIA, OBJECT, Long> getSearchManager() {
        throw new RuntimeException("This method is not implemented yet.");
    }

}

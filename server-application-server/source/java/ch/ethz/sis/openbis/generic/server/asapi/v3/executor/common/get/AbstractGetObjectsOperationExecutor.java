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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.get;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.get.GetObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.get.GetObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.OperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort.SortAndPage;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;

/**
 * @author pkupczyk
 */
public abstract class AbstractGetObjectsOperationExecutor<OBJECT_ID extends IObjectId, OBJECT_PE, OBJECT, FETCH_OPTIONS extends FetchOptions<?>>
        extends OperationExecutor<GetObjectsOperation<OBJECT_ID, FETCH_OPTIONS>, GetObjectsOperationResult<OBJECT_ID, OBJECT>>
{

    @Override
    protected GetObjectsOperationResult<OBJECT_ID, OBJECT> doExecute(IOperationContext context,
            GetObjectsOperation<OBJECT_ID, FETCH_OPTIONS> operation)
    {
        Map<OBJECT_ID, OBJECT_PE> idToPeMap = map(context, operation.getObjectIds(), operation.getFetchOptions());

        if (idToPeMap == null || idToPeMap.isEmpty())
        {
            return getOperationResult(Collections.<OBJECT_ID, OBJECT> emptyMap());
        }

        TranslationContext translationContext = new TranslationContext(context.getSession());
        Map<OBJECT_PE, OBJECT> peToObjectMap = translate(translationContext, idToPeMap.values(), operation.getFetchOptions());
        Map<OBJECT_ID, OBJECT> idToObjectMap = new LinkedHashMap<OBJECT_ID, OBJECT>();

        for (Map.Entry<OBJECT_ID, OBJECT_PE> entry : idToPeMap.entrySet())
        {
            OBJECT_ID id = entry.getKey();
            OBJECT_PE pe = entry.getValue();
            OBJECT object = peToObjectMap.get(pe);

            if (object != null)
            {
                idToObjectMap.put(id, object);
            }
        }
        
        // sort and page the objects internal collections - ignore the top level changes
        // (we want to maintain all the results and keep them in order of the passed ids)
        new SortAndPage().sortAndPage(idToObjectMap.values(), null, operation.getFetchOptions());

        return getOperationResult(idToObjectMap);
    }

    protected abstract Map<OBJECT_ID, OBJECT_PE> map(IOperationContext context, List<? extends OBJECT_ID> ids, FETCH_OPTIONS fetchOptions);

    protected abstract Map<OBJECT_PE, OBJECT> translate(TranslationContext context, Collection<OBJECT_PE> objects, FETCH_OPTIONS fetchOptions);

    protected abstract GetObjectsOperationResult<OBJECT_ID, OBJECT> getOperationResult(Map<OBJECT_ID, OBJECT> objectMap);

}

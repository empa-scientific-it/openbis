/*
 * Copyright ETH 2016 - 2023 Zürich, Scientific IT Services
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;

/**
 * @author pkupczyk
 */
public abstract class GetObjectsPEOperationExecutor<OBJECT_ID extends IObjectId, OBJECT_PE extends IIdHolder, OBJECT, FETCH_OPTIONS extends FetchOptions<?>>
        extends AbstractGetObjectsOperationExecutor<OBJECT_ID, Long, OBJECT, FETCH_OPTIONS>
{

    protected abstract IMapObjectByIdExecutor<OBJECT_ID, OBJECT_PE> getExecutor();

    protected abstract ITranslator<Long, OBJECT, FETCH_OPTIONS> getTranslator();

    @Override
    protected final Map<OBJECT_ID, Long> map(IOperationContext context, List<? extends OBJECT_ID> ids, FETCH_OPTIONS fetchOptions)
    {
        Map<OBJECT_ID, OBJECT_PE> map = getExecutor().map(context, ids);
        Map<OBJECT_ID, Long> idMap = new LinkedHashMap<OBJECT_ID, Long>();

        for (Map.Entry<OBJECT_ID, OBJECT_PE> entry : map.entrySet())
        {
            idMap.put(entry.getKey(), entry.getValue() != null ? entry.getValue().getId() : null);
        }

        return idMap;
    }

    @Override
    protected final Map<Long, OBJECT> translate(TranslationContext context, Collection<Long> objects, FETCH_OPTIONS fetchOptions)
    {
        return getTranslator().translate(context, objects, fetchOptions);
    }

}

/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.EmptyFetchOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;

/**
 * @author pkupczyk
 */
public abstract class ObjectBaseTranslator<RECORD extends ObjectBaseRecord> extends
        AbstractCachingTranslator<Long, ObjectHolder<RECORD>, EmptyFetchOptions> implements IObjectBaseTranslator<RECORD>
{

    @Override
    protected ObjectHolder<RECORD> createObject(TranslationContext context, Long input, EmptyFetchOptions fetchOptions)
    {
        return new ObjectHolder<RECORD>();
    }

    @Override
    protected Object getObjectsRelations(TranslationContext context, Collection<Long> materialIds, EmptyFetchOptions fetchOptions)
    {
        List<RECORD> records = loadRecords(new LongOpenHashSet(materialIds));

        Map<Long, RECORD> recordMap = new HashMap<Long, RECORD>();
        for (RECORD record : records)
        {
            recordMap.put(record.id, record);
        }

        return recordMap;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void updateObject(TranslationContext context, Long materialId, ObjectHolder<RECORD> result, Object relations,
            EmptyFetchOptions fetchOptions)
    {
        Map<Long, RECORD> recordMap = (Map<Long, RECORD>) relations;
        RECORD record = recordMap.get(materialId);
        result.setObject(record);
    }

    protected abstract List<RECORD> loadRecords(LongOpenHashSet objectIds);

}

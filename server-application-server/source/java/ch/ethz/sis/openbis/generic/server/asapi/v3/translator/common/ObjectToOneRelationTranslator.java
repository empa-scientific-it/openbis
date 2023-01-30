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
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;

/**
 * @author pkupczyk
 */
public abstract class ObjectToOneRelationTranslator<RELATED_OBJECT, RELATED_FETCH_OPTIONS extends FetchOptions<?>> extends
        AbstractCachingTranslator<Long, ObjectHolder<RELATED_OBJECT>, RELATED_FETCH_OPTIONS>
{

    @Override
    protected ObjectHolder<RELATED_OBJECT> createObject(TranslationContext context, Long objectId, RELATED_FETCH_OPTIONS relatedFetchOptions)
    {
        return new ObjectHolder<RELATED_OBJECT>();
    }

    @Override
    protected Object getObjectsRelations(TranslationContext context, Collection<Long> objectIds, RELATED_FETCH_OPTIONS relatedFetchOptions)
    {
        List<ObjectRelationRecord> records = loadRecords(new LongOpenHashSet(objectIds));

        Collection<Long> relatedIds = new HashSet<Long>();
        for (ObjectRelationRecord record : records)
        {
            if (record.relatedId != null)
            {
                relatedIds.add(record.relatedId);
            }
        }

        Map<Long, RELATED_OBJECT> relatedIdToRelated = translateRelated(context, relatedIds, relatedFetchOptions);

        Map<Long, RELATED_OBJECT> objectIdToRelatedMap = new HashMap<Long, RELATED_OBJECT>();
        for (ObjectRelationRecord record : records)
        {
            objectIdToRelatedMap.put(record.objectId, relatedIdToRelated.get(record.relatedId));
        }

        return objectIdToRelatedMap;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void updateObject(TranslationContext context, Long objectId, ObjectHolder<RELATED_OBJECT> result, Object relations,
            RELATED_FETCH_OPTIONS relatedFetchOptions)
    {
        Map<Long, RELATED_OBJECT> objectIdToRelatedMap = (Map<Long, RELATED_OBJECT>) relations;
        result.setObject(objectIdToRelatedMap.get(objectId));
    }

    protected abstract List<ObjectRelationRecord> loadRecords(LongOpenHashSet objectIds);

    protected abstract Map<Long, RELATED_OBJECT> translateRelated(TranslationContext context, Collection<Long> relatedIds,
            RELATED_FETCH_OPTIONS relatedFetchOptions);

}

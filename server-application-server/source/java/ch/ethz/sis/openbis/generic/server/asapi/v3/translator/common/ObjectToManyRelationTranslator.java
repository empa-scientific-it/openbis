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
public abstract class ObjectToManyRelationTranslator<RELATED_OBJECT, RELATED_FETCH_OPTIONS extends FetchOptions<?>> extends
        AbstractCachingTranslator<Long, ObjectHolder<Collection<RELATED_OBJECT>>, RELATED_FETCH_OPTIONS>
{

    @Override
    protected ObjectHolder<Collection<RELATED_OBJECT>> createObject(TranslationContext context, Long objectId,
            RELATED_FETCH_OPTIONS relatedFetchOptions)
    {
        return new ObjectHolder<Collection<RELATED_OBJECT>>();
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

        Map<Long, RELATED_OBJECT> relatedIdToRelated = filterRelatedObjects(translateRelated(context, relatedIds, relatedFetchOptions));
        Map<Long, Collection<RELATED_OBJECT>> objectIdToRelatedMap = new HashMap<Long, Collection<RELATED_OBJECT>>();

        for (ObjectRelationRecord record : records)
        {
            Collection<RELATED_OBJECT> relatedCollection = objectIdToRelatedMap.get(record.objectId);

            if (relatedCollection == null)
            {
                relatedCollection = createCollection();
                objectIdToRelatedMap.put(record.objectId, relatedCollection);
            }

            RELATED_OBJECT relatedObject = relatedIdToRelated.get(record.relatedId);
            if (relatedObject != null)
            {
                relatedCollection.add(relatedObject);
                injectAnnotations(record.objectId, relatedObject, record.annotations, record.relatedAnnotations);
            }
        }

        for (Long objectId : objectIds)
        {
            if (false == objectIdToRelatedMap.containsKey(objectId))
            {
                objectIdToRelatedMap.put(objectId, createCollection());
            }
        }

        return objectIdToRelatedMap;
    }

    protected Map<Long, RELATED_OBJECT> filterRelatedObjects(Map<Long, RELATED_OBJECT> relatedObjects)
    {
        return relatedObjects;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void updateObject(TranslationContext context, Long objectId, ObjectHolder<Collection<RELATED_OBJECT>> result, Object relations,
            RELATED_FETCH_OPTIONS fetchOptions)
    {
        Map<Long, Collection<RELATED_OBJECT>> objectIdToRelatedMap = (Map<Long, Collection<RELATED_OBJECT>>) relations;
        Collection<RELATED_OBJECT> related = objectIdToRelatedMap.get(objectId);
        result.setObject(related);
    }

    protected abstract List<ObjectRelationRecord> loadRecords(LongOpenHashSet objectIds);

    protected abstract Map<Long, RELATED_OBJECT> translateRelated(TranslationContext context, Collection<Long> relatedIds,
            RELATED_FETCH_OPTIONS relatedFetchOptions);

    protected abstract Collection<RELATED_OBJECT> createCollection();

    protected void injectAnnotations(Long objectId, RELATED_OBJECT relatedObject, String annotations, String relatedAnnotations)
    {
    }
    
}

/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.generic.server.api.v3.translator;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author pkupczyk
 */
public abstract class ToManyRelation<OWNER, RELATED_ID, ORIGINAL, TRANSLATED> implements Relation
{

    private Map<OWNER, Collection<TRANSLATED>> translatedMap;

    @Override
    public void load()
    {
        translatedMap = getTranslatedMap();
    }

    private Map<OWNER, Collection<TRANSLATED>> getTranslatedMap()
    {
        Map<OWNER, Collection<ORIGINAL>> ownerToOriginalCollectionMap = getOriginalMap();
        Map<RELATED_ID, ORIGINAL> originalIdToOriginalMap = new HashMap<RELATED_ID, ORIGINAL>();

        // get all original objects without duplicates (duplicates are identified by the same ids)

        for (Collection<ORIGINAL> originalCollection : ownerToOriginalCollectionMap.values())
        {
            for (ORIGINAL original : originalCollection)
            {
                RELATED_ID originalId = getOriginalId(original);
                if (false == originalIdToOriginalMap.containsKey(originalId))
                {
                    originalIdToOriginalMap.put(originalId, original);
                }
            }
        }

        // translate the original objects

        Map<RELATED_ID, TRANSLATED> translatedIdToTranslatedMap = new HashMap<RELATED_ID, TRANSLATED>();
        for (TRANSLATED translated : getTranslatedCollection(originalIdToOriginalMap.values()))
        {
            RELATED_ID translatedId = getTranslatedId(translated);
            translatedIdToTranslatedMap.put(translatedId, translated);
        }

        // create a map from an owner to a translated objects collection

        Map<OWNER, Collection<TRANSLATED>> result = new HashMap<OWNER, Collection<TRANSLATED>>();
        for (Map.Entry<OWNER, Collection<ORIGINAL>> ownerToOriginalCollectionEntry : ownerToOriginalCollectionMap.entrySet())
        {
            OWNER owner = ownerToOriginalCollectionEntry.getKey();
            Collection<ORIGINAL> originalCollection = ownerToOriginalCollectionEntry.getValue();
            Collection<TRANSLATED> translatedCollection = null;

            if (originalCollection instanceof List)
            {
                translatedCollection = new LinkedList<TRANSLATED>();
            } else if (originalCollection instanceof Set)
            {
                translatedCollection = new LinkedHashSet<TRANSLATED>();
            } else
            {
                throw new IllegalArgumentException("Collection of type: " + originalCollection.getClass() + " is not supported.");
            }

            for (ORIGINAL original : originalCollection)
            {
                RELATED_ID originalId = getOriginalId(original);
                TRANSLATED translated = translatedIdToTranslatedMap.get(originalId);
                translatedCollection.add(translated);
            }

            result.put(owner, translatedCollection);
        }

        return result;
    }

    public List<TRANSLATED> getTranslatedList(OWNER owner)
    {
        return (List<TRANSLATED>) translatedMap.get(owner);
    }

    public Set<TRANSLATED> getTranslatedSet(OWNER owner)
    {
        return (Set<TRANSLATED>) translatedMap.get(owner);
    }

    protected abstract Map<OWNER, Collection<ORIGINAL>> getOriginalMap();

    protected abstract Collection<TRANSLATED> getTranslatedCollection(Collection<ORIGINAL> originalCollection);

    protected abstract RELATED_ID getOriginalId(ORIGINAL original);

    protected abstract RELATED_ID getTranslatedId(TRANSLATED translated);

}

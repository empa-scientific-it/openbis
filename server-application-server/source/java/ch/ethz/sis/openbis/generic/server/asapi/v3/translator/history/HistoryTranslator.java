/*
 * Copyright ETH 2015 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.history;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.HistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.PropertyHistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.RelationHistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.fetchoptions.HistoryEntryFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectHolder;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.person.IPersonTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.property.PropertyRecord;

/**
 * @author pkupczyk
 */
public abstract class HistoryTranslator extends AbstractCachingTranslator<Long, ObjectHolder<List<HistoryEntry>>, HistoryEntryFetchOptions>
        implements IHistoryTranslator
{

    protected abstract List<ITranslator<Long, ObjectHolder<List<HistoryEntry>>, HistoryEntryFetchOptions>> getTranslators();

    @Override
    protected ObjectHolder<List<HistoryEntry>> createObject(TranslationContext context, Long entityId, HistoryEntryFetchOptions fetchOptions)
    {
        return new ObjectHolder<List<HistoryEntry>>();
    }

    @Override
    protected Object getObjectsRelations(TranslationContext context, Collection<Long> entityIds, HistoryEntryFetchOptions fetchOptions)
    {
        Map<Long, List<HistoryEntry>> entriesMap = new HashMap<Long, List<HistoryEntry>>();

        for (ITranslator<Long, ObjectHolder<List<HistoryEntry>>, HistoryEntryFetchOptions> translator : getTranslators())
        {
            Map<Long, ObjectHolder<List<HistoryEntry>>> newEntriesMap = translator.translate(context, entityIds, fetchOptions);

            if (newEntriesMap != null)
            {
                for (Long entityId : newEntriesMap.keySet())
                {
                    List<HistoryEntry> entityEntries = entriesMap.get(entityId);
                    List<HistoryEntry> newEntityEntries = newEntriesMap.get(entityId).getObject();

                    if (newEntityEntries != null && !newEntityEntries.isEmpty())
                    {
                        if (entityEntries == null)
                        {
                            entityEntries = new ArrayList<>();
                            entriesMap.put(entityId, entityEntries);
                        }
                        entityEntries.addAll(newEntityEntries);
                    }
                }
            }
        }

        for (Long entityId : entriesMap.keySet())
        {
            entriesMap.putIfAbsent(entityId, Collections.emptyList());
        }

        return entriesMap;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void updateObject(TranslationContext context, Long entityId, ObjectHolder<List<HistoryEntry>> result, Object relations,
            HistoryEntryFetchOptions fetchOptions)
    {
        Map<Long, List<HistoryEntry>> entriesMap = (Map<Long, List<HistoryEntry>>) relations;
        result.setObject(entriesMap.get(entityId));
    }

}

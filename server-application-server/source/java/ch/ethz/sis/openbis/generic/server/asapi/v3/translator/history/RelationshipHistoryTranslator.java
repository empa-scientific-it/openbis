/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.RelationHistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.fetchoptions.HistoryEntryFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectHolder;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.person.IPersonTranslator;

/**
 * @author pkupczyk
 */
public abstract class RelationshipHistoryTranslator
        extends AbstractCachingTranslator<Long, ObjectHolder<List<HistoryEntry>>, HistoryEntryFetchOptions>
{

    protected static final String SPACE_RELATIONSHIP_ENTITY_KIND = "SPACE";

    protected static final String PROJECT_RELATIONSHIP_ENTITY_KIND = "PROJECT";

    protected static final String EXPERIMENT_RELATIONSHIP_ENTITY_KIND = "EXPERIMENT";

    protected static final String SAMPLE_RELATIONSHIP_ENTITY_KIND = "SAMPLE";

    protected static final String DATA_SET_RELATIONSHIP_ENTITY_KIND = "DATA SET";

    @Autowired
    private IPersonTranslator personTranslator;

    protected abstract List<? extends HistoryRelationshipRecord> loadRelationshipHistory(TranslationContext context, Collection<Long> entityIds);

    @Override
    protected ObjectHolder<List<HistoryEntry>> createObject(TranslationContext context, Long entityId, HistoryEntryFetchOptions fetchOptions)
    {
        return new ObjectHolder<List<HistoryEntry>>();
    }

    @Override
    protected Object getObjectsRelations(TranslationContext context, Collection<Long> entityIds, HistoryEntryFetchOptions fetchOptions)
    {
        List<? extends HistoryRelationshipRecord> relationships = loadRelationshipHistory(context, entityIds);

        Map<Long, Person> authorMap = new HashMap<>();

        if (fetchOptions.hasAuthor())
        {
            Set<Long> authorIds = new HashSet<Long>();

            for (HistoryRecord record : relationships)
            {
                if (record.authorId != null)
                {
                    authorIds.add(record.authorId);
                }
            }

            authorMap = personTranslator.translate(context, authorIds, fetchOptions.withAuthor());
        }

        Map<Long, List<HistoryEntry>> entriesMap = new HashMap<Long, List<HistoryEntry>>();

        if (relationships != null)
        {
            createRelationshipEntries(entriesMap, relationships, authorMap, fetchOptions);
        }

        for (Long entityId : entityIds)
        {
            if (false == entriesMap.containsKey(entityId))
            {
                entriesMap.put(entityId, Collections.<HistoryEntry>emptyList());
            }
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

    private void createRelationshipEntries(Map<Long, List<HistoryEntry>> entriesMap, List<? extends HistoryRelationshipRecord> records,
            Map<Long, Person> authorMap, HistoryEntryFetchOptions fetchOptions)
    {
        List<? extends HistoryRelationshipRecord> sortedRecords = new ArrayList<>(records);

        sortedRecords.sort(Comparator.comparing(HistoryRelationshipRecord::getValidFrom)
                .thenComparing(HistoryRelationshipRecord::getValidTo, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(HistoryRelationshipRecord::getRelationType)
                .thenComparing(HistoryRelationshipRecord::getRelatedObjectId)
                .thenComparing(HistoryRelationshipRecord::getId));

        for (HistoryRelationshipRecord record : sortedRecords)
        {
            List<HistoryEntry> entries = entriesMap.get(record.objectId);

            if (entries == null)
            {
                entries = new LinkedList<HistoryEntry>();
                entriesMap.put(record.objectId, entries);
            }

            entries.add(createRelationshipEntry(record, authorMap, fetchOptions));
        }
    }

    protected RelationHistoryEntry createRelationshipEntry(HistoryRelationshipRecord record, Map<Long, Person> authorMap,
            HistoryEntryFetchOptions fetchOptions)
    {
        RelationHistoryEntry entry = new RelationHistoryEntry();
        entry.setFetchOptions(new HistoryEntryFetchOptions());
        entry.setValidFrom(record.validFrom);
        entry.setValidTo(record.validTo);

        if (fetchOptions.hasAuthor())
        {
            entry.setAuthor(authorMap.get(record.authorId));
            entry.getFetchOptions().withAuthorUsing(fetchOptions.withAuthor());
        }

        return entry;
    }

    protected boolean isDataSet(HistoryRelationshipRecord historyRelationshipRecord)
    {
        return isEntityKind(DATA_SET_RELATIONSHIP_ENTITY_KIND, historyRelationshipRecord);
    }

    protected boolean isExperiment(HistoryRelationshipRecord historyRelationshipRecord)
    {
        return isEntityKind(EXPERIMENT_RELATIONSHIP_ENTITY_KIND, historyRelationshipRecord);
    }

    protected boolean isProject(HistoryRelationshipRecord historyRelationshipRecord)
    {
        return isEntityKind(PROJECT_RELATIONSHIP_ENTITY_KIND, historyRelationshipRecord);
    }

    protected boolean isSample(HistoryRelationshipRecord historyRelationshipRecord)
    {
        return isEntityKind(SAMPLE_RELATIONSHIP_ENTITY_KIND, historyRelationshipRecord);
    }

    protected boolean isSpace(HistoryRelationshipRecord historyRelationshipRecord)
    {
        return isEntityKind(SPACE_RELATIONSHIP_ENTITY_KIND, historyRelationshipRecord);
    }

    private boolean isEntityKind(String entityKind, HistoryRelationshipRecord historyRelationshipRecord)
    {
        return entityKind.equals(historyRelationshipRecord.entityKind);
    }
}

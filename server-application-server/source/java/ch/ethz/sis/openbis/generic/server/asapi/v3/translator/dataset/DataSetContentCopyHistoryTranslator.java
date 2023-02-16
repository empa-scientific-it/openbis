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
package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.dataset;

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
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.ContentCopyHistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.HistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.fetchoptions.HistoryEntryFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectHolder;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.history.HistoryContentCopyRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.history.HistoryRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.person.IPersonTranslator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.lemnik.eodsql.QueryTool;

/**
 * @author pkupczyk
 */
@Component
public class DataSetContentCopyHistoryTranslator
        extends AbstractCachingTranslator<Long, ObjectHolder<List<HistoryEntry>>, HistoryEntryFetchOptions>
{

    @Autowired
    private IPersonTranslator personTranslator;

    @Override
    protected ObjectHolder<List<HistoryEntry>> createObject(TranslationContext context, Long entityId, HistoryEntryFetchOptions fetchOptions)
    {
        return new ObjectHolder<List<HistoryEntry>>();
    }

    @Override
    protected Object getObjectsRelations(TranslationContext context, Collection<Long> entityIds, HistoryEntryFetchOptions fetchOptions)
    {
        List<? extends HistoryRecord> contentCopyRecords = loadContentCopyHistory(context, entityIds);

        Map<Long, Person> authorMap = new HashMap<>();

        if (fetchOptions.hasAuthor())
        {
            Set<Long> authorIds = new HashSet<Long>();

            for (HistoryRecord record : contentCopyRecords)
            {
                if (record.authorId != null)
                {
                    authorIds.add(record.authorId);
                }
            }
            authorMap = personTranslator.translate(context, authorIds, fetchOptions.withAuthor());
        }

        Map<Long, List<HistoryEntry>> entriesMap = new HashMap<Long, List<HistoryEntry>>();

        createContentCopyEntries(entriesMap, contentCopyRecords, authorMap, fetchOptions);

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

    private List<? extends HistoryRecord> loadContentCopyHistory(TranslationContext context, Collection<Long> entityIds)
    {
        DataSetQuery query = QueryTool.getManagedQuery(DataSetQuery.class);
        return query.getContentCopyHistory(new LongOpenHashSet(entityIds));
    }

    private void createContentCopyEntries(Map<Long, List<HistoryEntry>> entriesMap, List<? extends HistoryRecord> records,
            Map<Long, Person> authorMap, HistoryEntryFetchOptions fetchOptions)
    {
        List<? extends HistoryRecord> sortedRecords = new ArrayList<>(records);

        sortedRecords.sort(Comparator.comparing(HistoryRecord::getValidFrom)
                .thenComparing(HistoryRecord::getValidTo, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(HistoryRecord::getId));

        for (HistoryRecord record : sortedRecords)
        {
            HistoryContentCopyRecord contentCopyRecord = (HistoryContentCopyRecord) record;
            List<HistoryEntry> entries = entriesMap.get(contentCopyRecord.dataSetId);

            if (entries == null)
            {
                entries = new LinkedList<HistoryEntry>();
                entriesMap.put(contentCopyRecord.dataSetId, entries);
            }

            entries.add(createContentCopyEntry(record, authorMap, fetchOptions));
        }
    }

    private HistoryEntry createContentCopyEntry(HistoryRecord record, Map<Long, Person> authorMap, HistoryEntryFetchOptions fetchOptions)
    {
        HistoryContentCopyRecord contentCopyRecord = (HistoryContentCopyRecord) record;
        ContentCopyHistoryEntry entry = new ContentCopyHistoryEntry();
        entry.setFetchOptions(new HistoryEntryFetchOptions());
        entry.setExternalCode(contentCopyRecord.externalCode);
        entry.setPath(contentCopyRecord.path);
        entry.setGitCommitHash(contentCopyRecord.gitCommitHash);
        entry.setGitRepositoryId(contentCopyRecord.gitRepositoryId);
        entry.setExternalDmsId(contentCopyRecord.externalDmsId);
        entry.setExternalDmsCode(contentCopyRecord.externalDmsCode);
        entry.setExternalDmsLabel(contentCopyRecord.externalDmsLabel);
        entry.setExternalDmsAddress(contentCopyRecord.externalDmsAddress);
        entry.setValidFrom(contentCopyRecord.validFrom);
        entry.setValidTo(contentCopyRecord.validTo);
        if (fetchOptions.hasAuthor())
        {
            entry.setAuthor(authorMap.get(record.authorId));
            entry.getFetchOptions().withAuthorUsing(fetchOptions.withAuthor());
        }
        return entry;
    }

}

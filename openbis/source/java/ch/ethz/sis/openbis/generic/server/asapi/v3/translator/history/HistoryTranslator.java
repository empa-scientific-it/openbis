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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.history;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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

    @Autowired
    private IPersonTranslator personTranslator;

    protected abstract List<? extends PropertyRecord> loadProperties(Collection<Long> entityIds);

    protected abstract List<? extends HistoryPropertyRecord> loadPropertyHistory(Collection<Long> entityIds);

    protected abstract List<? extends HistoryRelationshipRecord> loadRelationshipHistory(TranslationContext context, Collection<Long> entityIds);

    protected List<? extends HistoryRecord> loadAbritraryHistory(TranslationContext context, Collection<Long> entityIds)
    {
        return null;
    }

    @Override
    protected ObjectHolder<List<HistoryEntry>> createObject(TranslationContext context, Long entityId, HistoryEntryFetchOptions fetchOptions)
    {
        return new ObjectHolder<List<HistoryEntry>>();
    }

    @Override
    protected Object getObjectsRelations(TranslationContext context, Collection<Long> entityIds, HistoryEntryFetchOptions fetchOptions)
    {
        List<? extends HistoryPropertyRecord> oldProperties = loadPropertyHistory(entityIds);
        List<? extends PropertyRecord> currentProperties = loadProperties(entityIds);

        List<HistoryPropertyRecord> properties = new ArrayList<>();

        if (oldProperties != null)
        {
            properties.addAll(oldProperties);
        }

        if (currentProperties != null)
        {
            for (PropertyRecord currentProperty : currentProperties)
            {
                HistoryPropertyRecord currentPropertyRecord = new HistoryPropertyRecord();
                currentPropertyRecord.authorId = currentProperty.authorId;
                currentPropertyRecord.validFrom = currentProperty.modificationTimestamp;
                currentPropertyRecord.validTo = null;
                currentPropertyRecord.objectId = currentProperty.objectId;
                currentPropertyRecord.propertyCode = currentProperty.propertyCode;
                currentPropertyRecord.propertyValue = currentProperty.propertyValue;
                currentPropertyRecord.samplePropertyValue = currentProperty.sample_perm_id;

                if (currentProperty.vocabularyPropertyValue != null && currentProperty.vocabularyPropertyValueTypeCode != null)
                {
                    currentPropertyRecord.vocabularyPropertyValue =
                            currentProperty.vocabularyPropertyValue + " [" + currentProperty.vocabularyPropertyValueTypeCode + "]";
                }

                if (currentProperty.materialPropertyValueCode != null && currentProperty.materialPropertyValueTypeCode != null)
                {
                    currentPropertyRecord.materialPropertyValue =
                            currentProperty.materialPropertyValueCode + " [" + currentProperty.materialPropertyValueTypeCode + "]";
                }

                properties.add(currentPropertyRecord);
            }
        }

        List<? extends HistoryRelationshipRecord> relationships = loadRelationshipHistory(context, entityIds);
        List<? extends HistoryRecord> arbitraryRecords = loadAbritraryHistory(context, entityIds);

        Map<Long, Person> authorMap = new HashMap<>();

        if (fetchOptions.hasAuthor())
        {
            Set<Long> authorIds = new HashSet<Long>();
            List<HistoryRecord> completeHistory = new ArrayList<>();

            if (properties != null)
            {
                completeHistory.addAll(properties);
            }
            if (relationships != null)
            {
                completeHistory.addAll(relationships);
            }
            if (arbitraryRecords != null)
            {
                completeHistory.addAll(arbitraryRecords);
            }

            for (HistoryRecord record : completeHistory)
            {
                if (record.authorId != null)
                {
                    authorIds.add(record.authorId);
                }
            }
            authorMap = personTranslator.translate(context, authorIds, fetchOptions.withAuthor());
        }

        Map<Long, List<HistoryEntry>> entriesMap = new HashMap<Long, List<HistoryEntry>>();

        if (properties != null)
        {
            createPropertyEntries(entriesMap, properties, authorMap, fetchOptions);
        }
        if (relationships != null)
        {
            createRelationshipEntries(entriesMap, relationships, authorMap, fetchOptions);
        }
        if (arbitraryRecords != null)
        {
            createArbitraryEntries(entriesMap, arbitraryRecords, authorMap, fetchOptions);
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

    private void createPropertyEntries(Map<Long, List<HistoryEntry>> entriesMap, List<? extends HistoryPropertyRecord> records,
            Map<Long, Person> authorMap,
            HistoryEntryFetchOptions fetchOptions)
    {
        for (HistoryPropertyRecord record : records)
        {
            List<HistoryEntry> entries = entriesMap.get(record.objectId);

            if (entries == null)
            {
                entries = new LinkedList<HistoryEntry>();
                entriesMap.put(record.objectId, entries);
            }

            entries.add(createPropertyEntry(record, authorMap, fetchOptions));
        }
    }

    protected PropertyHistoryEntry createPropertyEntry(HistoryPropertyRecord record, Map<Long, Person> authorMap,
            HistoryEntryFetchOptions fetchOptions)
    {
        PropertyHistoryEntry entry = new PropertyHistoryEntry();
        entry.setFetchOptions(new HistoryEntryFetchOptions());
        entry.setValidFrom(record.validFrom);
        entry.setValidTo(record.validTo);
        entry.setPropertyName(record.propertyCode);

        if (record.propertyValue != null)
        {
            entry.setPropertyValue(record.propertyValue);
        } else if (record.vocabularyPropertyValue != null)
        {
            entry.setPropertyValue(record.vocabularyPropertyValue);
        } else if (record.materialPropertyValue != null)
        {
            entry.setPropertyValue(record.materialPropertyValue);
        } else if (record.samplePropertyValue != null)
        {
            entry.setPropertyValue(record.samplePropertyValue);
        } else
        {
            throw new IllegalArgumentException("Unexpected property history entry with all values null");
        }

        if (fetchOptions.hasAuthor())
        {
            entry.setAuthor(authorMap.get(record.authorId));
            entry.getFetchOptions().withAuthorUsing(fetchOptions.withAuthor());
        }
        return entry;
    }

    protected void createArbitraryEntries(Map<Long, List<HistoryEntry>> entriesMap, List<? extends HistoryRecord> records,
            Map<Long, Person> authorMap, HistoryEntryFetchOptions fetchOptions)
    {
    }

    private void createRelationshipEntries(Map<Long, List<HistoryEntry>> entriesMap, List<? extends HistoryRelationshipRecord> records,
            Map<Long, Person> authorMap, HistoryEntryFetchOptions fetchOptions)
    {
        for (HistoryRelationshipRecord record : records)
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
        return isEntityKind("DATA SET", historyRelationshipRecord);
    }

    protected boolean isExperiment(HistoryRelationshipRecord historyRelationshipRecord)
    {
        return isEntityKind("EXPERIMENT", historyRelationshipRecord);
    }

    protected boolean isProject(HistoryRelationshipRecord historyRelationshipRecord)
    {
        return isEntityKind("PROJECT", historyRelationshipRecord);
    }

    protected boolean isSample(HistoryRelationshipRecord historyRelationshipRecord)
    {
        return isEntityKind("SAMPLE", historyRelationshipRecord);
    }

    protected boolean isSpace(HistoryRelationshipRecord historyRelationshipRecord)
    {
        return isEntityKind("SPACE", historyRelationshipRecord);
    }

    private boolean isEntityKind(String entityKind, HistoryRelationshipRecord historyRelationshipRecord)
    {
        return entityKind.equals(historyRelationshipRecord.entityKind);
    }
}
